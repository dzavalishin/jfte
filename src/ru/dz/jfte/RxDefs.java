package ru.dz.jfte;

public interface RxDefs {


	/*
	 * Operator:
	 *
	 * ^            Match the beginning of line
	 * $            Match the end of line
	 * .            Match any character
	 * [ ]          Match characters in set
	 * [^ ]         Match characters not in set
	 * ?            Match previous pattern 0 or 1 times (greedy)
	 * |            Match previous or next pattern
	 * @            Match previous pattern 0 or more times (non-greedy)
	 * #            Match previous pattern 1 or more times (non-greedy)
	 * *            Match previous pattern 0 or more times (greedy)
	 * +            Match previous pattern 1 or more times (greedy)
	 * { }          Group characters to form one pattern
	 * ( )          Group and remember
	 * \            Quote next character (only of not a-z)
	 * <            Match beginning of a word
	 * >            Match end of a word
	 * \x##         Match character with ASCII code ## (hex)
	 * \N###        Match ascii code ### (dec)
	 * \o###        Match ascii code
	 * \a           Match \a              \r           Match 0x13 (cr)
	 * \b           Match \b              \t           Match 0x09 (tab)
	 * \f           Match \f              \v           Match \v
	 * \n           Match 0x10 (lf)       \e           Match escape (^E)
	 * \s           Match whitespace (cr/lf/tab/space)
	 * \S           Match nonwhitespace (!\S)
	 * \w           Match word character
	 * \W           Match non-word character
	 * \d           Match digit character
	 * \D           Match non-digit character
	 * \U           Match uppercase
	 * \L           Match lowercase
	 * \C           Match case sensitively from here on
	 * \c           Match case ingnore from here on
	 */

	public static final int  RE_NOTHING         =0;  // nothing
	public static final int  RE_JUMP            =1;  // jump to
	public static final int  RE_BREAK           =2;  // break |
	public static final int  RE_ATBOL           =3;  // match at beginning of line
	public static final int  RE_ATEOL           =4;  // match at end of line
	public static final int  RE_ATBOW           =5;  // match beginning of word
	public static final int  RE_ATEOW           =6;  // match end of word
	public static final int  RE_CASE            =7;  // match case sensitively from here
	public static final int  RE_NCASE           =8;  // ignore case from here.
	public static final int  RE_END            =31;  // end of regexp

	public static final int  RE_ANY       = (32 +  1); // match any character
	public static final int  RE_INSET     = (32 +  2); // match if in set
	public static final int  RE_NOTINSET  = (32 +  3); // match if not in set
	public static final int  RE_CHAR      = (32 +  4); // match character string
	public static final int  RE_WSPACE    = (32 +  5); // match whitespace
	public static final int  RE_NWSPACE   = (32 +  6); // match whitespace
	public static final int  RE_UPPER     = (32 +  7); // match uppercase
	public static final int  RE_LOWER     = (32 +  8); // match lowercase
	public static final int  RE_DIGIT     = (32 +  9); // match digit
	public static final int  RE_NDIGIT    = (32 + 10); // match non-digit
	public static final int  RE_WORD      = (32 + 11); // match word
	public static final int  RE_NWORD     = (32 + 12); // match non-word

	public static final int  RE_GROUP         =256;  // grouping
	public static final int  RE_OPEN          =512;  // open (
	public static final int  RE_CLOSE        =1024;  // close )
	public static final int  RE_MEM          =2048;  // store () match

	public static final int  RE_BRANCH       =4096;
	public static final int  RE_GREEDY       =2048;  // do a greedy match (as much as possible)

	public static final int  NSEXPS           = 64;  // for replace only 0-9

	public static final int  RX_CASE         = 1;  // matchcase

	
}
