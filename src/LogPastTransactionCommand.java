import java.util.Scanner;

// Command for the "Log Past Transaction" use case. Receiver: FinanceDepartment
public class LogPastTransactionCommand implements Command {
    private final FinanceDepartment finance;

    public LogPastTransactionCommand(FinanceDepartment finance) {
        this.finance = finance;
    }

    @Override
    public String getName() {
        return "Log Past Transaction";
    }

    @Override
    public void execute(Scanner in) {
        finance.uiLogPastTransaction(in);
    }
}
