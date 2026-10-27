import java.io.IOException;
import java.time.LocalDate;
import java.util.List;
import java.util.Scanner;


public class TalentRelations extends Department {
   
    //Wrestler  = {id(int), legalName(String), ringName(String), brand(Enum), status(Enum)}

    public TalentRelations() {
      
        super("Talent Relations");
    }

    @Override
    protected List<String> getUseCases() {
        return List.of("Sign Wrestler",
        "Release Wrestler",
        "Promote to Main Roster",
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
            case 3 -> renewContract(in);
            case 4 -> createContract(in);
            case 5 -> viewContracts(in);
             
        }
    }

    private void signWrestler(Scanner in) {
        System.out.println("Sign Wrestler is not implemented yet.");


    }

    private void createContract(Scanner in) {
        // TODO: check the wrestler exists once Wrestlers.txt is set up
        int wrestlerId = Math.toIntExact(askPositiveLong(in, "Wrestler ID: ", null));
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



     private void viewContracts(Scanner in) {
        List<Contract> contracts = Contract.loadAll();
        if (contracts.isEmpty()) {
            System.out.println("No contracts yet.");
        }
        for (Contract c : contracts) {
            System.out.println(c);
        }
    }

    private void releaseWrestler(Scanner in) {
        System.out.println("Release Wrestler is not implemented yet.");
    }

    private void promoteToMainRoster(Scanner in) {
        System.out.println("Promote to Main Roster is not implemented yet.");
    }

}
