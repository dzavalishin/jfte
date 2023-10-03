package ru.dz.jfte;

public class ECvsLog extends EBuffer 
{

	static ECvsLog CvsLogView = null;
	

	ECvsLog (int createFlags, EModel [] ARoot, String Directory, String OnFiles)
	{
		super(createFlags, ARoot, null);
		
	    int i,j,p;
	    //char msgFile[MAXPATH];

	    CvsLogView = this;
	    // Create filename for message
	/*#ifdef UNIX
	    // Use this in Unix - it says more to user
	    sprintf (msgFile,"/tmp/fte%d-cvs-msg",getpid ());
	#else */
	    tmpnam (msgFile);
	//#endif
	    SetFileName (msgFile,CvsLogMode);

	    // Preload buffer with info
	    InsertLine (0, "");
	    InsertLine (1, "CVS: -------------------------------------------------------");
	    InsertLine (2, "CVS: Enter log. Lines beginning with 'CVS:' will be removed");
	    InsertLine (3, "CVS:");
	    InsertLine (4, "CVS: Commiting in ");
	    
	    InsText( 4, 18, Directory.length(), Directory );
	    
	    if (OnFiles[0]) {
	        p=5;
	        // Go through files - use GetFileStatus to show what to do with files
	        // First count files
	        int cnt=0;i=0;
	        while (1) {
	            if (OnFiles[i]==0||OnFiles[i]==' ') {
	                while (OnFiles[i]==' ') i++;
	                cnt++;
	                if (!OnFiles[i]) break;
	            } else i++;
	        }
	        int [] position = new int[cnt];
	        int [] len=new int[cnt];
	        String status=new char[cnt];
	        
	        // Find out position and status for each file
	        i=j=0;position[0]=0;
	        while (1) {
	            if (OnFiles[i]==0||OnFiles[i]==' ') {
	                // This is not thread-safe!
	                len[j]=i-position[j];
	                char c=OnFiles[i];
	                OnFiles[i]=0;
	                status[j]=CvsView.GetFileStatus (OnFiles+position[j]);
	                if (status[j]==0) status[j]='x';
	                OnFiles[i]=c;
	                while (OnFiles[i]==' ') i++;
	                if (!OnFiles[i]) break;
	                position[++j]=i;
	            } else i++;
	        }
	        // Go through status
	        int fAdded=0,fRemoved=0,fModified=0,fOther=0;
	        for (i=0;i<cnt;i++) switch (status[i]) {
	            case 'A':case 'a':fAdded++;break;
	            case 'R':case 'r':fRemoved++;break;
	            case 'M':case 'm':fModified++;break;
	            default:fOther++;
	        }
	        // Now list files with given status
	        ListFiles (p,fAdded,"Added",cnt,position,len,status,OnFiles,"Aa");
	        ListFiles (p,fRemoved,"Removed",cnt,position,len,status,OnFiles,"Rr");
	        ListFiles (p,fModified,"Modified",cnt,position,len,status,OnFiles,"Mm");
	        ListFiles (p,fOther,"Other",cnt,position,len,status,OnFiles,"AaRrMm",1);
	        
	    } else {
	        InsertLine (5,4,"CVS:");
	        InsertLine (6,30,"CVS: Commiting whole directory");
	        p=7;
	    }
	    InsertLine (p,4,"CVS:");
	    InsertLine (p+1,60,"CVS: -------------------------------------------------------");
	    SetPos (0,0);
	    FreeUndo ();
	    Modified=0;
	}

	@Override
	public void close () {
		super.close();
	    CvsLogView=null;
	}

	void ListFiles (int [] p, int fCount, String title, int cnt, int [] position,
	                         int []len, String status, String list, String excinc, int exc) {
	    if (fCount != 0) {
	        InsertLine (p[0]++, 4, "CVS:");
	        
	        int i = title.length();
	        
	        InsertLine( p , 5, "CVS: ");
	        InsText ( p, 5, i, title);
	        InsText (p,i+=5,5, " file");
	        i+=5;
	        
	        if(fCount!=1) 
	        	InsText (p,i++,1,"s");
	        
	        InsText (p++,i,1,":");
	        
	        for( i=0; i < cnt; i++ )
	            if (!!strchr (excinc,status[i])^!!exc) 
	            {
	                // Should be displayed
	                InsertLine (p,9,"CVS:     ");
	                InsText (p,9,1,status+i);InsText (p,10,1," ");
	                InsText (p++,11,len[i],list+position[i]);
	            }
	    }
	}

	// Overridden because we don't want to load file
	@Override
	EViewPort CreateViewPort(EView V) {
	    V.Port = new EEditPort(this, V);
	    AddView(V);
	    return V.Port;
	}

	@Override
	boolean CanQuit () {
	    return false;
	}

	@Override
	int ConfQuit (GxView V, int multiFile) {
	    int i;

	    switch (V.Choice (GPC_ERROR,"CVS commit pending",3,"C&ommit","&Discard","&Cancel","")) 
	    {
	        case 0: // Commit
	            // First save - this is just try
	            if (!Save()) return 0;
	            
	            // Now remove CVS: lines and really save
	            for (i=0;i<RCount;) 
	            {
	                ELine l = RLine (i);
	                if (l.getCount() >= 4 && "CVS:".equals(l.Chars.subSequence(0, 4))) 
	                	DelLine (i);
	                else 
	                	i++;
	            }
	            Save ();
	            // DoneCommit returns 0 if OK
	            return !CvsView.DoneCommit (1);
	            
	        case 1: // Discard
	            CvsView.DoneCommit (0);
	            return 1;
	            
	        case 2: // Cancel
	        default:
	            return 0;
	    }
	}

	@Override
	String GetName() {
	    return "CVS log";
	}

	@Override
	String GetInfo () {
	    return String.format( 
	    		"%2d %04d:%03d%cCVS log: %-140s",
	    		ModelNo,1+CP.Row,1+CP.Col,
	    		Modified != 0 ? '*':' ', FileName);
	}

	@Override
	void GetTitle(String [] ATitle, String [] ASTitle) {
	    ATitle[0] = ASTitle[0] = "CVS log";
	}
	
	
	boolean InsertLine(int row, String s) {
		return InsertLine(new EPoint(row, 0), s.length(), s);
		}	
	
	boolean InsertLine(int row, int ACount, String AChars) {
		return InsertLine(new EPoint(row, 0), ACount, AChars);
		}	
	
	
}
