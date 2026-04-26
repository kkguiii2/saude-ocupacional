import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.Statement;

public class DropTable {
    public static void main(String[] args) throws Exception {
        Connection conn = DriverManager.getConnection("jdbc:postgresql://localhost:5432/saude_ocupacional", "postgres", "micro123");
        Statement stmt = conn.createStatement();
        stmt.execute("DROP TABLE IF EXISTS flyway_schema_history");
        conn.close();
        System.out.println("Tabela flyway_schema_history apagada!");
    }
}
