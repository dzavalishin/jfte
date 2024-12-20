package ru.dz.jfte;

import java.io.File;
import java.nio.file.FileSystems;
import java.nio.file.Path;
import java.nio.file.PathMatcher;
import java.util.logging.Logger;

public class FileFind //implements Closeable
{
	//private static final Logger log = Logger.getLogger(FileFind.class.getName());
	
	String Directory;
	//String Pattern;
	int Flags;
	PathMatcher matcher = null;


	public static final int  ffFAST       =1;  // optimization for UNIX (return name only, NO TYPE CHECK), ignored on OS/2 and NT
	public static final int  ffFULLPATH   =2;  // return full path to files
	public static final int  ffDIRECTORY  =4;  // return directories beside files (see ffFAST)
	public static final int  ffHIDDEN     =8;  // return hidden files (dot-files for UNIX)


	private String[] list;
	int current = 0;


	FileFind(String aDirectory, String aPattern, int aFlags) {
		Directory = Console.Slash(aDirectory, 0);
		//Pattern = aPattern;
		Flags = aFlags;

		File d = new File(Directory);
		list = d.list();
		
		if( null != aPattern)
			matcher = FileSystems.getDefault().getPathMatcher("glob:" + aPattern);
	}


	FileInfo FindNext() {

		FileInfo ret = null;

		while(true)
		{
			if(current >= list.length)
				return null;

			String name = list[current++];

			if (name.charAt(0) == '.')
				if ((Flags & ffHIDDEN)==0)
					continue;

			if( matcher!=null)
			{
				if(!matcher.matches(Path.of(name)))
					continue;
			}
			
			String [] fullpath = {""};
			
			if(0!= (Flags & ffFULLPATH)) {
				Console.JoinDirFile(fullpath, Directory, name);
				name = fullpath[0];
			}

			if(0!=  (Flags & ffFAST)) {
				ret = new FileInfo(name, FileInfo.fiUNKNOWN, 0, 0);
			} else {

				if(0== (Flags & ffFULLPATH)) // need it now
					Console.JoinDirFile(fullpath, Directory, name);

				File st = new File(fullpath[0]);

				if( (0 == (Flags & ffDIRECTORY)) && st.isDirectory())
					continue;

				ret = new FileInfo(name,
						st.isDirectory() ? FileInfo.fiDIRECTORY : FileInfo.fiFILE,
						st.length(),
						st.lastModified());
			}
			//printf("ok\n");
			return ret;
		}
	}


}
