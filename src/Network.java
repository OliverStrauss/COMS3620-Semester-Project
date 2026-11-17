import java.io.File;
import java.util.ArrayList;
import java.util.List;

/**
 * A TV network that airs shows, like Netflix or USA Network.
 * Each one is a line in Data/Networks.txt, like 1,Netflix,2000000.
 * targetViewers is the audience the network expects per broadcast (0 = no target).
 */
public class Network {
    public static final String FILE = "Data/Networks.txt";

    private final int id;
    private final String name;
    private final int targetViewers;

    public Network(int id, String name, int targetViewers) {
        this.id = id;
        this.name = name;
        this.targetViewers = targetViewers;
    }

    public int getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public int getTargetViewers() {
        return targetViewers;
    }

    // viewers as a whole percent of the target, like 105. 0 if there's no target.
    // long math so big audiences don't overflow when multiplied by 100
    public int percentOfTarget(int viewers) {
        if (targetViewers == 0) return 0;
        return (int) ((long) viewers * 100 / targetViewers);
    }

    public boolean meetsTarget(int viewers) {
        return viewers >= targetViewers;
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

    // Builds a network from one CsvParser row, like [1, Netflix, 2000000].
    // Old 2-column rows still load, with no target (0)
    public static Network fromRow(List<Object> row) {
        if (row.size() != 2 && row.size() != 3) {
            throw new IllegalArgumentException("Bad network row: " + row);
        }
        Object targetCell = row.size() == 3 ? row.get(2) : null;
        return new Network(
                Integer.parseInt(String.valueOf(row.get(0))),
                String.valueOf(row.get(1)),
                targetCell == null ? 0 : Integer.parseInt(String.valueOf(targetCell)));
    }

    // Fields in file order, ready for CsvWriter
    public List<Object> toRow() {
        return List.of(id, name, targetViewers);
    }

    @Override
    public String toString() {
        return String.format("Network #%d | %s", id, name);
    }
}
