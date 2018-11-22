package ca.daviddwhite.deep_chess.net;

import java.awt.Color;
import java.io.InputStream;
import java.io.PrintStream;
import java.util.Arrays;
import java.util.Scanner;

import ca.daviddwhite.deep_chess.net.Neuron.Synapse;
import processing.core.PApplet;

/**
 * A basic neural network class with methods for calculation and mutation
 */
public class NeuralNet {
	// Input Neurons
	private Neuron[] inputs;
	// Hidden Layer Neurons
	private Neuron[][] hiddenLayers;
	// Output Nerurons
	private Neuron[] outputs;

	/**
	 * Instantiates a new neural net and connects the neurons.
	 *
	 * @param inputNum
	 *            the number of input neurons
	 * @param hiddenLayerNum
	 *            an array representing the number and size of the hidden layers
	 * @param outputNum
	 *            the number of output neurons
	 */
	public NeuralNet(int inputNum, int[] hiddenLayerNum, int outputNum) {
		// Create the Neural Network with empty neurons
		this.inputs = new Neuron[inputNum];
		for (int i = 0; i < inputs.length; i++)
			inputs[i] = new Neuron();
		this.hiddenLayers = new Neuron[hiddenLayerNum.length][];
		for (int i = 0; i < this.hiddenLayers.length; i++) {
			this.hiddenLayers[i] = new Neuron[hiddenLayerNum[i]];
			for (int j = 0; j < hiddenLayers[i].length; j++)
				hiddenLayers[i][j] = new Neuron();
		}
		this.outputs = new Neuron[outputNum];
		for (int i = 0; i < outputs.length; i++)
			outputs[i] = new Neuron();

		// Connect the first layer to the input layer
		for (Neuron hidden : hiddenLayers[0]) {
			for (Neuron input : inputs)
				new Synapse(hidden, input, 2 * Math.random() - 1);
		}

		// Connect the hidden layers together
		for (int i = 1; i < hiddenLayers.length; i++) {
			for (Neuron hiddenFront : hiddenLayers[i]) {
				for (Neuron hiddenBack : hiddenLayers[i - 1])
					new Synapse(hiddenFront, hiddenBack, 2 * Math.random() - 1);
			}
		}

		// Connect the output layer to the last hidden layer
		for (Neuron output : outputs) {
			for (Neuron hidden : hiddenLayers[hiddenLayers.length - 1])
				new Synapse(output, hidden, 2 * Math.random() - 1);
		}
	}

	/**
	 * Copy Constructor.
	 *
	 * @param n
	 *            the NeuralNet to copy
	 */
	public NeuralNet(NeuralNet n) {
		// Copy the neural net structure
		this.inputs = new Neuron[n.inputs.length];
		for (int i = 0; i < inputs.length; i++)
			inputs[i] = new Neuron();
		this.hiddenLayers = new Neuron[n.hiddenLayers.length][];
		for (int i = 0; i < this.hiddenLayers.length; i++) {
			this.hiddenLayers[i] = new Neuron[n.hiddenLayers[i].length];
			for (int j = 0; j < hiddenLayers[i].length; j++)
				hiddenLayers[i][j] = new Neuron();
		}
		this.outputs = new Neuron[n.outputs.length];
		for (int i = 0; i < outputs.length; i++)
			outputs[i] = new Neuron();

		// Copy the weights of the first connection layer
		for (int i = 0; i < this.hiddenLayers[0].length; i++) {
			Synapse[] connections = n.hiddenLayers[0][i].getBackConnections();
			for (int j = 0; j < this.inputs.length; j++)
				new Synapse(this.hiddenLayers[0][i], this.inputs[j], connections[j].weight);
		}

		// Copy the hidden layer connection weights
		for (int i = 1; i < this.hiddenLayers.length; i++) {
			for (int j = 0; j < this.hiddenLayers[i].length; j++) {
				Synapse[] connections = n.hiddenLayers[i][j].getBackConnections();
				for (int k = 0; k < n.hiddenLayers[i - 1].length; k++)
					new Synapse(this.hiddenLayers[i][j], this.hiddenLayers[i - 1][k], connections[k].weight);
			}
		}

		// Copy the output layer connection weights
		for (int i = 0; i < this.outputs.length; i++) {
			Synapse[] connections = n.outputs[i].getBackConnections();
			for (int j = 0; j < this.hiddenLayers[hiddenLayers.length - 1].length; j++)
				new Neuron.Synapse(this.outputs[i], this.hiddenLayers[hiddenLayers.length - 1][j], connections[j].weight);
		}
	}

	/**
	 * Return the input neurons.
	 *
	 * @return the array of input neurons
	 */
	public Neuron[] getInputs() {
		return this.inputs;
	}

	/**
	 * Gets the output neurons.
	 *
	 * @return the array of output neurons
	 */
	public Neuron[] getOutputs() {
		return this.outputs;
	}

	/**
	 * Calculate the output values by forward propagation.
	 */
	public void feedForward() {
		for (int i = 0; i < hiddenLayers.length; i++) {
			for (Neuron n : hiddenLayers[i])
				n.feedForward();
		}

		for (Neuron n : outputs)
			n.feedForward();
	}

	/**
	 * Randomly increase or decrease the weights in this network by a given value.
	 *
	 * @param mutateChance
	 *            the chance that a given weight will be modified
	 * @param stepVal
	 *            the step value, i.e how much to add or subtract from a weights
	 */
	public void mutateWeights(double mutateChance, double stepVal) {
		for (Neuron[] ns : hiddenLayers) {
			for (Neuron n : ns) {
				Synapse[] connections = n.getBackConnections();
				for (Synapse s : connections) {
					if (Math.random() < mutateChance) {
						int sign = (int) Math.signum(Math.random() - 0.5);
						s.weight += stepVal * sign * Math.random();
					}
				}
			}
		}
		for (Neuron n : outputs) {
			Synapse[] connections = n.getBackConnections();
			for (Synapse s : connections) {
				if (Math.random() < mutateChance) {
					int sign = (int) Math.signum(Math.random() - 0.5);
					s.weight += stepVal * sign;
				}
			}
		}
	}

	/**
	 * Randomly adds or removes neurons from the hidden layers.
	 *
	 * @param mutateChance
	 *            the chance to select a neuron to be added or removed
	 * @param removeChance
	 *            the chance for a selected neuron to be removed rather than added
	 */
	public void mutateNeurons(double mutateChance, double removeChance) {
		for (int i = 0; i < hiddenLayers.length; i++) {
			if (Math.random() < mutateChance) {
				if (hiddenLayers[i].length > 1 && Math.random() < 0.5) // Remove a neuron
				{
					int index = (int) (Math.random() * hiddenLayers[i].length);
					hiddenLayers[i][index].clearConnections();

					Neuron[] newLayer = new Neuron[hiddenLayers[i].length - 1];
					for (int j = 0; j < index; j++)
						newLayer[j] = hiddenLayers[i][j];
					for (int j = index; j < newLayer.length; j++)
						newLayer[j] = hiddenLayers[i][j + 1];
					hiddenLayers[i] = newLayer;

				} else { // Add a neuron
					hiddenLayers[i] = Arrays.copyOf(hiddenLayers[i], hiddenLayers[i].length + 1);
					hiddenLayers[i][hiddenLayers[i].length - 1] = new Neuron();
					// Add Backwards Connections
					if (i > 0) {
						for (Neuron n : hiddenLayers[i - 1])
							new Synapse(hiddenLayers[i][hiddenLayers[i].length - 1], n, 2 * Math.random() - 1);
					} else {
						for (Neuron n : inputs)
							new Synapse(hiddenLayers[i][hiddenLayers[i].length - 1], n, 2 * Math.random() - 1);
					}
					// Add Forwards Connections
					if (i < hiddenLayers.length - 1) {
						for (Neuron n : hiddenLayers[i + 1])
							new Synapse(n, hiddenLayers[i][hiddenLayers[i].length - 1], 2 * Math.random() - 1);
					} else {
						for (Neuron n : outputs)
							new Synapse(n, hiddenLayers[i][hiddenLayers[i].length - 1], 2 * Math.random() - 1);
					}
				}
			}
		}
	}

	/**
	 * Randomly adds or removes a hidden layer from the network.
	 *
	 * @param maxInsertSize
	 *            the maximum neurons to put in a new layer
	 * @param maxRemoveSize
	 *            the maximum neurons tin a layer that will be removed
	 * @param removeChance
	 *            the chance to remove a layer
	 */
	public void mutateLayer(int maxInsertSize, int maxRemoveSize, double removeChance) {
		// Generate Dimension and Index
		int index = (int) (Math.random() * hiddenLayers.length);
		int size = (int) (Math.random() * maxInsertSize + 1);

		if (hiddenLayers.length > 1 && hiddenLayers[index].length <= maxRemoveSize && Math.random() < removeChance) {
			for (Neuron n : hiddenLayers[index])
				n.clearConnections();
			Neuron[][] newLayers = new Neuron[hiddenLayers.length - 1][];
			for (int i = 0; i < index; i++)
				newLayers[i] = hiddenLayers[i];
			for (int i = index + 1; i < hiddenLayers.length; i++)
				newLayers[i - 1] = hiddenLayers[i];

			if (index == 0) {
				for (Neuron n1 : inputs) {
					for (Neuron n2 : newLayers[0])
						new Synapse(n2, n1, Math.random() * 2 - 1);
				}
			} else if (index == hiddenLayers.length - 1) {
				for (Neuron n1 : newLayers[newLayers.length - 1]) {
					for (Neuron n2 : outputs)
						new Synapse(n2, n1, Math.random() * 2 - 1);
				}
			} else {
				for (Neuron n1 : newLayers[index - 1]) {
					for (Neuron n2 : newLayers[index])
						new Synapse(n2, n1, Math.random() * 2 - 1);
				}
			}
			hiddenLayers = newLayers;
		} else {
			index = (int) (Math.random() * (hiddenLayers.length + 1));

			// Create and fill new Hidden Layer Array
			Neuron[][] newLayers = new Neuron[hiddenLayers.length + 1][];
			for (int i = 0; i < index; i++)
				newLayers[i] = hiddenLayers[i];
			for (int i = index + 1; i < newLayers.length; i++)
				newLayers[i] = hiddenLayers[i - 1];
			// Disconnect old layers
			if (index < newLayers.length - 1) {
				for (Neuron n : newLayers[index + 1])
					n.clearBackConnections();
			} else {
				for (Neuron n : outputs)
					n.clearBackConnections();
			}

			// Connect old layers with inserted layer
			newLayers[index] = new Neuron[size];
			for (int i = 0; i < newLayers[index].length; i++) {
				newLayers[index][i] = new Neuron();
				if (index > 0) {
					for (Neuron n : newLayers[index - 1])
						new Synapse(newLayers[index][i], n, Math.random() * 2 - 1);
				} else {
					for (Neuron n : inputs)
						new Synapse(newLayers[index][i], n, Math.random() * 2 - 1);
				}
				if (index < newLayers.length - 1) {
					for (Neuron n : newLayers[index + 1])
						new Synapse(n, newLayers[index][i], Math.random() * 2 - 1);
				} else {
					for (Neuron n : outputs)
						new Synapse(n, newLayers[index][i], Math.random() * 2 - 1);
				}
			}
			// Replace hidden layers
			hiddenLayers = newLayers;
		}
	}

	/**
	 * Print this neural net to an output stream
	 *
	 * @param out
	 *            the output stream
	 */
	public void printNet(PrintStream out) {
		for (int i = 0; i < hiddenLayers.length; i++) { // Print hidden layer size and weights
			if (i > 0)
				out.print(';');
			for (int j = 0; j < hiddenLayers[i].length; j++) {
				if (j > 0)
					out.print(':');
				Synapse[] connections = hiddenLayers[i][j].getBackConnections();
				for (int k = 0; k < connections.length; k++) {
					if (k > 0)
						out.print(',');
					out.print(connections[k].weight);
				}
			}
		}

		out.print(';');

		for (int i = 0; i < outputs.length; i++) {
			if (i > 0)
				out.print(':');
			Synapse[] connections = outputs[i].getBackConnections();
			for (int j = 0; j < connections.length; j++) {
				if (j > 0)
					out.print(',');
				out.print(connections[j].weight);
			}
		}
	}

	/**
	 * Instantiates a new neural net based on input stream data.
	 *
	 * @param in
	 *            the input stream to read from
	 */
	public NeuralNet(InputStream in) {
		String[][] neurons;

		{ // Read weight list array from input stream
			Scanner s = new Scanner(in);
			String input = s.nextLine();
			s.close();

			String[] layers = input.split(";");
			neurons = new String[layers.length][];
			for (int i = 0; i < neurons.length; i++)
				neurons[i] = layers[i].split(":");
		}

		// Create the Neural Network with empty neurons
		inputs = new Neuron[neurons[0][0].split(",").length];
		for (

				int i = 0; i < inputs.length; i++)
			inputs[i] = new Neuron();
		hiddenLayers = new Neuron[neurons.length - 1][];
		for (int i = 0; i < hiddenLayers.length; i++) {
			hiddenLayers[i] = new Neuron[neurons[i].length];
			for (int j = 0; j < hiddenLayers[i].length; j++)
				hiddenLayers[i][j] = new Neuron();
		}
		outputs = new Neuron[neurons[neurons.length - 1].length];
		for (int i = 0; i < outputs.length; i++)
			outputs[i] = new Neuron();

		// Fill the weights of the first connection layer
		for (int i = 0; i < hiddenLayers[0].length; i++) {
			String[] connections = neurons[0][i].split(",");
			for (int j = 0; j < inputs.length; j++)
				new Synapse(hiddenLayers[0][i], inputs[j], Double.parseDouble(connections[j]));
		}

		// Fill the hidden layer connection weights
		for (int i = 1; i < hiddenLayers.length; i++) {
			for (int j = 0; j < hiddenLayers[i].length; j++) {
				String[] connections = neurons[i][j].split(",");
				for (int k = 0; k < hiddenLayers[i - 1].length; k++)
					new Synapse(hiddenLayers[i][j], hiddenLayers[i - 1][k], Double.parseDouble(connections[k]));
			}
		}

		// Fill the output layer connection weights
		for (int i = 0; i < outputs.length; i++) {
			String[] connections = neurons[neurons.length - 1][i].split(",");
			for (int j = 0; j < hiddenLayers[hiddenLayers.length - 1].length; j++)
				new Neuron.Synapse(outputs[i], hiddenLayers[hiddenLayers.length - 1][j], Double.parseDouble(connections[j]));
		}
	}

	// Debug Method
	public void draw(PApplet canvas, float x, float y, float height, float width, float neuronDiam) {
		int textColor = 0, neuronColor = 255, synapseColor = 150, weightColor = Color.RED.getRGB();
		int neuronLineColor = 0;
		int weightDist = 4;
		String format = "%.4f";

		canvas.ellipseMode(PApplet.CENTER);
		// canvas.textAlign(PApplet.CENTER, PApplet.CENTER);
		// canvas.textSize(neuronDiam / 5);

		int maxNeurons = Math.max(inputs.length, outputs.length);
		for (Neuron[] ns : hiddenLayers)
			maxNeurons = Math.max(ns.length, maxNeurons);
		float horizontalStep = width / (hiddenLayers.length + 1);
		float verticalStep = height / (maxNeurons - 1);

		// Draw Synapses
		canvas.strokeWeight(1);
		canvas.fill(weightColor);
		canvas.stroke(synapseColor);
		for (int i = 0; i < inputs.length; i++) {
			float center1 = (inputs.length - 1) / 2f * verticalStep;
			float center2 = (hiddenLayers[0].length - 1) / 2f * verticalStep;
			Synapse[] connections = inputs[i].getFrontConnections();
			for (int j = 0; j < connections.length; j++) {
				float x1 = x, x2 = x + horizontalStep;
				float y1 = y + i * verticalStep - center1, y2 = y + j * verticalStep - center2;
				float wx = ((weightDist - 1) * x1 + x2) / weightDist, wy = ((weightDist - 1) * y1 + y2) / weightDist;
				canvas.line(x1, y1, x2, y2);
				// canvas.text(String.format(format, connections[j].weight), wx, wy);
			}
		}
		for (int i = 0; i < hiddenLayers.length - 1; i++) {
			for (int j = 0; j < hiddenLayers[i].length; j++) {
				float center1 = (hiddenLayers[i].length - 1) / 2f * verticalStep;
				float center2 = (hiddenLayers[i + 1].length - 1) / 2f * verticalStep;
				Synapse[] connections = hiddenLayers[i][j].getFrontConnections();
				for (int k = 0; k < connections.length; k++) {
					float x1 = x + (i + 1) * horizontalStep, x2 = x + (i + 2) * horizontalStep;
					float y1 = y + j * verticalStep - center1, y2 = y + k * verticalStep - center2;
					float wx = ((weightDist - 1) * x1 + x2) / weightDist, wy = ((weightDist - 1) * y1 + y2) / weightDist;
					canvas.line(x1, y1, x2, y2);
					// canvas.text(String.format(format, connections[k].weight), wx, wy);
				}
			}
		}
		for (int i = 0; i < hiddenLayers[hiddenLayers.length - 1].length; i++) {
			float center1 = (hiddenLayers[hiddenLayers.length - 1].length - 1) / 2f * verticalStep;
			float center2 = (outputs.length - 1) / 2f * verticalStep;
			Synapse[] connections = hiddenLayers[hiddenLayers.length - 1][i].getFrontConnections();
			for (int j = 0; j < connections.length; j++) {
				float x1 = x + (hiddenLayers.length) * horizontalStep, x2 = x + (hiddenLayers.length + 1) * horizontalStep;
				float y1 = y + i * verticalStep - center1, y2 = y + j * verticalStep - center2;
				float wx = ((weightDist - 1) * x1 + x2) / weightDist, wy = ((weightDist - 1) * y1 + y2) / weightDist;
				canvas.line(x1, y1, x2, y2);
				// canvas.text(String.format(format, connections[j].weight), wx, wy);
			}
		}

		// Draw Neurons
		canvas.fill(neuronColor);
		canvas.stroke(neuronLineColor);
		for (int i = 0; i < inputs.length; i++) {
			float center = (inputs.length - 1) / 2f * verticalStep;
			int synapseCount = inputs[i].getBackConnections().length + inputs[i].getFrontConnections().length;
			canvas.ellipse(x, y + i * verticalStep - center, neuronDiam, neuronDiam);
			canvas.fill(textColor);
			// canvas.text(String.format(format, inputs[i].value), x, y + i * verticalStep -
			// center);
			// canvas.text("" + synapseCount, x, y + i * verticalStep - center + neuronDiam
			// / 2 + neuronDiam / 5);
			canvas.fill(neuronColor);
		}
		for (int i = 0; i < hiddenLayers.length; i++) {
			for (int j = 0; j < hiddenLayers[i].length; j++) {
				float center = (hiddenLayers[i].length - 1) / 2f * verticalStep;
				int synapseCount = hiddenLayers[i][j].getBackConnections().length + hiddenLayers[i][j].getFrontConnections().length;
				canvas.ellipse(x + (i + 1) * horizontalStep, y + j * verticalStep - center, neuronDiam, neuronDiam);
				canvas.fill(textColor);
				// canvas.text(String.format(format, hiddenLayers[i][j].value), x + (i + 1) *
				// horizontalStep, y + j * verticalStep - center);
				// canvas.text("" + synapseCount, x + (i + 1) * horizontalStep, y + j *
				// verticalStep - center + neuronDiam / 2 + neuronDiam / 5);
				canvas.fill(neuronColor);
			}
		}
		for (int i = 0; i < outputs.length; i++) {
			float center = (outputs.length - 1) / 2f * verticalStep;
			int synapseCount = outputs[i].getBackConnections().length + outputs[i].getFrontConnections().length;
			canvas.ellipse(x + (hiddenLayers.length + 1) * horizontalStep, y + i * verticalStep - center, neuronDiam, neuronDiam);
			canvas.fill(textColor);
			// canvas.text(String.format(format, outputs[i].value), x + (hiddenLayers.length
			// + 1) * horizontalStep, y + i * verticalStep - center);
			// canvas.text("" + synapseCount, x + (hiddenLayers.length + 1) *
			// horizontalStep, y + i * verticalStep - center + neuronDiam / 2 + neuronDiam /
			// 5);
			canvas.fill(neuronColor);
		}
	}
}
