import java.util.*;
import java.io.*;
import java.security.*;
import javax.crypto.*;
import javax.crypto.spec.*;

/**
 * Secure Voting System
 * This is the secure version of the voting system. It uses HMACs to protect the
 * integrity of both voter records and votes.
 * 
 * Files-
 * voters.txt - stores voter information, username, hashed password, hasVoted
 * flag, and HMAC of the record.
 * votes.txt - stores votes, username, candidate choice, and HMAC of the vote.
 * admin.txt - stores admin credentials, username and hashed password.
 * 
 * 
 * @author Heather Joyce and Kourtney Gray-Ford
 * @version Apr 18, 2026
 */
public class Secure {

    /**
     * Secret key used for HMAC functions.
     */

    static final String HMACSecretKey = "HeatherAndKourtney3200";

    static final String voterFile = "voters.txt";
    static final String voteFile = "votes.txt";
    static final String adminFile = "admin.txt";

    static Scanner scanner = new Scanner(System.in);

    public static void main(String[] args) {
        System.out.println("----------------------------------------");
        System.out.println("        K&H Secure Voting System        ");
        System.out.println("----------------------------------------");

        boolean running = true;

        while (running) {
            System.out.println("");
            System.out.println("\nMain Menu:");
            System.out.println("");
            System.out.println("    1. Regsiter as a voter");
            System.out.println("    2. Login in as a voter");
            System.out.println("    3. Login as Admin");
            System.out.println("    4. Quit");
            System.out.println("");
            System.out.print("Choose an option:   ");
            String choice = scanner.nextLine().trim();

            switch (choice) {
                case "1":
                    registerVoter();
                    break;
                case "2":
                    voterLogin();
                    break;
                case "3":
                    adminLogin();
                    break;
                case "3200":
                    createAdmin();
                    break;
                case "4":
                    running = false;
                    break;
                default:
                    System.out.print("Invalid option choosen, please pick number 1-4");
            }

        }
        System.out.println("Thank you. Goodbye!");
    }

    /**
     * Regsiteration Function
     * Saves: username, SHA256 password, hasVoted?
     */

    static void registerVoter() {
        System.out.println("\n--- Voter Registration ---");
        System.out.println("");
        System.out.print("Please enter username: ");
        String username = scanner.nextLine().trim();

        if (voterExists(username)) {
            System.out.println("Username take. Please try another");

            return;
        }

        System.out.print("Enter password: ");
        String password = scanner.nextLine().trim();
        String hashedPassword = sha256(password);
        if (hashedPassword == null) {
            System.out.println("Password Error. Registration failed");

            return;
        }

        String voterRecord = username + "|" + hashedPassword + "|false";
        String voterHMAC = generateHMAC(voterRecord, HMACSecretKey);
        if (voterHMAC == null) {
            System.out.println("Error registration failed");
        }

        // Saving the voter info to voter.txt
        try (FileWriter fw = new FileWriter(voterFile, true)) {
            fw.write(username + "," + hashedPassword + ",false," + voterHMAC + "\n");
            System.out.print("Registration Complete! You can now login and cast your vote.");
        } catch (IOException e) {
            System.out.print("error: " + e.getMessage());

        }
    }

    /**
     * Voter Login
     * Takes entered password and hashes it, compares that value to stored hash.
     */

    static void voterLogin() {
        System.out.println("\n --- Voter Login ---");
        System.out.println("");
        System.out.print("Username: ");
        String username = scanner.nextLine().trim();
        System.out.print("Password: ");
        String password = scanner.nextLine().trim();

        String hashedInput = sha256(password);
        String[] voterData = findVoter(username);

        if (voterData == null || !voterData[1].equals(hashedInput)) {
            System.out.println("Invalid username or password");
            return;
        }
        if (voterData[2].equals("true")) {
            System.out.println("You have already voted. Voters can only vote once.");
            return;
        }
        System.out.println("Login Successful! Welcome, " + username + "!");
        castVote(username);
        markVoterAsVoted(username);

    }

    /**
     * Cast Vote
     * Generates HAMC over voterID|Candidate and stores that along side vote.
     */
    static void castVote(String username) {
        System.out.println("\n --- Cast Your Vote---");
        System.out.println("");
        System.out.println("Candidates:");
        System.out.println("");
        System.out.println("1. Alice");
        System.out.println("2. Bob");
        System.out.println("3. Evil Mallory");
        System.out.print("Enter candidate number: ");
        String choice = scanner.nextLine().trim();

        String candidate;
        if (choice.equals("1")) {
            candidate = "Alice";
        } else if (choice.equals("2")) {
            candidate = "Bob";
        } else if (choice.equals("3")) {
            candidate = "Evil Mallory";
        } else {
            System.out.println("Invalid Choice, cast again");
            return;
        }

        String protectedVote = username + "|" + candidate;

        String hmac = generateHMAC(protectedVote, HMACSecretKey);
        if (hmac == null) {
            System.out.println("Error creating vote signature. Please try again");
            return;
        }
        // Saving vote to vote.txt
        try (FileWriter fw = new FileWriter(voteFile, true)) {
            fw.write(username + "," + candidate + "," + hmac + "\n");
        } catch (IOException e) {
            System.out.println("Error: " + e.getMessage());
            return;
        }

    }

    /**
     * Admin Login
     */

    static void adminLogin() {
        System.out.println("");
        System.out.println("\n ---Admin Login---");
        System.out.print("Admin Username: ");
        String username = scanner.nextLine().trim();
        System.out.print("Admin Password: ");
        String password = scanner.nextLine().trim();

        String hashedInput = sha256(password);
        String[] adminData = findAdmin(username);

        if (adminData == null || !adminData[1].equals(hashedInput)) {
            System.out.println("DENIED: Invalid admin credentials.");
            return;
        }

        System.out.print("Admin login in successful!");
        adminMenu();
    }

    static void adminMenu() {
        boolean inMenu = true;
        while (inMenu) {
            System.out.println("");

            System.out.println("\n --- Super Secrete and Confidential Admin Menu ---");
            System.out.println("");
            System.out.println("1. View verifed election results");
            System.out.println("2. Back to main menu");
            System.out.println("");
            System.out.print("Choose an option: ");
            String choice = scanner.nextLine().trim();

            switch (choice) {
                case "1":
                    viewResults();
                    break;
                case "2":
                    inMenu = false;
                    break;
                default:
                    System.out.println("Invalid Choice.");
            }
        }
    }

    /**
     * View Results
     * Reverifies the HMAC of every vote
     * Any vote whos HMAC does not match is flagged and does not count.
     */
    static void viewResults() {
        System.out.println("");
        System.out.print("\n --- Election results---");
        System.out.println("");

        File VOTES = new File(voteFile);
        if (!VOTES.exists()) {
            System.out.println("No votes have been made");
            return;
        }

        int aliceCount = 0;
        int bobCount = 0;
        int MalloryCount = 0;
        int tamperedCount = 0;
        Set<String> seenVoters = new HashSet<>();

        try (BufferedReader br = new BufferedReader(new FileReader(voteFile))) {
            String line;
            while ((line = br.readLine()) != null) {
                if (line.isBlank())
                    continue;

                String[] parts = line.split(",");
                if (parts.length != 3) {
                    System.out.println("WARNING! Malformed vote entry");
                    tamperedCount++;
                    continue;
                }
                String voterID = parts[0];
                String candidate = parts[1];
                String storedHMAC = parts[2];

                String expectedHMAC = generateHMAC(voterID + "|" + candidate, HMACSecretKey);

                if (expectedHMAC == null || !expectedHMAC.equals(storedHMAC)) {
                    System.out.println("");

                    System.out.println("TAMPERED VOTE FROM \"" + voterID + "\" failed integrity!");
                    tamperedCount++;
                    continue;
                }

                if (seenVoters.contains(voterID)) {
                    System.out.println("  [DUPLICATE] Vote from \"" + voterID + "\" already counted. Skipping.");
                    tamperedCount++;
                    continue;
                }
                seenVoters.add(voterID);

                if (candidate.equals("Alice"))
                    aliceCount++;
                else if (candidate.equals("Bob"))
                    bobCount++;
                else if (candidate.equals("Evil Mallory"))
                    MalloryCount++;
            }
        } catch (IOException e) {
            System.out.print("Error reading votes: " + e.getMessage());
            return;
        }

        System.out.println("\nResults: ");
        System.out.println("");

        System.out.println("  Alice : " + aliceCount + " vote(s)");
        System.out.println("  Bob : " + bobCount + " vote(s)");
        System.out.println("  Evil Mallory : " + MalloryCount + " vote(s)");
        if (tamperedCount > 0) {
            System.out.println("");
            System.out.println(" ! " + tamperedCount + " votes(s) were flagged and not counted");
        } else {
            System.out.println("All votes passed integrity checks");
        }
    }

    /**
     * Helper Function
     * SHA 256
     * returns hex string of hash
     */

    static String sha256(String input) {
        try {
            MessageDigest md = MessageDigest.getInstance("SHA-256");
            byte[] hashBytes = md.digest(input.getBytes("UTF-8"));
            StringBuilder sb = new StringBuilder();
            for (byte b : hashBytes) {
                sb.append(String.format("%02x", b));
            }
            return sb.toString();
        } catch (Exception e) {
            return null;
        }
    }

    /**
     * Helper Function
     * HMAC generation
     * Returns a hex string of the HMAC
     */
    static String generateHMAC(String data, String key) {
        try {
            Mac mac = Mac.getInstance("HmacSHA256");
            SecretKeySpec secretkey = new SecretKeySpec(key.getBytes("UTF-8"), "HmacSHA256");
            mac.init(secretkey);
            byte[] hmacBytes = mac.doFinal(data.getBytes("UTF-8"));
            StringBuilder sb = new StringBuilder();
            for (byte b : hmacBytes) {
                sb.append(String.format("%02x", b));
            }
            return sb.toString();
        } catch (Exception e) {
            return null;
        }

    }

    /**
     * Helper Function
     * checks if a voter username exists
     */
    static boolean voterExists(String username) {
        return findVoter(username) != null;
    }

    /**
     * Helper Function
     * finds a voter by username
     * returns [username,hashedPassword,hasVoted]
     */
    static String[] findVoter(String username) {
        File file = new File(voterFile);
        if (!file.exists())
            return null;
        try (BufferedReader br = new BufferedReader(new FileReader(file))) {
            String line;
            while ((line = br.readLine()) != null) {
                String[] parts = line.split(",");
                if (parts.length == 4 && parts[0].equals(username)) {
                    String storedUsername = parts[0];
                    String storedHashPass = parts[1];
                    String storedHasVoted = parts[2];
                    String storedHMAC = parts[3];

                    String expectedHMAC = generateHMAC(storedUsername + "|" + storedHashPass + "|" + storedHasVoted,
                            HMACSecretKey);

                    if (expectedHMAC == null || !expectedHMAC.equals(storedHMAC)) {
                        System.out.println("TAMPERED VOTER RECORD FOR " + username + " HAS FAILED INTEGRITY CHECK");
                        return null;
                    }
                    return new String[] { storedUsername, storedHashPass, storedHasVoted };
                }

            }
        } catch (IOException e) {
        }
        return null;
    }

    /**
     * Helper Function
     * Update a voters hasVoted variable
     */
    static void markVoterAsVoted(String username) {
        File file = new File(voterFile);
        List<String> lines = new ArrayList<>();
        try (BufferedReader br = new BufferedReader(new FileReader(file))) {
            String line;
            while ((line = br.readLine()) != null) {
                String[] parts = line.split(",");
                if (parts.length == 3 && parts[0].equals(username)) {
                    lines.add(parts[0] + "," + parts[1] + ",true");
                } else {
                    lines.add(line);
                }
            }
        } catch (IOException e) {
            System.out.println("Error" + e.getMessage());
            return;
        }
        try (FileWriter fw = new FileWriter(file, false)) {
            for (String l : lines)
                fw.write(l + "\n");
        } catch (IOException e) {
            System.out.println("Error updating voter file." + e.getMessage());
        }
    }

    /**
     * Helper Function
     * finds Admins by username
     * returns [username, hashedPassword]
     */

    static String[] findAdmin(String username) {
        File file = new File(adminFile);
        if (!file.exists())
            return null;
        try (BufferedReader br = new BufferedReader(new FileReader(file))) {
            String line;
            while ((line = br.readLine()) != null) {
                String[] parts = line.split(",");
                if (parts.length == 2 && parts[0].equals(username)) {
                    return parts;
                }
            }
        } catch (IOException e) {
        }
        return null;
    }

    /**
     * Secrete Admin Creation Function
     * This function would typically not be included in a real system, but it is
     * here to allow us to create an admin account without having to manually edit
     * the admin file.
     * 
     * It works exactly like the voter registration function, but it saves to the
     * admin file.
     */
    static void createAdmin() {
        System.out.println("\n--- Admin Registration ---");
        System.out.println("");

        System.out.print("Please enter username: ");
        String username = scanner.nextLine().trim();

        if (voterExists(username)) {
            System.out.print("Username take. Please try another");

            return;
        }

        System.out.print("Enter password: ");
        String password = scanner.nextLine().trim();
        String hashedPassword = sha256(password);
        if (hashedPassword == null) {
            System.out.println("Password Error. Registration failed");

            return;
        }
        // Saving the voter info to Admin.txt
        try (FileWriter fw = new FileWriter(adminFile, true)) {
            fw.write(username + "," + hashedPassword + "\n");
            System.out.print("Registration Complete! You can now login as an admin.");
        } catch (IOException e) {
            System.out.print("error: " + e.getMessage());

        }
    }

}
