package ru.dz.jfte;

public interface ModeDefs {

	public static final int  CMD_EXT = 0x1000;  // max 4096 internal commands, check cfte.cpp

	public static final int  CONTEXT_NONE      =0;
	public static final int  CONTEXT_FILE      =1;
	public static final int  CONTEXT_DIRECTORY =2;
	public static final int  CONTEXT_MESSAGES  =3;
	public static final int  CONTEXT_SHELL     =4;
	public static final int  CONTEXT_INPUT     =5;
	public static final int  CONTEXT_CHOICE    =6;
	public static final int  CONTEXT_LIST      =7;
	public static final int  CONTEXT_CHAR      =8;
	public static final int  CONTEXT_BUFFERS   =9;
	public static final int  CONTEXT_ROUTINES =10;
	public static final int  CONTEXT_MAPVIEW  =11;
	public static final int  CONTEXT_CVSBASE  =12;
	public static final int  CONTEXT_CVS      =13;
	public static final int  CONTEXT_CVSDIFF  =14;

	//typedef unsigned char ChColor;

	public static final int  HILIT_PLAIN   =0;
	public static final int  HILIT_C       =1;
	public static final int  HILIT_HTML    =2;
	public static final int  HILIT_MAKE    =3;
	public static final int  HILIT_REXX    =4;
	public static final int  HILIT_DIFF    =5;
	public static final int  HILIT_IPF     =6;
	public static final int  HILIT_PERL    =7;
	public static final int  HILIT_MERGE   =8;
	public static final int  HILIT_ADA     =9;
	public static final int  HILIT_MSG    =10;
	public static final int  HILIT_SH     =11;
	public static final int  HILIT_PASCAL =12;
	public static final int  HILIT_TEX    =13;
	public static final int  HILIT_FTE    =14;
	public static final int  HILIT_CATBS  =15;
	public static final int  HILIT_SIMPLE =16;

	public static final int  INDENT_PLAIN  =0;
	public static final int  INDENT_C      =1;
	public static final int  INDENT_REXX   =2;
	public static final int  INDENT_SIMPLE =3;

	public static final int  BFI_AutoIndent          =0;
	public static final int  BFI_Insert              =1;
	public static final int  BFI_DrawOn              =2;
	public static final int  BFI_HilitOn             =3;
	public static final int  BFI_ExpandTabs          =4;
	public static final int  BFI_Trim                =5;
	public static final int  BFI_TabSize             =6;
	public static final int  BFI_ShowTabs            =9;
	public static final int  BFI_HardMode           =15;
	public static final int  BFI_Undo               =16;
	public static final int  BFI_ReadOnly           =17;
	public static final int  BFI_AutoSave           =18;
	public static final int  BFI_KeepBackups        =19;
	public static final int  BFI_MatchCase          =22;
	public static final int  BFI_BackSpKillTab      =23;
	public static final int  BFI_DeleteKillTab      =24;
	public static final int  BFI_BackSpUnindents    =25;
	public static final int  BFI_SpaceTabs          =26;
	public static final int  BFI_IndentWithTabs     =27;
	public static final int  BFI_SeeThruSel         =30;
	public static final int  BFI_ShowMarkers        =32;
	public static final int  BFI_CursorThroughTabs  =33;
	public static final int  BFI_MultiLineHilit     =35;

	public static final int  BFI_WordWrap           =31;
	public static final int  BFI_LeftMargin         =28;
	public static final int  BFI_RightMargin        =29;

	public static final int  BFI_Colorizer           =7;
	public static final int  BFI_IndentMode          =8;

	public static final int  BFI_LineChar           =10;
	public static final int  BFI_StripChar          =11;
	public static final int  BFI_AddLF              =12;
	public static final int  BFI_AddCR              =13;
	public static final int  BFI_ForceNewLine       =14;
	public static final int  BFI_LoadMargin         =20;
	public static final int  BFI_SaveFolds          =34;

	public static final int  BFI_UndoLimit          =21;

	public static final int  BFI_AutoHilitParen     =36;
	public static final int  BFI_Abbreviations      =37;
	public static final int  BFI_BackSpKillBlock    =38;
	public static final int  BFI_DeleteKillBlock    =39;
	public static final int  BFI_PersistentBlocks   = 40;
	public static final int  BFI_InsertKillBlock    = 41;
	public static final int  BFI_EventMap           = 42;
	public static final int  BFI_UndoMoves          = 43;
	public static final int  BFI_DetectLineSep      = 44;
	public static final int  BFI_TrimOnSave         = 45;
	public static final int  BFI_SaveBookmarks      = 46;
	public static final int  BFI_HilitTags          = 47;
	public static final int  BFI_ShowBookmarks      = 48;

	public static final int  BFI_COUNT              = 49;

	public static final int  BFS_RoutineRegexp       = (0 | 256);
	public static final int  BFS_DefFindOpt          = (1 | 256);
	public static final int  BFS_DefFindReplaceOpt   = (2 | 256);
	public static final int  BFS_CommentStart        = (3 | 256);
	public static final int  BFS_CommentEnd          = (4 | 256);
	public static final int  BFS_FileNameRx          = (5 | 256);
	public static final int  BFS_FirstLineRx         = (6 | 256);
	public static final int  BFS_CompileCommand      = (7 | 256);

	public static final int  BFS_COUNT               = 8;

	public static final int  BFS_WordChars           = (100 | 256); // ext
	public static final int  BFS_CapitalChars        = (101 | 256);

	public static void WSETBIT(char [] x , int y, int z) 
	{
		int y3 = (y & 0xFF) >> 3;
		int y7 = y & 0x7;

		x[y3] = (char)(z != 0 ? 
				(x[y3] | (1 << y7)) : 
					(x[y3] & ~(1 << y7))
				);

	}


	/*
	public static final int  BFI(y,x) ((y)->Flags.num[(x) & 0xFF])
	public static final int  BFI_SET(y,x,v) ((y)->Flags.num[(x) & 0xFF]=(v))
	public static final int  BFS(y,x) ((y)->Flags.str[(x) & 0xFF])

	public static final int  WSETBIT(x,y,z) \
	    ((x)[(unsigned char)(y) >> 3] = char((z) ? \
	    ((x)[(unsigned char)(y) >> 3] |  (1 << ((unsigned char)(y) & 0x7))) : \
	    ((x)[(unsigned char)(y) >> 3] & ~(1 << ((unsigned char)(y) & 0x7))) ))

	public static final int  WGETBIT(x,y) \
	    (((x)[(unsigned char)(y) / 8] &  (1 << ((unsigned char)(y) % 8))) ? 1 : 0)
	 */

	/*
	typedef struct {
	    int num[BFI_COUNT];
	    char *str[BFS_COUNT];
	    char WordChars[32];
	    char CapitalChars[32];
	} EBufferFlags;

	extern EBufferFlags DefaultBufferFlags;

	/* globals */
	public static final int  FLAG_C_Indent            =1;
	public static final int  FLAG_C_BraceOfs          =2;
	public static final int  FLAG_REXX_Indent         =3;
	public static final int  FLAG_ScreenSizeX         =6;
	public static final int  FLAG_ScreenSizeY         =7;
	public static final int  FLAG_CursorInsertStart   =8;
	public static final int  FLAG_CursorInsertEnd     =9;
	public static final int  FLAG_CursorOverStart    = 10;
	public static final int  FLAG_CursorOverEnd      = 11;
	public static final int  FLAG_SysClipboard       = 12;
	public static final int  FLAG_ShowHScroll        = 13;
	public static final int  FLAG_ShowVScroll        = 14;
	public static final int  FLAG_ScrollBarWidth     = 15;
	public static final int  FLAG_SelectPathname     = 16;
	public static final int  FLAG_C_CaseOfs          = 18;
	public static final int  FLAG_DefaultModeName    = 19;
	public static final int  FLAG_CompletionFilter   =20;
	public static final int  FLAG_ShowMenuBar        =22;
	public static final int  FLAG_C_CaseDelta        =23;
	public static final int  FLAG_C_ClassOfs         =24;
	public static final int  FLAG_C_ClassDelta       =25;
	public static final int  FLAG_C_ColonOfs         =26;
	public static final int  FLAG_C_CommentOfs       =27;
	public static final int  FLAG_C_CommentDelta     =28;
	public static final int  FLAG_OpenAfterClose     =30;
	public static final int  FLAG_PrintDevice        =31;
	public static final int  FLAG_CompileCommand     =32;
	public static final int  FLAG_REXX_Do_Offset     =33;
	public static final int  FLAG_KeepHistory        =34;
	public static final int  FLAG_LoadDesktopOnEntry =35;
	public static final int  FLAG_SaveDesktopOnExit  =36;
	public static final int  FLAG_WindowFont         =37;
	public static final int  FLAG_KeepMessages       =38;
	public static final int  FLAG_ScrollBorderX      =39;
	public static final int  FLAG_ScrollBorderY      =40;
	public static final int  FLAG_ScrollJumpX        =41;
	public static final int  FLAG_ScrollJumpY        =42;
	public static final int  FLAG_ShowToolBar        =43;
	public static final int  FLAG_GUIDialogs         =44;
	public static final int  FLAG_PMDisableAccel     =45;
	public static final int  FLAG_SevenBit           =46;
	public static final int  FLAG_WeirdScroll        =47;
	public static final int  FLAG_LoadDesktopMode    =48;
	public static final int  FLAG_HelpCommand        =49;
	public static final int  FLAG_C_FirstLevelIndent =50;
	public static final int  FLAG_C_FirstLevelWidth  =51;
	public static final int  FLAG_C_Continuation     =52;
	public static final int  FLAG_C_ParenDelta       =53;
	public static final int  FLAG_FunctionUsesContinuation =54;
	public static final int  FLAG_IgnoreBufferList   =55;
	public static final int  FLAG_GUICharacters      =56;
	public static final int  FLAG_CvsCommand         =57;
	public static final int  FLAG_CvsLogMode         =58;
	public static final int  FLAG_ReassignModelIds   =59;

	public static final int  EM_MENUS =2;
	public static final int  EM_MainMenu =0;
	public static final int  EM_LocalMenu =1;

	public static final int  COL_SyntaxParser =1;

	public static final int  CLR_Normal         =0;
	public static final int  CLR_Keyword        =1;
	public static final int  CLR_String         =2;
	public static final int  CLR_Comment        =3;
	public static final int  CLR_CPreprocessor  =4;
	public static final int  CLR_Regexp         =5;
	public static final int  CLR_Header         =6;
	public static final int  CLR_Quotes         =7;
	public static final int  CLR_Number         =8;
	public static final int  CLR_HexNumber      =9;
	public static final int  CLR_OctalNumber   =10;
	public static final int  CLR_FloatNumber   =11;
	public static final int  CLR_Function      =12;
	public static final int  CLR_Command       =13;
	public static final int  CLR_Tag           =14;
	public static final int  CLR_Punctuation   =15;
	public static final int  CLR_New           =16;
	public static final int  CLR_Old           =17;
	public static final int  CLR_Changed       =18;
	public static final int  CLR_Control       =19;
	public static final int  CLR_Separator     =20;
	public static final int  CLR_Variable      =21;
	public static final int  CLR_Symbol        =22;
	public static final int  CLR_Directive     =23;
	public static final int  CLR_Label         =24;
	public static final int  CLR_Special       =25;
	public static final int  CLR_QuoteDelim    =26;
	public static final int  CLR_RegexpDelim   =27;

	public static final int  COUNT_CLR         =28;

	public static final int  MATCH_MUST_BOL     = 0x0001;
	public static final int  MATCH_MUST_BOLW    = 0x0002;
	public static final int  MATCH_MUST_EOL     = 0x0004;
	public static final int  MATCH_MUST_EOLW    = 0x0008;
	public static final int  MATCH_NO_CASE      = 0x0010;
	public static final int  MATCH_SET          = 0x0020;
	public static final int  MATCH_NOTSET       = 0x0040;
	public static final int  MATCH_QUOTECH      = 0x0100;
	public static final int  MATCH_QUOTEEOL     = 0x0200;
	public static final int  MATCH_NOGRAB       = 0x0400;
	public static final int  MATCH_NEGATE       = 0x0800;
	public static final int  MATCH_TAGASNEXT    = 0x1000;

	public static final int  ACTION_NXSTATE     = 0x0001;

	public static final int  STATE_NOCASE       = 0x0001;
	public static final int  STATE_TAGASNEXT    = 0x0002;
	public static final int  STATE_NOGRAB       = 0x0004;


	// Msg

	public static final int S_BUSY    = 0;
	public static final int S_INFO    = 1;
	public static final int S_BOLD    = 2;
	public static final int S_ERROR   = 3;


	// Macro variables
	public static final int  mvFilePath = 1;      // directory + name + extension 
	public static final int  mvFileName = 2;      // name + extension 
	public static final int  mvFileDirectory = 3; // directory + '/' 
	public static final int  mvFileBaseName = 4;  // without the last extension 
	public static final int  mvFileExtension = 5; // the last one 
	public static final int  mvCurDirectory = 6;
	public static final int  mvCurRow = 7;
	public static final int  mvCurCol = 8;
	public static final int  mvChar = 9;
	public static final int  mvWord = 10;
	public static final int  mvLine = 11;


	// etc
	public static final int  ABBREV_HASH      = 16;

	// GUI
	public static final int   GUIDLG_CHOICE      = 0x00000001;
	public static final int   GUIDLG_PROMPT      = 0x00000002;
	public static final int   GUIDLG_PROMPT2     = 0x00000004;
	public static final int   GUIDLG_FILE        = 0x00000008;
	public static final int   GUIDLG_FIND        = 0x00000010;
	public static final int   GUIDLG_FINDREPLACE = 0x00000020;


	/* history values */
	public static final int HIST_DEFAULT   = 0;
	public static final int HIST_PATH      = 1;
	public static final int HIST_SEARCH    = 2;
	public static final int HIST_POSITION  = 3;
	public static final int HIST_SETUP     = 4;
	public static final int HIST_SHELL     = 5;
	public static final int HIST_COMPILE   = 6;
	public static final int HIST_SEARCHOPT = 7;
	public static final int HIST_BOOKMARK  = 8;
	public static final int HIST_REGEXP    = 9;
	public static final int HIST_TRANS     =10;
	public static final int HIST_TAGFILES  =11;
	public static final int HIST_CVS       =12;
	public static final int HIST_CVSDIFF   =13;
	public static final int HIST_CVSCOMMIT =14;
	
}
