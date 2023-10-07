package ru.dz.jfte.c;

/**
 * 
 * Mutable string to replace C {@code char *} when porting from C to Java.
 * 
 * <p>
 * 
 * All constructors create objects that access given source object data.
 * 
 * @author dz
 *
 */

public class CStringPtr extends AbstractCString implements IArrayPtr
{
	private final int zeroShift;
	
	public CStringPtr(AbstractCString src) {
		super(src.mem, src.shift, src);
		zeroShift = src.shift;
	}

	public CStringPtr(AbstractCString src, int pos) {
		super(src.mem, src.shift + pos, src);
		zeroShift = src.shift + pos;
	}
	
	
	public CStringPtr(char[] src) {
		//super(src, 0, null);
		this(new CString(src));
	} 

	public CStringPtr(char[] src, int pos) {
		this(new CString(src, pos));
	}

	@Override
	protected void grow(int size) {
		owner.grow(size);
		mem = owner.mem;
	}

	
	// -------------------------------------------------------------------
	// From IArrayPtr
	// -------------------------------------------------------------------
	
	@Override
	public void shift(int add) { shift += add; }

	@Override
	public void inc() { shift++; }

	@Override
	public void dec() { shift--; }


	@Override
	public int getPos() { return shift; }

	@Override
	public int getDisplacement() { return shift; }

	@Override
	public void setPos(int pos) { shift = pos; }




	
	@Override
	public boolean hasCurrent() {			
		return shift >= 0 && shift < mem.length;
	}
	
	
	
	/**
	 * Write ORed
	 * 
	 * @param shift
	 * @param b
	 * /
	public void wor(int shift, int b) 
	{
		mem[ displ + shift ] |= b;	
	}
	
	public void wand(int shift, int b) 
	{
		mem[ displ + shift ] &= b;	
	}

	/** 
	 * Read and increment pointer
	 * @return <b>signed</b> char
	 */
	public char rpp() {
		char v = r(0);
		shift(1);
		return v;
	}
	
	/** 
	 * Read and increment pointer
	 * @return <b>unsigned</b> char
	 */
	public int urpp() {
		int v = 0xFFFF & r(0);
		shift(1);
		return v;
	}

	/**
	 * Write and increment pointer.
	 * @param b Char to write.
	 */
	public void wpp(char b) {
		w(0, b);
		shift(1);		
	}
	
	
	
}
