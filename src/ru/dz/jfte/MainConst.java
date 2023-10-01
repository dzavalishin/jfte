package ru.dz.jfte;

public interface MainConst 
{
	public static final String PROGRAM = "jfte"; 
	public static final String VERSION = "0.1"; 
	public static final String COPYRIGHT = 
			"\nOriginal C code copyright (c) 1994-1998 Marko Macek\n"
		   +"Java version copyright (c) 2022-2023 Dmitry Zavalishin"; 
	
	public static final String usage =
			"Usage: " +PROGRAM+ " [-?] [-h] [--help] [-CDHTmlrt] files...\n"+
			           "Version: " +VERSION+ " " +COPYRIGHT+ "\n"+
			           "   You may distribute under the terms of either the GNU General Public\n"+
			           "   License or the Artistic License, as specified in the README file.\n"+
			           "\n"+
			           "Options:\n"+
			           "  --                End of options, only files remain.\n"+
			           "  -+                Next option is file.\n"+
			           "  -? -h --help      Display usage.\n"+
			           "  -!                Ignore config file, use builtin defaults (also -c).\n"+
			           "  -C[<.cnf>]        Use specified configuration file (no arg=builtin).\n"+
			/*#ifdef CONFIG_DESKTOP
			           "  -D[<.dsk>]        Load/Save desktop from <.dsk> file (no arg=disable desktop).\n"
			#endif
			#ifdef CONFIG_HISTORY
			           "  -H[<.his>]        Load/Save history from <.his> file (no arg=disable history).\n"
			#endif */
			           "  -m[<mode>]        Override mode for remaining files (no arg=no override).\n"+
			           "  -l<line>[,<col>]  Go to line (and column) in next file.\n"+
			           "  -r                Open next file as read-only.\n"
			/*#ifdef CONFIG_TAGS
			           "  -T[<tagfile>]     Load tags file at startup.\n"
			           "  -t<tag>           Locate specified tag.\n"
			#endif */
//			           "       -p        Load files into already running FTE.\n"
			;

	/* default locations for the configuration files */
	public static final String Unix_RCPaths[] = {
	    "/usr/local/etc/fte/system.fterc",
	    "/etc/fte/system.fterc",
	    "/usr/X11R6/lib/X11/fte/system.fterc",
	};
	
	// TODO ver 
	public static final long VERNUM = 0x00490401L; 

	public static final String HISTORY_VER = "jFTE History 1\n";
	public static final String HISTORY_NAME = ".fte-history";
	
}
