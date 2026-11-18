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
        // Menu order. Each command calls back into this department (the receiver)
        addCommand(new ScheduleLiveBroadcastCommand(this));
        addCommand(new ViewLiveBroadcastsCommand(this));
        addCommand(new RecordBroadcastRatingsCommand(this));
        addCommand(new CancelLiveBroadcastCommand(this));
    }

    // Package-private so ScheduleLiveBroadcastCommand can run it
    void scheduleLiveBroadcast(Scanner in) {
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

    // Package-private so ViewLiveBroadcastsCommand can run it
    void viewLiveBroadcasts(Scanner in) {
        List<LiveBroadcast> broadcasts = schedule.getBroadcasts();
        if (broadcasts.isEmpty()) {
            System.out.println("No live broadcasts yet.");
        }
        for (LiveBroadcast b : broadcasts) {
            System.out.println(b);
        }
    }

    // Package-private so RecordBroadcastRatingsCommand can run it
    void recordBroadcastRatings(Scanner in) {
        // 1 getAwaitingRatings
        List<LiveBroadcast> awaiting = schedule.getAwaitingRatings();
        if (awaiting.isEmpty()) {
            System.out.println("No broadcasts are awaiting ratings.");
            return;
        }
        System.out.println("Broadcasts awaiting ratings:");
        for (LiveBroadcast b : awaiting) {
            System.out.println(b + " | " + showName(b));
        }

        LiveBroadcast broadcast;
        while (true) {
            // 2 askIdOrCancel
            Integer id = askIdOrCancel(in, "Broadcast ID (or 'cancel'): ");
            if (id == null) return;

            // 3 findBroadcast
            broadcast = schedule.findBroadcast(id);
            if (broadcast == null) {
                System.out.println("No broadcast with ID " + id + ".");
                continue;
            }

            // 4 checkReadyForRatings
            try {
                schedule.checkReadyForRatings(broadcast);
            } catch (IllegalArgumentException e) {
                System.out.println(e.getMessage() + " Try again.");
                continue;
            }
            break;
        }

        // 5 Show.findById, getNetwork
        Show show = Show.findById(broadcast.getShowId());
        Network network = show.getNetwork();
        System.out.println(show.getName() + " on " + network.getName() + ", aired "
                + broadcast.getAirDate() + " at " + broadcast.getStartTime());
        System.out.println(String.format("Target: %,d viewers", network.getTargetViewers()));

        // 6 ask for viewers
        Integer viewers = askIdOrCancel(in, "Viewers (or 'cancel'): ");
        if (viewers == null) return;

        // 7 recordRatings
        try {
            schedule.recordRatings(broadcast, viewers);
        } catch (IllegalArgumentException e) {
            System.out.println(e.getMessage());
            return;
        }

        // 8 save
        try {
            schedule.save();
        } catch (IOException e) {
            // put it back the way it was so the list matches the file
            broadcast.undoRatings();
            System.out.println("Could not save ratings: " + e.getMessage());
            return;
        }

        // 9 meetsTarget, percentOfTarget
        String result = network.meetsTarget(viewers) ? "Met" : "Missed";
        System.out.println(String.format("Recorded %,d viewers for %s. %s %s's target (%d%% of %,d).",
                viewers, show.getName(), result, network.getName(),
                network.percentOfTarget(viewers), network.getTargetViewers()));
    }

    // Package-private so CancelLiveBroadcastCommand can run it
    void cancelLiveBroadcast(Scanner in) {
        // 1 getCancellable
        List<LiveBroadcast> cancellable = schedule.getCancellable();
        if (cancellable.isEmpty()) {
            System.out.println("No upcoming broadcasts to cancel.");
            return;
        }
        System.out.println("Upcoming broadcasts:");
        for (LiveBroadcast b : cancellable) {
            System.out.println(b + " | " + showName(b));
        }

        LiveBroadcast broadcast;
        while (true) {
            // 2 askIdOrCancel
            Integer id = askIdOrCancel(in, "Broadcast ID (or 'cancel'): ");
            if (id == null) return;

            // 3 findBroadcast
            broadcast = schedule.findBroadcast(id);
            if (broadcast == null) {
                System.out.println("No broadcast with ID " + id + ".");
                continue;
            }

            // 4 checkCanCancel
            try {
                schedule.checkCanCancel(broadcast);
            } catch (IllegalArgumentException e) {
                System.out.println(e.getMessage() + " Try again.");
                continue;
            }
            break;
        }

        // 5 confirm
        Show show = Show.findById(broadcast.getShowId());
        Network network = show == null ? null : show.getNetwork();
        System.out.println(broadcast + " | " + showName(broadcast)
                + " on " + (network == null ? "unknown network" : network.getName()));
        Boolean yes = askYesOrCancel(in, "Cancel this broadcast? (y/n): ");
        if (yes == null) return;
        if (!yes) {
            System.out.println("Nothing was changed.");
            return;
        }

        // 6 cancelBroadcast
        try {
            schedule.cancelBroadcast(broadcast);
        } catch (IllegalArgumentException e) {
            System.out.println(e.getMessage());
            return;
        }

        // 7 save
        try {
            schedule.save();
        } catch (IOException e) {
            broadcast.setStatus(LiveBroadcast.BroadcastStatus.SCHEDULED); // keep the list in line with the file
            System.out.println("Could not save: " + e.getMessage());
            return;
        }

        // 8 done
        System.out.println("Cancelled Broadcast #" + broadcast.getId()
                + ". The event and time slot are free to schedule again.");
    }

    // The show's name for a broadcast, for listing
    private String showName(LiveBroadcast b) {
        Show show = Show.findById(b.getShowId());
        return show == null ? "Show #" + b.getShowId() : show.getName();
    }

    // Asks until the user types y or n. Returns true/false, or null if they type 'cancel'
    private Boolean askYesOrCancel(Scanner in, String prompt) {
        while (true) {
            String input = askOrCancel(in, prompt);
            if (input == null) return null;
            if (input.equalsIgnoreCase("y") || input.equalsIgnoreCase("yes")) return true;
            if (input.equalsIgnoreCase("n") || input.equalsIgnoreCase("no")) return false;
            System.out.println("Please enter y or n.");
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
