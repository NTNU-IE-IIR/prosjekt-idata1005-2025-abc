// Language: java
package controllers;

import dto.HouseholdDTO;
import dto.PriorityDTO;
import dto.StatusDTO;
import dto.TaskDTO;
import dto.UserDTO;
import javafx.event.ActionEvent;
import javafx.geometry.Insets;
import javafx.scene.control.*;
import javafx.scene.layout.GridPane;
import java.util.List;

public class TaskDialogFactory {

    private static <T> Dialog<T> createBasicDialog(String title, String header) {
        Dialog<T> dialog = new Dialog<>();
        dialog.setTitle(title);
        dialog.setHeaderText(header);
        return dialog;
    }

    private static GridPane createDialogGrid() {
        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(10);
        grid.setPadding(new Insets(20, 150, 10, 10));
        return grid;
    }

    private static void setupDialogButtons(Dialog<?> dialog, ButtonType... buttons) {
        dialog.getDialogPane().getButtonTypes().addAll(buttons);
        dialog.getDialogPane().getButtonTypes().add(ButtonType.CANCEL);
    }

    public static Dialog<TaskDTO> createEditTaskDialog(TaskDTO currentTask, HouseholdDTO household, List<StatusDTO> statuses, List<PriorityDTO> priorities, List<UserDTO> users) {
        Dialog<TaskDTO> dialog = createBasicDialog("Edit Task", "Enter task details");
        GridPane grid = createDialogGrid();

        TextField taskField = new TextField(currentTask.getDescription());
        taskField.setPromptText("New task name");
        ComboBox<PriorityDTO> priorityCombo = new ComboBox<>();
        priorityCombo.getItems().addAll(priorities);
        priorityCombo.setValue(currentTask.getPriority());

        ComboBox<StatusDTO> statusCombo = new ComboBox<>();
        statusCombo.getItems().addAll(statuses);
        statusCombo.setValue(currentTask.getStatus());

        ComboBox<UserDTO> userCombo = new ComboBox<>();
        userCombo.getItems().addAll(users);
        userCombo.setValue(currentTask.getUser());

        Label errorLabel = new Label();

        grid.add(new Label("Task:"), 0, 0);
        grid.add(taskField, 1, 0);
        grid.add(new Label("Priority:"), 0, 1);
        grid.add(priorityCombo, 1, 1);
        grid.add(new Label("Status:"), 0, 2);
        grid.add(statusCombo, 1, 2);
        grid.add(new Label("Member:"), 0, 3);
        grid.add(userCombo, 1, 3);
        grid.add(errorLabel, 1, 4);

        ButtonType editButton = new ButtonType("Edit", ButtonBar.ButtonData.OK_DONE);
        setupDialogButtons(dialog, editButton);
        dialog.getDialogPane().setContent(grid);

        dialog.getDialogPane().lookupButton(editButton).addEventFilter(ActionEvent.ACTION, e -> {
            if(taskField.getText().trim().isEmpty()){
                errorLabel.setText("Task name cannot be empty!");
                e.consume();
            } else {
                errorLabel.setText("");
            }
        });

        dialog.setResultConverter(button -> {
            if(button == editButton){
                return new TaskDTO(currentTask.getId(), taskField.getText(), household, statusCombo.getValue(), priorityCombo.getValue(), userCombo.getValue());
            }
            return null;
        });
        return dialog;
    }

    public static Dialog<TaskDTO> createAddTaskDialog(HouseholdDTO household, List<PriorityDTO> priorities, List<UserDTO> users) {
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
        grid.add(errorLabel, 1, 3);

        ButtonType addButton = new ButtonType("Add", ButtonBar.ButtonData.OK_DONE);
        setupDialogButtons(dialog, addButton);
        dialog.getDialogPane().setContent(grid);

        dialog.getDialogPane().lookupButton(addButton).addEventFilter(ActionEvent.ACTION, e -> {
            if(descriptionField.getText().trim().isEmpty()){
                errorLabel.setText("Description cannot be empty!");
                e.consume();
            } else {
                errorLabel.setText("");
            }
        });

        dialog.setResultConverter(button -> {
            if(button == addButton){
                return new TaskDTO(descriptionField.getText(), household, priorityCombo.getValue(), userCombo.getValue());
            }
            return null;
        });
        return dialog;
    }
}