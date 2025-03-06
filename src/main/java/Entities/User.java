package Entities;

public class User {
    private String name;
    private int userId;
    private int householdId;

    public User(int userId, int householdId, String name){
        this.householdId = householdId;
        this.name = name;
        this.userId = userId;
    }
    public String getName(){
        return name;
    }
    public int getUserId(){
        return userId;
    }

    public void setName(String name){
        this.name = name;
    }
    public void setUserId(int userId){
        this.userId = userId;
    }
    public int getHouseholdId(){
        return householdId;
    }
    public void setHouseholdId(int householdId){
        this.householdId = householdId;
    }
}
