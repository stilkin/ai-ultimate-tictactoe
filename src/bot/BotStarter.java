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
import java.util.List;
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
    private final TTTField macroField = new TTTField();
    private Field gameField;
    private int myId;
    private int oppId;
    private final Random rand = new Random();
    private final List<Move> cornerList = new ArrayList<Move>();

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
    // TODO: GET LISTS OF VALID MOVES AND THEN CHOOSE BETTER ONE BASED ON MACRO GAME
    public Move makeTurn(final Field field) {
	// initialization
	gameField = field;
	myId = BotParser.mBotId;
	oppId = 3 - myId;

	int mx, my;
	System.err.println(field.toString()); // debug

	macroField.clearBoard();
	smallField.clearBoard();

	// get big board
	macroField.setBoard(gameField.getValidMacroBoard());
	System.err.println(macroField.toString());

	mx = gameField.getActiveMicroboardX();
	my = gameField.getActiveMicroboardY();

	if (mx >= 0 && my >= 0) { // play on a designated small field
	    System.err.println("Playing on microboard: " + mx + " " + my);
	    smallField.setBoard(gameField.getMicroBoard(mx, my));
	    final Move safeMove = getSafeMove(smallField, myId, oppId);
	    if (safeMove != null) { // this is ideal
		System.err.println(smallField.toString());
		System.err.println("Keepin' it safe: " + safeMove.mX + " " + safeMove.mY);
		return translateMovetoGlobal(mx, my, safeMove);
	    }
	    // TODO: refactor else
	} else { // this means we get to choose the small field
	    final List<Move> myMacroMoveList = getOrderedMoveList(macroField, myId); // my moves on the big field
	    if (myMacroMoveList.size() < 1) {
		System.err.println("ERROR: NO MACRO MOVES POSSIBLE");
		return null;
	    }
	    // look for safe options
	    Move potentialBoardCoords;
	    for (int m = 0; m < myMacroMoveList.size(); m++) {
		potentialBoardCoords = myMacroMoveList.get(m);
		smallField.setBoard(gameField.getMicroBoard(potentialBoardCoords.mX, potentialBoardCoords.mY));
		final Move safeMove = getSafeMove(smallField, myId, oppId);
		if (safeMove != null) { // this is ideal
		    mx = potentialBoardCoords.mX;
		    my = potentialBoardCoords.mY;
		    System.err.println("Choosing microboard: " + mx + " " + my);
		    System.err.println(smallField.toString());
		    System.err.println("Keepin' it safe: " + safeMove.mX + " " + safeMove.mY);
		    return translateMovetoGlobal(mx, my, safeMove);
		}
	    }

	    // set a fallback option
	    final Move boardCoords = myMacroMoveList.get(0);
	    mx = boardCoords.mX;
	    my = boardCoords.mY;
	    System.err.println("Choosing microboard: " + mx + " " + my);
	    // TODO: choose better?
	}

	// if we are here, we are not playing safe moves ^^
	smallField.setBoard(gameField.getMicroBoard(mx, my));
	System.err.println(smallField.toString());

	final List<Move> localMoveList = getOrderedMoveList(smallField, myId);
	Move localMove = null;

	if (localMoveList.size() < 1) { // whoops
	    System.err.println("ERROR: NO MICRO MOVES POSSIBLE");
	    return null;
	}

	localMove = localMoveList.get(0); // default option
	return translateMovetoGlobal(mx, my, localMove);
    }

    private Move getSafeMove(final TTTField currentField, final int myId, final int oppId) {
	// get coordinates to fields that have not been won or tied yet
	final List<Move> blockingMoves = macroField.getAvailableMoves();
	System.err.println("Moves leading to no choice for enemy: \n\t" + blockingMoves.toString());

	if (blockingMoves.size() > 0) {
	    final List<Move> safeMoves = new ArrayList<Move>();
	    Move potentialMove;
	    final TTTField nextEnemyField = new TTTField();
	    // look for safe moves
	    for (int m = 0; m < blockingMoves.size(); m++) {
		potentialMove = blockingMoves.get(m);
		if (currentField.isValidMove(potentialMove)) { // is this a move we can make?
		    nextEnemyField.setBoard(gameField.getMicroBoard(potentialMove.mX, potentialMove.mY)); // board for enemy in next turn
		    if (!nextEnemyField.hasSomeInLine(oppId, 2)) { // as safe as it gets
			safeMoves.add(potentialMove);
		    }
		}
	    }
	    System.err.println("Moves leading to no choice + no win for enemy: \n\t" + safeMoves.toString());

	    if (safeMoves.size() == 0) {
		// no options
		return null;
	    } else if (safeMoves.size() == 1) {
		// only one choice -> go for it
		return safeMoves.get(0);
	    } else { // more than one choice -> YAY
		// get a sorted move list for this field
		final List<Move> availableMoves = getOrderedMoveList(currentField, myId);
		for (int m = 0; m < availableMoves.size(); m++) {
		    potentialMove = availableMoves.get(m);
		    if (safeMoves.contains(potentialMove)) {
			return potentialMove;
		    }
		}
	    }
	}
	return null;
    }

    /**
     * Translates a move to the big board coordinate system
     * 
     * @param mx
     *            microboard x coord
     * @param my
     *            microboard y coord
     * @param localMove
     *            local coords
     * @return
     */
    private Move translateMovetoGlobal(int mx, int my, final Move localMove) {
	System.err.println("Local move: " + localMove.mX + " " + localMove.mY);
	mx = mx * 3 + localMove.mX;
	my = my * 3 + localMove.mY;
	System.err.println("Global coords: " + mx + " " + my);
	return new Move(mx, my);
    }

    private List<Move> getOrderedMoveList(final TTTField tttField, final int playerId) {
	final ArrayList<Move> moves = new ArrayList<Move>();
	// Win: If the player has two in a row, they can place a third to get three in a row.
	for (int y = 0; y < 3; y++) {
	    for (int x = 0; x < 3; x++) {
		if (tttField.isValidMove(x, y)) {
		    tttField.setMark(x, y, playerId);
		    if (tttField.hasThreeInARow(playerId)) {
			System.err.println("Possibility for win on " + x + " " + y);
			moves.add(new Move(x, y));
		    }
		    tttField.removeMark(x, y);
		}
	    }
	}
	// Block: If the opponent has two in a row, the player must play the third themselves to block the opponent.
	final int oppId = 3 - playerId;
	for (int y = 0; y < 3; y++) {
	    for (int x = 0; x < 3; x++) {
		if (tttField.isValidMove(x, y)) {
		    tttField.setMark(x, y, oppId);
		    if (tttField.hasThreeInARow(oppId)) {
			System.err.println("Prevent opponent win on " + x + " " + y);
			final Move newMove = new Move(x, y);
			if (!moves.contains(newMove)) {
			    moves.add(newMove);
			}
		    }
		    tttField.removeMark(x, y);
		}
	    }
	}

	// Fork: Create an opportunity where the player has two threats to win (two non-blocked lines of 2).
	// TODO: dont know how to do that
	for (int y = 2; y >= 0; y--) {
	    for (int x = 0; x < 3; x++) {
		if (tttField.isValidMove(x, y)) {
		    tttField.setMark(x, y, playerId);
		    if (tttField.hasSomeInLine(playerId, 2)) {
			final Move newMove = new Move(x, y);
			if (!moves.contains(newMove)) {
			    moves.add(newMove);
			}
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
	    for (int x = 0; x < 3; x++) {
		if (tttField.isValidMove(x, y)) {
		    tttField.setMark(x, y, oppId);
		    if (tttField.hasSomeInLine(oppId, 2)) {
			final Move newMove = new Move(x, y);
			if (!moves.contains(newMove)) {
			    moves.add(newMove);
			}
		    }
		    tttField.removeMark(x, y);
		}
	    }
	}

	// Center: A player marks the center.
	if (tttField.isValidMove(1, 1)) {
	    final Move newMove = new Move(1, 1);
	    if (!moves.contains(newMove)) {
		moves.add(newMove);
	    }
	}

	// Opposite corner: If the opponent is in the corner, the player plays the opposite corner.
	Move corner, move;
	for (int m = 0; m < cornerList.size(); m++) {
	    corner = cornerList.get(m);
	    if (tttField.hasMark(corner.mX, corner.mY, oppId)) {
		move = new Move(2 - corner.mX, 2 - corner.mY); // opposite corner
		if (tttField.isValidMove(move.mX, move.mY)) {
		    final Move newMove = new Move(move.mX, move.mY);
		    if (!moves.contains(newMove)) {
			moves.add(newMove);
		    }
		}
	    }
	}

	// Empty corner: The player plays in a corner square.
	for (int m = 0; m < cornerList.size(); m++) {
	    corner = cornerList.get(m);
	    if (tttField.isValidMove(corner.mX, corner.mY)) {
		final Move newMove = new Move(corner.mX, corner.mY);
		if (!moves.contains(newMove)) {
		    moves.add(newMove);
		}
	    }
	}

	// TODO: Empty side: The player plays in a middle square on any of the 4 sides.

	// any other moves are added randomly
	final List<Move> availableMoves = tttField.getAvailableMoves();
	int idx;
	Move mv;
	while (!availableMoves.isEmpty()) {
	    idx = rand.nextInt(availableMoves.size());
	    mv = availableMoves.get(idx);
	    if (!moves.contains(mv)) {
		moves.add(mv);
	    }
	    availableMoves.remove(idx);
	}

	return moves;
    }

    public static void main(String[] args) {
	final BotParser parser = new BotParser(new BotStarter());
	parser.run();
    }
}
