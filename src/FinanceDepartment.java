import java.io.File;
import java.io.IOException;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Scanner;

public class FinanceDepartment extends Department{
    private static final FinanceDepartment INSTANCE = new FinanceDepartment();
    private static Map<Integer, Receipt> transactionLog = null;
    private static final String CSV_FILE_PATH = "src/Data/TransactionLog.txt";

    private FinanceDepartment() {
        super("Finance Department");
    }

    public static FinanceDepartment getInstance() {
        return INSTANCE;
    }

    private enum TransactionType{
        DEPOSIT,
        WITHDRAWAL
    }

    private static class Receipt{
        private int transactionId;
        private String transactionDescription;
        private LocalDate dateOfTransaction;
        private double amount;
        private TransactionType transactionType;

        public Receipt(int transactionId, String transactionDescription, double amount, TransactionType transactionType, LocalDate dateOfTransaction){
            this.transactionId = transactionId;
            this.transactionDescription = transactionDescription;
            this.amount = amount;
            this.transactionType = transactionType;
            this.dateOfTransaction = dateOfTransaction;
        }

        // Constructor for new transactions with a custom date
        public Receipt(String transactionDescription, double amount, TransactionType transactionType, LocalDate dateOfTransaction) {
            this(IdGenerator.nextId(CSV_FILE_PATH), transactionDescription, amount, transactionType, dateOfTransaction);
        }

        // Constructor for new transactions happening today
        public Receipt(String transactionDescription, double amount, TransactionType transactionType) {
            this(IdGenerator.nextId(CSV_FILE_PATH), transactionDescription, amount, transactionType, LocalDate.now());
        }

        public int getTransactionId() { return this.transactionId; }
        public String getTransactionDescription() { return this.transactionDescription; }
        public LocalDate getDateOfTransaction() { return this.dateOfTransaction; }
        public double getTransactionAmount() { return this.amount; }
        public TransactionType getTransactionType() { return this.transactionType; }
        
        @Override
        public String toString() {
            return String.format("#%d | %-10s | $%,10.2f | %s | %s",
                    transactionId, transactionType, amount, dateOfTransaction, transactionDescription);
        }
    }

    @Override
    protected List<String> getUseCases() {
        return List.of(
            "Deposit Funds",
            "Withdraw Funds",
            "View All Transactions",
            "View Transaction by ID",
            "Log Past Transaction"
        );
    }

    @Override
    protected void runUseCase(int choice, Scanner in) {
        switch (choice) {
            case 0 -> uiDeposit(in);
            case 1 -> uiWithdraw(in);
            case 2 -> uiViewAllTransactions();
            case 3 -> uiViewTransactionById(in);
            case 4 -> uiLogPastTransaction(in);
            default -> System.out.println("Invalid choice.");
        }
    }

    // --- Text UI Execution Flows ---

    private void uiDeposit(Scanner in) {
        System.out.println("\n=== RECORD DEPOSIT ===");
        System.out.printf("Current Bank Balance: $%,.2f\n", getCurrentBalance());

        String description = askText(in, "Enter deposit description: ", "Ticket & Merch Revenue");
        double amount = askPositiveDouble(in, "Enter deposit amount: $");

        try {
            Receipt receipt = processDeposit(description, amount);
            System.out.println("Successfully deposited funds and logged receipt:");
            System.out.println("   └─ " + receipt);
            System.out.printf("Updated Bank Balance: $%,.2f\n", getCurrentBalance());
        } catch (Exception e) {
            System.out.println("Deposit failed: " + e.getMessage());
        }
    }

    private void uiWithdraw(Scanner in) {
        System.out.println("\n=== RECORD WITHDRAWAL ===");
        System.out.printf("Current Bank Balance: $%,.2f\n", getCurrentBalance());

        String description = askText(in, "Enter withdrawal description: ", "Wrestler Payroll / Operations");
        double amount = askPositiveDouble(in, "Enter withdrawal amount: $");

        try {
            Receipt receipt = processWithdrawal(description, amount);
            if (receipt == null) {
                System.out.println("Withdrawal rejected by Bank (insufficient funds).");
                return;
            }
            System.out.println("Successfully withdrew funds and logged receipt:");
            System.out.println("   └─ " + receipt);
            System.out.printf("Updated Bank Balance: $%,.2f\n", getCurrentBalance());
        } catch (Exception e) {
            System.out.println("Withdrawal failed: " + e.getMessage());
        }
    }

    private void uiViewAllTransactions() {
        List<Receipt> allReceipts = getAllReceipts();

        System.out.println("\n======================================================================");
        System.out.println("                   MASTER PROMOTION TRANSACTION LOG                   ");
        System.out.printf(" Current Bank Balance: $%,.2f\n", getCurrentBalance());
        System.out.println("======================================================================");

        if (allReceipts.isEmpty()) {
            System.out.println(" No financial transactions recorded on disk.");
            System.out.println("======================================================================");
            return;
        }

        // Sort by transaction ID so they print in chronological order
        allReceipts.sort((r1, r2) -> Integer.compare(r1.getTransactionId(), r2.getTransactionId()));

        for (Receipt receipt : allReceipts) {
            System.out.println(" " + receipt);
        }
        System.out.println("======================================================================");
    }

    private void uiViewTransactionById(Scanner in) {
        System.out.println("\n=== TRANSACTION RECEIPT LOOKUP ===");
        int id = Math.toIntExact(askPositiveLong(in, "Enter Transaction ID: ", null));

        Receipt receipt = getReceiptById(id);

        System.out.println("\n=======================================================");
        System.out.printf(" TRANSACTION RECEIPT: ID #%d\n", id);
        System.out.println("=======================================================");

        if (receipt == null) {
            System.out.println(" No transaction found with ID #" + id + ".");
            System.out.println("=======================================================");
            return;
        }

        System.out.printf(" Type:        %s\n", receipt.getTransactionType());
        System.out.printf(" Amount:      $%,.2f\n", receipt.getTransactionAmount());
        System.out.printf(" Date:        %s\n", receipt.getDateOfTransaction());
        System.out.printf(" Description: %s\n", receipt.getTransactionDescription());
        System.out.println("=======================================================");
    }

    private void uiLogPastTransaction(Scanner in) {
        System.out.println("\n=== LOG PAST TRANSACTION (NO BANK UPDATE) ===");
        System.out.println("Note: This records a receipt in the log without changing the live Bank balance.");

        // 1. Choose Transaction Type
        System.out.println("Select transaction type:");
        System.out.println("[0] DEPOSIT");
        System.out.println("[1] WITHDRAWAL");
        int typeChoice = Math.toIntExact(askNonNegativeLong(in, "Enter choice (0 or 1): ", 0L));

        TransactionType type;
        if (typeChoice == 0) {
            type = TransactionType.DEPOSIT;
        } else if (typeChoice == 1) {
            type = TransactionType.WITHDRAWAL;
        } else {
            System.out.println("Invalid transaction type selected.");
            return;
        }

        // 2. Gather details and date
        String description = askText(in, "Enter transaction description: ", "Historical Entry");
        double amount = askPositiveDouble(in, "Enter transaction amount: $");
        LocalDate date = askDate(in, "Transaction date (YYYY-MM-DD, enter for today): ", LocalDate.now());

        // 3. Save receipt only
        try {
            Receipt receipt = logHistoricalTransaction(description, amount, type, date);
            System.out.println("Successfully logged past receipt (Bank balance unchanged):");
            System.out.println("   └─ " + receipt);
        } catch (Exception e) {
            System.out.println("Failed to log transaction: " + e.getMessage());
        }
    }


    // Helper for decimal currency input
    private double askPositiveDouble(Scanner in, String prompt) {
        while (true) {
            System.out.print(prompt);
            String input = in.nextLine().trim();
            try {
                double val = Double.parseDouble(input);
                if (val > 0) {
                    return val;
                }
                System.out.println("Please enter an amount greater than 0.");
            } catch (NumberFormatException e) {
                System.out.println("Invalid number. Please enter a valid dollar amount (e.g., 250.50).");
            }
        }
    }

    //--- Storage Methods ---

     /**
     * Calls the Bank API to deposit funds, then logs and saves a Receipt.
     */
    public Receipt processDeposit(String description, double amount) {
        if (amount <= 0) {
            throw new IllegalArgumentException("Deposit amount must be positive.");
        }

        Bank.deposit(amount);

        return createAndSaveReceipt(description, amount, TransactionType.DEPOSIT);
    }

    /**
     * Calls the Bank API to withdraw funds, then logs and saves a Receipt.
     */
    public Receipt processWithdrawal(String description, double amount) {
        if (amount <= 0) {
            throw new IllegalArgumentException("Withdrawal amount must be positive.");
        }

        Bank.withdraw(amount);

        return createAndSaveReceipt(description, amount, TransactionType.WITHDRAWAL);
    }

    /**
     * Logs a transaction that has already occurred in the Bank.
     * Saves a Receipt to the CSV without altering the current Bank balance.
     */
    public Receipt logHistoricalTransaction(String description, double amount, TransactionType type, LocalDate date) {
        if (amount <= 0) {
            throw new IllegalArgumentException("Transaction amount must be positive.");
        }
        if (date == null) {
            throw new IllegalArgumentException("Transaction date cannot be null.");
        }

        ensureLogIsLoaded();
        Receipt receipt = new Receipt(description, amount, type, date);
        transactionLog.put(receipt.getTransactionId(), receipt);
        saveLogToCSV();
        return receipt;
    }


    /**
     * Queries the Bank API for the current balance.
     */
    public double getCurrentBalance() {
        return Bank.getBalance();
    }

   private Receipt createAndSaveReceipt(String description, double amount, TransactionType type) {
        ensureLogIsLoaded();
        Receipt receipt = new Receipt(description, amount, type);
        transactionLog.put(receipt.getTransactionId(), receipt);
        saveLogToCSV();
        return receipt;
    }

    public List<Receipt> getAllReceipts() {
        ensureLogIsLoaded();
        return new ArrayList<>(transactionLog.values());
    }

    public Receipt getReceiptById(int id) {
        ensureLogIsLoaded();
        return transactionLog.get(id);
    }

    private static void ensureLogIsLoaded() {
        if (transactionLog != null) {
            return;
        }

        transactionLog = new HashMap<>();
        File file = new File(CSV_FILE_PATH);
        if (!file.exists()) {
            return;
        }

        ArrayList<ArrayList<Object>> parsedRows = CsvParser.parsecsv(file);

        for (ArrayList<Object> row : parsedRows) {
            if (row.size() < 5)
                continue;

            try {
                int transactionId = Integer.parseInt(row.get(0).toString().trim());
                String transactionDescription = row.get(1).toString().trim();
                LocalDate dateOfTransaction = LocalDate.parse(row.get(2).toString().trim());
                double amount = Double.parseDouble(row.get(3).toString().trim());
                TransactionType transactionType = TransactionType.valueOf(row.get(4).toString().trim().toUpperCase());

                Receipt receipt = new Receipt(transactionId, transactionDescription, amount, transactionType,
                        dateOfTransaction);
                transactionLog.put(transactionId, receipt);
            } catch (Exception e) {
                System.err.println("Skipping malformed finance row: " + row + " -> " + e.getMessage());
            }
        }
    }

    private static void saveLogToCSV() {
        if (transactionLog == null) return;

        List<List<Object>> rowsToWrite = new ArrayList<>();
        for (Receipt receipt : transactionLog.values()) {
            List<Object> row = new ArrayList<>();
            row.add(receipt.getTransactionId());
            row.add(receipt.getTransactionDescription());
            row.add(receipt.getDateOfTransaction().toString());
            row.add(receipt.getTransactionAmount());
            row.add(receipt.getTransactionType().name());
            rowsToWrite.add(row);
        }

        try {
            CsvWriter.writeAll(CSV_FILE_PATH, rowsToWrite);
        } catch (IOException e) {
            System.err.println("Error writing to TransactionLog CSV: " + e.getMessage());
        }
    }
}