import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Scanner;

public class FileManager {
    public static void loadTasks() {
        Path dir = Paths.get("data");
        try {
            if (!Files.exists(dir)) {
                Files.createDirectory(dir);
            }
        } catch (IOException e) {
            System.out.println("Error in the IO when creating data dir");
        }

        Path file = Paths.get("data/taskData.txt");
        try {
            if (!Files.exists(file)) {
                file = Files.createFile(file);
            }
            Scanner scanner = new Scanner(file);
            while(scanner.hasNext()) {
                Parser.parseData(scanner.nextLine());
            }
        } catch (IOException e) {
            System.out.println("Error in the IO when creating taskData file");
        } catch (YapperException e) {
            System.out.println(e.getMessage());
        }
    }

    // can have a hasChanged check with the taskList to prevent rewriting data even if no changed occurred
    public static void saveTasks() throws YapperException {
        try (FileWriter fw = new FileWriter("data/taskData.txt")){
            fw.write(Parser.parseToData());
            fw.close();
        } catch (IOException e) {
            throw (new YapperException("IO Exception when saving data"));
        }
    }
}
