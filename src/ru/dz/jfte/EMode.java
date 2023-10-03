package ru.dz.jfte;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;

public class EMode {
	private static final Logger log = Logger.getLogger(EMode.class.getName());

	EMode fNext = null;
	String fName;
	String MatchName = null;
	String MatchLine = null;

	Pattern MatchNameRx = null;
	Pattern MatchLineRx = null;

	EBufferFlags Flags = new EBufferFlags();
	EEventMap fEventMap;
	EMode fParent;

	EColorize fColorize = null;

	String filename;


	
	static EMode Modes = null;

	
	
	
	EMode(EMode aMode, EEventMap Map, String aName) {
		fName = aName;
		fEventMap = Map;
		fParent = aMode;
		//InitWordChars();
		if (aMode != null) {
			fColorize = aMode.fColorize;
			try {
				Flags = aMode.Flags.clone();
			} catch (CloneNotSupportedException e) {
				//e.printStackTrace();
				throw new RuntimeException("aMode.Flags.clone", e);
			}

			// duplicate strings in flags to allow them be freed
			/*
            for (int i=0; i<BFS_COUNT; i++)
            {
                if (aMode.Flags.str[i] != null)
                    Flags.str[i] = aMode.Flags.str[i];
            }*/

			if (aMode.MatchName!=null) {
				MatchName = aMode.MatchName;
				try {
					MatchNameRx = Pattern.compile(MatchName,Pattern.CASE_INSENSITIVE); // RxNode.RxCompile(MatchName);
				} catch(PatternSyntaxException e)
				{
					// TODO PatternSyntaxException
					System.err.printf("MatchNameRx '%s': %s", MatchName, e.toString() );
				}
			}
			if (aMode.MatchLine!=null) {
				MatchLine = aMode.MatchLine;
				try {
					MatchLineRx = Pattern.compile(MatchLine);
				} catch(PatternSyntaxException e)
				{
					// TODO PatternSyntaxException
					System.err.printf("MatchNameRx '%s': %s", MatchLine, e.toString() );
				}
			}
		}
	}

	static EMode FindMode(String Name) 
	{
		EMode m = Modes;

		while (m != null) {
			if (Name.equals(m.fName))
				return m;
			m = m.fNext;
		}

		return null;
	}


	static EMode GetModeForName(String FileName)     
	{
		//    char ext[10];
		//    char *p;
		//int l, i;
		//RxMatchRes RM;
		//char buf[81];
		//int fd;

		EMode m = Modes;

		if(!FileName.isBlank())
		{
			while (m != null) {
				if (m.MatchNameRx != null)
					//if (RxExec(m.MatchNameRx,                           FileName, strlen(FileName), FileName,                           &RM) == 1)
					if( m.MatchNameRx.matcher(FileName).matches() )
						return m;
				if (m.fNext == null) break;
				m = m.fNext;
			}

			String buf = null;
			try {
				buf = Files.readString(Path.of(FileName));
			} catch (IOException e) {
				log.log(Level.SEVERE, "GetModeForName("+FileName+")", e);			
			}

			if(buf != null && !buf.isBlank())
			{
				/*
			buf[l] = 0;
			for (i = 0; i < l; i++) {
				if (buf[i] == '\n') {
					buf[i] = 0;
					l = i;
					break;
				}
			}*/
				int eol = buf.indexOf('\n');
				if( eol >= 0)
					buf = buf.substring(0, eol);

				m = Modes;
				while (m != null) {
					if (m.MatchLineRx != null)
						//if (RxExec(m.MatchLineRx, buf, l, buf, &RM) == 1)
						if( m.MatchLineRx.matcher(buf).matches() )
							return m;
					if (m.fNext == null) break;
					m = m.fNext;
				}
			}
		}
		if ((m = FindMode(Config.DefaultModeName)) != null) 
			return m;

		m = Modes;
		while (m != null && m.fNext != null) 
			m = m.fNext;
		return m;
		/*
    	if(Modes == null || Modes.length == 0)
    		return null;

        return Modes[Modes.length-1];
		 */
	}


}
