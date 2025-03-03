import java.util.Date;

public class Task {
    private Date dueDate;
    private String title;
    private String description;
    private boolean isCompleted;

    public Task(Date dueDate, String title, String description, boolean isCompleted) {
        this.dueDate = dueDate;
        this.title = title;
        this.description = description;
        this.isCompleted = false;
    }
    public Date getDueDate(){
        return dueDate;
    }
    public String getTitle(){
        return title;
    }
    public String getDescription(){
        return description;
    }
    public void setData(Date date){
        this.dueDate = date;
    }
    public void setTitle(String title){
        this.title = title;
    }
    public void setDescription(String description){
        this.description = description;
    }
    public boolean getCompleted(){
        return isCompleted;
    }
    public void setCompleted(boolean completed){
        this.isCompleted = completed;
    }
    //TODO REOCURRING TASKS
}
