package ru.dz.jfte;

public class EBufferFlags implements ModeDefs 
{
	int num[];// = new Integer [BFI_COUNT];
	String str[];// = new String[BFS_COUNT];
	char WordChars[] = null;
	char CapitalChars[] = null;

	EBufferFlags(int [] n, String [] s, char [] w, char [] c)
	{
		num = n;
		str = s;
		WordChars = w;
		CapitalChars = c;
	}

	EBufferFlags()
	{
		num = defaultNumFlags;
		str = defaultStringFlags;
	}

	static int [] defaultNumFlags =     	    {
			1,                  // AutoIndent
			1,                  // InsertOn
			0,                  // DrawOn
			1,                  // HilitOn
			1,                  // ExpandTabs
			0,                  // Trim
			8,                  // TabSize
			HILIT_PLAIN,        // HilitMode
			INDENT_PLAIN,       // IndentMode
			0,                  // ShowTab
			10,                 // LineChar
			//#if !defined(UNIX)
			13,                 // StripChar
			//#else
			//        -1,
			//#endif
			1,                  // AddLine
			//#if !defined(UNIX)
			//        1,                  // AddStrip
			//#else
			0,
			//#endif
			0,                  // ForceNewLine
			0,                  // HardMode
			1,                  // UndoRedo
			0,                  // ReadOnly
			0,                  // AutoSave
			1,                  // KeepBackups
			-1,                 // LoadMargin
			256,                // Max Undo/Redo Commands
			1,                  // MatchCase
			0,                  // BackKillTab
			0,                  // DelKillTab
			1,                  // BackSpUnindent
			0,                  // SpaceTabs
			1,                  // IndentWTabs
			1,                  // Wrap.LeftMargin
			72,                 // Wrap.RightMargin
			1,                  // See Thru Sel
			0,                  // WordWrap
			0,                  // ShowMarkers
			1,                  // CursorThroughTabs
			0,                  // Save Folds
			0,                  // MultiLineHilit
			0,                  // AutoHilitParen
			0,                  // Abbreviations
			0,                  // BackSpKillBlock
			0,                  // DeleteKillBlock
			1,                  // PersistentBlocks
			0,                  // InsertKillBlock
			0,                  // UndoMoves
			//#ifdef UNIX
			//        0,                  // DetectLineSep
			//#else
			1,
			//#endif
			0,                  // trim on save
			0,                  // save bookmarks
			1,                  // HilitTags
			0,                  // ShowBookmarks
	}; 

	static String [] defaultStringFlags =
		{
				"",                  // Routine Regexp
				"",                  // DefFindOpt
				"",                  // DefFindReplaceOpt
				"",                  // comment start (folds)
				"",                  // comment end (folds)
				"",                  // filename rx
				"",                  // firstline rx
				""                   // compile command
		};    

	static char DefaultWordChars[] = new char[256];
	static char DefaultCapitalChars[] = new char[256];

	static EBufferFlags DefaultBufferFlags = new EBufferFlags( defaultNumFlags, defaultStringFlags, DefaultWordChars, DefaultCapitalChars);

	static  {
		for (int i = 0; i < 256; i++)
		{
			if (Character.isAlphabetic(i) || Character.isDigit(i) || (i == '_')) {
				ModeDefs.WSETBIT(DefaultWordChars, i, 1);
				if ((i >= 'A' && i <= 'Z') || Character.isUpperCase(i))
					ModeDefs.WSETBIT(DefaultCapitalChars, i, 1);
			}
		}
	}


};

