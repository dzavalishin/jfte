package ru.dz.jfte;

import java.awt.Point;

import ru.dz.jfte.ui.ConCanvas;
import ru.dz.jfte.ui.ConData;
import ru.dz.jfte.ui.ConFrame;

public class JavaConsole implements GuiDefs, EventDefs
{
	private ConData cd = new ConData();
	ConCanvas cc = new ConCanvas( cd );
	private ConFrame cf;
	

	public JavaConsole() {
		cf = new ConFrame(cc);
	}
	
	public void clear() {
		cd.clear();		
	}

	public int getWidth() {
		return cd.getXSize();
	}

	public int getHeight() {
		return cd.getYSize();
	}

	// put same line mult times?
	void PutLine(int x, int y, int w, int h, PCell cell) 
	{
		for(int i = 0; i < h; i++ )
		{
			for( int j = 0; j < w; j ++)
				cd.putc( x+j, y+i, cell.r(j));
		}
		cc.repaint();
	}

	// copy same char all over the box
	public void setBox(int x, int y, int w, int h, PCell cell) 
	{
		for(int i = 0; i < h; i++ )
		{
			for( int j = 0; j < w; j ++)
				cd.putc( x+j, y+i, cell.r(0));
		}		
		cc.repaint();
	}
	
	
	public void putBox(int x, int y, int w, int h, PCell cell) 
	{
		for(int i = 0; i < h; i++ )
		{
			for( int j = 0; j < w; j ++)
				cd.putc( x+j, y+i, cell.r(j + i*w));
		}		
		cc.repaint();
	}

	public void getBox(int x, int y, int w, int h, PCell cell) {
		for(int i = 0; i < h; i++ )
		{
			for( int j = 0; j < w; j ++)
			{
				long ch = cd.getc( x+j, y+i);
				cell.w(j + i*w, ch);
			}
		}		
		cc.repaint();
	}

	
	public void fill(int x, int y, int w, int h, char ch, int attr) 
	{
		for(int i = 0; i < h; i++ )
		{
			for( int j = 0; j < w; j ++)
				cd.putc( x+j, y+i, ch, attr);
		}		
		cc.repaint();
	}
	
	public void scroll(int way, int x, int y, int w, int h, int fill, int count) {

		TDrawBuffer db = new TDrawBuffer(w*h);

		switch(way)
		{
		case csUp:
			getBox( x, y+count, w, h-count, db );
			putBox( x, y, w, h-count, db );		
			
			fill( x, y+h-count, w, count, ' ', fill);
			break;
			
		case csDown:
			getBox( x, y, w, h-count, db );
			putBox( x, y+count, w, h-count, db );		
			
			fill( x, y, w, count, ' ', fill);
			break;
			
		}

		cc.repaint();
	}

	public void drawCursor(boolean b) {
		cd.cursorVisible  = b;
		cc.repaint();		
	}

	public void setTitle(String string) {
		cf.setTitle(string);
	}

	public void setCursorPos(int x, int y) {
		cd.cursorPos = new Point(x, y);		
		cc.repaint();
}

	public void queryCursorPos(int[] x, int[] y) {
		x[0] = cd.cursorPos.x;
		y[0] = cd.cursorPos.y;
	}

	public Point getMousePos() 
	{ 		
		return new Point(cc.mousePos.x / ConData.xCell, cc.mousePos.y / ConData.yCell);
	}

	public void enableMouse(boolean mouseVisible) {
		// TODO Auto-generated method stub
		
	}

	public TEvent pollKeyb() {
		return cc.pollKeyb();
	}
	
}
