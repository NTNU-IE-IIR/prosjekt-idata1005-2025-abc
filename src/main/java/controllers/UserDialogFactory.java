// Language: java
package controllers;

import dto.HouseholdDTO;
import dto.UserDTO;
import javafx.event.ActionEvent;
import javafx.geometry.Insets;
import javafx.scene.control.ButtonBar;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Dialog;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.layout.GridPane;

/**
 * Factory class for creating dialogs related to User operations.
 * It provides methods to create dialogs for adding or editing a user.
 */
public class UserDialogFactory extends DialogFactory {

    /**
     * Creates a dialog to edit an existing user's details.
     *
     * @param currentUser the user being edited
     * @param household   the associated household
     * @return a Dialog that returns an updated UserDTO on success, or null on cancel
     */
    public static Dialog<UserDTO> createEditUserDialog(UserDTO currentUser, HouseholdDTO household) {
        Dialog<UserDTO> dialog = createBasicDialog("Edit User Name", "Enter user details");
        GridPane grid = createDialogGrid();

        TextField nameField = new TextField(currentUser.getName());
        nameField.setPromptText("New name");
        Label errorLabel = new Label();

        grid.add(new Label("Name:"), 0, 0);
        grid.add(nameField, 1, 0);
        grid.add(errorLabel, 1, 1);

        ButtonType editButton = new ButtonType("Edit", ButtonBar.ButtonData.OK_DONE);
        setupDialogButtons(dialog, editButton);
        dialog.getDialogPane().setContent(grid);

        dialog.getDialogPane().lookupButton(editButton).addEventFilter(ActionEvent.ACTION, e -> {
            if (nameField.getText().trim().isEmpty()) {
                errorLabel.setText("Name field cannot be empty!");
                e.consume();
            } else {
                errorLabel.setText("");
            }
        });

        dialog.setResultConverter(button -> {
            if (button == editButton) {
                return new UserDTO(currentUser.getId(), nameField.getText(), household);
            }
            return null;
        });

        return dialog;
    }

    /**
     * Creates a dialog to add a new user.
     *
     * @param household the associated household for the new user
     * @return a Dialog that returns a new UserDTO on success, or null on cancel
     */
    public static Dialog<UserDTO> createAddUserDialog(HouseholdDTO household) {
        Dialog<UserDTO> dialog = createBasicDialog("Add New User", "Enter user details");
        GridPane grid = createDialogGrid();

        TextField nameField = new TextField();
        nameField.setPromptText("User name");
        Label errorLabel = new Label();

        grid.add(new Label("Name:"), 0, 0);
        grid.add(nameField, 1, 0);
        grid.add(errorLabel, 1, 1);

        ButtonType addButton = new ButtonType("Add", ButtonBar.ButtonData.OK_DONE);
        setupDialogButtons(dialog, addButton);
        dialog.getDialogPane().setContent(grid);

        dialog.getDialogPane().lookupButton(addButton).addEventFilter(ActionEvent.ACTION, e -> {
            if (nameField.getText().trim().isEmpty()) {
                errorLabel.setText("Name field cannot be empty!");
                e.consume();
            } else {
                errorLabel.setText("");
            }
        });

        dialog.setResultConverter(button -> {
            if (button == addButton) {
                return new UserDTO(0, nameField.getText(), household);
            }
            return null;
        });
        return dialog;
    }
}