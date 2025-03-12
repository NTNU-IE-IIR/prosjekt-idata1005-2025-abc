// Language: java
package controllers;

import dbcontext.DataHandler;
import dto.*;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.ComboBoxTableCell;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.BorderPane;
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
    root.getStylesheets().add(Objects.requireNonNull(getClass().getResource("/gui/main.css")).toExternalForm());
    dataHandler = new DataHandler();
    taskLimit = 4;
    taskPageCount = 1;
    taskTable.setEditable(true);
    descriptionColumn.setCellValueFactory(cellData ->
            new javafx.beans.property.SimpleStringProperty(cellData.getValue().getDescription()));

    statusColumn.setCellValueFactory(cellData ->
            new javafx.beans.property.SimpleObjectProperty<>(cellData.getValue().getStatus()));

    userColumn.setCellValueFactory(cellData ->
            new javafx.beans.property.SimpleStringProperty(
                    cellData.getValue().getUser() != null ? cellData.getValue().getUser().getName() : "Unassigned"
            ));

    Platform.runLater(() -> {
      initialStatusList = dataHandler.getAllStatus();
      initialPriorityList = dataHandler.getAllPriorities();
      initialUserList = dataHandler.getAllUsersByHousehold(household.getId());

      taskTable.setItems(getAllTasks());
      userTable.setItems(getAllUsers());

      statusColumn.setCellFactory(ComboBoxTableCell.forTableColumn(FXCollections.observableArrayList(initialStatusList)));
      priorityColumn.setCellValueFactory(cellData ->
              new javafx.beans.property.SimpleObjectProperty<>(cellData.getValue().getPriority()));
      priorityColumn.setCellFactory(ComboBoxTableCell.forTableColumn(FXCollections.observableArrayList(initialPriorityList)));
    });

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

    userTable.setOnMouseClicked(event -> {
      UserDTO user = userTable.getSelectionModel().getSelectedItem();
      if (user != null) {
        Logger.info("User clicked: " + user.getName());
        userClickedMenu(event, user);
      }
    });
    taskTable.setOnMouseClicked(event -> {
      TaskDTO task = taskTable.getSelectionModel().getSelectedItem();
      if (task != null) {
        Logger.info("Task clicked: " + task.getDescription());
        taskClickedMenu(event, task);
      }
    });
  }

  public void userClickedMenu(MouseEvent event, UserDTO user) {
    ContextMenu contextMenu = new ContextMenu();
    contextMenu.setAutoHide(true);

    MenuItem deleteUser = new MenuItem("Delete User");
    deleteUser.setOnAction(e -> {
      dataHandler.deleteUser(user);
      userTable.setItems(FXCollections.observableArrayList(dataHandler.getAllUsersByHousehold(household.getId())));
    });

    MenuItem editUser = new MenuItem("Edit User");
    editUser.setOnAction(e -> {
      Optional<UserDTO> result = UserDialogFactory.createEditUserDialog(user, household).showAndWait();
      result.ifPresent(editedUser -> {
        dataHandler.editUser(editedUser);
        userTable.setItems(getAllUsers());
      });
    });

    contextMenu.getItems().addAll(deleteUser, editUser);
    contextMenu.show(userTable, event.getScreenX(), event.getScreenY());
  }

  public void taskClickedMenu(MouseEvent event, TaskDTO task) {
    ContextMenu contextMenu = new ContextMenu();
    contextMenu.setAutoHide(true);

    MenuItem deleteTask = new MenuItem("Delete Task");
    deleteTask.setOnAction(e -> {
      dataHandler.deleteTask(task);
      taskTable.setItems(FXCollections.observableArrayList(dataHandler.getAllTasksByHouseHold(household.getId())));
    });

    MenuItem editTask = new MenuItem("Edit Task");
    editTask.setOnAction(e -> {
      Optional<TaskDTO> result = TaskDialogFactory.createEditTaskDialog(task, household, initialStatusList, initialPriorityList, initialUserList).showAndWait();
      result.ifPresent(editedTask -> {
        dataHandler.editTask(editedTask);
        taskTable.setItems(getAllTasks());
      });
    });

    contextMenu.getItems().addAll(deleteTask, editTask);
    contextMenu.show(taskTable, event.getScreenX(), event.getScreenY());
  }

  public void setHousehold(HouseholdDTO household) {
    this.household = household;
    Logger.info("Household received: " + household.getName());
  }

  private ObservableList<TaskDTO> getAllTasks() {
    return FXCollections.observableArrayList(dataHandler.getAllTasksByHouseHold(household.getId()));
  }

  public ObservableList<UserDTO> getAllUsers() {
    return FXCollections.observableArrayList(dataHandler.getAllUsersByHousehold(household.getId()));
  }

  private void handleAddUser(ActionEvent event){
    Optional<UserDTO> result = UserDialogFactory.createAddUserDialog(household).showAndWait();
    result.ifPresent(user -> {
      dataHandler.addUser(user);
      initialUserList.add(user);
      userTable.setItems(getAllUsers());
      Logger.info("User added successfully");
    });
  }

  private void handleAddTask(ActionEvent event) {
    Optional<TaskDTO> result = TaskDialogFactory.createAddTaskDialog(household, initialPriorityList, initialUserList).showAndWait();
    result.ifPresent(task -> {
      dataHandler.addTask(task);
      taskTable.setItems(getAllTasks());
      Logger.info("Task added successfully");
    });
  }

  private void handleSearchDescription(ActionEvent event) {
    String userQuery = searchField.getText();
    List<TaskDTO> query = dataHandler.getAllTasksByHouseHold(household.getId(), userQuery);
    ObservableList<TaskDTO> result = FXCollections.observableArrayList(query);
    Logger.info("Search tasks for: " + userQuery);
    taskTable.setItems(result);
  }

  private int getTaskOffset(){
    return (taskPageCount-1) * taskLimit;
  }
}