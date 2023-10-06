package ru.dz.jfte;

import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import ru.dz.jfte.c.CString;

public class Tags implements ModeDefs 
{
	private static final Logger log = Logger.getLogger(Tags.class.getName());


	static class TagData implements Comparable<TagData> 
	{
		String Tag;        // tag name pos
		String FileName;
		String TagBase;    // name of tag file
		int Line;
		String StrFind;    // string to find

		@Override
		public int compareTo(TagData o) {
			return Tag.compareTo(o.Tag);
		}
	};

	static class TagStack {
		String FileName;
		int Line, Col;
		TagStack Next;
		int TagPos;
		String CurrentTag;
	};

	static List<TagData> TagD = new ArrayList<>();



	//static String TagMem = 0;
	static int TagLen = 0;
	static int ATagMem  = 0;

	static int CTags = 0;            // number of tags
	static int ATags = 0;
	//static TagData *TagD = 0;
	static int  [] TagI = null;
	//static int TagFileCount = 0;
	//static int [] TagFiles = 0;

	static List<String> TagFiles = new ArrayList<>();

	static boolean TagFilesLoaded = false;   // tag files are loaded at first lookup
	static String CurrentTag = null;
	static int TagPosition = -1;
	static TagStack TStack;

	/*
	static int AllocMem(String Mem, int Len) { 
	    int N = 1024;
	    String NM;
	    int TagPos = TagLen;

	    while (N < TagLen + Len) N <<= 1;
	    if (ATagMem < N || TagMem == 0) {
	        NM = (String )realloc((void *)TagMem, N);
	        if (NM == 0)
	            return -1;
	        TagMem = NM;
	        ATagMem = N;
	    }
	    memcpy(TagMem + TagLen, Mem, Len);
	    TagLen += Len;
	    return TagPos;
	}*/

	static void AddTag(String Tag, String FileName, String TagBase, int Line, String StrFind) 
	{
		TagData td = new TagData();

		td.Tag = Tag;
		td.Line = Line;
		td.FileName = FileName;
		td.TagBase = TagBase;
		td.StrFind = StrFind;

		TagD.add(td);
	}

	/*
	int cmptags(const void *p1, const void *p2) {
	    return strcmp(TagMem + TagD[*(int *)p1].Tag,
	                  TagMem + TagD[*(int *)p2].Tag);
	}*/

	static int SortTags() {

		Collections.sort(TagD);
		/*
	    int *NI;
	    int i;

	    if (CTags == 0)
	        return 0;

	    NI = (int *)realloc((void *)TagI, CTags * sizeof(int));
	    if (NI == 0)
	        return -1;
	    TagI = NI;
	    for (i = 0; i < CTags; i++)
	        TagI[i] = i;

	    qsort(TagI, CTags, sizeof(TagI[0]), cmptags);
		 */
		return 0;
	}

	public static int TagsLoad(int id)  { /*FOLD00*/
		//char line[2048];
		//int fd;
		//struct stat sb;
		//long size;

		/*
	    TagI = 0;

	    BufferedReader fd = new BufferedReader(Path.of(TagFiles.get(id)), Main.charset));

	    if (fd == null)	        return -1;

	    if (fstat(fd, &sb) == -1)
	        return -1;

	    if ((tags = (String )malloc(sb.st_size)) == 0) {
	        close(fd);
	        return -1;
	    }

	    size = read(fd, tags, sb.st_size);
	    close(fd);
	    if (size != sb.st_size)
	        return -1;
		 */

		try {
			List<String> lines = Files.readAllLines(Path.of(TagFiles.get(id)), Main.charset);
			for( String line : lines )
			{
				loadLine(line, TagFiles.get(id));
			}
		} catch (IOException e) {
			log.log(Level.SEVERE, "Can't read "+TagFiles.get(id), e);
			return -1;
		}

		/*
	    if (TagMem == 0) { // preallocate (useful when big file)
	        String NM;

	       // NM = (String )realloc((void *)TagMem, TagLen + sb.st_size);
	        if (NM != 0) {
	            TagMem = NM;
	            ATagMem = TagLen + sb.st_size;
	        }
	    }*/

		//String e = tags + sb.st_size;
		//CStringPtr e = new CStringPtr( )

		//String LTag, *LFile, *LLine;
		//int TagL, FileL/*, LineL*/;
		//int MTag, MFile;

		/* 

	    while (p < e) {
	        LTag = p;
	        while (p < e && *p != '\t') p++;
	        if (p < e && *p == '\t') *p++ = 0;
	        else break;
	        TagL = p - LTag;
	        LFile = p;
	        while (p < e && *p != '\t') p++;
	        if (p < e && *p == '\t') *p++ = 0;
	        else break;
	        FileL = p - LFile;
	        LLine = p;
	        while (p < e && *p != '\r' && *p != '\n') p++;
	        if (p < e && *p == '\r') *p++ = 0;           // optional
	        if (p < e && *p == '\n') *p++ = 0;
	        else break;
	        //LineL = p - LLine;

	        MTag = AllocMem(LTag, TagL + FileL);

	        if (MTag == -1)
	            break;

	        MFile = MTag + TagL;

	        if (LLine[0] == '/') {
	            String AStr = LLine;
	            String p = AStr + 1;
	            String d = AStr;
	            int MStr;

	            while (*p) {
	                if (*p == '\\') {
	                    p++;
	                    if (*p)
		 *d++ = *p++;
	                } else if (*p == '^' || *p == '$') p++;
	                else if (*p == '/')
	                    break;
	                else
		 *d++ = *p++;
	            }
		 *d = 0;
	            if (stricmp(d - 10, "/*FOLD00* /") == 0)
	                d[-11] = 0; /* remove our internal folds * /

	            MStr = AllocMem(AStr, strlen(AStr) + 1);
	            if (MStr == -1)
	                break;

	            if (AddTag(MTag, MFile, TagFiles[id], -1, MStr) == -1)
	                break;
	        } else {
	            if (AddTag(MTag, MFile, TagFiles[id], atoi(LLine), -1) == -1)
	                break;
	        }
	    }
		 */
		return 0;
	}

	private static void loadLine(String s, String TagFile) 
	{
		// Comment
		if(s.charAt(0) == '!')
			return;
		
		String [] part = s.split("\t");

		String MTag = part[0];
		String MFile = part[1];
		String LLine = part[2];

		if(LLine.charAt(0) == '/')
		{
			StringBuilder sb = new StringBuilder();
			
			int p = 1;
			while (p < LLine.length()) 
			{
				char c = LLine.charAt(p++);
				if (c == '\\') {
					if (p < LLine.length())
						sb.append(LLine.charAt(p++));
				} 
				else if (c == '^' || c == '$') continue;
				else if (c == '/')
					break;
				else
					sb.append(c);
			}
            //AddTag(MTag, MFile, TagFiles[id], -1, sb.toString());
            AddTag(MTag, MFile, TagFile, -1, sb.toString());

		}
		else
		{
			int cpos = LLine.indexOf(';');
			
			if(cpos >= 0)
				LLine = LLine.substring(0, cpos); 
			
			int line = Integer.parseInt(LLine);

			AddTag(MTag, MFile, TagFile, line, null);
		}
	}

	static int TagsAdd(String FileName) { /*FOLD00*/
		TagFiles.add(FileName);
		return 1;
	}

	static void TagsSave(BufferedWriter w) throws IOException { /*FOLD00*/
		for (String f : TagFiles)
			w.write("T|"+f+"\n");
	}

	static void ClearTagStack() { /*FOLD00*/
		CurrentTag = null;
		TagPosition = -1;
		TStack = null;	    
	}

	static ExResult TagLoad(String FileName) 
	{
		if (TagsAdd(FileName) == 0)
			return ExResult.ErFAIL;

		ClearTagStack();
		if (TagFilesLoaded) {
			if (TagsLoad(TagFiles.size() - 1) == -1) 
				return ExResult.ErFAIL;

			if (SortTags() == -1) {
				TagClear();
				return ExResult.ErFAIL;
			}
		}
		return ExResult.ErOK;
	}

	static int LoadTagFiles() { /*FOLD00*/
		int i;

		assert(!TagFilesLoaded);
		for (i = 0; i < TagFiles.size(); i++)
			if (TagsLoad(i) == -1) {
				TagClear();
				return 0;
			}
		if (SortTags() == -1) {
			TagClear();
			return 0;
		}
		TagFilesLoaded = true;
		return 1;
	}

	static void TagClear() { /*FOLD00*/
		TagD.clear();
		TagFiles.clear();

		TagI = null;
		//CTags = 0;
		ATags = 0;

		//TagFileCount = 0;
		TagFilesLoaded = false;

		//TagMem = 0;
		TagLen = 0;
		ATagMem = 0;

		ClearTagStack();
	}

	static int GotoFilePos(EView View, String FileName, int Line, int Col) { /*FOLD00*/
		if (!Console.FileLoad(0, FileName, null, View))
			return 0;
		if (! ((EBuffer )EModel.ActiveModel[0]).Loaded)
			((EBuffer )EModel.ActiveModel[0]).Load();
		((EBuffer )EModel.ActiveModel[0]).CenterNearPosR(Col, Line);
		return 1;
	}

	static int GotoTag(int M, EView View) { /*FOLD00*/
		String path;
		//char Dir[MAXPATH];
		TagData TT = TagD.get(TagI[M]);

		String Dir = Console.directory(TT.TagBase);

		if (Console.IsFullPath(TT.FileName)) {
			path = TT.FileName;
		} else {
			path = Dir + "/" + TT.FileName;
		}
		if (TT.Line != -1) {
			if (GotoFilePos(View, path, TT.Line - 1, 0) == 0)
				return 0;
		} else {
			if (GotoFilePos(View, path, 0, 0) == 0)
				return 0;
			if (!((EBuffer )EModel.ActiveModel[0]).FindStr(TT.StrFind, TT.StrFind.length(), 0))
				return 0;
		}
		((EBuffer )EModel.ActiveModel[0]).FindStr(TT.Tag, TT.Tag.length(), 0);
		return 1;
	}

	static int PushPos(EBuffer B) { /*FOLD00*/
		TagStack T;

		T = new TagStack();

		T.FileName = B.FileName;
		T.Line = B.VToR(B.CP.Row);
		T.Col = B.CP.Col;
		T.Next = TStack;
		T.CurrentTag = CurrentTag;
		CurrentTag = null;
		T.TagPos = TagPosition;
		TagPosition = -1;
		TStack = T;
		return 1;
	}

	static int TagGoto(EView View, String Tag) 
	{
		if (!TagFilesLoaded)
			if (LoadTagFiles() == 0)
				return 0;

		int L = 0, R = CTags, M, cmp;

		if (CTags == 0)
			return 0;

		while (L < R) {
			M = (L + R) / 2;
			cmp = CString.strcmp(Tag, TagD.get(TagI[M]).Tag);
			if (cmp == 0) {
				while (M > 0 && CString.strcmp(Tag, TagD.get(TagI[M - 1]).Tag) == 0)
					M--;

				if (GotoTag(M, View) == 0)
					return 0;

				CurrentTag = Tag;
				TagPosition = M;
				return 1;
			} else if (cmp < 0) {
				R = M;
			} else {
				L = M + 1;
			}
		}
		return 0; // tag not found
	}

	static int TagFind(EBuffer B, EView View, String Tag) { /*FOLD00*/
		//assert(View != 0 && Tag != 0 && B != 0);

		if (!TagFilesLoaded )
			if (LoadTagFiles() == 0)
				return 0;

		int L = 0, R = CTags, M, cmp;

		if (CurrentTag != null) {
			if (CString.strcmp(CurrentTag, Tag) == 0) {
				if (PushPos(B) == 0)
					return 0;

				CurrentTag = Tag;
				if (CurrentTag == null)
					return 0;
				TagPosition = TStack.TagPos;

				return TagNext(View) == ExResult.ErOK ? 1 : 0; // TODO right?
			}
		}

		if (CTags == 0)
			return -1;

		while (L < R) {
			M = (L + R) / 2;
			cmp = CString.strcmp(Tag, TagD.get(TagI[M]).Tag);
			if (cmp == 0) {
				while (M > 0 && CString.strcmp(Tag, TagD.get(TagI[M - 1]).Tag) == 0)
					M--;

				if (PushPos(B) == 0)
					return 0;

				if (GotoTag(M, View) == 0)
					return 0;

				CurrentTag = Tag;
				TagPosition = M;

				return 1;
			} else if (cmp < 0) {
				R = M;
			} else {
				L = M + 1;
			}
		}
		return 0; // tag not found
	}

	public static boolean TagDefined(String Tag) {
		int L = 0, R = CTags, M, cmp;

		if (!TagFilesLoaded) // !!! always?
			if (LoadTagFiles() == 0)
				return false;

		if (CTags == 0)
			return false;

		while (L < R) {
			M = (L + R) / 2;
			cmp = CString.strcmp(Tag, TagD.get(TagI[M]).Tag);
			if (cmp == 0)
				return true;
			else if (cmp < 0)
				R = M;
			else
				L = M + 1;
		}
		return false; // tag not found
	}

	public static int TagComplete(String [] Words, int [] WordsPos, int WordsMax, String Tag) 
	{
		if ((Tag == null) || (Words == null) || (WordsPos[0] >= WordsMax))
			return 0;

		if (!TagFilesLoaded)
			if (LoadTagFiles() == 0)
				return 0;

		if (CTags == 0)
			return 0;

		int L = 0, R = CTags, len = Tag.length();

		while (L < R) {
			int c, M;

			M = (L + R) / 2;
			c = CString.strncmp(Tag, TagD.get(TagI[M]).Tag, len);
			if (c == 0) {
				while (M > 0 &&
				CString.strncmp(Tag,  TagD.get(TagI[M - 1]).Tag, len) == 0)
					M--;            // find begining
				int N = M, w = 0;
				while(CString.strncmp(Tag,  TagD.get(TagI[N]).Tag, len) == 0) {
					// the first word is not tested for previous match
					if (w == 0 || CString.strcmp(TagD.get(TagI[N]).Tag,
							TagD.get(TagI[N-1]).Tag) != 0) {
						int l =  TagD.get(TagI[N]).Tag.length() - len;
						if (l > 0) {
							String s = TagD.get(TagI[N]).Tag.substring(len);
							Words[(WordsPos[0])++] = s;
							w++; // also mark the first usage
							if (WordsPos[0] >= WordsMax)
								break;
						}
					}
					N++;
				}
				return w;
			} else if (c < 0) {
				R = M;
			} else {
				L = M + 1;
			}
		}
		return 0; // tag not found
	}

	public static ExResult TagNext(EView View) { /*FOLD00*/

		if (CurrentTag == null || TagPosition == -1) {
			return ExResult.ErFAIL;
		}

		if (TagPosition < CTags - 1 && CString.strcmp(CurrentTag, TagD.get(TagI[TagPosition + 1]).Tag) == 0) {
			TagPosition++;
			if (GotoTag(TagPosition, View) == 0)
				return ExResult.ErFAIL;
			return ExResult.ErOK;
		}
		View.Msg(S_INFO, "No next match for tag.");
		return ExResult.ErFAIL;
	}

	public static ExResult TagPrev(EView View) { /*FOLD00*/
		if (CurrentTag == null || TagPosition == -1) {
			View.Msg(S_INFO, "No current tag.");
			return ExResult.ErFAIL;
		}

		if (TagPosition > 0 && CString.strcmp(CurrentTag, TagD.get(TagI[TagPosition - 1]).Tag) == 0) {
			TagPosition--;
			if (GotoTag(TagPosition, View) == 0)
				return ExResult.ErFAIL;
			return ExResult.ErOK;
		}
		View.Msg(S_INFO, "No previous match for tag.");
		return ExResult.ErFAIL;
	}

	public static ExResult TagPop(EView View) { /*FOLD00*/
		TagStack T = TStack;

		if (T != null) {
			TStack = T.Next;

			CurrentTag = T.CurrentTag;
			TagPosition = T.TagPos;

			if (GotoFilePos(View, T.FileName, T.Line, T.Col) == 0) {
				return ExResult.ErFAIL;
			}
			return ExResult.ErOK;
		}
		View.Msg(S_INFO, "Tag stack empty.");
		return ExResult.ErFAIL;
	}


	/*
	public static void main(String[] args) {
		TagLoad("P:/phantomuserland/tags");
		LoadTagFiles();
	} */

}
