package gui.components;

import dto.*;
import javafx.application.Platform;
import javafx.beans.value.ObservableValue;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.control.Label;
import javafx.scene.control.ListCell;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.HBox;
import javafx.scene.control.ComboBox;
import utils.Logger;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Objects;
import java.util.function.Consumer;


public class TaskList extends ListCell<TaskDTO> {
  @FXML private Label taskDescription;
  @FXML private HBox taskDescriptionContainer;
  @FXML private HBox container;
  @FXML private ComboBox<StatusDTO> taskStatusDropdown;
  @FXML private ComboBox<PriorityDTO> taskPriorityDropdown;
  @FXML private ComboBox<UserDTO> taskOwnerDropdown;

  private final HouseholdDTO household;
  private boolean isProgrammaticChange = false;  // Guard flag
  private TaskDTO currentTask;  // Stores the current task being modified
  private final Consumer<TaskDTO> onChange;
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

    public void forceUpdate(T item, boolean empty) {
      updateItem(item, empty);
    }
  }


  public TaskList(ObservableList<StatusDTO> statusList,
                  ObservableList<PriorityDTO> priorityList,
                  ObservableList<UserDTO> userList, HouseholdDTO household,
                  Consumer<TaskDTO> onChange) {
    loadFXML();
    this.household = household;
    this.onChange = onChange;
    this.unassignedUser = new UserDTO(-1, "Unassigned", household);

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
      taskDescription.setText(task.getDescription() != null ? task.getDescription() : "N/A");

      String[] dropDownPaint = comboBoxPaint(task);
      taskStatusDropdown.setStyle("-fx-background-color:"+dropDownPaint[0]+";");
      taskPriorityDropdown.setStyle("-fx-background-color:"+dropDownPaint[1]+";");
      taskOwnerDropdown.setStyle("-fx-background-color:"+dropDownPaint[2]+";");

      if(Objects.equals(dropDownPaint[0], "#F6F6F6")) // <- Hard coded until further notice
        taskDescriptionContainer.setStyle("-fx-border-color: #C7C7C7;");
      else
        taskDescriptionContainer.setStyle("-fx-border-color:"+ dropDownPaint[0] +";");


      // Temporarily ignore events before setValue
      isProgrammaticChange = true;

      // Set the current status, priority, owner or fallback to unassigned for user
      UserDTO defaultOwner = (task.getUser() != null)
        ? task.getUser()
        : unassignedUser;

      taskStatusDropdown.setValue(task.getStatus());
      taskPriorityDropdown.setValue(task.getPriority());
      taskOwnerDropdown.setValue(defaultOwner);

      // Force the cell to update on initial load <-- Work around for javaFx limitations.
      Platform.runLater(()->{
        priorityButtonCell.forceUpdate(task.getPriority(), false);
        statusButtonCell.forceUpdate(task.getStatus(), false);
        ownerButtonCell.forceUpdate(defaultOwner, false);
      });

      // Re-allow the events
      isProgrammaticChange = false;
      setGraphic(container);
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
        styles[1] = "#7AF6B8";
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

  private void taskUpdate(ObservableValue<? extends SelectOption> observable, SelectOption  oldValue, SelectOption  newValue){
    if (isProgrammaticChange) return;
    if(newValue == null) {
      Logger.error("No newValue in update Task");
      return;
    }
    if(newValue instanceof StatusDTO)
      currentTask.setStatus((StatusDTO) newValue);
    if(newValue instanceof PriorityDTO)
      currentTask.setPriority((PriorityDTO) newValue);
    if(newValue instanceof UserDTO)
      currentTask.setUser((UserDTO) newValue);
    onChange.accept(currentTask);
    System.out.println("taskPriorityUpdate triggered: " + newValue.getName());
  }
}
