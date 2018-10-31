package ca.daviddwhite.deep_chess;

import ca.daviddwhite.deep_chess.net.NeuralNet;

public class ChessNet {
	public static final int INPUTS = 16;
	public static final int [] HIDDEN_LAYERS = {32,};
	public static final int OUTPUTS = 1;
	
	NeuralNet net;
	double fitness;
	int wins, loss, draws;
	
	public ChessNet ()
	{
		
	}
	
}
