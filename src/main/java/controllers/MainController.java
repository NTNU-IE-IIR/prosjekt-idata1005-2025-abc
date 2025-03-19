// Language: java
package controllers;

import controllers.helpers.UserListCellController;
import dbcontext.DataHandler;
import dto.*;
import javafx.beans.binding.Bindings;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.ComboBoxTableCell;
import javafx.scene.input.MouseButton;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.*;
import javafx.stage.Stage;
import utils.Logger;
import utils.Message;
import utils.MessageTypeEnum;

import java.io.IOException;
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
  @FXML private Label userCount;

  @FXML private Region spacer;  // Reference to the first spacer
  @FXML private Region spacer2; // Reference to the second spacer

  private DataHandler dataHandler;
  private HouseholdDTO household;
  private ObservableList<TaskDTO> taskList;
  private List<StatusDTO> initialStatusList;
  private List<PriorityDTO> initialPriorityList;
  private ObservableList<UserDTO> userList;

  /**
   * Initializes the MainController by setting up UI elements, registering event handlers,
   * and initializing data for tasks and users.
   */
  @FXML
  public void initialize() {
    root.getStylesheets().add(Objects.requireNonNull(getClass().getResource("/gui/main.css")).toExternalForm());
    root.getStylesheets().add(Objects.requireNonNull(getClass().getResource("/gui/global.css")).toExternalForm());
    dataHandler = new DataHandler();


    // Ensure both spacers expand to push elements apart
    HBox.setHgrow(spacer, Priority.ALWAYS);
    HBox.setHgrow(spacer2, Priority.ALWAYS);

    taskTable.setEditable(true);
    descriptionColumn.setCellValueFactory(cellData ->
            new javafx.beans.property.SimpleStringProperty(cellData.getValue().getDescription()));
    statusColumn.setCellValueFactory(cellData ->
            new javafx.beans.property.SimpleObjectProperty<>(cellData.getValue().getStatus()));
    userColumn.setCellValueFactory(cellData ->
            new javafx.beans.property.SimpleStringProperty(
                    cellData.getValue().getUser() != null ? cellData.getValue().getUser().getName() : "Unassigned"
            ));

    userTable.setFixedCellSize(70);
    userTable.setCellFactory(param -> new UserListCellController(this::handleUserClicked, this::handleUserEdit, this::handleUserDelete));

    Platform.runLater(() -> {
      initialStatusList = dataHandler.getAllStatus();
      initialPriorityList = dataHandler.getAllPriorities();
      taskList = FXCollections.observableArrayList(dataHandler.getAllTasksByHouseHold(household.getId()));
      userList =  FXCollections.observableArrayList(dataHandler.getAllUsersByHousehold(household.getId()));

      taskTable.setItems(taskList);
      userTable.setItems(userList);

      statusColumn.setCellFactory(ComboBoxTableCell.forTableColumn(FXCollections.observableArrayList(initialStatusList)));
      priorityColumn.setCellValueFactory(cellData ->
              new javafx.beans.property.SimpleObjectProperty<>(cellData.getValue().getPriority()));
      priorityColumn.setCellFactory(ComboBoxTableCell.forTableColumn(FXCollections.observableArrayList(initialPriorityList)));

      // Bind the label's text to the size of the list
      userCount.textProperty().bind(
          Bindings.size(userList).asString()
      );
      userTable.prefHeightProperty().bind(
          Bindings.size(userList).multiply(70).add(20)
      );
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

    taskTable.setOnMouseClicked(event -> {
      TaskDTO task = taskTable.getSelectionModel().getSelectedItem();
      if (event.getButton() == MouseButton.SECONDARY && task != null) {
        Logger.info("Task clicked: " + task.getDescription());
        taskClickedMenu(event, task);
      }
    });
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
                .createEditTaskDialog(task, household, initialStatusList, initialPriorityList, userList)
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
      Message<Integer> queryResult = dataHandler.addUser(user);
      if(queryResult.getType() == MessageTypeEnum.SUCCESS){
        // Set autogenerated id to user
        user.setId(queryResult.getResult());
        // Update userList
        userList.add(user);
      }
      Logger.info("User added successfully");
    });
  }

  /**
   * Handles the addition of a new task by displaying a dialog.
   *
   * @param event the action event triggered by clicking the add task button
   */
  private void handleAddTask(ActionEvent event) {
    Optional<TaskDTO> result = TaskDialogFactory.createAddTaskDialog(household, initialPriorityList, userList)
            .showAndWait();
    result.ifPresent(task -> {
      Message<Void> resultMsg = dataHandler.addTask(task);
      if(resultMsg.getType() == MessageTypeEnum.ERROR){
        Logger.error(resultMsg.getMessage());
      }
      else if(resultMsg.getType() == MessageTypeEnum.SUCCESS){
        Logger.info("Task added successfully");
        taskList.add(task);
      }
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
   * Handles the logout action for pressing button "logout"
   *
   * @param event the action event triggered by the logout button
   */
  public void handleLogout(ActionEvent event) throws IOException {
    FXMLLoader loader = new FXMLLoader(getClass().getResource("/gui/login.fxml"));
    Parent loginRoot = loader.load();

    Scene loginScene = new Scene(loginRoot, 800, 500);
    Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
    stage.setScene(loginScene);
    stage.setTitle("Login Page");

    stage.setMaximized(false);
    stage.setWidth(800);
    stage.setHeight(500);

    stage.centerOnScreen();
    stage.show();
  }

  public void handleUserClicked(UserDTO user) {
    System.out.println("User clicked");
  }

  public void handleUserEdit(UserDTO user) {
    Optional<UserDTO> result = UserDialogFactory.createEditUserDialog(user, household).showAndWait();
    result.ifPresent(editedUser -> {
      dataHandler.editUser(editedUser);
      userList.replaceAll(u -> u.getId() == editedUser.getId() ? editedUser : u);
    });
  }
  public void handleUserDelete(UserDTO user) {
    // Create Confirmation Alert
    Logger.info(String.valueOf(user.getId()));
    Logger.info(user.getName());

    Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
    alert.setTitle("Delete User");
    alert.setHeaderText("Are you sure you want to delete this user?");
    alert.setContentText("User: " + user.getName());

    // Show alert and wait for user response
    Optional<ButtonType> result = alert.showAndWait();

    // Check if user clicked OK
    if (result.isPresent() && result.get() == ButtonType.OK) {
      dataHandler.deleteUser(user);   // Delete from database
      userList.remove(user);          // Remove from UI list
      taskList.setAll(getAllTasks()); // Pull the changes to the tasks
      taskTable.setItems(taskList);
    }

  }
}