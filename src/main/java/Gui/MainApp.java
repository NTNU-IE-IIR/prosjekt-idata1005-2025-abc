package Gui;

import javafx.concurrent.Task;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.stage.Stage;
import javafx.scene.Parent;
import javafx.geometry.Pos;

public class MainApp {

    private BorderPane root;
    private TableView<Task> taskTable;

    public MainApp() {
        root = new BorderPane();


        root.getStylesheets().add(getClass().getResource("/Gui/mainapp.css").toExternalForm());

        HBox topBar = new HBox(15);
        topBar.getStyleClass().add("top-bar");
        topBar.setPadding(new Insets(10));
        topBar.setAlignment(Pos.CENTER_LEFT);


        Button addTaskBtn = new Button("Add new task");
        Button distributeBtn = new Button("Distribute all tasks");
        Button closeDoneBtn = new Button("Close done tasks");


        TextField searchField = new TextField();
        searchField.setPromptText("Search tasks...");


        topBar.getChildren().addAll(addTaskBtn, distributeBtn, closeDoneBtn, searchField);


        taskTable = new TableView<>();
        taskTable.getStyleClass().add("task-table");


        TableColumn<Task, String> taskNameCol = new TableColumn<>("Task");
        TableColumn<Task, String> priorityCol = new TableColumn<>("Priority");
        TableColumn<Task, String> statusCol = new TableColumn<>("Status");
        TableColumn<Task, String> descCol = new TableColumn<>("Description");


        taskTable.getColumns().addAll(taskNameCol, priorityCol, statusCol, descCol);

        root.setTop(topBar);
        root.setCenter(taskTable);

        addTaskBtn.setOnAction(e -> {

            System.out.println("Add new task clicked!");
        });

        distributeBtn.setOnAction(e -> {
            System.out.println("Distribute tasks clicked!");
        });

        closeDoneBtn.setOnAction(e -> {
            System.out.println("Close done tasks clicked!");
        });

        searchField.setOnAction(e -> {
            String searchText = searchField.getText();
            System.out.println("Search tasks for: " + searchText);
        });
    }

    public Parent getRoot() {
        return root;
    }
}
