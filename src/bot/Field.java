// // Copyright 2016 theaigames.com (developers@theaigames.com)

//    Licensed under the Apache License, Version 2.0 (the "License");
//    you may not use this file except in compliance with the License.
//    You may obtain a copy of the License at

//        http://www.apache.org/licenses/LICENSE-2.0

//    Unless required by applicable law or agreed to in writing, software
//    distributed under the License is distributed on an "AS IS" BASIS,
//    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
//    See the License for the specific language governing permissions and
//    limitations under the License.
//  
//    For the full copyright and license information, please view the LICENSE
//    file that was distributed with this source code.

package bot;

import java.util.ArrayList;

/**
 * Field class
 * 
 * Handles everything that has to do with the field, such as storing the current state and performing calculations on the field.
 * 
 * @author Jim van Eeden <jim@starapple.nl>, Joost de Meij <joost@starapple.nl>
 */

public class Field {
    private int mRoundNr;
    private int mMoveNr;
    private int[][] mBoard;
    private int[][] mMacroboard;

    private final int COLS = 9, ROWS = 9;
    private String mLastError = "";
    private int mLastX = 0, mLastY = 0;
    private Boolean mAllMicroboardsActive = true;
    private int mActiveMicroboardX = 0, mActiveMicroboardY = 0;

    public Field() {
	mBoard = new int[COLS][ROWS];
	mMacroboard = new int[COLS / 3][ROWS / 3];
	clearBoard();
    }

    /* MY CODE */

    /**
     * Get small board to play on
     * 
     * @param x
     * @param y
     * @return
     */
    public int[][] getMicroBoard(final int x, final int y) {
	final int[][] microBoard = new int[3][3];
	int xi, yj;
	for (int i = 0; i < 3; i++) {
	    for (int j = 0; j < 3; j++) {
		xi = 3 * x + i;
		yj = 3 * y + j;
		microBoard[i][j] = mBoard[xi][yj];
	    }
	}
	return microBoard;
    }
    
    public int[][] getMacroBoard() {
	return mMacroboard;
    }

    /* JIMS CODE */

    /**
     * Parse data about the game given by the engine
     * 
     * @param key
     *            : type of data given
     * @param value
     *            : value
     */
    public void parseGameData(final String key, final String value) {
	if (key.equals("round")) {
	    mRoundNr = Integer.parseInt(value);
	} else if (key.equals("move")) {
	    mMoveNr = Integer.parseInt(value);
	} else if (key.equals("field")) {
	    parseFromString(value); /* Parse Field with data */
	} else if (key.equals("macroboard")) {
	    parseMacroboardFromString(value); /* Parse macroboard with data */
	}
    }

    /**
     * Initialise field from comma separated String
     * 
     * @param String
     *            :
     */
    public void parseFromString(String s) {
	System.err.println("Move " + mMoveNr);
	s = s.replace(";", ",");
	String[] r = s.split(",");
	int counter = 0;
	for (int y = 0; y < ROWS; y++) {
	    for (int x = 0; x < COLS; x++) {
		mBoard[x][y] = Integer.parseInt(r[counter]);
		counter++;
	    }
	}
    }

    /**
     * Initialise macroboard from comma separated String
     * 
     * @param String
     *            :
     */
    public void parseMacroboardFromString(final String s) {
	String[] r = s.split(",");
	int counter = 0;
	mActiveMicroboardX = -1;
	mActiveMicroboardY = -1;
	mAllMicroboardsActive = true;
	for (int y = 0; y < 3; y++) {
	    for (int x = 0; x < 3; x++) {
		mMacroboard[x][y] = Integer.parseInt(r[counter]);
		if (mMacroboard[x][y] == -1) {
		    mActiveMicroboardX = x;
		    mActiveMicroboardY = y;
		    mAllMicroboardsActive = false;
		}
		counter++;
	    }
	}
    }

    public void clearBoard() {
	for (int x = 0; x < COLS; x++) {
	    for (int y = 0; y < ROWS; y++) {
		mBoard[x][y] = 0;
	    }
	}
    }

    public ArrayList<Move> getAvailableMoves() {
	final ArrayList<Move> moves = new ArrayList<Move>();

	if (getActiveMicroboardX() == -1) { /* Any microboard is available */
	    for (int y = 0; y < ROWS; y++) {
		for (int x = 0; x < COLS; x++) {
		    int macroY = (int) y / 3;
		    int macroX = (int) x / 3;
		    if (mBoard[x][y] == 0 && mMacroboard[macroX][macroY] <= 0) {
			moves.add(new Move(x, y));
		    }
		}
	    }
	} else {
	    int startX = getActiveMicroboardX() * 3;
	    int startY = getActiveMicroboardY() * 3;
	    for (int y = startY; y < startY + 3; y++) {
		for (int x = startX; x < startX + 3; x++) {
		    if (mBoard[x][y] == 0) {
			moves.add(new Move(x, y));
		    }
		}
	    }
	}

	return moves;
    }

    public Boolean isInActiveMicroboard(final int x, final int y) {
	if (mAllMicroboardsActive) {
	    return true;
	}
	return (Math.floor(x / 3) == getActiveMicroboardX() && Math.floor(y / 3) == getActiveMicroboardY());
    }

    public int getActiveMicroboardX() {
	if (mAllMicroboardsActive)
	    return -1;
	return mActiveMicroboardX;

    }

    public int getActiveMicroboardY() {
	if (mAllMicroboardsActive)
	    return -1;
	return mActiveMicroboardY;
    }

    /**
     * Returns reason why addMove returns false
     * 
     * @param args
     *            :
     * @return : reason why addMove returns false
     */
    public String getLastError() {
	return mLastError;
    }

    /**
     * Returns last inserted column
     * 
     * @param args
     *            :
     * @return : last inserted column
     */
    public int getLastX() {
	return mLastX;
    }

    /**
     * Returns last inserted row
     * 
     * @param args
     *            :
     * @return : last inserted row
     */
    public int getLastY() {
	return mLastY;
    }

    @Override
    /**
     * Creates comma separated String with player ids for the microboards.
     * 
     * @param args
     *            :
     * @return : String with player names for every cell, or 'empty' when cell is empty.
     */
    public String toString() {
	String prettyStr = " ";
	int counter = 0;
	for (int y = 0; y < ROWS; y++) {
	    for (int x = 0; x < COLS; x++) {
		if (counter > 0) {
		    prettyStr += ",";
		}
		prettyStr += mBoard[x][y];
		counter++;
	    }
	    prettyStr += '\n';
	}
	return prettyStr;
    }

    /**
     * Checks whether the field is full
     * 
     * @param args
     *            :
     * @return : Returns true when field is full, otherwise returns false.
     */
    public boolean isFull() {
	for (int x = 0; x < COLS; x++)
	    for (int y = 0; y < ROWS; y++)
		if (mBoard[x][y] == 0)
		    return false; // At least one cell is not filled
	// All cells are filled
	return true;
    }

    public int getNrColumns() {
	return COLS;
    }

    public int getNrRows() {
	return ROWS;
    }

    public boolean isEmpty() {
	for (int x = 0; x < COLS; x++) {
	    for (int y = 0; y < ROWS; y++) {
		if (mBoard[x][y] > 0) {
		    return false;
		}
	    }
	}
	return true;
    }

    /**
     * Returns the player id on given column and row
     * 
     * @param args
     *            : int column, int row
     * @return : int
     */
    public int getPlayerId(final int column, final int row) {
	return mBoard[column][row];
    }
}