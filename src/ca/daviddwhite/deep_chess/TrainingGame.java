package ca.daviddwhite.deep_chess;

import java.util.HashMap;

import org.petero.cuckoo.engine.chess.Game;
import org.petero.cuckoo.engine.chess.Move;
import org.petero.cuckoo.engine.chess.Piece;
import org.petero.cuckoo.engine.chess.TextIO;
import org.petero.cuckoo.engine.chess.UndoInfo;

/**
 * The Class TrainingGame hosts a game between two ChessNet instances.
 */
public class TrainingGame extends Game {

    // The move counter for the 50 move draw rule.
    private int moveCounter50 = 0;

    // The position history stored as the number of occurrences of each state.
    private HashMap<Long, Integer> posHistory = new HashMap<Long, Integer>();

    // References to the two ChessNets.
    private ChessNet whiteNet, blackNet;

    /** Whether or not the game has been run yet. */
    public boolean gameFinished;

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
    }

    /**
     * Gets the fitness of the given net for this game.
     *
     * @param net
     *            the ChessNet to check
     * @return the fitness of the ChessNet
     */
    public double getFitness(ChessNet net) {
	if (net != blackNet && net != whiteNet)
	    return -1;

	switch (getGameState()) {
	    case WHITE_MATE:
	    case RESIGN_BLACK:
		return (net == whiteNet) ? 2 : 0;
	    case BLACK_MATE:
	    case RESIGN_WHITE:
		return (net == whiteNet) ? 0 : 2;
	    case BLACK_STALEMATE:
	    case WHITE_STALEMATE:
	    case DRAW_50:
	    case DRAW_REP:
		return (net == whiteNet) ? Math.tanh((double) pos.wMtrl / (double) pos.bMtrl) : Math.tanh((double) pos.bMtrl / (double) pos.wMtrl);
	    default:
		return 0;
	}
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.petero.cuckoo.engine.chess.Game#getGameState()
     */
    @Override
    public GameState getGameState() {
	if (moveCounter50 >= 50) {
	    return GameState.DRAW_50;
	}
	else {
	    for (int i : posHistory.values()) {
		if (i >= 3)
		    return GameState.DRAW_REP;
	    }
	}
	return super.getGameState();
    }
}
