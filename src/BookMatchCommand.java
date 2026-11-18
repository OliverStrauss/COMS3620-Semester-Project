import java.util.Scanner;

// Command for the "Book Match" use case. Receiver: CreativeDepartment
public class BookMatchCommand implements Command {
    private final CreativeDepartment creative;

    public BookMatchCommand(CreativeDepartment creative) {
        this.creative = creative;
    }

    @Override
    public String getName() {
        return "Book Match";
    }

    @Override
    public void execute(Scanner in) {
        creative.bookMatch(in);
    }
}
