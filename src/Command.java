import java.util.Scanner;

// Command pattern: each menu action is an object with a name and an execute method.
// Department (the invoker) runs a command without knowing which use case it is.
// Each command talks to its department's controller (the receiver) to do the work.
public interface Command {
    // Shown in the department menu
    String getName();

    // Runs the use case: asks for input, calls the controller, prints the result
    void execute(Scanner in);
}
