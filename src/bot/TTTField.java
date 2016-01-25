package bot;

import java.util.ArrayList;

public class TTTField {
    private final int MAX = 3;
    private final int COLS = MAX, ROWS = MAX;
    private final int[][] gameBoard = new int[COLS][ROWS];
    private int totalMarks = 0;

    /**
     * Put a mark on the board. NO CHECKING IS DONE
     */
    public void setMark(final int x, final int y, final int mark) {
	gameBoard[x][y] = mark;
	totalMarks++;
    }

    public boolean isValidMove(final int x, final int y) {
	return (gameBoard[x][y] == 0);
    }

    /**
     * Remove a mark from the board. NO CHECKING IS DONE
     */
    public void removeMark(final int x, final int y) {
	if (gameBoard[x][y] != 0) {
	    totalMarks--;
	}
	gameBoard[x][y] = 0;
    }

    public boolean hasMark(final int x, final int y, final int mark) {
	return (gameBoard[x][y] == mark);
    }

    public int getMark(final int x, final int y) {
	return gameBoard[x][y];
    }

    /*
     * (re)set the board
     */
    public void setBoard(final int[][] newBoard) {
	totalMarks = 0;
	for (int y = 0; y < ROWS; y++) {
	    for (int x = 0; x < COLS; x++) {
		gameBoard[x][y] = newBoard[x][y];
		if (gameBoard[x][y] != 0) {
		    totalMarks++;
		}
	    }
	}
    }

    public boolean isFull() {
	return (totalMarks == ROWS * COLS);
    }

    public ArrayList<Move> getAvailableMoves() {
	final ArrayList<Move> moves = new ArrayList<Move>();

	for (int y = 0; y < ROWS; y++) {
	    for (int x = 0; x < COLS; x++) {
		if (gameBoard[x][y] <= 0) {
		    moves.add(new Move(x, y));
		}
	    }
	}

	return moves;
    }

    public boolean hasThreeInARow(final int player) {
	if (hasHorizontalThree(player) >= 0) {
	    return true;
	}
	if (hasVerticaThree(player) >= 0) {
	    return true;
	}
	if (hasDiagonalThree(player) >= 0) {
	    return true;
	}

	return false;
    }

    /**
     * Returns the first col for which this player has 3 in a row, -1 if none are present
     * 
     * @param player
     * @return
     */
    public int hasVerticaThree(final int player) {
	for (int x = 0; x < COLS; x++) {
	    int count = 0;
	    for (int y = 0; y < ROWS; y++) {
		if (gameBoard[x][y] == player) {
		    count++;
		    if (count == MAX) {
			return x;
		    }
		} else {
		    count = 0;
		}
	    }
	}
	return -1;
    }

    /**
     * Returns the first row for which this player has 3 in a row, -1 if none are present
     * 
     * @param player
     * @return
     */
    public int hasHorizontalThree(final int player) {
	for (int y = 0; y < ROWS; y++) {
	    int count = 0;
	    for (int x = 0; x < COLS; x++) {
		if (gameBoard[x][y] == player) {
		    count++;
		    if (count == MAX) {
			return x;
		    }
		} else {
		    count = 0;
		}
	    }
	}
	return -1;
    }

    /**
     * Returns the number 3 if either diagonal has 3 in a row, -1 if none are present
     * 
     * @param player
     * @return
     */
    public int hasDiagonalThree(final int player) {
	int countNWSE = 0; // check one diagonal -> \
	int countNESW = 0; // check other diagonal -> /
	for (int i = 0; i < MAX; i++) {
	    if (gameBoard[i][i] == player) {
		countNWSE++;
		if (countNWSE == MAX) {
		    return i;
		}
	    } else {
		countNWSE = 0;
	    }
	    if (gameBoard[(MAX - 1) - i][i] == player) {
		countNESW++;
		if (countNESW == MAX) {
		    return i;
		}
	    } else {
		countNESW = 0;
	    }
	}
	return -1;
    }

    /*
     * Checks if the field has some in line (with gaps)
     */
    public boolean hasSomeInLine(final int player, final int max) {

	if (hasSomeOnAVertical(player, max) >= 0) {
	    return true;
	}

	if (hasSomeOnAHorizontal(player, max) >= 0) {
	    return true;
	}

	if (hasSomeOnADiagonal(player, max) >= 0) {
	    return true;
	}

	return false;
    }

    /**
     * Returns the first col for which this player has 3 in a row, -1 if none are present
     * 
     * @param player
     * @return
     */
    public int hasSomeOnAVertical(final int player, final int max) {
	for (int x = 0; x < COLS; x++) {
	    int count = 0;
	    for (int y = 0; y < ROWS; y++) {
		if (gameBoard[x][y] == player) {
		    count++;
		    if (count >= max) {
			return x;
		    }
		} else if (gameBoard[x][y] == 3 - player) { // blocked by opponent
		    count = 0;
		}
	    }
	}
	return -1;
    }

    /**
     * Returns the first row for which this player has 3 in a row, -1 if none are present
     * 
     * @param player
     * @return
     */
    public int hasSomeOnAHorizontal(final int player, final int max) {
	for (int y = 0; y < ROWS; y++) {
	    int count = 0;
	    for (int x = 0; x < COLS; x++) {
		if (gameBoard[x][y] == player) {
		    count++;
		    if (count >= max) {
			return x;
		    }
		} else if (gameBoard[x][y] == 3 - player) { // blocked by opponent
		    count = 0;
		}
	    }
	}
	return -1;
    }

    /**
     * Returns the number 3 if either diagonal has 3 in a row, -1 if none are present
     * 
     * @param player
     * @return
     */
    public int hasSomeOnADiagonal(final int player, final int max) {
	int countNWSE = 0; // check one diagonal -> \
	int countNESW = 0; // check other diagonal -> /
	for (int i = 0; i < MAX; i++) {
	    if (gameBoard[i][i] == player) {
		countNWSE++;
		if (countNWSE >= max) {
		    return i;
		}
	    } else if (gameBoard[i][i] == 3 - player) { // blocked by opponent
		countNWSE = 0;
	    }
	    if (gameBoard[(MAX - 1) - i][i] == player) {
		countNESW++;
		if (countNESW >= max) {
		    return i;
		}
	    } else if (gameBoard[(MAX - 1) - i][i] == 3 - player) { // blocked by opponent
		countNESW = 0;
	    }
	}
	return -1;
    }

    @Override
    public String toString() {
	String prettyStr = " ";
	int counter = 0;
	for (int y = 0; y < ROWS; y++) {
	    for (int x = 0; x < COLS; x++) {
		if (counter > 0) {
		    prettyStr += " ";
		}
		prettyStr += gameBoard[x][y];
		counter++;
	    }
	    prettyStr += '\n';
	}
	return prettyStr;
    }

    /* Jims code */

    public void clearBoard() {
	totalMarks = 0;
	for (int x = 0; x < COLS; x++) {
	    for (int y = 0; y < ROWS; y++) {
		gameBoard[x][y] = 0;
	    }
	}
    }

}
