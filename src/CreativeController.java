import java.io.File;
import java.io.IOException;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

// GRASP Controller for Creative: receives each system operation from the UI
// and coordinates Event, Wrestler, Contract, Storyline, Match and saving. It never reads input or prints.
// Operations return the message to show the user, including any save failure.
public class CreativeController {

    public List<Event> getBookableEvents() {
        return Event.getBookable();
    }

    // The event with this ID, or null if there isn't one
    public Event getEvent(int id) {
        return Event.findById(id);
    }

    public List<Match> getMatches() {
        return Match.loadAll();
    }

    public List<Storyline> getStorylines() {
        return Storyline.loadAll();
    }

    public List<Storyline> getActiveStorylines() {
        return Storyline.getActive();
    }

    // The ACTIVE storyline with this name (ignoring case), or null if there isn't one
    public Storyline findActiveStoryline(String name) {
        return Storyline.findActiveByName(name);
    }

    // How many BOOKED matches are on this event's card
    public int matchesBooked(int eventId) {
        return Match.forEvent(eventId).size();
    }

    // Why no match can be booked on this event, or null if one can
    public String eventProblem(int eventId) {
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
    public List<Wrestler> eligibleWrestlers(Event e) {
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
    public String bookMatch(int eventId, int w1, int w2, Match.Type type, Integer storylineId,
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
    //   mkdir t && cd t && java -cp ../out CreativeController
    public static void main(String[] args) throws IOException {
        if (new File("Data").exists()) {
            throw new IllegalStateException("Run from an empty folder, this writes to Data/");
        }
        CreativeController c = new CreativeController();
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

        check(ids(c.eligibleWrestlers(c.getEvent(1))).equals(List.of(1, 2)), "weekly TV: own brand only, injured and no contract left out");
        check(ids(c.eligibleWrestlers(c.getEvent(2))).equals(List.of(1, 2, 3, 7)), "PLE: whole main roster, no NXT");
        check(c.eventProblem(99).contains("No event"), "missing event");
        check(c.bookMatch(1, 1, 1, Match.Type.SINGLES, null, null, 1).contains("themselves"), "same wrestler twice rejected");
        check(c.bookMatch(1, 1, 4, Match.Type.SINGLES, null, null, 1).contains("can't be booked"), "injured wrestler rejected");

        check(c.bookMatch(1, 1, 2, Match.Type.SINGLES, null, "Test Feud", 1).contains("Booked"), "match booked");
        check(Match.isBooked(1, 1) && Storyline.loadAll().size() == 1, "match and new storyline saved");
        check(c.eventProblem(1).contains("fewer than two"), "booked wrestlers leave the pool");
        check(c.bookMatch(1, 1, 2, Match.Type.SINGLES, null, null, 1).contains("fewer than two"), "already booked refused");

        c.bookMatch(2, 1, 2, Match.Type.LADDER, null, "test feud", 0);
        check(Storyline.loadAll().size() == 1, "same storyline name (any case) is created once");
        check(Match.findById(2).getStorylineId() == 1 && Match.findById(2).getWinnerId() == 0, "NO_CONTEST saves winnerId 0");
        c.bookMatch(2, 3, 7, Match.Type.SINGLES, null, null, 3);
        check(Match.findById(3).getStorylineId() == 0, "one-off saves storylineId 0");
        check(c.bookMatch(3, 3, 7, Match.Type.SINGLES, null, null, 1).contains("Winner"), "winner must be in the match");

        for (int i = 0; i < Event.Type.WEEKLY_TV.getMaxMatches(); i++) {
            CsvWriter.appendRow(Match.FILE, new Match(10 + i, 3, 100 + 2 * i, 101 + 2 * i,
                    Match.Type.SINGLES, 0, 0, Match.Status.BOOKED).toRow());
        }
        check(c.eventProblem(3).contains("full"), "card full");
        check(c.bookMatch(3, 3, 7, Match.Type.SINGLES, null, null, 3).contains("full"), "full card refused");

        System.out.println("CreativeController ok");
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
