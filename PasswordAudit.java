import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

/*
Name: Password Audit tool
Description: Reads a file of passwords (one per line),
scores each one against a basic strength ruling, and displays a
scoring report
Usage: java PasswordAudit passwords.txt
*/
public class PasswordAudit {

    // Small sample of very common passwords.
    private static final Set<String> COMMON = Set.of(
            "password", "12345678", "qwerty", "abc123",
            "letmein", "welcome", "admin",
            "iloveyou", "monkey", "password1", "dragon"
    );

    // Holds the result of auditing one password.
    static class Result {
        String password;
        int score;
        String rating;
        List<String> issues = new ArrayList<>();
    }

    // Scores one password from 0 to 100 and records any problems found
    static Result audit(String pw) {
        Result r = new Result();
        r.password = pw;
        int score = 0;

        // Length: up to 40 points
        if (pw.length() >= 16) {
            score += 40;
        } else if (pw.length() >= 12) {
            score += 30;
        } else if (pw.length() >= 8) {
            score += 20;
        } else {
            score += 5;
            r.issues.add("shorter than 8 characters");
        }

        // Character variety: 15 points per type up to 60
        boolean lower = false, upper = false, digit = false, symbol = false;
        for (char c : pw.toCharArray()) {
            if (Character.isLowerCase(c)) {
                lower = true;
            } else if (Character.isUpperCase(c)) {
                upper = true;
            } else if (Character.isDigit(c)) {
                digit = true;
            } else {
                symbol = true;
            }
        }
        int types = (lower ? 1 : 0) + (upper ? 1 : 0) + (digit ? 1 : 0) + (symbol ? 1 : 0);
        score += types * 15;
        if (types < 3) {
            r.issues.add("uses fewer than 3 character types");
        }

        // Penalty: common password
        if (COMMON.contains(pw.toLowerCase())) {
            score = Math.min(score, 10);
            r.issues.add("appears on common-password list");
        }

        // Penalty: same character repeated 3+ times in a row
        if (hasRepeatRun(pw, 3)) {
            score -= 15;
            r.issues.add("repeats a character 3+ times in a row");
        }

        // Penalty: only digits
        if (pw.chars().allMatch(Character::isDigit)) {
            score -= 20;
            r.issues.add("digits only");
        }

        score = Math.max(0, Math.min(100, score));
        r.score = score;
        r.rating = rate(score);
        return r;
    }

    /* True if any character repeats runlength or more times consecutively */
    static boolean hasRepeatRun(String s, int runlength) {
        int run = 1;
        for (int i = 1; i < s.length(); i++) {
            if (s.charAt(i) == s.charAt(i - 1)) {
                run++;
                if (run >= runlength) {
                    return true;
                }
            } else {
                run = 1;
            }
        }
        return false;
    }

    static String rate(int score) {
        if (score >= 80) return "STRONG";
        if (score >= 55) return "MODERATE";
        return "WEAK";
    }

    /* Masks a password for display so reports never show full values */
    static String mask(String pw) {
        if (pw.length() <= 2) return "**";
        return pw.charAt(0) + "*".repeat(pw.length() - 2) + pw.charAt(pw.length() - 1);
    }

    public static void main(String[] args) {
        if (args.length != 1) {
            System.out.println("Usage: java PasswordAudit <passwords-file>");
            return;
        }

        List<String> lines;
        try {
            lines = Files.readAllLines(Path.of(args[0]));
        } catch (IOException e) {
            System.out.println("Could not read file: " + e.getMessage());
            return;
        }

        int weak = 0, moderate = 0, strong = 0, total = 0;
        System.out.printf("%-22s %5s %-9s %s%n", "PASSWORD (masked)", "SCORE", "RATING", "ISSUES");
        System.out.println("-".repeat(78));

        for (String line : lines) {
            String pw = line.trim();
            if (pw.isEmpty()) continue;
            Result r = audit(pw);
            total++;
            switch (r.rating) {
                case "WEAK" -> weak++;
                case "MODERATE" -> moderate++;
                default -> strong++;
            }
            System.out.printf("%-22s %5s %-9s %s%n",
                    mask(r.password), r.score, r.rating, String.join("; ", r.issues));
        }

        System.out.printf("%nTotal passwords: %d%n", total);
        System.out.printf("Weak: %d, Moderate: %d, Strong: %d%n", weak, moderate, strong);
    }
}