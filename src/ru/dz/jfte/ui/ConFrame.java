package ru.dz.jfte.ui;

import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;

import javax.swing.JFrame;

public class ConFrame extends JFrame
{

	public ConFrame(ConCanvas cc) 
	{
		setDefaultCloseOperation(EXIT_ON_CLOSE);
		
		getContentPane().add(cc);
		cc.requestFocusInWindow();
		
		addKeyListener(new KeyListener() {
			
			@Override
			public void keyTyped(KeyEvent e) {
				//cc.queueEvent(e);
			}

			@Override
			public void keyReleased(KeyEvent e) {
				cc.queueEvent(e);
				
			}
			
			@Override
			public void keyPressed(KeyEvent e) {
				cc.queueEvent(e);
				
			}
		});
				
		
		setSize(1000, 800);
		
		pack();
		setVisible(true);

	}

	
	
}
