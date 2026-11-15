import java.util.Scanner;

// Sign Wrestler use case (Iteration 1)
public class SignWrestlerCommand implements Command {
    private final TalentController controller;

    public SignWrestlerCommand(TalentController controller) {
        this.controller = controller;
    }

    @Override
    public String getName() {
        return "Sign Wrestler";
    }

    @Override
    public void execute(Scanner in) {
        String legalName = Department.askText(in, "Enter wrestlers legal name ", "Jonny da");
        String stageName = Department.askText(in, "Enter wrestlers Stage name ", "Wrestler");
        Wrestler.Brand brand = Department.askOption(in, "Brand", Wrestler.Brand.values());
        System.out.println(controller.signWrestler(legalName, stageName, brand));
    }
}
