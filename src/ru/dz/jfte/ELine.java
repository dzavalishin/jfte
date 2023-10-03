package ru.dz.jfte;

/*
import ru.dz.jfte.c.ArrayPtr;
import ru.dz.jfte.c.BinaryString;
import ru.dz.jfte.c.ByteArrayPtr;
*/

import ru.dz.jfte.c.CString;
import ru.dz.jfte.c.CStringPtr;

/*

public class ELine 
{
	BinaryString Chars = new BinaryString(0);
	int  StateE; // hlState



	ELine(String AChars) {
		StateE = 0;

		if (AChars != null)
			Chars = new BinaryString(AChars);
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

	@Override
	public String toString() {
		return Chars.toString();
	}

	/**
	 * 
	 * Returns partial String 
	 * 
	 * @param start first character position
	 * @param end position AFTER last character 
	 * @return String or empty String if no intersection
	 * /
	public String substring(int start, int end) {
		return Chars.substring( start,  end);
	}

	public String substring(int start) {
		return Chars.substring( start);
	}


	
	
	
	
	
	public int getSize() {		return Chars.getSize();	}
	
	/**
	 * Set size
	 * @param size new size
	 * /
	void setSize(int size)
	{
		Chars.setSize(size);
	}

	public void Allocate(int size) {
		Chars.setSize(size);
	}

	int usedLength() { return Chars.usedLength(); } 


	//int getCount() { return Chars.usedLength(); } 
	int getCount() { return Chars.getSize(); } 

	
	//int getCount() { return Chars.usedLength(); } 
	
}

*/

public class ELine 
{
	CString Chars = new CString(0);
	int  StateE; // hlState
	
	
	public ELine() {
	}

	public ELine(String string) {
		Chars = new CString(string);
	}

	public int getCount() {
		return Chars.length();
	}

	public int getSize() {
		return Chars.length();
	}

	
	public void Allocate(int size) {
		Chars.setSize(size);
	}
	
	

	public char charAt(int ofs) {
		return Chars.charAt(ofs);
	}
	
	
	
	public void memmove(int dest, int src, int len) {
		Chars.memmove(dest, src, len);
	}

	public void memset(int ofs, char c, int count) {
		Chars.memset(ofs, c, count);		
	}



	
	
	/**
	 * 
	 * Returns partial String 
	 * 
	 * @param start first character position
	 * @param end position AFTER last character 
	 * @return String or empty String if no intersection
	 */
	public String substring(int start, int end) {
		return Chars.substring( start,  end).toString();
	}

	public String substring(int start) {
		return Chars.substring( start).toString();
	}

	public CStringPtr getPointer() {
		return Chars.getPointer();
	}

	public void copyIn(int ofs, String buffer, int len) {
		Chars.copyIn(ofs, buffer, len);		
	}
	
	
}

