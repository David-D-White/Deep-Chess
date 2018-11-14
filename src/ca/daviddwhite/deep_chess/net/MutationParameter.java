/*
 * File name: MutationParameter.java
 * Last updated: 11-Nov-2018
 * @author David White Copyright (c) (2018). All rights reserved.
 */
package ca.daviddwhite.deep_chess.net;

// TODO: Auto-generated Javadoc
/**
 * The Class MutationParameter.
 */
public class MutationParameter {

	/**
	 * The probability that weights will be mutated. This should add to 1 with
	 * neuronMutateChance and layerMutateChance. Default 1.
	 */
	public double weightMutateChance = 1;

	/**
	 * The probability that neurons will be mutated. This should add to 1 with
	 * weightMutateChance and layerMutateChance Default 0.1.
	 */
	public double neuronMutateChance = 0.1;

	/**
	 * The chance to mutate a layer. Default 0.01. This should add to 1 with
	 * weightMutateChance and neuronMutateChance
	 */
	public double layerMutateChance = 0.01;

	/** The amount to modify a weight by. Default 0.01. */
	public double weightStep = 0.01;

	/** The probability that an individual weight will be modified. Default 0.1. */
	public double weightModifyChance = 0.1;

	/**
	 * The probability that an individual neuron will be added or removed. Default
	 * 0.1.
	 */
	public double neuronModifyChance = 0.1;

	/**
	 * The probability that a neuron will be removed rather than added. Default 0.5.
	 */
	public double neuronRemoveChance = 0.5;

	/** The maximum layer size that can be inserted. Default 10 */
	public int maxLayerInsert = 10;

	/** The maximum layer size that can be removed. Default 10. */
	public int maxLayerRemove = 10;

	/** The chance that a layer will be removed rather than added. Default 0.5. */
	public double layerRemoveChance = 0.5;
}
