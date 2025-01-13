import java.io.*;
import java.net.*;
import java.util.*;

public class Client {
    private static final int BOARD_SIZE = 5;
    private static final char WATER = '~';
    private static final char HIT = 'X';
    private static final char MISS = 'O';

    private final char[][] board;
    private final Map<Integer, Integer> remainingShips;
    private final Set<String> shotPositions;

    public Client() {
        this.board = new char[BOARD_SIZE][BOARD_SIZE];
        this.remainingShips = new HashMap<>();
        this.shotPositions = new HashSet<>();
        initializeBoard();
    }

    private void initializeBoard() {
        for (int i = 0; i < BOARD_SIZE; i++) {
            Arrays.fill(board[i], WATER);
        }
    }

    private void displayBoard() {
        System.out.println("\n  0 1 2 3 4");
        for (int i = 0; i < BOARD_SIZE; i++) {
            System.out.print(i + " ");
            for (int j = 0; j < BOARD_SIZE; j++) {
                System.out.print(board[i][j] + " ");
            }
            System.out.println();
        }
        System.out.println();
        displayRemainingShips();
    }

    private void displayRemainingShips() {
        if (!remainingShips.isEmpty()) {
            System.out.println("Remaining ships:");
            remainingShips.forEach((size, count) ->
                System.out.println("Size " + size + ": " + count + " ship(s)"));
            System.out.println();
        }
    }

    private List<Integer> getShipConfiguration(Scanner scanner) {
        List<Integer> shipSizes = new ArrayList<>();

        while (true) {
            try {
                System.out.println("Enter the number of ships (1-5):");
                int numShips = scanner.nextInt();

                if (numShips <= 0 || numShips > 5) {
                    System.out.println("Please enter a number between 1 and 5.");
                    continue;
                }

                System.out.println("Enter the size of each ship (1-" + BOARD_SIZE + "):");
                for (int i = 0; i < numShips; i++) {
                    System.out.print("Ship " + (i + 1) + " size: ");
                    int size = scanner.nextInt();
                    if (size > 0 && size <= BOARD_SIZE) {
                        shipSizes.add(size);
                        remainingShips.merge(size, 1, Integer::sum);
                    } else {
                        System.out.println("Invalid size. Please enter a number between 1 and " + BOARD_SIZE);
                        i--;
                    }
                }
                scanner.nextLine(); // Consume newline
                break;
            } catch (InputMismatchException e) {
                System.out.println("Please enter valid numbers!");
                scanner.nextLine(); // Clear the invalid input
            }
        }

        return shipSizes;
    }

    private boolean processShot(int row, int col, String result) {
        String[] parts = result.split(":");
        String shotResult = parts[0];
        Integer shipSize = parts.length > 1 ? Integer.parseInt(parts[1]) : null;

        switch (shotResult) {
            case "HIT":
                System.out.println("Hit!");
                board[row][col] = HIT;
                break;
            case "SUNK":
                System.out.println("You sunk a ship of size " + shipSize + "!");
                board[row][col] = HIT;
                remainingShips.merge(shipSize, -1, Integer::sum);
                if (remainingShips.get(shipSize) <= 0) {
                    remainingShips.remove(shipSize);
                }
                break;
            case "MISS":
                System.out.println("Miss!");
                board[row][col] = MISS;
                break;
            case "ALREADY_SHOT":
                System.out.println("You already shot at this position!");
                return false;
            case "GAME_OVER":
                System.out.println("Congratulations! You've sunk all the ships!");
                board[row][col] = HIT;
                displayBoard();
                return true;
            case "INVALID":
                System.out.println("Invalid coordinates!");
                return false;
            default:
                System.out.println("Unknown response from server: " + result);
                return false;
        }
        return false;
    }

    private String getValidCoordinates(Scanner scanner) {
        while (true) {
            System.out.println("Enter coordinates (row,col) or 'quit' to exit:");
            String input = scanner.nextLine().trim();

            if (input.equalsIgnoreCase("quit")) {
                return input;
            }

            try {
                String[] coordinates = input.split(",");
                if (coordinates.length != 2) {
                    System.out.println("Please enter both row and column coordinates separated by a comma.");
                    continue;
                }

                int row = Integer.parseInt(coordinates[0].trim());
                int col = Integer.parseInt(coordinates[1].trim());

                if (row < 0 || row >= BOARD_SIZE || col < 0 || col >= BOARD_SIZE) {
                    System.out.println("Coordinates must be between 0 and " + (BOARD_SIZE - 1));
                    continue;
                }

                String position = row + "," + col;
                if (shotPositions.contains(position)) {
                    System.out.println("You've already shot at this position. Try different coordinates.");
                    continue;
                }

                shotPositions.add(position);
                return position;
            } catch (NumberFormatException e) {
                System.out.println("Please enter valid numbers for coordinates.");
            }
        }
    }

    public static void main(String[] args) {
        String address = "localhost";
        int port = 5000;
        Client gameClient = new Client();

        try (Socket clientSocket = new Socket(address, port);
             PrintWriter out = new PrintWriter(clientSocket.getOutputStream(), true);
             BufferedReader in = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
             Scanner scanner = new Scanner(System.in)) {

            String welcome = in.readLine();
            if (!welcome.equals("WELCOME")) {
                System.out.println("Failed to connect to server.");
                return;
            }

            System.out.println("Connected to Naval Battle server!");
            System.out.println("Welcome to Naval Battle!");
            System.out.println("------------------------");

            // Get ship configuration from user
            List<Integer> shipSizes = gameClient.getShipConfiguration(scanner);
            out.println("SHIPS:" + String.join(",", shipSizes.stream().map(String::valueOf).toArray(String[]::new)));

            System.out.println("\nGame started! The board shows:");
            System.out.println("~ : Water");
            System.out.println("X : Hit");
            System.out.println("O : Miss");

            while (true) {
                gameClient.displayBoard();

                String coordinates = gameClient.getValidCoordinates(scanner);
                if (coordinates.equalsIgnoreCase("quit")) {
                    out.println("quit");
                    break;
                }

                out.println(coordinates);
                String result = in.readLine();

                if (gameClient.processShot(
                    Integer.parseInt(coordinates.split(",")[0].trim()),
                    Integer.parseInt(coordinates.split(",")[1].trim()),
                    result)) {
                    break;
                }
            }

            System.out.println("Thanks for playing Naval Battle!");

        } catch (ConnectException e) {
            System.out.println("Could not connect to server. Please make sure the server is running.");
        } catch (IOException e) {
            System.out.println("Error during game: " + e.getMessage());
        }
    }
}
