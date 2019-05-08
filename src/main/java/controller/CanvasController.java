package controller;

import canvas.CanvasManager;
import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import edu.ksu.canvas.model.Course;
import edu.ksu.canvas.model.assignment.Assignment;
import model.CanvasCourse;
import model.CanvasStudent;
import model.User;
import spark.Request;
import spark.Response;

import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.UUID;

import static canvas.CanvasManager.*;

public class CanvasController {
    public static List<CanvasCourse> getCanvasCourses(Request request, Response response) throws IOException {
        UUID uuid = UUID.fromString(request.params(":uuid"));
        User user = User.getUser(uuid);
        List<Course> courses  = getUserCanvasCourses(user);
        List<CanvasCourse> coursesJson = new ArrayList<CanvasCourse>();
        for(Course course:courses){
            coursesJson.add(new CanvasCourse(course.getName(), course.getId()));
        }

        return coursesJson;
    }

    public static List<CanvasStudent> getCanvasStudents(Request request, Response response) throws IOException {
        UUID uuid = UUID.fromString(request.queryParams("user_uuid"));
        String course_uuid = request.queryParams("course_uuid");
        User user = User.getUser(uuid);
        List<edu.ksu.canvas.model.User> students  = CanvasManager.getCanvasStudents(user, course_uuid);
        List<CanvasStudent> studentsJson = new ArrayList<CanvasStudent>();
        for(edu.ksu.canvas.model.User student:students){
            studentsJson.add(new CanvasStudent(student.getName(), student.getId()));
        }

        return studentsJson;
    }

    public static String createCanvasRepository(Request request, Response response) throws IOException, ParseException {
        Gson googleJson = new Gson();
        JsonParser jsonParser = new JsonParser();
        JsonObject payload = jsonParser.parse(request.body()).getAsJsonObject();

        User user = User.getUser(UUID.fromString(payload.get("user_uuid").getAsString()));
        String canvas_course_uuid = payload.get("canvas_course_uuid").getAsString();
        String assignment_description = payload.get("assignment_description").getAsString();
        String assignment_name = payload.get("assignment_name").getAsString();
        Date release_date = new SimpleDateFormat("yyyy-MM-dd").parse(payload.get("release_date").getAsString());
        String repository_name = payload.get("repository_name").getAsString();
        String organization_name = payload.get("organization_name").getAsString();
        String assignment_due_date = payload.get("assignment_due_date").getAsString();
        String solution_repo_url = payload.get("solution_repo_url").getAsString();
        String[] user_canvas_ids = googleJson.fromJson(payload.get("user_canvas_ids").getAsJsonArray(), String[].class);
        Assignment assignment = createAssignment(user, canvas_course_uuid, assignment_description, assignment_name, assignment_due_date);

        CanvasManager.createCanvasRepository(user, user.getAccess_token(), organization_name, user_canvas_ids, user.getUsername(), repository_name, solution_repo_url, release_date, canvas_course_uuid, assignment.getId().toString());
        response.status(200);
        return "OK";
    }

    public static String integrateUser(Request request, Response response) throws IOException {
        JsonParser jsonParser = new JsonParser();
        JsonObject payload = jsonParser.parse(request.body()).getAsJsonObject();

        UUID uuid = UUID.fromString(payload.get("user_uuid").getAsString());
        String canvas_access_token = payload.get("canvas_course_uuid").getAsString();
        String canvas_base_url = payload.get("assignment_description").getAsString();
        integrateCanvasUser(uuid, canvas_access_token, canvas_base_url);
        response.status(200);
        return "OK";
    }

    public static String migrateUser(Request request, Response response) throws IOException {
        JsonParser jsonParser = new JsonParser();
        JsonObject payload = jsonParser.parse(request.body()).getAsJsonObject();

        UUID user_uuid = UUID.fromString(payload.get("user_uuid").getAsString());
        UUID old_user_uuid = UUID.fromString(payload.get("old_user_uuid").getAsString());
        migrateCanvasUser(user_uuid, old_user_uuid);

        response.status(200);
        return "OK";
    }




}
