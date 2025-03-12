package controllers;

import dbcontext.DataHandler;
import dto.*;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.geometry.Insets;
import javafx.scene.control.*;
import javafx.scene.control.cell.ComboBoxTableCell;
import javafx.scene.input.MouseEvent;
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


  /**
   * Initializes the controller.
   * <p>
   * This method sets up CSS, initializes the DataHandler, configures UI components and their event handlers.
   * It also asynchronously loads data into the task table and user list.
   * </p>
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

    Platform.runLater(()->{
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

    userTable.setOnMouseClicked(event ->{
      UserDTO user = userTable.getSelectionModel().getSelectedItem();
      if (user != null) {
        Logger.info("User clicked: " + user.getName());
        userClickedMenu(event, user);
      }
    });
    taskTable.setOnMouseClicked(event ->{
      TaskDTO task = taskTable.getSelectionModel().getSelectedItem();
      if (task != null) {
        Logger.info("Task clicked: " + task.getDescription());
        taskClickedMenu(event, task);
      }
    });
  }

  /**
   * Displays a context menu when a user is clicked.
   *
   * @param event The MouseEvent triggering the context menu.
   * @param user  The selected user.
   */
  public void userClickedMenu(MouseEvent event, UserDTO user) {
    ContextMenu contextMenu = new ContextMenu();
    contextMenu.setAutoHide(true);

    MenuItem deleteUser = new MenuItem("Delete User");
    deleteUser.setOnAction(e -> {
      dataHandler.deleteUser(user);
      userTable.setItems(javafx.collections.FXCollections.observableArrayList(
              dataHandler.getAllUsersByHousehold(household.getId())));
    });

    MenuItem editUser = new MenuItem("Edit User");
    editUser.setOnAction(e -> {
      editUserInfoDialog(user);
      userTable.setItems(javafx.collections.FXCollections.observableArrayList(
              dataHandler.getAllUsersByHousehold(household.getId())));
    });

    contextMenu.getItems().addAll(deleteUser, editUser);
    contextMenu.show(userTable, event.getScreenX(), event.getScreenY());
  }

  /**
   * Displays the edit user information dialog.
   *
   * @param user The user to be edited.
   */
  public void editUserInfoDialog(UserDTO user) {
    Dialog<UserDTO> dialog = createBasicDialog("Edit User Name", "Enter user details");
    GridPane grid = createDialogGrid();

    TextField nameField = new TextField();
    nameField.setPromptText("New name");

    Label errorLabel = new Label();
    grid.add(new Label("Name"),0,0);
    grid.add(nameField, 0, 0);

    ButtonType editButton = new ButtonType("Edit",ButtonBar.ButtonData.OK_DONE);
    setupDialogButtons(dialog, editButton);

    dialog.getDialogPane().setContent(grid);

    dialog.getDialogPane().lookupButton(editButton).addEventFilter(ActionEvent.ACTION, e -> {
      if(nameField.getText().trim().isEmpty()){
        errorLabel.setText("Name field cannot be empty!");
        e.consume();
      }else{
        errorLabel.setText("");
      }});

    dialog.setResultConverter(dialogButton -> {
      if(dialogButton == editButton){
        return new UserDTO(user.getId(), nameField.getText(),household);
      }
      return null;
    });
    Optional<UserDTO> result = dialog.showAndWait();
    result.ifPresent(editedUser -> {
      dataHandler.editUser(editedUser);
      userTable.setItems(getAllUsers());
    });
  }

  /**
   * Displays a context menu when a task is clicked.
   *
   * @param event The MouseEvent triggering the context menu.
   * @param task  The selected task.
   */
  public void taskClickedMenu(MouseEvent event, TaskDTO task) {
    ContextMenu contextMenu = new ContextMenu();
    contextMenu.setAutoHide(true);

    MenuItem deleteTask = new MenuItem("Delete Task");
    deleteTask.setOnAction(e -> {
      dataHandler.deleteTask(task);
      taskTable.setItems(javafx.collections.FXCollections.observableArrayList(
              dataHandler.getAllTasksByHouseHold(household.getId())));
    });

    MenuItem editTask = new MenuItem("Edit Task");
    editTask.setOnAction(e -> {
      taskInfoDialog(task);
      taskTable.setItems(javafx.collections.FXCollections.observableArrayList(
              dataHandler.getAllTasksByHouseHold(household.getId())));
    });

    contextMenu.getItems().addAll(deleteTask, editTask);
    contextMenu.show(userTable, event.getScreenX(), event.getScreenY());
  }

  /**
   * Displays the edit task dialog.
   *
   * @param task The task to be edited.
   */
  public void taskInfoDialog(TaskDTO task) {
    Dialog<TaskDTO> dialog = createBasicDialog("Edit Task", "Edit task details");
    GridPane grid = createDialogGrid();

    TextField taskField = new TextField();
    taskField.setPromptText("New task name");
    ComboBox<PriorityDTO> priorityCombo = createComboBox(initialPriorityList);
    ComboBox<StatusDTO> statusCombo = createComboBox(initialStatusList);
    ComboBox<UserDTO> userCombo = createComboBox(initialUserList);


    grid.add(new Label("Change Priority:"), 0, 1);
    grid.add(priorityCombo, 1, 1);
    grid.add(new Label("Change Member:"), 0, 2);
    grid.add(userCombo, 1, 2);
    grid.add(new Label("Change Status:"), 0, 3);
    grid.add(statusCombo, 1, 3);

    Label errorLabel = new Label();
    grid.add(new Label("Task"),0,0);
    grid.add(taskField, 0, 0);

    ButtonType editButton = new ButtonType("Edit",ButtonBar.ButtonData.OK_DONE);
    setupDialogButtons(dialog, editButton);

    dialog.getDialogPane().setContent(grid);

    dialog.getDialogPane().lookupButton(editButton).addEventFilter(ActionEvent.ACTION, e -> {
      if(taskField.getText().trim().isEmpty()){
        errorLabel.setText("Name field cannot be empty!");
        e.consume();
      }else{
        errorLabel.setText("");
      }});

    dialog.setResultConverter(dialogButton -> {
      if(dialogButton == editButton){
        return new TaskDTO(task.getId(),
                taskField.getText(),
                household,
                statusCombo.getValue(),priorityCombo.getValue(),userCombo.getValue());
      }
      return null;
    });
    Optional<TaskDTO> result = dialog.showAndWait();
    result.ifPresent(editedTask -> {
      dataHandler.editTask(editedTask);
      userTable.setItems(getAllUsers());
    });
  }

  /**
   * Sets the household context.
   *
   * @param household The HouseholdDTO to set.
   */
  public void setHousehold(HouseholdDTO household) {
    this.household = household;
    Logger.info("Household received: " + household.getName());
  }

  /**
   * Retrieves all tasks associated with the current household.
   *
   * @return ObservableList of TaskDTO objects.
   */
  private ObservableList<TaskDTO> getAllTasks() {
    return FXCollections.observableArrayList(dataHandler.getAllTasksByHouseHold(household.getId()));
  }

  /**
   * Retrieves all users associated with the current household.
   *
   * @return ObservableList of UserDTO objects.
   */
  public ObservableList<UserDTO> getAllUsers() {
    return FXCollections.observableArrayList(dataHandler.getAllUsersByHousehold(household.getId()));
  }

  /**
   * Handles the Add User button action.
   *
   * @param event The ActionEvent triggered by the button.
   */
  private void handleAddUser(ActionEvent event){
    Dialog<UserDTO> dialog = createBasicDialog("Add New User", "Enter user details");
    GridPane grid = createDialogGrid();

    TextField nameField = new TextField();
    nameField.setPromptText("User name");

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

  /**
   * Handles the Add Task button action.
   *
   * @param event The ActionEvent triggered by the button.
   */
  private void handleAddTask(ActionEvent event) {
    Dialog<TaskDTO> dialog = createBasicDialog("Add New Task", "Enter task details");
    GridPane grid = createDialogGrid();

    TextField descriptionField = new TextField();
    descriptionField.setPromptText("Task description");
    ComboBox<PriorityDTO> priorityCombo = createComboBox(initialPriorityList);
    ComboBox<UserDTO> userCombo = createComboBox(initialUserList);
    Label errorLabel = new Label();

    grid.add(new Label("Description:"), 0, 0);
    grid.add(descriptionField, 1, 0);
    grid.add(new Label("Priority:"), 0, 1);
    grid.add(priorityCombo, 1, 1);
    grid.add(new Label("Members:"), 0, 2);
    grid.add(userCombo, 1, 2);
    grid.add(errorLabel, 0, 3, 2, 1);

    ButtonType addButton = new ButtonType("Add",ButtonBar.ButtonData.OK_DONE);
    setupDialogButtons(dialog, addButton);
    dialog.getDialogPane().setContent(grid);

    dialog.getDialogPane().lookupButton(addButton).addEventFilter(ActionEvent.ACTION, e -> {
      if (descriptionField.getText().trim().isEmpty()) {
        errorLabel.setText("Description cannot be empty!");
        e.consume();
      } else {
        errorLabel.setText("");
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

  /**
   * Handles search action based on task description.
   *
   * @param event The ActionEvent triggered by the search field.
   */
  private void handleSearchDescription(ActionEvent event) {
    String userQuery = searchField.getText();
    List<TaskDTO> query = dataHandler.getAllTasksByHouseHold(household.getId(), userQuery);
    ObservableList<TaskDTO> result = FXCollections.observableArrayList(query);
    Logger.info("Search tasks for: " + searchField.getText());
    taskTable.setItems(result);
  }

  /**
   * Handles pagination to the next task page.
   *
   * @param event The ActionEvent triggered by the next page button.
   */
  private void handleNextTaskPage(ActionEvent event) {
    taskPageCount++;
    ObservableList<TaskDTO> result =
            FXCollections.observableArrayList(dataHandler.getLimitedTasks(household.getId(),taskLimit,this.getTaskOffset()));
    taskTable.setItems(result);
  }

  /**
   * Handles pagination to the previous task page.
   *
   * @param event The ActionEvent triggered by the previous page button.
   */
  private void handlePreviousTaskPage(ActionEvent event) {
    if((taskPageCount-1) == 0)
      return;

    taskPageCount--;
    ObservableList<TaskDTO> result =
            FXCollections.observableArrayList(dataHandler.getLimitedTasks(household.getId(),taskLimit,this.getTaskOffset()));
    taskTable.setItems(result);
  }

  /**
   * Creates a basic dialog with a title and header text.
   *
   * @param title      The title of the dialog.
   * @param headerText The header text of the dialog.
   * @param <T>        The type of the result.
   * @return A new Dialog instance.
   */
  private <T> Dialog<T> createBasicDialog(String title, String headerText) {
    Dialog<T> dialog = new Dialog<>();
    dialog.setTitle(title);
    dialog.setHeaderText(headerText);
    return dialog;
  }

  /**
   * Creates a GridPane for dialog layouts with predefined gaps and padding.
   *
   * @return A new GridPane instance.
   */
  private GridPane createDialogGrid() {
    GridPane grid = new GridPane();
    grid.setHgap(10);
    grid.setVgap(10);
    grid.setPadding(new Insets(20, 150, 10, 10));
    return grid;
  }

  /**
   * Creates a ComboBox with the provided list of items.
   *
   * @param list The list of items for the ComboBox.
   * @param <T>  The type of items.
   * @return A new ComboBox instance populated with the list.
   */
  private <T> ComboBox<T> createComboBox(List<T> list) {
    ComboBox<T> comboBox = new ComboBox<>();
    comboBox.setItems(FXCollections.observableArrayList(list));
    return comboBox;
  }

  /**
   * Sets up dialog buttons for the provided dialog.
   *
   * @param dialog  The Dialog to which buttons are to be added.
   * @param buttons The ButtonTypes to be added.
   */
  private void setupDialogButtons(Dialog<?> dialog, ButtonType... buttons) {
    dialog.getDialogPane().getButtonTypes().addAll(buttons);
    dialog.getDialogPane().getButtonTypes().add(ButtonType.CANCEL);
  }

  /**
   * Calculates the offset for task pagination.
   *
   * @return The calculated offset based on the current page and task limit.
   */
  private int getTaskOffset(){
    return (taskPageCount-1) * taskLimit;
  }
}
