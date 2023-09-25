package ru.dz.jfte.config;

import java.io.BufferedOutputStream;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import ru.dz.jfte.ConfigDefs;
import ru.dz.jfte.Console;
import ru.dz.jfte.Main;
import ru.dz.jfte.MainConst;
import ru.dz.jfte.c.ByteArrayPtr;

public class ConfigCompiler implements ConfigCompilerDefs, ConfigDefs 
{

	public ConfigCompiler() {
		// TODO Auto-generated constructor stub
	}

	
	int slen(String s) { return ((s!=null) ? (s.length()) : 0); }

	/*static class ExMacro {
	    String Name;
	} */


	//BufferedWriter output;
	BufferedOutputStream output;

	//FILE *output = 0;
	int lntotal = 0;
	long offset = -1;
	long pos = 0;
	String [] XTarget = {""};
	String [] StartDir ={""};


	class CurPos {
	    int sz;
	    //String a;
	    //String c;
	    //String z;
	    ByteArrayPtr c;
	    int line;
	    String name; // filename
	}

	/*
	void cleanup(int xerrno) {
	    if (output)
	        fclose(output);
	    if (XTarget[0] != 0)
	        unlink(XTarget);
	    System.exit(xerrno);
	}*/

	void Fail(CurPos cp, String fmt, Object... s) {

		String b = String.format(fmt, s);
		
	    System.err.printf("%s:%d: Error: %s\n", cp.name, cp.line, b);
	    System.exit(1);
	    //cleanup(1);
	}

	//int LoadFile(String WhereName, String CfgName, int Level = 1);
	
	void PutObject(CurPos cp, int tag, int len, byte []  obj) throws IOException {
	    //char tag = (char)xtag;
	    //int len = xlen;
	    //unsigned char l[2];

	    int l0 = len & 0xFF;
	    int l1 = (len >> 8) & 0xFF;

	    output.write(tag);
	    
	    output.write(l0);
	    output.write(l1);
	    
	    if( null != obj ) output.write(obj);
	    /*
	    if (fwrite(&tag, 1, 1, output) != 1 ||
	        fwrite(l, 2, 1, output) != 1 ||
	        fwrite(obj, 1, len, output) != len)
	    {
	        Fail(cp, "Disk full!");
	    }
	    pos += 1 + 2 + len;
	    if (offset != -1 && pos >= offset) {
	        Fail(cp, "Error location found at %ld", pos);
	    }*/
	    pos += 1 + 2 + len;
	}

	void PutNull(CurPos cp, int xtag) throws IOException {
	    PutObject(cp, xtag, 0, null);
	}

	void PutString(CurPos cp, int xtag, String str) throws IOException {
		//byte[] b = str.getBytes(Main.charset);
		byte[] b = str.getBytes(); // TODO utf-8?
	    PutObject(cp, xtag, b.length, b);
	}

	void PutNumber(CurPos cp, int xtag, long num) throws IOException {
	    long l = num;
	    byte [] b = new byte[4];

	    b[0] = (byte)(l & 0xFF);
	    b[1] = (byte)((l >> 8) & 0xFF);
	    b[2] = (byte)((l >> 16) & 0xFF);
	    b[3] = (byte)((l >> 24) & 0xFF);
	    PutObject(cp, xtag, 4, b);
	}

	public static void main(String []argv) {
	    String Source;
	    String Target;
	    //String p = argv[1];
	    int n = 0;
	    int argc = argv.length;

	    System.err.printf( "cfte " + MainConst.VERSION + "\n" + MainConst.COPYRIGHT + "\n");
	    if (argc < 2 || argc > 4) {
	    	System.err.printf( "Usage: cfte [-o<offset>] " +
	                "main.fte [fte-new.cnf]\n");
	        System.exit(1);
	    }

	    ConfigCompiler cc = new ConfigCompiler();
	    
	    cc.DefineWord("OS_JAVA");

	    /* TODO	    if (strncmp(p, "-o", 2) == 0) {
	        p += 2;
	        offset = atol(p);
	        n++;
	    }
	    if (n == 1 && argc == 4) {
	    	System.err.printf( "Invalid option '%s'\n", argv[1]);
	        System.exit(1);
	    } */
	    Source = argv[n++];
	    Target = "fte-new.cnf";
	    if (n < argc)
	        Target = argv[n++];

	    cc.compile(Source,Target);
	}


	private void compile(String Source, String Target) 
	{
		Target = new File(Target).getAbsolutePath();
	    Console.JustDirectory(Target, XTarget);
	    XTarget[0] = Console.Slash(XTarget[0], 1);
	    XTarget[0] += String.format( "cfte%d.tmp", 33 );// TODO (long)getpid());
	    
		//try(BufferedWriter o = Files.newBufferedWriter(Path.of(XTarget[0]), Main.charset))
	    try(BufferedOutputStream o = new BufferedOutputStream(new FileOutputStream(XTarget[0]));)
		{
			output = o;
		    System.out.printf( "Compiling to '%s'\n", Target);

			writeOutput(Source);
			output.close();


		    if (Console.FileExists(Target) &&  Console.unlink(Target) != 0) {
		    	System.err.printf( "Remove of '%s' failed, result left in %s\n",
		                Target, XTarget);
		        System.exit(1);
		    }

		    if (!Console.rename(XTarget[0], Target)) {
		    	System.err.printf( "Rename of '%s' to '%s' failed\n",
		                XTarget, Target );
		        System.exit(1);
		    }

		    System.out.printf( "\nDone.\n");
			
			return ;

		}
		catch(IOException e)
		{
		    System.err.printf( "Cannot create '%s',Exception %s", XTarget[0], e);
	        //cleanup(1);
	        Console.unlink(XTarget[0]);

			/*
			Console.unlink(AFileName);
			if (!Console.rename(ABackupName[0], AFileName)) {
				View.MView.Win.Choice(GPC_ERROR, "Error", 1, "O&K", "Error renaming backup file to original!");
			} else {
				View.MView.Win.Choice(GPC_ERROR, "Error", 1, "O&K", "Error writing file, backup restored.");
			}

			return false;
			*/
		}

	}
	
	
	void writeInt(long l) throws IOException
	{
		output.write( (char)(l & 0xFF) );
		output.write( (char)((l >> 8) & 0xFF) );
		output.write( (char)((l >> 16) & 0xFF) );
		output.write( (char)((l >> 24) & 0xFF) );		
	}
	
	void writeOutput(String Source) throws IOException
	{
		
		
	    
		/*
	    unsigned char b[4];

	    b[0] = b[1] = b[2] = b[3] = 0;

	    if (fwrite(b, sizeof(b), 1, output) != 1) {
	        fprintf(stderr, "Disk full!");
	        cleanup(1);
	    }
	    */
		writeInt(CONFIG_ID);
		writeInt(MainConst.VERNUM);

		/*
	    unsigned long l = VERNUM;

	    b[0] = (unsigned char)(l & 0xFF);
	    b[1] = (unsigned char)((l >> 8) & 0xFF);
	    b[2] = (unsigned char)((l >> 16) & 0xFF);
	    b[3] = (unsigned char)((l >> 24) & 0xFF);

	    if (fwrite(b, 4, 1, output) != 1) {
	        fprintf(stderr, "Disk full!");
	        cleanup(1);
	    }
	    */
	    pos = 2 * 4;

	    /*{
	        char PrevDir[MAXPATH];
	        sprintf(PrevDir, "%s/..", Target);
	        ExpandPath(PrevDir, StartDir);
	        Slash(StartDir, 1);
	    }*/

	    Console.ExpandPath("."
	/*#ifdef UNIX
	               "."
	#endif */
	               , StartDir);
	    StartDir[0] = Console.Slash(StartDir[0], 1);

	    {
	        CurPos cp = new CurPos();
	        String [] FSource = {""};

	        if (Console.ExpandPath(Source, FSource) != 0) {
	        	System.err.printf( "Could not expand path %s\n", Source);
	            System.exit(1);
	        }

	        cp.sz = 0;
	        cp.c = null;
	        //cp.a = cp.c = 0;
	        //cp.z = cp.a + cp.sz;
	        cp.line = 0;
	        cp.name = "<cfte-start>";

	        PutString(cp, CF_STRING, FSource[0]);
	    }

	    if (!LoadFile(StartDir[0], Source, 0)) {
	    	//System.err.printf( "\nCompile failed\n");
	        //cleanup(1);
	    	throw new IOException("Compile failed");
	    }

	    /*
	    l = CONFIG_ID;
	    b[0] = (unsigned char)(l & 0xFF);
	    b[1] = (unsigned char)((l >> 8) & 0xFF);
	    b[2] = (unsigned char)((l >> 16) & 0xFF);
	    b[3] = (unsigned char)((l >> 24) & 0xFF);
	    fseek(output, 0, SEEK_SET);
	    fwrite(b, 4, 1, output);
	    return 0;
	    */
	}


	int Lookup(OrdLookup [] where, String what) {

	    for (OrdLookup w : where) {
	        if (what.equals(w.Name))
	            return w.num;
	    }
//	    fprintf(stderr, "\nBad name: %s (i = %d)\n", what, i);
	    return -1;
	}


	//typedef char Word[64];


	//String []words = null;
	Map<String,Integer> words = new HashMap<>();
	int wordCount = 0;

	boolean DefinedWord(String w) {
		return words.get(w) != null;
		/*
	    if (words == 0 || wordCount == 0)
	        return 0;
	    for (int i = 0; i < wordCount; i++)
	        if (strcmp(w, words[i]) == 0)
	            return 1;
	    return 0;
	    */
	}

	void DefineWord(String w) {
		words.put(w, wordCount++);
		/*
	    if (!w || !w[0])
	        return ;
	    if (!DefinedWord(w)) {
	        words = (String *)realloc(words, sizeof (String ) * (wordCount + 1));
	        assert(words != 0);
	        words[wordCount] = w;
	        assert(words[wordCount] != 0);
	        wordCount++;
	    }*/
	}

	/*
	int colorCount;
	struct _color {
	    String colorName;
	    String colorValue;
	} *colors;
	*/
	Map<String,String> colors = new HashMap<String, String>();

	void DefineColor(String name, String value) {
		colors.put(name, value);
		/*
	    if (!name || !value)
	        return 0;
	    colors = (struct _color *)realloc(colors, sizeof (struct _color) * (colorCount + 1));
	    assert(colors != 0);
	    colors[colorCount].colorName = name);
	    colors[colorCount].colorValue = value);
	    assert(colors != null);
	    assert(colors[colorCount].colorName != 0);
	    assert(colors[colorCount].colorValue != 0);
	    colorCount++;
	    return 1; */
	}

	String DefinedColor(String name) {
		return colors.get(name);
		/*
	    if (colors == 0 || colorCount == 0)
	        return 0;
	    for (int i = 0; i < colorCount; i++)
	        if (strcmp(name, colors[i].colorName) == 0)
	            return colors[i].colorValue;
	    return 0; */
	}

	boolean isxdigit(char c)
	{
		return Character.isDigit(c) 
				|| ( c >= 'a' && c <= 'f')
				|| ( c >= 'A' && c <= 'F');
	}
	
	String GetColor(CurPos cp, String name) {
	    //String c;
	    //String name;
	    //static char color[4];

	    // add support for fore:back and remove it from fte itself

		if( name.indexOf(' ') > 0 ) ; //do nothing // name = splitBy( cp, name, ' ');
	    else if( name.indexOf(':') > 0 ) name = splitBy( cp, name, ':');
	    
	    /*
	    if ((c = strchr(name, ' ')) != null) {
	    } else if ((c = strchr(name, ':')) != null) {
	        //char clr[4];
	        //*c++ = 0;
	        clr[0] = GetColor(cp, name)[0];
	        clr[1] = ' ';
	        clr[2] = GetColor(cp, c)[2];
	        clr[3] = 0;

	        memcpy(color, clr, sizeof(color));
	        name = (String )color;
	        
	        name = GetColor(cp, name1).charAt(0) + " " + GetColor(cp, name2).charAt(2);
	    } */ else {
	        String p = DefinedColor(name);
	        if (p == null)
	            Fail(cp, "Unknown symbolic color %s", name);
	        name = p;
	    }
	    
	    if (!isxdigit(name.charAt(0)) &&
	        name.charAt(1) != ' ' &&
	        !isxdigit(name.charAt(2)) &&
	        name.length() > 3)
	    {
	        Fail(cp, "malformed color specification: %s", name);
	    }
	    return name;
	}

	private String splitBy(CurPos cp, String name, char c) {
		String[] s = name.split(""+c);
		return GetColor(cp, s[0]).charAt(0) + " " + GetColor(cp, s[1]).charAt(2);
	}


	String GetWord(CurPos cp) 
	{
		StringBuilder sb = new StringBuilder();

		while(true)
		{
			int c = cp.c.urpp();
			if( !isWordChar(c) )
			{
				cp.c.shift(-1);
				break;
			}
			
			sb.append((char)c);
		}
		
		/*
	    while (cp.c < cp.z &&
	           ((*cp.c >= 'a' && *cp.c <= 'z') ||
	            (*cp.c >= 'A' && *cp.c <= 'Z') ||
	            (*cp.c >= '0' && *cp.c <= '9') ||
	            (*cp.c == '_')))
	    {
	        *p++ = *cp.c++;
	        len++;
	    }*/

	    return sb.length() > 0 ? sb.toString() : null;
	}


	private static boolean isWordChar(int c) {
		return 
				(c >= 'a' && c <= 'z') ||
	            (c >= 'A' && c <= 'Z') ||
	            (c >= '0' && c <= '9') ||
	            (c == '_');
	}

	static boolean isKeyWord(CurPos cp, String kw)
	{
		int len = kw.length();
		
		if(cp.c.hasBytesLeft() < len) return false;
		
		if( kw.equals( cp.c.getLenAsString(len) ) )
			return true;
	
		cp.c.shift(-len);
		return false;
	}

	int Parse(CurPos cp) {
	    while (cp.c.hasCurrent()) {
	        switch (cp.c.ur(0)) {
	/* #ifndef UNIX
	        case '\x1A': // ^Z :-*  
	        return P_EOF;
	#endif */
	        case '#':
	            while (cp.c.hasCurrent() && cp.c.ur(0) != '\n') cp.c.shift(1);
	            break;
	        case '%':
	            cp.c.shift(1);
	            if ( isKeyWord( cp, "define(") ) {

	                while (cp.c.hasCurrent() && cp.c.ur(0) != ')') {
	                    String w = GetWord(cp);
	                    //printf("define '%s'\n", w);
	                    DefineWord(w);
	                    if (cp.c.hasCurrent() && cp.c.ur(0) != ',' && cp.c.ur(0) != ')' )
	                        Fail(cp, "unexpected: %c", cp.c.ur(0));
	                    if (cp.c.hasCurrent() && cp.c.ur(0) == ',')
	                        cp.c.shift(1);
	                }
	                cp.c.shift(1);
	/*            } else if (cp.c + 6 && strcmp(cp.c, "undef(", 6) == 0) {
	                Word w;
	                cp.c += 6;

	                while (cp.c.hasCurrent() && cp.c.ur(0) != ')') {
	                    GetWord(cp, w);
	                    UndefWord(w);
	                }*/
	            } else if (isKeyWord( cp, "if(") ) {
	               // Word w;
	                int wasWord = 0;

	                while (cp.c.hasCurrent() && cp.c.ur(0) != ')') {
	                    int neg = 0;
	                    if (cp.c.ur(0) == '!') {
	                        cp.c.shift(1);
	                        neg = 1;
	                    }
	                    String w = GetWord(cp);
	                    if (DefinedWord(w))
	                        wasWord = 1;
	                    if (neg!=0)
	                        wasWord = wasWord != 0 ? 0 : 1;
	                    /*if (wasWord)
	                        printf("yes '%s'\n", w);
	                    else
	                        printf("not '%s'\n", w);*/

	                    if (cp.c.hasCurrent() && cp.c.ur(0) != ',' && cp.c.ur(0) != ')' )
	                        Fail(cp, "unexpected: %c", cp.c.ur(0));
	                    if (cp.c.hasCurrent() && cp.c.ur(0) == ',')
	                        cp.c.shift(1);
	                }
	                cp.c.shift(1);
	                if (0==wasWord) {
	                    int nest = 1;
	                    while (cp.c.hasCurrent()) {
	                        if (cp.c.ur(0) == '\n') {
	                            cp.line++;
	                            lntotal++;
	                        } else if (cp.c.ur(0) == '%') {
	                            if (isKeyWord( cp, "%endif"))
	                            {
	                                if (--nest == 0)
	                                    break;
	                            }
	                            if (isKeyWord( cp, "%if"))
	                            {
	                                ++nest;
	                            }
	                        } else if (cp.c.ur(0) == '#') {
	                            // we really shouldn't process hashed % directives
	                            while( cp.c.hasCurrent() && cp.c.ur(0) != '\n' ) cp.c.shift(1);

	                            // workaround to make line numbering correct
	                            cp.line++;
	                            lntotal++;
	                        }
	                        cp.c.shift(1);
	                    }
	                }
	            } else if (isKeyWord( cp, "endif")) {
	                // none
	            }
	            if (cp.c.hasCurrent() && cp.c.ur(0) != '\n' && cp.c.ur(0) != '\r')
	                Fail(cp, "syntax error %30.30s", cp.c);
	            break;
	        case '\n':
	            cp.line++;
	            lntotal++;
	        case ' ':
	        case '\t':
	        case '\r':
	            cp.c.shift(1);
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
	            /*if ((*cp.c >= 'a' && *cp.c <= 'z') ||
	                (*cp.c >= 'A' && *cp.c <= 'Z') ||
	                (*cp.c == '_')) */
	            if(isWordChar(cp.c.ur(0)))
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
	        //cp.c.shift(1);
	        cp.c.shift(1);
	        break;
	    }
	}

	String GetString(CurPos cp) {
	    //String p = cp.c;
	    //String d = cp.c;
	    int n;

	    StringBuilder sb = new StringBuilder();
	    
	    char c = (char) cp.c.ur(0);
	    if (c == '[') c = ']';

	    cp.c.shift(1); // skip '"`
	    while (cp.c.hasCurrent()) {
	        if (cp.c.ur(0) == '\\') {
	            if (c == '/')
	                sb.append( (char) cp.c.ur(0) );
	            cp.c.shift(1);
	            
	            if (!cp.c.hasCurrent()) return null;
	             
	            if (c == '"') {
	                switch (cp.c.ur(0)) {
	                case 'e': cp.c.w(0,  (byte)0x1B ); break;
	                case 't': cp.c.w(0,  (byte)'\t'); break;
	                case 'r': cp.c.w(0,  (byte)'\r'); break;
	                case 'n': cp.c.w(0,  (byte)'\n'); break;
	                case 'b': cp.c.w(0,  (byte)'\b'); break;
	                case 'v': cp.c.w(0,  (byte) 0x0B); break;
	                case 'a': cp.c.w(0,  (byte) 0x07); break;
	                case 'x':
	                    cp.c.shift(1);
	                    if (!cp.c.hasCurrent()) return null;
	                    if (cp.c.ur(0) >= '0' && cp.c.ur(0) <= '9') n = cp.c.ur(0) - '0';
	                    else if (cp.c.ur(0) >='a' && cp.c.ur(0) <= 'f') n = cp.c.ur(0) - 'a' + 10;
	                    else if (cp.c.ur(0) >='A' && cp.c.ur(0) <= 'F') n = cp.c.ur(0) - 'A' + 10;
	                    else return null;
	                    cp.c.shift(1);
	                    if (!cp.c.hasCurrent()) cp.c.shift(-1); // cp.c--;
	                    else if (cp.c.ur(0) >= '0' && cp.c.ur(0) <= '9') n = n * 16 + cp.c.ur(0) - '0';
	                    else if (cp.c.ur(0) >= 'a' && cp.c.ur(0) <= 'f') n = n * 16 + cp.c.ur(0) - 'a' + 10;
	                    else if (cp.c.ur(0) >= 'A' && cp.c.ur(0) <= 'F') n = n * 16 + cp.c.ur(0) - 'A' + 10;
	                    else cp.c.shift(-1); //cp.c--;
	                    cp.c.w(0,  (byte) n);
	                    break;
	                }
	            }
	        } else if (cp.c.ur(0) == c) {
	            cp.c.shift(1);
	            //*p = 0;
	            return sb.toString();
	        } else if (cp.c.ur(0) == '\n') return null;
	        
	        if (cp.c.ur(0) == '\n') cp.line++;
	        if (cp.c.ur(0) == '\r') {
	            cp.c.shift(1);

	            if (!cp.c.hasCurrent()) return null;
	        }
	        
	        sb.append( (char) cp.c.urpp() );
	    }
	    return null;
	}

	int GetNumber(CurPos cp) {
	    int value = 0;
	    int neg = 0;

	    if (cp.c.hasCurrent() && cp.c.ur(0) == '-' || cp.c.ur(0) == '+') {
	        if (cp.c.ur(0) == '-') neg = 1;
	        cp.c.shift(1);
	    }
	    while (cp.c.hasCurrent() && (cp.c.ur(0) >= '0' && cp.c.ur(0) <= '9')) {
	        value = value * 10 + (cp.c.ur(0) - '0');
	        cp.c.shift(1);
	    }
	    return neg != 0 ? -value : value;
	}

	int CmdNum(String Cmd) 
	{
		for( CmdDef c : Command_Table )
		{
			if( c.Name.equals(Cmd) )
				return c.CmdId;
		}
		/*
	    int i;

	    for (i = 0;
	         i < sizeof(Command_Table) / sizeof(Command_Table[0]);
	         i++)
	        if (strcmp(Cmd, Command_Table[i].Name) == 0)
	            return Command_Table[i].CmdId;
	    for (i = 0; i < CMacros; i++)
	        if (Macros[i].Name && (strcmp(Cmd, Macros[i].Name)) == 0)
	            return i | CMD_EXT;
	    */
		
		for( int i = 0; i < Macros.size(); i++)
			if(Macros.get(i).equals(Cmd))
	            return i | CMD_EXT;
		
		System.err.println("Command not found: "+Cmd);
	    return 0; // Nop
	}

	
	//int CMacros = 0;
	List<String> Macros = new ArrayList<>();
	
	int NewCommand(String Name) 
	{
	    if (Name == null)
	        Name = "";
		/*
	    Macros = (ExMacro ) realloc(Macros, sizeof(ExMacro) * (1 + CMacros));
	    Macros[CMacros].Name = Name);
	    CMacros++;
	    return CMacros - 1;
	    */
	    Macros.add(Name);
	    return Macros.size()-1;
	}

	int ParseCommands(CurPos cp, String Name) throws IOException {
	    //if (!Name)
	    //    return 0;
	    //Word cmd;
	    String cmd;
	    int p;
	    long Cmd = NewCommand(Name) | CMD_EXT;

	    long cnt;
	    long ign = 0;

	    PutNumber(cp, CF_INT, Cmd);
	    GetOp(cp, P_OPENBRACE);
	    cnt = 1;
	    while (true) {
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

	            if ((cmd = GetWord(cp)) == null) Fail(cp, "Syntax error");
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
	            String w;
	            if ((w = GetWord(cp)) == null) Fail(cp, "Syntax error (bad variable name)");
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

	int ParseConfigFile(CurPos cp) throws IOException {
		String w = "";
	    String s = null;
	    int p = 0;

	    String ObjName = "", UpMode = "";

	    while (true) {
	        p = Parse(cp);

	        switch (p) {
	        case P_WORD:
	            if( (w = GetWord(cp)) == null) Fail(cp, "Syntax error");
	            switch (Lookup(CfgKW, w)) {
	            case K_SUB:
	                {
	                   String Name;

	                    if (Parse(cp) != P_WORD) Fail(cp, "Syntax error");
	                    if ((Name = GetWord(cp)) == null) Fail(cp, "Syntax error");
	                    PutString(cp, CF_SUB, Name);
	                    if (Parse(cp) != P_OPENBRACE) Fail(cp, "'{' expected");
	                    GetOp(cp, P_OPENBRACE);
	                    if (ParseCommands(cp, Name != null ? Name : "") == -1)
	                        Fail(cp, "Parse failed");
	                    PutNull(cp, CF_END);
	                }
	                break;
	                
	            case K_MENU:
	                {
	                	String MenuName;
	                    //int menu = -1, item = -1;

	                    if (Parse(cp) != P_WORD) Fail(cp, "Syntax error");;
	                    if ((MenuName = GetWord(cp)) == null) Fail(cp, "Syntax error");;
	                    if (Parse(cp) != P_OPENBRACE) Fail(cp, "'{' expected");
	                    GetOp(cp, P_OPENBRACE);

	                    PutString(cp, CF_MENU, MenuName);

	                    while (true) {
	                        p = Parse(cp);
	                        if (p == P_CLOSEBRACE) break;
	                        if (p == P_EOF) Fail(cp, "Unexpected EOF");
	                        if (p != P_WORD) Fail(cp, "Syntax error");

	                        if ((w = GetWord(cp)) == null) Fail(cp, "Parse failed");
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
	                            if (ParseCommands(cp, null) == -1)
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
	                            if ((w = GetWord(cp)) == null)
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
	                            if ((w = GetWord(cp)) == null)
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
	                    if ((ObjName = GetWord(cp)) == null) Fail(cp, "Parse failed");
	                    PutString(cp, CF_EVENTMAP, ObjName);

	                    //UpMode[0] = 0;
	                    if (Parse(cp) == P_COLON) {
	                        GetOp(cp, P_COLON);
	                        if (Parse(cp) != P_WORD) Fail(cp, "Syntax error");
	                        if ((UpMode = GetWord(cp)) == null) Fail(cp, "Parse failed");
	                    }
	                    PutString(cp, CF_PARENT, UpMode);
	                    if (Parse(cp) != P_OPENBRACE) Fail(cp, "'{' expected");
	                    GetOp(cp, P_OPENBRACE);

	                    while (true) {
	                        p = Parse(cp);
	                        if (p == P_CLOSEBRACE) break;
	                        if (p == P_EOF) Fail(cp, "Unexpected EOF");
	                        if (p != P_WORD) Fail(cp, "Syntax error");

	                        if ((w = GetWord(cp)) == null) Fail(cp, "Parse failed");
	                        switch (Lookup(CfgKW, w)) {
	                        case K_KEY: // mode::key
	                            if (Parse(cp) != P_KEYSPEC) Fail(cp, "'[' expected");
	                            s = GetString(cp);
	                            PutString(cp, CF_KEY, s);
	                            if (Parse(cp) != P_OPENBRACE) Fail(cp, "'{' expected");
	                            PutNull(cp, CF_KEYSUB);
	                            if (ParseCommands(cp, null) == -1) Fail(cp, "Parse failed");
	                            PutNull(cp, CF_END);
	                            break;

	                        case K_ABBREV:
	                            if (Parse(cp) != P_STRING) Fail(cp, "String expected");
	                            s = GetString(cp);
	                            PutString(cp, CF_ABBREV, s);
	                            switch (Parse(cp)) {
	                            case P_OPENBRACE:
	                                PutNull(cp, CF_KEYSUB);
	                                if (ParseCommands(cp, null) == -1) Fail(cp, "Parse failed");
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
	                                    if (s == null) Fail(cp, "String expected");
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
	                    if ((ObjName = GetWord(cp)) == null) Fail(cp, "Parse failed");
	                    PutString(cp, CF_COLORIZE, ObjName);

	                    //UpMode[0] = 0;
	                    if (Parse(cp) == P_COLON) {
	                        GetOp(cp, P_COLON);
	                        if (Parse(cp) != P_WORD) Fail(cp, "Syntax error");
	                        if ((UpMode = GetWord(cp)) == null) Fail(cp, "Parse failed");
	                    }
	                    PutString(cp, CF_PARENT, UpMode);
	                    if (Parse(cp) != P_OPENBRACE) Fail(cp, "'{' expected");
	                    GetOp(cp, P_OPENBRACE);

	                    while (true) {
	                        p = Parse(cp);
	                        if (p == P_CLOSEBRACE) break;
	                        if (p == P_EOF) Fail(cp, "Unexpected EOF");
	                        if (p != P_WORD) Fail(cp, "Syntax error");

	                        if ((w  = GetWord(cp)) == null) Fail(cp, "Parse failed");
	                        switch (Lookup(CfgKW, w)) {
	                        case K_COLOR: // mode::color
	                            if (Parse(cp) != P_OPENBRACE) Fail(cp, "'{' expected");
	                            GetOp(cp, P_OPENBRACE);
	                            PutNull(cp, CF_COLOR);

	                            while (true) {
	                                String sname, svalue;
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
	                                String colorstr, kname;
	                                //int color;

	                                if (Parse(cp) != P_STRING) Fail(cp, "String expected");
	                                colorstr = GetString(cp);
	                                colorstr = GetColor(cp, colorstr);
	                                if (Parse(cp) != P_OPENBRACE) Fail(cp, "'{' expected");
	                                GetOp(cp, P_OPENBRACE);

	                                PutString(cp, CF_KEYWORD, colorstr);

	                                while (true) {
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
	                                String opts, match;
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
	                                if (0 <= opts.indexOf('^')) match_opts |= MATCH_MUST_BOL;
	                                if (0 <= opts.indexOf('$')) match_opts |= MATCH_MUST_EOL;
	                                //if (0 <= opts.indexOf('b')) match_opts |= MATCH_MUST_BOLW;
	                                //if (0 <= opts.indexOf('e')) match_opts |= MATCH_MUST_EOLW;
	                                if (0 <= opts.indexOf('i')) match_opts |= MATCH_NO_CASE;
	                                if (0 <= opts.indexOf('s')) match_opts |= MATCH_SET;
	                                if (0 <= opts.indexOf('S')) match_opts |= MATCH_NOTSET;
	                                if (0 <= opts.indexOf('-')) match_opts |= MATCH_NOGRAB;
	                                if (0 <= opts.indexOf('<')) match_opts |= MATCH_TAGASNEXT;
	                                if (0 <= opts.indexOf('>')) match_opts &= ~MATCH_TAGASNEXT;
	                                //if (0 <= opts.indexOf('!')) match_opts |= MATCH_NEGATE;
	                                if (0 <= opts.indexOf('q')) match_opts |= MATCH_QUOTECH;
	                                if (0 <= opts.indexOf('Q')) match_opts |= MATCH_QUOTEEOL;

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
	                                if (0 <= opts.indexOf('i')) options |= STATE_NOCASE;
	                                if (0 <= opts.indexOf('<')) options |= STATE_TAGASNEXT;
	                                if (0 <= opts.indexOf('>')) options &= ~STATE_TAGASNEXT;
	                                if (0 <= opts.indexOf('-')) options |= STATE_NOGRAB;

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
	                                String colorstr, kname;
	                                //int color;

	                                if (Parse(cp) != P_STRING) Fail(cp, "String expected");
	                                colorstr = GetString(cp);
	                                colorstr = GetColor(cp, colorstr);

	                                if (Parse(cp) != P_OPENBRACE) Fail(cp, "'{' expected");
	                                GetOp(cp, P_OPENBRACE);

	                                PutString(cp, CF_HWORDS, colorstr);

	                                while (true) {
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
	                                    if (s == null) Fail(cp, "Parse failed");
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
	                    if ((ObjName = GetWord(cp)) == null) Fail(cp, "Parse failed");
	                    PutString(cp, CF_MODE, ObjName);

	                    //UpMode[0] = 0;
	                    if (Parse(cp) == P_COLON) {
	                        GetOp(cp, P_COLON);
	                        if (Parse(cp) != P_WORD) Fail(cp, "Syntax error");
	                        if ((UpMode = GetWord(cp)) == null) Fail(cp, "Parse failed");
	                    }
	                    PutString(cp, CF_PARENT, UpMode);
	                    if (Parse(cp) != P_OPENBRACE) Fail(cp, "'{' expected");
	                    GetOp(cp, P_OPENBRACE);

	                    while (true) {
	                        p = Parse(cp);
	                        if (p == P_CLOSEBRACE) break;
	                        if (p == P_EOF) Fail(cp, "Unexpected EOF");
	                        if (p != P_WORD) Fail(cp, "Syntax error");

	                        if ((w = GetWord(cp)) == null) Fail(cp, "Parse failed");
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
	                                if (s == null) Fail(cp, "Parse failed");
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
	                    if ((ObjName = GetWord(cp)) == null) Fail(cp, "Parse failed");
	                    if (Parse(cp) != P_OPENBRACE) Fail(cp, "'{' expected");
	                    GetOp(cp, P_OPENBRACE);

	                    PutString(cp, CF_OBJECT, ObjName);

	                    while (true) {
	                        p = Parse(cp);
	                        if (p == P_CLOSEBRACE) break;
	                        if (p == P_EOF) Fail(cp, "Unexpected EOF");
	                        if (p != P_WORD) Fail(cp, "Syntax error");

	                        if ((w = GetWord(cp)) == null) Fail(cp, "Parse failed");
	                        switch (Lookup(CfgKW, w)) {
	                        case K_COLOR: // mode::color
	                            if (Parse(cp) != P_OPENBRACE) Fail(cp, "'{' expected");
	                            GetOp(cp, P_OPENBRACE);
	                            PutNull(cp, CF_COLOR);

	                            while (true) {
	                                String sname, svalue;

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
	                                    if (s == null) Fail(cp, "Parse failed");
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

	                    while (true) {
	                        String sname, svalue;

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
	                        
	                        DefineColor(sname, svalue);
	                        
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

	                    if (!LoadFile(cp.name, fn, 1)) Fail(cp, "Include of file '%s' failed", fn);
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

	boolean LoadFile(String WhereName, String CfgName, int Level) throws IOException {
	    int rc;
	    //String buffer = 0;
	    //struct stat statbuf;
	    String [] last = {null};
	    String [] Cfg = {null};

	    //fprintf(stderr, "Loading file %s %s\n", WhereName, CfgName);

	    Console.JustDirectory(WhereName, last);

	    if (Console.IsFullPath(CfgName)) {
	        Cfg[0] = CfgName;
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
	/* #ifdef UNIX
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
	#else // UNIX */
	        last[0] = Console.SlashDir(last[0]);
	        last[0] += CfgName;
	        Console.ExpandPath(last[0], Cfg);
	//#endif // UNIX
	    }
	    // puts(Cfg);

	    /*
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
		*/

		byte[] allCfg = null;
		try {
			allCfg = Files.readAllBytes(Path.of(Cfg[0]));
		} catch (IOException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();

			Console.DieError(1, "Error in %s exception %s\n", Cfg, e1.toString());
			return false;
		}
	    
	    CurPos cp = new CurPos();
	    
	    cp.sz = allCfg.length;
	    //cp.a = cp.c = buffer;
	    //cp.z = cp.a + cp.sz;
	    cp.c = new ByteArrayPtr(allCfg);
	    cp.line = 1;
	    cp.name = Cfg[0];

	    rc = ParseConfigFile(cp);
	    // puts("End Loading file");
	    if (Level == 0)
	        PutNull(cp, CF_EOF);

	    if (rc == -1) {
	        Fail(cp, "Parse failed");
	    }
	    //free(buffer);
	    return rc == 0;
	}
	
	
}
