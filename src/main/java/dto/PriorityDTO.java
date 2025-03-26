package dto;

/**
 * Represents a task priority.
 * Contains an identifier and a name for the priority.
 */
public class PriorityDTO implements SelectOption {
  private int id;
  private String name;

  /**
   * Default constructor.
   */
  public PriorityDTO() {}

  /**
   * Constructs a PriorityDTO with the specified id and name.
   *
   * @param id   the priority id
   * @param name the priority name
   */
  public PriorityDTO(int id, String name) {
    this.id = id;
    this.name = name;
  }

  /**
   * Returns the priority id.
   *
   * @return the id of the priority
   */
  public int getId() {
    return id;
  }

  /**
   * Returns the priority name.
   *
   * @return the name of the priority
   */
  public String getName() {
    return name;
  }

  /**
   * Returns a String representation of the priority.
   *
   * @return the name of the priority
   */
  @Override
  public String toString() {
    return name;
  }
}