import java.util.Scanner;

// Command for the "Schedule Live Broadcast" use case. Receiver: TVShows
public class ScheduleLiveBroadcastCommand implements Command {
    private final TVShows tvShows;

    public ScheduleLiveBroadcastCommand(TVShows tvShows) {
        this.tvShows = tvShows;
    }

    @Override
    public String getName() {
        return "Schedule Live Broadcast";
    }

    @Override
    public void execute(Scanner in) {
        tvShows.scheduleLiveBroadcast(in);
    }
}
