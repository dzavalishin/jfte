package ru.dz.jfte;

public interface BufferDefs {

	static final int  bmLine    =0;
	static final int  bmStream  =1;
	static final int  bmColumn  =2;

	static final int  E_OK          =0;   // all ok
	static final int  E_CANCEL      =1;   // operation cancelled
	static final int  E_ERROR       =2;   // error
	static final int  E_NOMEM       =3;   // out of memory

	static final int  umDelete      =0;
	static final int  umInsert      =1;
	static final int  umSplitLine   =2;
	static final int  umJoinLine    =3;

	static final int  tmNone        =0;
	static final int  tmLeft        =1;
	static final int  tmRight       =2;

	//typedef unsigned char TransTable[256];

	static final int  RWBUFSIZE     = 32768;


	default int ChClass(char x) { return (WGETBIT(Flags.WordChars, (x)) ? 1 : 0); }
	default int ChClassK(char x) { return ((x == ' ') || (x == (char)9)) ? 2 : ChClass(x); }
	    
	default boolean InRange(int a, int x, int b) { return  (((a) <= (x)) && ((x) < (b))); }
	
	//static final int  Min(a,b) (((a) < (b))?(a):(b))
	//static final int  Max(a,b) (((a) > (b))?(a):(b))

	default int NextTab(int pos, int ts) { return  (((pos) / (ts) + 1) * (ts)); }

	// x before gap -> x
	// x less than count -> after gap
	// count - 1 before gap -> count - 1
	// after gap -> allocated - 1
	default int GapLine(int x,int g,int c, int a) { return (((x) < (g)) ? (x) : (x) < (c) ? ((x) + (a) - (c)) : (c) - 1 < (g) ? (c) - 1 : (a) - 1 ); }


	static final int  CHAR_TRESHOLD  = 0x3;







	
}
