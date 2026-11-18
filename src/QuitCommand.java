import java.util.Scanner;

// Top-level command for "quit" / "q" / "exit": says goodbye and ends the main loop. Receiver: Main
public class QuitCommand implements Command {

    @Override
    public String getName() {
        return "quit";
    }

    @Override
    public void execute(Scanner in) {
        System.out.println("Goodbye!");
        Main.stop();
    }
}
