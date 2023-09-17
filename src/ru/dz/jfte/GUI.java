package ru.dz.jfte;

public class GUI implements GuiDefs 
{
	static GFrame frames = null;
	static GUI gui = null;

	void deleteFrame(GFrame frame) {
	    if (frame.isLastFrame()) {
	        frames = null;
	    } else {
	        //frame.Prev.Next = frame.Next;
	        //frame.Next.Prev = frame.Prev;
	        //if (frames == frame)
	        //    frames = frame.Next;

	        //frames.Activate();
	    }
	}

	int Start(String [] args) {
	    return 0;
	}

	void Stop() {
	}

	void StopLoop() {
	    doLoop = 0;
	}

	int ConGrabEvents(TEventMask EventMask) {
	    return 0;
	}

	void DispatchEvent(GFrame frame, GView view, TEvent Event) {
	    if (Event.What != evNone) {
	        if (view != null)
	            view.HandleEvent(Event);
	    }
	}

	int ConGetEvent(TEventMask EventMask, TEvent Event, int WaitTime, int Delete, GView []view) {
	    if (view != null)
	        view[0] = null;
	    return ::ConGetEvent(EventMask, Event, WaitTime, Delete);
	}

	int ConPutEvent(TEvent Event) {
	    return ::ConPutEvent(Event);
	}

	int ConFlush() {
	    return 0;
	}

	GUI(String args, int XSize, int YSize) { /*FOLD00*/
	    fArgc = argc;
	    fArgv = argv;
	    ::ConInit(-1, -1);
	    SaveScreen();
	    ::ConSetSize(XSize, YSize);
	    gui = this;
	}

	/** TODO ~GUI() { 
	    RestoreScreen();

	    if (SavedScreen)
	        free(SavedScreen);

	    ::ConDone();
	    gui = 0;
	} */

	int ConSuspend() { /*FOLD00
	    RestoreScreen();
	    return ::ConSuspend(); */
	    return 1;
	}

	int ConContinue() { /*FOLD00
	    SaveScreen();
	    return ::ConContinue(); */
	    return 1;
	}

	int ShowEntryScreen() { /*FOLD00
	    TEvent E;

	    ConHideMouse();
	    RestoreScreen();
	    SetConsoleActiveScreenBuffer(ConOut);
	    do { gui.ConGetEvent(evKeyDown, &E, -1, 1, 0); } while (E.What != evKeyDown);
	    SetConsoleActiveScreenBuffer(OurConOut);
	    ConShowMouse();
	    if (frames)
	        frames.Repaint();
	    */
	    return 1;
	}
	
	
}
