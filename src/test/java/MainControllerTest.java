// Language: java
package gui;

import javafx.embed.swing.JFXPanel;
import javafx.event.ActionEvent;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.collections.FXCollections;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import dto.*;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests for the MainController class.
 *
 * <p>
 * This test class verifies the functionality of the MainController including
 * searching task descriptions and sorting tasks by description in both ascending
 * and descending order.
 * </p>
 */
class MainControllerTest {

    private MainController controller;
    private TaskDTO t1, t2, t3;
    private HouseholdDTO household;

    /**
     * Initializes the JavaFX toolkit so that JavaFX controls can be instantiated in tests.
     */
    @BeforeAll
    static void initJfxToolkit() {
        new JFXPanel();
    }

    /**
     * Sets up the MainController instance, stubs FXML‐injected controls, and creates dummy task data.
     */
    @BeforeEach
    void setUp() {
        controller = new MainController();

        // Stub the FXML‐injected controls the logic touches:
        controller.searchField = new TextField();
        controller.sortTaskDescription = new Label("Description");
        controller.sortTaskStatus      = new Label("Status");
        controller.sortTaskPriority    = new Label("Priority");
        controller.sortTaskOwner       = new Label("Owner");

        // Dummy household
        household = new HouseholdDTO(1, "TestHouse");

        // Create three tasks with distinct descriptions
        t1 = new TaskDTO("alpha", household, new PriorityDTO(1, "Low"), null);
        t2 = new TaskDTO("Bravo", household, new PriorityDTO(1, "Low"), null);
        t3 = new TaskDTO("charlie", household, new PriorityDTO(1, "Low"), null);

        // Set up originalTaskList and taskList
        controller.originalTaskList = new ArrayList<>(List.of(t1, t2, t3));
        controller.taskList = FXCollections.observableArrayList(t1, t2, t3);

        // Clear any previous filter
        controller.userQuery = "";
    }

    /**
     * Tests that an empty search query returns all tasks in their original order.
     */
    @Test
    void testSearchDescription_emptyQueryReturnsAll() {
        controller.searchField.setText("");
        controller.handleSearchDescription((ActionEvent) null);

        assertEquals(3, controller.taskList.size());
        // Should maintain original order
        assertIterableEquals(controller.originalTaskList, controller.taskList);
    }

    /**
     * Tests that the search is case insensitive by looking for a mixed-case query.
     */
    @Test
    void testSearchDescription_caseInsensitive() {
        controller.searchField.setText("bRaVo");
        controller.handleSearchDescription((ActionEvent) null);

        assertEquals(1, controller.taskList.size());
        assertSame(t2, controller.taskList.get(0));
    }

    /**
     * Tests that sorting task descriptions in ascending order works correctly.
     */
    @Test
    void testSortTaskDescription_ascending() {
        // Shuffle the list for sorting test
        controller.taskList = FXCollections.observableArrayList(t3, t1, t2);

        controller.sortTaskDescription(false);

        // Expected order: alpha, Bravo, charlie
        assertEquals(List.of(t1, t2, t3), controller.taskList);
    }

    /**
     * Tests that sorting task descriptions in descending order works correctly.
     */
    @Test
    void testSortTaskDescription_descending() {
        controller.taskList = FXCollections.observableArrayList(t1, t2, t3);

        controller.sortTaskDescription(true);

        // Expected reverse order: charlie, Bravo, alpha
        assertEquals(List.of(t3, t2, t1), controller.taskList);
    }
}