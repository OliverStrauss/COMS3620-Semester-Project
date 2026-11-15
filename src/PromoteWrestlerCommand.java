import java.util.List;
import java.util.Scanner;

// Promote to Main Roster: move an NXT wrestler to RAW or SMACKDOWN
public class PromoteWrestlerCommand implements Command {
    private final TalentController controller;

    public PromoteWrestlerCommand(TalentController controller) {
        this.controller = controller;
    }

    @Override
    public String getName() {
        return "Promote to Main Roster";
    }

    @Override
    public void execute(Scanner in) {
        List<Wrestler> nxt = controller.getNxtWrestlers();
        if (nxt.isEmpty()) {
            System.out.println("No NXT wrestlers to promote.");
            return;
        }
        for (Wrestler w : nxt) {
            System.out.println(w);
        }

        int wrestlerId = Math.toIntExact(Department.askPositiveLong(in, "Wrestler ID to promote: ", null));
        // Check before asking for a brand, so the user doesn't answer for nothing
        String problem = controller.promotionProblem(wrestlerId);
        if (problem != null) {
            System.out.println(problem);
            return;
        }

        Wrestler.Brand brand = Department.askOption(in, "Promote to",
                new Wrestler.Brand[]{Wrestler.Brand.RAW, Wrestler.Brand.SMACKDOWN});
        System.out.println(controller.promote(wrestlerId, brand));
    }
}
