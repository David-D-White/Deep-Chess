/*
 * File name: Sigmoid.java
 * Last updated: 5-Nov-2018
 * @author David White Copyright (c) (2018). All rights reserved.
 */
package ca.daviddwhite.deep_chess.net;

/**
 * A static class containing multiple sigmoid functions, useful for neural
 * networks.
 */
public class Sigmoid {

	/**
	 * Fast sigmoid function using absolute value.
	 *
	 * @param x
	 *            the x
	 * @param compression
	 *            the compression
	 * @return the double
	 */
	public static double fastSigmoid(double x, double compression) {
		x *= compression;
		return x / (1 + Math.abs(x));
	}

	/**
	 * Sigmoid function using inverse square root.
	 *
	 * @param x
	 *            the input value
	 * @param compression
	 *            the compression factor
	 * @return the Sigmoid value, Range:(-1,1)
	 */
	public static double rootSigmoid(double x, double compression) {
		x *= compression;
		return x / Math.sqrt(1 + x * x);
	}

	/**
	 * Hyperbolic tangent sigmoid function.
	 *
	 * @param x
	 *            the input value
	 * @param compression
	 *            the compression factor
	 * @return the Sigmoid value, Range:(-1,1)
	 */
	public static double tanhSigmoid(double x, double compression) {
		x *= compression;
		return Math.tanh(x);
	}

	/**
	 * Logistic sigmoid function.
	 *
	 * @param x
	 *            the input value
	 * @param compression
	 *            the compression factor
	 * @return the Sigmoid value, Range:(0,1)
	 */
	public static double logisticSigmoid(double x, double compression) {
		x *= compression;
		return 1 / (1 + Math.exp(-x));
	}

	/**
	 * Gudermannian sigmoid function.
	 *
	 * @param x
	 *            the input value
	 * @param compression
	 *            the compression factor
	 * @return the Sigmoid value, Range:(-1,1)
	 */
	public static double gudermannianSigmoid(double x, double compression) {
		x *= compression;
		return Math.tanh(Math.atan(x / 2));
	}
}
