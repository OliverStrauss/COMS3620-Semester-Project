import java.util.List;
import java.util.Scanner;

// Base class for every department. Handles the menu loop;
// subclasses only list their use cases and say what each one does.
public abstract class Department {
    private final String name;

    protected Department(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }

    // Use case names, shown in menu order
    protected abstract List<String> getUseCases();

    // Run the use case at the given 0-based index
    protected abstract void runUseCase(int choice, Scanner in);

    public void open(Scanner in) {
        List<String> useCases = getUseCases();
        while (true) {
            System.out.println("\n" + name + ":");
            for (int i = 0; i < useCases.size(); i++) {
                System.out.println("  " + (i + 1) + ". " + useCases.get(i));
            }
            System.out.print("Pick an action number (or 'back'): ");
            if (!in.hasNextLine()) return;
            String input = in.nextLine().trim();
            if (input.equalsIgnoreCase("back") || input.equalsIgnoreCase("b")) return;

            int choice = parseChoice(input, useCases.size());
            if (choice < 0) {
                System.out.println("Invalid choice.");
                continue;
            }
            runUseCase(choice, in);
        }
    }

    // Returns 0-based index, or -1 if input isn't a number in 1..max
    public static int parseChoice(String input, int max) {
        try {
            int n = Integer.parseInt(input);
            return (n >= 1 && n <= max) ? n - 1 : -1;
        } catch (NumberFormatException e) {
            return -1;
        }
    }
}
