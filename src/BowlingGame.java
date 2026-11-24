import java.util.Scanner;

public class BowlingGame {
    // One shared Scanner for the whole program (closing it in a method would also close System.in)
    static Scanner input = new Scanner(System.in);

    public static void main(String[] args) {
        mainMenu();
        input.close();
    }

    // Main menu (void & no parameter)
    public static void mainMenu() {
        System.out.println("       ____                ______  ___ __     \r\n" + //
                "      / __ )____ _      __/ / __ )/ (_) /_____\r\n" + //
                "     / __  / __ \\ | /| / / / __  / / / __/_  /\r\n" + //
                "    / /_/ / /_/ / |/ |/ / / /_/ / / / /_  / /_\r\n" + //
                "   /_____/\\____/|__/|__/_/_____/_/_/\\__/ /___/\r\n");

        System.out.println("-- Welcome to a Bowling Game Score Calculator --");
        System.out.println("");
        inputPlayer(); // call inputPlayer() in mainMenu()
    }

    // Input how many player (1, 2, 3, ...)
    public static void inputPlayer() {
        int totalPlayer = 0;

        while (totalPlayer < 1 || totalPlayer > 6) {
            System.out.print("Total player (max 6): ");
            if (input.hasNextInt()) {
                totalPlayer = input.nextInt();
            } else {
                input.next(); // throw away the invalid text
            }
            input.nextLine(); // clear the rest of the line

            if (totalPlayer < 1 || totalPlayer > 6) {
                System.out.println("Please enter a number from 1 to 6.");
            }
        }
        char[][][] frames = inputFrames(totalPlayer); // call inputFrames() in inputPlayer()
        calculateScores(frames); // call calculateScores() in inputPlayer()
    }

    // Input every throw of the 10 frames for every player
    // Returns frames[player][frame][throw]
    // Each throw is '0'-'9', '/' (spare), 'X' (strike) or ' ' (no throw)
    public static char[][][] inputFrames(int totalPlayer) {
        char[][][] frames = new char[totalPlayer][10][3];

        // Players take turns every frame: P1 frame 1 -> P2 frame 1 -> ... -> P1 frame 2 -> ...
        for (int f = 0; f < 10; f++) {
            for (int p = 0; p < totalPlayer; p++) {
                System.out.println("\n--- Frame " + (f + 1) + " | Player " + (p + 1) + " ---");
                boolean lastFrame = (f == 9);
                char t1, t2 = ' ', t3 = ' ';

                // Throw 1: 0-9 or X
                t1 = readThrow(f + 1, 1, true, false, 9);

                if (!lastFrame) {
                    // Frame 1-9: a strike ends the frame, otherwise throw again
                    if (t1 != 'X') {
                        t2 = readThrow(f + 1, 2, false, true, 9 - (t1 - '0'));
                    }
                } else {
                    // Frame 10: bonus throw(s) after a strike or a spare
                    if (t1 == 'X') {
                        t2 = readThrow(10, 2, true, false, 9);
                        if (t2 == 'X') {
                            t3 = readThrow(10, 3, true, false, 9);
                        } else {
                            t3 = readThrow(10, 3, false, true, 9 - (t2 - '0'));
                        }
                    } else {
                        t2 = readThrow(10, 2, false, true, 9 - (t1 - '0'));
                        if (t2 == '/') {
                            t3 = readThrow(10, 3, true, false, 9);
                        }
                    }
                }

                frames[p][f][0] = t1;
                frames[p][f][1] = t2;
                frames[p][f][2] = t3;
            }
        }
        return frames;
    }

    // Read ONE valid throw (keeps asking until the input is valid)
    // allowStrike : 'X' is allowed
    // allowSpare  : '/' is allowed
    // maxPins     : biggest digit allowed (0 - maxPins)
    public static char readThrow(int frame, int throwNo, boolean allowStrike, boolean allowSpare, int maxPins) {
        String options;
        if (maxPins == 0) {
            options = "0";
        } else {
            options = "0-" + maxPins;
        }
        if (allowStrike) {
            options += ", X";
        }
        if (allowSpare) {
            options += ", /";
        }

        while (true) {
            System.out.print("Frame " + frame + ", throw " + throwNo + " (" + options + "): ");
            String line = input.nextLine().trim().toUpperCase();

            if (line.length() == 1) {
                char c = line.charAt(0);
                if (c >= '0' && c <= '0' + maxPins) {
                    return c;
                }
                if (allowStrike && c == 'X') {
                    return c;
                }
                if (allowSpare && c == '/') {
                    return c;
                }
            }
            System.out.println("Invalid input, try again.");
        }
    }

    // Calculate and display the final score for each player
    public static void calculateScores(char[][][] frames) {
        System.out.println("\n=== FINAL SCORES ===");

        for (int p = 0; p < frames.length; p++) {
            // 1. Flatten all valid throws for this player into a single array (max 21 rolls)
            char[] rolls = new char[21];
            int rollCount = 0;
            for (int f = 0; f < 10; f++) {
                for (int t = 0; t < 3; t++) {
                    if (frames[p][f][t] != ' ') {
                        rolls[rollCount++] = frames[p][f][t];
                    }
                }
            }

            // 2. Calculate score frame by frame
            int totalScore = 0;
            int rollIndex = 0;

            for (int frame = 0; frame < 10; frame++) {
                if (rolls[rollIndex] == 'X') { // Strike
                    totalScore += 10 + getStrikeBonus(rolls, rollIndex);
                    rollIndex += 1;
                } else if (rolls[rollIndex + 1] == '/') { // Spare
                    totalScore += 10 + getRollValue(rolls, rollIndex + 2);
                    rollIndex += 2;
                } else { // Open frame
                    totalScore += getRollValue(rolls, rollIndex) + getRollValue(rolls, rollIndex + 1);
                    rollIndex += 2;
                }
            }

            System.out.println("Player " + (p + 1) + " Total Score: " + totalScore);
        }
    }

    // Helper: Gets the pin value of a single roll
    private static int getRollValue(char[] rolls, int index) {
        if (rolls[index] == 'X') return 10;
        return rolls[index] - '0';
    }

    // Helper: Gets the next two rolls after a strike (handles spares properly)
    private static int getStrikeBonus(char[] rolls, int strikeIndex) {
        int firstNext = getRollValue(rolls, strikeIndex + 1);
        if (rolls[strikeIndex + 2] == '/') {
            return 10; // Next two rolls equal 10 total
        }
        return firstNext + getRollValue(rolls, strikeIndex + 2);
    }
}

