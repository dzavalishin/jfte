package ru.dz.jfte;

import java.io.Closeable;
import java.io.IOException;

public class ExASCII extends ExView implements Closeable, KeyDefs, EventDefs, ColorDefs 
{
    private int Pos, LPos;
    
    
    static int SPos = 0;
    static int SLPos = 0;

    
    
    ExASCII() {
        Pos = SPos;
        LPos = SLPos;
    }

    
    
    
    @Override
    public void close() {
        SPos = Pos;
        SLPos = LPos;
    }

    @Override
    ExView GetViewContext() { return Next; }

    @Override
    void Activate(boolean gotfocus) {
        super.Activate(gotfocus);
    }

    @Override
    int BeginMacro() {
        return 1;
    }

    @Override
    void HandleEvent(TEvent Event) throws IOException {
        int [] W = {0}, H = {0};
        
        ConQuerySize(W, H);
        
        switch (Event.What) {
        case evKeyDown:
            switch( KeyDefs.kbCode( ((TKeyEvent)Event).Code ) ) 
            {
            case kbLeft:           Pos--; Event.What = evNone; break;
            case kbRight:          Pos++; Event.What = evNone; break;
            case kbHome:           Pos = 0; Event.What = evNone; break;
            case kbEnd:            Pos = 255; Event.What = evNone; break;
            case kbLeft + kfCtrl:  Pos -= 16; Event.What = evNone; break;
            case kbRight + kfCtrl: Pos += 16; Event.What = evNone; break;
            case kbUp:             Pos -= W[0]; LPos -= W[0]; Event.What = evNone; break;
            case kbDown:           Pos += W[0]; LPos += W[0]; Event.What = evNone; break;
            case kbEsc:            EndExec(-1); Event.What = evNone; break;
            case kbEnter:          EndExec(Pos); Event.What = evNone; break;
            }
            break;
    /*
        case evMouseDown:
            if (E.Mouse.X < XPos || E.Mouse.X >= XPos + 34 ||
                E.Mouse.Y < YPos || E.Mouse.Y >= YPos + 10)
            {
                abort = 2;
                break;
            }
            
            do {
                x = E.Mouse.X - XPos - 1;
                y = E.Mouse.Y - YPos - 1;
                if (x >= 0 && x < 32 &&
                    y >= 0 && y < 8)
                {
                    X = x;
                    Y = y;
                    if (X >= 32) X = 31;
                    if (Y >= 8) Y = 7;
                    if (X < 0) X = 0;
                    if (Y < 0) Y = 0;
                    frames.ConSetCursorPos(X + XPos + 1, Y + YPos + 1);
                    sprintf(s, "0%03o %03d 0x%02X",
                            X + Y * 32, X + Y * 32, X + Y * 32);
                    MoveStr(B, 0, 13, s, hcAsciiStatus, 13);
                    frames.ConPutBox(XPos + 2, YPos + 9, 13, 1, B);
                }
                if (E.Mouse.Count == 2) {
                    abort = 1;
                    break;
                }
                gui.ConGetEvent(evMouse, &E, -1, 1);
                if (E.What == evMouseUp) break;
            } while (1);
            break;
    */
        }
    }

    @Override
    void UpdateView() {
        if (Next != null) {
            Next.UpdateView();
        }
    }

    @Override
    void RepaintView() {
        if (Next != null) {
            Next.RepaintView();
        }
    }

    @Override
    void UpdateStatus() {
        RepaintStatus();
    }


    @Override
    void RepaintStatus() {
        TDrawBuffer B = new TDrawBuffer();
        int [] W = {0}, H = {0};
        
        ConQuerySize(W, H);
        
        if (Pos > 255) Pos = 255;
        if (Pos < 0) Pos = 0;
        if (LPos + W[0] < Pos) LPos = Pos - W[0] + 1;
        if (LPos > 255 - W[0]) LPos = 255 - W[0] + 1;
        if (LPos > Pos) LPos = Pos;
        if (LPos < 0) LPos = 0;
        
        for (int i = 0; i < W[0]; i++)
        	new PCell(B,i).MoveCh( /*B + i, */ (char)(i + LPos), hcAsciiChars, 1);
        ConSetCursorPos(Pos - LPos, H[0] - 1);
        ConShowCursor();
        ConPutBox(0, H[0] - 1, W[0], 1, B);
    }
    
}
