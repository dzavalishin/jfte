package ru.dz.jfte.c;

import java.util.Arrays;

public abstract class AbstractCString implements ICString 
{
	protected char[] mem; // = new byte[ALLOC_START];

	/**
	 * All the operations are done as if string starts at this position.
	 */

	protected int shift = 0;

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
		this.shift = pos;
	}



	// -------------------------------------------------------------------
	// CharSequence
	// -------------------------------------------------------------------


	@Override
	public int length() {		
		return mem.length-shift;
	}

	@Override
	public char charAt(int index) {
		return mem[index+shift];
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

		for( int i = shift; i < mem.length; i++ )
			ret ^= mem[shift];

		return ret;
	}

	// -------------------------------------------------------------------
	// Length
	// -------------------------------------------------------------------


	@Override
	public int strlen() 
	{
		for(int i = shift; i < mem.length; i++)
			if(mem[i] == 0)
				return i-shift;

		return mem.length-shift;
	}

	@Override
	public int size() {
		return mem.length - shift;
	}

	/**
	 * Internal. Ignores 'pos' variable.
	 * 
	 * @param size Absolute container size to set.
	 */

	protected void reSize(int size) {
		throw new RuntimeException("AbstractCString.reSize called");		
	}


	/**
	 * Resize if mem is smaller
	 * @param size New size
	 */
	protected void grow(int size) {
		reSize(Math.max( owner.mem.length, size) );		
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
			mem[i+shift] = ch;
			if( ch == 0 )
				break;
		}
	}

	@Override
	public void strncpy(CharSequence src, int size) {
		for( int i = 0; i < size; i ++)
		{
			char ch = src.charAt(i);
			mem[i+shift] = ch;
			if( ch == 0 )
				break;
		}
	}


	@Override
	public void strcpy(byte[] src) {
		for( int i = 0; i < src.length; i ++)
		{
			char ch = (char) (0xFF & src[i]);
			mem[i+shift] = ch;
			if( ch == 0 )
				break;
		}
	}

	@Override
	public void strcpy(byte[] src, int spos, int len) {
		for( int i = 0; i < len; i ++)
		{
			char ch = (char) (0xFF & src[i+spos]);
			mem[i+shift] = ch;
			if( ch == 0 )
				break;
		}
	}


	@Override
	public void strcpy(int spos, int len) {
		for( int i = 0; i < len; i ++)
		{
			char ch = mem[shift+spos+i];
			mem[i+shift] = ch;
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

			System.arraycopy(csrc.mem, csrc.shift, mem, shift, size);			
		}
		else
			for( int i = 0; i < size; i ++)
				mem[i+shift] = src.charAt(i);
	}

	@Override
	public void memmove(int destPos, CharSequence src, int srcPos, int size) 
	{
		if (src instanceof AbstractCString) {
			AbstractCString csrc = (AbstractCString) src;

			System.arraycopy(csrc.mem, csrc.shift+srcPos, mem, shift+destPos, size);			
		}
		else
			for( int i = 0; i < size; i ++)
				mem[i+shift+destPos] = src.charAt(i+srcPos);
	} 

	@Override
	public void memmove(int destPos, char [] src, int srcPos, int size) 
	{
		for( int i = 0; i < size; i ++)
			mem[i+shift+destPos] = src[i+srcPos];
	} 

	@Override
	public void memmove(int destPos, byte [] src, int srcPos, int size) 
	{
		for( int i = 0; i < size; i ++)
			mem[i+shift+destPos] = (char) (0xFF & (int)src[i+srcPos]);
	} 

	@Override
	public void memmove(int dest, int src, int len) {
		System.arraycopy(mem, src+shift, mem, dest+shift, len);		
	}



	@Override
	public void memset(char data, int size) {
		Arrays.fill(mem, 0+shift, size, data);

	}

	@Override
	public void memset(char data, int start, int size) {
		Arrays.fill(mem, start+shift, start+size+shift, data);

	}


	// -------------------------------------------------------------------
	// Concat
	// -------------------------------------------------------------------

	@Override
	public void strcat(CharSequence src) 
	{
		int len = src.length();
		int oldLen = length();
		int newSize = len + oldLen + shift;
		owner.grow( newSize );
		memmove(oldLen, src, 0, len);
	}

	@Override
	public void strncat(CharSequence src, int len) {
		int oldLen = length();
		int newSize = len + oldLen + shift;
		owner.grow( newSize );
		memmove(oldLen, src, 0, len);
	}


	// -------------------------------------------------------------------
	// Cmp
	// -------------------------------------------------------------------

	@Override
	public int strcmp(CharSequence src) {		
		return compareTo(src);
	}

	@Override
	public int strncmp(CharSequence src, int n) 
	{
		if (n == 0)	         return 0;

		while (n-- > 0 && charAt(n) == src.charAt(n)) 
		{
			if (n == 0 || charAt(n) == '\0')
				return 0;
		}

		return charCmp(src, n);	
	}

	@Override
	public int memcmp(CharSequence src, int n) 
	{
		if (n == 0)	         return 0;

		while (n-- > 0 && charAt(n) == src.charAt(n)) 
			if (n == 0)
				return 0;

		return charCmp(src, n);	
	}


	private int charCmp(CharSequence src, int n) {
		int uc1 = (char)( 0xFF & charAt(n));
		int uc2 = (char)( 0xFF & src.charAt(n));

		if(uc1 < uc2) return -1;
		if(uc1 > uc2) return 1;

		return 0;
	}


	/** TODO strcoll
	@Override
	public int strcoll(CharSequence src) {
		// TODO Auto-generated method stub
		return 0;
	} */

	// -------------------------------------------------------------------
	// Search
	// -------------------------------------------------------------------


	@Override
	public int strchr(char c) {
		for( int i = 0; i < mem.length-shift; i++)
		{
			if( mem[i+shift] == 0 )
				break;

			if( mem[i+shift] == c )
				return i;
		}

		return -1;
	}

	@Override
	public int strchr(char c, int start) {
		for( int i = shift+start; i < mem.length; i++)
		{
			if( mem[i] == 0 )
				break;

			if( mem[i] == c )
				return i-shift;
		}

		return -1;
	}

	@Override
	public int strrchr(char c) 
	{
		int ret = -1;

		for( int i = shift; i < mem.length; i++)
		{
			if( mem[i] == 0 )
				break;

			if( mem[i] == c )
				ret = i-shift;
		}

		return ret;
	}

	@Override
	public int memchr(char c, int len) {
		for( int i = shift; i < len; i++)
			if( mem[i] == c )
				return i-shift;

		return -1;
	}

	@Override
	public int strstr(CharSequence src) 
	{
		// TODO if src is AbstractCString - compare checking for '\0'
		int slen = src.length();

		for( int i = shift; i < mem.length; i++)
		{
			if( mem[i] == 0 )
				break;

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
					return i-shift;
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
		return strspn( delimiters, 0);
	}

	@Override
	public int strspn(CharSequence delimiters, int start) {
		// TODO Auto-generated method stub
		throw new RuntimeException();
		//return 0;
	}

	@Override
	public int strcspn(CharSequence delimiters) {
		return strcspn( delimiters, 0);
	}

	@Override
	public int strcspn(CharSequence delimiters, int start) 
	{
		int ret = -1;

		for (int dn = 0; dn < delimiters.length(); dn++)
		{
			int pos = strchr(delimiters.charAt(dn), start);
			if( pos < 0 )
				continue;
			if( ret < 0 || pos < ret )
				ret = pos;
		}

		return ret;
	}

	@Override
	public int strpbrk(CharSequence src) {
		// TODO Auto-generated method stub
		return 0;
	}


	/* TODO strxfrm
	@Override
	public int strxfrm(CharSequence src, int len) {
		// TODO Auto-generated method stub
		return 0;
	} */



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
		return new String( mem, shift, mem.length-shift );
	}

	@Override
	public String toString(int startPos) {
		return new String( mem, startPos+shift, mem.length-startPos-shift );
	}

	@Override
	public String toString(int startPos, int endPos) {
		return new String( mem, startPos+shift, endPos-startPos-shift );
	}

	@Override
	public CStringPtr toPointer() { return new CStringPtr(this); } 


	// -------------------------------------------------------------------
	// Size
	// -------------------------------------------------------------------

	//static ICString malloc( int size ) { return new CString(size); }

	/**
	 * 
	 * NB! Sets absolute size for the container array. Caller must take in account own
	 * 'pos' variable. 
	 * 
	 * @param size
	 */

	@Override
	public void setSize(int size) {
		reSize(shift+size);
	}

	public void realloc(int size) {
		setSize(size);
	}


}
