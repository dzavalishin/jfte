package ru.dz.jfte;

import java.io.Closeable;
import java.io.IOException;

public class ExView implements Closeable, KeyDefs, EventDefs, ModeDefs 
{
    GxView Win;
    protected ExView Next;

	@Override
	public void close()  {
		
	}
    
    
    boolean IsModelView() { return false; }
    void WnSwitchBuffer(EModel M) { Next.WnSwitchBuffer(M); }

    void Repaint() { RepaintStatus(); RepaintView(); }
    void Update() { UpdateStatus(); UpdateView(); }

    int GetContext() { return CONTEXT_NONE; }
    ExView GetViewContext() { return this; }
    ExView GetStatusContext() { return this; }

    

    void Activate(boolean gotfocus) {
    }

    boolean IsActive() {
        if (Win != null)
            return Win.IsActive();
        return false;
    }

    EEventMap GetEventMap() { return null; }

    ExResult ExecCommand(ExCommands Command, ExState State) { return ExResult.ErFAIL; }

    int BeginMacro() {
        return 1;
    }

    void HandleEvent(TEvent Event) throws IOException
    {
        if (Event.What == evKeyDown && KeyDefs.kbCode(((TKeyEvent)Event).Code) == kbF12)
            Win.Parent.SelectNext(0);
    }

    void EndExec(int NewResult)  {
        if (Win.Result == -2) { // hack
            Win.EndExec(NewResult);
        } else {
            if (Next != null) {
                Win.PopView().close(); // self
            }
        }
    }

    void UpdateView() {
    }

    void UpdateStatus() {
    }

    void RepaintView() {
    }

    void RepaintStatus() {
    }

    void Resize(int width, int height) {
        Repaint();
    }

    int ConPutBox(int X, int Y, int W, int H, PCell Cell) {
        if (Win != null)
            return Win.ConPutBox(X, Y, W, H, Cell);
        return -1;
    }

    int ConScroll(int Way, int X, int Y, int W, int H, int /*TAttr*/ Fill, int Count) {
        if (Win != null)
            return Win.ConScroll(Way, X, Y, W, H, Fill, Count);
        return -1;
    }

    int ConQuerySize(int []X, int []Y) {
        if (Win != null)
            return Win.ConQuerySize(X, Y);
        return -1;
    }

	public int ConWidth() {
	    int []X= {0};
	    int []Y= {0};
	    
	    if (Win != null)
	    {
	    	Win.ConQuerySize(X, Y);
	    	return X[0];
	    }
	    
		throw new RuntimeException("Win.ConQuerySize");
	}

	public int ConHeight() {
	    int []X= {0};
	    int []Y= {0};
	    
	    if (Win != null)
	    {
	    	Win.ConQuerySize(X, Y);
	    	return Y[0];
	    }
	    
		throw new RuntimeException("Win.ConQuerySize");
	}
    
    
    void ConSetCursorPos(int X, int Y) {
        if (Win != null)
            Win.ConSetCursorPos(X, Y);
    }

    int ConShowCursor() {
        if (Win != null)
            return Win.ConShowCursor();
        return -1;
    }

    int ConHideCursor() {
        if (Win != null)
            return Win.ConHideCursor();
        return -1;
    }

    int ConSetCursorSize(int Start, int End) {
        if (Win != null)
            return Win.ConSetCursorSize(Start, End);
        return -1;
    }
    
    
}
