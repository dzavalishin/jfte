package ru.dz.jfte;

import java.util.Arrays;

public class Hiliter implements ColorDefs, ConfigDefs, ModeDefs, GuiDefs 
{


	static final int hsPLAIN_Normal = 0;

	/*


	#ifdef NTCONSOLE 
	#    define PCLI unsigned short
	#else
	#    define PCLI unsigned char
	#endif

	#define ColorChar() \
	    do {\
	    BPos = C - Pos; \
	    if (B) \
	    if (BPos >= 0 && BPos < Width) { \
	    BPtr = (PCLI *) (B + BPos); \
	    BPtr[0] = *p; \
	    BPtr[1] = Color; \
	    } \
	    if (StateMap) StateMap[i] = (hsState)(State & 0xFF); \
	    } while (0)

	// MoveChar(B, C - Pos, Width, *p, Color, 1); 
	// if (StateMap) StateMap[i] = State; }

	#define NextChar() do { i++; p++; len--; C++; } while (0)
	#define ColorNext() do { ColorChar(); NextChar(); } while (0)

	#define HILIT_VARS(ColorNormal, Line) \
	    PCLI *BPtr; \
	    int BPos; \
	    ChColor Color = ColorNormal; \
	    int i; \
	    int len = Line->Count; \
	    char *p = Line->Chars; \
	    int NC = 0, C = 0; \
	    int TabSize = BFI(BF, BFI_TabSize); \
	    int ExpandTabs = BFI(BF, BFI_ExpandTabs);

	//#define HILIT_VARS2()
//	    int len1 = len;
//	    char *last = p + len1 - 1;

	#define IF_TAB() \
	    if (*p == '\t' && ExpandTabs) { \
	    NC = NextTab(C, TabSize); \
	    if (StateMap) StateMap[i] = hsState(State);\
	    if (B) MoveChar(B, C - Pos, Width, ' ', Color, NC - C);\
	    if (BFI(BF, BFI_ShowTabs)) ColorChar();\
	    i++,len--,p++;\
	    C = NC;\
	    continue;\
	    }

	 */

	// NB! Actual Hilit_Plain is in EBuffer

	int Hilit_Plain(EBuffer BF, int LN, PCell B, int Pos, int Width, ELine Line, int /*hlState*/ State, int /*hsState*/ [] StateMap, int []ECol) {
		//ChColor *Colors = BF.Mode.fColorize.Colors;
		// TODO int[] Colors = BF.Mode.fColorize.Colors;
		//HILIT_VARS(Colors[CLR_Normal], Line);

		//PCLI BPtr; 
		int BPos; 
		// TOD int /*ChColor*/ Color = Colors[CLR_Normal];
		int Color = hcPlain_Normal;
		//int i; 
		//int len = Line.getCount(); 
		//String p = Line.Chars;
		//int pp = 0;
		int NC = 0, C = 0; 
		//int TabSize = EBuffer.iBFI(BF, BFI_TabSize); 
		//boolean ExpandTabs = EBuffer.BFI(BF, BFI_ExpandTabs);


		/*#ifdef CONFIG_WORD_HILIT
	    int j = 0;

	    if (BF.Mode.fColorize.Keywords.TotalCount > 0 ||
	        BF.WordCount > 0)
	    { //* words have to be hilited, go slow 
	        for(i = 0; i < Line.Count;) {
	            IF_TAB() else {
	                if (isalpha(*p) || (*p == '_')) {
	                    j = 0;
	                    while (((i + j) < Line.Count) &&
	                           (isalnum(Line.Chars[i+j]) ||
	                            (Line.Chars[i + j] == '_'))
	                          ) j++;
	                    if (BF.GetHilitWord(j, Line.Chars + i, Color, 1)) ;
	                    else {
	                        Color = Colors[CLR_Normal];
	                        State = hsPLAIN_Normal;
	                    }
	                    if (StateMap)
	                        memset(StateMap + i, State, j);
	                    if (B)
	                        MoveMem(B, C - Pos, Width, Line.Chars + i, Color, j);
	                    i += j;
	                    len -= j;
	                    p += j;
	                    C += j;
	                    State = hsPLAIN_Normal;
	                    Color = Colors[CLR_Normal];
	                    continue;
	                }
	                ColorNext();
	                continue;
	            }
	        }
	    } else
	#endif */
		/* TOD 
		  if (ExpandTabs) { // use slow mode 
	        for (i = 0; i < Line.getCount();) {
	            //IF_TAB() else {	                ColorNext();	            }

	            if (*p == '\t' && ExpandTabs) { 
	                NC = NextTab(C, TabSize); 
	                if (StateMap) StateMap[i] = hsState(State);
	                if (B) MoveChar(B, C - Pos, Width, ' ', Color, NC - C);
	                if (BFI(BF, BFI_ShowTabs)) ColorChar();
	                i++,len--,p++;
	                C = NC;
	                continue;
	                else {	                ColorNext();	            }

	        }
	    } else */ { /* fast mode */
	    	if (Pos < Line.getCount()) {
	    		if (Pos + Width < Line.getCount()) {
	    			if (B != null) 
	    				//B.MoveMem(0, Width, Line.Chars + Pos, Color, Width);
	    				B.MoveMem(0, Width, Line.Chars, Pos, Color, Width);
	    			if (StateMap != null)
	    				//memset(StateMap, State, Line.getCount());
	    				Arrays.fill(StateMap, 0, Line.getCount(), State);

	    		} else {
	    			if (B != null) 
	    				//B.MoveMem(0, Width, Line.Chars, + Pos, Color, Line.getCount() - Pos);
	    				B.MoveMem(0, Width, Line.Chars, Pos, Color, Line.getCount() - Pos);
	    			if (StateMap != null)
	    				Arrays.fill(StateMap, 0, Line.getCount(), State);
	    			//memset(StateMap, State, Line.getCount());
	    		}
	    	}
	    	C = Line.getCount();
	    }
	    ECol[0] = C;
	    State = 0;
	    return 0;
	}




	static int Indent_Plain(EBuffer B, int Line, int PosCursor) {
		int OI = B.LineIndented(Line);
		B.IndentLine(Line, B.LineIndented(Line - 1));
		if (PosCursor!=0) {
			int I = B.LineIndented(Line);
			int X = B.CP.Col;

			X = X - OI + I;
			if (X < I) X = I;
			if (X < 0) X = 0;
			B.SetPosR(X, Line);
		}
		return 1;
	}














	static class HilitMode
	{
		final String Name;
		final int Num;
		final SyntaxProc Proc;		

		public HilitMode(String n, int id, SyntaxProc p) {
			Name = n;
			Num = id;
			Proc = p;
		}
	}

	//private static Object oHilit_Plain;

	static HilitMode HilitModes[] = {
			new HilitMode( "PLAIN", HILIT_PLAIN, EBuffer::Hilit_Plain ),

			/* TODO hilit proc
			new HilitMode( "C", HILIT_C, Hilit_C ),
			new HilitMode( "REXX", HILIT_REXX, Hilit_REXX ),

			new HilitMode( "HTML", HILIT_HTML, Hilit_HTML ),

			new HilitMode( "MAKE", HILIT_MAKE, Hilit_MAKE ),

			new HilitMode( "DIFF", HILIT_DIFF, Hilit_DIFF ),

			new HilitMode( "MERGE", HILIT_MERGE, Hilit_MERGE ),

			new HilitMode( "IPF", HILIT_IPF, Hilit_IPF ),

			new HilitMode( "Ada", HILIT_ADA, Hilit_ADA ),

			new HilitMode( "MSG", HILIT_MSG, Hilit_MSG ),

			new HilitMode( "SH", HILIT_SH, Hilit_SH ),

			new HilitMode( "PASCAL", HILIT_PASCAL, Hilit_PASCAL ),

			new HilitMode( "TEX", HILIT_TEX, Hilit_TEX ),

			new HilitMode( "FTE", HILIT_FTE, Hilit_FTE ),

			new HilitMode( "CATBS", HILIT_CATBS, Hilit_CATBS ),

			new HilitMode( "SIMPLE", HILIT_SIMPLE, Hilit_SIMPLE ),
			 */
	};





	static SyntaxProc GetHilitProc(int id) {
		for (HilitMode i : HilitModes)
			if (id == i.Num)
				return i.Proc;

		// TODO hack plain hiliter 
		return HilitModes[0].Proc;

		//return null;
	}

	static int GetHilitMode(String Str) {
		for (HilitMode i : HilitModes)
			if (i.Name.equals(Str))
				return i.Num;

		return HILIT_PLAIN;
	}











	static int GetIndentMode(String Str) 
	{
		/*
	    for (unsigned int i = 0; i < sizeof(IndentModes) / sizeof(IndentModes[0]); i++)
	        if (strcmp(Str, IndentModes[i].Name) == 0)
	            return IndentModes[i].Num;
		 */

		switch(Str)
		{
		case "C": return  INDENT_C ;
		case "REXX": return  INDENT_REXX ;
		case "SIMPLE": return  INDENT_REXX ;
		case "PLAIN": return  INDENT_PLAIN ;

		}

		return INDENT_PLAIN;
	}



}
