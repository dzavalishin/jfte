package ru.dz.jfte;

import ru.dz.jfte.c.CString;

public class CLanguage 
{

	static final int  hsC_Normal     =  0;
	static final int  hsC_Comment    =  1;
	static final int  hsC_CommentL   =  2;
	static final int  hsC_Keyword    =  4;
	static final int  hsC_String1    = 10;
	static final int  hsC_String2    = 11;
	static final int  hsC_CPP        = 12;
	static final int  hsC_CPP_Comm   = 13;
	static final int  hsC_CPP_String1= 14;
	static final int  hsC_CPP_String2= 15;
	static final int  hsC_CPP_ABrace = 16;

	static final int   SKIP_FORWARD = 0;
	static final int   SKIP_BACK    = 1;
	static final int   SKIP_MATCH   = 2;
	static final int   SKIP_LINE    = 4;
	static final int   SKIP_TOBOL   = 8;

	

	static final int  FIND_IF        = 0x0001;
	static final int  FIND_SEMICOLON = 0x0002;
	static final int  FIND_COMMA     = 0x0004;
	static final int  FIND_COLON     = 0x0008;
	static final int  FIND_ELSE      = 0x0010;
	static final int  FIND_FOR       = 0x0020;
	static final int  FIND_WHILE     = 0x0040;
	static final int  FIND_ENDBLOCK  = 0x0080;
	//static final int  FIND_BEGINBLOCK= 0x0100;
	static final int  FIND_CLASS     = 0x0200;
	static final int  FIND_CASE      = 0x0400;
	static final int  FIND_SWITCH    = 0x0800;
	static final int  FIND_QUESTION  = 0x1000;
	
	
	static int C_Indent = 4;
	static int C_BraceOfs = 0;
	static int C_ParenDelta = -1;
	static int C_CaseOfs = 0;
	static int C_CaseDelta = 4;
	static int C_ClassOfs = 0;
	static int C_ClassDelta = 4;
	static int C_ColonOfs = 0;//-4;
	static int C_CommentOfs = 0;
	static int C_CommentDelta = 1;
	static int C_FirstLevelWidth = -1;
	static int C_FirstLevelIndent = 4;
	static int C_Continuation = 4;
	static int FunctionUsesContinuation = 0;
	
	
	
	
	
	
	
	

	static int Indent_C(EBuffer B, int Line, int PosCursor) {
		int I;
		int OI;

		int [][] aStateMap = new int[1][];
		int [] aStateLen = {0};

		int [] StateMap;
		int StateLen;

		OI = I = B.LineIndented(Line);
		if (Line == 0) {
			I = 0;
		} else {
			if (I != 0) B.IndentLine(Line, 0);
			if (!B.GetMap(Line, aStateLen, aStateMap)) return 0;
			StateLen = aStateLen[0];
			StateMap = aStateMap[0];

			switch (B.RLine(Line - 1).StateE) {
			case hsC_Comment:
			case hsC_CPP_Comm:
				I = IndentComment(B, Line, StateLen, StateMap);
				break;
			case hsC_CPP:
				/*case hsC_CPP_Comm:*/
			case hsC_CPP_String1:
			case hsC_CPP_String2:
			case hsC_CPP_ABrace:
				I = C_Indent;
				break;
			default:
				if (StateLen > 0) {                 // line is not empty
					if (StateMap[0] == hsC_CPP || StateMap[0] == hsC_CPP_Comm ||
							StateMap[0] == hsC_CPP_String1 || StateMap[0] == hsC_CPP_String2 ||
							StateMap[0] == hsC_CPP_ABrace)
					{
						I = IndentCPP(B, Line, StateLen, null);
					} else {
						I = IndentNormal(B, Line, StateLen, StateMap);
						if ((StateMap[0] == hsC_Comment
								|| StateMap[0] == hsC_CommentL
								|| StateMap[0] == hsC_CPP_Comm)
								&& ((LookAt(B, Line, 0, "/*", hsC_Comment, 0)
										|| LookAt(B, Line, 0, "/*", hsC_CPP_Comm, 0)
										|| LookAt(B, Line, 0, "//", hsC_CommentL, 0))))
						{
							I += C_CommentOfs;
						} else if (CheckLabel(B, Line)) {
							if (LookAt(B, Line, 0, "case", hsC_Keyword) ||
									LookAt(B, Line, 0, "default", hsC_Keyword) ||
									LookAt(B, Line, 0, "public:", hsC_Keyword, 0) ||
									LookAt(B, Line, 0, "private:", hsC_Keyword, 0) ||
									LookAt(B, Line, 0, "protected:", hsC_Keyword, 0))
								;
							else
								I += C_ColonOfs;
						} //else if (LookAt(B, Line, 0, "{", hsC_Normal, 0)) {
						//    I -= C_Indent - C_BraceOfs;
						//}
					}
				} else {
					I = IndentNormal(B, Line, 0, null);
				}
				break;
			}
		}

		if (I >= 0)
			B.IndentLine(Line, I);
		else
			I = 0;
		if (PosCursor == 1) {
			int X = B.CP.Col;

			X = X - OI + I;
			if (X < I) X = I;
			if (X < 0) X = 0;
			if (X > B.LineLen(Line)) {
				X = B.LineLen(Line);
				if (X < I) X = I;
			}
			if (!B.SetPosR(X, Line)) return 0;
		} else if (PosCursor == 2) {
			if (!B.SetPosR(I, Line)) return 0;
		}
		return 1;
	}



	static int IndentCPP(EBuffer B, int Line, int StateLen, int [] StateMap) {
		if (LookAt(B, Line, 0, "#", hsC_CPP, 0))
			return 0;
		else
			return C_Indent;
	}


	static boolean LookAt(EBuffer B, int Row, int Pos, String What, int State, int NoWord ) { 
		return LookAt(B, Row, Pos, What, State, NoWord, false ); 
	}

	static boolean LookAt(EBuffer B, int Row, int Pos, String What, int State) { 
		return LookAt(B, Row, Pos, What, State, 1, false); 
	}	

	static boolean LookAt(EBuffer B, int Row, int Pos, String What, int State, int NoWord, boolean CaseInsensitive) 
	{

		int Len = What.length();

		if (Row < 0 || Row >= B.RCount) {
			//LOG << "Row out of range: " << Row << " vs " << B.RCount << ENDLINE;
			return false;
		}
		String  pLine       = B.RLine(Row).Chars.toString();
		int uLineLength = B.RLine(Row).getCount();

		Pos = B.CharOffset(B.RLine(Row), Pos);

		if (Pos + What.length() > uLineLength) { return false; }

		if (NoWord != 0 && uLineLength > Pos + Len && ISNAME(pLine.charAt(Pos + Len)))
		{
			return false;
		}
		//LOG << "Check against [" << What << ']' << ENDLINE;
		if (
				(CaseInsensitive && CString.memicmp(pLine + Pos, (String )What, Len) == 0) ||
				(!CaseInsensitive && CString.memcmp(pLine + Pos, (String )What, Len) == 0)
				)
		{
			return true;
		}
		else
		{
			return false;
		}
	}

	static boolean ISNAME(char x) {
		return Character.isAlphabetic(x) || Character.isDigit(x) || (x == '_');
	}



	static int IndentComment(EBuffer B, int Row, int StateLen, int [] /*hsState * */  StateMap) {
		int I = 0, R;

		//puts("Comment");

		if (Row > 0) {
			R = Row - 1;
			while (R >= 0) {
				if (B.RLine(R).getCount() == 0) R--;
				else {
					I = B.LineIndented(R);
					break;
				}
			}
			if (B.RLine(Row - 1).StateE == hsC_Comment)
				if (LookAt(B, Row - 1, I, "/*", hsC_Comment, 0)) I += C_CommentDelta;
			if (B.RLine(Row - 1).StateE == hsC_CPP_Comm)
				if (LookAt(B, Row - 1, I, "/*", hsC_CPP_Comm, 0)) I += C_CommentDelta;
		}
		return I;
	}


	static int IndentNormal(EBuffer B, int Line, int StateLen, int [] /*hsState * */  StateMap) {
		//STARTFUNC("IndentNormal{h_c.cpp}");
		int I = 0;
		int [] Pos = {0};
		int [] L = {0};

		if (LookAt(B, Line, 0, "case", hsC_Keyword) ||
				LookAt(B, Line, 0, "default", hsC_Keyword))
		{
			I = SearchBackMatch(-1, B, Line - 1, hsC_Normal, "{", "}", Pos, L) + C_CaseOfs;
			return I;
		} else if (LookAt(B, Line, 0, "public:", hsC_Keyword, 0) ||
				LookAt(B, Line, 0, "private:", hsC_Keyword, 0) ||
				LookAt(B, Line, 0, "protected:", hsC_Keyword, 0))
		{
			I = SearchBackMatch(-1, B, Line - 1, hsC_Normal, "{", "}", Pos, L) + C_ClassOfs;
			return I;
		} else if (LookAt(B, Line, 0, "else", hsC_Keyword)) {
			I = SearchBackMatch(-1, B, Line - 1, hsC_Keyword, "if", "else", Pos, L, 1);
			return I;
		} else if (LookAt(B, Line, 0, "}", hsC_Normal, 0)) {
			I = SearchBackMatch(-1, B, Line - 1, hsC_Normal, "{", "}", Pos, L, 0, 1);
			return I;
		} else if (LookAt(B, Line, 0, ")", hsC_Normal, 0)) {
			I = SearchBackMatch(-1, B, Line - 1, hsC_Normal, "(", ")", Pos, L);
			if (C_ParenDelta >= 0)
				return I + C_ParenDelta;
			else
				return Pos[0];
		} else if (LookAt(B, Line, 0, "]", hsC_Normal, 0)) {
			I = SearchBackMatch(-1, B, Line - 1, hsC_Normal, "[", "]", Pos, L);
			if (C_ParenDelta >= 0)
				return I + C_ParenDelta;
			else
				return Pos[0];
		} else {
			char [] CharP = {' '};
			// char FirstCharP = ' ';
			int [] RowP = {Line};
			int [] ColP = {-1};
			int [] PrevRowP = { RowP[0] };
			int [] PrevColP = { ColP[0] };
			int FirstRowP;
			int FirstColP;
			int ContinuationIndent = 0;

			if (SkipWhite(B, Line, PrevRowP, PrevColP, SKIP_BACK) != 1)
				return 0;

			PrevColP[0]++;
			//LOG << "PrevRowP=" << PrevRowP << ", PrevColP=" << PrevColP << ENDLINE;

			if (FindPrevIndent(B, RowP, ColP, CharP,
					FIND_IF |
					FIND_ELSE |
					FIND_FOR |
					FIND_WHILE |
					FIND_SWITCH |
					FIND_CASE |
					//FIND_CLASS |
					FIND_COLON |
					FIND_SEMICOLON |
					FIND_COMMA |
					FIND_ENDBLOCK) != 1)
			{
				if (RowP[0] != PrevRowP[0])
					ContinuationIndent = C_Continuation;
				I = 0;
				if (LookAt(B, Line, 0, "{", hsC_Normal, 0)) {
					I += C_BraceOfs;
					ContinuationIndent = 0;
				}
				return I + ContinuationIndent;
			}

			FirstRowP = RowP[0];
			FirstColP = ColP[0];
			// FirstCharP = CharP;

			//LOG << "FirstRowP=" << FirstRowP << ", FirstColP=" << FirstColP <<			", CharP=" << BinChar(CharP) << ENDLINE;

			switch (CharP[0]) {
			case 'c':
				I = B.LineIndented(RowP[0]) + C_Continuation;
				return I;

			case '(':
			case '[':
				if (C_ParenDelta >= 0) {
					I = B.LineIndented(FirstRowP) + C_ParenDelta;
				} else {
					ColP[0]++;
					if (SkipWhite(B, Line, RowP, ColP, SKIP_FORWARD | SKIP_LINE) != 1)
						return 0;
					if (ColP[0] < B.LineChars(RowP[0]) || 0 == FunctionUsesContinuation) {
						char [] strLeft = { CharP[0], 0 };
						char [] strRight = { CharP[0] == '(' ? ')' : ']', 0 };
						I = SearchBackMatch(-1, B, Line - 1, hsC_Normal, strLeft.toString(), strRight.toString(), Pos, L);
						I = Pos[0] + 1;
					} else {
						I = B.LineIndented(RowP[0]) + C_Continuation;
					}
				}
				return I;

			case '{':
				ColP[0]++;
				if (((PrevRowP[0] != RowP[0]) ||
						((PrevRowP[0] == RowP[0]) && (PrevColP[0] != ColP[0])))
						&& FirstRowP != PrevRowP[0])
					ContinuationIndent = C_Continuation;
				ColP[0]--; ColP[0]--;
				if (SkipWhite(B, Line, RowP, ColP, SKIP_BACK | SKIP_TOBOL | SKIP_MATCH) != 1)
					return 0;
				I = B.LineIndented(RowP[0]);
				if (B.LineIndented(FirstRowP) <= C_FirstLevelWidth)
					I += C_FirstLevelIndent;
				else
					I += C_Indent;
				//PRINTF(("'{' indent : Line=%d, RowP=%d, ColP=%d, CharP=%c\n", Line, RowP, ColP, CharP[0]));

				if (LookAt(B, Line, 0, "{", hsC_Normal, 0))
					I -= C_BraceOfs;
				else
					I += ContinuationIndent;

				return I;
			case ',':
				I = B.LineIndented(FirstRowP);
				return I;
			case '}':
				ColP[0]++;
				ColP[0]++;
				/*---nobreak---*/
			case ';':
				ColP[0]--;
				if (FindPrevIndent(B, RowP, ColP, CharP,
						((CharP[0] == ',') ? FIND_COMMA | FIND_COLON :
							//(CharP == ';') ? FIND_SEMICOLON | FIND_COLON :
							FIND_SEMICOLON | FIND_COLON)) != 1)
				{
					if (FirstRowP != PrevRowP[0])
						ContinuationIndent = C_Continuation;
					I = 0;
					if (LookAt(B, Line, 0, "{", hsC_Normal, 0)) {
						I += C_BraceOfs;
						ContinuationIndent = 0;
					}
					return I + ContinuationIndent;
				}
				//PRINTF(("';' Line=%d, RowP=%d, ColP=%d, CharP=%c\n", Line, RowP[0], ColP, CharP));

				//LOG << "  CharP now: " << BinChar(CharP) << ENDLINE;
				switch (CharP[0]) {
				case ',':
				case ';':
				case '{':
				case ':':
					ColP[0]++;
					if (SkipWhite(B, Line, RowP, ColP, SKIP_FORWARD) != 1)
						return 0;
					//ColP[0]--;
					//if (SkipWhite(B, RowP, ColP, SKIP_BACK) != 1)
					//if (CharP[0] == ':') {
					//    I -= C_ColonOfs;
					//}
					//PRINTF(("';' indent : Line=%d, RowP=%d, ColP=%d, CharP=%c\n", Line, RowP, ColP, CharP[0]));
					I = B.LineIndented(RowP[0]);
					if (((PrevRowP[0] != RowP[0]) ||
							((PrevRowP[0] == RowP[0]) && (PrevColP[0] != ColP[0])))
							&& FirstRowP != PrevRowP[0])
						ContinuationIndent = C_Continuation;

					if (LookAt(B, Line, 0, "{", hsC_Normal, 0)) {
						//I -= C_BraceOfs;
						ContinuationIndent = 0;
					}
					if (LookAt(B, Line, 0, "{", hsC_Normal, 0)
							&& LookAt(B, RowP[0], ColP[0], "{", hsC_Normal, 0))
						I -= 0; //C_BraceOfs;
					else if (LookAt(B, Line, 0, "{", hsC_Normal, 0)
							&& !LookAt(B, RowP[0], ColP[0], "{", hsC_Normal, 0))
						I += C_BraceOfs;
					else if (!LookAt(B, Line, 0, "{", hsC_Normal, 0)
							&& LookAt(B, RowP[0], ColP[0], "{", hsC_Normal, 0))
						I -= C_BraceOfs;
					break;
				case '(':
					ColP[0]++;
					if (SkipWhite(B, Line, RowP, ColP, SKIP_FORWARD | SKIP_LINE) != 1)
						return 0;
					I = B.ScreenPos(B.RLine(RowP[0]), ColP[0]);
					break;
				default:
					I = B.LineIndented(RowP[0]);
					break;
				}
				//PRINTF(("';' -- indent : Line=%d, RowP=%d, ColP=%d, CharP=%c\n", Line, RowP[0], ColP, CharP[0]));

				//            else
				//            if (LookAt(B, Line, 0, "{", hsC_Normal, 0))
				//                I += C_Indent - C_BraceOfs;

				return I + ContinuationIndent;

			case ':':
				ColP[0]--;
				if (FindPrevIndent(B, RowP, ColP, CharP, FIND_SEMICOLON | FIND_COLON | FIND_QUESTION | FIND_CLASS | FIND_CASE) != 1) {
					if (FirstRowP != PrevRowP[0])
						ContinuationIndent = C_Continuation;
					return 0 + ContinuationIndent;
				}

				//PRINTF(("':' Line=%d, RowP=%d, ColP=%d, CharP=%c\n", Line, RowP, ColP, CharP));

				switch (CharP[0]) {
				case ':':
					//ColP++;
					/*if (SkipWhite(B, Line, RowP, ColP, SKIP_FORWARD) != 1)
	                 return 0;
	                 I = B.LineIndented(RowP[0]);// - C_ColonOfs;
	                 PRINTF(("':' 0 indent : Line=%d, RowP=%d, ColP=%d, CharP=%c\n", Line, RowP[0], ColP, CharP[0]));
	                 break;*/
				case '{':
				case ';':
					ColP[0]++;
					if (SkipWhite(B, Line, RowP, ColP, SKIP_FORWARD) != 1)
						return 0;
					I = B.LineIndented(RowP[0]);
					//PRINTF(("!!! FirstRowP=%d, PrevRowP=%d, RowP=%d, I=%d\n", FirstRowP, PrevRowP, RowP[0], I));
					//PRINTF(("!!! FirstColP=%d, PrevColP=%d, ColP=%d\n", FirstColP, PrevColP, ColP));
					if (CheckLabel(B, RowP[0]))
						I -= C_ColonOfs;
					else if (PrevRowP[0] == RowP[0] && FirstRowP == PrevRowP[0] && FirstColP + 1 == PrevColP[0])
						I += C_Continuation;
					if (LookAt(B, Line, 0, "{", hsC_Normal, 0)
							&& LookAt(B, RowP[0], ColP[0], "{", hsC_Normal, 0))
						I -= 0;//C_BraceOfs;
					else if (LookAt(B, Line, 0, "{", hsC_Normal, 0)
							&& !LookAt(B, RowP[0], ColP[0], "{", hsC_Normal, 0))
						I += C_BraceOfs;
					else if (!LookAt(B, Line, 0, "{", hsC_Normal, 0)
							&& LookAt(B, RowP[0], ColP[0], "{", hsC_Normal, 0))
						I -= C_BraceOfs;
					//PRINTF(("':' 1 indent : Line=%d, RowP=%d, ColP=%d, CharP=%c\n", Line, RowP[0], ColP, CharP[0]));
					break;
				case 'p':
					ColP[0]++;
					//if (SkipWhite(B, Line, RowP, ColP, SKIP_FORWARD) != 1)
					//                    return 0;
					I = B.LineIndented(RowP[0]) + C_ClassDelta;
					//                if (FirstRowP == RowP) {
					//                    I += C_ClassDelta;
					///                if (LookAt(B, Line, 0, "{", hsC_Normal, 0)) {
					///                    I += C_Indent - C_BraceOfs;
					///                }
					//                }
					//PRINTF(("':' 2 indent : Line=%d, RowP=%d, ColP=%d, CharP=%c\n", Line, RowP, ColP, CharP[0]));
					break;
				case 'l':
					ColP[0]++;
					I = B.LineIndented(RowP[0]) + C_BraceOfs;
					//C_ClassOfs + C_ClassDelta;
					break;
				case 'c':
					ColP[0]++;
					//                if (SkipWhite(B, Line, RowP[0], ColP, SKIP_FORWARD) != 1)
					//                    return 0;
					I = B.LineIndented(RowP[0]) + C_CaseDelta;
					//                if (FirstRowP == RowP[0]) {
					//                    I += C_CaseDelta;
					///                if (LookAt(B, Line, 0, "{", hsC_Normal, 0)) {
					///                        I += C_Indent - C_BraceOfs;
					///                }
					//                }

					//PRINTF(("':' 3 indent : Line=%d, RowP=%d, ColP=%d, CharP=%c\n", Line, RowP[0], ColP, CharP[0]));
					break;
				default:
					I = B.LineIndented(RowP[0]);
					break;
				}
				if (((PrevRowP[0] != RowP[0]) ||
						((PrevRowP[0] == RowP[0]) && (PrevColP[0] != ColP[0])))
						&& FirstRowP != PrevRowP[0])
					ContinuationIndent = C_Continuation;
				if (LookAt(B, Line, 0, "{", hsC_Normal, 0)) {
					//I -= C_Indent - C_BraceOfs;
					ContinuationIndent = 0;
				}
				//PRINTF(("':' -- indent : Line=%d, RowP=%d, ColP=%d, CharP=%c\n", Line, RowP[0], ColP, CharP[0]));
				return I + ContinuationIndent;

			case 'i':
			case 's':
			case 'f':
			case 'e':
			case 'w':
				I = B.LineIndented(RowP[0]);
				switch (CharP[0]) {
				case 'i': ColP[0] += 2; break; // if
				case 'f': ColP[0] += 3; break; // for
				case 'e': ColP[0] += 4; break; // else
				case 'w': ColP[0] += 5; break; // while
				case 's': ColP[0] += 6; break;
				}
				//PRINTF(("'ifews' -- indent 1: Line=%d, RowP=%d, ColP[0]=%d, CharP=%c\n", Line, RowP[0], ColP, CharP[0]));
				
				if (SkipWhite(B, Line, RowP, ColP, SKIP_FORWARD | (CharP[0] != 'e' ? SKIP_MATCH : 0)) != 1)
					return 0;
				
				if (RowP[0] >= Line) {
					RowP[0] = Line;
					ColP[0] = -1;
				} else
					ColP[0]--;
				if (SkipWhite(B, Line, RowP, ColP, SKIP_BACK) != 1)
					return 0;
				ColP[0]++;
				//PRINTF(("'ifews' -- indent 2: Line=%d, RowP=%d, ColP=%d, CharP=%c\n", Line, RowP, ColP[0], CharP[0]));

				if (((PrevRowP[0] != RowP[0]) ||
						((PrevRowP[0] == RowP[0]) && (PrevColP[0] != ColP[0])))
						&& FirstRowP != PrevRowP[0])
					ContinuationIndent = C_Continuation;

				I += C_Indent;

				if (LookAt(B, Line, 0, "{", hsC_Normal, 0)) {
					I -= C_Indent - C_BraceOfs;
					ContinuationIndent = 0;
				}

				return I + ContinuationIndent;

				// default: return 0;
			}
		}
		return 0;
	}

	static boolean CheckLabel(EBuffer B, int Line) {
		ELine L = B.RLine(Line);
		int P = B.CharOffset(L, B.LineIndented(Line));
		int Cnt = 0;

		if (Line > 0 && B.RLine(Line - 1).StateE != hsC_Normal)
			return false;

		while ((P < L.getCount()) &&
				(L.Chars.charAt(P) == ' ' || L.Chars.charAt(P) == 9)) P++;
		while (P < L.getCount()) {
			if (Cnt > 0)
				if (L.Chars.charAt(P) == ':' && (Cnt == 1 || L.Chars.charAt(P + 1) != ':')) return true;
			if (!CString.isalnum(L.Chars.charAt(P)) && L.Chars.charAt(P) != '_') return false;
			Cnt++;
			P++;
		}
		return false;
	}


	static int SearchBackMatch(int Count, EBuffer B, int Row, int /*hsState*/ State, String Open, String Close, int [] OPos, int [] OLine, int matchparens) 
	{ 
		return SearchBackMatch(Count, B, Row, State, Open, Close, OPos,  OLine,  matchparens , 0); 
	}

	static int SearchBackMatch(int Count, EBuffer B, int Row, int /*hsState*/ State, String Open, String Close, int [] OPos, int [] OLine) { 
		return SearchBackMatch(Count, B, Row, State, Open, Close, OPos, OLine, 0, 0); 
	}

	static int SearchBackMatch(int Count, EBuffer B, int Row, int /*hsState*/ State, String Open, String Close, int [] OPos, int [] OLine, int matchparens, int bolOnly) 
	{
		String P;
		int L;
		int Pos;
		int LOpen = Open.length();
		int LClose = Close.length();
		int StateLen;
		int [] /*hsState * */ StateMap;
		int CountX[] = { 0, 0, 0 };
		int didMatch = 0;

		OLine[0] = Row;
		OPos[0] = 0;
		while (Row >= 0) {
			P = B.RLine(Row).Chars.toString();
			L = B.RLine(Row).getCount();
			StateMap = null;

			int [][] aStateMap = new int[1][];
			int [] aStateLen = {0};
			if (!B.GetMap(Row, aStateLen, aStateMap)) return -1;
			StateLen = aStateLen[0];
			StateMap = aStateMap[0];

			Pos = L - 1;
			if (L > 0) while (Pos >= 0) {
				if (P.charAt(Pos) != ' ' && P.charAt(Pos) != 9) {
					if (StateMap[Pos] == hsC_Normal) {
						switch (P.charAt(Pos)) {
						case '{': CountX[0]--; break;
						case '}': CountX[0]++; break;
						case '(': CountX[1]--; break;
						case ')': CountX[1]++; break;
						case '[': CountX[2]--; break;
						case ']': CountX[2]++; break;
						}
					}
					if (0==matchparens ||
							(CountX[0] == 0 && CountX[1] == 0 && CountX[2] == 0))
					{
						if (LOpen + Pos <= L) {
							if (IsState(StateMap, Pos, State, LOpen)) {
								if (CString.memcmp(P + Pos, Open, LOpen) == 0) Count++;
								if (Count == 0) {
									if (bolOnly!=0)
										didMatch = 1;
									else {
										OPos[0] = B.ScreenPos(B.RLine(Row), Pos);
										OLine[0] = Row;

										return B.LineIndented(Row);
									}
								}
							}
							if (LClose + Pos <= L) {
								if (IsState(StateMap, Pos, State, LClose)) {
									if (CString.memcmp(P + Pos, Close, LClose) == 0) Count--;
								}
							}
						}
					}
				}
				Pos--;
			}
			if ((0 != bolOnly) && (0!=didMatch) && CountX[1] == 0 && CountX[2] == 0) {
				OPos[0] = 0;
				OLine[0] = Row;

				return B.LineIndented(Row);
			}

			Row--;
		}
		return -1;
	}

	private static int FindPrevIndent(EBuffer B, int [] RowP, int [] ColP, char [] CharP, int Flags) {
		//STARTFUNC("FindPrevIndent{h_c.cpp}");
		//LOG << "Flags: " << hex << Flags << dec << ENDLINE;
		//int StateLen;
		int [] /*hsState * */ StateMap = null;
		String P;
		int L;
		int Count[] = {
				0, // { }
				0, // ( )
				0, // [ ]
				0, // if/else (one if for each else)
		};

		assert(RowP[0] >= 0 && RowP[0] < B.RCount);
		L = B.RLine(RowP[0]).getCount();
		if (ColP[0] >= L)
			ColP[0] = L - 1;
		assert(ColP[0] >= -1 && ColP[0] <= L);

		char BolChar = ' ';
		int BolCol = -1;
		int BolRow = -1;

		while (RowP[0] >= 0) {

			P = B.RLine(RowP[0]).Chars.toString();
			L = B.RLine(RowP[0]).getCount();
			StateMap = null;
			int [][] aStateMap = new int[1][];
			int [] aStateLen = {0};
			if (!B.GetMap(RowP[0], aStateLen, aStateMap))
			{
				//LOG << "Can't get state maps" << ENDLINE;
				return (0);
			}
			//StateLen = aStateLen[0];
			StateMap = aStateMap[0];
			
			if (L > 0) while (ColP[0] >= 0) {
				//LOG << "ColP[0]: " << ColP[0] << " State: " << (int)StateMap[ColP[0]] << ENDLINE;
				if (StateMap[ColP[0]] == hsC_Normal) {
					//LOG << "CharP[0]: " << BinChar(P[ColP[0]]) << " BolChar: " << BinChar(BolChar) <<	                    " BolRow: " << BolRow <<	                    " BolCol: " << BolCol <<	                    ENDLINE;
					switch (CharP[0] = P.charAt(ColP[0])) {
					case '{':
						if (BolChar == ':' || BolChar == ',') {
							CharP[0] = BolChar;
							ColP[0] = BolCol;
							RowP[0] = BolRow;

							return (1);
						}
						if (isZeroArray(Count)) {

							return (1);
						}
						Count[0]--;
						break;
					case '}':
						if (BolChar == ':' || BolChar == ',') {
							CharP[0] = BolChar;
							ColP[0] = BolCol;
							RowP[0] = BolRow;

							return (1);
						}
						if (BolChar == ';') {
							CharP[0] = BolChar;
							ColP[0] = BolCol;
							RowP[0] = BolRow;

							return (1);
						}
						if (ColP[0] == 0) { /* speed optimization */

							return (1);
						}
						if (isZeroArray(Count) && 0 != (Flags & FIND_ENDBLOCK)) {

							return (1);
						}
						Count[0]++;
						break;
					case '(':
						if (isZeroArray(Count)) {

							return (1);
						}
						Count[1]--;
						break;
					case ')':
						Count[1]++;
						break;
					case '[':
						if (isZeroArray(Count)) {

							return (1);
						}
						Count[2]--;
						break;
					case ']':
						Count[2]++;
						break;
					case ':':
						if (ColP[0] >= 1 && P.charAt(ColP[0] - 1) == ':') { // skip ::
							ColP[0] -= 2;
							continue;
						}
					case ',':
					case ';':
						if (isZeroArray(Count) && BolChar == ' ') {
							if ((CharP[0] == ';' && 0 != (Flags & FIND_SEMICOLON))
									|| (CharP[0] == ',' && 0 != (Flags & FIND_COMMA))
									|| (CharP[0] == ':' && 0 !=(Flags & FIND_COLON))) {
								BolChar = CharP[0];
								BolCol = ColP[0];
								BolRow = RowP[0];
								// this should be here
								// if not say why ???

								//return 1;
							}
						}
						if (BolChar == ',' && CharP[0] == ':') {
							BolChar = ' ';
							BolCol = -1;
							BolRow = -1;
							break;
						}
						if ((BolChar == ':' || BolChar == ',')
								&& (CharP[0] == ';'/* || CharP[0] == ','*/)) {
							CharP[0] = ':';
							ColP[0] = BolCol;
							RowP[0] = BolRow;

							return (1);
						}
						break;
					case '?':
						//if ((Flags & FIND_QUESTION)) {
						if (BolChar == ':' || BolChar == ',') {
							BolChar = ' ';
							BolCol = -1;
							BolRow = -1;
						}
						//}
						break;
					}
				} else if (StateMap[ColP[0]] == hsC_Keyword && (BolChar == ' ' || BolChar == ':')) {
					if (L - ColP[0] >= 2 &&
							IsState(StateMap, ColP[0], hsC_Keyword, 2) &&
							CString.memcmp(P + ColP[0], "if", 2) == 0)
					{
						//puts("\nif");
						if (Count[3] > 0)
							Count[3]--;
						if(0 != (Flags & FIND_IF)) {
							if (isZeroArray(Count)) {
								CharP[0] = 'i';

								return (1);
							}
						}
					}
					if (L - ColP[0] >= 4 &&
							IsState(StateMap, ColP[0], hsC_Keyword, 4) &&
							CString.memcmp(P + ColP[0], "else", 4) == 0)
					{
						//puts("\nelse\x7");
						if(0 != (Flags & FIND_ELSE)) {
							if (isZeroArray(Count)) {
								CharP[0] = 'e';

								return (1);
							}
						}
						Count[3]++;
					}
					if (isZeroArray(Count)) {

						if (0 != (Flags & FIND_FOR) &&
								L - ColP[0] >= 3 &&
								IsState(StateMap, ColP[0], hsC_Keyword, 3) &&
								CString.memcmp(P + ColP[0], "for", 3) == 0)
						{
							CharP[0] = 'f';

							return (1);
						}
						if (0 != (Flags & FIND_WHILE) &&
								L - ColP[0] >= 5 &&
								IsState(StateMap, ColP[0], hsC_Keyword, 5) &&
								CString.memcmp(P + ColP[0], "while", 5) == 0)
						{
							CharP[0] = 'w';

							return (1);
						}
						if (0 != (Flags & FIND_SWITCH) &&
								L - ColP[0] >= 6 &&
								IsState(StateMap, ColP[0], hsC_Keyword, 6) &&
								CString.memcmp(P + ColP[0], "switch", 6) == 0)
						{
							CharP[0] = 's';

							return (1);
						}
						if ((0 != (Flags & FIND_CASE) || (BolChar == ':')) &&
								(L - ColP[0] >= 4 &&
								IsState(StateMap, ColP[0], hsC_Keyword, 4) &&
								CString.memcmp(P + ColP[0], "case", 4) == 0) ||
								((L - ColP[0] >= 7) &&
										IsState(StateMap, ColP[0], hsC_Keyword, 7) &&
										CString.memcmp(P + ColP[0], "default", 7) == 0))
						{
							CharP[0] = 'c';
							if (BolChar == ':') {
								CharP[0] = BolChar;
								ColP[0] = BolCol;
								RowP[0] = BolRow;
							}
							
							return (1);
						}
						if ((0 != (Flags & FIND_CLASS) || (BolChar == ':')) &&
								(L - ColP[0] >= 5 &&
								IsState(StateMap, ColP[0], hsC_Keyword, 5) &&
								CString.memcmp(P + ColP[0], "class", 5) == 0))
						{
							CharP[0] = 'l';
							if (BolChar == ':') {
								CharP[0] = BolChar;
								ColP[0] = BolCol;
								RowP[0] = BolRow;
							}

							return (1);
						}
						if ((0 != (Flags & FIND_CLASS) || (BolChar == ':')) &&
								((L - ColP[0] >= 6 &&
								IsState(StateMap, ColP[0], hsC_Keyword, 6) &&
								CString.memcmp(P + ColP[0], "public", 6) == 0) ||
										((L - ColP[0] >= 7) &&
												IsState(StateMap, ColP[0], hsC_Keyword, 7) &&
												CString.memcmp(P + ColP[0], "private", 7) == 0) ||
										((L - ColP[0] >= 9) &&
												IsState(StateMap, ColP[0], hsC_Keyword, 9) &&
												CString.memcmp(P + ColP[0], "protected", 9) == 0)))
						{
							CharP[0] = 'p';
							if (BolChar == ':') {
								CharP[0] = BolChar;
								ColP[0] = BolCol;
								RowP[0] = BolRow;
							}

							return (1);
						}
					}
				}
				ColP[0]--;
			}
			
			if (BolChar != ' ' && BolChar != ':' && BolChar != ',') {
				CharP[0] = BolChar;
				ColP[0] = BolCol;
				return (1);
			}
			RowP[0]--;
			if (RowP[0] >= 0) {
				L = B.RLine(RowP[0]).getCount();
				ColP[0] = L - 1;
			}
		}
		//#undef isZeroArray(Count)
		return (0);
	}


	private static int SkipWhite(EBuffer B, int Bottom, int []Row, int []Col, int Flags) {
		String P;
		int L;
		//int StateLen;
		int [] /*hsState * */ StateMap;
		int Count[] = { 0, 0, 0 };

		//assert (Row[0] >= 0 && Row[0] < B.RCount);
		L = B.RLine(Row[0]).getCount();
		if (Col[0] >= L)
			Col[0] = L;
		assert (Col[0] >= -1 && Col[0] <= L) ;

		while (Row[0] >= 0 && Row[0] < B.RCount) {
			P = B.RLine(Row[0]).Chars.toString();
			L = B.RLine(Row[0]).getCount();
			StateMap = null;

			int [][] aStateMap = new int[1][];
			int [] aStateLen = {0};
			if (!B.GetMap(Row[0], aStateLen, aStateMap))
				return 0;
			//StateLen = aStateLen[0];
			StateMap = aStateMap[0];

			if (L > 0)
				for ( ; Col[0] >= 0 && Col[0] < L;
						Col[0] += (0 != (Flags & SKIP_BACK) ? -1 : +1)) {
					if (P.charAt(Col[0]) == ' ' || P.charAt(Col[0]) == '\t')
						continue;
					if (StateMap[Col[0]] != hsC_Normal &&
							StateMap[Col[0]] != hsC_Keyword &&
							StateMap[Col[0]] != hsC_String1 &&
							StateMap[Col[0]] != hsC_String2)
						continue;
					if (StateMap[Col[0]] == hsC_Normal && 0 != (Flags & SKIP_MATCH)) {
						switch (P.charAt(Col[0])) {
						case '{': Count[0]--; continue;
						case '}': Count[0]++; continue;
						case '(': Count[1]--; continue;
						case ')': Count[1]++; continue;
						case '[': Count[2]--; continue;
						case ']': Count[2]++; continue;
						}
					}
					if (Count[0] == 0 && Count[1] == 0 && Count[2] == 0
							&& 0 == (Flags & SKIP_TOBOL)) {

						return 1;
					}
				}

			if (Count[0] == 0 && Count[1] == 0 && Count[2] == 0 && 0 != (Flags & SKIP_TOBOL))
				return 1;
			if(0 !=  (Flags & SKIP_LINE)) {
				return 1;
			}
			if(0 !=  (Flags & SKIP_BACK)) {
				Row[0]--;
				if (Row[0] >= 0) {
					L = B.RLine(Row[0]).getCount();
					Col[0] = L - 1;
				}
			} else {
				if (Row[0] + 1 >= Bottom)
					return 1;
				Row[0]++;
				Col[0] = 0;
			}
		}
		return 0;
	}

	
	
	static boolean isZeroArray(int [] Count)
	{
	    for ( int i = 0; i < Count.length; ++i)
	        if (Count[i] != 0)
	            return false;
	    
	    return true;
	}
	
	static boolean IsState(int [] /*hsState * */Buf, int /*hsState*/ State, int Len) {
	    for(int I = 0; I < Len; I++)
	        if (Buf[I] != State) return false;
	    return true;
	}

	static boolean IsState(int [] /*hsState * */Buf, int start, int /*hsState*/ State, int Len) {
	    for(int I = start; I < start+Len; I++)
	        if (Buf[I] != State) return false;
	    return true;
	}

	
	
	
	
	
	
	
	
	
	/**
	 * Does not belong here, but well...
	 * @param B
	 * @param Line
	 * @param PosCursor
	 * @return
	 */
	static int Indent_SIMPLE(EBuffer B, int Line, int PosCursor) {
	    int Pos, Old;
	    
	    if (Line == 0) {
	        Pos = 0;
	    } else {
	        if (B.RLine(Line - 1).StateE == 0) {
	            Pos = B.LineIndented(Line - 1);
	        } else { // for comments, strings, etc, use same as prev line.
	            Pos = B.LineIndented(Line - 1);
	        }
	    }

	    Old = B.LineIndented(Line);
	    if (Pos < 0)
	        Pos = 0;
	    if (Pos != Old)
	        if (B.IndentLine(Line, Pos) == 0)
	            return 0;;
	    return 1;
	}
	
	
	
	
}











