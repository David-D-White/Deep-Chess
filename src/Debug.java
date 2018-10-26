import processing.core.PApplet;

public class Debug extends PApplet {
	final int BOARD_WIDTH = 8, BOARD_HEIGHT = 8;
	final int PADDING = 100, GRID_SIZE = 100;
	
	
	public void settings ()
	{
		setSize(BOARD_WIDTH*GRID_SIZE + PADDING,BOARD_HEIGHT*GRID_SIZE + PADDING);
	}
	
	public void setup ()
	{
		rectMode(CORNER);
		noFill();
		stroke(0);
	}
	
	public void draw()
	{
		background(255);
		for (int i = 0; i < BOARD_WIDTH; i++)
		{
			for (int j = 0; j < BOARD_HEIGHT; j++)
			{
				if (i%2 != j%2)
					fill (0);
				else
					fill(255);
				rect(PADDING/2 + i*GRID_SIZE, PADDING/2 + j*GRID_SIZE, GRID_SIZE, GRID_SIZE);
			}
		}
		
		
	}
	
	public static void main(String [] args)
	{
		PApplet.main("Debug");
	}
}
