package ru.dz.jfte;

/**
 * 
 *  Class providing access to most of CVS commands.
 *  
 * @author dz
 *
 */

public class ECvs extends ECvsBase implements GuiDefs 
{
    private String LogFile = null;
    private int Commiting = 0;

    
	static ECvs CvsView = null;
	static final String CvsStatusChars = "?UPMCAR";
	
	
	
    ECvs (int createFlags, EModel []ARoot, String ADir, String ACommand, String AOnFiles ) 
    {
    	super(createFlags,ARoot,"CVS");
    	
        CvsView=this;
        RunPipe (ADir,ACommand,AOnFiles);
    }

    ECvs (int createFlags,EModel [] ARoot) 
    {
    	super(createFlags,ARoot,"CVS");
        CvsView=this;
    }

    @Override
    public void close() {
        CvsView=null;
        RemoveLogFile ();
    }

    void RemoveLogFile () {
        if (LogFile!=null) {
            Console.unlink (LogFile);
            LogFile=null;
        }
    }

    String MarkedAsList () 
    {
        String s = "";
        
        for( CvsLine l : Lines)
        {
        	if(0 != (l.Status & 2)) 
        		s += l.File + " ";
        }

        if( s.isBlank() )
        {
        	CvsLine l = Lines.get(Row);
            // Nothing marked, use the file at cursor
            if(0 != (l.Status & 4))
            	return l.File;
            else 
            	return null;
        }
        
        return s;
    }

    char GetFileStatus (String file) {
        // Search backward, file can be present several times (old messages)
    	/*
        for (int i=LineCount-1;i>=0;i--)
            if (Lines[i].File&&filecmp (Lines[i].File,file)==0) return Lines[i].Msg[0];
            */
    	
        for (int i = Lines.size()-1; i >= 0; i--)
        {
        	CvsLine l = Lines.get(i);
            if(l.File != null && Console.filecmp(l.File,file)==0) 
            	return l.Msg.charAt(0);
        }   
        
        return 0;
    }

    void ParseLine (String line,int len) {
        //if(len > 2 && line.charAt(1)==' ' && strchr (CvsStatusChars,line[0])) 
        if(len > 2 && line.charAt(1) == ' ' && CvsStatusChars.indexOf(line.charAt(0)) >= 0 ) 
        {
            AddLine (line+2,-1,line,5);
        } 
        else 
        	AddLine (null,-1,line, 0);
    }

    int RunPipe (String ADir,String ACommand,String AOnFiles) {
        Commiting=0;
        if (!Console.SameDir (Directory,ADir)) FreeLines ();
        return super.RunPipe (ADir,ACommand,AOnFiles);
    }

    void ClosePipe () {
        super.ClosePipe();
        
        if (Commiting != 0 && 0 == ReturnCode) {
            // Successful commit - reload files
            // Is it safe to do this ? Currently not done, manual reload is needed
        }
        Commiting=0;
    }

    int RunCommit (String ADir,String ACommand,String AOnFiles) {
        if (!Console.SameDir (Directory,ADir)) FreeLines ();

        Command=ACommand;
        Directory=ADir;
        OnFiles=AOnFiles;

        RemoveLogFile();
        // Disallow any CVS command while commiting
        Running=true;

        // Create message buffer
        ECvsLog cvslog=new ECvsLog (0,ActiveModel,Directory,OnFiles);
        LogFile=cvslog.FileName;
        View.SwitchToModel (cvslog);

        AddLine (LogFile,-1,(String )"CVS commit start - enter message text",1);

        return 0;
    }


    int DoneCommit (int commit) {
        Running=false;
        // Remove line with link to log
        //LineCount--;
        Lines.remove(Lines.size()-1);
        
        UpdateList ();

        if (commit!=0) {
            // We need a copy of Command/Directory/OnFiles because RunPipe deletes them!
            //String ACommand=(String )malloc (strlen (Command)+strlen (LogFile)+10);
            String ADirectory=Directory;
            String AOnFiles=OnFiles;
            String ACommand= String.format("%s -F %s",Command,LogFile);
            
            int ret = RunPipe(ADirectory, ACommand, AOnFiles);

            // We set Commiting after RunPipe since it sets it to 0
            // This is OK since FTE is not multi-threaded
            Commiting=1;

            if (EView.ActiveView.Model== ECvsLog.CvsLogView) {
                // CvsLogView is currently active, move CvsView just after it
                if (ECvsLog.CvsLogView.Next != CvsView) 
                {
                    // Need to move, is not at right place yet
                    // Here we use the fact that if current model is closed,
                    // the one just after it (Next) is focused.
                    CvsView.Prev.Next=CvsView.Next;
                    CvsView.Next.Prev=CvsView.Prev;
                    CvsView.Next=ECvsLog.CvsLogView.Next;
                    ECvsLog.CvsLogView.Next.Prev=CvsView;
                    ECvsLog.CvsLogView.Next=CvsView;
                    CvsView.Prev=ECvsLog.CvsLogView;
                }
            }
            // else - CvsLogView is not active, there is need to make CvsView
            // active some other way. However, SwitchToModel() or SelectModel()
            // calls does not work. Currently I don't know how to do this. !!!

            return ret;
        } else {
            RemoveLogFile ();
            UpdateList ();
            return 0;
        }
    }

    // If running, can't be closed without asking
    @Override
    boolean CanQuit () {
        return !Running;
    }

    // Ask user if we can close this model
    @Override
    int ConfQuit (GxView V,int multiFile) {
        if (null != ECvsLog.CvsLogView) {
            // Log is open
            if (0 != ECvsLog.CvsLogView.ConfQuit (V,multiFile)) {
                // Commit confirmed or discarded - depends on Running
                EView.ActiveView.DeleteModel(ECvsLog.CvsLogView);
            } else return 0;
        }
        if (Running) {
            // CVS command in progress
            switch (V.Choice (GPC_ERROR,"CVS command is running",2,"&Kill","&Cancel","")) {
                case 0: // Kill
                    return 1;
                case 1: // Cancel
                default:
                    return 0;
            }
        } else return 1;
    }

    // Event map - this name is used in config files when defining eventmap
    @Override
    EEventMap GetEventMap () {
        return EEventMap.FindEventMap("CVS");
    }
    
}
