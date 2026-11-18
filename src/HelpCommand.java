import java.util.Scanner;

// Top-level command for "help" / "h": prints the command list. Receiver: Main
public class HelpCommand implements Command {

    @Override
    public String getName() {
        return "help";
    }

    @Override
    public void execute(Scanner in) {
        Main.printHelp();
    }
}
