package ru.dz.jfte;

import ru.dz.jfte.ui.ConCanvas;
import ru.dz.jfte.ui.ConData;

public class JavaConsole implements GuiDefs, EventDefs
{
	private ConData cd = new ConData();
	ConCanvas cc = new ConCanvas(cd );

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
	}

	// copy same char all over the box
	public void setBox(int x, int y, int w, int h, PCell cell) 
	{
		for(int i = 0; i < h; i++ )
		{
			for( int j = 0; j < w; j ++)
				cd.putc( x+j, y+i, cell.r(0));
		}		
	}
	
	
	public void putBox(int x, int y, int w, int h, PCell cell) 
	{
		for(int i = 0; i < h; i++ )
		{
			for( int j = 0; j < w; j ++)
				cd.putc( x+j, y+i, cell.r(j + i*w));
		}		
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
	}

	
	public void fill(int x, int y, int w, int h, char ch, int attr) 
	{
		for(int i = 0; i < h; i++ )
		{
			for( int j = 0; j < w; j ++)
				cd.putc( x+j, y+i, ch, attr);
		}		
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

	}
	
}
