package Gui;

import DbContext.DataHandler;
import Entities.User;
import javafx.geometry.Insets;
import javafx.scene.control.*;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.Parent;
import javafx.geometry.Pos;
import tools.Logger;
import javafx.scene.layout.GridPane;
import javafx.scene.control.Dialog;
import javafx.scene.control.ComboBox;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Label;

import java.util.List;
import java.util.Optional;
import Entities.Task;

public class MainApp {

    private final Logger logger = new Logger();
    private BorderPane root;
    private TableView<Task> taskTable;
    private DataHandler dataHandler = new DataHandler();

    public MainApp() {
        root = new BorderPane();

        root.getStylesheets().add(getClass().getResource("/Gui/mainapp.css").toExternalForm());

        HBox topBar = new HBox(15);
        topBar.getStyleClass().add("top-bar");
        topBar.setPadding(new Insets(10));
        topBar.setAlignment(Pos.CENTER_LEFT);


        Button addTaskBtn = new Button("Add new task");
        Button distributeBtn = new Button("Distribute all tasks");
        Button closeDoneBtn = new Button("Close done tasks");


        TextField searchField = new TextField();
        searchField.setPromptText("Search tasks...");


        topBar.getChildren().addAll(addTaskBtn, distributeBtn, closeDoneBtn, searchField);


        taskTable = new TableView<>();
        taskTable.getStyleClass().add("task-table");


        TableColumn<Task, String> taskNameCol = new TableColumn<>("Task");
        TableColumn<Task, String> priorityCol = new TableColumn<>("Priority");
        TableColumn<Task, String> statusCol = new TableColumn<>("Status");
        TableColumn<Task, String> descCol = new TableColumn<>("Description");


        taskTable.getColumns().addAll(taskNameCol, priorityCol, statusCol, descCol);

        root.setTop(topBar);
        root.setCenter(taskTable);
        addTaskBtn.setOnAction(e -> {
            logger.info("Add new task clicked!");
            showAddTaskDialog();
        });

        distributeBtn.setOnAction(e -> {
            logger.info("Distribute tasks clicked!");
        });

        closeDoneBtn.setOnAction(e -> {
            logger.info("Close done tasks clicked!");
        });

        searchField.setOnAction(e -> {
            String searchText = searchField.getText();

            logger.info("Search tasks for: " + searchText);
        });
    }
    private void showAddTaskDialog() {
        Dialog<Task> dialog = createBasicDialog("Add New Task", "Enter task details");
        GridPane grid = createDialogGrid();

        DialogControls controls = createTaskDialogControls();
        addControlsToGrid(grid, controls);

        ButtonType addButton = new ButtonType("Add", ButtonBar.ButtonData.OK_DONE);
        setupDialogButtons(dialog, addButton);
        dialog.getDialogPane().setContent(grid);

        setupTaskResultConverter(dialog, addButton, controls);
        handleTaskDialogResult(dialog);
    }

    private record DialogControls(
            TextField descriptionField,
            ComboBox<String> householdCombo,
            ComboBox<String> statusCombo,
            ComboBox<String> priorityCombo,
            ComboBox<User> MemberCombo
    ) {}

    private <T> Dialog<T> createBasicDialog(String title, String headerText) {
        Dialog<T> dialog = new Dialog<>();
        dialog.setTitle(title);
        dialog.setHeaderText(headerText);
        return dialog;
    }

    private GridPane createDialogGrid() {
        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(10);
        grid.setPadding(new Insets(20, 150, 10, 10));
        return grid;
    }

    private DialogControls createTaskDialogControls() {
        TextField descriptionField = new TextField();
        descriptionField.setPromptText("Task description");

        ComboBox<String> householdCombo = new ComboBox<>();
        householdCombo.getItems().addAll(dataHandler.getAllHouseholds());
        householdCombo.setPromptText("Select household");

        ComboBox<String> statusCombo = new ComboBox<>();
        statusCombo.getItems().addAll("Not Started", "In Progress", "Completed");
        statusCombo.setPromptText("Select status");

        ComboBox<String> priorityCombo = new ComboBox<>();
        priorityCombo.getItems().addAll("Low", "Medium", "High");
        priorityCombo.setPromptText("Select priority");

        ComboBox<User> MemberCombo = createOwnerComboBox();
        setupHouseholdOwnerLink(householdCombo, MemberCombo);

        return new DialogControls(descriptionField, householdCombo, statusCombo, priorityCombo, MemberCombo);
    }

    private ComboBox<User> createOwnerComboBox() {
        ComboBox<User> combo = new ComboBox<>();
        combo.setPromptText("Select owner");
        combo.setCellFactory(param -> new ListCell<User>() {
            @Override
            protected void updateItem(User user, boolean empty) {
                super.updateItem(user, empty);
                setText(empty || user == null ? "" : user.getName());
            }
        });
        return combo;
    }
    private void setupHouseholdOwnerLink(ComboBox<String> householdCombo, ComboBox<User> ownerCombo) {
        householdCombo.setOnAction(e -> {
            String selectedHousehold = householdCombo.getValue();
            if (selectedHousehold != null) {
                int householdId = getHouseholdId(selectedHousehold);
                List<User> users = dataHandler.getUsersByHousehold(householdId);
                ownerCombo.getItems().clear();
                ownerCombo.getItems().addAll(users);
            }
        });
    }
    private void addControlsToGrid(GridPane grid, DialogControls controls) {
        grid.add(new Label("Description:"), 0, 0);
        grid.add(controls.descriptionField(), 1, 0);
        grid.add(new Label("Household:"), 0, 1);
        grid.add(controls.householdCombo(), 1, 1);
        grid.add(new Label("Status:"), 0, 2);
        grid.add(controls.statusCombo(), 1, 2);
        grid.add(new Label("Priority:"), 0, 3);
        grid.add(controls.priorityCombo(), 1, 3);
        grid.add(new Label("Members:"), 0, 4);
        grid.add(controls.MemberCombo(), 1, 4);
    }

    private void setupDialogButtons(Dialog<?> dialog, ButtonType... buttons) {
        dialog.getDialogPane().getButtonTypes().addAll(buttons);
        dialog.getDialogPane().getButtonTypes().add(ButtonType.CANCEL);
    }

    private void setupTaskResultConverter(Dialog<Task> dialog, ButtonType addButton, DialogControls controls) {
        dialog.setResultConverter(dialogButton -> {
            if (dialogButton != addButton) return null;
            User selectedUser = controls.MemberCombo().getValue();
            if (selectedUser == null) return null;

            return new Task(
                    generateTaskId(),
                    getHouseholdId(controls.householdCombo().getValue()),
                    controls.descriptionField().getText(),
                    getStatusId(controls.statusCombo().getValue()),
                    getPriorityId(controls.priorityCombo().getValue()),
                    selectedUser.getUserId()
            );
        });
    }

    private void handleTaskDialogResult(Dialog<Task> dialog) {
        Optional<Task> result = dialog.showAndWait();
        result.ifPresent(task -> {
            dataHandler.addTask(task);
            Logger.info("Task added successfully");
        });
    }

    private int getHouseholdId(String householdName) {
        return dataHandler.getHouseholdId(householdName);
    }



    private int getStatusId(String status) {
        switch (status) {
            case "Not Started": return 1;
            case "In Progress": return 2;
            case "Completed": return 3;
            default: return 1;
        }
    }

    private int getPriorityId(String priority) {
        switch (priority) {
            case "Low": return 1;
            case "Medium": return 2;
            case "High": return 3;
            default: return 1;
        }
    }
    private int generateTaskId() {
        // TODO Implement task ID generation logic here
        return (int) (System.currentTimeMillis() % 10000);
    }
    public Parent getRoot() {
        return root;
    }
}
