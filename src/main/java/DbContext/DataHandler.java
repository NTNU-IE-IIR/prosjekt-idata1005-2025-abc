package DbContext;

import Entities.User;
import Entities.Task;
import tools.Logger;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class DataHandler {

    private static String url = "jdbc:mysql://mysql.stud.ntnu.no:3306/thomabol_homedatabase?allowPublicKeyRetrieval=true&useSSL=false";
    private static String user = "thomabol_homeUser";
    private static String password = "abc123"; // Replace with actual password

    public List<String> getAllHouseholds() {
        List<String> households = new ArrayList<>();
        String query = "SELECT name FROM households";

        try (Connection conn = DriverManager.getConnection(url, user, password);
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(query)) {

            while (rs.next()) {
                households.add(rs.getString("name"));
            }

        } catch (SQLException e) {
            Logger.error("Error fetching households: " + e.getMessage());
        }
        return households;
    }
    public int getHouseholdId(String householdName) {
        String query = "SELECT id FROM households WHERE name = ?";
        try (Connection conn = DriverManager.getConnection(url, user, password);
             PreparedStatement stmt = conn.prepareStatement(query)) {

            stmt.setString(1, householdName);
            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
                return rs.getInt("id");
            }

        } catch (SQLException e) {
            Logger.error("Error getting household ID: " + e.getMessage());
        }
        return -1;
    }

    public void addTask(Task task) {
        String query = "INSERT INTO tasks (id, householdId, description, statusId, priorityId, ownerId) " +
                "VALUES (?, ?, ?, ?, ?, ?)";

        try (Connection connection = DriverManager.getConnection(url,user,password);
            PreparedStatement preparedStatement = connection.prepareStatement(query)){
            preparedStatement.setInt(1, task.getId());
            preparedStatement.setInt(2, task.getHouseholdId());
            preparedStatement.setString(3, task.getDescription());
            preparedStatement.setInt(4, task.getStatusId());
            preparedStatement.setInt(5, task.getPriorityId());
            preparedStatement.setInt(6, task.getOwnerId());

            int rowsAffected = preparedStatement.executeUpdate();

            Logger.info("Task added to database with the parameters: "
                    + task.getId() + ", " + task.getHouseholdId() + ", " + task.getDescription() + ", "
                    + task.getStatusId() + ", " + task.getPriorityId() + ", " + task.getOwnerId());
        }
        catch(SQLException sqlException){
            Logger.error("Database error: " + sqlException.getMessage());
        }
    }
    public List<User> getUsersByHousehold(int householdId) {
        List<User> users = new ArrayList<>();
        String query = "SELECT id, householdId, name FROM users WHERE householdId = ?";

        try (Connection conn = DriverManager.getConnection(url, user, password);
             PreparedStatement stmt = conn.prepareStatement(query)) {

            stmt.setInt(1, householdId);
            ResultSet rs = stmt.executeQuery();

            while (rs.next()) {
                int id = rs.getInt("id");
                int household = rs.getInt("householdId");
                String username = rs.getString("name");
                users.add(new User(id, household, username));
            }

        } catch (SQLException e) {
            Logger.error("Error fetching users: " + e.getMessage());
        }
        return users;
    }
}
