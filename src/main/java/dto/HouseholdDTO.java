package dto;

public class HouseholdDTO {
  private int id;
  private String name;
  private String password;

  public HouseholdDTO() {}

  public HouseholdDTO(int id, String name) {
    this.id = id;
    this.name = name;
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
