package ru.dz.jfte.ui;

import javax.swing.JFrame;

public class ConFrame extends JFrame
{

	public ConFrame(ConCanvas cc) {

		add(cc);
		
		pack();
		setVisible(true);

	}

	
	
}
