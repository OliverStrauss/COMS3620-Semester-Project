import java.util.Scanner;

// Command for the "View Transaction by ID" use case. Receiver: FinanceDepartment
public class ViewTransactionByIdCommand implements Command {
    private final FinanceDepartment finance;

    public ViewTransactionByIdCommand(FinanceDepartment finance) {
        this.finance = finance;
    }

    @Override
    public String getName() {
        return "View Transaction by ID";
    }

    @Override
    public void execute(Scanner in) {
        finance.uiViewTransactionById(in);
    }
}
