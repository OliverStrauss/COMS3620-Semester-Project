import java.util.Scanner;

// Command for the "Sign Wrestler" use case. Receiver: TalentDepartment
public class SignWrestlerCommand implements Command {
    private final TalentDepartment talent;

    public SignWrestlerCommand(TalentDepartment talent) {
        this.talent = talent;
    }

    @Override
    public String getName() {
        return "Sign Wrestler";
    }

    @Override
    public void execute(Scanner in) {
        talent.signWrestler(in);
    }
}
