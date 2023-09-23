package ru.dz.jfte;

import java.nio.file.Files;
import java.nio.file.Path;

import ru.dz.jfte.c.ArrayPtr;
import ru.dz.jfte.c.ByteArrayPtr;

public class Config implements ConfigDefs 
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




	boolean LoadConfig(String CfgFileName) {
		//STARTFUNC("LoadConfig");
		//LOG << "Config file: " << CfgFileName << ENDLINE;

		byte [] allCfg = Files.readAllBytes(Path.of(CfgFileName));

		long ln = (allCfg[3] << 24) + (allCfg[2] << 16) + (allCfg[1] << 8) + allCfg[0];

		if (ln != CONFIG_ID) {
			Console.DieError(0, "Bad .CNF signature");
			return false;
		}

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

		boolean rc = ReadConfigFile(cp);

		if (!rc) {
			Console.DieError(1, "Error %s offset %d\n", CfgFileName, cp.c - cp.a);
			return false;
		}

		return true;
	}



















	boolean ReadConfigFile(CurPos cp) {
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

				if ((obj = GetObj(cp, obj.len)) != CF_PARENT) return false;
				if (len > 0)
					if ((UpMap = GetCharStr(cp, len)) == null) return false;

				// add new mode
				if ((EventMap = FindEventMap(MapName)) == 0) {
					EEventMap OrgMap = 0;

					if (!UpMap.isBlank())
						OrgMap = FindEventMap(UpMap);
					EventMap = new EEventMap(MapName, OrgMap);
				} else {
					if (EventMap.Parent == null)
						EventMap.Parent = FindEventMap(UpMap);
				}
				if (ReadEventMap(cp, EventMap, MapName) == -1) return false;
			}
			break;

			case CF_COLORIZE:
			{
				EColorize Mode = null;
				String ModeName = GetCharStr(cp, obj.len);
				String UpMode = null;

				if ((obj = GetObj(cp, obj.len)) != CF_PARENT) return false;
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
				EMode Mode = 0;
				String ModeName = GetCharStr(cp, obj.len);
				String UpMode = 0;

				if ((obj = GetObj(cp, obj.len)) != CF_PARENT) return false;
				if (obj.len > 0)
					if ((UpMode = GetCharStr(cp, obj.len)) == 0) return false;

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
				return false;
			}
		}
		return false;
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

	String GetCharStr(CurPos cp, int len) {
		//STARTFUNC("GetCharStr");
		// // LOG << "Length: " << len << ENDLINE;

		/*
	    String p = cp.c;
	    if (cp.c + len > cp.z)
	    {
	        // LOG << "End of config file in GetCharStr" << ENDLINE;
	        ENDFUNCRC(0);
	    }
	    cp.c += len;
	    ENDFUNCRC(p);
		 */
		return cp.c.getLenAsString(len);
	}

	long GetNum(CurPos cp) {
		int n0 = cp.c.urpp();
		int n1 = cp.c.urpp();
		int n2 = cp.c.urpp();
		int n3 = cp.c.urpp();
		long num =
				(n3 << 24) +
				(n2 << 16) +
				(n1 << 8) +
				n0;

		if ((n[3] > 127))
			num = num | (~0xFFFFFFFFUL);

		return num;
	}

	int ReadCommands(CurPos cp, String Name) {
		//STARTFUNC("ReadCommands");
		// LOG << "Name = " << Name << ENDLINE;

		Obj obj;
		int len;
		long Cmd = NewCommand(Name);
		long cmdno;

		if (GetObj(cp, len) != CF_INT) ENDFUNCRC(-1);
		if (GetNum(cp, cmdno) == 0) ENDFUNCRC(-1);
		if (cmdno != (Cmd | CMD_EXT)) {
			fprintf(stderr, "Bad Command map %s . %ld != %ld\n", Name, Cmd, cmdno);
			ENDFUNCRC(-1);
		}

		while(true) 
		{
			obj = GetObj(cp); 
			if(obj.type == 0xFF)
				break;

			switch (obj) {
			case CF_COMMAND:
			{
				//              String s;
				long cnt;
				long ign;
				long cmd;

				//                if ((s = GetCharStr(cp, len)) == 0) return -1;
				if (GetNum(cp, cmd) == 0) ENDFUNCRC(-1);
				if (GetObj(cp, len) != CF_INT) ENDFUNCRC(-1);
				if (GetNum(cp, cnt) == 0) ENDFUNCRC(-1);
				if (GetObj(cp, len) != CF_INT) ENDFUNCRC(-1);
				if (GetNum(cp, ign) == 0) ENDFUNCRC(-1);

				//                if (cmd != CmdNum(s)) {
				//                    fprintf(stderr, "Bad Command Id: %s . %d\n", s, cmd);
				//                    return -1;
				//                }

				if (AddCommand(Cmd, cmd, cnt, ign) == 0) {
					if (Name == 0 || strcmp(Name, "xx") != 0) {
						fprintf(stderr, "Bad Command Id: %ld\n", cmd);
						ENDFUNCRC(-1);
					}
				}
			}
			break;
			case CF_STRING:
			{
				String s = GetCharStr(cp, len);

				//if (s == 0) ENDFUNCRC(-1);
				if (AddString(Cmd, s) == 0) ENDFUNCRC(-1);
			}
			break;
			case CF_INT:
			{
				long num = GetNum(cp);
				if (AddNumber(Cmd, num) == 0) ENDFUNCRC(-1);
			}
			break;
			case CF_VARIABLE:
			{
				long num = GetNum(cp);

				if (AddVariable(Cmd, num) == 0) ENDFUNCRC(-1);
			}
			break;
			case CF_CONCAT:
				if (AddConcat(Cmd) == 0) ENDFUNCRC(-1);
				break;
			case CF_END:
				ENDFUNCRC(Cmd);
			default:
				ENDFUNCRC(-1);
			}
		}
		ENDFUNCRC(-1);
	}

	int ReadMenu(CurPos cp, String MenuName) {
		Obj obj;

		int menu = -1, item = -1;

		menu = NewMenu(MenuName);

		while(true) 
		{
			obj = GetObj(cp); 
			if(obj.type == 0xFF)
				break;

			switch (obj) {
			case CF_ITEM:
			{
				if (len == 0) {
					item = NewItem(menu, 0);
				} else {
					String s = GetCharStr(cp, len);
					int Cmd;
					if (s == 0) return -1;
					item = NewItem(menu, s);
					if ((obj = GetObj(cp, len)) != CF_MENUSUB) return -1;
					if ((Cmd = ReadCommands(cp, 0)) == -1) return -1;
					Menus[menu].Items[item].Cmd = Cmd + 65536;
				}
			}
			break;
			case CF_SUBMENU:
			{
				String s = GetCharStr(cp, len);
				String w;

				if ((obj = GetObj(cp, len)) != CF_STRING) return -1;
				if ((w = GetCharStr(cp, len)) == 0) return -1;
				item = NewSubMenu(menu, s, GetMenuId(w), SUBMENU_NORMAL);
			}
			break;

			case CF_SUBMENUCOND:
			{
				String s = GetCharStr(cp, len);
				String w;

				if ((obj = GetObj(cp, len)) != CF_STRING) return -1;
				if ((w = GetCharStr(cp, len)) == 0) return -1;
				item = NewSubMenu(menu, s, GetMenuId(w), SUBMENU_CONDITIONAL);
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

	int ReadColors(CurPos cp, String ObjName) {
		Obj obj;

		while(true) 
		{
			obj = GetObj(cp); 
			if(obj.type == 0xFF)
				break;

			switch (obj) {
			case CF_STRING:
			{
				///char cl[30];
				String sname = GetCharStr(cp, len);
				String svalue;
				if (sname == 0) return -1;
				if ((obj = GetObj(cp, len)) != CF_STRING) return -1;
				if ((svalue = GetCharStr(cp, len)) == 0) return -1;
				strcpy(cl, ObjName);
				strcat(cl, ".");
				strcat(cl, sname);
				if (SetColor(cl, svalue) == 0) return -1;
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















}
