import java.io.IOException;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.List;
import java.util.Scanner;

public class TVShows extends Department {

    // H instead of HH so 9:00 works as well as 09:00
    private static final DateTimeFormatter TIME_FORMAT = DateTimeFormatter.ofPattern("H:mm");

    private final BroadcastSchedule schedule = new BroadcastSchedule();

    public TVShows() {
        super("TV Shows");
    }

    @Override
    protected List<String> getUseCases() {
        return List.of("Schedule Live Broadcast",
        "View Live Broadcasts"
        );
    }

    @Override
    protected void runUseCase(int choice, Scanner in) {
        switch (choice) {
            case 0 -> scheduleLiveBroadcast(in);
            case 1 -> viewLiveBroadcasts(in);
        }
    }

    private void scheduleLiveBroadcast(Scanner in) {
        List<Show> shows = Show.loadAll();
        if (shows.isEmpty()) {
            System.out.println("No shows yet.");
            return;
        }
        for (Show s : shows) {
            System.out.println(s);
        }

        Show show;
        List<Event> events;
        while (true) {
            Integer showId = askIdOrCancel(in, "Show ID (or 'cancel'): ");
            if (showId == null) return;
            // 1 Show.findById
            show = Show.findById(showId);
            if (show == null) {
                System.out.println("No show with ID " + showId + ".");
                continue;
            }
            events = Event.getUpcomingForShow(showId);
            if (events.isEmpty()) {
                System.out.println("That show has no upcoming events.");
                continue;
            }
            break;
        }

        System.out.println("Upcoming events:");
        for (Event e : events) {
            System.out.println(e);
        }

        while (true) {
            Integer eventId = askIdOrCancel(in, "Event ID (or 'cancel'): ");
            if (eventId == null) return;
            LocalTime startTime = askTimeOrCancel(in, "Start time (HH:MM, 24-hour, or 'cancel'): ");
            if (startTime == null) return;

            // 2 Event.findById
            Event event = Event.findById(eventId);
            if (event == null || event.getShowId() == null || event.getShowId() != show.getId()) {
                System.out.println("Event " + eventId + " isn't an event for " + show.getName() + ". Try again.");
                continue;
            }

            // 3 createBroadcast
            LiveBroadcast broadcast;
            try {
                broadcast = schedule.createBroadcast(show, event, startTime);
            } catch (IllegalArgumentException e) {
                System.out.println(e.getMessage() + " Try again.");
                continue;
            }

            // 4 save
            try {
                broadcast.save();
            } catch (IOException e) {
                schedule.remove(broadcast); // keep the list in line with the file
                System.out.println("Could not save broadcast: " + e.getMessage());
                return;
            }
            System.out.println("Scheduled " + broadcast);
            return;
        }
    }

    private void viewLiveBroadcasts(Scanner in) {
        List<LiveBroadcast> broadcasts = schedule.getBroadcasts();
        if (broadcasts.isEmpty()) {
            System.out.println("No live broadcasts yet.");
        }
        for (LiveBroadcast b : broadcasts) {
            System.out.println(b);
        }
    }

    // Asks until the user types a whole number above 0. Returns null if they type 'cancel'
    private Integer askIdOrCancel(Scanner in, String prompt) {
        while (true) {
            String input = askOrCancel(in, prompt);
            if (input == null) return null;
            try {
                int n = Integer.parseInt(input);
                if (n > 0) return n;
            } catch (NumberFormatException e) {
                // fall through to the error message
            }
            System.out.println("Please enter a whole number above 0.");
        }
    }

    // Asks until the user types a time like 20:00 or 9:00. Returns null if they type 'cancel'
    private LocalTime askTimeOrCancel(Scanner in, String prompt) {
        while (true) {
            String input = askOrCancel(in, prompt);
            if (input == null) return null;
            try {
                return LocalTime.parse(input, TIME_FORMAT);
            } catch (DateTimeParseException e) {
                System.out.println("Please enter a time as HH:MM, like 20:00.");
            }
        }
    }

    // Shared by the two helpers above: the trimmed input, or null after printing the cancel message
    private String askOrCancel(Scanner in, String prompt) {
        System.out.print(prompt);
        String input = in.hasNextLine() ? in.nextLine().trim() : "cancel"; // treat end of input as cancel
        if (input.equalsIgnoreCase("cancel")) {
            System.out.println("Cancelled. Nothing was saved.");
            return null;
        }
        return input;
    }
}
