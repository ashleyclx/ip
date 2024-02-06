package yapper;

import java.util.HashMap;
import java.util.Map;

import exception.YapperException;

public class Yapper {
    public enum Command {
        BYE("bye"),
        LIST("list"),
        MARK("mark"),
        UNMARK("unmark"),
        DELETE("delete"),
        TODO("todo"),
        DEADLINE("deadline"),
        EVENT("event"),
        FIND("find");

        private static final Map<String, Command> COMMAND_MAP = new HashMap<>();
        private final String name;

        private Command(String name) {
            this.name = name;
        }

        static {
            for (Command c: values()) {
                COMMAND_MAP.put(c.name, c);
            }
        }

        public static Command valueOfCommandName(String name) {
            return COMMAND_MAP.get(name);
        }
    }

    public static void run() {
        Ui.hello();
        TaskList mainTasks = new TaskList();
        Ui ui = new Ui(mainTasks);
        Parser parser = new Parser(mainTasks, ui);
        FileManager fm = new FileManager(parser);
        fm.loadTasks();
        while (!ui.hasEnded()) {
            try {
                parser.parseCommand();
            } catch (YapperException e) {
                System.out.println(e.getMessage());
            }

            if (ui.hasEnded()) {
                try {
                    fm.saveTasks();
                } catch (YapperException e) {
                    System.out.println(e.getMessage());
                }
            }
            System.out.print(Ui.line());
        }
    }

    public static void main(String[] args) {
        Yapper.run();
    }
}
