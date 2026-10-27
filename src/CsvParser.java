import java.io.File;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;
import java.util.function.Function;

public class CsvParser {

    private static final List<Function<String, Object>> TYPE_PARSERS = List.of(
        // 1. Try Integer
        s -> {
            try { return Integer.parseInt(s); } 
            catch (NumberFormatException e) { return null; }
        },
        // 2. Try Double/Float
        s -> {
            try { return Double.parseDouble(s); } 
            catch (NumberFormatException e) { return null; }
        },
        // 3. Try Boolean (strictly matches true/false)
        s -> {
            if (s.equalsIgnoreCase("true")) return Boolean.TRUE;
            if (s.equalsIgnoreCase("false")) return Boolean.FALSE;
            return null;
        },
        // 4. Default fallback: Keep it as a raw String
        s -> s
    );

    private static Object convertToNativeType(String rawValue) {
        if (rawValue == null || rawValue.isEmpty()) {
            return null; // Handle empty cells as null references
        }
        
        // Find the first parser that doesn't return null
        for (Function<String, Object> parser : TYPE_PARSERS) {
            Object result = parser.apply(rawValue.trim());
            if (result != null) {
                return result;
            }
        }
        return rawValue; // Absolute fallback
    }

    /**
     * Returns a 2d Arraylist containing rows then data
     * @param f Java File type
     * @return ArrayList of rows of data
     */
    public static ArrayList<ArrayList<Object>> parsecsv(File f) {
        ArrayList<ArrayList<Object>> result = new ArrayList<>();
        try {
            Scanner reader = new Scanner(f);

            while (reader.hasNextLine()) {
                String data = reader.nextLine();
                StringBuilder currentField = new StringBuilder();
                ArrayList<Object> res = new ArrayList<>();
                boolean inQuotes = false;

                for (int i = 0; i < data.length(); i++) {
                    char c = data.charAt(i);

                    if (inQuotes) {
                        if (c == '"') {
                            if (i + 1 < data.length() && data.charAt(i + 1) == '"') {
                                currentField.append('"');
                                i++;
                            } else {
                                inQuotes = false;
                            }
                        } else {
                            currentField.append(c);
                        }
                    } else {
                        if (c == '"') {
                            inQuotes = true;
                        } else if (c == ',') {
                            res.add(convertToNativeType(currentField.toString()));
                            currentField.setLength(0);
                        } else {
                            currentField.append(c);
                        }
                    }
                }
                res.add(convertToNativeType(currentField.toString()));
                result.add(res);
            }
            reader.close();
            return result;
        } catch (FileNotFoundException e) {
            System.out.println("An error occurred: File not found.");
            e.printStackTrace();
        }
        return result;
    }

    /**
     * Returns a 2d Arraylist containing rows then data
     * @param f String containgin file path
     * @return ArrayList of rows of data
     */
    public static ArrayList<ArrayList<Object>> parsecsv(String s) {
        File f = new File(s);
        return parsecsv(f);
    }

    //for testing
    public static void main(String args[]){
        ArrayList<ArrayList<Object>> list = parsecsv("test.csv");
        for (ArrayList<Object> row : list) {
            for (Object cell : row) {
                System.out.printf("%-25s", cell);
            }
            System.out.println();
        }

        System.out.println(list);

        if (!list.isEmpty() && list.size() > 1) {
            ArrayList<Object> firstDataRow = list.get(1);
            for (Object obj : firstDataRow) {
                System.out.println("Value: [" + obj + "] parsed natively as Class: " + obj.getClass().getSimpleName());
            }
        }

    }
}