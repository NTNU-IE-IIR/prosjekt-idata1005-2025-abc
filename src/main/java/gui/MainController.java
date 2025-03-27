// Language: java
package gui;

import controllers.TaskDialogFactory;
import controllers.UserDialogFactory;
import gui.components.TaskList;
import gui.components.UserList;
import dbcontext.DataHandler;
import dto.*;
import gui.components.Toast;
import javafx.beans.binding.Bindings;
import javafx.application.Platform;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;

import java.util.function.Function;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.*;
import javafx.stage.Stage;
import utils.Logger;
import utils.Message;
import utils.MessageTypeEnum;

import java.io.IOException;
import java.util.*;

/**
 * The MainController class manages the primary UI interactions including handling tasks,
 * users and related context menus. It initializes the table and list views, handles
 * user and task creation, editing, deletion, and search operations.
 */
public class MainController {

  @FXML private BorderPane root;
  @FXML private ListView<TaskDTO> taskTable;
  @FXML private ListView<UserDTO> userTable;
  @FXML private TableColumn<TaskDTO, String> descriptionColumn;
  @FXML private TableColumn<TaskDTO, StatusDTO> statusColumn;
  @FXML private TableColumn<TaskDTO, PriorityDTO> priorityColumn;
  @FXML private TableColumn<TaskDTO, String> userColumn;
  @FXML private Button addTaskBtn, distributeBtn, closeDoneBtn, addUserBtn, viewAllTasks, logoutButton;
  @FXML private TextField searchField;
  @FXML private Label userCount;
  @FXML private Label sortTaskDescription, sortTaskStatus, sortTaskPriority, sortTaskOwner;


  @FXML private Region spacer;  // Reference to the first spacer
  @FXML private Region spacer2; // Reference to the second spacer

  private DataHandler dataHandler;
  private HouseholdDTO household;
  private ObservableList<TaskDTO> taskList;
  private ObservableList<StatusDTO> statusList;
  private ObservableList<PriorityDTO> priorityList;
  private ObservableList<UserDTO> userList;
  private Label lastClickedTaskOrder = null;
  private SimpleBooleanProperty isProgrammaticChange;

  /**
   * Initializes the MainController by setting up UI elements, registering event handlers,
   * and initializing data for tasks and users.
   */
  @FXML
  public void initialize() {
    root.getStylesheets().add(Objects.requireNonNull(getClass().getResource("/gui/styles/main.css")).toExternalForm());
    root.getStylesheets().add(Objects.requireNonNull(getClass().getResource("/gui/styles/global.css")).toExternalForm());
    dataHandler = new DataHandler();
    isProgrammaticChange = new SimpleBooleanProperty(false);

    // Ensure both spacers expand to push elements apart
    HBox.setHgrow(spacer, Priority.ALWAYS);
    HBox.setHgrow(spacer2, Priority.ALWAYS);

    userTable.setFixedCellSize(60);
    taskTable.setCellFactory(param ->
      new TaskList(statusList, priorityList, userList, household, isProgrammaticChange, this::handleEditTask));

    userTable.setFixedCellSize(70);
    userTable.setCellFactory(param ->
      new UserList(this::handleUserClicked, this::handleUserEdit, this::handleUserDelete));

    Platform.runLater(() -> {
      statusList = FXCollections.observableArrayList(dataHandler.getAllStatus());
      priorityList = FXCollections.observableArrayList(dataHandler.getAllPriorities());
      taskList = FXCollections.observableArrayList(dataHandler.getAllTasksByHouseHold(household.getId()));
      userList =  FXCollections.observableArrayList(dataHandler.getAllUsersByHousehold(household.getId()));

      taskTable.setItems(taskList);
      userTable.setItems(userList);

      // Bind the label's text to the size of the list
      userCount.textProperty().bind(
          Bindings.size(userList).asString()
      );
      userTable.prefHeightProperty().bind(
          Bindings.size(userList).multiply(70).add(20)
      );
      taskTable.prefHeightProperty().bind(
        Bindings.size(taskList).multiply(60).add(100)
      );

    });

    addTaskBtn.setOnAction(this::handleAddTask);
    distributeBtn.setOnAction(e -> Logger.info("Distribute tasks clicked!"));
    closeDoneBtn.setOnAction(e -> Logger.info("Close done tasks clicked!"));
    addUserBtn.setOnAction(this::handleAddUser);
    searchField.setOnAction(this::handleSearchDescription);
    viewAllTasks.setOnAction(this::handleViewAllTask);

  }

  @FXML
  private void handleSortClick(MouseEvent event) {
    Label clickedLabel = (Label) event.getSource();
    String currentSort = clickedLabel.getId();

    // Detect if the user clicked the same label twice (-> reverse sort).
    boolean reverse = clickedLabel.equals(lastClickedTaskOrder);

    // Remove any existing "▲"/"▼" indicators from labels before applying a new one.
    resetSortLabels();

    // Map each label ID to its corresponding sort method.
    Map<String, Runnable> sortActions = Map.of(
      "sortTaskDescription", () -> sortTaskDescription(reverse),
      "sortTaskStatus", () -> sortTaskStatus(reverse),
      "sortTaskPriority", () -> sortTaskPriority(reverse),
      "sortTaskOwner", () -> sortTaskOwner(reverse)
    );

    // Run the sort function for the clicked label, if present.
    sortActions.getOrDefault(currentSort, () -> {}).run();

    // Update the clicked label with the appropriate arrow.
    clickedLabel.setText(getLabelText(clickedLabel.getText(), reverse));

    // Update lastClickedTaskOrder: set it to null if the user clicked the same label again
    // (meaning next time it will do normal sort), or set it to this label otherwise.
    lastClickedTaskOrder = reverse ? null : clickedLabel;
  }

  /**
   * Removes "▲" and "▼" from all labels to reset their displayed state.
   */
  private void resetSortLabels() {
    List<Label> sortLabels = List.of(sortTaskDescription, sortTaskStatus, sortTaskPriority, sortTaskOwner);
    sortLabels.forEach(label -> label.setText(label.getText()
      .replace(" ▲", "")
      .replace(" ▼", "")
    ));
  }

  /**
   * Appends an up or down arrow, depending on the sort order (reverse or not).
   */
  private String getLabelText(String baseText, boolean reverse) {
    // Remove old arrows just in case
    baseText = baseText.replace(" ▲", "").replace(" ▼", "");
    return baseText + (reverse ? " ▲" : " ▼");
  }

  /**
   * A generic helper to apply the same sorting logic for different fields.
   * @param keyExtractor  function to get the field from TaskDTO (e.g., TaskDTO::getStatus).
   * @param keyComparator comparator for that field (e.g., Comparator.nullsLast(…)).
   * @param reverse       true for reversed sort order, false otherwise.
   */
  private <T> void sortTaskList(Function<TaskDTO, T> keyExtractor,
                                Comparator<? super T> keyComparator,
                                boolean reverse) {

    Comparator<TaskDTO> comparator = Comparator.comparing(keyExtractor, keyComparator);
    if (reverse) {
      comparator = comparator.reversed();
    }
    taskList.sort(comparator);
  }

  private void sortTaskOwner(boolean reverse) {
    sortTaskList(
      TaskDTO::getUser,
      Comparator.nullsLast(
        Comparator.comparing(UserDTO::getName, String.CASE_INSENSITIVE_ORDER)
      ),
      reverse
    );
  }

  private void sortTaskStatus(boolean reverse) {
    sortTaskList(
      TaskDTO::getStatus,
      Comparator.nullsLast(
        Comparator.comparing(StatusDTO::getName, String.CASE_INSENSITIVE_ORDER)
      ),
      reverse
    );
  }

  private void sortTaskPriority(boolean reverse) {
    sortTaskList(
      TaskDTO::getPriority,
      Comparator.nullsLast(
        Comparator.comparing(PriorityDTO::getName, String.CASE_INSENSITIVE_ORDER)
      ),
      reverse
    );
  }

  private void sortTaskDescription(boolean reverse) {
    // For simple String fields (no null checks needed in your example):
    sortTaskList(
      TaskDTO::getDescription,
      String.CASE_INSENSITIVE_ORDER,
      reverse
    );
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
    Optional<TaskDTO> result = TaskDialogFactory.createAddTaskDialog(household, priorityList, userList)
            .showAndWait();
    result.ifPresent(task -> {
      task.setStatus(new StatusDTO(3, "Not started"));
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

  private void handleViewAllTask(ActionEvent event) {
    viewAllTasks.setVisible(false);
    try {
      taskList.setAll(getAllTasks());
    }catch (Exception e){
      Toast.showToast(root, new Message<>(MessageTypeEnum.ERROR, "An error occurred: "+e.getMessage()),-1);
    }
    Toast.showToast(root, new Message<>(MessageTypeEnum.INFO, "Viewing all tasks"),3000);
  }

  public void handleEditTask(TaskDTO task) {
    dataHandler.editTask(task); // TODO error handling

    List<TaskDTO> updatedList = new ArrayList<>(taskList);
    updatedList.replaceAll(t -> t.getId() == task.getId() ? task : t);
    taskList.setAll(updatedList); // Triggers UI refresh
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
    FXMLLoader loader = new FXMLLoader(getClass().getResource("/gui/Login.fxml"));
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
    Message<List<TaskDTO>> queryResult = dataHandler.getUserTasks(user);
    viewAllTasks.setVisible(true);
    if(queryResult.getType() == MessageTypeEnum.ERROR){
      Toast.showToast(root, queryResult, 5000);
      Logger.error(queryResult.getMessage());
      return;
    }
    queryResult.setType(MessageTypeEnum.INFO);
    Toast.showToast(root, queryResult, 3000);
    taskList.setAll(queryResult.getResult());
    taskTable.refresh(); // Forces UI refresh
  }

  public void handleUserEdit(UserDTO user) {
    Optional<UserDTO> result = UserDialogFactory.createEditUserDialog(user, household).showAndWait();
    result.ifPresent(editedUser -> {
      isProgrammaticChange.setValue(true);
      Message<Void> queryResult = dataHandler.editUser(editedUser);

      if(queryResult.getType() == MessageTypeEnum.ERROR){
        Toast.showToast(root, queryResult, -1);
        return;
      }
      Toast.showToast(root, queryResult, 3000);

      List<UserDTO> updatedList = new ArrayList<>(userList);
      updatedList.replaceAll(u -> u.getId() == editedUser.getId() ? editedUser : u);


      List<TaskDTO> updatedTaskList = new ArrayList<>(taskList);
      updatedTaskList.replaceAll(u -> {
        if(u.getUser() != null){
          if(u.getUser().getId() == editedUser.getId()) {
            u.setUser(editedUser);
          }
        }
        return u;
      });
      userList.setAll(updatedList);
      taskList.setAll(updatedTaskList);
      isProgrammaticChange.setValue(false);
    });
  }
  public void handleUserDelete(UserDTO user) {
    isProgrammaticChange.setValue(true);
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

      List<TaskDTO> updatedTaskList = new ArrayList<>(taskList);
      updatedTaskList.replaceAll(u -> {
        if(u.getUser() != null){
          if(u.getUser().getId() == user.getId()) {
            u.setUser(null);
          }
        }
        return u;
      });
      taskList.setAll(updatedTaskList);
    }
    isProgrammaticChange.setValue(false);
  }
}