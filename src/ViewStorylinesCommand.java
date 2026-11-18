import java.util.Scanner;

// Command for the "View Storylines" use case. Receiver: CreativeDepartment
public class ViewStorylinesCommand implements Command {
    private final CreativeDepartment creative;

    public ViewStorylinesCommand(CreativeDepartment creative) {
        this.creative = creative;
    }

    @Override
    public String getName() {
        return "View Storylines";
    }

    @Override
    public void execute(Scanner in) {
        creative.viewStorylines();
    }
}
