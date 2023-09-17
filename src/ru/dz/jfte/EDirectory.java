package ru.dz.jfte;

public class EDirectory extends EList 
{

	String Path;
	FileInfo [] Files = null;
	int FCount;
	int SearchLen;
	char SearchName[];
	int SearchPos[];



	
	
	EDirectory(int createFlags, EModel []ARoot, String aPath) 
	{
		super(createFlags, ARoot, aPath);
		
	    String [] XPath = {""};

	    FCount = 0;
	    SearchLen = 0;
	    ExpandPath(aPath, XPath);
	    Slash(XPath, 1);
	    Path = XPath;
	    RescanList();
	}


	EEventMap GetEventMap() {
	    return FindEventMap("DIRECTORY");
	}

	void DrawLine(int /*TCell*/ B, int Line, int Col, int /*ChColor*/ color, int Width) {
	    //char s[1024];

	    MoveCh(B, ' ', color, Width);
	    if (Files && Line >= 0 && Line < FCount) {
	        int Year, Mon, Day, Hour, Min, Sec;
	        struct tm *t;
	        time_t tim;

	        tim = Files[Line].MTime();
	        t = localtime(&tim);

	        if (t) {
	            Year = t.tm_year + 1900;
	            Mon = t.tm_mon + 1;
	            Day = t.tm_mday;
	            Hour = t.tm_hour;
	            Min = t.tm_min;
	            Sec = t.tm_sec;
	        } else {
	            Year = Mon = Day = Hour = Min = Sec = 0;
	        }

	        String s = String.format(
	                " %04d/%02d/%02d %02d:%02d:%02d %8ld ",
	                Year, Mon, Day, Hour, Min, Sec,
	                Files[Line].Size());


	        s += Files[Line].Name();
	        
	        s += (Files[Line].Type() == fiDIRECTORY)? SLASH : ' ';

	        if (Col < s.length())
	            MoveStr(B, 0, Width, s + Col, color, Width);
	    }
	}

	boolean IsHilited(int Line) {
	    return (Line >= 0 && Line < FCount) ? Files[Line].Type() == fiDIRECTORY : 0;
	}

	int  FileNameCmp(const void *a, const void *b) {
	    FileInfo *A = *(FileInfo **)a;
	    FileInfo *B = *(FileInfo **)b;

	    if (!(A.Type() == fiDIRECTORY) && (B.Type() == fiDIRECTORY))
	        return 1;

	    if ((A.Type() == fiDIRECTORY) && !(B.Type() == fiDIRECTORY))
	        return -1;

	    return filecmp(A.Name(), B.Name());
	}

	void RescanList() {
	    String Dir;
	    String Name;
	    int DirCount = 0;
	    int SizeCount = 0;
	    FileFind ff;
	    FileInfo fi;
	    int rc;

        FreeList();

	    Count = 0;
	    FCount = 0;
	    if (JustDirectory(Path, Dir) != 0) return;
	    JustFileName(Path, Name);

	    ff = new FileFind(Dir, "*", ffDIRECTORY | ffHIDDEN);
	    if (ff == 0)
	        return ;

	    rc = ff.FindFirst(fi);
	    while (rc == 0) {
	        assert(fi != 0);
	        if (strcmp(fi.Name(), ".") != 0) {
	            Files = (FileInfo **)realloc((void *)Files, ((FCount | 255) + 1) * sizeof(FileInfo *));
	            if (Files == 0)
	            {
	                delete fi;
	                delete ff;
	                return;
	            }

	            Files[FCount] = fi;

	            SizeCount += Files[FCount].Size();
	            if (fi.Type() == fiDIRECTORY && (strcmp(fi.Name(), "..") != 0))
	                DirCount++;
	            Count++;
	            FCount++;
	        } else
	            delete fi;
	        rc = ff.FindNext(&fi);
	    }
	    delete ff;

	    {
	        char CTitle[256];

	        sprintf(CTitle, "%d files%c%d dirs%c%d bytes%c%-200.200s",
	                FCount, ConGetDrawChar(DCH_V),
	                DirCount, ConGetDrawChar(DCH_V),
	                SizeCount, ConGetDrawChar(DCH_V),
	                Dir);
	        SetTitle(CTitle);
	    }
	    qsort(Files, FCount, sizeof(FileInfo *), FileNameCmp);
	    NeedsRedraw = 1;
	}

	void FreeList() {
	    Files = null;
	    FCount = 0;
	}

	int isDir(int No) {
	    String FilePath[];

	    JustDirectory(Path, FilePath);
	    Slash(FilePath, 1);
	    strcat(FilePath, Files[No].Name());
	    return IsDirectory(FilePath);
	}

	int ExecCommand(int Command, ExState State) {
	    switch (Command) {
	    case ExActivateInOtherWindow:
	        SearchLen = 0;
	        Msg(S_INFO, "");
	        if (Files && Row >= 0 && Row < FCount) {
	            if (isDir(Row)) {
	            } else {
	                return FmLoad(Files[Row].Name(), View.Next);
	            }
	        }
	        return ErFAIL;

	    case ExRescan:
	        if (RescanDir() == 0)
	            return ErFAIL;
	        return ErOK;

	    case ExDirGoUp:
	        SearchLen = 0;
	        Msg(S_INFO, "");
	        FmChDir(SDOT SDOT);
	        return ErOK;

	    case ExDirGoDown:
	        SearchLen = 0;
	        Msg(S_INFO, "");
	        if (Files && Row >= 0 && Row < FCount) {
	            if (isDir(Row)) {
	                FmChDir(Files[Row].Name());
	                return ErOK;
	            }
	        }
	        return ErFAIL;

	    case ExDirGoto:
	        SearchLen = 0;
	        Msg(S_INFO, "");
	        return ChangeDir(State);

	    case ExDirGoRoot:
	        SearchLen = 0;
	        Msg(S_INFO, "");
	        FmChDir(SSLASH);
	        return ErOK;

	    case ExDirSearchCancel:
	        // Kill search when moving
	        SearchLen = 0;
	        Msg(S_INFO, "");
	        return ErOK;

	    case ExDirSearchNext:
	        // Find next matching file, search is case in-sensitive while sorting is sensitive
	        if (SearchLen) {
	            for (int i = Row + 1; i < FCount; i++) {
	                if (strnicmp(SearchName, Files[i].Name(), SearchLen) == 0) {
	                    Row = i;
	                    break;
	                }
	            }
	        }
	        return ErOK;

	    case ExDirSearchPrev:
	        // Find prev matching file, search is case in-sensitive while sorting is sensitive
	        if (SearchLen) {
	            for (int i = Row - 1; i >= 0; i--) {
	                if (strnicmp(SearchName, Files[i].Name(), SearchLen) == 0) {
	                    Row = i;
	                    break;
	                }
	            }
	        }
	        return ErOK;

	    case ExDeleteFile:
	        SearchLen = 0;
	        Msg(S_INFO, "");
	        return FmRmDir(Files[Row].Name());
	    }
	    return EList::ExecCommand(Command, State);
	}

	int Activate(int No) {
	    SearchLen = 0;
	    Msg(S_INFO, "");
	    if (Files && No >= 0 && No < FCount) {
	        if (isDir(No)) {
	            FmChDir(Files[No].Name());
	            return 0;
	        } else {
	            return FmLoad(Files[No].Name(), View);
	        }
	    }
	    return 1;
	}

	void HandleEvent(TEvent Event) {
	    STARTFUNC("HandleEvent");
	    int resetSearch = 0;
	    super.HandleEvent(Event);
	    switch (Event.What) {
	    case evKeyUp:
	        resetSearch = 0;
	        break;
	    case evKeyDown:
	        LOG << "Key Code: " << kbCode(Event.Key.Code) << ENDLINE;
	        resetSearch = 1;
	        switch (kbCode(Event.Key.Code)) {
	        case kbBackSp:
	            LOG << "Got backspace" << ENDLINE;
	            resetSearch = 0;
	            if (SearchLen > 0) {
	                SearchName[--SearchLen] = 0;
	                Row = SearchPos[SearchLen];
	                Msg(S_INFO, "Search: [%s]", SearchName);
	            } else
	                Msg(S_INFO, "");
	            break;
	        case kbEsc:
	            Msg(S_INFO, "");
	            break;
	        default:
	            resetSearch = 0; // moved here - its better for user
	            // otherwice there is no way to find files like i_ascii
	            if (isAscii(Event.Key.Code) && (SearchLen < MAXISEARCH)) {
	                char Ch = (char) Event.Key.Code;
	                int Found;

	                LOG << " . " << BinChar(Ch) << ENDLINE;

	                SearchPos[SearchLen] = Row;
	                SearchName[SearchLen] = Ch;
	                SearchName[++SearchLen] = 0;
	                Found = 0;
	                LOG << "Comparing " << SearchName << ENDLINE;
	                for (int i = Row; i < FCount; i++) {
	                    LOG << "  to . " << Files[i].Name() << ENDLINE;
	                    if (strnicmp(SearchName, Files[i].Name(), SearchLen) == 0) {
	                        Row = i;
	                        Found = 1;
	                        break;
	                    }
	                }
	                if (Found == 0)
	                    SearchName[--SearchLen] = 0;
	                Msg(S_INFO, "Search: [%s]", SearchName);
	            }
	            break;
	        }
	    }
	    if (resetSearch) {
	        SearchLen = 0;
	    }
	    LOG << "SearchLen = " << SearchLen << ENDLINE;
	}

	int RescanDir() {
	    String CName = "";

	    if (Row >= 0 && Row < FCount)
	        strcpy(CName, Files[Row].Name());
	    Row = 0;
	    RescanList();
	    if (CName[0] != 0) {
	        for (int i = 0; i < FCount; i++) {
	            if (filecmp(Files[i].Name(), CName) == 0)
	            {
	                Row = i;
	                break;
	            }
	        }
	    }
	    return 1;
	}

	int FmChDir(const String Name) {
	    char Dir[256];
	    char CName[256] = "";

	    if (strcmp(Name, SSLASH) == 0) {
	        JustRoot(Path, Dir);
	    } else if (strcmp(Name, SDOT SDOT) == 0) {
	        Slash(Path, 0);
	        JustFileName(Path, CName);
	        JustDirectory(Path, Dir);
	    } else {
	        JustDirectory(Path, Dir);
	        Slash(Dir, 1);
	        strcat(Dir, Name);
	    }
	    Slash(Dir, 1);
	    free(Path);
	    Path = strdup(Dir);
	    Row = 0;
	    RescanList();
	    if (CName[0] != 0) {
	        for (int i = 0; i < FCount; i++) {
	            if (filecmp(Files[i].Name(), CName) == 0)
	            {
	                Row = i;
	                break;
	            }
	        }
	    }
	    UpdateTitle();
	    return 1;
	}

	int FmRmDir(String Name)
	{
		String FilePath = Path;
	    Slash(FilePath, 1);
	    FilePath += Name;

	    int choice =
	        View.MView.Win.Choice(GPC_CONFIRM,
	                                 "Remove File",
	                                 2, "O&K", "&Cancel",
	                                 "Remove %s?", Name);

	    if (choice == 0)
	    {
	        if (unlink(FilePath) == 0)
	        {
	            // put the cursor to the previous row
	            --Row;

	            // There has to be a more efficient way of doing this ...
	            return RescanDir();
	        }
	        else
	        {
	            Msg(S_INFO, "Failed to remove %s", Name);
	            return 0;
	        }
	    }
	    else
	    {
	        Msg(S_INFO, "Cancelled");
	        return 0;
	    }
	}

	int FmLoad(String Name, EView XView) {
		String FilePath;

	    JustDirectory(Path, FilePath);
	    Slash(FilePath, 1);
	    strcat(FilePath, Name);
	    return FileLoad(0, FilePath, NULL, XView);
	}

	String GetName() {
		String AName = Path;
	    Slash(AName, 0);
	    return AName;
	}

	void GetPath() {
		String APath = Path;
	    Slash(APath, 0);
	    return APath;
	}

	void GetInfo(String AInfo, int /*MaxLen*/) {
	    char buf[256] = {0};
	    char winTitle[256] = {0};

	    JustFileName(Path, buf);
	    if (buf[0] == '\0') // if there is no filename, try the directory name.
	        JustLastDirectory(Path, buf);

	    if (buf[0] != 0) // if there is a file/dir name, stick it in here.
	    {
	        strncat(winTitle, buf, sizeof(winTitle) - 1 - strlen(winTitle));
	        strncat(winTitle, "/ - ", sizeof(winTitle) - 1 - strlen(winTitle));
	    }
	    strncat(winTitle, Path, sizeof(winTitle) - 1 - strlen(winTitle));
	    winTitle[sizeof(winTitle) - 1] = 0;

	    sprintf(AInfo,
	            "%2d %04d/%03d %-150s",
	            ModelNo,
	            Row + 1, FCount,
	            winTitle);
	/*    sprintf(AInfo,
	            "%2d %04d/%03d %-150s",
	            ModelNo,
	            Row + 1, FCount,
	            Path);*/
	}

	void GetTitle(String ATitle, int MaxLen, String ASTitle, int /*SMaxLen*/) {

	    strncpy(ATitle, Path, MaxLen - 1);
	    ATitle[MaxLen - 1] = 0;

	    {
	        char P[MAXPATH];
	        strcpy(P, Path);
	        Slash(P, 0);

	        JustDirectory(P, ASTitle);
	        Slash(ASTitle, 1);
	    }
	}

	int ChangeDir(ExState State) {
	    char Dir[MAXPATH];
	    char Dir2[MAXPATH];

	    if (State.GetStrParam(View, Dir, sizeof(Dir)) == 0) {
	        strcpy(Dir, Path);
	        if (View.MView.Win.GetStr("Set directory", sizeof(Dir), Dir, HIST_PATH) == 0)
	            return 0;
	    }
	    if (ExpandPath(Dir, Dir2) == -1)
	        return 0;
	
	    // is this needed for other systems as well ?
	    //Slash(Dir2, 1);
	
	    Path = Dir2;
	    Row = -1;
	    UpdateTitle();
	    return RescanDir();
	}

	int GetContext() { return CONTEXT_DIRECTORY; }
	String FormatLine(int Line) { return null; };
	boolean CanActivate(int Line) { return true; }
	

}
