import java.util.Scanner;

// Command for the "View Live Broadcasts" use case. Receiver: TVShows
public class ViewLiveBroadcastsCommand implements Command {
    private final TVShows tvShows;

    public ViewLiveBroadcastsCommand(TVShows tvShows) {
        this.tvShows = tvShows;
    }

    @Override
    public String getName() {
        return "View Live Broadcasts";
    }

    @Override
    public void execute(Scanner in) {
        tvShows.viewLiveBroadcasts(in);
    }
}
