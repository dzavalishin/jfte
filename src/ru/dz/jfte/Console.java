package ru.dz.jfte;

public class Console {

	static TEvent ReadConsoleEvent() { 
		// TODO impl me
		return null;
	}

	static int ConInit(int XSize, int YSize) {
		
	}
	
	static int ConDone()
	{
		
	}
	
	
	static int ConClear()
	{
		
	}
	
	static int ConPutBox(int X, int Y, int W, int H, PCell Cell) /*FOLD00*/
	{
	}
	
	
	static int ConGetBox(int X, int Y, int W, int H, PCell Cell) /*FOLD00*/
	{
	}

	static int ConPutLine(int X, int Y, int W, int H, PCell Cell) /*FOLD00*/
	{
	}

	static int ConSetBox(int X, int Y, int W, int H, TCell Cell) /*FOLD00*/
	{
	}
	
	
	static int ConScroll(int Way, int X, int Y, int W, int H, int /*TAttr*/ Fill, int Count) /*FOLD00*/
	{
	}	
	
	
	static int ConSetSize(int X, int Y) { /*FOLD00*/
	    return -1;
	}
	

	static int ConQuerySize(int []X, int []Y) { /*FOLD00*/
	}
	
	static int getWidth() { return width; } 
	static int getHeigh() { return height; } 
	
	static int ConSetCursorPos(int X, int Y) { /*FOLD00*/
	}
			
	
	static int ConQueryCursorPos(int []X, int []Y) { /*FOLD00*/
	}

	
	static int ConShowCursor() { /*FOLD00*/
	    CursorVisible = 1;
	    DrawCursor(1);
	    return 0;
	}

	static int ConHideCursor() { /*FOLD00*/
	    CursorVisible = 0;
	    DrawCursor(0);
	    return 0;
	}

	static int ConCursorVisible() { /*FOLD00*/
	    return (CursorVisible == 1);
	}

	static int ConSetCursorSize(int Start, int End) { /*FOLD00*/
	    return -1;
	}

	static int ConSetMousePos(int X, int Y) { /*FOLD00*/
	    return -1;
	}

	static int ConQueryMousePos(int []X, int []Y) { /*FOLD00*/
	    X[0] = LastMouseX;
	    Y[0] = LastMouseY;

	    // NT does not have this ? (not needed anyway, but check mouse hiding above).
	    return 0;
	}

	static int ConShowMouse() { /*FOLD00*/
	    MouseVisible = 1;
	    if (!MousePresent) return -1;
	    return 0;
	}

	static int ConHideMouse() { /*FOLD00*/
	    MouseVisible = 0;
	    if (!MousePresent) return -1;
	    return 0;
	}

	static int ConMouseVisible() { /*FOLD00*/
	    return (MouseVisible == 1);
	}

	static int ConQueryMouseButtons(int []ButtonCount) { /*FOLD00*/
	    return 0;
	}

	static int ConPutEvent(TEvent Event) { /*FOLD00*/
	    EventBuf = Event;
	    return 0;
	}

	static int ConFlush() { /*FOLD00*/
	    return 0;
	}

	static int ConGrabEvents(int /*TEventMask*/ EventMask) { /*FOLD00*/
	    return 0;
	}

	public static char ConGetDrawChar(int index) {
		// TODO Auto-generated method stub
		//return ;
	}
	
	
	

	
	static int GetDefaultDirectory(EModel M, String [] Path) {
	    if (M!=null) 
	        Path[0] = M.GetPath();
	    if (M==null || Path[0] == null)
	        if (ExpandPath(".", Path) == -1)
	            return 0;
	    SlashDir(Path);
	    return 1;
	}

	static int SetDefaultDirectory(EModel M) {
	    String [] Path;
	    
	    if (GetDefaultDirectory(M, Path) == 0)
	        return 0;
	    if (ChangeDir(Path) == -1)
	        return 0;
	    return 1;
	}
	
	
}
