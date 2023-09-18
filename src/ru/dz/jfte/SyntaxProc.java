package ru.dz.jfte;

public interface SyntaxProc {
	int proc(EBuffer BF, int LN, PCell B, int Pos, int Width, ELine Line, byte /*hlState*/ [] State, byte /*hsState*/ [] StateMap, int [] ECol);
}
