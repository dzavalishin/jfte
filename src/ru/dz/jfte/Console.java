package ru.dz.jfte;

import java.io.File;
import java.io.IOException;
import java.nio.file.attribute.BasicFileAttributes;

/**
 * 
 * All the OS dependent stuff is here
 * 
 * @author dz
 *
 */

public class Console {

	public static Completer CompletePath = new FileCompleter();

	static TEvent ReadConsoleEvent() { 
		// TODO impl me
		return null;
	}

	static int ConInit(int XSize, int YSize) {
		
	}
	
	static int ConDone()
	{
		
	}
	
	
	static int ConClear()
	{
		
	}
	
	static int ConPutBox(int X, int Y, int W, int H, PCell Cell) /*FOLD00*/
	{
	}
	
	
	static int ConGetBox(int X, int Y, int W, int H, PCell Cell) /*FOLD00*/
	{
	}

	static int ConPutLine(int X, int Y, int W, int H, PCell Cell) /*FOLD00*/
	{
	}

	static int ConSetBox(int X, int Y, int W, int H, PCell Cell) /*FOLD00*/
	{
	}
	
	
	static int ConScroll(int Way, int X, int Y, int W, int H, int /*TAttr*/ Fill, int Count) /*FOLD00*/
	{
	}	
	
	
	static int ConSetSize(int X, int Y) { /*FOLD00*/
	    return -1;
	}
	

	static int ConQuerySize(int []X, int []Y) { /*FOLD00*/
	}
	
	static int getWidth() { return width; } 
	static int getHeigh() { return height; } 
	
	static int ConSetCursorPos(int X, int Y) { /*FOLD00*/
	}
			
	
	static int ConQueryCursorPos(int []X, int []Y) { /*FOLD00*/
	}

	
	static int ConShowCursor() { /*FOLD00*/
	    CursorVisible = 1;
	    DrawCursor(1);
	    return 0;
	}

	static int ConHideCursor() { /*FOLD00*/
	    CursorVisible = 0;
	    DrawCursor(0);
	    return 0;
	}

	static int ConCursorVisible() { /*FOLD00*/
	    return (CursorVisible == 1);
	}

	static int ConSetCursorSize(int Start, int End) { /*FOLD00*/
	    return -1;
	}

	static int ConSetMousePos(int X, int Y) { /*FOLD00*/
	    return -1;
	}

	static int ConQueryMousePos(int []X, int []Y) { /*FOLD00*/
	    X[0] = LastMouseX;
	    Y[0] = LastMouseY;

	    // NT does not have this ? (not needed anyway, but check mouse hiding above).
	    return 0;
	}

	static int ConShowMouse() { /*FOLD00*/
	    MouseVisible = 1;
	    if (!MousePresent) return -1;
	    return 0;
	}

	static int ConHideMouse() { /*FOLD00*/
	    MouseVisible = 0;
	    if (!MousePresent) return -1;
	    return 0;
	}

	static int ConMouseVisible() { /*FOLD00*/
	    return (MouseVisible == 1);
	}

	static int ConQueryMouseButtons(int []ButtonCount) { /*FOLD00*/
	    return 0;
	}

	static int ConPutEvent(TEvent Event) { /*FOLD00*/
	    EventBuf = Event;
	    return 0;
	}

	static int ConFlush() { /*FOLD00*/
	    return 0;
	}

	static int ConGrabEvents(int /*TEventMask*/ EventMask) { /*FOLD00*/
	    return 0;
	}

	public static char ConGetDrawChar(int index) {
		// TODO Auto-generated method stub
		//return ;
	}
	
	
	

	
	static int GetDefaultDirectory(EModel M, String [] Path) {
	    if (M!=null) 
	        Path[0] = M.GetPath();
	    if (M==null || Path[0] == null)
	        if (ExpandPath(".", Path) == -1)
	            return 0;
	    SlashDir(Path);
	    return 1;
	}

	static int SetDefaultDirectory(EModel M) {
	    String [] Path;
	    
	    if (GetDefaultDirectory(M, Path) == 0)
	        return 0;
	    if (ChangeDir(Path) == -1)
	        return 0;
	    return 1;
	}

	public static void ConSetTitle(String title, String sTitle) {
		// TODO Auto-generated method stub
		
	}

	public static int ConGetTitle(String[] title, String[] sTitle) {
		// TODO Auto-generated method stub
		//return 0;
	}

	public static TEvent ConGetEvent(int eventMask, int waitTime, boolean delete) {
		// TODO Auto-generated method stub
		//return null;
	}
	

	static void DieError(int rc, String msg, Object... p) {
		System.err.printf(msg,p);
	    System.exit(rc);
	}

	/**
	 * 
	 * @param fileName
	 * @param mode
	 * @return true if file exists
	 */
	public static boolean access(String fileName, int mode) {
		File f = new File(fileName);
		return f.isFile();
	}


	static boolean ISSLASH(char c) { return ((c == '/') || (c == '\\')); }
	static boolean ISSEP(char c) { return  ((c == ':') || ISSLASH(c)); }

	/**
	 * 
	 * @param Path
	 * @param Add if not - remove final separator
	 * @return
	 */
	static String Slash(String Path, int Add) {
	    int len = Path.length();

	    if (Add!=0) {
	        if ((len == 0) || !ISSLASH(Path.charAt(len - 1)))
	            Path += '/';
	    } else {
	        if ((len > 1)
	//#if PATHTYPE == PT_DOSISH
	            && ((len > 3) || (Path.charAt(1) != ':'))
	//#endif
	           ) {
	            if (ISSLASH(Path.charAt(len - 1))) {
	                Path = Path.substring(len - 1);
	            }
	        }
	    }
	    return Path;
	}



	
	
	
	
	
	
	
	
	
	
	
	
	
	
	

	EBuffer FindFile(String FileName) {
	    EModel M;
	    EBuffer B;
	    
	    M = EModel.ActiveModel;
	    while (M!=null) {
	        if (M.GetContext() == CONTEXT_FILE) {
	            B = (EBuffer )M;
	            if (filecmp(B.FileName, FileName) == 0) { return B; }
	        }
	        M = M.Next;
	        if (M == EModel.ActiveModel) break;
	    }
	    return null;
	}

	/*#if 0
	static void SwitchModel(EModel *AModel) {
	    if (AModel != AcgiveModel && MM && AModel) {
	        AModel.Prev.Next = AModel.Next;
	        AModel.Next.Prev = AModel.Prev;

	        AModel.Next = MM;
	        AModel.Prev = MM.Prev;
	        AModel.Prev.Next = AModel;
	        MM.Prev = AModel;
	        MM = AModel;
	    }
	}
	#endif */

	int FileLoad(int createFlags, String FileName, String Mode, EView View) {
	    String Name[] = {""};
	    EBuffer B;

	    assert(View != null);
	    
	    if (ExpandPath(FileName, Name) == -1) {
	        View.MView.Win.Choice(GPC_ERROR, "Error", 1, "O&K", "Invalid path: %s.", FileName);
	        return 0;
	    }
	    B = FindFile(Name[0]);
	    if (B) {
	        if (Mode != null)
	            B.SetFileName(Name, Mode);
	        View.SwitchToModel(B);
	        return 1;
	    }
	    B = new EBuffer(createFlags, EModel.ActiveModel, Name[0]);
	    B.SetFileName(Name[0], Mode);

	    View.SwitchToModel(B);
	    return 1;
	}

	int MultiFileLoad(int createFlags, String FileName, String Mode, EView View) {
		String  fX[];
	    int count = 0;
	    String  FPath[];
	    String  FName[];
	    FileFind ff;
	    FileInfo fi;
	    int rc;

	    assert(View != null);

	    JustDirectory(FileName, fX);
	    if (fX[0] == 0) strcpy(fX, ".");
	    JustFileName(FileName, FName);
	    if (ExpandPath(fX, FPath) == -1) return 0;
	    Slash(FPath, 1);

	    ff = new FileFind(FPath, FName, ffHIDDEN | ffFULLPATH);
	    if (ff == 0)
	        return 0;
	    rc = ff.FindFirst(fi);
	    while (rc == 0) {
	        count++;
	        if (FileLoad(createFlags, fi.Name(), Mode, View) == 0) {
	            return 0;
	        }
	        rc = ff.FindNext(fi);
	    }
	    if (count == 0)
	        return FileLoad(createFlags, FileName, Mode, View);
	    return 1;
	}

	public static boolean FileExists(String fileName) {
		File f = new File(fileName);
		return f.isFile();
	}

	public static BasicFileAttributes stat(String fileName) {
		// TODO Auto-generated method stub
		//return null;
	}

	public static boolean isReadonly(String fileName) {
		// TODO Auto-generated method stub
		//return false;
	}

	public static void unlink(String fn) {
		new File(fn).delete();
	}

	public static boolean rename(String from, String to) {
		return new File(from).renameTo(new File(to));
	}

	public static int RunProgram(int runWait, String Command, GFrame frames) {
		    int [] W = {0}, H = {0}, W1 = {0}, H1 = {0};

		    ConQuerySize(W, H);
		    ConHideMouse();
		    // TODO ConSuspend();

		    // TODO unix
		    if (Command == null)      // empty string = shell
		        Command = System.getenv( "COMSPEC" );

		    //int rc = System.system(Command);
		    
		    
		    int rc = 0;
		    if(runWait!=0)
				try {
				    Process p = Runtime.getRuntime().exec(Command);
					rc = p.waitFor();
				} catch (InterruptedException | IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
					rc = -1;
				}
		    
		    // TODO ConContinue();
		    ConShowMouse();
		    ConQuerySize(W1, H1);

		    if (W != W1 || H != H1) {
		        frames.Resize(W1[0], H1[0]);
		    }
		    frames.Repaint();
		    
		    return rc;
		
	}
	
	
	
	
}
