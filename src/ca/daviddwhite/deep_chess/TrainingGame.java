package ca.daviddwhite.deep_chess;

import java.util.HashMap;

import org.petero.cuckoo.engine.chess.Game;
import org.petero.cuckoo.engine.chess.Move;
import org.petero.cuckoo.engine.chess.Piece;
import org.petero.cuckoo.engine.chess.TextIO;
import org.petero.cuckoo.engine.chess.UndoInfo;

import ca.daviddwhite.deep_chess.ChessNet.TrainingCopy;

/**
 * The Class TrainingGame hosts a game between two ChessNet instances.
 */
public class TrainingGame extends Game {
	public static final double TIE_FITNESS = Math.tanh(1);

	// The move counter for the 50 move draw rule.
	private int moveCounter50 = 0;

	// The position history stored as the number of occurrences of each state.
	private HashMap<Long, Integer> posHistory = new HashMap<Long, Integer>();

	// References to the two ChessNets.
	private ChessNet whiteNet, blackNet;

	// Has the game finished yet
	private boolean gameFinished;
	private boolean gameStarted;

	/**
	 * Instantiates a new training game.
	 *
	 * @param whitePlayer
	 *            the white player
	 * @param blackPlayer
	 *            the black player
	 */
	public TrainingGame(ChessNet whiteNet, ChessNet blackNet) {
		super(whiteNet, blackNet);
		this.whiteNet = whiteNet;
		this.blackNet = blackNet;
	}

	/**
	 * Make a move.
	 *
	 * @param m
	 *            the move to make
	 */
	public void makeMove(Move m) {
		// Track draw by 50 move rule
		int movePiece = pos.getPiece(m.from);
		if (movePiece == Piece.WPAWN || movePiece == Piece.BPAWN || isCaptureMove(m))
			moveCounter50 = 0;
		else
			moveCounter50++;

		// Track draw by 3 fold repition
		long hash = pos.zobristHash();
		if (posHistory.containsKey(hash))
			posHistory.put(hash, posHistory.get(hash) + 1);
		else
			posHistory.put(hash, 1);

		UndoInfo ui = new UndoInfo();
		pos.makeMove(m, ui);
		TextIO.fixupEPSquare(pos);
		while (currentMove < moveList.size()) {
			moveList.remove(currentMove);
			uiInfoList.remove(currentMove);
			drawOfferList.remove(currentMove);
		}
		moveList.add(m);
		uiInfoList.add(ui);
		drawOfferList.add(pendingDrawOffer);
		pendingDrawOffer = false;
		currentMove++;
	}

	// Check if the given move captures a piece
	private boolean isCaptureMove(Move m) {
		if (pos.getPiece(m.to) == Piece.EMPTY) {
			int p = pos.getPiece(m.from);
			return (p == (pos.whiteMove ? Piece.WPAWN : Piece.BPAWN) && m.to == pos.getEpSquare());
		}
		return true;
	}

	/**
	 * Run through a whole game simulation.
	 */
	public void completeGame() {
		while (getGameState() == GameState.ALIVE) {
			makeMove(whiteNet.getMove(pos));
			if (getGameState() != GameState.ALIVE)
				break;
			makeMove(blackNet.getMove(pos));
		}
		whiteNet.addGameStat(getFitness(true), true, getGameState());
		blackNet.addGameStat(getFitness(false), false, getGameState());
		gameFinished = true;
	}

	/**
	 * A thread safe version of completeGame which has more overhead.
	 */
	public void completeGameThreadSafe() {
		TrainingCopy white = new TrainingCopy(whiteNet);
		TrainingCopy black = new TrainingCopy(whiteNet);

		while (getGameState() == GameState.ALIVE) {
			makeMove(white.copy.getMove(pos));
			if (getGameState() != GameState.ALIVE)
				break;
			makeMove(black.copy.getMove(pos));
		}
		white.reference.addGameStat(getFitness(true), true, getGameState());
		black.reference.addGameStat(getFitness(false), false, getGameState());
		gameFinished = true;
	}

	/**
	 * Return true if the game has been fully run.
	 */
	public boolean gameFinished() {
		return gameFinished;
	}

	/**
	 * Gets the fitness of the given net for this game.
	 *
	 * @param net
	 *            the ChessNet to check
	 * @return the fitness of the ChessNet
	 */
	public double getFitness(boolean white) {
		switch (getGameState()) {
		case WHITE_MATE:
		case RESIGN_BLACK:
			return white ? 2 : Math.tanh((double) pos.bMtrl / (double) pos.wMtrl) * 0.25;
		case BLACK_MATE:
		case RESIGN_WHITE:
			return white ? Math.tanh((double) pos.wMtrl / (double) pos.bMtrl) * 0.25 : 2;
		case BLACK_STALEMATE:
		case WHITE_STALEMATE:
		case DRAW_50:
		case DRAW_REP:
			return white ? Math.tanh((double) pos.wMtrl / (double) pos.bMtrl) * 0.5 : Math.tanh((double) pos.bMtrl / (double) pos.wMtrl) * 0.5;
		default:
			return 0;
		}
	}

	/*
	 * (non-Javadoc)
	 * @see org.petero.cuckoo.engine.chess.Game#getGameState()
	 */
	@Override
	public GameState getGameState() {
		if (moveCounter50 >= 50) {
			return GameState.DRAW_50;
		} else {
			for (int i : posHistory.values()) {
				if (i >= 3)
					return GameState.DRAW_REP;
			}
		}
		return super.getGameState();
	}
}
