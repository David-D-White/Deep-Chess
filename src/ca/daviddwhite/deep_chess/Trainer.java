/*
 * File name: Trainer.java
 * Last updated: 11-Nov-2018
 * 
 * @author David White Copyright (c) (2018). All rights reserved.
 */
package ca.daviddwhite.deep_chess;

import java.util.ArrayList;

public class Trainer {
    // The current generation
    private ChessNet[] generation;

    // The best net from each previous generation.
    private ArrayList<ChessNet> generationHistory;

    /**
     * Instantiates a new Trainer class to train chessNets.
     *
     * @param generationSize
     *            the generation size
     */
    public Trainer(int generationSize) {
	if (generationSize < 1)
	    throw new IllegalArgumentException("Generation size must be greater than 1");
	generation = new ChessNet[generationSize];
	for (int i = 0; i < generation.length; i++)
	    generation[i] = new ChessNet();
    }

    /**
     * Run the current generation to completion.
     *
     * @param threadNum the number of threads to use.
     */
    public void runGeneration(int threadNum) {
	TrainingGame[] generationGames = new TrainingGame[(generation.length) * (generation.length - 1)];
	int gameNum = 0;
	for (int i = 0; i < generation.length; i++) {
	    for (int j = i + 1; j < generation.length; j++) {
		generationGames[gameNum] = new TrainingGame(generation[i], generation[j]);
		generationGames[gameNum + 1] = new TrainingGame(generation[j], generation[i]);
		gameNum += 2;
	    }
	}

	if (threadNum > 1) {
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
		System.out.println(String.format("%.2f", (1 - (double) gamesLeft / generationGames.length) * 100) + "%");
	    }
	    while (gamesLeft > 0);
	}
	else {
	    double percent;
	    for (int i = 0; i < generationGames.length; i++) {
		generationGames[i].completeGame();
		if (System.currentTimeMillis() % 500 < 4)
		    System.out.println(String.format("%.2f", (double) i / generationGames.length * 100) + "%");
	    }
	}

	for (int i = 0; i < generation.length; i++)
	    System.out.println(generation[i].getStats()[0]);
    }

    public void mutateGeneration() {

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
