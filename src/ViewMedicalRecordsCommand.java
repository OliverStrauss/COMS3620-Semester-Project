import java.util.Scanner;

// Command for the "View Medical Records" use case. Receiver: MedicalDepartment
public class ViewMedicalRecordsCommand implements Command {
    private final MedicalDepartment medical;

    public ViewMedicalRecordsCommand(MedicalDepartment medical) {
        this.medical = medical;
    }

    @Override
    public String getName() {
        return "View Medical Records";
    }

    @Override
    public void execute(Scanner in) {
        medical.uiViewMedicalRecords(in);
    }
}
