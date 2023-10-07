package ru.dz.jfte;

import java.nio.file.Path;
import java.util.regex.Matcher;

import ru.dz.jfte.c.CString;

public class FileCompleter implements Completer 
{

	static final boolean isWindows = true; // XXX isWindows 

	@Override
	public int complete(String Base, String[]Match, int Count) 
	{
		//return -1;
		//char Name[MAXPATH];
		//const char *dirp;
		//char *namep;
		int count = 0;
		//char cname[MAXPATH];
		int hascname = 0;
		//RxMatchRes RM;
		//FileFind ff;
		//FileInfo fi;
		//int rc;

		if(Base == null || Base.isBlank())
			Base = ".";

		//String Name = Console.expandPath(Base);

		//	    SlashDir(Name);
		/*
	    dirp = Name;
	    namep = SepRChr(Name);
	    if (namep == Name) {
	        dirp = SSLASH;
	        namep = Name + 1;
	    } else if (namep == NULL) {
	        namep = Name;
	        dirp = SDOT;
	    } else {
		 *namep = 0;
	        namep++;
	    }*/


		Path pName = Path.of(Base).toAbsolutePath();

		String namep, dirp, cname = null;

		if(pName.getNameCount() == 1)
		{
			dirp = pName.getRoot().toString();
			namep = pName.getFileName().toString();
		}
		else
		{
			dirp = pName.getParent().toString();
			namep = pName.getFileName().toString();
		}

		int len = namep.length();
		Match[0] = Console.SlashDir(dirp);

		FileFind ff = new FileFind(dirp, "*", FileFind.ffDIRECTORY | FileFind.ffHIDDEN );
		FileInfo fi;

		while ((fi = ff.FindNext()) != null) 
		{
			String dname = fi.Name();

			boolean match = true;
			if( null != Config.CompletionFilter )
			{
				Matcher m = Config.CompletionFilter.matcher(dname);
				match = m.matches();
			}

			// filter out unwanted files
			if ( (dname.equals(".")) || (dname.equals("..")) || !match )
				continue;


			boolean partEquals = isWindows ?
					(CString.strnicmp(namep, dname, len) == 0)
					:
					(CString.strncmp(namep, dname, len) == 0);
			
			if (partEquals && dname.charAt(0) != '.' || namep.charAt(0) == '.')
			{
				count++;
				if (Count == count) {
					Match[0] = Console.Slash(Match[0], 1);
					Match[0] += dname;
					if ( fi.Type() == FileInfo.fiDIRECTORY )
						Match[0] = Console.Slash(Match[0], 1);
				} 
				else if (Count == -1) 
				{
					if (0==hascname) {
						cname = dname;
						hascname = 1;
					} else {
						int o = 0;
						/*#ifdef UNIX
	                        while (cname[o] && dname[o] && (cname[o] == dname[o])) o++;
						 */
						while( o < cname.length() && o < dname.length() && 
								0 == CString.charICmp(cname.charAt(o), dname.charAt(o)) ) 
							o++;
						//#endif
						//cname[o] = 0;
						cname = cname.substring(0, o);
					}
				}
			}


		}

		if (Count == -1) {
			Match[0] = Console.Slash(Match[0], 1);
			Match[0] += cname;
			if (count == 1) 
				Match[0] = Console.SlashDir(Match[0]);
		}
		return count;
		// */
	}

}
