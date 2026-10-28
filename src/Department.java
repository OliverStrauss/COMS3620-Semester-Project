import java.time.LocalDate;
import java.time.format.DateTimeParseException;
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

    // Asks until the user types a whole number above 0. Commas are allowed, like 2,500,000
    // Pressing enter gives defaultValue, unless it's null
    protected static long askPositiveLong(Scanner in, String prompt, Long defaultValue) {
        while (true) {
            System.out.print(prompt);
            String input = in.nextLine().trim().replace(",", "");
            if (input.isEmpty() && defaultValue != null) return defaultValue;
            try {
                long n = Long.parseLong(input);
                if (n > 0) return n;
            } catch (NumberFormatException e) {
                // fall through to the error message
            }
            System.out.println("Please enter a whole number above 0.");
        }
    }

    // Asks until the user types something that isn't blank. Returns it trimmed
    // Pressing enter gives defaultValue, unless it's null
    protected static String askText(Scanner in, String prompt, String defaultValue) {
        while (true) {
            System.out.print(prompt);
            String input = in.nextLine().trim();
            if (!input.isEmpty()) return input;
            if (defaultValue != null) return defaultValue;
            System.out.println("Please enter something.");
        }
    }

    // Asks until the user types a date as YYYY-MM-DD. Pressing enter gives defaultDate, unless it's null
    protected static LocalDate askDate(Scanner in, String prompt, LocalDate defaultDate) {
        while (true) {
            System.out.print(prompt);
            String input = in.nextLine().trim();
            if (input.isEmpty() && defaultDate != null) return defaultDate;
            try {
                return LocalDate.parse(input);
            } catch (DateTimeParseException e) {
                System.out.println("Please enter a date as YYYY-MM-DD.");
            }
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
