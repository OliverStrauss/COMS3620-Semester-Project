import java.io.File;
import java.io.IOException;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Scanner;

// Creative department: Book Match, View Matches and View Storylines.
public class CreativeDepartment extends Department {
    public CreativeDepartment() {
        super("Creative");
        // Menu order. Each command calls back into this department (the receiver)
        addCommand(new BookMatchCommand(this));
        addCommand(new ViewMatchesCommand(this));
        addCommand(new ViewStorylinesCommand(this));
    }

    // Book Match use case. Step numbers match the spec
    // Package-private so BookMatchCommand can run it
    void bookMatch(Scanner in) {
        // 2-3. Pick an upcoming scheduled event
        List<Event> events = new ArrayList<>(Event.getBookable());
        if (events.isEmpty()) {
            System.out.println("No upcoming scheduled events.");
            return;
        }
        Event event;
        while (true) {
            String[] labels = new String[events.size()];
            for (int i = 0; i < labels.length; i++) {
                Event e = events.get(i);
                labels[i] = String.format("#%d %s | %s | %s | %s | %d/%d matches", e.getId(), e.getName(),
                        e.getType(), e.getBrand(), e.getDate(), Match.forEvent(e.getId()).size(), e.getType().getMaxMatches());
            }
            event = events.get(askIndex(in, "Event", labels));
            String problem = eventProblem(event.getId());
            if (problem == null) break;
            System.out.println(problem);
            if (!event.isCardFull()) return; // 4a. not enough wrestlers ends the use case
            events.remove(event); // 3b. full card: list the others again
            if (events.isEmpty()) {
                System.out.println("Every upcoming event's card is full.");
                return;
            }
        }

        // 4-7. Pick two eligible wrestlers
        List<Wrestler> eligible = eligibleWrestlers(event);
        Wrestler w1 = askOption(in, "First wrestler", eligible.toArray(new Wrestler[0]));
        eligible.remove(w1);
        Wrestler w2 = askOption(in, "Opponent", eligible.toArray(new Wrestler[0]));

        // 8-9. Storyline, new storyline, or one-off
        List<Storyline> active = Storyline.getActive();
        String[] storyOptions = new String[active.size() + 2];
        for (int i = 0; i < active.size(); i++) {
            storyOptions[i] = active.get(i).getName();
        }
        storyOptions[active.size()] = "Start a new storyline";
        storyOptions[active.size() + 1] = "One-off match";
        int pick = askIndex(in, "Storyline", storyOptions);

        Integer storylineId = null;
        String newStorylineName = null;
        String storyLabel = "One-off";
        if (pick < active.size()) {
            storylineId = active.get(pick).getId();
            storyLabel = active.get(pick).getName();
        } else if (pick == active.size()) {
            String name = askText(in, "Storyline name: ", null);
            Storyline existing = Storyline.findActiveByName(name);
            if (existing != null) { // 9c
                System.out.println("\"" + existing.getName() + "\" already exists, using it.");
                storylineId = existing.getId();
                storyLabel = existing.getName();
            } else {
                newStorylineName = name;
                storyLabel = name + " (new)";
            }
        }

        // 10-11. Match type
        Match.Type type = askOption(in, "Match type", Match.Type.values());

        // 12-13. Planned finish
        int finish = askIndex(in, "Finish",
                new String[]{w1.getRingName() + " wins", w2.getRingName() + " wins", "No contest"});
        int winnerId = new int[]{w1.getId(), w2.getId(), 0}[finish];

        // 14-15. Show the match and confirm
        System.out.println("\nEvent:     " + event.getName() + " (" + event.getDate() + ")");
        System.out.println("Match:     " + w1.getRingName() + " vs " + w2.getRingName() + ", " + type);
        System.out.println("Storyline: " + storyLabel);
        System.out.println("Finish:    " + (winnerId == 0 ? "No contest" : (winnerId == w1.getId() ? w1 : w2).getRingName() + " wins"));
        if (askOption(in, "Confirm booking", new String[]{"Yes", "No"}).equals("No")) {
            System.out.println("Booking cancelled.");
            return;
        }

        // 16. Save
        System.out.println(saveMatch(event.getId(), w1.getId(), w2.getId(), type,
                storylineId, newStorylineName, winnerId));
    }

    // Every match, grouped by event
    // Package-private so ViewMatchesCommand can run it
    void viewMatches() {
        List<Match> matches = Match.loadAll();
        if (matches.isEmpty()) {
            System.out.println("No matches yet.");
            return;
        }
        matches.sort(Comparator.comparingInt(Match::getEventId));
        int lastEventId = -1;
        for (Match m : matches) {
            if (m.getEventId() != lastEventId) {
                lastEventId = m.getEventId();
                Event e = Event.findById(lastEventId);
                System.out.println("\n" + (e == null ? "Event #" + lastEventId : e.getName() + " (" + e.getDate() + ")") + ":");
            }
            System.out.println("  " + m);
        }
    }

    // Package-private so ViewStorylinesCommand can run it
    void viewStorylines() {
        List<Storyline> storylines = Storyline.loadAll();
        if (storylines.isEmpty()) {
            System.out.println("No storylines yet.");
        }
        for (Storyline s : storylines) {
            System.out.println(s + " | " + s.matchCount() + " match(es)");
        }
    }

    // Why no match can be booked on this event, or null if one can
    private String eventProblem(int eventId) {
        Event event = Event.findById(eventId);
        if (event == null) return "No event with ID " + eventId + ".";
        if (event.getStatus() != Event.Status.SCHEDULED || !event.isUpcoming()) {
            return event.getName() + " isn't an upcoming scheduled event.";
        }
        if (event.isCardFull()) {
            return event.getName() + "'s card is full (" + event.getType().getMaxMatches() + " matches).";
        }
        if (eligibleWrestlers(event).size() < 2) {
            return event.getName() + " has fewer than two eligible wrestlers, so it can't be booked yet.";
        }
        return null;
    }

    // Wrestlers who could be put in a match on this event
    private List<Wrestler> eligibleWrestlers(Event e) {
        List<Wrestler> eligible = new ArrayList<>();
        for (Wrestler w : Wrestler.loadAll()) {
            if (isEligible(e, w)) eligible.add(w);
        }
        return eligible;
    }

    // Each check asks the class that owns the data (Information Expert)
    private boolean isEligible(Event e, Wrestler w) {
        return e.allowsBrand(w.getBrand())
                && w.canCompete()
                && Contract.hasActiveContract(w.getId(), e.getDate())
                && !Match.isBooked(w.getId(), e.getId());
    }

    // Books a match. storylineId null or 0 means one-off, unless newStorylineName is given,
    // which starts that storyline (or reuses the ACTIVE one with the same name).
    // winnerId 0 means no contest
    private String saveMatch(int eventId, int w1, int w2, Match.Type type, Integer storylineId,
                            String newStorylineName, int winnerId) {
        // Check every rule again: the UI's lists may be stale by now
        String problem = eventProblem(eventId);
        if (problem != null) return problem;
        Event event = Event.findById(eventId);

        if (w1 == w2) return "A wrestler can't face themselves.";
        for (int id : new int[]{w1, w2}) {
            Wrestler w = Wrestler.findById(id);
            if (w == null) return "No wrestler with ID " + id + ".";
            if (!isEligible(event, w)) return w.getRingName() + " can't be booked on " + event.getName() + ".";
        }
        if (winnerId != 0 && winnerId != w1 && winnerId != w2) {
            return "Winner must be one of the two wrestlers, or no contest.";
        }

        int storyId = 0; // one-off
        String note = "";
        if (newStorylineName != null) {
            if (newStorylineName.isBlank()) return "Storyline needs a name.";
            Storyline existing = Storyline.findActiveByName(newStorylineName);
            if (existing != null) {
                storyId = existing.getId();
                note = "Using existing " + existing + "\n";
            }
        } else if (storylineId != null && storylineId != 0) {
            Storyline s = Storyline.findById(storylineId);
            if (s == null || s.getStatus() != Storyline.Status.ACTIVE) {
                return "No active storyline with ID " + storylineId + ".";
            }
            storyId = storylineId;
        }

        // Everything checked. Get IDs last, so bad input never uses one up
        if (newStorylineName != null && storyId == 0) {
            Storyline s = new Storyline(IdGenerator.nextId(Storyline.FILE), newStorylineName.trim(), Storyline.Status.ACTIVE);
            try {
                CsvWriter.appendRow(Storyline.FILE, s.toRow());
            } catch (IOException e) {
                return "Could not save storyline: " + e.getMessage();
            }
            storyId = s.getId();
            note = "Started " + s + "\n";
        }

        Match match = new Match(IdGenerator.nextId(Match.FILE), eventId, w1, w2, type, storyId, winnerId, Match.Status.BOOKED);
        try {
            CsvWriter.appendRow(Match.FILE, match.toRow());
        } catch (IOException e) {
            // A storyline started above stays saved; it's ACTIVE and can be booked again
            return note + "Could not save match: " + e.getMessage();
        }
        return note + "Booked " + match;
    }

    // For testing. Data paths are relative and this writes to them, so run it from an empty folder:
    //   mkdir t && cd t && java -cp ../out CreativeDepartment
    public static void main(String[] args) throws IOException {
        if (new File("Data").exists()) {
            throw new IllegalStateException("Run from an empty folder, this writes to Data/");
        }
        CreativeDepartment c = new CreativeDepartment();
        LocalDate today = LocalDate.now();
        LocalDate soon = today.plusDays(7);

        Wrestler.saveAll(List.of(
                new Wrestler(1, "A", "Raw One", Wrestler.Brand.RAW, Wrestler.Status.ACTIVE, true),
                new Wrestler(2, "B", "Raw Two", Wrestler.Brand.RAW, Wrestler.Status.ACTIVE, true),
                new Wrestler(3, "C", "Smack One", Wrestler.Brand.SMACKDOWN, Wrestler.Status.ACTIVE, true),
                new Wrestler(4, "D", "Hurt", Wrestler.Brand.RAW, Wrestler.Status.INJURED, false),
                new Wrestler(5, "E", "No Deal", Wrestler.Brand.RAW, Wrestler.Status.ACTIVE, true),
                new Wrestler(6, "F", "Rookie", Wrestler.Brand.NXT, Wrestler.Status.ACTIVE, true),
                new Wrestler(7, "G", "Smack Two", Wrestler.Brand.SMACKDOWN, Wrestler.Status.ACTIVE, true)));
        List<Contract> contracts = new ArrayList<>();
        for (int id : new int[]{1, 2, 3, 4, 6, 7}) { // 5 has no contract
            contracts.add(new Contract(id, id, 1000, today, today.plusYears(1)));
        }
        Contract.saveAll(contracts);
        Event.saveAll(List.of(
                new Event(1, "Raw", Event.Type.WEEKLY_TV, Wrestler.Brand.RAW, soon, "Boston", 0, null, 0, Event.Status.SCHEDULED),
                new Event(2, "Big PLE", Event.Type.PREMIUM_LIVE_EVENT, Wrestler.Brand.RAW, soon, "Boston", 0, null, 0, Event.Status.SCHEDULED),
                new Event(3, "SmackDown", Event.Type.WEEKLY_TV, Wrestler.Brand.SMACKDOWN, soon, "Chicago", 0, null, 0, Event.Status.SCHEDULED)));

        check(ids(c.eligibleWrestlers(Event.findById(1))).equals(List.of(1, 2)), "weekly TV: own brand only, injured and no contract left out");
        check(ids(c.eligibleWrestlers(Event.findById(2))).equals(List.of(1, 2, 3, 7)), "PLE: whole main roster, no NXT");
        check(c.eventProblem(99).contains("No event"), "missing event");
        check(c.saveMatch(1, 1, 1, Match.Type.SINGLES, null, null, 1).contains("themselves"), "same wrestler twice rejected");
        check(c.saveMatch(1, 1, 4, Match.Type.SINGLES, null, null, 1).contains("can't be booked"), "injured wrestler rejected");

        check(c.saveMatch(1, 1, 2, Match.Type.SINGLES, null, "Test Feud", 1).contains("Booked"), "match booked");
        check(Match.isBooked(1, 1) && Storyline.loadAll().size() == 1, "match and new storyline saved");
        check(c.eventProblem(1).contains("fewer than two"), "booked wrestlers leave the pool");
        check(c.saveMatch(1, 1, 2, Match.Type.SINGLES, null, null, 1).contains("fewer than two"), "already booked refused");

        c.saveMatch(2, 1, 2, Match.Type.LADDER, null, "test feud", 0);
        check(Storyline.loadAll().size() == 1, "same storyline name (any case) is created once");
        check(Match.findById(2).getStorylineId() == 1 && Match.findById(2).getWinnerId() == 0, "NO_CONTEST saves winnerId 0");
        c.saveMatch(2, 3, 7, Match.Type.SINGLES, null, null, 3);
        check(Match.findById(3).getStorylineId() == 0, "one-off saves storylineId 0");
        check(c.saveMatch(3, 3, 7, Match.Type.SINGLES, null, null, 1).contains("Winner"), "winner must be in the match");

        for (int i = 0; i < Event.Type.WEEKLY_TV.getMaxMatches(); i++) {
            CsvWriter.appendRow(Match.FILE, new Match(10 + i, 3, 100 + 2 * i, 101 + 2 * i,
                    Match.Type.SINGLES, 0, 0, Match.Status.BOOKED).toRow());
        }
        check(c.eventProblem(3).contains("full"), "card full");
        check(c.saveMatch(3, 3, 7, Match.Type.SINGLES, null, null, 3).contains("full"), "full card refused");

        System.out.println("CreativeDepartment ok");
    }

    private static List<Integer> ids(List<Wrestler> wrestlers) {
        List<Integer> ids = new ArrayList<>();
        for (Wrestler w : wrestlers) ids.add(w.getId());
        return ids;
    }

    private static void check(boolean condition, String what) {
        if (!condition) throw new AssertionError(what);
    }
}
