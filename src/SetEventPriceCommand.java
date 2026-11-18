import java.util.Scanner;

// Command for the "Set Event Price" use case. Receiver: Ticketing
public class SetEventPriceCommand implements Command {
    private final Ticketing ticketing;

    public SetEventPriceCommand(Ticketing ticketing) {
        this.ticketing = ticketing;
    }

    @Override
    public String getName() {
        return "Set Event Price";
    }

    @Override
    public void execute(Scanner in) {
        ticketing.setEventPrice(in);
    }
}
