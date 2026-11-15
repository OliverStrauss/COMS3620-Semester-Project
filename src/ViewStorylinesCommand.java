import java.util.List;
import java.util.Scanner;

// Every storyline with how many matches it has
public class ViewStorylinesCommand implements Command {
    private final CreativeController controller;

    public ViewStorylinesCommand(CreativeController controller) {
        this.controller = controller;
    }

    @Override
    public String getName() {
        return "View Storylines";
    }

    @Override
    public void execute(Scanner in) {
        List<Storyline> storylines = controller.getStorylines();
        if (storylines.isEmpty()) {
            System.out.println("No storylines yet.");
        }
        for (Storyline s : storylines) {
            System.out.println(s + " | " + s.matchCount() + " match(es)");
        }
    }
}
