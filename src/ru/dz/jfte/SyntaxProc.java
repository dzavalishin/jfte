package ru.dz.jfte;

public interface SyntaxProc {
	//int proc(EBuffer BF, int LN, PCell B, int Pos, int Width, ELine Line, byte /*hlState*/ [] State, byte /*hsState*/ [] StateMap, int [] ECol);
	int proc(EBuffer BF, int LN, PCell B, int Pos, int Width, ELine Line, int /*hlState*/ State, int /*hsState*/ [] StateMap, int [] ECol);
}
