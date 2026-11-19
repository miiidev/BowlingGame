import java.util.Scanner;
public class BowlingGame {
    public static void main(String[] args) {
        mainMenu();
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
        int totalPlayer;
        Scanner input = new Scanner(System.in);
        System.out.print("Total player (max 6): ");
        totalPlayer = input.nextInt();
        input.close();
    }
}
