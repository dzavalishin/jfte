package ru.dz.jfte;

import ru.dz.jfte.c.CString;
import ru.dz.jfte.c.CStringPtr;

public class ExInput extends ExView implements KeyDefs, EventDefs, ColorDefs 
{
	String Prompt;
	CString Line = new CString();
	String MatchStr;
	String CurStr;
	int Pos;
	int LPos;
	int MaxLen = 500;

	Completer Comp;

	int TabCount;
	int HistId;
	int CurItem;

	int SelStart;
	int SelEnd;


	ExView GetViewContext() { return Next; }


	ExInput(String APrompt, String [] ALine, Completer AComp, int Select, int AHistId) {
		//assert(ABufLen > 0);
		//MaxLen = ABufLen - 1;
		Comp = AComp;
		SelStart = SelEnd = 0;
		Prompt = APrompt;
		//Line = "";
		MatchStr = "";
		CurStr = "";

		//Line = ALine[0];
		int w = 80; // TODO ConWidth(); fails for no window yet
		//Line = new BinaryString(w,' ');
		Line = new CString(w,' ');
		Line.copyIn(0, ALine[0]);
		
		//Pos = Line.getSize();
		Pos = ALine[0].length();
		LPos = 0;


		if (Select != 0)
			SelEnd = Pos;
		TabCount = 0;
		HistId = AHistId;
		CurItem = 0;
	}


	void Activate(boolean gotfocus) {
		super.Activate(gotfocus);
	}

	int BeginMacro() {
		return 1;
	}

	void HandleEvent(TEvent Event) {
		switch (Event.What) {
		case evKeyDown:
			switch (KeyDefs.kbCode(((TKeyEvent)Event).Code)) {
			case kbLeft: if (Pos > 0) Pos--; SelStart = SelEnd = 0; TabCount = 0; Event.What = evNone; break;
			case kbRight: Pos++; SelStart = SelEnd = 0; TabCount = 0; Event.What = evNone; break;
			case kbLeft | kfCtrl:
				if (Pos > 0) {
					Pos--;
					while (Pos > 0) {
						if (KeyDefs.isalnum(Line.charAt(Pos)) && !KeyDefs.isalnum(Line.charAt(Pos - 1)))
							break;
						Pos--;
					}
				}
				SelStart = SelEnd = 0;
				TabCount = 0;
				Event.What = evNone;
				break;
			case kbRight | kfCtrl:
			{
				int len = Line.getSize();
				if (Pos < len) {
					Pos++;
					while (Pos < len) {
						if (KeyDefs.isalnum(Line.charAt(Pos)) && !KeyDefs.isalnum(Line.charAt(Pos - 1)))
							break;
						Pos++;
					}
				}
			}
			SelStart = SelEnd = 0;
			TabCount = 0;
			Event.What = evNone;
			break;
			case kbHome: Pos = 0; SelStart = SelEnd = 0; TabCount = 0; Event.What = evNone; break;
			case kbEnd: Pos = Line.getSize(); SelStart = SelEnd = 0; TabCount = 0; Event.What = evNone; break;
			case kbEsc: EndExec(0); Event.What = evNone; break;
			case kbEnter: 
				InputHistory.AddInputHistory(HistId, Line.toString());
				EndExec(1);
				Event.What = evNone; 
				break;
			case kbBackSp | kfCtrl | kfShift:
				SelStart = SelEnd = 0; 
				Pos = 0;
				Line.setSize(0);
				TabCount = 0;
				break;
			case kbBackSp | kfCtrl:
				if (Pos > 0) {
					if (Pos > Line.getSize()) {
						Pos = Line.getSize();
					} else {
						char Ch;

						if (Pos > 0) do {
							Pos--;
							//memmove(Line + Pos, Line + Pos + 1, strlen(Line + Pos + 1) + 1);
							int len = Line.getSize() - Pos;
							Line.memmove(Pos, Pos+1, len);
							if (Pos == 0) break;
							Ch = Line.charAt(Pos - 1);
						} while (Pos > 0 && Ch != '\\' && Ch != '/' && Ch != '.' && KeyDefs.isalnum(Ch));
					}
				}
				SelStart = SelEnd = 0; 
				TabCount = 0;
				Event.What = evNone;
				break;
			case kbBackSp:
			case kbBackSp | kfShift:
				if (SelStart < SelEnd) {
					//memmove(Line + SelStart, Line + SelEnd, strlen(Line + SelEnd) + 1);
					int len = Line.getSize() - SelEnd + 1;
					Line.memmove(SelStart, SelEnd, len);
					Pos = SelStart;
					SelStart = SelEnd = 0;
					break;
				}
				if (Pos <= 0) break;
				Pos--;
				if (Pos < Line.getSize())
				{
					//memmove(Line + Pos, Line + Pos + 1, strlen(Line + Pos + 1) + 1);
					int len = Line.getSize() - Pos;
					Line.memmove(Pos, Pos+1, len);
				}
				TabCount = 0;
				Event.What = evNone;
				break;
			case kbDel:
				if (SelStart < SelEnd) {
					//memmove(Line + SelStart, Line + SelEnd, strlen(Line + SelEnd) + 1);
					int len = Line.getSize() - SelEnd + 1;
					Line.memmove(SelStart, SelEnd, len);
					Pos = SelStart;
					SelStart = SelEnd = 0;
					break;
				}
				if (Pos < Line.getSize())
				{
					//memmove(Line + Pos, Line + Pos + 1, strlen(Line + Pos + 1) + 1);
					int len = Line.getSize() - Pos;
					Line.memmove(Pos, Pos+1, len);
				}
				TabCount = 0;
				Event.What = evNone;
				break;
			case kbDel | kfCtrl:
				if (SelStart < SelEnd) {
					//memmove(Line + SelStart, Line + SelEnd, strlen(Line + SelEnd) + 1);
					int len = Line.getSize() - SelEnd + 1;
					Line.memmove(SelStart, SelEnd, len);
					Pos = SelStart;
					SelStart = SelEnd = 0;
					break;
				}
				SelStart = SelEnd = 0;
				//Line[Pos] = 0;
				//Line = new BinaryString( Line.substring(0,Pos) );
				Line = new CString( Line.substring(0,Pos) );
				TabCount = 0;
				Event.What = evNone;
				break;
			case kbIns | kfShift:
			case 'V'   | kfCtrl:
			{
				int len;

				if (Config.SystemClipboard)
					ClipData.GetPMClip();

				if (EBuffer.SSBuffer == null) break;
				if (EBuffer.SSBuffer.RCount == 0) break;

				if (SelStart < SelEnd) {
					//memmove(Line + SelStart, Line + SelEnd, strlen(Line + SelEnd) + 1);
					int mlen = Line.getSize() - SelEnd + 1;
					Line.memmove(SelStart, SelEnd, mlen);
					Pos = SelStart;
					SelStart = SelEnd = 0;
				}

				len = EBuffer.SSBuffer.LineChars(0);
				if (Line.getSize() + len < MaxLen) {
					//memmove(Line + Pos + len, Line + Pos, strlen(Line + Pos) + 1);
					//memcpy(Line + Pos, EBuffer.SSBuffer.RLine(0).Chars, len);
					int mlen = Line.getSize() - Pos + 1;
					//BinaryString src = EBuffer.SSBuffer.RLine(0).Chars;
					CString src = EBuffer.SSBuffer.RLine(0).Chars;
					Line.memmove(Pos+len, Pos, mlen);
					Line.copyIn( Pos, src, 0, len );
					TabCount = 0;
					Event.What = evNone;
					Pos += len;
				}
			}
			break;
			case kbUp:
				SelStart = SelEnd = 0;
				if (CurItem == 0) 
					CurStr = Line.toString();
				CurItem += 2;
			case kbDown:
				SelStart = SelEnd = 0;
				if (CurItem == 0) break;
				CurItem--;
				{
					int cnt = InputHistory.CountInputHistory(HistId);

					if (CurItem > cnt) CurItem = cnt;
					if (CurItem < 0) CurItem = 0;

					if (CurItem == 0)
						Line = new CString(CurStr);
					else
					{
						String [] ss = {""};
						if (!InputHistory.GetInputHistory(HistId, ss, CurItem))
							Line = new CString(ss[0]);
						else 
							Line = new CString(CurStr);
					}
					Pos = Line.getSize();
					//                    SelStart = SelEnd = 0;
				} 
				Event.What = evNone;
				break;
			case kbTab | kfShift:
				TabCount -= 2;
			case kbTab:
				if (Comp!=null) {
					String []Str2 = {""};
					int n;

					TabCount++;
					if (TabCount < 1) TabCount = 1;
					if ((TabCount == 1) && (KeyDefs.kbCode(((TKeyEvent)Event).Code) == kbTab)) {
						MatchStr = Line.toString();
					}
					n = Comp.complete(MatchStr, Str2, TabCount);
					if ((n > 0) && (TabCount <= n)) {
						Line = new CString(Str2[0]);
						Pos = Line.getSize();
					} else if (TabCount > n) TabCount = n;
					//free(Str2);
				}
				SelStart = SelEnd = 0;
				Event.What = evNone;
				break;
			case 'Q' | kfCtrl:
				//Event.What = evKeyDown;
				//Event.Key.Code = Win.GetChar(null);
				Event = new TKeyEvent(evKeyDown, (int) Win.GetChar(null));
				Event.dispatch();
			default:
			{
				char Ch;

				if( 0 != (Ch = ((TKeyEvent)Event).GetChar()) && (Line.getSize() < MaxLen)) {
					if (SelStart < SelEnd) {
						//memmove(Line + SelStart, Line + SelEnd, strlen(Line + SelEnd) + 1);
						int len = Line.getSize() - SelEnd + 1;
						Line.memmove(SelStart, SelEnd, len);
						Pos = SelStart;
						SelStart = SelEnd = 0;
					}
					//memmove(Line + Pos + 1, Line + Pos, strlen(Line + Pos) + 1);
					//Line[Pos++] = Ch;

					//int mlen = Line.getSize() - Pos + 1;
					int mlen = Line.getSize() - Pos - 1;
					Line.memmove(Pos+1, Pos, mlen);
					Line.copyIn(Pos++, ""+Ch, 1);

					TabCount = 0;
					Event.What = evNone;
				}
			}
			break;
			}
			Event.What = evNone;
			break;
		}
	}

	void UpdateView() {
		if (Next != null) {
			Next.UpdateView();
		}
	}

	void RepaintView() {
		if (Next != null) {
			Next.RepaintView();
		}
	}

	void UpdateStatus() {
		RepaintStatus();
	}

	void RepaintStatus() {
		TDrawBuffer B = new TDrawBuffer();
		int [] W = {0}, H = {0};
		int FLen, FPos;

		ConQuerySize(W, H);

		FPos = Prompt.length() + 2;
		FLen = W[0] - FPos;

		if (Pos > Line.getSize()) 
			Pos = Line.getSize();
		//if (Pos < 0) Pos = 0;
		if (LPos + FLen <= Pos) LPos = Pos - FLen + 1;
		if (Pos < LPos) LPos = Pos;

		B.MoveChar( 0, W[0], ' ', hcEntry_Field, W[0]);
		B.MoveStr( 0, W[0], Prompt, hcEntry_Prompt, FPos);
		B.MoveChar( FPos - 2, W[0], ':', hcEntry_Prompt, 1);

		//B.MoveStr( FPos, W[0], Line + LPos, hcEntry_Field, FLen);

		//ArrayPtr<Character> lp = Line.getPointer();
		CStringPtr lp = Line.getPointer();
		lp.shift(LPos);
		B.MoveStr( FPos, W[0], lp.toString(), hcEntry_Field, FLen);
		//B.MoveStr( FPos, W[0], lp, hcEntry_Field, Math.min(FLen, lp.length()));

		B.MoveAttr( FPos + SelStart - LPos, W[0], hcEntry_Selection, SelEnd - SelStart);
		ConSetCursorPos(FPos + Pos - LPos, H[0] - 1);
		ConPutBox(0, H[0] - 1, W[0], 1, B);
		ConShowCursor();
	}

}
