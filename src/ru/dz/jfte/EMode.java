package ru.dz.jfte;

public class EMode {
    EMode fNext = null;
    String fName;
    String MatchName = null;
    String MatchLine = null;
    RxNode MatchNameRx = null;
    RxNode MatchLineRx = null;
    EBufferFlags Flags = new EBufferFlags();
    EEventMap fEventMap;
    EMode fParent;

    // TODO EColorize fColorize = null;

    String filename;

    
    static EMode [] Modes = new EMode [1];
    
    static {
    	// TODO Modes
    	Modes[0] = new EMode(null, null, null);
    }
    
    EMode(EMode aMode, EEventMap Map, String aName) {
        fName = aName;
        fEventMap = Map;
        fParent = aMode;
        //InitWordChars();
        if (aMode != null) {
            // TODO fColorize = aMode.fColorize;
            Flags = aMode.Flags;

            // duplicate strings in flags to allow them be freed
            /*
            for (int i=0; i<BFS_COUNT; i++)
            {
                if (aMode.Flags.str[i] != null)
                    Flags.str[i] = aMode.Flags.str[i];
            }*/

            if (aMode.MatchName!=null) {
                MatchName = aMode.MatchName;
                MatchNameRx = RxNode.RxCompile(MatchName);
            }
            if (aMode.MatchLine!=null) {
                MatchLine = aMode.MatchLine;
                MatchLineRx = RxNode.RxCompile(MatchLine);
            }
        }
    }

    static EMode FindMode(String Name) 
    {
    	/*
        EMode m = Modes;

        //fprintf(stderr, "Searching mode %s\n", Name);
        while (m) {
            if (strcmp(Name, m->fName) == 0)
                return m;
            m = m->fNext;
        }
        return 0;
        */
    	
    	for( EMode m : Modes )
    		if(m.fName.equals(Name))
    			return m;
    	
    	return null;
    }
    
    
    static EMode GetModeForName(String FileName)     
    {
    	/* TODO
        //    char ext[10];
        //    char *p;
        int l, i;
        EMode m;
        RxMatchRes RM;
        char buf[81];
        int fd;

        m = Modes;
        while (m) {
            if (m->MatchNameRx)
                if (RxExec(m->MatchNameRx,
                           FileName, strlen(FileName), FileName,
                           &RM) == 1)
                    return m;
            if (m->fNext == 0) break;
            m = m->fNext;
        }

        fd = open(FileName, O_RDONLY);
        if (fd != -1) {
            l = read(fd, buf, 80);
            close(fd);
            if (l > 0) {
                buf[l] = 0;
                for (i = 0; i < l; i++) {
                    if (buf[i] == '\n') {
                        buf[i] = 0;
                        l = i;
                        break;
                    }
                }
                m = Modes;
                while (m) {
                    if (m->MatchLineRx)
                        if (RxExec(m->MatchLineRx, buf, l, buf, &RM) == 1)
                            return m;
                    if (m->fNext == 0) break;
                    m = m->fNext;
                }
            }
        }

        if ((m = FindMode(DefaultModeName)) != 0) return m;

		*/

        //m = Modes;
        //while (m && m->fNext) m = m->fNext;
        //return m;
        
    	if(Modes == null || Modes.length == 0)
    		return null;
    	
        return Modes[Modes.length-1];
    }

    
}
