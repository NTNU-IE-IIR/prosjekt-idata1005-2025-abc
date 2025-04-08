package gui.components;

import gui.helpers.TaskDialogFactory;
import dto.*;
import gui.helpers.TooltipHelper;
import javafx.application.Platform;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.value.ObservableValue;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ListCell;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.HBox;
import javafx.scene.control.ComboBox;
import utils.Logger;
import utils.Message;
import utils.MessageTypeEnum;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Objects;
import java.util.Optional;
import java.util.function.Consumer;


public class TaskList extends ListCell<TaskDTO> {
  @FXML private Label taskDescription, closedTaskDescription, doneDate, closedPriority, closedOwner;
  @FXML private HBox taskDescriptionContainer, closedTaskDescriptionContainer;
  @FXML private HBox container, closedContainer;
  @FXML private ComboBox<StatusDTO> taskStatusDropdown;
  @FXML private ComboBox<PriorityDTO> taskPriorityDropdown;
  @FXML private ComboBox<UserDTO> taskOwnerDropdown;
  @FXML private Button deleteTaskButton, taskDescriptionEdit;
  @FXML private ImageView closedPriorityImage;

  private final HouseholdDTO household;
  private SimpleBooleanProperty isProgrammaticChange; // Guard flag
  private TaskDTO currentTask;  // Stores the current task being modified
  private final Consumer<Message<TaskDTO>> onChange;
  private final Consumer<TaskDTO> onDelete;
  private final MyStatusListCell<StatusDTO> statusButtonCell;
  private final MyStatusListCell<PriorityDTO> priorityButtonCell;
  private final MyStatusListCell<UserDTO> ownerButtonCell;
  private final UserDTO unassignedUser;


  public class MyStatusListCell<T extends SelectOption> extends ListCell<T> {
    private final ImageView imageView = new ImageView();

    public MyStatusListCell() {
      imageView.setFitHeight(20);
      imageView.setPreserveRatio(true);
    }

    @Override
    protected void updateItem(T item, boolean empty) {
      super.updateItem(item, empty);
      if (empty || item == null) {
        setText(null);
        setGraphic(null);
      } else {
        setText(item.getName());
        try {
          Image icon = createIcon(item);
          imageView.setImage(icon);
        } catch (FileNotFoundException ex) {
          Logger.error("Error loading icon: " + ex.getMessage());
          imageView.setImage(null);
        }
        setGraphic(imageView);
      }
    }



    public void forceUpdate(T item, boolean empty) {
      updateItem(item, empty);
    }
  }


  public TaskList(ObservableList<StatusDTO> statusList,
                  ObservableList<PriorityDTO> priorityList,
                  ObservableList<UserDTO> userList, HouseholdDTO household,
                  SimpleBooleanProperty isProgrammaticChange,
                  Consumer<Message<TaskDTO>> onChange,
                  Consumer<TaskDTO> onDelete) {
    loadFXML();
    this.household = household;
    this.onChange = onChange;
    this.onDelete = onDelete;
    this.unassignedUser = new UserDTO(-1, "Unassigned", household);
    this.isProgrammaticChange = new SimpleBooleanProperty(false);
    this.isProgrammaticChange.bindBidirectional(isProgrammaticChange);

    this.statusButtonCell = new MyStatusListCell<>();
    this.priorityButtonCell = new MyStatusListCell<>();
    this.ownerButtonCell = new MyStatusListCell<>();

    taskStatusDropdown.setButtonCell(statusButtonCell);
    taskStatusDropdown.setItems(statusList);

    taskPriorityDropdown.setButtonCell(priorityButtonCell);
    taskPriorityDropdown.setItems(priorityList);

    taskOwnerDropdown.setButtonCell(ownerButtonCell);
    taskOwnerDropdown.setItems(userList);

    // Attach the listener, and handle user-initiated change
    taskStatusDropdown.valueProperty().addListener(this::taskUpdate);
    taskPriorityDropdown.valueProperty().addListener(this::taskUpdate);
    taskOwnerDropdown.valueProperty().addListener(this::taskUpdate);

    deleteTaskButton.setOnAction(e -> onDelete.accept(currentTask));
  }

  private void loadFXML() {
    try {
      FXMLLoader loader = new FXMLLoader(getClass().getResource("/gui/components/TaskList.fxml"));
      loader.setController(this);
      loader.load();
    } catch (IOException e) {
      e.printStackTrace();
    }
  }

  @Override
  protected void updateItem(TaskDTO task, boolean empty) {
    super.updateItem(task, empty);
    if (empty || task == null) {
      setGraphic(null);
      currentTask = null;
    } else {
      currentTask = task;

      // Closed done tasks
      if(currentTask.getStatusId() == 4){
        displayClosedTasks(task);
        setGraphic(closedContainer);
        return;
      }

      // Paint different components
      String[] dropDownPaint = comboBoxPaint(task);
      taskStatusDropdown.setStyle("-fx-background-color:"+dropDownPaint[0]+";");
      taskPriorityDropdown.setStyle("-fx-background-color:"+dropDownPaint[1]+";");
      taskOwnerDropdown.setStyle("-fx-background-color:"+dropDownPaint[2]+";");

      // Paint the left hand-side border of the row.
      if(Objects.equals(dropDownPaint[0], "#F6F6F6")) // <- Hard coded until further notice
        taskDescriptionContainer.setStyle("-fx-border-color: #C7C7C7;");
      else
        taskDescriptionContainer.setStyle("-fx-border-color:"+ dropDownPaint[0] +";");

      // Temporarily ignore events before setValue
      isProgrammaticChange.setValue(true);

      // Set the current status, priority, owner or fallback to unassigned user
      UserDTO defaultOwner = (task.getUser() != null)
        ? task.getUser()
        : unassignedUser;

      // Setup edit task description
      taskDescriptionEdit.setOnAction(this::setupDescriptionEdit);

      taskDescription.setText(task.getDescription() != null ? task.getDescription() : "N/A");
      taskStatusDropdown.setValue(task.getStatus());
      taskPriorityDropdown.setValue(task.getPriority());
      taskOwnerDropdown.setValue(defaultOwner);

      // Set tooltips
      TooltipHelper.bindTooltip(taskDescription, taskDescription.getText());
      TooltipHelper.bindTooltip(taskDescriptionEdit, "Edit task description");
      TooltipHelper.bindTooltip(taskStatusDropdown, "Change task status");
      TooltipHelper.bindTooltip(taskPriorityDropdown, "Change task priority");
      TooltipHelper.bindTooltip(taskOwnerDropdown, "Change task owner");
      TooltipHelper.bindTooltip(deleteTaskButton, "Delete task");

      // Force the cell to update on initial load <-- Work around for javaFx limitations.
      Platform.runLater(()->{
        priorityButtonCell.forceUpdate(task.getPriority(), false);
        statusButtonCell.forceUpdate(task.getStatus(), false);
        ownerButtonCell.forceUpdate(defaultOwner, false);
      });

      // Re-allow the events
      isProgrammaticChange.setValue(false);
      setGraphic(container);
    }
  }

  private void displayClosedTasks(TaskDTO task){
    closedTaskDescription.setText(task.getDescription());
    doneDate.setText(task.getDoneDate().toString());
    closedPriority.setText(task.getPriority().getName());

    UserDTO defaultOwner = (task.getUser() != null)
      ? task.getUser()
      : unassignedUser;

    closedOwner.setText(defaultOwner.getName());
    try {
      closedPriorityImage.setImage(createIcon(task.getPriority()));
    } catch (FileNotFoundException ex) {
      Logger.error("(TaskList.displayClosedTasks) file not found: " + ex.getMessage());
    }
  }

  private String[] comboBoxPaint(TaskDTO task) {
    String[] styles = new String[3];
    switch (task.getStatus().getId()){
      case 1:
        styles[0] = "#C6EECD";
        break;
      case 2:
        styles[0] = "#FFE78F";
        break;
      default:
        styles[0] = "#F6F6F6";
        break;
    }
    switch (task.getPriority().getId()){
      case 1:
        styles[1] = "#98F5F9";
        break;
      case 2:
        styles[1] = "#B5BAF5";
        break;
      case 3:
        styles[1] = "#F4A684";
        break;
      case 4:
        styles[1] = "#F68C8C";
        break;
      default:
        styles[1] = "#F6F6F6";
        break;
    }
    styles[2] = "#F6F6F6";
    return styles;
  }

  private Image createIcon(SelectOption item) throws FileNotFoundException {
    // Build iconPath depending on runtime type
    String iconPath = "/icons";
    switch (item) {
      case StatusDTO statusDTO -> {
        switch (statusDTO.getId()) {
          case 1:
            iconPath += "/done-icon.png";
            break;
          case 2:
            iconPath += "/working-on-it-icon.png";
            break;
          case 3:
            iconPath += "/not-started-icon.png";
            break;
          case -1:
            iconPath += "/abc-logo.png";
            break;
        }
      }
      case PriorityDTO priorityDTO -> {
        switch (priorityDTO.getId()) {
          case 1:
            iconPath += "/priority-low.png";
            break;
          case 2:
            iconPath += "/priority-medium.png";
            break;
          case 3:
            iconPath += "/priority-high.png";
            break;
          case 4:
            iconPath += "/priority-critical.png";
            break;
          case -1:
            iconPath += "/abc-logo.png";
            break;
        }
      }
      case UserDTO userDTO -> iconPath += "/person-icon.png";
      default -> throw new IllegalStateException("Unexpected value: " + item);
    }
    var resourceUrl = getClass().getResource(iconPath);
    if (resourceUrl == null) {
      // Throw exception if the icon cannot be loaded
      throw new FileNotFoundException("Could not load icon from path: " + iconPath);
    }
    // If the resource is found, return the Image
    return new Image(resourceUrl.toExternalForm());
  }

  private void setupDescriptionEdit(ActionEvent actionEvent) {
    String oldDescription = currentTask.getDescription();

    // Create edit description dialog
    Optional<TaskDTO> result =
      TaskDialogFactory.createEditTaskDescriptionDialog(currentTask, household).showAndWait();

    // Handle edit description submit
    result.ifPresent(task -> {
    String newDescription = task.getDescription();
      String stringMessage = "Status updated: " + oldDescription + " → " + newDescription;
      Message<TaskDTO> message = new Message<>(MessageTypeEnum.SUCCESS, stringMessage, task);
      onChange.accept(message);
    });
  }

  private void taskUpdate(ObservableValue<? extends SelectOption> observable, SelectOption  oldValue, SelectOption  newValue){
    if (isProgrammaticChange.getValue()) return;
    if(newValue == null) {
      Logger.error("No newValue in update Task");
      return;
    }
    Message<TaskDTO> message;

    switch (newValue) {
      case StatusDTO statusDTO -> {
        currentTask.setStatus(statusDTO);
        String stringMessage = "Status updated: " + oldValue.getName() + " → " + newValue.getName();
        message = new Message<>(MessageTypeEnum.SUCCESS, stringMessage, currentTask);
      }
      case PriorityDTO priorityDTO -> {
        currentTask.setPriority(priorityDTO);
        String stringMessage = "Priority updated: " + oldValue.getName() + " → " + newValue.getName();
        message = new Message<>(MessageTypeEnum.SUCCESS, stringMessage, currentTask);
      }
      case UserDTO userDTO -> {
        currentTask.setUser(userDTO);
        String stringMessage = "Owner updated: " + oldValue.getName() + " → " + newValue.getName();
        message = new Message<>(MessageTypeEnum.SUCCESS, stringMessage, currentTask);
      }
      default -> {
        message = new Message<>("Unsupported action");
      }
    }
    onChange.accept(message);
    Logger.info("Task updated triggered: " + newValue.getName());
  }
}
