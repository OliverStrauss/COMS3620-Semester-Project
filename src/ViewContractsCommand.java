import java.util.Scanner;

// Command for the "View Contracts" use case. Receiver: TalentDepartment
public class ViewContractsCommand implements Command {
    private final TalentDepartment talent;

    public ViewContractsCommand(TalentDepartment talent) {
        this.talent = talent;
    }

    @Override
    public String getName() {
        return "View Contracts";
    }

    @Override
    public void execute(Scanner in) {
        talent.viewContracts(in);
    }
}
