package ru.dz.jfte;

public class EView {
    EView Next;        // next view
    EView Prev;        // prev view
    ExModelView MView; // model view controller
    EModel Model;       // model for this view
    EView NextView;    // next view for model
    EViewPort Port;
    String CurMsg;


    static EView ActiveView = null;
    
    EView(EModel AModel) {
        if (ActiveView != null) {
            Prev = ActiveView;
            Next = ActiveView.Next;
            Prev.Next = this;
            Next.Prev = this;
        } else
            Prev = Next = this;
        ActiveView = this;
        Model = AModel;
        NextView = null;
        Port = null;
        MView = null;
        CurMsg = null;
        if (Model != null)
            Model.CreateViewPort(this);
    }

    /* TODO
    ~EView() {
        if (Next != this) {
            Prev.Next = Next;
            Next.Prev = Prev;
            if (ActiveView == this)
                ActiveView = Next;
        } else
            ActiveView = 0;
        if (MView)
            MView.View = 0;
        if (Model)
            Model.RemoveView(this);
        if (Port)
            delete Port;
    }*/

    int CanQuit() {
        if (Model != null)
            return Model.CanQuit();
        else
            return 1;
    }

    void FocusChange(int GetFocus) {
        if (GetFocus != 0) {
            if ((Model.View != null) && (Model.View.Port != null))
                Model.View.Port.GetPos();
            Model.CreateViewPort(this);
        } else {
            if (Model != null) {
                Model.RemoveView(this);
                Port = null;
                if ((Model.View != null) && (Model.View.Port != null))
                    Model.View.Port.StorePos();
            }
        }
    }

    void Resize(int Width, int Height) {
        if (Port != null)
            Port.Resize(Width, Height);
    }

    void SetModel(EModel AModel) {
        Model = AModel;
        EModel.ActiveModel = Model;
    }

    void SelectModel(EModel AModel) {
        if (Model != AModel) {
            if (Model != null)
                FocusChange(0);
            SetModel(AModel);
            if (Model != null)
                FocusChange(1);
        }
    }

    void SwitchToModel(EModel AModel) {
        if (Model != AModel) {
            if (Model != null)
                FocusChange(0);

            AModel.Prev.Next = AModel.Next;
            AModel.Next.Prev = AModel.Prev;

            if (Model != null) {
                AModel.Next = Model;
                AModel.Prev = Model.Prev;
                AModel.Prev.Next = AModel;
                Model.Prev = AModel;
            } else {
                AModel.Next = AModel.Prev = AModel;
            }

            SetModel(AModel);

            if (Model != null)
                FocusChange(1);
        }
    }

    void Activate(int GotFocus) {
        if ((Model!=null) && Model.View != this && (Port!=null)) {
            Model.SelectView(this);
            if (GotFocus!=0) {
                Port.StorePos();
            } else {
                Port.GetPos();
            }
            Port.RepaintView();
            if (GotFocus!=0)
                ActiveView = this;
        }
    }

    int GetContext() {
        return (Model != null) ? Model.GetContext() : 0;
    }

    EEventMap GetEventMap() {
        return (Model != null) ? Model.GetEventMap() : 0;
    }

    int BeginMacro() {
        return (Model!=null) ? Model.BeginMacro() : 0;
    }

    int ExecCommand(ExCommands Command, ExState State) {
        switch (Command) {
        case ExSwitchTo:            return SwitchTo(State);
        case ExFilePrev:            return FilePrev();
        case ExFileNext:            return FileNext();
        case ExFileLast:            return FileLast();
        case ExFileOpen:            return FileOpen(State);
        case ExFileOpenInMode:      return FileOpenInMode(State);
        case ExFileSaveAll:         return FileSaveAll();

        case ExListRoutines:
            return ViewRoutines(State);

        case ExDirOpen:
            return DirOpen(State);

        case ExViewMessages:        return ViewMessages(State);
        case ExCompile:             return Compile(State);
        case ExRunCompiler:         return RunCompiler(State);
        case ExCompilePrevError:    return CompilePrevError(State);
        case ExCompileNextError:    return CompileNextError(State);

        case ExCvs:                 return Cvs(State);
        case ExRunCvs:              return RunCvs(State);
        case ExViewCvs:             return ViewCvs(State);
        case ExClearCvsMessages:    return ClearCvsMessages(State);
        case ExCvsDiff:             return CvsDiff(State);
        case ExRunCvsDiff:          return RunCvsDiff(State);
        case ExViewCvsDiff:         return ViewCvsDiff(State);
        case ExCvsCommit:           return CvsCommit(State);
        case ExRunCvsCommit:        return RunCvsCommit(State);
        case ExViewCvsLog:          return ViewCvsLog(State);

        case ExViewBuffers:         return ViewBuffers(State);

        case ExShowKey:             return ShowKey(State);
        case ExToggleSysClipboard:  return ToggleSysClipboard(State);
        case ExSetPrintDevice:      return SetPrintDevice(State);
        case ExShowVersion:         return ShowVersion();
        case ExViewModeMap:         return ViewModeMap(State);
        case ExClearMessages:       return ClearMessages();

        case ExTagNext:             return TagNext(this);
        case ExTagPrev:             return TagPrev(this);
        case ExTagPop:              return TagPop(this);
        case ExTagClear:            TagClear(); return 1;
        case ExTagLoad:             return TagLoad(State);

        case ExShowHelp:            return SysShowHelp(State, 0);
        case ExConfigRecompile:     return ConfigRecompile(State);
        case ExRemoveGlobalBookmark:return RemoveGlobalBookmark(State);
        case ExGotoGlobalBookmark:  return GotoGlobalBookmark(State);
        case ExPopGlobalBookmark:   return PopGlobalBookmark();
        }
        return Model ? Model.ExecCommand(Command, State) : 0;
    }

    void HandleEvent(TEvent Event) {
        if (Model!=null)
            Model.HandleEvent(Event);
        if (Port!=null)
            Port.HandleEvent(Event);
        if (Event.What == evCommand) {
            switch (Event.Msg.Command) {
            case cmDroppedFile:
                {
                    String file = (String)Event.Msg.Param2;

                    if (IsDirectory(file))
                        OpenDir(file);
                    MultiFileLoad(0, file, null, this);
                }
                break;
            }
        }
    }

    void UpdateView() {
        if (Port != null)
            Port.UpdateView();
    }

    void RepaintView() {
        if (Port != null)
            Port.RepaintView();
    }

    void UpdateStatus() {
        if (Port != null)
            Port.UpdateStatus();
    }

    void RepaintStatus() {
        if (Port != null)
            Port.RepaintStatus();
    }

    void DeleteModel(EModel M) {
        EView V;
        EModel M1;
        String s;

        if (M == null)
            return;

        s = M.GetName();
        Msg(S_INFO, "Closing %s.", s);

        V = ActiveView = this;
        while (V!=null) {
            M1 = V.Model;
            if (M1 == M) {
                if (M.Next != M)
                    V.SelectModel(M.Next);
                else
                    V.SelectModel(null);
            }
            V = V.Next;
            if (V == ActiveView)
                break;
        }
        M.close();
        SetMsg(null);
        return;
    }

    /////////////////////////////////////////////////////////////////////////////
    /////////////////////////////////////////////////////////////////////////////

    int FilePrev() {
        if (Model != null) {
            EModel n=Model.Prev;
            if (IgnoreBufferList&&n&&n.GetContext ()==CONTEXT_BUFFERS) n=n.Prev;
            SelectModel(n);
            return 1;
        }
        return 0;
    }

    int FileNext() {
        if (Model != null) {
            EModel n=Model.Next;
            if (IgnoreBufferList&&n&&n.GetContext ()==CONTEXT_BUFFERS) n=n.Next;
            SelectModel(n);
            return 1;
        }
        return 0;
    }

    int FileLast() {
        if (Model != null) {
            EModel n=Model.Next;
            if (IgnoreBufferList&&n&&n.GetContext ()==CONTEXT_BUFFERS) n=n.Next;
            SwitchToModel(n);
            return 1;
        }
        return 0;
    }

    int SwitchTo(ExState State) {
        EModel M;
        int No;

        if (State.GetIntParam(this, &No) == 0) {
            String [] str = {""};

            if (MView.Win.GetStr("Obj.Number", str, 0) == 0) return 0;
            No = atoi(str[0]);
        }
        M = Model;
        while (M!=null) {
            if (M.ModelNo == No) {
                SwitchToModel(M);
                return 1;
            }
            M = M.Next;
            if (M == Model)
                return 0;
        }
        return 0;
    }


    int FileSaveAll() {
        EModel M = Model;
        while (M != null) {
            if (M.GetContext() == CONTEXT_FILE) {
                EBuffer B = (EBuffer )M;
                if (B.Modified) {
                    SwitchToModel(B);
                    if (B.Save() == 0) return 0;
                }
            }
            M = M.Next;
            if (M == Model) break;
        }
        return 1;
    }

    int FileOpen(ExState State) {
        String [] FName;

        if (FName[0] = State.GetStrParam(this) == null) {
            if (GetDefaultDirectory(Model, FName, sizeof(FName)) == 0)
                return 0;
            if (MView.Win.GetFile("Open file", sizeof(FName), FName, HIST_PATH, GF_OPEN) == 0) return 0;
        }

        if( FName[0].length() == 0 ) return 0;

        if (IsDirectory(FName))
            return OpenDir(FName);

        return MultiFileLoad(0, FName, null, this);
    }

    int FileOpenInMode(ExState State) {
    	String Mode = "";
        String FName;

        if (Mode = State.GetStrParam(this) == 0)
            if (MView.Win.GetStr("Mode", sizeof(Mode), Mode, HIST_SETUP) != 1) return 0;

        if (FindMode(Mode) == 0) {
            MView.Win.Choice(GPC_ERROR, "Error", 1, "O&K", "Invalid mode '%s'", Mode);
            return 0;
        }

        if (GetDefaultDirectory(Model, FName, sizeof(FName)) == 0)
            return 0;
        if (State.GetStrParam(this, FName, sizeof(FName)) == 0)
            if (MView.Win.GetFile("Open file", sizeof(FName), FName, HIST_PATH, GF_OPEN) == 0) return 0;

        if (IsDirectory(FName))
            return OpenDir(FName);


        if( strlen( FName ) == 0 ) return 0;

        return MultiFileLoad(0, FName, Mode, this);
    }

    int SetPrintDevice(ExState State) {
        String Dev;

        strcpy(Dev, PrintDevice);
        if (State.GetStrParam(this, Dev, sizeof(Dev)) == 0)
            if (MView.Win.GetStr("Print to", sizeof(Dev), Dev, HIST_SETUP) == 0) return 0;

        strcpy(PrintDevice, Dev);
        return 1;
    }

    int ToggleSysClipboard(ExState State) {
        SystemClipboard = SystemClipboard ? 0 : 1;
        Msg(S_INFO, "SysClipboard is now %s.", SystemClipboard ? "ON" : "OFF");
        return 1;
    }

    int ShowKey(ExState State) {
        char buf[100];
        KeySel ks;

        ks.Mask = 0;
        ks.Key = MView.Win.GetChar(0);

        GetKeyName(buf, ks);
        Msg(S_INFO, "Key: '%s' - '%8X'", buf, ks.Key);
        return 1;
    }

    /* TODO
    void Msg(int level, String s, Object o ...) {
        va_list ap;

        va_start(ap, s);
        vsprintf(msgbuftmp, s, ap);
        va_end(ap);

        if (level != S_BUSY)
            SetMsg(msgbuftmp);
    } */

    void SetMsg(String Msg) {
        if (CurMsg!=null)
            CurMsg.close();
        CurMsg = null;
        if ((Msg!=null) && Msg.length()!=0)
            CurMsg = Msg;
        if ( (CurMsg!=null) && (Msg!=null) && (MView!=null)) {
            TDrawBuffer B;
            char SColor;
            int [] Cols, Rows;

            MView.ConQuerySize(Cols, Rows);

            if (MView.IsActive())
                SColor = hcStatus_Active;
            else
                SColor = hcStatus_Normal;

            MoveChar(B, 0, Cols[0], ' ', SColor, Cols[0]);
            MoveStr(B, 0, Cols[0], CurMsg, SColor, Cols[0]);
            if (MView.Win.GetStatusContext() == MView)
                MView.ConPutBox(0, Rows[0] - 1, Cols[0], 1, B);
            //printf("%s\n", Msg);
        }
    }

    int ViewBuffers(ExState State) {
        if (BufferList == null) {
            BufferList = new BufferView(0, ActiveModel);
            SwitchToModel(BufferList);
        } else {
            BufferList.UpdateList();
            BufferList.Row = 1;
            SwitchToModel(BufferList);
            return 1;
        }
        return 0;
    }

    int ViewRoutines(ExState State) {
        //int rc = 1;
        //RoutineView *routines;
        EModel M;
        EBuffer Buffer;

        M = Model;
        if (M.GetContext() != CONTEXT_FILE)
            return 0;
        Buffer = (EBuffer)M;

        if (Buffer.Routines == 0) {
            if (BFS(Buffer, BFS_RoutineRegexp) == 0) {
                MView.Win.Choice(GPC_ERROR, "Error", 1, "O&K", "No routine regexp.");
                return 0;
            }
            Buffer.Routines = new RoutineView(0, &ActiveModel, Buffer);
            if (Buffer.Routines == 0)
                return 0;
        } else {
            Buffer.Routines.UpdateList();
        }
        SwitchToModel(Buffer.Routines);
        return 1;
    }

    int DirOpen(ExState State) {
        String [] Path = {""};

        if (State.GetStrParam(this, Path) == 0)
            if (GetDefaultDirectory(Model, Path) == 0)
                return 0;
        return OpenDir(Path[0]);
    }

    int OpenDir(String Path) {
        char XPath[MAXPATH];
        EDirectory dir = 0;

        if (ExpandPath(Path, XPath) == -1)
            return 0;
        {
            EModel x = Model;
            while (x) {
                if (x.GetContext() == CONTEXT_DIRECTORY) {
                    if (filecmp(((EDirectory )x).Path, XPath) == 0)
                    {
                        dir = (EDirectory )x;
                        break;
                    }
                }
                x = x.Next;
                if (x == Model)
                    break;
            }
        }
        if (dir == 0)
            dir = new EDirectory(0, &ActiveModel, XPath);
        SelectModel(dir);
        return 1;
    }



    int Compile(ExState State) {
        String Cmd[] = {""};
        char Command[256] = "";

        if (CompilerMsgs != 0 && CompilerMsgs.Running) {
            Msg(S_INFO, "Already running...");
            return 0;
        }

        if (State.GetStrParam(this, Command, sizeof(Command)) == 0) {
            if (Model.GetContext() == CONTEXT_FILE) {
                EBuffer *B = (EBuffer *)Model;
                if (BFS(B, BFS_CompileCommand) != 0) 
                    strcpy(Cmd, BFS(B, BFS_CompileCommand));
            }
            if (Cmd[0] == 0)
                strcpy(Cmd, CompileCommand);

            if (MView.Win.GetStr("Compile", sizeof(Cmd), Cmd, HIST_COMPILE) == 0) return 0;

            strcpy(Command, Cmd);
        } else {
            if (MView.Win.GetStr("Compile", sizeof(Command), Command, HIST_COMPILE) == 0) return 0;
        }
        return Compile(Command);
    }

    int RunCompiler(ExState State) {
        char Command[256] = "";

        if (CompilerMsgs != 0 && CompilerMsgs.Running) {
            Msg(S_INFO, "Already running...");
            return 0;
        }

        if (State.GetStrParam(this, Command, sizeof(Command)) == 0) {
            if (Model.GetContext() == CONTEXT_FILE) {
                EBuffer B = (EBuffer )Model;
                if (BFS(B, BFS_CompileCommand) != 0) 
                    strcpy(Command, BFS(B, BFS_CompileCommand));
            }
            if (Command[0] == 0)
                strcpy(Command, CompileCommand);
        }
        return Compile(Command);
    }

    int Compile(String Command) {
        char Dir[MAXPATH] = "";
        EMessages msgs;
        
        if (CompilerMsgs != 0) {
            strcpy(Dir, CompilerMsgs.Directory);
            CompilerMsgs.RunPipe(Dir, Command);
            msgs = CompilerMsgs;
        } else {
            if (GetDefaultDirectory(Model, Dir, sizeof(Dir)) == 0)
                return 0;

            msgs = new EMessages(0, &ActiveModel, Dir, Command);
        }
        SwitchToModel(msgs);
        return 1;
    }

    int ViewMessages(ExState State) {
        if (CompilerMsgs != 0) {
            SwitchToModel(CompilerMsgs);
            return 1;
        }
        return 0;
    }

    int CompilePrevError(ExState State) {
        if (CompilerMsgs != 0)
            return CompilerMsgs.CompilePrevError(this);
        return 0;
    }

    int CompileNextError(ExState State) {
        if (CompilerMsgs != 0)
            return CompilerMsgs.CompileNextError(this);
        return 0;
    }


    int ShowVersion() {
        MView.Win.Choice(0, "About", 1, "O&K", PROGRAM + " " + VERSION + " " + COPYRIGHT);
        return 1;
    }

    int ViewModeMap(ExState State) {
        if (TheEventMapView != 0)
            TheEventMapView.ViewMap(GetEventMap());
        else
            (void)new EventMapView(0, ActiveModel, GetEventMap());
        if (TheEventMapView != 0)
            SwitchToModel(TheEventMapView);
        else
            return 0;
        return 1;
    }

    int ClearMessages() {
        if (CompilerMsgs != 0 && CompilerMsgs.Running) {
            Msg(S_INFO, "Running...");
            return 0;
        }
        if (CompilerMsgs != 0) {
            CompilerMsgs.FreeErrors();
            CompilerMsgs.UpdateList();
        }
        return 1;
    }

    int TagLoad(ExState State) {
        char Tag[MAXPATH];
        char FullTag[MAXPATH];

        char const* pTagFile = getenv("TAGFILE");
        if (pTagFile == NULL)
        {
            pTagFile = "tags";
        }
        if (ExpandPath(pTagFile, Tag) == -1)
            return 0;
        if (State.GetStrParam(this, Tag, sizeof(Tag)) == 0)
            if (MView.Win.GetFile("Load tags", sizeof(Tag), Tag, HIST_TAGFILES, GF_OPEN) == 0) return 0;

        if (ExpandPath(Tag, FullTag) == -1)
            return 0;

        if (!FileExists(FullTag)) {
            Msg(S_INFO, "Tag file '%s' not found.", FullTag);
            return 0;
        }

        return super.TagLoad(FullTag);
    }
    

    int ConfigRecompile(ExState State) {
        if (ConfigSourcePath == 0 || ConfigFileName[0] == 0) {
            Msg(S_ERROR, "Cannot recompile (must use external configuration).");
            return 0;
        }

        char command[1024];

        strcpy(command, "cfte ");
        strcat(command, ConfigSourcePath);
        strcat(command, " ");
    //#ifdef UNIX
        if (ExpandPath("~/.fterc", command + strlen(command)) != 0)
            return 0;
    /*TODO #else
        strcat(command, ConfigFileName);
    #endif */
        return Compile(command);
    }

    int RemoveGlobalBookmark(ExState State) {
    	 String [] name = {""};

        if (State.GetStrParam(this, name) == 0)
            if (MView.Win.GetStr("Remove Global Bookmark", name, HIST_BOOKMARK) == 0) return 0;
        if (markIndex.remove(name) == 0) {
            Msg(S_ERROR, "Error removing global bookmark %s.", name);
            return 0;
        }
        return 1;
    }

    int GotoGlobalBookmark(ExState State) {
        char name[256] = "";

        if (State.GetStrParam(this, name, sizeof(name)) == 0)
            if (MView.Win.GetStr("Goto Global Bookmark", sizeof(name), name, HIST_BOOKMARK) == 0) return 0;
        if (markIndex.view(this, name) == 0) {
            Msg(S_ERROR, "Error locating global bookmark %s.", name);
            return 0;
        }
        return 1;
    }
    int PopGlobalBookmark() {
        if (markIndex.popMark(this) == 0) {
            Msg(S_INFO, "Bookmark stack empty.");
            return 0;
        }
        return 1;
    }

    int GetStrVar(int var) {
        return Model.GetStrVar(var);
    }
                          
    int GetIntVar(int var, int []value) {
        //switch (var) {
        //}
        return Model.GetIntVar(var, value);
    }


    int Cvs(ExState State) {
        static char Opts[128] = "";
        char Options[128] = "";

        if (CvsView != 0 && CvsView.Running) {
            Msg(S_INFO, "Already running...");
            return 0;
        }

        if (State.GetStrParam(this, Options, sizeof(Options)) == 0) {
            if (MView.Win.GetStr("CVS options", sizeof(Opts), Opts, HIST_CVS) == 0) return 0;
            strcpy(Options, Opts);
        } else {
            if (MView.Win.GetStr("CVS options", sizeof(Options), Options, HIST_CVS) == 0) return 0;
        }
        return Cvs(Options);
    }

    int RunCvs(ExState State) {
        char Options[128] = "";

        if (CvsView != 0 && CvsView.Running) {
            Msg(S_INFO, "Already running...");
            return 0;
        }

        State.GetStrParam(this, Options, sizeof(Options));
        return Cvs(Options);
    }

    int Cvs(String Options) {
        String Dir = "";
        String Command = "";
        String buf = "";
        //char *OnFiles = buf;
        ECvs cvs;

        if (GetDefaultDirectory(Model, Dir, sizeof(Dir)) == 0) return 0;

        strcpy(Command, CvsCommand);
        strcat(Command, " ");
        if (Options[0] != 0) {
            strcat(Command, Options);
            strcat(Command, " ");
        }

        switch (Model.GetContext()) {
            case CONTEXT_FILE:
                if (JustFileName(((EBuffer)Model).FileName, OnFiles) != 0) return 0;
                break;
            case CONTEXT_CVSDIFF:
                OnFiles = strdup(CvsDiffView.OnFiles);
                break;
            case CONTEXT_CVS:
                OnFiles = ((ECvs)Model).MarkedAsList();
                if (!OnFiles) OnFiles = strdup(((ECvs)Model).OnFiles);
                break;
        }

        if (CvsView != 0) {
            CvsView.RunPipe(Dir, Command, OnFiles);
            cvs = CvsView;
        } else {
            cvs = new ECvs(0, ActiveModel, Dir, Command, OnFiles);
        }
        if (OnFiles != buf) free(OnFiles);
        SwitchToModel(cvs);
        return 1;
    }

    int ClearCvsMessages(ExState State) {
        if (CvsView != 0) {
            if (CvsView.Running) {
                Msg(S_INFO, "Running...");
                return 0;
            } else {
                CvsView.FreeLines();
                CvsView.UpdateList();
                return 1;
            }
        }
        return 0;
    }

    int ViewCvs(ExState State) {
        if (CvsView != 0) {
            SwitchToModel(CvsView);
            return 1;
        }
        return 0;
    }

    int CvsDiff(ExState State) {
        static char Opts[128] = "";
        char Options[128] = "";

        if (CvsDiffView != 0 && CvsDiffView.Running) {
            Msg(S_INFO, "Already running...");
            return 0;
        }

        if (State.GetStrParam(this, Options, sizeof(Options)) == 0) {
            if (MView.Win.GetStr("CVS diff options", sizeof(Opts), Opts, HIST_CVSDIFF) == 0) return 0;
            strcpy(Options, Opts);
        } else {
            if (MView.Win.GetStr("CVS diff options", sizeof(Options), Options, HIST_CVSDIFF) == 0) return 0;
        }
        return CvsDiff(Options);
    }

    int RunCvsDiff(ExState State) {
    	String Options = "";

        if (CvsDiffView != 0 && CvsDiffView.Running) {
            Msg(S_INFO, "Already running...");
            return 0;
        }

        State.GetStrParam(this, Options, sizeof(Options));
        return CvsDiff(Options);
    }

    int CvsDiff(String Options) {
    	String Dir = "";
    	String Command = "";
    	String buf = "";
        //char *OnFiles = buf;
        ECvsDiff diffs;

        if (GetDefaultDirectory(Model, Dir, sizeof(Dir)) == 0) return 0;

        strcpy(Command, CvsCommand);
        strcat(Command, " diff -c ");
        if (Options[0] != 0) {
            strcat(Command, Options);
            strcat(Command, " ");
        }

        switch (Model.GetContext()) {
            case CONTEXT_FILE:
                if (JustFileName(((EBuffer)Model).FileName, OnFiles) != 0) return 0;
                break;
            case CONTEXT_CVSDIFF:
                OnFiles = strdup(CvsDiffView.OnFiles);
                break;
            case CONTEXT_CVS:
                OnFiles = ((ECvs)Model).MarkedAsList();
                if (!OnFiles) OnFiles = strdup(((ECvs)Model).OnFiles);
                break;
        }

        if (CvsDiffView != 0) {
            CvsDiffView.RunPipe(Dir, Command, OnFiles);
            diffs = CvsDiffView;
        } else {
            diffs = new ECvsDiff(0, ActiveModel, Dir, Command, OnFiles);
        }
        if (OnFiles != buf) free(OnFiles);
        SwitchToModel(diffs);
        return 1;
    }

    int ViewCvsDiff(ExState State) {
        if (CvsDiffView != 0) {
            SwitchToModel(CvsDiffView);
            return 1;
        }
        return 0;
    }

    int CvsCommit(ExState &State) {
        static char Opts[128] = "";
        String Options = "";

        if (CvsView != 0 && CvsView.Running) {
            Msg(S_INFO, "Already running...");
            return 0;
        }

        if (State.GetStrParam(this, Options, sizeof(Options)) == 0) {
            if (MView.Win.GetStr("CVS commit options", sizeof(Opts), Opts, HIST_CVSCOMMIT) == 0) return 0;
            strcpy(Options, Opts);
        } else {
            if (MView.Win.GetStr("CVS commit options", sizeof(Options), Options, HIST_CVSCOMMIT) == 0) return 0;
        }
        return CvsCommit(Options);
    }

    int RunCvsCommit(ExState State) {
    	String Options = "";

        if (CvsView != 0 && CvsView.Running) {
            Msg(S_INFO, "Already running...");
            return 0;
        }

        State.GetStrParam(this, Options, sizeof(Options));
        return CvsCommit(Options);
    }

    int CvsCommit(String Options) {
    	String Dir = "";
    	String Command = "";
    	String buf = "";
        //char *OnFiles = buf;
        ECvs cvs;

        if (GetDefaultDirectory(Model, Dir, sizeof(Dir)) == 0) return 0;

        strcpy(Command, CvsCommand);
        strcat(Command, " commit ");
        if (Options[0] != 0) {
            strcat(Command, Options);
            strcat(Command, " ");
        }

        switch (Model.GetContext()) {
            case CONTEXT_FILE:
                if (JustFileName(((EBuffer )Model).FileName, OnFiles) != 0) return 0;
                break;
            case CONTEXT_CVSDIFF:
                OnFiles = strdup(CvsDiffView.OnFiles);
                break;
            case CONTEXT_CVS:
                OnFiles = ((ECvs )Model).MarkedAsList();
                if (!OnFiles) OnFiles = strdup(((ECvs )Model).OnFiles);
                break;
        }

        if (CvsView == 0) cvs = new ECvs(0, ActiveModel);else cvs = CvsView;
        SwitchToModel(cvs);
        cvs.RunCommit(Dir, Command, OnFiles);
        if (OnFiles != buf) free(OnFiles);
        return 1;
    }

    int ViewCvsLog(ExState State) {
        if (CvsLogView != 0) {
            SwitchToModel(CvsLogView);
            return 1;
        }
        return 0;
    }
    
};
