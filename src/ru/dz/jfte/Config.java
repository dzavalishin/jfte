package ru.dz.jfte;

public class Config 
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
	    const char *a;
	    const char *c;
	    const char *z;
	    int line;
	    String name; // filename
	};

	
	
	
	
	public static int UseDefaultConfig() {
		    CurPos cp;
		    int rc;

		    cp.name = "Internal Configuration";
		    cp.sz = sizeof(DefaultConfig);
		    cp.a = (char *)DefaultConfig;
		    cp.c = (char *)DefaultConfig + 2 * 4;
		    cp.z = cp.a + cp.sz;
		    cp.line = 1;

		    rc = ReadConfigFile(cp);

		    if (rc == -1)
		        DieError(1, "Error %s offset %d\n", cp.name, cp.c - cp.a);
		    return rc;
		}

	
	
	
	int ReadConfigFile(CurPos cp) {
	    char obj;
	    short len;

	    {
	        const char *p;

	        obj = GetObj(cp, len);
	        assert(obj == CF_STRING);
	        if ((p = GetCharStr(cp, len)) == 0)
	            return -1;

	        ConfigSourcePath = strdup(p);
	    }

	    while ((obj = GetObj(cp, len)) != 0xFF) {
	        switch (obj) {
	        case CF_SUB:
	            {
	                const char *CmdName = GetCharStr(cp, len);

	                if (ReadCommands(cp, CmdName) == -1) return -1;
	            }
	            break;
	        case CF_MENU:
	            {
	                const char *MenuName = GetCharStr(cp, len);

	                if (ReadMenu(cp, MenuName) == -1) return -1;
	            }
	            break;
	        case CF_EVENTMAP:
	            {
	                EEventMap *EventMap = 0;
	                const char *MapName = GetCharStr(cp, len);
	                const char *UpMap = 0;

	                if ((obj = GetObj(cp, len)) != CF_PARENT) return -1;
	                if (len > 0)
	                    if ((UpMap = GetCharStr(cp, len)) == 0) return -1;

	                // add new mode
	                if ((EventMap = FindEventMap(MapName)) == 0) {
	                    EEventMap *OrgMap = 0;

	                    if (strcmp(UpMap, "") != 0)
	                        OrgMap = FindEventMap(UpMap);
	                    EventMap = new EEventMap(MapName, OrgMap);
	                } else {
	                    if (EventMap->Parent == 0)
	                        EventMap->Parent = FindEventMap(UpMap);
	                }
	                if (ReadEventMap(cp, EventMap, MapName) == -1) return -1;
	            }
	            break;

	        case CF_COLORIZE:
	            {
	                EColorize *Mode = 0;
	                const char *ModeName = GetCharStr(cp, len);
	                const char *UpMode = 0;

	                if ((obj = GetObj(cp, len)) != CF_PARENT) return -1;
	                if (len > 0)
	                    if ((UpMode = GetCharStr(cp, len)) == 0) return -1;

	                // add new mode
	                if ((Mode = FindColorizer(ModeName)) == 0)
	                    Mode = new EColorize(ModeName, UpMode);
	                else {
	                    if (Mode->Parent == 0)
	                        Mode->Parent = FindColorizer(UpMode);
	                }
	                if (ReadColorize(cp, Mode, ModeName) == -1)
	                    return -1;
	            }
	            break;

	        case CF_MODE:
	            {
	                EMode *Mode = 0;
	                const char *ModeName = GetCharStr(cp, len);
	                const char *UpMode = 0;

	                if ((obj = GetObj(cp, len)) != CF_PARENT) return -1;
	                if (len > 0)
	                    if ((UpMode = GetCharStr(cp, len)) == 0) return -1;

	                // add new mode
	                if ((Mode = FindMode(ModeName)) == 0) {
	                    EMode *OrgMode = 0;
	                    EEventMap *Map;

	                    if (strcmp(UpMode, "") != 0)
	                        OrgMode = FindMode(UpMode);
	                    Map = FindEventMap(ModeName);
	                    if (Map == 0) {
	                        EEventMap *OrgMap = 0;

	                        if (strcmp(UpMode, "") != 0)
	                            OrgMap = FindEventMap(UpMode);
	                        Map = new EEventMap(ModeName, OrgMap);
	                    }
	                    Mode = new EMode(OrgMode, Map, ModeName);
	                    Mode->fNext = Modes;
	                    Modes = Mode;
	                } else {
	                    if (Mode->fParent == 0)
	                        Mode->fParent = FindMode(UpMode);
	                }
	                if (ReadMode(cp, Mode, ModeName) == -1)
	                    return -1;
	            }
	            break;
	        case CF_OBJECT:
	            {
	                const char *ObjName;

	                if ((ObjName = GetCharStr(cp, len)) == 0)
	                    return -1;
	                if (ReadObject(cp, ObjName) == -1)
	                    return -1;
	            }
	            break;
	        case CF_EOF:
	            return 0;
	        default:
	            return -1;
	        }
	    }
	    return -1;
	}

	
	
	
	
	
	
	
	
	
}
