import java.io.File;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

/**
 * A ticket has id, eventName, buyerName, section, price and purchaseDate.
 * Each one is a line in Data/Tickets.txt,
 * like 1,WrestleMania,Jane Smith,FLOOR,150,2026-11-04.
 */
public class Ticket {
    public static final String FILE = "Data/Tickets.txt";

    // Capacities are small so a sold out section is easy to show in the demo
    public enum Section {
        FLOOR(150, 5), LOWER_BOWL(90, 10), UPPER_BOWL(45, 20);

        private final long price;
        private final int capacity;

        Section(long price, int capacity) {
            this.price = price;
            this.capacity = capacity;
        }

        public long getPrice() {
            return price;
        }

        public int getCapacity() {
            return capacity;
        }
    }

    private final int id;
    private final String eventName;
    private final String buyerName;
    private final Section section;
    private final long price;
    private final LocalDate purchaseDate;

    public Ticket(int id, String eventName, String buyerName, Section section, long price, LocalDate purchaseDate) {
        if (price <= 0) {
            throw new IllegalArgumentException("Price must be positive");
        }
        if (section == null || purchaseDate == null) {
            throw new IllegalArgumentException("Section and purchase date are required");
        }
        this.id = id;
        this.eventName = eventName;
        this.buyerName = buyerName;
        this.section = section;
        this.price = price;
        this.purchaseDate = purchaseDate;
    }

    public int getId() {
        return id;
    }

    public String getEventName() {
        return eventName;
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

    // Builds a ticket from one CsvParser row, like [1, WrestleMania, Jane Smith, FLOOR, 150, 2026-11-04]
    public static Ticket fromRow(List<Object> row) {
        if (row.size() != 6) {
            throw new IllegalArgumentException("Bad ticket row: " + row);
        }
        return new Ticket(
                Integer.parseInt(String.valueOf(row.get(0))),
                String.valueOf(row.get(1)),
                String.valueOf(row.get(2)),
                Section.valueOf(String.valueOf(row.get(3))),
                Long.parseLong(String.valueOf(row.get(4))),
                LocalDate.parse(String.valueOf(row.get(5))));
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

    // Seats still open in this section for this event
    public static int seatsLeft(String eventName, Section section) {
        int sold = 0;
        for (Ticket t : loadAll()) {
            if (t.getEventName().equalsIgnoreCase(eventName) && t.getSection() == section) sold++;
        }
        return section.getCapacity() - sold;
    }

    // Fields in file order, ready for CsvWriter
    public List<Object> toRow() {
        return List.of(
                id,
                eventName,
                buyerName,
                section,
                price,
                purchaseDate);
    }

    // Readable form for printing, like: Ticket #1 | WrestleMania | Jane Smith | FLOOR | $150 | 2026-11-04
    @Override
    public String toString() {
        return String.format("Ticket #%d | %s | %s | %s | $%,d | %s",
                id, eventName, buyerName, section, price, purchaseDate);
    }
}