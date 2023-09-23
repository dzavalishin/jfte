package ru.dz.jfte.config;

import ru.dz.jfte.ExCommands;
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
	new OrdLookup( "mode", K_MODE ),
	new OrdLookup( "eventmap", K_EVENTMAP ),
	new OrdLookup( "key", K_KEY ),
	new OrdLookup( "color", K_COLOR ),
	new OrdLookup( "color_palette", K_COLPALETTE ),
	new OrdLookup( "keyword", K_KEYWORD ),
	new OrdLookup( "object", K_OBJECT ),
	new OrdLookup( "menu", K_MENU ),
	new OrdLookup( "item", K_ITEM ),
	new OrdLookup( "submenu", K_SUBMENU ),
	new OrdLookup( "CompileRx", K_COMPILERX ),
	new OrdLookup( "extern", K_EXTERN ),
	new OrdLookup( "include", K_INCLUDE ),
	new OrdLookup( "sub", K_SUB ),
	new OrdLookup( "colorize", K_COLORIZE ),
	new OrdLookup( "abbrev", K_ABBREV ),
	new OrdLookup( "h_state", K_HSTATE ),
	new OrdLookup( "h_trans", K_HTRANS ),
	new OrdLookup( "h_words", K_HWORDS ),
	new OrdLookup( "h_wtype", K_HWTYPE ),
	new OrdLookup( "submenucond", K_SUBMENUCOND ),
	new OrdLookup( "CvsIgnoreRx", K_CVSIGNRX ),
	//{ 0, 0 },
	};

	OrdLookup CfgVar[] = {
	    new OrdLookup( "FilePath", mvFilePath ),
	    new OrdLookup( "FileName", mvFileName ),
	    new OrdLookup( "FileDirectory", mvFileDirectory ),
	    new OrdLookup( "FileBaseName", mvFileBaseName ),
	    new OrdLookup( "FileExtension", mvFileExtension ),
	    new OrdLookup( "CurDirectory", mvCurDirectory ),
	    new OrdLookup( "CurRow", mvCurRow ),
	    new OrdLookup( "CurCol", mvCurCol ),
	    new OrdLookup( "Char", mvChar ),
	    new OrdLookup( "Word", mvWord ),
	    new OrdLookup( "Line", mvLine ),
	    //{ 0, 0 },
	};
	

	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	/*    c_cmdtab.h
	 *
	 *    Copyright (c) 1994-1996, Marko Macek
	 *
	 *    You may distribute under the terms of either the GNU General Public
	 *    License or the Artistic License, as specified in the README file.
	 *
	 */

	//#define TAB(x) 	    { Ex##x, #x }

	
	static class CmdDef
	{
		int CmdId;
	    String Name;

	    public CmdDef(String s, ExCommands c) {
			Name = s;
			CmdId = c.ordinal();
		}
	
	}
	
	
	
	CmdDef [] Command_Table = {
	new CmdDef("Nop", ExCommands.ExNop),
	new CmdDef("Fail", ExCommands.ExFail),
	new CmdDef("MoveLeft", ExCommands.ExMoveLeft),
	new CmdDef("MoveRight", ExCommands.ExMoveRight),
	new CmdDef("MoveUp", ExCommands.ExMoveUp),
	new CmdDef("MoveDown", ExCommands.ExMoveDown),
	new CmdDef("MovePrev", ExCommands.ExMovePrev),
	new CmdDef("MoveNext", ExCommands.ExMoveNext),
	new CmdDef("MoveWordLeft", ExCommands.ExMoveWordLeft),
	new CmdDef("MoveWordRight", ExCommands.ExMoveWordRight),
	new CmdDef("MoveWordPrev", ExCommands.ExMoveWordPrev),
	new CmdDef("MoveWordNext", ExCommands.ExMoveWordNext),
	new CmdDef("MoveWordEndLeft", ExCommands.ExMoveWordEndLeft),
	new CmdDef("MoveWordEndRight", ExCommands.ExMoveWordEndRight),
	new CmdDef("MoveWordEndPrev", ExCommands.ExMoveWordEndPrev),
	new CmdDef("MoveWordEndNext", ExCommands.ExMoveWordEndNext),
	new CmdDef("MoveWordOrCapLeft", ExCommands.ExMoveWordOrCapLeft),
	new CmdDef("MoveWordOrCapRight", ExCommands.ExMoveWordOrCapRight),
	new CmdDef("MoveWordOrCapPrev", ExCommands.ExMoveWordOrCapPrev),
	new CmdDef("MoveWordOrCapNext", ExCommands.ExMoveWordOrCapNext),
	new CmdDef("MoveWordOrCapEndLeft", ExCommands.ExMoveWordOrCapEndLeft),
	new CmdDef("MoveWordOrCapEndRight", ExCommands.ExMoveWordOrCapEndRight),
	new CmdDef("MoveWordOrCapEndPrev", ExCommands.ExMoveWordOrCapEndPrev),
	new CmdDef("MoveWordOrCapEndNext", ExCommands.ExMoveWordOrCapEndNext),
	new CmdDef("MoveLineStart", ExCommands.ExMoveLineStart),
	new CmdDef("MoveLineEnd", ExCommands.ExMoveLineEnd),
	new CmdDef("MovePageUp", ExCommands.ExMovePageUp),
	new CmdDef("MovePageDown", ExCommands.ExMovePageDown),
	new CmdDef("MovePageLeft", ExCommands.ExMovePageLeft),
	new CmdDef("MovePageRight", ExCommands.ExMovePageRight),
	new CmdDef("MovePageStart", ExCommands.ExMovePageStart),
	new CmdDef("MovePageEnd", ExCommands.ExMovePageEnd),
	new CmdDef("MoveFileStart", ExCommands.ExMoveFileStart),
	new CmdDef("MoveFileEnd", ExCommands.ExMoveFileEnd),
	new CmdDef("MoveBlockStart", ExCommands.ExMoveBlockStart),
	new CmdDef("MoveBlockEnd", ExCommands.ExMoveBlockEnd),
	new CmdDef("MoveFirstNonWhite", ExCommands.ExMoveFirstNonWhite),
	new CmdDef("MoveLastNonWhite", ExCommands.ExMoveLastNonWhite),
	new CmdDef("MovePrevEqualIndent", ExCommands.ExMovePrevEqualIndent),
	new CmdDef("MoveNextEqualIndent", ExCommands.ExMoveNextEqualIndent),
	new CmdDef("MovePrevTab", ExCommands.ExMovePrevTab),
	new CmdDef("MoveNextTab", ExCommands.ExMoveNextTab),
	new CmdDef("MoveLineTop", ExCommands.ExMoveLineTop),
	new CmdDef("MoveLineCenter", ExCommands.ExMoveLineCenter),
	new CmdDef("MoveLineBottom", ExCommands.ExMoveLineBottom),
	new CmdDef("ScrollLeft", ExCommands.ExScrollLeft),
	new CmdDef("ScrollRight", ExCommands.ExScrollRight),
	new CmdDef("ScrollDown", ExCommands.ExScrollDown),
	new CmdDef("ScrollUp", ExCommands.ExScrollUp),    
	new CmdDef("MoveTabStart", ExCommands.ExMoveTabStart),
	new CmdDef("MoveTabEnd", ExCommands.ExMoveTabEnd),

	new CmdDef("KillLine", ExCommands.ExKillLine),
	new CmdDef("KillChar", ExCommands.ExKillChar),
	new CmdDef("KillCharPrev", ExCommands.ExKillCharPrev),
	new CmdDef("KillWord", ExCommands.ExKillWord),
	new CmdDef("KillWordPrev", ExCommands.ExKillWordPrev),
	new CmdDef("KillWordOrCap", ExCommands.ExKillWordOrCap),
	new CmdDef("KillWordOrCapPrev", ExCommands.ExKillWordOrCapPrev),
	new CmdDef("KillToLineStart", ExCommands.ExKillToLineStart),
	new CmdDef("KillToLineEnd", ExCommands.ExKillToLineEnd),
	new CmdDef("KillBlock", ExCommands.ExKillBlock),
	new CmdDef("KillBlockOrChar", ExCommands.ExKillBlockOrChar),
	new CmdDef("KillBlockOrCharPrev", ExCommands.ExKillBlockOrCharPrev),
	new CmdDef("BackSpace", ExCommands.ExBackSpace),
	new CmdDef("Delete", ExCommands.ExDelete),
	new CmdDef("CharCaseUp", ExCommands.ExCharCaseUp),
	new CmdDef("CharCaseDown", ExCommands.ExCharCaseDown),
	new CmdDef("CharCaseToggle", ExCommands.ExCharCaseToggle),
	new CmdDef("LineCaseUp", ExCommands.ExLineCaseUp),
	new CmdDef("LineCaseDown", ExCommands.ExLineCaseDown),
	new CmdDef("LineCaseToggle", ExCommands.ExLineCaseToggle),    
	new CmdDef("LineInsert", ExCommands.ExLineInsert),
	new CmdDef("LineAdd", ExCommands.ExLineAdd),
	new CmdDef("LineSplit", ExCommands.ExLineSplit),
	new CmdDef("LineJoin", ExCommands.ExLineJoin),
	new CmdDef("LineNew", ExCommands.ExLineNew),
	new CmdDef("LineIndent", ExCommands.ExLineIndent),
	new CmdDef("LineTrim", ExCommands.ExLineTrim),
	new CmdDef("FileTrim", ExCommands.ExFileTrim),
	new CmdDef("BlockTrim", ExCommands.ExBlockTrim),

	new CmdDef("InsertSpacesToTab", ExCommands.ExInsertSpacesToTab),
	new CmdDef("InsertTab", ExCommands.ExInsertTab),
	new CmdDef("InsertSpace", ExCommands.ExInsertSpace),
	new CmdDef("WrapPara", ExCommands.ExWrapPara),
	new CmdDef("InsPrevLineChar", ExCommands.ExInsPrevLineChar),
	new CmdDef("InsPrevLineToEol", ExCommands.ExInsPrevLineToEol),
	new CmdDef("LineDuplicate", ExCommands.ExLineDuplicate),    
	new CmdDef("BlockBegin", ExCommands.ExBlockBegin),
	new CmdDef("BlockEnd", ExCommands.ExBlockEnd),
	new CmdDef("BlockUnmark", ExCommands.ExBlockUnmark),
	new CmdDef("BlockCut", ExCommands.ExBlockCut),
	new CmdDef("BlockCopy", ExCommands.ExBlockCopy),
	new CmdDef("BlockCutAppend", ExCommands.ExBlockCutAppend),
	new CmdDef("BlockCopyAppend", ExCommands.ExBlockCopyAppend),
	new CmdDef("ClipClear", ExCommands.ExClipClear),
	new CmdDef("BlockPaste", ExCommands.ExBlockPaste),
	new CmdDef("BlockKill", ExCommands.ExBlockKill),
	new CmdDef("BlockSort", ExCommands.ExBlockSort),
	new CmdDef("BlockSortReverse", ExCommands.ExBlockSortReverse),
	new CmdDef("BlockIndent", ExCommands.ExBlockIndent),
	new CmdDef("BlockUnindent", ExCommands.ExBlockUnindent),
	new CmdDef("BlockClear", ExCommands.ExBlockClear),
	new CmdDef("BlockMarkStream", ExCommands.ExBlockMarkStream),
	new CmdDef("BlockMarkLine", ExCommands.ExBlockMarkLine),
	new CmdDef("BlockMarkColumn", ExCommands.ExBlockMarkColumn),
	new CmdDef("BlockCaseUp", ExCommands.ExBlockCaseUp),
	new CmdDef("BlockCaseDown", ExCommands.ExBlockCaseDown),
	new CmdDef("BlockCaseToggle", ExCommands.ExBlockCaseToggle),
	new CmdDef("BlockExtendBegin", ExCommands.ExBlockExtendBegin),
	new CmdDef("BlockExtendEnd", ExCommands.ExBlockExtendEnd),
	new CmdDef("BlockReIndent", ExCommands.ExBlockReIndent),
	new CmdDef("BlockSelectWord", ExCommands.ExBlockSelectWord),
	new CmdDef("BlockSelectLine", ExCommands.ExBlockSelectLine),
	new CmdDef("BlockSelectPara", ExCommands.ExBlockSelectPara),
	new CmdDef("Undo", ExCommands.ExUndo),
	new CmdDef("Redo", ExCommands.ExRedo),
	new CmdDef("MatchBracket", ExCommands.ExMatchBracket),
	new CmdDef("MovePrevPos", ExCommands.ExMovePrevPos),
	new CmdDef("MoveSavedPosCol", ExCommands.ExMoveSavedPosCol),
	new CmdDef("MoveSavedPosRow", ExCommands.ExMoveSavedPosRow),
	new CmdDef("MoveSavedPos", ExCommands.ExMoveSavedPos),
	new CmdDef("SavePos", ExCommands.ExSavePos),
	new CmdDef("CompleteWord", ExCommands.ExCompleteWord),
	new CmdDef("MoveToLine", ExCommands.ExMoveToLine),
	new CmdDef("MoveToColumn", ExCommands.ExMoveToColumn),
	new CmdDef("BlockPasteStream", ExCommands.ExBlockPasteStream),
	new CmdDef("BlockPasteLine", ExCommands.ExBlockPasteLine),
	new CmdDef("BlockPasteColumn", ExCommands.ExBlockPasteColumn),
	new CmdDef("ShowPosition", ExCommands.ExShowPosition),

	new CmdDef("FoldCreate", ExCommands.ExFoldCreate),
	new CmdDef("FoldCreateByRegexp", ExCommands.ExFoldCreateByRegexp),
	new CmdDef("FoldDestroy", ExCommands.ExFoldDestroy),
	new CmdDef("FoldDestroyAll", ExCommands.ExFoldDestroyAll),
	new CmdDef("FoldPromote", ExCommands.ExFoldPromote),
	new CmdDef("FoldDemote", ExCommands.ExFoldDemote),
	new CmdDef("FoldOpen", ExCommands.ExFoldOpen),
	new CmdDef("FoldOpenNested", ExCommands.ExFoldOpenNested),
	new CmdDef("FoldClose", ExCommands.ExFoldClose),
	new CmdDef("FoldOpenAll", ExCommands.ExFoldOpenAll),
	new CmdDef("FoldCloseAll", ExCommands.ExFoldCloseAll),
	new CmdDef("FoldToggleOpenClose", ExCommands.ExFoldToggleOpenClose),
	new CmdDef("MoveFoldTop", ExCommands.ExMoveFoldTop),
	new CmdDef("MoveFoldPrev", ExCommands.ExMoveFoldPrev),
	new CmdDef("MoveFoldNext", ExCommands.ExMoveFoldNext),

	new CmdDef("PlaceBookmark", ExCommands.ExPlaceBookmark),
	new CmdDef("RemoveBookmark", ExCommands.ExRemoveBookmark),
	new CmdDef("GotoBookmark", ExCommands.ExGotoBookmark),

	new CmdDef("InsertString", ExCommands.ExInsertString),
	new CmdDef("SelfInsert", ExCommands.ExSelfInsert),
	new CmdDef("FilePrev", ExCommands.ExFilePrev),
	new CmdDef("FileNext", ExCommands.ExFileNext),
	new CmdDef("FileLast", ExCommands.ExFileLast),
	new CmdDef("SwitchTo", ExCommands.ExSwitchTo),

	new CmdDef("FileReload", ExCommands.ExFileReload),
	new CmdDef("FileSave", ExCommands.ExFileSave),
	new CmdDef("FileSaveAll", ExCommands.ExFileSaveAll),
	new CmdDef("FileSaveAs", ExCommands.ExFileSaveAs),
	new CmdDef("FileWriteTo", ExCommands.ExFileWriteTo),
	new CmdDef("FileOpen", ExCommands.ExFileOpen),
	new CmdDef("FileOpenInMode", ExCommands.ExFileOpenInMode),
	new CmdDef("FilePrint", ExCommands.ExFilePrint),

	new CmdDef("BlockPrint", ExCommands.ExBlockPrint),
	new CmdDef("BlockRead", ExCommands.ExBlockRead),
	new CmdDef("BlockReadStream", ExCommands.ExBlockReadStream),
	new CmdDef("BlockReadLine", ExCommands.ExBlockReadLine),
	new CmdDef("BlockReadColumn", ExCommands.ExBlockReadColumn),
	new CmdDef("BlockWrite", ExCommands.ExBlockWrite),

	new CmdDef("IncrementalSearch", ExCommands.ExIncrementalSearch),
	new CmdDef("Find", ExCommands.ExFind),
	new CmdDef("FindReplace", ExCommands.ExFindReplace),
	new CmdDef("FindRepeat", ExCommands.ExFindRepeat),
	new CmdDef("FindRepeatOnce", ExCommands.ExFindRepeatOnce),
	new CmdDef("FindRepeatReverse", ExCommands.ExFindRepeatReverse),

	new CmdDef("InsertChar", ExCommands.ExInsertChar),

	new CmdDef("FileClose", ExCommands.ExFileClose),
	new CmdDef("FileCloseAll", ExCommands.ExFileCloseAll),

	new CmdDef("WinRefresh", ExCommands.ExWinRefresh),

	new CmdDef("WinHSplit", ExCommands.ExWinHSplit),
	new CmdDef("WinNext", ExCommands.ExWinNext),
	new CmdDef("WinPrev", ExCommands.ExWinPrev),
	new CmdDef("WinClose", ExCommands.ExWinClose),
	new CmdDef("WinZoom", ExCommands.ExWinZoom),
	new CmdDef("WinResize", ExCommands.ExWinResize),

	new CmdDef("ExitEditor", ExCommands.ExExitEditor),

	new CmdDef("ViewBuffers", ExCommands.ExViewBuffers),
	new CmdDef("ListRoutines", ExCommands.ExListRoutines),
	new CmdDef("DirOpen", ExCommands.ExDirOpen),

	new CmdDef("Compile", ExCommands.ExCompile),
	new CmdDef("CompilePrevError", ExCommands.ExCompilePrevError),
	new CmdDef("CompileNextError", ExCommands.ExCompileNextError),
	new CmdDef("ViewMessages", ExCommands.ExViewMessages),

	new CmdDef("ShowKey", ExCommands.ExShowKey),
	new CmdDef("ShowEntryScreen", ExCommands.ExShowEntryScreen),
	new CmdDef("RunProgram", ExCommands.ExRunProgram),
	new CmdDef("HilitWord", ExCommands.ExHilitWord),
	new CmdDef("SearchWordPrev", ExCommands.ExSearchWordPrev),
	new CmdDef("SearchWordNext", ExCommands.ExSearchWordNext),
	new CmdDef("HilitMatchBracket", ExCommands.ExHilitMatchBracket),
	new CmdDef("MainMenu", ExCommands.ExMainMenu),
	new CmdDef("LocalMenu", ExCommands.ExLocalMenu),
	new CmdDef("ShowMenu", ExCommands.ExShowMenu),
	new CmdDef("ChangeMode", ExCommands.ExChangeMode),
	new CmdDef("ChangeKeys", ExCommands.ExChangeKeys),
	new CmdDef("ChangeFlags", ExCommands.ExChangeFlags),

	new CmdDef("ToggleAutoIndent", ExCommands.ExToggleAutoIndent),
	new CmdDef("ToggleInsert", ExCommands.ExToggleInsert),
	new CmdDef("ToggleExpandTabs", ExCommands.ExToggleExpandTabs),
	new CmdDef("ToggleShowTabs", ExCommands.ExToggleShowTabs),
	new CmdDef("ToggleUndo", ExCommands.ExToggleUndo),
	new CmdDef("ToggleReadOnly", ExCommands.ExToggleReadOnly),
	new CmdDef("ToggleKeepBackups", ExCommands.ExToggleKeepBackups),
	new CmdDef("ToggleMatchCase", ExCommands.ExToggleMatchCase),
	new CmdDef("ToggleBackSpKillTab", ExCommands.ExToggleBackSpKillTab),
	new CmdDef("ToggleDeleteKillTab", ExCommands.ExToggleDeleteKillTab),
	new CmdDef("ToggleSpaceTabs", ExCommands.ExToggleSpaceTabs),
	new CmdDef("ToggleIndentWithTabs", ExCommands.ExToggleIndentWithTabs),
	new CmdDef("ToggleBackSpUnindents", ExCommands.ExToggleBackSpUnindents),
	new CmdDef("ToggleWordWrap", ExCommands.ExToggleWordWrap),
	new CmdDef("ToggleTrim", ExCommands.ExToggleTrim),
	new CmdDef("ToggleShowMarkers", ExCommands.ExToggleShowMarkers),
	new CmdDef("ToggleHilitTags", ExCommands.ExToggleHilitTags),
	new CmdDef("ToggleShowBookmarks", ExCommands.ExToggleShowBookmarks),
	new CmdDef("SetLeftMargin", ExCommands.ExSetLeftMargin),
	new CmdDef("SetRightMargin", ExCommands.ExSetRightMargin),
	new CmdDef("SetPrintDevice", ExCommands.ExSetPrintDevice),
	new CmdDef("ChangeTabSize", ExCommands.ExChangeTabSize),
	new CmdDef("ChangeLeftMargin", ExCommands.ExChangeLeftMargin),
	new CmdDef("ChangeRightMargin", ExCommands.ExChangeRightMargin),
	new CmdDef("ToggleSysClipboard", ExCommands.ExToggleSysClipboard),
	new CmdDef("Cancel", ExCommands.ExCancel),
	new CmdDef("Activate", ExCommands.ExActivate),
	new CmdDef("Rescan", ExCommands.ExRescan),
	new CmdDef("CloseActivate", ExCommands.ExCloseActivate),
	new CmdDef("ActivateInOtherWindow", ExCommands.ExActivateInOtherWindow),
	new CmdDef("DirGoUp", ExCommands.ExDirGoUp),
	new CmdDef("DirGoDown", ExCommands.ExDirGoDown), 
	new CmdDef("DirGoRoot", ExCommands.ExDirGoRoot),
	new CmdDef("DirGoto", ExCommands.ExDirGoto),
	new CmdDef("DirSearchCancel", ExCommands.ExDirSearchCancel),
	new CmdDef("DirSearchNext", ExCommands.ExDirSearchNext),
	new CmdDef("DirSearchPrev", ExCommands.ExDirSearchPrev),
	new CmdDef("DeleteFile", ExCommands.ExDeleteFile),
	new CmdDef("ShowVersion", ExCommands.ExShowVersion),
	new CmdDef("ASCIITable", ExCommands.ExASCIITable),
	new CmdDef("TypeChar", ExCommands.ExTypeChar),
	new CmdDef("CharTrans", ExCommands.ExCharTrans),
	new CmdDef("LineTrans", ExCommands.ExLineTrans),
	new CmdDef("BlockTrans", ExCommands.ExBlockTrans),
	new CmdDef("DesktopSave", ExCommands.ExDesktopSave),
	new CmdDef("DesktopSaveAs", ExCommands.ExDesktopSaveAs),
	new CmdDef("ChildClose", ExCommands.ExChildClose),
	new CmdDef("BufListFileSave", ExCommands.ExBufListFileSave),
	new CmdDef("BufListFileClose", ExCommands.ExBufListFileClose),
	new CmdDef("BufListSearchCancel", ExCommands.ExBufListSearchCancel),
	new CmdDef("BufListSearchNext", ExCommands.ExBufListSearchNext),
	new CmdDef("BufListSearchPrev", ExCommands.ExBufListSearchPrev),
	new CmdDef("ViewModeMap", ExCommands.ExViewModeMap),
	new CmdDef("ClearMessages", ExCommands.ExClearMessages),
	new CmdDef("BlockUnTab", ExCommands.ExBlockUnTab),
	new CmdDef("BlockEnTab", ExCommands.ExBlockEnTab),
	new CmdDef("TagFind", ExCommands.ExTagFind),
	new CmdDef("TagFindWord", ExCommands.ExTagFindWord),
	new CmdDef("TagNext", ExCommands.ExTagNext),
	new CmdDef("TagPrev", ExCommands.ExTagPrev),
	new CmdDef("TagPop", ExCommands.ExTagPop),
	new CmdDef("TagLoad", ExCommands.ExTagLoad),
	new CmdDef("TagClear", ExCommands.ExTagClear),
	new CmdDef("TagGoto", ExCommands.ExTagGoto),
	new CmdDef("BlockMarkFunction", ExCommands.ExBlockMarkFunction),
	new CmdDef("IndentFunction", ExCommands.ExIndentFunction),
	new CmdDef("MoveFunctionPrev", ExCommands.ExMoveFunctionPrev),
	new CmdDef("MoveFunctionNext", ExCommands.ExMoveFunctionNext),
	new CmdDef("Search", ExCommands.ExSearch),
	new CmdDef("SearchB", ExCommands.ExSearchB),
	new CmdDef("SearchRx", ExCommands.ExSearchRx),
	new CmdDef("SearchAgain", ExCommands.ExSearchAgain),
	new CmdDef("SearchAgainB", ExCommands.ExSearchAgainB),
	new CmdDef("SearchReplace", ExCommands.ExSearchReplace),
	new CmdDef("SearchReplaceB", ExCommands.ExSearchReplaceB),
	new CmdDef("SearchReplaceRx", ExCommands.ExSearchReplaceRx),
	new CmdDef("InsertDate", ExCommands.ExInsertDate),
	new CmdDef("InsertUid", ExCommands.ExInsertUid),
	new CmdDef("FrameNew", ExCommands.ExFrameNew),
	new CmdDef("FrameClose", ExCommands.ExFrameClose),
	new CmdDef("FrameNext", ExCommands.ExFrameNext),
	new CmdDef("FramePrev", ExCommands.ExFramePrev),
	new CmdDef("ShowHelpWord", ExCommands.ExShowHelpWord),
	new CmdDef("ShowHelp", ExCommands.ExShowHelp),
	new CmdDef("ConfigRecompile", ExCommands.ExConfigRecompile),
	new CmdDef("PlaceGlobalBookmark", ExCommands.ExPlaceGlobalBookmark),
	new CmdDef("RemoveGlobalBookmark", ExCommands.ExRemoveGlobalBookmark),
	new CmdDef("GotoGlobalBookmark", ExCommands.ExGotoGlobalBookmark),
	new CmdDef("MoveBeginOrNonWhite", ExCommands.ExMoveBeginOrNonWhite),
	new CmdDef("MoveBeginLinePageFile", ExCommands.ExMoveBeginLinePageFile),
	new CmdDef("MoveEndLinePageFile", ExCommands.ExMoveEndLinePageFile),
	new CmdDef("PushGlobalBookmark", ExCommands.ExPushGlobalBookmark),
	new CmdDef("PopGlobalBookmark", ExCommands.ExPopGlobalBookmark),
	new CmdDef("SetCIndentStyle", ExCommands.ExSetCIndentStyle),
	new CmdDef("SetIndentWithTabs", ExCommands.ExSetIndentWithTabs),
	new CmdDef("RunCompiler", ExCommands.ExRunCompiler),
	new CmdDef("FoldCreateAtRoutines", ExCommands.ExFoldCreateAtRoutines),
	new CmdDef("LineCenter", ExCommands.ExLineCenter),
	new CmdDef("RunProgramAsync", ExCommands.ExRunProgramAsync),

	new CmdDef("ListMark", ExCommands.ExListMark),
	new CmdDef("ListUnmark", ExCommands.ExListUnmark),
	new CmdDef("ListToggleMark", ExCommands.ExListToggleMark),
	new CmdDef("ListMarkAll", ExCommands.ExListMarkAll),
	new CmdDef("ListUnmarkAll", ExCommands.ExListUnmarkAll),
	new CmdDef("ListToggleMarkAll", ExCommands.ExListToggleMarkAll),

	new CmdDef("Cvs", ExCommands.ExCvs),
	new CmdDef("RunCvs", ExCommands.ExRunCvs),
	new CmdDef("ViewCvs", ExCommands.ExViewCvs),
	new CmdDef("ClearCvsMessages", ExCommands.ExClearCvsMessages),
	new CmdDef("CvsDiff", ExCommands.ExCvsDiff),
	new CmdDef("RunCvsDiff", ExCommands.ExRunCvsDiff),
	new CmdDef("ViewCvsDiff", ExCommands.ExViewCvsDiff),
	new CmdDef("CvsCommit", ExCommands.ExCvsCommit),
	new CmdDef("RunCvsCommit", ExCommands.ExRunCvsCommit),
	new CmdDef("ViewCvsLog", ExCommands.ExViewCvsLog),
	//#if 0
	//new CmdDef("ShowMsg", ExCommands.ExShowMsg),
	/*
	new CmdDef("BlockReadPipe", ExCommands.ExBlockReadPipe),
	new CmdDef("BlockWritePipe", ExCommands.ExBlockWritePipe),
	new CmdDef("BlockPipe", ExCommands.ExBlockPipe),
	*/
	//#endif
	};
	
	
	
}
