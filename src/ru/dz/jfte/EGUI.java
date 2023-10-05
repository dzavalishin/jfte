package ru.dz.jfte;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.logging.Level;
import java.util.logging.Logger;

public class EGUI extends GUI implements ModeDefs, GuiDefs, KeyDefs 
{
	private static final Logger log = Logger.getLogger(EGUI.class.getName());

	EKeyMap ActiveMap;
	EKeyMap OverrideMap;
	String CharMap = null;
	private EFrame eFrame; // dz added for GC not to kill

	static final int  RUN_WAIT = 0;
	static final int  RUN_ASYNC = 1;

	static int LastEventChar = -1;


	EGUI(String [] argv, int XSize, int YSize)
	{
		super(argv, XSize, YSize);
		ActiveMap = null;
		OverrideMap = null;
		CharMap = "";
		gui = this;
	}


	@Override
	ExResult ExecCommand(GxView view, ExCommands Command, ExState State) throws IOException 
	{

		if(0 != (Command.ordinal() & CMD_EXT))
			return ExecMacro(view, Command.ordinal() & ~CMD_EXT);


		if (Command == ExCommands.ExFail)
			return ExResult.ErFAIL;

		if (view.IsModelView()) {
			ExModelView V = (ExModelView)view.Top;
			EView View = V.View;

			switch (Command) {
			case ExFileClose:               return FileClose(View, State);
			case ExFileCloseAll:            return FileCloseAll(View, State);
			case ExExitEditor:              return ExitEditor(View);
			case ExIncrementalSearch:
				return View.MView.Win.IncrementalSearch(View) == 0 
				? ExResult.ErFAIL : ExResult.ErOK;
			}
		}
		switch (Command) {
		case ExWinRefresh:              view.Repaint(); return ExResult.ErOK;
		case ExWinNext:                 return WinNext(view);
		case ExWinPrev:                 return WinPrev(view);
		case ExShowEntryScreen:         return ShowEntryScreen();
		case ExRunProgram:              return RunProgram(State, view);
		case ExRunProgramAsync:         return RunProgramAsync(State, view);
		case ExMainMenu:                return MainMenu(State, view);
		case ExShowMenu:                return ShowMenu(State, view);
		case ExLocalMenu:               return LocalMenu(view);
		case ExFrameNew:                return FrameNew();
		case ExFrameNext:               return FrameNext(view);
		case ExFramePrev:               return FramePrev(view);

		case ExWinHSplit:               return WinHSplit(view);
		case ExWinClose:                return WinClose(view);
		case ExWinZoom:                 return WinZoom(view);
		case ExWinResize:               return WinResize(State, view);
		case ExDesktopSaveAs:           return DesktopSaveAs(State, view);

		case ExDesktopSave:
			if (!Main.DesktopFileName.isBlank())
				return ExResult.ofBool( SaveDesktop(Main.DesktopFileName) );
			return ExResult.ErFAIL;

		case ExChangeKeys:
		{
			String[] kmaps = {null};

			if (State.GetStrParam(null, kmaps ) == 0) {
				SetOverrideMap(null, null);
				return ExResult.ErFAIL;
			}
			EEventMap m = EEventMap.FindEventMap(kmaps[0]);
			if (m == null)
				return ExResult.ErFAIL;
			SetOverrideMap(m.KeyMap, m.Name);
			return ExResult.ErOK;
		}
		}
		return view.ExecCommand(Command, State);
	}


	static int BeginMacro(GxView view) {
		view.BeginMacro();
		return 1;
	}


	static ExResult ExecMacro(GxView view, int Macro) {
		if (Macro == -1)
			return ExResult.ErFAIL;

		if (BeginMacro(view) == -1)
			return ExResult.ErFAIL;

		ExState State = new ExState();
		State.Macro = Macro;
		State.Pos = 0;

		ExMacro m = ExMacro.Macros.get(State.Macro);

		for (; State.Pos < m.cmds.size(); State.Pos++)
		{
			int i = State.Pos;
			CommandType mc = m.cmds.get(i);

			if (mc.type != CommandType.CT_COMMAND ||
					mc.num == ExCommands.ExNop.ordinal())
				continue;

			for (int j = 0; j < mc.repeat; j++) {
				State.Pos = i + 1;
				ExCommands cmd = ExCommands.byOrdinal(mc.num);
				try {
					if (gui.ExecCommand(view, cmd, State) == ExResult.ErFAIL && 0 == mc.ign)
					{
						return ExResult.ErFAIL;
					}
				} catch (IOException e) {
					log.log(Level.SEVERE, "ExecCommand " + cmd.toString(), e);
					return ExResult.ErFAIL;
				}
			}
			State.Pos = i;
		}
		return ExResult.ErOK;
	}

	void SetMsg(String Msg) {
		String CharMap = "";

		if (Msg == null) {
			CharMap = "";
		} else {
			CharMap = "["+Msg+"]";
		}
		if (EModel.ActiveModel[0] != null)
			EModel.ActiveModel[0].Msg(S_INFO, CharMap);
	}

	void SetOverrideMap(EKeyMap aMap, String ModeName) {
		OverrideMap = aMap;
		if (aMap == null)
			SetMsg(null);
		else
			SetMsg(ModeName);
	}

	void SetMap(EKeyMap aMap, KeySel ks) {
		String [] key = {""};

		ActiveMap = aMap;
		if (ActiveMap == null) {
			SetMsg(null);
		} else {
			if (ks != null) {
				KeyDefs.GetKeyName(key, ks);
				SetMsg(key[0]);
			}
		}
	}

	void DispatchKey(GxView view, TKeyEvent Event) {
		EKey key = null;
		char [] Ch = {0};

		if(0 != (Event.Code & kfModifier))
			return;

		LastEventChar = -1;
		if(Event.GetChar(Ch))
			LastEventChar = Ch[0];

		EEventMap EventMap;
		if ((EventMap = view.GetEventMap()) == null)
			return;

		EKeyMap map = EventMap.KeyMap;

		if (ActiveMap!=null || OverrideMap!=null) {
			map = ActiveMap;
			if (OverrideMap!=null)
				map = OverrideMap;
			while (map!=null) {
				if ((key = map.FindKey(Event.Code)) != null) {
					if (key.KeyMap!=null) {
						SetMap(key.KeyMap, key.fKey);
						Event.What = evNone;
						return ;
					} else {
						SetMap(null, /* & */key.fKey);
						EGUI.ExecMacro(view, key.Cmd);
						Event.What = evNone;
						return ;
					}
				}
				//            printf("Going up\n");
				map = map.fParent;
			}
			if (OverrideMap==null) {
				SetMap(null, null);
				Event.What = evNone;
			}
			return ;
		}
		while (EventMap!=null) {
			if (map!=null) {
				if ((key = map.FindKey(Event.Code)) != null) {
					if (key.KeyMap != null) {
						SetMap(key.KeyMap, key.fKey);
						Event.What = evNone;
						return ;
					} else {
						ExecMacro(view, key.Cmd);
						Event.What = evNone;
						return ;
					}
				}
			}
			EventMap = EventMap.Parent;
			if (EventMap == null) break;
			map = EventMap.KeyMap;
		}
		// [dz] commented out in orig code        
		//        if (GetCharFromEvent(Event, &Ch))
		//            CharEvent(view, Event, Ch);
		SetMap(null, null);
	}

	void DispatchCommand(GxView view, TEvent pEvent) throws IOException 
	{
		TMsgEvent Event = (TMsgEvent) pEvent;
		if (Event.Command > 65536 + 16384)
		{ // hack for PM toolbar
			Event.Command -= 65536 + 16384;
			BeginMacro(view);
			ExState State = new ExState();
			State.Macro = 0;
			State.Pos = 0;
			ExecCommand(view, ExCommands.byOrdinal(Event.Command), State);
			Event.What = evNone;
		} else if (Event.Command >= 65536) {
			Event.Command -= 65536;
			ExecMacro(view, Event.Command);
			Event.What = evNone;
		}
	}

	@Override
	void DispatchEvent(GFrame frame, GView view, TEvent Event) throws IOException {
		GxView xview = (GxView) view;

		if (Event.What == evNone ||
				(Event.What == evMouseMove && ((TMouseEvent)Event).Buttons == 0))
			return ;

		if (Event.What == evNotify && ((TMsgEvent)Event).Command == cmPipeRead) {
			((TMsgEvent)Event).Model.NotifyPipe((int)((TMsgEvent)Event).Param1);
			return;
		}

		if (xview.GetEventMap() != null) {
			switch (Event.What) {
			case evKeyDown:
				DispatchKey(xview, (TKeyEvent) Event);
				break;
			case evCommand:
				TMsgEvent mEvent = (TMsgEvent) Event;
				if (mEvent.Command >= 65536) {
					DispatchCommand(xview, Event);
				} else {
					switch (mEvent.Command) {
					case cmClose:
					{
						assert(EView.ActiveView != null);
						FrameClose(EView.ActiveView.MView.Win);
						return;
					}
					}
				}
			}
		}

		super.DispatchEvent(frame, view, Event);
	}

	/////////////////////////////////////////////////////////////////////////////
	/////////////////////////////////////////////////////////////////////////////

	ExResult WinNext(GxView view) {
		view.Parent.SelectNext(0);
		return ExResult.ErOK;
	}

	ExResult WinPrev(GxView view) {
		view.Parent.SelectNext(1);
		return ExResult.ErOK;
	}

	ExResult FileCloseX(EView View, int CreateNew, int XClose) throws IOException {
		String [] Path = {""};

		// this should never fail!
		if (Console.GetDefaultDirectory(View.Model, Path) == 0)
			return ExResult.ErFAIL;

		if (View.Model.ConfQuit(View.MView.Win,0)!=0) {

			View.Model.DeleteRelated();

			// close everything that can be closed without confirmation if closing all
			if (XClose!=0)
				while (View.Model.Next != View.Model &&
				View.Model.Next.CanQuit())
					View.Model.Next.close();

			View.DeleteModel(View.Model);

			if (EModel.ActiveModel == null && CreateNew!=0) {
				EView V = EView.ActiveView;
				//EModel[] rootPtr = {EModel.ActiveModel};
				EModel m = new EDirectory(0, EModel.ActiveModel, Path[0]);
				assert(m != null);

				do {
					V = V.Next;
					V.SelectModel(EModel.ActiveModel[0]);
				} while (V != EView.ActiveView);
				return ExResult.ErFAIL;
			}

			if (EModel.ActiveModel == null) {
				StopLoop();
			}
			return ExResult.ErOK;
		}
		return ExResult.ErFAIL;
	}


	ExResult FileClose(EView View, ExState State) throws IOException {
		int [] x = {0};

		if (State.GetIntParam(View, x) == 0)
			x[0] = Config.OpenAfterClose;

		return FileCloseX(View, x[0], 0);
	}

	ExResult FileCloseAll(EView View, ExState State) throws IOException {
		int [] x = {0};

		if (State.GetIntParam(View, x) == 0)
			x[0] = Config.OpenAfterClose;

		while (EModel.ActiveModel!=null)
			if (FileCloseX(View, x[0], 1) == ExResult.ErFAIL) return ExResult.ErFAIL;
		return ExResult.ErOK;
	}

	ExResult WinHSplit(GxView View) {
		GxView view;
		ExModelView edit;
		EView win;
		int [] W= {0}, H= {0};

		View.ConQuerySize(W, H);

		if (H[0] < 8)
			return ExResult.ErFAIL;
		view = new GxView(View.Parent);
		if (view == null)
			return ExResult.ErFAIL;
		win = new EView(EModel.ActiveModel[0]);
		if (win == null)
			return ExResult.ErFAIL;
		edit = new ExModelView(win);
		if (edit == null)
			return ExResult.ErFAIL;
		view.PushView(edit);
		view.Parent.SelectNext(0);
		return ExResult.ErOK;
	}

	ExResult WinClose(GxView V) throws IOException {
		EView View = EView.ActiveView;

		if (View.Next == View) {
			// when closing last window, close all files
			if (ExitEditor(View) == null)
				return ExResult.ErFAIL;
		} else {
			View.MView.Win.Parent.SelectNext(0);
			View.MView.Win.close();
		}
		return ExResult.ErOK;
	}

	ExResult WinZoom(GxView View) throws IOException {
		GView V = View.Next;
		GView V1;

		while (V!=null) {
			V1 = V;
			if (V == View)
				break;
			V = V.Next;
			V1.close();
		}
		return ExResult.ErOK;
	}

	ExResult WinResize(ExState State, GxView View) {
		int [] Delta = {1};

		if (State.GetIntParam(null, Delta)!=0) {
			if (View.ExpandHeight(Delta[0]) == 0)
				return ExResult.ErOK;
		}
		return ExResult.ErFAIL;
	}

	ExResult ExitEditor(EView View) {
		EModel B = EModel.ActiveModel[0];

		// check/save modified files
		while (EModel.ActiveModel[0]!=null) {
			if (EModel.ActiveModel[0].CanQuit()) ;
			else {
				View.SelectModel(EModel.ActiveModel[0]);
				int rc = EModel.ActiveModel[0].ConfQuit(View.MView.Win, 1);
				if (rc == -2) {
					View.FileSaveAll();
					break;
				}
				if (rc == 0)
					return ExResult.ErFAIL;
			}

			EModel.ActiveModel[0] = EModel.ActiveModel[0].Next;
			if (EModel.ActiveModel[0] == B)
				break;
		}

		if (Config.SaveDesktopOnExit && !Main.DesktopFileName.isBlank())
			SaveDesktop(Main.DesktopFileName);
		else if (Config.LoadDesktopMode == 2) {       // Ask about saving?
			GxView gx = View.MView.Win;

			String dfn[] = {""};

			if (gx.GetStr("Save desktop As",
					dfn,
					HIST_DEFAULT) != 0)
			{
				Main.DesktopFileName = dfn[0];
				SaveDesktop(Main.DesktopFileName);
			}
		}

		while (EModel.ActiveModel!=null) {
			if (View.Model.GetContext() == CONTEXT_ROUTINES)  // Never delete Routine models directly
			{
				EModel.ActiveModel[0] = EModel.ActiveModel[0].Next;
				View.SelectModel(EModel.ActiveModel[0]);
			}

			View.Model.DeleteRelated();  // delete related views first


			View.DeleteModel(View.Model);
		}

		StopLoop();
		return ExResult.ErOK;
	}

	ExResult ShowEntryScreen() {
		return gui.ShowEntryScreen();
	}

	static String [] Cmd= {""};
	ExResult RunProgram(ExState State, GxView view) {

		if (EModel.ActiveModel!=null)
			Console.SetDefaultDirectory(EModel.ActiveModel[0]);

		if (State.GetStrParam(EView.ActiveView, Cmd) == 0)
			if (view.GetStr("Run", Cmd, HIST_COMPILE) == 0) 
				return ExResult.ErFAIL;

		gui.RunProgram(RUN_WAIT, Cmd[0]);
		return ExResult.ErOK;
	}

	ExResult RunProgramAsync(ExState State, GxView view) {

		if (EModel.ActiveModel!=null)
			Console.SetDefaultDirectory(EModel.ActiveModel[0]);

		if (State.GetStrParam(EView.ActiveView, Cmd ) == 0)
			if (view.GetStr("Run", Cmd, HIST_COMPILE) == 0) 
				return ExResult.ErFAIL;
		
		gui.RunProgram(RUN_ASYNC, Cmd[0]);
		return ExResult.ErOK;
	}

	ExResult MainMenu(ExState State, GxView View) {
		String [] s = {""};

		if (State.GetStrParam(null, s) == 0)
			s[0] = "";

		char c = s[0].length() > 0 ? s[0].charAt(0) : 0;

		View.Parent.ExecMainMenu(c);
		return ExResult.ErOK;
	}

	ExResult ShowMenu(ExState State, GxView View) {
		String  MName[] = {""};

		if (State.GetStrParam(null, MName) == 0)
			return ExResult.ErFAIL;

		View.Parent.PopupMenu(MName[0]);
		return ExResult.ErFAIL; // TODO why?
	}

	ExResult LocalMenu(GxView View) {
		EEventMap Map = View.GetEventMap();
		String MName = null;

		if (Map!=null)
			MName = Map.GetMenu(EM_LocalMenu);
		if (MName == null)
			MName = "Local";
		View.Parent.PopupMenu(MName);
		return ExResult.ErFAIL;
	}

	ExResult DesktopSaveAs(ExState State, GxView view) 
	{
		String dfn[] = {""};
		if (State.GetStrParam(null, dfn) == 0)
			if (view.GetFile("Save Desktop", dfn, HIST_PATH, GF_SAVEAS) == 0)
				return ExResult.ErFAIL;

		Main.DesktopFileName = dfn[0];

		if (!Main.DesktopFileName.isBlank())
			return ExResult.ofBool(SaveDesktop(Main.DesktopFileName));

		return ExResult.ErFAIL;
	}

	ExResult FrameNew() {
		GxView view;
		ExModelView edit;

		if (0==multiFrame() && frames!=null)
			return ExResult.ErFAIL;

		eFrame = new EFrame(Config.ScreenSizeX, Config.ScreenSizeY);
		assert(frames != null);

		//frames.SetMenu("Main"); //??

		view = new GxView(frames);
		assert(view != null);

		new EView(EModel.ActiveModel[0]);
		assert(EView.ActiveView != null);

		edit = new ExModelView(EView.ActiveView);
		assert(edit != null);
		view.PushView(edit);
		frames.Show();
		return ExResult.ErOK;
	}

	ExResult FrameClose(GxView View) {
		assert(frames != null);
		assert(View != null);

		if (!frames.isLastFrame()) {
			deleteFrame(frames);
		} else {
			if (ExitEditor(EView.ActiveView) == ExResult.ErFAIL)
				return ExResult.ErFAIL;
			deleteFrame(frames);
		}
		return ExResult.ErOK;
	}

	ExResult FrameNext(GxView View) {
		if (!frames.isLastFrame()) {
			frames.Next.Activate();
			return ExResult.ErOK;
		}
		return ExResult.ErFAIL;
	}

	ExResult FramePrev(GxView View) {
		if (!frames.isLastFrame()) {
			frames.Prev.Activate();
			return ExResult.ErOK;
		}
		return ExResult.ErFAIL;
	}

	/**
	 *  Locates the desktop file depending on the load desktop mode flag:
	 *  0:  Try the "current" directory, then the FTE .exe directory (PC) or
	 *      the user's homedir (Unix). Fail if not found (original FTE
	 *      algorithm).
	 *  1:  Try the current directory, then loop {go one directory UP and try}
	 *      If not found, don't load a desktop!
	 *  2:  As above, but asks to save the desktop if one was not found.
	 *  
	 *  This is called if the desktop is not spec'd on the command line.
	 */
	boolean findDesktop(String argv[]) 
	{
		switch (Config.LoadDesktopMode) {
		default:
			//** 0: try curdir then "homedir"..
			//         fprintf(stderr, "ld: Mode 0\n");
			if (Console.FileExists(MainConst.DESKTOP_NAME))
			{
				String [] dfn = {""};
				Console.ExpandPath(MainConst.DESKTOP_NAME, dfn);
				Main.DesktopFileName = dfn[0];
			}
			else {
				//** Use homedir,
				//#ifdef UNIX
				//            ExpandPath("~/" DESKTOP_NAME, DesktopFileName);
				//#else
				// TO DO argv[0] is not prog path
				//Console.JustDirectory(argv[0], DesktopFileName);
				//DesktopFileName [0] = Console.directory(argv[0]); 
				//DesktopFileName [0] += MainConst.DESKTOP_NAME

				Main.DesktopFileName = Console.getHomeDir() + "/" + MainConst.DESKTOP_NAME; 
				//#endif
			}
			return Console.FileExists(Main.DesktopFileName);

			/* TODO findDesktop
        case 1:
        case 2:
            //** Try curdir, then it's owner(s)..
        	Console.ExpandPath(".", DesktopFileName);
            //fprintf(stderr, "ld: Mode 1 (start at %s)\n", DesktopFileName);

            for (;;) {
                //** Try current location,
                String pe = new String(Main.DesktopFileName);
                Main.DesktopFileName = Console.Slash(Main.DesktopFileName, 1);      // Add appropriate slash
                Main.DesktopFileName += DESKTOP_NAME;

                //fprintf(stderr, "ld: Mode 1 (trying %s)\n", DesktopFileName);
                if (Console.FileExists(Main.DesktopFileName)) {
                    //fprintf(stderr, "ld: Mode 1 (using %s)\n", DesktopFileName);
                    return true;
                }

                //** Not found. Remove added stuff, then go level UP,
                //*pe = 0;
                Main.DesktopFileName = pe;

                // Remove the current part,
                String p = SepRChr(DesktopFileName);

                if (p == NULL) {
                    //** No desktop! Set default name in current directory,
                    ExpandPath(".", DesktopFileName);
                    Slash(DesktopFileName, 1);
                    strcat(DesktopFileName, DESKTOP_NAME);

                    SaveDesktopOnExit = 0;      // Don't save,
                    return 0;                   // NOT found!!
                }
			 *p = 0;                         // Truncate name at last
            } */
		}
	}


	void DoLoadDesktopOnEntry(String []argv) {
		if (Main.DesktopFileName.isBlank())
			findDesktop(argv);

		if (!Main.DesktopFileName.isBlank()) {
			if (Console.IsDirectory(Main.DesktopFileName)) {
				Main.DesktopFileName = Console.Slash(Main.DesktopFileName, 1);
				Main.DesktopFileName += MainConst.DESKTOP_NAME;
			}

			if (Config.LoadDesktopOnEntry != 0 && Console.FileExists(Main.DesktopFileName))
				LoadDesktop(Main.DesktopFileName);
		}
	}


	void EditorInit() 
	{
		EModel [] ssm = {EBuffer.SSBuffer};
		EBuffer.SSBuffer = new EBuffer(0, ssm, "Scrap");
		//SSBuffer = new EBuffer(0, (EModel **)&SSBuffer, "Scrap");
		//assert(SSBuffer != null);
		EBuffer.SSBuffer.BFI_SET(EBuffer.SSBuffer, BFI_Undo, 0); // disable undo for clipboard
		EModel.ActiveModel[0] = null;
	}

	int InterfaceInit() {
		if (FrameNew() == null)
			Console.DieError(1, "Failed to create window\n");
		return 0;
	}

	void DoLoadHistoryOnEntry() {
		if (Main.HistoryFileName.isBlank()) {
			/*#ifdef UNIX
            ExpandPath("~/.fte-history", HistoryFileName);
    #else */
			Main.HistoryFileName = Console.getHomeDir() + "/" + MainConst.HISTORY_NAME;
			//JustDirectory(argv[0], Main.HistoryFileName);
			//strcat(Main.HistoryFileName, "fte.his");
			//#endif
		} else {
			//char p[256];

			String[] p = {""};

			Console.ExpandPath(Main.HistoryFileName, p);
			if (Console.IsDirectory(p[0])) {
				p[0] = Console.Slash(p[0], 1);
			}
			Main.HistoryFileName =  p + MainConst.HISTORY_NAME;
		}

		if (Config.KeepHistory && Console.FileExists(Main.HistoryFileName))
			InputHistory.LoadHistory(Main.HistoryFileName);
	}

	void DoSaveHistoryOnExit() {
		if (Config.KeepHistory && !Main.HistoryFileName.isBlank())
			InputHistory.SaveHistory(Main.HistoryFileName);
	}


	ExResult CmdLoadFiles(String [] argv) 
	{
		boolean QuoteNext = false;
		boolean QuoteAll = false;
		boolean GotoLine = false;
		int LineNum = 1;
		int ColNum = 1;
		boolean ModeOverride = false;
		String Mode = null;
		int LCount = 0;
		boolean ReadOnly = false;

		for (int Arg = 0; Arg < argv.length; Arg++) 
		{
			if (!QuoteAll && !QuoteNext && (argv[Arg].charAt(0) == '-')) 
			{
				char aa1 = argv[Arg].charAt(1);

				if (aa1 == '-') {
					//if (strncmp(argv[Arg], "--debug", 7) != 0) QuoteAll = true;
					if( !argv[Arg].substring(0, 7).equals("--debug") )
						QuoteAll = true;
				} else if (aa1 == '!') {
					// handled before
				} else if (aa1 == 'c' || aa1 == 'C') {
					// ^
				} else if (aa1 == 'D' || aa1 == 'd') {
					// ^
				} else if (aa1 == 'H') {
					// ^
				} else if (aa1 == '+') {
					QuoteNext = true;
				} else if (aa1 == '#' || aa1 == 'l') {
					LineNum = 1;
					ColNum = 1;
					String[] sp = argv[Arg].substring(2).split(",");
					GotoLine = true;
					LineNum = Integer.parseInt(sp[0]);
					if( sp.length > 1 )
						ColNum = Integer.parseInt(sp[1]);
					/*
                    if (strchr(argv[Arg], ',')) {
                        GotoLine = (2 == sscanf(argv[Arg] + 2, "%d,%d", LineNum, ColNum));
                    } else {
                        GotoLine = (1 == sscanf(argv[Arg] + 2, "%d", LineNum));
                    }*/
					//                printf("Gotoline = %d, line = %d, col = %d\n", GotoLine, LineNum, ColNum);
				} else if (aa1 == 'r') {
					ReadOnly = true;
				} else if (aa1 == 'm') {
					if (argv[Arg].length() == 2) {
						ModeOverride = false;
					} else {
						ModeOverride = true;
						Mode = argv[Arg].substring(2);
					}
				} else if (aa1 == 'T') {
					Tags.TagsAdd(argv[Arg].substring(2) );
				} else if (aa1 == 't') {
					Tags.TagGoto(EView.ActiveView, argv[Arg].substring(2));
				} else {
					Console.DieError(2, "Invalid command line option %s", argv[Arg]);
					return ExResult.ErFAIL;
				}
			} else {
				String [] Path = {null};

				QuoteNext = false;
				if (Console.ExpandPath(argv[Arg], Path) == 0 && Console.IsDirectory(Path[0])) 
				{
					//EModel[] rootPtr = {EModel.ActiveModel};
					//EModel m = EDirectory.newEDirectory(EModel.cfAppend, EModel.ActiveModel, Path[0]);
					EModel m = new EDirectory(EModel.cfAppend, EModel.ActiveModel, Path[0]);
					assert(EModel.ActiveModel != null && m != null);
				}
				else 
				{
					if (LCount != 0)
						EBuffer.suspendLoads = 1;
					if (!Console.MultiFileLoad(EModel.cfAppend, argv[Arg],
							ModeOverride ? Mode : null,
									EView.ActiveView)) {
						EBuffer.suspendLoads = 0;
						return ExResult.ErFAIL;
					}
					EBuffer.suspendLoads = 0;

					if (GotoLine) {
						if (!((EBuffer )EModel.ActiveModel[0]).Loaded)
							((EBuffer )EModel.ActiveModel[0]).Load();
						if (GotoLine) {
							GotoLine = false;
							((EBuffer)EModel.ActiveModel[0]).SetNearPosR(ColNum - 1, LineNum - 1);
						} else {
							int [] r = {0}, c = {0};

							if (FPosHistory.RetrieveFPos(((EBuffer)EModel.ActiveModel[0]).FileName, r, c))
								((EBuffer)EModel.ActiveModel[0]).SetNearPosR(c[0], r[0]);
						}
						//EView.ActiveView.SelectModel(EModel.ActiveModel);
					}
					if (ReadOnly) {
						ReadOnly = false;
						EBuffer b = (EBuffer)EModel.ActiveModel[0]; 
						b.BFI_SET(b, BFI_ReadOnly, 1);
					}
				}
				EBuffer.suspendLoads = 1;
				EView.ActiveView.SelectModel(EModel.ActiveModel[0].Next);
				EBuffer.suspendLoads = 0;
				LCount++;
			}
		} 

		EModel P = EModel.ActiveModel[0];
		while (LCount-- > 0)
			P = P.Prev;
		EView.ActiveView.SelectModel(P);
		return ExResult.ErOK;
	}

	int Start(String [] argv) {
		{
			int rc = super.Start(argv);

			if (rc!=0)
				return rc;
		}

		if (InterfaceInit() != 0)
			return 2;

		EditorInit();

		DoLoadHistoryOnEntry();
		DoLoadDesktopOnEntry(argv);   

		if (CmdLoadFiles(argv) == ExResult.ErFAIL)            return 3;

		if (EModel.ActiveModel[0] == null) {
			// #ifdef CONFIG_OBJ_DIRECTORY
			String [] Path = {"."};

			Console.GetDefaultDirectory(null, Path);

			//EModel[] rootPtr = { EModel.ActiveModel };
			//EModel m = EDirectory.newEDirectory(0, EModel.ActiveModel, Path[0]);
			EModel m = new EDirectory(0, EModel.ActiveModel, Path[0]);

			assert(EModel.ActiveModel != null && m != null);
			EView.ActiveView.SwitchToModel(EModel.ActiveModel);
			/*#else
            Usage();
            return 1;
    #endif */
		} //*/
		return 0;
	}

	void Stop() {
		DoSaveHistoryOnExit();
		super.Stop();
	}




	boolean SaveDesktop(String FileName) 
	{
		try (BufferedWriter writer = Files.newBufferedWriter(Path.of(FileName), Main.charset)) 
		{
			return doSaveDesktop(writer);
		} catch (IOException x) {
			System.err.format("Writing to %s IOException: %s%n", FileName, x);
			//View.MView.Win.Choice(GPC_ERROR, "Error", 1, "O&K", "Failed to write block to %s", AFileName);
			new File(FileName).delete();
			return false;
		}

	}


	private static final String DESKTOP_VER = "FTE Desktop 2\n";

	boolean doSaveDesktop(BufferedWriter w) throws IOException 
	{        
		w.write(DESKTOP_VER);

		EModel M = EModel.ActiveModel[0];
		while (M != null) {
			switch(M.GetContext()) {
			case CONTEXT_FILE:

			if (M != ECvsLog.CvsLogView)
			{
				EBuffer B = (EBuffer )M;
				w.write( String.format("F|%d|%s\n", B.ModelNo, B.FileName) );
			}
			break;
			case CONTEXT_DIRECTORY:
			{
				EDirectory D = (EDirectory )M;
				w.write( String.format( "D|%d|%s\n", D.ModelNo, D.Path) );
			}
			break;
			}
			M = M.Next;
			if (M == EModel.ActiveModel[0])
				break;
		}

		Tags.TagsSave(w);
		EMarkIndex.markIndex.saveToDesktop(w);

		w.close();

		return true;
	}

	boolean LoadDesktop(String FileName) 
	{
		try (BufferedReader r = Files.newBufferedReader(Path.of(FileName), Main.charset)) 
		{
			return doLoadDesktop(r);
		} catch (IOException x) {
			System.err.format("Reading %s IOException: %s%n", FileName, x);
			//View.MView.Win.Choice(GPC_ERROR, "Error", 1, "O&K", "Failed to write block to %s", AFileName);
			return false;
		}

	}

	boolean doLoadDesktop(BufferedReader r) throws IOException 
	{
		int FLCount = 0;

		Tags.TagClear();

		String line = r.readLine();

		if (line == null || !line.equals(DESKTOP_VER))
			return false;

		while ( (line = r.readLine()) != null) 
		{
			char c0 = line.charAt(0);
			char c1 = line.charAt(1);

			if ((c0 == 'D' || c0 == 'F') && c1 == '|') {
				int ModelNo = -1;
				int p = 2;
				if (Character.isDigit(line.charAt(2))) {
					ModelNo = Integer.parseInt(line.substring(2));
					while (Character.isDigit(line.charAt(p))) p++;
					if (line.charAt(p) == '|')
						p++;
				}

				if (c0 == 'F') { // file
					if (FLCount > 0)
						EBuffer.suspendLoads = 1;
					if (Console.FileLoad(0, line.substring(p), null, EView.ActiveView))
						FLCount++;
					EBuffer.suspendLoads  = 0;

				} else if (c0 == 'D') { // directory
					//EModel[] rootPtr = {EModel.ActiveModel};
					EModel m = new EDirectory(0, EModel.ActiveModel, line.substring(p));
					assert(EModel.ActiveModel != null && m != null);
				}

				if (EModel.ActiveModel != null) {
					if (ModelNo != -1) {
						if (EModel.FindModelID(EModel.ActiveModel[0], ModelNo) == null)
							EModel.ActiveModel[0].ModelNo = ModelNo;
					}

					if (EModel.ActiveModel[0] != EModel.ActiveModel[0].Next) {
						EBuffer.suspendLoads = 1;
						EView.ActiveView.SelectModel(EModel.ActiveModel[0].Next);
						EBuffer.suspendLoads  = 0;
					}
				}
			} else {

				if (c0 == 'T' && c1 == '|') { // tag file
					Tags.TagsAdd(line.substring(2));
				} /* TODO else if (c0 == 'M' && c1 == '|') { // TODO mark
                    String name;
                    String file;
                    EPoint P;
                    //long l;
                    String e;

                    int p = 2;
                    P.Row = strtol(p, &e, 10);
                    if (*e != '|')
                        break;
                    p = e + 1;
                    P.Col = strtol(p, &e, 10);
                    if (*e != '|')
                        break;
                    p = e + 1;
                    name = p;
                    while (*p && *p != '|')
                        p++;
                    if (*p == '|')
				 *p++ = 0;
                    else
                        break;
                    file = p;

                    markIndex.insert(name, file, P);
                } */
			}
		}

		r.close();
		return true;
	}

}
