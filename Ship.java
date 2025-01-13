// Ship.java
public class Ship {
    private final int size;
    private final boolean isHorizontal;
    private final int row;
    private final int col;
    private final boolean[] segments;
    private boolean isSunk;

    public Ship(int size, boolean isHorizontal, int row, int col) {
        this.size = size;
        this.isHorizontal = isHorizontal;
        this.row = row;
        this.col = col;
        this.segments = new boolean[size];
        this.isSunk = false;
    }

    public boolean checkHit(int hitRow, int hitCol) {
        if (isHorizontal) {
            if (hitRow != row || hitCol < col || hitCol >= col + size) {
                return false;
            }
            segments[hitCol - col] = true;
        } else {
            if (hitCol != col || hitRow < row || hitRow >= row + size) {
                return false;
            }
            segments[hitRow - row] = true;
        }
        checkIfSunk();
        return true;
    }

    private void checkIfSunk() {
        for (boolean segment : segments) {
            if (!segment) return;
        }
        isSunk = true;
    }

    public boolean isSunk() {
        return isSunk;
    }

    public boolean occupiesPosition(int checkRow, int checkCol) {
        if (isHorizontal) {
            return checkRow == row && checkCol >= col && checkCol < col + size;
        } else {
            return checkCol == col && checkRow >= row && checkRow < row + size;
        }
    }

    public int getRow() {
        return row;
    }

    public int getCol() {
        return col;
    }

    public boolean isHorizontal() {
        return isHorizontal;
    }

    public int getSize() {
        return size;
    }
}
