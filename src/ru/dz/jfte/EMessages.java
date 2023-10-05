package ru.dz.jfte;

import java.io.Closeable;
import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.regex.Matcher;

import ru.dz.jfte.c.BitOps;

public class EMessages extends EList implements Closeable 
{
	String Command = null;
	String Directory = null;

	int ErrCount = 0;
	Error [] ErrList = null;
	boolean Running = true;

	int BufLen = 0;
	int BufPos = 0;
	int PipeId;
	int ReturnCode = -1;
	int MatchCount = 0;
	//String MsgBuf;
	aDir   curr_dir = null;                       // top of dir stack.

	static EMessages CompilerMsgs = null;

	/*static EMessages newEMessages(int createFlags, EModel ARoot, String ADir, String ACommand) 
	{
		EModel []ARootP = {ARoot};
		return new EMessages(createFlags, ARootP,  ADir,  ACommand);
	}*/

	EMessages(int createFlags, EModel []ARoot, String ADir, String ACommand) 
	{
		super(createFlags, ARoot, "Messages");

		CompilerMsgs = this;
		RunPipe(ADir, ACommand);
	}

	@Override
	public void close() {
		GPipe.ClosePipe(PipeId);
		FreeErrors();
		CompilerMsgs = null;
	}

	@Override
	int GetContext() { return CONTEXT_MESSAGES; }


	@Override
	void NotifyDelete(EModel Deleting) {
		for (int i = 0; i < ErrCount; i++) {
			if (ErrList[i].Buf == Deleting) {
				/* NOT NEEDED!
                 char bk[16];
                 sprintf(bk, "_MSG.%d", i);
                 ((EBuffer *)Deleting).RemoveBookmark(bk);
				 */
				ErrList[i].Buf = null;
			}
		}
	}

	void FindErrorFiles() {
		for (int i = 0; i < ErrCount; i++)
			if (ErrList[i].Buf == null && ErrList[i].file != null)
				FindErrorFile(i);
	}

	void FindErrorFile(int err) {
		assert(err >= 0 && err < ErrCount);
		if (ErrList[err].file == null)
			return ;

		EBuffer B;

		ErrList[err].Buf = null;

		B = Console.FindFile(ErrList[err].file);
		if (B == null)
			return ;

		if(!B.Loaded)
			return;

		AddFileError(B, err);
	}

	void AddFileError(EBuffer B, int err) {
		EPoint P = new EPoint();

		assert(err >= 0 && err < ErrCount);

		String bk = String.format("_MSG.%d", err);

		P.Col = 0;
		P.Row = ErrList[err].line - 1; // offset 0


		if (P.Row >= B.RCount)
			P.Row = B.RCount - 1;
		if (P.Row < 0)
			P.Row = 0;

		if (B.PlaceBookmark(bk, P))
			ErrList[err].Buf = B;
	}

	void FindFileErrors(EBuffer B) {
		for (int i = 0; i < ErrCount; i++)
			if (ErrList[i].Buf == null && ErrList[i].file != null) {
				if (Console.filecmp(B.FileName, ErrList[i].file) == 0) {
					AddFileError(B, i);
				}
			}
	}

	int RunPipe(String ADir, String ACommand) {
		if (Config.KeepMessages==0)
			FreeErrors();

		Command = ACommand;
		Directory = ADir;

		MatchCount = 0;
		ReturnCode = -1;
		Running = true;
		BufLen = BufPos = 0;
		Row = ErrCount - 1;

		{
			String s = String.format("[running '%s' in '%s']", Command, Directory);
			AddError(null, -1, null, s);
		}

		{
			String s = String.format("Messages [%s]: %s", Directory, Command);
			SetTitle(s);
		}

		Console.ChangeDir(Directory);
		PipeId = GPipe.OpenPipe(Command, Directory, this);
		return 0;
	}

	@Override
	EEventMap GetEventMap() {
		return EEventMap.FindEventMap("MESSAGES");
	}

	@Override
	ExResult ExecCommand(ExCommands Command, ExState State) {
		switch (Command) {
		case ExChildClose:
			if (!Running || PipeId == -1)
				break;
			ReturnCode = GPipe.ClosePipe(PipeId);
			PipeId = -1;
			Running = false;
			{
				String s = String.format("[aborted, status=%d]", ReturnCode);
				AddError(null, -1, null, s);
			}
			return ExResult.ErOK;

		case ExActivateInOtherWindow:
			ShowError(View.Next, Row);
			return ExResult.ErOK;
		}
		return super.ExecCommand(Command, State);
	}

	void AddError(Error p) 
	{
		ErrCount++;
		//ErrList = (Error **) realloc(ErrList, sizeof(void *) * ErrCount);
		if(null == ErrList)
			ErrList = new Error[1];
		else
			ErrList = Arrays.copyOf(ErrList, ErrCount);

		ErrList[ErrCount - 1] = p;
		ErrList[ErrCount - 1].Buf = null;
		FindErrorFile(ErrCount - 1);

		if (ErrCount > Count)
			if (Row >= Count - 1) {
				//if (ErrCount > 1 && !ErrList[TopRow].file)
				Row = ErrCount - 1;
			}

		UpdateList();
	}

	void AddError(String file, int line, String msg, String text) {
		AddError( file,  line,  msg,  text, 0);
	}

	void AddError(String file, int line, String msg, String text, int hilit) {
		Error pe = new Error();

		pe.file = file;
		pe.line = line;
		pe.msg = msg;
		pe.text = text;
		pe.hilit = hilit;

		AddError(pe);
	}

	void FreeErrors() {
		if (ErrList!=null) {
			for (int i = 0; i < ErrCount; i++) {
				if (ErrList[i].Buf != null) {
					String bk = String.format("_MSG.%d", i);
					((EBuffer)(ErrList[i].Buf)).RemoveBookmark(bk);
				}
			}
		}
		ErrCount = 0;
		ErrList = null;
		BufLen = BufPos = 0;
	}

	String GetLine() 
	{
		if (Running && PipeId != -1) {
			String p = GPipe.ReadPipe(PipeId);
			//fprintf(stderr, "GetLine: ReadPipe rc = %d\n", rc);
			if (p == null) {
				ReturnCode = GPipe.ClosePipe(PipeId);
				PipeId = -1;
				Running = false;
			}
			return p;
		}
		return null;
	}

	int GetLine(String [] Line, int maxim) {
		int rc;
		String p;
		int l;

		Line[0] = null;

		if (Running && PipeId != -1) {
			p = GPipe.ReadPipe(PipeId);
			//fprintf(stderr, "GetLine: ReadPipe rc = %d\n", rc);
			if (p == null) {
				ReturnCode = GPipe.ClosePipe(PipeId);
				PipeId = -1;
				Running = false;
			}

			// got line
			Line[0] = p;
			return 1;

		}
		return 0;

		/*
		//fprintf(stderr, "GetLine: %d\n", Running);

		Line[0] = null;
		if (Running && PipeId != -1) {
			rc = GUI.gui.ReadPipe(PipeId, MsgBuf + BufLen, sizeof(MsgBuf) - BufLen);
			//fprintf(stderr, "GetLine: ReadPipe rc = %d\n", rc);
			if (rc == -1) {
				ReturnCode = GUI.gui.ClosePipe(PipeId);
				PipeId = -1;
				Running = false;
			}
			if (rc > 0)
				BufLen += rc;
		}
		l = maxim - 1;
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
		 */
	}



	ExResult CompilePrevError(EView V) 
	{
		if (ErrCount <= 0) {
			V.Msg(S_INFO, "No errors.");
			return ExResult.ErFAIL;
		}

		while(Row > 0) {
			Row--;
			if (ErrList[Row].line == -1 || null == ErrList[Row].file) 
				continue;
			ShowError(V, Row);
			return ExResult.ErOK;
		}

		V.Msg(S_INFO, "No previous error.");
		return ExResult.ErFAIL;

	}

	ExResult CompileNextError(EView V) 
	{
		if (ErrCount <= 0) {
			if (Running) 
				V.Msg(S_INFO, "No errors (yet).");
			else
				V.Msg(S_INFO, "No errors.");
			return ExResult.ErFAIL;
		}		

		while(Row < ErrCount - 1) {
			Row++;
			if (ErrList[Row].line == -1 || null == ErrList[Row].file) 
				continue;
			ShowError(V, Row);
			return ExResult.ErOK;
		}
		if (Running)
			V.Msg(S_INFO, "No more errors (yet).");
		else
			V.Msg(S_INFO, "No more errors.");
		return ExResult.ErFAIL;
	}







	void ShowError(EView V, int err) {
		if (err >= 0 && err < ErrCount) {
			if (null == ErrList[err].file) {
				// should check if relative path
				// possibly scan for (gnumake) directory info in output
				if (ErrList[err].Buf != null) {
					//char bk[16];

					V.SwitchToModel(ErrList[err].Buf);

					String bk = String.format("_MSG.%d", err);
					ErrList[err].Buf.GotoBookmark(bk);
				} else {
					if (Console.FileLoad(0, ErrList[err].file, null, V)) {
						V.SwitchToModel(ActiveModel);
						((EBuffer)ActiveModel[0]).CenterNearPosR(0, ErrList[err].line - 1);
					}
				}
				if (ErrList[err].msg != null)
					V.Msg(S_INFO, "%s", ErrList[err].msg);
				else
					V.Msg(S_INFO, "%s", ErrList[err].text);
			}
		}
	}



	@Override
	String GetInfo() {
		return String.format( 
				"%2d %04d/%03d Messages: %d (%s) ",
				ModelNo,
				Row, Count,
				MatchCount,
				Command);
	}












	int Compile(String Command) {
		return 0;
	}



	@Override
	void DrawLine(PCell B, int Line, int Col, int color, int Width) 
	{
		if (Line < ErrCount)
		{
			if (Col < ErrList[Line].text.length() ) 
			{
				//char str[1024];
				int len;
				String str = PCell.UnTabStr( ErrList[Line].text );
				len = str.length(); 

				if (len > Col)
					B.MoveStr(0, Width, str + Col, color, Width);
			}
		}
	}

	@Override
	String FormatLine(int Line) {
		String p;
		if (Line < ErrCount)
			p = ErrList[Line].text;
		else
			p = null;
		return p;
	}

	@Override
	boolean IsHilited(int Line) {
		return (Line >= 0 && Line < ErrCount) ? 0 != ErrList[Line].hilit : false;
	}

	@Override
	void UpdateList() {
		Count = ErrCount;
		super.UpdateList();
	}

	@Override
	int Activate(int No) {
		//assert(No == Row);
		//Row = No;
		ShowError(View, Row);
		return 1;
	}

	@Override
	boolean CanActivate(int Line) {
		boolean ok = false;
		if (Line < ErrCount)
			if (ErrList[Line].file != null || ErrList[Line].line != -1) 
				ok = true;
		return ok;
	}

	@Override
	void NotifyPipe(int APipeId) {
		//fprintf(stderr, "Got notified");
		if (APipeId == PipeId)
			GetErrors();
	}

	@Override
	String GetName() {
		return "Messages";
	}


	@Override
	String GetPath() {
		return Console.Slash(Directory, 0);
	}

	@Override
	void GetTitle(String [] ATitle, String [] ASTitle) {
		ATitle[0] = "Messages: "+ Command;
		ASTitle[0] = "Messages";
	}





	static final String t1 = "entering directory";
	static final String t2 = "leaving directory";

	void GetErrors() {
		//char line[4096];
		RxMatchRes RM;
		//int retc;
		int i, n;
		boolean didmatch = false;
		boolean WasRunning = Running;
		//char fn[256];
		String line; 

		//fprintf(stderr, "Reading pipe\n");
		while( (line =GetLine()) != null ) 
		{
			didmatch = false;
			for (CRegexpDef r : CRegexpDef.CRegexp) 
			{
				Matcher m = r.rx.matcher(line);
				
				if (!m.matches())
					continue;
				
				//int ngrp = m.groupCount();
				
				{
					String file = "";

					String fn = m.group(r.RefFile);					
					String ln = m.group(r.RefLine);
					String msg = m.group(r.RefMsg);


					if (Console.IsFullPath(fn))
						file = fn;
					else {
						String fn1;
						if (curr_dir == null)
							fn1 = Directory;
						else
							fn1 = curr_dir.name;
						fn1 = Console.Slash(fn1, 1);
						fn1 += fn;
						
						/*
						Console.ExpandPath(fn1);
						if (ExpandPath(fn1, fn2) == 0)
							file = fn2;
						else
							file = fn1;
							*/
						file = new File(fn1).getAbsolutePath();
					}
					AddError(file, Integer.parseInt(ln), msg.isEmpty() ? null : msg, line, 1);
					didmatch = true;
					MatchCount++;
					break;
				}
			}
			
			/* TODO check for gnumake 'entering directory'
			if (!didmatch)
			{
				AddError(null, -1, null, line);
				//** Quicky: check for gnumake 'entering directory'
				//** make[x]: entering directory `xxx'
				//** make[x]: leaving...
				//static char t1[] = "entering directory";
				//static char t2[] = "leaving directory";
				//char*   pin;

				int pin = line.indexOf("]:");
				if( pin  >= 0)
				{
					//** It *is* some make line.. Check for 'entering'..
					pin += 2;

					//while(*pin != ':' && *pin != 0) pin++;
					//pin++;
					while (line.charAt(pin) == ' ')
						pin++;
					if (BitOps.strnicmp(line.substring(pin), t1, t1.length()) == 0) {  // Entering?
						//** Get the directory name from the line,
						pin += t1.length();
						getWord(fn, pin);
						//dbg("entering %s", fn);

						if (fn[0]) {
							//** Indeedy entering directory! Link in list,
							aDir a = new aDir();
							a.name= fn;
							a.next = curr_dir;
							curr_dir = a;
						}
					} else if (BitOps.strnicmp(pin, t2, t2.length()) == 0) {  // Leaving?
						pin += t2.length();
						getWord(fn, pin);                   // Get dirname,
						//dbg("leaving %s", fn);

						//aDir *a;

						aDir a = curr_dir;
						if (a != null)
							curr_dir = curr_dir.next;       // Remove from stack,
						if (a != null && stricmp(a.name, fn) == 0) {
							//** Pop this item.
							//free(a.name);
							//delete a;
						} else {
							//** Mismatch filenames . error, and revoke stack.
							//dbg("mismatch on %s", fn);
							AddError(null, -1, null, "fte: mismatch in directory stack!?");

							//** In this case we totally die the stack..
							while(a != null)
							{
								a = curr_dir;
								if (a != null)
									curr_dir = curr_dir.next;
							}
						}
					}
				}
			}
			
			//*/
		}
		//fprintf(stderr, "Reading Stopped\n");
		if (!Running && WasRunning) 
			AddError(null, -1, null, String.format("[done, status=%d]", ReturnCode));

		//UpdateList();
		//NeedsUpdate = 1;
	}

	
	
	
	/*
	String getWord(char* dest, char*& pin)
	{
	    char *pout, *pend;
	    char ch, ec;
	    
	    while (*pin == ' ' || *pin == '\t')
	        pin++;

	    pout = dest;
	    pend = dest + 256 - 1;
	    if (*pin == '\'' || *pin == '"' || *pin == '`') {
	        ec = *pin++;
	        if (ec == '`')
	            ec = '\'';
	        for (;;) {
	            ch  = *pin++;
	            if (ch == '`')
	                ch = '\'';
	            if (ch == ec || ch == 0)
	                break;
	            
	            if (pout < pend)
	                *pout++ = ch;
	        }
	        if (ch == 0)
	            pin--;
	    } else {
	        for(;;) {
	            ch  = *pin++;
	            if (ch == ' ' || ch == '\t' || ch == 0)
	                break;
	            if (pout < pend) *pout++ = ch;
	        }
	    }
	    *pout = 0;
	}
	*/


}





class aDir
{
	aDir       next;
	String     name;
}