import java.util.Scanner;

// Command for the "Clear Injury" use case. Receiver: MedicalDepartment
public class ClearInjuryCommand implements Command {
    private final MedicalDepartment medical;

    public ClearInjuryCommand(MedicalDepartment medical) {
        this.medical = medical;
    }

    @Override
    public String getName() {
        return "Clear Injury";
    }

    @Override
    public void execute(Scanner in) {
        medical.uiClearInjury(in);
    }
}
