//import gui.LoginPage;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.Parent;
import javafx.scene.image.Image;
import javafx.stage.Stage;

import java.io.IOException;
import java.util.Objects;

public class Main extends Application {

    @Override
    public void start(Stage primaryStage) throws IOException {
        // Enable antialiasing for the text
        System.setProperty("prism.text", "gray");
        System.setProperty("prism.lcdtext", "false");

        FXMLLoader loader = new FXMLLoader(getClass().getResource("/gui/Login.fxml"));
        Parent root = loader.load();

        Scene scene = new Scene(root, 920, 600);
        primaryStage.setScene(scene);
        primaryStage.setTitle("Login to H.O.M.E");

        Image ico = new Image(Objects.requireNonNull(getClass().getResourceAsStream("/icons/tab-logo.png")));
        primaryStage.getIcons().add(ico);
        primaryStage.show();
    }

    public static void main(String[] args) {
        launch(args);
    }
}
