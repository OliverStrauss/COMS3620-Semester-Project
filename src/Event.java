import java.io.File;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;

/**
 * A live event at a venue on a date. showId is the TV show it's taped for,
 * or null for events that aren't part of a weekly show (like a PPV).
 * Each one is a line in Data/Events.txt, like 1,Raw - Boston,2026-11-09,1,1,0.
 *
 * Stand-in until the Live Events teammate (Caleb) adds his Event class.
 * The TV code needs getId(), getName(), getDate(), getShowId(), isUpcoming(),
 * findById() and getUpcomingForShow(), so his version should keep those.
 */
public class Event {
    public static final String FILE = "Data/Events.txt";

    private final int id;
    private final String name;
    private final LocalDate date;
    private final int venueId;
    private final Integer showId;
    private final int ppvPrice;

    public Event(int id, String name, LocalDate date, int venueId, Integer showId, int ppvPrice) {
        this.id = id;
        this.name = name;
        this.date = date;
        this.venueId = venueId;
        this.showId = showId;
        this.ppvPrice = ppvPrice;
    }

    public int getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public LocalDate getDate() {
        return date;
    }

    public int getVenueId() {
        return venueId;
    }

    // null if this event isn't for a weekly show
    public Integer getShowId() {
        return showId;
    }

    public int getPpvPrice() {
        return ppvPrice;
    }

    // True if the event is after today
    public boolean isUpcoming() {
        return date.isAfter(LocalDate.now());
    }

    // Every event in the file. Empty list if the file doesn't exist yet
    public static List<Event> loadAll() {
        List<Event> events = new ArrayList<>();
        if (!new File(FILE).exists()) return events; // CsvParser prints an error for missing files

        for (ArrayList<Object> row : CsvParser.parsecsv(FILE)) {
            if (row.get(0) == null) continue; // blank line
            events.add(fromRow(row));
        }
        return events;
    }

    // The event with this ID, or null if there isn't one
    public static Event findById(int id) {
        for (Event e : loadAll()) {
            if (e.getId() == id) return e;
        }
        return null;
    }

    // Events for this show that haven't happened yet, soonest first
    public static List<Event> getUpcomingForShow(int showId) {
        List<Event> upcoming = new ArrayList<>();
        for (Event e : loadAll()) {
            if (e.getShowId() != null && e.getShowId() == showId && e.isUpcoming()) {
                upcoming.add(e);
            }
        }
        upcoming.sort(Comparator.comparing(Event::getDate));
        return upcoming;
    }

    // Builds an event from one CsvParser row, like [1, Raw - Boston, 2026-11-09, 1, 1, 0]
    public static Event fromRow(List<Object> row) {
        if (row.size() != 6) {
            throw new IllegalArgumentException("Bad event row: " + row);
        }
        Object showCell = row.get(4); // empty cell comes back as null
        return new Event(
                Integer.parseInt(String.valueOf(row.get(0))),
                String.valueOf(row.get(1)),
                LocalDate.parse(String.valueOf(row.get(2))),
                Integer.parseInt(String.valueOf(row.get(3))),
                showCell == null ? null : Integer.parseInt(String.valueOf(showCell)),
                Integer.parseInt(String.valueOf(row.get(5))));
    }

    // Fields in file order, ready for CsvWriter. Arrays.asList because showId can be null
    public List<Object> toRow() {
        return Arrays.asList(id, name, date, venueId, showId, ppvPrice);
    }

    @Override
    public String toString() {
        return String.format("Event #%d | %s | %s | Venue #%d | Show %s",
                id, name, date, venueId, showId == null ? "none" : "#" + showId);
    }
}
