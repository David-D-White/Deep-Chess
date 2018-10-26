import java.util.Scanner;

import org.petero.cuckoo.engine.chess.*;

public class cuckooTest {
	public static void main(String[] args) {
		Player p1 = new HumanPlayer();
		Player p2 = new ComputerPlayer();
		Game game = new Game(p1, p2);
		System.out.println(game.pos.toString());

		Scanner s = new Scanner(System.in);

		while (true) {
			String command = s.next();
			MoveGen.MoveList moves;
			if (!MoveGen.inCheck(game.pos))
				moves = new MoveGen().pseudoLegalMoves(game.pos);
			else 
				moves = new MoveGen().checkEvasions(game.pos);
			MoveGen.removeIllegal(game.pos, moves);
			Move move = TextIO.uciStringToMove(command);
			boolean valid = false;
			for (int i = 0; i < moves.m.length; i++) {
				if (moves.m[i].equals(move)) {
					game.pos.makeMove(move, new UndoInfo());
					valid = true;
					break;
				}
			}
			if (!valid)
				System.out.println("Invalid Move: " + move);
			else {
				System.out.println(game.pos.toString());
				System.out.println(game.getGameState());
				System.out.println(MoveGen.inCheck(game.pos));
			}

			// String hash = Long.toHexString(game.pos.zobristHash());
			// for (char c : hash)

		}

		// f0ba1035a7a520df

	}
}
