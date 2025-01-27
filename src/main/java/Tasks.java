import java.util.Date;

public class Tasks {
    private Date date;
    private String title;
    private String description;
    private boolean state = false; //false if the task is undone

    public Tasks(Date date, String title, String description,boolean state) {
        this.date = date;
        this.title = title;
        this.description = description;
        this.state = state;
    }
    public Date getDate(){
        return date;
    }
    public String getTitle(){
        return title;
    }
    public String getDescription(){
        return description;
    }
    public void setData(Date date){
        this.date = date;
    }
    public void setTitle(String title){
        this.title = title;
    }
    public void setDescription(String description){
        this.description = description;
    }
    public boolean getState(){
        return state;
    }
    public void setState(boolean state){
        this.state = state;
    }
}
