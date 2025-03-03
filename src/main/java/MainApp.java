import javafx.concurrent.Task;

import java.util.ArrayList;

public class MainApp {
    private Task task;
    public void printAllTasks(){
        ArrayList<Task> tasks = new ArrayList<Task>();
        for (Task task: tasks){
            System.out.println(task);
        }
    }
    
}
