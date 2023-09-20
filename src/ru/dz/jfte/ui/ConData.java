package ru.dz.jfte.ui;

import java.awt.Color;
import java.awt.Graphics2D;
import java.util.Arrays;

import ru.dz.jfte.PCell;

public class ConData {
	int xSize = 80;
	int ySize = 25;
	
	static int xCell = 20;
	static int yCell = 30;
	
	int colors[][] = new int[xSize][ySize];
	char chars[][] = new char[xSize][ySize];
	
	public void paint(Graphics2D g) {

		for( int y = 0; y < ySize; y++ )
			for( int x = 0; x < xSize; x++ )
			{
				int fb = colors[x][y];
				char ch = chars[x][y];
				
				Color fg = map[ fb & 0xF];
				Color bg = map[ (fb >> 4) & 0xF];

				g.setColor(bg);
				g.setColor(Color.black); // TODO test
				g.fillRect(x*xCell, y*yCell, xCell, yCell);
				
				g.setColor(fg);				
				g.drawString(""+ch, x*xCell, y*yCell);
			}
	}
	
	
	static final int NCOLOR = 16;
	
	static Color [] map = new Color[NCOLOR];
	
	static {
		for( int i = 0; i < NCOLOR; i++ )
		{
			map[i] = Color.yellow;
		}
		
		map[0] = Color.black;
		// TODO others
	}

	public void clear() {
		Arrays.fill(colors,0);
		Arrays.fill(chars,' ');
	}

	public void putc(int x, int y, char ch, int attr) {
		colors[x][y] = attr;
		chars[x][y] = ch;
	}

	public void putc(int x, int y, Long c) {
		colors[x][y] = PCell.getAttr(c);
		chars[x][y] = (char) PCell.getChar(c);
	}

	public long getc(int x, int y) {
		return PCell.charAndAttr(chars[x][y], colors[x][y]);
	}

	public int getXSize() { return xSize; }
	public int getYSize() { return ySize; }

	public int getWidth() {
		return xSize * xCell;
	}

	public int getHeight() {
		return ySize * yCell;
	}
	
}
