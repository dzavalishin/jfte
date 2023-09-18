package ru.dz.jfte;

public class FileCompleter implements Completer {

	
	@Override
	public int complete(String Base, String[]Match, int Count) 
	{
		return -1;
		/** TODO completer
	    char Name[MAXPATH];
	    const char *dirp;
	    char *namep;
	    int len, count = 0;
	    char cname[MAXPATH];
	    int hascname = 0;
	    RxMatchRes RM;
	    FileFind *ff;
	    FileInfo *fi;
	    int rc;

	    if (strcmp(Base, "") == 0) {
	        if (ExpandPath(".", Name) != 0) return -1;
	    } else {
	        if (ExpandPath(Base, Name) != 0) return -1;
	    }
//	    SlashDir(Name);
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
	    }
	    
	    len = strlen(namep);
	    strcpy(Match, dirp);
	    SlashDir(Match);
	    cname[0] = 0;

	    ff = new FileFind(dirp, "*",
	                      ffDIRECTORY | ffHIDDEN
	                     );
	    if (ff == 0)
	        return 0;
	    rc = ff->FindFirst(&fi);
	    while (rc == 0) {
	        char *dname = fi->Name();

	        // filter out unwanted files
	        if ((strcmp(dname, ".") != 0) &&
	            (strcmp(dname, "..") != 0) &&
	            (!CompletionFilter || RxExec(CompletionFilter, dname, strlen(dname), dname, &RM) != 1))
	        {
	            if ((
	#if defined(UNIX)
	                strncmp
	#else // os2, nt, ...
	                strnicmp
	#endif
	                (namep, dname, len) == 0)
	                && (dname[0] != '.' || namep[0] == '.'))
	            {
	                count++;
	                if (Count == count) {
	                    Slash(Match, 1);
	                    strcat(Match, dname);
	                    if (
	#if defined(USE_DIRENT) // for SPEED
	                        IsDirectory(Match)
	#else
	                        fi->Type() == fiDIRECTORY
	#endif
	                       )
	                        Slash(Match, 1);
	                } else if (Count == -1) {
	                    
	                    if (!hascname) {
	                        strcpy(cname, dname);
	                        hascname = 1;
	                    } else {
	                        int o = 0;
	#ifdef UNIX
	                        while (cname[o] && dname[o] && (cname[o] == dname[o])) o++;
	#endif
	#if defined(OS2) || defined(NT) || defined(DOS) || defined(DOSP32)
	                        while (cname[o] && dname[o] && (toupper(cname[o]) == toupper(dname[o]))) o++;
	#endif
	                        cname[o] = 0;
	                    }
	                }
	            }
	        }
	        delete fi;
	        rc = ff->FindNext(&fi);
	    }
	    delete ff;
	    if (Count == -1) {
	        Slash(Match, 1);
	        strcat(Match, cname);
	        if (count == 1) SlashDir(Match);
	    }
	    return count;
	    */
	}
	
}
