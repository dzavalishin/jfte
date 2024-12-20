package ru.dz.jfte;

import ru.dz.jfte.c.BitOps;


public class BufferView extends EList implements EventDefs, KeyDefs 
{
	public static final int MAXISEARCH = 256;
	
    String [] BList = null;
    int BCount = 0;
    int SearchLen = 0;
    String SearchString;
    int [] SearchPos = new int[MAXISEARCH];

    static BufferView BufferList = null;
    


    BufferView(int createFlags, EModel []ARoot)  
    {
    	super(createFlags, ARoot, "Buffers");    	
    }

        
    @Override
    EEventMap GetEventMap() {
        return EEventMap.FindEventMap("BUFFERS");
    }

    @Override
    int GetContext() {
        return CONTEXT_BUFFERS; 
    }

    @Override
    void DrawLine(PCell B, int Line, int Col, int /*ChColor*/ color, int Width) {
        if (Line < BCount)
            if (BList[Line] != null && Col < BList[Line].length())
                B.MoveStr( 0, Width, BList[Line].substring(Col), color, Width);
    }

    @Override
    String FormatLine(int Line) {
        return BList[Line];
    }

    @Override
    void UpdateList() {
        EModel B = ActiveModel[0];
        //int No;
        //char s[512] = "";
        
        BList = null;
        BCount = 0;
        while (B != null) {
            BCount++;
            B = B.Next;
            if (B == ActiveModel[0]) break;
        }
        //BList = (char **) malloc(sizeof(char *) * BCount);
        BList = new String[BCount];
        B = ActiveModel[0];
        int No = 0;
        while (B!=null) {
            String s = B.GetInfo();
            BList[No++] = s;
            B = B.Next;
            if (B == ActiveModel[0]) break;
            if (No >= BCount) break;
        }
        Count = BCount;
        NeedsUpdate = 1;
    }
        
    EModel GetBufferById(int No) {
        EModel B = ActiveModel[0];
        while (B != null) {
            if (No == 0) {
                return B;
            }
            No--;
            B = B.Next;
            if (B == ActiveModel[0]) break;
        }
        return null;
    }
        
    @Override
    ExResult ExecCommand(ExCommands Command, ExState State) {

        switch (Command) {
        case ExCloseActivate:
            {
                CancelSearch();
                EModel B = GetBufferById(Row);
                if (B != null && B != this) {
                    View.SwitchToModel(B);
                    //delete this;
                    return ExResult.ErOK;
                }
            }
            return ExResult.ErFAIL;
        case ExBufListFileClose:
            {
                EModel B = GetBufferById(Row);

                CancelSearch();
                if (B != null && B != this && Count > 1) {
                    if (B.ConfQuit(View.MView.Win,0)!=0) {
                        View.DeleteModel(B);
                    }
                    UpdateList();
                    return ExResult.ErOK;
                }
            }
            return ExResult.ErFAIL;
        case ExBufListFileSave:
            {
                EModel B = GetBufferById(Row);

                if (B != null && B.GetContext() == CONTEXT_FILE)
                    if (((EBuffer)B).Save())
                        return ExResult.ErOK;
            }
            return ExResult.ErFAIL;
            
        case ExActivateInOtherWindow:
            {
                EModel B = GetBufferById(Row);

                CancelSearch();
                if (B!=null) {
                    View.Next.SwitchToModel(B);
                    return ExResult.ErOK;
                }
            }
            return ExResult.ErFAIL;
        case ExBufListSearchCancel:
            CancelSearch();
            return ExResult.ErOK;
        case ExBufListSearchNext:
            // Find next matching line
            if (SearchLen!=0) {
                int i = Row + 1;
                i = getMatchingLine(i == BCount ? 0 : i, 1);
                // Never returns -1 since something already found before call
                Row = SearchPos[SearchLen] = i;
            }
            return ExResult.ErOK;
        case ExBufListSearchPrev:
            // Find prev matching line
            if (SearchLen!=0) {
                int i = Row - 1;
                i = getMatchingLine(i == -1 ? BCount - 1 : i, -1);
                // Never returns -1 since something already found before call
                Row = SearchPos[SearchLen] = i;
            }
            return ExResult.ErOK;
        }

        return super.ExecCommand(Command, State);
    }

    @Override
    void HandleEvent(TEvent Event) {
        int resetSearch = 1;
        
        super.HandleEvent(Event);
        
        switch (Event.What) {
            case evKeyUp:
                resetSearch = 0;
                break;
            case evKeyDown:
            	TKeyEvent ke = (TKeyEvent) Event;
                switch (KeyDefs.kbCode(ke.Code)) {
                    case kbBackSp:
                        resetSearch = 0;
                        if (SearchLen > 0) {
                            //SearchString[--SearchLen] = 0;
                        	SearchString = SearchString.substring(0,--SearchLen);
                            Row = SearchPos[SearchLen];
                            Msg(S_INFO, "Search: [%s]", SearchString);
                        } else
                            Msg(S_INFO, "");
                        break;
                    case kbEsc:
                        Msg(S_INFO, "");
                        break;
                    default:
                        resetSearch = 0;
                        if (KeyDefs.isAscii(ke.Code) && (SearchLen < MAXISEARCH)) {
                            char Ch = (char) ke.Code;

                            SearchPos[SearchLen] = Row;
                            SearchString += Ch;
                            //SearchString[++SearchLen] = null;
                            int i = getMatchingLine(Row, 1);
                            if (i == -1)
                                //SearchString[--SearchLen] = null;
                            	SearchString = SearchString.substring(0,--SearchLen);
                            else
                                Row = i;
                            Msg(S_INFO, "Search: [%s]", SearchString);
                        }
                        break;
                }
        }
        if (resetSearch != 0) {
            SearchLen = 0;
        }
    }

    /**
     * Search for next line containing SearchString starting from line 'start'.
     * Direction should be 1 for ascending and -1 for descending.
     * Returns line found or -1 if none.
     */
    int getMatchingLine (int start, int direction) {
        int i = start;
        do {
            // Find SearchString at any place in string for line i
            for(int j = 0; BList[i] != null && !BList[i].isEmpty(); j++)
            {
            	String ss = BList[i].substring(j);
                if (BList[i].charAt(j) == SearchString.charAt(0) && BitOps.strnicmp(SearchString, ss/*BList[i]+j*/, SearchLen) == 0) {
                    return i;
                }
            }
            i += direction;
            if (i == BCount) i = 0; else if (i == -1) i = BCount - 1;
        } while (i != start);
        return -1;
    }

    @Override
    int Activate(int No) {
        CancelSearch();
        EModel B = GetBufferById(No);
        if (B!=null) {
            View.SwitchToModel(B);
            return 1;
        }
        return 0;
    }

    void CancelSearch() {
        SearchLen = 0;
        Msg(S_INFO, "");
    }

    void GetInfo(String [] i) {
    	i[0] = String.format("%2d %04d/%03d Buffers", getModelNo(), Row + 1, Count);
    }

    @Override
    void GetTitle(String [] ATitle, String [] ASTitle) {
        ATitle[0] = "Buffers";
        ASTitle[0] = "Buffers";
    }

    @Override
    String GetInfo() {
        return String.format("%2d %04d/%03d Buffers", getModelNo(), Row + 1, Count);
    }
    
}
