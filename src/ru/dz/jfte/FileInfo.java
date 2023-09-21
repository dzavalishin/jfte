package ru.dz.jfte;

public class FileInfo {
	String name;   // minimum set of file information
	long size;
	long mtime;
	int type;


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
		// TODO Auto-generated method stub
		//return false;
	}


}
