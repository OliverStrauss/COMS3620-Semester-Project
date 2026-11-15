import java.util.List;
import java.util.Scanner;

public class ViewContractsCommand implements Command {
    private final TalentController controller;

    public ViewContractsCommand(TalentController controller) {
        this.controller = controller;
    }

    @Override
    public String getName() {
        return "View Contracts";
    }

    @Override
    public void execute(Scanner in) {
        List<Contract> contracts = controller.getContracts();
        if (contracts.isEmpty()) {
            System.out.println("No contracts yet.");
        }
        for (Contract c : contracts) {
            System.out.println(c);
        }
    }
}
