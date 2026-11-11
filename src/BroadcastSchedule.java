import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;

// Creator + Information Expert for live broadcasts: holds every broadcast,
// so it can check a new one against them before making it
public class BroadcastSchedule {
    private final List<LiveBroadcast> broadcasts;

    public BroadcastSchedule() {
        this.broadcasts = LiveBroadcast.loadAll();
    }

    // Checks the event and time slot, then makes a SCHEDULED broadcast and adds it to the list.
    // Doesn't save it; the caller calls save() on the result
    public LiveBroadcast createBroadcast(Show show, Event event, LocalTime startTime) {
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
                LiveBroadcast.BroadcastStatus.SCHEDULED);
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

            LocalDateTime existingStart = b.getAirDate().atTime(b.getStartTime());
            LocalDateTime existingEnd = existingStart.plusMinutes(existingShow.getLengthMinutes());
            if (newStart.isBefore(existingEnd) && existingStart.isBefore(newEnd)) {
                return b;
            }
        }
        return null;
    }

    public List<LiveBroadcast> getBroadcasts() {
        return broadcasts;
    }

    // Takes a broadcast back out of the list, like when saving it failed
    public void remove(LiveBroadcast b) {
        broadcasts.remove(b);
    }
}
