import java.util.Scanner;

// Command for the "View All Transactions" use case. Receiver: FinanceDepartment
public class ViewAllTransactionsCommand implements Command {
    private final FinanceDepartment finance;

    public ViewAllTransactionsCommand(FinanceDepartment finance) {
        this.finance = finance;
    }

    @Override
    public String getName() {
        return "View All Transactions";
    }

    @Override
    public void execute(Scanner in) {
        finance.uiViewAllTransactions();
    }
}
