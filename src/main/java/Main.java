//import gui.LoginPage;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.Parent;
import javafx.stage.Stage;

import java.io.IOException;

public class Main extends Application {

    @Override
    public void start(Stage primaryStage) throws IOException {
        // Enable antialiasing for the text
        System.setProperty("prism.text", "gray");
        System.setProperty("prism.lcdtext", "false");

        FXMLLoader loader = new FXMLLoader(getClass().getResource("/gui/login.fxml"));
        Parent root = loader.load();

        Scene scene = new Scene(root, 800, 500);
        primaryStage.setScene(scene);
        primaryStage.setTitle("Login Page");
        primaryStage.show();
    }

    public static void main(String[] args) {
        launch(args);
    }
}
