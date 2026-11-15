import java.util.List;

// Creative menu. Each use case is a Command object (Command pattern);
// CreativeController is the receiver that does the work and saves.
public class CreativeDepartment extends Department {
    private final CreativeController controller = new CreativeController();

    // Menu order
    private final List<Command> commands = List.of(
            new BookMatchCommand(controller),
            new ViewMatchesCommand(controller),
            new ViewStorylinesCommand(controller)
    );

    public CreativeDepartment() {
        super("Creative");
    }

    @Override
    protected List<Command> getCommands() {
        return commands;
    }
}
