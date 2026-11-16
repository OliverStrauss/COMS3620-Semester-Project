import java.io.File;
import java.io.IOException;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

/**
 * A ticket has id, eventId, buyerName, section, price, purchaseDate and status.
 * Each one is a line in Data/Tickets.txt,
 * like 1,3,Jane Smith,FLOOR,150,2026-11-04,SOLD.
 * Older lines without a status are read as SOLD.
 */
public class Ticket {
    public static final String FILE = "Data/Tickets.txt";

    // Each section gets a percent of the venue's seats. The percents add up to 100
    public enum Section {
        FLOOR(150, 5), LOWER_BOWL(90, 35), UPPER_BOWL(45, 60);

        private final long price;
        private final int percentOfVenue;

        Section(long price, int percentOfVenue) {
            this.price = price;
            this.percentOfVenue = percentOfVenue;
        }

        public long getPrice() {
            return price;
        }

        public int getCapacity(Venue venue) {
            return venue.getCapacity() * percentOfVenue / 100;
        }
    }

    public enum Status { SOLD, REFUNDED }

    private final int id;
    private final int eventId;
    private final String buyerName;
    private final Section section;
    private final long price;
    private final LocalDate purchaseDate;
    private Status status;

    public Ticket(int id, int eventId, String buyerName, Section section, long price, LocalDate purchaseDate,
            Status status) {
        if (price <= 0) {
            throw new IllegalArgumentException("Price must be positive");
        }
        if (section == null || purchaseDate == null || status == null) {
            throw new IllegalArgumentException("Section, purchase date and status are required");
        }
        this.id = id;
        this.eventId = eventId;
        this.buyerName = buyerName;
        this.section = section;
        this.price = price;
        this.purchaseDate = purchaseDate;
        this.status = status;
    }

    public int getId() {
        return id;
    }

    public int getEventId() {
        return eventId;
    }

    public String getBuyerName() {
        return buyerName;
    }

    public Section getSection() {
        return section;
    }

    public long getPrice() {
        return price;
    }

    public LocalDate getPurchaseDate() {
        return purchaseDate;
    }

    public Status getStatus() {
        return status;
    }

    public boolean isRefunded() {
        return status == Status.REFUNDED;
    }

    public void refund() {
        status = Status.REFUNDED;
    }

    // Builds a ticket from one CsvParser row, like [1, 3, Jane Smith, FLOOR, 150, 2026-11-04, SOLD]
    // Rows from before refunds existed have 6 fields and no status, so they count as SOLD
    public static Ticket fromRow(List<Object> row) {
        if (row.size() != 6 && row.size() != 7) {
            throw new IllegalArgumentException("Bad ticket row: " + row);
        }
        return new Ticket(
                Integer.parseInt(String.valueOf(row.get(0))),
                Integer.parseInt(String.valueOf(row.get(1))),
                String.valueOf(row.get(2)),
                Section.valueOf(String.valueOf(row.get(3))),
                Long.parseLong(String.valueOf(row.get(4))),
                LocalDate.parse(String.valueOf(row.get(5))),
                row.size() == 7 ? Status.valueOf(String.valueOf(row.get(6))) : Status.SOLD);
    }

    // Every ticket in the file. Empty list if the file doesn't exist yet
    public static List<Ticket> loadAll() {
        List<Ticket> tickets = new ArrayList<>();
        if (!new File(FILE).exists()) return tickets; // CsvParser prints an error for missing files
        for (ArrayList<Object> row : CsvParser.parsecsv(FILE)) {
            if (row.get(0) == null) continue; // blank line
            tickets.add(fromRow(row));
        }
        return tickets;
    }

    // What this section costs for this event: the event's custom price if it has one, otherwise the default
    public static long priceFor(Event event, Section section) {
        EventPrice custom = EventPrice.find(event.getId(), section);
        return custom != null ? custom.getPrice() : section.getPrice();
    }

    // The ticket with this ID, or null if there isn't one
    public static Ticket findById(int id) {
        for (Ticket t : loadAll()) {
            if (t.getId() == id) return t;
        }
        return null;
    }

    // Rewrites the whole file. Use after changing a ticket from loadAll()
    public static void saveAll(List<Ticket> tickets) throws IOException {
        List<List<Object>> rows = new ArrayList<>();
        for (Ticket t : tickets) {
            rows.add(t.toRow());
        }
        CsvWriter.writeAll(FILE, rows);
    }

    // Seats still open in this section for this event. 0 if the event's venue isn't in the file
    public static int seatsLeft(Event event, Section section) {
        Venue venue = Venue.findById(event.getVenueId());
        if (venue == null) return 0;
        int sold = 0;
        for (Ticket t : loadAll()) {
            // Refunded tickets give their seat back
            if (t.getEventId() == event.getId() && t.getSection() == section && !t.isRefunded()) sold++;
        }
        return section.getCapacity(venue) - sold;
    }

    // Fields in file order, ready for CsvWriter
    public List<Object> toRow() {
        return List.of(
                id,
                eventId,
                buyerName,
                section,
                price,
                purchaseDate,
                status);
    }

    // Readable form for printing, like: Ticket #1 | Event #3 | Jane Smith | FLOOR | $150 | 2026-11-04 | SOLD
    @Override
    public String toString() {
        return String.format("Ticket #%d | Event #%d | %s | %s | $%,d | %s | %s",
                id, eventId, buyerName, section, price, purchaseDate, status);
    }
}