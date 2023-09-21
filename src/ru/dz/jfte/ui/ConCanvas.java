package ru.dz.jfte.ui;

import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.event.MouseEvent;
import java.awt.event.MouseMotionAdapter;

import javax.swing.JPanel;

public class ConCanvas extends JPanel {

	

	private ConData cd;

	public ConCanvas(ConData cd) {

		this.cd = cd;

		addMouseMotionListener(
				new MouseMotionAdapter(){
					//store the drag coordinates and repaint
					@Override
					public void mouseDragged(MouseEvent event) 
					{
						//repaint();
					}
				} );
		setMinimumSize(new Dimension(cd.getWidth(), cd.getHeight()));
	}

	@Override
	public void paintComponent(Graphics gp) {
		Graphics2D g = (Graphics2D) gp;
		//clear drawing area
		super.paintComponent(g);

		cd.paint(g);

	}

}
