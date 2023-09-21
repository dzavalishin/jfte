package ru.dz.jfte;

import java.io.Closeable;
import java.io.File;
import java.io.FilenameFilter;

public class FileFind //implements Closeable
{
	String Directory;
	String Pattern;
	int Flags;


	public static final int  ffFAST       =1;  // optimization for UNIX (return name only, NO TYPE CHECK), ignored on OS/2 and NT
	public static final int  ffFULLPATH   =2;  // return full path to files
	public static final int  ffDIRECTORY  =4;  // return directories beside files (see ffFAST)
	public static final int  ffHIDDEN     =8;  // return hidden files (dot-files for UNIX)


	private String[] list;
	int current = 0;


	FileFind(String aDirectory, String aPattern, int aFlags) {
		Directory = Console.Slash(aDirectory, 0);
		Pattern = aPattern;
		Flags = aFlags;

		File d = new File(Directory);
		//FilenameFilter filter;
		//list = d.list(filter);
		list = d.list();
	}

	/*
	@Override
	public void close() {

		// TODO close
	}*/

	int FindFirst(FileInfo []fi) {
		return FindNext(fi);
	}

	int FindNext(FileInfo []fi) {

		fi[0] = null;
		while(true)
		{
			if(current >= list.length)
				return -1;

			String name = list[current++];

			if (name.charAt(0) == '.')
				if ((Flags & ffHIDDEN)==0)
					continue;

			if (Pattern != null && fnmatch(Pattern, name, 0) != 0)
				continue;

			if(0!= (Flags & ffFULLPATH)) {
				JoinDirFile(fullpath, Directory, name);
				name = fullpath;
			}

			if(0!=  (Flags & ffFAST)) {
				fi[0] = new FileInfo(name, FileInfo.fiUNKNOWN, 0, 0);
			} else {
				// stat st;

				if(0== (Flags & ffFULLPATH)) // need it now
					JoinDirFile(fullpath, Directory, name);

				File st = new File(fullpath);

				if( (0 == (Flags & ffDIRECTORY)) && st.isDirectory())
					continue;

				fi[0] = new FileInfo(name,
						st.isDirectory() ? FileInfo.fiDIRECTORY : FileInfo.fiFILE,
						st.length(),
						st.lastModified());
			}
			//printf("ok\n");
			return 0;
		}
		return -1;
	}


}
