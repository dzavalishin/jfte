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
import ru.dz.jfte.KeyDefs;
import ru.dz.jfte.TEvent;

public class ConCanvas extends JPanel implements EventDefs, KeyDefs
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

		/*
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
		});/**/
		
		addKeyListener(new KeyListener() {
			
			@Override
			public void keyTyped(KeyEvent e) {
				//queueEvent(e);
				// TODO else cry aloud?
			}

			@Override
			public void keyReleased(KeyEvent e) {
				// TODO Auto-generated method stub
				queueEvent(e);
				
			}
			
			@Override
			public void keyPressed(KeyEvent e) {
				// TODO Auto-generated method stub
				queueEvent(e);
				
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

	
	void queueEvent(KeyEvent e) {
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
			int code = remapKey( e.getKeyCode() );
			int id = e.getID();
			
			int mod = e.getModifiersEx();
					
			//System.out.printf("key %s \n", e );
			//System.out.printf("k char %x code %x id %d\n", (int)c, code, id );
			
			//if( c != 0xFFFF ) code = c;
			// TODO key ch
			
			if(id == KeyEvent.KEY_RELEASED)
				code |= kfKeyUp;
			
			if(0 != (mod & KeyEvent.ALT_DOWN_MASK)) code |= kfAlt;
			if(0 != (mod & KeyEvent.CTRL_DOWN_MASK)) code |= kfCtrl;
			if(0 != (mod & KeyEvent.SHIFT_DOWN_MASK)) code |= kfShift;
			
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

	private int remapKey(int k) {
		
		//if(k >= 112 && k <= 123)			return (k - 112 + 0x101) | kfSpecial; // F1...F12
		
		switch(k)
		{
		case KeyEvent.VK_UP: return kbUp|kfGray;
		case KeyEvent.VK_DOWN: return kbDown|kfGray;
		case KeyEvent.VK_LEFT: return kbLeft|kfGray;
		case KeyEvent.VK_RIGHT: return kbRight|kfGray;

		case KeyEvent.VK_HOME: return kbHome|kfGray;
		case KeyEvent.VK_END: return kbEnd|kfGray;
		case KeyEvent.VK_PAGE_UP: return kbPgUp|kfGray;
		case KeyEvent.VK_PAGE_DOWN: return kbPgDn|kfGray;

		case KeyEvent.VK_INSERT: return kbIns|kfGray;
		case KeyEvent.VK_DELETE: return kbDel|kfGray;

		case KeyEvent.VK_BACK_SPACE: return kbBackSp;
		case KeyEvent.VK_TAB: return kbTab;
		case KeyEvent.VK_ENTER: return kbEnter;
		case KeyEvent.VK_ESCAPE: return kbEsc;

		
		case KeyEvent.VK_F1: return kbF1;
		case KeyEvent.VK_F2: return kbF2;
		case KeyEvent.VK_F3: return kbF3;
		case KeyEvent.VK_F4: return kbF4;
		case KeyEvent.VK_F5: return kbF5;
		case KeyEvent.VK_F6: return kbF6;
		case KeyEvent.VK_F7: return kbF7;
		case KeyEvent.VK_F8: return kbF8;
		case KeyEvent.VK_F9: return kbF9;
		case KeyEvent.VK_F10: return kbF10;
		case KeyEvent.VK_F11: return kbF11;
		case KeyEvent.VK_F12: return kbF12;
		
		// TODO more keys
		
		}
		
		return k;
	}

}
