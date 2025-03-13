// Language: java
package controllers;

import dbcontext.DataHandler;
import dto.*;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.ComboBoxTableCell;
import javafx.scene.input.MouseButton;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.BorderPane;
import utils.Logger;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

/**
 * The MainController class manages the primary UI interactions including handling tasks,
 * users and related context menus. It initializes the table and list views, handles
 * user and task creation, editing, deletion, and search operations.
 */
public class MainController {

  @FXML private BorderPane root;
  @FXML private TableView<TaskDTO> taskTable;
  @FXML private ListView<UserDTO> userTable;
  @FXML private TableColumn<TaskDTO, String> descriptionColumn;
  @FXML private TableColumn<TaskDTO, StatusDTO> statusColumn;
  @FXML private TableColumn<TaskDTO, PriorityDTO> priorityColumn;
  @FXML private TableColumn<TaskDTO, String> userColumn;
  @FXML private Button addTaskBtn, distributeBtn, closeDoneBtn, addUserBtn, nextTaskPage, previousTaskPage, logoutButton;
  @FXML private TextField searchField;

  private DataHandler dataHandler;
  private HouseholdDTO household;
  private List<StatusDTO> initialStatusList;
  private List<PriorityDTO> initialPriorityList;
  private List<UserDTO> initialUserList;
  private int taskLimit;
  private int taskPageCount;
  private static int numberOfContextMenus = 0;

  /**
   * Initializes the MainController by setting up UI elements, registering event handlers,
   * and initializing data for tasks and users.
   */
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
      if (event.getButton() == MouseButton.SECONDARY && user != null) {
        Logger.info("User clicked: " + user.getName());
        userClickedMenu(event, user);
      } else {
        Logger.info("Another button was clicked: " + event.getButton());
      }
    });
    taskTable.setOnMouseClicked(event -> {
      TaskDTO task = taskTable.getSelectionModel().getSelectedItem();
      if (event.getButton() == MouseButton.SECONDARY && task != null) {
        Logger.info("Task clicked: " + task.getDescription());
        taskClickedMenu(event, task);
      }
    });
  }

  /**
   * Displays a context menu for user actions such as delete and edit.
   *
   * @param event the mouse event triggering the menu
   * @param user  the user object associated with the event
   */
  public void userClickedMenu(MouseEvent event, UserDTO user) {
    ContextMenu contextMenu = new ContextMenu();
    contextMenu.setAutoHide(true);

    MenuItem deleteUser = new MenuItem("Delete User");
    MenuItem editUser = new MenuItem("Edit User");
    EventHandler<ActionEvent> handler = e -> {
      if (e.getSource() == deleteUser) {
        dataHandler.deleteUser(user);
        userTable.setItems(FXCollections.observableArrayList(dataHandler.getAllUsersByHousehold(household.getId())));
      } else if (e.getSource() == editUser) {
        Optional<UserDTO> result = UserDialogFactory.createEditUserDialog(user, household).showAndWait();
        result.ifPresent(editedUser -> {
          dataHandler.editUser(editedUser);
          userTable.setItems(getAllUsers());
        });
      }
      contextMenu.hide();
    };
    deleteUser.setOnAction(handler);
    editUser.setOnAction(handler);
    contextMenu.getItems().addAll(deleteUser, editUser);

    EventHandler<MouseEvent> sceneMouseHandler = e -> {
      if (contextMenu.isShowing()) {
        contextMenu.hide();
      }
    };

    contextMenu.setOnHidden(e -> {
      userTable.getScene().removeEventFilter(MouseEvent.MOUSE_CLICKED, sceneMouseHandler);
    });
    userTable.getScene().addEventFilter(MouseEvent.MOUSE_CLICKED, sceneMouseHandler);
    contextMenu.show(userTable, event.getScreenX(), event.getScreenY());
  }

  /**
   * Displays a context menu for task actions such as delete and edit.
   *
   * @param event the mouse event triggering the menu
   * @param task  the task object associated with the event
   */
  public void taskClickedMenu(MouseEvent event, TaskDTO task) {
    ContextMenu contextMenu = new ContextMenu();
    contextMenu.setAutoHide(true);

    MenuItem deleteTask = new MenuItem("Delete Task");
    MenuItem editTask = new MenuItem("Edit Task");
    EventHandler<ActionEvent> handler = e -> {
      if (e.getSource() == deleteTask) {
        dataHandler.deleteTask(task);
        taskTable.setItems(FXCollections.observableArrayList(dataHandler.getAllTasksByHouseHold(household.getId())));
      } else if (e.getSource() == editTask) {
        Optional<TaskDTO> result = TaskDialogFactory
                .createEditTaskDialog(task, household, initialStatusList, initialPriorityList, initialUserList)
                .showAndWait();
        result.ifPresent(editedTask -> {
          dataHandler.editTask(editedTask);
          taskTable.setItems(getAllTasks());
        });
      }
      contextMenu.hide();
    };

    deleteTask.setOnAction(handler);
    editTask.setOnAction(handler);
    contextMenu.getItems().addAll(deleteTask, editTask);

    EventHandler<MouseEvent> sceneMouseHandler = e -> {
      if (contextMenu.isShowing()) {
        contextMenu.hide();
      }
    };
    contextMenu.setOnHidden(e -> {
      taskTable.getScene().removeEventFilter(MouseEvent.MOUSE_CLICKED, sceneMouseHandler);
    });
    taskTable.getScene().addEventFilter(MouseEvent.MOUSE_CLICKED, sceneMouseHandler);
    contextMenu.show(taskTable, event.getScreenX(), event.getScreenY());
  }

  /**
   * Sets the household context for the current session.
   *
   * @param household the HouseholdDTO to be set
   */
  public void setHousehold(HouseholdDTO household) {
    this.household = household;
    Logger.info("Household received: " + household.getName());
  }

  /**
   * Retrieves all tasks for the current household.
   *
   * @return an ObservableList of TaskDTO objects representing the tasks
   */
  private ObservableList<TaskDTO> getAllTasks() {
    return FXCollections.observableArrayList(dataHandler.getAllTasksByHouseHold(household.getId()));
  }

  /**
   * Retrieves all users for the current household.
   *
   * @return an ObservableList of UserDTO objects representing the users
   */
  public ObservableList<UserDTO> getAllUsers() {
    return FXCollections.observableArrayList(dataHandler.getAllUsersByHousehold(household.getId()));
  }

  /**
   * Handles the addition of a new user by displaying a dialog.
   *
   * @param event the action event triggered by clicking the add user button
   */
  private void handleAddUser(ActionEvent event) {
    Optional<UserDTO> result = UserDialogFactory.createAddUserDialog(household).showAndWait();
    result.ifPresent(user -> {
      dataHandler.addUser(user);
      initialUserList.add(user);
      userTable.setItems(getAllUsers());
      Logger.info("User added successfully");
    });
  }

  /**
   * Handles the addition of a new task by displaying a dialog.
   *
   * @param event the action event triggered by clicking the add task button
   */
  private void handleAddTask(ActionEvent event) {
    Optional<TaskDTO> result = TaskDialogFactory.createAddTaskDialog(household, initialPriorityList, initialUserList)
            .showAndWait();
    result.ifPresent(task -> {
      dataHandler.addTask(task);
      taskTable.setItems(getAllTasks());
      Logger.info("Task added successfully");
    });
  }

  /**
   * Handles the search action for filtering tasks based on the description.
   *
   * @param event the action event triggered by the search field
   */
  private void handleSearchDescription(ActionEvent event) {
    String userQuery = searchField.getText();
    List<TaskDTO> query = dataHandler.getAllTasksByHouseHold(household.getId(), userQuery);
    ObservableList<TaskDTO> result = FXCollections.observableArrayList(query);
    Logger.info("Search tasks for: " + userQuery);
    taskTable.setItems(result);
  }

  /**
   * Calculates the task offset for pagination based on the current page count.
   *
   * @return the offset as an integer value for pagination
   */
  private int getTaskOffset(){
    return (taskPageCount - 1) * taskLimit;
  }
}