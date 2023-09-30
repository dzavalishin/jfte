package ru.dz.jfte.c;

import java.util.Arrays;

public abstract class AbstractCString implements ICString 
{
	protected char[] mem; // = new byte[ALLOC_START];
	
	/**
	 * All the operations are done as if string starts at this position.
	 */
	
	protected int pos = 0;

	final AbstractCString owner;
	
	// -------------------------------------------------------------------
	// Constructors
	// -------------------------------------------------------------------
	
	
	protected AbstractCString(int size, AbstractCString owner) {
		this.owner = owner == null ? this : owner;
		mem = new char[size];		
	}

	
	protected AbstractCString(char[] mem, int pos, AbstractCString owner) {
		this.owner = owner == null ? this : owner;
		this.mem = mem;
		this.pos = pos;
	}



	// -------------------------------------------------------------------
	// CharSequence
	// -------------------------------------------------------------------


	@Override
	public int length() {		
		return mem.length-pos;
	}

	@Override
	public char charAt(int index) {
		return mem[index+pos];
	}

	@Override
	public CharSequence subSequence(int start, int end) {
		return new CString(this, start, end-start);
	}

	@Override
	public int compareTo(CharSequence o) {
		return ICString.compare(this, o);
	}
	
	@Override
	public boolean equals(CharSequence src) {
		return 0 == CharSequence.compare(this, src);
	}
	
	
	/**
	 * Note that hash code is for the rest of string that is pointed by us. From pos to end. 
	 */
	
	@Override
	public int hashCode() {
		int ret = 0;
		
		for( int i = pos; i < mem.length; i++ )
			ret ^= mem[pos];
		
		return ret;
	}
	
	// -------------------------------------------------------------------
	// Length
	// -------------------------------------------------------------------
	

	@Override
	public int strlen() 
	{
		for(int i = pos; i < mem.length; i++)
			if(mem[i] == 0)
				return i-pos;
				
		return mem.length-pos;
	}

	@Override
	public int size() {
		return mem.length - pos;
	}

	
	protected void setSize(int size) {
		throw new RuntimeException("AbstractCString.setSize called");		
	}

	
	
	// -------------------------------------------------------------------
	// Copy
	// -------------------------------------------------------------------
	
	// TODO strcpy - do we have to add \0 in any case?
	
	@Override
	public void strcpy(CharSequence src) 
	{
		for( int i = 0; i < src.length(); i ++)
		{
			char ch = src.charAt(i);
			mem[i+pos] = ch;
			if( ch == 0 )
				break;
		}
	}

	@Override
	public void strncpy(CharSequence src, int size) {
		for( int i = 0; i < size; i ++)
		{
			char ch = src.charAt(i);
			mem[i+pos] = ch;
			if( ch == 0 )
				break;
		}
	}

	
	@Override
	public void strcpy(byte[] src) {
		for( int i = 0; i < src.length; i ++)
		{
			char ch = (char) (0xFF & src[i]);
			mem[i+pos] = ch;
			if( ch == 0 )
				break;
		}
	}

	@Override
	public void strcpy(byte[] src, int spos, int len) {
		for( int i = 0; i < len; i ++)
		{
			char ch = (char) (0xFF & src[i+spos]);
			mem[i+pos] = ch;
			if( ch == 0 )
				break;
		}
	}
	

	@Override
	public void strcpy(int spos, int len) {
		for( int i = 0; i < len; i ++)
		{
			char ch = mem[pos+spos+i];
			mem[i+pos] = ch;
			if( ch == 0 )
				break;
		}
	}
	
	
	// -------------------------------------------------------------------
	// Mem
	// -------------------------------------------------------------------
	
	
	@Override
	public void memcpy( CharSequence src, int size ) { memmove( src, size ); } 

	@Override
	public void memmove(CharSequence src, int size) 
	{
		if (src instanceof AbstractCString) {
			AbstractCString csrc = (AbstractCString) src;
			
			System.arraycopy(csrc.mem, csrc.pos, mem, pos, size);			
		}
		else
			for( int i = 0; i < size; i ++)
				mem[i+pos] = src.charAt(i);
	}

	@Override
	public void memmove(int destPos, CharSequence src, int srcPos, int size) 
	{
		if (src instanceof AbstractCString) {
			AbstractCString csrc = (AbstractCString) src;
			
			System.arraycopy(csrc.mem, csrc.pos+srcPos, mem, pos+destPos, size);			
		}
		else
			for( int i = 0; i < size; i ++)
				mem[i+pos+destPos] = src.charAt(i+srcPos);
	} 

	@Override
	public void memmove(int destPos, char [] src, int srcPos, int size) 
	{
		for( int i = 0; i < size; i ++)
			mem[i+pos+destPos] = src[i+srcPos];
	} 
	
	@Override
	public void memmove(int destPos, byte [] src, int srcPos, int size) 
	{
		for( int i = 0; i < size; i ++)
			mem[i+pos+destPos] = (char) (0xFF & (int)src[i+srcPos]);
	} 
	
	@Override
	public void memmove(int dest, int src, int len) {
		System.arraycopy(mem, src+pos, mem, dest+pos, len);		
	}
	
	
	
	@Override
	public void memset(char data, int size) {
		Arrays.fill(mem, 0+pos, size, data);

	}

	@Override
	public void memset(char data, int start, int size) {
		Arrays.fill(mem, start+pos, start+size+pos, data);

	}
	
	
	
	@Override
	public void strcat(CharSequence src) 
	{
		int len = src.length();
		int newSize = len + pos;
		owner.setSize( Math.max( owner.mem.length, newSize) );
		memmove(0, src, 0, len);
	}

	@Override
	public void strncat(CharSequence src, int len) {
		int newSize = len + pos;
		owner.setSize( Math.max( owner.mem.length, newSize) );
		memmove(0, src, 0, len);
	}



	@Override
	public int strcmp(CharSequence src) {		
		return compareTo(src);
	}

	@Override
	public int strncmp(CharSequence src, int len) {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public int memcmp(CharSequence src, int size) {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public int strcoll(CharSequence src) {
		// TODO Auto-generated method stub
		return 0;
	}


	@Override
	public int strchr(char c) {
		for( int i = pos; i < mem.length; i++)
			if( mem[i] == c )
				return i;
		
		return -1;
	}

	@Override
	public int strrchr(char c) 
	{
		int ret = -1;
		
		for( int i = pos; i < mem.length; i++)
			if( mem[i] == c )
				ret = i;
		
		return ret;
	}

	@Override
	public int memchr(char c, int len) {
		for( int i = pos; i < len; i++)
			if( mem[i] == c )
				return i;
		
		return -1;
	}

	@Override
	public int strstr(CharSequence src) 
	{
		// TODO if src is AbstractCString - compare checking for '\0'
		int slen = src.length();
		
		for( int i = pos; i < mem.length; i++)
		{
			if( mem[i] == src.charAt(0) )
			{
				boolean eq = true;
				for( int j = 1; j < slen; j++ )
					if(mem[i+j] != src.charAt(j))
					{
						eq = false;
						break;
					}
				if(eq)
					return i;
			}
		}
		
		return -1;
	}

	@Override
	public CStringTokenizer strtok(CharSequence delimiters) {
		return new CStringTokenizer(this, delimiters);
	}

	@Override
	public int strspn(CharSequence delimiters) {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public int strspn(CharSequence delimiters, int start) {
		// TODO Auto-generated method stub
		return 0;
	}
	
	@Override
	public int strcspn(CharSequence delimiters) {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public int strpbrk(CharSequence src) {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public int strxfrm(CharSequence src, int len) {
		// TODO Auto-generated method stub
		return 0;
	}


	
	// -------------------------------------------------------------------
	// Substring
	// -------------------------------------------------------------------
	
	
	
	
	public CString substring(int startPos, int endPos) {
		return new CString( this, startPos, endPos - startPos );
	}

	
	// -------------------------------------------------------------------
	// Export
	// -------------------------------------------------------------------
	
	
	@Override
	public String toString() {
		return new String( mem );
	}

	@Override
	public String toString(int startPos) {
		return new String( mem, startPos+pos, mem.length );
	}

	@Override
	public String toString(int startPos, int endPos) {
		return new String( mem, startPos+pos, endPos-startPos );
	}
	
	@Override
	public CStringPtr toPointer() { return new CStringPtr(this); } 
	
}
