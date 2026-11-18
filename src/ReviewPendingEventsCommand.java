import java.util.Scanner;

// Command for the "Review Pending Events (Finance)" use case. Receiver: LiveEvents
public class ReviewPendingEventsCommand implements Command {
    private final LiveEvents liveEvents;

    public ReviewPendingEventsCommand(LiveEvents liveEvents) {
        this.liveEvents = liveEvents;
    }

    @Override
    public String getName() {
        return "Review Pending Events (Finance)";
    }

    @Override
    public void execute(Scanner in) {
        liveEvents.reviewPendingEvents(in);
    }
}
