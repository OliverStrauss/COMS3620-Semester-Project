import java.util.Scanner;

// Command for the "Create Live Event" use case. Receiver: LiveEvents
public class CreateLiveEventCommand implements Command {
    private final LiveEvents liveEvents;

    public CreateLiveEventCommand(LiveEvents liveEvents) {
        this.liveEvents = liveEvents;
    }

    @Override
    public String getName() {
        return "Create Live Event";
    }

    @Override
    public void execute(Scanner in) {
        liveEvents.createLiveEvent(in);
    }
}
