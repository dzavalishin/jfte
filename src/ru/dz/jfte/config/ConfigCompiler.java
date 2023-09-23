package ru.dz.jfte.config;

import ru.dz.jfte.c.ByteArrayPtr;

public class ConfigCompiler implements ConfigCompilerDefs 
{

	public ConfigCompiler() {
		// TODO Auto-generated constructor stub
	}

	
	int slen(String s) { return ((s!=null) ? (s.length()) : 0); }

	static class ExMacro {
	    String Name;
	} 

	unsigned int CMacros = 0;
	ExMacro []Macros = 0;

	//FILE *output = 0;
	int lntotal = 0;
	long offset = -1;
	long pos = 0;
	String XTarget = "";
	String StartDir = "";


	class CurPos {
	    int sz;
	    //String a;
	    //String c;
	    //String z;
	    ByteArrayPtr a, c;
	    int line;
	    String name; // filename
	}

	void cleanup(int xerrno) {
	    if (output)
	        fclose(output);
	    if (XTarget[0] != 0)
	        unlink(XTarget);
	    System.exit(xerrno);
	}

	void Fail(CurPos cp, String fmt, String... s) {

		String b = String.format(fmt, s);
		
	    System.err.printf("%s:%d: Error: %s\n", cp.name, cp.line, b);
	    cleanup(1);
	}

	//int LoadFile(String WhereName, String CfgName, int Level = 1);
	
	void PutObject(CurPos cp, int xtag, int xlen, Object obj) {
	    unsigned char tag = (unsigned char)xtag;
	    unsigned short len = (unsigned short)xlen;
	    unsigned char l[2];

	    l[0] = len & 0xFF;
	    l[1] = (len >> 8) & 0xFF;

	    if (fwrite(&tag, 1, 1, output) != 1 ||
	        fwrite(l, 2, 1, output) != 1 ||
	        fwrite(obj, 1, len, output) != len)
	    {
	        Fail(cp, "Disk full!");
	    }
	    pos += 1 + 2 + len;
	    if (offset != -1 && pos >= offset) {
	        Fail(cp, "Error location found at %ld", pos);
	    }
	}

	void PutNull(CurPos cp, int xtag) {
	    PutObject(cp, xtag, 0, 0);
	}

	void PutString(CurPos cp, int xtag, String str) {
	    PutObject(cp, xtag, slen(str), str);
	}

	void PutNumber(CurPos cp, int xtag, long num) {
	    unsigned long l = num;
	    unsigned char b[4];

	    b[0] = (unsigned char)(l & 0xFF);
	    b[1] = (unsigned char)((l >> 8) & 0xFF);
	    b[2] = (unsigned char)((l >> 16) & 0xFF);
	    b[3] = (unsigned char)((l >> 24) & 0xFF);
	    PutObject(cp, xtag, 4, b);
	}

	public static void main(String []argv) {
	    char Source[MAXPATH];
	    char Target[MAXPATH];
	    String p = argv[1];
	    int n = 1;

	    fprintf(stderr, PROG_CFTE " " VERSION "\n" COPYRIGHT "\n");
	    if (argc < 2 || argc > 4) {
	        fprintf(stderr, "Usage: " PROG_CFTE " [-o<offset>] "
	#ifndef UNIX
	                "config/"
	#endif
	                "main.fte [fte-new.cnf]\n");
	        exit(1);
	    }

	    DefineWord("OS_JAVA");

	    if (strncmp(p, "-o", 2) == 0) {
	        p += 2;
	        offset = atol(p);
	        n++;
	    }
	    if (n == 1 && argc == 4) {
	        fprintf(stderr, "Invalid option '%s'\n", argv[1]);
	        exit(1);
	    }
	    strcpy(Source, argv[n++]);
	    strcpy(Target, "fte-new.cnf");
	    if (n < argc)
	        strcpy(Target, argv[n++]);

	    JustDirectory(Target, XTarget);
	    Slash(XTarget, 1);
	    sprintf(XTarget + strlen(XTarget), "cfte%ld.tmp", (long)getpid());
	    output = fopen(XTarget, "wb");
	    if (output == 0) {
	        fprintf(stderr, "Cannot create '%s', errno=%d!\n", XTarget, errno);
	        cleanup(1);
	    }

	    unsigned char b[4];

	    b[0] = b[1] = b[2] = b[3] = 0;

	    if (fwrite(b, sizeof(b), 1, output) != 1) {
	        fprintf(stderr, "Disk full!");
	        cleanup(1);
	    }

	    unsigned long l = VERNUM;

	    b[0] = (unsigned char)(l & 0xFF);
	    b[1] = (unsigned char)((l >> 8) & 0xFF);
	    b[2] = (unsigned char)((l >> 16) & 0xFF);
	    b[3] = (unsigned char)((l >> 24) & 0xFF);

	    if (fwrite(b, 4, 1, output) != 1) {
	        fprintf(stderr, "Disk full!");
	        cleanup(1);
	    }
	    pos = 2 * 4;

	    fprintf(stderr, "Compiling to '%s'\n", Target);
	    /*{
	        char PrevDir[MAXPATH];
	        sprintf(PrevDir, "%s/..", Target);
	        ExpandPath(PrevDir, StartDir);
	        Slash(StartDir, 1);
	    }*/

	    ExpandPath("."
	/*#ifdef UNIX
	               "."
	#endif */
	               , StartDir);
	    StartDir = Slash(StartDir, 1);

	    {
	        CurPos cp;
	        char FSource[MAXPATH];

	        if (ExpandPath(Source, FSource) != 0) {
	            fprintf(stderr, "Could not expand path %s\n", Source);
	            exit(1);
	        }

	        cp.sz = 0;
	        cp.c = 0;
	        cp.a = cp.c = 0;
	        cp.z = cp.a + cp.sz;
	        cp.line = 0;
	        cp.name = "<cfte-start>";

	        PutString(cp, CF_STRING, FSource);
	    }

	    if (LoadFile(StartDir, Source, 0) != 0) {
	        fprintf(stderr, "\nCompile failed\n");
	        cleanup(1);
	    }

	    l = CONFIG_ID;
	    b[0] = (unsigned char)(l & 0xFF);
	    b[1] = (unsigned char)((l >> 8) & 0xFF);
	    b[2] = (unsigned char)((l >> 16) & 0xFF);
	    b[3] = (unsigned char)((l >> 24) & 0xFF);
	    fseek(output, 0, SEEK_SET);
	    fwrite(b, 4, 1, output);
	    fclose(output);

	    if (unlink(Target) != 0 && errno != ENOENT) {
	        fprintf(stderr, "Remove of '%s' failed, result left in %s, errno=%d\n",
	                Target, XTarget, errno);
	        exit(1);
	    }

	    if (rename(XTarget, Target) != 0) {
	        fprintf(stderr, "Rename of '%s' to '%s' failed, errno=%d\n",
	                XTarget, Target, errno);
	        exit(1);
	    }

	    fprintf(stderr, "\nDone.\n");
	    return 0;
	}


	int Lookup(OrdLookup [] where, String what) {
	    int i;

	    for (i = 0; where[i].Name != 0; i++) {
	        if (strcmp(what, where[i].Name) == 0)
	            return where[i].num;
	    }
//	    fprintf(stderr, "\nBad name: %s (i = %d)\n", what, i);
	    return -1;
	}


	typedef char Word[64];


	String *words = 0;
	unsigned int wordCount = 0;

	int DefinedWord(String w) {
	    if (words == 0 || wordCount == 0)
	        return 0;
	    for (unsigned int i = 0; i < wordCount; i++)
	        if (strcmp(w, words[i]) == 0)
	            return 1;
	    return 0;
	}

	void DefineWord(String w) {
	    if (!w || !w[0])
	        return ;
	    if (!DefinedWord(w)) {
	        words = (String *)realloc(words, sizeof (String ) * (wordCount + 1));
	        assert(words != 0);
	        words[wordCount] = strdup(w);
	        assert(words[wordCount] != 0);
	        wordCount++;
	    }
	}

	int colorCount;
	struct _color {
	    String colorName;
	    String colorValue;
	} *colors;

	int DefineColor(String name, String value) {
	    if (!name || !value)
	        return 0;
	    colors = (struct _color *)realloc(colors, sizeof (struct _color) * (colorCount + 1));
	    assert(colors != 0);
	    colors[colorCount].colorName = strdup(name);
	    colors[colorCount].colorValue = strdup(value);
	    assert(colors != NULL);
	    assert(colors[colorCount].colorName != 0);
	    assert(colors[colorCount].colorValue != 0);
	    colorCount++;
	    return 1;
	}

	String DefinedColor(String name) {
	    if (colors == 0 || colorCount == 0)
	        return 0;
	    for (int i = 0; i < colorCount; i++)
	        if (strcmp(name, colors[i].colorName) == 0)
	            return colors[i].colorValue;
	    return 0;
	}

	String GetColor(CurPos cp, String name) {
	    String c;
	    static char color[4];

	    // add support for fore:back and remove it from fte itself
	    if ((c = strchr(name, ' ')) != NULL) {
	    } else if ((c = strchr(name, ':')) != NULL) {
	        char clr[4];
	        *c++ = 0;
	        clr[0] = GetColor(cp, name)[0];
	        clr[1] = ' ';
	        clr[2] = GetColor(cp, c)[2];
	        clr[3] = 0;

	        memcpy((void *)color, (void *)clr, sizeof(color));
	        name = (String )color;
	    } else {
	        String p = DefinedColor(name);
	        if (!p)
	            Fail(cp, "Unknown symbolic color %s", name);
	        name = p;
	    }
	    if (!isxdigit(name[0]) &&
	        name[1] != ' ' &&
	        !isxdigit(name[2]) &&
	        name[3] != 0)
	    {
	        Fail(cp, "malformed color specification: %s", name);
	    }
	    return name;
	}

	int GetWord(CurPos cp, String w) {
	    String p = w;
	    int len = 0;

	    while (len < int(sizeof(Word)) && cp.c < cp.z &&
	           ((*cp.c >= 'a' && *cp.c <= 'z') ||
	            (*cp.c >= 'A' && *cp.c <= 'Z') ||
	            (*cp.c >= '0' && *cp.c <= '9') ||
	            (*cp.c == '_')))
	    {
	        *p++ = *cp.c++;
	        len++;
	    }
	    if (len == sizeof(Word)) return -1;
	    *p = 0;
	    return 0;
	}


	int Parse(CurPos cp) {
	    while (cp.c < cp.z) {
	        switch (*cp.c) {
	/* #ifndef UNIX
	        case '\x1A': // ^Z :-*  
	        return P_EOF;
	#endif */
	        case '#':
	            while (cp.c < cp.z && *cp.c != '\n') cp.c++;
	            break;
	        case '%':
	            cp.c++;
	            if (cp.c + 7 < cp.z && strncmp(cp.c, "define(", 7) == 0) {
	                Word w;
	                cp.c += 7;

	                while (cp.c < cp.z && *cp.c != ')') {
	                    GetWord(cp, w);
	                    //printf("define '%s'\n", w);
	                    DefineWord(w);
	                    if (cp.c < cp.z && *cp.c != ',' && *cp.c != ')' )
	                        Fail(cp, "unexpected: %c", cp.c[0]);
	                    if (cp.c < cp.z && *cp.c == ',')
	                        cp.c++;
	                }
	                cp.c++;
	/*            } else if (cp.c + 6 && strcmp(cp.c, "undef(", 6) == 0) {
	                Word w;
	                cp.c += 6;

	                while (cp.c < cp.z && *cp.c != ')') {
	                    GetWord(cp, w);
	                    UndefWord(w);
	                }*/
	            } else if (cp.c + 3 < cp.z && strncmp(cp.c, "if(", 3) == 0) {
	                Word w;
	                int wasWord = 0;
	                cp.c += 3;

	                while (cp.c < cp.z && *cp.c != ')') {
	                    int neg = 0;
	                    if (*cp.c == '!') {
	                        cp.c++;
	                        neg = 1;
	                    }
	                    GetWord(cp, w);
	                    if (DefinedWord(w))
	                        wasWord = 1;
	                    if (neg)
	                        wasWord = wasWord ? 0 : 1;
	                    /*if (wasWord)
	                        printf("yes '%s'\n", w);
	                    else
	                        printf("not '%s'\n", w);*/

	                    if (cp.c < cp.z && *cp.c != ',' && *cp.c != ')' )
	                        Fail(cp, "unexpected: %c", cp.c[0]);
	                    if (cp.c < cp.z && *cp.c == ',')
	                        cp.c++;
	                }
	                cp.c++;
	                if (!wasWord) {
	                    int nest = 1;
	                    while (cp.c < cp.z) {
	                        if (*cp.c == '\n') {
	                            cp.line++;
	                            lntotal++;
	                        } else if (*cp.c == '%') {
	                            if (cp.c + 6 < cp.z &&
	                                strncmp(cp.c, "%endif", 6) == 0)
	                            {
	                                cp.c += 6;
	                                if (--nest == 0)
	                                    break;
	                            }
	                            if (cp.c + 3 < cp.z &&
	                                strncmp(cp.c, "%if", 3) == 0)
	                            {
	                                cp.c += 3;
	                                ++nest;
	                            }
	                        } else if (*cp.c == '#') {
	                            // we really shouldn't process hashed % directives
	                            while( cp.c < cp.z && *cp.c != '\n' ) cp.c++;

	                            // workaround to make line numbering correct
	                            cp.line++;
	                            lntotal++;
	                        }
	                        cp.c++;
	                    }
	                }
	            } else if (cp.c + 5 < cp.z && strncmp(cp.c, "endif", 5) == 0) {
	                cp.c += 5;
	            }
	            if (cp.c < cp.z && *cp.c != '\n' && *cp.c != '\r')
	                Fail(cp, "syntax error %30.30s", cp.c);
	            break;
	        case '\n':
	            cp.line++;
	            lntotal++;
	        case ' ':
	        case '\t':
	        case '\r':
	            cp.c++;
	            break;
	        case '=': return P_ASSIGN;
	        case ';': return P_EOS;
	        case ',': return P_COMMA;
	        case ':': return P_COLON;
	        case '.': return P_DOT;
	        case '\'':
	        case '"':
	        case '`':
	        case '/': return P_STRING;
	        case '[': return P_KEYSPEC;
	        case '{': return P_OPENBRACE;
	        case '}': return P_CLOSEBRACE;
	        case '?': return P_QUEST;
	        case '$': return P_VARIABLE;
	        case '-': case '+':
	        case '0': case '1': case '2': case '3': case '4':
	        case '5': case '6': case '7': case '8': case '9': return P_NUMBER;
	        default:
	            if ((*cp.c >= 'a' && *cp.c <= 'z') ||
	                (*cp.c >= 'A' && *cp.c <= 'Z') ||
	                (*cp.c == '_'))
	                return P_WORD;
	            else
	                return P_SYNTAX;
	        }
	    }
	    return P_EOF;
	}

	void GetOp(CurPos cp, int what) {
	    switch (what) {
	    case P_COMMA:
	    case P_OPENBRACE:
	    case P_CLOSEBRACE:
	    case P_ASSIGN:
	    case P_EOS:
	    case P_COLON:
	    case P_QUEST:
	    case P_VARIABLE:
	    case P_DOT:
	        cp.c++;
	        break;
	    }
	}

	String GetString(CurPos cp) {
	    char c = *cp.c;
	    String p = cp.c;
	    String d = cp.c;
	    int n;

	    if (c == '[') c = ']';

	    cp.c++; // skip '"`
	    while (cp.c < cp.z) {
	        if (*cp.c == '\\') {
	            if (c == '/')
	                *p++ = *cp.c;
	            cp.c++;
	            if (cp.c == cp.z) return 0;
	            if (c == '"') {
	                switch (*cp.c) {
	                case 'e': *cp.c = '\x1B'; break;
	                case 't': *cp.c = '\t'; break;
	                case 'r': *cp.c = '\r'; break;
	                case 'n': *cp.c = '\n'; break;
	                case 'b': *cp.c = '\b'; break;
	                case 'v': *cp.c = '\v'; break;
	                case 'a': *cp.c = '\a'; break;
	                case 'x':
	                    cp.c++;
	                    if (cp.c == cp.z) return 0;
	                    if (*cp.c >= '0' && *cp.c <= '9') n = *cp.c - '0';
	                    else if (*cp.c >='a' && *cp.c <= 'f') n = *cp.c - 'a' + 10;
	                    else if (*cp.c >='A' && *cp.c <= 'F') n = *cp.c - 'A' + 10;
	                    else return 0;
	                    cp.c++;
	                    if (cp.c == cp.z) cp.c--;
	                    else if (*cp.c >= '0' && *cp.c <= '9') n = n * 16 + *cp.c - '0';
	                    else if (*cp.c >= 'a' && *cp.c <= 'f') n = n * 16 + *cp.c - 'a' + 10;
	                    else if (*cp.c >= 'A' && *cp.c <= 'F') n = n * 16 + *cp.c - 'A' + 10;
	                    else cp.c--;
	                    *cp.c = n;
	                    break;
	                }
	            }
	        } else if (*cp.c == c) {
	            cp.c++;
	            *p = 0;
	            return d;
	        } else if (*cp.c == '\n') return 0;
	        if (*cp.c == '\n') cp.line++;
	        if (*cp.c == '\r') {
	            cp.c++;
	            if (cp.c == cp.z) return 0;
	        }
	        *p++ = *cp.c++;
	    }
	    return 0;
	}

	int GetNumber(CurPos cp) {
	    int value = 0;
	    int neg = 0;

	    if (cp.c < cp.z && *cp.c == '-' || *cp.c == '+') {
	        if (*cp.c == '-') neg = 1;
	        cp.c++;
	    }
	    while (cp.c < cp.z && (*cp.c >= '0' && *cp.c <= '9')) {
	        value = value * 10 + (*cp.c - '0');
	        cp.c++;
	    }
	    return neg ? -value : value;
	}

	int CmdNum(String Cmd) {
	    unsigned int i;

	    for (i = 0;
	         i < sizeof(Command_Table) / sizeof(Command_Table[0]);
	         i++)
	        if (strcmp(Cmd, Command_Table[i].Name) == 0)
	            return Command_Table[i].CmdId;
	    for (i = 0; i < CMacros; i++)
	        if (Macros[i].Name && (strcmp(Cmd, Macros[i].Name)) == 0)
	            return i | CMD_EXT;
	    return 0; // Nop
	}

	int NewCommand(String Name) {
	    if (Name == 0)
	        Name = "";
	    Macros = (ExMacro ) realloc(Macros, sizeof(ExMacro) * (1 + CMacros));
	    Macros[CMacros].Name = strdup(Name);
	    CMacros++;
	    return CMacros - 1;
	}

	int ParseCommands(CurPos cp, String Name) {
	    //if (!Name)
	    //    return 0;
	    Word cmd;
	    int p;
	    long Cmd = NewCommand(Name) | CMD_EXT;

	    long cnt;
	    long ign = 0;

	    PutNumber(cp, CF_INT, Cmd);
	    GetOp(cp, P_OPENBRACE);
	    cnt = 1;
	    while (1) {
	        p = Parse(cp);
	        if (p == P_CLOSEBRACE) break;
	        if (p == P_EOF) Fail(cp, "Unexpected EOF");

	        if (p == P_DOT) {
	            GetOp(cp, P_DOT);
	            PutNull(cp, CF_CONCAT);
	        } else if (p == P_NUMBER) {
	            long num = GetNumber(cp);
	            if (Parse(cp) != P_COLON) {
	                PutNumber(cp, CF_INT, num);
	            } else {
	                cnt = num;
	                GetOp(cp, P_COLON);
	            }
	        } else if (p == P_WORD) {
	            long Command;

	            if (GetWord(cp, cmd) == -1) Fail(cp, "Syntax error");
	            Command = CmdNum(cmd);
	            if (Command == 0)
	                Fail(cp, "Unrecognised command: %s", cmd);
	            PutNumber(cp, CF_COMMAND, Command);
	            PutNumber(cp, CF_INT, cnt);
	            PutNumber(cp, CF_INT, ign);
	            ign = 0;
	            cnt = 1;
	        } else if (p == P_STRING) {
	            String s = GetString(cp);
	            PutString(cp, CF_STRING, s);
	        } else if (p == P_QUEST) {
	            ign = 1;
	            GetOp(cp, P_QUEST);
	        } else if (p == P_VARIABLE) {
	            GetOp(cp, P_VARIABLE);
	            if (Parse(cp) != P_WORD) Fail(cp, "Syntax error (variable name expected)");
	            Word w;
	            if (GetWord(cp, w) != 0) Fail(cp, "Syntax error (bad variable name)");
	            long var = Lookup(CfgVar, w);
	            if (var == -1) Fail(cp, "Unrecognised variable");
	            PutNumber(cp, CF_VARIABLE, var);
	        } else if (p == P_EOS) {
	            GetOp(cp, P_EOS);
	            cnt = 1;
	        } else
	            Fail(cp, "Syntax error");
	    }
	    GetOp(cp, P_CLOSEBRACE);
	    return 0;
	}

	int ParseConfigFile(CurPos cp) {
	    Word w = "";
	    String s = 0;
	    int p = 0;

	    Word ObjName = "", UpMode = "";

	    while (1) {
	        p = Parse(cp);

	        switch (p) {
	        case P_WORD:
	            if (GetWord(cp, w) != 0) Fail(cp, "Syntax error");
	            switch (Lookup(CfgKW, w)) {
	            case K_SUB:
	                {
	                    Word Name;

	                    if (Parse(cp) != P_WORD) Fail(cp, "Syntax error");
	                    if (GetWord(cp, Name) != 0) Fail(cp, "Syntax error");
	                    PutString(cp, CF_SUB, Name);
	                    if (Parse(cp) != P_OPENBRACE) Fail(cp, "'{' expected");
	                    GetOp(cp, P_OPENBRACE);
	                    if (ParseCommands(cp, strdup(Name ? Name : "")) == -1)
	                        Fail(cp, "Parse failed");
	                    PutNull(cp, CF_END);
	                }
	                break;
	            case K_MENU:

	                {
	                    Word MenuName;
	                    //int menu = -1, item = -1;

	                    if (Parse(cp) != P_WORD) Fail(cp, "Syntax error");;
	                    if (GetWord(cp, MenuName) != 0) Fail(cp, "Syntax error");;
	                    if (Parse(cp) != P_OPENBRACE) Fail(cp, "'{' expected");
	                    GetOp(cp, P_OPENBRACE);

	                    PutString(cp, CF_MENU, MenuName);

	                    while (1) {
	                        p = Parse(cp);
	                        if (p == P_CLOSEBRACE) break;
	                        if (p == P_EOF) Fail(cp, "Unexpected EOF");
	                        if (p != P_WORD) Fail(cp, "Syntax error");

	                        if (GetWord(cp, w) != 0) Fail(cp, "Parse failed");
	                        switch (Lookup(CfgKW, w)) {
	                        case K_ITEM: // menu::item
	                            switch (Parse(cp)) {
	                            case P_EOS:
	                                PutNull(cp, CF_ITEM);
	                                break;
	                            case P_STRING:
	                                s = GetString(cp);
	                                PutString(cp, CF_ITEM, s);
	                                break;
	                            default:
	                                Fail(cp, "Syntax error");;
	                            }
	                            if (Parse(cp) == P_EOS) {
	                                GetOp(cp, P_EOS);
	                                break;
	                            }
	                            if (Parse(cp) != P_OPENBRACE)
	                                Fail(cp, "'{' expected");

	                            PutNull(cp, CF_MENUSUB);
	                            if (ParseCommands(cp, 0) == -1)
	                                Fail(cp, "Parse failed");
	                            PutNull(cp, CF_END);
	                            break;
	                        case K_SUBMENU: // menu::submenu
	                            if (Parse(cp) != P_STRING)
	                                Fail(cp, "String expected");
	                            s = GetString(cp);
	                            if (Parse(cp) != P_COMMA)
	                                Fail(cp, "',' expected");
	                            GetOp(cp, P_COMMA);
	                            if (Parse(cp) != P_WORD)
	                                Fail(cp, "Syntax error");
	                            if (GetWord(cp, w) == -1)
	                                Fail(cp, "Parse failed");

	                            PutString(cp, CF_SUBMENU, s);
	                            PutString(cp, CF_STRING, w);
	                            if (Parse(cp) != P_EOS)
	                                Fail(cp, "';' expected");
	                            GetOp(cp, P_EOS);
	                            break;

	                        case K_SUBMENUCOND: // menu::submenu
	                            if (Parse(cp) != P_STRING)
	                                Fail(cp, "String expected");
	                            s = GetString(cp);
	                            if (Parse(cp) != P_COMMA)
	                                Fail(cp, "',' expected");
	                            GetOp(cp, P_COMMA);
	                            if (Parse(cp) != P_WORD)
	                                Fail(cp, "Syntax error");
	                            if (GetWord(cp, w) == -1)
	                                Fail(cp, "Parse failed");

	                            PutString(cp, CF_SUBMENUCOND, s);
	                            PutString(cp, CF_STRING, w);
	                            if (Parse(cp) != P_EOS)
	                                Fail(cp, "';' expected");
	                            GetOp(cp, P_EOS);
	                            break;
	                        default:  // menu::
	                            Fail(cp, "Syntax error");
	                        }
	                    }
	                    GetOp(cp, P_CLOSEBRACE);
	                    PutNull(cp, CF_END);
	                }
	                break;
	            case K_EVENTMAP:
	                {
	                    if (Parse(cp) != P_WORD) Fail(cp, "Syntax error");
	                    if (GetWord(cp, ObjName) != 0) Fail(cp, "Parse failed");
	                    PutString(cp, CF_EVENTMAP, ObjName);

	                    UpMode[0] = 0;
	                    if (Parse(cp) == P_COLON) {
	                        GetOp(cp, P_COLON);
	                        if (Parse(cp) != P_WORD) Fail(cp, "Syntax error");
	                        if (GetWord(cp, UpMode) != 0) Fail(cp, "Parse failed");
	                    }
	                    PutString(cp, CF_PARENT, UpMode);
	                    if (Parse(cp) != P_OPENBRACE) Fail(cp, "'{' expected");
	                    GetOp(cp, P_OPENBRACE);

	                    while (1) {
	                        p = Parse(cp);
	                        if (p == P_CLOSEBRACE) break;
	                        if (p == P_EOF) Fail(cp, "Unexpected EOF");
	                        if (p != P_WORD) Fail(cp, "Syntax error");

	                        if (GetWord(cp, w) != 0) Fail(cp, "Parse failed");
	                        switch (Lookup(CfgKW, w)) {
	                        case K_KEY: // mode::key
	                            if (Parse(cp) != P_KEYSPEC) Fail(cp, "'[' expected");
	                            s = GetString(cp);
	                            PutString(cp, CF_KEY, s);
	                            if (Parse(cp) != P_OPENBRACE) Fail(cp, "'{' expected");
	                            PutNull(cp, CF_KEYSUB);
	                            if (ParseCommands(cp, 0) == -1) Fail(cp, "Parse failed");
	                            PutNull(cp, CF_END);
	                            break;

	                        case K_ABBREV:
	                            if (Parse(cp) != P_STRING) Fail(cp, "String expected");
	                            s = GetString(cp);
	                            PutString(cp, CF_ABBREV, s);
	                            switch (Parse(cp)) {
	                            case P_OPENBRACE:
	                                PutNull(cp, CF_KEYSUB);
	                                if (ParseCommands(cp, 0) == -1) Fail(cp, "Parse failed");
	                                PutNull(cp, CF_END);
	                                break;
	                            case P_STRING:
	                                s = GetString(cp);
	                                PutString(cp, CF_STRING, s);
	                                if (Parse(cp) != P_EOS) Fail(cp, "';' expected");
	                                GetOp(cp, P_EOS);
	                                break;
	                            default:
	                                Fail(cp, "Syntax error");
	                            }
	                            break;

	                        default:  // mode::
	                            if (Parse(cp) != P_ASSIGN) Fail(cp, "'=' expected");
	                            GetOp(cp, P_ASSIGN);

	                            switch (Parse(cp)) {
	                                /*  case P_NUMBER:
	                                 {
	                                 long var;
	                                 long num;

	                                 num = GetNumber(cp);
	                                 var = LookupEventNumber(w);
	                                 if (var == -1) return -1;
	                                 PutObj(cp, CF_SETVAR, sizeof(long), &var);
	                                 PutObj(cp, CF_INT, sizeof(long), &num);
	                                 }
	                                 break;*/
	                            case P_STRING:
	                                {
	                                    long var;

	                                    s = GetString(cp);
	                                    if (s == 0) Fail(cp, "String expected");
	                                    var = Lookup(event_string, w);
	                                    if (var == -1) Fail(cp, "Lookup of '%s' failed", w);
	                                    PutNumber(cp, CF_SETVAR, var);
	                                    PutString(cp, CF_STRING, s);
	                                }
	                                break;
	                            default:
	                                return -1;
	                            }
	                            if (Parse(cp) != P_EOS) return -1;
	                            GetOp(cp, P_EOS);
	                            break;
	                        }
	                    }
	                    GetOp(cp, P_CLOSEBRACE);
	                    PutNull(cp, CF_END);
	                }
	                break;

	            case K_COLORIZE:
	                {
	                    long LastState = -1;

	                    if (Parse(cp) != P_WORD) Fail(cp, "Syntax error");
	                    if (GetWord(cp, ObjName) != 0) Fail(cp, "Parse failed");
	                    PutString(cp, CF_COLORIZE, ObjName);

	                    UpMode[0] = 0;
	                    if (Parse(cp) == P_COLON) {
	                        GetOp(cp, P_COLON);
	                        if (Parse(cp) != P_WORD) Fail(cp, "Syntax error");
	                        if (GetWord(cp, UpMode) != 0) Fail(cp, "Parse failed");
	                    }
	                    PutString(cp, CF_PARENT, UpMode);
	                    if (Parse(cp) != P_OPENBRACE) Fail(cp, "'{' expected");
	                    GetOp(cp, P_OPENBRACE);

	                    while (1) {
	                        p = Parse(cp);
	                        if (p == P_CLOSEBRACE) break;
	                        if (p == P_EOF) Fail(cp, "Unexpected EOF");
	                        if (p != P_WORD) Fail(cp, "Syntax error");

	                        if (GetWord(cp, w) != 0) Fail(cp, "Parse failed");
	                        switch (Lookup(CfgKW, w)) {
	                        case K_COLOR: // mode::color
	                            if (Parse(cp) != P_OPENBRACE) Fail(cp, "'{' expected");
	                            GetOp(cp, P_OPENBRACE);
	                            PutNull(cp, CF_COLOR);

	                            while (1) {
	                                String sname, *svalue;
	                                long cidx;

	                                if (Parse(cp) == P_CLOSEBRACE) break;
	                                if (Parse(cp) != P_OPENBRACE) Fail(cp, "'{' expected");
	                                GetOp(cp, P_OPENBRACE);
	                                if (Parse(cp) != P_STRING) Fail(cp, "String expected");
	                                sname = GetString(cp);
	                                if ((cidx = Lookup(hilit_colors, sname)) == -1)
	                                    Fail(cp, "Lookup of '%s' failed", sname);
	                                PutNumber(cp, CF_INT, cidx);
	                                if (Parse(cp) != P_COMMA)
	                                    Fail(cp, "',' expected");
	                                GetOp(cp, P_COMMA);
	                                if (Parse(cp) != P_STRING) Fail(cp, "String expected");
	                                svalue = GetString(cp);
	                                svalue = GetColor(cp, svalue);
	                                PutString(cp, CF_STRING, svalue);
	                                if (Parse(cp) != P_CLOSEBRACE) Fail(cp, "'}' expected");
	                                GetOp(cp, P_CLOSEBRACE);
	                                if (Parse(cp) != P_COMMA)
	                                    break;
	                                else
	                                    GetOp(cp, P_COMMA);
	                            }
	                            if (Parse(cp) != P_CLOSEBRACE) Fail(cp, "'}' expected");
	                            GetOp(cp, P_CLOSEBRACE);
	                            if (Parse(cp) != P_EOS) Fail(cp, "';' expected");
	                            GetOp(cp, P_EOS);
	                            PutNull(cp, CF_END);
	                            break;

	                        case K_KEYWORD: // mode::keyword
	                            {
	                                String colorstr, *kname;
	                                //int color;

	                                if (Parse(cp) != P_STRING) Fail(cp, "String expected");
	                                colorstr = GetString(cp);
	                                colorstr = GetColor(cp, colorstr);
	                                if (Parse(cp) != P_OPENBRACE) Fail(cp, "'{' expected");
	                                GetOp(cp, P_OPENBRACE);

	                                PutString(cp, CF_KEYWORD, colorstr);

	                                while (1) {
	                                    if (Parse(cp) == P_CLOSEBRACE) break;
	                                    if (Parse(cp) != P_STRING) Fail(cp, "String expected");
	                                    kname = GetString(cp);
	                                    PutString(cp, CF_STRING, kname);

	                                    if (Parse(cp) != P_COMMA)
	                                        break;
	                                    else
	                                        GetOp(cp, P_COMMA);
	                                }
	                            }
	                            if (Parse(cp) != P_CLOSEBRACE) Fail(cp, "'}' expected");
	                            GetOp(cp, P_CLOSEBRACE);
	                            if (Parse(cp) != P_EOS) Fail(cp, "';' expected");
	                            GetOp(cp, P_EOS);
	                            PutNull(cp, CF_END);
	                            break;

	                        case K_HSTATE:
	                            {
	                                long stateno;
	                                String cname;
	                                long cidx;

	                                if (Parse(cp) != P_NUMBER) Fail(cp, "state index expected");
	                                stateno = GetNumber(cp);
	                                if (stateno != LastState + 1) Fail(cp, "invalid state index");

	                                if (Parse(cp) != P_OPENBRACE) Fail(cp, "'{' expected");
	                                GetOp(cp, P_OPENBRACE);
	                                PutNumber(cp, CF_HSTATE, stateno);

	                                if (Parse(cp) != P_STRING) Fail(cp, "String expected");
	                                cname = GetString(cp);
	                                if ((cidx = Lookup(hilit_colors, cname)) == -1)
	                                    Fail(cp, "Lookup of '%s' failed", cname);
	                                PutNumber(cp, CF_INT, cidx);
	                                if (Parse(cp) != P_CLOSEBRACE) Fail(cp, "'}' expected");
	                                GetOp(cp, P_CLOSEBRACE);
	                                LastState = stateno;
	                            }
	                            break;

	                        case K_HTRANS:
	                            {
	                                long next_state;
	                                String opts, *match;
	                                long match_opts;
	                                String cname;
	                                long cidx;

	                                if (Parse(cp) != P_OPENBRACE) Fail(cp, "'{' expected");
	                                GetOp(cp, P_OPENBRACE);

	                                if (Parse(cp) != P_NUMBER) Fail(cp, "next_state index expected");
	                                next_state = GetNumber(cp);
	                                if (Parse(cp) != P_COMMA) Fail(cp, "',' expected");
	                                GetOp(cp, P_COMMA);
	                                if (Parse(cp) != P_STRING) Fail(cp, "match options expected");
	                                opts = GetString(cp);
	                                if (Parse(cp) != P_COMMA) Fail(cp, "',' expected");
	                                GetOp(cp, P_COMMA);
	                                if (Parse(cp) != P_STRING) Fail(cp, "match string expected");
	                                match = GetString(cp);
	                                PutNumber(cp, CF_HTRANS, next_state);
	                                match_opts = 0;
	                                if (strchr(opts, '^')) match_opts |= MATCH_MUST_BOL;
	                                if (strchr(opts, '$')) match_opts |= MATCH_MUST_EOL;
	                                //if (strchr(opts, 'b')) match_opts |= MATCH_MUST_BOLW;
	                                //if (strchr(opts, 'e')) match_opts |= MATCH_MUST_EOLW;
	                                if (strchr(opts, 'i')) match_opts |= MATCH_NO_CASE;
	                                if (strchr(opts, 's')) match_opts |= MATCH_SET;
	                                if (strchr(opts, 'S')) match_opts |= MATCH_NOTSET;
	                                if (strchr(opts, '-')) match_opts |= MATCH_NOGRAB;
	                                if (strchr(opts, '<')) match_opts |= MATCH_TAGASNEXT;
	                                if (strchr(opts, '>')) match_opts &= ~MATCH_TAGASNEXT;
	                                //if (strchr(opts, '!')) match_opts |= MATCH_NEGATE;
	                                if (strchr(opts, 'q')) match_opts |= MATCH_QUOTECH;
	                                if (strchr(opts, 'Q')) match_opts |= MATCH_QUOTEEOL;

	                                if (Parse(cp) != P_COMMA) Fail(cp, "',' expected");
	                                GetOp(cp, P_COMMA);
	                                if (Parse(cp) != P_STRING) Fail(cp, "String expected");
	                                cname = GetString(cp);
	                                if ((cidx = Lookup(hilit_colors, cname)) == -1)
	                                    Fail(cp, "Lookup of '%s' failed", cname);

	                                PutNumber(cp, CF_INT, match_opts);
	                                PutNumber(cp, CF_INT, cidx);
	                                PutString(cp, CF_STRING, match);

	                                if (Parse(cp) != P_CLOSEBRACE) Fail(cp, "'}' expected");
	                                GetOp(cp, P_CLOSEBRACE);
	                            }
	                            break;

	                        case K_HWTYPE:
	                            if (Parse(cp) != P_OPENBRACE) Fail(cp, "'{' expected");
	                            GetOp(cp, P_OPENBRACE);

	                            {
	                                long options = 0;
	                                long nextKwdMatchedState;
	                                long nextKwdNotMatchedState;
	                                long nextKwdNoCharState;
	                                String opts;
	                                String wordChars;


	                                if (Parse(cp) != P_NUMBER) Fail(cp, "next_state index expected");
	                                nextKwdMatchedState = GetNumber(cp);

	                                if (Parse(cp) != P_COMMA) Fail(cp, "',' expected");
	                                GetOp(cp, P_COMMA);

	                                if (Parse(cp) != P_NUMBER) Fail(cp, "next_state index expected");
	                                nextKwdNotMatchedState = GetNumber(cp);

	                                if (Parse(cp) != P_COMMA) Fail(cp, "',' expected");
	                                GetOp(cp, P_COMMA);

	                                if (Parse(cp) != P_NUMBER) Fail(cp, "next_state index expected");
	                                nextKwdNoCharState = GetNumber(cp);

	                                if (Parse(cp) != P_COMMA) Fail(cp, "',' expected");
	                                GetOp(cp, P_COMMA);

	                                if (Parse(cp) != P_STRING) Fail(cp, "String expected");
	                                opts = GetString(cp);
	                                if (strchr(opts, 'i')) options |= STATE_NOCASE;
	                                if (strchr(opts, '<')) options |= STATE_TAGASNEXT;
	                                if (strchr(opts, '>')) options &= ~STATE_TAGASNEXT;
	                                if (strchr(opts, '-')) options |= STATE_NOGRAB;

	                                if (Parse(cp) != P_COMMA) Fail(cp, "',' expected");
	                                GetOp(cp, P_COMMA);

	                                if (Parse(cp) != P_STRING) Fail(cp, "String expected");
	                                wordChars = GetString(cp);

	                                PutNull(cp, CF_HWTYPE);
	                                PutNumber(cp, CF_INT, nextKwdMatchedState);
	                                PutNumber(cp, CF_INT, nextKwdNotMatchedState);
	                                PutNumber(cp, CF_INT, nextKwdNoCharState);
	                                PutNumber(cp, CF_INT, options);
	                                PutString(cp, CF_STRING, wordChars);
	                            }
	                            if (Parse(cp) != P_CLOSEBRACE) Fail(cp, "'}' expected");
	                            GetOp(cp, P_CLOSEBRACE);
	                            break;

	                        case K_HWORDS:
	                            {
	                                String colorstr, *kname;
	                                //int color;

	                                if (Parse(cp) != P_STRING) Fail(cp, "String expected");
	                                colorstr = GetString(cp);
	                                colorstr = GetColor(cp, colorstr);

	                                if (Parse(cp) != P_OPENBRACE) Fail(cp, "'{' expected");
	                                GetOp(cp, P_OPENBRACE);

	                                PutString(cp, CF_HWORDS, colorstr);

	                                while (1) {
	                                    if (Parse(cp) == P_CLOSEBRACE) break;
	                                    if (Parse(cp) != P_STRING) Fail(cp, "String expected");
	                                    kname = GetString(cp);
	                                    PutString(cp, CF_STRING, kname);

	                                    if (Parse(cp) != P_COMMA)
	                                        break;
	                                    else
	                                        GetOp(cp, P_COMMA);
	                                }
	                            }
	                            if (Parse(cp) != P_CLOSEBRACE) Fail(cp, "'}' expected");
	                            GetOp(cp, P_CLOSEBRACE);

	                            PutNull(cp, CF_END);
	                            break;

	                        default:
	                            if (Parse(cp) != P_ASSIGN) Fail(cp, "'=' expected");
	                            GetOp(cp, P_ASSIGN);
	                            switch (Parse(cp)) {
	                                /*case P_NUMBER:
	                                 {
	                                 long var;
	                                 long num;

	                                 num = GetNumber(cp);
	                                 var = LookupColorizeNumber(w);
	                                 if (var == -1) return -1;
	                                 PutObj(cp, CF_SETVAR, sizeof(long), &var);
	                                 PutObj(cp, CF_INT, sizeof(long), &num);
	                                 }
	                                 break;*/
	                            case P_STRING:
	                                {
	                                    long var;

	                                    s = GetString(cp);
	                                    if (s == 0) Fail(cp, "Parse failed");
	                                    var = Lookup(colorize_string, w);
	                                    if (var == -1)
	                                        Fail(cp, "Lookup of '%s' failed", w);
	                                    PutNumber(cp, CF_SETVAR, var);
	                                    PutString(cp, CF_STRING, s);
	                                }
	                                break;
	                            default:
	                                return -1;
	                            }
	                            if (Parse(cp) != P_EOS) Fail(cp, "';' expected");
	                            GetOp(cp, P_EOS);
	                            break;
	                        }
	                    }
	                    GetOp(cp, P_CLOSEBRACE);
	                    PutNull(cp, CF_END);
	                }
	                break;

	            case K_MODE: // mode::
	                {
	                    if (Parse(cp) != P_WORD) Fail(cp, "Syntax error");
	                    if (GetWord(cp, ObjName) != 0) Fail(cp, "Parse failed");
	                    PutString(cp, CF_MODE, ObjName);

	                    UpMode[0] = 0;
	                    if (Parse(cp) == P_COLON) {
	                        GetOp(cp, P_COLON);
	                        if (Parse(cp) != P_WORD) Fail(cp, "Syntax error");
	                        if (GetWord(cp, UpMode) != 0) Fail(cp, "Parse failed");
	                    }
	                    PutString(cp, CF_PARENT, UpMode);
	                    if (Parse(cp) != P_OPENBRACE) Fail(cp, "'{' expected");
	                    GetOp(cp, P_OPENBRACE);

	                    while (1) {
	                        p = Parse(cp);
	                        if (p == P_CLOSEBRACE) break;
	                        if (p == P_EOF) Fail(cp, "Unexpected EOF");
	                        if (p != P_WORD) Fail(cp, "Syntax error");

	                        if (GetWord(cp, w) != 0) Fail(cp, "Parse failed");
	                        //switch (Lookup(CfgKW, w)) {
	                        //default:  // mode::
	                        if (Parse(cp) != P_ASSIGN) Fail(cp, "'=' expected");
	                        GetOp(cp, P_ASSIGN);
	                        switch (Parse(cp)) {
	                        case P_NUMBER:
	                            {
	                                long var;
	                                long num;

	                                num = GetNumber(cp);
	                                var = Lookup(mode_num, w);
	                                if (var == -1)
	                                    Fail(cp, "Lookup of '%s' failed", w);
	                                PutNumber(cp, CF_SETVAR, var);
	                                PutNumber(cp, CF_INT, num);
	                            }
	                            break;
	                        case P_STRING:
	                            {
	                                long var;

	                                s = GetString(cp);
	                                if (s == 0) Fail(cp, "Parse failed");
	                                var = Lookup(mode_string, w);
	                                if (var == -1)
	                                    Fail(cp, "Lookup of '%s' filed", w);
	                                PutNumber(cp, CF_SETVAR, var);
	                                PutString(cp, CF_STRING, s);
	                            }
	                            break;
	                        default:
	                            return -1;
	                        }
	                        if (Parse(cp) != P_EOS) Fail(cp, "';' expected");
	                        GetOp(cp, P_EOS);
	                        //    break;
	                        //}
	                    }
	                    GetOp(cp, P_CLOSEBRACE);
	                    PutNull(cp, CF_END);
	                }
	                break;
	            case K_OBJECT:
	                {
	                    if (Parse(cp) != P_WORD) Fail(cp, "Syntax error");
	                    if (GetWord(cp, ObjName) != 0) Fail(cp, "Parse failed");
	                    if (Parse(cp) != P_OPENBRACE) Fail(cp, "'{' expected");
	                    GetOp(cp, P_OPENBRACE);

	                    PutString(cp, CF_OBJECT, ObjName);

	                    while (1) {
	                        p = Parse(cp);
	                        if (p == P_CLOSEBRACE) break;
	                        if (p == P_EOF) Fail(cp, "Unexpected EOF");
	                        if (p != P_WORD) Fail(cp, "Syntax error");

	                        if (GetWord(cp, w) != 0) Fail(cp, "Parse failed");
	                        switch (Lookup(CfgKW, w)) {
	                        case K_COLOR: // mode::color
	                            if (Parse(cp) != P_OPENBRACE) Fail(cp, "'{' expected");
	                            GetOp(cp, P_OPENBRACE);
	                            PutNull(cp, CF_COLOR);

	                            while (1) {
	                                String sname, *svalue;

	                                if (Parse(cp) == P_CLOSEBRACE) break;
	                                if (Parse(cp) != P_OPENBRACE) Fail(cp, "'{' expected");
	                                GetOp(cp, P_OPENBRACE);
	                                if (Parse(cp) != P_STRING) Fail(cp, "String expected");
	                                sname = GetString(cp);
	                                PutString(cp, CF_STRING, sname);
	                                if (Parse(cp) != P_COMMA) Fail(cp, "',' expected");
	                                GetOp(cp, P_COMMA);
	                                if (Parse(cp) != P_STRING) Fail(cp, "String expected");
	                                svalue = GetString(cp);
	                                svalue = GetColor(cp, svalue);
	                                PutString(cp, CF_STRING, svalue);
	                                if (Parse(cp) != P_CLOSEBRACE) Fail(cp, "'}' expected");
	                                GetOp(cp, P_CLOSEBRACE);
	                                if (Parse(cp) != P_COMMA)
	                                    break;
	                                else
	                                    GetOp(cp, P_COMMA);
	                            }
	                            if (Parse(cp) != P_CLOSEBRACE) Fail(cp, "'}' expected");
	                            GetOp(cp, P_CLOSEBRACE);
	                            if (Parse(cp) != P_EOS) Fail(cp, "';' expected");
	                            GetOp(cp, P_EOS);
	                            PutNull(cp, CF_END);
	                            break;

	                        case K_COMPILERX:
	                            {
	                                long file, line, msg;
	                                String regexp;

	                                if (Parse(cp) != P_ASSIGN) Fail(cp, "'=' expected");
	                                GetOp(cp, P_ASSIGN);
	                                if (Parse(cp) != P_OPENBRACE) Fail(cp, "'{' expected");
	                                GetOp(cp, P_OPENBRACE);
	                                if (Parse(cp) != P_NUMBER) Fail(cp, "Number expected");
	                                file = GetNumber(cp);
	                                if (Parse(cp) != P_COMMA) Fail(cp, "',' expected");
	                                GetOp(cp, P_COMMA);
	                                if (Parse(cp) != P_NUMBER) Fail(cp, "Number expected");
	                                line = GetNumber(cp);
	                                if (Parse(cp) != P_COMMA) Fail(cp, "',' expected");
	                                GetOp(cp, P_COMMA);
	                                if (Parse(cp) != P_NUMBER) Fail(cp, "Number expected");
	                                msg = GetNumber(cp);
	                                if (Parse(cp) != P_COMMA) Fail(cp, "',' expected");
	                                GetOp(cp, P_COMMA);
	                                if (Parse(cp) != P_STRING) Fail(cp, "String expected");
	                                regexp = GetString(cp);
	                                if (Parse(cp) != P_CLOSEBRACE) Fail(cp, "'}' expected");
	                                GetOp(cp, P_CLOSEBRACE);
	                                PutNull(cp, CF_COMPRX);
	                                PutNumber(cp, CF_INT, file);
	                                PutNumber(cp, CF_INT, line);
	                                PutNumber(cp, CF_INT, msg);
	                                PutString(cp, CF_REGEXP, regexp);
	                                if (Parse(cp) != P_EOS) Fail(cp, "';' expected");
	                                GetOp(cp, P_EOS);
	                            }
	                            break;
	                        case K_CVSIGNRX:
	                            {
	                                String regexp;

	                                if (Parse(cp) != P_ASSIGN) Fail(cp, "'=' expected");
	                                GetOp(cp, P_ASSIGN);
	                                if (Parse(cp) != P_STRING) Fail(cp, "String expected");
	                                regexp = GetString(cp);
	                                PutNull(cp, CF_CVSIGNRX);
	                                PutString(cp, CF_REGEXP, regexp);
	                                if (Parse(cp) != P_EOS) Fail(cp, "';' expected");
	                                GetOp(cp, P_EOS);
	                            }
	                            break;
	                        default:  // mode::
	                            if (Parse(cp) != P_ASSIGN) Fail(cp, "'=' expected");
	                            GetOp(cp, P_ASSIGN);

	                            switch (Parse(cp)) {
	                            case P_NUMBER:
	                                {
	                                    long var;
	                                    long num;

	                                    num = GetNumber(cp);
	                                    var = Lookup(global_num, w);
	                                    if (var == -1)
	                                        Fail(cp, "Lookup of '%s' failed", w);
	                                    PutNumber(cp, CF_SETVAR, var);
	                                    PutNumber(cp, CF_INT, num);
	                                }
	                                break;
	                            case P_STRING:
	                                {
	                                    long var;

	                                    s = GetString(cp);
	                                    if (s == 0) Fail(cp, "Parse failed");
	                                    var = Lookup(global_string, w);
	                                    if (var == -1) Fail(cp, "Lookup of '%s' failed");
	                                    PutNumber(cp, CF_SETVAR, var);
	                                    PutString(cp, CF_STRING, s);
	                                }
	                                break;
	                            default:
	                                Fail(cp, "Syntax error");
	                            }
	                            if (Parse(cp) != P_EOS) Fail(cp, "';' expected");
	                            GetOp(cp, P_EOS);
	                            break;
	                        }
	                    }
	                    GetOp(cp, P_CLOSEBRACE);
	                    PutNull(cp, CF_END);
	                }
	                break;

	            case K_COLPALETTE:
	                {
	                    if (Parse(cp) != P_OPENBRACE) Fail(cp, "'{' expected");
	                    GetOp(cp, P_OPENBRACE);

	                    while (1) {
	                        String sname, *svalue;

	                        if (Parse(cp) == P_CLOSEBRACE) break;
	                        if (Parse(cp) != P_OPENBRACE) Fail(cp, "'{' expected");
	                        GetOp(cp, P_OPENBRACE);
	                        if (Parse(cp) != P_STRING) Fail(cp, "String expected");
	                        sname = GetString(cp);
	                        if (Parse(cp) != P_COMMA) Fail(cp, "',' expected");
	                        GetOp(cp, P_COMMA);
	                        if (Parse(cp) != P_STRING) Fail(cp, "String expected");
	                        svalue = GetString(cp);
	                        svalue = GetColor(cp, svalue);
	                        assert(DefineColor(sname, svalue) == 1);
	                        if (Parse(cp) != P_CLOSEBRACE) Fail(cp, "'}' expected");
	                        GetOp(cp, P_CLOSEBRACE);
	                        if (Parse(cp) != P_COMMA)
	                            break;
	                        else
	                            GetOp(cp, P_COMMA);
	                    }
	                    if (Parse(cp) != P_CLOSEBRACE) Fail(cp, "'}' expected");
	                    GetOp(cp, P_CLOSEBRACE);
	                }
	                break;
	            case K_INCLUDE:
	                {
	                    String fn;

	                    if (Parse(cp) != P_STRING) Fail(cp, "String expected");
	                    fn = GetString(cp);

	                    if (LoadFile(cp.name, fn) != 0) Fail(cp, "Include of file '%s' failed", fn);
	                    if (Parse(cp) != P_EOS) Fail(cp, "';' expected");
	                    GetOp(cp, P_EOS);
	                }
	                break;
	            default:
	                Fail(cp, "Syntax error");
	            }
	            break;
	        case P_EOF: return 0;
	        default:    Fail(cp, "Syntax error");
	        }
	    }
	}

	int LoadFile(String WhereName, String CfgName, int Level) {
	    int fd, rc;
	    String buffer = 0;
	    struct stat statbuf;
	    CurPos cp;
	    char last[MAXPATH];
	    char Cfg[MAXPATH];

	    //fprintf(stderr, "Loading file %s %s\n", WhereName, CfgName);

	    JustDirectory(WhereName, last);

	    if (IsFullPath(CfgName)) {
	        strcpy(Cfg, CfgName);
	    } else {
	        // here we will try relative to a number of places.
	        // 1. User's .fte directory.
	        // 2. System's "local config" directory.
	        // 3. Initial file's directory.
	        // 4. Current directory.
	        // This means that a user's directory will always win out,
	        // allowing a given user to always be able to override everything,
	        // followed by a system standard to override anything.

	        // #'s 1 and 2 are unix-only.
	#ifdef UNIX
	        // 1. User's .fte directory.
	        char tmp[MAXPATH];
	        sprintf(tmp, "~/.fte/%s", CfgName);
	        ExpandPath(tmp, Cfg);
	        //fprintf(stderr, "Looking for %s\n", Cfg);
	        if (!FileExists(Cfg))
	        {
	            // Okay, now try "local config".
	            sprintf(tmp, "%slocalconfig/%s", StartDir, CfgName);
	            ExpandPath(tmp, Cfg);
	            //fprintf(stderr, "Looking for %s\n", Cfg);
	            if (!FileExists(Cfg))
	            {
	                sprintf(tmp, "%sconfig/%s", StartDir, CfgName);
	                ExpandPath(tmp, Cfg);
	                //fprintf(stderr, "Looking for %s\n", Cfg);
	                if (!FileExists(Cfg))
	                {
	                    sprintf(tmp, "./%s", CfgName);
	                    ExpandPath(tmp, Cfg);
	                    //fprintf(stderr, "Looking for %s\n", Cfg);
	                    if (!FileExists(Cfg))
	                    {
	                        fprintf(stderr, "Cannot find '%s' in:\n"
	                                "\t~/.fte,\n""\t%slocalconfig,\n"
	                                "\t%sconfig, or\n"
	                                "\t.",
	                                CfgName, StartDir, StartDir);
	                    }
	                }
	            }
	        }
	#else // UNIX
	        SlashDir(last);
	        strcat(last, CfgName);
	        ExpandPath(last, Cfg);
	#endif // UNIX
	    }
	    // puts(Cfg);

	    //fprintf(stderr, "Loading file %s\n", Cfg);
	    if ((fd = open(Cfg, O_RDONLY | O_BINARY)) == -1) {
	        fprintf(stderr, "Cannot open '%s', errno=%d\n", Cfg, errno);
	        return -1;
	    }
	    if (fstat(fd, &statbuf) != 0) {
	        close(fd);
	        fprintf(stderr, "Cannot stat '%s', errno=%d\n", Cfg, errno);
	        return -1;
	    }
	    buffer = (String ) malloc(statbuf.st_size);
	    if (buffer == 0) {
	        close(fd);
	        return -1;
	    }
	    if (read(fd, buffer, statbuf.st_size) != statbuf.st_size) {
	        close(fd);
	        free(buffer);
	        return -1;
	    }
	    close(fd);

	    cp.sz = statbuf.st_size;
	    cp.a = cp.c = buffer;
	    cp.z = cp.a + cp.sz;
	    cp.line = 1;
	    cp.name = Cfg;

	    rc = ParseConfigFile(cp);
	    // puts("End Loading file");
	    if (Level == 0)
	        PutNull(cp, CF_EOF);

	    if (rc == -1) {
	        Fail(cp, "Parse failed");
	    }
	    free(buffer);
	    return rc;
	}
	
	
}
