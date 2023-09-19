package ru.dz.jfte;

import java.io.IOException;

public class EGUI extends GUI implements ModeDefs, GuiDefs, KeyDefs 
{
    EKeyMap ActiveMap;
    EKeyMap OverrideMap;
    String CharMap = null;

    static final int  RUN_WAIT = 0;
    static final int  RUN_ASYNC = 1;
    
    static int LastEventChar = -1;

    
    EGUI(String [] argv, int XSize, int YSize)
    {
    	super(argv, XSize, YSize);
        ActiveMap = null;
        OverrideMap = null;
        CharMap = "";
    }


    ExResult ExecCommand(GxView view, ExCommands Command, ExState State) throws IOException 
    {
    	/* TODO 
        if(0 != (Command & CMD_EXT))
            return ExecMacro(view, Command & ~CMD_EXT);
		*/
    	
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
    //#ifdef CONFIG_I_SEARCH
    //            return View.MView.Win.IncrementalSearch(View);
    //#else
                return ExResult.ErFAIL;
    //#endif
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
        /*
        case ExDesktopSave:
            if (DesktopFileName[0] != 0)
                return SaveDesktop(DesktopFileName);
            return 0;
         */
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

    /*
    int BeginMacro(GxView view) {
        view.BeginMacro();
        return 1;
    }

   
    int ExecMacro(GxView view, int Macro) {
        int i, j;
        ExMacro m;
        ExState State;

        if (Macro == -1)
            return ErFAIL;

        if (BeginMacro(view) == -1)
            return ErFAIL;

        State.Macro = Macro;
        State.Pos = 0;
        m = &Macros[State.Macro];
        for (; State.Pos < m.Count; State.Pos++) {
            i = State.Pos;
            if (m.cmds[i].type != CT_COMMAND ||
                m.cmds[i].u.num == ExNop)
                continue;

            for (j = 0; j < m.cmds[i].repeat; j++) {
                State.Pos = i + 1;
                if (ExecCommand(view, m.cmds[i].u.num, State) == 0 && !m.cmds[i].ign)
                {
                    return ErFAIL;
                }
            }
            State.Pos = i;
        }
        return ErOK;
    } */

    void SetMsg(String Msg) {
        String CharMap = "";

        if (Msg == null) {
            CharMap = "";
        } else {
            CharMap = "["+Msg+"]";
        }
        if (EModel.ActiveModel != null)
            EModel.ActiveModel.Msg(S_INFO, CharMap);
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
                KeyTable.GetKeyName(key, ks);
                SetMsg(key[0]);
            }
        }
    }

    void DispatchKey(GxView view, TKeyEvent Event) {
        EEventMap EventMap;
        EKey key = null;
        char [] Ch = {0};

        if(0 != (Event.Code & kfModifier))
            return;

        LastEventChar = -1;
        if(Event.GetChar(Ch))
            LastEventChar = Ch[0];

        if ((EventMap = view.GetEventMap()) == null)
            return;

        EKeyMap map = EventMap.KeyMap;

        if (ActiveMap!=null || OverrideMap!=null) {
            map = ActiveMap;
            if (OverrideMap!=null)
                map = OverrideMap;
            while (map!=null) {
                if ((key = map.FindKey(Event.Code)) != null) {
                    if (key.fKeyMap!=null) {
                        SetMap(key.fKeyMap, key.fKey);
                        Event.What = evNone;
                        return ;
                    } else {
                        SetMap(null, /* & */key.fKey);
                        // TODO ExecMacro(view, key.Cmd);
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
                    if (key.fKeyMap != null) {
                        SetMap(key.fKeyMap, key.fKey);
                        Event.What = evNone;
                        return ;
                    } else {
                        // TODO ExecMacro(view, key.Cmd);
                        Event.What = evNone;
                        return ;
                    }
                }
            }
            EventMap = EventMap.Parent;
            if (EventMap == null) break;
            map = EventMap.KeyMap;
        }
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
            // TODO BeginMacro(view);
            ExState State = new ExState();
            State.Macro = 0;
            State.Pos = 0;
            ExecCommand(view, ExCommands.byOrdinal(Event.Command), State);
            Event.What = evNone;
        } else if (Event.Command >= 65536) {
            Event.Command -= 65536;
         // TODO ExecMacro(view, Event.Command);
            Event.What = evNone;
        }
    }

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

    /* TODO #ifdef CONFIG_OBJ_DIRECTORY
            if (EModel.ActiveModel == 0 && CreateNew) {
                EView *V = EView.ActiveView;
                EModel *m = new EDirectory(0, &EModel.ActiveModel, Path);
                assert(m != 0);

                do {
                    V = V.Next;
                    V.SelectModel(EModel.ActiveModel);
                } while (V != EView.ActiveView);
                return 0;
            }
    #endif */

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
        win = new EView(EModel.ActiveModel);
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

    ExResult WinZoom(GxView View) {
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
        EModel B = EModel.ActiveModel;

        // check/save modified files
        while (EModel.ActiveModel!=null) {
            if (EModel.ActiveModel.CanQuit()) ;
            else {
                View.SelectModel(EModel.ActiveModel);
                int rc = EModel.ActiveModel.ConfQuit(View.MView.Win, 1);
                if (rc == -2) {
                    View.FileSaveAll();
                    break;
                }
                if (rc == 0)
                    return ExResult.ErFAIL;
            }

            EModel.ActiveModel = EModel.ActiveModel.Next;
            if (EModel.ActiveModel == B)
                break;
        }

    /* TODO #ifdef CONFIG_DESKTOP
        if (SaveDesktopOnExit && DesktopFileName[0] != 0)
            SaveDesktop(DesktopFileName);
        else if (LoadDesktopMode == 2) {       // Ask about saving?
            GxView* gx = View.MView.Win;

            if (gx.GetStr("Save desktop As",
                           sizeof(DesktopFileName), DesktopFileName,
                           HIST_DEFAULT) != 0)
            {
                SaveDesktop(DesktopFileName);
            }
        }
    #endif */

        while (EModel.ActiveModel!=null) {
            if (View.Model.GetContext() == CONTEXT_ROUTINES)  // Never delete Routine models directly
            {
                EModel.ActiveModel = EModel.ActiveModel.Next;
                View.SelectModel(EModel.ActiveModel);
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
            Console.SetDefaultDirectory(EModel.ActiveModel);

        if (State.GetStrParam(EView.ActiveView, Cmd) == 0)
            if (view.GetStr("Run", Cmd, HIST_COMPILE) == 0) return ExResult.ErFAIL;
        
        gui.RunProgram(RUN_WAIT, Cmd[0]);
        return ExResult.ErOK;
    }

    ExResult RunProgramAsync(ExState State, GxView view) {

        if (EModel.ActiveModel!=null)
        	Console.SetDefaultDirectory(EModel.ActiveModel);

        if (State.GetStrParam(EView.ActiveView, Cmd ) == 0)
            if (view.GetStr("Run", Cmd, HIST_COMPILE) == 0) return ExResult.ErFAIL;
        gui.RunProgram(RUN_ASYNC, Cmd[0]);
        return ExResult.ErOK;
    }

    ExResult MainMenu(ExState State, GxView View) {
        String [] s = {""};

        if (State.GetStrParam(null, s) == 0)
            s[0] = "";

        View.Parent.ExecMainMenu(s[0].charAt(0));
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

    ExResult DesktopSaveAs(ExState State, GxView view) {
    	/* TODO
        if (State.GetStrParam(0, DesktopFileName) == 0)
            if (view.GetFile("Save Desktop", DesktopFileName, HIST_PATH, GF_SAVEAS) == 0)
                return ExResult.ErFAIL;

        if (DesktopFileName[0] != 0)
            return SaveDesktop(DesktopFileName);
        */
        return ExResult.ErFAIL;
    }

    ExResult FrameNew() {
        GxView view;
        ExModelView edit;

        if (0==multiFrame() && frames!=null)
            return ExResult.ErFAIL;

        new EFrame(Config.ScreenSizeX, Config.ScreenSizeY);
        assert(frames != null);

        //frames.SetMenu("Main"); //??

        view = new GxView(frames);
        assert(view != null);

        new EView(EModel.ActiveModel);
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
    /*
    //#ifdef CONFIG_DESKTOP
    int findDesktop(String argv[]) {
        switch (LoadDesktopMode) {
        default:
            //** 0: try curdir then "homedir"..
            //         fprintf(stderr, "ld: Mode 0\n");
            if (FileExists(DESKTOP_NAME))
                ExpandPath(DESKTOP_NAME, DesktopFileName);
            else {
                //** Use homedir,
    //#ifdef UNIX
    //            ExpandPath("~/" DESKTOP_NAME, DesktopFileName);
    //#else
                JustDirectory(argv[0], DesktopFileName);
                strcat(DesktopFileName, DESKTOP_NAME);
    //#endif
            }
            return FileExists(DesktopFileName);

        case 1:
        case 2:
            //** Try curdir, then it's owner(s)..
            ExpandPath(".", DesktopFileName);
            //fprintf(stderr, "ld: Mode 1 (start at %s)\n", DesktopFileName);

            for (;;) {
                //** Try current location,
                char *pe = DesktopFileName + strlen(DesktopFileName);
                Slash(DesktopFileName, 1);      // Add appropriate slash
                strcat(DesktopFileName, DESKTOP_NAME);

                //fprintf(stderr, "ld: Mode 1 (trying %s)\n", DesktopFileName);
                if (FileExists(DesktopFileName)) {
                    //fprintf(stderr, "ld: Mode 1 (using %s)\n", DesktopFileName);
                    return 1;
                }

                //** Not found. Remove added stuff, then go level UP,
                *pe = 0;

                // Remove the current part,
                char *p = SepRChr(DesktopFileName);

                if (p == NULL) {
                    //** No desktop! Set default name in current directory,
                    ExpandPath(".", DesktopFileName);
                    Slash(DesktopFileName, 1);
                    strcat(DesktopFileName, DESKTOP_NAME);

                    SaveDesktopOnExit = 0;      // Don't save,
                    return 0;                   // NOT found!!
                }
                *p = 0;                         // Truncate name at last
            }
        }
    }

    /* TODO
    void DoLoadDesktopOnEntry(char **argv) {
        if (DesktopFileName[0] == 0)
            findDesktop(argv);

        if (DesktopFileName[0] != 0) {
            if (IsDirectory(DesktopFileName)) {
                Slash(DesktopFileName, 1);
                strcat(DesktopFileName, DESKTOP_NAME);
            }

            if (LoadDesktopOnEntry && FileExists(DesktopFileName))
                LoadDesktop(DesktopFileName);
        }
    }
    //#endif */

    void EditorInit() 
    {
    	EModel [] ssm = {EBuffer.SSBuffer};
    	EBuffer.SSBuffer = new EBuffer(0, ssm, "Scrap");
        //SSBuffer = new EBuffer(0, (EModel **)&SSBuffer, "Scrap");
        //assert(SSBuffer != null);
        BFI(EBuffer.SSBuffer, BFI_Undo) = 0; // disable undo for clipboard
        EModel.ActiveModel = null;
    }

    int InterfaceInit() {
        if (FrameNew() == null)
            Console.DieError(1, "Failed to create window\n");
        return 0;
    }

    /* TOD #ifdef CONFIG_HISTORY
    void DoLoadHistoryOnEntry(String [] argv) {
        if (HistoryFileName[0] == 0) {
    #ifdef UNIX
            ExpandPath("~/.fte-history", HistoryFileName);
    #else
            JustDirectory(argv[0], HistoryFileName);
            strcat(HistoryFileName, "fte.his");
    #endif
        } else {
            char p[256];

            ExpandPath(HistoryFileName, p);
            if (IsDirectory(p)) {
                Slash(p, 1);
                strcat(p, HISTORY_NAME);
            }
            strcpy(HistoryFileName, p);
        }

        if (KeepHistory && FileExists(HistoryFileName))
            LoadHistory(HistoryFileName);
    }

    void DoSaveHistoryOnExit() {
        if (KeepHistory && HistoryFileName[0] != 0)
            SaveHistory(HistoryFileName);

        // since we are exiting, free history
        ClearHistory();
    }
    #endif */

    ExResult CmdLoadFiles(String [] argv) 
    {
        int QuoteNext = 0;
        int QuoteAll = 0;
        int GotoLine = 0;
        int LineNum = 1;
        int ColNum = 1;
        int ModeOverride = 0;
        //char Mode[32];
        int LCount = 0;
        int ReadOnly = 0;

        /* TODO
        for (int Arg = 1; Arg < argv.length; Arg++) {
            if (!QuoteAll && !QuoteNext && (argv[Arg][0] == '-')) {
                if (argv[Arg][1] == '-') {
                    if (strncmp(argv[Arg], "--debug", 7) != 0)
                        QuoteAll = 1;
                } else if (argv[Arg][1] == '!') {
                    // handled before
                } else if (argv[Arg][1] == 'c' || argv[Arg][1] == 'C') {
                    // ^
                } else if (argv[Arg][1] == 'D' || argv[Arg][1] == 'd') {
                    // ^
                } else if (argv[Arg][1] == 'H') {
                    // ^
                } else if (argv[Arg][1] == '+') {
                    QuoteNext = 1;
                } else if (argv[Arg][1] == '#' || argv[Arg][1] == 'l') {
                    LineNum = 1;
                    ColNum = 1;
                    if (strchr(argv[Arg], ',')) {
                        GotoLine = (2 == sscanf(argv[Arg] + 2, "%d,%d", &LineNum, &ColNum));
                    } else {
                        GotoLine = (1 == sscanf(argv[Arg] + 2, "%d", &LineNum));
                    }
                    //                printf("Gotoline = %d, line = %d, col = %d\n", GotoLine, LineNum, ColNum);
                } else if (argv[Arg][1] == 'r') {
                    ReadOnly = 1;
                } else if (argv[Arg][1] == 'm') {
                    if (argv[Arg][2] == 0) {
                        ModeOverride = 0;
                    } else {
                        ModeOverride = 1;
                        strcpy(Mode, argv[Arg] + 2);
                    }
                } else if (argv[Arg][1] == 'T') {
                    TagsAdd(argv[Arg] + 2);
                } else if (argv[Arg][1] == 't') {
                    TagGoto(EView.ActiveView, argv[Arg] + 2);
                } else {
                    DieError(2, "Invalid command line option %s", argv[Arg]);
                    return 0;
                }
            } else {
                char Path[MAXPATH];

                QuoteNext = 0;
                if (ExpandPath(argv[Arg], Path) == 0 && IsDirectory(Path)) {
                    EModel m = new EDirectory(cfAppend, EModel.ActiveModel, Path);
                    assert(EModel.ActiveModel != 0 && m != 0);
                } else {
                    if (LCount != 0)
                        suspendLoads = 1;
                    if (MultiFileLoad(cfAppend, argv[Arg],
                                      ModeOverride ? Mode : 0,
                                      EView.ActiveView) == 0) {
                        suspendLoads = 0;
                        return 0;
                    }
                    suspendLoads = 0;

                    if (GotoLine) {
                        if (((EBuffer *)EModel.ActiveModel).Loaded == 0)
                            ((EBuffer *)EModel.ActiveModel).Load();
                        if (GotoLine) {
                            GotoLine = 0;
                            ((EBuffer *)EModel.ActiveModel).SetNearPosR(ColNum - 1, LineNum - 1);
                        } else {
                            int r, c;

                            if (RetrieveFPos(((EBuffer)EModel.ActiveModel).FileName, r, c) == 1)
                                ((EBuffer)EModel.ActiveModel).SetNearPosR(c, r);
                        }
                        //EView.ActiveView.SelectModel(EModel.ActiveModel);
                    }
                    if (ReadOnly) {
                        ReadOnly = 0;
                        BFI(((EBuffer)EModel.ActiveModel), BFI_ReadOnly) = 1;
                    }
                }
                suspendLoads = 1;
                EView.ActiveView.SelectModel(EModel.ActiveModel.Next);
                suspendLoads = 0;
                LCount++;
            }
        } */
        
        EModel P = EModel.ActiveModel;
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

    /* TODO #ifdef CONFIG_HISTORY
        DoLoadHistoryOnEntry(argc, argv);
    #endif

    #ifdef CONFIG_DESKTOP
        DoLoadDesktopOnEntry(argc, argv);
    #endif */

        if (CmdLoadFiles(argv) == ExResult.ErFAIL)
            return 3;

        /* TODO
        if (EModel.ActiveModel == 0) {
    #ifdef CONFIG_OBJ_DIRECTORY
            char Path[MAXPATH];

            GetDefaultDirectory(0, Path, sizeof(Path));
            EModel *m = new EDirectory(0, &EModel.ActiveModel, Path);
            assert(EModel.ActiveModel != 0 && m != 0);
            EView.ActiveView.SwitchToModel(EModel.ActiveModel);
    #else
            Usage();
            return 1;
    #endif
        } */
        return 0;
    }

    void Stop() {
    /** TODO #ifdef CONFIG_HISTORY
        DoSaveHistoryOnExit();
    #endif */


        super.Stop();
    }
    
}
