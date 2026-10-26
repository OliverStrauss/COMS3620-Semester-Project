import java.util.List;
import java.util.Scanner;

public class TalentRelations extends Department {
    public TalentRelations() {
        super("Talent Relations");
    }

    @Override
    protected List<String> getUseCases() {
        return List.of("Sign Wrestler", "Renew Contract", "Release Wrestler", "Promote to Main Roster");
    }

    @Override
    protected void runUseCase(int choice, Scanner in) {
        switch (choice) {
            case 0 -> signWrestler(in);
            case 1 -> renewContract(in);
            case 2 -> releaseWrestler(in);
            case 3 -> promoteToMainRoster(in);
        }
    }

    private void signWrestler(Scanner in) {
        System.out.println("Sign Wrestler is not implemented yet.");
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
