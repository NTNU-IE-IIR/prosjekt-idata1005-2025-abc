package controllers;

import dbcontext.DataHandler;
import dto.*;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.concurrent.Task;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.geometry.Insets;
import javafx.scene.control.*;
import javafx.scene.control.cell.ComboBoxTableCell;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.GridPane;
import utils.Logger;

import java.util.List;
import java.util.Objects;
import java.util.Optional;

public class MainController {

  @FXML private BorderPane root;
  @FXML private TableView<TaskDTO> taskTable;
  @FXML private ListView<UserDTO> userTable;
  @FXML private TableColumn<TaskDTO, String> descriptionColumn;
  @FXML private TableColumn<TaskDTO, StatusDTO> statusColumn;
  @FXML private TableColumn<TaskDTO, PriorityDTO> priorityColumn;
  @FXML private TableColumn<TaskDTO, String> userColumn;
  @FXML private Button addTaskBtn, distributeBtn, closeDoneBtn, addUserBtn, nextTaskPage, previousTaskPage;
  @FXML private TextField searchField;

  private DataHandler dataHandler;
  private HouseholdDTO household;
  private List<StatusDTO> initialStatusList;
  private List<PriorityDTO> initialPriorityList;
  private List<UserDTO> initialUserList;
  private int taskLimit;
  private int taskPageCount;

  @FXML
  public void initialize() {
    // Load CSS file
    root.getStylesheets().add(Objects.requireNonNull(getClass().getResource("/gui/main.css")).toExternalForm());

    // Db context
    dataHandler = new DataHandler();

    // Variables
    taskLimit = 4;
    taskPageCount = 1;

    taskTable.setEditable(true);
    // Configure Table Columns
    descriptionColumn.setCellValueFactory(cellData ->
      new javafx.beans.property.SimpleStringProperty(cellData.getValue().getDescription()));

    statusColumn.setCellValueFactory(cellData ->
      new javafx.beans.property.SimpleObjectProperty<>(cellData.getValue().getStatus()));



    userColumn.setCellValueFactory(cellData ->
      new javafx.beans.property.SimpleStringProperty(
        cellData.getValue().getUser() != null ? cellData.getValue().getUser().getName() : "Unassigned"
      ));

    Platform.runLater(()->{
      // Fetch Data
      initialStatusList = dataHandler.getAllStatus();
      initialPriorityList = dataHandler.getAllPriorities();
      initialUserList = dataHandler.getAllUsersByHousehold(household.getId());

      // Load data into table
      taskTable.setItems(getAllTasks());

      // Load data into user list
      userTable.setItems(getAllUsers());

      statusColumn.setCellFactory(ComboBoxTableCell.forTableColumn(FXCollections.observableArrayList(initialStatusList)));

      priorityColumn.setCellValueFactory(cellData ->
          new javafx.beans.property.SimpleObjectProperty<>(cellData.getValue().getPriority()));
      priorityColumn.setCellFactory(ComboBoxTableCell.forTableColumn(FXCollections.observableArrayList(initialPriorityList)));
    });


    // Attach Button Actions
    addTaskBtn.setOnAction(this::handleAddTask);
    distributeBtn.setOnAction(e -> Logger.info("Distribute tasks clicked!"));
    closeDoneBtn.setOnAction(e -> Logger.info("Close done tasks clicked!"));
    addUserBtn.setOnAction(this::handleAddUser);

    statusColumn.setOnEditCommit(event -> {
        TaskDTO task = event.getRowValue();
        task.setStatus(event.getNewValue());
        dataHandler.updateTask(task);
    });
    priorityColumn.setOnEditCommit(event -> {
      TaskDTO task = event.getRowValue();
      task.setPriority(event.getNewValue());
      dataHandler.updateTask(task);
    });

    searchField.setOnAction(this::handleSearchDescription);
  }

  // Method to receive the HouseholdDTO object
  public void setHousehold(HouseholdDTO household) {
    this.household = household;
    System.out.println("Household received: " + household.getName()); // Debugging
  }

  private ObservableList<TaskDTO> getAllTasks() {
    return FXCollections.observableArrayList(dataHandler.getAllTasks(household.getId()));
  }

  private ObservableList<UserDTO> getAllUsers() {
    return FXCollections.observableArrayList(dataHandler.getAllUsersByHousehold(household.getId()));
  }
  private void handleAddUser(ActionEvent event){
    Dialog<UserDTO> dialog = createBasicDialog("Add New User", "Enter user details");
    GridPane grid = createDialogGrid();

    TextField nameField = new TextField();
    nameField.setPromptText("User name");

    ComboBox<UserDTO> nameComboBox = createComboBox(initialUserList);

    Label errorLabel = new Label();
    grid.add(new Label("Name"),0,0);
    grid.add(nameField, 0, 0);

      ButtonType addButton = new ButtonType("Add",ButtonBar.ButtonData.OK_DONE);
      setupDialogButtons(dialog, addButton);

      dialog.getDialogPane().setContent(grid);

      dialog.getDialogPane().lookupButton(addButton).addEventFilter(ActionEvent.ACTION, e -> {
        if(nameField.getText().trim().isEmpty()){
          errorLabel.setText("Name field cannot be empty!");
          e.consume();
        }else{
          errorLabel.setText("");
        }});

      dialog.setResultConverter(dialogButton -> {
        if(dialogButton == addButton){
          return new UserDTO(0, nameField.getText(),household);
        }
        return null;
      });
      Optional<UserDTO> result = dialog.showAndWait();
        result.ifPresent(user -> {
            dataHandler.addUser(user);
            initialUserList.add(user);
            userTable.setItems(getAllUsers());
            Logger.info("User added successfully");
        });
  }

  // Handles Add Task Button
  private void handleAddTask(ActionEvent event) {
    Dialog<TaskDTO> dialog = createBasicDialog("Add New Task", "Enter task details");
    GridPane grid = createDialogGrid();

    // Form Inputs
    TextField descriptionField = new TextField();
    descriptionField.setPromptText("Task description");
    ComboBox<PriorityDTO> priorityCombo = createComboBox(initialPriorityList);
    ComboBox<UserDTO> userCombo = createComboBox(initialUserList);
    Label errorLabel = new Label();

    // Add Elements to Grid
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

    // Prevent Empty Description
    dialog.getDialogPane().lookupButton(addButton).addEventFilter(ActionEvent.ACTION, e -> {
      if (descriptionField.getText().trim().isEmpty()) {
        errorLabel.setText("Description cannot be empty!");
        e.consume(); // Prevents dialog from closing
      } else {
        errorLabel.setText(""); // Clear error
      }
    });

    dialog.setResultConverter(dialogButton -> {
      if (dialogButton == addButton) {
        return new TaskDTO(descriptionField.getText(), household, priorityCombo.getValue(), userCombo.getValue());
      }
      return null;
    });

    Optional<TaskDTO> result = dialog.showAndWait();
    result.ifPresent(task -> {
      dataHandler.addTask(task);
      taskTable.setItems(getAllTasks());
      Logger.info("Task added successfully");
    });
  }

  private void handleSearchDescription(ActionEvent event) {
    String userQuery = searchField.getText();
    List<TaskDTO> query = dataHandler.getAllTasks(household.getId(), userQuery);
    ObservableList<TaskDTO> result = FXCollections.observableArrayList(query);
    Logger.info("Search tasks for: " + searchField.getText());
    taskTable.setItems(result);
  }


  private void handleNextTaskPage(ActionEvent event) {
    taskPageCount++;
    ObservableList<TaskDTO> result =
      FXCollections.observableArrayList(dataHandler.getLimitedTasks(household.getId(),taskLimit,this.getTaskOffset()));
    taskTable.setItems(result);
  }

  private void handlePreviousTaskPage(ActionEvent event) {
    if((taskPageCount-1) == 0)
      return;

    taskPageCount--;
    ObservableList<TaskDTO> result =
      FXCollections.observableArrayList(dataHandler.getLimitedTasks(household.getId(),taskLimit,this.getTaskOffset()));
    taskTable.setItems(result);
  }

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

  private <T> ComboBox<T> createComboBox(List<T> list) {
    ComboBox<T> comboBox = new ComboBox<>();
    comboBox.setItems(FXCollections.observableArrayList(list));
    return comboBox;
  }

  private void setupDialogButtons(Dialog<?> dialog, ButtonType... buttons) {
    dialog.getDialogPane().getButtonTypes().addAll(buttons);
    dialog.getDialogPane().getButtonTypes().add(ButtonType.CANCEL);
  }

  private int getTaskOffset(){
    return (taskPageCount-1) * taskLimit;
  }
}
