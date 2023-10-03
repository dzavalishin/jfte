package ru.dz.jfte;

public class EPoint 
{
    int Row;
    int Col;

    public EPoint(int aRow, int aCol) { Row = aRow; Col = aCol; }
    public EPoint(int aRow) { Row = aRow; Col = 0; }
    public EPoint() { Row = Col = 0; }

    public EPoint(EPoint src) {
    	Row = src.Row; 
    	Col = src.Col;
	}
    
	public int getCol() { return Col; }
    public int getRow() { return Row; }
}
