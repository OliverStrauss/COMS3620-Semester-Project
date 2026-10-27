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
        "Renew Contract",
        "Release Wrestler",
        "Promote to Main Roster",
        "Create Contract"
        );
    }

    @Override
    protected void runUseCase(int choice, Scanner in) {
        switch (choice) {
            case 0 -> signWrestler(in);
            case 1 -> renewContract(in);
            case 2 -> releaseWrestler(in);
            case 3 -> promoteToMainRoster(in);
            case 4 -> createContract(in);
        }
    }

    private void signWrestler(Scanner in) {
        System.out.println("Sign Wrestler is not implemented yet.");


    }

    private void createContract(Scanner in) {
        // TODO: replace with user input and next ID from file
        Contract contract = new Contract(1, 1, 2500000, LocalDate.of(2026, 11, 1), LocalDate.of(2029, 8, 15));
        try {
            CsvWriter.appendRow(Contract.FILE, contract.toRow());
            System.out.println("Created contract: " + CsvWriter.toCsvLine(contract.toRow()));
        } catch (IOException e) {
            System.out.println("Could not save contract: " + e.getMessage());
        }
    }

    private void renewContract(Scanner in) {
        System.out.println("Renew Contract is not implemented yet.");
    }

    private void releaseWrestler(Scanner in) {
        System.out.println("Release Wrestler is not implemented yet.");
    }

    private void promoteToMainRoster(Scanner in) {
        System.out.println("Promote to Main Roster is not implemented yet.");
    }

}
