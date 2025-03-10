package dbcontext;

import dto.*;
import utils.Logger;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class DataHandler {
    private static final DatabaseHelper dbHelper;
    private static final String url = "jdbc:mysql://mysql.stud.ntnu.no:3306/thomabol_homedatabase?allowPublicKeyRetrieval=true&useSSL=false";
    private static final String user = "thomabol_homeUser";
    private static final String password = "abc123"; // Replace with actual password

    static{
        dbHelper = new DatabaseHelper(url, user, password);
    }

    public List<HouseholdDTO> getAllHouseholds() {
        List<HouseholdDTO> households = new ArrayList<>();
        String query = "SELECT id, name FROM households";
        try {
            households = dbHelper.executeSelect(query,HouseholdDTO.class);
        } catch (SQLException e) {
            Logger.error("Error fetching households: " + e.getMessage());
        }
        return households;
    }

    public List<StatusDTO> getAllStatus() {
        List<StatusDTO> status = new ArrayList<>();
        String query = "SELECT id, name FROM status";
        try {
            status = dbHelper.executeSelect(query,StatusDTO.class);
        }catch (SQLException e) {
            Logger.error("Error fetching status: " + e.getMessage());
        }
        return status;
    }

    public List<PriorityDTO> getAllPriorities() {
        List<PriorityDTO> priorities = new ArrayList<>();
        String query = "SELECT id, name FROM priorities";
        try {
            priorities = dbHelper.executeSelect(query,PriorityDTO.class);
        }catch (SQLException e) {
            Logger.error("Error fetching priorities: " + e.getMessage());
        }
        return priorities;
    }

    public int getHouseholdId(String householdName) {
        String query = "SELECT id FROM households WHERE name = ?";
        try {
            return dbHelper.executeSelect(query, Integer.class).getFirst();
        } catch (SQLException e) {
            Logger.error("Error getting household ID: " + e.getMessage());
        }
        return -1;
    }

    public void addTask(TaskDTO task) {
        String query = "INSERT INTO tasks (householdId, description, priorityId, ownerId) " +
                "VALUES (?, ?, ?, ?)";
        try{
            int rowsAffected = dbHelper.executeUpdate(query, task.getHouseholdId(),
              task.getDescription(), task.getPriorityId(), task.getUserId());
        }
        catch(SQLException e){
            Logger.error("Error adding task: " + e.getMessage());
        }
    }
    public List<UserDTO> getAllUsersByHousehold(int householdId) {
        List<UserDTO> users = new ArrayList<>();
        String query=
          "SELECT u.id, u.name, h.id as household_id, h.name as household_name " +
          "FROM users u " +
          "Join households h on h.id = u.householdId WHERE householdId = ?";
        try{
            users = dbHelper.executeSelect(query, UserDTO.class, householdId);
        }catch (SQLException e){
            Logger.error("Error fetching users: " + e.getMessage());
        }
        return users;
    }

    public List<TaskDTO> getAllTasks(int householdId){
        List<TaskDTO> tasks = new ArrayList<>();
        String query = "SELECT " +
          "t.id, t.description, " +
          "h.id AS household_id, h.name AS household_name, " +
          "s.id AS status_id, s.name AS status_name, " +
          "p.id AS priority_id, p.name AS priority_name, " +
          "u.id AS user_id, u.name AS user_name " +
          "FROM tasks t " +
          "LEFT JOIN households h ON t.householdId = h.id " +
          "LEFT JOIN status s ON t.statusId = s.id " +
          "LEFT JOIN priorities p ON t.priorityId = p.id " +
          "LEFT JOIN users u ON t.ownerId = u.id " +
          "WHERE t.householdId = ? "+
          "ORDER BY t.id DESC";
        try{
            tasks = dbHelper.executeSelect(query, TaskDTO.class, householdId);
        } catch ( SQLException e){
            Logger.error("Error fetching tasks: " + e.getMessage());
        }
        return tasks;
    }

    public List<TaskDTO> getAllTasks(int householdId, String userQuery){
        List<TaskDTO> tasks = new ArrayList<>();
        userQuery = "%" + userQuery + "%"; // <- Enables "like" searching.
        String query = "SELECT " +
            "t.id, t.description, " +
            "h.id AS household_id, h.name AS household_name, " +
            "s.id AS status_id, s.name AS status_name, " +
            "p.id AS priority_id, p.name AS priority_name, " +
            "u.id AS user_id, u.name AS user_name " +
            "FROM tasks t " +
            "LEFT JOIN households h ON t.householdId = h.id " +
            "LEFT JOIN status s ON t.statusId = s.id " +
            "LEFT JOIN priorities p ON t.priorityId = p.id " +
            "LEFT JOIN users u ON t.ownerId = u.id " +
            "WHERE t.householdId = ? AND t.description LIKE ? "+
            "ORDER BY t.id DESC";
        try{
            tasks = dbHelper.executeSelect(query, TaskDTO.class, householdId, userQuery);
        } catch ( SQLException e){
            Logger.error("Error fetching tasks: " + e.getMessage());
        }
        return tasks;
    }


    public List<TaskDTO> getLimitedTasks(int householdId, int limit, int offset) {
        List<TaskDTO> tasks = new ArrayList<>();
        String query = "SELECT " +
          "t.id, t.description, " +
          "h.id AS household_id, h.name AS household_name, " +
          "s.id AS status_id, s.name AS status_name, " +
          "p.id AS priority_id, p.name AS priority_name, " +
          "u.id AS user_id, u.name AS user_name " +
          "FROM tasks t " +
          "LEFT JOIN households h ON t.householdId = h.id " +
          "LEFT JOIN status s ON t.statusId = s.id " +
          "LEFT JOIN priorities p ON t.priorityId = p.id " +
          "LEFT JOIN users u ON t.ownerId = u.id " +
          "WHERE t.householdId = ? "+
          "ORDER BY t.id DESC "+
          "LIMIT ? OFFSET ?";
        try{
            tasks = dbHelper.executeSelect(query, TaskDTO.class, householdId, limit, offset);
        } catch ( SQLException e){
            Logger.error("Error fetching tasks: " + e.getMessage());
        }
        return tasks;
    }

    public void addUser(UserDTO user) {
        String query = "INSERT INTO users (name, householdId) VALUES (?, ?)";
        try {
            int rowsAffected = dbHelper.executeUpdate(query, user.getName(), user.getHousehold().getId());
            if (rowsAffected > 0) {
                Logger.info("User added successfully: " + user.getName());
            } else {
                Logger.error("Failed to add user: " + user.getName());
            }
        } catch (SQLException e) {
            Logger.error("Error adding user: " + e.getMessage());
        }
    }

    public void updateTask(TaskDTO task) {
        String query = "UPDATE tasks SET description = ?,statusId = ?, priorityId = ?, ownerId = ? WHERE id = ?";
        try{
            int rowsAffected = dbHelper.executeUpdate(
                    query,
                    task.getDescription(),
                    task.getStatusId(),
                    task.getPriorityId(),
                    task.getUserId(),
                    task.getId());
                    if (rowsAffected > 0){
                        Logger.info("Task updated successfully: " + task.getDescription());
                    }else {
                        Logger.error("Failed to update task: " + task.getDescription());
                    }
        }catch(SQLException e){
            Logger.error("Error updating task : " + e.getMessage());
        }
    }
}
