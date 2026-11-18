import java.util.Scanner;

// Command for the "View Tickets" use case. Receiver: Ticketing
public class ViewTicketsCommand implements Command {
    private final Ticketing ticketing;

    public ViewTicketsCommand(Ticketing ticketing) {
        this.ticketing = ticketing;
    }

    @Override
    public String getName() {
        return "View Tickets";
    }

    @Override
    public void execute(Scanner in) {
        ticketing.viewTickets(in);
    }
}
