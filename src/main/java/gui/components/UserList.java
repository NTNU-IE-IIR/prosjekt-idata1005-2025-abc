package gui.components;

import dto.UserDTO;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.control.Button;
import javafx.scene.control.ListCell;
import javafx.scene.image.ImageView;
import javafx.scene.layout.HBox;
import java.io.IOException;
import java.util.function.Consumer;

public class UserList extends ListCell<UserDTO> {
  @FXML private HBox container;
  @FXML private Button nameButton;
  @FXML private Button editButton;
  @FXML private Button deleteButton;
  @FXML private ImageView editIcon;
  @FXML private ImageView deleteIcon;

  private final Consumer<UserDTO> clickListener; // Callback function
  private final Consumer<UserDTO> editClickListener; // Name button callback
  private final Consumer<UserDTO> deleteClickListener; // Edit button callback

  public UserList(Consumer<UserDTO> clickListener, Consumer<UserDTO> editClickListener, Consumer<UserDTO> deleteClickListener) {
    this.clickListener = clickListener;
    this.editClickListener = editClickListener;
    this.deleteClickListener = deleteClickListener;
    loadFXML();
  }

  private void loadFXML() {
    try {
      FXMLLoader loader = new FXMLLoader(getClass().getResource("/gui/components/UserList.fxml"));
      loader.setController(this);
      loader.load();
    } catch (IOException e) {
      e.printStackTrace();
    }
  }

  @Override
  protected void updateItem(UserDTO user, boolean empty) {
    super.updateItem(user, empty);
    if (empty || user == null) {
      setGraphic(null);
    } else {
      nameButton.setText(user.getName());
      // When the button is clicked, trigger the parent view method
      nameButton.setOnAction(event -> clickListener.accept(user));
      editButton.setOnAction(event -> editClickListener.accept(user));
      deleteButton.setOnAction(event -> deleteClickListener.accept(user));

      setGraphic(container);
    }
  }
}
