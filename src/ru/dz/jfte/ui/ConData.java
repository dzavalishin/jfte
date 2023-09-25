package ru.dz.jfte.ui;

import java.awt.Color;
import java.awt.Graphics2D;
import java.util.Arrays;

import ru.dz.jfte.PCell;

public class ConData 
{
	static int xCell = 14;
	static int yCell = 18;
	
	int xSize = 80;
	int ySize = 25;
	
	int colors[][] = new int[xSize][ySize];
	char chars[][] = new char[xSize][ySize];
	
	public ConData() {
		for( int x = 0; x < xSize; x++ )
		{
			putc(x, 2, 'a', 0x22);
			putc(x, 4, 'b', 0x22);
		}
	}
	
	public void paint(Graphics2D g) 
	{
		g.setFont(g.getFont().deriveFont(40));
		
		for( int y = 0; y < ySize; y++ )
		{
			for( int x = 0; x < xSize; x++ )
			{
				int fb = colors[x][y];
				char ch = chars[x][y];
				
				Color fg = map[ fb & 0xF];
				Color bg = map[ (fb >> 4) & 0xF];

				int xp = (x+0)*xCell;
				int yp = (y+1)*yCell-1;
				
				g.setColor(bg);
				//g.setColor(Color.black); // TODO test
				g.fillRect(x*xCell, y*yCell, xCell, yCell);
				
				g.setColor(fg);				
				//g.setColor(Color.yellow); // TODO test
				g.drawString(""+ch, xp, yp);
				
				g.setColor(Color.darkGray); // TODO test
				//g.drawRect(x*xCell, y*yCell, xCell, yCell);
				//g.drawLine(x, y, x, y);
				
				//System.out.print(ch);
			}
			//System.out.print('\n');
		}
	}
	
	
	static final int NCOLOR = 16;
	
	static Color [] map = new Color[NCOLOR];
	
	static {
		/*for( int i = 0; i < NCOLOR; i++ )
		{
			map[i] = Color.yellow;
		}*/
		
		map[0] = Color.black;
		map[1] = Color.blue;
		map[2] = Color.green;
		map[3] = Color.cyan;
		map[4] = Color.red;
		map[5] = Color.magenta;
		map[6] = new Color(0xAA5500);
		map[7] = new Color(0xAAAAAA);
		
		map[8] = new Color(0x555555);
		map[9] = new Color(0x5555FF);
		map[10] = new Color(0x55FF55);
		map[11] = new Color(0x55FFFF);
		map[12] = new Color(0xFF5555);
		map[13] = new Color(0xFF55FF);
		map[14] = new Color(0xFFFF55);
		map[15] = new Color(0xFFFFFF);
	}

	public void clear() {
		Arrays.fill(colors,0);
		Arrays.fill(chars,' ');
	}

	public void putc(int x, int y, char ch, int attr) {
		colors[x][y] = attr;
		chars[x][y] = ch;
	}

	public void putc(int x, int y, long c) {
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
