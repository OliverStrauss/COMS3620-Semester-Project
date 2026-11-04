// @author Caleb Sanchez

import java.io.File;
import java.io.IOException;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;

/**
 * An event has id, name, type, brand, date, city, venueId, showId, costCents and status.
 * Each is a line within Data/Events.txt
 * venueId is 0 when no venue is held. showId is empty unless a TV show tapes it.
 */
public class Event {
    public static final String FILE = "Data/Events.txt";

    public enum Type {
        WEEKLY_TV(8000, 6_500_000),
        PREMIUM_LIVE_EVENT(15000, 10_000_000),
        HOUSE_SHOW(2000, 1_000_000);

        private final int minCapacity;
        private final long budgetCents;

        Type(int minCapacity, long budgetCents) {
            this.minCapacity = minCapacity;
            this.budgetCents = budgetCents;
        }

        public int getMinCapacity() {
            return minCapacity;
        }

        public long getBudgetCents() {
            return budgetCents;
        }
    }

    public enum Status { DRAFT, PENDING_APPROVAL, SCHEDULED }

    private final int id;
    private final String name;
    private final Type type;
    private final Wrestler.Brand brand;
    private final LocalDate date;
    private final String city;
    private final Integer showId;
    private int venueId;
    private long costCents;
    private Status status;

    public Event(int id, String name, Type type, Wrestler.Brand brand, LocalDate date, String city,
                 int venueId, Integer showId, long costCents, Status status) {
        this.id = id;
        this.name = name;
        this.type = type;
        this.brand = brand;
        this.date = date;
        this.city = city;
        this.venueId = venueId;
        this.showId = showId;
        this.costCents = costCents;
        this.status = status;
    }

    public int getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public LocalDate getDate() {
        return date;
    }

    public Integer getShowId() {
        return showId;
    }

    public Status getStatus() {
        return status;
    }

    public boolean isUpcoming() {
        return date.isAfter(LocalDate.now());
    }

    public boolean hasPassed() {
        return date.isBefore(LocalDate.now());
    }

    public void schedule() {
        status = Status.SCHEDULED;
    }

    // Finance rejected it, so give the venue back
    public void revertToDraft() {
        status = Status.DRAFT;
        venueId = 0;
        costCents = 0;
    }

    public static boolean brandHasEventOn(Wrestler.Brand brand, LocalDate date) {
        for (Event e : loadAll()) {
            if (e.status != Status.DRAFT && e.brand == brand && e.date.equals(date)) return true;
        }
        return false;
    }

    public static boolean venueBookedOn(int venueId, LocalDate date) {
        for (Event e : loadAll()) {
            if (e.venueId == venueId && e.date.equals(date)) return true;
        }
        return false;
    }

    public static List<Event> getUpcomingForShow(int showId) {
        List<Event> upcoming = new ArrayList<>();
        for (Event e : loadAll()) {
            if (e.getShowId() != null && e.getShowId() == showId && e.isUpcoming()) {
                upcoming.add(e);
            }
        }
        upcoming.sort(Comparator.comparing(Event::getDate));
        return upcoming;
    }

    public static Event findById(int id) {
        for (Event e : loadAll()) {
            if (e.getId() == id) return e;
        }
        return null;
    }

    public static List<Event> loadAll() {
        List<Event> events = new ArrayList<>();
        if (!new File(FILE).exists()) return events;

        for (ArrayList<Object> row : CsvParser.parsecsv(FILE)) {
            if (row.get(0) == null) continue;
            events.add(fromRow(row));
        }
        return events;
    }

    public static void saveAll(List<Event> events) throws IOException {
        List<List<Object>> rows = new ArrayList<>();
        for (Event e : events) {
            rows.add(e.toRow());
        }
        CsvWriter.writeAll(FILE, rows);
    }

    public static Event fromRow(List<Object> row) {
        if (row.size() != 10) {
            throw new IllegalArgumentException("Bad event row in data: " + row);
        }
        Object showCell = row.get(7);
        return new Event(
            Integer.parseInt(String.valueOf(row.get(0))),
            String.valueOf(row.get(1)),
            Type.valueOf(String.valueOf(row.get(2))),
            Wrestler.Brand.valueOf(String.valueOf(row.get(3))),
            LocalDate.parse(String.valueOf(row.get(4))),
            String.valueOf(row.get(5)),
            Integer.parseInt(String.valueOf(row.get(6))),
            showCell == null ? null : Integer.valueOf(String.valueOf(showCell)),
            ((Number) row.get(8)).longValue(),
            Status.valueOf(String.valueOf(row.get(9)))
        );
    }

    // Arrays.asList because showId can be null
    public List<Object> toRow() {
        return Arrays.asList(id, name, type, brand, date, city, venueId, showId, costCents, status);
    }

    @Override
    public String toString() {
        return String.format("Event #%d | %s | %s | %s | %s | %s | Venue %s | Cost %s of %s budget | %s",
            id, name, type, brand, date, city, venueId == 0 ? "none" : "#" + venueId,
            Venue.dollars(costCents), Venue.dollars(type.getBudgetCents()), status
        );
    }
}
