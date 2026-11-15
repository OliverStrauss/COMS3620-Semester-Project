import java.util.List;

// Talent Relations menu. Each use case is a Command object (Command pattern);
// TalentController is the receiver that does the work and saves.
public class TalentDepartment extends Department {
    private final TalentController controller = new TalentController();

    // Menu order
    private final List<Command> commands = List.of(
            new SignWrestlerCommand(controller),
            new ReleaseWrestlerCommand(controller),
            new PromoteWrestlerCommand(controller),
            new ViewWrestlersCommand(controller),
            new RenewContractCommand(controller),
            new CreateContractCommand(controller),
            new ViewContractsCommand(controller)
    );

    public TalentDepartment() {
        super("Talent Relations");
    }

    @Override
    protected List<Command> getCommands() {
        return commands;
    }
}
