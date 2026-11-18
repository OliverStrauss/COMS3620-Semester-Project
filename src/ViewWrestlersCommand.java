import java.util.Scanner;

// Command for the "View Wrestlers" use case. Receiver: TalentDepartment
public class ViewWrestlersCommand implements Command {
    private final TalentDepartment talent;

    public ViewWrestlersCommand(TalentDepartment talent) {
        this.talent = talent;
    }

    @Override
    public String getName() {
        return "View Wrestlers";
    }

    @Override
    public void execute(Scanner in) {
        talent.viewWrestlers(in);
    }
}
