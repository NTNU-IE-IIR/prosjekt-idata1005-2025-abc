package DbContext;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

public class DataHandler {
    
    public static void main(String[] args) {
        // JDBC connection details
        String url = "jdbc:mysql://mysql.stud.ntnu.no:3306/thomabol_homedatabase?allowPublicKeyRetrieval=true&useSSL=false";
        String user = "thomabol_homeUser";
        String password = "abc123"; // Replace with actual password


        String query = "SELECT * FROM tasks";

        // Try-with-resources to auto-close connections
        try (Connection conn = DriverManager.getConnection(url, user, password);
             Statement stmt = conn.createStatement();
             ResultSet resultSet = stmt.executeQuery(query)) {

            System.out.println("✅ Connected to MySQL. Retrieving tasks...");

            // Get column count dynamically
            int columnCount = resultSet.getMetaData().getColumnCount();

            // Print results
            while (resultSet.next()) {
                for (int i = 1; i <= columnCount; i++) {
                    System.out.print(resultSet.getString(i) + "  |  ");
                }
                System.out.println(); // New line after each row
            }

        } catch (SQLException e) {
            System.err.println("❌ Database error: " + e.getMessage());
        }
    }
}
