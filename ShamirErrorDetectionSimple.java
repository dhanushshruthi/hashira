import java.io.*;
import java.math.BigInteger;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.*;
import java.util.stream.Collectors;

public class ShamirErrorDetectionSimple {
    
    static class Share {
        int id;
        int base;
        String value;
        BigInteger decimalValue;
        
        public Share(int id, int base, String value) {
            this.id = id;
            this.base = base;
            this.value = value;
            this.decimalValue = baseToDecimal(value, base);
        }
    }
    
    static class Point {
        BigInteger x;
        BigInteger y;
        
        public Point(BigInteger x, BigInteger y) {
            this.x = x;
            this.y = y;
        }
    }
    
    static class ShareAnalysis {
        int correct = 0;
        int incorrect = 0;
        int total = 0;
        
        public double getErrorRate() {
            return total == 0 ? 0 : (double) incorrect / total * 100;
        }
        
        public double getCorrectRate() {
            return total == 0 ? 0 : (double) correct / total * 100;
        }
    }
    
    public static BigInteger baseToDecimal(String value, int base) {
        if (base <= 10) {
            return new BigInteger(value, base);
        } else {
            // Handle bases > 10
            String digits = "0123456789abcdefghijklmnopqrstuvwxyz";
            BigInteger result = BigInteger.ZERO;
            BigInteger baseBig = BigInteger.valueOf(base);
            
            for (char digit : value.toLowerCase().toCharArray()) {
                int digitValue = digits.indexOf(digit);
                result = result.multiply(baseBig).add(BigInteger.valueOf(digitValue));
            }
            return result;
        }
    }
    
    public static BigDecimal lagrangeInterpolation(List<Point> points, BigInteger x) {
        BigDecimal result = BigDecimal.ZERO;
        int n = points.size();
        
        for (int i = 0; i < n; i++) {
            Point pi = points.get(i);
            BigDecimal term = new BigDecimal(pi.y);
            
            for (int j = 0; j < n; j++) {
                if (i != j) {
                    Point pj = points.get(j);
                    BigDecimal numerator = new BigDecimal(x.subtract(pj.x));
                    BigDecimal denominator = new BigDecimal(pi.x.subtract(pj.x));
                    term = term.multiply(numerator.divide(denominator, 50, RoundingMode.HALF_UP));
                }
            }
            
            result = result.add(term);
        }
        
        return result;
    }
    
    public static List<List<Share>> generateCombinations(List<Share> shares, int k) {
        List<List<Share>> combinations = new ArrayList<>();
        generateCombinationsHelper(shares, new ArrayList<>(), 0, k, combinations);
        return combinations;
    }
    
    private static void generateCombinationsHelper(List<Share> shares, List<Share> current, 
                                                 int start, int k, List<List<Share>> combinations) {
        if (current.size() == k) {
            combinations.add(new ArrayList<>(current));
            return;
        }
        
        for (int i = start; i < shares.size(); i++) {
            current.add(shares.get(i));
            generateCombinationsHelper(shares, current, i + 1, k, combinations);
            current.remove(current.size() - 1);
        }
    }
    
    // Simple JSON parser for our specific structure
    public static Map<String, Object> parseJsonFromFile(String filename) throws IOException {
        String content = new String(Files.readAllBytes(Paths.get(filename)));
        return parseJson(content);
    }
    
    private static Map<String, Object> parseJson(String json) {
        json = json.trim();
        if (json.startsWith("{") && json.endsWith("}")) {
            return parseObject(json.substring(1, json.length() - 1));
        }
        throw new IllegalArgumentException("Invalid JSON");
    }
    
    private static Map<String, Object> parseObject(String content) {
        Map<String, Object> result = new HashMap<>();
        content = content.trim();
        
        while (!content.isEmpty()) {
            // Find key
            int colonIndex = content.indexOf(':');
            String key = content.substring(0, colonIndex).trim();
            key = key.replaceAll("\"", "");
            
            content = content.substring(colonIndex + 1).trim();
            
            // Find value
            Object value;
            if (content.startsWith("{")) {
                int braceCount = 0;
                int endIndex = 0;
                for (int i = 0; i < content.length(); i++) {
                    if (content.charAt(i) == '{') braceCount++;
                    if (content.charAt(i) == '}') braceCount--;
                    if (braceCount == 0) {
                        endIndex = i;
                        break;
                    }
                }
                String objectStr = content.substring(1, endIndex);
                value = parseObject(objectStr);
                content = content.substring(endIndex + 1);
            } else {
                int commaIndex = findNextComma(content);
                String valueStr = (commaIndex == -1) ? content : content.substring(0, commaIndex);
                valueStr = valueStr.trim().replaceAll("\"", "");
                
                // Try to parse as number
                try {
                    if (valueStr.contains(".")) {
                        value = Double.parseDouble(valueStr);
                    } else {
                        value = Integer.parseInt(valueStr);
                    }
                } catch (NumberFormatException e) {
                    value = valueStr;
                }
                
                content = (commaIndex == -1) ? "" : content.substring(commaIndex + 1);
            }
            
            result.put(key, value);
            content = content.trim();
            if (content.startsWith(",")) {
                content = content.substring(1).trim();
            }
        }
        
        return result;
    }
    
    private static int findNextComma(String content) {
        int braceCount = 0;
        for (int i = 0; i < content.length(); i++) {
            char c = content.charAt(i);
            if (c == '{') braceCount++;
            if (c == '}') braceCount--;
            if (c == ',' && braceCount == 0) {
                return i;
            }
        }
        return -1;
    }
    
    public static void findWrongShare(Map<String, Object> data) {
        @SuppressWarnings("unchecked")
        Map<String, Object> keys = (Map<String, Object>) data.get("keys");
        int n = (Integer) keys.get("n");
        int k = (Integer) keys.get("k");
        
        // Parse all shares
        List<Share> shares = new ArrayList<>();
        Map<Integer, Share> shareMap = new HashMap<>();
        
        for (Map.Entry<String, Object> entry : data.entrySet()) {
            if (!"keys".equals(entry.getKey())) {
                int shareId = Integer.parseInt(entry.getKey());
                @SuppressWarnings("unchecked")
                Map<String, Object> shareData = (Map<String, Object>) entry.getValue();
                int base = Integer.parseInt(shareData.get("base").toString());
                String value = shareData.get("value").toString();
                
                Share share = new Share(shareId, base, value);
                shares.add(share);
                shareMap.put(shareId, share);
            }
        }
        
        System.out.println("Parsed " + shares.size() + " shares:");
        for (Share share : shares) {
            System.out.println("Share " + share.id + ": " + share.decimalValue);
        }
        
        // Get all possible combinations of exactly k shares
        List<List<Share>> allCombinations = generateCombinations(shares, k);
        
        System.out.println("\nTrying all " + allCombinations.size() + " combinations of exactly " + k + " shares...");
        
        // Calculate secret for each combination
        Map<BigInteger, List<List<Share>>> secretsFrequency = new HashMap<>();
        Map<List<Share>, BigInteger> combinationSecrets = new HashMap<>();
        
        for (List<Share> combo : allCombinations) {
            List<Point> points = combo.stream()
                    .map(s -> new Point(BigInteger.valueOf(s.id), s.decimalValue))
                    .collect(Collectors.toList());
            
            BigDecimal secret = lagrangeInterpolation(points, BigInteger.ZERO);
            BigInteger secretInt = secret.setScale(0, RoundingMode.HALF_UP).toBigInteger();
            
            combinationSecrets.put(combo, secretInt);
            
            if (!secretsFrequency.containsKey(secretInt)) {
                secretsFrequency.put(secretInt, new ArrayList<>());
            }
            secretsFrequency.get(secretInt).add(combo);
        }
        
        // Find the most frequent secret
        BigInteger mostFrequentSecret = secretsFrequency.entrySet().stream()
                .max(Map.Entry.comparingByValue(Comparator.comparing(List::size)))
                .get().getKey();
        
        List<List<Share>> correctCombinations = secretsFrequency.get(mostFrequentSecret);
        
        System.out.println("\nSecret frequency analysis:");
        List<Map.Entry<BigInteger, List<List<Share>>>> sortedSecrets = secretsFrequency.entrySet().stream()
                .sorted(Map.Entry.<BigInteger, List<List<Share>>>comparingByValue(Comparator.comparing(List::size)).reversed())
                .limit(10)
                .collect(Collectors.toList());
        
        for (int i = 0; i < sortedSecrets.size(); i++) {
            Map.Entry<BigInteger, List<List<Share>>> entry = sortedSecrets.get(i);
            double percentage = (double) entry.getValue().size() / allCombinations.size() * 100;
            System.out.printf("  %d. Secret %s: appears in %d combinations (%.2f%%)\n", 
                    i + 1, entry.getKey(), entry.getValue().size(), percentage);
        }
        
        System.out.println("\nMost frequent secret: " + mostFrequentSecret);
        double correctPercentage = (double) correctCombinations.size() / allCombinations.size() * 100;
        System.out.printf("Appears in %d out of %d combinations (%.2f%%)\n", 
                correctCombinations.size(), allCombinations.size(), correctPercentage);
        
        // Analyze which shares appear in correct vs incorrect combinations
        Map<Integer, ShareAnalysis> shareAnalysis = new HashMap<>();
        for (Share share : shares) {
            shareAnalysis.put(share.id, new ShareAnalysis());
        }
        
        for (Map.Entry<List<Share>, BigInteger> entry : combinationSecrets.entrySet()) {
            boolean isCorrect = entry.getValue().equals(mostFrequentSecret);
            for (Share share : entry.getKey()) {
                ShareAnalysis analysis = shareAnalysis.get(share.id);
                analysis.total++;
                if (isCorrect) {
                    analysis.correct++;
                } else {
                    analysis.incorrect++;
                }
            }
        }
        
        System.out.println("\nShare analysis:");
        System.out.println("--------------------------------------------------------------------------------");
        System.out.printf("%-6s %-6s %-8s %-10s %-10s %s\n", "Share", "Total", "Correct", "Incorrect", "Correct %", "Error Rate");
        System.out.println("--------------------------------------------------------------------------------");
        
        int mostSuspicious = -1;
        double highestErrorRate = 0;
        
        for (int shareId : shareAnalysis.keySet().stream().sorted().collect(Collectors.toList())) {
            ShareAnalysis stats = shareAnalysis.get(shareId);
            double correctPct = stats.getCorrectRate();
            double errorRate = stats.getErrorRate();
            
            System.out.printf("%-6d %-6d %-8d %-10d %-9.1f%% %.1f%%\n", 
                    shareId, stats.total, stats.correct, stats.incorrect, correctPct, errorRate);
            
            if (errorRate > highestErrorRate) {
                highestErrorRate = errorRate;
                mostSuspicious = shareId;
            }
        }
        
        System.out.println("\n*** ANALYSIS RESULT ***");
        if (mostSuspicious != -1 && highestErrorRate > 50) {
            Share wrongShare = shareMap.get(mostSuspicious);
            System.out.println("WRONG SHARE IDENTIFIED: Share " + mostSuspicious);
            System.out.println("Share " + mostSuspicious + " value: " + wrongShare.decimalValue);
            System.out.printf("This share appears in incorrect combinations %.1f%% of the time\n", highestErrorRate);
            System.out.println("Correct secret: " + mostFrequentSecret);
            
            System.out.println("\nFinal Result:");
            System.out.println("Correct secret: " + mostFrequentSecret);
            System.out.println("Wrong share(s):");
            System.out.printf("  Share %d: base=%d, value='%s' (decimal: %s)\n", 
                    wrongShare.id, wrongShare.base, wrongShare.value, wrongShare.decimalValue);
        } else {
            System.out.println("Could not definitively identify a single wrong share");
            System.out.println("\nFinal Result:");
            System.out.println("Correct secret: " + mostFrequentSecret);
            System.out.println("No wrong shares definitively identified");
        }
    }
    
    public static void main(String[] args) {
        System.out.println("Shamir's Secret Sharing - Error Detection");
        System.out.println("==================================================");
        
        try {
            Map<String, Object> data = parseJsonFromFile("test.json");
            findWrongShare(data);
            
        } catch (IOException e) {
            System.err.println("Error reading test.json: " + e.getMessage());
            e.printStackTrace();
        }
    }
}
