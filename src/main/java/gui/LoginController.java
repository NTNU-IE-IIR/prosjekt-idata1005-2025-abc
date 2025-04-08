package gui;

import dbcontext.DataHandler;
import dto.HouseholdDTO;
import javafx.animation.*;
import javafx.beans.property.DoubleProperty;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.beans.value.ChangeListener;
import javafx.beans.property.SimpleObjectProperty;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Insets;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.CheckBox;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.scene.layout.Background;
import javafx.scene.layout.BackgroundFill;
import javafx.scene.layout.CornerRadii;
import javafx.scene.layout.VBox;
import javafx.scene.paint.*;
import javafx.stage.Stage;
import javafx.util.Duration;
import utils.HashUtil;
import utils.Message;
import utils.MessageTypeEnum;
import utils.PreferenceUtil;
import javafx.stage.Screen;

import java.util.List;
import java.util.Objects;
import java.util.Random;

public class LoginController {

  @FXML private VBox root;
  @FXML private TextField usernameField;
  @FXML private PasswordField passwordField;
  @FXML private CheckBox checkRememberMe;

  private DataHandler dataHandler;
  private PreferenceUtil prefUtil;

  @FXML public void initialize() {
    // Db context
    dataHandler = new DataHandler();
    // Load CSS file
    root.getStylesheets().add(Objects.requireNonNull(getClass().getResource("/gui/styles/login.css")).toExternalForm());

    // Background animation
    // Define gradient stops
    List<Stop> stops = List.of(
      new Stop(0.00, Color.web("#9DF1CF")),
      new Stop(0.06, Color.web("#9DA4F1")),
      new Stop(0.12, Color.web("#EB926C")),

      new Stop(0.18, Color.web("#9DF1CF")),
      new Stop(0.24, Color.web("#9DA4F1")),
      new Stop(0.30, Color.web("#EB926C")),

      new Stop(0.36, Color.web("#9DF1CF")),
      new Stop(0.42, Color.web("#9DA4F1")),
      new Stop(0.48, Color.web("#EB926C")),

      new Stop(0.54, Color.web("#9DF1CF")),
      new Stop(0.60, Color.web("#9DA4F1")),
      new Stop(0.66, Color.web("#EB926C")),

      new Stop(0.72, Color.web("#9DF1CF")),
      new Stop(0.78, Color.web("#9DA4F1")),
      new Stop(0.84, Color.web("#EB926C")),

      new Stop(0.90, Color.web("#9DF1CF")),
      new Stop(0.96, Color.web("#9DA4F1")),
      new Stop(1.00, Color.web("#EB926C"))
    );

    // These properties will drive the gradient direction
    DoubleProperty x1 = new SimpleDoubleProperty(0);
    DoubleProperty y1 = new SimpleDoubleProperty(0);

    // Rebind background whenever direction changes
    ChangeListener<Number> update = (obs, oldVal, newVal) -> {
      RadialGradient gradient = new RadialGradient(
        0,                  // focusAngle
        0,                     // focusDistance
        x1.get(), y1.get(),    // center X/Y (we animate these)
        1.2,                   // radius
        true,                  // proportional
        CycleMethod.NO_CYCLE,  // no hard repeating
        List.of(
          new Stop(0.0, Color.web("#9DF1CF")),  // mint
          new Stop(0.4, Color.web("#9DA4F1")),  // indigo
          new Stop(0.8, Color.web("#EB926C"))   // coral
        )
      );

      root.setBackground(new Background(new BackgroundFill(gradient, CornerRadii.EMPTY, Insets.EMPTY)));
    };

    x1.addListener(update);
    y1.addListener(update);

    // Function to start random movement loop for a property
    Runnable animateProperty = () -> {
      animateDrift(x1);
      animateDrift(y1);
    };

    animateProperty.run(); // Kick off the drifting animation

    // Initialize preferences
    prefUtil = new PreferenceUtil(this.getClass().getName());
    if(Boolean.parseBoolean(prefUtil.getSetting("login-is-remembered")))
      populateFromPreferences();
  }

  private void animateDrift(DoubleProperty prop) {
    Random rand = new Random();
    double newTarget = 0.1 + rand.nextDouble() * 0.8; // range: [0.1, 0.9]

    Duration duration = Duration.seconds(8 + rand.nextDouble() * 4); // random between 8–12s

    Timeline timeline = new Timeline(
      new KeyFrame(duration, new KeyValue(prop, newTarget, Interpolator.EASE_BOTH))
    );

    timeline.setOnFinished(e -> animateDrift(prop)); // Recurse to keep it going
    timeline.play();
  }


  @FXML
  public void handleLogin(ActionEvent event) {
    String username = usernameField.getText();
    String password = passwordField.getText();
    String hashedPassword = HashUtil.hashSHA256(password);
    boolean isRemembered = checkRememberMe.isSelected();

    // Handle login preferences
    if(isRemembered){
      prefUtil.saveSetting("login-username",username);
      prefUtil.saveSetting("login-password",password);
      prefUtil.saveSetting("login-is-remembered", "true");
    }
    else{
      prefUtil.saveSetting("login-username","");
      prefUtil.saveSetting("login-password","");
      prefUtil.saveSetting("login-is-remembered", "false");
    }

    // Handle empty input fields
    if (username.isEmpty() || password.isEmpty()){
      showErrorPopup(event, new Message<>(MessageTypeEnum.WARNING, "Username or password is empty"));
      return;
    }

    Message<HouseholdDTO> resultMessage = dataHandler.getHousehold(username, hashedPassword);

    if(resultMessage.getType() == MessageTypeEnum.ERROR){
      showErrorPopup(event, resultMessage);
      return;
    }

    loginSuccess(event, resultMessage.getResult());
  }

  private void loginSuccess(ActionEvent event, HouseholdDTO household) {
    try {
      // Load MainApp FXML
      FXMLLoader loader = new FXMLLoader(getClass().getResource("/gui/Main.fxml"));
      Parent root = loader.load(); // Load the FXML first

      // Get the MainController instance and pass the household object
      MainController mainController = loader.getController();
      mainController.setHousehold(household);

      // Create and set the scene
      Scene mainScene = new Scene(root, 1200, 800);
      Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
      stage.setScene(mainScene);
      stage.setTitle("Dashboard: "+household.getName());

      stage.setMaximized(true);
      stage.setWidth(Screen.getPrimary().getVisualBounds().getWidth());
      stage.setHeight(Screen.getPrimary().getVisualBounds().getHeight());

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

  private void populateFromPreferences() {
    usernameField.setText(prefUtil.getSetting("login-username"));
    passwordField.setText(prefUtil.getSetting("login-password"));
    checkRememberMe.setSelected(true);
  }
}
