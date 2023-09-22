package ru.dz.jfte;

public interface GuiDefs {

	static final int  GUIDLG_CHOICE      = 0x00000001;
	static final int  GUIDLG_PROMPT      = 0x00000002;
	static final int  GUIDLG_PROMPT2     = 0x00000004;
	static final int  GUIDLG_FILE        = 0x00000008;
	static final int  GUIDLG_FIND        = 0x00000010;
	static final int  GUIDLG_FINDREPLACE = 0x00000020;


	static final int  GF_OPEN    = 0x0001;
	static final int  GF_SAVEAS  = 0x0002;


	static final int  GPC_NOTE    = 0x0000;
	static final int  GPC_CONFIRM = 0x0001;
	static final int  GPC_WARNING = 0x0002;
	static final int  GPC_ERROR   = 0x0004;
	static final int  GPC_FATAL   = 0x0008;

	static final int  SEARCH_BACK    = 0x00000001;   // reverse (TODO for regexps)
	static final int  SEARCH_RE      = 0x00000002;   // use regexp
	static final int  SEARCH_NCASE   = 0x00000004;   // case
	static final int  SEARCH_GLOBAL  = 0x00000008;   // start from beggining (or end if BACK)
	static final int  SEARCH_BLOCK   = 0x00000010;   // search in block
	static final int  SEARCH_NEXT    = 0x00000020;   // next match
	static final int  SEARCH_NASK    = 0x00000040;   // ask before replacing
	static final int  SEARCH_ALL     = 0x00000080;   // search all
	static final int  SEARCH_REPLACE = 0x00000100;   // do a replace operation
	static final int  SEARCH_JOIN    = 0x00000200;   // join line
	static final int  SEARCH_DELETE  = 0x00000400;   // delete line
	static final int  SEARCH_CENTER  = 0x00001000;   // center finds
	static final int  SEARCH_NOPOS   = 0x00002000;   // don't move the cursor
	static final int  SEARCH_WORDBEG = 0x00004000;   // match at beginning of words only
	static final int  SEARCH_WORDEND = 0x00008000;   // match at end of words only
	static final int  SEARCH_WORD    = (SEARCH_WORDBEG | SEARCH_WORDEND);
	//= 0x00000800   // search words
	//static final int  SEARCH_LINE    = 0x00002000   // search on current line only TODO
	//static final int  SEARCH_WRAP    = 0x00004000   // similiar to GLOBAL, but goes to start
	// only when match from current position fails TODO
	//static final int  SEARCH_BOL     = 0x00008000   // search at line start
	//static final int  SEARCH_EOL     = 0x00010000   // search at line end

	static final int  MAXSEARCH = 512;

	static final String DESKTOP_NAME = ".fte-desktop";
	
	
}
