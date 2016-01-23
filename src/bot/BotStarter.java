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
import java.util.Random;

/**
 * BotStarter class
 * 
 * Magic happens here. You should edit this file, or more specifically the makeTurn() method to make your bot do more than random moves.
 * 
 * @author Jim van Eeden <jim@starapple.nl>
 * @author stilkin
 */

public class BotStarter {
    private final TTTField smallField = new TTTField();
    private Field gameField;
    private int myId;
    private final Random rand = new Random();
    private final ArrayList<Move> cornerList = new ArrayList<>();

    public BotStarter() {
	cornerList.add(new Move(0, 0));
	cornerList.add(new Move(0, 2));
	cornerList.add(new Move(2, 0));
	cornerList.add(new Move(2, 2));
    }

    /**
     * Makes a turn. Edit this method to make your bot smarter. Currently does only random moves.
     *
     * @return The column where the turn was made.
     */
    public Move makeTurn(final Field field) {
	gameField = field;
	myId = BotParser.mBotId;
	System.err.println(field.toString());

	smallField.clearBoard(); // JIC
	int mx = gameField.getActiveMicroboardX();
	int my = gameField.getActiveMicroboardY();

	if (mx < 0 || my < 0) { // WE GET TO CHOOSE THE BOARD
	    // get big board, play on big board
	    smallField.setBoard(gameField.getValidMacroBoard());
	    System.err.println(smallField.toString());
	    Move myMove = playField(smallField);

	    if (myMove == null) { // nothing useful
		final ArrayList<Move> validFields = smallField.getAvailableMoves();
		// default behavior: play random move
		myMove = validFields.get(rand.nextInt(validFields.size())); /* get random move from available moves */
	    }
	    mx = myMove.mX;
	    my = myMove.mY;
	    System.err.println("Choosing microboard: " + mx + " " + my);
	} else {
	    System.err.println("Playing on microboard: " + mx + " " + my);
	}

	smallField.setBoard(gameField.getMicroBoard(mx, my));

	// PLAY ON THE SMALL FIELD
	Move myMove = playField(smallField);
	if (myMove == null) {
	    // default behavior: play random move
	    final ArrayList<Move> validMoves = smallField.getAvailableMoves();
	    myMove = validMoves.get(rand.nextInt(validMoves.size()));
	}

	System.err.println("Local move: " + myMove.mX + " " + myMove.mY);
	// translate to big board
	mx = mx * 3 + myMove.mX;
	my = my * 3 + myMove.mY;
	System.err.println("Putting move: " + mx + " " + my);
	return new Move(mx, my);

    }

    // TODO: GET LISTS OF VALID MOVES AND THEN CHOOSE BETTER ONE BASED ON MACRO GAME

    private Move playField(final TTTField tttField) {
	// Win: If the player has two in a row, they can place a third to get three in a row.
	for (int y = 0; y < 3; y++) {
	    for (int x = 0; x < 3; x++) {
		if (tttField.isValidMove(x, y)) {
		    tttField.setMark(x, y, myId);
		    if (tttField.hasThreeInARow(myId)) {
			System.err.println("Going for a win on " + x + " " + y);
			return new Move(x, y);
		    }
		    tttField.removeMark(x, y);
		}
	    }
	}
	// Block: If the opponent has two in a row, the player must play the third themselves to block the opponent.
	final int oppId = 3 - myId;
	for (int y = 0; y < 3; y++) {
	    for (int x = 0; x < 3; x++) {
		if (tttField.isValidMove(x, y)) {
		    tttField.setMark(x, y, oppId);
		    if (tttField.hasThreeInARow(oppId)) {
			System.err.println("Preventing opponent win on " + x + " " + y);
			return new Move(x, y);
		    }
		    tttField.removeMark(x, y);
		}
	    }
	}

	// Fork: Create an opportunity where the player has two threats to win (two non-blocked lines of 2).
	// TODO: dont know how to do that
	for (int y = 2; y >= 0; y--) {
	    for (int x = 2; x >= 0; x--) {
		if (tttField.isValidMove(x, y)) {
		    tttField.setMark(x, y, myId);
		    if (tttField.hasSequenceInARow(myId, 2)) {
			return new Move(x, y);
		    }
		    tttField.removeMark(x, y);
		}
	    }
	}

	/**
	 * Blocking an opponent's fork: Option 1: The player should create two in a row to force the opponent into defending, as long as it doesn't result in them creating a fork.
	 * For example, if "X" has a corner, "O" has the center, and "X" has the opposite corner as well, "O" must not play a corner in order to win. (Playing a corner in this
	 * scenario creates a fork for "X" to win.) Option 2: If there is a configuration where the opponent can fork, the player should block that fork.
	 */
	// TODO: don't know how to do that
	for (int y = 2; y >= 0; y--) {
	    for (int x = 2; x >= 0; x--) {
		if (tttField.isValidMove(x, y)) {
		    tttField.setMark(x, y, oppId);
		    if (tttField.hasSequenceInARow(oppId, 2)) {
			return new Move(x, y);
		    }
		    tttField.removeMark(x, y);
		}
	    }
	}
/*
	// Center: A player marks the center.
	if (tttField.isValidMove(1, 1)) {
	    return new Move(1, 1);
	}

	// Opposite corner: If the opponent is in the corner, the player plays the opposite corner.
	Move corner, move;
	for (int m = 0; m < cornerList.size(); m++) {
	    corner = cornerList.get(m);
	    if (tttField.isOccupiedBy(corner.mX, corner.mY, oppId)) {
		move = new Move(2 - corner.mX, 2 - corner.mY); // opposite corner
		if (tttField.isValidMove(move.mX, move.mY)) {
		    return move;
		}
	    }
	}

	// Empty corner: The player plays in a corner square.
	for (int m = 0; m < cornerList.size(); m++) {
	    corner = cornerList.get(m);
	    if (tttField.isValidMove(corner.mX, corner.mY)) {
		return corner;
	    }
	}

	// TODO: Empty side: The player plays in a middle square on any of the 4 sides.
*/
	return null;
    }

    public static void main(String[] args) {
	final BotParser parser = new BotParser(new BotStarter());
	parser.run();
    }
}
