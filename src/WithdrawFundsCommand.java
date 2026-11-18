import java.util.Scanner;

// Command for the "Withdraw Funds" use case. Receiver: FinanceDepartment
public class WithdrawFundsCommand implements Command {
    private final FinanceDepartment finance;

    public WithdrawFundsCommand(FinanceDepartment finance) {
        this.finance = finance;
    }

    @Override
    public String getName() {
        return "Withdraw Funds";
    }

    @Override
    public void execute(Scanner in) {
        finance.uiWithdraw(in);
    }
}
