//// File: src/test/java/dbcontext/DataHandlerTest.java
//
//import dbcontext.DataHandler;
//import dbcontext.DatabaseHelper;
//import dto.HouseholdDTO;
//import dto.TaskDTO;
//import dto.StatusDTO;
//import dto.PriorityDTO;
//import dto.UserDTO;
//import utils.Message;
//import utils.MessageTypeEnum;
//import org.junit.jupiter.api.BeforeAll;
//import org.junit.jupiter.api.BeforeEach;
//import org.junit.jupiter.api.Test;
//
//import java.lang.reflect.Field;
//import java.sql.SQLException;
//import java.util.ArrayList;
//import java.util.Collections;
//import java.util.List;
//
//import static org.junit.jupiter.api.Assertions.*;
//
//public class DataHandlerTest {
//
//    private DataHandler dataHandler;
//
//    @BeforeAll
//    public static void setupClass() throws Exception {
//        // Replace the static final dbHelper field with our fake
//        Field dbHelperField = DataHandler.class.getDeclaredField("dbHelper");
//        dbHelperField.setAccessible(true);
//        dbHelperField.set(null, new FakeDatabaseHelper());
//    }
//
//    @BeforeEach
//    public void setup() {
//        dataHandler = new DataHandler();
//    }
//
//    @Test
//    public void testGetAllHouseholds() {
//        List<HouseholdDTO> households = dataHandler.getAllHouseholds();
//        assertNotNull(households);
//        assertEquals(1, households.size());
//        assertEquals("TestHouse", households.get(0).getName());
//    }
//
//    @Test
//    public void testGetHousehold_Success() {
//        Message<HouseholdDTO> message = dataHandler.getHousehold("TestHouse", "valid");
//        assertEquals(MessageTypeEnum.SUCCESS, message.getType());
//        assertNotNull(message.getMessage());
//        assertEquals("TestHouse", message.getMessage().getName());
//    }
//
//    @Test
//    public void testGetHousehold_Failure() {
//        Message<HouseholdDTO> message = dataHandler.getHousehold("TestHouse", "invalid");
//        assertEquals(MessageTypeEnum.ERROR, message.getType());
//        assertNull(message.getMessage());
//    }
//
//    @Test
//    public void testGetHouseholdId() {
//        int id = dataHandler.getHouseholdId("TestHouse");
//        assertEquals(1, id);
//    }
//
//    @Test
//    public void testAddTask() {
//        // Create a dummy task. It is assumed that TaskDTO and StatusDTO have setters available.
//        TaskDTO task = new TaskDTO();
//        PriorityDTO priority = new PriorityDTO();
//        UserDTO user = new UserDTO();
//        task.setId(1);
//        task.setDescription("Test Task");
//        StatusDTO status = new StatusDTO();
//        status.setId(1);
//        task.setStatus(status);
//        task.setPriority(priority);
//        task.setUser(user);
//
//        Message<Integer> message = dataHandler.addTask(task);
//        assertEquals(MessageTypeEnum.SUCCESS, message.getType());
//        assertNotNull(message.getMessage());
//        assertTrue(message.getMessage() > 0);
//    }
//
//    // FakeDatabaseHelper simulates the required DatabaseHelper behavior
//    public static class FakeDatabaseHelper extends DatabaseHelper {
//        public FakeDatabaseHelper() {
//            super("", "", ""); // connection parameters ignored in fake
//        }
//
//        @Override
//        public <T> List<T> executeSelect(String query, Class<T> clazz, Object... params) throws SQLException {
//            // Simulate getAllHouseholds
//            if (query.contains("FROM households") && !query.contains("WHERE name=?")) {
//                if (clazz.equals(HouseholdDTO.class)) {
//                    HouseholdDTO house = new HouseholdDTO(1, "TestHouse");
//                    return (List<T>) Collections.singletonList(house);
//                }
//            }
//            // Simulate getHousehold with name and password check
//            else if (query.contains("FROM households WHERE name=? AND password=?")) {
//                if (params.length >= 2 && "valid".equals(params[1])) {
//                    HouseholdDTO house = new HouseholdDTO(1, "TestHouse");
//                    return (List<T>) Collections.singletonList(house);
//                } else {
//                    return new ArrayList<>();
//                }
//            }
//            // Simulate getHouseholdId
//            else if (query.contains("SELECT id FROM households WHERE name = ?")) {
//                if (params.length >= 1 && "TestHouse".equals(params[0])) {
//                    return (List<T>) Collections.singletonList(1);
//                }
//            }
//            // Simulate getAllStatus
//            else if (query.contains("FROM status") && clazz.equals(StatusDTO.class)) {
//                StatusDTO status = new StatusDTO();
//                status.setId(1);
//                status.setName("Open");
//                return (List<T>) Collections.singletonList(status);
//            }
//            // Simulate getAllPriorities
//            else if (query.contains("FROM priorities") && clazz.equals(PriorityDTO.class)) {
//                PriorityDTO priority = new PriorityDTO(1, "High");
//                return (List<T>) Collections.singletonList(priority);
//            }
//            return new ArrayList<>();
//        }
//
//        @Override
//        public int executeUpdate(String query, Object... params) throws SQLException {
//            // Simulate successful update or delete
//            return 1;
//        }
//
//        @Override
//        public IntSet executeUpdateGeneratedKeys(String query, Object... params) throws SQLException {
//            // Simulate successful insertion returning generated key 100
//            return new IntSetImpl(1, 100);
//        }
//    }
//
//    // Fake implementation of DatabaseHelper.IntSet
//    public static class IntSetImpl extends DatabaseHelper.IntSet {
//        private final int rowsAffected;
//        private final int autoGeneratedId;
//
//        public IntSetImpl(int rowsAffected, int autoGeneratedId) {
//            this.rowsAffected = rowsAffected;
//            this.autoGeneratedId = autoGeneratedId;
//        }
//
//        @Override
//        public int rowsAffected() {
//            return rowsAffected;
//        }
//
//        @Override
//        public int autoGeneratedId() {
//            return autoGeneratedId;
//        }
//    }
//}