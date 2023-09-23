package ru.dz.jfte.config;

import ru.dz.jfte.ModeDefs;

public interface ConfigCompilerDefs extends ModeDefs 
{
	static final int  P_EOF         =0 ; // end of file
	static final int  P_SYNTAX      =1 ; // unknown
	static final int  P_WORD        =2 ; // a-zA-Z_
	static final int  P_NUMBER      =3 ; // 0-9
	static final int  P_STRING      =4 ; // "'`
	static final int  P_ASSIGN      =5 ; // =
	static final int  P_EOS         =6 ; // ;
	static final int  P_KEYSPEC     =7 ; // []
	static final int  P_OPENBRACE   =8 ; // {
	static final int  P_CLOSEBRACE  =9 ; // }
	static final int  P_COLON      =10 ; // :
	static final int  P_COMMA      =11 ; // ,
	static final int  P_QUEST      =12 ; //
	static final int  P_VARIABLE   =13 ; // $
	static final int  P_DOT        =14 ; // . (concat)

	static final int  K_UNKNOWN     =0;
	static final int  K_MODE        =1;
	static final int  K_KEY         =2;
	static final int  K_COLOR       =3;
	static final int  K_KEYWORD     =4;
	static final int  K_OBJECT      =5;
	static final int  K_MENU        =6;
	static final int  K_ITEM        =7;
	static final int  K_SUBMENU     =8;
	static final int  K_COMPILERX   =9;
	static final int  K_EXTERN     =10;
	static final int  K_INCLUDE    =11;
	static final int  K_SUB        =12;
	static final int  K_EVENTMAP   =13;
	static final int  K_COLORIZE   =14;
	static final int  K_ABBREV     =15;
	static final int  K_HSTATE     =16;
	static final int  K_HTRANS     =17;
	static final int  K_HWORDS     =18;
	static final int  K_SUBMENUCOND= 19;
	static final int  K_HWTYPE     =20;
	static final int  K_COLPALETTE =21;
	static final int  K_CVSIGNRX   =22;

	
	
	
	
	/*
	#define new OrdLookup("x", BFI_x) { #x, BFI_##x }
	#define new OrdLookup("x", BFS_x) { #x, BFS_##x }
	#define new OrdLookup("x", FLAG_x) { #x, FLAG_##x }
	#define new OrdLookup("x", EM_x) { #x, EM_##x }
	#define new OrdLookup("x", COL_x) { #x, COL_##x }
	#define new OrdLookup("x", CLR_x) { #x, CLR_##x }
	*/

	static class OrdLookup {
		String Name;
	    int num;

	    public OrdLookup(String name, int num) {
			Name = name;
			this.num = num;
		}
	}

	OrdLookup mode_num[] = {
	new OrdLookup("AutoIndent", BFI_AutoIndent),
	new OrdLookup("Insert", BFI_Insert),
	new OrdLookup("DrawOn", BFI_DrawOn),
	new OrdLookup("HilitOn", BFI_HilitOn),
	new OrdLookup("ExpandTabs", BFI_ExpandTabs),
	new OrdLookup("Trim", BFI_Trim),
	new OrdLookup("TabSize", BFI_TabSize),
	new OrdLookup("ShowTabs", BFI_ShowTabs),
	new OrdLookup("LineChar", BFI_LineChar),
	new OrdLookup("StripChar", BFI_StripChar),
	new OrdLookup("AddLF", BFI_AddLF),
	new OrdLookup("AddCR", BFI_AddCR),
	new OrdLookup("ForceNewLine", BFI_ForceNewLine),
	new OrdLookup("HardMode", BFI_HardMode),
	new OrdLookup("Undo", BFI_Undo),
	new OrdLookup("ReadOnly", BFI_ReadOnly),
	new OrdLookup("AutoSave", BFI_AutoSave),
	new OrdLookup("KeepBackups", BFI_KeepBackups),
	new OrdLookup("LoadMargin", BFI_LoadMargin),
	new OrdLookup("UndoLimit", BFI_UndoLimit),
	new OrdLookup("MatchCase", BFI_MatchCase),
	new OrdLookup("BackSpKillTab", BFI_BackSpKillTab),
	new OrdLookup("DeleteKillTab", BFI_DeleteKillTab),
	new OrdLookup("BackSpUnindents", BFI_BackSpUnindents),
	new OrdLookup("SpaceTabs", BFI_SpaceTabs),
	new OrdLookup("IndentWithTabs", BFI_IndentWithTabs),
	new OrdLookup("LeftMargin", BFI_LeftMargin),
	new OrdLookup("RightMargin", BFI_RightMargin),
	new OrdLookup("SeeThruSel", BFI_SeeThruSel),
	new OrdLookup("WordWrap", BFI_WordWrap),
	new OrdLookup("ShowMarkers", BFI_ShowMarkers),
	new OrdLookup("CursorThroughTabs", BFI_CursorThroughTabs),
	new OrdLookup("SaveFolds", BFI_SaveFolds),
	new OrdLookup("MultiLineHilit", BFI_MultiLineHilit),
	new OrdLookup("AutoHilitParen", BFI_AutoHilitParen),
	new OrdLookup("Abbreviations", BFI_Abbreviations),
	new OrdLookup("BackSpKillBlock", BFI_BackSpKillBlock),
	new OrdLookup("DeleteKillBlock", BFI_DeleteKillBlock),
	new OrdLookup("PersistentBlocks", BFI_PersistentBlocks),
	new OrdLookup("InsertKillBlock", BFI_InsertKillBlock),
	new OrdLookup("UndoMoves", BFI_UndoMoves),
	new OrdLookup("DetectLineSep", BFI_DetectLineSep),
	new OrdLookup("TrimOnSave", BFI_TrimOnSave),
	new OrdLookup("SaveBookmarks", BFI_SaveBookmarks),
	new OrdLookup("HilitTags", BFI_HilitTags),
	new OrdLookup("ShowBookmarks", BFI_ShowBookmarks),
	//{ 0, 0 },
	};

	OrdLookup mode_string[] = {
	new OrdLookup("Colorizer", BFI_Colorizer),
	new OrdLookup("IndentMode", BFI_IndentMode),
	new OrdLookup("RoutineRegexp", BFS_RoutineRegexp),
	new OrdLookup("DefFindOpt", BFS_DefFindOpt),
	new OrdLookup("DefFindReplaceOpt", BFS_DefFindReplaceOpt),
	new OrdLookup("CommentStart", BFS_CommentStart),
	new OrdLookup("CommentEnd", BFS_CommentEnd),
	new OrdLookup("WordChars", BFS_WordChars),
	new OrdLookup("CapitalChars", BFS_CapitalChars),
	new OrdLookup("FileNameRx", BFS_FileNameRx),
	new OrdLookup("FirstLineRx", BFS_FirstLineRx),
	new OrdLookup("CompileCommand", BFS_CompileCommand),
	new OrdLookup("EventMap", BFI_EventMap),
	//{ 0, 0 },
	};

	OrdLookup global_num[] = {
			
	//#ifdef CONFIG_INDENT_C
	new OrdLookup("C_Indent", FLAG_C_Indent),
	new OrdLookup("C_BraceOfs", FLAG_C_BraceOfs),
	new OrdLookup("C_CaseOfs", FLAG_C_CaseOfs),
	new OrdLookup("C_CaseDelta", FLAG_C_CaseDelta),
	new OrdLookup("C_ClassOfs", FLAG_C_ClassOfs),
	new OrdLookup("C_ClassDelta", FLAG_C_ClassDelta),
	new OrdLookup("C_ColonOfs", FLAG_C_ColonOfs),
	new OrdLookup("C_CommentOfs", FLAG_C_CommentOfs),
	new OrdLookup("C_CommentDelta", FLAG_C_CommentDelta),
	new OrdLookup("C_FirstLevelWidth", FLAG_C_FirstLevelWidth),
	new OrdLookup("C_FirstLevelIndent", FLAG_C_FirstLevelIndent),
	new OrdLookup("C_Continuation", FLAG_C_Continuation),
	new OrdLookup("C_ParenDelta", FLAG_C_ParenDelta),
	new OrdLookup("FunctionUsesContinuation", FLAG_FunctionUsesContinuation),
	//#endif
	//#ifdef CONFIG_INDENT_REXX
	new OrdLookup("REXX_Indent", FLAG_REXX_Indent),
	new OrdLookup("REXX_Do_Offset", FLAG_REXX_Do_Offset),
	//#endif
	//*/
	new OrdLookup("ScreenSizeX", FLAG_ScreenSizeX),
	new OrdLookup("ScreenSizeY", FLAG_ScreenSizeY),
	new OrdLookup("CursorInsertStart", FLAG_CursorInsertStart),
	new OrdLookup("CursorInsertEnd", FLAG_CursorInsertEnd),
	new OrdLookup("CursorOverStart", FLAG_CursorOverStart),
	new OrdLookup("CursorOverEnd", FLAG_CursorOverEnd),
	new OrdLookup("SysClipboard", FLAG_SysClipboard),
	new OrdLookup("OpenAfterClose", FLAG_OpenAfterClose),
	new OrdLookup("ShowVScroll", FLAG_ShowVScroll),
	new OrdLookup("ShowHScroll", FLAG_ShowHScroll),
	new OrdLookup("ScrollBarWidth", FLAG_ScrollBarWidth),
	new OrdLookup("SelectPathname", FLAG_SelectPathname),
	new OrdLookup("ShowToolBar", FLAG_ShowToolBar),
	new OrdLookup("ShowMenuBar", FLAG_ShowMenuBar),
	new OrdLookup("KeepHistory", FLAG_KeepHistory),
	new OrdLookup("LoadDesktopOnEntry", FLAG_LoadDesktopOnEntry),
	new OrdLookup("SaveDesktopOnExit", FLAG_SaveDesktopOnExit),
	new OrdLookup("KeepMessages", FLAG_KeepMessages),
	new OrdLookup("ScrollBorderX", FLAG_ScrollBorderX),
	new OrdLookup("ScrollBorderY", FLAG_ScrollBorderY),
	new OrdLookup("ScrollJumpX", FLAG_ScrollJumpX),
	new OrdLookup("ScrollJumpY", FLAG_ScrollJumpY),
	new OrdLookup("GUIDialogs", FLAG_GUIDialogs),
	new OrdLookup("PMDisableAccel", FLAG_PMDisableAccel),
	new OrdLookup("SevenBit", FLAG_SevenBit),
	new OrdLookup("WeirdScroll", FLAG_WeirdScroll),
	new OrdLookup("LoadDesktopMode", FLAG_LoadDesktopMode),
	new OrdLookup("IgnoreBufferList", FLAG_IgnoreBufferList),
	new OrdLookup("ReassignModelIds", FLAG_ReassignModelIds),
	//{ 0, 0 },
	};

	OrdLookup global_string[] = {
	new OrdLookup("DefaultModeName", FLAG_DefaultModeName),
	new OrdLookup("CompletionFilter", FLAG_CompletionFilter),
	new OrdLookup("PrintDevice", FLAG_PrintDevice),
	new OrdLookup("CompileCommand", FLAG_CompileCommand),
	new OrdLookup("WindowFont", FLAG_WindowFont),
	new OrdLookup("HelpCommand", FLAG_HelpCommand),
	new OrdLookup("GUICharacters", FLAG_GUICharacters),
	new OrdLookup("CvsCommand", FLAG_CvsCommand),
	new OrdLookup("CvsLogMode", FLAG_CvsLogMode),
	//{ 0, 0 },
	};

	OrdLookup event_string[] = {
	new OrdLookup("MainMenu", EM_MainMenu),
	new OrdLookup("LocalMenu", EM_LocalMenu),
	//{ 0, 0 },
	};

	OrdLookup colorize_string[] = {
	new OrdLookup("SyntaxParser", COL_SyntaxParser),
	//{ 0, 0 },
	};

	OrdLookup hilit_colors[] = {
	new OrdLookup("Normal", CLR_Normal),
	new OrdLookup("Keyword", CLR_Keyword),
	new OrdLookup("String", CLR_String),
	new OrdLookup("Comment", CLR_Comment),
	new OrdLookup("CPreprocessor", CLR_CPreprocessor),
	new OrdLookup("Regexp", CLR_Regexp),
	new OrdLookup("Header", CLR_Header),
	new OrdLookup("Quotes", CLR_Quotes),
	new OrdLookup("Number", CLR_Number),
	new OrdLookup("HexNumber", CLR_HexNumber),
	new OrdLookup("OctalNumber", CLR_OctalNumber),
	new OrdLookup("FloatNumber", CLR_FloatNumber),
	new OrdLookup("Function", CLR_Function),
	new OrdLookup("Command", CLR_Command),
	new OrdLookup("Tag", CLR_Tag),
	new OrdLookup("Punctuation", CLR_Punctuation),
	new OrdLookup("New", CLR_New),
	new OrdLookup("Old", CLR_Old),
	new OrdLookup("Changed", CLR_Changed),
	new OrdLookup("Control", CLR_Control),
	new OrdLookup("Separator", CLR_Separator),
	new OrdLookup("Variable", CLR_Variable),
	new OrdLookup("Symbol", CLR_Symbol),
	new OrdLookup("Directive", CLR_Directive),
	new OrdLookup("Label", CLR_Label),
	new OrdLookup("Special", CLR_Special),
	new OrdLookup("QuoteDelim", CLR_QuoteDelim),
	new OrdLookup("RegexpDelim", CLR_RegexpDelim),
	//{ 0, 0 },
	};

	
	
	
	
	OrdLookup CfgKW[] = {
	{ "mode", K_MODE },
	{ "eventmap", K_EVENTMAP },
	{ "key", K_KEY },
	{ "color", K_COLOR },
	{ "color_palette", K_COLPALETTE },
	{ "keyword", K_KEYWORD },
	{ "object", K_OBJECT },
	{ "menu", K_MENU },
	{ "item", K_ITEM },
	{ "submenu", K_SUBMENU },
	{ "CompileRx", K_COMPILERX },
	{ "extern", K_EXTERN },
	{ "include", K_INCLUDE },
	{ "sub", K_SUB },
	{ "colorize", K_COLORIZE },
	{ "abbrev", K_ABBREV },
	{ "h_state", K_HSTATE },
	{ "h_trans", K_HTRANS },
	{ "h_words", K_HWORDS },
	{ "h_wtype", K_HWTYPE },
	{ "submenucond", K_SUBMENUCOND },
	{ "CvsIgnoreRx", K_CVSIGNRX },
	{ 0, 0 },
	};

	OrdLookup CfgVar[] = {
	    { "FilePath", mvFilePath },
	    { "FileName", mvFileName },
	    { "FileDirectory", mvFileDirectory },
	    { "FileBaseName", mvFileBaseName },
	    { "FileExtension", mvFileExtension },
	    { "CurDirectory", mvCurDirectory },
	    { "CurRow", mvCurRow, },
	    { "CurCol", mvCurCol },
	    { "Char", mvChar },
	    { "Word", mvWord },
	    { "Line", mvLine },
	    { 0, 0 },
	};
	
	
	
	
}
