package ru.dz.jfte;

import java.awt.Font;
import java.awt.FontFormatException;
import java.awt.GraphicsEnvironment;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.Charset;
import java.util.logging.Level;
import java.util.logging.LogManager;
import java.util.logging.Logger;

public class Main implements MainConst
{
	private static final Logger log = Logger.getLogger(Main.class.getName());
	private static final String LOGGING_PROPERTIES = "/logging.properties";

	public static final Charset charset = Charset.forName("US-ASCII"); // TODO charset - ASCII?
	public static final boolean DEBUG_EDITOR = false;

	
	static String ConfigFileName = null;
	static String HistoryFileName = "";
    static String DesktopFileName = "";

	
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
				CfgName = Console.Slash(CfgName, 1);
				CfgName += "fte.cnf";
			}

			if (!home.isBlank() || !Console.fileExist(CfgName, 0)) {
				//CfgName = argv[0];
				//strcpy(findPathExt(CfgName), ".cnf");
				//Path p = Path.of(CfgName);
				//File f = new File(CfgName);
				// TODO ext
				//CfgName += ".cnf";
				CfgName = "fte.cnf";
			}


			ConfigFileName[0] = CfgName;
		}
		if (!Console.fileExist(ConfigFileName[0], 0))
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

	static String getenv(String string) {		
		return System.getenv(string);
	}


	static int CmdLoadConfiguration(String [] argv) {
		//* TODO CmdLoadConfiguration
		boolean ign = false;
		boolean QuoteAll = false, QuoteNext = false;
		boolean haveConfig = false;

		int Arg;
		int argc = argv.length;

		for (Arg = 0; Arg < argc; Arg++) 
		{
			char aa1 = argv[Arg].charAt(1);

			if (!QuoteAll && !QuoteNext && (argv[Arg].charAt(0) == '-')) {
				if (aa1 == '-') {
					if (argv[Arg].equals("--help")) {
						Usage();
						return 0;
					}
					boolean debug_clean = argv[Arg].equals("--debugclean");
					if (debug_clean || argv[Arg].equals("--debug")) {
						String [] path = {""};
						/*#ifdef UNIX
	                    ExpandPath("~/.fte", path);
	#else */
						// TODO JustDirectory(argv[0], path);
						Console.ExpandPath(".", path);
						//#endif
						path[0] = Console.Slash(path[0],1);
						path[0] += "fte.log";
						if (debug_clean) Console.unlink(path[0]);

						// TODO globalLog.SetLogFile(path);
						//printf("Trace Log in: %s\n", path);
					}
					else
						QuoteAll = true;
				} else if (aa1 == '!') {
					ign = true;
				} else if (aa1 == '+') {
					QuoteNext = true;
				} else if (aa1 == '?' || aa1 == 'h') {
					Usage();
					return 0;
				} else if (aa1 == 'c' || aa1 == 'C') {
					/* TODO config file if (argv[Arg][2])
	                {
	                    Console.ExpandPath(argv[Arg] + 2, ConfigFileName);
	                    haveConfig = true;
	                }
	                else */
					ign = true;
				}
			}
		}

		if (!haveConfig)
		{
			String [] cfn = {null};
			if (GetConfigFileName(argv, cfn) == 0) 
				// should we default to internal
				ign = true;
			ConfigFileName = cfn[0];
		}

		/*if (ign) {
	        if (UseDefaultConfig() == -1)
	            Console.DieError(1, "Error in internal configuration??? FATAL!");
	    } else */{
	    	if (!Config.LoadConfig(ConfigFileName))
	    		Console.DieError(1,
	    				"Failed to load configuration file '%s'.\n"+
	    						"Use '-C' option.", ConfigFileName);

	    }
	    for (Arg = 0; Arg < argc; Arg++) 
	    {
	    	char aa1 = argv[Arg].charAt(1);

	    	if (!QuoteAll && !QuoteNext && (argv[Arg].charAt(0) == '-')) {
	    		if (aa1 == '-' && argv[Arg].length() == 2) {
	    			QuoteAll = true;
	    		} else if (aa1 == '+') {
	    			QuoteNext = true;
	            } else if (aa1 == 'D') {
	            	DesktopFileName = Console.expandPath(argv[Arg].substring(2));
	                if (Console.IsDirectory(DesktopFileName)) {
	                	DesktopFileName = Console.Slash(DesktopFileName, 1);
	                    DesktopFileName += DESKTOP_NAME;
	                }
	                if (DesktopFileName.isBlank()) {
	                    Config.LoadDesktopOnEntry = 0;
	                    Config.SaveDesktopOnExit = false;
	                } else {
	                	Config.LoadDesktopOnEntry = 1;
	                }

	            } else if (aa1 == 'H') {
	                HistoryFileName =  argv[Arg].substring(2);
	                    Config.KeepHistory = HistoryFileName.isBlank();
	    		}
	    	} else {
	    		if (Config.LoadDesktopOnEntry == 2) {
	    			Config.LoadDesktopOnEntry = 0;
	    			Config.SaveDesktopOnExit = false;
	    			DesktopFileName = "";
	    		}
	    	}
	    }
	    if (Config.LoadDesktopOnEntry == 2)
	    	Config.LoadDesktopOnEntry = 1;
	    //*/
	    return 1;
	}

	
	private static Font monoFont;
	
	public static Font getMonoFont() { return monoFont; }
	
	public static void main(String[] argv) throws FontFormatException, IOException  
	{
		final InputStream logMode = Main.class.getResourceAsStream(LOGGING_PROPERTIES);
		if(logMode != null)
			LogManager.getLogManager().readConfiguration(logMode);
		else
			System.err.println("Can't get "+LOGGING_PROPERTIES);

		log.info("Start");
		
		if (CmdLoadConfiguration(argv) == 0)
			return;

		var ge = GraphicsEnvironment.getLocalGraphicsEnvironment();
		monoFont = loadFont("JetBrainsMono-Medium.ttf", ge).deriveFont(16.0f);

		
		Console.start();
		//STARTFUNC("main");

		//EModel.ActiveModel[0] = null; // strange...
		
		EGUI g = new EGUI( argv, Config.ScreenSizeX, Config.ScreenSizeY);
		if (GUI.gui == null || g == null)
			Console.DieError(1, "Failed to initialize display\n");

		GUI.gui.Run();
	}

	private static Font loadFont(String fontName, GraphicsEnvironment ge) throws FontFormatException, IOException 
	{
		//try(InputStream rs = Main.class.getResourceAsStream("/fonts/"+fontName)) 
		try(InputStream rs = Main.class.getResourceAsStream("/"+fontName)) 
		{
			Font f = Font.createFont(Font.TRUETYPE_FONT, rs);
			ge.registerFont(f);
			
			//System.out.println("loaded "+f.getName());
			log.log(Level.FINE, "Got font "+fontName);
			return f;
		} catch(Throwable e)
		{
			log.log(Level.SEVERE, "Load font "+fontName, e);
			//return null;
			throw e;
		}
	}
	

}
