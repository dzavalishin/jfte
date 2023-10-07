package ru.dz.jfte;

import ru.dz.jfte.c.BitOps;

/**
 * o_cvsdiff.cpp
 *
 * Contributed by Martin Frydl <frydl@matfyz.cz>
 *
 * Class showing output from CVS diff command. Allows copying of lines
 * to clipboard and allows to jump to lines in real sources.
 * 
 */

public class ECvsDiff extends ECvsBase implements BufferDefs 
{

    private int 	CurrLine, ToLine, InToFile;
    private String 	CurrFile = null;
	
	
	
	
	static ECvsDiff CvsDiffView = null;

	
	
	
	
	

	ECvsDiff (int createFlags, EModel []ARoot, String ADir, String ACommand, String AOnFiles)
	{
		super( createFlags, ARoot, "CVS diff");
		
	    CvsDiffView=this;
	    
	    CurrLine=0;
	    ToLine=0;
	    InToFile=0;
	    
	    RunPipe (ADir,ACommand,AOnFiles);
	}

	@Override
	public void close() {
	    CvsDiffView=null;
	}

	void ParseFromTo (String line, int len) 
	{
		/*
	    String end;
	    CurrLine = strtol (line+4,end,10)-1;
	    if (*end==',') ToLine=atoi (end+1);else ToLine=CurrLine+1;
	    */

		line = line.substring(4).strip();
		
		CurrLine = Integer.parseInt(line);
		int cp = line.indexOf(',');
		if( cp >= 0)
			ToLine = Integer.parseInt(line.substring(cp));
		else
			ToLine=CurrLine+1;
		
	    if( !(CurrLine<ToLine&&ToLine>0) ) 
	    	CurrLine=ToLine=0;
	}

	@Override
	void ParseLine (String line, int len) 
	{
	    if( len > 7 && BitOps.strncmp(line,"Index: ",7)==0) 
	    {
	        // Filename
	        CurrFile=line+7;
	        CurrLine=ToLine=InToFile=0;
	        AddLine (CurrFile,-1,line,0);
	    } 
	    else if (len>8 && BitOps.strncmp(line,"*** ",4)==0) 
	    {
	        // From file or from hunk
	        if (BitOps.strcmp(line.substring(len-5)," ****")==0) 
	        {
	            // From hunk
	            ParseFromTo (line,len);
	        }
	        InToFile=0;
	        AddLine (null,-1,line,0);
	    } 
	    else if (len>8 && BitOps.strncmp(line,"--- ",4)==0) 
	    {
	        // To file or to hunk
	        if(BitOps.strcmp(line.substring(len-5)," ----")==0) {
	            // To hunk
	            if (CurrFile != null) 
	            {
	                ParseFromTo (line,len);
	                AddLine (CurrFile,CurrLine,line,1);
	            } else AddLine (null,-1,line,0);
	        } else {
	            // To-file
	            AddLine (CurrFile,-1,line,0);
	        }
	        InToFile=1;
	    } 
	    else if(line.equals("***************")) 
	    {
	        // Hunk start
	        CurrLine=ToLine=0;
	        AddLine (null,-1,line,0);
	    } 
	    else if (CurrLine<ToLine) 
	    {
	        // Diff line (markable, if CurrFile is set, also hilited)
	        if (InToFile!=0) AddLine (CurrFile,CurrLine,line,5);
	        else AddLine (null,CurrLine,line,4);
	        CurrLine++;
	    } else AddLine (null,-1,line,0);
	}

	@Override
	int RunPipe (String ADir,String ACommand,String AOnFiles) {
	    FreeLines ();
	    CurrLine=ToLine=InToFile=0;
	    CurrFile=null;
	    return super.RunPipe (ADir,ACommand,AOnFiles);
	}

	@Override
	ExResult ExecCommand(ExCommands Command, ExState State) {
	    switch (Command) {
	        case ExBlockCopy: return BlockCopy (false);
	        case ExBlockCopyAppend: return BlockCopy (true);
	        default: break;
	    }
	    return super.ExecCommand(Command, State);
	}

	ExResult BlockCopy (boolean Append) 
	{
	    if (EBuffer.SSBuffer==null) return ExResult.ErFAIL;
	    
	    if (Append) {
	        if (Config.SystemClipboard) ClipData.GetPMClip();
	    } 
	    else 
	    	EBuffer.SSBuffer.Clear ();
	    
	    EBuffer.SSBuffer.BlockMode=bmLine;
	    
	    // How to set these two ?
	    EBuffer.BFI_SET(EBuffer.SSBuffer,BFI_TabSize,8);
	    EBuffer.BFI_SET(EBuffer.SSBuffer,BFI_ExpandTabs,0);
	    EBuffer.BFI_SET(EBuffer.SSBuffer,BFI_Undo,0);
	    
	    // Go through list of marked lines
	    int last=-1,tl=0;
	    for( int i=0; i < Lines.size(); i++ ) {
	        if(0!= (Lines.get(i).Status&2)) {
	            // Marked
	            if( last != i-1 && tl != 0) 
	            {
	                // Gap between this and last marked line
	            	EBuffer.SSBuffer.InsLine (tl++,0);
	            }
	            //EBuffer.SSBuffer.InsertLine (tl++,BitOps.strlen (Lines.get(i).Msg+2), Lines.get(i).Msg+2 );
	            EBuffer.SSBuffer.InsertLine( new EPoint( tl++, 0 ), Lines.get(i).Msg.length()-2, Lines.get(i).Msg.substring(2) );
	            last=i;
	        }
	    }
	    if (Config.SystemClipboard) ClipData.PutPMClip();
	    
	    return ExResult.ErOK;
	}

	// Event map - this name is used in config files when defining eventmap
	@Override
	EEventMap GetEventMap () {
	    return EEventMap.FindEventMap("CVSDIFF");
	}

	
	
}
