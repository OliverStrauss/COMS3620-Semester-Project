import java.io.IOException;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

public class Ticketing extends Department {

    public Ticketing() {
        super("Ticketing");
    }

    @Override
    protected List<String> getUseCases() {
        return List.of("Sell Ticket",
                "View Tickets");
    }

    @Override
    protected void runUseCase(int choice, Scanner in) {
        switch (choice) {
            case 0 -> sellTicket(in);
            case 1 -> viewTickets(in);
        }
    }

    private void sellTicket(Scanner in) {
        String eventName = askText(in, "Event name: ", null);

        // Only sections with seats left get offered
        List<Ticket.Section> openSections = new ArrayList<>();
        for (Ticket.Section s : Ticket.Section.values()) {
            if (Ticket.seatsLeft(eventName, s) > 0) openSections.add(s);
        }
        if (openSections.isEmpty()) {
            System.out.println(eventName + " is sold out.");
            return;
        }

        Ticket.Section section = askSection(in, eventName, openSections);
        String buyerName = askText(in, "Buyer name: ", null);

        String confirm = askText(in, "Sell " + section + " ticket to " + buyerName
                + " for $" + section.getPrice() + "? (y/n): ", null);
        if (!confirm.equalsIgnoreCase("y")) {
            System.out.println("Sale cancelled.");
            return;
        }
        saveNewTicket(eventName, buyerName, section);
    }

    private void saveNewTicket(String eventName, String buyerName, Ticket.Section section) {
        // Get the ID last, so bad input never uses one up
        Ticket ticket = new Ticket(IdGenerator.nextId(Ticket.FILE), eventName, buyerName, section,
                section.getPrice(), LocalDate.now());
        try {
            CsvWriter.appendRow(Ticket.FILE, ticket.toRow());
            System.out.println("Sold " + ticket);
        } catch (IOException e) {
            System.out.println("Could not save ticket: " + e.getMessage());
        }
    }

    private void viewTickets(Scanner in) {
        List<Ticket> tickets = Ticket.loadAll();
        if (tickets.isEmpty()) {
            System.out.println("No tickets yet.");
        }
        for (Ticket t : tickets) {
            System.out.println(t);
        }
    }

    // Lists each open section as a numbered choice and asks until the user picks one
    private Ticket.Section askSection(Scanner in, String eventName, List<Ticket.Section> sections) {
        while (true) {
            System.out.println("Section:");
            for (int i = 0; i < sections.size(); i++) {
                Ticket.Section s = sections.get(i);
                System.out.println("  " + (i + 1) + ". " + s + " | $" + s.getPrice()
                        + " | Seats left: " + Ticket.seatsLeft(eventName, s));
            }
            System.out.print("Pick a number: ");
            int choice = parseChoice(in.nextLine().trim(), sections.size());
            if (choice >= 0) return sections.get(choice);
            System.out.println("Invalid choice.");
        }
    }
}