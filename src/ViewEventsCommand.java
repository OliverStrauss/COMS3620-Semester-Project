import java.util.Scanner;

// Command for the "View Events" use case. Receiver: LiveEvents
public class ViewEventsCommand implements Command {
    private final LiveEvents liveEvents;

    public ViewEventsCommand(LiveEvents liveEvents) {
        this.liveEvents = liveEvents;
    }

    @Override
    public String getName() {
        return "View Events";
    }

    @Override
    public void execute(Scanner in) {
        liveEvents.viewEvents();
    }
}
