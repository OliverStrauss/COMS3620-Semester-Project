import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Scanner;

// UI for Creative: asks for input and prints results. CreativeController does the work.
public class CreativeDepartment extends Department {
    private final CreativeController controller = new CreativeController();

    public CreativeDepartment() {
        super("Creative");
    }

    @Override
    protected List<String> getUseCases() {
        return List.of("Book Match", "View Matches", "View Storylines");
    }

    @Override
    protected void runUseCase(int choice, Scanner in) {
        switch (choice) {
            case 0 -> bookMatch(in);
            case 1 -> viewMatches();
            case 2 -> viewStorylines();
        }
    }

    // Book Match use case. Step numbers match the spec
    private void bookMatch(Scanner in) {
        // 2-3. Pick an upcoming scheduled event
        List<Event> events = new ArrayList<>(controller.getBookableEvents());
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
                        e.getType(), e.getBrand(), e.getDate(), controller.matchesBooked(e.getId()), e.getType().getMaxMatches());
            }
            event = events.get(askIndex(in, "Event", labels));
            String problem = controller.eventProblem(event.getId());
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
        List<Wrestler> eligible = controller.eligibleWrestlers(event);
        Wrestler w1 = askOption(in, "First wrestler", eligible.toArray(new Wrestler[0]));
        eligible.remove(w1);
        Wrestler w2 = askOption(in, "Opponent", eligible.toArray(new Wrestler[0]));

        // 8-9. Storyline, new storyline, or one-off
        List<Storyline> active = controller.getActiveStorylines();
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
            Storyline existing = controller.findActiveStoryline(name);
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
        System.out.println(controller.bookMatch(event.getId(), w1.getId(), w2.getId(), type,
                storylineId, newStorylineName, winnerId));
    }

    // Every match, grouped by event
    private void viewMatches() {
        List<Match> matches = controller.getMatches();
        if (matches.isEmpty()) {
            System.out.println("No matches yet.");
            return;
        }
        matches.sort(Comparator.comparingInt(Match::getEventId));
        int lastEventId = -1;
        for (Match m : matches) {
            if (m.getEventId() != lastEventId) {
                lastEventId = m.getEventId();
                Event e = controller.getEvent(lastEventId);
                System.out.println("\n" + (e == null ? "Event #" + lastEventId : e.getName() + " (" + e.getDate() + ")") + ":");
            }
            System.out.println("  " + m);
        }
    }

    private void viewStorylines() {
        List<Storyline> storylines = controller.getStorylines();
        if (storylines.isEmpty()) {
            System.out.println("No storylines yet.");
        }
        for (Storyline s : storylines) {
            System.out.println(s + " | " + s.matchCount() + " match(es)");
        }
    }
}
