// Language: java
package gui;

import dbcontext.DataHandler;
import dto.HouseholdDTO;
import dto.TaskDTO;
import javafx.embed.swing.JFXPanel;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.scene.control.TextField;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import utils.Message;
import utils.MessageTypeEnum;

import java.util.ArrayList;

import static org.junit.jupiter.api.Assertions.*;

public class MainControllerTest {

    private MainController controller;

    // Initialize JavaFX toolkit
    @BeforeAll
    public static void initJavaFX() {
        new JFXPanel();
    }

    // A fake DataHandler to simulate data operations
    private static class FakeDataHandler extends DataHandler {
        public FakeDataHandler() {
            super();
        }

        @Override
        public java.util.List<TaskDTO> getAllTasksByHouseHold(int householdId) {
            TaskDTO task = new TaskDTO();
            task.setId(1);
            task.setDescription("Clean kitchen");
            return java.util.Collections.singletonList(task);
        }

        @Override
        public Message<Void> closeDoneTasks(TaskDTO task) {
            return new Message<>(MessageTypeEnum.SUCCESS, null);
        }
    }

    @BeforeEach
    public void setup() {
        controller = new MainController();
        controller.dataHandler = new FakeDataHandler();
        controller.household = new HouseholdDTO(1, "TestHouse");
        controller.originalTaskList = new ArrayList<>();
        ObservableList<TaskDTO> tasks = FXCollections.observableArrayList();
        controller.taskList = tasks;
        controller.searchField = new TextField();
    }

    @Test
    public void testSetHousehold() {
        HouseholdDTO newHouse = new HouseholdDTO(2, "AnotherHouse");
        controller.setHousehold(newHouse);

        try {
            java.lang.reflect.Field householdField = MainController.class.getDeclaredField("household");
            householdField.setAccessible(true);
            HouseholdDTO result = (HouseholdDTO) householdField.get(controller);
            assertEquals("AnotherHouse", result.getName());
        } catch (Exception e) {
            fail("Reflection failed: " + e.getMessage());
        }
    }

    @Test
    public void testHandleSearchDescription() {
        TaskDTO task1 = new TaskDTO();
        task1.setId(1);
        task1.setDescription("Clean kitchen");

        TaskDTO task2 = new TaskDTO();
        task2.setId(2);
        task2.setDescription("Wash car");

        controller.originalTaskList = new ArrayList<>();
        controller.originalTaskList.add(task1);
        controller.originalTaskList.add(task2);
        controller.taskList = FXCollections.observableArrayList(controller.originalTaskList);
        controller.searchField.setText("Clean");
        controller.handleSearchDescription(new ActionEvent());

        assertEquals(1, controller.taskList.size());
        assertEquals("Clean kitchen", controller.taskList.get(0).getDescription());
    }
}