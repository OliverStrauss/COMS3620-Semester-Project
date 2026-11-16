import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * A match has id, eventId, wrestler1Id, wrestler2Id, type, storylineId, winnerId and status.
 * Each one is a line in Data/Matches.txt,
 * like 1,2,3,5,SINGLES,2,3,BOOKED.
 * storylineId 0 means a one-off match. winnerId 0 means NO_CONTEST.
 */
public class Match {
    public static final String FILE = "Data/Matches.txt";

    public enum Type { SINGLES, NO_DQ, LADDER, STEEL_CAGE }

    // PULLED is for Medical's Pre-Event Check, when a booked wrestler can't go
    public enum Status { BOOKED, PULLED }

    private final int id;
    private final int eventId;
    private final int wrestler1Id;
    private final int wrestler2Id;
    private final Type type;
    private final int storylineId;
    private final int winnerId;
    private final Status status;

    public Match(int id, int eventId, int wrestler1Id, int wrestler2Id, Type type,
                 int storylineId, int winnerId, Status status) {
        if (wrestler1Id == wrestler2Id) {
            throw new IllegalArgumentException("A wrestler can't face themselves");
        }
        if (winnerId != 0 && winnerId != wrestler1Id && winnerId != wrestler2Id) {
            throw new IllegalArgumentException("Winner must be one of the two wrestlers, or 0 for no contest");
        }
        this.id = id;
        this.eventId = eventId;
        this.wrestler1Id = wrestler1Id;
        this.wrestler2Id = wrestler2Id;
        this.type = type;
        this.storylineId = storylineId;
        this.winnerId = winnerId;
        this.status = status;
    }

    public int getId() {
        return id;
    }

    public int getEventId() {
        return eventId;
    }

    public int getWrestler1Id() {
        return wrestler1Id;
    }

    public int getWrestler2Id() {
        return wrestler2Id;
    }

    public Type getType() {
        return type;
    }

    public int getStorylineId() {
        return storylineId;
    }

    public int getWinnerId() {
        return winnerId;
    }

    public Status getStatus() {
        return status;
    }

    public boolean involves(int wrestlerId) {
        return wrestler1Id == wrestlerId || wrestler2Id == wrestlerId;
    }

    // BOOKED matches on this event. PULLED ones don't count toward the card
    public static List<Match> forEvent(int eventId) {
        List<Match> matches = new ArrayList<>();
        for (Match m : loadAll()) {
            if (m.eventId == eventId && m.status == Status.BOOKED) matches.add(m);
        }
        return matches;
    }

    // True if this wrestler is already in a BOOKED match on this event
    public static boolean isBooked(int wrestlerId, int eventId) {
        for (Match m : forEvent(eventId)) {
            if (m.involves(wrestlerId)) return true;
        }
        return false;
    }

    // Every match in the file. Empty list if the file doesn't exist yet
    public static List<Match> loadAll() {
        List<Match> matches = new ArrayList<>();
        if (!new File(FILE).exists()) return matches; // CsvParser prints an error for missing files
        for (ArrayList<Object> row : CsvParser.parsecsv(FILE)) {
            if (row.get(0) == null) continue; // blank line
            matches.add(fromRow(row));
        }
        return matches;
    }

    // The match with this ID, or null if there isn't one
    public static Match findById(int id) {
        for (Match m : loadAll()) {
            if (m.getId() == id) return m;
        }
        return null;
    }

    // Rewrites the whole file. Use after changing a match from loadAll()
    public static void saveAll(List<Match> matches) throws IOException {
        List<List<Object>> rows = new ArrayList<>();
        for (Match m : matches) {
            rows.add(m.toRow());
        }
        CsvWriter.writeAll(FILE, rows);
    }

    // Builds a match from one CsvParser row, like [1, 2, 3, 5, SINGLES, 2, 3, BOOKED]
    public static Match fromRow(List<Object> row) {
        if (row.size() != 8) {
            throw new IllegalArgumentException("Bad match row: " + row);
        }
        return new Match(
                Integer.parseInt(String.valueOf(row.get(0))),
                Integer.parseInt(String.valueOf(row.get(1))),
                Integer.parseInt(String.valueOf(row.get(2))),
                Integer.parseInt(String.valueOf(row.get(3))),
                Type.valueOf(String.valueOf(row.get(4))),
                Integer.parseInt(String.valueOf(row.get(5))),
                Integer.parseInt(String.valueOf(row.get(6))),
                Status.valueOf(String.valueOf(row.get(7))));
    }

    // Fields in file order, ready for CsvWriter
    public List<Object> toRow() {
        return List.of(id, eventId, wrestler1Id, wrestler2Id, type, storylineId, winnerId, status);
    }

    // Like: Match #1 | SmackDown - Chicago | Lightning Lena vs La Tormenta | SINGLES | Battle for SmackDown | Winner: Lightning Lena | BOOKED
    @Override
    public String toString() {
        Event event = Event.findById(eventId);
        Storyline storyline = Storyline.findById(storylineId);
        return String.format("Match #%d | %s | %s vs %s | %s | %s | %s | %s",
                id,
                event == null ? "Event #" + eventId : event.getName(),
                ringName(wrestler1Id), ringName(wrestler2Id),
                type,
                storylineId == 0 ? "One-off" : storyline == null ? "Storyline #" + storylineId : storyline.getName(),
                winnerId == 0 ? "NO_CONTEST" : "Winner: " + ringName(winnerId),
                status);
    }

    // Ring name for display, or #id if the wrestler is gone from the file
    private static String ringName(int wrestlerId) {
        Wrestler w = Wrestler.findById(wrestlerId);
        return w == null ? "#" + wrestlerId : w.getRingName();
    }
}
