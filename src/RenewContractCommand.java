import java.util.Scanner;

// Command for the "Renew Contract" use case. Receiver: TalentDepartment
public class RenewContractCommand implements Command {
    private final TalentDepartment talent;

    public RenewContractCommand(TalentDepartment talent) {
        this.talent = talent;
    }

    @Override
    public String getName() {
        return "Renew Contract";
    }

    @Override
    public void execute(Scanner in) {
        talent.renewContract(in);
    }
}
