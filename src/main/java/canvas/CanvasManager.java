package canvas;

import edu.ksu.canvas.CanvasApiFactory;
import edu.ksu.canvas.impl.*;
import edu.ksu.canvas.interfaces.AccountReader;
import edu.ksu.canvas.interfaces.AssignmentWriter;
import edu.ksu.canvas.interfaces.UserReader;
import edu.ksu.canvas.model.Account;
import edu.ksu.canvas.model.Course;
import edu.ksu.canvas.model.Progress;
import edu.ksu.canvas.model.User;
import edu.ksu.canvas.model.assignment.Assignment;
import edu.ksu.canvas.net.RestClient;
import edu.ksu.canvas.net.SimpleRestClient;
import edu.ksu.canvas.oauth.NonRefreshableOauthToken;
import edu.ksu.canvas.oauth.OauthToken;
import edu.ksu.canvas.requestOptions.*;
import lombok.Data;
import model.Organization;
import org.apache.log4j.Logger;
import org.eclipse.egit.github.core.Repository;
import org.eclipse.egit.github.core.RepositoryHook;
import org.eclipse.egit.github.core.service.RepositoryService;
import org.eclipse.jetty.util.log.Log;
import org.hibernate.Session;
import org.hibernate.Transaction;
import util.HibernateUtil;

import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;

import static github.GithubManager.*;
import static model.Repository.getCanvasRepositories;
import static model.User.getCanvasUser;
import static model.User.getUser;

@Data
public class CanvasManager {
    private static final Logger LOGGER = Logger.getLogger(CanvasManager.class);
    public static Assignment createAssignment(model.User user,
                                              String courseId,
                                              String description,
                                              String name,
                                              String assignment_due_date
    ) throws IOException, ParseException {
        Assignment assignment = new Assignment();
        assignment.setCourseId(courseId);
        assignment.setDescription(description);
        assignment.setName(name);
        assignment.setDueAt(new SimpleDateFormat("yyyy-MM-dd").parse(assignment_due_date));
        assignment.setGradingType("pass_fail");
        assignment.setPublished(true);
        System.out.println(user.getUsername());
        String canvasBaseUrl = (user.getCanvas_base_url());
        OauthToken oauthToken = new NonRefreshableOauthToken(user.getCanvas_access_token());
        SimpleRestClient client = new SimpleRestClient();
        AssignmentImpl assignmentImpl = new AssignmentImpl(canvasBaseUrl, 1, oauthToken, client, 5000, 5000, 1000, false);
        return assignmentImpl.createAssignment(courseId, assignment).get();
    }

    public static void gradeAssignment(model.User user,
                                       String courseId,
                                       String assignmentId,
                                       String userId,
                                       String grade
    ) throws IOException {
        String canvasBaseUrl = (user.getCanvas_base_url());
        OauthToken oauthToken = new NonRefreshableOauthToken(user.getCanvas_access_token());
        SimpleRestClient client = new SimpleRestClient();
        SubmissionImpl submissionWriter = new SubmissionImpl(canvasBaseUrl, 1, oauthToken, client, 5000, 5000, 1000, false);

        MultipleSubmissionsOptions multipleSubmissionsOptions = new MultipleSubmissionsOptions(courseId, Integer.parseInt(assignmentId), null);
        Map<String, MultipleSubmissionsOptions.StudentSubmissionOption> map = new HashMap<>();
        MultipleSubmissionsOptions.StudentSubmissionOption studentSubmissionOption = multipleSubmissionsOptions.createStudentSubmissionOption("test-grade", grade, null, null, null, null);
        map.put(userId, studentSubmissionOption);
        multipleSubmissionsOptions.setStudentSubmissionOptionMap(map);

        submissionWriter.gradeMultipleSubmissionsByCourse(multipleSubmissionsOptions);
    }

    public static List<Course> getUserCanvasCourses(model.User user) throws IOException {
        String canvasBaseUrl = (user.getCanvas_base_url());
        OauthToken oauthToken = new NonRefreshableOauthToken(user.getCanvas_access_token());
        SimpleRestClient client = new SimpleRestClient();
        CourseImpl courseImpl = new CourseImpl(canvasBaseUrl, 1, oauthToken, client, 5000, 5000, 1000, false);
        ListUserCoursesOptions options = new ListUserCoursesOptions(user.getCanvas_user_uuid());

        return courseImpl.listUserCourses(options);
    }

    public static List<User> getCanvasStudents(model.User user,
                                               String courseId) throws IOException {
        String canvasBaseUrl = (user.getCanvas_base_url());
        OauthToken oauthToken = new NonRefreshableOauthToken(user.getCanvas_access_token());
        CanvasApiFactory apiFactory = new CanvasApiFactory(canvasBaseUrl);
        GetUsersInCourseOptions options =
                new GetUsersInCourseOptions(courseId)
                        .enrollmentType(Arrays.asList(GetUsersInCourseOptions.EnrollmentType.STUDENT));
        UserReader userReader = apiFactory.getReader(UserReader.class, oauthToken, 1000);
        return userReader.getUsersInCourse(options);
    }

    public static void sendMessage(model.User user, String userId, String message) throws IOException {
        String canvasBaseUrl = (user.getCanvas_base_url());
        OauthToken oauthToken = new NonRefreshableOauthToken(user.getCanvas_access_token());
        SimpleRestClient client = new SimpleRestClient();
        ConversationImpl conversationImpl = new ConversationImpl(canvasBaseUrl, 1, oauthToken, client, 5000, 5000, 1000, false);
        CreateConversationOptions options = new CreateConversationOptions(userId, message);

        conversationImpl.createConversation(options);
    }

    public static String createCanvasRepository(model.User user, String access_token, String org_name, String[] user_canvas_ids, String admin_user_name, String template_repo_name, String solution_repo_url, Date release_date, String canvas_course_uuid, String canvas_assignment_uuid) throws IOException {
        RepositoryService service = new RepositoryService();
        service.getClient().setOAuth2Token(access_token);
        for (String user_canvas_id : user_canvas_ids) {
            String repo_name = user_canvas_id + "-" + UUID.randomUUID().toString();
            RepositoryHook hook = createHook();
            Repository repository = new Repository();
            repository.setName(repo_name);
            model.User canvas_student = getCanvasUser(user_canvas_id);
            if (canvas_student == null) {
                canvas_student = model.User.createCanvasUser(user_canvas_id);
                sendMessage(user, user_canvas_id, "Hello, You have just received a new assignment through creeware.com. " +
                        "Please go to creeware.com and login with your GitHub account to be able to use the given secret key on the get access process in the Dashboard." +
                        "\n Secret Key: " + canvas_student.getUser_uuid());
            }
            repository = service.createRepository(org_name, repository);
            if (new SimpleDateFormat("yyyy-MM-dd").format(release_date).equals(new SimpleDateFormat("yyyy-MM-dd").format(new Date()))) {
                if (canvas_student.getUsername() != null) {
                    inviteMember(access_token, repository, canvas_student.getUsername());
                }
            }
            clone_and_push(access_token, org_name, repo_name, template_repo_name);
            service.createHook(repository, hook);
            putCanvasRepositoryToDb(repository, canvas_student.getCanvas_user_uuid(), admin_user_name, org_name, solution_repo_url, release_date, canvas_course_uuid, canvas_assignment_uuid);

        }
        return "success";
    }


    public static void putCanvasRepositoryToDb(Repository repository, String canvas_user_uuid, String admin_user_name, String org_name, String solution_repo_url, Date release_date, String canvas_course_uuid, String canvas_assignment_uuid) {
        Session session = HibernateUtil.getSessionFactory().openSession();
        model.User user = model.User.getCanvasUser(canvas_user_uuid);
        model.User admin = model.User.getUser(admin_user_name, "GitHubClient");
        Organization organization = Organization.getOrganization(org_name);
        model.Repository newRepository = new model.Repository();
        newRepository.setRepository_uuid(UUID.randomUUID());
        newRepository.setUser_uuid(user.getUser_uuid());
        newRepository.setCanvas_assignment_uuid(canvas_assignment_uuid);
        newRepository.setCanvas_course_uuid(canvas_course_uuid);
        newRepository.setCanvas_student_uuid(user.getCanvas_user_uuid());
        newRepository.setOrganization_uuid(organization.getOrganization_uuid());
        newRepository.setRepository_name(repository.getName());
        newRepository.setRepository_description(repository.getDescription());
        newRepository.setRepository_visibility(Boolean.toString(!repository.isPrivate()));
        newRepository.setRepository_git_url(repository.getCloneUrl());
        newRepository.setRepository_github_type(repository.getDefaultBranch());
        newRepository.setRepository_type("challenge");
        if (new SimpleDateFormat("yyyy-MM-dd").format(release_date).equals(new SimpleDateFormat("yyyy-MM-dd").format(new Date()))) {
            newRepository.setRepository_status("unreleased");
        } else {
            newRepository.setRepository_status("unsolved");
        }
        newRepository.setSolution_repository_git_url(solution_repo_url);
        newRepository.setRepository_admin_uuid(admin.getUser_uuid());
        newRepository.setRelease_date(release_date);
        newRepository.setCreated_at(new Date());

        Transaction transaction = session.beginTransaction();
        session.persist(newRepository);
        transaction.commit();

        try {

        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            session.close();
        }
    }

    public static void integrateCanvasUser(UUID user_uuid, String canvas_access_token, String canvas_base_url) throws IOException {
        model.User user = getUser(user_uuid);
        User canvasUser = getUserFromCanvas(user.getCanvas_access_token(), user.getCanvas_base_url());
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            user.setCanvas_access_token(canvas_access_token);
            user.setCanvas_base_url(canvas_base_url);
            user.setCanvas_user_uuid(Integer.toString(canvasUser.getId()));
            Transaction transaction = session.beginTransaction();
            session.merge(user);
            transaction.commit();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static User getUserFromCanvas(String canvas_access_token, String canvas_base_url) throws IOException {
        OauthToken oauthToken = new NonRefreshableOauthToken(canvas_access_token);
        CanvasApiFactory apiFactory = new CanvasApiFactory(canvas_base_url);
        SimpleRestClient client = new SimpleRestClient();
        UserImpl userImpl = new UserImpl(canvas_base_url, 1, oauthToken, client, 5000, 5000, 1000, false);


        return userImpl.showUserDetails("self").get();
    }

    public static void migrateCanvasUser(UUID user_uuid, UUID old_user_uuid) {
        model.User oldUser = getUser(old_user_uuid);
        model.User user = getUser(user_uuid);
        String canvas_student_uuid = oldUser.getCanvas_user_uuid();
        Session session = HibernateUtil.getSessionFactory().openSession();
        try {
            user.setCanvas_user_uuid(oldUser.getCanvas_user_uuid());
            Transaction transaction = session.beginTransaction();
            session.merge(user);
            transaction.commit();
            migrateCanvasRepositories(user_uuid, old_user_uuid, canvas_student_uuid);
            transaction = session.beginTransaction();
            session.delete(oldUser);
            transaction.commit();
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            session.close();
        }
    }

    public static void migrateCanvasRepositories(UUID user_uuid, UUID old_user_uuid, String canvas_student_uuid) {
        List<model.Repository> repositories = getCanvasRepositories(old_user_uuid, canvas_student_uuid);
        model.User user = model.User.getUser(user_uuid);
        Session session = HibernateUtil.getSessionFactory().openSession();
        try {
            for (model.Repository repository : repositories) {
                model.User admin = model.User.getUser(repository.getRepository_admin_uuid());
                RepositoryService service = new RepositoryService();
                service.getClient().setOAuth2Token(admin.getAccess_token());
                Organization organization = Organization.getOrganization(repository.getOrganization_uuid());
                org.eclipse.egit.github.core.Repository githubRepository = service.getRepository(organization.getOrganization_name(), repository.getRepository_name());
                repository.setUser_uuid(user_uuid);
                Transaction transaction = session.beginTransaction();
                session.merge(repository);
                transaction.commit();
                if (new SimpleDateFormat("yyyy-MM-dd").format(repository.getRelease_date()).equals(new SimpleDateFormat("yyyy-MM-dd").format(new Date()))) {
                    inviteMember(admin.getAccess_token(), githubRepository,  user.getUsername());
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            session.close();
        }
    }

    public static void main(String[] args) throws IOException {

        String canvasBaseUrl = "https://kth.instructure.com";
        OauthToken oauthToken = new NonRefreshableOauthToken("8779~5e9GvSjkrrCVlXV793f4Lyf9p6qVjkyy3zrz7bVvr1948s36LnFNBe2DgkVbnfSR");
        CanvasApiFactory apiFactory = new CanvasApiFactory(canvasBaseUrl);
        SimpleRestClient client = new SimpleRestClient();

        integrateCanvasUser(UUID.fromString("baca7d72-d06f-43d9-b40a-1efbcafc772a"), "8779~5e9GvSjkrrCVlXV793f4Lyf9p6qVjkyy3zrz7bVvr1948s36LnFNBe2DgkVbnfSR", canvasBaseUrl);

    }
}
