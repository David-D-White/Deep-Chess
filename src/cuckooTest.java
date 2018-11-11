import java.util.Scanner;

import org.petero.cuckoo.engine.chess.*;
import org.petero.cuckoo.engine.chess.Game.GameState;

import ca.daviddwhite.deep_chess.ChessNet;
import ca.daviddwhite.deep_chess.Trainer;
import ca.daviddwhite.deep_chess.TrainingGame;
import ca.daviddwhite.deep_chess.net.Sigmoid;

public class cuckooTest {
	public static final Move EMPTY = new Move(0, 0, Piece.EMPTY);

	public static ChessNet cp = new ChessNet();
	public static ChessNet cp2 = new ChessNet();
	public static MoveGen mg = new MoveGen();

	public static void main(String[] args) throws InterruptedException {
		TrainingGame game = null;

		Trainer t = new Trainer(101);
		long time = System.currentTimeMillis();
		t.runGeneration();
		System.out.println((System.currentTimeMillis() - time) / 1000.0);

		System.out.println("Done");

		// for (int i = 0; i < 10100; i++) {
		// game = new TrainingGame(cp, cp2);
		//
		// // System.out.println("White: " + game.getFitness(cp));
		// // System.out.println("Black: " + game.getFitness(cp2));
		//
		// game.completeGame();
		// if (i % 100 == 0)
		// System.out.println(String.format("%.2f", i / 10100.0 * 100) + "%");
		// }

		// Scanner s = new Scanner(System.in);
		//
		// while (game.getGameState() == GameState.ALIVE) {
		// MoveGen.MoveList moves;
		// if (!MoveGen.inCheck(game.pos))
		// moves = mg.pseudoLegalMoves(game.pos);
		// else
		// moves = mg.checkEvasions(game.pos);
		// MoveGen.removeIllegal(game.pos, moves);
		//
		// moveCheck: while (true) {
		// String command = s.next();
		// Move move = TextIO.uciStringToMove(command);
		// for (int i = 0; i < moves.m.length; i++) {
		// if (moves.m[i].equals(move)) {
		// game.makeMove(move);
		// break moveCheck;
		// } else if (moves.m[i].equals(EMPTY))
		// break;
		// }
		// System.out.println("Invalid Move: " + move);
		// }
		// System.out.println(game.pos.toString());
		// System.out.println(game.getGameState());
		// System.out.println(MoveGen.inCheck(game.pos));
		// }

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
