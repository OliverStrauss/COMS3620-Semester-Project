import java.io.IOException;
import java.time.LocalDate;
import java.util.List;
import java.util.Scanner;


public class TalentRelations extends Department {
   
    //Wrestler  = {id(int), legalName(String), ringName(String), brand(Enum), status(Enum)}

    public TalentRelations() {
      
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
        Wrestler.Status status = Wrestler.Status.ACTIVE;
        saveNewWrestler(legalName,stageName,brand,status);




    }

    private void createContract(Scanner in) {
        int wrestlerId = Math.toIntExact(askPositiveLong(in, "Wrestler ID: ", null));
        Wrestler wrestler = Wrestler.findById(wrestlerId);
        if (wrestler == null) {
            System.out.println("No wrestler with ID " + wrestlerId + ".");
            return;
        }
        System.out.println("Contract for: " + wrestler);

        long salary = askPositiveLong(in, "Yearly salary: ", null);
        LocalDate start = askDate(in, "Start date (YYYY-MM-DD, enter for today): ", LocalDate.now());
        LocalDate end = askEndDate(in, start);
        saveNewContract(wrestlerId, salary, start, end, "Created");
    }

    // Renewing keeps the old contract as history and adds a new one after it
    private void renewContract(Scanner in) {
        System.out.println("Choose ID of contract you want to renew");
        int contractChoice = Math.toIntExact(askPositiveLong(in, "Contract ID: ", null));

        Contract chosenContract = Contract.findById(contractChoice);
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
        saveNewContract(chosenContract.getWrestlerId(), salary, start, end, "Renewed as");
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

    private void saveNewContract(int wrestlerId, long salary, LocalDate start, LocalDate end, String verb) {
        // Get the ID last, so bad input never uses one up
        Contract contract = new Contract(IdGenerator.nextId(Contract.FILE), wrestlerId, salary, start, end);
        try {
            CsvWriter.appendRow(Contract.FILE, contract.toRow());
            System.out.println(verb + " " + contract);
        } catch (IOException e) {
            System.out.println("Could not save contract: " + e.getMessage());
        }
    }

    private void saveNewWrestler(String legalName, String stageName, Wrestler.Brand brand, Wrestler.Status status) {
    // Get the ID last, so bad input never uses one up
    Wrestler wrestler = new Wrestler(IdGenerator.nextId(Wrestler.FILE), legalName, stageName, brand, status);
    try {
        CsvWriter.appendRow(Wrestler.FILE, wrestler.toRow());
        System.out.println("Created" + wrestler);
    } catch (IOException e) {
        System.out.println("Could not save wrestler: " + e.getMessage());
    }
    }



    private void viewContracts(Scanner in) {
        List<Contract> contracts = Contract.loadAll();
        if (contracts.isEmpty()) {
            System.out.println("No contracts yet.");
        }
        for (Contract c : contracts) {
            System.out.println(c);
        }
    }

    private void viewWrestlers(Scanner in) {
        List<Wrestler> wrestlers = Wrestler.loadAll();
        if (wrestlers.isEmpty()) {
            System.out.println("No contracts yet.");
        }
        for (Wrestler w : wrestlers) {
            System.out.println(w);
        }
    }

    private void releaseWrestler(Scanner in) {
        System.out.println("Release Wrestler is not implemented yet.");
    }

    // Moves an NXT wrestler up to a main roster brand
    private void promoteToMainRoster(Scanner in) {
        List<Wrestler> wrestlers = Wrestler.loadAll();
        boolean anyNxt = false;
        for (Wrestler w : wrestlers) {
            if (!w.getBrand().isMainRoster()) {
                System.out.println(w);
                anyNxt = true;
            }
        }
        if (!anyNxt) {
            System.out.println("No NXT wrestlers to promote.");
            return;
        }

        int wrestlerId = Math.toIntExact(askPositiveLong(in, "Wrestler ID to promote: ", null));
        // Edit the wrestler from this list, so saveAll writes the change back
        Wrestler wrestler = null;
        for (Wrestler w : wrestlers) {
            if (w.getId() == wrestlerId) wrestler = w;
        }
        if (wrestler == null) {
            System.out.println("No wrestler with ID " + wrestlerId + ".");
            return;
        }
        if (wrestler.getBrand().isMainRoster()) {
            System.out.println(wrestler.getRingName() + " is already on the main roster.");
            return;
        }
        if (wrestler.getStatus() != Wrestler.Status.ACTIVE) {
            System.out.println(wrestler.getRingName() + " is " + wrestler.getStatus() + " and can't be promoted.");
            return;
        }
        if (!Contract.hasActiveContract(wrestlerId, LocalDate.now())) {
            System.out.println(wrestler.getRingName() + " needs an active contract to be promoted.");
            return;
        }

        Wrestler.Brand brand = askOption(in, "Promote to",
                new Wrestler.Brand[]{Wrestler.Brand.RAW, Wrestler.Brand.SMACKDOWN});
        wrestler.setBrand(brand);
        try {
            Wrestler.saveAll(wrestlers);
            System.out.println("Promoted " + wrestler.getRingName() + " to " + brand + ".");
        } catch (IOException e) {
            System.out.println("Could not save wrestler: " + e.getMessage());
        }
    }

    // Lists every brand as a numbered choice and asks until the user picks one
    private Wrestler.Brand askBrand(Scanner in) {
        return askOption(in, "Brand", Wrestler.Brand.values());
    }

    // Lists every status as a numbered choice and asks until the user picks one
    private Wrestler.Status askStatus(Scanner in) {
        return askOption(in, "Status", Wrestler.Status.values());
    }

    // Shared by askBrand and askStatus: works for any enum's values()
    private <E> E askOption(Scanner in, String label, E[] options) {
        while (true) {
            System.out.println(label + ":");
            for (int i = 0; i < options.length; i++) {
                System.out.println("  " + (i + 1) + ". " + options[i]);
            }
            System.out.print("Pick a number: ");
            int choice = parseChoice(in.nextLine().trim(), options.length);
            if (choice >= 0) return options[choice];
            System.out.println("Invalid choice.");
        }
    }

}
