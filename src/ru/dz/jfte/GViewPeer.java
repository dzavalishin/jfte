package ru.dz.jfte;

/**
 * 
 * must be subclasses - now just text mode
 * 
 * @author dz
 *
 */

public class GViewPeer implements ColorDefs, EventDefs 
{
    GView View;
    int wX, wY, wW, wH, wState;
    int cX, cY, cStart, cEnd;
    boolean cVisible = true;
    int sbVstart, sbVamount, sbVtotal, sbVupdate;
    int sbHstart, sbHamount, sbHtotal, sbHupdate;
    int SbVBegin, SbVEnd, SbHBegin, SbHEnd;

    static GView MouseCapture = null;
    static GView FocusCapture = null;
	
    public static final int sfFocus   = 1;

	
    
    GViewPeer(GView view, int XSize, int YSize) {
        View = view;
        wX = 0;
        wY = 0;
        wW = XSize;
        wH = YSize;
        sbVtotal = 0;
        sbVstart = 0;
        sbVamount = 0;
        sbVupdate = 1;
        sbHtotal = 0;
        sbHstart = 0;
        sbHamount = 0;
        sbHupdate = 1;
        wState = 0;
        cStart = 0; // %
        cEnd = 100;
        cX = cY = 0;
    }

    /* TODO ~GViewPeer() {
        if (MouseCapture == View)
            MouseCapture = 0;
        if (FocusCapture == View)
            FocusCapture = 0;
    } */

    int ConPutBox(int X, int Y, int W, int H, PCell Cell) {
        return Console.ConPutBox(X + wX, Y + wY, W, H, Cell);
    }

    int ConGetBox(int X, int Y, int W, int H, PCell Cell) {
        return Console.ConGetBox(X + wX, Y + wY, W, H, Cell);
    }

    int ConPutLine(int X, int Y, int W, int H, PCell Cell) {
        return Console.ConPutLine(X + wX, Y + wY, W, H, Cell);
    }

    int ConSetBox(int X, int Y, int W, int H, PCell Cell) {
        return Console.ConSetBox(X + wX, Y + wY, W, H, Cell);
    }

    int ConScroll(int Way, int X, int Y, int W, int H, int /*TAttr*/ Fill, int Count) {
        return Console.ConScroll(Way, X + wX, Y + wY, W, H, Fill, Count);
    }
        
    int ConSetSize(int X, int Y) {
        wW = X;
        wH = Y;
        return 1;
    }

    int ConQuerySize(int []X, int []Y) {
        X[0] = wW;
        Y[0] = wH;
        return 1;
    }

    int ConSetCursorPos(int X, int Y) {
        if (X < 0) X = 0;
        if (X >= wW) X = wW - 1;
        if (Y < 0) Y = 0;
        if (Y >= wH) Y = wH - 1;
        cX = X;
        cY = Y;
        if(0 != (wState & sfFocus))
            return Console.ConSetCursorPos(cX + wX, cY + wY);
        else
            return 1;
    }

    int ConQueryCursorPos(int []X, int []Y) {
        X[0] = cX;
        Y[0] = cY;
        return 1;
    }

    int ConShowCursor() {
        cVisible = true;
        if(0 != (wState & sfFocus))
            return Console.ConShowCursor();
        else
            return 1;
    }

    int ConHideCursor() {
        cVisible = false;
        if(0 != (wState & sfFocus))
            return Console.ConHideCursor();
        else
            return 1;
    }

    boolean ConCursorVisible() {
        return cVisible;
    }

    int ConSetCursorSize(int Start, int End) {
        cStart = Start;
        cEnd = End;
        if(0 != (wState & sfFocus))
            return Console.ConSetCursorSize(Start, End);
        else
            return 1;
    }

    int CaptureMouse(boolean grab) {
        if (MouseCapture == null) {
            if (grab)
                MouseCapture = View;
            else
                return 0;
        } else {
            if (grab || MouseCapture != View)
                return 0;
            else
                MouseCapture = null;
        }
        return 1;
    }

    int CaptureFocus(boolean grab) {
        if (FocusCapture == null) {
            if (grab)
                FocusCapture = View;
            else
                return 0;
        } else {
            if (grab || FocusCapture != View)
                return 0;
            else
                FocusCapture = null;
        }
        return 1;
    }

    int ExpandHeight(int DeltaY) {
        if (View.Parent.Top == View.Next)
            return -1;
        if (DeltaY + wH < 3)
            DeltaY = - (wH - 3);
        if (View.Next.Peer.wH - DeltaY < 3)
            DeltaY = View.Next.Peer.wH - 3;
        View.Peer.ConSetSize(wW, wH + DeltaY);
        View.Next.Peer.wY += DeltaY;
        View.Next.Peer.ConSetSize(View.Next.Peer.wW, View.Next.Peer.wH - DeltaY);
        View.Resize(View.Peer.wW, View.Peer.wH);
        View.Next.Resize(View.Next.Peer.wW, View.Next.Peer.wH);
        return 0;
    }

    int QuerySbVPos() {
        return sbVstart;
    }

    int SetSbVPos(int Start, int Amount, int Total) {
        if (sbVstart != Start ||
            sbVamount != Amount ||
            sbVtotal != Total)
        {
            sbVstart = Start;
            sbVamount = Amount;
            sbVtotal = Total;
            sbVupdate = 1;
//            DrawScrollBar();
        }
        return 1;
    }

    int SetSbHPos(int Start, int Amount, int Total) {
        if (sbHstart != Start ||
            sbHamount != Amount ||
            sbHtotal != Total)
        {
            sbHstart = Start;
            sbHamount = Amount;
            sbHtotal = Total;
            sbHupdate = 1;
//            DrawScrollBar();
        }
        return 1;
    }

    int UpdateCursor() {
        ConSetCursorPos(cX, cY);
        ConSetCursorSize(cStart, cEnd);
        if (cVisible)
            ConShowCursor();
        else
            ConHideCursor();
        return 0;
    }

    int DrawScrollBar() {
        TDrawBuffer B = new TDrawBuffer();
        int NRows, NCols, I;
        char fore = Console.ConGetDrawChar(DCH_HFORE);
        char back = Console.ConGetDrawChar(DCH_HBACK);
        
        int W, H;
        {
            int [] Wp = {0}, Hp = {0};
            ConQuerySize(Wp, Hp);
            W = Wp[0];
            H = Hp[0];
        }

        if (GFrame.ShowVScroll) {
            B.MoveCh(Console.ConGetDrawChar(DCH_AUP), hcScrollBar_Arrows, 1);
            ConPutBox(W, 0, 1, 1, B);
            B.MoveCh(Console.ConGetDrawChar(DCH_ADOWN), hcScrollBar_Arrows, 1);
            ConPutBox(W, H - 1, 1, 1, B);
            
            NRows = H - 2;
            
            if (sbVtotal <= NRows) {
                SbVBegin = 0;
                SbVEnd = NRows - 1;
            } else {
                SbVBegin = NRows * sbVstart / sbVtotal;
                SbVEnd   = SbVBegin + NRows * sbVamount / sbVtotal;
            }
            
            for (I = 0; I < NRows; I++) {
                if (I >= SbVBegin && I <= SbVEnd)
                    B.MoveCh(fore, hcScrollBar_Fore, 1);
                else
                	B.MoveCh(back, hcScrollBar_Back, 1);
                ConPutBox(W, I + 1, 1, 1, B);
            }
        }
        if (GFrame.ShowHScroll) {
            B.MoveCh(Console.ConGetDrawChar(DCH_ALEFT), hcScrollBar_Arrows, 1);
            ConPutBox(0, H, 1, 1, B);
            B.MoveCh(Console.ConGetDrawChar(DCH_ARIGHT), hcScrollBar_Arrows, 1);
            ConPutBox(W - 1, H, 1, 1, B);
            
            NCols = W - 2;
            
            if (sbHtotal <= NCols) {
                SbHBegin = 0;
                SbHEnd = NCols - 1;
            } else {
                SbHBegin = NCols * sbHstart / sbHtotal;
                SbHEnd   = SbHBegin + NCols * sbHamount / sbHtotal;
            }

            // could be made faster
            for (I = 0; I < NCols; I++) {
                if (I >= SbHBegin && I <= SbHEnd)
                    B.MoveCh(fore, hcScrollBar_Fore, 1);
                else
                    B.MoveCh(back, hcScrollBar_Back, 1);
                ConPutBox(I + 1, H, 1, 1, B);
            }
        }
        if (GFrame.ShowVScroll && GFrame.ShowHScroll) {
            B.MoveCh(' ', hcScrollBar_Arrows, 1);
            ConPutBox(W, H, 1, 1, B);
        }
            
        return 0;
    }
    
}
