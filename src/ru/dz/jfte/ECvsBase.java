package ru.dz.jfte;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

public class ECvsBase extends EList 
{
	String Command = null;
	String Directory = null;
	String OnFiles = null;
	String OnFilesPos = null;

	//int Lines.size();
	//CvsLine [] Lines = null;
	List<CvsLine> Lines = new ArrayList<>();
	boolean Running = false;

	int BufLen = 0;
	int BufPos = 0;
	int PipeId = -1;
	int ReturnCode = -1;

	//String MsgBuf;



	static List<Pattern> CvsIgnoreRegexp = new ArrayList<>();

	static void AddCvsIgnoreRegexp(String regexp) 
	{ 
		CvsIgnoreRegexp.add(Pattern.compile(regexp));
	}


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
		//int i = 0;
		for(CvsLine l : Lines)
		{
			if (l.Buf != null && l.Line >= 0) {
				// Has buffer and line == bookmark . remove it
				l.Buf.RemoveBookmark(String.format("_CVS.%d",Lines.indexOf(l)) ); // TODO i = item pos in container - REDO with hash?
			}
			//i++;
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
		Lines.size()++;
		Lines=(CvsLine **)realloc (Lines,sizeof (CvsLine *)*Lines.size());
		Lines[Lines.size()-1]=l;
		FindBuffer (Lines.size()-1);
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
			l.Buf = null;

			String path = Directory;

			path =  Console.Slash(path,1);
			path += l.File;

			EBuffer B = Console.FindFile(path);
			if( B != null && B.Loaded ) 
				AssignBuffer( B, line );
		}
	}

	void AssignBuffer (EBuffer B, int line) 
	{
		CvsLine l = Lines.get(line);
		l.Buf=B;

		if (l.Line >= 0) 
		{
			String book = String.format("_CVS.%d",line);
			EPoint P = new EPoint();
			P.Col=0;
			P.Row=l.Line;
			if (P.Row>=B.RCount)
				P.Row=B.RCount-1;
			B.PlaceBookmark (book,P);
		}
	}

	void  FindFileLines (EBuffer B) 
	{
		String path = Console.Slash (Directory,1);

		for(CvsLine l : Lines)
		{
			if(l.Buf==null && l.File != null) 
			{
				String fn = path + l.File;
				if (Console.filecmp (B.FileName,fn)==0) {
					AssignBuffer (B,Lines.indexOf(l)); 
				}
			}
		}
	}

	void NotifyDelete (EModel Deleting) 
	{
		for(CvsLine l : Lines)
		{
			if (l.Buf==Deleting) 
				l.Buf=null;
		}		
	}

	/*
	//@Override
	int GetLine(String [] Line) 
	{
		int rc;
		String p;
		int l;
		int max = 500;

		//fprintf(stderr, "GetLine: %d\n", Running);

		Line[0] = null;
		if (Running && PipeId != -1) {
			rc = GPipe.ReadPipe(PipeId, MsgBuf + BufLen, sizeof(MsgBuf) - BufLen);
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
			return null;
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
	*/
	
	void ParseLine (String line,int v) {
		AddLine (null,-1,line,0);
	}

	void NotifyPipe (int APipeId) 
	{
		if (APipeId!=PipeId)
			return;

		//char line[1024];
		//RxMatchRes RM;
		//int i;
		//String [] line = {""};

		while(true) // (GetLine(line)) 
		{
			String line = GPipe.ReadPipe(PipeId);
			if(line == null)
				break;
			/*
			int len = line.length();
			//if (len>0&&line[len-1]=='\n') line[--len]=0;
			
			for( i=0; i < CvsIgnoreRegexpCount; i++ )
				if (RxExec (CvsIgnoreRegexp[i],line,len,line,&RM)==1) 
					break;
			
			if (i==CvsIgnoreRegexpCount) ParseLine (line,len);
			*/
			if(!matchIgnoreRegexp(line))
				ParseLine (line,0);
		}

		if (!Running) {
			AddLine (null, -1, String.format("[done, status=%d]",ReturnCode), 0);
		}

	}

	private boolean matchIgnoreRegexp(String line) {
		for( Pattern p : CvsIgnoreRegexp )
			if( p.matcher(line).matches() ) return true;

		return false;
	}

	int RunPipe (String ADir,String ACommand,String AOnFiles) {

		Command=ACommand;
		Directory=ADir;
		OnFiles=AOnFiles;

		ReturnCode=-1;
		Row=Lines.size()-1;
		OnFilesPos=OnFiles;

		AddLine (null,-1, String.format("[running cvs in '%s']",Directory), 0);

		Console.ChangeDir (Directory);
		return ContinuePipe();
	}

	int ContinuePipe () {
		//char RealCommand[2048];
		int space;

		if (OnFilesPos == null) {
			// At the end of all files, terminate
			ClosePipe ();
			return 0;
		} else if (Running) {
			// Already running, close the pipe and continue
			ReturnCode=GPipe.ClosePipe(PipeId);
		} else {
			// Not running . set to Running mode
			Running=true;
		}

		// Make real command with some files from OnFiles, update OnFilesPos
		String RealCommand = Command+" ";
		/*
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
		} */
		RealCommand  += OnFilesPos;
		OnFilesPos = null;

		BufLen=BufPos=0;

		{
			//char s[sizeof (RealCommand)+32];

			AddLine(null,-1,String.format("[continuing: '%s']",RealCommand), 0);
		}

		PipeId= GPipe.OpenPipe(RealCommand,".",this);
		return 0;
	}

	void ClosePipe () {
		ReturnCode = GPipe.ClosePipe(PipeId);
		PipeId = -1;
		Running = false;
	}

	@Override
	void DrawLine (PCell B, int Line,int Col, int color, int Width) 
	{
		if(Line >= Lines.size())
			return;

		CvsLine l = Lines.get(Line);

		if( Col < l.Msg.length() ) 
		{
			//char str[1024];
			//int len = UnTabStr(str,sizeof (str),Lines.get(Line).Msg,strlen (Lines.get(Line).Msg));
			String str = PCell.UnTabStr(Lines.get(Line).Msg);
			int len = str.length();

			if(len>Col) 
				B.MoveStr(0,Width,str+Col,color,Width);
		}
	}

	@Override
	String FormatLine (int Line) {
		if( Line < Lines.size()) 
			return Lines.get(Line).Msg;
		else return null;
	}

	@Override
	void UpdateList () {
		if (Lines.size()<=Row||Row>=Count-1) 
			Row=Lines.size()-1;

		Count=Lines.size();

		super.UpdateList();
	}

	@Override
	int Activate (int No) {
		ShowLine (View,No);
		return 1;
	}

	@Override
	boolean CanActivate(int Line) {
		return Line < Lines.size() && Lines.get(Line).File != null;
	}

	@Override
	boolean IsHilited (int Line) {
		return Line < Lines.size() && 0 != (Lines.get(Line).Status&1);
	}

	@Override
	boolean  IsMarked (int Line) {
		return Line < Lines.size() && 0 != (Lines.get(Line).Status&2);
	}

	@Override
	int Mark (int Line) {
		if (Line<Lines.size()) {
			if(0 != (Lines.get(Line).Status&4)) 
				Lines.get(Line).Status|=2;
			return 1;
		} else return 0;
	}

	@Override
	int Unmark (int Line) {
		if (Line<Lines.size()) {
			if(0 != (Lines.get(Line).Status & 4)) 
				Lines.get(Line).Status &= ~2;
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
			if (!Running || PipeId == -1)
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

	//@Override
	private void ShowLine(EView V, int line) 
	{
		if(line >= 0 && line < Lines.size() && Lines.get(line).File != null) 
		{
			if (Lines.get(line).Buf!=null) {
				V.SwitchToModel (Lines.get(line).Buf);
				if (Lines.get(line).Line!=-1) {
					//char book[16];
					String book = String.format("_CVS.%d",line);
					Lines.get(line).Buf.GotoBookmark (book);
				}
			} else {
				//char path[MAXPATH];
				String path = Console.Slash(Directory,1);
				path += Lines.get(line).File;

				if (Console.FileLoad(0,path,null,V)) 
				{
					V.SwitchToModel (ActiveModel);
					if (Lines.get(line).Line!=-1) ((EBuffer)ActiveModel[0]).CenterNearPosR (0,Lines.get(line).Line);
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

		//String s = String.format("%2d %04d/%03d %s (%%.%is) ",ModelNo,Row,Count,Title,MaxLen-24-Title.length());
		//return String.format(s,Command);

		return String.format("%2d %04d/%03d %s (%s) ",ModelNo,Row,Count,Title,Command);
	}

	// Used to get default directory of model
	@Override
	String GetPath() {
		/*
		strncpy (APath,Directory,MaxLen);
		APath[MaxLen-1]=0;
		Slash (APath,0);
		*/
		return Console.Slash(Directory, 0);
	}

	// Normal and short title (normal for window, short for icon in X)
	@Override
	void GetTitle(String [] ATitle, String [] ASTitle ) {
		//char format[128];

		//String format = String.format("%s: %%.%is",Title,MaxLen-4-Title.length() );
		//ATitle[0] = String.format(format,Command);
		
		ATitle[0] = String.format("%s: %s", Title, Command );
		
		ASTitle[0] = Title;
	}

}
