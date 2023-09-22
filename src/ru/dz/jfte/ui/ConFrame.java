package ru.dz.jfte.ui;

import javax.swing.JFrame;

public class ConFrame extends JFrame
{

	public ConFrame(ConCanvas cc) 
	{
		setDefaultCloseOperation(EXIT_ON_CLOSE);
		
		getContentPane().add(cc);
		
		setSize(1000, 800);
		
		pack();
		setVisible(true);

	}

	
	
}
