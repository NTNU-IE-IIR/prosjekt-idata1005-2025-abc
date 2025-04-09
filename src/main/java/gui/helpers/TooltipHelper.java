package gui.helpers;

import javafx.animation.FadeTransition;
import javafx.application.Platform;
import javafx.scene.Node;
import javafx.scene.control.Control;
import javafx.scene.control.Tooltip;
import javafx.stage.Window;
import javafx.util.Duration;

import java.awt.*;

/**
 * A helper class for managing JavaFX tooltips with custom styling, delays, animations,
 * and repositioning.
 * <p>
 * This class provides utility methods to attach tooltips to JavaFX nodes. It configures
 * tooltips with custom show/hide delays, fade-in and fade-out animations, and repositions
 * the tooltip lower on the screen.
 * </p>
 */
public class TooltipHelper {

  /**
   * Binds a tooltip with the specified text to the given node.
   * <p>
   * If the node is an instance of {@link Control}, the tooltip is attached using the
   * {@code setTooltip()} method. Otherwise, for non-control nodes, the tooltip is installed
   * using {@link Tooltip#install(Node, Tooltip)}.
   * </p>
   *
   * @param node        the JavaFX node to which the tooltip should be bound.
   * @param tooltipText the text to display in the tooltip.
   */
  public static void bindTooltip(Node node, String tooltipText) {
    // Create a tooltip with custom configuration
    Tooltip tooltip = createTooltip(tooltipText);

    // Attach the tooltip using the appropriate method based on node type.
    if (node instanceof Control) {
      // For Control nodes, set the tooltip directly.
      ((Control) node).setTooltip(tooltip);
    } else {
      // For non-Control nodes, install the tooltip.
      Tooltip.install(node, tooltip);
    }
  }

  /**
   * Creates a configured {@link Tooltip} with the specified text.
   * <p>
   * The created tooltip has customized show and hide delays, custom styling, and fade-in
   * and fade-out animations. Additionally, when shown, the tooltip is repositioned lower
   * on the screen.
   * </p>
   *
   * @param tooltipText the text to display in the tooltip.
   * @return a {@link Tooltip} instance configured with delays, styling, animations, and repositioning.
   */
  public static Tooltip createTooltip(String tooltipText) {
    // Create a new Tooltip with the provided text.
    Tooltip tooltip = new Tooltip(tooltipText);

    // Set up the delays for showing and hiding the tooltip.
    tooltip.setShowDelay(Duration.millis(200));
    tooltip.setHideDelay(Duration.ZERO);
    tooltip.setShowDuration(Duration.INDEFINITE);

    // Set max width and wrap text
    tooltip.setMaxWidth(330);
    tooltip.setWrapText(true);

    // Apply custom styling to the tooltip (e.g., white text and a custom font).
    tooltip.setStyle(
      "-fx-text-fill: white;" +
      "-fx-font-family: \"Inter 18pt SemiBold\";"
    );

    // Configure fade-in animation when the tooltip is shown.
    tooltip.setOnShown(event -> {
      // Retrieve the underlying visual node of the tooltip from its skin.
      Node tooltipNode = tooltip.getSkin().getNode();
      // Start with the tooltip fully transparent.
      tooltipNode.setOpacity(0);
      // Create a fade transition to animate opacity from 0 (transparent) to 1 (opaque).
      FadeTransition fadeIn = new FadeTransition(Duration.millis(240), tooltipNode);
      fadeIn.setFromValue(0);
      fadeIn.setToValue(1);
      fadeIn.play(); // Execute the fade-in animation.

      // Reposition the tooltip lower on the screen.
      Platform.runLater(() -> {
        // Set tooltip position
        Point mouse = MouseInfo.getPointerInfo().getLocation();
        tooltip.setX(mouse.getX()-tooltip.getWidth()/2);
        tooltip.setY(mouse.getY()+15);
      });
    });

    // Configure fade-out animation when the tooltip is about to hide.
    tooltip.setOnHiding(event -> {
      // Consume the default hide event to delay the action until after our fade-out animation.
      event.consume();
      // Retrieve the tooltip's visual node.
      Node tooltipNode = tooltip.getSkin().getNode();
      // Create a fade transition to animate opacity from 1 (opaque) to 0 (transparent).
      FadeTransition fadeOut = new FadeTransition(Duration.millis(240), tooltipNode);
      fadeOut.setFromValue(1);
      fadeOut.setToValue(0);
      // Once the fade-out animation completes, manually hide the tooltip.
      fadeOut.setOnFinished(e -> tooltip.hide());
      fadeOut.play(); // Execute the fade-out animation.
    });

    return tooltip; // Return the fully configured tooltip.
  }
}
