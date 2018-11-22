import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.util.Scanner;

import org.petero.cuckoo.engine.chess.ComputerPlayer;
import org.petero.cuckoo.engine.chess.Game;
import org.petero.cuckoo.engine.chess.Game.GameState;
import org.petero.cuckoo.engine.chess.HumanPlayer;
import org.petero.cuckoo.engine.chess.Move;
import org.petero.cuckoo.engine.chess.MoveGen;
import org.petero.cuckoo.engine.chess.Piece;
import org.petero.cuckoo.engine.chess.Player;
import org.petero.cuckoo.engine.chess.TextIO;

import ca.daviddwhite.deep_chess.ChessNet;

public class cuckooTest {
	public static final Move EMPTY = new Move(0, 0, Piece.EMPTY);

	public static ChessNet cp;
	public static ChessNet cp2;
	public static ComputerPlayer c1, c2;
	public static HumanPlayer hp = new HumanPlayer();
	public static MoveGen mg = new MoveGen();

	public static void playLoadedNet(String fileHandle) {
		try {
			cp = new ChessNet(new FileInputStream("nets/" + fileHandle));
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
	}

	public static void playRandomNets() {
		cp = new ChessNet();
		cp2 = new ChessNet();

		Game game = new Game(cp, cp2);

		System.out.println(game.pos.toString());

		while (game.getGameState() == GameState.ALIVE) {

			game.processString(cp.getCommand(game.pos, false, null));

			game.processString(cp2.getCommand(game.pos, false, null));

			try {
				Thread.sleep(1000);
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}

			System.out.println(game.pos.toString());
			System.out.println(game.getGameStateString());
			System.out.println(game.pos.fullMoveCounter);

			System.out.println("White:" + game.pos.wMtrl);
			System.out.println("Black:" + game.pos.bMtrl);
		}
	}

	public static void playRandomComp(int minTime, int maxTime) {
		c1 = new ComputerPlayer();
		c1.timeLimit(minTime, maxTime, false);
		c1.useBook(false);
		c2 = new ComputerPlayer();
		c2.useBook(false);
		c2.timeLimit(minTime, maxTime, false);

		Game game = new Game(c1, c2);

		System.out.println();

		// System.out.println(game.pos.toString());

		while (game.getGameState() == GameState.ALIVE) {

			game.processString(c1.getCommand(game.pos, false, game.getHistory()));
			if (game.getGameState() != GameState.ALIVE)
				break;
			game.processString(c2.getCommand(game.pos, false, game.getHistory()));
		}

		System.out.println(game.pos.toString());
		System.out.println(game.getGameStateString());
		System.out.println(game.pos.fullMoveCounter);

		System.out.println("White:" + game.pos.wMtrl);
		System.out.println("Black:" + game.pos.bMtrl);
	}

	public static void compvcomp(Player p1, Player p2) {
		while (true) {
			Game game = new Game(p1, p2);

			// System.out.println(game.pos.toString());

			while (game.getGameState() == GameState.ALIVE) {

				game.processString(p1.getCommand(game.pos, false, game.getHistory()));

				if (game.getGameState() != GameState.ALIVE)
					break;
				game.processString(p2.getCommand(game.pos, false, game.getHistory()));
			}

			System.out.println(game.pos.toString());
			System.out.println(game.getGameStateString());
			System.out.println(game.pos.fullMoveCounter);

			System.out.println("White:" + game.pos.wMtrl);
			System.out.println("Black:" + game.pos.bMtrl);

			if (true || game.getGameState() == GameState.BLACK_MATE) {
				System.out.println(game.getHistory().size());
				break;
			}
		}
	}

	public static void main(String[] args) throws InterruptedException, FileNotFoundException {
		// String fileHandle = "net_1590.txt";
		// cp = new ChessNet(new FileInputStream("nets/" + fileHandle));
		// c1 = new ComputerPlayer();
		// c1.useBook(false);
		// c1.timeLimit(60000, 60000, false);
		//
		// compvcomp(cp, c1);
		// compvcomp(c1, cp);

		playLoadedNet("net_530.txt");

		// for (int i = 0; i < 10; i++)
		// playRandomComp(6000, 6000);
	}
}
