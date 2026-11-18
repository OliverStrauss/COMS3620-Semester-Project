import java.util.Scanner;

// Command for the "Resume Draft" use case. Receiver: LiveEvents
public class ResumeDraftCommand implements Command {
    private final LiveEvents liveEvents;

    public ResumeDraftCommand(LiveEvents liveEvents) {
        this.liveEvents = liveEvents;
    }

    @Override
    public String getName() {
        return "Resume Draft";
    }

    @Override
    public void execute(Scanner in) {
        liveEvents.resumeDraft(in);
    }
}
