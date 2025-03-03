
import com.google.gson.Gson;

import java.io.*;

public class DataHandler {
    private static final Gson gson = new Gson();
    private static final String dataFile = "resources";

    private static String getDataFileName(User username){
        return username  + "_household_data.json";
    }
    public static void saveUser(User user){
        try {
            BufferedWriter bufferedWriter = new BufferedWriter(new FileWriter(dataFile));
            bufferedWriter.write(user.getUsername());
//            bufferedWriter.write(user.getPassword());
            bufferedWriter.close();
        }catch (IOException e){
            System.err.println(e.getMessage());
        }
    }
    public static User loadUser(String username){
        try {
            BufferedReader bufferedReader = new BufferedReader(new FileReader(dataFile));
            readFile(dataFile);
            if (logicMatches(username) == true)
                return gson.fromJson(bufferedReader.readLine(), User.class);
            return gson.fromJson(bufferedReader, User.class);
        }catch(IOException e){
            System.err.println(e.getMessage());
        }
        return null; // TODO
    }
    public static void readFile(String dataFile){
        try {
            BufferedReader bufferedReader = new BufferedReader(new FileReader(dataFile));
        } catch (FileNotFoundException e) {
            throw new RuntimeException(e);
        }
    }
    public static boolean logicMatches(String username){
        //TODO
        return true;
    }
}

