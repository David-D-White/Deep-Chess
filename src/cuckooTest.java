import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.util.Scanner;

import org.petero.cuckoo.engine.chess.Game;
import org.petero.cuckoo.engine.chess.Game.GameState;
import org.petero.cuckoo.engine.chess.HumanPlayer;
import org.petero.cuckoo.engine.chess.Move;
import org.petero.cuckoo.engine.chess.MoveGen;
import org.petero.cuckoo.engine.chess.Piece;
import org.petero.cuckoo.engine.chess.TextIO;

import ca.daviddwhite.deep_chess.ChessNet;

public class cuckooTest {
	public static final Move EMPTY = new Move(0, 0, Piece.EMPTY);

	public static ChessNet cp;
	public static HumanPlayer hp = new HumanPlayer();
	public static MoveGen mg = new MoveGen();

	public static void main(String[] args) throws InterruptedException {

		try {
			cp = new ChessNet(new FileInputStream("nets/Net_Trained.txt"));
		} catch (FileNotFoundException e) {
			e.printStackTrace();
			System.out.println("wrong file fukko");
			System.exit(0);
		}

		Game game = new Game(hp, cp);

		Scanner s = new Scanner(System.in);

		System.out.println(game.pos.toString());
		System.out.println(game.getGameState());
		System.out.println(MoveGen.inCheck(game.pos));

		while (game.getGameState() == GameState.ALIVE) {
			MoveGen.MoveList moves;
			if (!MoveGen.inCheck(game.pos))
				moves = mg.pseudoLegalMoves(game.pos);
			else
				moves = mg.checkEvasions(game.pos);
			MoveGen.removeIllegal(game.pos, moves);

			moveCheck: while (true) {
				String command = s.next();
				Move move = TextIO.uciStringToMove(command);
				for (int i = 0; i < moves.m.length; i++) {
					if (moves.m[i].equals(move)) {
						game.processString(TextIO.moveToString(game.pos, move, false));
						break moveCheck;
					} else if (moves.m[i].equals(EMPTY))
						break;
				}
				System.out.println("Invalid Move: " + move);
			}

			game.processString(cp.getCommand(game.pos, false, null));

			System.out.println(game.pos.toString());
			System.out.println(game.getGameState());
			System.out.println(MoveGen.inCheck(game.pos));
		}

		s.close();

		// System.out.println(game.pos.toString());
		// System.out.println(game.getGameStateString());
		// System.out.println(game.pos.fullMoveCounter);
		//
		// System.out.println("White:" + game.getFitness(cp));
		// System.out.println("Black:" + game.getFitness(cp2));
		// f0ba1035a7a520df

		// Thread.sleep(10*60*1000);
	}
}
