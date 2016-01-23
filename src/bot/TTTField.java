package bot;

import java.util.ArrayList;

public class TTTField {
    private final int COLS = 3, ROWS = 3;
    private final int[][] mBoard = new int[COLS][ROWS];

    /**
     * Put a mark on the board. NO CHECKING IS DONE
     */
    public void setMark(final int x, final int y, final int mark) {
	mBoard[x][y] = mark;
    }

    public boolean isValidMove(final int x, final int y) {
	return (mBoard[x][y] == 0);
    }

    /**
     * Remove a mark from the board. NO CHECKING IS DONE
     */
    public void removeMark(final int x, final int y) {
	mBoard[x][y] = 0;
    }

    /*
     * (re)set the board
     */
    public void setBoard(final int[][] newBoard) {
	for (int y = 0; y < ROWS; y++) {
	    for (int x = 0; x < COLS; x++) {
		mBoard[x][y] = newBoard[x][y];
	    }
	}
    }

    public boolean hasThreeInARow(final int player) {
	if (hasThreeHorizontal(player, 3) >= 0) {
	    return true;
	}
	if (hasThreeVertical(player, 3) >= 0) {
	    return true;
	}
	if (hasThreeDiagonal(player, 3)) {
	    // warning: putting this one first may have negative impact on performance
	    return true;
	}

	return false;
    }

    public int hasThreeVertical(final int player, final int n) {
	for (int x = 0; x < COLS; x++) {
	    int count = 0;
	    for (int y = 0; y < ROWS; y++) {
		if (mBoard[x][y] == player) {
		    count++;
		    if (count >= n) {
			return x;
		    }
		} else {
		    count = 0;
		}
	    }
	}
	return -1;
    }

    public int hasThreeHorizontal(final int player, final int n) {
	for (int y = 0; y < ROWS; y++) {
	    int count = 0;
	    for (int x = 0; x < COLS; x++) {
		if (mBoard[x][y] == player) {
		    count++;
		    if (count >= n) {
			return y;
		    }
		} else {
		    count = 0;
		}
	    }
	}
	return -1;
    }

    public boolean hasThreeDiagonal(final int player, final int n) {

	// check one diagonal \\
	for (int i = 0; i <= COLS - n; i++) {
	    if (checkNWSEDiagonal(player, i, 0, n))
		return true;
	}
	for (int j = 1; j <= ROWS - n; j++) {
	    if (checkNWSEDiagonal(player, 0, j, n))
		return true;
	}

	// check other diagonal //
	for (int i = 0; i <= COLS - n; i++) {
	    if (checkNESWDiagonal(player, -i, 0, n))
		return true;
	}
	for (int j = 1; j <= ROWS - n; j++) {
	    if (checkNESWDiagonal(player, 0, j, n))
		return true;
	}
	return false;
    }

    private boolean checkNWSEDiagonal(final int player, final int i, final int j, final int n) {
	int count = 0;
	for (int x = i, y = j; x < COLS && y < ROWS; x++, y++) {
	    if (mBoard[x][y] == player) {
		count++;
		if (count >= n) {
		    return true;
		}
	    } else {
		count = 0;
	    }
	}
	return false;
    }

    private boolean checkNESWDiagonal(final int player, final int i, final int j, final int n) {
	int count = 0;
	for (int x = (COLS - 1) + i, y = j; x >= 0 && y < ROWS; x--, y++) {
	    if (mBoard[x][y] == player) {
		count++;
		if (count >= n) {
		    return true;
		}
	    } else {
		count = 0;
	    }
	}
	return false;
    }

    public boolean isFull() {
	for (int y = 0; y < ROWS; y++) {
	    for (int x = 0; x < COLS; x++) {
		if (mBoard[x][y] == 0) {
		    return false;
		}
	    }
	}

	return true;
    }

    public ArrayList<Move> getAvailableMoves() {
	final ArrayList<Move> moves = new ArrayList<Move>();

	for (int y = 0; y < ROWS; y++) {
	    for (int x = 0; x < COLS; x++) {
		if (mBoard[x][y] <= 0) {
		    moves.add(new Move(x, y));
		}
	    }
	}

	return moves;
    }

    /* Jims code */

    public void clearBoard() {
	for (int x = 0; x < COLS; x++) {
	    for (int y = 0; y < ROWS; y++) {
		mBoard[x][y] = 0;
	    }
	}
    }

}
