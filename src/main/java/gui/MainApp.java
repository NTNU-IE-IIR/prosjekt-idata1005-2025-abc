package gui;

import dbcontext.DataHandler;
import dto.*;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.Parent;
import javafx.geometry.Pos;
import utils.Logger;
import javafx.scene.layout.GridPane;
import javafx.scene.control.Dialog;
import javafx.scene.control.ComboBox;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Label;
import javafx.event.ActionEvent;

import java.util.List;
import java.util.Optional;
import Entities.Task;

public class MainApp {

    private BorderPane root;
    private TableView<TaskDTO> taskTable;
    private DataHandler dataHandler;
    private final List<StatusDTO> initalStatusList;
    private final List<PriorityDTO> initalPriorityList;
    private final List<UserDTO> initalUserList;
    private HouseholdDTO household;


    public MainApp() {
        // Db context
        dataHandler = new DataHandler();

        // Variables
        household = dataHandler.getAllHouseholds().getFirst();  // <- Hard coded until we'll be able to supply it from the login page
        initalStatusList = dataHandler.getAllStatus();
        initalPriorityList = dataHandler.getAllPriorities();
        initalUserList = dataHandler.getAllUsersByHousehold(household.getId());

        // Borderpane, a layout container that arranges components in five regions: TOP, BOTTOM, LEFT, RIGHT, CENTER
        root = new BorderPane();
        root.getStylesheets().add(getClass().getResource("/gui/mainapp.css").toExternalForm()); // <- Link stylesheet

        // Create the top bar, the buttons on it and the search field
        HBox topBar = new HBox(15);
        topBar.getStyleClass().add("top-bar");
        topBar.setPadding(new Insets(10));
        topBar.setAlignment(Pos.CENTER_LEFT);

        Button addTaskBtn = new Button("Add new task");
        Button distributeBtn = new Button("Distribute all tasks");
        Button closeDoneBtn = new Button("Close done tasks");

        TextField searchField = new TextField();
        searchField.setPromptText("Search tasks...");

        // Create the left bar
        HBox rightBar = new HBox(15);
        rightBar.setPadding(new Insets(10));
        topBar.setAlignment(Pos.CENTER_LEFT);

        Button addUserBtn = new Button("Add people");

        rightBar.getChildren().addAll(addUserBtn);
        // Adds all the components to the top bar
        topBar.getChildren().addAll(addTaskBtn, distributeBtn, closeDoneBtn, searchField);

        // Creates a table for the tasks, and it's accompanying columns
        taskTable = new TableView<>();
        taskTable.getStyleClass().add("task-table");
        // Define Table Columns
        TableColumn<TaskDTO, String> descriptionColumn = new TableColumn<>("Description");
        descriptionColumn.setCellValueFactory(cellData ->
          new javafx.beans.property.SimpleStringProperty(cellData.getValue().getDescription()));

        TableColumn<TaskDTO, String> statusColumn = new TableColumn<>("Status");
        statusColumn.setCellValueFactory(cellData ->
          new javafx.beans.property.SimpleStringProperty(
            cellData.getValue().getStatus() != null ? cellData.getValue().getStatus().getName() : "N/A"
          )
        );

        TableColumn<TaskDTO, String> priorityColumn = new TableColumn<>("Priority");
        priorityColumn.setCellValueFactory(cellData ->
          new javafx.beans.property.SimpleStringProperty(
            cellData.getValue().getPriority() != null ? cellData.getValue().getPriority().getName() : "N/A"
          )
        );

        TableColumn<TaskDTO, String> userColumn = new TableColumn<>("User");
        userColumn.setCellValueFactory(cellData ->
          new javafx.beans.property.SimpleStringProperty(
            cellData.getValue().getUser() != null ? cellData.getValue().getUser().getName() : "N/A"
          )
        );

        // Add columns to the table
        taskTable.getColumns().addAll(descriptionColumn, statusColumn, priorityColumn, userColumn);

        // Populate with test data
        taskTable.setItems(getTasks());

        // Sets the top-bar at the top
        root.setTop(topBar);

        root.setRight(rightBar);
        // Sets the task-table in the center
        root.setCenter(taskTable);

        // Configure actions on the buttons and text-field on the top-bar
        addTaskBtn.setOnAction(e -> {
            Logger.info("Add new task clicked!");
            addTask(); // <- Opens up the Add task "pop-up"
        });

        distributeBtn.setOnAction(e -> {
            Logger.info("Distribute tasks clicked!");
        });

        closeDoneBtn.setOnAction(e -> {
            Logger.info("Close done tasks clicked!");
        });

        searchField.setOnAction(e -> {
            String searchText = searchField.getText();

            Logger.info("Search tasks for: " + searchText);
        });
    }

    private ObservableList<TaskDTO> getTasks() {
        ObservableList<TaskDTO> tasks = FXCollections.observableArrayList();
        tasks.addAll(dataHandler.getAllTasks(household.getId()));
        return tasks;
    }

    private void addTask(){
        Dialog<TaskDTO> dialog = createBasicDialog("Add New Task", "Enter task details");
        GridPane grid = createDialogGrid();

        // Description field
        TextField descriptionField = new TextField();
        descriptionField.setPromptText("Task description");

        // Priority combo
        ComboBox<PriorityDTO> priorityCombo = createComboBox(initalPriorityList);
        priorityCombo.setPromptText("Select priority");

        // User combo
        ComboBox<UserDTO> userCombo = createComboBox(initalUserList);
        userCombo.setPromptText("Select user");

        // Error msg label
        Label errorLabel = new Label();

        // Add combos and field to dialog grid
        grid.add(new Label("Description:"), 0, 0);
        grid.add(descriptionField, 1, 0);
        grid.add(new Label("Priority:"), 0, 1);
        grid.add(priorityCombo, 1, 1);
        grid.add(new Label("Members:"), 0, 2);
        grid.add(userCombo, 1, 2);
        grid.add(errorLabel, 0, 3, 2, 1);

        ButtonType addButton = new ButtonType("Add", ButtonBar.ButtonData.OK_DONE);
        setupDialogButtons(dialog, addButton);
        dialog.getDialogPane().setContent(grid);

        // Prevent dialog from closing if validation fails
        dialog.getDialogPane().lookupButton(addButton).addEventFilter(ActionEvent.ACTION, event -> {
            if (descriptionField.getText().trim().isEmpty()) {
                errorLabel.setText("Description cannot be empty!");
                event.consume(); // Prevents dialog from closing
            } else {
                errorLabel.setText(""); // Clear error message if input is valid
            }
        });

        // Set the result converter
        dialog.setResultConverter(dialogButton -> {
            if (dialogButton == addButton) {
                // Retrieve user input values
                String description = descriptionField.getText();
                PriorityDTO selectedPriority = priorityCombo.getValue();
                UserDTO selectedUser = userCombo.getValue();
                return new TaskDTO(description, household, selectedPriority, selectedUser);
            }
            return null; // If cancel is clicked, return null
        });

        Optional<TaskDTO> result = dialog.showAndWait();
        result.ifPresent(task -> {
            dataHandler.addTask(task);
            Logger.info("Task added successfully");
        });
    }

    private<T> ComboBox<T> createComboBox(List<T> list) {
        ComboBox<T> newCombo = new ComboBox<>();
        newCombo.setItems(FXCollections.observableArrayList(list));
        return newCombo;
    }

    /**
     * Creates a basic dialog with the specified title and header text.
     *
     * @param <T>        the type of the dialog's result
     * @param title      the title of the dialog
     * @param headerText the header text of the dialog
     * @return a new {@link Dialog} instance with the specified title and header text
     */
    private <T> Dialog<T> createBasicDialog(String title, String headerText) {
        Dialog<T> dialog = new Dialog<>();
        dialog.setTitle(title);
        dialog.setHeaderText(headerText);
        return dialog;
    }

    /**
     * Creates a {@link GridPane} layout for use in a dialog.
     *
     * @return a {@link GridPane} with predefined spacing and padding
     */
    private GridPane createDialogGrid() {
        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(10);
        grid.setPadding(new Insets(20, 150, 10, 10));
        return grid;
    }

    private void setupDialogButtons(Dialog<?> dialog, ButtonType... buttons) {
        dialog.getDialogPane().getButtonTypes().addAll(buttons);
        dialog.getDialogPane().getButtonTypes().add(ButtonType.CANCEL);
    }

    public Parent getRoot() {
        return root;
    }
}
