package yapper;

import java.time.LocalDate;
import java.time.format.DateTimeParseException;

import exception.YapperException;
import task.Deadline;
import task.Event;
import task.Task;
import task.Todo;

/**
 * Handles interpreting strings for commands and task list data.
 */
public class Parser {
    private final TaskList mainTasks;
    private final Ui ui;
    private FileManager fm;

    /**
     * Initialises a Parser that corresponds to a main {@link TaskList} and {@link Ui}.
     *
     * @param mainTasks Corresponding main {@link TaskList}.
     * @param ui Corresponding {@link Ui}.
     */
    public Parser(TaskList mainTasks, Ui ui) {
        this.mainTasks = mainTasks;
        this.ui = ui;
    }

    public void setFileManager(FileManager fileManager) {
        fm = fileManager;
    }

    /**
     * Takes parsed command and calls the related functions.
     * Further parses argument String as required by the identified command.
     *
     * @throws YapperException Thrown when invalid command, arguments, index or formatting detected.
     */
    public String parseCommand(String input) throws YapperException {
        String[] cmdArg = input.trim().split(" ", 2); // [command, arguments]
        Yapper.Command cmd = Yapper.Command.valueOfCommandName(cmdArg[0]);
        String response = "";

        boolean isInvalidCommand = cmd == null;
        boolean hasNoArguments = cmdArg.length != 2;

        if (isInvalidCommand) {
            throw (new YapperException("What is blud yappin'? Here's the legit commands:\n"
                    + "help, list, todo, deadline, event, mark, unmark, delete, find, bye"));
        }

        if (hasNoArguments) {
            parseHasNoArgumentException(cmd);
        }

        // Commands create a Command class in the future
        switch (cmd) {
        case HELP:
            response = Ui.helpMessage();
            break;
        case BYE:
            try {
                assert(fm != null);
                fm.saveTasks();
            } catch (YapperException y) {
                response = y.getMessage();
            } finally {
                response += ui.byeMessage();
            }
            break;
        case LIST:
            response = mainTasks.listTasks();
            break;
        case FIND:
            // can be expanded to change current TaskList to look at found Task List
            response = mainTasks.find(cmdArg[1]);
            break;
        case MARK:
            // Fallthrough
        case UNMARK:
            // Fallthrough
        case DELETE:
            try {
                response = parseMarkUnmarkDelete(cmdArg);
            } catch (java.lang.NumberFormatException e) { // non number typed
                throw (new YapperException("Ain't no way! We lackin' just numbers after mark/unmark/delete.\n"
                        + "e.g. unmark 2"));
            } catch (YapperException e) {
                throw (e);
            }
            break;
        case TODO:
            response = parseTask(cmdArg[1], Task.ID.TODO);
            break;
        case DEADLINE:
            response = parseTask(cmdArg[1], Task.ID.DEADLINE);
            break;
        case EVENT:
            response = parseTask(cmdArg[1], Task.ID.EVENT);
            break;
        default: // Shouldn't reach here, invalid commands should be null
            throw (new YapperException("What is blud yappin'? Here's the legit commands:\n"
                    + "help, list, todo, deadline, event, mark, unmark, delete, find, bye\n"));
        }
        return response;
    }

    private void parseHasNoArgumentException(Yapper.Command cmd) throws YapperException {
        switch (cmd) {
        case FIND:
            throw (new YapperException("What is lil bro looking for?"));
        case MARK:
            throw (new YapperException("Ain't no way! Which task in the list we vibin' with?\n"
                    + "e.g. mark 1"));
        case UNMARK:
            throw (new YapperException("Ain't no way! Which task in the list we vibin' with?\n"
                    + "e.g. unmark 1"));
        case DELETE:
            throw (new YapperException("Ain't no way! Which task in the list we vibin' with?\n"
                    + "e.g. delete 1"));
        case TODO:
            throw (new YapperException("Ain't no way! You got caught lackin' the format!\n"
                    + "e.g. todo <task>"));
        case DEADLINE:
            throw (new YapperException("Ain't no way! You got caught lackin' the format!\n"
                    + "e.g. deadline <task> /by <date/time>\n"));
        case EVENT:
            throw (new YapperException("Ain't no way! You got caught lackin' the format!\n"
                    + "e.g. event <task> /from <start date/time> /to <start date/time>\n"));
        case HELP:
            // Fallthrough
        case LIST:
            // Fallthrough
        case BYE:
            break; // HELP, LIST, BYE expects no arguments
        default:
            throw (new YapperException("Unhandled command type in parseHasNoArgumentException\n"));
        }
    }

    /**
     * Parses command and arguments to call mark, unmark and delete commands.
     *
     * @param cmdArg Array where 0-th element is the command and subsequent elements are the arguments.
     * @return Response after successful execution of command.
     * @throws YapperException Occurs when there is a incorrect index.
     */
    private String parseMarkUnmarkDelete(String[] cmdArg) throws YapperException, java.lang.NumberFormatException {
        String response;
        int i = Integer.parseInt(cmdArg[1]);

        boolean isIndexTooLarge = i > mainTasks.listSize();
        boolean isIndexTooSmall = i < 1;

        if (isIndexTooLarge) {
            throw (new YapperException("You ain't got that many tasks bruh!\n"));
        }

        if (isIndexTooSmall) {
            throw (new YapperException("Start from task 1 lil bro!\n"));
        }

        // Execute MARK/UNMARK/DELETE
        switch (cmdArg[0]) {
        case "mark":
            response = mainTasks.markTask(i);
            break;
        case "unmark":
            response = mainTasks.unmarkTask(i);
            break;
        case "delete":
            response = mainTasks.deleteTask(i);
            break;
        default:
            throw new YapperException("Unexpected command fallen through the switch case\n");
        }
        return response;
    }

    /**
     * Parses arguments for their corresponding {@link Task} type for initialising and adding to {@link TaskList}.
     *
     * @param arg Parsed arguments to be split based on identified {@link Task} type.
     * @param id Type of {@link Task} identified.
     * @throws YapperException Incorrect types of formatting detected.
     */
    public String parseTask(String arg, Task.ID id) throws YapperException {
        String response;
        switch (id) {
        case TODO:
            response = parseTodo(arg);
            break;
        case DEADLINE: {
            response = parseDeadline(arg);
            break;
        }
        case EVENT: {
            response = parseEvent(arg);
            break;
        }
        default: // Invalid Task ID
            throw (new YapperException("Invalid Task ID, user shouldn't reach here"));
        }
        return response;
    }

    private String parseTodo(String arg) {
        String response;
        Todo todo = new Todo(arg);
        response = mainTasks.addTask(todo);
        return response;
    }

    private String parseDeadline(String arg) throws YapperException {
        String response;
        String[] descDate = arg.split(" /by ", 2); // [description, by]
        boolean isIncorrectByFormat = descDate.length != 2;
        if (isIncorrectByFormat) {
            throw (new YapperException("When you wanna do this task by lil bro?\n"
                    + "type deadline <task> /by <yyyy-mm-dd>\n"
                    + "e.g. deadline hit the griddy by 2024-12-31"));
        }

        try {
            LocalDate deadlineBy = LocalDate.parse(descDate[1]);
            Deadline deadline = new Deadline(descDate[0], deadlineBy);
            response = mainTasks.addTask(deadline);
        } catch (DateTimeParseException e) { // incorrect formatting for date
            throw (new YapperException("When you wanna do this task by lil bro?\n"
                    + "type deadline <task> /by <yyyy-mm-dd>\n"
                    + "e.g. deadline hit the griddy by 2024-12-31"));
        }
        return response;
    }

    private String parseEvent(String arg) throws YapperException {
        String response;
        String[] descDate = arg.split(" /from ", 2); // [description, fromTo]

        boolean isIncorrectFromFormat = descDate.length != 2;
        if (isIncorrectFromFormat) {
            throw (new YapperException("When does this event start lil bro?\n"
                    + "type event <task> /from <yyyy-mm-dd> /to <yyyy-mm-dd>\n"
                    + "e.g. event party rock /from <yyyy-mm-dd> /to <yyyy-mm-dd>"));
        }

        String[] fromTo = descDate[1].split(" /to ", 2); // [from , to]

        boolean isIncorrectToFormat = fromTo.length != 2;
        if (isIncorrectToFormat) {
            throw (new YapperException("When does this event end lil bro?\n"
                    + "type event <task> /from <yyyy-mm-dd> /to <yyyy-mm-dd>\n"
                    + "e.g. event party rock /from <yyyy-mm-dd> /to <yyyy-mm-dd>"));
        }

        try {
            LocalDate eventFrom = LocalDate.parse(fromTo[0]);
            LocalDate eventTo = LocalDate.parse(fromTo[1]);
            Event event = new Event(descDate[0], eventFrom, eventTo);
            response = mainTasks.addTask(event);
        } catch (DateTimeParseException e) {
            throw (new YapperException("When does this event start/end lil bro?\n"
                    + "type event <task> /from <yyyy-mm-dd> /to <yyyy-mm-dd>\n"
                    + "e.g. event party rock /from <yyyy-mm-dd> /to <yyyy-mm-dd>"));
        }
        return response;
    }

    /**
     * Parses task list data to create and add tasks to the {@link TaskList}.
     *
     * @param data Task list data saved in the local file represented as a String.
     * @throws YapperException Data found in local file is detected to be incorrectly formatted.
     */
    public void parseDataToTask(String data) throws YapperException {
        String[] taskData = data.split("" + " / ");
        switch (taskData[0]) {
        case "T":
            parseDataToTodo(taskData);
            break;
        case "D":
            parseDataToDeadline(taskData);
            break;
        case "E":
            parseDataToEvent(taskData);
            break;
        default:
            throw new YapperException("Error in the save files 9");
        }
    }

    private void parseDataToTodo(String[] taskData) throws YapperException {
        boolean hasWrongNumberOfArgs;
        boolean isDone;
        hasWrongNumberOfArgs = taskData.length != 3;
        if (hasWrongNumberOfArgs) {
            throw new YapperException("Error in the save files 1");
        }

        if (taskData[1].equals("0")) {
            isDone = false;
        } else if (taskData[1].equals("1")) {
            isDone = true;
        } else {
            throw new YapperException("Error in the save files 2");
        }

        mainTasks.addTaskNoMessage(new Todo(isDone, taskData[2]));
    }

    private void parseDataToDeadline(String[] taskData) throws YapperException {
        boolean hasWrongNumberOfArgs;
        boolean isDone;
        hasWrongNumberOfArgs = taskData.length != 4;
        if (hasWrongNumberOfArgs) {
            throw new YapperException("Error in the save files 3");
        }

        if (taskData[1].equals("0")) {
            isDone = false;
        } else if (taskData[1].equals("1")) {
            isDone = true;
        } else {
            throw new YapperException("Error in the save files 4");
        }

        try {
            LocalDate by = LocalDate.parse(taskData[3]);
            mainTasks.addTaskNoMessage(new Deadline(isDone, taskData[2], by));
        } catch (DateTimeParseException e) {
            throw new YapperException("Error in the save files 5");
        }
    }

    private void parseDataToEvent(String[] taskData) throws YapperException {
        boolean isDone;
        boolean hasWrongNumberOfArgs;
        hasWrongNumberOfArgs = taskData.length != 5;
        if (hasWrongNumberOfArgs) {
            throw new YapperException("Error in the save files 6");
        }

        if (taskData[1].equals("0")) {
            isDone = false;
        } else if (taskData[1].equals("1")) {
            isDone = true;
        } else {
            throw new YapperException("Error in the save files 7");
        }

        try {
            LocalDate from = LocalDate.parse(taskData[3]);
            LocalDate to = LocalDate.parse(taskData[3]);
            mainTasks.addTaskNoMessage(new Event(isDone, taskData[2], from, to));
        } catch (DateTimeParseException e) {
            throw new YapperException("Error in the save files 8");
        }
    }

    /**
     * Parses all tasks in the {@link TaskList} into format to be saved in the local file.
     *
     * @return String representation of task list data to be saved to the local file.
     */
    public String parseTaskToData() {
        String data = "";
        for (int i = 0; i < mainTasks.listSize(); i++) {
            Task task = mainTasks.getTask(i);

            if (task instanceof Todo) {
                data += String.format("T / %d / %s",
                        task.getIsDoneInt(),
                        task.getDescription());
            } else if (task instanceof Deadline) {
                Deadline deadline = (Deadline) task;
                data += String.format("D / %d / %s / %s",
                        task.getIsDoneInt(),
                        task.getDescription(),
                        deadline.getBy());
            } else if (task instanceof Event) {
                Event event = (Event) task;
                data += String.format("E / %d / %s / %s / %s",
                        task.getIsDoneInt(),
                        task.getDescription(),
                        event.getFrom(),
                        event.getTo());
            } else {
                System.out.println("Unaccounted for Task type present");
            }

            // add new line after each task except for the last line
            if (i != mainTasks.listSize() - 1) {
                data += System.lineSeparator();
            }
        }
        return data;
    }
}
