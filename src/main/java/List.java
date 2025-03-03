import java.util.ArrayList;

public class List {
    private ArrayList<Tasks> listOfTasks = new ArrayList<>();
    private String listName;

    public List(String listName){
        this.listName = listName;
    }
    public String getListName(){
        return listName;
    }
    public  ArrayList<Tasks> getListOfTasks(){
        return listOfTasks;
    }
    public void setListName(String listName){
        this.listName = listName;
    }
    public void addTask(Tasks task){
        listOfTasks.add(task);
    }
    public void removeTask(Tasks task){
        listOfTasks.remove(task);
    }

}
