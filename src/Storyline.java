import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * A storyline is a rivalry that builds over several shows. It has id, name and status.
 * Each one is a line in Data/Storylines.txt,
 * like 1,The Titan vs Dark Horse,ACTIVE.
 */
public class Storyline {
    public static final String FILE = "Data/Storylines.txt";

    public enum Status { ACTIVE, CONCLUDED }

    private final int id;
    private final String name;
    private final Status status;

    public Storyline(int id, String name, Status status) {
        if (name == null || name.isBlank()) {
            throw new IllegalArgumentException("Storyline needs a name");
        }
        this.id = id;
        this.name = name;
        this.status = status;
    }

    public int getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public Status getStatus() {
        return status;
    }

    // How many BOOKED matches belong to this storyline
    public int matchCount() {
        int count = 0;
        for (Match m : Match.loadAll()) {
            if (m.getStorylineId() == id && m.getStatus() == Match.Status.BOOKED) count++;
        }
        return count;
    }

    // Every storyline in the file. Empty list if the file doesn't exist yet
    public static List<Storyline> loadAll() {
        List<Storyline> storylines = new ArrayList<>();
        if (!new File(FILE).exists()) return storylines; // CsvParser prints an error for missing files
        for (ArrayList<Object> row : CsvParser.parsecsv(FILE)) {
            if (row.get(0) == null) continue; // blank line
            storylines.add(fromRow(row));
        }
        return storylines;
    }

    // The storyline with this ID, or null if there isn't one
    public static Storyline findById(int id) {
        for (Storyline s : loadAll()) {
            if (s.getId() == id) return s;
        }
        return null;
    }

    public static List<Storyline> getActive() {
        List<Storyline> active = new ArrayList<>();
        for (Storyline s : loadAll()) {
            if (s.status == Status.ACTIVE) active.add(s);
        }
        return active;
    }

    // The ACTIVE storyline with this name (ignoring case), or null if there isn't one
    public static Storyline findActiveByName(String name) {
        for (Storyline s : getActive()) {
            if (s.name.equalsIgnoreCase(name.trim())) return s;
        }
        return null;
    }

    // Rewrites the whole file. Use after changing a storyline from loadAll()
    public static void saveAll(List<Storyline> storylines) throws IOException {
        List<List<Object>> rows = new ArrayList<>();
        for (Storyline s : storylines) {
            rows.add(s.toRow());
        }
        CsvWriter.writeAll(FILE, rows);
    }

    // Builds a storyline from one CsvParser row, like [1, The Titan vs Dark Horse, ACTIVE]
    public static Storyline fromRow(List<Object> row) {
        if (row.size() != 3) {
            throw new IllegalArgumentException("Bad storyline row: " + row);
        }
        return new Storyline(
                Integer.parseInt(String.valueOf(row.get(0))),
                String.valueOf(row.get(1)),
                Status.valueOf(String.valueOf(row.get(2))));
    }

    // Fields in file order, ready for CsvWriter
    public List<Object> toRow() {
        return List.of(id, name, status);
    }

    @Override
    public String toString() {
        return String.format("Storyline #%d | %s | %s", id, name, status);
    }
}
