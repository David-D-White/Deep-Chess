import java.awt.Color;

import ca.daviddwhite.deep_chess.net.NeuralNet;
import ca.daviddwhite.deep_chess.net.Neuron;
import processing.core.PApplet;

public class Debug extends PApplet {
	final int BOARD_WIDTH = 8, BOARD_HEIGHT = 8;
	final int PADDING = 100, GRID_SIZE = 100;

	NeuralNet n;

	public void settings() {
		size(1280, 720);
	}

	public void setup() {
		n = new NeuralNet(2, new int[] {4, 4}, 1);
		Neuron[] inputs = n.getInputs();
		inputs[0].value = 0;
		inputs[1].value = 1;
		n.feedForward();
	}

	public void draw() {
		background(150, 200, 255);
		n.draw(this, 100, height / 2, height - 200, width - 200, 100);
		if (keyPressed) {
			System.out.println("keyPressed");
			if (key == 'w') {
				n.mutateWeights(0.5, 0.1);
				System.out.println("mutated weights");
			} else if (key == 'n') {
				n.mutateNeurons(0.5);
				System.out.println("mutated neurons");
			}
			n.feedForward();
			keyPressed = false;
		}
	}

	public static void main(String[] args) {
		PApplet.main("Debug");
	}
}
