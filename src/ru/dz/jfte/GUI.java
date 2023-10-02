package ru.dz.jfte;

import java.io.IOException;

public abstract class GUI implements GuiDefs, EventDefs 
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
			frame.close();
		}
	}

	int Start(String [] args) {
		return 0;
	}

	void Stop() {
	}

	void StopLoop() {
		doLoop = false;
	}

	int ConGrabEvents(int /*TEventMask*/ EventMask) {
		return 0;
	}

	void DispatchEvent(GFrame frame, GView view, TEvent Event) throws IOException {
		if (Event.What != evNone) {
			if (view != null)
				view.HandleEvent(Event);
		}
	}


	static TEvent ConGetEvent(int /*TEventMask*/ EventMask, int WaitTime, int Delete) {
		return Console.ConGetEvent(EventMask, WaitTime, Delete!=0);
	}

	TEvent ConGetEvent(int /*TEventMask*/ EventMask, int WaitTime, int Delete, GView []view) {
		if (view != null)
			view[0] = null;
		return Console.ConGetEvent(EventMask, WaitTime, Delete!=0);
	}

	int ConPutEvent(TEvent Event) {
		return Console.ConPutEvent(Event);
	}

	int ConFlush() {
		return 0;
	}

	GUI(String []args, int XSize, int YSize) { /*FOLD00*/
		fArgv = args;
		Console.ConInit(-1, -1);
		//SaveScreen();
		Console.ConSetSize(XSize, YSize);
		gui = this;
	}

	/**  ~GUI() { 
	    RestoreScreen();

	    if (SavedScreen)
	        free(SavedScreen);

	    ::ConDone();
	    gui = 0;
	} 
	 * @throws IOException */

	
    abstract ExResult ExecCommand(GxView view, ExCommands Command, ExState State) throws IOException; 
	
	
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

	ExResult ShowEntryScreen() { 
		/*
	    TEvent E;

	    Console.ConHideMouse();
	    Console.RestoreScreen();
	    SetConsoleActiveScreenBuffer(ConOut);
	    do { E = gui.ConGetEvent(evKeyDown, -1, 1, null); } while (E.What != evKeyDown);
	    SetConsoleActiveScreenBuffer(OurConOut);
	    Console.ConShowMouse();
	    */
	    if (frames != null)
	        frames.Repaint();
	 
		return ExResult.ErOK;
	}


	static TEvent NextEvent = new TEmptyEvent();
	void ProcessEvent()  {
		try {
			doProcessEvent();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	void doProcessEvent() throws IOException  {
		TEvent E = NextEvent;

		E = NextEvent.clone();

		if (E.What != evNone) {
			NextEvent.What = evNone;
		}

		frames.Update(); // [dz] added by me. delete?  no, black screen.

		if (E.What == evNone &&
				( (E = Console.ConGetEvent(evMouse | evCommand | evKeyboard, 0, true)) == null ||
				E.What == evNone )
				)
		{
			frames.Update();
			while( (E=Console.ConGetEvent(evMouse | evCommand | evKeyboard, -1, true)) == null ||
					(E.What == evMouseMove && ((TMouseEvent)E).Buttons == 0));
		}
		if (E.What != evNone) {
			GView view = frames.Active;

			if( 0 != (E.What & evMouse)) 
			{
				TMouseEvent mE = (TMouseEvent) E;
				if (E.What == evMouseDown && mE.Y == 0 && GFrame.ShowMenuBar &&
						GViewPeer.MouseCapture == null && GViewPeer.FocusCapture == null)
				{
					frames.Update(); // sync before menu
					if (UpMenu.ExecMainMenu(E, (char)0) == -1) {
						if (E.What == evCommand && ((TMsgEvent)E).Command == cmResize) {
							int [] X = {0}, Y = {0};
							Console.ConQuerySize(X, Y);
							frames.Resize(X[0], Y[0]);
						}
						E.What = evNone;
					}
					//	                fprintf(stderr, "Command got = %d\n", E.Msg.Command);
				}
				if (E.What == evMouseDown && GViewPeer.MouseCapture == null && GViewPeer.FocusCapture == null) {
					GView V = frames.Active;

					while (V!=null) {
						if (mE.Y >= V.Peer.wY &&
								mE.Y <  V.Peer.wY + V.Peer.wH + (GFrame.ShowHScroll ? 1 : 0))
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
				if (GFrame.ShowVScroll && GFrame.ShowHScroll && E.What == evMouseDown &&
						GViewPeer.MouseCapture == null && GViewPeer.FocusCapture == null &&
						mE.Y == view.Peer.wY + view.Peer.wH &&
						mE.X == view.Peer.wX + view.Peer.wW)
				{
				} else {
					if (GFrame.ShowVScroll && E.What == evMouseDown && GViewPeer.MouseCapture == null && GViewPeer.FocusCapture == null &&
							mE.X == view.Peer.wX + view.Peer.wW)
					{
						HandleVScroll(view, (TMouseEvent) E);
						return ;
					}
					if (GFrame.ShowHScroll && E.What == evMouseDown && GViewPeer.MouseCapture == null && GViewPeer.FocusCapture == null &&
							mE.Y == view.Peer.wY + view.Peer.wH)
					{
						HandleHScroll(view, (TMouseEvent) E);
						return ;
					}
				}
				if(0 != (E.What & evMouse)) {
					mE.Y -= view.Peer.wY;
					mE.X -= view.Peer.wX;
				}
			}

			if (E.What == evCommand) 
			{
				TMsgEvent mE = (TMsgEvent) E;
				switch (mE.Command) {
				case cmResize: 
				{
					int [] X = {0}, Y = {0};
					Console.ConQuerySize(X, Y);
					frames.Resize(X[0], Y[0]);
				}
				break;
				case cmMainMenu:
				{
					char Sub = (char)mE.Param1;

					frames.Update(); // sync before menu
					if (UpMenu.ExecMainMenu(E, Sub) != 1) {//;
						if (E.What == evCommand && mE.Command == cmResize) {
							int [] X = {0}, Y = {0};
							Console.ConQuerySize(X, Y);
							frames.Resize(X[0], Y[0]);
						}
						E.What = evNone;
					}
				}
				break;
				case cmPopupMenu:
				{
					int id = (int) mE.Param1;
					int [] Cols = {0}, Rows = {0};

					if (id == -1) return;
					frames.ConQuerySize(Cols, Rows);
					int [] x = {Cols[0] / 2};
					int [] y = {Rows[0] / 2};
					Console.ConQueryMousePos(x, y);

					frames.Update(); // sync before menu
					if (UpMenu.ExecVertMenu(x[0], y[0], id, E, null) != 1) {
						if (E.What == evCommand && mE.Command == cmResize) {
							int [] X = {0}, Y = {0};
							Console.ConQuerySize(X, Y);
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






















	static void HandleVScroll(GView view, TMouseEvent E) throws IOException {
		int y; //, x
		int wY, wH;
		TEvent E1;

		//x = E.Mouse.X;
		y = E.Y;
		wY = view.Peer.wY;
		wH = view.Peer.wH;
		if (y == wY) {
			do {
				//E1.What = evCommand;
				//E1.Msg.View = view;
				//E1.Msg.Command = cmVScrollUp;
				E1 = new TMsgEvent(evCommand, view, cmVScrollUp);
				gui.DispatchEvent(frames, view, E1);
				//E = localDispatch(view);
				if (scrollBreak(localDispatch(view))) break;
			} while (true);
		} else if (y == wY + wH - 1) {
			do {
				//E1.What = evCommand;
				//E1.Msg.View = view;
				//E1.Msg.Command = cmVScrollDown;
				E1 = new TMsgEvent(evCommand, view, cmVScrollDown);
				gui.DispatchEvent(frames, view, E1);
				//E = localDispatch(view);
				if (scrollBreak(localDispatch(view))) break;
			} while (true);
		} else if (y < wY + view.Peer.SbVBegin + 1) {
			do {
				//E1.What = evCommand;
				//E1.Msg.View = view;
				//E1.Msg.Command = cmVScrollPgUp;
				E1 = new TMsgEvent(evCommand, view, cmVScrollPgUp);
				gui.DispatchEvent(frames, view, E1);
				//E = localDispatch(view);
				if (scrollBreak(localDispatch(view))) break;
			} while (true);
		} else if (y > wY + view.Peer.SbVEnd + 1) {
			do {
				//E1.What = evCommand;
				//E1.Msg.View = view;
				//E1.Msg.Command = cmVScrollPgDn;
				E1 = new TMsgEvent(evCommand, view, cmVScrollPgDn);
				gui.DispatchEvent(frames, view, E1);
				//E = localDispatch(view);
				if (scrollBreak(localDispatch(view))) break;
			} while (true);
		} else {
			int delta = y - 1 - view.Peer.SbVBegin - wY;

			do {
				//E1.What = evCommand;
				//E1.Msg.View = view;
				//E1.Msg.Command = cmVScrollMove;
				/*E1.Msg.*/ int Param1 = (E.Y - wY - 1 - delta + 1) * view.Peer.sbVtotal / (wH - 2);
				E1 = new TMsgEvent(evCommand, view, cmVScrollMove, Param1 );
				//	            printf("YPos = %d %d %d \n\x7", E.Mouse.Y, wY, delta);
				gui.DispatchEvent(frames, view, E1);
				//E = localDispatch(view);
				if (scrollBreak(localDispatch(view))) break;
			} while (true);
		}
		E.What = evNone;
	}

	static void HandleHScroll(GView view, TMouseEvent E) throws IOException {
		int x; //, x
		int wX, wW;
		TEvent E1;

		//x = E.Mouse.X;
		x = E.X;
		wX = view.Peer.wX;
		wW = view.Peer.wW;
		if (x == wX) {
			do {
				//E1.What = evCommand;
				//E1.Msg.View = view;
				//E1.Msg.Command = cmHScrollLeft;
				E1 = new TMsgEvent(evCommand, view, cmHScrollLeft);
				gui.DispatchEvent(frames, view, E1);
				//E = localDispatch(view);
				if (scrollBreak(localDispatch(view))) break;
			} while (true);
		} else if (x == wX + wW - 1) {
			do {
				//E1.What = evCommand;
				//E1.Msg.View = view;
				//E1.Msg.Command = cmHScrollRight;
				E1 = new TMsgEvent(evCommand, view, cmHScrollRight);
				gui.DispatchEvent(frames, view, E1);
				//E = localDispatch(view);
				if (scrollBreak(localDispatch(view))) break;
			} while (true);
		} else if (x < wX + view.Peer.SbHBegin + 1) {
			do {
				//E1.What = evCommand;
				//E1.Msg.View = view;
				//E1.Msg.Command = cmHScrollPgLt;
				E1 = new TMsgEvent(evCommand, view, cmHScrollPgLt);
				gui.DispatchEvent(frames, view, E1);
				//E = localDispatch(view);
				if (scrollBreak(localDispatch(view))) break;
			} while (true);
		} else if (x > wX + view.Peer.SbHEnd + 1) {
			do {
				//E1.What = evCommand;
				//E1.Msg.View = view;
				//E1.Msg.Command = cmHScrollPgRt;
				E1 = new TMsgEvent(evCommand, view, cmHScrollPgRt);
				gui.DispatchEvent(frames, view, E1);
				//E = localDispatch(view);
				if (scrollBreak(localDispatch(view))) break;
			} while (true);
		} else {
			int delta = x - 1 - view.Peer.SbHBegin - wX;

			do {
				//E1.What = evCommand;
				//E1.Msg.View = view;
				//E1.Msg.Command = cmHScrollMove;
				/*E1.Msg.*/ int Param1 = (E.X - wX - 1 - delta + 1) * view.Peer.sbHtotal / (wW - 2);
				E1 = new TMsgEvent(evCommand, view, cmHScrollMove, Param1);
				//	            printf("YPos = %d %d %d \n\x7", E.Mouse.Y, wY, delta);
				gui.DispatchEvent(frames, view, E1);
				//E = localDispatch(view);
				if (scrollBreak(localDispatch(view))) break;
			} while (true);
		}
		E.What = evNone;
	}

	/**
	 * Strange stuff...
	 */
	private static TEvent localDispatch(GView view) throws IOException {
		TEvent E;
		frames.Update();
		do {
			E = ConGetEvent(evMouse | evNotify, -1, 1);
			if(0 != (E.What & evNotify))
				gui.DispatchEvent(frames, view, E);
		} while(0 != (E.What & evNotify));
		return E;
	}


	static boolean scrollBreak(TEvent E)
	{
		return (E.What == evMouseUp);
	}

	public void RunProgram(int runWait, String string) {
		Console.RunProgram(runWait,string,frames);
	}














}
