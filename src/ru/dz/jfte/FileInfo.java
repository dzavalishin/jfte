package ru.dz.jfte;

public class FileInfo {
	String name;   // minimum set of file information
	long size;
	long mtime;
	int type;

	public static final int fiUNKNOWN   = 0;
	public static final int fiFILE      = 1;
	public static final int fiDIRECTORY = 2;


	FileInfo(String Name, int Type, long Size, long MTime) {
		name = Name;
		size = Size;
		type = Type;
		mtime = MTime;
		
		/*
		 * 			File myFile = new File("/home/mayur/GFG.java");
	        
	        long modifiedValue = myFile.lastModified();

		 */
		
	}

	String Name() { return name; }
	long Size() { return size; }
	int Type() { return type; }
	long MTime() { return mtime; }

	public boolean isDir() {
		return 0 != (type & fiDIRECTORY);
	}


}
