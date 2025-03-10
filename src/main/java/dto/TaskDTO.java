package dto;

public class TaskDTO {
  private int id;
  private String description;
  private HouseholdDTO household;
  private StatusDTO status;
  private PriorityDTO priority;
  private UserDTO user;

  public TaskDTO() {}

  public TaskDTO(String description, HouseholdDTO household, PriorityDTO priority, UserDTO user) {
    this.description = description;
    this.household = household;
    this.priority = priority;
    this.user = user;
  }

  public TaskDTO(int id, String description, HouseholdDTO household, StatusDTO status, PriorityDTO priority, UserDTO user) {
    this.id = id;
    this.description = description;
    this.household = household;
    this.status = status;
    this.priority = priority;
    this.user = user;
  }

  public int getId(){
    return id;
  }

  public Integer getHouseholdId(){
    if(household != null)
      return household.getId();
    return null;
  }

  public String getDescription(){
    return description;
  }

  public StatusDTO getStatus(){
    return status;
  }

  public PriorityDTO getPriority(){
    return priority;
  }

  public UserDTO getUser(){
    return user;
  }

  public Integer getStatusId(){
    if(status != null)
      return status.getId();
    return null;
  }

  public Integer getPriorityId(){
    if(priority != null)
      return priority.getId();
    return null;
  }

  public Integer getUserId(){
    if(user != null)
      return user.getId();
    return null;
  }
  public void setStatus(StatusDTO status){
    this.status = status;
  }
  public void setPriority(PriorityDTO priority){
    this.priority = priority;
  }
}