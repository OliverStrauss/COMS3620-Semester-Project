import java.time.LocalDate;
import java.util.Scanner;

// Renew Contract: keeps the old contract as history and adds a new one after it
public class RenewContractCommand implements Command {
    private final TalentController controller;

    public RenewContractCommand(TalentController controller) {
        this.controller = controller;
    }

    @Override
    public String getName() {
        return "Renew Contract";
    }

    @Override
    public void execute(Scanner in) {
        System.out.println("Choose ID of contract you want to renew");
        int contractChoice = Math.toIntExact(Department.askPositiveLong(in, "Contract ID: ", null));

        Contract chosenContract = controller.getContract(contractChoice);
        if (chosenContract == null) {
            System.out.println("No contract with ID " + contractChoice + ".");
            return;
        }
        System.out.println("Current: " + chosenContract);

        long salary = Department.askPositiveLong(in,
                "New yearly salary (enter to keep " + chosenContract.getYearlySalary() + "): ",
                chosenContract.getYearlySalary());
        LocalDate start = Department.askDate(in,
                "New start date (YYYY-MM-DD, enter for " + chosenContract.getEndDate() + "): ",
                chosenContract.getEndDate());
        LocalDate end = CreateContractCommand.askEndDate(in, start);
        System.out.println(controller.renewContract(contractChoice, salary, start, end));
    }
}
