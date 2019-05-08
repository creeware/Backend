package util;

public class Path {

    public class Web {
        public static final String WEBHOOK = "/hook_payload";
        public static final String REPOSITORIES = "/repositories";
        public static final String ORGANIZATIONS = "/organizations";
        public static final String CANVAS = "/canvas";
        public static final String USERS = "/users";
        public static final String API = "/api";
        public static final String STATISTICS = "/statistics";
    }

    public class Canvas{
        public static final String COURSES = "/courses";
        public static final String STUDENTS = "/students";
        public static final String ASSIGNMENT = "/assignment";
    }

    public class Auth {
        public static final String GITHUB = "auth/github";
    }
}
