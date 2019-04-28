package util;

import io.github.cdimascio.dotenv.Dotenv;
import org.hibernate.SessionFactory;
import org.hibernate.cfg.Configuration;

public class HibernateUtil {
    private static SessionFactory sessionFactory;

    public static SessionFactory getSessionFactory() {
        if (sessionFactory == null) {
                Dotenv dotenv = Dotenv.configure().ignoreIfMissing().load();
                Configuration cfg = new Configuration().configure("hibernate.cfg.xml")
                                        .setProperty("hibernate.connection.url", dotenv.get("JDBC_DATABASE_URL"))
                                        .setProperty("hibernate.connection.username", dotenv.get("JDBC_DATABASE_USERNAME") )
                                        .setProperty("hibernate.connection.password",dotenv.get("JDBC_DATABASE_PASSWORD"));

                // Create SessionFactory
                sessionFactory = cfg.buildSessionFactory();
        }
        return sessionFactory;
    }

    public static void shutdown() {
        if (sessionFactory != null) {
            sessionFactory.close();
        }
    }
}