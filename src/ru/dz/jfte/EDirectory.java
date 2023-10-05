package ru.dz.jfte;

import java.util.Arrays;
import java.util.Date;

public class EDirectory extends EList implements EventDefs, KeyDefs, GuiDefs 
{

	String Path;
	FileInfo [] Files = null;
	int FCount = 0;
	int SearchLen = 0;
	String SearchName = null;
	int SearchPos[];



	/*
	static EDirectory newEDirectory(int createFlags, EModel ARoot, String aPath) 
	{
		EModel []ARootP = {ARoot};
		return new EDirectory( createFlags, ARootP, aPath);
	} */

	EDirectory(int createFlags, EModel []ARoot, String aPath) 
	{
		super(createFlags, ARoot, aPath);

		String [] XPath = {""};

		//FCount = 0;
		//SearchLen = 0;
		Console.ExpandPath(aPath, XPath);
		Path = Console.Slash(XPath[0], 1);
		RescanList();
	}


    @Override
	EEventMap GetEventMap() {
		return EEventMap.FindEventMap("DIRECTORY");
	}

    @Override
	void DrawLine(PCell B, int Line, int Col, int /*ChColor*/ color, int Width) {
		//char s[1024];

		B.MoveCh( ' ', color, Width);
		if (Files!=null && Line >= 0 && Line < FCount) 
		{
			/*
			int Year, Mon, Day, Hour, Min, Sec;
			tm t;
			time_t tim;

			tim = Files[Line].MTime();
			t = localtime(tim);

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
			*/
	        Date modifiedDate = new Date(Files[Line].MTime());
	        String s = modifiedDate.toString(); // + " " + Files[Line].Size()+ " ";

			String fn = Files[Line].name;
			fn += Files[Line].isDir() ? '/' : ' ';

			s = String.format("%s %9d  %-40s", s, Files[Line].Size(), fn);
			
			if (Col < s.length())
				B.MoveStr( 0, Width, s.substring(Col), color, Width);
				//TO DO s + Col : B.MoveStr( 0, Width, s + Col, color, Width);
		}
	}

    @Override
	boolean IsHilited(int Line) {
		return (Line >= 0 && Line < FCount) ? Files[Line].isDir() : false;
	}

	/*
	int  FileNameCmp(const void *a, const void *b) {
	    FileInfo *A = *(FileInfo **)a;
	    FileInfo *B = *(FileInfo **)b;

	    if (!(A.Type() == fiDIRECTORY) && (B.Type() == fiDIRECTORY))
	        return 1;

	    if ((A.Type() == fiDIRECTORY) && !(B.Type() == fiDIRECTORY))
	        return -1;

	    return filecmp(A.Name(), B.Name());
	}*/

    @Override
	void RescanList() {
		String [] Dir = {""};
		//String [] Name = {""};
		int DirCount = 0;
		int SizeCount = 0;
		FileFind ff;
		FileInfo fi;
		//int rc;

		FreeList();

		Count = 0;
		FCount = 0;
		
		Dir[0] = Console.directory(Path);
		if( null == Dir[0]) return;
		
		//if (Console.JustDirectory(Path, Dir) != 0) return;
		//Console.JustFileName(Path, Name);

		ff = new FileFind(Dir[0], "*", FileFind.ffDIRECTORY | FileFind.ffHIDDEN);

		while ((fi = ff.FindNext()) != null) {
			assert(fi != null);
			if (!fi.Name().equals(".")) {
				//Files = (FileInfo **)realloc((void *)Files, ((FCount | 255) + 1) * sizeof(FileInfo *));
				if(null == Files)
					Files = new FileInfo[(FCount | 255) + 1];
				else
					Files = Arrays.copyOf(Files, (FCount | 255) + 1);

				Files[FCount] = fi;

				SizeCount += Files[FCount].Size();
				if (fi.isDir() && !fi.Name().equals(".."))
					DirCount++;
				Count++;
				FCount++;
			}
		}
		//delete ff;

		{

			String CTitle = String.format("%d files%c%d dirs%c%d bytes%c%-200.200s",
					FCount, Console.ConGetDrawChar(DCH_V),
					DirCount, Console.ConGetDrawChar(DCH_V),
					SizeCount, Console.ConGetDrawChar(DCH_V),
					Dir[0]);
			SetTitle(CTitle);
		}
		// TODO qsort(Files, FCount, sizeof(FileInfo *), FileNameCmp);
		NeedsRedraw = 1;
	}

    @Override
	void FreeList() {
		Files = null;
		FCount = 0;
	}

	boolean isDir(int No) {
		String FilePath[] = {""};

		//Console.JustDirectory(Path, FilePath);
		FilePath[0] = Console.directory(Path); 
		FilePath[0] = Console.Slash(FilePath[0], 1);
		FilePath[0] += Files[No].name;
		return Console.IsDirectory(FilePath[0]);
	}

    @Override
	ExResult ExecCommand(ExCommands Command, ExState State) {
		switch (Command) {
		case ExActivateInOtherWindow:
			SearchLen = 0;
			Msg(S_INFO, "");
			if (Files != null && Row >= 0 && Row < FCount) {
				if (isDir(Row)) {
				} else {
					return FmLoad(Files[Row].name, View.Next);
				}
			}
			return ExResult.ErFAIL;

		case ExRescan:
			if (RescanDir() == ExResult.ErFAIL)
				return ExResult.ErFAIL;
			return ExResult.ErOK;

		case ExDirGoUp:
			SearchLen = 0;
			Msg(S_INFO, "");
			FmChDir("..");
			return ExResult.ErOK;

		case ExDirGoDown:
			SearchLen = 0;
			Msg(S_INFO, "");
			if (Files!=null && Row >= 0 && Row < FCount) {
				if (isDir(Row)) {
					FmChDir(Files[Row].Name());
					return ExResult.ErOK;
				}
			}
			return ExResult.ErFAIL;

		case ExDirGoto:
			SearchLen = 0;
			Msg(S_INFO, "");
			return ChangeDir(State);

		case ExDirGoRoot:
			SearchLen = 0;
			Msg(S_INFO, "");
			//FmChDir(SSLASH);
			FmChDir("/");
			return ExResult.ErOK;

		case ExDirSearchCancel:
			// Kill search when moving
			SearchLen = 0;
			Msg(S_INFO, "");
			return ExResult.ErOK;

		case ExDirSearchNext:
			// Find next matching file, search is case in-sensitive while sorting is sensitive
			if (SearchLen!=0) {
				for (int i = Row + 1; i < FCount; i++) {
					if (SearchName.equalsIgnoreCase(Files[i].Name())) {
						Row = i;
						break;
					}
				}
			}
			return ExResult.ErOK;

		case ExDirSearchPrev:
			// Find prev matching file, search is case in-sensitive while sorting is sensitive
			if (SearchLen!=0) {
				for (int i = Row - 1; i >= 0; i--) 
				{
					if (SearchName.equalsIgnoreCase(Files[i].Name())) {
						Row = i;
						break;
					}
				}
			}
			return ExResult.ErOK;

		case ExDeleteFile:
			SearchLen = 0;
			Msg(S_INFO, "");
			return FmRmDir(Files[Row].Name());
		}
		return super.ExecCommand(Command, State);
	}

    @Override
	int Activate(int No) {
		SearchLen = 0;
		Msg(S_INFO, "");
		if (Files!=null && No >= 0 && No < FCount) {
			if (isDir(No)) {
				FmChDir(Files[No].Name());
				return 0;
			} else {
				return FmLoad(Files[No].Name(), View) == ExResult.ErOK ? 1 : 0;
			}
		}
		return 1;
	}

    @Override
	void HandleEvent(TEvent Event) {
		//STARTFUNC("HandleEvent");
		int resetSearch = 0;
		super.HandleEvent(Event);
		switch (Event.What) {
		case evKeyUp:
			resetSearch = 0;
			break;
		case evKeyDown:
			//LOG << "Key Code: " << kbCode(Event.Key.Code) << ENDLINE;
			resetSearch = 1;
			TKeyEvent ke = (TKeyEvent) Event;
			switch (KeyDefs.kbCode(ke.Code)) {
			case kbBackSp:
				//LOG << "Got backspace" << ENDLINE;
				resetSearch = 0;
				if (SearchLen > 0) {
					//SearchName[--SearchLen] = 0;
					SearchName = SearchName.substring(0,--SearchLen);
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
				if (KeyDefs.isAscii(ke.Code) && (SearchLen < BufferView.MAXISEARCH)) {
					char Ch = (char) ke.Code;
					int Found;

					//LOG << " . " << BinChar(Ch) << ENDLINE;

					SearchPos[SearchLen] = Row;
					SearchName += Ch;
					//SearchName[++SearchLen] = 0;
					Found = 0;
					//LOG << "Comparing " << SearchName << ENDLINE;
					for (int i = Row; i < FCount; i++) {
						//LOG << "  to . " << Files[i].Name() << ENDLINE;
						if (SearchName.equalsIgnoreCase(Files[i].Name())) {
							Row = i;
							Found = 1;
							break;
						}
					}
					if (Found == 0)
						SearchName = SearchName.substring(0,--SearchLen);
					Msg(S_INFO, "Search: [%s]", SearchName);
				}
				break;
			}
		}
		if (resetSearch!=0) {
			SearchLen = 0;
		}
		//LOG << "SearchLen = " << SearchLen << ENDLINE;
	}

	ExResult RescanDir() {
		String CName = "";

		if (Row >= 0 && Row < FCount)
			CName = Files[Row].Name();
		Row = 0;
		RescanList();
		if (!CName.isBlank()) {
			for (int i = 0; i < FCount; i++) {
				if (Console.filecmp(Files[i].Name(), CName) == 0)
				{
					Row = i;
					break;
				}
			}
		}
		return ExResult.ErOK;
	}

	int FmChDir(String Name) {
		String [] Dir = {""};
		String [] CName = {""};

		//if (strcmp(Name, SSLASH) == 0) {
		if (Name.equals("/")) {
			Console.JustRoot(Path, Dir);
		} else if (Name.equals("..")) {
			Path = Console.Slash(Path, 0);
			Console.JustFileName(Path, CName);
			//Console.JustDirectory(Path, Dir);
			Dir[0] = Console.parent(Path);
		} else {
			//Console.JustDirectory(Path, Dir);
			Dir[0] = Console.directory(Path);
			Dir[0] = Console.Slash(Dir[0], 1);
			Dir[0] += Name;
		}
		Dir[0] = Console.Slash(Dir[0], 1);

		Path = Dir[0];
		Row = 0;
		RescanList();
		if (!CName[0].isBlank()) {
			for (int i = 0; i < FCount; i++) {
				if (Console.filecmp(Files[i].Name(), CName[0]) == 0)
				{
					Row = i;
					break;
				}
			}
		}
		UpdateTitle();
		return 1;
	}

	ExResult FmRmDir(String Name)
	{
		String FilePath = Console.Slash(Path, 1);
		FilePath += Name;

		int choice =
				View.MView.Win.Choice(GPC_CONFIRM,
						"Remove File",
						2, "O&K", "&Cancel",
						"Remove %s?", Name);

		if (choice == 0)
		{
			if (Console.unlink(FilePath) == 0)
			{
				// put the cursor to the previous row
				--Row;

				// There has to be a more efficient way of doing this ...
				return RescanDir();
			}
			else
			{
				Msg(S_INFO, "Failed to remove %s", Name);
				return ExResult.ErFAIL;
			}
		}
		else
		{
			Msg(S_INFO, "Cancelled");
			return ExResult.ErFAIL;
		}
	}

	ExResult FmLoad(String Name, EView XView) {
		String [] FilePath = {""};

		//Console.JustDirectory(Path, FilePath);
		FilePath[0] = Console.directory(Path);
		FilePath[0] = Console.Slash(FilePath[0], 1);
		FilePath[0] += Name;
		return Console.FileLoad(0, FilePath[0], null, XView) ? ExResult.ErOK : ExResult.ErFAIL;
	}

    @Override
	String GetName() {
		//String AName = Path;
		//Slash(AName, 0);
		return Console.Slash(Path, 0);
	}

    @Override
	String GetPath() {
		//String APath = Path;
		//Slash(APath, 0);
		return Console.Slash(Path, 0);
	}

	void GetInfo(String AInfo, int MaxLen) {
		String buf[] = {""};
		String winTitle[] = {""};

		Console.JustFileName(Path, buf);
		if (buf[0].isBlank()) // if there is no filename, try the directory name.
			Console.JustLastDirectory(Path, buf);

		if (!buf[0].isBlank()) // if there is a file/dir name, stick it in here.
		{
			winTitle[0] += buf;
			winTitle[0] += "/ - ";
		}
		winTitle[0] += Path;

		AInfo = String.format(
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

    @Override
	void GetTitle(String [] ATitle, String [] ASTitle) {

		ATitle[0] = Path;


		String sp = Console.Slash(Path, 0);

		//Console.JustDirectory(sp, ASTitle);
		ASTitle[0] = Console.directory(sp);
		ASTitle[0] = Console.Slash(ASTitle[0], 1);

	}

	ExResult ChangeDir(ExState State) {
		String Dir[] = {null};
		String Dir2[] = {null};

		if (State.GetStrParam(View, Dir) == 0) {
			Dir[0] = Path;
			if (View.MView.Win.GetStr("Set directory", Dir, HIST_PATH) == 0)
				return ExResult.ErFAIL;
		}
		if (Console.ExpandPath(Dir[0].strip(), Dir2) == -1)
			return ExResult.ErFAIL;

		// is this needed for other systems as well ?
		//Slash(Dir2, 1);

		Path = Dir2[0];
		Row = -1;
		UpdateTitle();
		return RescanDir();
	}

    @Override
	int GetContext() { return CONTEXT_DIRECTORY; }

	@Override
	String FormatLine(int Line) { return null; };
	
    @Override
	boolean CanActivate(int Line) { return true; }

    @Override
    String GetInfo() {
    	    String buf[] = {""};
    	    String winTitle = "";

    	    Console.JustFileName(Path, buf);
    	    if (buf[0] == null || buf[0].isBlank()) // if there is no filename, try the directory name.
    	    	Console.JustLastDirectory(Path, buf);

    	    if (buf[0] != null) // if there is a file/dir name, stick it in here.
    	        winTitle += buf[0] + "/ - ";

    	    winTitle += Path;

    	    return String.format(
    	            "%2d %04d/%03d %-150s",
    	            ModelNo,
    	            Row + 1, FCount,
    	            winTitle);
    	
    }

}
