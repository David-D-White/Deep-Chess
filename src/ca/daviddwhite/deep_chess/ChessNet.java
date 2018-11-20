package ca.daviddwhite.deep_chess;

import java.io.InputStream;
import java.util.List;

import org.petero.cuckoo.engine.chess.Game.GameState;
import org.petero.cuckoo.engine.chess.Move;
import org.petero.cuckoo.engine.chess.MoveGen;
import org.petero.cuckoo.engine.chess.Piece;
import org.petero.cuckoo.engine.chess.Player;
import org.petero.cuckoo.engine.chess.Position;
import org.petero.cuckoo.engine.chess.TextIO;
import org.petero.cuckoo.engine.chess.UndoInfo;

import ca.daviddwhite.deep_chess.net.MutationParameter;
import ca.daviddwhite.deep_chess.net.NeuralNet;
import ca.daviddwhite.deep_chess.net.Neuron;
import processing.core.PApplet;

/**
 * The Class ChessNet is a class which makes chess moves using a neural network.
 */
public class ChessNet implements Player {

    /**
     * A copy of a ChessNet class with reference to the original for thread-safe
     * training.
     */
    public static class TrainingCopy {

	/** The reference (original) ChessNet. */
	public ChessNet reference;

	/** The copy ChessNet. */
	public ChessNet copy;

	/**
	 * Instantiates a new training copy.
	 *
	 * @param chessNet
	 *            the chess net to copy
	 */
	public TrainingCopy(ChessNet chessNet) {
	    reference = chessNet;
	    copy = new ChessNet(chessNet.net);
	}
    }

    /** The number of neurons in the output layer of the network. */
    public static final int INPUTS = 64 * 2;

    /** The number of neurons in each hidden layer of the network. */
    public static final int[] HIDDEN_LAYERS = { 128 };

    /** The number of neurons in the output layer of the network */
    public static final int OUTPUTS = 1;

    /** The step size used to normalize the inputs from -1 to 1. */
    public static final double INPUT_STEP = (2 / 15.0);

    public static final double KING_VAL = 3.5, PAWN_VAL = 1, KNIGHT_VAL = 3.2, BISHOP_VAL = 3.33, ROOK_VAL = 5.2, QUEEN_VAL = 9.6;
    public static final double KING_REL = KING_VAL / QUEEN_VAL, PAWN_REL = PAWN_VAL / QUEEN_VAL, KNIGHT_REL = KNIGHT_VAL / QUEEN_VAL,
	    BISHOP_REL = BISHOP_VAL / QUEEN_VAL, ROOK_REL = ROOK_VAL / QUEEN_VAL, QUEEN_REL = 1;

    private NeuralNet net;
    // Current fitness of the net
    private double fitness;
    // Game stats
    private int wins, losses, draws, wasted;

    /**
     * Instantiates a new chess net.
     */
    public ChessNet() {
	net = new NeuralNet(INPUTS, HIDDEN_LAYERS, OUTPUTS);
    }

    public ChessNet(InputStream is) {
	net = new NeuralNet(is);
    }

    // Copy constructor
    public ChessNet(ChessNet cn) {
	net = new NeuralNet(cn.net);
	this.fitness = cn.fitness;
	this.wins = cn.wins;
	this.losses = cn.losses;
	this.draws = cn.draws;
	this.wasted = cn.wasted;
    }

    // Creates a new chessNet with an identical neural net
    private ChessNet(NeuralNet n) {
	net = new NeuralNet(n);
    }

    /**
     * Adds the given game stat to this nets record.
     *
     * @param fitness
     *            the fitness of the game
     * @param state
     *            the state of the finished game
     */
    public void addGameStat(double fitness, boolean isWhite, GameState state) {
	switch (state) {
	    case ALIVE:
		return;
	    case WHITE_MATE:
	    case RESIGN_BLACK:
		if (isWhite)
		    wins++;
		else
		    losses++;
		break;
	    case BLACK_MATE:
	    case RESIGN_WHITE:
		if (isWhite)
		    losses++;
		else
		    wins++;
		break;
	    case DRAW_50:
	    case DRAW_REP:
		wasted++;
		break;
	    default:
		draws++;
	}

	// Keep a rolling average of the fitness score
	int gameNum = wins + losses + draws + wasted;
	this.fitness *= (gameNum - 1.0) / gameNum;
	this.fitness += fitness / gameNum;
    }

    /**
     * Get the stats for this neural net in an array
     *
     * @return the stats as an array in the form of {fitness, wins, losses, draws, wasted}
     */
    public double[] getStats() {
	return new double[] { fitness, wins, losses, draws, wasted };
    }

    /**
     * Reset the stats of this chess net.
     */
    public void clearStats() {
	fitness = 0;
	wins = 0;
	losses = 0;
	draws = 0;
	wasted = 0;
    }

    /**
     * Decide on a mobe based on the current board state.
     *
     * @param pos
     *            the position of the game
     * @return the move to make
     */
    public Move getMove(Position pos) {
	// Generate Legal Moves
	MoveGen.MoveList moves;
	if (!MoveGen.inCheck(pos))
	    moves = new MoveGen().pseudoLegalMoves(pos);
	else
	    moves = new MoveGen().checkEvasions(pos);
	MoveGen.removeIllegal(pos, moves);

	double[] inputs = new double[INPUTS];
	double[] moveValues = new double[moves.size];
	boolean white = pos.whiteMove;

	// Fill neurons for the current position
	double[] start = getInputs(pos, white);
	Neuron[] n = net.getInputs();
	for (int j = 0; j < 64; j++) {
	    n[j].value = start[j];
	}

	// Fill second neuron set for all potential moves
	for (int i = 0; i < moves.size; i++) {
	    UndoInfo ui = new UndoInfo();
	    pos.makeMove(moves.m[i], ui);

	    double[] move = getInputs(pos, white);
	    for (int j = 64; j < INPUTS; j++) {
		n[j].value = move[j - 64];
	    }
	    net.feedForward();

	    // Rank move
	    moveValues[i] = net.getOutputs()[0].value;

	    pos.unMakeMove(moves.m[i], ui);
	}

	// Search for highest ranked move
	int bestMoveIndex = 0;
	for (int i = 0; i < moveValues.length; i++) {
	    if (moveValues[i] > moveValues[bestMoveIndex])
		bestMoveIndex = i;
	}

	return moves.m[bestMoveIndex];
    }

    public static double[] getInputs(Position pos, boolean white) {
	double[] inputs = new double[64];

	if (white) {
	    for (int i = 0; i < 64; i++) {
		switch (pos.getPiece(i)) {
		    case Piece.WPAWN:
			inputs[i] = PAWN_REL;
			break;
		    case Piece.WKNIGHT:
			inputs[i] = KNIGHT_REL;
			break;
		    case Piece.WROOK:
			inputs[i] = ROOK_REL;
			break;
		    case Piece.WBISHOP:
			inputs[i] = BISHOP_REL;
			break;
		    case Piece.WQUEEN:
			inputs[i] = QUEEN_REL;
			break;
		    case Piece.WKING:
			inputs[i] = KING_REL;
			break;
		    case Piece.BPAWN:
			inputs[i] = -PAWN_REL;
			break;
		    case Piece.BKNIGHT:
			inputs[i] = -KNIGHT_REL;
			break;
		    case Piece.BROOK:
			inputs[i] = -ROOK_REL;
			break;
		    case Piece.BBISHOP:
			inputs[i] = -BISHOP_REL;
			break;
		    case Piece.BQUEEN:
			inputs[i] = -QUEEN_REL;
			break;
		    case Piece.BKING:
			inputs[i] = -KING_REL;
			break;
		    case Piece.EMPTY:
			inputs[i] = 0;
			break;
		}
	    }
	}
	else {
	    for (int i = 0; i < 64; i++) {
		int index = 64 - (8 + i / 8 * 8) + ((i) % 8);
		switch (pos.getPiece(i)) {
		    case Piece.WPAWN:
			inputs[index] = -PAWN_REL;
			break;
		    case Piece.WKNIGHT:
			inputs[index] = -KNIGHT_REL;
			break;
		    case Piece.WROOK:
			inputs[index] = -ROOK_REL;
			break;
		    case Piece.WBISHOP:
			inputs[index] = -BISHOP_REL;
			break;
		    case Piece.WQUEEN:
			inputs[index] = -QUEEN_REL;
			break;
		    case Piece.WKING:
			inputs[index] = -KING_REL;
			break;
		    case Piece.BPAWN:
			inputs[index] = PAWN_REL;
			break;
		    case Piece.BKNIGHT:
			inputs[index] = KNIGHT_REL;
			break;
		    case Piece.BROOK:
			inputs[index] = ROOK_REL;
			break;
		    case Piece.BBISHOP:
			inputs[index] = BISHOP_REL;
			break;
		    case Piece.BQUEEN:
			inputs[index] = QUEEN_REL;
			break;
		    case Piece.BKING:
			inputs[index] = KING_REL;
			break;
		    case Piece.EMPTY:
			inputs[index] = 0;
			break;
		}
	    }
	}

	return inputs;
    }

    /**
     * Gets a mutated copy of this Chess NEet.
     *
     * @return the mutated copy of this Chess NEt
     */
    public ChessNet getMutatedCopy(MutationParameter mp) {
	ChessNet mutated = new ChessNet(this.net);

	double mutationType = Math.random();

	if (mutationType < mp.weightMutateChance)
	    mutated.net.mutateWeights(mp.weightModifyChance, mp.weightModifyChance);
	else if (mutationType < mp.weightMutateChance + mp.neuronMutateChance)
	    mutated.net.mutateNeurons(mp.neuronModifyChance, mp.neuronRemoveChance);
	else
	    mutated.net.mutateLayer(mp.maxLayerInsert, mp.maxLayerRemove, mp.layerRemoveChance);

	return mutated;
    }

    /**
     * Gets the neural net attached to this chessNet.
     *
     * @return the net attached to this chess net
     */
    public NeuralNet getNet() {
	return this.net;
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * org.petero.cuckoo.engine.chess.Player#getCommand(org.petero.cuckoo.engine.
     * chess.Position, boolean, java.util.List)
     */
    @Override
    public String getCommand(Position pos, boolean drawOffer, List<Position> history) {
	return TextIO.moveToString(pos, getMove(pos), false);
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.petero.cuckoo.engine.chess.Player#isHumanPlayer()
     */
    @Override
    public boolean isHumanPlayer() {
	return false;
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.petero.cuckoo.engine.chess.Player#useBook(boolean)
     */
    @Override
    public void useBook(boolean bookOn) {}

    /*
     * (non-Javadoc)
     * 
     * @see org.petero.cuckoo.engine.chess.Player#timeLimit(int, int, boolean)
     */
    @Override
    public void timeLimit(int minTimeLimit, int maxTimeLimit, boolean randomMode) {}

    /*
     * (non-Javadoc)
     * 
     * @see org.petero.cuckoo.engine.chess.Player#clearTT()
     */
    @Override
    public void clearTT() {}

    // Debug method
    public void drawNet(PApplet p, float x, float y, float height, float width, float neuronDiam) {
	net.draw(p, x, y, height, width, neuronDiam);
    }
}
