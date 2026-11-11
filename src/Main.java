import java.util.List;
import java.util.Scanner;

public class Main {
    // Add each new Department subclass here
    private static final List<Department> DEPARTMENTS = List.of(


            new TVShows(),

            new TalentDepartment(),
            new CreativeDepartment(),
            new Ticketing(),
            new LiveEvents(),
            MedicalDepartment.getInstance()
    );

    public static void main(String[] args) {
        Scanner in = new Scanner(System.in);
        clearScreen();
        System.out.println("Welcome to the WWE Management System!");
        printHelp();

        while (true) {
            System.out.print("\n> ");
            if (!in.hasNextLine()) break;
            String command = in.nextLine().trim().toLowerCase();

            switch (command) {
                case "departments", "d" -> departmentMenu(in);
                case "help", "h" -> printHelp();
                case "quit", "q", "exit" -> {
                    System.out.println("Goodbye!");
                    return;
                }
                case "" -> { }
                default -> System.out.println("Unknown command: " + command + " (type 'help')");
            }
        }
    }

    private static void clearScreen() {
        try {
            if (System.getProperty("os.name").contains("Windows")) {
                new ProcessBuilder("cmd", "/c", "cls").inheritIO().start().waitFor();
            } else {
                new ProcessBuilder("clear").inheritIO().start().waitFor();
            }
        } catch (Exception e) {
            // Fallback if the system commands fail
            System.out.print("\033[H\033[2J");
            System.out.flush();
        }
    }


    private static void printHelp() {
        System.out.println("Commands:");
        System.out.println("  departments (d) - list departments");
        System.out.println("  help (h)        - show this message");
        System.out.println("  quit (q)        - exit");
    }

    private static void departmentMenu(Scanner in) {
        while (true) {
            clearScreen();
            System.out.println("\nDepartments:");
            for (int i = 0; i < DEPARTMENTS.size(); i++) {
                System.out.println("  " + (i + 1) + ". " + DEPARTMENTS.get(i).getName());
            }
            System.out.print("Pick a department number (or 'back'): ");
            if (!in.hasNextLine()) return;
            String input = in.nextLine().trim();
            if (input.equalsIgnoreCase("back") || input.equalsIgnoreCase("b")) {
                clearScreen();
                printHelp(); 
                return;
            }

            int choice = Department.parseChoice(input, DEPARTMENTS.size());
            if (choice < 0) {
                System.out.println("Invalid choice.");
                continue;
            }
            clearScreen();
            DEPARTMENTS.get(choice).open(in);
        }
    }
}
