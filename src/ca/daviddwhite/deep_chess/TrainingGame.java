package ca.daviddwhite.deep_chess;

import java.util.ArrayList;
import java.util.HashMap;

import org.petero.cuckoo.engine.chess.Game;
import org.petero.cuckoo.engine.chess.Move;
import org.petero.cuckoo.engine.chess.Piece;
import org.petero.cuckoo.engine.chess.Player;
import org.petero.cuckoo.engine.chess.Position;
import org.petero.cuckoo.engine.chess.TextIO;
import org.petero.cuckoo.engine.chess.UndoInfo;

/**
 * The Class TrainingGame hosts a game between two ChessNet instances.
 */
public class TrainingGame extends Game {
	// The position history stored as the number of occurrences of each state.
	private HashMap<Long, Integer> posOccurrences = new HashMap<Long, Integer>();

	// The position history for the 50 move rule
	private ArrayList<Position> history;

	// Has the game finished yet
	private boolean gameFinished;

	/**
	 * Instantiates a new training game.
	 *
	 * @param whitePlayer
	 *            the white player
	 * @param blackPlayer
	 *            the black player
	 */
	public TrainingGame(Player whitePlayer, Player blackPlayer) {
		super(whitePlayer, blackPlayer);
		history = new ArrayList<Position>();
		posOccurrences = new HashMap<Long, Integer>();
	}

	/*
	 * (non-Javadoc)
	 * @see org.petero.cuckoo.engine.chess.Game#processString(java.lang.String)
	 */
	@Override
	public boolean processString(String str) {
		if (handleCommand(str))
			return true;

		Move m = TextIO.stringToMove(pos, str);
		if (m == null) {
			return false;
		}

		makeMove(m);

		return true;
	}

	// make a move and calculate draw conditionss
	private void makeMove(Move m) {
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

		// Track draw by 3 fold repition
		long hash = pos.zobristHash();
		if (posOccurrences.containsKey(hash))
			posOccurrences.put(hash, posOccurrences.get(hash) + 1);
		else
			posOccurrences.put(hash, 1);

		// Track draw by 50 move rule
		int movePiece = pos.getPiece(m.from);
		if (movePiece == Piece.WPAWN || movePiece == Piece.BPAWN || isCaptureMove(m))
			history.clear();
		else
			history.add(pos);
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
	 * Run through an entire game simulations.
	 */
	public void completeGame() {
		while (getGameState() == GameState.ALIVE) {
			processString(whitePlayer.getCommand(pos, false, history));
			if (getGameState() != GameState.ALIVE)
				break;
			processString(blackPlayer.getCommand(pos, false, history));
		}
		if (whitePlayer instanceof ChessNet)
			((ChessNet) whitePlayer).addGameStat(getFitness(true), true, getGameState());
		if (blackPlayer instanceof ChessNet)
			((ChessNet) blackPlayer).addGameStat(getFitness(false), false, getGameState());
		gameFinished = true;
	}

	/**
	 * A thread safe version of completeGame which has more overhead.
	 */
	public void completeGameThreadSafe() {
		Player tempWhite = whitePlayer instanceof ChessNet ? new ChessNet((ChessNet) whitePlayer) : whitePlayer;
		Player tempBlack = blackPlayer instanceof ChessNet ? new ChessNet((ChessNet) blackPlayer) : blackPlayer;

		while (getGameState() == GameState.ALIVE) {
			processString(tempWhite.getCommand(pos, false, history));
			if (getGameState() != GameState.ALIVE)
				break;
			processString(tempBlack.getCommand(pos, false, history));
		}
		if (whitePlayer instanceof ChessNet) {
			((ChessNet) whitePlayer).addGameStat(getFitness(true), true, getGameState());
		}
		if (blackPlayer instanceof ChessNet) {
			((ChessNet) blackPlayer).addGameStat(getFitness(false), false, getGameState());
		}
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
		double turnVal = Math.tanh(pos.fullMoveCounter / 30.0);
		switch (getGameState()) {
		case WHITE_MATE:
		case RESIGN_BLACK:
			return white ? 2 * Math.tanh((double) pos.wMtrl / (double) pos.bMtrl) : 0.5 * Math.tanh((double) pos.bMtrl / (double) pos.wMtrl) * turnVal;
		case BLACK_MATE:
		case RESIGN_WHITE:
			return white ? 0.5 * Math.tanh((double) pos.wMtrl / (double) pos.bMtrl) * turnVal : 2 * Math.tanh((double) pos.bMtrl / (double) pos.wMtrl);
		case BLACK_STALEMATE:
		case WHITE_STALEMATE:
		case DRAW_50:
		case DRAW_REP:
			return white ? Math.tanh((double) pos.wMtrl / (double) pos.bMtrl) * turnVal : Math.tanh((double) pos.bMtrl / (double) pos.wMtrl) * turnVal;
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
		if (history.size() >= 50) {
			return GameState.DRAW_50;
		} else {
			for (int i : posOccurrences.values()) {
				if (i >= 3)
					return GameState.DRAW_REP;
			}
		}
		return super.getGameState();
	}
}
