import java.util.Scanner;

// Command for the "Create Contract" use case. Receiver: TalentDepartment
public class CreateContractCommand implements Command {
    private final TalentDepartment talent;

    public CreateContractCommand(TalentDepartment talent) {
        this.talent = talent;
    }

    @Override
    public String getName() {
        return "Create Contract";
    }

    @Override
    public void execute(Scanner in) {
        talent.createContract(in);
    }
}
