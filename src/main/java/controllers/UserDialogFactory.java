// Language: java
package controllers;

import dto.HouseholdDTO;
import dto.UserDTO;
import javafx.event.ActionEvent;
import javafx.geometry.Insets;
import javafx.scene.control.*;
import javafx.scene.layout.GridPane;

public class UserDialogFactory {

    private static <T> Dialog<T> createBasicDialog(String title, String header) {
        Dialog<T> dialog = new Dialog<>();
        dialog.setTitle(title);
        dialog.setHeaderText(header);
        return dialog;
    }

    private static GridPane createDialogGrid() {
        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(10);
        grid.setPadding(new Insets(20, 150, 10, 10));
        return grid;
    }

    private static void setupDialogButtons(Dialog<?> dialog, ButtonType... buttons) {
        dialog.getDialogPane().getButtonTypes().addAll(buttons);
        dialog.getDialogPane().getButtonTypes().add(ButtonType.CANCEL);
    }

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
            if(nameField.getText().trim().isEmpty()){
                errorLabel.setText("Name field cannot be empty!");
                e.consume();
            } else {
                errorLabel.setText("");
            }
        });

        dialog.setResultConverter(button -> {
            if(button == editButton){
                return new UserDTO(currentUser.getId(), nameField.getText(), household);
            }
            return null;
        });

        return dialog;
    }

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
            if(nameField.getText().trim().isEmpty()){
                errorLabel.setText("Name field cannot be empty!");
                e.consume();
            } else {
                errorLabel.setText("");
            }
        });

        dialog.setResultConverter(button -> {
            if(button == addButton){
                return new UserDTO(0, nameField.getText(), household);
            }
            return null;
        });
        return dialog;
    }
}