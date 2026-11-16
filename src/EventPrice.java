import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * A custom price for one section of one event. It overrides the section's default price.
 * Each one is a line in Data/EventPrices.txt,
 * like 7,FLOOR,250 (Survivor Series floor seats cost $250).
 */
public class EventPrice {
    public static final String FILE = "Data/EventPrices.txt";

    private final int eventId;
    private final Ticket.Section section;
    private long price;

    public EventPrice(int eventId, Ticket.Section section, long price) {
        if (price <= 0) {
            throw new IllegalArgumentException("Price must be positive");
        }
        if (section == null) {
            throw new IllegalArgumentException("Section is required");
        }
        this.eventId = eventId;
        this.section = section;
        this.price = price;
    }

    public int getEventId() {
        return eventId;
    }

    public Ticket.Section getSection() {
        return section;
    }

    public long getPrice() {
        return price;
    }

    public void setPrice(long price) {
        if (price <= 0) {
            throw new IllegalArgumentException("Price must be positive");
        }
        this.price = price;
    }

    // Builds a price from one CsvParser row, like [7, FLOOR, 250]
    public static EventPrice fromRow(List<Object> row) {
        if (row.size() != 3) {
            throw new IllegalArgumentException("Bad event price row: " + row);
        }
        return new EventPrice(
                Integer.parseInt(String.valueOf(row.get(0))),
                Ticket.Section.valueOf(String.valueOf(row.get(1))),
                Long.parseLong(String.valueOf(row.get(2))));
    }

    // Every custom price in the file. Empty list if the file doesn't exist yet
    public static List<EventPrice> loadAll() {
        List<EventPrice> prices = new ArrayList<>();
        if (!new File(FILE).exists()) return prices; // CsvParser prints an error for missing files
        for (ArrayList<Object> row : CsvParser.parsecsv(FILE)) {
            if (row.get(0) == null) continue; // blank line
            prices.add(fromRow(row));
        }
        return prices;
    }

    // Rewrites the whole file. Use after changing a price from loadAll()
    public static void saveAll(List<EventPrice> prices) throws IOException {
        List<List<Object>> rows = new ArrayList<>();
        for (EventPrice p : prices) {
            rows.add(p.toRow());
        }
        CsvWriter.writeAll(FILE, rows);
    }

    // The custom price for this event and section, or null if it uses the default
    public static EventPrice find(int eventId, Ticket.Section section) {
        for (EventPrice p : loadAll()) {
            if (p.getEventId() == eventId && p.getSection() == section) return p;
        }
        return null;
    }

    // Fields in file order, ready for CsvWriter
    public List<Object> toRow() {
        return List.of(
                eventId,
                section,
                price);
    }

    @Override
    public String toString() {
        return String.format("Event #%d | %s | $%,d", eventId, section, price);
    }
}
