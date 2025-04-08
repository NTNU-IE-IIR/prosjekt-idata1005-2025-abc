package gui.components;

import dto.UserDTO;
import gui.helpers.TooltipHelper;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.control.Button;
import javafx.scene.control.ListCell;
import javafx.scene.image.ImageView;
import javafx.scene.layout.HBox;

import java.io.IOException;
import java.util.function.Consumer;

/**
 * A custom ListCell implementation for displaying a {@link UserDTO} in a list view.
 * <p>
 * This cell is configured via FXML and provides three buttons:
 * <ul>
 *   <li>Name Button: Displays the user's name and triggers a callback when clicked.</li>
 *   <li>Edit Button: Triggers an edit callback for the user.</li>
 *   <li>Delete Button: Triggers a delete callback for the user.</li>
 * </ul>
 * Tooltips are attached to these buttons using the {@link TooltipHelper} to provide additional
 * information to the user.
 * </p>
 */
public class UserList extends ListCell<UserDTO> {

  // FXML-injected UI elements from UserList.fxml

  /** The container that holds all UI elements for this cell. */
  @FXML
  private HBox container;

  /** Button displaying the user's name. */
  @FXML
  private Button nameButton;

  /** Button used to trigger editing of the user. */
  @FXML
  private Button editButton;

  /** Button used to trigger deletion of the user. */
  @FXML
  private Button deleteButton;

  /** Image icon associated with the edit button. */
  @FXML
  private ImageView editIcon;

  /** Image icon associated with the delete button. */
  @FXML
  private ImageView deleteIcon;

  // Callback functions to handle button actions

  /**
   * Callback for when the name button is clicked.
   */
  private final Consumer<UserDTO> clickListener;

  /**
   * Callback for when the edit button is clicked.
   */
  private final Consumer<UserDTO> editClickListener;

  /**
   * Callback for when the delete button is clicked.
   */
  private final Consumer<UserDTO> deleteClickListener;

  /**
   * Constructs a new {@code UserList} cell with the specified callback listeners.
   *
   * @param clickListener       the callback function to invoke when the name button is clicked.
   * @param editClickListener   the callback function to invoke when the edit button is clicked.
   * @param deleteClickListener the callback function to invoke when the delete button is clicked.
   */
  public UserList(Consumer<UserDTO> clickListener, Consumer<UserDTO> editClickListener, Consumer<UserDTO> deleteClickListener) {
    this.clickListener = clickListener;
    this.editClickListener = editClickListener;
    this.deleteClickListener = deleteClickListener;
    loadFXML();
  }

  /**
   * Loads the associated FXML file for this cell and sets this class as its controller.
   * <p>
   * The FXML file is expected to be located at "/gui/components/UserList.fxml".
   * </p>
   */
  private void loadFXML() {
    try {
      FXMLLoader loader = new FXMLLoader(getClass().getResource("/gui/components/UserList.fxml"));
      loader.setController(this); // Set this class as the controller for the FXML.
      loader.load();
    } catch (IOException e) {
      e.printStackTrace();
    }
  }

  /**
   * Updates the content of this cell with the given {@link UserDTO} item.
   * <p>
   * This method sets the text on the nameButton and attaches action listeners
   * and tooltips to the buttons based on the provided {@code UserDTO}. If the item is empty or null,
   * the graphic is cleared.
   * </p>
   *
   * @param user  the {@link UserDTO} item for this cell.
   * @param empty indicates whether this cell represents data.
   */
  @Override
  protected void updateItem(UserDTO user, boolean empty) {
    super.updateItem(user, empty);
    if (empty || user == null) {
      // Clear the cell if there's no data.
      setGraphic(null);
    } else {
      // Set the name button's text using the user's name.
      nameButton.setText(user.getName());

      // Set the action handler for the name button to trigger the callback with the user.
      nameButton.setOnAction(event -> clickListener.accept(user));

      // Set the action handler for the edit button to trigger the edit callback with the user.
      editButton.setOnAction(event -> editClickListener.accept(user));

      // Set the action handler for the delete button to trigger the delete callback with the user.
      deleteButton.setOnAction(event -> deleteClickListener.accept(user));

      // Bind tooltips to the buttons using TooltipHelper.
      TooltipHelper.bindTooltip(nameButton, "View tasks only for " + user.getName());
      TooltipHelper.bindTooltip(editButton, "Edit persons name");
      TooltipHelper.bindTooltip(deleteButton, "Delete person");

      // Set the container as the graphic for this cell.
      setGraphic(container);
    }
  }
}
