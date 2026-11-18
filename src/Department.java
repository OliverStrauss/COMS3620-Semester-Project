import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

// Base class for every department, and the Invoker in the Command pattern.
// Handles the menu loop; subclasses add one Command per use case.
public abstract class Department {
    private final String name;
    private final List<Command> commands = new ArrayList<>();

    protected Department(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }

    // Adds a use case to the menu. Call in menu order
    protected void addCommand(Command command) {
        commands.add(command);
    }

    // Shows each command's name as a numbered menu and runs the one picked, until 'back'
    public void open(Scanner in) {
        while (true) {
            System.out.println("\n" + name + ":");
            for (int i = 0; i < commands.size(); i++) {
                System.out.println("  " + (i + 1) + ". " + commands.get(i).getName());
            }
            System.out.print("Pick an action number (or 'back'): ");
            if (!in.hasNextLine()) return;
            String input = in.nextLine().trim();
            if (input.equalsIgnoreCase("back") || input.equalsIgnoreCase("b")) return;

            int choice = parseChoice(input, commands.size());
            if (choice < 0) {
                System.out.println("Invalid choice.");
                continue;
            }
            commands.get(choice).execute(in);
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

    // Asks until the user types a whole number above -1. Commas are allowed, like 2,500,000
    // Pressing enter gives defaultValue, unless it's null
    protected static long askNonNegativeLong(Scanner in, String prompt, Long defaultValue) {
        while (true) {
            System.out.print(prompt);
            String input = in.nextLine().trim().replace(",", "");
            if (input.isEmpty() && defaultValue != null) return defaultValue;
            try {
                long n = Long.parseLong(input);
                if (n > -1) return n;
            } catch (NumberFormatException e) {
                // fall through to the error message
            }
            System.out.println("Please enter a whole number above -1.");
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

    // Lists each option as a numbered choice and asks until the user picks one
    protected static <E> E askOption(Scanner in, String label, E[] options) {
        return options[askIndex(in, label, options)];
    }

    // Same as askOption, but returns the 0-based index of the pick. Handy for menus built from text
    protected static int askIndex(Scanner in, String label, Object[] options) {
        while (true) {
            System.out.println(label + ":");
            for (int i = 0; i < options.length; i++) {
                System.out.println("  " + (i + 1) + ". " + options[i]);
            }
            System.out.print("Pick a number: ");
            int choice = parseChoice(in.nextLine().trim(), options.length);
            if (choice >= 0) return choice;
            System.out.println("Invalid choice.");
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
