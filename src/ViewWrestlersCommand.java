import java.util.List;
import java.util.Scanner;

public class ViewWrestlersCommand implements Command {
    private final TalentController controller;

    public ViewWrestlersCommand(TalentController controller) {
        this.controller = controller;
    }

    @Override
    public String getName() {
        return "View Wrestlers";
    }

    @Override
    public void execute(Scanner in) {
        List<Wrestler> wrestlers = controller.getWrestlers();
        if (wrestlers.isEmpty()) {
            System.out.println("No wrestlers yet.");
        }
        for (Wrestler w : wrestlers) {
            System.out.println(w);
        }
    }
}
