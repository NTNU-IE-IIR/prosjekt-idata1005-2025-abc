package dto;

/**
 * Represents a user with an id, name, and associated household.
 */
public class UserDTO {
  private int id;
  private String name;
  private HouseholdDTO household;

  /**
   * Default constructor.
   */
  public UserDTO() {}

  /**
   * Constructs a UserDTO with the specified id, name, and household.
   *
   * @param id        the user id
   * @param name      the user name
   * @param household the household associated with the user
   */
  public UserDTO(int id, String name, HouseholdDTO household) {
    this.id = id;
    this.name = name;
    this.household = household;
  }

  /**
   * Returns the user id.
   *
   * @return the id of the user
   */
  public int getId() {
    return id;
  }

  /**
   * Returns the user name.
   *
   * @return the name of the user
   */
  public String getName() {
    return name;
  }

  /**
   * Returns a String representation of the user.
   *
   * @return the user's name
   */
  @Override
  public String toString() {
    return name;
  }

  /**
   * Returns the household associated with the user.
   *
   * @return the HouseholdDTO instance
   */
  public HouseholdDTO getHousehold() {
    return household;
  }
}