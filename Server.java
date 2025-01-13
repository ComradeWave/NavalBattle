
// Server.java
import java.io.*;
import java.net.*;
import java.util.*;

public class Server {
    private static final int BOARD_SIZE = 5;
    private static final char WATER = '~';
    private static final char SHIP = 'S';
    private static final char HIT = 'X';
    private static final char MISS = 'O';

    private final char[][] board;
    private final List<Ship> ships;
    private final Random random;

    public Server(List<Integer> shipSizes) {
        this.board = new char[BOARD_SIZE][BOARD_SIZE];
        this.ships = new ArrayList<>();
        this.random = new Random();

        initializeBoard();
        if (!placeShips(shipSizes)) {
            throw new IllegalArgumentException("Cannot place all ships on the board. Try with fewer or smaller ships.");
        }
        printBoard();
    }

    private void initializeBoard() {
        for (int i = 0; i < BOARD_SIZE; i++) {
            Arrays.fill(board[i], WATER);
        }
    }

    private boolean placeShips(List<Integer> shipSizes) {
        // Sort ships by size in descending order for better placement
        shipSizes.sort(Collections.reverseOrder());

        // Validate total ship size against board size
        int totalShipSize = shipSizes.stream().mapToInt(Integer::intValue).sum();
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

    private boolean tryPlaceShip(int size) {
        if (size > BOARD_SIZE) {
            return false;
        }

        List<PlacementOption> options = new ArrayList<>();

        // Generate all possible placements
        for (int row = 0; row < BOARD_SIZE; row++) {
            for (int col = 0; col < BOARD_SIZE; col++) {
                for (boolean horizontal : new boolean[]{true, false}) {
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
        Ship newShip = new Ship(size, chosen.horizontal, chosen.row, chosen.col);
        ships.add(newShip);
        placeShipOnBoard(newShip);
        return true;
    }

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

    private boolean canPlaceShip(int row, int col, int size, boolean isHorizontal) {
        // Check bounds
        if (isHorizontal && col + size > BOARD_SIZE) return false;
        if (!isHorizontal && row + size > BOARD_SIZE) return false;

        // Check if space is free and no adjacent ships
        for (int r = Math.max(0, row - 1); r <= Math.min(BOARD_SIZE - 1, row + (isHorizontal ? 1 : size)); r++) {
            for (int c = Math.max(0, col - 1); c <= Math.min(BOARD_SIZE - 1, col + (isHorizontal ? size : 1)); c++) {
                if (board[r][c] != WATER) {
                    return false;
                }
            }
        }

        return true;
    }

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

    private ShotResult processShot(int row, int col) {
        if (row < 0 || row >= BOARD_SIZE || col < 0 || col >= BOARD_SIZE) {
            return new ShotResult("INVALID", null);
        }

        if (board[row][col] == HIT || board[row][col] == MISS) {
            return new ShotResult("ALREADY_SHOT", null);
        }

        if (board[row][col] == SHIP) {
            board[row][col] = HIT;

            for (Ship ship : ships) {
                if (ship.occupiesPosition(row, col)) {
                    ship.checkHit(row, col);
                    if (ship.isSunk()) {
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

    private boolean allShipsSunk() {
        return ships.stream().allMatch(Ship::isSunk);
    }

    private void printBoard() {
        System.out.println("Server Board Configuration:");
        for (char[] row : board) {
            System.out.println(Arrays.toString(row));
        }
        System.out.println("Number of ships: " + ships.size());
    }

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

    @SuppressWarnings("CallToPrintStackTrace")
    public static void main(String[] args) {
        try (ServerSocket serverSocket = new ServerSocket(5000)) {
            System.out.println("Naval Battle Server is running on port 5000...");

            while (true) {
                try (Socket clientSocket = serverSocket.accept();
                     BufferedReader in = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
                     PrintWriter out = new PrintWriter(clientSocket.getOutputStream(), true)) {

                    System.out.println("Player connected: " + clientSocket.getInetAddress());
                    out.println("WELCOME");

                    // Parse ship configuration
                    String shipConfig = in.readLine();
                    List<Integer> shipSizes;
                    try {
                        shipSizes = parseShipConfig(shipConfig);
                        Server gameServer = new Server(shipSizes);
                        System.out.println("New game started with ships: " + shipSizes);

                        handleGameLoop(in, out, gameServer);
                    } catch (IllegalArgumentException e) {
                        out.println("ERROR:Too many or too large ships for the board");
                        System.out.println("Game creation failed: " + e.getMessage());
                        continue;
                    }

                    System.out.println("Player disconnected");
                }
            }
        } catch (IOException e) {
            System.out.println("Server exception: " + e.getMessage());
            e.printStackTrace();
        }
    }

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

    private static void handleGameLoop(BufferedReader in, PrintWriter out, Server gameServer) throws IOException {
        String input;
        while ((input = in.readLine()) != null && !input.equalsIgnoreCase("quit")) {
            try {
                String[] coordinates = input.split(",");
                int row = Integer.parseInt(coordinates[0].trim());
                int col = Integer.parseInt(coordinates[1].trim());

                ShotResult result = gameServer.processShot(row, col);
                out.println(result.toString());
                System.out.println("Shot at (" + row + "," + col + "): " + result);

                if (result.result.equals("GAME_OVER")) {
                    break;
                }
            } catch (NumberFormatException e) {
                out.println("INVALID");
                System.out.println("Invalid input received: " + input);
            }
        }
    }
}
