import java.io.*;
import java.net.*;
import java.util.*;

public class Client extends ValidateIPv4 {

    // Define the size of the game board.
    private static final int BOARD_SIZE = 5;
    // Define characters representing different states of the board.
    private static final char WATER = '~';
    private static final char HIT = 'X';
    private static final char MISS = 'O';

    // The game board represented as a 2D array of characters.
    private final char[][] board;
    // Stores the count of remaining ships of each size.
    private final Map<Integer, Integer> remainingShips;
    // Keeps track of positions already shot at to prevent duplicate shots.
    private final Set<String> shotPositions;

    // Constructor to initialize the game board and other data structures.
    public Client() {
        this.board = new char[BOARD_SIZE][BOARD_SIZE];
        this.remainingShips = new HashMap<>();
        this.shotPositions = new HashSet<>();
        initializeBoard();
    }

    // Initializes the board with all positions set to WATER.
    private void initializeBoard() {
        for (int i = 0; i < BOARD_SIZE; i++) {
            Arrays.fill(board[i], WATER);
        }
    }

    // Displays the current state of the game board.
    private void displayBoard() {
        System.out.println("\n  0 1 2 3 4"); // Column numbers
        for (int i = 0; i < BOARD_SIZE; i++) {
            System.out.print(i + " "); // Row numbers
            for (int j = 0; j < BOARD_SIZE; j++) {
                System.out.print(board[i][j] + " "); // Print the state of each cell
            }
            System.out.println();
        }
        System.out.println();
        displayRemainingShips(); // Display the number of remaining ships
    }

    // Displays the count of remaining ships of each size.
    private void displayRemainingShips() {
        if (!remainingShips.isEmpty()) {
            System.out.println("Remaining ships:");
            remainingShips.forEach((size, count) ->
                System.out.println("Size " + size + ": " + count + " ship(s)")
            );
            System.out.println();
        }
    }

    // Gets ship configuration from the user.
    private List<Integer> getShipConfiguration(Scanner scanner) {
        List<Integer> shipSizes = new ArrayList<>();

        while (true) {
            try {
                System.out.println("Enter the number of ships (1-5):");
                int numShips = scanner.nextInt();

                // Validate the number of ships entered by the user.
                if (numShips <= 0 || numShips > 5) {
                    System.out.println(
                        "Please enter a number between 1 and 5."
                    );
                    continue;
                }

                System.out.println(
                    "Enter the size of each ship (1-" + BOARD_SIZE + "):"
                );
                // Prompt user for the size of each ship
                for (int i = 0; i < numShips; i++) {
                    System.out.print("Ship " + (i + 1) + " size: ");
                    int size = scanner.nextInt();
                    // Validate ship size.
                    if (size > 0 && size <= BOARD_SIZE) {
                        shipSizes.add(size);
                        remainingShips.merge(size, 1, Integer::sum); // increment the count of ships of the given size.
                    } else {
                        System.out.println(
                            "Invalid size. Please enter a number between 1 and " +
                            BOARD_SIZE
                        );
                        i--; // Decrement i to get the size for the current ship again
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

    // Processes the result of a shot fired.
    private boolean processShot(int row, int col, String result) {
        String[] parts = result.split(":");
        String shotResult = parts[0]; // Get the result of the shot.
        Integer shipSize = parts.length > 1 ? Integer.valueOf(parts[1]) : null; // Get the size of the ship sunk.

        switch (shotResult) {
            case "HIT" -> {
                System.out.println("Hit!");
                board[row][col] = HIT;
            }
            case "SUNK" -> {
                System.out.println("You sunk a ship of size " + shipSize + "!");
                board[row][col] = HIT;
                remainingShips.merge(shipSize, -1, Integer::sum);
                // If the last ship of the size is sunk, remove it from the remainingShips
                if (remainingShips.get(shipSize) <= 0) {
                    remainingShips.remove(shipSize);
                }
            }
            case "MISS" -> {
                System.out.println("Miss!");
                board[row][col] = MISS;
            }
            case "ALREADY_SHOT" -> {
                System.out.println("You already shot at this position!");
                return false; // Return false to ask again for the shot.
            }
            case "GAME_OVER" -> {
                System.out.println(
                    "Congratulations! You've sunk all the ships!"
                );
                board[row][col] = HIT;
                displayBoard();
                return true; // Return true to end the game loop.
            }
            case "INVALID" -> {
                System.out.println("Invalid coordinates!");
                return false;
            }
            default -> {
                System.out.println("Unknown response from server: " + result);
                return false;
            }
        }
        return false;
    }

    // Gets valid coordinates from the user.
    private String getValidCoordinates(Scanner scanner) {
        while (true) {
            System.out.println(
                "Enter coordinates (row,col) or 'quit' to exit:"
            );
            String input = scanner.nextLine().trim();

            if (input.equalsIgnoreCase("quit")) {
                return input;
            }

            try {
                String[] coordinates = input.split(",");
                // Validate the format of input
                if (coordinates.length != 2) {
                    System.out.println(
                        "Please enter both row and column coordinates separated by a comma."
                    );
                    continue;
                }

                int row = Integer.parseInt(coordinates[0].trim());
                int col = Integer.parseInt(coordinates[1].trim());

                // Validate the range of coordinates
                if (
                    row < 0 || row >= BOARD_SIZE || col < 0 || col >= BOARD_SIZE
                ) {
                    System.out.println(
                        "Coordinates must be between 0 and " + (BOARD_SIZE - 1)
                    );
                    continue;
                }

                String position = row + "," + col;
                // Prevent the user from shooting at the same coordinates twice
                if (shotPositions.contains(position)) {
                    System.out.println(
                        "You've already shot at this position. Try different coordinates."
                    );
                    continue;
                }

                shotPositions.add(position);
                return position;
            } catch (NumberFormatException e) {
                System.out.println(
                    "Please enter valid numbers for coordinates."
                );
            }
        }
    }

    // Main method to start the client application.
    public static void main(String[] args) {
        Scanner ipReader = new Scanner(System.in);
        String address = "placeholder";
        if (!isValidIPv4(address)) {
            System.out.println("Insert new IP: ");
            address = ipReader.nextLine();
        }
        int port = 5000; // Port to connect to
        Client gameClient = new Client();

        try (
            Socket clientSocket = new Socket(address, port); // create new socket to communicate with the server.
            PrintWriter out = new PrintWriter(
                clientSocket.getOutputStream(),
                true
            );
            BufferedReader in = new BufferedReader(
                new InputStreamReader(clientSocket.getInputStream())
            );
            Scanner scanner = new Scanner(System.in)
        ) {
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
            // Send ships configuration to server
            out.println(
                "SHIPS:" +
                String.join(
                    ",",
                    shipSizes
                        .stream()
                        .map(String::valueOf)
                        .toArray(String[]::new)
                )
            );

            System.out.println("\nGame started! The board shows:");
            System.out.println("~ : Water");
            System.out.println("X : Hit");
            System.out.println("O : Miss");

            // Start the main game loop.
            while (true) {
                gameClient.displayBoard();

                String coordinates = gameClient.getValidCoordinates(scanner);
                if (coordinates.equalsIgnoreCase("quit")) {
                    out.println("quit");
                    break;
                }

                out.println(coordinates); // Send the chosen coordinates to the server
                String result = in.readLine(); // Get response from the server

                // Process the shot result and check if the game is over.
                if (
                    gameClient.processShot(
                        Integer.parseInt(coordinates.split(",")[0].trim()),
                        Integer.parseInt(coordinates.split(",")[1].trim()),
                        result
                    )
                ) {
                    break;
                }
            }

            System.out.println("Thanks for playing Naval Battle!");
        } catch (ConnectException e) {
            System.out.println(
                "Could not connect to server. Please make sure the server is running."
            );
        } catch (IOException e) {
            System.out.println("Error during game: " + e.getMessage());
        }
    }
}
