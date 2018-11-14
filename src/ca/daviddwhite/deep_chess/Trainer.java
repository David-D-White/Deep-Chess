/*
 * File name: Trainer.java
 * Last updated: 11-Nov-2018
 * @author David White Copyright (c) (2018). All rights reserved.
 */
package ca.daviddwhite.deep_chess;

import java.util.ArrayList;
import java.util.HashMap;

import ca.daviddwhite.deep_chess.net.MutationParameter;

public class Trainer {
	public static double elitePercent = 0.05;

	// The current generation
	private ChessNet[] generation;
	private ChessNet[] best;

	// The best net from each previous generation.
	private ArrayList<ChessNet> generationHistory;
	private ArrayList<double[]> generationFitness;
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
		generationRelativeFitness = new HashMap<Integer, double[]>();
		if (generationSize < 1)
			throw new IllegalArgumentException("Generation size must be greater than 1");
		generation = new ChessNet[generationSize];
		for (int i = 0; i < generation.length; i++)
			generation[i] = new ChessNet();
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

	public ChessNet getGenerationBest(int generationNum) {
		if (generationNum > generationHistory.size() - 1)
			return null;
		return generationHistory.get(generationNum);
	}

	public double[] getGenerationFitness(int generationNum) {
		if (generationNum > generationHistory.size() - 1)
			return null;

		return generationFitness.get(generationNum);
	}

	/**
	 * Run the current generation to completion.
	 *
	 * @param threadNum
	 *            the number of threads to use.
	 */
	public void runGeneration(int threadNum) {
		TrainingGame[] generationGames;
		ArrayList<TrainingGame> tempGames = new ArrayList<TrainingGame>();

		for (int i = 0; i < generation.length; i++) {
			for (int j = i + 1; j < generation.length; j++) {
				tempGames.add(new TrainingGame(generation[i], generation[j]));
				tempGames.add(new TrainingGame(generation[j], generation[i]));
			}
		}

		generationGames = new TrainingGame[tempGames.size()];
		generationGames = tempGames.toArray(generationGames);

		for (int i = 0; i < threadNum; i++) {
			runGamesThreadded(generationGames, i, threadNum);
		}
		int gamesLeft;
		do {
			try {
				Thread.sleep(500);
			} catch (InterruptedException e) {}
			gamesLeft = 0;
			for (int i = 0; i < generationGames.length; i++) {
				if (!generationGames[i].gameFinished())
					gamesLeft++;
			}
		} while (gamesLeft > 0);

		ChessNet[] top = new ChessNet[(int) (elitePercent * generation.length)];
		for (int i = 0; i < top.length; i++) {
			for (int j = 0; j < generation.length; j++) {
				if (generation[i] != null) {
					if (top[i] == null || generation[j].getStats()[0] > top[i].getStats()[0])
						top[i] = generation[j];
				}
			}
		}

		if (!generationHistory.contains(top[0])) {
			generationHistory.add(top[0]);
			generationFitness.add(top[0].getStats());
		}

		for (int i = 0; i < top.length; i++)
			top[i].clearStats();

		best = top;
	}

	/**
	 * Mutate generation.
	 */
	public void mutateGeneration(MutationParameter mp) {
		for (int i = 0; i < best.length; i++) {
			generation[i] = best[i];
		}

		for (int i = best.length; i < generation.length; i++) {
			int index = (int) (Math.random() * best.length);
			generation[i] = best[index].getMutatedCopy(mp);
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
