package ru.dz.jfte;

import java.awt.Point;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.logging.Logger;

import ru.dz.jfte.c.BitOps;

/**
 * 
 * All the OS dependent stuff is here
 * 
 * @author dz
 *
 */

public class Console implements ModeDefs, GuiDefs, EventDefs
{
	private static final Logger log = Logger.getLogger(Console.class.getName());

	public static Completer CompletePath = new FileCompleter();
	static int CursorVisible = 1;

	static JavaConsole jc;
	private static boolean MouseVisible;

	public static void start() {
		jc = new JavaConsole();		
	}



	static int ConInit(int XSize, int YSize) {
		// ignore size here - not really used
		return 0;
	}

	static int ConDone()
	{
		//jc.close();
		return 0;
	}


	static int ConClear()
	{
		jc.clear();
		return 1;
	}

	static int ConPutBox(int X, int Y, int W, int H, PCell Cell) /*FOLD00*/
	{
		jc.putBox( X,  Y,  W,  H, Cell);
		return 0;
	}


	static int ConGetBox(int X, int Y, int W, int H, PCell Cell) /*FOLD00*/
	{
		jc.getBox( X,  Y,  W,  H, Cell);
		return 0;
	}

	static int ConPutLine(int X, int Y, int W, int H, PCell Cell) /*FOLD00*/
	{
		jc.PutLine( X,  Y,  W,  H, Cell);
		return 0;
	}

	static int ConSetBox(int X, int Y, int W, int H, PCell Cell) /*FOLD00*/
	{
		jc.setBox( X,  Y,  W,  H, Cell);
		return 0;
	}


	static int ConScroll(int Way, int X, int Y, int W, int H, int /*TAttr*/ Fill, int Count) /*FOLD00*/
	{
		jc.scroll( Way, X,  Y,  W,  H,  /*TAttr*/ Fill,  Count);
		return 0;
	}	


	static int ConSetSize(int X, int Y) { /*FOLD00*/
		return -1;
	}


	static int ConQuerySize(int []X, int []Y) { /*FOLD00*/
		X[0] = jc.getWidth();
		Y[0] = jc.getHeight();
		return 1;
	}

	static int getWidth() { return jc.getWidth(); } 
	static int getHeigh() { return jc.getHeight(); } 

	static void ConSetCursorPos(int X, int Y) { /*FOLD00*/
		jc.setCursorPos(X,Y);
	}


	static void ConQueryCursorPos(int []X, int []Y) { /*FOLD00*/
		jc.queryCursorPos(X, Y);
	}


	static int ConShowCursor() { /*FOLD00*/
		CursorVisible = 1;
		jc.drawCursor(true);
		return 0;
	}

	static int ConHideCursor() { /*FOLD00*/
		CursorVisible = 0;
		jc.drawCursor(false);
		return 0;
	}

	static int ConCursorVisible() { /*FOLD00*/
		return CursorVisible;
	}

	static int ConSetCursorSize(int Start, int End) { /*FOLD00*/
		return -1;
	}

	static int ConSetMousePos(int X, int Y) { /*FOLD00*/
		return -1;
	}

	static int ConQueryMousePos(int []X, int []Y) { /*FOLD00*/
		Point m = jc.getMousePos();
		X[0] = m.x ;
		Y[0] = m.y;

		// NT does not have this ? (not needed anyway, but check mouse hiding above).
		return 0;
	}

	static int ConShowMouse() { /*FOLD00*/
		MouseVisible = true;
		//if (!MousePresent) return -1;
		jc.enableMouse(MouseVisible);
		return 0;
	}

	static int ConHideMouse() { /*FOLD00*/
		MouseVisible = false;
		//if (!MousePresent) return -1;
		jc.enableMouse(MouseVisible);
		return 0;
	}

	static boolean ConMouseVisible() { /*FOLD00*/
		return MouseVisible;
	}

	static int ConQueryMouseButtons(int []ButtonCount) { /*FOLD00*/
		return 0;
	}

	static int ConFlush() { /*FOLD00*/
		return 0;
	}

	static char [] chtab = null;
	public static char ConGetDrawChar(int index) 
	{
		if (null==chtab)
		{
			chtab="\u250C\u2510\u2514\u2518\u2500\u2502\u252C\u251C\u2524\u2534\u253C\u001A·─▒░\u001B\u001A".toCharArray();
			chtab[DCH_RPTR] = '→';
			chtab[DCH_EOF]= '□';
			
			chtab[DCH_AUP] = '↑';
			chtab[DCH_ADOWN] = '↓';
		}

		return chtab[index];
	}

	// Which characters to get. defaultCharacters if not set, rest filled
	// with defaultCharacters if too short
	// List of GUICharacters is freed, only one item remains
	/*
	static char []GetGUICharacters( String which, const char []defChars) {
	    GUICharactersEntry *g, *gg, *found = NULL;
	    String s;
	    unsigned int i;

	    for (g = GUICharacters; g; g=gg) {
	        gg = g.next;
	        if (strcmp(g.name, which) == 0) {
	            if ((i = strlen(g.chars)) < strlen(defChars)) {
	                s = new char [strlen(defChars) + 1];
	                assert(s != NULL);
	                strcpy(s, g.chars);
	                strcpy(s + i, defChars + i);
	                delete g.chars;
	                g.chars = s;
	            }
	            if (found) {
	                free(found.chars); free(found.name); free(found);
	            }
	            found = g;
	        } else {
	            free(g.name); free(g.chars); free(g);
	        }
	    }
	    GUICharacters = found;
	    if (found) return found.chars; else return defChars;
	}
	 */



	static int GetDefaultDirectory(EModel M, String [] Path) {
		if (M!=null) 
			Path[0] = M.GetPath();
		if (M==null || Path[0] == null)
			if (ExpandPath(".", Path) == -1)
				return 0;
		Path[0] = Console.SlashDir(Path[0]);
		return 1;
	}

	static int SetDefaultDirectory(EModel M) {
		String [] Path = {""};

		if (GetDefaultDirectory(M, Path) == 0)
			return 0;
		ChangeDir(Path[0]);

		return 1;
	}





	private static String tt = "", stt = "";
	
	/**
	 * 
	 * @param title Window title
	 * @param sTitle Icon/toolbar title - short
	 * 
	 */
	public static void ConSetTitle(String title, String sTitle) {

		tt = title;
		stt = sTitle;

		//jc.setTitle("jFTE - "+title+" - "+sTitle);
		jc.setTitle("jFTE - "+title);
	}

	public static int ConGetTitle(String[] title, String[] sTitle) {
		title[0] = tt;
		sTitle[0] = stt;
		return 0;
	}







	static TEvent EventBuf = null;
	static int ConPutEvent(TEvent Event) { /*FOLD00*/
		EventBuf = Event;
		return 0;
	}



	private static TEvent e = null;

	public static TEvent ConGetEvent(int eventMask, int waitTime, boolean delete) 
	{
		// TODO eventMask is ignored in some versions of original code, but is used by callers

		while(e == null)
			fillEvent();

		System.out.println(e);
		
		TEvent ret = e;

		if(delete) 
			e = null;

		return ret;
	}

	private static Object waito = new Object();
	private static void fillEvent()
	{
		// must wait for mult objects, will just poll
		while(true)
		{
			if(EventBuf !=null)
			{
				e = EventBuf;
				EventBuf = null;
				break;
			}

			//e = pollMouse();
			//if(e != null) break;

			// does mouse too
			e = jc.pollKeyb();
			if(e != null) break;

			

			// poll pipes?
			if( (e = GPipe.checkPipeData()) != null)
				break;
			
			try {
				synchronized (waito){
				waito.wait(10);
				}
			} catch (InterruptedException e1) {
				log.severe("Exception in fillEvent: "+e);
			}
		}

	}

	static int ConGrabEvents(int /*TEventMask*/ EventMask) { /*FOLD00*/
		return 0;
	}








	public static void DieError(int rc, String msg, Object... p) {
		System.err.printf(msg,p);
		System.exit(rc);
	}

	/**
	 * 
	 * @param fileName
	 * @param mode
	 * @return true if file exists
	 */
	public static boolean fileExist(String fileName, int mode) {
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
	public static String Slash(String Path, int Add) {
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
					Path = Path.substring(0,len - 1);
				}
			}
		}
		return Path;
	}


public static String SlashDir(String Path) 
{
    int len = Path.length();
    
    if ((len == 2) && Path.charAt(1) == ':')
        return Path+"/";

    /*
    if (len > 1) {
            if (!ISSLASH(Path[len - 1])) {
                if (stat(Path, &statbuf) == 0) {
                    if (S_ISDIR(statbuf.st_mode)) {
                        Path[len] = SLASH;
                        Path[len+1] = 0;
                    }
                }
            }
    }
    */
    
    if(IsDirectory(Path))
    	return Path+"/";
    
    return Path;
}


















	static EBuffer FindFile(String FileName) {
		EModel M;
		EBuffer B;

		M = EModel.ActiveModel[0];
		while (M!=null) {
			if (M.GetContext() == CONTEXT_FILE) {
				B = (EBuffer )M;
				if (filecmp(B.FileName, FileName) == 0) { return B; }
			}
			M = M.Next;
			if (M == EModel.ActiveModel[0]) break;
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

	static boolean FileLoad(int createFlags, String FileName, String Mode, EView View) {
		String Name[] = {""};
		EBuffer B;

		assert(View != null);

		if (ExpandPath(FileName, Name) == -1) {
			View.MView.Win.Choice(GPC_ERROR, "Error", 1, "O&K", "Invalid path: %s.", FileName);
			return false;
		}
		B = FindFile(Name[0]);
		if (B != null) {
			if (Mode != null)
				B.SetFileName(Name[0], Mode);
			View.SwitchToModel(B);
			return true;
		}
		B = new EBuffer(createFlags, EModel.ActiveModel, Name[0]);
		B.SetFileName(Name[0], Mode);

		View.SwitchToModel(B);
		return true;
	}

	static boolean MultiFileLoad(int createFlags, String FileName, String Mode, EView View) {
		String  fX[] = {""};
		int count = 0;
		String  FPath[] = {""};
		String  FName[] = {""};
		FileFind ff;
		FileInfo fi;
		//int rc;

		assert(View != null);

		fX[0] = directory(FileName);
		if (fX[0] == null) fX[0] = ".";
		
		JustFileName(FileName, FName);
		if (ExpandPath(fX[0], FPath) == -1) return false;
		FPath[0] = Slash(FPath[0], 1);

		ff = new FileFind(FPath[0], FName[0], FileFind.ffHIDDEN | FileFind.ffFULLPATH);

		while((fi = ff.FindNext()) != null) {
			count++;
			if (!FileLoad(createFlags, fi.Name(), Mode, View)) {
				return false;
			}
		}
		
		if (count == 0)
			return FileLoad(createFlags, FileName, Mode, View);
		
		return true;
	}

	public static boolean FileExists(String fileName) {
		File f = new File(fileName);
		return f.isFile();
	}

	public static BasicFileAttributes stat(String fileName) 
	{
		Path p = Path.of(fileName);
		BasicFileAttributes attrs;
		try {
			attrs = Files.readAttributes(p, BasicFileAttributes.class);
		} catch (IOException e) {
			log.severe("Exception in stat("+fileName+"): "+e);
			return null;
		}
		return attrs; 		
	}

	public static boolean isReadonly(String fileName) {
		return !new File(fileName).canWrite();
	}

	public static int unlink(String fn) {
		return (new File(fn).delete()) ? 0 : -1;
	}

	public static boolean rename(String from, String to) {
		return new File(from).renameTo(new File(to));
	}

	public static int RunProgram(int runWait, String Command, GFrame frames) {
		int [] W = {0}, H = {0}, W1 = {0}, H1 = {0};

		ConQuerySize(W, H);
		ConHideMouse();
		// XXX ConSuspend();

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
				log.severe("Exception in RunProgram("+Command+"): "+e);
				rc = -1;
			}

		// XXX ConContinue();
		ConShowMouse();
		ConQuerySize(W1, H1);

		if (W != W1 || H != H1) {
			frames.Resize(W1[0], H1[0]);
		}
		frames.Repaint();

		return rc;

	}

	public static boolean IsDirectory(String file) {
		return new File(file).isDirectory();
	}


	public static String expandPath(String Path) {		
		return new File(Path).getAbsolutePath();
	}
	
	public static int ExpandPath(String Path, String[] Expand) {		
		Expand[0] = new File(Path).getAbsolutePath();
		return 0;
		/*
		String Name;

		if (Path.isBlank()) {
			Expand[0] = null;
			return 0;
		}
		
		#if PATHTYPE == PT_DOSISH
				int slashed = 0;

		strcpy(Name, Path);
		if (Name[0] != 0)
			slashed = ISSLASH(Name[strlen(Name)-1]);
		Slash(Name, 0);

			//puts(Name);
		if (Name[0] && Name[1] == ':' && Name[2] == 0) { // '?:'
			int drive = Name[0];

			strcpy(Expand, Name);
			Expand[2] = '\\';
			Expand[3] = 0;

			drive = (int)(toupper(Name[0]) - 'A' + 1);

			if (GetDiskCurDir(drive, Expand + 3) == -1)
				return -1;
		} else {

				if (_fullpath(Expand, Name, MAXPATH) == NULL) return -1;

		}


		if (slashed)
			SlashDir(Expand);
		//puts(Expand);
		return 0;
		#endif
		
		#if PATHTYPE == PT_UNIXISH
		char Name2[MAXPATH];
		String path, *p;

		strcpy(Name, Path);
		switch (Name[0]) {
		case SLASH:
			break;
		case '~':
			if (Name[1] == SLASH || Name[1] == 0) {
				path = Name + 1;
				strncpy(Name2, getenv("HOME"), sizeof(Name2) - 1);
				Name2[sizeof(Name2) - 1] = 0;
			} else {
				struct passwd *pwd;

				p = Name;
				p++;
				while (*p && (*p != SLASH)) p++;
				if (*p == SLASH) {
					path = p + 1;
					*p = 0;
				} else {
					path = p;
				}
				pwd = getpwnam(Name + 1);
				if (pwd == NULL)
					return -1;
				strcpy(Name2, pwd.pw_dir);
			}
			if (path[0] != SLASH)
				Slash(Name2, 1);
			strcat(Name2, path);
			strcpy(Name, Name2);
			break;
		default:
			if (getcwd(Name, MAXPATH) == NULL) return -1;
			Slash(Name, 1);
			strcat(Name, Path);
			break;
		}
		return RemoveDots(Name, Expand);
		#endif
		*/
	}



	static boolean IsSameFile(String Path1, String Path2) 
	{
		/*
	    String  p1[] = {null}, p2[] = {null};

	    if (ExpandPath(Path1, p1) == -1) return -1;
	    if (ExpandPath(Path2, p2) == -1) return -1;
	    if (filecmp(p1, p2) == 0) return 1;
	    */
		Path p1 = Path.of(Path1).toAbsolutePath();
		Path p2 = Path.of(Path2).toAbsolutePath();
		return p1.equals(p2);
	    //return 0;
	}


	/**
	 * 
	 * @param path
	 * @return Parent or null for root. Converts to absolute
	 */
	public static String parent(String path) {
		Path a = Path.of(path).toAbsolutePath();
		Path p = a.getParent();
		
		if(p == null)
			return null;
		else
			return p.toString();
	}

	/**
	 * 
	 * @param path
	 * @return path refers to dir - return it. If to file - return dir
	 */
	public static String directory(String path) {
		Path a = Path.of(path).toAbsolutePath();

		if( a.toFile().isDirectory() )
			return a.toString();

		Path p = a.getParent();
		
		if(p == null)
			return null;
		else
			return p.toString();
	}
	
	
	public static int _off_JustDirectory(String path, String[] Dir) {
		Path a = Path.of(path).toAbsolutePath();
		Path p = a.getParent();

		/*
		if( p == null && a.toFile().isDirectory() )
		{
			Dir[0] = a.toString();
			return 0;
		}*/
		
		if(p == null)
		{
			// must be root dir?
			Dir[0] = null;
			return -1;
			
			//Dir[0] = a.toString();
			//return 0;
		}
		else
		{
			Dir[0] = p.toString();
			return 0;
		}
		/*
		String p;

	    if (ExpandPath(Path, Dir) == -1)
	        strcpy(Dir, Path);
	    p = SepRChr(Dir);
	    if (p) { p[1] = 0; }
	    else Dir[0] = 0;
	    */
	    //return 0;
	}

	static int JustLastDirectory(String path, String [] Dir) 	
	{
		Path p = Path.of(path);
		if(p.getNameCount() < 2)
			Dir[0] = "";
		else
			Dir[0] = p.getName(p.getNameCount()-2).toString();
		
		/*
	    int lastSlash = strlen(Path);
	    while (lastSlash > 0 && !ISSEP(Path[lastSlash])) lastSlash--;

	    int secondLastSlash = lastSlash;
	    while (secondLastSlash > 0 && !ISSEP(Path[secondLastSlash - 1])) secondLastSlash--;

	    strncpy(Dir, Path + secondLastSlash, lastSlash - secondLastSlash);
	    Dir[lastSlash - secondLastSlash] = 0;

	    */
	    return 0;
	}

	static int JustFileName(String path, String [] Name) 
	{
		Path p = Path.of(path).getFileName();
		Name[0] = p == null ? path : p.toString();
		/*
		int len = strlen(Path);

	    while (len > 0 && !ISSEP(Path[len - 1])) len--;
	    strcpy(Name, Path + len);
	    */
	    return 0;
	}

	static int JustRoot(String path, String [] Root) {
		Path p = Path.of(path);
		Root[0] = p.getRoot().toString();
		/*
	#if PATHTYPE == PT_UNIXISH
	    strcpy(Root, SSLASH);
	#else
	    strncpy(Root, Path, 3);
	    Root[3] = 0;
	#endif
		*/
	    return 0;
	}

	static int JoinDirFile(String [] Dest, String Dir, String Name) {
	    Dest[0] = Dir;
	    Dest[0] = Slash(Dest[0], 1);
	    Dest[0] += Name;
	    return 0;
	}


	public static int filecmp(String f1, String f2) {
		return new File(f1).equals(new File(f2)) ? 0 : 1;
	}


	public static boolean copyfile(String f, String t) {
		try {
			Files.copy(Path.of(f), Path.of(t));
			return true;
		} catch (IOException e) {
			log.severe("Exception in copyfile("+f+" -> "+t+"): "+e);
			return false;
		}
	}


	public static void ChangeDir(String directory) 
	{
		//File dir = new File(directory).getAbsoluteFile();
		//System.setProperty("user.dir", dir.toString());
		// TODO ChangeDir
	}


	
	
	
	
	
	
	
	
	

	
	
	
	
	public static ExResult SysShowHelp(ExState state, String subject) {
		// TODO SysShowHelp
		return ExResult.ErFAIL;
	}


	public static boolean IsFullPath(String name) {
		return new File(name).isAbsolute();
	}


	public static String getHomeDir() {
		return System.getProperty("user.home");
	}



	public static boolean SameDir(String D1, String D2) 
	{
	    if (D1 == null || D2 == null) return false;
	    
	    D1 = Slash(D1,0);
	    D2 = Slash(D2,0);
	    
	    //int l1 = D1.length();
	    //int l2 = D2.length();

    	return 0 == BitOps.strcmp(D1,D2);
	    
	    
	    /*
	    if (l1<l2) 
	    	return BitOps.strncmp(D1,D2,l1)==0 && BitOps.strcmp(D2.substring(l1),SSLASH)==0;	    
	    else if (l1==l2) 
	    	return 0 == BitOps.strcmp(D1,D2);
	    else 
	    	return strncmp (D1,D2,l2)==0&&strcmp (D1+l1,SSLASH)==0;
	    	*/
	}


	
}
