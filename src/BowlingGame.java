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
        initializeFrames(frames);

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

                displayLiveScores(frames, p, f);
            }
        }
        return frames;
    }

    // Fill every throw with a blank value so live scoring can safely ignore future frames.
    public static void initializeFrames(char[][][] frames) {
        for (int p = 0; p < frames.length; p++) {
            for (int f = 0; f < 10; f++) {
                for (int t = 0; t < 3; t++) {
                    frames[p][f][t] = ' ';
                }
            }
        }
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

    // Calculate and return the final score for each player
    public static int[] calculateScores(char[][][] frames) {
        int[] finalScores = new int[frames.length];

        for (int p = 0; p < frames.length; p++) {
        // 1. Flatten all valid throws for this player into a single array (max 21 rolls)
            char[] rolls = new char[21];
            int rollCount = 0;
            for (int f = 0; f < 10; f++) {
                for (int t = 0; t < 3; t++) {
                    char throwChar = frames[p][f][t];
                    if (throwChar != ' ' && throwChar != '\0') {
                        rolls[rollCount++] = throwChar;
                    }
                }
            }

            // 2. Calculate score frame by frame
            int totalScore = 0;
            int rollIndex = 0;
            for (int frame = 0; frame < 10; frame++) {
                if (rolls[rollIndex] == 'X') {
                // Strike: 10 + next two rolls
                    int bonus = 0;
                    if (rolls[rollIndex + 2] == '/') {
                        bonus = 10;
                    } else {
                        if (rolls[rollIndex + 1] == 'X') bonus += 10;
                        else if (rolls[rollIndex + 1] != '-') bonus += rolls[rollIndex + 1] - '0';

                        if (rolls[rollIndex + 2] == 'X') bonus += 10;
                        else if (rolls[rollIndex + 2] != '-') bonus += rolls[rollIndex + 2] - '0';
                    }

                    totalScore += 10 + bonus;
                    rollIndex += 1;

                } else if (rolls[rollIndex + 1] == '/') {
                // Spare: 10 + next single roll
                    int bonus = 0;
                    if (rolls[rollIndex + 2] == 'X') bonus = 10;
                    else if (rolls[rollIndex + 2] != '-') bonus = rolls[rollIndex + 2] - '0';

                    totalScore += 10 + bonus;
                    rollIndex += 2;

                } else {
                // Open frame: sum of the two rolls
                    int firstRoll = 0;
                    if (rolls[rollIndex] != '-') {
                        firstRoll = rolls[rollIndex] - '0';
                    }

                    int secondRoll = 0;
                    if (rolls[rollIndex + 1] != '-') {
                        secondRoll = rolls[rollIndex + 1] - '0';
                    }

                    totalScore += firstRoll + secondRoll;
                    rollIndex += 2;
                }
            }

            // Store this player's final score in the array
            finalScores[p] = totalScore;
        }

        return finalScores;
    }

    // Display a ranked live scoreboard after each player completes a frame.
    public static void displayLiveScores(char[][][] frames, int currentPlayer, int currentFrame) {
        int totalPlayers = frames.length;
        int[] scores = new int[totalPlayers];
        int[] completedFrames = new int[totalPlayers];
        boolean[] pendingBonus = new boolean[totalPlayers];
        int leaderScore = 0;
        int leader = 0;

        for (int p = 0; p < totalPlayers; p++) {
            int[] progress = calculateLiveProgress(frames[p]);
            scores[p] = progress[0];
            completedFrames[p] = progress[1];
            pendingBonus[p] = (progress[2] == 1);

            if (p == 0 || scores[p] > leaderScore) {
                leaderScore = scores[p];
                leader = p;
            }
        }

        System.out.println("\n=================================================");
        System.out.println("=== LIVE SCOREBOARD | After Player " + (currentPlayer + 1)
                + ", Frame " + (currentFrame + 1) + " ===");
        System.out.println("Rank | Player | Frame | Score | Status");
        System.out.println("---------------------------------------");

        for (int p = 0; p < totalPlayers; p++) {
            int rank = 1;
            for (int other = 0; other < totalPlayers; other++) {
                if (scores[other] > scores[p]) {
                    rank++;
                }
            }

            String marker = (p == currentPlayer) ? " <- current" : "";
            String status;
            if (p == leader) {
                status = "Leader";
            } else {
                status = (leaderScore - scores[p]) + " behind";
            }
            if (pendingBonus[p]) {
                status += ", bonus pending";
            }

            System.out.printf("%4d | P%-5d | %5d | %5d | %s%s%n",
                    rank, (p + 1), completedFrames[p], scores[p], status, marker);
        }
        System.out.println("=================================================\n");
    }

    // Returns {confirmed score, scored frames, has pending strike/spare bonus}.
    public static int[] calculateLiveProgress(char[][] playerFrames) {
        int totalScore = 0;
        int scoredFrames = 0;
        boolean pendingBonus = false;

        for (int frame = 0; frame < 10; frame++) {
            char first = playerFrames[frame][0];
            char second = playerFrames[frame][1];
            char third = playerFrames[frame][2];

            if (first == ' ') {
                break;
            }

            if (frame == 9) {
                int tenthScore = calculateTenthFrameScore(first, second, third);
                if (tenthScore < 0) {
                    pendingBonus = true;
                    break;
                }
                totalScore += tenthScore;
                scoredFrames++;
                break;
            }

            if (first == 'X') {
                int[] bonusRolls = new int[2];
                if (collectNextRollValues(playerFrames, frame + 1, bonusRolls, 2) < 2) {
                    pendingBonus = true;
                    break;
                }
                totalScore += 10 + bonusRolls[0] + bonusRolls[1];
                scoredFrames++;
            } else if (second == '/') {
                int[] bonusRolls = new int[1];
                if (collectNextRollValues(playerFrames, frame + 1, bonusRolls, 1) < 1) {
                    pendingBonus = true;
                    break;
                }
                totalScore += 10 + bonusRolls[0];
                scoredFrames++;
            } else if (second != ' ') {
                totalScore += getThrowValue(first, ' ') + getThrowValue(second, first);
                scoredFrames++;
            } else {
                pendingBonus = true;
                break;
            }
        }

        return new int[]{totalScore, scoredFrames, pendingBonus ? 1 : 0};
    }

    public static int calculateTenthFrameScore(char first, char second, char third) {
        if (first == 'X') {
            if (second == ' ' || third == ' ') {
                return -1;
            }
            return 10 + getThrowValue(second, first) + getThrowValue(third, second);
        }
        if (second == ' ') {
            return -1;
        }
        if (second == '/') {
            if (third == ' ') {
                return -1;
            }
            return 10 + getThrowValue(third, second);
        }
        return getThrowValue(first, ' ') + getThrowValue(second, first);
    }

    public static int collectNextRollValues(char[][] playerFrames, int startFrame, int[] values, int needed) {
        int count = 0;
        for (int f = startFrame; f < 10 && count < needed; f++) {
            char previous = ' ';
            for (int t = 0; t < 3 && count < needed; t++) {
                char roll = playerFrames[f][t];
                if (roll == ' ') {
                    continue;
                }
                values[count++] = getThrowValue(roll, previous);
                previous = roll;
            }
        }
        return count;
    }

    public static int getThrowValue(char roll, char previousRoll) {
        if (roll == 'X') {
            return 10;
        }
        if (roll == '/') {
            return 10 - getThrowValue(previousRoll, ' ');
        }
        return roll - '0';
    }

   
}

