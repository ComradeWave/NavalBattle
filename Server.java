import java.io.*;
import java.net.*;
import java.util.*;

public class Server {

    // Define the size of the game board.
    private static final int BOARD_SIZE = 5;
    // Define characters representing different states of the board.
    private static final char WATER = '~';
    private static final char SHIP = 'S';
    private static final char HIT = 'X';
    private static final char MISS = 'O';

    // The game board represented as a 2D array of characters.
    private final char[][] board;
    // List to store all ships.
    private final List<Ship> ships;
    // Random number generator for ship placement.
    private final Random random;

    // Constructor to initialize the server with ship sizes.
    public Server(List<Integer> shipSizes) {
        this.board = new char[BOARD_SIZE][BOARD_SIZE];
        this.ships = new ArrayList<>();
        this.random = new Random();

        initializeBoard();
        // Attempt to place ships on the board, throw exception if placement fails.
        if (!placeShips(shipSizes)) {
            throw new IllegalArgumentException(
                "Cannot place all ships on the board. Try with fewer or smaller ships."
            );
        }
        printBoard();
    }

    // Initializes the board with all positions set to WATER.
    private void initializeBoard() {
        for (int i = 0; i < BOARD_SIZE; i++) {
            Arrays.fill(board[i], WATER);
        }
    }

    // Attempts to place ships of given sizes on the board.
    private boolean placeShips(List<Integer> shipSizes) {
        // Sort ships by size in descending order for better placement
        shipSizes.sort(Collections.reverseOrder());

        // Validate total ship size against board size
        int totalShipSize = shipSizes
            .stream()
            .mapToInt(Integer::intValue)
            .sum();
        if (totalShipSize > BOARD_SIZE * BOARD_SIZE) {
            return false;
        }

        int attempts = 0;
        while (attempts < 100) {
            boolean success = true;
            ships.clear();
            initializeBoard();

            for (int size : shipSizes) {
                if (!tryPlaceShip(size)) {
                    success = false;
                    break;
                }
            }

            if (success) {
                return true;
            }
            attempts++;
        }
        return false;
    }

    // Attempts to place a single ship of a given size on the board.
    private boolean tryPlaceShip(int size) {
        if (size > BOARD_SIZE) {
            return false;
        }

        List<PlacementOption> options = new ArrayList<>();

        // Generate all possible placements
        for (int row = 0; row < BOARD_SIZE; row++) {
            for (int col = 0; col < BOARD_SIZE; col++) {
                for (boolean horizontal : new boolean[] { true, false }) {
                    if (canPlaceShip(row, col, size, horizontal)) {
                        options.add(new PlacementOption(row, col, horizontal));
                    }
                }
            }
        }

        if (options.isEmpty()) {
            return false;
        }

        // Choose a random valid placement
        PlacementOption chosen = options.get(random.nextInt(options.size()));
        Ship newShip = new Ship(
            size,
            chosen.horizontal,
            chosen.row,
            chosen.col
        );
        ships.add(newShip);
        placeShipOnBoard(newShip);
        return true;
    }

    // Inner class to represent a ship placement option.
    private static class PlacementOption {

        final int row;
        final int col;
        final boolean horizontal;

        PlacementOption(int row, int col, boolean horizontal) {
            this.row = row;
            this.col = col;
            this.horizontal = horizontal;
        }
    }

    // Checks if a ship can be placed at a given position.
    private boolean canPlaceShip(
        int row,
        int col,
        int size,
        boolean isHorizontal
    ) {
        // Check bounds
        if (isHorizontal && col + size > BOARD_SIZE) return false;
        if (!isHorizontal && row + size > BOARD_SIZE) return false;

        // Check if space is free and no adjacent ships
        for (
            int r = Math.max(0, row - 1);
            r <= Math.min(BOARD_SIZE - 1, row + (isHorizontal ? 1 : size));
            r++
        ) {
            for (
                int c = Math.max(0, col - 1);
                c <= Math.min(BOARD_SIZE - 1, col + (isHorizontal ? size : 1));
                c++
            ) {
                if (board[r][c] != WATER) {
                    return false;
                }
            }
        }

        return true;
    }

    // Places a ship on the board.
    private void placeShipOnBoard(Ship ship) {
        if (ship.isHorizontal()) {
            for (int i = 0; i < ship.getSize(); i++) {
                board[ship.getRow()][ship.getCol() + i] = SHIP;
            }
        } else {
            for (int i = 0; i < ship.getSize(); i++) {
                board[ship.getRow() + i][ship.getCol()] = SHIP;
            }
        }
    }

    // Processes a shot fired at the given coordinates.
    private ShotResult processShot(int row, int col) {
        // Check if the coordinates are valid.
        if (row < 0 || row >= BOARD_SIZE || col < 0 || col >= BOARD_SIZE) {
            return new ShotResult("INVALID", null);
        }

        // Check if the position has already been shot at.
        if (board[row][col] == HIT || board[row][col] == MISS) {
            return new ShotResult("ALREADY_SHOT", null);
        }

        // Check if a ship is hit.
        if (board[row][col] == SHIP) {
            board[row][col] = HIT;

            // Iterate through all ships to check which ship is hit
            for (Ship ship : ships) {
                if (ship.occupiesPosition(row, col)) {
                    ship.checkHit(row, col);
                    // Check if ship is sunk.
                    if (ship.isSunk()) {
                        // Check if all ships are sunk and game is over.
                        if (allShipsSunk()) {
                            return new ShotResult("GAME_OVER", ship.getSize());
                        }
                        return new ShotResult("SUNK", ship.getSize());
                    }
                    return new ShotResult("HIT", null);
                }
            }
        }

        board[row][col] = MISS;
        return new ShotResult("MISS", null);
    }

    // Checks if all ships have been sunk.
    private boolean allShipsSunk() {
        return ships.stream().allMatch(Ship::isSunk);
    }

    // Prints the current state of the board to the console.
    private void printBoard() {
        System.out.println("Server Board Configuration:");
        for (char[] row : board) {
            System.out.println(Arrays.toString(row));
        }
        System.out.println("Number of ships: " + ships.size());
    }

    // Inner class to represent the result of a shot.
    private static class ShotResult {

        final String result;
        final Integer shipSize;

        ShotResult(String result, Integer shipSize) {
            this.result = result;
            this.shipSize = shipSize;
        }

        @Override
        public String toString() {
            return shipSize == null ? result : result + ":" + shipSize;
        }
    }

    // Main method to start the server application.
    @SuppressWarnings("CallToPrintStackTrace")
    public static void main(String[] args) {
        try (ServerSocket serverSocket = new ServerSocket(5000)) {
            System.out.println(
                "Naval Battle Server is running on port 5000..."
            );

            // Keep server running and waiting for clients.
            while (true) {
                // Accept incoming client connections.
                try (
                    Socket clientSocket = serverSocket.accept();
                    BufferedReader in = new BufferedReader(
                        new InputStreamReader(clientSocket.getInputStream())
                    );
                    PrintWriter out = new PrintWriter(
                        clientSocket.getOutputStream(),
                        true
                    )
                ) {
                    // Log client connection.
                    System.out.println(
                        "Player connected: " + clientSocket.getInetAddress()
                    );
                    out.println("WELCOME"); // Send welcome message to client.

                    // Parse ship configuration from the client.
                    String shipConfig = in.readLine();
                    List<Integer> shipSizes;
                    try {
                        shipSizes = parseShipConfig(shipConfig);
                        // Create new server instance for each game.
                        Server gameServer = new Server(shipSizes);
                        System.out.println(
                            "New game started with ships: " + shipSizes
                        );
                        // Start the game loop.
                        handleGameLoop(in, out, gameServer);
                    } catch (IllegalArgumentException e) {
                        // Send error message to the client if the ships are invalid.
                        out.println(
                            "ERROR:Too many or too large ships for the board"
                        );
                        System.out.println(
                            "Game creation failed: " + e.getMessage()
                        );
                        continue;
                    }

                    System.out.println("Player disconnected");
                }
            }
        } catch (IOException e) {
            // Print error message and stack trace.
            System.out.println("Server exception: " + e.getMessage());
            e.printStackTrace();
        }
    }

    // Parses the ship configuration string from the client.
    private static List<Integer> parseShipConfig(String shipConfig) {
        if (shipConfig != null && shipConfig.startsWith("SHIPS:")) {
            List<Integer> shipSizes = new ArrayList<>();
            String[] sizes = shipConfig.substring(6).split(",");
            for (String size : sizes) {
                shipSizes.add(Integer.valueOf(size.trim()));
            }
            return shipSizes;
        }
        return Arrays.asList(3, 2, 1); // Default configuration
    }

    // Manages the main game loop for each client.
    private static void handleGameLoop(
        BufferedReader in,
        PrintWriter out,
        Server gameServer
    ) throws IOException {
        String input;
        while (
            (input = in.readLine()) != null && !input.equalsIgnoreCase("quit")
        ) {
            try {
                String[] coordinates = input.split(",");
                int row = Integer.parseInt(coordinates[0].trim());
                int col = Integer.parseInt(coordinates[1].trim());

                ShotResult result = gameServer.processShot(row, col);
                out.println(result.toString()); // Send the shot result to the client.
                System.out.println(
                    "Shot at (" + row + "," + col + "): " + result
                );

                if (result.result.equals("GAME_OVER")) {
                    break;
                }
            } catch (NumberFormatException e) {
                out.println("INVALID"); // Send invalid message if the input format is invalid
                System.out.println("Invalid input received: " + input);
            }
        }
    }
}
