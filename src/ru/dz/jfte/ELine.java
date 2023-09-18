package ru.dz.jfte;

import ru.dz.jfte.c.ArrayPtr;
import ru.dz.jfte.c.BinaryString;
import ru.dz.jfte.c.ByteArrayPtr;

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

	int getCount() { return Chars.length(); } // TODO is it ok?

	/**
	 * Set size
	 * @param size new size
	 */
	void setCount(int size)
	{
		Chars.trySetSize(size);
	}

	public void Allocate(int size) {
		Chars.trySetSize(size);
	}

	public void memmove(int dest, int src, int len) {
		Chars.memmove(dest, src, len);
	}

	public void memset(int ofs, char c, int count) {
		Chars.memset(ofs, c, count);
		
	}

	public void copyIn(int ofs, String data, int count) {
		Chars.copyIn(ofs, data, count);
		
	}

	public char charAt(int ofs) {
		return Chars.charAt(ofs);
	}

	public ArrayPtr<Character> getPointer() {
		return Chars.getPointer();
		
	}
	
}
