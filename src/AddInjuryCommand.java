import java.util.Scanner;

// Command for the "Add Injury" use case. Receiver: MedicalDepartment
public class AddInjuryCommand implements Command {
    private final MedicalDepartment medical;

    public AddInjuryCommand(MedicalDepartment medical) {
        this.medical = medical;
    }

    @Override
    public String getName() {
        return "Add Injury";
    }

    @Override
    public void execute(Scanner in) {
        medical.uiAddInjury(in);
    }
}
