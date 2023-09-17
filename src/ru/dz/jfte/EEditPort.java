package ru.dz.jfte;

import java.io.Closeable;

public class EEditPort extends EViewPort implements Closeable, EventDefs 
{
    EBuffer Buffer;
    EPoint TP, OldTP;
    EPoint CP;
    int Rows, Cols;

    
    
    EEditPort(EBuffer B, EView V) 
    {
    	super(V);
    	
        Buffer = B;
        Rows = Cols = 0;
        OldTP.Row = -1;
        OldTP.Col = -1;
        GetPos();
        TP = B.TP;
        CP = B.CP;
        if (V && V.MView && V.MView.Win) {
            V.MView.ConQuerySize(&Cols, &Rows);
            Rows--;
        }
    }

    public void close() {
        StorePos();
    }

    void Resize(int Width, int Height) {
        Cols = Width;
        Rows = Height - 1;
        RedrawAll();
    }

    int SetTop(int Col, int Line) {
        int A, B;

        if (Line >= Buffer.VCount) Line = Buffer.VCount - 1;
        if (Line < 0) Line = 0;

        A = Line;
        B = Line + Rows;

        TP.Row = Line;
        TP.Col = Col;

        if (A >= Buffer.VCount) A = Buffer.VCount - 1;
        if (B >= Buffer.VCount) {
            B = Buffer.VCount - 1;
        }
        Buffer.Draw(Buffer.VToR(A), -1);
        return 1;
    }

    void StorePos() {
        Buffer.CP = CP;
        Buffer.TP = TP;
    }

    void GetPos() {
        CP = Buffer.CP;
        TP = Buffer.TP;
    }

    void ScrollY(int Delta) {
        // optimization
        // no need to scroll (clear) entire window which we are about to redraw
        if (Delta >= Rows || -Delta >= Rows)
            return ;

        if (Delta < 0) {
            Delta = -Delta;
            if (Delta > Rows) return;
            View.MView.ConScroll(csDown, 0, 0, Cols, Rows, hcPlain_Background, Delta);
        } else {
            if (Delta > Rows) return;
            View.MView.ConScroll(csUp, 0, 0, Cols, Rows, hcPlain_Background, Delta);
        }
    }

    void DrawLine(int L, TDrawBuffer B) {
        if (L < TP.Row) return;
        if (L >= TP.Row + Rows) return;
        if (View.MView.Win.GetViewContext() == View.MView)
            View.MView.ConPutBox(0, L - TP.Row, Cols, 1, B);
        //    printf("%d %d (%d %d %d %d)\n", 0, L - TP.Row, view.sX, view.sY, view.sW, view.sH);
    }

    void RedrawAll() {
        Buffer.Draw(TP.Row, -1);
        ///    Redraw(0, 0, Cols, Rows);
    }


    void HandleEvent(TEvent Event) 
    {
        super.HandleEvent(Event);
        
        switch (Event.What) {
        case evKeyDown:
            {
                char Ch;
                if(0 != (Ch = GetCharFromEvent(Event))) {
                    if (Buffer.BeginMacro() == 0)
                        return ;
                    Buffer.TypeChar(Ch);
                    Event.What = evNone;
                }
            }
            break;
        case evCommand:
            switch (Event.Msg.Command) {
            case cmVScrollUp:
                Buffer.ScrollDown(1);
                Event.What = evNone;
                break;
            case cmVScrollDown:
                Buffer.ScrollUp(1);
                Event.What = evNone;
                break;
            case cmVScrollPgUp:
                Buffer.ScrollDown(Rows);
                Event.What = evNone;
                break;
            case cmVScrollPgDn:
                Buffer.ScrollUp(Rows);
                Event.What = evNone;
                break;
            case cmVScrollMove:
                {
                    int ypos;

//                    fprintf(stderr, "Pos = %d\n\x7", Event.Msg.Param1);
                    ypos = Buffer.CP.Row - TP.Row;
                    Buffer.SetNearPos(Buffer.CP.Col, Event.Msg.Param1 + ypos);
                    SetTop(TP.Col, Event.Msg.Param1);
                    RedrawAll();
                }
                Event.What = evNone;
                break;
            case cmHScrollLeft:
                Buffer.ScrollRight(1);
                Event.What = evNone;
                break;
            case cmHScrollRight:
                Buffer.ScrollLeft(1);
                Event.What = evNone;
                break;
            case cmHScrollPgLt:
                Buffer.ScrollRight(Cols);
                Event.What = evNone;
                break;
            case cmHScrollPgRt:
                Buffer.ScrollLeft(Cols);
                Event.What = evNone;
                break;
            case cmHScrollMove:
                {
                    int xpos;

                    xpos = Buffer.CP.Col - TP.Col;
                    Buffer.SetNearPos(Event.Msg.Param1 + xpos, Buffer.CP.Row);
                    SetTop(Event.Msg.Param1, TP.Row);
                    RedrawAll();
                }
                Event.What = evNone;
                break;
            }
            break;
        case evMouseDown:
        case evMouseMove:
        case evMouseAuto:
        case evMouseUp:
            HandleMouse(Event);
            break;
        }
    }
    void HandleMouse(TEvent event) {
        int x, y, xx, yy, W, H;

        //View.MView.ConQuerySize(&W, &H);
        W = View.MView.ConWidth();
        W = View.MView.ConHeight();

        x = event.X;
        y = event.Y;

        if (event.What != evMouseDown || y < H - 1) {
            xx = x + TP.Col;
            yy = y + TP.Row;
            if (yy >= Buffer.VCount) yy = Buffer.VCount - 1;
            if (yy < 0) yy = 0;
            if (xx < 0) xx = 0;

            switch (event.What) {
            case evMouseDown:
                if (event.Y == H - 1)
                    break;
                if (View.MView.Win.CaptureMouse(1))
                    View.MView.MouseCaptured = 1;
                else
                    break;

                View.MView.MouseMoved = 0;

                if (event.Buttons == 1) {
                    Buffer.SetNearPos(xx, yy);
                    switch (event.Count % 5) {
                    case 1:
                        break;
                    case 2:
                        Buffer.BlockSelectWord();
                        break;
                    case 3:
                        Buffer.BlockSelectLine();
                        break;
                    case 4:
                        Buffer.BlockSelectPara();
                        break;
                    }
                    //            Window.Buffer.Redraw();
                    if (SystemClipboard) {
                        Buffer.NextCommand();
                        Buffer.BlockCopy(0);
                    }
                    event.What = evNone;
                } else if (event.Buttons == 2) {
                    Buffer.SetNearPos(xx, yy);
                }
                break;
            case evMouseAuto:
            case evMouseMove:
                if (View.MView.MouseCaptured) {
                    if (event.Buttons == 1) {
                        if (!View.MView.MouseMoved) {
                            if (event.KeyMask == kfCtrl) Buffer.BlockMarkColumn();
                            else if (event.KeyMask == kfAlt) Buffer.BlockMarkLine();
                            else Buffer.BlockMarkStream();
                            Buffer.BlockUnmark();
                            if (event.What == evMouseMove)
                                View.MView.MouseMoved = 1;
                        }
                        Buffer.BlockExtendBegin();
                        Buffer.SetNearPos(xx, yy);
                        Buffer.BlockExtendEnd();
                    } else if (event.Buttons == 2) {
                        if (event.KeyMask == kfAlt) {
                        } else {
                            Buffer.SetNearPos(xx, yy);
                        }
                    }

                    event.What = evNone;
                }
                break;
    /*        case evMouseAuto:
                if (View.MView.MouseCaptured) {
                    Event.What = evNone;
                }
                break;*/
            case evMouseUp:
                if (View.MView.MouseCaptured)
                    View.MView.Win.CaptureMouse(0);
                else
                    break;
                View.MView.MouseCaptured = 0;
                if (event.Buttons == 1) {
                    if (View.MView.MouseMoved)
                        if (SystemClipboard) {
                            Buffer.NextCommand();
                            Buffer.BlockCopy(0);
                        }
                }
                if (event.Buttons == 2) {
                    if (!View.MView.MouseMoved) {
                        EEventMap *Map = View.MView.Win.GetEventMap();
                        const char *MName = 0;

                        if (Map)
                            MName = Map.GetMenu(EM_LocalMenu);
                        if (MName == 0)
                            MName = "Local";
                        View.MView.Win.Parent.PopupMenu(MName);
                    }
                }
                if (event.Buttons == 4) {
                    if (SystemClipboard) {
                        Buffer.NextCommand();
                        if (event.KeyMask == 0)
                            Buffer.BlockPasteStream();
                        else if (event.KeyMask == kfCtrl)
                            Buffer.BlockPasteColumn();
                        else if (event.KeyMask == kfAlt)
                            Buffer.BlockPasteLine();
                    }
                }
                event.What = evNone;
                break;
            }
        }
    }

    void UpdateView() {
        Buffer.Redraw();
    }

    void RepaintView() {
        RedrawAll();
    }

    void UpdateStatus() {
    }

    void RepaintStatus() {
        //Buffer.Redraw();
    }
    
}
