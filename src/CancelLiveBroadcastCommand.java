import java.util.Scanner;

// Command for the "Cancel Live Broadcast" use case. Receiver: TVShows
public class CancelLiveBroadcastCommand implements Command {
    private final TVShows tvShows;

    public CancelLiveBroadcastCommand(TVShows tvShows) {
        this.tvShows = tvShows;
    }

    @Override
    public String getName() {
        return "Cancel Live Broadcast";
    }

    @Override
    public void execute(Scanner in) {
        tvShows.cancelLiveBroadcast(in);
    }
}
