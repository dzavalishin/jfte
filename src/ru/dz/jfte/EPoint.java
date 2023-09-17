package ru.dz.jfte;

public class EPoint {
    int Row;
    int Col;

    EPoint(int aRow, int aCol) { Row = aRow; Col = aCol; }
    EPoint(int aRow) { Row = aRow; Col = 0; }
    EPoint() { Row = Col = 0; }

}
