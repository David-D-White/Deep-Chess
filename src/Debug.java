import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.PrintStream;

import ca.daviddwhite.deep_chess.ChessNet;
import ca.daviddwhite.deep_chess.Trainer;
import ca.daviddwhite.deep_chess.net.MutationParameter;
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
	// ChessNet seeder = null;
	// try {
	// FileInputStream netFile = new FileInputStream("nets/completed depth/depth_1 semi.txt");
	// seeder = new ChessNet(netFile);
	// netFile.close();
	// }
	// catch (IOException e) {
	// // TODO Auto-generated catch block
	// e.printStackTrace();
	// System.exit(0);
	// }

	mp = new MutationParameter();
	mp.weightMutateChance = 0.9;
	mp.neuronMutateChance = 0.07;
	mp.layerMutateChance = 0.03;
	mp.weightStep = 0.0001;
	mp.weightModifyChance = 0.4;
	mp.neuronModifyChance = 0.8;
	mp.neuronRemoveChance = 0.5;
	mp.maxLayerInsert = 25;
	mp.maxLayerRemove = 25;
	mp.layerRemoveChance = 0.5;

	// t = new Trainer(400, seeder, mp);
	t = new Trainer(90);

	System.out.println("Generation: " + t.generationNum());
	long time = System.currentTimeMillis();
	t.runGeneration(2, 60000, true, false, 6);// Runtime.getRuntime().availableProcessors());
	t.mutateGeneration(mp);
	System.out.println(((System.currentTimeMillis() - time) / 1000.0) + " seconds to process generation");
	System.out.println("---------------------------------------------");
    }

    public void draw() {
	background(150, 200, 255);

	textSize(20);
	textAlign(PApplet.LEFT, PApplet.BOTTOM);
	text("Generation: " + t.generationNum(), 40, 40);

	System.out.println("Generation: " + t.generationNum());
	long time = System.currentTimeMillis();
	t.runGeneration(2, 60000, true, false, 6);// Runtime.getRuntime().availableProcessors());
	t.mutateGeneration(mp);
	System.out.println(((System.currentTimeMillis() - time) / 1000.0) + " seconds to process generation");
	double[] generationStats = t.bestFitness(t.generationNum() - 1);
	System.out.println("Generation Best: " + String.format("%.20f", generationStats[0]) + "  -  (" + generationStats[1] + "," + generationStats[2] + ","
		+ generationStats[3] + "," + generationStats[4] + ")");
	generationStats = t.getGenerationRelativeFitness(t.generationNum() - 1);
	System.out.println("Generation Relative Fitness: " + String.format("%.20f", generationStats[0]) + "  -  (" + generationStats[1] + ","
		+ generationStats[2] + "," + generationStats[3] + "," + generationStats[4] + ")");
	// generationStats = t.getGenerationRelativeFitness(t.generationNum() - 1);
	// System.out.println("Generation Relative Fitness: " + generationStats[0] + "," + generationStats[1] + "," + generationStats[2] + "," +
	// generationStats[3]
	// + "," + generationStats[4]);
	System.out.println("---------------------------------------------");

	t.getGenerationBest(t.generationNum() - 1).drawNet(this, 100, height / 2, height - 200, width - 200, 20);

	if (t.generationNum() % 10 == 0) {
	    try {
		PrintStream ps = new PrintStream(".//nets/net_" + t.generationNum() + ".txt");
		t.getGenerationBest(t.generationNum() - 1).getNet().printNet(ps);
		ps.close();
	    }
	    catch (FileNotFoundException e) {
		// TODO Auto-generated catch block
		e.printStackTrace();
	    }
	}
    }

    public static void main(String[] args) {
	PApplet.main("Debug");
    }
}
