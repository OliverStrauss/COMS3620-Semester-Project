import java.io.File;
import java.util.ArrayList;
import java.util.List;

/**
 * A TV network that airs shows, like Netflix or USA Network.
 * Each one is a line in Data/Networks.txt, like 1,Netflix.
 */
public class Network {
    public static final String FILE = "Data/Networks.txt";

    private final int id;
    private final String name;

    public Network(int id, String name) {
        this.id = id;
        this.name = name;
    }

    public int getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    // Every network in the file. Empty list if the file doesn't exist yet
    public static List<Network> loadAll() {
        List<Network> networks = new ArrayList<>();
        if (!new File(FILE).exists()) return networks; // CsvParser prints an error for missing files

        for (ArrayList<Object> row : CsvParser.parsecsv(FILE)) {
            if (row.get(0) == null) continue; // blank line
            networks.add(fromRow(row));
        }
        return networks;
    }

    // The network with this ID, or null if there isn't one
    public static Network findById(int id) {
        for (Network n : loadAll()) {
            if (n.getId() == id) return n;
        }
        return null;
    }

    // Builds a network from one CsvParser row, like [1, Netflix]
    public static Network fromRow(List<Object> row) {
        if (row.size() != 2) {
            throw new IllegalArgumentException("Bad network row: " + row);
        }
        return new Network(
                Integer.parseInt(String.valueOf(row.get(0))),
                String.valueOf(row.get(1)));
    }

    // Fields in file order, ready for CsvWriter
    public List<Object> toRow() {
        return List.of(id, name);
    }

    @Override
    public String toString() {
        return String.format("Network #%d | %s", id, name);
    }
}
