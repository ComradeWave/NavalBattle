# Naval Battle Game

This is a simple implementation of the classic Naval Battle game, featuring a server-client architecture. The game is played on a 5x5 grid where a player tries to sink the opponent's ships by guessing their positions.

## Project Structure

The project consists of the following Java files:

- `Client.java`: This file contains the client-side logic for the Naval Battle game. It handles user input, displays the game board, and communicates with the server.
- `Server.java`: This file contains the server-side logic for the Naval Battle game. It manages the game board, ship placements, and processes incoming shots from the client.
- `ValidateIPv4.java`: This is an abstract class used for validating if the ip used for the connection is a valid ipv4 (not in the commit but implemented in client.java)
- `Ship.java`: This class represents a ship in the game, storing information about its size, orientation, position, and hit status (not in the commit but implemented in Server.java)

## How to Run the Game

### Prerequisites

- Java Development Kit (JDK) 17 or higher.

### Steps

1. **Compile the Java files:**
    Open a terminal or command prompt, navigate to the project's directory, and compile the Java files:

   ```bash
   javac battaglia\ navale\*.java
   ```

2.  **Run the Server:**
    In a new terminal window, execute the `Server.java` file:

    ```bash
   java battaglia\ navale\Server
    ```
    The server will start and listen for client connections.

3.  **Run the Client:**
    In another terminal window, execute the `Client.java` file:

    ```bash
   java battaglia\ navale\Client
   ```

    The client will prompt you to enter the server's IP address if it is an invalid ip address.
    After that, it'll prompt for the number of ships and their respective sizes.
    The game starts after the ship configuration.
    You will be prompted to enter the coordinates of your shots in the form of "row,col", or you can type "quit" to exit the game.

## Game Logic

### Server

-   **Board Initialization:** The server creates a 5x5 grid initialized with water.
-   **Ship Placement:** The server receives ship sizes from the client, and tries to place them on the board randomly.
-   **Shot Processing:** When the server receives a shot from the client:
    -   It checks if the shot hits a ship, misses, has already been made, or is invalid
    -   It returns the result to the client.

### Client

-   **User Input:** The client takes user input for ship configuration and shots.
-   **Board Display:** The client displays the game board with water, hits, and misses.
-   **Communication:** The client sends shots to the server and receives results.

## Game Flow

1.  The server starts and waits for connections.
2.  The client connects to the server and sends ship configuration.
3.  The server places the ships and starts the game loop.
4.  The client makes shots and receives results from the server.
5.  The game continues until the player has sunk all of the opponent's ships.

## Classes

### Server

-   **`Server(List<Integer> shipSizes)`:** Constructor of the server class. It creates and places the ships
-   **`initializeBoard()`:** Initializes the game board with water.
-   **`placeShips(List<Integer> shipSizes)`:** Places ships randomly on the board based on sizes from the client.
-   **`tryPlaceShip(int size)`:** Tries to place one single ship in a random position.
-   **`canPlaceShip(int row, int col, int size, boolean isHorizontal)`:** Checks if a ship can be placed at a given position without overlapping another ship.
-   **`placeShipOnBoard(Ship ship)`:** Places a ship on the board
-   **`processShot(int row, int col)`:** Checks the result of a shot based on the position.
-   **`allShipsSunk()`:** Checks if all ships have been sunk.
-   **`printBoard()`:** Prints the state of the game board on server side.
-   **`ShotResult`:** An inner class representing the result of a shot (hit, miss, etc)
-   **`main(String[] args)`:** Entry point of the server program, managing the server and clients connections.

### Client

-   **`Client()`:** Constructor that initializes the board, remaining ships and a hashset of all the fired shots.
-   **`initializeBoard()`:** Initializes the game board with water.
-   **`displayBoard()`:** Prints the state of the game board on the client side.
-   **`displayRemainingShips()`:** Prints all the remaining ships
-   **`getShipConfiguration(Scanner scanner)`:** Gets the number and sizes of the ships from the user.
-   **`processShot(int row, int col, String result)`:** Checks the result of a shot made on the server side, if there are ships remaining or if the player won the match.
-    **`getValidCoordinates(Scanner scanner)`:** Asks the user for the coordinates to fire, it also ensures that coordinates are valid (in range, previously shot).
-   **`main(String[] args)`:** Entry point of the client program, managing the connection to the server, the game logic and the end of it.
## Ship Class
-   **`Ship(int size, boolean isHorizontal, int row, int col)`:** Constructor of the ship, it initializes all of the data.
-   **`occupiesPosition(int row, int col)`:** Returns a boolean if the ship occupies that position.
-   **`checkHit(int row, int col)`:** Sets the position to 'hit' on the ships internal representation.
-   **`isSunk()`:** Returns if the ship is sunk (all positions have been hit).
-   **`getSize()`:** Returns the size of the ship.
-   **`isHorizontal()`:** Returns if the ship is horizontal.
-   **`getRow()`:** Returns the row of the starting position.
-   **`getCol()`:** Returns the column of the starting position.

## Additional Notes

-   The game uses a simple text-based interface.
-   The server and client communicate over TCP sockets.
-   The ship placement is handled randomly by the server.

## Potential Improvements

-   Implement a graphical user interface (GUI) for a more engaging experience.
-   Add more sophisticated ship placement algorithms.
-   Implement different game modes or board sizes.
-   Add error handling for unexpected server disconnections.
-   Add client-side validation of user input.
