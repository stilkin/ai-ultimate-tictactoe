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
	    smallField.setBoard(gameField.getMacroBoard());

	    Move myMove = playField(smallField);
	    if (myMove == null) { // nothing useful
		// default behavior: play random move
		final ArrayList<Move> validMoves = smallField.getAvailableMoves();
		myMove = validMoves.get(rand.nextInt(validMoves.size())); /* get random move from available moves */
	    }
	    mx = myMove.mX;
	    my = myMove.mY;
	}

	System.err.println("Playing on microboard: " + mx + " " + my);
	smallField.setBoard(gameField.getMicroBoard(mx, my));

	// PLAY ON THE SMALL FIELD
	Move myMove = playField(smallField);
	if (myMove == null) {
	 // default behavior: play random move
	    final ArrayList<Move> validMoves = smallField.getAvailableMoves();
	    myMove = validMoves.get(rand.nextInt(validMoves.size()));
	}

	// translate to big board
	mx = mx*3 + myMove.mX;
	my = my*3 + myMove.mY;
	System.err.println("Putting move: " + mx + " " + my);
	return new Move(mx, my);

    }

    private Move playField(final TTTField tttField) {
	// Win: If the player has two in a row, they can place a third to get three in a row.
	for (int y = 0; y < 3; y++) {
	    for (int x = 0; x < 3; x++) {
		tttField.setMark(x, y, myId);
		if (tttField.hasThreeInARow(myId)) {
		    return new Move(x, y);
		}
	    }
	}

	return null;
    }

    /*
     * 
     * Win: If the player has two in a row, they can place a third to get three in a row. Block: If the opponent has two in a row, the player must play the third themselves to
     * block the opponent. Fork: Create an opportunity where the player has two threats to win (two non-blocked lines of 2). Blocking an opponent's fork: Option 1: The player
     * should create two in a row to force the opponent into defending, as long as it doesn't result in them creating a fork. For example, if "X" has a corner, "O" has the center,
     * and "X" has the opposite corner as well, "O" must not play a corner in order to win. (Playing a corner in this scenario creates a fork for "X" to win.) Option 2: If there is
     * a configuration where the opponent can fork, the player should block that fork. Center: A player marks the center. (If it is the first move of the game, playing on a corner
     * gives "O" more opportunities to make a mistake and may therefore be the better choice; however, it makes no difference between perfect players.) Opposite corner: If the
     * opponent is in the corner, the player plays the opposite corner. Empty corner: The player plays in a corner square. Empty side: The player plays in a middle square on any of
     * the 4 sides.
     * 
     * 
     */

    public static void main(String[] args) {
	final BotParser parser = new BotParser(new BotStarter());
	parser.run();
    }
}
