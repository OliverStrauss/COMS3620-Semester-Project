import java.util.Scanner;

// Command for the "Sell Ticket" use case. Receiver: Ticketing
public class SellTicketCommand implements Command {
    private final Ticketing ticketing;

    public SellTicketCommand(Ticketing ticketing) {
        this.ticketing = ticketing;
    }

    @Override
    public String getName() {
        return "Sell Ticket";
    }

    @Override
    public void execute(Scanner in) {
        ticketing.sellTicket(in);
    }
}
