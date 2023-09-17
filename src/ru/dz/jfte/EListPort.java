package ru.dz.jfte;

import java.io.Closeable;

public class EListPort extends EViewPort implements Closeable, ColorDefs, EventDefs 
{
    EList List;
    int Row, TopRow, LeftCol;
    int OldRow, OldTopRow, OldLeftCol, OldCount;

    
    
    EListPort(EList L, EView V) 
    {
    	super(V);
        List = L;
        OldTopRow = OldLeftCol = OldRow = OldCount = -1;
        GetPos();
    }

    public void close() {
        StorePos();
    }

    void StorePos() {
        List.Row = Row;
        List.TopRow = TopRow;
        List.LeftCol = LeftCol;
        List.NeedsUpdate = 1;
    }

    void GetPos() {
        Row = List.Row;
        TopRow = List.TopRow;
        LeftCol = List.LeftCol;
    }

    void HandleEvent(TEvent Event) {
        int W = 1;
        int H = 1;
        
        super.HandleEvent(Event);
        
        if (View != null && View.MView  != null && View.MView.Win != null) {
            //View.MView.ConQuerySize(&W, &H);
            {
                int [] Wp = {0}, Hp = {0};

                View.MView.ConQuerySize(Wp, Hp);
                
                H = Hp[0];
                W = Wp[0];
            }
        	
            H--;
        }
        
        switch (Event.What) {
        case evCommand:
            switch (((TMsgEvent)Event).Command) {
            case cmVScrollUp:
                List.ScrollDown(1);
                Event.What = evNone;
                break;
            case cmVScrollDown:
                List.ScrollUp(1);
                Event.What = evNone;
                break;
            case cmVScrollPgUp:
                List.MovePageUp();
                Event.What = evNone;
                break;
            case cmVScrollPgDn:
                List.MovePageDown();
                Event.What = evNone;
                break;
            case cmVScrollMove:
                {
                    int ypos;
                    
                    ypos = List.Row - List.TopRow;
                    List.TopRow = (int) ((TMsgEvent)Event).Param1;
                    List.Row = List.TopRow + ypos;
                }
                Event.What = evNone;
                break;
            case cmHScrollLeft:
                List.ScrollRight(1);
                Event.What = evNone;
                break;
            case cmHScrollRight:
                List.ScrollLeft(1);
                Event.What = evNone;
                break;
            case cmHScrollPgLt:
                List.ScrollRight(W);
                Event.What = evNone;
                break;
            case cmHScrollPgRt:
                List.ScrollLeft(W);
                Event.What = evNone;
                break;
            case cmHScrollMove:
                List.LeftCol = (int) ((TMsgEvent)Event).Param1;
                Event.What = evNone;
                break;
            }
            break;
        case evMouseDown:
        case evMouseUp:
        case evMouseMove:
        case evMouseAuto:
            HandleMouse((TMouseEvent) Event);
            break;
        }
    }

    void HandleMouse(TMouseEvent Event) {
        int W, H;
        int x, y, xx, yy;
        
        //View.MView.ConQuerySize(&W, &H);
        {
            int [] Wp, Hp;

            View.MView.ConQuerySize(Wp, Hp);
            
            H = Hp[0];
            W = Wp[0];
        }
        
        x = Event.X;
        y = Event.Y;
        yy = y + TopRow;
        xx = x + LeftCol;
//        if (yy >= Selected) yy = Window.Buffer.VCount - 1;
        if (yy < 0) yy = 0;
        if (xx < 0) xx = 0;
        
        switch (Event.What) {
        case evMouseDown:
            if (Event.Y == H - 1)
                break;
            if (View.MView.Win.CaptureMouse(1))
                View.MView.MouseCaptured = 1;
            else
                break;
            
            if (Event.Buttons == 1)
                if (yy < List.Count && yy >= 0) {
                    List.SetPos(yy, LeftCol);
                    if (Event.Count == 2) {
                        if (List.CanActivate(List.Row)) {
                            View.MView.Win.CaptureMouse(0);
                            if (List.Activate() == 1) {
                                //View.MView.EndExec(1);
                            }
                        }
                    }
                }
            if (Event.Buttons == 2)
                if (yy < List.Count && yy >= 0)
                    List.SetPos(yy, LeftCol);
            Event.What = evNone;
            break;
        case evMouseAuto:
        case evMouseMove:
            if (View.MView.MouseCaptured!=0) {
                if (Event.Buttons == 1 || Event.Buttons == 2)
                    if (yy < List.Count && yy >= 0) {
                        List.SetPos(yy, LeftCol);
                    }
                Event.What = evNone;
            }
            break;
        case evMouseUp:
            if (View.MView.MouseCaptured!=0)
                View.MView.Win.CaptureMouse(0);
            else
                break;
            if (Event.Buttons == 2) {
                EEventMap Map = View.MView.Win.GetEventMap();
                String MName = null;

                if (yy < List.Count && yy >= 0) {
                    List.SetPos(yy, LeftCol);
                }

                if (Map!=null)
                    MName = Map.GetMenu(EM_LocalMenu);
                if (MName == null)
                    MName = "Local";
                View.MView.Win.Parent.PopupMenu(MName);
            }
            View.MView.MouseCaptured = 0;
            Event.What = evNone;
            break;
        }
    }

    void UpdateView() {
        if (OldLeftCol != LeftCol || OldTopRow != TopRow || OldCount != List.Count)
            List.NeedsRedraw = List.NeedsUpdate = 1;
        
        if (List.NeedsUpdate!=0) {

            List.UpdateList();

            List.FixPos();

            if (List.View == View)
                GetPos();

            if (OldLeftCol != LeftCol || OldTopRow != TopRow || OldCount != List.Count)
                List.NeedsRedraw = List.NeedsUpdate = 1;

            PaintView(List.NeedsRedraw);
            OldRow = Row;
            OldTopRow = TopRow;
            OldLeftCol = LeftCol;
            OldCount = List.Count;
            List.NeedsUpdate = 0;
            List.NeedsRedraw = 0;
        }
    }

    void RepaintView() {
        PaintView(1);
        OldRow = Row;
        OldTopRow = TopRow;
        OldLeftCol = LeftCol;
        OldCount = List.Count;
        List.NeedsUpdate = 0;
        List.NeedsRedraw = 0;
    }

    void PaintView(int PaintAll) {
        TDrawBuffer B;
        int I;
        int /*ChColor*/ color;

        if (List.NeedsRedraw!=0)
            PaintAll = 1;
        
        if (View == null || View.MView == null || View.MView.Win == null)
            return ;
        
        int W, H;
        {
            int [] Wp, Hp;

            View.MView.ConQuerySize(Wp, Hp);
            
            H = Hp[0];
            W = Wp[0];
        }
        
        H--;

        if (View.MView.Win.GetViewContext() != View.MView)
            return;
        for (I = 0; I < H; I++) {
            if (PaintAll!=0 || I + TopRow == Row || I + TopRow == OldRow) {
                boolean mark = List.IsMarked(I + TopRow);
                boolean hilit = List.IsHilited(I + TopRow);
                color = ((Row == I + TopRow) && View.MView.Win.IsActive())
                    ? (mark ? (hilit ? hcList_MarkHilitSel : hcList_MarkSelect ):
                       (hilit ? hcList_HilitSelect : hcList_Selected)) :
                    (mark ? (hilit ? hcList_MarkHilit : hcList_Marked) :
                     (hilit ? hcList_Hilited : hcList_Normal));
                MoveChar(B, 0, W, ' ', color, W);
                if (I + TopRow < List.Count)
                    List.DrawLine(B, I + TopRow, LeftCol, color, W);
                View.MView.ConPutBox(0, I, W, 1, B);
            }
        }
    }

    void UpdateStatus() {
        RepaintStatus();
    }

    void RepaintStatus() {
        TDrawBuffer B;
        //char s[80];
        char SColor;
        
        if (View == null || View.MView == null || View.MView.Win == null)
            return ;
        
        int W, H;
        {
            int [] Wp, Hp;

            View.MView.ConQuerySize(Wp, Hp);
            
            H = Hp[0];
            W = Wp[0];
        }
        
        List.UpdateList();
        
        List.FixPos();
        
        if (List.View == View)
            GetPos();
        
        if (View.MView.Win.GetStatusContext() != View.MView)
            return;
        
        View.MView.Win.SetSbVPos(TopRow, H, List.Count + (Config.WeirdScroll ? H - 1 : 0));
        View.MView.Win.SetSbHPos(LeftCol, W, 1024 + (Config.WeirdScroll ? W - 1 : 0));
        
        if (View.MView.IsActive()) // hack
            SColor = hcStatus_Active;
        else
            SColor = hcStatus_Normal;
        MoveCh(B, ' ', SColor, W);
        if (View.CurMsg == 0) {
            if (List.Title)
                MoveStr(B, 0, W, List.Title, SColor, W);
            String s = String.format("%c%d/%d", ConGetDrawChar(DCH_V), Row + 1, List.Count);
            MoveStr(B, W - s.length(), W, s, SColor, W);
        } else {
            MoveStr(B, 0, W, View.CurMsg, SColor, W);
        }
        View.MView.ConPutBox(0, H - 1, W, 1, B);

        if (View.MView.Win.GetStatusContext() == View.MView &&
            View.MView.Win.IsActive())
            View.MView.Win.ConSetCursorPos(0, Row - TopRow);
    }
    
}
