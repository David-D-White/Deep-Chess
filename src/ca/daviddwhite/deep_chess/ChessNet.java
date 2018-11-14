package ca.daviddwhite.deep_chess;

import java.util.List;

import org.petero.cuckoo.engine.chess.Game.GameState;
import org.petero.cuckoo.engine.chess.Move;
import org.petero.cuckoo.engine.chess.MoveGen;
import org.petero.cuckoo.engine.chess.Player;
import org.petero.cuckoo.engine.chess.Position;
import org.petero.cuckoo.engine.chess.TextIO;

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
	public static final int INPUTS = 17;

	/** The number of neurons in each hidden layer of the network. */
	public static final int[] HIDDEN_LAYERS = {16,8};

	/** The number of neurons in the output layer of the network */
	public static final int OUTPUTS = 1;

	/** The step size used to normalize the inputs from -1 to 1. */
	public static final double INPUT_STEP = (2 / 15.0);

	private NeuralNet net;
	// Current fitness of the net
	private double fitness;
	// Game stats
	private int wins, losses, draws;

	/**
	 * Instantiates a new chess net.
	 */
	public ChessNet() {
		net = new NeuralNet(INPUTS, HIDDEN_LAYERS, OUTPUTS);
	}

	// Copy constructor
	public ChessNet(ChessNet cn) {
		net = new NeuralNet(cn.net);
		fitness = cn.fitness;
		wins = cn.wins;
		losses = cn.losses;
		draws = cn.draws;
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
		default:
			draws++;
		}

		// Keep a rolling average of the fitness score
		int gameNum = wins + losses + draws;
		this.fitness *= (gameNum - 1.0) / gameNum;
		this.fitness += fitness / gameNum;
	}

	/**
	 * Get the stats for this neural net in an array
	 *
	 * @return the stats as an array in the form of {fitness, wins, losses, draws}
	 */
	public double[] getStats() {
		return new double[] {fitness, wins, losses, draws};
	}

	/**
	 * Reset the stats of this chess net.
	 */
	public void clearStats() {
		fitness = 0;
		wins = 0;
		losses = 0;
		draws = 0;
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

	// Debug method
	public void drawNet(PApplet p, float x, float y, float height, float width, float neuronDiam) {
		net.draw(p, x, y, height, width, neuronDiam);
	}
}
