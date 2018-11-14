/*
 * File name: Trainer.java
 * Last updated: 11-Nov-2018
 * 
 * @author David White Copyright (c) (2018). All rights reserved.
 */
package ca.daviddwhite.deep_chess;

import java.util.ArrayList;
import java.util.HashMap;

import ca.daviddwhite.deep_chess.net.MutationParameter;

public class Trainer {
    // The current generation
    private ChessNet[] generation;
    private ChessNet best;

    // The best net from each previous generation.
    private ArrayList<ChessNet> generationHistory;
    // The fitness of each generation
    private HashMap<Integer, double[]> generationFitness;

    /**
     * Instantiates a new Trainer class to train chessNets.
     *
     * @param generationSize
     *            the generation size
     */
    public Trainer(int generationSize) {
	generationHistory = new ArrayList<ChessNet>();
	generationFitness = new HashMap<Integer, double[]>();
	if (generationSize < 1)
	    throw new IllegalArgumentException("Generation size must be greater than 1");
	generation = new ChessNet[generationSize];
	for (int i = 0; i < generation.length; i++)
	    generation[i] = new ChessNet();
	best = new ChessNet();
	generationHistory.add(best);
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
    public double[] getGenerationFitness(int generationNum) {
	if (generationNum > generationHistory.size() - 1)
	    return null;

	if (generationFitness.containsKey(generationNum))
	    return generationFitness.get(generationNum);

	generationHistory.get(generationNum).clearStats();

	for (int i = 0; i < generationNum; i++) {
	    new TrainingGame(generationHistory.get(generationNum), generationHistory.get(i)).completeGame();
	    new TrainingGame(generationHistory.get(i), generationHistory.get(generationNum)).completeGame();
	}

	generationFitness.put(generationNum, generationHistory.get(generationNum).getStats());
	return generationFitness.get(generationNum);
    }

    public ChessNet getGenerationBest(int generationNum) {
	if (generationNum > generationHistory.size() - 1)
	    return null;
	return generationHistory.get(generationNum);
    }

    /**
     * Run the current generation to completion.
     *
     * @param threadNum
     *            the number of threads to use.
     */
    public void runGeneration(int threadNum) {
	TrainingGame[] generationGames = new TrainingGame[generation.length * 2];
	for (int i = 0; i < generationGames.length; i += 2) {
	    generationGames[i] = new TrainingGame(best, generation[i / 2]);
	    generationGames[i + 1] = new TrainingGame(generation[i / 2], best);
	}

	for (int i = 0; i < threadNum; i++) {
	    runGamesThreadded(generationGames, i, threadNum);
	}
	int gamesLeft;
	do {
	    try {
		Thread.sleep(500);
	    }
	    catch (InterruptedException e) {}
	    gamesLeft = 0;
	    for (int i = 0; i < generationGames.length; i++) {
		if (!generationGames[i].gameFinished())
		    gamesLeft++;
	    }
	}
	while (gamesLeft > 0);

	ArrayList<ChessNet> winners = new ArrayList<ChessNet>();

	for (int i = 0; i < generation.length; i++) {
	    if (generation[i].getStats()[0] > TrainingGame.TIE_FITNESS && generation[i].getStats()[1] > 0) {
		generation[i].clearStats();
		winners.add(generation[i]);
	    }
	}

	if (winners.size() > 0) {
	    winners.add(best);
	    best.clearStats();
	    ArrayList<TrainingGame> round2 = new ArrayList<TrainingGame>();
	    for (int i = 0; i < winners.size(); i++) {
		for (int j = 0; j < generationHistory.size(); j++) {
		    round2.add(new TrainingGame(winners.get(i), generationHistory.get(j)));
		    round2.add(new TrainingGame(generationHistory.get(j), winners.get(i)));
		}
	    }
	    generationGames = new TrainingGame[round2.size()];
	    generationGames = round2.toArray(generationGames);

	    for (int i = 0; i < threadNum; i++) {
		runGamesThreadded(generationGames, i, threadNum);
	    }
	    gamesLeft = 0;
	    do {
		try {
		    Thread.sleep(500);
		}
		catch (InterruptedException e) {}
		gamesLeft = 0;
		for (int i = 0; i < generationGames.length; i++) {
		    if (!generationGames[i].gameFinished())
			gamesLeft++;
		}
	    }
	    while (gamesLeft > 0);

	    best = winners.get(0);

	    for (int i = 0; i < winners.size(); i++) {
		if (winners.get(i).getStats()[0] > best.getStats()[0])
		    best = winners.get(i);
	    }

	    if (!generationHistory.contains(best))
		generationHistory.add(best);
	}
    }

    /**
     * Mutate generation.
     */
    public void mutateGeneration(MutationParameter mp) {
	for (int i = 0; i < generation.length; i++) {
	    generation[i] = best.getMutatedCopy(mp);
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
