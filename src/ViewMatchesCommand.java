import java.util.Scanner;

// Command for the "View Matches" use case. Receiver: CreativeDepartment
public class ViewMatchesCommand implements Command {
    private final CreativeDepartment creative;

    public ViewMatchesCommand(CreativeDepartment creative) {
        this.creative = creative;
    }

    @Override
    public String getName() {
        return "View Matches";
    }

    @Override
    public void execute(Scanner in) {
        creative.viewMatches();
    }
}
