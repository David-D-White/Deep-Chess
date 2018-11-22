package ca.daviddwhite.deep_chess.net;

import java.util.ArrayList;

/**
 * The Neuron class stores a value, and connections to other neurons to help
 * build neural networks.
 *
 * @author David White
 */
public class Neuron {
	/**
	 * The Synapse class serves as a connection between two neurons.
	 */
	public final static class Synapse {

		/**
		 * The front neuron connected to this synapse (i.e the neuron activated last in
		 * forward propagation).
		 */
		public Neuron front;
		/**
		 * The back neuron connected to this synapse (i.e the neuron activated first in
		 * forward propagation).
		 */
		public Neuron back;
		/** The synapse weight. */
		public double weight;

		/**
		 * Instantiates a new synapse and connect the corresponding neurons.
		 *
		 * @param front
		 *            the front neuron
		 * @param back
		 *            the back neuron
		 * @param weight
		 *            the synapse weight
		 */
		public Synapse(Neuron front, Neuron back, double weight) {
			this.front = front;
			this.back = back;
			this.weight = weight;
			front.addBackConnection(this);
			back.addFrontConnection(this);
		}

		/**
		 * Returns the weighted value of this neuron.
		 *
		 * @param back
		 *            true if specifying the back neuron
		 * @return the weighted value of the specified neuron
		 */
		public double weightedValue(boolean back) {
			if (back)
				return this.back.value * weight;
			else
				return this.front.value * weight;
		}

		/**
		 * Destroy this synapse.
		 */
		public void destroy() {
			back.frontConnections.remove(this);
			front.backConnections.remove(this);
		}
	}

	/** The value of this neuron. */
	public double value;

	// Front and back synapse connections
	private ArrayList<Synapse> frontConnections, backConnections;

	/**
	 * Instantiates a new neuron.
	 */
	public Neuron() {
		frontConnections = new ArrayList<Synapse>();
		backConnections = new ArrayList<Synapse>();
	}

	// Connect this neuron to neurons further forward in the network
	private void addFrontConnection(Synapse connection) {
		frontConnections.add(connection);
	}

	// Connect this neuron to neurons further back in the network
	private void addBackConnection(Synapse connection) {
		backConnections.add(connection);
	}

	/**
	 * Destroy all synapses associated with this neuron.
	 */
	public void clearConnections() {
		clearFrontConnections();
		clearBackConnections();
	}

	/**
	 * Clear all synapses at the front of this neuron.
	 */
	public void clearFrontConnections() {
		for (int i = frontConnections.size(); i > 0; i--)
			frontConnections.get(0).destroy();
	}

	/**
	 * Clear all synapses at the back of this neuron.
	 */
	public void clearBackConnections() {
		for (int i = backConnections.size(); i > 0; i--)
			backConnections.get(0).destroy();
	}

	/**
	 * Return the connections to neurons further back.
	 *
	 * @return the array of Synapses connecting backwards.
	 */
	public Synapse[] getBackConnections() {
		Synapse[] s = new Synapse[backConnections.size()];
		backConnections.toArray(s);
		return s;
	}

	/**
	 * Return the connections to neurons further forward.
	 *
	 * @return the array of Synapses connecting forward
	 */
	public Synapse[] getFrontConnections() {
		Synapse[] s = new Synapse[frontConnections.size()];
		frontConnections.toArray(s);
		return s;
	}

	/**
	 * Calculate the value of this neuron for feed-forward propagation.
	 */
	public void feedForward() {
		this.value = 0;
		for (Synapse s : backConnections)
			this.value += s.weightedValue(true);

		this.value = Sigmoid.rootSigmoid(this.value, 1);
		// this.value = Math.tanh(this.value);
	}
}
