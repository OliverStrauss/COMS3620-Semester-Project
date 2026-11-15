import java.util.Scanner;

// Release Wrestler: release the wrestler, end their contracts, or both
public class ReleaseWrestlerCommand implements Command {
    private final TalentController controller;

    public ReleaseWrestlerCommand(TalentController controller) {
        this.controller = controller;
    }

    @Override
    public String getName() {
        return "Release Wrestler";
    }

    @Override
    public void execute(Scanner in) {
        if (controller.getWrestlers().isEmpty()) {
            System.out.println("No wrestlers to release.");
            return;
        }

        int wrestlerId = Math.toIntExact(Department.askPositiveLong(in, "Wrestler ID to release: ", null));
        if (controller.getWrestler(wrestlerId) == null) {
            System.out.println("No wrestler with ID " + wrestlerId + ".");
            return;
        }

        String choice = Department.askOption(in, "Release",
                new String[]{"Wrestler only", "Contract only", "Both"});
        if (!choice.equals("Contract only")) {
            System.out.println(controller.releaseWrestler(wrestlerId));
        }
        if (!choice.equals("Wrestler only")) {
            System.out.println(controller.endContracts(wrestlerId));
        }
    }
}
