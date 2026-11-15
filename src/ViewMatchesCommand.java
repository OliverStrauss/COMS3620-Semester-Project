import java.util.Comparator;
import java.util.List;
import java.util.Scanner;

// Every match, grouped by event
public class ViewMatchesCommand implements Command {
    private final CreativeController controller;

    public ViewMatchesCommand(CreativeController controller) {
        this.controller = controller;
    }

    @Override
    public String getName() {
        return "View Matches";
    }

    @Override
    public void execute(Scanner in) {
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
}
