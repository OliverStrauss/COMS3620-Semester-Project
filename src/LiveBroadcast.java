import java.io.File;
import java.io.IOException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * A show airing live from an event on a date and time.
 * Each one is a line in Data/LiveBroadcasts.txt,
 * like 1,1,1,2026-11-09,20:00,AIRED,2100000.
 * viewers is empty until ratings are recorded.
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
    private Integer viewers; // null until ratings are recorded

    public LiveBroadcast(int id, int showId, int eventId, LocalDate airDate,
                         LocalTime startTime, BroadcastStatus status, Integer viewers) {
        this.id = id;
        this.showId = showId;
        this.eventId = eventId;
        this.airDate = airDate;
        this.startTime = startTime;
        this.status = status;
        this.viewers = viewers;
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

    public Integer getViewers() {
        return viewers;
    }

    // Ratings came in, so the broadcast has aired
    public void recordViewers(int viewers) {
        this.viewers = viewers;
        this.status = BroadcastStatus.AIRED;
    }

    // Puts it back to SCHEDULED with no viewers, like when saving the ratings failed
    public void undoRatings() {
        this.viewers = null;
        this.status = BroadcastStatus.SCHEDULED;
    }

    public void cancel() {
        status = BroadcastStatus.CANCELLED;
    }

    public LocalDateTime startDateTime() {
        return airDate.atTime(startTime);
    }

    // True once the start time has come
    public boolean hasStarted() {
        return !startDateTime().isAfter(LocalDateTime.now());
    }

    // True once the show's full length has gone by. Uses LocalDateTime so shows past midnight work
    public boolean hasFinishedAiring(int lengthMinutes) {
        return startDateTime().plusMinutes(lengthMinutes).isBefore(LocalDateTime.now());
    }

    // Adds this broadcast to the end of the file
    public void save() throws IOException {
        CsvWriter.appendRow(FILE, toRow());
    }

    // Rewrites the whole file, for edits like ratings or cancelling
    public static void saveAll(List<LiveBroadcast> broadcasts) throws IOException {
        List<List<Object>> rows = new ArrayList<>();
        for (LiveBroadcast b : broadcasts) {
            rows.add(b.toRow());
        }
        CsvWriter.writeAll(FILE, rows);
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

    // Builds a broadcast from one CsvParser row, like [1, 1, 1, 2026-11-09, 20:00, AIRED, 2100000].
    // Old 6-column rows (no viewers) still load, with viewers null
    public static LiveBroadcast fromRow(List<Object> row) {
        if (row.size() != 6 && row.size() != 7) {
            throw new IllegalArgumentException("Bad broadcast row: " + row);
        }
        Object viewersCell = row.size() == 7 ? row.get(6) : null;
        return new LiveBroadcast(
                Integer.parseInt(String.valueOf(row.get(0))),
                Integer.parseInt(String.valueOf(row.get(1))),
                Integer.parseInt(String.valueOf(row.get(2))),
                LocalDate.parse(String.valueOf(row.get(3))),
                LocalTime.parse(String.valueOf(row.get(4))),
                BroadcastStatus.valueOf(String.valueOf(row.get(5))),
                viewersCell == null ? null : Integer.valueOf(String.valueOf(viewersCell)));
    }

    // Fields in file order, ready for CsvWriter. Arrays.asList because viewers can be null
    public List<Object> toRow() {
        return Arrays.asList(id, showId, eventId, airDate, startTime, status, viewers);
    }

    @Override
    public String toString() {
        String text = String.format("Broadcast #%d | Show #%d | Event #%d | %s %s | %s",
                id, showId, eventId, airDate, startTime, status);
        if (viewers != null) {
            text += String.format(" | %,d viewers", viewers);
        }
        return text;
    }
}
