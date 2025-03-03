import Gui.LoginPage;
import javafx.application.Application;
import javafx.scene.Scene;
import javafx.stage.Stage;

public class Main extends Application {

    @Override
    public void start(Stage primaryStage) {
        // Initialize your login page
        LoginPage loginPage = new LoginPage();

        // Create a scene for the login UI
        Scene loginScene = new Scene(loginPage.getRoot(), 400, 300);

        // Set up the primary stage
        primaryStage.setTitle("Login Page");
        primaryStage.setScene(loginScene);
        primaryStage.show();
    }

    public static void main(String[] args) {
        launch(args);
    }
}
