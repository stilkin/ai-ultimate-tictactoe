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
    public Move makeTurn(final Field field) {
	// initialization
	gameField = field;
	myId = BotParser.mBotId;
	oppId = 3 - myId;

	int mx = -1, my = -1;
	System.err.println(field.toString()); // debug

	macroField.clearBoard();
	smallField.clearBoard();

	// get big board
	macroField.setBoard(gameField.getValidMacroBoard());
	System.err.println(macroField.toString());

	final List<Move> activeBoards = gameField.getActiveMicroBoards();
	System.err.println("Active boards: " + activeBoards.toString());
	if (activeBoards.size() == 1) {
	    final Move activeBoard = activeBoards.get(0);
	    mx = activeBoard.mX;
	    my = activeBoard.mY;
	}

	if (mx >= 0 && my >= 0) { // play on a designated small field
	    System.err.println("Playing on microboard: " + mx + " " + my);
	    smallField.setBoard(gameField.getMicroBoard(mx, my));
	    final Move safeMove = getSafeMove(smallField, myId, oppId);
	    if (safeMove != null) { // this is ideal
		System.err.println(smallField.toString());
		System.err.println("Keeping it safe: " + safeMove.mX + " " + safeMove.mY);
		return translateMovetoGlobal(mx, my, safeMove);
	    }
	} else { // this means we get to choose the small field
	    final List<Move> myMacroMoveList = getOrderedMoveList(macroField, myId); // my moves on the big field
	    if (myMacroMoveList.size() < 1) {
		System.err.println("ERROR: NO MACRO MOVES POSSIBLE");
		return null;
	    }

	    // look for winning option
	    Move potentialBoardCoords;
	    for (int m = 0; m < myMacroMoveList.size(); m++) {
		potentialBoardCoords = myMacroMoveList.get(m);
		final int bx = potentialBoardCoords.mX;
		final int by = potentialBoardCoords.mY;
		if (macroField.isValidMove(bx, by)) {
		    macroField.setMark(bx, by, myId);
		    if (macroField.hasThreeInARow(myId)) {
			System.err.println("Possibility for global win on position " + bx + " " + by);
			smallField.setBoard(gameField.getMicroBoard(bx, by));
			final List<Move> winningMoves = getWinningMoves(smallField, myId);
			if (winningMoves.size() > 0) {
			    final Move finishingMove = winningMoves.get(0);
			    System.err.println("Choosing winning microboard: " + bx + " " + by);
			    System.err.println(smallField.toString());
			    return translateMovetoGlobal(bx, by, finishingMove);
			}
		    }
		    macroField.removeMark(bx, by);
		}
	    }

	    // look for safe options
	    for (int m = 0; m < myMacroMoveList.size(); m++) {
		potentialBoardCoords = myMacroMoveList.get(m);
		smallField.setBoard(gameField.getMicroBoard(potentialBoardCoords.mX, potentialBoardCoords.mY));
		final Move safeMove = getSafeMove(smallField, myId, oppId);
		if (safeMove != null) { // this is ideal
		    mx = potentialBoardCoords.mX;
		    my = potentialBoardCoords.mY;
		    System.err.println("Choosing safe microboard: " + mx + " " + my);
		    System.err.println(smallField.toString());
		    return translateMovetoGlobal(mx, my, safeMove);
		}
	    }

	    // set a fallback option
	    final Move boardCoords = myMacroMoveList.get(0);
	    mx = boardCoords.mX;
	    my = boardCoords.mY;
	    System.err.println("Choosing (unsafe) microboard: " + mx + " " + my);
	    // TODO: choose better?
	}

	// if we are here, we are not playing safe moves ^^
	smallField.setBoard(gameField.getMicroBoard(mx, my));
	System.err.println(smallField.toString());

	final List<Move> localMoveList = getOrderedMoveList(smallField, myId);
	if (localMoveList.size() < 1) { // whoops
	    System.err.println("ERROR: NO MICRO MOVES POSSIBLE");
	    return null;
	}

	Move localMove = localMoveList.get(0); // default option
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

    private List<Move> getWinningMoves(final TTTField tttField, final int playerId) {
	final ArrayList<Move> moves = new ArrayList<Move>();

	for (int y = 0; y < 3; y++) {
	    for (int x = 0; x < 3; x++) {
		if (tttField.isValidMove(x, y)) {
		    tttField.setMark(x, y, playerId);
		    if (tttField.hasThreeInARow(playerId)) {
			System.err.println(String.format("Player %d can win on %d %d", playerId, x, y));
			moves.add(new Move(x, y));
		    }
		    tttField.removeMark(x, y);
		}
	    }
	}

	return moves;
    }

    private List<Move> getTwoInlineMoves(final TTTField tttField, final int playerId) {
	final ArrayList<Move> moves = new ArrayList<Move>();

	for (int y = 2; y >= 0; y--) {
	    for (int x = 0; x < 3; x++) {
		if (tttField.isValidMove(x, y)) {
		    if (!tttField.hasSomeInLine(playerId, 2)) { // no marks to begin with
			tttField.setMark(x, y, playerId);
			if (tttField.hasSomeInLine(playerId, 2)) { // a mark appears
			    moves.add(new Move(x, y));
			}
			tttField.removeMark(x, y);
		    }
		}
	    }
	}

	return moves;
    }

    // TODO: check if order makes sense
    // TODO: filter out pointless moves (that can not lead to three in a row
    private List<Move> getOrderedMoveList(final TTTField tttField, final int playerId) {
	final ArrayList<Move> moves = new ArrayList<Move>();
	final int oppId = 3 - playerId;

	// Win: If the player has two in a row, they can place a third to get three in a row.
	moves.addAll(getWinningMoves(tttField, playerId));

	// Block: If the opponent has two in a row, the player must play the third themselves to block the opponent.
	moves.addAll(getWinningMoves(tttField, oppId));

	// Fork: Create an opportunity where the player has two threats to win (two non-blocked lines of 2)..
	// TODO: don't know how to do that
	/**
	 * Blocking an opponent's fork: Option 1: The player should create two in a row to force the opponent into defending, as long as it doesn't result in them creating a fork.
	 * For example, if "X" has a corner, "O" has the center, and "X" has the opposite corner as well, "O" must not play a corner in order to win. (Playing a corner in this
	 * scenario creates a fork for "X" to win.) Option 2: If there is a configuration where the opponent can fork, the player should block that fork.
	 */
	// TODO: don't know how to do that

	// try to get two in line
	moves.addAll(getTwoInlineMoves(tttField, playerId));

	// try to block enemy two in line
	moves.addAll(getTwoInlineMoves(tttField, oppId));

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
		    if (!moves.contains(move)) {
			moves.add(move);
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

	// remove all duplicates // TODO: make more elegant
	final List<Move> filteredList = new ArrayList<Move>();
	for (int m = 0; m < moves.size(); m++) {
	    mv = moves.get(m);
	    if (!filteredList.contains(mv)) {
		filteredList.add(mv);
	    }
	}

	return filteredList;
    }

    public static void main(String[] args) {
	final BotParser parser = new BotParser(new BotStarter());
	parser.run();
    }
}
