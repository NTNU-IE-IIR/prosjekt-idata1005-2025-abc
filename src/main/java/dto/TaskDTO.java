package dto;

/**
 * Represents a task with associated details such as description, status, priority, and assigned user.
 */
public class TaskDTO {
  private int id;
  private String description;
  private HouseholdDTO household;
  private StatusDTO status;
  private PriorityDTO priority;
  private UserDTO user;

  /**
   * Default constructor.
   */
  public TaskDTO() {}

  /**
   * Constructs a TaskDTO for a new task.
   *
   * @param description the task description
   * @param household   the household associated with the task
   * @param priority    the task priority
   * @param user        the user assigned to the task
   */
  public TaskDTO(String description, HouseholdDTO household, PriorityDTO priority, UserDTO user) {
    this.description = description;
    this.household = household;
    this.priority = priority;
    this.user = user;
  }

  /**
   * Constructs a TaskDTO with all properties for an existing task.
   *
   * @param id          the task id
   * @param description the task description
   * @param household   the associated household
   * @param status      the task status
   * @param priority    the task priority
   * @param user        the assigned user
   */
  public TaskDTO(int id, String description, HouseholdDTO household, StatusDTO status, PriorityDTO priority, UserDTO user) {
    this.id = id;
    this.description = description;
    this.household = household;
    this.status = status;
    this.priority = priority;
    this.user = user;
  }

  /**
   * Returns the task id.
   *
   * @return the id of the task
   */
  public int getId() {
    return id;
  }

  /**
   * Returns the household id associated with the task.
   *
   * @return the household id or null if not set
   */
  public Integer getHouseholdId() {
    if (household != null)
      return household.getId();
    return null;
  }

  /**
   * Returns the task description.
   *
   * @return the description of the task
   */
  public String getDescription() {
    return description;
  }

  /**
   * Returns the task status.
   *
   * @return the status of the task
   */
  public StatusDTO getStatus() {
    return status;
  }

  /**
   * Returns the task priority.
   *
   * @return the priority of the task
   */
  public PriorityDTO getPriority() {
    return priority;
  }

  /**
   * Returns the assigned user.
   *
   * @return the user assigned to the task, may be null
   */
  public UserDTO getUser() {
    return user;
  }

  /**
   * Returns the id of the task status.
   *
   * @return the status id or null if not set
   */
  public Integer getStatusId() {
    if (status != null)
      return status.getId();
    return null;
  }

  /**
   * Returns the id of the task priority.
   *
   * @return the priority id or null if not set
   */
  public Integer getPriorityId() {
    if (priority != null)
      return priority.getId();
    return null;
  }

  /**
   * Returns the id of the assigned user.
   *
   * @return the user id or null if not set
   */
  public Integer getUserId() {
    if (user != null)
      return user.getId();
    return null;
  }

  /**
   * Updates the task status.
   *
   * @param status the new status for the task
   */
  public void setStatus(StatusDTO status) {
    this.status = status;
  }

  /**
   * Updates the task priority.
   *
   * @param priority the new priority for the task
   */
  public void setPriority(PriorityDTO priority) {
    this.priority = priority;
  }
}