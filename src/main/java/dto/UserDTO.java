package dto;

public class UserDTO {
  private int id;
  private String name;
  private HouseholdDTO household;

  public UserDTO() {}

  public UserDTO(int id, String name, HouseholdDTO household) {
    this.id = id;
    this.name = name;
    this.household = household;
  }

  public int getId() {
    return id;
  }

  public String getName() {
    return name;
  }

  @Override
  public String toString() {
    return name;
  }
}
