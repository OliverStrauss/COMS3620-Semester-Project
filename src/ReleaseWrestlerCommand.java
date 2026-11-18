import java.util.Scanner;

// Command for the "Release Wrestler" use case. Receiver: TalentDepartment
public class ReleaseWrestlerCommand implements Command {
    private final TalentDepartment talent;

    public ReleaseWrestlerCommand(TalentDepartment talent) {
        this.talent = talent;
    }

    @Override
    public String getName() {
        return "Release Wrestler";
    }

    @Override
    public void execute(Scanner in) {
        talent.releaseWrestler(in);
    }
}
