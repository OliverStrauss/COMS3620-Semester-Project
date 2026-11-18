import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class IdGenerator {

    /**
     * Returns the next free ID for a data file: the highest ID in the first column, plus 1.
     * Returns 1 if the file is missing or empty.
     * @param path file path, like "Data/Contracts.txt"
     * @return next free ID
     */
    public static int nextId(String path) {
        File f = new File(path);
        if (!f.exists()) return 1; // CsvParser prints an error for missing files, so check first

        int max = 0;
        List<ArrayList<Object>> rows = CsvParser.parsecsv(f);
        for (int i = 0; i < rows.size(); i++) {
            Object first = rows.get(i).get(0);
            if (first == null) continue; // blank line
            if (i == 0 && first instanceof String) continue; // header row, like id,name,...
            if (!(first instanceof Integer id)) {
                // Fail loudly: skipping a bad row could hand out an ID that's already used
                throw new IllegalStateException("Bad ID in " + path + ": " + rows.get(i));
            }
            max = Math.max(max, id);
        }
        return max + 1;
    }

    //for testing
    public static void main(String args[]) throws IOException {
        String path = "idgenerator_test.csv";
        new File(path).delete();
        if (nextId(path) != 1) throw new AssertionError("missing file should give 1");

        CsvWriter.writeAll(path, List.of(List.of(1, "a"), List.of(5, "b"), List.of(3, "c")));
        if (nextId(path) != 6) throw new AssertionError("should be highest + 1");

        CsvWriter.writeAll(path, List.of(List.of("id", "name"), List.of(2, "a")));
        if (nextId(path) != 3) throw new AssertionError("should skip the header row");

        new File(path).delete();
        System.out.println("IdGenerator ok");
    }
}
