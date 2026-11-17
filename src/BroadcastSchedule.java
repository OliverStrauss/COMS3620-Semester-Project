import java.io.IOException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

// Creator + Information Expert for live broadcasts: holds every broadcast,
// so it can check a new one against them before making it, and check the
// rules for recording ratings and cancelling
public class BroadcastSchedule {
    private final List<LiveBroadcast> broadcasts;

    public BroadcastSchedule() {
        this.broadcasts = LiveBroadcast.loadAll();
    }

    // Checks the event and time slot, then makes a SCHEDULED broadcast and adds it to the list.
    // Doesn't save it; the caller calls save() on the result
    public LiveBroadcast createBroadcast(Show show, Event event, LocalTime startTime) {
        // 3.0 only events Live Events has scheduled (not drafts or pending finance approval)
        if (event.getStatus() != Event.Status.SCHEDULED) {
            throw new IllegalArgumentException(event.getName() + " is " + event.getStatus()
                    + ", so it can't be broadcast until it's scheduled.");
        }

        // 3.1 isUpcoming
        if (!event.isUpcoming()) {
            throw new IllegalArgumentException(event.getName() + " is on " + event.getDate()
                    + ", which has already passed.");
        }

        // 3.2 hasBroadcastFor
        LiveBroadcast existing = hasBroadcastFor(event);
        if (existing != null) {
            throw new IllegalArgumentException(event.getName() + " already has a live broadcast: " + existing);
        }

        // 3.3 getNetwork
        Network network = show.getNetwork();
        if (network == null) {
            throw new IllegalArgumentException(show.getName() + " has no network (ID "
                    + show.getNetworkId() + " not found).");
        }

        // 3.4 isSlotTaken
        LiveBroadcast clash = isSlotTaken(network, event.getDate(), startTime, show.getLengthMinutes());
        if (clash != null) {
            throw new IllegalArgumentException(network.getName() + " already has a broadcast at that time: " + clash);
        }

        // 3.5 create
        LiveBroadcast broadcast = new LiveBroadcast(IdGenerator.nextId(LiveBroadcast.FILE),
                show.getId(), event.getId(), event.getDate(), startTime,
                LiveBroadcast.BroadcastStatus.SCHEDULED, null);
        broadcasts.add(broadcast);
        return broadcast;
    }

    // The non-cancelled broadcast for this event, or null if there isn't one
    public LiveBroadcast hasBroadcastFor(Event event) {
        for (LiveBroadcast b : broadcasts) {
            if (b.getEventId() == event.getId()
                    && b.getStatus() != LiveBroadcast.BroadcastStatus.CANCELLED) {
                return b;
            }
        }
        return null;
    }

    // A non-cancelled broadcast on this network whose air time overlaps the new one, or null.
    // Uses LocalDateTime so shows that run past midnight still compare right
    public LiveBroadcast isSlotTaken(Network network, LocalDate date, LocalTime start, int lengthMinutes) {
        LocalDateTime newStart = date.atTime(start);
        LocalDateTime newEnd = newStart.plusMinutes(lengthMinutes);

        for (LiveBroadcast b : broadcasts) {
            if (b.getStatus() == LiveBroadcast.BroadcastStatus.CANCELLED) continue;
            Show existingShow = Show.findById(b.getShowId());
            if (existingShow == null || existingShow.getNetworkId() != network.getId()) continue;

            LocalDateTime existingStart = b.startDateTime();
            LocalDateTime existingEnd = existingStart.plusMinutes(existingShow.getLengthMinutes());
            if (newStart.isBefore(existingEnd) && existingStart.isBefore(newEnd)) {
                return b;
            }
        }
        return null;
    }

    // The broadcast with this ID, or null if there isn't one
    public LiveBroadcast findBroadcast(int id) {
        for (LiveBroadcast b : broadcasts) {
            if (b.getId() == id) return b;
        }
        return null;
    }

    // ---- Record Broadcast Ratings ----

    // 1 getAwaitingRatings: SCHEDULED broadcasts whose show has finished airing, soonest first
    public List<LiveBroadcast> getAwaitingRatings() {
        List<LiveBroadcast> awaiting = new ArrayList<>();
        for (LiveBroadcast b : broadcasts) {
            if (b.getStatus() != LiveBroadcast.BroadcastStatus.SCHEDULED) continue;
            // 1.1 Show.findById for the show's length
            Show show = Show.findById(b.getShowId());
            if (show != null && b.hasFinishedAiring(show.getLengthMinutes())) {
                awaiting.add(b);
            }
        }
        awaiting.sort(Comparator.comparing(LiveBroadcast::startDateTime));
        return awaiting;
    }

    // 4 checkReadyForRatings: throws if this broadcast can't get ratings right now
    public void checkReadyForRatings(LiveBroadcast b) {
        // 4.1 not cancelled
        if (b.getStatus() == LiveBroadcast.BroadcastStatus.CANCELLED) {
            throw new IllegalArgumentException("Broadcast #" + b.getId() + " was cancelled, so it has no ratings.");
        }
        // 4.2 no ratings yet
        if (b.getViewers() != null || b.getStatus() == LiveBroadcast.BroadcastStatus.AIRED) {
            String count = b.getViewers() == null ? "unknown" : String.format("%,d", b.getViewers());
            throw new IllegalArgumentException("Broadcast #" + b.getId() + " already has ratings: "
                    + count + " viewers.");
        }
        // 4.3 hasFinishedAiring
        Show show = Show.findById(b.getShowId());
        int length = show == null ? 0 : show.getLengthMinutes();
        if (!b.hasFinishedAiring(length)) {
            throw new IllegalArgumentException("Broadcast #" + b.getId() + " hasn't finished airing yet (airs "
                    + b.getAirDate() + " at " + b.getStartTime() + ").");
        }
    }

    // 7 recordRatings: checks the rules again, then records the viewers and marks it AIRED
    public void recordRatings(LiveBroadcast b, int viewers) {
        // 7.1 checkReadyForRatings
        checkReadyForRatings(b);
        if (viewers <= 0) {
            throw new IllegalArgumentException("Viewers must be a whole number above 0.");
        }
        // 7.2 recordViewers
        b.recordViewers(viewers);
    }

    // ---- Cancel Live Broadcast ----

    // 1 getCancellable: SCHEDULED broadcasts that haven't started yet, soonest first
    public List<LiveBroadcast> getCancellable() {
        List<LiveBroadcast> cancellable = new ArrayList<>();
        for (LiveBroadcast b : broadcasts) {
            if (b.getStatus() == LiveBroadcast.BroadcastStatus.SCHEDULED && !b.hasStarted()) {
                cancellable.add(b);
            }
        }
        cancellable.sort(Comparator.comparing(LiveBroadcast::startDateTime));
        return cancellable;
    }

    // 4 checkCanCancel: throws if this broadcast can't be cancelled
    public void checkCanCancel(LiveBroadcast b) {
        // 4.1 not already cancelled
        if (b.getStatus() == LiveBroadcast.BroadcastStatus.CANCELLED) {
            throw new IllegalArgumentException("Broadcast #" + b.getId() + " is already cancelled.");
        }
        // 4.2 hasStarted
        if (b.getStatus() == LiveBroadcast.BroadcastStatus.AIRED || b.hasStarted()) {
            throw new IllegalArgumentException("Broadcast #" + b.getId()
                    + " has already aired, so it can't be cancelled.");
        }
    }

    // 6 cancelBroadcast: checks the rules again, then marks it CANCELLED
    public void cancelBroadcast(LiveBroadcast b) {
        // 6.1 checkCanCancel
        checkCanCancel(b);
        // 6.2 cancel
        b.cancel();
    }

    // Rewrites the file with every broadcast, after an edit like ratings or cancelling
    public void save() throws IOException {
        LiveBroadcast.saveAll(broadcasts);
    }

    public List<LiveBroadcast> getBroadcasts() {
        return broadcasts;
    }

    // Takes a broadcast back out of the list, like when saving it failed
    public void remove(LiveBroadcast b) {
        broadcasts.remove(b);
    }
}
