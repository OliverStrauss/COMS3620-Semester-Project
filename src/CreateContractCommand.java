import java.time.LocalDate;
import java.util.Scanner;

public class CreateContractCommand implements Command {
    private final TalentController controller;

    public CreateContractCommand(TalentController controller) {
        this.controller = controller;
    }

    @Override
    public String getName() {
        return "Create Contract";
    }

    @Override
    public void execute(Scanner in) {
        int wrestlerId = Math.toIntExact(Department.askPositiveLong(in, "Wrestler ID: ", null));
        Wrestler wrestler = controller.getWrestler(wrestlerId);
        if (wrestler == null) {
            System.out.println("No wrestler with ID " + wrestlerId + ".");
            return;
        }
        System.out.println("Contract for: " + wrestler);

        long salary = Department.askPositiveLong(in, "Yearly salary: ", null);
        LocalDate start = Department.askDate(in, "Start date (YYYY-MM-DD, enter for today): ", LocalDate.now());
        LocalDate end = askEndDate(in, start);
        System.out.println(controller.createContract(wrestlerId, salary, start, end));
    }

    // Asks until the end date is after start. Renew Contract uses it too
    static LocalDate askEndDate(Scanner in, LocalDate start) {
        LocalDate end = Department.askDate(in, "End date (YYYY-MM-DD): ", null);
        while (!end.isAfter(start)) {
            System.out.println("End date must be after " + start + ".");
            end = Department.askDate(in, "End date (YYYY-MM-DD): ", null);
        }
        return end;
    }
}
