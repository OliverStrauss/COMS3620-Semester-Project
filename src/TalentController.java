import java.io.File;
import java.io.IOException;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

// GRASP Controller for Talent Relations: receives each system operation from the UI
// and coordinates Wrestler, Contract and saving. It never reads input or prints.
// Operations return the message to show the user, including any save failure.
public class TalentController {

    public List<Wrestler> getWrestlers() {
        return Wrestler.loadAll();
    }

    public List<Contract> getContracts() {
        return Contract.loadAll();
    }

    // The wrestler with this ID, or null if there isn't one
    public Wrestler getWrestler(int id) {
        return Wrestler.findById(id);
    }

    // The contract with this ID, or null if there isn't one
    public Contract getContract(int id) {
        return Contract.findById(id);
    }

    public List<Wrestler> getNxtWrestlers() {
        List<Wrestler> nxt = new ArrayList<>();
        for (Wrestler w : Wrestler.loadAll()) {
            if (!w.getBrand().isMainRoster()) nxt.add(w);
        }
        return nxt;
    }

    public String signWrestler(String legalName, String ringName, Wrestler.Brand brand) {
        // Get the ID last, so bad input never uses one up
        Wrestler wrestler = new Wrestler(IdGenerator.nextId(Wrestler.FILE), legalName, ringName, brand, Wrestler.Status.ACTIVE, true); // new signings start medically cleared
        try {
            CsvWriter.appendRow(Wrestler.FILE, wrestler.toRow());
            return "Created " + wrestler;
        } catch (IOException e) {
            return "Could not save wrestler: " + e.getMessage();
        }
    }

    public String createContract(int wrestlerId, long salary, LocalDate start, LocalDate end) {
        if (Wrestler.findById(wrestlerId) == null) return "No wrestler with ID " + wrestlerId + ".";
        return saveNewContract(wrestlerId, salary, start, end, "Created");
    }

    // Renewing keeps the old contract as history and adds a new one after it
    public String renewContract(int contractId, long salary, LocalDate start, LocalDate end) {
        Contract old = Contract.findById(contractId);
        if (old == null) return "No contract with ID " + contractId + ".";
        return saveNewContract(old.getWrestlerId(), salary, start, end, "Renewed as");
    }

    private String saveNewContract(int wrestlerId, long salary, LocalDate start, LocalDate end, String verb) {
        // Get the ID last, so bad input never uses one up
        Contract contract = new Contract(IdGenerator.nextId(Contract.FILE), wrestlerId, salary, start, end);
        try {
            CsvWriter.appendRow(Contract.FILE, contract.toRow());
        } catch (IOException e) {
            return "Could not save contract: " + e.getMessage();
        }
        return verb + " " + contract + reactivate(wrestlerId);
    }

    // Re-signing a released wrestler brings them back as ACTIVE, or INJURED if Medical hasn't cleared them
    // Returns an extra line for the message, or "" if nothing changed. It handles its own save failure,
    // so the user still hears that the contract saved
    private String reactivate(int wrestlerId) {
        List<Wrestler> wrestlers = Wrestler.loadAll();
        Wrestler wrestler = find(wrestlers, wrestlerId);
        if (wrestler == null || wrestler.getStatus() != Wrestler.Status.RELEASED) return "";

        wrestler.setStatus(wrestler.hasMedicalClearance() ? Wrestler.Status.ACTIVE : Wrestler.Status.INJURED);
        try {
            Wrestler.saveAll(wrestlers);
            return "\n" + wrestler.getRingName() + " is " + wrestler.getStatus() + " again.";
        } catch (IOException e) {
            return "\nCould not save wrestler: " + e.getMessage();
        }
    }

    // Releasing keeps the wrestler in the file as RELEASED, so history isn't lost
    public String releaseWrestler(int wrestlerId) {
        List<Wrestler> wrestlers = Wrestler.loadAll();
        Wrestler wrestler = find(wrestlers, wrestlerId);
        if (wrestler == null) return "No wrestler with ID " + wrestlerId + ".";
        if (wrestler.getStatus() == Wrestler.Status.RELEASED) return wrestler.getRingName() + " is already released.";

        wrestler.setStatus(Wrestler.Status.RELEASED);
        try {
            Wrestler.saveAll(wrestlers);
            return "Released " + wrestler.getRingName() + ".";
        } catch (IOException e) {
            return "Could not save wrestler: " + e.getMessage();
        }
    }

    // Ends this wrestler's contracts today. Contracts that haven't started yet are dropped
    public String endContracts(int wrestlerId) {
        LocalDate today = LocalDate.now();
        List<Contract> kept = new ArrayList<>();
        int ended = 0;
        for (Contract c : Contract.loadAll()) {
            if (c.getWrestlerId() != wrestlerId || !c.getEndDate().isAfter(today)) {
                kept.add(c); // someone else's, or already over
            } else {
                ended++;
                if (!c.getStartDate().isAfter(today)) {
                    kept.add(new Contract(c.getId(), wrestlerId, c.getYearlySalary(), c.getStartDate(), today));
                }
            }
        }
        if (ended == 0) return "No running contracts for wrestler #" + wrestlerId + ".";
        try {
            Contract.saveAll(kept);
            return "Ended " + ended + " contract(s) for wrestler #" + wrestlerId + ".";
        } catch (IOException e) {
            return "Could not save contracts: " + e.getMessage();
        }
    }

    // Why this wrestler can't be promoted, or null if they can
    public String promotionProblem(int wrestlerId) {
        Wrestler wrestler = Wrestler.findById(wrestlerId);
        if (wrestler == null) return "No wrestler with ID " + wrestlerId + ".";
        if (wrestler.getBrand().isMainRoster()) return wrestler.getRingName() + " is already on the main roster.";
        if (wrestler.getStatus() != Wrestler.Status.ACTIVE) return wrestler.getRingName() + " is " + wrestler.getStatus() + " and can't be promoted.";
        if (!Contract.hasActiveContract(wrestlerId, LocalDate.now())) return wrestler.getRingName() + " needs an active contract to be promoted.";
        return null;
    }

    // Moves an NXT wrestler up to a main roster brand
    public String promote(int wrestlerId, Wrestler.Brand brand) {
        String problem = promotionProblem(wrestlerId);
        if (problem != null) return problem;

        List<Wrestler> wrestlers = Wrestler.loadAll();
        Wrestler wrestler = find(wrestlers, wrestlerId);
        wrestler.setBrand(brand);
        try {
            Wrestler.saveAll(wrestlers);
            return "Promoted " + wrestler.getRingName() + " to " + brand + ".";
        } catch (IOException e) {
            return "Could not save wrestler: " + e.getMessage();
        }
    }

    // Finds the wrestler in this list, so saveAll writes the change back
    private Wrestler find(List<Wrestler> wrestlers, int id) {
        for (Wrestler w : wrestlers) {
            if (w.getId() == id) return w;
        }
        return null;
    }

    // For testing. Data paths are relative and this writes to them, so run it from an empty folder:
    //   mkdir t && cd t && java -cp ../src TalentController
    public static void main(String[] args) {
        if (new File("Data").exists()) {
            throw new IllegalStateException("Run from an empty folder, this writes to Data/");
        }
        TalentController c = new TalentController();
        LocalDate today = LocalDate.now();

        c.signWrestler("Test Legal", "Test Ring", Wrestler.Brand.NXT);
        check(c.promotionProblem(99).contains("No wrestler"), "missing wrestler blocks promotion");
        check(c.promotionProblem(1).contains("active contract"), "no contract blocks promotion");
        check(c.promote(1, Wrestler.Brand.RAW).contains("active contract"), "promote enforces the same rules");

        c.createContract(1, 1000, today, today.plusYears(1));
        check(c.promotionProblem(1) == null, "contract allows promotion");
        c.promote(1, Wrestler.Brand.RAW);
        check(c.getWrestler(1).getBrand() == Wrestler.Brand.RAW, "promotion saved");

        c.releaseWrestler(1);
        check(c.getWrestler(1).getStatus() == Wrestler.Status.RELEASED, "release saved");
        check(c.releaseWrestler(1).contains("already released"), "second release says so");
        check(Contract.hasActiveContract(1, today), "releasing the wrestler alone keeps the contract");
        c.endContracts(1);
        check(!Contract.hasActiveContract(1, today), "contract ended");

        String renewed = c.renewContract(1, 2000, today, today.plusYears(1));
        check(renewed.startsWith("Renewed as") && renewed.contains("ACTIVE again"), "renewal reactivates");
        check(c.createContract(99, 1000, today, today.plusYears(1)).contains("No wrestler"), "contract needs a wrestler");

        System.out.println("TalentController ok");
    }

    private static void check(boolean condition, String what) {
        if (!condition) throw new AssertionError(what);
    }
}
