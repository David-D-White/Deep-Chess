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
	 * Run the current generation to completion using multithreading.
	 */
	public void runGeneration() {
		System.out.println("Copying Networks");
		TrainingGame[] generationGames = new TrainingGame[(generation.length) * (generation.length - 1)];
		int gameNum = 0;
		for (int i = 0; i < generation.length; i++) {
			for (int j = i + 1; j < generation.length; j++) {
				generationGames[gameNum] = new TrainingGame(generation[i], generation[j]);
				generationGames[gameNum + 1] = new TrainingGame(generation[j], generation[i]);
				gameNum += 2;
				if (gameNum % 1000 == 0)
					System.out.println(gameNum);
			}
		}

		System.out.println("Games left: " + generationGames.length);
		int threadNum = 1;// Runtime.getRuntime().availableProcessors();
		System.out.println(threadNum);
		for (int i = 0; i < threadNum; i++) {
			runGamesThreadded(generationGames, i, threadNum);
		}

		int gamesLeft;
		do {
			try {
				Thread.sleep(1000);
			} catch (InterruptedException e) {}
			gamesLeft = 0;
			for (int i = 0; i < generationGames.length; i++) {
				try {
					if (!generationGames[i].gameFinished())
						gamesLeft++;
				} catch (NullPointerException e) {
					e.printStackTrace();
					System.out.println(i);
					System.out.println(gamesLeft);
					return;
				}
			}
			System.out.println("Games left: " + gamesLeft);
		} while (gamesLeft > 0);

	}

	// Run the games on a fixed interval
	private void runGamesThreadded(TrainingGame[] games, int start, int increment) {
		new Thread() {
			public void run() {
				for (int i = start; i < games.length; i += increment) {
					// games[i].completeGameThreadSafe();
					games[i].completeGame();
				}
			}
		}.start();
	}
}
