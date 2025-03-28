package dbcontext;

import dto.*;
import utils.Logger;
import utils.Message;
import utils.MessageTypeEnum;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class DataHandler {
    private static final DatabaseHelper dbHelper;
    private static final String url = "jdbc:mysql://mysql.stud.ntnu.no:3306/thomabol_homedatabase?allowPublicKeyRetrieval=true&useSSL=false";
    private static final String user = "thomabol_homeUser";
    private static final String password = "abc123"; // Replace with actual password

    static {
        dbHelper = new DatabaseHelper(url, user, password);
    }

    public List<HouseholdDTO> getAllHouseholds() {
        List<HouseholdDTO> households = new ArrayList<>();
        String query = "SELECT id, name FROM households";
        try {
            households = dbHelper.executeSelect(query, HouseholdDTO.class);
        } catch (SQLException e) {
            Logger.error("Error fetching households: " + e.getMessage());
        }
        return households;
    }

    public Message<HouseholdDTO> getHousehold(String name, String hashedPassword) {
        String query = "SELECT id, name FROM households WHERE name=? AND password=? LIMIT 1";
        Message<HouseholdDTO> message;
        try {
            List<HouseholdDTO> queryResult = dbHelper.executeSelect(query, HouseholdDTO.class, name, hashedPassword);
            if (queryResult.isEmpty()) {
                message = new Message<>(MessageTypeEnum.ERROR, "Either password or name is wrong");
            } else {
                message = new Message<>(MessageTypeEnum.SUCCESS, "Success!", queryResult.getFirst());
            }
        } catch (SQLException e) {
            message = new Message<>(MessageTypeEnum.ERROR, "Internal server error. Could not fetch households");
            Logger.error("Error fetching household by name and password: " + e.getMessage());
        }
        return message;
    }

    public List<StatusDTO> getAllStatus() {
        List<StatusDTO> status = new ArrayList<>();
        String query = "SELECT id, name FROM status";
        try {
            status = dbHelper.executeSelect(query, StatusDTO.class);
        } catch (SQLException e) {
            Logger.error("Error fetching status: " + e.getMessage());
        }
        return status;
    }

    public List<PriorityDTO> getAllPriorities() {
        List<PriorityDTO> priorities = new ArrayList<>();
        String query = "SELECT id, name FROM priorities";
        try {
            priorities = dbHelper.executeSelect(query, PriorityDTO.class);
        } catch (SQLException e) {
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

    public Message<Void> addTask(TaskDTO task) {
        Message<Void> message;
        String query = "INSERT INTO tasks (householdId, description, statusId, priorityId, ownerId) " +
                "VALUES (?, ?, ?, ?, ?)";
        try {
            int rowsAffected = dbHelper.executeUpdate(query, task.getHouseholdId(),
                    task.getDescription(), 4, task.getPriorityId(), task.getUserId());

            if (rowsAffected == 0) {
                message = new Message<>(MessageTypeEnum.ERROR, "Could not add the task.");
            } else {
                message = new Message<>(MessageTypeEnum.SUCCESS, "Successfully added the task!");
            }
        } catch (SQLException e) {
            message = new Message<>(MessageTypeEnum.ERROR, "Internal server error. Could not add the task.");
            Logger.error("Error adding task: " + e.getMessage());
        }
        return message;
    }

    public List<UserDTO> getAllUsersByHousehold(int householdId) {
        List<UserDTO> users = new ArrayList<>();
        String query =
                "SELECT u.id, u.name, h.id as household_id, h.name as household_name " +
                        "FROM users u " +
                        "Join households h on h.id = u.householdId WHERE householdId = ?";
        try {
            users = dbHelper.executeSelect(query, UserDTO.class, householdId);
        } catch (SQLException e) {
            Logger.error("Error fetching users: " + e.getMessage());
        }
        return users;
    }

    public List<TaskDTO> getAllTasksByHouseHold(int householdId) {
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
                "WHERE t.householdId = ? " +
                "ORDER BY t.id DESC";
        try {
            tasks = dbHelper.executeSelect(query, TaskDTO.class, householdId);
        } catch (SQLException e) {
            Logger.error("Error fetching tasks: " + e.getMessage());
        }
        return tasks;
    }

    public List<TaskDTO> getAllTasksByHouseHold(int householdId, String userQuery) {
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
                "WHERE t.householdId = ? AND t.description LIKE ? " +
                "ORDER BY t.id DESC";
        try {
            tasks = dbHelper.executeSelect(query, TaskDTO.class, householdId, userQuery);
        } catch (SQLException e) {
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
                "WHERE t.householdId = ? " +
                "ORDER BY t.id DESC " +
                "LIMIT ? OFFSET ?";
        try {
            tasks = dbHelper.executeSelect(query, TaskDTO.class, householdId, limit, offset);
        } catch (SQLException e) {
            Logger.error("Error fetching tasks: " + e.getMessage());
        }
        return tasks;
    }


    public void updateTask(TaskDTO task) {
        String query = "UPDATE tasks SET description = ?,statusId = ?, priorityId = ?, ownerId = ? WHERE id = ?";
        try {
            int rowsAffected = dbHelper.executeUpdate(
                    query,
                    task.getDescription(),
                    task.getStatusId(),
                    task.getPriorityId(),
                    task.getUserId(),
                    task.getId());
            if (rowsAffected > 0) {
                Logger.info("Task updated successfully: " + task.getDescription());
            } else {
                Logger.error("Failed to update task: " + task.getDescription());
            }
        } catch (SQLException e) {
            Logger.error("Error updating task : " + e.getMessage());
        }
    }

    public Message<Integer> addUser(UserDTO user) {
        String query = "INSERT INTO users (name, householdId) VALUES (?, ?)";
        Message<Integer> message;
        try {
            DatabaseHelper.IntSet queryResult = dbHelper.executeUpdateGeneratedKeys(query, user.getName(), user.getHousehold().getId());
            if (queryResult.rowsAffected() > 0) {
                Logger.info("User added successfully: " + user.getName());
                message = new Message<>(MessageTypeEnum.SUCCESS, "User added successfully: "+ user.getName()+" !"
                  ,queryResult.autoGeneratedId());
            } else {
                Logger.error("Failed to add user: " + user.getName());
                message = new Message<>(MessageTypeEnum.ERROR, "Failed to add user: " + user.getName());
            }
        } catch (SQLException e) {
            Logger.error("Error adding user: " + e.getMessage());
            message = new Message<>(MessageTypeEnum.ERROR, "Error adding user: " + e.getMessage());
        }
        return message;
    }

    //TODO
    public void deleteUser(UserDTO user) {
        String query = "DELETE FROM users WHERE id = ?";
        String queryUnassignUser = "UPDATE tasks SET ownerId = NULL WHERE ownerId = ?";

        try {
            int unassignUserRowsAffected = dbHelper.executeUpdate(queryUnassignUser, user.getId());
            int rowsAffected = dbHelper.executeUpdate(query, user.getId());
            if (rowsAffected > 0) {
                Logger.info("User deleted successfully: " + user.getName()+". "+unassignUserRowsAffected+" rows affected.");
            } else {
                Logger.error("Failed to delete user: " + user.getName());
            }
        } catch (SQLException e) {
            Logger.error("Error deleting user: " + e.getMessage() +
                    " User: " + user.getName());
            System.out.println("Household id " + user.getHousehold().getId()
                    + " User id " + user.getId());
            System.out.println(query);
        }
    }

    public Message<List<TaskDTO>> getUserTasks(UserDTO user) {
        Message<List<TaskDTO>> message;
        String query ="SELECT " +
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
          "WHERE t.householdId = ? AND u.id = ? " +
          "ORDER BY t.id DESC";
        try {
            List<TaskDTO> result = dbHelper.executeSelect(query, TaskDTO.class,user.getHousehold().getId(), user.getId());
            message = new Message<>(MessageTypeEnum.SUCCESS, "Successfully viewing tasks for "+user.getName(), result);
        }
        catch (SQLException e) {
            message = new Message<>(MessageTypeEnum.ERROR, "Internal server error: " + e.getMessage());
        }
        return message;
    }

    public Message<Void> editUser(UserDTO selectedUser) {
        Message<Void> message;
        String query = "UPDATE users SET name = ? WHERE id = ?";
        try {
            int rowsAffected = dbHelper.executeUpdate(query, selectedUser.getName(), selectedUser.getId());
            if (rowsAffected > 0) {
                message = new Message<>(MessageTypeEnum.SUCCESS, "Successfully updated user: "+ selectedUser.getName());
                Logger.info("Successfully updated user: "+ selectedUser.getName());
            } else {
                message = new Message<>("Failed to update user: "+ selectedUser.getName());
                Logger.error("Failed to update user: " + selectedUser.getId() + " to " + selectedUser.getName());
            }
        } catch (SQLException e) {
            message = new Message<>("Internal server error while updating user");
        }
        return message;
    }

    public Message<Void> deleteTask(TaskDTO task) {
        Message<Void> message;
        String query = "DELETE FROM tasks WHERE id = ?";
        try {
            int rowsAffected = dbHelper.executeUpdate(query, task.getId());
            if (rowsAffected > 0) {
                message = new Message<>(MessageTypeEnum.SUCCESS, "Successfully deleted task");
                Logger.info("Task deleted successfully: " + task.getDescription());
            } else {
                message = new Message<>("Failed to delete task");
                Logger.error("Failed to delete task: " + task.getDescription());
            }
        } catch (SQLException e) {
            message = new Message<>("Internal server error while deleting task");
            Logger.error("Error deleting task: " + e.getMessage());
        }
        return message;
    }

    public Message<Void> editTask(TaskDTO editedTask) {
        Message<Void> message = null;
        String query = "UPDATE tasks SET description = ?, statusId = ?, priorityId = ?, ownerId = ? WHERE id = ?";
        try {
            int rowsAffected = dbHelper.executeUpdate(
                    query,
                    editedTask.getDescription(),
                    editedTask.getStatusId(),
                    editedTask.getPriorityId(),
                    editedTask.getUserId(),
                    editedTask.getId());
            if (rowsAffected > 0) {
                Logger.info("Task updated successfully: " + editedTask.getDescription());
            } else {
                Logger.error("Failed to update task: " + editedTask.getDescription());
            }
        } catch (SQLException e) {
            message = new Message<>("Internal server error while updating task");
            Logger.error("Error updating task: " + e.getMessage());
        }
        return message;
    }

    public Message<Void> closeDoneTasks(TaskDTO doneTasks) {
        Message<Void> message = null;
        String query = "UPDATE tasks SET statusId = 4 WHERE id = ?";
        try{
            int rowsAffected = dbHelper.executeUpdate(
                    query,
                    doneTasks.getId());
            if (rowsAffected > 0) {
                Logger.info("Task closed successfully: " + doneTasks.getDescription());
            } else {
                System.out.println(query);
                Logger.error("Failed to close task: " + doneTasks.getDescription());
            }
        } catch (SQLException e) {
            System.out.println(query);
            message = new Message<>("Internal server error while closing tasks");
            Logger.error("Error closing tasks: " + e.getMessage());
        }
        return message;
    }
}
