import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.ArrayList;
import java.util.List;

public class CsvWriter {

    /**
     * Turns one row of values into a CSV line (no trailing newline).
     * Fields with commas or quotes get wrapped in quotes, and inner quotes are doubled,
     * so CsvParser reads them back the same. null becomes an empty cell.
     * @param fields values for the row, written with toString()
     * @return the CSV line
     */
    public static String toCsvLine(List<?> fields) {
        StringBuilder line = new StringBuilder();
        for (int i = 0; i < fields.size(); i++) {
            if (i > 0) line.append(',');
            line.append(escape(fields.get(i)));
        }
        return line.toString();
    }
    //Used as helper for other functions
    private static String escape(Object value) {
        if (value == null) return "";
        String s = value.toString();
        // CsvParser reads one line per row, so a newline inside a field can't be read back
        if (s.contains("\n") || s.contains("\r")) {
            throw new IllegalArgumentException("CSV field can't contain a newline: " + s);
        }
        if (s.contains(",") || s.contains("\"")) {
            return "\"" + s.replace("\"", "\"\"") + "\"";
        }
        return s;
    }

    /**
     * Adds one row to the end of the file. Creates the file if it doesn't exist.
     * @param path file path, like "Data/Contracts.txt"
     * @param fields values for the row
     */
    public static void appendRow(String path, List<?> fields) throws IOException {
        Path p = Path.of(path);
        String text = toCsvLine(fields) + "\n";
        if (Files.exists(p)) {
            // If someone hand-edited the file and left no newline at the end, don't glue rows together
            String existing = Files.readString(p);
            if (!existing.isEmpty() && !existing.endsWith("\n")) {
                text = "\n" + text;
            }
        } else if (p.getParent() != null) {
            Files.createDirectories(p.getParent());
        }
        Files.writeString(p, text, StandardOpenOption.CREATE, StandardOpenOption.APPEND);
    }

    /**
     * Replaces the whole file with these rows. Use for edits and deletes.
     * @param path file path, like "Data/Contracts.txt"
     * @param rows every row the file should contain
     */
    public static void writeAll(String path, List<? extends List<?>> rows) throws IOException {
        Path p = Path.of(path);
        if (p.getParent() != null) {
            Files.createDirectories(p.getParent());
        }
        StringBuilder text = new StringBuilder();
        for (List<?> row : rows) {
            text.append(toCsvLine(row)).append('\n');
        }
        Files.writeString(p, text);
    }

    //for testing:
    public static void main(String args[]) throws IOException {
        String path = "csvwriter_test.csv";
        List<List<Object>> rows = List.of(
                List.of(1, "John Doe", "Regular text"),
                List.of(2, "Smith, Jane", "Enjoys \"automation\" testing"));
        writeAll(path, rows);
        appendRow(path, List.of(3, "Bob", "appended"));

        ArrayList<ArrayList<Object>> read = CsvParser.parsecsv(path);
        System.out.println(read);
        if (read.size() != 3
                || !read.get(1).get(1).equals("Smith, Jane")
                || !read.get(1).get(2).equals("Enjoys \"automation\" testing")
                || !read.get(2).get(2).equals("appended")) {
            throw new AssertionError("Round trip failed: " + read);
        }
        Files.delete(Path.of(path));
        System.out.println("CsvWriter round trip ok");
    }
}
