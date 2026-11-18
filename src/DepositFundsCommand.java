import java.util.Scanner;

// Command for the "Deposit Funds" use case. Receiver: FinanceDepartment
public class DepositFundsCommand implements Command {
    private final FinanceDepartment finance;

    public DepositFundsCommand(FinanceDepartment finance) {
        this.finance = finance;
    }

    @Override
    public String getName() {
        return "Deposit Funds";
    }

    @Override
    public void execute(Scanner in) {
        finance.uiDeposit(in);
    }
}
