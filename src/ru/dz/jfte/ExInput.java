package ru.dz.jfte;

public class ExInput extends ExView implements KeyDefs, EventDefs, ColorDefs 
{
    String Prompt;
    String Line;
    String MatchStr;
    String CurStr;
    int Pos;
    int LPos;
    int MaxLen = 500; // TODO need?
    
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
        Line = "";
        MatchStr = "";
        CurStr = "";
        if (Line != null) {
            Line = ALine[0];
            Pos = Line.length();
            LPos = 0;
        }
        if (Select != 0)
            SelEnd = Pos;
        TabCount = 0;
        HistId = AHistId;
        CurItem = 0;
    }


    void Activate(int gotfocus) {
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
                        if (isalnum(Line[Pos]) && !isalnum(Line[Pos - 1]))
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
                    int len = strlen(Line);
                    if (Pos < len) {
                        Pos++;
                        while (Pos < len) {
                            if (isalnum(Line[Pos]) && !isalnum(Line[Pos - 1]))
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
            case kbEnd: Pos = strlen(Line); SelStart = SelEnd = 0; TabCount = 0; Event.What = evNone; break;
            case kbEsc: EndExec(0); Event.What = evNone; break;
            case kbEnter: 
                AddInputHistory(HistId, Line);
                EndExec(1);
                Event.What = evNone; 
                break;
            case kbBackSp | kfCtrl | kfShift:
                SelStart = SelEnd = 0; 
                Pos = 0;
                Line[0] = 0;
                TabCount = 0;
                break;
            case kbBackSp | kfCtrl:
                if (Pos > 0) {
                    if (Pos > strlen(Line)) {
                        Pos = strlen(Line);
                    } else {
                        char Ch;
                        
                        if (Pos > 0) do {
                            Pos--;
                            memmove(Line + Pos, Line + Pos + 1, strlen(Line + Pos + 1) + 1);
                            if (Pos == 0) break;
                            Ch = Line[Pos - 1];
                        } while (Pos > 0 && Ch != '\\' && Ch != '/' && Ch != '.' && isalnum(Ch));
                    }
                }
                SelStart = SelEnd = 0; 
                TabCount = 0;
                Event.What = evNone;
                break;
            case kbBackSp:
            case kbBackSp | kfShift:
                if (SelStart < SelEnd) {
                    memmove(Line + SelStart, Line + SelEnd, strlen(Line + SelEnd) + 1);
                    Pos = SelStart;
                    SelStart = SelEnd = 0;
                    break;
                }
                if (Pos <= 0) break;
                Pos--;
                if (Pos < strlen(Line))
                    memmove(Line + Pos, Line + Pos + 1, strlen(Line + Pos + 1) + 1);
                TabCount = 0;
                Event.What = evNone;
                break;
            case kbDel:
                if (SelStart < SelEnd) {
                    memmove(Line + SelStart, Line + SelEnd, strlen(Line + SelEnd) + 1);
                    Pos = SelStart;
                    SelStart = SelEnd = 0;
                    break;
                }
                if (Pos < strlen(Line))
                    memmove(Line + Pos, Line + Pos + 1, strlen(Line + Pos + 1) + 1);
                TabCount = 0;
                Event.What = evNone;
                break;
            case kbDel | kfCtrl:
                if (SelStart < SelEnd) {
                    memmove(Line + SelStart, Line + SelEnd, strlen(Line + SelEnd) + 1);
                    Pos = SelStart;
                    SelStart = SelEnd = 0;
                    break;
                }
                SelStart = SelEnd = 0;
                Line[Pos] = 0;
                TabCount = 0;
                Event.What = evNone;
                break;
            case kbIns | kfShift:
            case 'V'   | kfCtrl:
                {
                    int len;

                    if (SystemClipboard)
                        GetPMClip();
                    
                    if (SSBuffer == 0) break;
                    if (SSBuffer.RCount == 0) break;

                    if (SelStart < SelEnd) {
                        memmove(Line + SelStart, Line + SelEnd, strlen(Line + SelEnd) + 1);
                        Pos = SelStart;
                        SelStart = SelEnd = 0;
                    }

                    len = SSBuffer.LineChars(0);
                    if (strlen(Line) + len < MaxLen) {
                        memmove(Line + Pos + len, Line + Pos, strlen(Line + Pos) + 1);
                        memcpy(Line + Pos, SSBuffer.RLine(0).Chars, len);
                        TabCount = 0;
                        Event.What = evNone;
                        Pos += len;
                    }
                }
                break;
            case kbUp:
                SelStart = SelEnd = 0;
                if (CurItem == 0) 
                    strcpy(CurStr, Line);
                CurItem += 2;
            case kbDown:
                SelStart = SelEnd = 0;
                if (CurItem == 0) break;
                CurItem--;
                {
                    int cnt = CountInputHistory(HistId);
                    
                    if (CurItem > cnt) CurItem = cnt;
                    if (CurItem < 0) CurItem = 0;
                    
                    if (CurItem == 0)
                        strcpy(Line, CurStr);
                    else if (GetInputHistory(HistId, Line, MaxLen, CurItem));
                    else strcpy(Line, CurStr);
                    Pos = strlen(Line);
//                    SelStart = SelEnd = 0;
                }
                Event.What = evNone;
                break;
            case kbTab | kfShift:
                TabCount -= 2;
            case kbTab:
                if (Comp) {
                    String Str2 = malloc(MaxLen + 1);
                    int n;

                    assert(Str2);
                    TabCount++;
                    if (TabCount < 1) TabCount = 1;
                    if ((TabCount == 1) && (kbCode(Event.Key.Code) == kbTab)) {
                        strcpy(MatchStr, Line);
                    }
                    n = Comp(MatchStr, Str2, TabCount);
                    if ((n > 0) && (TabCount <= n)) {
                        strcpy(Line, Str2);
                        Pos = strlen(Line);
                    } else if (TabCount > n) TabCount = n;
                    free(Str2);
                }
                SelStart = SelEnd = 0;
                Event.What = evNone;
                break;
            case 'Q' | kfCtrl:
                Event.What = evKeyDown;
                Event.Key.Code = Win.GetChar(0);
            default:
                {
                    char Ch;

                    if( 0 != (Ch = GetCharFromEvent(Event)) && (strlen(Line) < MaxLen)) {
                        if (SelStart < SelEnd) {
                            memmove(Line + SelStart, Line + SelEnd, strlen(Line + SelEnd) + 1);
                            Pos = SelStart;
                            SelStart = SelEnd = 0;
                        }
                        memmove(Line + Pos + 1, Line + Pos, strlen(Line + Pos) + 1);
                        Line[Pos++] = Ch;
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
        TDrawBuffer B;
        int [] W, H;
        int FLen, FPos;
        
        ConQuerySize(W, H);
        
        FPos = Prompt.length() + 2;
        FLen = W[0] - FPos;
        
        if (Pos > Line.length()) 
            Pos = Line.length();
        //if (Pos < 0) Pos = 0;
        if (LPos + FLen <= Pos) LPos = Pos - FLen + 1;
        if (Pos < LPos) LPos = Pos;
        
        MoveChar(B, 0, W[0], ' ', hcEntry_Field, W[0]);
        MoveStr(B, 0, W[0], Prompt, hcEntry_Prompt, FPos);
        MoveChar(B, FPos - 2, W[0], ':', hcEntry_Prompt, 1);
        MoveStr(B, FPos, W[0], Line + LPos, hcEntry_Field, FLen);
        MoveAttr(B, FPos + SelStart - LPos, W[0], hcEntry_Selection, SelEnd - SelStart);
        ConSetCursorPos(FPos + Pos - LPos, H[0] - 1);
        ConPutBox(0, H[0] - 1, W[0], 1, B);
        ConShowCursor();
    }
    
}
