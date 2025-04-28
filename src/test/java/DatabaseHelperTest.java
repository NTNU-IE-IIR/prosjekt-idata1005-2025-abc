// Language: java
package dbcontext;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.List;

/**
 * Test class for {@link DatabaseHelper} functionality.
 *
 * <p>
 * These tests use an H2 in‑memory database to verify the correct behavior of
 * {@code executeSelect}, {@code executeUpdate}, and {@code executeUpdateGeneratedKeys}
 * methods of {@link DatabaseHelper}. The tests also include mapping SQL query results to a DTO.
 * </p>
 */
public class DatabaseHelperTest {

    private static Connection connection;
    private static DatabaseHelper dbHelper;

    /**
     * Sets up an in‑memory database and initializes {@link DatabaseHelper} before all tests.
     *
     * @throws SQLException if a database access error occurs
     */
    @BeforeAll
    public static void setup() throws SQLException {
        // Use H2 in‑memory database
        String url = "jdbc:h2:mem:test;DB_CLOSE_DELAY=-1";
        connection = DriverManager.getConnection(url, "sa", "");
        Statement stmt = connection.createStatement();
        // Create a test table
        stmt.execute("CREATE TABLE test_table (id INT AUTO_INCREMENT PRIMARY KEY, name VARCHAR(255))");
        stmt.execute("INSERT INTO test_table (name) VALUES ('Alice'), ('Bob')");
        stmt.close();

        dbHelper = new DatabaseHelper(url, "sa", "");
    }

    /**
     * Cleans up the in‑memory database after all tests are complete.
     *
     * @throws SQLException if a database access error occurs
     */
    @AfterAll
    public static void teardown() throws SQLException {
        Statement stmt = connection.createStatement();
        stmt.execute("DROP TABLE test_table");
        stmt.close();
        connection.close();
    }

    /**
     * Tests that executing a select query to retrieve a list of names works correctly.
     *
     * @throws SQLException if a database access error occurs
     */
    @Test
    public void testExecuteSelectSimpleType() throws SQLException {
        List<String> names = dbHelper.executeSelect("SELECT name FROM test_table", String.class);
        assertNotNull(names);
        assertEquals(3, names.size());
        assertTrue(names.contains("Charlie"));
        assertTrue(names.contains("Bob"));
        assertTrue(names.contains("David"));
    }

    /**
     * Tests that executing a select query to map results into a DTO works correctly.
     *
     * @throws SQLException if a database access error occurs
     */
    @Test
    public void testExecuteSelectMappingToDTO() throws SQLException {
        List<TestDTO> dtos = dbHelper.executeSelect("SELECT id, name FROM test_table", TestDTO.class);
        assertNotNull(dtos);
        assertEquals(3, dtos.size());
        for (TestDTO dto : dtos) {
            assertNotEquals(0, dto.id);
            assertNotNull(dto.name);
        }
    }

    /**
     * Tests that executing an update query works correctly.
     *
     * @throws SQLException if a database access error occurs
     */
    @Test
    public void testExecuteUpdate() throws SQLException {
        int rowsAffected = dbHelper.executeUpdate("UPDATE test_table SET name = ? WHERE name = ?", "Charlie", "Alice");
        assertEquals(1, rowsAffected);
        List<String> names = dbHelper.executeSelect("SELECT name FROM test_table WHERE name = 'Charlie'", String.class);
        assertEquals(1, names.size());
        assertEquals("Charlie", names.get(0));
    }

    /**
     * Tests that executing an update query which returns generated keys works correctly.
     *
     * @throws SQLException if a database access error occurs
     */
    @Test
    public void testExecuteUpdateGeneratedKeys() throws SQLException {
        // Insert a new row into the table and retrieve the generated key.
        DatabaseHelper.dbNumberSet result = dbHelper.executeUpdateGeneratedKeys("INSERT INTO test_table (name) VALUES (?)", "David");
        // Verify one row was affected.
        assertEquals(1, result.getFirst());
        // Ensure the auto-generated ID is valid.
        assertTrue(result.getSecond() > 0);

        // Retrieve the row using the generated key and verify the name.
        List<String> names = dbHelper.executeSelect("SELECT name FROM test_table WHERE id = ?", String.class, result.getSecond());
        assertEquals(1, names.size());
        assertEquals("David", names.get(0));
    }

    /**
     * Dummy DTO for testing result mapping.
     */
    public static class TestDTO {
        public int id;
        public String name;

        /**
         * Default constructor.
         */
        public TestDTO() {}
    }
}