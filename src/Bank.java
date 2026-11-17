import java.io.IOException;
import java.util.List;

public class Bank {
    private static final String FILE_PATH = "src/Data/Bank.txt";

    public static double getBalance() {
        return ((Number) CsvParser.parsecsv(FILE_PATH).get(0).get(0)).doubleValue();
    }

    private static boolean updateBalance(double newBalance) {
        try {
            CsvWriter.writeAll(FILE_PATH, List.of(List.of(String.valueOf(newBalance))));
            return true;
        } catch (IOException e) {
            System.err.println("Failed to write to file: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }

    public static boolean withdraw(double amount) {
        if (amount <= 0) return false;

        double currentBalance = getBalance();
        if (currentBalance < amount) {
            System.err.println("Insufficient funds.");
            return false;
        }
        return updateBalance(currentBalance - amount);
    }

    public static boolean deposit(double amount) {
        if (amount <= 0) return false;

        double currentBalance = getBalance();
        return updateBalance(currentBalance + amount);
    }
}
