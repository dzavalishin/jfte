package ru.dz.jfte;

/**
 * 
 * Must be base class with implementations. for now just text mode
 * 
 * @author dz
 *
 */
public class GView {
    GFrame Parent;
    GView Next, Prev;
    GViewPeer Peer;
    int Result;


    
    GView(GFrame parent, int XSize, int YSize) {
        Parent = parent;
        Prev = Next = 0;
        Peer = new GViewPeer(this, XSize, YSize);
        if (Parent)
            Parent.AddView(this);
    }

    /* TODO ~GView() {
        if (Parent)
            Parent.RemoveView(this);
        delete Peer;
    } */

    int ConClear() {
        int W, H;
        TDrawBuffer B;
        
        ConQuerySize(&W, &H);
        MoveChar(B, 0, W, ' ', 0x07, 1);
        ConSetBox(0, 0, W, H, B[0]);
        return 1;
    }

    int ConPutBox(int X, int Y, int W, int H, PCell Cell) {
        return Peer.ConPutBox(X, Y, W, H, Cell);
    }

    int ConGetBox(int X, int Y, int W, int H, PCell Cell) {
        return Peer.ConGetBox(X, Y, W, H, Cell);
    }

    int ConPutLine(int X, int Y, int W, int H, PCell Cell) {
        return Peer.ConPutLine(X, Y, W, H, Cell);
    }

    int ConSetBox(int X, int Y, int W, int H, TCell Cell) {
        return Peer.ConSetBox(X, Y, W, H, Cell);
    }

    int ConScroll(int Way, int X, int Y, int W, int H, TAttr Fill, int Count) {
        return Peer.ConScroll(Way, X, Y, W, H, Fill, Count);
    }

    int ConSetSize(int X, int Y) {
        if (Peer.ConSetSize(X, Y))
            Resize(X, Y);
        else
            return 0;
        return 1;
    }

    int ConQuerySize(int []X, int []Y) {
        return Peer.ConQuerySize(X, Y);
    }

    int ConSetCursorPos(int X, int Y) {
        return Peer.ConSetCursorPos(X, Y);
    }

    int ConQueryCursorPos(int []X, int []Y) {
        return Peer.ConQueryCursorPos(X, Y);
    }

    int ConShowCursor() {
        return Peer.ConShowCursor();
    }

    int ConHideCursor() {
        return Peer.ConHideCursor();
    }

    int ConCursorVisible() {
        return Peer.ConCursorVisible(); 
    }

    int ConSetCursorSize(int Start, int End) {
        return Peer.ConSetCursorSize(Start, End);
    }

    int CaptureMouse(int grab) {
        return Peer.CaptureMouse(grab);
    }

    int CaptureFocus(int grab) {
        return Peer.CaptureFocus(grab);
    }

    int QuerySbVPos() {
        return Peer.QuerySbVPos();
    }

    int SetSbVPos(int Start, int Amount, int Total) {
        return Peer.SetSbVPos(Start, Amount, Total);
    }

    int SetSbHPos(int Start, int Amount, int Total) {
        return Peer.SetSbHPos(Start, Amount, Total);
    }

    int ExpandHeight(int DeltaY) {
        return Peer.ExpandHeight(DeltaY);
    }

    void Update() {
    }

    void Repaint() {
    }

    void HandleEvent(TEvent Event) {
    }

    void Resize(int width, int height) {
        Repaint();
    }

    void EndExec(int NewResult) {
        Result = NewResult;
    }

    int Execute() {
        int SaveRc = Result;
        int NewResult;
        int didFocus = 0;
        
        if (FocusCapture == 0) {
            if (CaptureFocus(1) == 0) return -1;
            didFocus = 1;
        } else
            if (FocusCapture != this)
                return -1;
        Result = -2;
        while (Result == -2 && frames != 0)
            gui.ProcessEvent();
        NewResult = Result;
        Result = SaveRc;
        if (didFocus)
            CaptureFocus(0);
        return NewResult;
    }

    int IsActive() {
        return (Parent.Active == this);
    }

    void Activate(int gotfocus) {
        if (gotfocus) {
            Peer.wState |= sfFocus;
            Peer.UpdateCursor();
        } else {
            Peer.wState &= ~sfFocus;
        }
        Repaint();
    }
    
    
    
    
    
}
