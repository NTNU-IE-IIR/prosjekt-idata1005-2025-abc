package controllers;

import dbcontext.DataHandler;
import dto.HouseholdDTO;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import utils.HashUtil;
import utils.Message;
import utils.MessageTypeEnum;

import java.lang.runtime.SwitchBootstraps;
import java.util.Objects;

public class LoginController {

  @FXML private VBox root;
  @FXML private TextField usernameField;
  @FXML private PasswordField passwordField;

  private DataHandler dataHandler;

  @FXML public void initialize() {
    // Load CSS file
    root.getStylesheets().add(Objects.requireNonNull(getClass().getResource("/gui/login.css")).toExternalForm());
    // Db context
    dataHandler = new DataHandler();
  }

  @FXML
  public void handleLogin(ActionEvent event) {
    String username = usernameField.getText();
    String password = HashUtil.hashSHA256(passwordField.getText());

    // Handle empty input fields
    if (username.isEmpty() || password.isEmpty()){
      showErrorPopup(event, new Message<>(MessageTypeEnum.WARNING, "Username or password is empty"));
      return;
    }

    Message<HouseholdDTO> resultMessage = dataHandler.getHousehold(username, password);

    if(resultMessage.getType() == MessageTypeEnum.ERROR){
      showErrorPopup(event, resultMessage);
      return;
    }

    loginSuccess(event, resultMessage.getResult());
  }


  private void loginSuccess(ActionEvent event, HouseholdDTO household) {
    try {
      // Load MainApp FXML
      FXMLLoader loader = new FXMLLoader(getClass().getResource("/gui/main.fxml"));
      Parent root = loader.load(); // Load the FXML first

      // Get the MainController instance and pass the household object
      MainController mainController = loader.getController();
      mainController.setHousehold(household);

      // Create and set the scene
      Scene mainScene = new Scene(root, 1200, 800);
      Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
      stage.setScene(mainScene);
      stage.setTitle("Main Application");
      stage.centerOnScreen();
    } catch (Exception e) {
      e.printStackTrace();
    }
  }

  private <T> void showErrorPopup(ActionEvent event, Message<T> message) {
    Alert alert = null;
    if (Objects.requireNonNull(message.getType()) == MessageTypeEnum.WARNING) {
      alert = new Alert(Alert.AlertType.WARNING);
    } else {
      alert = new Alert(Alert.AlertType.ERROR);
    }
    alert.setTitle("And issue occurred");
    alert.setHeaderText(null);
    alert.setContentText(message.getMessage());
    alert.showAndWait();
  }
}
