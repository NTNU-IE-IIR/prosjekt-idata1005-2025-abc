// Language: java
package controllers;

import javafx.geometry.Insets;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Dialog;
import javafx.scene.layout.GridPane;

/**
 * Provides base dialog creation utilities for all dialog factories.
 * This abstract class contains helper methods to create a basic dialog,
 * a preconfigured GridPane, and to set up dialog buttons.
 */
public abstract class DialogFactory {

    /**
     * Creates a basic dialog with the specified title and header.
     *
     * @param title  the title of the dialog
     * @param header the header text of the dialog
     * @param <T>    the type of the dialog result
     * @return a new Dialog instance
     */
    public static <T> Dialog<T> createBasicDialog(String title, String header) {
        Dialog<T> dialog = new Dialog<>();
        dialog.setTitle(title);
        dialog.setHeaderText(header);
        return dialog;
    }

    /**
     * Creates a GridPane configured with common gaps and padding for dialogs.
     *
     * @return a new configured GridPane instance
     */
    public static GridPane createDialogGrid() {
        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(10);
        grid.setPadding(new Insets(20, 150, 10, 10));
        return grid;
    }

    /**
     * Adds the provided button types to the dialog and a Cancel button.
     *
     * @param dialog  the Dialog to which the buttons are added
     * @param buttons the button types to add
     */
    public static void setupDialogButtons(Dialog<?> dialog, ButtonType... buttons) {
        dialog.getDialogPane().getButtonTypes().addAll(buttons);
        dialog.getDialogPane().getButtonTypes().add(ButtonType.CANCEL);
    }
}
