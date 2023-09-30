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

public class CStringPtr extends AbstractCString
{
	
	public CStringPtr(AbstractCString src) {
		super(src.mem, src.pos, src);
	}

	public CStringPtr(AbstractCString src, int pos) {
		super(src.mem, src.pos + pos, src);
	}
	
	
	public CStringPtr(char[] src) {
		//super(src, 0, null);
		this(new CString(src));
	} 

	public CStringPtr(char[] src, int pos) {
		this(new CString(src, pos));
	}


	
	
}
