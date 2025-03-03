import java.util.ArrayList;

public class List {
    private ArrayList<Task> listOfTasks = new ArrayList<>();
    private String listName;

    public List(String listName, ArrayList<Task> listOfTasks){
        this.listName = listName;
    }
    public String getListName(){
        return listName;
    }
    public  ArrayList<Task> getListOfTasks(){
        return listOfTasks;
    }
    public void setListName(String listName){
        this.listName = listName;
    }
    public void addTask(Task task){
        listOfTasks.add(task);
    }
    public void removeTask(Task task){
        listOfTasks.remove(task);
    }

}
