import java.util.*;
import java.io.File;
import java.io.IOException;
import java.time.LocalDate;

public class MedicalDepartment extends Department {

    private static final MedicalDepartment INSTANCE = new MedicalDepartment();
    private static Map<Integer, List<Injury>> wrestlerInjuries = null;
    private static final String CSV_FILE_PATH = "Data/medical_records.txt";

    // Private constructor matching the pattern of other departments
    private MedicalDepartment() {
        super("Medical Department");
    }

    public static MedicalDepartment getInstance() {
        return INSTANCE;
    }

    // --- Private Static Inner Class for Injury Records ---
    private static class Injury {
        private final String injury;
        private boolean isInjured;
        private LocalDate startDate;
        private LocalDate endDate;

        public Injury(String injury, boolean isInjured, LocalDate startDate) {
            this.injury = injury;
            this.isInjured = isInjured;
            this.startDate = startDate;
            this.endDate = null;
        }

        public String getInjury() { return injury; }
        public boolean isInjured() { return isInjured; }
        public void setInjured(boolean injured) { this.isInjured = injured; }
        public LocalDate getStartDate() { return startDate; }
        public LocalDate getEndDate() { return endDate; }
        public void setEndDate(LocalDate endDate) { this.endDate = endDate; }

        @Override
        public String toString() {
            String endStr = (endDate == null) ? "Ongoing" : endDate.toString();
            return String.format("%s [Status: %s | Started: %s | Cleared: %s]", 
                injury, (isInjured ? "INJURED" : "RECOVERED"), startDate, endStr);
        }
    }
    
    @Override 
    protected List<String> getUseCases() {
        return List.of(
            "Add Injury",
            "Clear Injury",
            "View Medical Records"
        );
    }

    @Override
    protected void runUseCase(int choice, Scanner in) {
        switch (choice) {
            case 0 -> uiAddInjury(in);
            case 1 -> uiClearInjury(in);
            case 2 -> uiViewMedicalRecords(in);
            default -> System.out.println("Invalid choice.");
        }
    }

    // --- Text UI Execution Flows ---

    private void uiAddInjury(Scanner in) {
        int wrestlerId = Math.toIntExact(askPositiveLong(in, "Enter Wrestler ID to report injury: ", null));
        Wrestler wrestler = Wrestler.findById(wrestlerId);
        
        if (wrestler == null) {
            System.out.println("No wrestler found with ID " + wrestlerId + ".");
            return;
        }

        String injuryName = askText(in, "Enter injury description: ", "Sprained Ankle");
        LocalDate start = askDate(in, "Start date (YYYY-MM-DD, enter for today): ", LocalDate.now());
        
        // Execute the backend static method
        addInjury(wrestler, injuryName, true, start);
        System.out.println("Successfully logged injury for " + wrestler.getRingName() + ".");
    }

    private void uiClearInjury(Scanner in) {
        int wrestlerId = Math.toIntExact(askPositiveLong(in, "Enter Wrestler ID to clear an injury: ", null));
        Wrestler wrestler = Wrestler.findById(wrestlerId);
        
        if (wrestler == null) {
            System.out.println("No wrestler found with ID " + wrestlerId + ".");
            return;
        }

        ensureLogIsLoaded();
        List<Injury> injuries = wrestlerInjuries.get(wrestlerId);
        if (injuries == null || injuries.isEmpty()) {
            System.out.println(wrestler.getRingName() + " has no logged injury history.");
            return;
        }

        System.out.println("\nSelect injury index to clear:");
        for (int i = 0; i < injuries.size(); i++) {
            System.out.println("[" + i + "] " + injuries.get(i));
        }

        int index = Math.toIntExact(askNonNegativeLong(in, "Select index number: ", null));
        if (index < 0 || index >= injuries.size()) {
            System.out.println("Invalid selection index.");
            return;
        }

        // Execute clearance and push status changes back out
        clearInjury(wrestler, index);
        System.out.println("Injury status successfully cleared. Wrestler evaluated as: " + wrestler.getStatus());
    }

    private void uiViewMedicalRecords(Scanner in) {
        System.out.println("\n=== MEDICAL RECORD VIEWER ===");
        System.out.println("Enter a specific Wrestler ID, or enter [0] to view the entire master directory.");
        int choice = Math.toIntExact(askNonNegativeLong(in, "Wrestler ID or 0: ", 0L));

        if (choice == 0) {
            // --- Option A: Print Corporate Master Directory ---
            Map<Integer, List<Injury>> allRecords = getAllMedicalRecords();

            if (allRecords.isEmpty()) {
                System.out.println("The master medical log directory is currently empty.");
                return;
            }

            System.out.println("\n=======================================================");
            System.out.println("          MASTER PROMOTION MEDICAL DIRECTORY           ");
            System.out.println("=======================================================");
            for (Map.Entry<Integer, List<Injury>> entry : allRecords.entrySet()) {
                int wrestlerId = entry.getKey();
                List<Injury> injuries = entry.getValue();

                // Try to find the wrestler's name for richer display
                Wrestler wrestler = Wrestler.findById(wrestlerId);
                String label = (wrestler != null) ? wrestler.getRingName() : "Unknown Wrestler";

                System.out.printf("Wrestler ID #%d (%s) — %d Injury Entry(s):\n", wrestlerId, label, injuries.size());
                for (Injury injury : injuries) {
                    System.out.println("   └─ " + injury);
                }
                System.out.println("-------------------------------------------------------");
            }

        } else {
            // --- Option B: Print Single Wrestler Dossier ---
            Wrestler wrestler = Wrestler.findById(choice);
            String nameLabel = (wrestler != null) ? wrestler.getRingName() : "Unknown Wrestler";
            String statusLabel = (wrestler != null) ? wrestler.getStatus().toString() : "UNKNOWN";

            List<Injury> history = getInjuriesByWrestlerId(choice);

            System.out.println("\n=======================================================");
            System.out.printf(" MEDICAL DOSSIER: %s (ID #%d)\n", nameLabel.toUpperCase(), choice);
            System.out.printf(" Current Availability Status: %s\n", statusLabel);
            System.out.println("=======================================================");

            if (history.isEmpty()) {
                System.out.println(" No history of medical or injury events recorded on disk.");
                System.out.println("=======================================================");
                return;
            }

            for (int i = 0; i < history.size(); i++) {
                System.out.printf("[%d] %s\n", i, history.get(i));
            }
            System.out.println("=======================================================");
        }
    }

    // --- Core Global Static Methods ---

    public static void addInjury(Wrestler wrestler, String injury, boolean isInjured, LocalDate startDate) {
        ensureLogIsLoaded();

        wrestlerInjuries.putIfAbsent(wrestler.getId(), new ArrayList<>());
        Injury newInjury = new Injury(injury, isInjured, startDate);
        wrestlerInjuries.get(wrestler.getId()).add(newInjury);

        syncWrestlerStatus(wrestler);
        saveLogToCSV();
        saveWrestler(wrestler);
    }

    public static void clearInjury(Wrestler wrestler, int injuryIndex) {
        ensureLogIsLoaded();

        List<Injury> injuries = wrestlerInjuries.get(wrestler.getId());
        if (injuries != null && injuryIndex >= 0 && injuryIndex < injuries.size()) {
            Injury injury = injuries.get(injuryIndex);
            injury.setInjured(false);
            injury.setEndDate(LocalDate.now());
            
            syncWrestlerStatus(wrestler);
            saveLogToCSV();
        }
    }

    // Writes this wrestler's new status and clearance back to Wrestlers.txt
    private static void saveWrestler(Wrestler wrestler) {
        try {
            List<Wrestler> allWrestlers = Wrestler.loadAll();
            for (int i = 0; i < allWrestlers.size(); i++) {
                if (allWrestlers.get(i).getId() == wrestler.getId()) {
                    allWrestlers.set(i, wrestler);
                    break;
                }
            }
            Wrestler.saveAll(allWrestlers);
        } catch (IOException e) {
            System.out.println("Medical could not persist state to Wrestlers database: " + e.getMessage());
        }
    }

    public static void syncWrestlerStatus(Wrestler wrestler) {
        ensureLogIsLoaded();

        List<Injury> injuries = wrestlerInjuries.get(wrestler.getId());
        boolean currentlyInjured = false;

        if (injuries != null && !injuries.isEmpty()) {
            for (Injury injury : injuries) {
                if (injury.isInjured()) {
                    currentlyInjured = true;
                    break; // One active injury is enough to bench them
                }
            }
        }

        // Medical never undoes a release; a released wrestler only has their clearance tracked
        boolean released = wrestler.getStatus() == Wrestler.Status.RELEASED;
        if (currentlyInjured) {
            if (!released) wrestler.setStatus(Wrestler.Status.INJURED);
            wrestler.setMedicalClearance(false);
        } else {
            if (!released) wrestler.setStatus(Wrestler.Status.ACTIVE);
            wrestler.setMedicalClearance(true);
        }

        saveWrestler(wrestler);
    }


   /**
    * Retrieves the complete injury history for a specific wrestler.
    * @param wrestlerId The ID of the wrestler to look up.
    * @return A list of Injury objects, or an empty list if none exist.
    */
    public static List<Injury> getInjuriesByWrestlerId(int wrestlerId) {
        ensureLogIsLoaded();
        List<Injury> injuries = wrestlerInjuries.get(wrestlerId);

        // Return an unmodifiable copy to protect our internal memory map state
        return injuries != null ? Collections.unmodifiableList(injuries) : List.of();
    }

   /**
    * Retrieves the entire master medical database mapping wrestler IDs to their injuries.
    * @return A map containing all recorded wrestler IDs and their injury lists.
    */
    public static Map<Integer, List<Injury>> getAllMedicalRecords() {
        ensureLogIsLoaded();

        // Return an unmodifiable copy so outside classes can read the map but not break it
        return Collections.unmodifiableMap(wrestlerInjuries);
    }



    // --- Storage Handling Methods ---

    private static void ensureLogIsLoaded() {
        if (wrestlerInjuries != null) {
            return;
        }

        wrestlerInjuries = new HashMap<>();
        File file = new File(CSV_FILE_PATH);
        if (!file.exists()) {
            return;
        }

        ArrayList<ArrayList<Object>> paredRows = CsvParser.parsecsv(file);

        for (ArrayList<Object> row : paredRows) {
            if (row.size() < 3) continue;

            int wrestlerId = (int) row.get(0);
            String injuryName = (String) row.get(1);
            
            Object statusObj = row.get(2);
            boolean isInjured = statusObj instanceof Boolean ? (boolean) statusObj : Boolean.parseBoolean(statusObj.toString());

            LocalDate startDate = LocalDate.now();
            if (row.size() > 3 && row.get(3) != null && !row.get(3).toString().isEmpty()) {
                startDate = LocalDate.parse(row.get(3).toString().trim());
            }

            Injury injuryObj = new Injury(injuryName, isInjured, startDate);

            if (row.size() > 4 && row.get(4) != null && !row.get(4).toString().isEmpty()) {
                injuryObj.setEndDate(LocalDate.parse(row.get(4).toString().trim()));
            }

            wrestlerInjuries.putIfAbsent(wrestlerId, new ArrayList<>());
            wrestlerInjuries.get(wrestlerId).add(injuryObj);
        }
    }

    private static void saveLogToCSV() {
        if (wrestlerInjuries == null) return;

        List<List<Object>> rowsToWrite = new ArrayList<>();
        for (Map.Entry<Integer, List<Injury>> entry : wrestlerInjuries.entrySet()) {
            int id = entry.getKey();
            for (Injury injury : entry.getValue()) {
                List<Object> row = new ArrayList<>();
                row.add(id);
                row.add(injury.getInjury());
                row.add(injury.isInjured());
                row.add(injury.getStartDate() != null ? injury.getStartDate().toString() : "");
                row.add(injury.getEndDate() != null ? injury.getEndDate().toString() : "");
                rowsToWrite.add(row);
            }
        }

        try {
            CsvWriter.writeAll(CSV_FILE_PATH, rowsToWrite);
        } catch (IOException e) {
            System.err.println("Error writing to medical CSV: " + e.getMessage());
        }
    }
}
