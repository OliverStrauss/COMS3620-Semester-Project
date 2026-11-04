import java.io.File;
import java.io.IOException;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;

/**
 * A show airing live from an event on a date and time.
 * Each one is a line in Data/LiveBroadcasts.txt,
 * like 1,1,1,2026-11-09,20:00,SCHEDULED.
 */
public class LiveBroadcast {
    public static final String FILE = "Data/LiveBroadcasts.txt";

    public enum BroadcastStatus { SCHEDULED, AIRED, CANCELLED }

    private final int id;
    private final int showId;
    private final int eventId;
    private final LocalDate airDate;
    private final LocalTime startTime;
    private BroadcastStatus status;

    public LiveBroadcast(int id, int showId, int eventId, LocalDate airDate,
                         LocalTime startTime, BroadcastStatus status) {
        this.id = id;
        this.showId = showId;
        this.eventId = eventId;
        this.airDate = airDate;
        this.startTime = startTime;
        this.status = status;
    }

    public int getId() {
        return id;
    }

    public int getShowId() {
        return showId;
    }

    public int getEventId() {
        return eventId;
    }

    public LocalDate getAirDate() {
        return airDate;
    }

    public LocalTime getStartTime() {
        return startTime;
    }

    public BroadcastStatus getStatus() {
        return status;
    }

    public void setStatus(BroadcastStatus status) {
        this.status = status;
    }

    // Adds this broadcast to the end of the file
    public void save() throws IOException {
        CsvWriter.appendRow(FILE, toRow());
    }

    // Every broadcast in the file. Empty list if the file doesn't exist yet
    public static List<LiveBroadcast> loadAll() {
        List<LiveBroadcast> broadcasts = new ArrayList<>();
        if (!new File(FILE).exists()) return broadcasts; // CsvParser prints an error for missing files

        for (ArrayList<Object> row : CsvParser.parsecsv(FILE)) {
            if (row.get(0) == null) continue; // blank line
            broadcasts.add(fromRow(row));
        }
        return broadcasts;
    }

    // Builds a broadcast from one CsvParser row, like [1, 1, 1, 2026-11-09, 20:00, SCHEDULED]
    public static LiveBroadcast fromRow(List<Object> row) {
        if (row.size() != 6) {
            throw new IllegalArgumentException("Bad broadcast row: " + row);
        }
        return new LiveBroadcast(
                Integer.parseInt(String.valueOf(row.get(0))),
                Integer.parseInt(String.valueOf(row.get(1))),
                Integer.parseInt(String.valueOf(row.get(2))),
                LocalDate.parse(String.valueOf(row.get(3))),
                LocalTime.parse(String.valueOf(row.get(4))),
                BroadcastStatus.valueOf(String.valueOf(row.get(5))));
    }

    // Fields in file order, ready for CsvWriter
    public List<Object> toRow() {
        return List.of(id, showId, eventId, airDate, startTime, status);
    }

    @Override
    public String toString() {
        return String.format("Broadcast #%d | Show #%d | Event #%d | %s %s | %s",
                id, showId, eventId, airDate, startTime, status);
    }
}
