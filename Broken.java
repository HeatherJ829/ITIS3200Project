import java.util.*;
import java.io.*;
import java.security.*;
import javax.crypto.*;
import javax.crypto.spec.*;

/**
 * Broken Voting System
 * This is the broken version of the voting system. It does not use HMACs to
 * protect the integrity of votes, so votes can be tampered with undetectably.
 * 
 * There is no way for this system to test the integrity of the votes, so all
 * votes are counted as is, without any notification that some may be tampered.
 * 
 * Files-
 * votersBROKEN.txt - stores voter information, username, hashed password,
 * hasVoted flag.
 * votesBROKEN.txt - stores votes, username and candidate choice. No integrity
 * protection, so votes can be tampered with.
 * adminBROKEN.txt - stores admin credentials, username and hashed password.
 * 
 * @author Heather Joyce and Kourtney Gray-Ford
 * @version Apr 18, 2026
 */

public class Broken {
    static final String voterBroken = "votersBROKEN.txt";
    static final String votesBroken = "votesBROKEN.txt";
    static final String adminBroken = "adminBROKEN.txt";

    static Scanner scanner = new Scanner(System.in);

    public static void main(String[] args) {
        System.out.println("----------------------------------------");
        System.out.println("      K&H Insecure Voting System        ");
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
     * Regsiteration Function SAME AS SECURE VERSION
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
        // Saving the voter info to voter.txt
        try (FileWriter fw = new FileWriter(voterBroken, true)) {
            fw.write(username + "," + hashedPassword + ",false\n");
            System.out.print("Registration Complete! You can now login and cast your vote.");
        } catch (IOException e) {
            System.out.print("error: " + e.getMessage());

        }
    }

    /**
     * Voter Login SAME AS SECURE VERSION
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
     * Broken Version of castVote()
     * Votes are not saved with an HMAC tag, so there is no way to know if the votes
     * have beeen tampered with.
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

        // Broken portion, vote is stored with no integrity protection.
        try (FileWriter fw = new FileWriter(votesBroken, true)) {
            fw.write(username + "," + candidate + "\n");
        } catch (IOException e) {
            System.out.println("Error saving vote: " + e.getMessage());
            return;
        }
        markVoterAsVoted(username);

    }

    /**
     * Admin Login SAME AS SECURE VERSION
     * Takes entered password and hashes it, compares that value to stored hash.
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
     * View Results BROKEN VERSION
     * Counts votes without checking the integrity.
     * All votes including tampered votes are counted as valid votes.
     */
    static void viewResults() {
        System.out.println("");
        System.out.println("\n         --- Unverified Election results---");
        System.out.println("WARNING THESE VOTES ARE NOT VERIFIED, SOME MAY BE TAMPERED WITH");
        System.out.println("");

        File VOTES = new File(votesBroken);
        if (!VOTES.exists()) {
            System.out.println("No votes have been made");
            return;
        }

        int aliceCount = 0;
        int bobCount = 0;
        int MalloryCount = 0;

        try (BufferedReader br = new BufferedReader(new FileReader(votesBroken))) {
            String line;
            while ((line = br.readLine()) != null) {
                if (line.isBlank())
                    continue;

                String[] parts = line.split(",");
                if (parts.length < 2)
                    continue;

                String candidate = parts[1];

                if (candidate.equals("Alice"))
                    aliceCount++;
                else if (candidate.equals("Bob"))
                    bobCount++;
                else if (candidate.equals("Evil Mallory"))
                    MalloryCount++;
            }
        } catch (IOException e) {
            System.out.println("Error reading votes: " + e.getMessage());
            return;
        }
        System.out.println("Results:");
        System.out.println("");

        System.out.println("Alice: " + aliceCount + " votes");
        System.out.println("Bob: " + bobCount + " votes");
        System.out.println("Evil Mallory: " + MalloryCount + " votes");
        System.out.println("");
        System.err.println("These votes have not been verified, all are counted as is.");

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
        File file = new File(voterBroken);
        if (!file.exists())
            return null;
        try (BufferedReader br = new BufferedReader(new FileReader(file))) {
            String line;
            while ((line = br.readLine()) != null) {
                String[] parts = line.split(",");
                if (parts.length == 3 && parts[0].equals(username)) {
                    return parts;
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
        File file = new File(voterBroken);
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
        File file = new File(adminBroken);
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
     * Secrete Admin Creation Function SAME AS SECURE VERSION
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
        // Saving the voter info to adminBROKEN.txt
        try (FileWriter fw = new FileWriter(adminBroken, true)) {
            fw.write(username + "," + hashedPassword + "\n");
            System.out.print("Registration Complete! You can now login as an admin.");
        } catch (IOException e) {
            System.out.print("error: " + e.getMessage());

        }
    }

}
