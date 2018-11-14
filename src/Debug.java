import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.PrintStream;

import ca.daviddwhite.deep_chess.Trainer;
import ca.daviddwhite.deep_chess.net.MutationParameter;
import ca.daviddwhite.deep_chess.net.NeuralNet;
import ca.daviddwhite.deep_chess.net.Neuron;
import processing.core.PApplet;

public class Debug extends PApplet {
	final int BOARD_WIDTH = 8, BOARD_HEIGHT = 8;
	final int PADDING = 100, GRID_SIZE = 100;

	Trainer t;
	MutationParameter mp;

	public void settings() {
		size(1600, 900);
	}

	public void setup() {
		t = new Trainer(100);

		mp = new MutationParameter();
		mp.weightMutateChance = 0.6;
		mp.neuronMutateChance = 0.3;
		mp.layerMutateChance = 0.1;
		mp.weightStep = 0.01;
		mp.weightModifyChance = 0.3;
		mp.neuronModifyChance = 0.7;
		mp.neuronRemoveChance = 0.5;
		mp.maxLayerInsert = 10;
		mp.maxLayerRemove = 10;
		mp.layerRemoveChance = 0.5;

		t.runGeneration(Runtime.getRuntime().availableProcessors());
		t.mutateGeneration(mp);
	}

	public void draw() {
		background(150, 200, 255);

		textSize(20);
		textAlign(PApplet.LEFT, PApplet.BOTTOM);
		text("Generation: " + t.generationNum(), 40, 40);

		System.out.println("Generation: " + t.generationNum());
		long time = System.currentTimeMillis();
		t.runGeneration(Runtime.getRuntime().availableProcessors());
		t.mutateGeneration(mp);
		System.out.println(((System.currentTimeMillis() - time) / 1000.0) + " seconds to process generation");
		double[] generationStats = t.getGenerationRelativeFitness(t.generationNum() - 1);
		System.out.println("Generation Fitness: " + generationStats[0] + "," + generationStats[1] + "," + generationStats[2] + "," + generationStats[3]);
		generationStats = t.getGenerationFitness(t.generationNum() - 1);
		//System.out.println("Generation Fitness: " + generationStats[0] + "," + generationStats[1] + "," + generationStats[2] + "," + generationStats[3]);
		System.out.println("---------------------------------------------");

		t.getGenerationBest(t.generationNum() - 1).drawNet(this, 100, height / 2, height - 200, width - 200, 40);

		if (t.generationNum() % 10 == 0) {
			try {
				PrintStream ps = new PrintStream(".//nets/net_" + t.generationNum() + ".txt");
				t.getGenerationBest(t.generationNum() - 1).getNet().printNet(ps);
				ps.close();
			} catch (FileNotFoundException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}

	public static void main(String[] args) {
		PApplet.main("Debug");
	}
}
