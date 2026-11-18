import java.util.Scanner;

// Top-level command for "departments" / "d": opens the department menu. Receiver: Main
public class DepartmentsCommand implements Command {

    @Override
    public String getName() {
        return "departments";
    }

    @Override
    public void execute(Scanner in) {
        Main.departmentMenu(in);
    }
}
