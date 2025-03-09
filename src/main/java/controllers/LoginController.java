package controllers;

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

import java.util.Objects;

public class LoginController {

  @FXML private VBox root;
  @FXML private TextField usernameField;
  @FXML private PasswordField passwordField;

  @FXML public void initialize() {
    // Load CSS file
    root.getStylesheets().add(Objects.requireNonNull(getClass().getResource("/gui/login.css")).toExternalForm());
  }

  @FXML
  public void handleLogin(ActionEvent event) {
    String username = usernameField.getText();
    String password = passwordField.getText();

    if (!username.isEmpty() && !password.isEmpty()) {
      loginSuccess(event);
    } else {
      showErrorPopup(event);
    }
  }

  private void loginSuccess(ActionEvent event) {
    try {
      // Load MainApp FXML
      FXMLLoader loader = new FXMLLoader(getClass().getResource("/gui/main.fxml"));
      Scene mainScene = new Scene(loader.load(), 1200, 800);

      Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
      stage.setScene(mainScene);
      stage.setTitle("Main Application");
      stage.centerOnScreen();
    } catch (Exception e) {
      e.printStackTrace();
    }
  }

  private void showErrorPopup(ActionEvent event) {
    Alert alert = new Alert(Alert.AlertType.ERROR);
    alert.setTitle("Login Failed");
    alert.setHeaderText(null);
    alert.setContentText("Both fields are required to log in!");
    alert.showAndWait();
  }
}
