import java.util.Scanner;

// Command for the "Record Broadcast Ratings" use case. Receiver: TVShows
public class RecordBroadcastRatingsCommand implements Command {
    private final TVShows tvShows;

    public RecordBroadcastRatingsCommand(TVShows tvShows) {
        this.tvShows = tvShows;
    }

    @Override
    public String getName() {
        return "Record Broadcast Ratings";
    }

    @Override
    public void execute(Scanner in) {
        tvShows.recordBroadcastRatings(in);
    }
}
