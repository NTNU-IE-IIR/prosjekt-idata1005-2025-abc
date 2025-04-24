package dto;

/**
 * Data Transfer Object (DTO) representing a Household.
 *
 * <p>This class is used to transfer household data between different layers of an application,
 * typically from the database to the business logic or presentation layer. It contains basic
 * household information such as ID, name, and optionally a password.</p>
 */
public class HouseholdDTO {

  /** The unique identifier for the household. */
  private int id;

  /** The name of the household. */
  private String name;

  /** The password associated with the household (not used in current constructor or methods). */
  private String password;

  /**
   * Default no-argument constructor.
   * <p>Creates an empty HouseholdDTO instance. Required for frameworks that use reflection
   * or for serialization/deserialization purposes.</p>
   */
  public HouseholdDTO() {}

  /**
   * Constructs a HouseholdDTO with the specified ID and name.
   *
   * @param id the unique identifier for the household
   * @param name the name of the household
   */
  public HouseholdDTO(int id, String name) {
    this.id = id;
    this.name = name;
  }

  /**
   * Returns the unique identifier of the household.
   *
   * @return the household ID
   */
  public int getId() {
    return id;
  }

  /**
   * Returns the name of the household.
   *
   * @return the household name
   */
  public String getName() {
    return name;
  }

  /**
   * Returns a string representation of the household.
   * <p>Overrides the default {@code toString()} method to return the household's name.</p>
   *
   * @return the name of the household
   */
  @Override
  public String toString() {
    return name;
  }
}
