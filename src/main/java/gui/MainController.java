// Language: java
package gui;

import com.mysql.cj.log.Log;
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
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.*;
import javafx.stage.Stage;
import utils.Logger;
import utils.Message;
import utils.MessageTypeEnum;

import java.io.IOException;
import java.util.*;
import java.util.function.Function;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

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
  @FXML private Button addTaskBtn, distributeBtn, closeDoneBtn, addUserBtn, viewAllTasks, logoutButton, doneTasksBtn;
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
  private String userQuery="";

  // For storing the original unsorted order of tasks.
  private List<TaskDTO> originalTaskList;
  // Used for tracking sort clicks for each label (cycle: default -> normal -> reverse -> default).
  private final Map<Label, Integer> sortClickCounts = new HashMap<>();

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

    // Set cell factories for list views.
    userTable.setFixedCellSize(70);
    userTable.setCellFactory(param -> new UserList(this::handleUserClicked, this::handleUserEdit, this::handleUserDelete));

    taskTable.setFixedCellSize(60);
    taskTable.setCellFactory(param -> new TaskList(statusList, priorityList, userList, household, isProgrammaticChange, this::handleEditTask, this::handleDeleteTask));

    Platform.runLater(() -> {
      statusList = FXCollections.observableArrayList(dataHandler.getAllStatus());
      priorityList = FXCollections.observableArrayList(dataHandler.getAllPriorities());
      taskList = FXCollections.observableArrayList(dataHandler.getAllTasksByHouseHold(household.getId()));
      userList = FXCollections.observableArrayList(dataHandler.getAllUsersByHousehold(household.getId()));

      // Save original order
      originalTaskList = new ArrayList<>(taskList);

      taskTable.setItems(taskList);
      userTable.setItems(userList);

      // Bind the label's text to the size of the list
      userCount.textProperty().bind(Bindings.size(userList).asString());
      userTable.prefHeightProperty().bind(Bindings.size(userList).multiply(70).add(20));
      taskTable.prefHeightProperty().bind(Bindings.size(taskList).multiply(60).add(100));
    });

    // Register button event handlers.
    addTaskBtn.setOnAction(this::handleAddTask);
    distributeBtn.setOnAction(e -> Logger.info("Distribute tasks clicked!"));
    closeDoneBtn.setOnAction(this::closeDoneTasks);
    addUserBtn.setOnAction(this::handleAddUser);
    searchField.setOnAction(this::handleSearchDescription);
    viewAllTasks.setOnAction(this::handleViewAllTask);
    doneTasksBtn.setOnAction(this::showClosedTasks);

  }

  private void showClosedTasks(ActionEvent actionEvent) {
    List<TaskDTO> query = dataHandler.getClosedTasks(household.getId());
    if(query.isEmpty()){
      Toast.showToast(root, new Message<>(MessageTypeEnum.INFO, "No closed tasks"), 3000);
    }else{
      Toast.showToast(root, new Message<>(MessageTypeEnum.INFO, "Viewing closed tasks"), 3000);
      viewAllTasks.setVisible(true);
      taskList.setAll(dataHandler.getClosedTasks(household.getId()));
      originalTaskList = new ArrayList<>(taskList);
      taskTable.refresh();
      sortTaskStatus.setText("Date");
      resetSortActions();
    }
  }

  private void closeDoneTasks(ActionEvent actionEvent) {
    List<TaskDTO> doneTasks = new ArrayList<>();
    taskList.forEach(task -> {
      if(task.getStatus().getId() == 1){
        doneTasks.add(task);
      }
    });
    for(TaskDTO task : doneTasks){
      Message<Void> queryResult = dataHandler.closeDoneTasks(task);
      if(doneTasks.isEmpty()){
        Toast.showToast(root, new Message<>(MessageTypeEnum.INFO, "No tasks to close"), 3000);
      }else{
        Toast.showToast(root, new Message<>(MessageTypeEnum.INFO, "Tasks closed successfully"),3000);
      }
    }
    taskList.setAll(dataHandler.getAllTasksByHouseHold(household.getId()));
    originalTaskList = new ArrayList<>(taskList);
    taskTable.refresh();
  }

  /**
   * Handles click events on sort labels. Cycles through three states:
   * 0: Default order; 1: Normal sort; 2: Reverse sort.
   */
  @FXML
  private void handleSortClick(MouseEvent event) {
    Label clickedLabel = (Label) event.getSource();
    String currentSort = clickedLabel.getId();

    // Update click count for the clicked label.
    int clickCount = sortClickCounts.getOrDefault(clickedLabel, 0);
    // Cycle through: 0 -> 1 -> 2 -> 0
    clickCount = (clickCount + 1) % 3;
    sortClickCounts.put(clickedLabel, clickCount);

    // Reset arrows on all labels.
    resetSortLabels();

    // Third press: revert to default (unsorted) order.
    if (clickCount == 0) {
      // Local sort within search parameters
      if (!userQuery.isEmpty()) {
        String regex = ".*" + Pattern.quote(userQuery) + ".*";
        Pattern pattern = Pattern.compile(regex, Pattern.CASE_INSENSITIVE);

        taskList.setAll(originalTaskList.stream()
          .filter(task -> {
            String description = task.getDescription();
            return description != null && pattern.matcher(description).find();
          })
          .collect(Collectors.toCollection(ArrayList::new))
        );
      }else{
        taskList.setAll(originalTaskList);
      }
      clickedLabel.setText(stripArrows(clickedLabel.getText()));
      return;
    }

    // Determine sort order: state 1 is normal, state 2 is reverse.
    boolean reverse = (clickCount == 2);

    // Map each label ID to its corresponding sort method.
    Map<String, Runnable> sortActions = Map.of(
      "sortTaskDescription", () -> sortTaskDescription(reverse),
      "sortTaskStatus", () -> sortTaskStatus(reverse),
      "sortTaskPriority", () -> sortTaskPriority(reverse),
      "sortTaskOwner", () -> sortTaskOwner(reverse)
    );

    sortActions.getOrDefault(currentSort, () -> {}).run();

    // Update the clicked label with the appropriate arrow.
    clickedLabel.setText(getLabelText(stripArrows(clickedLabel.getText()), reverse));

    // Optionally, reset click counts for other labels.
    sortClickCounts.forEach((label, count) -> {
      if (!label.equals(clickedLabel)) {
        sortClickCounts.put(label, 0);
      }
    });
  }

  /**
   * Removes arrow indicators ("▲" and " ▼") from all sort labels.
   */
  private void resetSortLabels() {
    List<Label> sortLabels = List.of(sortTaskDescription, sortTaskStatus, sortTaskPriority, sortTaskOwner);
    sortLabels.forEach(label -> label.setText(stripArrows(label.getText())));
  }

  private void resetSortActions() {
    resetSortLabels();
    sortClickCounts.clear();
  }

  /**
   * Strips arrow characters from the given text.
   */
  private String stripArrows(String text) {
    return text.replace(" ▲", "").replace(" ▼", "");
  }

  /**
   * Appends an arrow indicator to the base text depending on the sort order.
   * @param baseText the label text without arrows.
   * @param reverse  if true, appends " ▲" (reverse sort); if false, " ▼" (normal sort).
   */
  private String getLabelText(String baseText, boolean reverse) {
    return baseText + (reverse ? " ▲" : " ▼");
  }

  /**
   * A generic helper to apply the same sorting logic for different fields.
   * @param keyExtractor  function to get the field from TaskDTO (e.g., TaskDTO::getStatus).
   * @param keyComparator comparator for that field (e.g., Comparator.nullsLast(...)).
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
    Comparator<UserDTO> userComparator = Comparator.nullsLast((u1, u2) -> {
      // If one user has id -1 and the other doesn't, that user goes last.
      if (u1.getId() == -1 && u2.getId() != -1) {
        return 1;
      } else if (u1.getId() != -1 && u2.getId() == -1) {
        return -1;
      }
      // Both are either -1 or both are non -1, so compare names case-insensitively.
      return String.CASE_INSENSITIVE_ORDER.compare(u1.getName(), u2.getName());
    });
    sortTaskList(TaskDTO::getUser, userComparator, reverse);
  }

  private void sortTaskStatus(boolean reverse) {
    sortTaskList(
      TaskDTO::getStatus,
      Comparator.nullsLast(Comparator.comparing(StatusDTO::getId)),
      reverse
    );
  }

  private void sortTaskPriority(boolean reverse) {
    sortTaskList(
      TaskDTO::getPriority,
      Comparator.nullsLast(Comparator.comparing(PriorityDTO::getId)),
      !reverse
    );
  }

  private void sortTaskDescription(boolean reverse) {
    sortTaskList(
      TaskDTO::getDescription,
      String.CASE_INSENSITIVE_ORDER,
      reverse
    );
  }

  /**
   * Sets the household context for the current session.
   * @param household the HouseholdDTO to be set.
   */
  public void setHousehold(HouseholdDTO household) {
    this.household = household;
    Logger.info("Household received: " + household.getName());
  }

  /**
   * Retrieves all tasks for the current household.
   * @return an ObservableList of TaskDTO objects representing the tasks.
   */
  private ObservableList<TaskDTO> getAllTasks() {
    return FXCollections.observableArrayList(dataHandler.getAllTasksByHouseHold(household.getId()));
  }

  /**
   * Handles the addition of a new user by displaying a dialog.
   * @param event the action event triggered by clicking the add user button.
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
   * @param event the action event triggered by clicking the add task button.
   */
  private void handleAddTask(ActionEvent event) {
    Optional<TaskDTO> result = TaskDialogFactory.createAddTaskDialog(household, priorityList, userList).showAndWait();
    result.ifPresent(task -> {
      task.setStatus(new StatusDTO(3, "Not started"));
      Message<Void> resultMsg = dataHandler.addTask(task);
      if(resultMsg.getType() == MessageTypeEnum.ERROR){
        Logger.error(resultMsg.getMessage());
      } else if(resultMsg.getType() == MessageTypeEnum.SUCCESS){
        Logger.info("Task added successfully");
        taskList.add(task);
      }
    });
  }

  private void handleViewAllTask(ActionEvent event) {
    viewAllTasks.setVisible(false);
    try {
      taskList.setAll(getAllTasks());
      originalTaskList = new ArrayList<>(taskList);
    } catch (Exception e) {
      Toast.showToast(root, new Message<>(MessageTypeEnum.ERROR, "An error occurred: " + e.getMessage()), -1);
    }
    Toast.showToast(root, new Message<>(MessageTypeEnum.INFO, "Viewing all tasks"), 3000);
    sortTaskStatus.setText("Status");
    resetSortActions();
  }

  public void handleEditTask(Message<TaskDTO> message) {
    if(message.getType() == MessageTypeEnum.ERROR){
      Toast.showToast(root, message, -1);
      Logger.error("(MainController.handleEditTask) An error occurred: " + message.getMessage());
      return;
    }
    TaskDTO task = message.getResult();
    Message<Void> queryMessage = dataHandler.editTask(task);

    if(queryMessage != null && queryMessage.getType() == MessageTypeEnum.ERROR){
      Toast.showToast(root, queryMessage, -1);
      Logger.error("(MainController.handleEditTask) An error occurred: " + message.getMessage());
      return;
    }

    Toast.showToast(root, message, 5000);
    List<TaskDTO> updatedList = new ArrayList<>(taskList);
    updatedList.replaceAll(t -> t.getId() == task.getId() ? task : t);
    taskList.setAll(updatedList); // Triggers UI refresh
  }

  public void handleDeleteTask(TaskDTO task) {
    Message<Void> queryResult = dataHandler.deleteTask(task);

    if(queryResult.getType() == MessageTypeEnum.ERROR){
      Logger.error("(MainController.handleDeleteTask) An error occurred: " + queryResult.getMessage());
      Toast.showToast(root, queryResult, -1);
      return;
    }
    Toast.showToast(root, queryResult, 3000);
    taskList.remove(task);
  }

  /**
   * Handles the search action for filtering tasks based on the description.
   * @param event the action event triggered by the search field.
   */
  private void handleSearchDescription(ActionEvent event) {
    String userQuery = searchField.getText();
    // Create a regex to simulate SQL LIKE '%query%'
    String regex = ".*" + Pattern.quote(userQuery) + ".*";
    Pattern pattern = Pattern.compile(regex, Pattern.CASE_INSENSITIVE);
    ObservableList<TaskDTO> filteredList;
    if (userQuery.trim().isEmpty()) {
      // If the search query is empty, return the full list
      filteredList = FXCollections.observableArrayList(originalTaskList);
    }else {
      // Filter the list using the pattern.
      filteredList = originalTaskList.stream()
        .filter(task -> {
          String description = task.getDescription();
          return description != null && pattern.matcher(description).matches();
        })
        .collect(Collectors.toCollection(FXCollections::observableArrayList));
    }
    taskList.setAll(filteredList);
    this.userQuery = userQuery;
    resetSortActions();
  }

  /**
   * Handles the logout action for pressing button "logout".
   * @param event the action event triggered by the logout button.
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
    originalTaskList = new ArrayList<>(taskList);
    taskTable.refresh(); // Forces UI refresh
    resetSortActions();
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
    Logger.info(String.valueOf(user.getId()));
    Logger.info(user.getName());

    Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
    alert.setTitle("Delete User");
    alert.setHeaderText("Are you sure you want to delete this user?");
    alert.setContentText("User: " + user.getName());

    Optional<ButtonType> result = alert.showAndWait();

    if (result.isPresent() && result.get() == ButtonType.OK) {
      dataHandler.deleteUser(user);   // Delete from database
      userList.remove(user);            // Remove from UI list

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
