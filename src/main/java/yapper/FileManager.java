package yapper;

import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Scanner;

import exception.YapperException;

public class FileManager {
    private final Parser parser;
    public FileManager(Parser parser) {
        this.parser = parser;
    }

    public void loadTasks() {
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
                parser.parseData(scanner.nextLine());
            }
        } catch (IOException e) {
            System.out.println("Error in the IO when creating taskData file");
        } catch (YapperException e) {
            System.out.println(e.getMessage());
        }
    }

    // can have a hasChanged check with the taskList to prevent rewriting data even if no changed occurred
    public void saveTasks() throws YapperException {
        try (FileWriter fw = new FileWriter("data/taskData.txt")){
            fw.write(parser.parseToData());
        } catch (IOException e) {
            throw (new YapperException("IO Exception when saving data"));
        }
    }
}
