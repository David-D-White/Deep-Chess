/*
 * File name: Trainer.java
 * Last updated: 11-Nov-2018
 * @author David White Copyright (c) (2018). All rights reserved.
 */
package ca.daviddwhite.deep_chess;

import java.util.ArrayList;
import java.util.HashMap;

import ca.daviddwhite.deep_chess.net.MutationParameter;

// TODO: Auto-generated Javadoc
/**
 * The Class Trainer.
 */
public class Trainer {

	// percent top nets to keep per generation
	private static double elitePercent = 0.1;
	// percent random nets to add per generation
	private static double randomPercent = 0.1;

	// The current generation
	private ChessNet[] generation;
	private ChessNet[] best;

	// The best nets and stats from each previous generation.
	private ArrayList<ChessNet> generationHistory;
	private ArrayList<double[]> generationFitness;
	private ArrayList<double[]> bestFitness;

	// The fitness of each generation
	private HashMap<Integer, double[]> generationRelativeFitness;

	/**
	 * Instantiates a new Trainer class to train chessNets.
	 *
	 * @param generationSize
	 *            the generation size
	 */
	public Trainer(int generationSize) {
		generationHistory = new ArrayList<ChessNet>();
		generationFitness = new ArrayList<double[]>();
		bestFitness = new ArrayList<double[]>();
		generationRelativeFitness = new HashMap<Integer, double[]>();
		if (generationSize < 1)
			throw new IllegalArgumentException("Generation size must be greater than 1");
		generation = new ChessNet[generationSize];
		for (int i = 0; i < generation.length; i++)
			generation[i] = new ChessNet();
	}

	/**
	 * Instantiates a new Trainer class filled with mutated copies of a given net.
	 *
	 * @param generationSize
	 *            the generation size
	 * @param seedNet
	 *            the net to begin training from
	 * @param mp
	 *            the mutation parameters for filling the generation.
	 */
	public Trainer(int generationSize, ChessNet seedNet, MutationParameter mp) {
		generationHistory = new ArrayList<ChessNet>();
		generationFitness = new ArrayList<double[]>();
		bestFitness = new ArrayList<double[]>();
		generationRelativeFitness = new HashMap<Integer, double[]>();
		if (generationSize < 1)
			throw new IllegalArgumentException("Generation size must be greater than 1");
		generation = new ChessNet[generationSize];
		generation[0] = new ChessNet(seedNet);
		for (int i = 1; i < generation.length; i++)
			generation[i] = seedNet.getMutatedCopy(mp);
	}

	/**
	 * Get the current generation number.
	 *
	 * @return the current generation number
	 */
	public int generationNum() {
		return generationHistory.size();
	}

	/**
	 * Gets the fitness for a given generation.
	 *
	 * @param generationNum
	 *            the generation number
	 * @return the generation fitness
	 */
	public double[] getGenerationRelativeFitness(int generationNum) {
		if (generationNum > generationHistory.size() - 1 || generationNum < 0)
			return new double[] {1 - 1, -1, -1};

		if (generationRelativeFitness.containsKey(generationNum))
			return generationRelativeFitness.get(generationNum);

		generationHistory.get(generationNum).clearStats();

		for (int i = 0; i < generationNum; i++) {
			new TrainingGame(generationHistory.get(generationNum), generationHistory.get(i)).completeGame();
			new TrainingGame(generationHistory.get(i), generationHistory.get(generationNum)).completeGame();
		}

		generationRelativeFitness.put(generationNum, generationHistory.get(generationNum).getStats());
		return generationRelativeFitness.get(generationNum);
	}

	/**
	 * Gets the generation best.
	 *
	 * @param generationNum
	 *            the generation num
	 * @return the generation best
	 */
	public ChessNet getGenerationBest(int generationNum) {
		if (generationNum > generationHistory.size() - 1)
			return null;
		return generationHistory.get(generationNum);
	}

	/**
	 * Gets the generation fitness.
	 *
	 * @param generationNum
	 *            the generation num
	 * @return the generation fitness
	 */
	public double[] getGenerationFitness(int generationNum) {
		if (generationNum > generationHistory.size() - 1)
			return null;

		return generationFitness.get(generationNum);
	}

	/**
	 * Get the best fitness from a given generation
	 * 
	 * @param generationNum
	 *            to check
	 * @return the best fitness
	 */
	public double[] bestFitness(int generationNum) {
		if (generationNum > bestFitness.size() - 1)
			return null;

		return bestFitness.get(generationNum);
	}

	/**
	 * Run the current generation to completion.
	 *
	 * @param threadNum
	 *            the number of threads to use.
	 */
	public void runGeneration(int gameNum, int searchTime, boolean useBook, boolean randomMode, int threadNum) {
		// Play AI
		// TrainingGame[] generationGames = new TrainingGame[gameNum *
		// generation.length];
		// for (int i = 0; i < generation.length; i++) {
		// for (int j = 0; j < gameNum; j++) {
		// ComputerPlayer cuckooAi = new ComputerPlayer();
		// cuckooAi.useBook(useBook);
		// cuckooAi.timeLimit(searchTime, searchTime, randomMode);
		// if (j % 2 == 0)
		// generationGames[i * gameNum + j] = new TrainingGame(cuckooAi, generation[i]);
		// else
		// generationGames[i * gameNum + j] = new TrainingGame(generation[i], cuckooAi);
		// }
		// }

		// Play Self

		ArrayList<TrainingGame> gameList = new ArrayList<TrainingGame>();
		for (int i = 0; i < generation.length; i++) {
			for (int j = i + 1; j < generation.length; j++) {
				gameList.add(new TrainingGame(generation[i], generation[j]));
				gameList.add(new TrainingGame(generation[j], generation[i]));
			}
		}

		TrainingGame[] generationGames = new TrainingGame[gameList.size()];
		generationGames = gameList.toArray(generationGames);

		// Run all games to completion
		for (int i = 0; i < threadNum; i++) {
			runGamesThreadded(generationGames, i, threadNum);
		}
		int gamesLeft;
		int printRate = 50;
		int count = 0;
		do {
			try {
				Thread.sleep(100);
			} catch (InterruptedException e) {}
			gamesLeft = 0;
			for (int i = 0; i < generationGames.length; i++) {
				if (!generationGames[i].gameFinished())
					gamesLeft++;
			}
			count++;
			if (count == printRate) {
				System.out.print(String.format("%.2f", (1 - ((double) gamesLeft) / generationGames.length) * 100) + "% ");
				count = 0;
			}
		} while (gamesLeft > 0);
		System.out.println();

		// Find most fit
		ChessNet[] top = new ChessNet[(int) Math.ceil(elitePercent * generation.length)];
		int topIndex = 0;
		for (int i = 0; i < top.length; i++) {
			for (int j = 0; j < generation.length; j++) {
				if (generation[j] != null) {
					if (generation[topIndex] == null || generation[j].getStats()[0] > generation[topIndex].getStats()[0])
						topIndex = j;
				}
			}
			top[i] = generation[topIndex];
			generation[topIndex] = null;
		}

		generationHistory.add(top[0]);
		bestFitness.add(top[0].getStats());
		double[] generationFitness = new double[top[0].getStats().length];
		for (int i = 0; i < top.length; i++) {
			for (int j = 0; j < generationFitness.length; j++)
				generationFitness[j] += top[i].getStats()[j];
			double[] generationStats = top[i].getStats();
			System.out.println("(" + i + ")" + String.format("%.20f", generationStats[0]) + "  -  [" + (generationStats[1] + generationStats[2]) + ":(" + generationStats[1] + "," + generationStats[2]
					+ ")," + (generationStats[3] + generationStats[4]) + ":(" + generationStats[3] + "," + generationStats[4] + ")," + generationStats[5] + "]");
		}
		for (int i = 0; i < generationFitness.length; i++)
			generationFitness[i] /= top.length;
		this.generationFitness.add(generationFitness);

		for (int i = 0; i < top.length; i++)
			top[i].clearStats();

		best = top;
	}

	/**
	 * Mutate to a new generation.
	 *
	 * @param mp
	 *            the mutation parameter
	 */
	public void mutateGeneration(MutationParameter mp) {
		for (int i = 0; i < best.length; i++) {
			generation[i] = best[i];
		}

		for (int i = best.length; i < Math.ceil(generation.length * (1 - randomPercent)); i++) {
			int index = (int) (Math.random() * best.length);
			generation[i] = best[index].getMutatedCopy(mp);
		}

		for (int i = 0; i < generation.length; i++) {
			if (generation[i] == null)
				generation[i] = new ChessNet();
		}
	}

	// Run the games on a fixed interval
	private void runGamesThreadded(TrainingGame[] games, int start, int increment) {
		new Thread() {
			public void run() {
				for (int i = start; i < games.length; i += increment)
					games[i].completeGameThreadSafe();
			}
		}.start();
	}
}
