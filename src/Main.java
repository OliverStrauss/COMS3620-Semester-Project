import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Scanner;

public class Main {
    // Add each new Department subclass here
    private static final List<Department> DEPARTMENTS = List.of(


            new TVShows(),

            new TalentDepartment(),
            new CreativeDepartment(),
            new Ticketing(),
            new LiveEvents(),
            MedicalDepartment.getInstance(),
            FinanceDepartment.getInstance()
    );

    // Top-level commands, looked up by every name the user can type for them
    private static final Map<String, Command> COMMANDS = new HashMap<>();
    static {
        Command departments = new DepartmentsCommand();
        Command help = new HelpCommand();
        Command quit = new QuitCommand();
        COMMANDS.put("departments", departments);
        COMMANDS.put("d", departments);
        COMMANDS.put("help", help);
        COMMANDS.put("h", help);
        COMMANDS.put("quit", quit);
        COMMANDS.put("q", quit);
        COMMANDS.put("exit", quit);
    }

    // Set to false by QuitCommand to end the main loop
    private static boolean running = true;

    public static void main(String[] args) {
        Scanner in = new Scanner(System.in);
        clearScreen();
        System.out.println("Welcome to the WWE Management System!");
        printHelp();

        while (running) {
            System.out.print("\n> ");
            if (!in.hasNextLine()) break;
            String input = in.nextLine().trim().toLowerCase();
            if (input.isEmpty()) continue;

            Command command = COMMANDS.get(input);
            if (command == null) {
                System.out.println("Unknown command: " + input + " (type 'help')");
            } else {
                command.execute(in);
            }
        }
    }

    // Package-private so QuitCommand can end the main loop
    static void stop() {
        running = false;
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


    // Package-private so HelpCommand can run it
    static void printHelp() {
        System.out.println("Commands:");
        System.out.println("  departments (d) - list departments");
        System.out.println("  help (h)        - show this message");
        System.out.println("  quit (q)        - exit");
    }

    // Package-private so DepartmentsCommand can run it
    static void departmentMenu(Scanner in) {
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
