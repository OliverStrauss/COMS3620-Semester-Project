import java.time.LocalDate;
import java.util.List;
import java.util.Scanner;


// UI for Talent Relations: asks for input and prints results. TalentController does the work.
public class TalentDepartment extends Department {
    private final TalentController controller = new TalentController();

    //Wrestler  = {id(int), legalName(String), ringName(String), brand(Enum), status(Enum)}

    public TalentDepartment() {

        super("Talent Relations");
    }

    /**
     * Wrestler Managment
     *  Sign Wrestler
     *  Promote Wrestler
     *  Release Wrestler
     *  Change Brand
     *  View
     * Contract Managemnt
     *  Create Contract
     *  Renew Contract
     *  View Contracts
     */

    @Override
    protected List<String> getUseCases() {
        return List.of("Sign Wrestler",
        "Release Wrestler",
        "Promote to Main Roster",
        "View Wrestlers",
        "Renew Contract",
        "Create Contract",
        "View Contracts"
        );
    }

    @Override
    protected void runUseCase(int choice, Scanner in) {
        switch (choice) {
            case 0 -> signWrestler(in);
            case 1 -> releaseWrestler(in);
            case 2 -> promoteToMainRoster(in);
            case 3 -> viewWrestlers(in);
            case 4 -> renewContract(in);
            case 5 -> createContract(in);
            case 6 -> viewContracts(in);

        }
    }

    /**
     * String legalName, String ringName, Brand brand, Status status) {
     */

    private void signWrestler(Scanner in) {
        String legalName = askText(in,"Enter wrestlers legal name ","Jonny da");
        String stageName = askText(in,"Enter wrestlers Stage name ","Wrestler");
        Wrestler.Brand brand = askBrand(in);
        System.out.println(controller.signWrestler(legalName, stageName, brand));
    }

    private void createContract(Scanner in) {
        int wrestlerId = Math.toIntExact(askPositiveLong(in, "Wrestler ID: ", null));
        Wrestler wrestler = controller.getWrestler(wrestlerId);
        if (wrestler == null) {
            System.out.println("No wrestler with ID " + wrestlerId + ".");
            return;
        }
        System.out.println("Contract for: " + wrestler);

        long salary = askPositiveLong(in, "Yearly salary: ", null);
        LocalDate start = askDate(in, "Start date (YYYY-MM-DD, enter for today): ", LocalDate.now());
        LocalDate end = askEndDate(in, start);
        System.out.println(controller.createContract(wrestlerId, salary, start, end));
    }

    private void renewContract(Scanner in) {
        System.out.println("Choose ID of contract you want to renew");
        int contractChoice = Math.toIntExact(askPositiveLong(in, "Contract ID: ", null));

        Contract chosenContract = controller.getContract(contractChoice);
        if (chosenContract == null) {
            System.out.println("No contract with ID " + contractChoice + ".");
            return;
        }
        //Found contract now promt user to update it
        System.out.println("Current: " + chosenContract);

        long salary = askPositiveLong(in,
                "New yearly salary (enter to keep " + chosenContract.getYearlySalary() + "): ",
                chosenContract.getYearlySalary());
        LocalDate start = askDate(in,
                "New start date (YYYY-MM-DD, enter for " + chosenContract.getEndDate() + "): ",
                chosenContract.getEndDate());
        LocalDate end = askEndDate(in, start);
        System.out.println(controller.renewContract(contractChoice, salary, start, end));
    }

    // Asks until the end date is after start
    private LocalDate askEndDate(Scanner in, LocalDate start) {
        LocalDate end = askDate(in, "End date (YYYY-MM-DD): ", null);
        while (!end.isAfter(start)) {
            System.out.println("End date must be after " + start + ".");
            end = askDate(in, "End date (YYYY-MM-DD): ", null);
        }
        return end;
    }

    private void viewContracts(Scanner in) {
        List<Contract> contracts = controller.getContracts();
        if (contracts.isEmpty()) {
            System.out.println("No contracts yet.");
        }
        for (Contract c : contracts) {
            System.out.println(c);
        }
    }

    private void viewWrestlers(Scanner in) {
        List<Wrestler> wrestlers = controller.getWrestlers();
        if (wrestlers.isEmpty()) {
            System.out.println("No wrestlers yet.");
        }
        for (Wrestler w : wrestlers) {
            System.out.println(w);
        }
    }

    private void releaseWrestler(Scanner in) {
        if (controller.getWrestlers().isEmpty()) {
            System.out.println("No wrestlers to release.");
            return;
        }

        int wrestlerId = Math.toIntExact(askPositiveLong(in, "Wrestler ID to release: ", null));
        if (controller.getWrestler(wrestlerId) == null) {
            System.out.println("No wrestler with ID " + wrestlerId + ".");
            return;
        }

        String choice = askOption(in, "Release",
                new String[]{"Wrestler only", "Contract only", "Both"});
        if (!choice.equals("Contract only")) {
            System.out.println(controller.releaseWrestler(wrestlerId));
        }
        if (!choice.equals("Wrestler only")) {
            System.out.println(controller.endContracts(wrestlerId));
        }
    }

    private void promoteToMainRoster(Scanner in) {
        List<Wrestler> nxt = controller.getNxtWrestlers();
        if (nxt.isEmpty()) {
            System.out.println("No NXT wrestlers to promote.");
            return;
        }
        for (Wrestler w : nxt) {
            System.out.println(w);
        }

        int wrestlerId = Math.toIntExact(askPositiveLong(in, "Wrestler ID to promote: ", null));
        // Check before asking for a brand, so the user doesn't answer for nothing
        String problem = controller.promotionProblem(wrestlerId);
        if (problem != null) {
            System.out.println(problem);
            return;
        }

        Wrestler.Brand brand = askOption(in, "Promote to",
                new Wrestler.Brand[]{Wrestler.Brand.RAW, Wrestler.Brand.SMACKDOWN});
        System.out.println(controller.promote(wrestlerId, brand));
    }

    // Lists every brand as a numbered choice and asks until the user picks one
    private Wrestler.Brand askBrand(Scanner in) {
        return askOption(in, "Brand", Wrestler.Brand.values());
    }

    // Lists every status as a numbered choice and asks until the user picks one
    private Wrestler.Status askStatus(Scanner in) {
        return askOption(in, "Status", Wrestler.Status.values());
    }

}
