package ru.dz.jfte.ui;

import java.awt.AWTEvent;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.event.InputEvent;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionAdapter;
import java.util.ArrayList;
import java.util.List;

import javax.swing.JPanel;

import ru.dz.jfte.EventDefs;
import ru.dz.jfte.TEvent;

public class ConCanvas extends JPanel implements EventDefs
{

	private static final int MAX_EVENTS = 50;

	private ConData cd;
	public Point mousePos = new Point(0, 0);
	private List<KeyEvent> kelist = new ArrayList<>(10);
	private List<MouseEvent> melist = new ArrayList<>(10);
	

	public ConCanvas(ConData cd) 
	{
		this.cd = cd;

		//setSize(1000, 800);

		
		addMouseMotionListener(
				new MouseMotionAdapter(){

					//store the drag coordinates and repaint
					@Override
					public void mouseDragged(MouseEvent e) 
					{
						//repaint();
						mousePos = e.getPoint();
						queueEvent(e);
					}
					
					public void mouseMoved(MouseEvent e) {
						mousePos = e.getPoint();
						queueEvent(e);						
					};
					
				} );

		addMouseListener(new MouseListener() {			
			@Override
			public void mouseReleased(MouseEvent e) {
				queueEvent(e);
			}
			
			@Override
			public void mousePressed(MouseEvent e) {
				queueEvent(e);				
			}
			
			@Override
			public void mouseExited(MouseEvent e) {
			}
			
			@Override
			public void mouseEntered(MouseEvent e) {
			}
			
			@Override
			public void mouseClicked(MouseEvent e) {
				//queueEvent(e);
			}
		});
		
		addKeyListener(new KeyListener() {
			
			@Override
			public void keyTyped(KeyEvent e) {
				queueEvent(e);
				// TODO else cry aloud?
			}

			@Override
			public void keyReleased(KeyEvent e) {
				// TODO Auto-generated method stub
				
			}
			
			@Override
			public void keyPressed(KeyEvent e) {
				// TODO Auto-generated method stub
				
			}
		});
		
		Dimension d = new Dimension(cd.getWidth(), cd.getHeight());
		
		setMinimumSize(d);
		setPreferredSize(d);
		setSize(d);
	}

	@Override
	public void paintComponent(Graphics gp) {
		Graphics2D g = (Graphics2D) gp;
		//clear drawing area
		super.paintComponent(g);

		cd.paint(g);

	}

	
	private void queueEvent(KeyEvent e) {
		if( kelist.size() < MAX_EVENTS)
			kelist.add(e);
	}

	private void queueEvent(MouseEvent e) {
		if( melist.size() < MAX_EVENTS)
			melist.add(e);
	}
	
	
	public TEvent pollKeyb() {

		if(kelist.size() > 0)
		{
			KeyEvent e = kelist.remove(0);
			
			char c = e.getKeyChar();
			int code = e.getKeyCode();
					
			// TODO remap?
			
			if( c != 0 ) code = c;
			// TODO key up?
			return TEvent.newKeyDownEvent(code);
		}
		
		if(melist.size() > 0)
		{
			MouseEvent e = melist.remove(0);
			
			int what, x,  y,  buttons,  count = 0;
			
			x = e.getX();
			y = e.getY();
			
			count = e.getClickCount();
			
			buttons = e.getButton()+1;
			
			what = 0;
			
			// TODO evMouseAuto - what's that?
			if(e.getID() == MouseEvent.MOUSE_PRESSED) what = evMouseDown;
			if(e.getID() == MouseEvent.MOUSE_RELEASED) what = evMouseUp;
			if(e.getID() == MouseEvent.MOUSE_MOVED) what = evMouseMove;
			if(e.getID() == MouseEvent.MOUSE_DRAGGED) what = evMouseMove;
			
			if(what != 0)
				return TEvent.newMouseEvent(what, x, y, buttons, count);			
		}
		
		return null;
	}

}
