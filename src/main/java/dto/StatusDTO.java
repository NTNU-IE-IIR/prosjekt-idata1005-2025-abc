package dto;

import java.io.Serializable;

/**
 * Represents a task status.
 * Contains an identifier and a name for the status.
 */
public class StatusDTO implements SelectOption {
  private int id;
  private String name;

  /**
   * Default constructor.
   */
  public StatusDTO() {}

  /**
   * Constructs a StatusDTO with the specified id and name.
   *
   * @param id   the status id
   * @param name the status name
   */
  public StatusDTO(int id, String name) {
    this.id = id;
    this.name = name;
  }

  /**
   * Returns the status id.
   *
   * @return the id of the status
   */
  public int getId() {
    return id;
  }

  /**
   * Returns the status name.
   *
   * @return the name of the status
   */
  public String getName() {
    return name;
  }

  /**
   * Returns a String representation of the status.
   *
   * @return the name of the status
   */
  @Override
  public String toString() {
    return name;
  }
}