package ru.dz.jfte;

public class EList extends EModel
{
    String Title = "";
    int Row, LeftCol, TopRow, Count;
    int MouseCaptured;
    int MouseMoved;
    int NeedsUpdate, NeedsRedraw;

    
    
    EList(int createFlags, EModel []ARoot, String aTitle) 
    {
    	super(createFlags, ARoot);
        Title = aTitle;
        Row = TopRow = Count = LeftCol = 0;
        NeedsUpdate = 1;
        NeedsRedraw = 1;
        MouseMoved = 0;
        MouseCaptured = 0;
    }


    EViewPort CreateViewPort(EView V) {
        V.Port = new EListPort(this, V);
        AddView(V);
        
        return V.Port;
    }

    EListPort GetViewVPort(EView V) {
        return (EListPort)V.Port;
    }
    
    EListPort GetVPort() {
        return (EListPort )View.Port;
    }

    void SetTitle(String ATitle) {
        Title = ATitle;
        if (View != null && View.MView != null)
            View.MView.RepaintStatus();
    }
        

    ExResult ExecCommand(ExCommands Command, ExState State) {
        int W = 1;
        int H = 1;
        
        if (View != null && View.MView != null && View.MView.Win != null) {
            //View.MView.ConQuerySize(&W, &H);
            {
                int [] Wp = {0}, Hp = {0};

                View.MView.ConQuerySize(Wp, Hp);
                
                H = Hp[0];
                W = Wp[0];
            }
            
            H--;
        }
        FixPos();
        switch (Command) {
        case ExMoveLeft:             return MoveLeft();
        case ExMoveRight:            return MoveRight();
        case ExMoveUp:               return MoveUp();
        case ExMoveDown:             return MoveDown();
        case ExMovePageUp:           return MovePageUp();
        case ExMovePageDown:         return MovePageDown();
        case ExScrollLeft:           return ScrollLeft(8);
        case ExScrollRight:          return ScrollRight(8);
        case ExMovePageStart:        return MovePageStart();
        case ExMovePageEnd:          return MovePageEnd();
        case ExMoveFileStart:        return MoveFileStart();
        case ExMoveFileEnd:          return MoveFileEnd();
        case ExMoveLineStart:        return MoveLineStart();
        case ExMoveLineEnd:          return MoveLineEnd();
        case ExRescan:               RescanList();            return ExResult.ErOK;
        case ExActivate:             return Activate();
        case ExListMark:             return Mark();
        case ExListUnmark:           return Unmark();
        case ExListToggleMark:       return ToggleMark();
        case ExListMarkAll:          return MarkAll();
        case ExListUnmarkAll:        return UnmarkAll();
        case ExListToggleMarkAll:    return ToggleMarkAll();
        }
        return super.ExecCommand(Command, State);
    }

    EEventMap GetEventMap() {
        return EEventMap.FindEventMap("LIST");
    }

    void HandleEvent(TEvent Event) {
    }

    void DrawLine(PCell B, int Line, int Col, int /*ChColor*/ color, int Width) {
    }

    String FormatLine(int Line) {
        return "";
    }

    void RescanList() {}
    void UpdateList() { NeedsUpdate = 1; }
    void FreeList() {}

    void FixPos() {
        int W, H;
        int OTopRow = TopRow;
        int OLeftCol = LeftCol;
        int ORow = Row;

        if (View == null || View.MView == null || View.MView.Win == null)
            return ;

        //View.MView.Win.ConQuerySize(&W, &H);
        {
            int [] Wp = {0}, Hp = {0};

            View.MView.ConQuerySize(Wp, Hp);
            
            H = Hp[0];
            W = Wp[0];
        }
        
        H--;

        //int scrollJumpX = Min(ScrollJumpX, W / 2);
        int scrollJumpY = Math.min(Config.ScrollJumpY, H / 2);
        //int scrollBorderX = Min(ScrollBorderX, W / 2);
        int scrollBorderY = Math.min(Config.ScrollBorderY, H / 2);
        
        if (LeftCol < 0) LeftCol = 0;
        if (Row >= Count) Row = Count - 1;
        if (Config.WeirdScroll)
            if (TopRow + H > Count) TopRow = Count - H;
        if (Row < 0) Row = 0;
        
        if (GetVPort().ReCenter!=0) {
            TopRow = Row - H / 2;
            GetVPort().ReCenter = 0;
        }
        if (TopRow + scrollBorderY > Row) TopRow = Row - scrollJumpY + 1 - scrollBorderY;
        if (TopRow + H - scrollBorderY <= Row) TopRow = Row - H + 1 + scrollJumpY - 1 + scrollBorderY;
        if (TopRow < 0) TopRow = 0;

        if (OTopRow != TopRow || OLeftCol != LeftCol || ORow != Row) {
            NeedsRedraw = 1;
            NeedsUpdate = 1;
        }
    }

    int GetContext() { return CONTEXT_LIST; };
    int BeginMacro() { return 1; }
    boolean CanActivate(int Line) { return true; }
    int Activate(int No) { return 0; }
    boolean IsHilited(int Line) { return false; }
    boolean IsMarked(int Line) { return false; }
    int Mark(int Line) { return 1; }
    int Unmark(int Line) { return 1; }

    ExResult SetPos(int ARow, int ACol) {
        Row = ARow;
        LeftCol = ACol;
        NeedsUpdate = 1;
        return ExResult.ErOK;
    }

    ExResult MoveLeft() {
        if (LeftCol == 0)
            return ExResult.ErFAIL;
        LeftCol--;
        NeedsUpdate = 1;
        return ExResult.ErOK;
    }

    ExResult MoveRight() {
        LeftCol++;
        NeedsUpdate = 1;
        return ExResult.ErOK;
    }

    ExResult MoveUp() {
        if (Row == 0)
            return ExResult.ErFAIL;
        Row--;
        NeedsUpdate = 1;
        return ExResult.ErOK;
    }

    ExResult MoveDown() {
        if (Row == Count - 1)
            return ExResult.ErFAIL;
        Row++;
        NeedsUpdate = 1;
        return ExResult.ErOK;
    }

    ExResult MoveLineStart() {
        if (LeftCol != 0) {
            NeedsUpdate = 1;
            LeftCol = 0;
        }
        return ExResult.ErOK;
    }

    ExResult MoveLineEnd() {
        int [] W = {0}, H = {0};
        
        View.MView.Win.ConQuerySize(W, H);
        H[0]--;
        if (LeftCol != H[0] / 2) {
            LeftCol = H[0] / 2;
            NeedsUpdate = 1;
        }
        return ExResult.ErOK;
    }

    ExResult MovePageUp() {
        int W, H;
        
        if (Row == 0)
            return ExResult.ErFAIL;
        
        //View.MView.Win.ConQuerySize(&W, &H);
        {
            int [] Wp = {0}, Hp = {0};

            View.MView.ConQuerySize(Wp, Hp);
            
            H = Hp[0];
            W = Wp[0];
        }
        
        H--;

        Row -= H;
        TopRow -= H;
        if (Row < 0)
            Row = 0;
        if (TopRow < 0)
            TopRow = 0;
        NeedsUpdate = 1;
        return ExResult.ErOK;
    }

    ExResult MovePageDown() {
        int W, H;
        
        if (Row == Count - 1)
            return ExResult.ErFAIL;
        
        //View.MView.Win.ConQuerySize(&W, &H);
        {
            int [] Wp = {0}, Hp = {0};

            View.MView.ConQuerySize(Wp, Hp);
            
            H = Hp[0];
            W = Wp[0];
        }
        
        H--;
        
        Row += H;
        TopRow += H;
        if (Row >= Count)
            Row = Count - 1;
        if (TopRow > Row)
            TopRow = Row;
        if (Row < 0)
            Row = 0;
        if (TopRow < 0)
            TopRow = 0;
        NeedsUpdate = 1;
        return ExResult.ErOK;
    }

    ExResult ScrollRight(int Cols) {
        if (LeftCol >= Cols) {
            LeftCol -= Cols;
            NeedsUpdate = 1;
        } else if (LeftCol != 0) {
            LeftCol = 0;
            NeedsUpdate = 1;
        } else
            return ExResult.ErFAIL;
        return ExResult.ErOK;
    }

    ExResult ScrollLeft(int Cols) {
        LeftCol += Cols;
        NeedsUpdate = 1;
        return ExResult.ErOK;
    }

    ExResult ScrollUp(int Rows) {
        if (TopRow == Count - 1)
            return ExResult.ErFAIL;

        TopRow += Rows;
        Row += Rows;
        
        if (Row >= Count)
            Row = Count - 1;
        if (Row < 0)
            Row = 0;
        if (TopRow > Row)
            TopRow = Row;
        NeedsUpdate = 1;
        return ExResult.ErOK;
    }

    ExResult ScrollDown(int Rows) {
        if (TopRow == 0)
            return ExResult.ErFAIL;
        
        TopRow -= Rows;
        Row -= Rows;
        
        if (Row < 0)
            Row = 0;
        if (TopRow < 0)
            TopRow = 0;
        NeedsUpdate = 1;
        return ExResult.ErOK;
    }

    ExResult MovePageStart() {
        if (Row <= TopRow)
            return ExResult.ErFAIL;
        Row = TopRow;
        NeedsUpdate = 1;
        return ExResult.ErOK;
    }

    ExResult MovePageEnd() {
        int [] W = {0}, H = {0};

        if (Row == Count - 1)
            return ExResult.ErOK;
        
        View.MView.Win.ConQuerySize(W, H);
        H[0]--;
        if (Row == TopRow + H[0] - 1)
            return ExResult.ErOK;
        
        Row = TopRow + H[0] - 1;
        if (Row >= Count)
            Row = Count - 1;
        if (Row < 0)
            Row = 0;
        NeedsUpdate = 1;
        return ExResult.ErOK;
    }

    ExResult MoveFileStart() {
        if (Row == 0 && LeftCol == 0)
            return ExResult.ErOK;
        Row = 0;
        LeftCol = 0;
        NeedsUpdate = 1;
        return ExResult.ErOK;
    }

    ExResult MoveFileEnd() {
        if (Row == Count - 1 && LeftCol == 0)
            return ExResult.ErFAIL;
        Row = Count - 1;
        if (Row < 0)
            Row = 0;
        NeedsUpdate = 1;
        LeftCol = 0;
        return ExResult.ErOK;
    }

    ExResult Activate() {
        if (Count > 0)
            if (CanActivate(Row))
                if (Activate(Row) == 1)
                    return ExResult.ErOK;
        return ExResult.ErFAIL;
    }

    ExResult Mark() {
        if (Count > 0 && ! IsMarked(Row) && Mark(Row) == 1) {
            NeedsRedraw = 1;
            return ExResult.ErOK;
        } else return ExResult.ErFAIL;
    }

    ExResult Unmark() {
        if (Count > 0 && IsMarked(Row) && Unmark(Row) == 1) {
            NeedsRedraw = 1;
            return ExResult.ErOK;
        } else return ExResult.ErFAIL;
    }

    ExResult ToggleMark() {
        if (Count > 0)
            if (IsMarked(Row)) {
                if (Unmark(Row) == 1) {
                    NeedsRedraw = 1;
                    return ExResult.ErOK;
                }
            } else {
                if (Mark(Row) == 1) {
                    NeedsRedraw = 1;
                    return ExResult.ErOK;
                }
            }
        return ExResult.ErFAIL;
    }

    ExResult MarkAll() {
        NeedsRedraw = 1;
        for (int i = 0; i < Count; i++) {
            if (! IsMarked(i))
                if (Mark(i) != 1) return ExResult.ErFAIL;
        }
        return ExResult.ErOK;
    }

    ExResult UnmarkAll() {
        NeedsRedraw = 1;
        for (int i = 0; i < Count; i++) {
            if (IsMarked(i))
                if (Unmark(i) != 1) return ExResult.ErFAIL;
        }
        return ExResult.ErOK;
    }

    ExResult ToggleMarkAll() {
        NeedsRedraw = 1;
        for (int i = 0; i < Count; i++) {
            if (IsMarked(i)) {
                if (Unmark(i) != 1) return ExResult.ErFAIL;
            } else {
                if (Mark(i) != 1) return ExResult.ErFAIL;
            }
        }
        return ExResult.ErOK;
    }
    
}
