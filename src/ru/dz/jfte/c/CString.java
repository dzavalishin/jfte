package ru.dz.jfte.c;

import java.util.Arrays;

/**
 * 
 * Mutable string to replace C {@code char *} when porting from C to Java.
 * 
 * <p>
 * 
 * All constructors create new instance of string, copying source if given.
 * 
 * @author dz
 *
 */

public class CString extends AbstractCString 
{
	
	public CString() {
		super(0,null);
	}
	
	
	public CString(int size) {
		super(size,null);
	}
	
	public CString(int size, char fill) {
		super(size,null);
		Arrays.fill(mem, fill);
	}

	public CString(CharSequence src) {
		super(src.length(),null);
		memmove(src,src.length());
	}

	public CString(CharSequence src, int pos, int len) {
		super(len,null);
		memmove( 0, src, pos, len);
	}
	

	/*
	public CString(CString src) {
		super(src.length());
		strcpy(src);
	}

	public CString(CString src, int pos, int len) {
		super(len);
		strcpy(src.subSequence(pos, len));
	}*/
	
	
	public CString(byte[] src) {
		super(src.length, null);
		memmove(0, src, 0, src.length);
	} 

	public CString(byte[] src, int pos, int len) {
		super(len,null);
		memmove(0, src, pos, len);
	} 
	
	public CString(char[] src) {
		super(src.length, null);
		memmove(0, src, 0, src.length);
	}
	
	public CString(char[] src, int spos) {
		super(src.length-spos, null);
		memmove(0, src, spos, src.length-spos);
	}
	
	// -------------------------------------------------------------------
	// Size
	// -------------------------------------------------------------------


	/**
	 * Intternal
	 */
	
	@Override
	protected void reSize(int size) {
		mem = Arrays.copyOf(mem, shift+size);
	}





	
	

	
}
