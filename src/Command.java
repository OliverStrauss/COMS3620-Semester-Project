import java.util.Scanner;

// Command pattern: one object per use case. A department's menu is a list of these,
// so the menu shows getName() and runs execute() without a switch on the choice number
public interface Command {

    // The text shown for this command in its menu
    String getName();

    // Runs the use case, reading any input it needs from in
    void execute(Scanner in);
}
