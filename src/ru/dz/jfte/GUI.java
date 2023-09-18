package ru.dz.jfte;

public class GUI implements GuiDefs, EventDefs 
{
	String []fArgv;
	boolean doLoop = false;

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

	int ConGrabEvents(int /*TEventMask*/ EventMask) {
		return 0;
	}

	void DispatchEvent(GFrame frame, GView view, TEvent Event) {
		if (Event.What != evNone) {
			if (view != null)
				view.HandleEvent(Event);
		}
	}

	int ConGetEvent(int /*TEventMask*/ EventMask, TEvent Event, int WaitTime, int Delete, GView []view) {
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

	GUI(String []args, int XSize, int YSize) { /*FOLD00*/
		fArgv = args;
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


	static TEvent NextEvent = null;
	void ProcessEvent() {
		TEvent E;

		E = NextEvent.clone();

		if (E.What != evNone) {
			NextEvent.What = evNone;
		}
		if (E.What == evNone &&
				( (E = ConGetEvent(evMouse | evCommand | evKeyboard, 0, 1, 0)) == null ||
				E.What == evNone )
				)
		{
			frames.Update();
			while( (E=ConGetEvent(evMouse | evCommand | evKeyboard, -1, 1, 0)) == null ||
					(E.What == evMouseMove && E.Mouse.Buttons == 0));
		}
		if (E.What != evNone) {
			GView view = frames.Active;

			if (E.What & evMouse) {
				if (E.What == evMouseDown && E.Mouse.Y == 0 && ShowMenuBar &&
						MouseCapture == 0 && FocusCapture == 0)
				{
					frames.Update(); // sync before menu
					if (ExecMainMenu(E, 0) == -1) {
						if (E.What == evCommand && E.Msg.Command == cmResize) {
							int [] X = {0}, Y = {0};
							ConQuerySize(X, Y);
							frames.Resize(X[0], Y[0]);
						}
						E.What = evNone;
					}
					//	                fprintf(stderr, "Command got = %d\n", E.Msg.Command);
				}
				if (E.What == evMouseDown && MouseCapture == 0 && FocusCapture == 0) {
					GView V = frames.Active;

					while (V!=null) {
						if (E.Mouse.Y >= V.Peer.wY &&
								E.Mouse.Y <  V.Peer.wY + V.Peer.wH + (ShowHScroll ? 1 : 0))
						{
							frames.SelectView(V);
							view = V;
							break;
						}
						V = V.Next;
						if (V == frames.Active)
							break;
					}
				}
				if (ShowVScroll && ShowHScroll && E.What == evMouseDown &&
						MouseCapture == 0 && FocusCapture == 0 &&
						E.Mouse.Y == view.Peer.wY + view.Peer.wH &&
						E.Mouse.X == view.Peer.wX + view.Peer.wW)
				{
				} else {
					if (ShowVScroll && E.What == evMouseDown && MouseCapture == 0 && FocusCapture == 0 &&
							E.Mouse.X == view.Peer.wX + view.Peer.wW)
					{
						HandleVScroll(view, E);
						return ;
					}
					if (ShowHScroll && E.What == evMouseDown && MouseCapture == 0 && FocusCapture == 0 &&
							E.Mouse.Y == view.Peer.wY + view.Peer.wH)
					{
						HandleHScroll(view, E);
						return ;
					}
				}
				if (E.What & evMouse) {
					E.Mouse.Y -= view.Peer.wY;
					E.Mouse.X -= view.Peer.wX;
				}
			}
			if (E.What == evCommand) {
				switch (E.Msg.Command) {
				case cmResize: 
				{
					int [] X = {0}, Y = {0};
					ConQuerySize(X, Y);
					frames.Resize(X[0], Y[0]);
				}
				break;
				case cmMainMenu:
				{
					char Sub = (char)E.Msg.Param1;

					frames.Update(); // sync before menu
					if (UpMenu.ExecMainMenu(E, Sub) != 1) {//;
						if (E.What == evCommand && E.Msg.Command == cmResize) {
							int [] X = {0}, Y = {0};
							ConQuerySize(X, Y);
							frames.Resize(X[0], Y[0]);
						}
						E.What = evNone;
					}
				}
				break;
				case cmPopupMenu:
				{
					int id = E.Msg.Param1;
					int [] Cols = {0}, Rows = {0};

					if (id == -1) return;
					frames.ConQuerySize(Cols, Rows);
					int x = Cols / 2, y = Rows / 2;
					::ConQueryMousePos(&x, &y);

					frames.Update(); // sync before menu
					if (::ExecVertMenu(x, y, id, E, 0) != 1) {
						if (E.What == evCommand && E.Msg.Command == cmResize) {
							int [] X = {0}, Y = {0};
							ConQuerySize(X, Y);
							frames.Resize(X[0], Y[0]);
						}
						E.What = evNone;
					}
				}
				break;
				}
			}
			if (E.What != evNone)
				DispatchEvent(frames, view, E);
		}
	}

	int Run() {
		if (Start(fArgv) == 0) {
			doLoop = true;
			while (doLoop)
				ProcessEvent();
			Stop();
			return 0;
		}
		return 1;
	}

	int multiFrame() {
		return 0;
	}


}
