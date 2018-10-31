/*
 * 
 */
package ca.daviddwhite.deep_chess.net;

import java.util.Arrays;

import java.awt.Color;

import ca.daviddwhite.deep_chess.net.Neuron.Synapse;
import processing.core.PApplet;

public class NeuralNet {
	// Input Neurons
	private Neuron[] inputs;
	// Hidden Layer Neurons
	private Neuron[][] hiddenLayers;
	// Output Nerurons
	private Neuron[] outputs;

	// Construct an empty net
	private NeuralNet() {
	}

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
		// Copy the neural net size
		this.inputs = new Neuron[n.inputs.length];
		this.hiddenLayers = new Neuron[n.hiddenLayers.length][];
		for (int i = 0; i < this.hiddenLayers.length; i++)
			this.hiddenLayers[i] = new Neuron[n.hiddenLayers[i].length];
		this.outputs = new Neuron[n.outputs.length];

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
	 * Mutate the weights of this neural net up or down based on stepVal.
	 *
	 * @param mutateChance
	 *            the chance to modify the weight
	 * @param stepVal
	 *            the step value (i.e how much to modify by)
	 */
	public void mutateWeights(double mutateChance, double stepVal) {
		for (Neuron[] ns : hiddenLayers) {
			for (Neuron n : ns) {
				Synapse[] connections = n.getBackConnections();
				for (Synapse s : connections) {
					if (Math.random() < mutateChance) {
						int sign = (int) Math.signum(Math.random() - 0.5);
						s.weight += stepVal * sign;
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
	 * Add or remove neurons from the hidden layers.
	 *
	 * @param mutateChance
	 *            the chance to add or remove a neuron
	 */
	public void mutateNeurons(double mutateChance) {
		for (int i = 0; i < hiddenLayers.length; i++) {
			if (Math.random() < mutateChance) {
				if (hiddenLayers[i].length > 1 && Math.random() < 0.5) // Remove a neuron
				{
					int index = (int) Math.random() * hiddenLayers[i].length;
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

	public void draw(PApplet canvas, float x, float y, float height, float width, float neuronRad) {
		int textColor = 0, neuronColor = 255, synapseColor = 150, weightColor = Color.RED.getRGB();
		int neuronLineColor = 0;
		int weightDist = 4;
		String format = "%.4f";

		canvas.ellipseMode(PApplet.CENTER);
		canvas.textAlign(PApplet.CENTER, PApplet.CENTER);
		canvas.textSize(neuronRad / 5);

		int maxNeurons = Math.max(inputs.length, outputs.length);
		for (Neuron[] ns : hiddenLayers)
			maxNeurons = Math.max(ns.length, maxNeurons);
		float horizontalStep = width / (hiddenLayers.length + 1);
		float verticalStep = height / (maxNeurons - 1);

		// Draw Synapses
		canvas.strokeWeight(2);
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
				canvas.text(String.format(format, connections[j].weight), wx, wy);
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
					canvas.text(String.format(format, connections[k].weight), wx, wy);
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
				canvas.text(String.format(format, connections[j].weight), wx, wy);
			}
		}

		// Draw Neurons
		canvas.fill(neuronColor);
		canvas.stroke(neuronLineColor);
		for (int i = 0; i < inputs.length; i++) {
			float center = (inputs.length - 1) / 2f * verticalStep;
			canvas.ellipse(x, y + i * verticalStep - center, neuronRad, neuronRad);
			canvas.fill(textColor);
			canvas.text(String.format(format, inputs[i].value), x, y + i * verticalStep - center);
			canvas.fill(neuronColor);
		}
		for (int i = 0; i < hiddenLayers.length; i++) {
			for (int j = 0; j < hiddenLayers[i].length; j++) {
				float center = (hiddenLayers[i].length - 1) / 2f * verticalStep;
				canvas.ellipse(x + (i + 1) * horizontalStep, y + j * verticalStep - center, neuronRad, neuronRad);
				canvas.fill(textColor);
				canvas.text(String.format(format, hiddenLayers[i][j].value), x + (i + 1) * horizontalStep, y + j * verticalStep - center);
				canvas.fill(neuronColor);
			}
		}
		for (int i = 0; i < outputs.length; i++) {
			float center = (outputs.length - 1) / 2f * verticalStep;
			canvas.ellipse(x + (hiddenLayers.length + 1) * horizontalStep, y + i * verticalStep - center, neuronRad, neuronRad);
			canvas.fill(textColor);
			canvas.text(String.format(format, outputs[i].value), x + (hiddenLayers.length + 1) * horizontalStep, y + i * verticalStep - center);
			canvas.fill(neuronColor);
		}
	}
}
