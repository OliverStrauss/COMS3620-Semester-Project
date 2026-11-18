import java.util.Scanner;

// Command for the "Refund Ticket" use case. Receiver: Ticketing
public class RefundTicketCommand implements Command {
    private final Ticketing ticketing;

    public RefundTicketCommand(Ticketing ticketing) {
        this.ticketing = ticketing;
    }

    @Override
    public String getName() {
        return "Refund Ticket";
    }

    @Override
    public void execute(Scanner in) {
        ticketing.refundTicket(in);
    }
}
