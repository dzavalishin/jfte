package ru.dz.jfte;

public class TDrawBuffer extends PCell 
{
	static final int ConMaxCols = 500;
	
	public TDrawBuffer() {
		super(new Long [ConMaxCols]);
	}


	public TDrawBuffer(int size) {
		super(new Long [size]);
	}
	
}
