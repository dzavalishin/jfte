package ru.dz.jfte;

import java.util.List;

public class ECvsBase extends EList 
{
	String Command = null;
	String Directory = null;
	String OnFiles = null;
	String OnFilesPos = null;

	//int LineCount;
	//CvsLine [] Lines = null;
	List<CvsLine> Lines = new ArrayList<>();
	int Running = 0;

	int BufLen = 0;
	int BufPos = 0;
	int PipeId = -1;
	int ReturnCode = -1;

	//String MsgBuf;






	ECvsBase(int createFlags, EModel [] ARoot, String title)
	{
		super(createFlags, ARoot, title);
	}

	@Override
	public void close() {
		GPipe.ClosePipe(PipeId);
		FreeLines ();
	}

	void FreeLines () 
	{
		for(CvsLine l : Lines)
		{
			if (l.Buf != null && l.Line >= 0) {
				// Has buffer and line == bookmark . remove it
				l.Buf.RemoveBookmark(String.format("_CVS.%d",i) ); // TODO i = item pos in container - REDO with hash?
			}
		}

		Lines.clear();;
		BufLen=BufPos=0;
	}

	void AddLine(String file, int line, String msg, int status) 
	{
		CvsLine l = new CvsLine();

		l.File=file;
		l.Line=line;
		l.Msg=msg;
		l.Buf=null;
		l.Status=status;

		/*
		LineCount++;
		Lines=(CvsLine **)realloc (Lines,sizeof (CvsLine *)*LineCount);
		Lines[LineCount-1]=l;
		FindBuffer (LineCount-1);
		 */

		Lines.add(l);
		FindBuffer (Lines.size()-1);

		UpdateList ();
	}

	void FindBuffer (int line) 
	{
		CvsLine l = Lines.get(line);

		if (l.File != null) 
		{
			l.Buf=0;

			String path = Directory;

			path =  Console.Slash(path,1);
			path += l.File;

			EBuffer B = Console.FindFile(path);
			if( B != null && B.Loaded ) 
				AssignBuffer( B, line );
		}
	}

	void AssignBuffer (EBuffer B,int line) 
	{
		//char book[16];
		EPoint P;

		Lines[line].Buf=B;
		if (Lines[line].Line>=0) {
			sprintf (book,"_CVS.%d",line);
			P.Col=0;
			P.Row=Lines[line].Line;
			if (P.Row>=B.RCount)
				P.Row=B.RCount-1;
			B.PlaceBookmark (book,P);
		}
	}

	void  FindFileLines (EBuffer B) {
		//char path[MAXPATH];
		String pos;
		strcpy (path,Directory);Slash (path,1);pos=path+strlen (path);
		for (int i=0;i<LineCount;i++)
			if (Lines[i].Buf==0&&Lines[i].File!=0) {
				strcpy (pos,Lines[i].File);
				if (filecmp (B.FileName,path)==0) {
					AssignBuffer (B,i);
				}
			}
	}

	void NotifyDelete (EModel Deleting) {
		for (int i=0;i<LineCount;i++) {
			if (Lines[i].Buf==Deleting) {
				Lines[i].Buf=0;
			}
		}
	}

	@Override
	int GetLine(String Line, int max) {
		int rc;
		String p;
		int l;

		//fprintf(stderr, "GetLine: %d\n", Running);

		*Line = 0;
		if (Running && PipeId != -1) {
			rc = gui.ReadPipe(PipeId, MsgBuf + BufLen, sizeof(MsgBuf) - BufLen);
			//fprintf(stderr, "GetLine: ReadPipe rc = %d\n", rc);
			if (rc == -1) {
				ContinuePipe ();
			}
			if (rc > 0)
				BufLen += rc;
		}
		l = max - 1;
		if (BufLen - BufPos < l)
			l = BufLen - BufPos;
		//fprintf(stderr, "GetLine: Data %d\n", l);
		p = (String )memchr(MsgBuf + BufPos, '\n', l);
		if (p) {
			*p = 0;
			strcpy(Line, MsgBuf + BufPos);
			l = strlen(Line);
			if (l > 0 && Line[l - 1] == '\r')
				Line[l - 1] = 0;
			BufPos = p + 1 - MsgBuf;
			//fprintf(stderr, "GetLine: Line %d\n", strlen(Line));
		} else if (Running && sizeof(MsgBuf) != BufLen) {
			memmove(MsgBuf, MsgBuf + BufPos, BufLen - BufPos);
			BufLen -= BufPos;
			BufPos = 0;
			//fprintf(stderr, "GetLine: Line Incomplete\n");
			return 0;
		} else {
			if (l == 0)
				return 0;
			memcpy(Line, MsgBuf + BufPos, l);
			Line[l] = 0;
			if (l > 0 && Line[l - 1] == '\r')
				Line[l - 1] = 0;
			BufPos += l;
			//fprintf(stderr, "GetLine: Line Last %d\n", l);
		}
		memmove(MsgBuf, MsgBuf + BufPos, BufLen - BufPos);
		BufLen -= BufPos;
		BufPos = 0;
		//fprintf(stderr, "GetLine: Got Line\n");
		return 1;
	}

	void ParseLine (String line,int v) {
		AddLine (0,-1,line);
	}

	void NotifyPipe (int APipeId) {
		if (APipeId==PipeId) {
			char line[1024];
			RxMatchRes RM;
			int i;

			while (GetLine((String )line, sizeof(line))) {
				int len=strlen (line);
				if (len>0&&line[len-1]=='\n') line[--len]=0;
				for (i=0;i<CvsIgnoreRegexpCount;i++)
					if (RxExec (CvsIgnoreRegexp[i],line,len,line,&RM)==1) break;
				if (i==CvsIgnoreRegexpCount) ParseLine (line,len);
			}
			if (!Running) {
				char s[30];

				sprintf (s,"[done, status=%d]",ReturnCode);
				AddLine (0,-1,s);
			}
		}
	}

	int RunPipe (String ADir,String ACommand,String AOnFiles) {

		Command=ACommand;
		Directory=ADir;
		OnFiles=AOnFiles;

		ReturnCode=-1;
		Row=LineCount-1;
		OnFilesPos=OnFiles;

		{
			//char s[2*MAXPATH*4];

			sprintf (s,"[running cvs in '%s']",Directory);
			AddLine (0,-1,s);
		}

		ChangeDir (Directory);
		return ContinuePipe ();
	}

	int ContinuePipe () {
		//char RealCommand[2048];
		int space;

		if (!OnFilesPos) {
			// At the end of all files, terminate
			ClosePipe ();
			return 0;
		} else if (Running) {
			// Already running, close the pipe and continue
			ReturnCode=GPipe.ClosePipe (PipeId);
		} else {
			// Not running . set to Running mode
			Running=1;
		}

		// Make real command with some files from OnFiles, update OnFilesPos
		strcat (strcpy (RealCommand,Command)," ");
		space=sizeof (RealCommand)-strlen (RealCommand)-1;
		if (space>=strlen (OnFilesPos)) {
			strcat (RealCommand,OnFilesPos);
			OnFilesPos=NULL;
		} else {
			char c=OnFilesPos[space];
			OnFilesPos[space]=0;
			String s=strrchr (OnFilesPos,' ');
			OnFilesPos[space]=c;
			if (!s) {
				ClosePipe ();
				return 0;
			}
			*s=0;
			strcat (RealCommand,OnFilesPos);
			OnFilesPos=s+1;
			while (*OnFilesPos==' ') OnFilesPos++;
			if (!*OnFilesPos) OnFilesPos=NULL;
		}

		BufLen=BufPos=0;

		{
			//char s[sizeof (RealCommand)+32];

			sprintf (s,"[continuing: '%s']",RealCommand);
			AddLine (0,-1,s);
		}

		PipeId=gui.OpenPipe (RealCommand,this);
		return 0;
	}

	void ClosePipe () {
		ReturnCode = GPipe.ClosePipe(PipeId);
		PipeId = -1;
		Running = 0;
	}

	@Override
	void DrawLine (PCell B, int Line,int Col, int color, int Width) 
	{
		if(Line >= Lines.size())
			return;

		CvsLine l = Lines.get(Line);

		if (Col<(int)strlen (l.Msg)) {
			//char str[1024];
			int len;

			len=UnTabStr (str,sizeof (str),Lines[Line].Msg,strlen (Lines[Line].Msg));
			
			if (len>Col) 
				B.MoveStr(0,Width,str+Col,color,Width);
		}
	}

	@Override
	String FormatLine (int Line) {
		if (Line<LineCount) 
			return Lines[Line].Msg;
		else return null;
	}

	@Override
	void UpdateList () {
		if (LineCount<=Row||Row>=Count-1) 
			Row=LineCount-1;

		Count=LineCount;

		super.UpdateList();
	}

	@Override
	int Activate (int No) {
		ShowLine (View,No);
		return 1;
	}

	@Override
	int CanActivate(int Line) {
		return Line<LineCount&&Lines[Line].File;
	}

	@Override
	boolean IsHilited (int Line) {
		return Line < LineCount && 0 != (Lines[Line].Status&1);
	}

	@Override
	boolean  IsMarked (int Line) {
		return Line < LineCount && 0 != (Lines[Line].Status&2);
	}

	@Override
	int Mark (int Line) {
		if (Line<LineCount) {
			if(0 != (Lines[Line].Status&4)) 
				Lines[Line].Status|=2;
			return 1;
		} else return 0;
	}

	@Override
	int Unmark (int Line) {
		if (Line<LineCount) {
			if(0 != (Lines[Line].Status & 4)) 
				Lines[Line].Status &= ~2;
			return 1;
		} 
		else 
			return 0;
	}

	@Override
	//int ExecCommand(int Command, ExState State) {
	ExResult ExecCommand(ExCommands Command, ExState State) {
		switch (Command) {
		case ExChildClose:
			if (Running == 0 || PipeId == -1)
				break;
			ClosePipe ();
			AddLine(null, -1, String.format("[aborted, status=%d]", ReturnCode), 0 );
			return ExResult.ErOK;

		case ExActivateInOtherWindow:
			ShowLine(View.Next, Row);
			return ExResult.ErOK;
		}
		return super.ExecCommand(Command, State);
	}

	@Override
	void ShowLine (EView V, int line) 
	{
		if (line>=0&&line<LineCount&&Lines[line].File) {
			if (Lines[line].Buf!=0) {
				V.SwitchToModel (Lines[line].Buf);
				if (Lines[line].Line!=-1) {
					//char book[16];
					sprintf(book,"_CVS.%d",line);
					Lines[line].Buf.GotoBookmark (book);
				}
			} else {
				//char path[MAXPATH];
				String path = Directory;
				path = Slash(path,1);
				path += Lines[line].File;

				if (FileLoad (0,path,0,V)==1) {
					V.SwitchToModel (ActiveModel);
					if (Lines[line].Line!=-1) ((EBuffer)ActiveModel).CenterNearPosR (0,Lines[line].Line);
				}
			}
		}
	}

	// Event map - this name is used in config files when defining eventmap
	@Override
	EEventMap GetEventMap () {
		return EEventMap.FindEventMap("CVSBASE");
	}

	// Shown in "Closing xxx..." message when closing model
	@Override
	String GetName() {
		return Title;
	}

	// Shown in buffer list
	@Override
	String GetInfo() {
		//char format[128];

		sprintf (format,"%2d %04d/%03d %s (%%.%is) ",ModelNo,Row,Count,Title,MaxLen-24-strlen (Title));
		sprintf (AInfo,format,Command);
	}

	// Used to get default directory of model
	@Override
	String GetPath() {
		strncpy (APath,Directory,MaxLen);
		APath[MaxLen-1]=0;
		Slash (APath,0);
	}

	// Normal and short title (normal for window, short for icon in X)
	@Override
	void GetTitle(String [] ATitle, String [] ASTitle ) {
		//char format[128];

		sprintf (format,"%s: %%.%is",Title,MaxLen-4-strlen (Title));
		sprintf (ATitle,format,Command);
		strncpy (ASTitle,Title,SMaxLen);
		ASTitle[SMaxLen-1]=0;
	}

}
