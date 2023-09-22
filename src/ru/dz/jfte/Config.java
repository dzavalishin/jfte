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
	static boolean LoadDesktopOnEntry = false;
	static int SaveDesktopOnExit = 0;
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

}
