package controllers.helpers;

import dto.*;
import javafx.application.Platform;
import javafx.beans.value.ObservableValue;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.control.Label;
import javafx.scene.control.ListCell;
import javafx.scene.layout.HBox;
import javafx.scene.control.ComboBox;
import javafx.scene.layout.Priority;

import java.io.IOException;
import java.util.List;
import java.util.Objects;
import java.util.function.Consumer;


public class TaskListController extends ListCell<TaskDTO> {
  @FXML private Label taskDescription, taskPriority, taskStatus, taskOwner;
  @FXML private HBox container;
  @FXML private ComboBox<StatusDTO> taskStatusDropdown;
  @FXML private ComboBox<PriorityDTO> taskPriorityDropdown;
  @FXML private ComboBox<UserDTO> taskOwnerDropdown;

  private ObservableList<StatusDTO> statusList;
  private ObservableList<PriorityDTO> priorityList;
  private ObservableList<UserDTO> userList;
  private HouseholdDTO household;
  private boolean isProgrammaticChange = false;  // Guard flag
  private TaskDTO currentTask;  // Stores the current task being modified
  private Consumer<TaskDTO> onChange;


  public TaskListController(ObservableList<StatusDTO> statusList,
                            ObservableList<PriorityDTO> priorityList,
                            ObservableList<UserDTO> userList, HouseholdDTO household,
                            Consumer<TaskDTO> onChange) {
    loadFXML();
    this.statusList = statusList;
    this.priorityList = priorityList;
    this.userList = userList;
    this.household = household;
    this.onChange = onChange;

    taskStatusDropdown.setItems(statusList);
    taskPriorityDropdown.setItems(priorityList);
    taskOwnerDropdown.setItems(userList);

    // Attach the listener, and handle user-initiated change
    taskStatusDropdown.valueProperty().addListener((obs, oldVal, newVal) -> {
      if (!isProgrammaticChange && newVal != null) {
        taskStatusUpdate(obs, oldVal, newVal);
      }
    });
    taskPriorityDropdown.valueProperty().addListener((obs, oldVal, newVal) -> {
      if (!isProgrammaticChange && newVal != null) {
        taskPriorityUpdate(obs, oldVal, newVal);
      }
    });
    taskOwnerDropdown.valueProperty().addListener((obs, oldVal, newVal) -> {
      if (!isProgrammaticChange && newVal != null) {
        taskOwnerUpdate(obs, oldVal, newVal);
      }
    });
  }

  private void loadFXML() {
    try {
      FXMLLoader loader = new FXMLLoader(getClass().getResource("/gui/taskList.fxml"));
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

      // Temporarily ignore events before setValue
      isProgrammaticChange = true;

      // Set the current status or fallback to default
      taskStatusDropdown.setValue(task.getStatus() != null ? task.getStatus() : new StatusDTO(-1, "N/A"));
      taskPriorityDropdown.setValue(task.getPriority() != null ? task.getPriority() : new PriorityDTO(-1, "N/A"));
      taskOwnerDropdown.setValue(task.getUser() != null ? task.getUser() : new UserDTO(-1, "N/A", household));

      // Re-allow the events
      isProgrammaticChange = false;

      setGraphic(container);
    }

  }
  private void taskStatusUpdate(ObservableValue<? extends StatusDTO> observable, StatusDTO oldValue, StatusDTO newValue) {
    if (newValue != null) {
      currentTask.setStatus(newValue);
      onChange.accept(currentTask);
      System.out.println("taskStatusUpdate triggered: " + newValue.getName());
    }
  }
  private void taskPriorityUpdate(ObservableValue<? extends PriorityDTO> observable, PriorityDTO oldValue, PriorityDTO newValue) {
    if (newValue != null) {
      currentTask.setPriority(newValue);
      onChange.accept(currentTask);
      System.out.println("taskPriorityUpdate triggered: " + newValue.getName());
    }
  }
  private void taskOwnerUpdate(ObservableValue<? extends UserDTO> observable, UserDTO oldValue, UserDTO newValue) {
    if (newValue != null) {
      currentTask.setUser(newValue);
      onChange.accept(currentTask);
      System.out.println("taskOwnerUpdate triggered: " + newValue.getName());
    }
  }





}
