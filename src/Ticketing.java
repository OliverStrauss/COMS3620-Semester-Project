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
                "View Tickets",
                "Set Event Price",
                "Refund Ticket");
    }

    @Override
    protected void runUseCase(int choice, Scanner in) {
        switch (choice) {
            case 0 -> sellTicket(in);
            case 1 -> viewTickets(in);
            case 2 -> setEventPrice(in);
            case 3 -> refundTicket(in);
        }
    }

    private void sellTicket(Scanner in) {
        // Only scheduled, upcoming events can sell tickets
        List<Event> onSale = new ArrayList<>();
        for (Event e : Event.loadAll()) {
            if (e.getStatus() == Event.Status.SCHEDULED && e.isUpcoming()) onSale.add(e);
        }
        if (onSale.isEmpty()) {
            System.out.println("No scheduled upcoming events.");
            return;
        }
        Event event = askOption(in, "Event", onSale.toArray(new Event[0]));

        // Only sections with seats left get offered
        List<Ticket.Section> openSections = new ArrayList<>();
        for (Ticket.Section s : Ticket.Section.values()) {
            if (Ticket.seatsLeft(event, s) > 0) openSections.add(s);
        }
        if (openSections.isEmpty()) {
            System.out.println(event.getName() + " is sold out.");
            return;
        }

        Ticket.Section section = askSection(in, event, openSections);
        String buyerName = askText(in, "Buyer name: ", null);
        long price = Ticket.priceFor(event, section);

        String confirm = askText(in, "Sell " + section + " ticket to " + buyerName
                + " for $" + price + "? (y/n): ", null);
        if (!confirm.equalsIgnoreCase("y")) {
            System.out.println("Sale cancelled.");
            return;
        }
        saveNewTicket(event.getId(), buyerName, section, price);
    }

    private void saveNewTicket(int eventId, String buyerName, Ticket.Section section, long price) {
        // Get the ID last, so bad input never uses one up
        Ticket ticket = new Ticket(IdGenerator.nextId(Ticket.FILE), eventId, buyerName, section,
                price, LocalDate.now(), Ticket.Status.SOLD);
        try {
            CsvWriter.appendRow(Ticket.FILE, ticket.toRow());
            System.out.println("Sold " + ticket);
        } catch (IOException e) {
            System.out.println("Could not save ticket: " + e.getMessage());
        }
    }

    // Sets a custom price for one section of one event. Tickets already sold keep their old price
    private void setEventPrice(Scanner in) {
        List<Event> onSale = new ArrayList<>();
        for (Event e : Event.loadAll()) {
            if (e.getStatus() == Event.Status.SCHEDULED && e.isUpcoming()) onSale.add(e);
        }
        if (onSale.isEmpty()) {
            System.out.println("No scheduled upcoming events.");
            return;
        }
        Event event = askOption(in, "Event", onSale.toArray(new Event[0]));
        Ticket.Section section = askOption(in, "Section", Ticket.Section.values());

        long current = Ticket.priceFor(event, section);
        long newPrice = askPositiveLong(in, "New price (enter to keep $" + current + "): ", current);
        if (newPrice == current) {
            System.out.println("Price unchanged.");
            return;
        }

        // Update this event's price if it already has one, otherwise add it
        List<EventPrice> prices = EventPrice.loadAll();
        EventPrice existing = null;
        for (EventPrice p : prices) {
            if (p.getEventId() == event.getId() && p.getSection() == section) existing = p;
        }
        if (existing != null) {
            existing.setPrice(newPrice);
        } else {
            prices.add(new EventPrice(event.getId(), section, newPrice));
        }
        try {
            EventPrice.saveAll(prices);
            System.out.println(section + " for " + event.getName() + " changed from $" + current
                    + " to $" + newPrice + ".");
        } catch (IOException e) {
            System.out.println("Could not save price: " + e.getMessage());
        }
    }

    // Refunds the price the buyer paid and frees the seat. Only for events that haven't happened yet
    private void refundTicket(Scanner in) {
        int ticketId = Math.toIntExact(askPositiveLong(in, "Ticket ID: ", null));
        // Edit the ticket from this list, so saveAll writes the change back
        List<Ticket> tickets = Ticket.loadAll();
        Ticket ticket = null;
        for (Ticket t : tickets) {
            if (t.getId() == ticketId) ticket = t;
        }
        if (ticket == null) {
            System.out.println("No ticket with ID " + ticketId + ".");
            return;
        }
        System.out.println("Current: " + ticket);
        if (ticket.isRefunded()) {
            System.out.println("Ticket #" + ticketId + " was already refunded.");
            return;
        }

        Event event = Event.findById(ticket.getEventId());
        if (event == null) {
            System.out.println("No event with ID " + ticket.getEventId() + ", so the ticket can't be refunded.");
            return;
        }
        if (!event.isUpcoming()) {
            System.out.println(event.getName() + " was on " + event.getDate() + ". Only tickets for upcoming events can be refunded.");
            return;
        }

        String confirm = askText(in, "Refund $" + ticket.getPrice() + " to " + ticket.getBuyerName()
                + " for " + event.getName() + "? (y/n): ", null);
        if (!confirm.equalsIgnoreCase("y")) {
            System.out.println("Refund cancelled.");
            return;
        }
        ticket.refund();
        try {
            Ticket.saveAll(tickets);
            System.out.println("Refunded $" + ticket.getPrice() + " to " + ticket.getBuyerName()
                    + ". The " + ticket.getSection() + " seat is open again.");
        } catch (IOException e) {
            System.out.println("Could not save refund: " + e.getMessage());
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
    private Ticket.Section askSection(Scanner in, Event event, List<Ticket.Section> sections) {
        while (true) {
            System.out.println("Section:");
            for (int i = 0; i < sections.size(); i++) {
                Ticket.Section s = sections.get(i);
                System.out.println("  " + (i + 1) + ". " + s + " | $" + Ticket.priceFor(event, s)
                        + " | Seats left: " + Ticket.seatsLeft(event, s));
            }
            System.out.print("Pick a number: ");
            int choice = parseChoice(in.nextLine().trim(), sections.size());
            if (choice >= 0) return sections.get(choice);
            System.out.println("Invalid choice.");
        }
    }
}