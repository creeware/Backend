import io.github.cdimascio.dotenv.Dotenv;
import org.flywaydb.core.Flyway;

public class Migrations {
    public static void main(String[] args) throws Exception {
        Dotenv dotenv = Dotenv.configure().ignoreIfMissing().load();
        Flyway flyway = new Flyway();
        flyway.setDataSource(dotenv.get("JDBC_DATABASE_URL"),
                dotenv.get("JDBC_DATABASE_USERNAME"),
                dotenv.get("JDBC_DATABASE_PASSWORD"));
        flyway.migrate();
    }
}
