import java.time.LocalDate;
import java.util.List;

/**
 * A contract has id, wrestlerId, yearlySalary, startDate and endDate.
 * Each one is a line in Data/Contracts.txt,
 * like 1,1,2500000,2026-11-01,2029-08-15.
 */
public class Contract {
    public static final String FILE = "Data/Contracts.txt";

    private final int id;
    private final int wrestlerId;
    private final long yearlySalary;
    private final LocalDate startDate;
    private final LocalDate endDate;

    public Contract(int id, int wrestlerId, long yearlySalary, LocalDate startDate, LocalDate endDate) {
        if (yearlySalary <= 0) {
            throw new IllegalArgumentException("Salary must be positive");
        }
        if (startDate == null || endDate == null) {
            throw new IllegalArgumentException("Start and end dates are required");
        }
        if (!endDate.isAfter(startDate)) {
            throw new IllegalArgumentException("End date must be after start date");
        }
        this.id = id;
        this.wrestlerId = wrestlerId;
        this.yearlySalary = yearlySalary;
        this.startDate = startDate;
        this.endDate = endDate;
    }

    public int getId() {
        return id;
    }

    public int getWrestlerId() {
        return wrestlerId;
    }

    public long getYearlySalary() {
        return yearlySalary;
    }

    public LocalDate getStartDate() {
        return startDate;
    }

    public LocalDate getEndDate() {
        return endDate;
    }

    //TRUE if date is NOT before start AND before end
    public boolean isActiveOn(LocalDate date) {
        return !date.isBefore(startDate) && date.isBefore(endDate);
    }

    // Builds a contract from one CsvParser row, like [1, 1, 2500000, 2026-11-01, 2029-08-15]
    // ponytail: CsvParser guesses types, so a salary over Integer.MAX_VALUE comes back as a Double
    // and won't parse here; switch to raw String cells if salaries get that big
    public static Contract fromRow(List<Object> row) {
        if (row.size() != 5) {
            throw new IllegalArgumentException("Bad contract row: " + row);
        }
        return new Contract(
                Integer.parseInt(String.valueOf(row.get(0))),
                Integer.parseInt(String.valueOf(row.get(1))),
                Long.parseLong(String.valueOf(row.get(2))),
                LocalDate.parse(String.valueOf(row.get(3))),
                LocalDate.parse(String.valueOf(row.get(4))));
    }

    // Fields in file order, ready for CsvWriter
    public List<Object> toRow() {
        return List.of(
                id,
                wrestlerId,
                yearlySalary,
                startDate,
                endDate);
    }
}
