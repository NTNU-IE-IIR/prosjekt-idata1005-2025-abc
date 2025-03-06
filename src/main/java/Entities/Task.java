package Entities;

public class Task {
    private int id;
    private int householdId;
    private String description;
    private int statusId;
    private int priorityId;
    private int ownerId;
    private boolean isCompleted;

    // New constructor based on the new signature
    public Task(int id, int householdId, String description, int statusId, int priorityId, int ownerId) {
        this.id = id;
        this.householdId = householdId;
        this.description = description;
        this.statusId = statusId;
        this.priorityId = priorityId;
        this.ownerId = ownerId;
        this.isCompleted = false;
    }

    public int getId() {
        return id;
    }

    public int getHouseholdId() {
        return householdId;
    }

    public String getDescription() {
        return description;
    }

    public int getStatusId() {
        return statusId;
    }

    public int getPriorityId() {
        return priorityId;
    }

    public int getOwnerId() {
        return ownerId;
    }

    public boolean isCompleted() {
        return isCompleted;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public void setStatusId(int statusId) {
        this.statusId = statusId;
    }

    public void setPriorityId(int priorityId) {
        this.priorityId = priorityId;
    }

    public void setOwnerId(int ownerId) {
        this.ownerId = ownerId;
    }

    public void setCompleted(boolean completed) {
        this.isCompleted = completed;
    }
}