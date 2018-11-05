package ca.daviddwhite.deep_chess;

import java.util.List;

import org.petero.cuckoo.engine.chess.Move;
import org.petero.cuckoo.engine.chess.MoveGen;
import org.petero.cuckoo.engine.chess.Player;
import org.petero.cuckoo.engine.chess.Position;
import org.petero.cuckoo.engine.chess.TextIO;

import ca.daviddwhite.deep_chess.net.NeuralNet;
import ca.daviddwhite.deep_chess.net.Neuron;

/**
 * The Class ChessNet is a class which makes chess moves using a neural network.
 */
public class ChessNet implements Player {

	/** The number of neurons in the output layer of the network. */
	public static final int INPUTS = 17;

	/** The number of neurons in each hidden layer of the network. */
	public static final int[] HIDDEN_LAYERS = {100, 100, 100, 100};

	/** The number of neurons in the output layer of the network */
	public static final int OUTPUTS = 1;

	/** The step size used to normalize the inputs from -1 to 1. */
	public static final double INPUT_STEP = (2 / 15.0);

	private NeuralNet net;
	// Current fitness of the net
	double fitness;
	// Game stats
	int wins, loss, draws;

	/**
	 * Instantiates a new chess net.
	 */
	public ChessNet() {
		net = new NeuralNet(INPUTS, HIDDEN_LAYERS, OUTPUTS);
	}

	
	/**
	 * Decide on a mobe based on the current board state.
	 *
	 * @param pos the position of the game
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

		// Split the hash value into hex digits and parse it
		String hash = Long.toHexString(pos.zobristHash());
		double[] inputs = new double[INPUTS];
		for (int i = 0; i < hash.length(); i++) {
			inputs[i] = Integer.parseInt(hash.charAt(i) + "", 16) * INPUT_STEP - 1;
		}

		// Add number of moves to the input layer
		inputs[INPUTS - 1] = moves.size;

		// Set the input neurons of the neural net and calculate move
		Neuron[] inputLayer = net.getInputs();
		for (int i = 0; i < inputLayer.length; i++)
			inputLayer[i].value = inputs[i];
		net.feedForward();

		// Convert output to a move index
		int move = (int) ((net.getOutputs()[0].value + 1) / 2 * moves.size);
		return moves.m[move];
	}

	/*
	 * (non-Javadoc)
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
	 * @see org.petero.cuckoo.engine.chess.Player#isHumanPlayer()
	 */
	@Override
	public boolean isHumanPlayer() {
		return false;
	}

	/*
	 * (non-Javadoc)
	 * @see org.petero.cuckoo.engine.chess.Player#useBook(boolean)
	 */
	@Override
	public void useBook(boolean bookOn) {
	}

	/*
	 * (non-Javadoc)
	 * @see org.petero.cuckoo.engine.chess.Player#timeLimit(int, int, boolean)
	 */
	@Override
	public void timeLimit(int minTimeLimit, int maxTimeLimit, boolean randomMode) {
	}

	/*
	 * (non-Javadoc)
	 * @see org.petero.cuckoo.engine.chess.Player#clearTT()
	 */
	@Override
	public void clearTT() {
	}
}
