import java.io.File;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;

/**
 * A weekly TV show, like Monday Night Raw.
 * Each one is a line in Data/Shows.txt,
 * like 1,Monday Night Raw,RAW,1,Monday,20:00,180.
 */
public class Show {
    public static final String FILE = "Data/Shows.txt";

    private final int id;
    private final String name;
    private final Wrestler.Brand brand;
    private final int networkId;
    private final String dayOfWeek;
    private final LocalTime startTime;
    private final int lengthMinutes;

    public Show(int id, String name, Wrestler.Brand brand, int networkId,
                String dayOfWeek, LocalTime startTime, int lengthMinutes) {
        this.id = id;
        this.name = name;
        this.brand = brand;
        this.networkId = networkId;
        this.dayOfWeek = dayOfWeek;
        this.startTime = startTime;
        this.lengthMinutes = lengthMinutes;
    }

    public int getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public Wrestler.Brand getBrand() {
        return brand;
    }

    public int getNetworkId() {
        return networkId;
    }

    public String getDayOfWeek() {
        return dayOfWeek;
    }

    public LocalTime getStartTime() {
        return startTime;
    }

    public int getLengthMinutes() {
        return lengthMinutes;
    }

    // The network this show airs on, or null if the ID isn't in Networks.txt
    public Network getNetwork() {
        return Network.findById(networkId);
    }

    // Every show in the file. Empty list if the file doesn't exist yet
    public static List<Show> loadAll() {
        List<Show> shows = new ArrayList<>();
        if (!new File(FILE).exists()) return shows; // CsvParser prints an error for missing files

        for (ArrayList<Object> row : CsvParser.parsecsv(FILE)) {
            if (row.get(0) == null) continue; // blank line
            shows.add(fromRow(row));
        }
        return shows;
    }

    // The show with this ID, or null if there isn't one
    public static Show findById(int id) {
        for (Show s : loadAll()) {
            if (s.getId() == id) return s;
        }
        return null;
    }

    // Builds a show from one CsvParser row, like [1, Monday Night Raw, RAW, 1, Monday, 20:00, 180]
    public static Show fromRow(List<Object> row) {
        if (row.size() != 7) {
            throw new IllegalArgumentException("Bad show row: " + row);
        }
        return new Show(
                Integer.parseInt(String.valueOf(row.get(0))),
                String.valueOf(row.get(1)),
                Wrestler.Brand.valueOf(String.valueOf(row.get(2))),
                Integer.parseInt(String.valueOf(row.get(3))),
                String.valueOf(row.get(4)),
                LocalTime.parse(String.valueOf(row.get(5))),
                Integer.parseInt(String.valueOf(row.get(6))));
    }

    // Fields in file order, ready for CsvWriter
    public List<Object> toRow() {
        return List.of(id, name, brand, networkId, dayOfWeek, startTime, lengthMinutes);
    }

    @Override
    public String toString() {
        Network network = getNetwork();
        String networkName = network == null ? "Network #" + networkId : network.getName();
        return String.format("Show #%d | %s (%s) | %s | %s %s, %d min",
                id, name, brand, networkName, dayOfWeek, startTime, lengthMinutes);
    }
}
