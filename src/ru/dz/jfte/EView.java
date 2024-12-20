package ru.dz.jfte;

import java.io.Closeable;
import java.io.IOException;

public class EView implements GuiDefs, EventDefs, ModeDefs, ColorDefs, Closeable 
{
	EView Next;        // next view
	EView Prev;        // prev view
	ExModelView MView; // model view controller
	EModel Model;       // model for this view
	//EView NextView;    // next view for model
	EViewPort Port;
	String CurMsg;


	//static final EView [] ActiveView = {null};
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
		//NextView = null;
		Port = null;
		MView = null;
		CurMsg = null;
		if (Model != null)
			Model.CreateViewPort(this);
	}

	/* 
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

	@Override
	public void close()
	{
		if (Next != this) {
			Prev.Next = Next;
			Next.Prev = Prev;
			if (ActiveView == this)
				ActiveView = Next;
		} else
			ActiveView = null;
		
		MView.View = null;

		if (Model!=null)
			Model.RemoveView(this);
	}

	boolean CanQuit() {
		if (Model != null)
			return Model.CanQuit();
		else
			return true;
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
		EModel.ActiveModel[0] = Model;
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

	void SwitchToModel(EModel [] AModel)
	{
		SwitchToModel(AModel[0]); 
	}
	
	void SwitchToModel(EModel AModel) 
	{
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

	void Activate(boolean GotFocus) {
		if ((Model!=null) && Model.View != this && (Port!=null)) {
			Model.SelectView(this);
			if (GotFocus) {
				Port.StorePos();
			} else {
				Port.GetPos();
			}
			Port.RepaintView();
			if (GotFocus)
				ActiveView = this;
		}
	}

	int GetContext() {
		return (Model != null) ? Model.GetContext() : 0;
	}

	EEventMap GetEventMap() {
		return (Model != null) ? Model.GetEventMap() : null;
	}

	int BeginMacro() {
		return (Model!=null) ? Model.BeginMacro() : 0;
	}

	ExResult ExecCommand(ExCommands Command, ExState State) {
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

        case ExTagNext:             return Tags.TagNext(this);
        case ExTagPrev:             return Tags.TagPrev(this);
        case ExTagPop:              return Tags.TagPop(this);
        case ExTagClear:            Tags.TagClear(); return ExResult.ErOK;
        case ExTagLoad:             return TagLoad(State);

		case ExShowHelp:            return Console.SysShowHelp(State, null);
		case ExConfigRecompile:     return ConfigRecompile(State);
		case ExRemoveGlobalBookmark:return RemoveGlobalBookmark(State);
		case ExGotoGlobalBookmark:  return GotoGlobalBookmark(State);
		case ExPopGlobalBookmark:   return PopGlobalBookmark();
		}

		return Model != null ? Model.ExecCommand(Command, State) : ExResult.ErFAIL;
	}

	void HandleEvent(TEvent Event) {
		if (Model!=null)
			Model.HandleEvent(Event);
		if (Port!=null)
			Port.HandleEvent(Event);
		if (Event.What == evCommand) {
			switch (((TMsgEvent)Event).Command) {
			case cmDroppedFile:
			{
				String file = (String) ((TMsgEvent)Event).Param2;

				if (Console.IsDirectory(file))
					OpenDir(file);
				Console.MultiFileLoad(0, file, null, this);
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

	ExResult FilePrev() {
		if (Model != null) {
			EModel n = Model.Prev;
			if (Config.IgnoreBufferList && n!=null && n.GetContext ()==CONTEXT_BUFFERS) n=n.Prev;
			SelectModel(n);
			return ExResult.ErOK;
		}
		return ExResult.ErFAIL;
	}

	ExResult FileNext() {
		if (Model != null) {
			EModel n = Model.Next;
			if (Config.IgnoreBufferList && n!=null && n.GetContext ()==CONTEXT_BUFFERS) n=n.Next;
			SelectModel(n);
			return ExResult.ErOK;
		}
		return ExResult.ErFAIL;
	}

	ExResult FileLast() {
		if (Model != null) {
			EModel n=Model.Next;
			if (Config.IgnoreBufferList && n!=null && n.GetContext ()==CONTEXT_BUFFERS) n=n.Next;
			SwitchToModel(n);
			return ExResult.ErOK;
		}
		return ExResult.ErFAIL;
	}

	ExResult SwitchTo(ExState State) {
		EModel M;
		int [] No = {0};

		if (State.GetIntParam(this, No) == 0) {
			String [] str = {""};

			if (MView.Win.GetStr("Obj.Number", str, 0) == 0) return ExResult.ErFAIL;
			No[0] = Integer.parseInt(str[0]);
		}
		M = Model;
		while (M!=null) {
			if (M.getModelNo() == No[0]) {
				SwitchToModel(M);
				return ExResult.ErOK;
			}
			M = M.Next;
			if (M == Model)
				return ExResult.ErFAIL;
		}
		return ExResult.ErFAIL;
	}


	ExResult FileSaveAll() {
		EModel M = Model;
		while (M != null) {
			if (M.GetContext() == CONTEXT_FILE) {
				EBuffer B = (EBuffer )M;
				if (B.Modified!=0) {
					SwitchToModel(B);
					if (!B.Save()) return ExResult.ErFAIL;
				}
			}
			M = M.Next;
			if (M == Model) break;
		}
		return ExResult.ErOK;
	}

	ExResult FileOpen(ExState State) {
		String [] FName = {""};

		if (State.GetStrParam(this,FName) == 0) {
			if (Console.GetDefaultDirectory(Model, FName) == 0)
				return ExResult.ErFAIL;
			if (MView.Win.GetFile("Open file", FName, HIST_PATH, GF_OPEN) == 0) return ExResult.ErFAIL;
		}

		FName[0] = FName[0].strip(); // [dz] remove it - fix line edit code to have no extra spaces
		
		if( FName[0].length() == 0 ) return ExResult.ErFAIL;

		if (Console.IsDirectory(FName[0]))
			return OpenDir(FName[0]);

		return Console.MultiFileLoad(0, FName[0], null, this) ? ExResult.ErOK : ExResult.ErFAIL;
	}

	ExResult FileOpenInMode(ExState State) {
		String [] Mode = {""};
		String [] FName = {""};

		if ( State.GetStrParam(this, Mode) == 0)
			if (MView.Win.GetStr("Mode", Mode, HIST_SETUP) != 1) return ExResult.ErFAIL;

		if (EMode.FindMode(Mode[0]) == null) {
			MView.Win.Choice(GPC_ERROR, "Error", 1, "O&K", "Invalid mode '%s'", Mode[0]);
			return ExResult.ErFAIL;
		}

		if (Console.GetDefaultDirectory(Model, FName) == 0)
			return ExResult.ErFAIL;
		if (State.GetStrParam(this, FName) == 0)
			if (MView.Win.GetFile("Open file", FName, HIST_PATH, GF_OPEN) == 0) return ExResult.ErFAIL;

		if (Console.IsDirectory(FName[0]))
			return OpenDir(FName[0]);


		if( FName[0].length() == 0 ) return ExResult.ErFAIL;

		return Console.MultiFileLoad(0, FName[0], Mode[0], this) ? ExResult.ErOK : ExResult.ErFAIL;
	}

	ExResult SetPrintDevice(ExState State)  {
		String [] Dev = {Config.PrintDevice};

		if (State.GetStrParam(this, Dev) == 0)
			if (MView.Win.GetStr("Print to", Dev, HIST_SETUP) == 0) return ExResult.ErFAIL;

		Config.PrintDevice = Dev[0];
		return ExResult.ErOK;
	}

	ExResult ToggleSysClipboard(ExState State) {
		Config.SystemClipboard = !Config.SystemClipboard;
		Msg(S_INFO, "SysClipboard is now %s.", Config.SystemClipboard ? "ON" : "OFF");
		return ExResult.ErOK;
	}

	ExResult ShowKey(ExState State) {
		String [] buf = {""};
		KeySel ks = new KeySel();

		ks.Mask = 0;
		ks.Key = (int) MView.Win.GetChar(null);

		KeyDefs.GetKeyName(buf, ks);
		Msg(S_INFO, "Key: '%s' - '%8X'", buf[0], ks.Key);
		return ExResult.ErOK;
	}

	void Msg(int level, String s, Object... o) 
	{
		String m = String.format(s, o);
		/*va_list ap;

        va_start(ap, s);
        vsprintf(msgbuftmp, s, ap);
        va_end(ap); */

		if (level != S_BUSY)
			SetMsg(m);
	}

	void SetMsg(String Msg) 
	{
		CurMsg = null;
		if ((Msg!=null) && Msg.length()!=0)
			CurMsg = Msg;

		if ( (CurMsg!=null) && (Msg!=null) && (MView!=null)) {
			TDrawBuffer B = new TDrawBuffer();
			char SColor;
			int [] Cols = {0}, Rows = {0};

			MView.ConQuerySize(Cols, Rows);

			if (MView.IsActive())
				SColor = hcStatus_Active;
			else
				SColor = hcStatus_Normal;

			B.MoveChar( 0, Cols[0], ' ', SColor, Cols[0]);
			B.MoveStr( 0, Cols[0], CurMsg, SColor, Cols[0]);
			if (MView.Win.GetStatusContext() == MView)
				MView.ConPutBox(0, Rows[0] - 1, Cols[0], 1, B);
			//printf("%s\n", Msg);
		}
	}

	ExResult ViewBuffers(ExState State) {
		if (BufferView.BufferList == null) {
			BufferView.BufferList = new BufferView(0, EModel.ActiveModel);
			SwitchToModel(BufferView.BufferList);
		} else {
			BufferView.BufferList.UpdateList();
			BufferView.BufferList.Row = 1;
			SwitchToModel(BufferView.BufferList);
			return ExResult.ErOK;
		}
		return ExResult.ErFAIL;
	}

	ExResult ViewRoutines(ExState State) {
		//int rc = 1;
		//RoutineView *routines;
		EModel M;
		EBuffer Buffer;

		M = Model;
		if (M.GetContext() != CONTEXT_FILE)
			return ExResult.ErFAIL;
		Buffer = (EBuffer)M;

		if (Buffer.Routines == null) {
			if (EBuffer.BFS(Buffer, BFS_RoutineRegexp) == null) {
				MView.Win.Choice(GPC_ERROR, "Error", 1, "O&K", "No routine regexp.");
				return ExResult.ErFAIL;
			}
			//EModel [] am = {EModel.ActiveModel};
			Buffer.Routines = new RoutineView(0, EModel.ActiveModel, Buffer);
			if (Buffer.Routines == null)
				return ExResult.ErFAIL;
		} else {
			Buffer.Routines.UpdateList();
		}
		SwitchToModel(Buffer.Routines);
		return ExResult.ErOK;
	}

	ExResult DirOpen(ExState State) {
		String [] Path = {""};

		if (State.GetStrParam(this, Path) == 0)
			if (Console.GetDefaultDirectory(Model, Path) == 0)
				return ExResult.ErFAIL;
		return OpenDir(Path[0]);
	}

	ExResult OpenDir(String Path) {
		String [] XPath = {""};
		EDirectory dir = null;

		if (Console.ExpandPath(Path, XPath) == -1)
			return ExResult.ErFAIL;
		{
			EModel x = Model;
			while (x != null) {
				if (x.GetContext() == CONTEXT_DIRECTORY) {
					if (Console.filecmp(((EDirectory )x).Path, XPath[0]) == 0)
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
		if (dir == null)
		{
			//EModel [] rootPtr = {EModel.ActiveModel};
			dir = new EDirectory(0, EModel.ActiveModel, XPath[0]);
		}
		SelectModel(dir);
		return ExResult.ErOK;
	}



	ExResult Compile(ExState State) {
		String [] Cmd     = {""};
		String [] Command = {""};

		if (EMessages.CompilerMsgs != null && EMessages.CompilerMsgs.Running) {
			Msg(S_INFO, "Already running...");
			return ExResult.ErFAIL;
		}

		if (State.GetStrParam(this, Command) == 0) {
			if (Model.GetContext() == CONTEXT_FILE) {
				EBuffer B = (EBuffer )Model;
				if (EBuffer.BFS(B, BFS_CompileCommand) != null) 
					Cmd[0] = EBuffer.BFS(B, BFS_CompileCommand);
			}
			if (Cmd[0] == null)
				Cmd[0] = Config.CompileCommand;

			if (MView.Win.GetStr("Compile", Cmd, HIST_COMPILE) == 0) return ExResult.ErFAIL;

			Command[0] = Cmd[0];
		} else {
			if (MView.Win.GetStr("Compile", Command, HIST_COMPILE) == 0) return ExResult.ErFAIL;
		}
		return Compile(Command[0]);
	}

	ExResult RunCompiler(ExState State) {
		String [] Command = {""};

		if (EMessages.CompilerMsgs != null && EMessages.CompilerMsgs.Running) {
			Msg(S_INFO, "Already running...");
			return ExResult.ErFAIL;
		}

		if (State.GetStrParam(this, Command) == 0) {
			if (Model.GetContext() == CONTEXT_FILE) {
				EBuffer B = (EBuffer )Model;
				if (EBuffer.BFS(B, BFS_CompileCommand) != null) 
					Command[0] = EBuffer.BFS(B, BFS_CompileCommand);
			}
			if (Command[0] == null)
				Command[0] = Config.CompileCommand;
		}
		return Compile(Command[0]);
	}

	ExResult Compile(String Command) {
		String [] Dir = {""};
		EMessages msgs;

		if (EMessages.CompilerMsgs != null) {
			Dir[0] = EMessages.CompilerMsgs.Directory;
			EMessages.CompilerMsgs.RunPipe(Dir[0], Command);
			msgs = EMessages.CompilerMsgs;
		} else {
			if (Console.GetDefaultDirectory(Model, Dir) == 0)
				return ExResult.ErFAIL;

			msgs = new EMessages(0, EModel.ActiveModel, Dir[0], Command);
		}
		SwitchToModel(msgs);
		return ExResult.ErOK;
	}

	ExResult ViewMessages(ExState State) {
		if (EMessages.CompilerMsgs != null) {
			SwitchToModel(EMessages.CompilerMsgs);
			return ExResult.ErOK;
		}
		return ExResult.ErFAIL;
	}

	ExResult CompilePrevError(ExState State) {
		if (EMessages.CompilerMsgs != null)
			return EMessages.CompilerMsgs.CompilePrevError(this);
		return ExResult.ErFAIL;
	}

	ExResult CompileNextError(ExState State) {
		if (EMessages.CompilerMsgs != null)
			return EMessages.CompilerMsgs.CompileNextError(this);
		return ExResult.ErFAIL;
	}


	ExResult ShowVersion() {
		MView.Win.Choice(0, "About", 1, "O&K", MainConst.PROGRAM + " " + MainConst.VERSION + " " + MainConst.COPYRIGHT);
		return ExResult.ErOK;
	}

	ExResult ViewModeMap(ExState State) 
	{
        if (EventMapView.TheEventMapView != null)
        	EventMapView.TheEventMapView.ViewMap(GetEventMap());
        else
            new EventMapView(0, EModel.ActiveModel, GetEventMap());
        
        if (EventMapView.TheEventMapView != null)
            SwitchToModel(EventMapView.TheEventMapView);
        else
            return ExResult.ErFAIL;
        return ExResult.ErOK;
	}

	ExResult ClearMessages() {
		if (EMessages.CompilerMsgs != null && EMessages.CompilerMsgs.Running) {
			Msg(S_INFO, "Running...");
			return ExResult.ErFAIL;
		}
		if (EMessages.CompilerMsgs != null) {
			EMessages.CompilerMsgs.FreeErrors();
			EMessages.CompilerMsgs.UpdateList();
		}
		return ExResult.ErOK;
	}


	ExResult TagLoad(ExState State) {
		String Tag[] = {""};
		String FullTag[] = {""};

		String pTagFile = Main.getenv("TAGFILE");
		if (pTagFile == null)
		{
			pTagFile = "tags";
		}
		if (Console.ExpandPath(pTagFile, Tag) == -1)
			return ExResult.ErFAIL;
		
		if (State.GetStrParam(this, Tag) == 0)
			if (MView.Win.GetFile("Load tags", Tag, HIST_TAGFILES, GF_OPEN) == 0) return ExResult.ErFAIL;

		if (Console.ExpandPath(Tag[0], FullTag) == -1)
			return ExResult.ErFAIL;

		if (!Console.FileExists(FullTag[0])) {
			Msg(S_INFO, "Tag file '%s' not found.", FullTag);
			return ExResult.ErFAIL;
		}

		return Tags.TagLoad(FullTag[0]);
	}


	ExResult ConfigRecompile(ExState State) 
	{
		if (Config.ConfigSourcePath == null || Main.ConfigFileName !=null) {
			Msg(S_ERROR, "Cannot recompile (must use external configuration).");
			return ExResult.ErFAIL;
		}

		String command = "cfte "+Config.ConfigSourcePath+" ";
		//#ifdef UNIX
		String [] exp = {""};
		if (Console.ExpandPath("~/.fterc", exp) != 0)
			return ExResult.ErFAIL;
		command += exp[0];
        command += Main.ConfigFileName;

		// XXX Run conf compiler directly?
		return Compile(command);
	}

	ExResult RemoveGlobalBookmark(ExState State) {
		String [] name = {""};

		if (State.GetStrParam(this, name) == 0)
			if (MView.Win.GetStr("Remove Global Bookmark", name, HIST_BOOKMARK) == 0) return ExResult.ErFAIL;
		if (EMarkIndex.markIndex.remove(name[0]) == 0) {
			Msg(S_ERROR, "Error removing global bookmark %s.", name);
			return ExResult.ErFAIL;
		}
		return ExResult.ErOK;
	}

	ExResult GotoGlobalBookmark(ExState State) {
		String [] name = {""};

		if (State.GetStrParam(this, name ) == 0)
			if (MView.Win.GetStr("Goto Global Bookmark", name, HIST_BOOKMARK) == 0) return ExResult.ErFAIL;
		if (!EMarkIndex.markIndex.view(this, name[0])) {
			Msg(S_ERROR, "Error locating global bookmark %s.", name[0]);
			return ExResult.ErFAIL;
		}
		return ExResult.ErOK;
	}

	ExResult PopGlobalBookmark() {
		if (EMarkIndex.markIndex.popMark(this) == 0) {
			Msg(S_INFO, "Bookmark stack empty.");
			return ExResult.ErFAIL;
		}
		return ExResult.ErOK;
	}

	int GetStrVar(int var, String [] str) {
		return Model.GetStrVar(var, str);
	}

	int GetIntVar(int var, int []value) {
		//switch (var) {
		//}
		return Model.GetIntVar(var, value);
	}

	
	
	
	
	
	

	//*

	ExResult Cvs(ExState State) {
        //static char Opts[128] = "";
        //char Options[128] = "";
		String [] Options = {""};

        if (ECvs.CvsView != null && ECvs.CvsView.Running) {
            Msg(S_INFO, "Already running...");
            return ExResult.ErFAIL;
        }

        if (State.GetStrParam(this, Options) == 0) {
            if (MView.Win.GetStr("CVS options", Opts, HIST_CVS) == 0) return ExResult.ErFAIL;
            //Options = Opts;
        } else {
            if (MView.Win.GetStr("CVS options", Options, HIST_CVS) == 0) return ExResult.ErFAIL;
        }
        return Cvs(Options[0]);
    }

	ExResult RunCvs(ExState State) {
        //char Options[128] = "";
		String [] Options = {""};

        if (ECvs.CvsView != null && ECvs.CvsView.Running) {
            Msg(S_INFO, "Already running...");
            return ExResult.ErFAIL;
        }

        State.GetStrParam(this, Options);
        return Cvs(Options[0]);
    }

    ExResult Cvs(String Options) {
        String [] Dir = {""};
        String Command = "";
        String [] OnFiles  = {""};
        ECvs cvs;

        if (Console.GetDefaultDirectory(Model, Dir) == 0) return ExResult.ErFAIL;

        Command = Config.CvsCommand+" ";
        if (Options != null) {
            Command += Options+" ";
        }

        switch (Model.GetContext()) {
            case CONTEXT_FILE:
                if (Console.JustFileName(((EBuffer)Model).FileName, OnFiles) != 0) return ExResult.ErFAIL;
                break;
            case CONTEXT_CVSDIFF:
                OnFiles[0] = ECvsDiff.CvsDiffView.OnFiles;
                break;
            case CONTEXT_CVS:
                OnFiles[0] = ((ECvs)Model).MarkedAsList();
                if(null == OnFiles[0]) OnFiles[0] = ((ECvs)Model).OnFiles;
                break;
        }

        if (ECvs.CvsView != null) {
            ECvs.CvsView.RunPipe(Dir[0], Command, OnFiles[0]);
            cvs = ECvs.CvsView;
        } else {
            cvs = new ECvs(0, EModel.ActiveModel, Dir[0], Command, OnFiles[0]);
        }

        SwitchToModel(cvs);
        return ExResult.ErOK;
    }

    ExResult ClearCvsMessages(ExState State) {
        if (ECvs.CvsView != null) {
            if (ECvs.CvsView.Running) {
                Msg(S_INFO, "Running...");
                return ExResult.ErFAIL;
            } else {
                ECvs.CvsView.FreeLines();
                ECvs.CvsView.UpdateList();
                return ExResult.ErOK;
            }
        }
        return ExResult.ErFAIL;
    }

    ExResult ViewCvs(ExState State) {
        if (ECvs.CvsView != null) {
            SwitchToModel(ECvs.CvsView);
            return ExResult.ErOK;
        }
        return ExResult.ErFAIL;
    }

    ExResult CvsDiff(ExState State) {
        //static char Opts[128] = "";
        //char Options[128] = "";
    	String [] Options = {""};

        if (ECvsDiff.CvsDiffView != null && ECvsDiff.CvsDiffView.Running) {
            Msg(S_INFO, "Already running...");
            return ExResult.ErFAIL;
        }

        if (State.GetStrParam(this, Options) == 0) {
            if (MView.Win.GetStr("CVS diff options", Opts, HIST_CVSDIFF) == 0) return ExResult.ErFAIL;
            Options[0] = Opts[0];
        } else {
            if (MView.Win.GetStr("CVS diff options", Options, HIST_CVSDIFF) == 0) return ExResult.ErFAIL;
        }
        return CvsDiff(Options[0]);
    }

    ExResult RunCvsDiff(ExState State) {
    	String [] Options = {""};

        if (ECvsDiff.CvsDiffView != null && ECvsDiff.CvsDiffView.Running) {
            Msg(S_INFO, "Already running...");
            return ExResult.ErFAIL;
        }

        State.GetStrParam(this, Options);
        return CvsDiff(Options[0]);
    }

    ExResult CvsDiff(String Options) {
    	String [] Dir = {""};
    	String Command = "";
    	String buf = "";
        //char *OnFiles = buf;
        ECvsDiff diffs;
        String [] OnFiles = {""};

        if (Console.GetDefaultDirectory(Model, Dir) == 0) return ExResult.ErFAIL;

        Command = Config.CvsCommand+" diff -c ";
        if (Options != null) {
            Command += Options+" ";
        }

        switch (Model.GetContext()) {
            case CONTEXT_FILE:
                if (Console.JustFileName(((EBuffer)Model).FileName, OnFiles) != 0) return ExResult.ErFAIL;
                break;
            case CONTEXT_CVSDIFF:
                OnFiles[0] = ECvsDiff.CvsDiffView.OnFiles;
                break;
            case CONTEXT_CVS:
                OnFiles[0] = ((ECvs)Model).MarkedAsList();
                if (null == OnFiles[0]) OnFiles[0] = ((ECvs)Model).OnFiles;
                break;
        }

        if (ECvsDiff.CvsDiffView != null) {
            ECvsDiff.CvsDiffView.RunPipe(Dir[0], Command, OnFiles[0]);
            diffs = ECvsDiff.CvsDiffView;
        } else {
            diffs = new ECvsDiff(0, EModel.ActiveModel, Dir[0], Command, OnFiles[0]);
        }

        SwitchToModel(diffs);
        return ExResult.ErOK;
    }

    ExResult ViewCvsDiff(ExState State) {
        if (ECvsDiff.CvsDiffView != null) {
            SwitchToModel(ECvsDiff.CvsDiffView);
            return ExResult.ErOK;
        }
        return ExResult.ErFAIL;
    }

    static String [] Opts = {""};
    ExResult CvsCommit(ExState State) {
        String [] Options = {""};

        if (ECvs.CvsView != null && ECvs.CvsView.Running) {
            Msg(S_INFO, "Already running...");
            return ExResult.ErFAIL;
        }

        if (State.GetStrParam(this, Options) == 0) {
            if (MView.Win.GetStr("CVS commit options", Opts, HIST_CVSCOMMIT) == 0) return ExResult.ErFAIL;
            Options[0] = Opts[0];
        } else {
            if (MView.Win.GetStr("CVS commit options", Options, HIST_CVSCOMMIT) == 0) return ExResult.ErFAIL;
        }
        return CvsCommit(Options[0]);
    }

    ExResult RunCvsCommit(ExState State) {
        String [] Options = {""};

        if (ECvs.CvsView != null && ECvs.CvsView.Running) {
            Msg(S_INFO, "Already running...");
            return ExResult.ErFAIL;
        }

        State.GetStrParam(this, Options);
        return CvsCommit(Options[0]);
    }

    ExResult CvsCommit(String Options) {
    	String [] Dir = {""};
    	String Command = "";
    	String buf = "";
        //char *OnFiles = buf;
        ECvs cvs;
        String [] OnFiles = {""};

        if (Console.GetDefaultDirectory(Model, Dir) == 0) return ExResult.ErFAIL;

        Command = Config.CvsCommand+" commit ";
        if (Options != null) {
            Command += Options+" ";
        }

        switch (Model.GetContext()) {
            case CONTEXT_FILE:
                if (Console.JustFileName(((EBuffer )Model).FileName, OnFiles) != 0) return ExResult.ErFAIL;
                break;
            case CONTEXT_CVSDIFF:
                OnFiles[0] = ECvsDiff.CvsDiffView.OnFiles;
                break;
            case CONTEXT_CVS:
                OnFiles[0] = ((ECvs )Model).MarkedAsList();
                if (null == OnFiles[0]) OnFiles[0] = ((ECvs )Model).OnFiles;
                break;
        }

        if (ECvs.CvsView == null) cvs = new ECvs(0, EModel.ActiveModel);else cvs = ECvs.CvsView;
        SwitchToModel(cvs);
        cvs.RunCommit(Dir[0], Command, OnFiles[0]);

        return ExResult.ErOK;
    }

    ExResult ViewCvsLog(ExState State) {
        if (ECvsLog.CvsLogView != null) {
            SwitchToModel(ECvsLog.CvsLogView);
            return ExResult.ErOK;
        }
        return ExResult.ErFAIL;
    }
	// */


};
