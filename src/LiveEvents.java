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
        return List.of("Create Live Event", "Review Pending Events (Finance)", "View Events");
    }

    @Override
    protected void runUseCase(int choice, Scanner in) {
        switch (choice) {
            case 0 -> createLiveEvent(in);
            case 1 -> reviewPendingEvents(in);
            case 2 -> viewEvents();
        }
    }

    private void createLiveEvent(Scanner in) {
        String name = askText(in, "Event name: ", null);
        Event.Type type = askOption(in, "Type", Event.Type.values());
        Wrestler.Brand brand = askOption(in, "Brand", Wrestler.Brand.values());
        LocalDate date = askDateNotPast(in, "Date (YYYY-MM-DD): ", null);
        String city = askText(in, "City: ", null);
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
                saveNewEvent(name, type, brand, date, city, 0, 0, Event.Status.DRAFT);
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
            saveNewEvent(name, type, brand, date, venue.getLocation(), venue.getId(), cost, status);
            return;
        }
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

    private void saveNewEvent(String name, Event.Type type, Wrestler.Brand brand, LocalDate date, String city,
                              int venueId, long cost, Event.Status status) {
        Event event = new Event(IdGenerator.nextId(Event.FILE), name, type, brand, date, city, venueId, null, cost, status);
        try {
            CsvWriter.appendRow(Event.FILE, event.toRow());
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
            if (e.getStatus() == Event.Status.PENDING_APPROVAL) pending.add(e);
        }
        if (pending.isEmpty()) {
            System.out.println("No events pending approval.");
            return;
        }

        Event event = askOption(in, "Pending events", pending.toArray(new Event[0]));
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

    private <E> E askOption(Scanner in, String label, E[] options) {
        while (true) {
            System.out.println(label + ":");
            for (int i = 0; i < options.length; i++) {
                System.out.println("  " + (i + 1) + ". " + options[i]);
            }
            System.out.print("Pick a number: ");
            int choice = parseChoice(in.nextLine().trim(), options.length);
            if (choice >= 0) return options[choice];
            System.out.println("Invalid choice.");
        }
    }
}
