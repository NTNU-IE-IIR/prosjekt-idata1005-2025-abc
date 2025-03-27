package gui.components;

import javafx.animation.FadeTransition;
import javafx.animation.PauseTransition;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Bounds;
import javafx.geometry.Point2D;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.HBox;
import javafx.stage.Popup;
import javafx.util.Duration;
import utils.Logger;
import utils.Message;

import java.io.IOException;
import java.util.Objects;

public class Toast {

  /* ---------------- FXML fields (Controller) ---------------- */
  @FXML
  private HBox toastRoot;

  @FXML
  private Label iconLabel;

  @FXML
  private Label messageLabel;

  @FXML
  private Label closeLabel;

  @FXML
  private ImageView toastImage;

  /**
   * Static method to show a toast anchored to the bottom-left corner
   * of the given root node, displaying the given message for the
   * specified duration in milliseconds. If {@code durationMs} is less than 0 the toast won't disappear.
   */
  public static<T> void showToast(Node anchorNode, Message<T> message, int durationMs) {
    try {
      // 1) Load the FXML / CSS, which uses Toast as its controller
      FXMLLoader loader = new FXMLLoader(Toast.class.getResource("/gui/components/Toast.fxml"));
      Parent toastContent = loader.load();
      toastContent.getStylesheets().add(Objects.requireNonNull(Toast.class.getResource("/gui/styles/toast.css")).toExternalForm());

      // 2) The loader's controller is a new Toast instance
      Toast controller = loader.getController();

      // 3) Set the message text
      controller.messageLabel.setText(message.getMessage());

      switch (message.getType()) {

        case SUCCESS:
          controller.toastRoot.setStyle("-fx-background-color: #C6EECD");
          controller.toastImage.setImage(
            new Image(Objects.requireNonNull(Toast.class.getResource("/icons/done-icon.png"))
              .toExternalForm()));
          break;

        case ERROR:
          controller.toastRoot.setStyle("-fx-background-color: #F68C8C");
          controller.toastImage.setImage(
            new Image(Objects.requireNonNull(Toast.class.getResource("/icons/priority-critical.png"))
              .toExternalForm()));
          break;
        case WARNING:
          controller.toastRoot.setStyle("-fx-background-color: #F4A684");
          controller.toastImage.setImage(
            new Image(Objects.requireNonNull(Toast.class.getResource("/icons/warning-icon.png"))
              .toExternalForm()));
          break;
        case INFO:
          controller.toastRoot.setStyle("-fx-background-color: #D6F8FF");
          controller.toastImage.setImage(
            new Image(Objects.requireNonNull(Toast.class.getResource("/icons/info-icon.png"))
              .toExternalForm()));
          break;
      }

      // 4) Create and configure a Popup
      Popup popup = new Popup();
      popup.setAutoHide(true); // closes if user clicks outside
      popup.getContent().add(toastContent);

      // 5) Position the toast at the bottom-left of anchorNode
      Bounds nodeBounds = anchorNode.getBoundsInLocal();
      Point2D screenCoords = anchorNode.localToScreen(
        nodeBounds.getMinX(),
        nodeBounds.getMaxY()
      );

      if (screenCoords != null) {
        // Offsets so it doesn’t appear exactly at the corner
        double xOffset = 20;   // shift right 20px
        double yOffset = -100; // shift up 100px
        popup.show(anchorNode, screenCoords.getX() + xOffset, screenCoords.getY() + yOffset);
      }

      // 6) Close button handler
      controller.closeLabel.setOnMouseClicked(e -> popup.hide());

      // 7) Fade in
      FadeTransition fadeIn = new FadeTransition(Duration.millis(200), toastContent);
      fadeIn.setFromValue(0.0);
      fadeIn.setToValue(1.0);
      fadeIn.play();

      // 8) Only auto-hide if durationMs > 0
      if (durationMs > 0) {
        PauseTransition wait = new PauseTransition(Duration.millis(durationMs));
        wait.setOnFinished(event -> {
          FadeTransition fadeOut = new FadeTransition(Duration.millis(200), toastContent);
          fadeOut.setFromValue(1.0);
          fadeOut.setToValue(0.0);
          fadeOut.setOnFinished(evt -> popup.hide());
          fadeOut.play();
        });
        wait.play();
      }
    } catch (IOException e) {
      Logger.error("The toast has failed: "+e.getMessage());
    }
  }
}
