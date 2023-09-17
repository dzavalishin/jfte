package ru.dz.jfte;

import ru.dz.jfte.c.BinaryString;

public class ELine {
	//int Count;
	//String Chars;
	BinaryString Chars = new BinaryString();
	int /*hlState*/ StateE;



	ELine(String AChars) {
		StateE = 0;

		if (AChars != null)
			Chars = new BinaryString(AChars);
	}


}
