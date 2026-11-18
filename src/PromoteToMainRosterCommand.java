import java.util.Scanner;

// Command for the "Promote to Main Roster" use case. Receiver: TalentDepartment
public class PromoteToMainRosterCommand implements Command {
    private final TalentDepartment talent;

    public PromoteToMainRosterCommand(TalentDepartment talent) {
        this.talent = talent;
    }

    @Override
    public String getName() {
        return "Promote to Main Roster";
    }

    @Override
    public void execute(Scanner in) {
        talent.promoteToMainRoster(in);
    }
}
