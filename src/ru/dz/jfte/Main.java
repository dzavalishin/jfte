package ru.dz.jfte;

import java.nio.file.Path;
import java.io.File;

public class Main implements MainConst
{
	static String ConfigFileName = null;

	static void Usage() {
	    System.out.print(usage);
	}

	/*
	 * findPathExt() returns a ^ to the suffix in a file name string. If the
	 * name contains a suffix, the pointer ^ts to the suffix' dot character,
	 * if the name has no suffix the pointer points to the NUL terminator of
	 * the file name string.
	 * .lib: CBASE.LIB
	 * /
	static char *findPathExt(char *filename) {
	    char *p, *sps;

	    for (p = filename, sps = NULL; *p; p++) {
	        if (ISSLASH(*p))
	            sps = NULL;
	        if (*p == '.')
	            sps = p;
	    }
	    if (sps == NULL)
	        sps = p;
	    return sps;
	}
	*/



	static int GetConfigFileName(String [] argv, String [] ConfigFileName) {
	    String CfgName = "";

	    if (ConfigFileName[0] == null) {
	/*#if defined(UNIX)
	        // ? use ./.fterc if by current user ?
	        ExpandPath("~/.fterc", CfgName);
	#elif defined(OS2) || defined(NT) */
	        String home = "";
	        String ph;
	//#if defined(OS2)
	        ph = getenv("HOME");
	        if (ph!=null) home = ph;
	//#endif
	//#if defined(NT)
	        ph = getenv("HOMEDRIVE");
	        if (ph!=null) home = ph;
	        ph = getenv("HOMEPATH");
	        if (ph!=null) home += ph;
	//#endif
	        if (!home.isBlank()) {
	            CfgName = home;
	            Console.Slash(CfgName, 1);
	            CfgName += "fte.cnf";
	        }

	        if (!home.isBlank() || Console.access(CfgName, 0)) {
	            CfgName = argv[0];
	            //strcpy(findPathExt(CfgName), ".cnf");
	            //Path p = Path.of(CfgName);
	            //File f = new File(CfgName);
	            // TODO ext
	            CfgName += ".cnf";
	        }
	

	        ConfigFileName[0] = CfgName;
	    }
	    if (!Console.access(ConfigFileName[0], 0))
	        return 1;

	/* #if defined(UNIX)
	    for (unsigned int i = 0; i < sizeof(Unix_RCPaths)/sizeof(Unix_RCPaths[0]); i++) {
	        if (access(Unix_RCPaths[i], 0) == 0) {
	            strcpy(ConfigFileName, Unix_RCPaths[i]);
	            return 1;
	        }
	    }
	#endif */
	    return 0;
	}

	private static String getenv(String string) {		
		return System.getenv(string);
	}

	
	static int CmdLoadConfiguration(String [] argv) {
		/* TODO CmdLoadConfiguration
	    int ign = 0;
	    int QuoteAll = 0, QuoteNext = 0;
	    int haveConfig = 0;
	    int Arg;
	    int argc = argv.length;

	    for (Arg = 1; Arg < argc; Arg++) {
	        if (!QuoteAll && !QuoteNext && (argv[Arg][0] == '-')) {
	            if (argv[Arg][1] == '-') {
	                if (strcmp(argv[Arg], "--help") == 0) {
	                    Usage();
	                    return 0;
	                }
	                int debug_clean = strcmp(argv[Arg], "--debugclean") == 0;
	                if (debug_clean || strcmp(argv[Arg], "--debug") == 0) {
	#ifndef FTE_NO_LOGGING
	                    char path[MAXPATH];
	#ifdef UNIX
	                    ExpandPath("~/.fte", path);
	#else
	                    JustDirectory(argv[0], path);
	#endif
	                    Slash(path,1);
	                    strcat(path, "fte.log");
	                    if (debug_clean) unlink(path);

	                    globalLog.SetLogFile(path);
	                    printf("Trace Log in: %s\n", path);
	#else
	                    printf("--debug, --debugclean disabled\n");
	#endif
	                }
	                else
	                    QuoteAll = 1;
	            } else if (argv[Arg][1] == '!') {
	                ign = 1;
	            } else if (argv[Arg][1] == '+') {
	                QuoteNext = 1;
	            } else if (argv[Arg][1] == '?' || argv[Arg][1] == 'h') {
	                Usage();
	                return 0;
	            } else if (argv[Arg][1] == 'c' || argv[Arg][1] == 'C') {
	                if (argv[Arg][2])
	                {
	                    ExpandPath(argv[Arg] + 2, ConfigFileName);
	                    haveConfig = 1;
	                }
	                else
	                    ign = 1;
	            }
	        }
	    }
	    if (!haveConfig && GetConfigFileName(argc, argv, ConfigFileName) == 0) {
	        // should we default to internal
	#ifdef DEFAULT_INTERNAL_CONFIG
	       ign = 1;
	#endif
	    }

	    if (ign) {
	        if (UseDefaultConfig() == -1)
	            Console.DieError(1, "Error in internal configuration??? FATAL!");
	    } else {
	        if (LoadConfig(argc, argv, ConfigFileName) == -1)
	            Console.DieError(1,
	                     "Failed to load configuration file '%s'.\n"
	                     "Use '-C' option.", ConfigFileName);
	    }
	    for (Arg = 1; Arg < argc; Arg++) {
	        if (!QuoteAll && !QuoteNext && (argv[Arg][0] == '-')) {
	            if (argv[Arg][1] == '-' && argv[Arg][2] == '\0') {
	                QuoteAll = 1;
	            } else if (argv[Arg][1] == '+') {
	                QuoteNext = 1;
	#ifdef CONFIG_DESKTOP
	            } else if (argv[Arg][1] == 'D') {
	                ExpandPath(argv[Arg] + 2, DesktopFileName);
	                if (IsDirectory(DesktopFileName)) {
	                    Slash(DesktopFileName, 1);
	                    strcat(DesktopFileName, DESKTOP_NAME);
	                }
	                if (DesktopFileName[0] == 0) {
	                    LoadDesktopOnEntry = 0;
	                    SaveDesktopOnExit = 0;
	                } else {
	                    LoadDesktopOnEntry = 1;
	                }
	#endif
	#ifdef CONFIG_HISTORY
	            } else if (argv[Arg][1] == 'H') {
	                strcpy(HistoryFileName, argv[Arg] + 2);
	                if (HistoryFileName[0] == 0) {
	                    KeepHistory = 0;
	                } else {
	                    KeepHistory = 1;
	                }
	#endif
	            }
	        } else {
	            if (LoadDesktopOnEntry == 2) {
	                LoadDesktopOnEntry = 0;
	                SaveDesktopOnExit = 0;
	                DesktopFileName[0] = 0;
	            }
	        }
	    }
	    if (LoadDesktopOnEntry == 2)
	        LoadDesktopOnEntry = 1;
	    */
	    return 1;
	}

	
	
	static void main(String [] argv) {




	    if (CmdLoadConfiguration(argv) == 0)
	        return;

	    //STARTFUNC("main");

	    EGUI g = new EGUI( argv, Config.ScreenSizeX, Config.ScreenSizeY);
	    if (GUI.gui == null || g == null)
	        Console.DieError(1, "Failed to initialize display\n");

	    GUI.gui.Run();





	}
	
	
}
