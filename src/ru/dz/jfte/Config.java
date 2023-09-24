package ru.dz.jfte;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import ru.dz.jfte.c.ArrayPtr;
import ru.dz.jfte.c.ByteArrayPtr;

public class Config implements ConfigDefs, ModeDefs, GuiDefs, ColorDefs 
{
	static int SystemClipboard = 0;
	static int ScreenSizeX = -1, ScreenSizeY = -1;
	static int ScrollBarWidth = 1;
	static int CursorInsSize[] = { 90, 100 };
	static int CursorOverSize[] = { 0, 100 };
	static int OpenAfterClose = 1;
	static int SelectPathname = 0;
	static String DefaultModeName = "";
	RxNode CompletionFilter = null;
	//#if defined(DOS) || defined(DOSP32)
	//char Prstatic intDevice[MAXPATH] = "PRN";
	//#else
	static String PrintDevice = "\\DEV\\PRN";
	//#endif
	static String CompileCommand = "make";
	static int KeepHistory = 0;
	static int LoadDesktopOnEntry = 0;
	static boolean SaveDesktopOnExit = false;
	static String WindowFont = "";
	static int KeepMessages = 0;
	static int ScrollBorderX = 0;
	static int ScrollBorderY = 0;
	static int ScrollJumpX = 8;
	static int ScrollJumpY = 1;
	static int GUIDialogs = 1;
	static int PMDisableAccel = 0;
	static int SevenBit = 0;
	static boolean WeirdScroll = false;
	static int LoadDesktopMode = 0;
	static String HelpCommand = "man -a";
	static String ConfigSourcePath = null;
	static boolean IgnoreBufferList = false;
	//GUICharactersEntry *GUICharacters = NULL;
	static String CvsCommand = "cvs";
	static String CvsLogMode = "PLAIN";
	static int ReassignModelIds = 0;



	static class CurPos {
		int sz;
		//String a;
		//String c;
		//String z;
		ByteArrayPtr a;
		ByteArrayPtr c; // cur ptr
		//ByteArrayPtr z; // endpos?
		int line;
		String name; // filename
	};

	static class Obj
	{
		byte type;
		int len;
	}


	/*
	public static int UseDefaultConfig() {
		    CurPos cp;
		    int rc;

		    cp.name = "Internal Configuration";
		    cp.sz = sizeof(DefaultConfig);
		    cp.a = (String )DefaultConfig;
		    cp.c = (String )DefaultConfig + 2 * 4;
		    cp.z = cp.a + cp.sz;
		    cp.line = 1;

		    rc = ReadConfigFile(cp);

		    if (rc == -1)
		        DieError(1, "Error %s offset %d\n", cp.name, cp.c - cp.a);
		    return rc;
		}
	 */




	static boolean LoadConfig(String CfgFileName)  
	{
		//STARTFUNC("LoadConfig");
		//LOG << "Config file: " << CfgFileName << ENDLINE;

		byte[] allCfg = null;
		try {
			allCfg = Files.readAllBytes(Path.of(CfgFileName));
		} catch (IOException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();

			Console.DieError(1, "Error in %s exception %s\n", CfgFileName, e1.toString());
			return false;
		}

		long ln = (allCfg[3] << 24) + (allCfg[2] << 16) + (allCfg[1] << 8) + allCfg[0];

		//* TODO cnf sig
		if (ln != CONFIG_ID) {
			Console.DieError(0, "Bad .CNF signature");
			return false;
		} //*/

		ln = (allCfg[4+3] << 24) + (allCfg[4+2] << 16) + (allCfg[4+1] << 8) + allCfg[4+0];

		/* TODO if (ln != VERNUM) {
	    	Console.DieError(0, "Bad .CNF version.");
	        return false;
	    } */

		CurPos cp = new CurPos();

		cp.name = CfgFileName;
		cp.sz = allCfg.length;
		cp.a = new ByteArrayPtr(allCfg);
		//cp.c = cp.a + 2 * 4;
		cp.c = new ByteArrayPtr( cp.a, 2 * 4 ); 
		//cp.z = cp.a + cp.sz;
		cp.line = 1;

		boolean rc;
		try {
			rc = ReadConfigFile(cp);
		} catch (ConfigFormatException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();

			Console.DieError(1, "Error in %s exception %s\n", CfgFileName, e.toString());
			return false;
		}

		if (!rc) {
			Console.DieError(1, "Error %s offset %d\n", CfgFileName, cp.c.getPos());
			return false;
		}

		return true;
	}



















	static boolean ReadConfigFile(CurPos cp) throws ConfigFormatException {
		Obj obj;
		//short len;

		{
			String p;

			obj = GetObj(cp);
			assert(obj.type == CF_STRING);
			if ((p = GetCharStr(cp, obj.len)) == null)
				return false;

			ConfigSourcePath = p;
		}

		while(true) 
		{
			obj = GetObj(cp);
			if( obj.type == 0xFF)
				break;


			switch (obj.type) {
			case CF_SUB:
			{
				String CmdName = GetCharStr(cp, obj.len);

				if (ReadCommands(cp, CmdName) == -1) return false;
			}
			break;
			case CF_MENU:
			{
				String MenuName = GetCharStr(cp, obj.len);

				if (ReadMenu(cp, MenuName) == -1) return false;
			}
			break;
			case CF_EVENTMAP:
			{
				EEventMap EventMap = null;
				String MapName = GetCharStr(cp, obj.len);
				String UpMap = null;

				obj = GetObj(cp);
				if (obj.type != CF_PARENT) return false;
				if (obj.len > 0)
					if ((UpMap = GetCharStr(cp, obj.len)) == null) return false;

				// add new mode
				if ((EventMap = EEventMap.FindEventMap(MapName)) == null) {
					EEventMap OrgMap = null;

					if (!UpMap.isBlank())
						OrgMap = EEventMap.FindEventMap(UpMap);
					EventMap = new EEventMap(MapName, OrgMap);
				} else {
					if (EventMap.Parent == null)
						EventMap.Parent = EEventMap.FindEventMap(UpMap);
				}
				if (ReadEventMap(cp, EventMap, MapName) == -1) return false;

			}
			break;

			case CF_COLORIZE:
			{
				EColorize Mode = null;
				String ModeName = GetCharStr(cp, obj.len);
				String UpMode = null;

				obj = GetObj(cp);
				if (obj.type != CF_PARENT) return false;
				if (obj.len > 0)
					if ((UpMode = GetCharStr(cp, obj.len)) == null) return false;


				/* TODO / add new mode
				if ((Mode = FindColorizer(ModeName)) == 0)
					Mode = new EColorize(ModeName, UpMode);
				else {
					if (Mode.Parent == 0)
						Mode.Parent = FindColorizer(UpMode);
				}
				if (ReadColorize(cp, Mode, ModeName) == -1)
					return false;
				 */
			}
			break;

			case CF_MODE:
			{
				EMode Mode = null;
				String ModeName = GetCharStr(cp, obj.len);
				String UpMode = null;

				obj = GetObj(cp);
				if (obj.type != CF_PARENT) return false;
				if (obj.len > 0)
					if ((UpMode = GetCharStr(cp, obj.len)) == null) return false;

				/* TODO / add new mode
				if ((Mode = FindMode(ModeName)) == 0) {
					EMode OrgMode = 0;
					EEventMap Map;

					if (strcmp(UpMode, "") != 0)
						OrgMode = FindMode(UpMode);
					Map = FindEventMap(ModeName);
					if (Map == 0) {
						EEventMap OrgMap = 0;

						if (strcmp(UpMode, "") != 0)
							OrgMap = FindEventMap(UpMode);
						Map = new EEventMap(ModeName, OrgMap);
					}
					Mode = new EMode(OrgMode, Map, ModeName);
					Mode.fNext = Modes;
					Modes = Mode;
				} else {
					if (Mode.fParent == 0)
						Mode.fParent = FindMode(UpMode);
				}
				if (ReadMode(cp, Mode, ModeName) == -1)
					return false;
				 */
			}
			break;
			case CF_OBJECT:
			{
				String ObjName;

				if ((ObjName = GetCharStr(cp, obj.len)) == null)
					return false;
				if (ReadObject(cp, ObjName) == -1)
					return false;
			}
			break;
			case (byte) CF_EOF:
				return true;

			default:
				System.err.printf("unk obj type %d\n", obj.type);
				cp.c.shift(obj.len);
				break;
				//return false;
			}
		}
		return false;
	}




	static int ReadObject(CurPos cp, String ObjName) throws ConfigFormatException 
	{
		Obj obj;

		while(true) 
		{
			obj = GetObj(cp);
			if( obj.type == 0xFF)
				break;


			switch (obj.type) {

			case CF_COLOR:
				if (ReadColors(cp, ObjName) == -1) return -1;
				break;

			case CF_COMPRX:
			{
				long file, line, msg;
				String regexp;

				//if (GetObj(cp, len) != CF_INT) return -1;
				GetAssertObj(cp, CF_INT);
				file =GetNum(cp);
				//if (GetObj(cp, len) != CF_INT) return -1;
				GetAssertObj(cp, CF_INT);
				line = GetNum(cp);
				//if (GetObj(cp, len) != CF_INT) return -1;
				GetAssertObj(cp, CF_INT);
				msg = GetNum(cp);
				//if (GetObj(cp, len) != CF_REGEXP) return -1;
				obj = GetAssertObj(cp, CF_REGEXP);
				if ((regexp = GetCharStr(cp, obj.len)) == null) return -1;

				// TODO if (AddCRegexp(file, line, msg, regexp) == 0) return -1;
			}
			break;

			case CF_CVSIGNRX:
			{
				String regexp;

				//if (GetObj(cp, len) != CF_REGEXP) return -1;
				obj = GetAssertObj(cp, CF_REGEXP);
				if ((regexp = GetCharStr(cp, obj.len)) == null) return -1;

				// TODO if (AddCvsIgnoreRegexp(regexp) == 0) return -1;
			}
			break;

			case CF_SETVAR:
			{
				long what = GetNum(cp);

				obj = GetObj(cp);
				switch (obj.type) {
				case CF_STRING:
				{
					if (obj.len == 0) return -1;
					String val = GetCharStr(cp, obj.len);
					if (SetGlobalString((int)what, val) != 0) return -1;
				}
				break;
				case CF_INT:
				{
					long num;

					num = GetNum(cp);
					if (SetGlobalNumber((int)what, (int) num) != 0) return -1;
				}
				break;
				default:
					return -1;
				}
			}
			break;
			case CF_END:
				return 0;
			default:
				System.err.printf("unk obj type %d in ReadObject\n", obj.type);
				cp.c.shift(obj.len);
				break;
				/// TODO return -1;
			}
		}
		return -1;
	}


























	/*unsigned char GetObj(CurPos cp, unsigned short &len) 
	{
	    len = 0;
	    if (cp.c + 3 <= cp.z) {
	        unsigned char c;
	        unsigned char l[2];
	        c = *cp.c++;
	        memcpy(l, cp.c, 2);
	        len = (l[1] << 8) + l[0];
	        cp.c += 2;
	        return c;
	    }
	    return 0xFF;
	}*/


	static Obj GetObj(CurPos cp)
	{
		Obj ret = new Obj();

		ret.len = 0;
		ret.type = (byte) 0xff;

		int lh, ll;
		ret.type  = cp.c.rpp();
		ll = cp.c.rpp();
		lh = cp.c.rpp();
		ret.len = (lh << 8) + ll;

		return ret;
	}

	static Obj GetAssertObj(CurPos cp, int type) throws ConfigFormatException
	{
		Obj o = GetObj(cp);
		if( o.type != type )
			throw new ConfigFormatException("type is not "+type+" at "+cp.c.getPos());
		return o;
	}

	static String GetCharStr(CurPos cp, int len) {
		//STARTFUNC("GetCharStr");
		// // LOG << "Length: " << len << ENDLINE;

		/*
	    String p = cp.c;
	    if (cp.c + len > cp.z)
	    {
	        // LOG << "End of config file in GetCharStr" << ENDLINE;
	        return 0;
	    }
	    cp.c += len;
	    ENDFUNCRC(p);
		 */
		return cp.c.getLenAsString(len);
	}

	static int GetNum(CurPos cp) {
		int n0 = cp.c.urpp();
		int n1 = cp.c.urpp();
		int n2 = cp.c.urpp();
		int n3 = cp.c.urpp();
		int num =
				(n3 << 24) +
				(n2 << 16) +
				(n1 << 8) +
				n0;

		//if ((n3 > 127))			num = num | (~0xFFFFFFFFL);

		return num;
	}

	static int ReadCommands(CurPos cp, String Name) {
		//STARTFUNC("ReadCommands");
		// LOG << "Name = " << Name << ENDLINE;

		//long Cmd = ExMacro.NewCommand(Name);

		Obj obj = GetObj(cp);
		if ( obj.type != CF_INT) return -1;

		int cmdno = GetNum(cp);
		/*if (cmdno != (Cmd | CMD_EXT)) {
			System.err.printf("Bad Command map '%s' . %d != %d\n", Name, Cmd | CMD_EXT, cmdno);
			return -1;
		}*/

		int Cmd = cmdno;
		ExMacro.NewCommand(Name, Cmd);

		while(true) 
		{
			obj = GetObj(cp); 
			if(obj.type == 0xFF)
				break;

			switch (obj.type) {
			case CF_COMMAND:
			{
				//              String s;
				//int cnt;
				//long ign;
				//long cmd;

				//                if ((s = GetCharStr(cp, len)) == 0) return -1;
				int cmd = GetNum(cp);
				obj = GetObj(cp); 
				if (obj.type != CF_INT) return -1;
				int cnt = GetNum(cp);
				obj = GetObj(cp); 
				if (obj.type != CF_INT) return -1;
				int ign = GetNum(cp);

				//                if (cmd != CmdNum(s)) {
				//                    fprintf(stderr, "Bad Command Id: %s . %d\n", s, cmd);
				//                    return -1;
				//                }

				/* TODO AddCommand
				if (AddCommand(Cmd, cmd, cnt, ign) == 0) {
					if (Name == null || !Name.equals("xx")) {
						System.err.printf("Bad Command Id: %ld\n", cmd);
						return -1;
					}
				}
				 */
				ExMacro.AddCommand(Cmd, cmd, cnt, ign);
			}
			break;
			case CF_STRING:
			{
				String s = GetCharStr(cp, obj.len);
				if (ExMacro.AddString(Cmd, s) == 0) return -1;
			}
			break;
			case CF_INT:
			{
				long num = GetNum(cp);
				if (ExMacro.AddNumber(Cmd, num) == 0) return -1;
			}
			break;
			case CF_VARIABLE:
			{
				int num = GetNum(cp);

				if (ExMacro.AddVariable(Cmd, num) == 0) return -1;
			}
			break;
			case CF_CONCAT:
				if (ExMacro.AddConcat(Cmd) == 0) return -1;
				break;
			case CF_END:
				return (int) Cmd;
			default:
				System.err.printf("unk obj type %d in ReadCommands\n", obj.type);
				cp.c.shift(obj.len);
				break;
				// TODO return -1;
			}
		}
		return -1;
	}

	static int ReadMenu(CurPos cp, String MenuName) {
		Obj obj;

		int menu = -1, item = -1;

		menu = UpMenu.NewMenu(MenuName);

		while(true) 
		{
			obj = GetObj(cp); 
			if(obj.type == 0xFF)
				break;

			switch (obj.type) {
			case CF_ITEM:
			{
				if (obj.len == 0) {
					item = UpMenu.NewItem(menu, null);
				} else {
					String s = GetCharStr(cp, obj.len);
					int Cmd;
					if (s == null) return -1;
					item = UpMenu.NewItem(menu, s);
					obj = GetObj(cp);
					if (obj.type != CF_MENUSUB) return -1;
					if ((Cmd = ReadCommands(cp, null)) == -1) return -1;
					UpMenu.Menus[menu].Items.get(item).Cmd = Cmd + 65536;
				}
			}
			break;
			case CF_SUBMENU:
			{
				String s = GetCharStr(cp, obj.len);
				String w;

				obj = GetObj(cp);
				if (obj.type != CF_STRING) return -1;
				if ((w = GetCharStr(cp, obj.len)) == null) return -1;
				item = UpMenu.NewSubMenu(menu, s, UpMenu.GetMenuId(w), SUBMENU_NORMAL);
			}
			break;

			case CF_SUBMENUCOND:
			{
				String s = GetCharStr(cp, obj.len);
				String w;

				obj = GetObj(cp);
				if (obj.type != CF_STRING) return -1;
				if ((w = GetCharStr(cp, obj.len)) == null) return -1;
				item = UpMenu.NewSubMenu(menu, s, UpMenu.GetMenuId(w), SUBMENU_CONDITIONAL);
			}
			break;

			case CF_END:
				return 0;
			default:
				System.err.printf("unk obj type %d in ReadMenu\n", obj.type);
				cp.c.shift(obj.len);
				break;
				// TODO return -1;
			}
		}
		return -1;
	}

	static int ReadColors(CurPos cp, String ObjName) {
		Obj obj;

		while(true) 
		{
			obj = GetObj(cp); 
			if(obj.type == 0xFF)
				break;

			switch (obj.type) {
			case CF_STRING:
			{
				///char cl[30];
				String sname = GetCharStr(cp, obj.len);
				String svalue;
				if (sname == null) return -1;

				obj = GetObj(cp);
				if (obj.type != CF_STRING) return -1;
				if ((svalue = GetCharStr(cp, obj.len)) == null) return -1;
				String cl = ObjName+"."+sname;
				// TODO if (SetColor(cl, svalue) == 0) return -1;
			}
			break;
			case CF_END:
				return 0;

			default:
				System.err.printf("unk obj type %d in ReadColors\n", obj.type);
				cp.c.shift(obj.len);
				break;
				// TODO return -1;
			}
		}
		return -1;
	}































	static int SetGlobalNumber(int what, int number) {
		//LOG << "What: " << what << " Number: " << number << ENDLINE;

		boolean b = ofInt(number);

		switch (what) {

		/*
	    case FLAG_C_Indent:          C_Indent = number; break;
	    case FLAG_C_BraceOfs:        C_BraceOfs = number; break;
	    case FLAG_C_CaseOfs:         C_CaseOfs = number; break;
	    case FLAG_C_CaseDelta:       C_CaseDelta = number; break;
	    case FLAG_C_ClassOfs:        C_ClassOfs = number; break;
	    case FLAG_C_ClassDelta:      C_ClassDelta = number; break;
	    case FLAG_C_ColonOfs:        C_ColonOfs = number; break;
	    case FLAG_C_CommentOfs:      C_CommentOfs = number; break;
	    case FLAG_C_CommentDelta:    C_CommentDelta = number; break;
	    case FLAG_C_FirstLevelIndent: C_FirstLevelIndent = number; break;
	    case FLAG_C_FirstLevelWidth: C_FirstLevelWidth = number; break;
	    case FLAG_C_Continuation:    C_Continuation = number; break;
	    case FLAG_C_ParenDelta:      C_ParenDelta = number; break;
	    case FLAG_FunctionUsesContinuation: FunctionUsesContinuation = number; break;

	    case FLAG_REXX_Indent:       REXX_Base_Indent = number; break;
	    case FLAG_REXX_Do_Offset:    REXX_Do_Offset = number; break;
		 */
		case FLAG_ScreenSizeX:       ScreenSizeX = number; break;
		case FLAG_ScreenSizeY:       ScreenSizeY = number; break;
		case FLAG_CursorInsertStart: CursorInsSize[0] = number; break;
		case FLAG_CursorInsertEnd:   CursorInsSize[1] = number; break;
		case FLAG_CursorOverStart:   CursorOverSize[0] = number; break;
		case FLAG_CursorOverEnd:     CursorOverSize[1] = number; break;
		case FLAG_SysClipboard:      SystemClipboard = number; break;
		case FLAG_OpenAfterClose:    OpenAfterClose = number; break;
		// case FLAG_ShowVScroll:       ShowVScroll = number; break; 
		//case FLAG_ShowHScroll:       ShowHScroll = number; break;
		case FLAG_ScrollBarWidth:    ScrollBarWidth = number; break;
		case FLAG_SelectPathname:    SelectPathname = number; break;
		//case FLAG_ShowMenuBar:       ShowMenuBar = number; break;
		//case FLAG_ShowToolBar:       ShowToolBar = number; break;
		case FLAG_KeepHistory:       KeepHistory = number; break;
		case FLAG_LoadDesktopOnEntry: LoadDesktopOnEntry = number; break;
		case FLAG_SaveDesktopOnExit: SaveDesktopOnExit = b; break;
		case FLAG_KeepMessages:      KeepMessages = number; break;
		case FLAG_ScrollBorderX:     ScrollBorderX = number; break;
		case FLAG_ScrollBorderY:     ScrollBorderY = number; break;
		case FLAG_ScrollJumpX:       ScrollJumpX = number; break;
		case FLAG_ScrollJumpY:       ScrollJumpY = number; break;
		case FLAG_GUIDialogs:        GUIDialogs = number; break;
		case FLAG_PMDisableAccel:    PMDisableAccel = number; break;
		case FLAG_SevenBit:          SevenBit = number; break;
		case FLAG_WeirdScroll:       WeirdScroll = b; break;
		case FLAG_LoadDesktopMode:   LoadDesktopMode = number; break;
		case FLAG_IgnoreBufferList:  IgnoreBufferList = b; break;
		case FLAG_ReassignModelIds:  ReassignModelIds = number; break;
		default:
			System.err.printf("Unknown global number: %d\n", what);
			return 0;
			//return -1;
		}
		return 0;
	}

	public static boolean ofInt(int v) { return v != 0; }

	static int SetGlobalString(int what, String string) {
		//LOG << "What: " << what << " String: " << string << ENDLINE;

		switch (what) {
		case FLAG_DefaultModeName: DefaultModeName = string; break;
		// TODO case FLAG_CompletionFilter: if ((CompletionFilter = RxCompile(string)) == NULL) return -1; break;
		case FLAG_PrintDevice: PrintDevice = string; break;
		case FLAG_CompileCommand: CompileCommand = string; break;
		case FLAG_WindowFont: WindowFont = string; break;
		case FLAG_HelpCommand: HelpCommand = string; break;
		// TODO case FLAG_GUICharacters: AppendGUICharacters (string); break;
		case FLAG_CvsCommand: CvsCommand = string; break;
		case FLAG_CvsLogMode: CvsLogMode = string; break;
		default:
			System.err.printf("Unknown global string: %d = '%s'\n", what, string);
			return 0;
			//return -1;
		}
		return 0;
	}
















	static int ReadEventMap(CurPos cp, EEventMap Map, String  MapName) {
		Obj obj;

		while(true) 
		{
			obj = GetObj(cp); 
			if(obj.type == 0xFF)
				break;

			switch (obj.type) {
			case CF_KEY:
			{
				EKey Key;
				String s;
				int Cmd;

				if ((s = GetCharStr(cp, obj.len)) == null) return -1;
				if ((Key = SetKey(Map, s)) == null) return -1;
				
				obj = GetObj(cp);
				if (obj.type != CF_KEYSUB) return -1;
				if ((Cmd = ReadCommands(cp, null)) == -1) return -1;
				Key.Cmd = Cmd;
			}
			break;

			//#ifdef CONFIG_ABBREV
			case CF_ABBREV:
			{
				EAbbrev Ab;
				String s;
				String x;
				int Cmd;

				if ((s = GetCharStr(cp, obj.len)) == null) return -1;
				obj = GetObj(cp);
				if (obj.type == CF_KEYSUB) {
					if ((Cmd = ReadCommands(cp, null)) == -1) return -1;
					Ab = new EAbbrev(s, Cmd);
				} else if (obj.type == CF_STRING) {
					x = GetCharStr(cp, obj.len);
					Ab = new EAbbrev(s, x);
				} else
					return -1;
				if (Ab != null) {
					Map.AddAbbrev(Ab);
				}
			}
			break;
			//#endif

			case CF_SETVAR:
			{
				int what = GetNum(cp);

				obj = GetObj(cp);
				switch (obj.type) {
				case CF_STRING:
				{
					String val = GetCharStr(cp, obj.len);
					if (obj.len == 0) return -1;
					if (SetEventString(Map, what, val) != 0) return -1;
				}
				break;
				/*                case CF_INT:
	                     {
	                     long num;

	                     if (GetNum(cp, num) == 0) return -1;
	                     if (SetModeNumber(Mode, what, num) != 0) return -1;
	                     }
	                     break;*/
				default:
					return -1;
				}
			}
			break;
			case CF_END:
				return 0;
			default:
				return -1;
			}
		}
		return -1;
	}

	//#ifdef CONFIG_SYNTAX_HILIT
	int ReadColorize(CurPos cp, EColorize Colorize, String ModeName) {
		Obj obj;
		short len;

		long LastState = -1;

		while(true) 
		{
			obj = GetObj(cp); 
			if(obj.type == 0xFF)
				break;

			switch (obj.type) {
			case CF_COLOR:
				if (ReadHilitColors(cp, Colorize, ModeName) == -1) return -1;
				break;

			case CF_KEYWORD:
			{
				String colorstr;

				if ((colorstr = GetCharStr(cp, obj.len)) == null) return -1;

				int Col;
				int ColBg, ColFg;

				if (sscanf(colorstr, "%1X %1X", ColFg, ColBg) != 2)
					return 0;

				Col = ColFg | (ColBg << 4);

				int color = ChColor(Col);
				if (ReadKeywords(cp, Colorize.Keywords, color) == -1) return -1;
			}
			break;

			case CF_HSTATE:
			{
				if (Colorize.hm == null)
					Colorize.hm = new HMachine();

				assert(Colorize.hm != null);

				int stateno = GetNum(cp);

				assert(stateno == LastState + 1);

				obj = GetObj(cp);
				assert(obj.type == CF_INT);

				int color = GetNum(cp);

				HState newState;

				newState.InitState();

				newState.color = color;

				Colorize.hm.AddState(newState);
				LastState = stateno;
			}
			break;

			case CF_HTRANS:
			{
				HTrans newTrans;
				//long nextState;
				//long matchFlags;
				String match;
				//long color;

				int nextState = GetNum(cp);
				
				obj = GetObj(cp);
				assert(obj.type == CF_INT);
				int matchFlags = GetNum(cp);
				
				obj = GetObj(cp);
				assert(obj.type == CF_INT);
				int color = GetNum(cp);
				
				obj = GetObj(cp);
				assert(obj.type == CF_STRING);
				if ((match = GetCharStr(cp, obj.len)) == null)
					return -1;

				newTrans.InitTrans();

				newTrans.matchFlags = matchFlags;
				newTrans.nextState = nextState;
				newTrans.color = color;

				if( 0 != (newTrans.matchFlags & MATCH_SET) ||
						0 != (newTrans.matchFlags & MATCH_NOTSET))
				{
					//newTrans.matchLen = 1;
					//newTrans.match = (String )malloc(256/8);
					//assert(newTrans.match != NULL);
					SetWordChars(newTrans.match, match);
				} else {
					newTrans.match = match;
					//newTrans.matchLen = strlen(match);
				}

				Colorize.hm.AddTrans(newTrans);
			}
			break;

			case CF_HWTYPE:
			{
				String wordChars;

				obj = GetObj(cp);
				assert(obj.type == CF_INT);
				int nextKwdMatchedState = GetNum(cp);

				obj = GetObj(cp);
				assert(obj.type == CF_INT);
				int nextKwdNotMatchedState = GetNum(cp);

				obj = GetObj(cp);
				assert(obj.type == CF_INT);
				int nextKwdNoCharState = GetNum(cp);

				obj = GetObj(cp);
				assert(obj.type == CF_INT);
				int options = GetNum(cp);

				obj = GetObj(cp);
				assert(obj.type == CF_STRING);
				if ((wordChars = GetCharStr(cp, obj.len)) == null)
					return -1;

				Colorize.hm.LastState().options = options;
				Colorize.hm.LastState().nextKwdMatchedState = nextKwdMatchedState;
				Colorize.hm.LastState().nextKwdNotMatchedState = nextKwdNotMatchedState;
				Colorize.hm.LastState().nextKwdNoCharState = nextKwdNoCharState;

				if (wordChars != null && !wordChars.isBlank()) {
					//Colorize.hm.LastState().wordChars = (String )malloc(256/8);
					//assert(Colorize.hm.LastState().wordChars != NULL);
					SetWordChars(Colorize.hm.LastState().wordChars, wordChars);
				}
			}
			break;

			case CF_HWORDS:
			{
				String colorstr;
				int color;

				if ((colorstr = GetCharStr(cp, len)) == null) return -1;

				color = hcPlain_Keyword;

				if (!colorstr.equals("-")) {
					String Value = colorstr;
					int Col;

					if (Value.charAt(0) == '-') {
						Value++;
						if (sscanf(Value, "%1X", Col) != 1) return -1;
						Col |= (hcPlain_Background & 0xF0);
					} else if (Value[1] == '-') {
						if (sscanf(Value, "%1X", Col) != 1) return -1;
						Col <<= 4;
						Col |= (hcPlain_Background & 0x0F);
					} else {
						if (sscanf(Value, "%2X", Col) != 1) return -1;
					}
					color = Col;
				}
				if (ReadKeywords(cp, Colorize.hm.LastState().keywords, color) == -1) return -1;
			}
			break;

			case CF_SETVAR:
			{
				int what = GetNum(cp);
				
				obj = GetObj(cp);
				switch (obj.type) {
				case CF_STRING:
				{
					String val = GetCharStr(cp, obj.len);
					if (len == 0) return -1;
					if (SetColorizeString(Colorize, what, val) != 0) return -1;
				}
				break;
				/*                case CF_INT:
	                     {
	                     long num;

	                     if (GetNum(cp, num) == 0) return -1;
	                     if (SetModeNumber(Mode, what, num) != 0) return -1;
	                     }
	                     break;*/
				default:
					return -1;
				}
			}
			break;
			case CF_END:
				return 0;
			default:
				return -1;
			}
		}
		return -1;
	}
	//#endif

	int ReadMode(CurPos cp, EMode Mode, String  ModeName) {
		Obj obj;

		while (true) 
		{
			obj = GetObj(cp);
			if(obj.type == 0xFF) break;
			
			switch (obj.type) {
			case CF_SETVAR:
			{

				int what = GetNum(cp);
				
				obj = GetObj(cp);
				switch (obj.type) {
				case CF_STRING:
				{
					String val = GetCharStr(cp, obj.len);
					if (obj.len == 0) return -1;
					if (SetModeString(Mode, what, val) != 0) return -1;
				}
				break;
				case CF_INT:
				{
					int num = GetNum(cp);
					if (SetModeNumber(Mode, what, num) != 0) return -1;
				}
				break;
				default:
					return -1;
				}
			}
			break;
			
			case CF_END:
				return 0;
				
			default:
				return -1;
			}
		}
		return -1;
	}




	
	
	
	
	
	int SetModeNumber(EMode mode, int what, int number) {
	    int j = what;

	    if (j == BFI_LeftMargin || j == BFI_RightMargin) number--;
	    mode.Flags.num[j] = number;
	    return 0;
	}

	int SetModeString(EMode mode, int what, String string) {
	    int j = what;

	//#ifdef CONFIG_SYNTAX_HILIT
	    if (j == BFI_Colorizer) {
	        // TODO mode.fColorize = FindColorizer(string);
	    } else
	//#endif
	        if (j == BFI_EventMap) {
	            mode.fEventMap = EEventMap.FindEventMap(string);
	        } else if (j == BFI_IndentMode) {
	            mode.Flags.num[j] = GetIndentMode(string);
	        } else if (j == BFS_WordChars) {
	            SetWordChars(mode.Flags.WordChars, string);
	        } else if (j == BFS_CapitalChars) {
	            SetWordChars(mode.Flags.CapitalChars, string);
	        } else if (j == BFS_FileNameRx) {
	            mode.MatchName = string;
	            mode.MatchNameRx = null; // TODO RxCompile(string);
	        } else if (j == BFS_FirstLineRx) {
	            mode.MatchLine = string;
	            mode.MatchLineRx = null; // TODO RxCompile(string);
	        } else {
	            mode.Flags.str[j & 0xFF] = string;
	        }
	    return 0;
	}

	
	
	
	static void SetWordChars(char [] w, String s) {
		//String p;
		//memset((void *)w, 0, 32);
		w[0] = w[1] = w[2] = w[3] = 0;

		int p = 0;
		while(p < s.length()) {
			if (s.charAt(p) == '\\') 
			{
				p++;
				if (p >= s.length()) return;
			} 
			else if (s.charAt(p+1) == '-') 
			{
				if (p+2 >= s.length()) return;
				
				for (int i = s.charAt(p); i < s.charAt(p+2); i++)
					ModeDefs.WSETBIT(w, i, 1);
				p += 2;
			}
			ModeDefs.WSETBIT(w, s.charAt(p), 1);
			p++;
		}
	}
	

	
	
	
	
	static EKey SetKey(EEventMap aMap, String aKey) 
	{
	    EKeyMap []map = aMap.KeyMap, pm, parent = null;
	    EKey k;
	    char [] Key  = new char[256];
	    String p, d;
	    EEventMap xm = aMap;

	    // printf("Setting key %s\n", Key);
	    strcpy(Key, aKey);

	    // if mode has parent, get parent keymap
	    while (xm && xm.Parent && (parent == 0)) {
	        parent = xm.Parent.KeyMap;
	        //        printf("%s : %s : %d\n", xm.fName, xm.fParent.fName, parent);
	        xm = xm.Parent;
	    }

	    d = p = Key;
	    while (d) {
	        // parse key combination
	        p = d;
	        d = strchr(p, '_');
	        if (d) {
	            if (d[1] == 0 || d[1] == '_')
	                d++;

	            if (*d == 0)
	                d = 0;
	            else {
	                *d = 0;
	                d++;
	            }
	        }

	        // if lastkey

	        if (d == 0) {
	            k = new EKey(p);
	            if (*map) {
	                (*map).AddKey(k);
	            } else {
	                *map = new EKeyMap();
	                (*map).fParent = parent;
	                (*map).AddKey(k);
	            }
	            return k;

	        } else {
	            // if introductory key

	            if (*map == 0) { // first key in mode, create map
	                //                printf("new map key = %s, parent %d\n", p, parent);
	                k = new EKey(p, 0);
	                *map = new EKeyMap();
	                (*map).fParent = parent;
	                (*map).AddKey(k);
	            } else {
	                KeySel ks;

	                ParseKey(p, ks);
	                if ((k = (*map).FindKey(ks.Key)) == 0) { // check if key exists
	                    // add it if not
	                    k = new EKey(p, 0);
	                    (*map).AddKey(k);
	                }
	            }
	            map = &k.fKeyMap; // set current map to key's map

	            // get parent keymap
	            pm = parent;
	            parent = 0;
	            //            printf("Searching %s\n", p);
	            while (pm) { // while exists
	                KeySel ks;
	                EKey pk;

	                ParseKey(p, ks);
	                if ((pk = pm.FindKey(ks.Key)) != 0) { // if key exists, find parent of it
	                    parent = pk.fKeyMap;
	                    //                    printf("Key found %d\n", parent);
	                    break;
	                }
	                pm = pm.fParent; // otherwise find parent of current keymap
	            }
	        }
	    }
	    return 0;
	}
	
	
	
	
}
