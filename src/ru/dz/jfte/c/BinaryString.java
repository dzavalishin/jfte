package ru.dz.jfte.c;

import java.util.Arrays;


/** 
 * 
 * Emulate C byte array strings
 * 
 * @author dz
 *
 * Mutable, mimics StringBuilder to some extent too.
 *
 */

public class BinaryString 
{
	private static final int ALLOC_STEP = 15;
	private static final int ALLOC_START = 15;
	
	private Character[] mem; // = new byte[ALLOC_START];
	private int usedLen; // = 15;
	private int pos = 0;

	public BinaryString() 
	{
		//mem = new char[ALLOC_START];
		mem = new Character[ALLOC_START];
		
		usedLen = 0;
		pos = 0;
	}

	public BinaryString( BinaryString bs ) 
	{
		mem = Arrays.copyOf(bs.mem, bs.usedLen);
		usedLen = mem.length;
		pos = usedLen; // append
	}

	public BinaryString( String s ) 
	{
		mem = new Character[s.length()];
		usedLen = 0;
		pos = 0;
		append(s);
	}
	
	/**
	 * Used in language pack loader
	 * @param s byte string to load from
	 * @param ppos position
	 * @param len length
	 */
	public BinaryString(byte[] s, int ppos, int len) 
	{
		mem = new Character[len];
		usedLen = 0;
		pos = 0;
		
		for( int p = 0; p < len; p++ )
			append(s[ppos+p]);
	}

	/**
	 * NB! Uses buf as is, no copy.
	 * @param buf
	 * /
	private BinaryString(char[] buf) 
	{
		mem = buf;
		usedLen = buf.length;
		pos = usedLen;
	}*/

	public BinaryString(byte[] ba) {
		this(ba, 0, ba.length);
	}

	public char charAt(int i)
	{
		return mem[i];
	}

	public int length() { return usedLen; }

	/*
	public void setLength(int i) 
	{
		usedLen = Math.min( usedLen, i );
		if( pos > usedLen ) pos = usedLen;
	}*/

	public void trySetSize(int size)
	{
		if(size <= mem.length)
			TryContract(size);
		else
			tryExtend(size);
	}

	/**
	 * Shorten
	 * @param i ne size
	 */
	public void TryContract(int i) 
	{
		usedLen = Math.min( usedLen, i );
		if( pos > usedLen ) pos = usedLen;
	}
	
	/**
	 * Lengthen
	 * @param size
	 */
	private void tryExtend(int size) 
	{
		if( mem.length - pos > size)
			return;
		
		int ext = Math.max(size - (mem.length-pos), ALLOC_STEP );
		mem = Arrays.copyOf(mem, ext + mem.length);
	}
	
	
	public BinaryString append( char c )
	{
		tryExtend(1);
		mem[pos++] = c;
		if(pos > usedLen) usedLen = pos;
		return this;
	}

	public BinaryString append( byte c )
	{
		tryExtend(1);
		mem[pos++] = (char) (0xFF & (int)c); // positive
		if(pos > usedLen) usedLen = pos;
		return this;
	}


	public BinaryString append( String s )
	{
		tryExtend(s.length());
		
		for( char c : s.toCharArray() )
			append(c);
		
		return this;
	}

	public Character[] toCharArray() {
		return Arrays.copyOf(mem, usedLen);
	}
	

	
	
	
	/*
	public int READ_LE_int(int i) 
	{
		int hi = mem[1+i];
		int lo = mem[0+i];
		return (0xFF & lo) + ((0xFF & hi) << 8);
	}*/


	@Override
	public String toString() {
		char [] ca = new char[usedLen];
		
		for(int i = 0; i < usedLen; i++)
			ca[i] = mem[i];
		
		return String.valueOf(ca, 0, usedLen);
	}

	public void memmove(int dest, int src, int len) {
		System.arraycopy(mem, src, mem, dest, len);		
	}

	public void memset(int ofs, char c, int count) {
		Arrays.fill(mem, ofs, ofs+count, c );		
	}

	public void copyIn(int ofs, String data, int count) {
		char[] ca = data.toCharArray();
		System.arraycopy(ca, 0, mem, ofs, count);		
	}

	public ArrayPtr<Character> getPointer() {
		return new ArrayPtr<Character>(mem);
	}
	
}
