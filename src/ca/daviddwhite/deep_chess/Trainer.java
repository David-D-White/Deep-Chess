package ca.daviddwhite.deep_chess;

import java.util.ArrayList;

public class Trainer {
	ChessNet[] generation;
	ArrayList<ChessNet> generationHistory;

	public Trainer(int generationSize) {
		generation = new ChessNet[generationSize];
		for (int i = 0; i < generation.length; i++)
			generation[i] = new ChessNet();
	}

	public void runGeneration() {
		TrainingGame[] generationGames = new TrainingGame[(generation.length + 2) / 2 * generation.length];
		int gameNum = 0;
		for (int i = 0; i < generation.length; i++) {
			for (int j = i + 1; j < generation.length; j++) {
				generationGames[gameNum] = new TrainingGame(generation[i], generation[j]);
				generationGames[gameNum + 1] = new TrainingGame(generation[j], generation[i]);
			}
		}

		int threadNum = Runtime.getRuntime().availableProcessors();

		for (int i = 0; i < threadNum; i++) {
			runGamesThreadded(generationGames, i, threadNum);
		}
	}

	private void runGamesThreadded(TrainingGame[] games, int start, int increment) {
		new Thread() {
			public void run() {
				for (int i = start; i < games.length; i += increment)
					games[i].completeGame();
			}
		};
	}
}
