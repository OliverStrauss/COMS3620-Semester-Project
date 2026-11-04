// @author Caleb Sanchez

import java.io.File;
import java.io.IOException;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

/**
 * A venue has an id, a location, capacity, and rental rate.
 * Each is a line within Data/Venue.txt
 */
public class Venue {
    public static final String FILE = "Data/Venue.txt";

    private final int id;
    private final int capacity;
    private final String location;
    private final long rentalCents;

    public Venue(int id, int capacity, String location, long rentalCents) {
        this.id = id;
        this.capacity = capacity;
        this.location = location;
        this.rentalCents = rentalCents;
    }

    public int getId() {
        return id;
    }

    public int getCapacity() {
        return capacity;
    }

    public String getLocation() {
        return location;
    }

    public long getRentalPrice() {
        return rentalCents;
    }

    public boolean isAvailableOn(LocalDate date) {
        return !Event.venueBookedOn(id, date);
    }

    // The venue with this ID, or null if there isn't one
    public static Venue findById(int id) {
        for (Venue v : loadAll()) {
            if (v.getId() == id) return v;
        }
        return null;
    }

    public static List<Venue> findAvailable(String city, LocalDate date, int minCapacity) {
        List<Venue> matches = new ArrayList<>();
        for (Venue v : loadAll()) {
            if (v.location.equalsIgnoreCase(city) && v.capacity >= minCapacity && v.isAvailableOn(date)) {
                matches.add(v);
            }
        }
        return matches;
    }

    public static String dollars(long cents) {
        return String.format("$%,.2f", cents / 100.0);
    }

    public static void saveAll(List<Venue> venues) throws IOException {
        List<List<Object>> rows = new ArrayList<>();
        for (Venue v : venues) {
            rows.add(v.toRow());
        }
        CsvWriter.writeAll(FILE, rows);
    }

    public static List<Venue> loadAll() {
        List<Venue> venues = new ArrayList<>();
        if (!new File(FILE).exists()) return venues;

        for (ArrayList<Object> row : CsvParser.parsecsv(FILE)) {
            if (row.get(0) == null) continue;
            venues.add(fromRow(row));
        }
        return venues;
    }

    public List<Object> toRow() {
        return List.of(
            id,
            capacity,
            location,
            rentalCents);
    }

    public static Venue fromRow(List<Object> row) {
        if (row.size() != 4) {
            throw new IllegalArgumentException("Bad venue row in data: " + row);
        }
        return new Venue(
            Integer.parseInt(String.valueOf(row.get(0))),
            Integer.parseInt(String.valueOf(row.get(1))),
            String.valueOf(row.get(2)),
            ((Number) row.get(3)).longValue()
        );
    }

    @Override
    public String toString() {
        return String.format("VenueID #%d | Capacity %d | Location %s | Rental %s",
            id, capacity, location, dollars(rentalCents)
        );
    }
}
