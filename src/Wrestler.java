import java.io.File;
import java.io.IOException;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

/**
 * A wrestler has id, legalName, ringName, brand and status.
 * Each one is a line in Data/Wrestlers.txt,
 * like 1,John Smith,The Hammer,RAW,ACTIVE.
 */
public class Wrestler {
    public static final String FILE = "Data/Wrestlers.txt";

    public enum Brand {
        RAW, SMACKDOWN, NXT;

        // NXT is developmental, everything else is main roster
        public boolean isMainRoster() {
            return this != NXT;
        }
    }

    public enum Status { ACTIVE, INJURED, RELEASED }

    private final int id;
    private final String legalName;
    private final String ringName;

    private Brand brand;
    private Status status;

    public Wrestler(int id, String legalName, String ringName, Brand brand, Status status) {
        // TODO: validate (names not blank, brand and status not null)
        this.id = id;
        this.legalName = legalName;
        this.ringName = ringName;
        this.brand = brand;
        this.status = status;
    }

    public int getId() {
        return id;
    }

    public String getLegalName() {
        return legalName;
    }

    public String getRingName() {
        return ringName;
    }

    public Brand getBrand() {
        return brand;
    }

    public Status getStatus() {
        return status;
    }

    public void setBrand(Brand brand) {
        this.brand = brand;
    }

    // Rewrites the whole file. Use after changing a wrestler from loadAll()
    public static void saveAll(List<Wrestler> wrestlers) throws IOException {
        List<List<Object>> rows = new ArrayList<>();
        for (Wrestler w : wrestlers) {
            rows.add(w.toRow());
        }
        CsvWriter.writeAll(FILE, rows);
    }


    // Every wrestler in the file. Empty list if the file doesn't exist yet
    public static List<Wrestler> loadAll() {
        List<Wrestler> wrestlers = new ArrayList<>();
        if (!new File(FILE).exists()) return wrestlers; // CsvParser prints an error for missing files

        for (ArrayList<Object> row : CsvParser.parsecsv(FILE)) {
            if (row.get(0) == null) continue; // blank line
            wrestlers.add(fromRow(row));
        }
        return wrestlers;
    }

    // The wrestler with this ID, or null if there isn't one
    public static Wrestler findById(int id) {
       for (Wrestler w : loadAll()) {
            if (w.getId() == id) return w;
        }
        return null;
    }

    // Builds a wrestler from one CsvParser row, like [1, John Smith, The Hammer, RAW, ACTIVE]
    public static Wrestler fromRow(List<Object> row) {
         if (row.size() != 5) {
            throw new IllegalArgumentException("Bad contract row: " + row);
        }
        return new Wrestler(
                Integer.parseInt(String.valueOf(row.get(0))),
                String.valueOf(row.get(1)),
                String.valueOf(row.get(2)),
                Brand.valueOf(String.valueOf(row.get(3))),
                Status.valueOf(String.valueOf(row.get(4)))
        );    
    }

    // Fields in file order, ready for CsvWriter
    public List<Object> toRow() {
        return List.of(
                id,
                legalName,
                ringName,
                brand,
                status);
    }

    @Override
    public String toString() {
        return String.format("WrestlerID #%d | LegalName %s | StageName %s | Brand %s | Status %s",
                 id,legalName, ringName, brand.toString(), status.toString());
    }
}
