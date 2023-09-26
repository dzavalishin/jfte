package ru.dz.jfte;

import ru.dz.jfte.c.ArrayPtr;
import ru.dz.jfte.c.BinaryString;

/**
 * Cell pointer
 * 
 * @author dz
 *
 */
public class PCell extends ArrayPtr<Long> 
{
	public PCell( Long [] start ) {
		super(start);
	}

	public PCell(PCell b) {
		super(b);
	}

	public PCell(PCell b, int shift) {
		super(b,shift);
	}



	/**
	 * Create of given size
	 * @param size
	 */
	public PCell(int size) {
		super(new TDrawBuffer(size));
	}

	private static final int shift = 8; // 32
	
	public static long charAndAttr(int c, int a)
	{
		return c | ( ((long)a) << shift);
	}

	public static int getChar(long ca)
	{
		//return (int) (ca & 0xFFFFFFFF);
		return (int) (ca & 0xFF);
	}

	public static int getAttr(long ca)
	{
		//return (int) ((ca>>shift) & 0xFFFFFFFF);
		return (int) ((ca>>shift) & 0xFF);
	}



	void MoveCh(char CCh, int /*TAttr*/  Attr, int Count) {
		MoveCh(this, CCh, Attr, Count);
	}	

	static void MoveCh(PCell B, char CCh, int /*TAttr*/  Attr, int Count) {
		//unsigned char *p = (unsigned char *) B;
		PCell p = new PCell(B);
		while (Count > 0) {
			//*p++ = (unsigned char) CCh;
			//*p++ = (unsigned char) Attr;
			p.wpp( charAndAttr(CCh, Attr) );
			Count--;
		}
	}


	void MoveChar(int Pos, int Width, char  CCh, int /*TAttr*/  Attr, int Count) {
		MoveChar(this, Pos, Width, CCh, Attr, Count);
	}

	static void MoveChar(PCell B, int Pos, int Width, char  CCh, int /*TAttr*/  Attr, int Count) {
		//unsigned char *p = (unsigned char *) B;
		PCell p = new PCell(B);
		if (Pos < 0) {
			Count += Pos;
			Pos = 0;
		}
		if (Pos >= Width) return;
		if (Pos + Count > Width) Count = Width - Pos;
		if (Count <= 0) return;
		p.shift(Pos);
		for( /*p += sizeof(TCell) * Pos*/; Count > 0; Count--) 
		{
			//*p++ = (unsigned char) CCh;
			//*p++ = (unsigned char) Attr;
			p.wpp( charAndAttr(CCh, Attr) );
		}
	}


	/*
	
	void MoveMem(PCell B, int Pos, int Width, const char* Ch, int   Attr, int Count) {
		//unsigned char *p = (unsigned char *) B;
		PCell p = new PCell(B);

		if (Pos < 0) {
			Count += Pos;
			Ch -= Pos;
			Pos = 0;
		}
		if (Pos >= Width) return;
		if (Pos + Count > Width) Count = Width - Pos;
		if (Count <= 0) return;

		p.shift(Pos);
		for (; Count > 0; Count--) {
			*p++ = (unsigned char) (*Ch++);
			*p++ = (unsigned char) Attr;
		}
	}

	*/
	public void MoveMem(int Pos, int Width, BinaryString src, int srcPos, int Attr, int Count) {
		PCell p = new PCell(this);

		//BinaryString src = new BinaryString(asrc);
		//src.
		
		if (Pos < 0) {
			Count += Pos;
			//Ch -= Pos;
			srcPos -= Pos;
			Pos = 0;
		}
		if (Pos >= Width) return;
		if (Pos + Count > Width) Count = Width - Pos;
		if (Count <= 0) return;

		p.shift(Pos);
		for (; Count > 0; Count--) {
			//*p++ = (unsigned char) (*Ch++);
			//*p++ = (unsigned char) Attr;

			int CCh = src.charAt(srcPos++);
			p.wpp( charAndAttr(CCh, Attr) );
		}
	}

	
	
	
	void MoveStr(/*PCell B,*/ int Pos, int Width, String Ch, int Attr, int MaxCount) {
		//unsigned char *p = (unsigned char *) B;
		PCell p = new PCell(this);

		if (Pos < 0) {
			/* TODO pos <0
			MaxCount += Pos;
			Ch -= Pos;
			Pos = 0; */
			throw new RuntimeException("MoveStr pos < 0");
		}
		
		if (Pos >= Width) return;
		if (Pos + MaxCount > Width) MaxCount = Width - Pos;
		if (MaxCount <= 0) return;

		p.shift(Pos);
		int chpos = 0;
		for (; MaxCount > 0 && (chpos < Ch.length()); MaxCount--) 
		{
			//*p++ = (unsigned char) Ch.charAt(chpos++);
			//*p++ = (unsigned char) Attr;
			long set = charAndAttr(Ch.charAt(chpos++), Attr);
			p.wpp(set);

		}
	} 

	public void MoveStr(int Pos, int Width, ArrayPtr<Character> cp, int Attr, int MaxCount) {
		PCell p = new PCell(this);

		if (Pos < 0) {
			MaxCount += Pos;
			cp.shift(-Pos); // TODO check out of range
			Pos = 0; 
		}
		
		if (Pos >= Width) return;
		if (Pos + MaxCount > Width) MaxCount = Width - Pos;
		if (MaxCount <= 0) return;

		p.shift(Pos);
		int chpos = 0;
		for (; MaxCount > 0 && (chpos < cp.length()); MaxCount--) 
		{
			//*p++ = (unsigned char) Ch.charAt(chpos++);
			//*p++ = (unsigned char) Attr;
			long set = charAndAttr(cp.r(chpos++), Attr);
			p.wpp(set);

		}
	}

	/*
	void MoveCStr(PCell B, int Pos, int Width, const char* Ch, int   A0, int A1, int MaxCount) {
		//unsigned char *p = (unsigned char *) B;
		PCell p = new PCell(B);

		boolean was = 0;
		if (Pos < 0) {
			MaxCount += Pos;
			Ch -= Pos;
			Pos = 0;
		}
		if (Pos >= Width) return;
		if (Pos + MaxCount > Width) MaxCount = Width - Pos;
		if (MaxCount <= 0) return;

		p.shift(Pos);
		for (/*p += sizeof(TCell) * Pos* /; MaxCount > 0 && (*Ch != 0); MaxCount--) {
			if (*Ch == '&' && !was) {
				Ch++;
				MaxCount++;
				was = 1;
				continue;
			} 
			*p++ = (unsigned char) (*Ch++);
			if (was) {
				*p++ = (unsigned char) A1;
				was = 0;
			} else
				*p++ = (unsigned char) A0;
		}
	} */

	void MoveAttr(int Pos, int Width, int /*TAttr*/  Attr, int Count) {
		MoveAttr(this, Pos, Width, Attr, Count);
	}

	static void MoveAttr(PCell B, int Pos, int Width, int /*TAttr*/  Attr, int Count) {
		//unsigned char *p = (unsigned char *) B;
		PCell p = new PCell(B);

		if (Pos < 0) {
			Count += Pos;
			Pos = 0;
		}
		if (Pos >= Width) return;
		if (Pos + Count > Width) Count = Width - Pos;
		if (Count <= 0) return;

		p.shift(Pos);
		for (/*p += sizeof(TCell) * Pos*/; Count > 0; Count--) {
			//p++;
			//*p++ = (unsigned char) Attr;
			long old = p.r();
			long set = charAndAttr(getChar(old), Attr);
			p.wpp(set);
		}
	}


	void MoveBgAttr(int Pos, int Width, int /*TAttr*/  Attr, int Count) {
		MoveBgAttr(this, Pos, Width, Attr, Count);
	}

	static void MoveBgAttr(PCell B, int Pos, int Width, int /*TAttr*/  Attr, int Count) {
		//char *p = (char *) B;
		PCell p = new PCell(B);

		if (Pos < 0) {
			Count += Pos;
			Pos = 0;
		}
		if (Pos >= Width) return;
		if (Pos + Count > Width) Count = Width - Pos;
		if (Count <= 0) return;

		p.shift(Pos);	    
		for (/*p += sizeof(TCell) * Pos*/; Count > 0; Count--) {
			//p++;
			//*p = ((unsigned char)(*p & 0x0F)) | ((unsigned char) Attr);
			//p++;

			long old = p.r();
			int olda = getAttr(old);
			int newa = (olda & 0x0F) | Attr;
			long set = charAndAttr(getChar(old), newa);
			p.wpp(set);

		}
	}



	void MoveCStr(int Pos, int Width, String s, int /*TAttr*/  A0, int /*TAttr*/  A1, int MaxCount) {
		MoveCStr(this, Pos, Width, s, A0, A1, MaxCount);
	}

	static void MoveCStr(PCell B, int Pos, int Width, String s, int /*TAttr*/  A0, int /*TAttr*/  A1, int MaxCount) {
		//unsigned char *p = (unsigned char *) B;
		PCell p = new PCell(B);

		boolean was = false;
		if (Pos < 0) {
			//MaxCount += Pos;
			//Ch -= Pos;
			//Pos = 0;
			throw new RuntimeException("pos < 0 in MoveCStr(String)");
		}

		if (Pos >= Width) return;
		if (Pos + MaxCount > Width) MaxCount = Width - Pos;
		if (MaxCount <= 0) return;

		int Ch = 0;
		int slen = s.length();
		p.shift(Pos);

		for (/*p += sizeof(TCell) * Pos*/; MaxCount > 0 && (Ch < slen); MaxCount--) {
			if (s.charAt(Ch) == '&' && !was) {
				Ch++;
				MaxCount++;
				was = true;
				continue;
			} 
			int at = was ? A1 : A0;
			was = false;

			long set = charAndAttr(s.charAt(Ch), at);
			p.wpp(set);
			Ch++;
			/**p++ = (unsigned char) (*Ch++);
	        if (was) {
			 *p++ = (unsigned char) A1;
	            was = false;
	        } else
			 *p++ = (unsigned char) A0;
			 */
		}
	}





	public static int CStrLen(String s)
	{
		int len = 0;
		boolean was = false;
		for(int i = 0; i < s.length(); i++) {
			len++;
			if (s.charAt(i) == '&' && !was) {
				len--;
				was = true;
			}
			was = false;
		}
		return len;
	}


	public static String UnTabStr(String source) 
	{
		StringBuilder sb = new StringBuilder();
		
		int pos = 0;

		for (int i = 0; i < source.length(); i++) {
			if (source.charAt(i) == '\t') {
				do {
					sb.append( ' ' );
					pos++;
				} while( 0 != (pos & 0x7) );
			} else {
				sb.append( source.charAt(i) );
				pos++;
			}
		}

		return sb.toString();
	}


	
	
	@Override
	public String toString() {
		byte [] res = new byte[length()]; 
		for(int i = 0; i < length(); i++)
		{
			if(null == r(i))
				break;
			res[i] = (byte)(long)r(i);
		}
			
		return new String(res);
	}


}
