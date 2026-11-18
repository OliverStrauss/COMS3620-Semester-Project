// @author Caleb Sanchez

import java.io.IOException;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

public class LiveEvents extends Department {

    public LiveEvents() {
        super("Live Events");
    }

    @Override
    protected List<String> getUseCases() {
        return List.of("Create Live Event", "Review Pending Events (Finance)", "View Events", "Resume Draft", "Settle Event");
    }

    @Override
    protected void runUseCase(int choice, Scanner in) {
        switch (choice) {
            case 0 -> createLiveEvent(in);
            case 1 -> reviewPendingEvents(in);
            case 2 -> viewEvents();
            case 3 -> resumeDraft(in);
            case 4 -> settleEvent(in);
        }
    }

    private void createLiveEvent(Scanner in) {
        String name = askText(in, "Event name: ", null);
        Event.Type type = askOption(in, "Type", Event.Type.values());
        Wrestler.Brand brand = askOption(in, "Brand", Wrestler.Brand.values());
        Integer showId = type == Event.Type.WEEKLY_TV ? askShowId(in, brand) : null;
        LocalDate date = askDateNotPast(in, "Date (YYYY-MM-DD): ", null);
        String city = askText(in, "City: ", null);
        planEvent(in, null, name, type, brand, showId, date, city);
    }

    private void resumeDraft(Scanner in) {
        List<Event> drafts = new ArrayList<>();
        for (Event e : Event.loadAll()) {
            if (e.getStatus() == Event.Status.DRAFT) drafts.add(e);
        }
        if (drafts.isEmpty()) {
            System.out.println("No drafts to resume.");
            return;
        }

        Event draft = askOption(in, "Drafts", drafts.toArray(new Event[0]));
        LocalDate date = draft.getDate();
        if (draft.hasPassed()) {
            System.out.println("This draft's date has passed.");
            date = askDateNotPast(in, "New date (YYYY-MM-DD): ", null);
        }
        Integer showId = draft.getShowId();
        if (showId == null && draft.getType() == Event.Type.WEEKLY_TV) showId = askShowId(in, draft.getBrand());
        planEvent(in, draft, draft.getName(), draft.getType(), draft.getBrand(), showId, date, draft.getCity());
    }

    // draft is null for a new event
    private void planEvent(Scanner in, Event draft, String name, Event.Type type, Wrestler.Brand brand,
                           Integer showId, LocalDate date, String city) {
        List<Integer> rejected = new ArrayList<>();

        while (true) {
            List<Venue> venues = Venue.findAvailable(city, date, type.getMinCapacity());
            venues.removeIf(v -> rejected.contains(v.getId()));
            if (venues.isEmpty()) System.out.println("No venue fits.");
            for (int i = 0; i < venues.size(); i++) {
                System.out.println("  " + (i + 1) + ". " + venues.get(i));
            }

            String input = askText(in, "Pick a venue number, or add, change, draft, cancel: ", null).toLowerCase();
            if (input.equals("cancel")) {
                System.out.println("Cancelled. Nothing saved.");
                return;
            }
            if (input.equals("draft")) {
                saveEvent(draft, name, type, brand, date, city, 0, showId, 0, Event.Status.DRAFT);
                return;
            }
            if (input.equals("add")) {
                addVenue(in, city, type.getMinCapacity());
                continue;
            }
            if (input.equals("change")) {
                date = askDateNotPast(in, "Date (enter to keep " + date + "): ", date);
                city = askText(in, "City (enter to keep " + city + "): ", city);
                rejected.clear();
                continue;
            }
            int choice = parseChoice(input, venues.size());
            if (choice < 0) {
                System.out.println("Invalid choice.");
                continue;
            }

            Venue venue = venues.get(choice);
            if (!askYes(in, "Did the venue accept the hold? (y/n): ")) {
                System.out.println("Venue rejected the hold.");
                rejected.add(venue.getId());
                continue;
            }
            System.out.println("Tentative hold placed: " + venue);

            if (!askYes(in, "Confirm this event? (y/n): ")) {
                System.out.println("Hold released. Nothing saved.");
                return;
            }
            if (Event.brandHasEventOn(brand, date)) {
                System.out.println(brand + " already has an event on " + date + ".");
                if (!askYes(in, "Change the date? (y/n): ")) {
                    System.out.println("Hold released. Nothing saved.");
                    return;
                }
                date = askDateNotPast(in, "Date (YYYY-MM-DD): ", null);
                rejected.clear();
                continue;
            }

            long cost = venue.getRentalPrice();
            Event.Status status = cost > type.getBudgetCents() ? Event.Status.PENDING_APPROVAL : Event.Status.SCHEDULED;
            saveEvent(draft, name, type, brand, date, venue.getLocation(), venue.getId(), showId, cost, status);
            return;
        }
    }

    // The ID of the brand's TV show this event tapes, or null if the brand has no shows
    private Integer askShowId(Scanner in, Wrestler.Brand brand) {
        List<Show> shows = new ArrayList<>();
        for (Show s : Show.loadAll()) {
            if (s.getBrand() == brand) shows.add(s);
        }
        if (shows.isEmpty()) {
            System.out.println(brand + " has no TV shows, so this event won't be linked to one.");
            return null;
        }
        return askOption(in, "Show", shows.toArray(new Show[0])).getId();
    }

    private void addVenue(Scanner in, String city, int minCapacity) {
        int capacity = Math.toIntExact(askPositiveLong(in, "Capacity: ", null));
        long rentalDollars = askPositiveLong(in, "Rental rate in dollars: ", null);
        String location = askText(in, "City (enter for " + city + "): ", city);
        Venue venue = new Venue(IdGenerator.nextId(Venue.FILE), capacity, location, rentalDollars * 100);
        try {
            CsvWriter.appendRow(Venue.FILE, venue.toRow());
            System.out.println("Added " + venue);
            if (!location.equalsIgnoreCase(city)) {
                System.out.println("It won't be listed because it isn't in " + city + ".");
            } else if (capacity < minCapacity) {
                System.out.println("It won't be listed because this event type needs at least " + minCapacity + " seats.");
            }
        } catch (IOException e) {
            System.out.println("Could not save venue: " + e.getMessage());
        }
    }

    // A resumed draft keeps its ID and is replaced in the file
    private void saveEvent(Event draft, String name, Event.Type type, Wrestler.Brand brand, LocalDate date, String city,
                           int venueId, Integer showId, long cost, Event.Status status) {
        int id = draft == null ? IdGenerator.nextId(Event.FILE) : draft.getId();
        Event event = new Event(id, name, type, brand, date, city, venueId, showId, cost, status);
        try {
            if (draft == null) {
                CsvWriter.appendRow(Event.FILE, event.toRow());
            } else {
                List<Event> events = Event.loadAll();
                events.replaceAll(e -> e.getId() == id ? event : e);
                Event.saveAll(events);
            }
            System.out.println("Saved " + event);
            if (status == Event.Status.SCHEDULED) notifyDepartments(event);
            if (status == Event.Status.PENDING_APPROVAL) System.out.println("Over budget. Routed to the finance manager.");
        } catch (IOException e) {
            System.out.println("Could not save event: " + e.getMessage());
        }
    }

    private void reviewPendingEvents(Scanner in) {
        List<Event> events = Event.loadAll();
        List<Event> pending = new ArrayList<>();
        for (Event e : events) {
            if (e.getStatus() == Event.Status.PENDING_APPROVAL || e.getStatus() == Event.Status.SETTLEMENT_PENDING) {
                pending.add(e);
            }
        }
        if (pending.isEmpty()) {
            System.out.println("No events pending approval.");
            return;
        }

        Event event = askOption(in, "Pending events", pending.toArray(new Event[0]));
        if (event.getStatus() == Event.Status.SETTLEMENT_PENDING) {
            reviewInvoice(in, events, event);
            return;
        }
        if (event.hasPassed()) System.out.println("This event's date has passed, so it can't be approved.");
        boolean approved = !event.hasPassed() && askYes(in, "Approve? (y/n): ");
        if (approved) {
            event.schedule();
        } else {
            event.revertToDraft();
        }
        try {
            Event.saveAll(events);
            System.out.println("Saved " + event);
            if (approved) notifyDepartments(event);
        } catch (IOException e) {
            System.out.println("Could not save event: " + e.getMessage());
        }
    }

    private void settleEvent(Scanner in) {
        List<Event> events = Event.loadAll();
        List<Event> settleable = new ArrayList<>();
        for (Event e : events) {
            if (e.getStatus() == Event.Status.SCHEDULED && e.hasPassed()) settleable.add(e);
        }
        if (settleable.isEmpty()) {
            System.out.println("No events to settle.");
            return;
        }

        Event event = askOption(in, "Events to settle", settleable.toArray(new Event[0]));
        long quote = event.getCostCents();
        System.out.println("Quoted venue cost: " + Venue.dollars(quote));
        long invoice = askNonNegativeLong(in, "Final invoice in dollars (enter for the quote): ", quote / 100) * 100;
        printSettlementReport(event, invoice);
        if (!askYes(in, "Confirm settlement? (y/n): ")) {
            System.out.println("Nothing changed.");
            return;
        }

        if (invoice > quote) {
            event.holdForSettlement(invoice);
            if (saveEvents(events)) System.out.println("The invoice is over the quote. Routed to the finance manager.");
            return;
        }
        finishSettlement(events, event, invoice);
    }

    private void reviewInvoice(Scanner in, List<Event> events, Event event) {
        printSettlementReport(event, event.getInvoiceCents());
        if (askYes(in, "Approve the invoice? (y/n): ")) {
            finishSettlement(events, event, event.getInvoiceCents());
            return;
        }
        event.rejectInvoice();
        if (saveEvents(events)) {
            System.out.println("Invoice rejected. " + event.getName() + " can be settled again with a corrected invoice.");
        }
    }

    // the bank has to cover the invoice once the ticket revenue is in
    private void finishSettlement(List<Event> events, Event event, long invoiceCents) {
        FinanceDepartment finance = FinanceDepartment.getInstance();
        long revenue = event.revenueCents();
        long balance = Math.round(finance.getCurrentBalance() * 100);
        if (balance + revenue < invoiceCents) {
            System.out.println("The bank is " + Venue.dollars(invoiceCents - balance - revenue)
                + " short of the invoice. Nothing was deposited or paid.");
            return;
        }

        if (revenue > 0) {
            System.out.println("Deposited: " + finance.processDeposit("Ticket revenue for event #" + event.getId(), revenue / 100.0));
        }
        if (invoiceCents > 0) {
            System.out.println("Paid: " + finance.processWithdrawal("Venue invoice for event #" + event.getId(), invoiceCents / 100.0));
        }
        event.complete(invoiceCents);
        if (saveEvents(events)) {
            System.out.println("Settled " + event);
            System.out.printf("Bank balance: $%,.2f%n", finance.getCurrentBalance());
        }
    }

    private void printSettlementReport(Event event, long invoiceCents) {
        Venue venue = Venue.findById(event.getVenueId());
        int capacity = venue == null ? 0 : venue.getCapacity();
        System.out.println("\nSettlement report: " + event.getName() + " on " + event.getDate());
        for (Ticket.Section s : Ticket.Section.values()) {
            int seats = venue == null ? 0 : s.getCapacity(venue);
            int sold = event.ticketsSold(s);
            System.out.printf(" %-10s %,6d of %,6d sold (%s) | %s%n",
                s, sold, seats, percent(sold, seats), Venue.dollars(event.revenueCents(s)));
        }
        System.out.printf(" %-10s %,6d of %,6d (%s)%n",
            "Attendance", event.ticketsSold(), capacity, percent(event.ticketsSold(), capacity));
        
        long revenue = event.revenueCents();
        long profit = revenue - invoiceCents;
        System.out.println(" Ticket revenue: " + Venue.dollars(revenue));
        System.out.println(" Venue invoice: " + Venue.dollars(invoiceCents) + " (quoted " + Venue.dollars(event.getCostCents()) + ")");
        System.out.println(" " + (profit >= 0 ? "Profit:" : "Loss ") + "      " + Venue.dollars(Math.abs(profit)));
    }

    private String percent(int part, int whole) {
        return whole == 0 ? "n/a" : String.format("%.1f%%", 100.0 * part / whole);
    }

    private boolean saveEvents(List<Event> events) {
        try {
            Event.saveAll(events);
            return true;
        } catch (IOException e) {
            System.out.println("Could not save event: " + e.getMessage());
            return false;
        }
    }
    
    private void viewEvents() {
        List<Event> events = Event.loadAll();
        if (events.isEmpty()) {
            System.out.println("No events yet.");
        }
        for (Event e : events) {
            System.out.println(e);
        }
    }

    private void notifyDepartments(Event event) {
        System.out.println("Notified Ticketing and Talent Relations about event #" + event.getId() + ".");
    }

    private LocalDate askDateNotPast(Scanner in, String prompt, LocalDate defaultDate) {
        while (true) {
            LocalDate date = askDate(in, prompt, defaultDate);
            if (!date.isBefore(LocalDate.now())) return date;
            System.out.println("That date has passed.");
        }
    }

    private boolean askYes(Scanner in, String prompt) {
        while (true) {
            String input = askText(in, prompt, null).toLowerCase();
            if (input.equals("y") || input.equals("yes")) return true;
            if (input.equals("n") || input.equals("no")) return false;
            System.out.println("Please enter y or n.");
        }
    }
}
