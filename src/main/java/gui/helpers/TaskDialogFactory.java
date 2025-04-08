// Language: java
package gui.helpers;

import dto.HouseholdDTO;
import dto.PriorityDTO;
import dto.TaskDTO;
import dto.UserDTO;
import javafx.event.ActionEvent;
import javafx.scene.control.ButtonBar;
import javafx.scene.control.ButtonType;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Dialog;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.layout.GridPane;
import java.util.List;

/**
 * Factory class for creating dialogs related to Task operations.
 * It provides methods to create dialogs for adding or editing a task.
 */
public class TaskDialogFactory extends DialogFactory {

    /**
     * Creates a dialog to edit an existing task.
     *
     * @param currentTask the task being edited
     * @param household   the associated household
     * @return a Dialog that returns an updated TaskDTO on success, or null on cancel
     */
    public static Dialog<TaskDTO> createEditTaskDescriptionDialog(TaskDTO currentTask, HouseholdDTO household) {
        Dialog<TaskDTO> dialog = createBasicDialog("Edit description", "Enter task details");
        GridPane grid = createDialogGrid();

        TextField taskField = new TextField(currentTask.getDescription());
        Label errorLabel = new Label();

        // Configure grid
        grid.add(new Label("Description:"), 0, 0);
        grid.add(taskField, 1, 0);
        grid.add(errorLabel, 0, 1, 3, 1);

        // Setup buttons
        ButtonType editButton = new ButtonType("Edit", ButtonBar.ButtonData.OK_DONE);
        setupDialogButtons(dialog, editButton);
        dialog.getDialogPane().setContent(grid);

        // Middleware to capture any issues / invalid attempts before submitting
        dialog.getDialogPane().lookupButton(editButton).addEventFilter(ActionEvent.ACTION, e -> {
            if (taskField.getText().trim().isEmpty()) {
                errorLabel.setText("Description cannot be empty!");
                e.consume();
            } else {
                errorLabel.setText("");
            }
        });

        // Converts input data to actual object to be used
        dialog.setResultConverter(button -> {
            if (button == editButton) {
                return new TaskDTO(currentTask.getId(), taskField.getText(), household,
                  currentTask.getStatus(), currentTask.getPriority(), currentTask.getUser());
            }
            return null;
        });
        return dialog;
    }

    /**
     * Creates a dialog to add a new task.
     *
     * @param household  the associated household for the new task
     * @param priorities the list of available priorities
     * @param users      the list of available users
     * @return a Dialog that returns a new TaskDTO on success, or null on cancel
     */
    public static Dialog<TaskDTO> createAddTaskDialog(HouseholdDTO household, List<PriorityDTO> priorities,
                                                      List<UserDTO> users) {
        Dialog<TaskDTO> dialog = createBasicDialog("Add New Task", "Enter task details");
        GridPane grid = createDialogGrid();

        TextField descriptionField = new TextField();
        descriptionField.setPromptText("Task description");

        ComboBox<PriorityDTO> priorityCombo = new ComboBox<>();
        priorityCombo.getItems().addAll(priorities);

        ComboBox<UserDTO> userCombo = new ComboBox<>();
        userCombo.getItems().addAll(users);

        Label errorLabel = new Label();

        grid.add(new Label("Description:"), 0, 0);
        grid.add(descriptionField, 1, 0);
        grid.add(new Label("Priority:"), 0, 1);
        grid.add(priorityCombo, 1, 1);
        grid.add(new Label("Member:"), 0, 2);
        grid.add(userCombo, 1, 2);
        grid.add(errorLabel, 0, 3, 3, 1);

        ButtonType addButton = new ButtonType("Add", ButtonBar.ButtonData.OK_DONE);
        setupDialogButtons(dialog, addButton);
        dialog.getDialogPane().setContent(grid);

        dialog.getDialogPane().lookupButton(addButton).addEventFilter(ActionEvent.ACTION, e -> {
            if (descriptionField.getText().trim().isEmpty()) {
                errorLabel.setText("Description cannot be empty!");
                e.consume();
            } else if (priorityCombo.getValue() == null) {
                errorLabel.setText("Priority cannot be empty!");
                e.consume();
            } else {
                errorLabel.setText("");
            }
        });

        dialog.setResultConverter(button -> {
            if (button == addButton) {
                return new TaskDTO(descriptionField.getText(), household, priorityCombo.getValue(), userCombo.getValue());
            }
            return null;
        });
        return dialog;
    }
}