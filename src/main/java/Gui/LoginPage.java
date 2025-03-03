package Gui;

import javafx.animation.FadeTransition;
import javafx.event.ActionEvent;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.scene.layout.VBox;
import javafx.stage.Popup;
import javafx.stage.Stage;
import javafx.scene.Node;
import javafx.util.Duration;

import java.net.URL;

public class LoginPage {

    private VBox root;

    public LoginPage() {
        root = new VBox(10);
        root.setPadding(new Insets(20));
        root.setAlignment(Pos.CENTER);
        root.getStyleClass().add("login-container");
        URL cssURL = getClass().getResource("/Gui/loginpage.css");
        if (cssURL == null) {
            System.err.println("loginpage.css not found!");
        } else {
            root.getStylesheets().add(cssURL.toExternalForm());
        }

        // UI components
        Label welcomeLabel = new Label("Welcome to H.O.M.E");
        TextField usernameField = new TextField();
        usernameField.setPromptText("Username");

        PasswordField passwordField = new PasswordField();
        passwordField.setPromptText("Password");

        Button loginButton = new Button("Login");

        root.getChildren().addAll(welcomeLabel, usernameField, passwordField, loginButton);

        loginButton.setOnAction(event -> {
            String username = usernameField.getText();
            String password = passwordField.getText();
            if (!username.isEmpty() && !password.isEmpty()) {
                loginSuccess(event);
            } else {
                loginFailed(event);
            }
        });
    }

    private static void loginSuccess(ActionEvent event) {
        MainApp mainApp = new MainApp();
        Scene mainScene = new Scene(mainApp.getRoot(), 800, 600);
        Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
        stage.setScene(mainScene);
        stage.setTitle("Main Application");
    }

    public void loginFailed(ActionEvent event) {
        Popup errorPopup = new Popup();
        Label errorLabel = new Label("Both fields required to log in!");
        errorLabel.getStyleClass().add("error-label");
        errorPopup.getContent().add(errorLabel);

        Node source = (Node) event.getSource();
        errorPopup.show(source.getScene().getWindow(),
                source.localToScreen(0, 0).getX(),
                source.localToScreen(0, 0).getY() - 40
        );

        FadeTransition fade = new FadeTransition(Duration.seconds(2), errorLabel);
        fade.setFromValue(1.0);
        fade.setToValue(0.0);
        fade.setDelay(Duration.seconds(1));
        fade.play();
        fade.setOnFinished(e -> errorPopup.hide());
    }

    public Parent getRoot() {
        return root;
    }
}
