package ru.dz.jfte;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.logging.Level;
import java.util.logging.Logger;

public class GPipe {
	private static final Logger log = Logger.getLogger(GPipe.class.getName());

	int id;

	boolean stopped = false;
	Process p;
	BufferedReader input;
	BufferedReader err;

	EModel notify;

	
	
	static final int MAX_PIPES = 4;
	static GPipe [] Pipes = new GPipe[MAX_PIPES];
	
	
	public GPipe(int id, EModel m) {
		this.id = id;
		notify = m;
	}
	

	public boolean run(String command, String directory) 
	{	
		command = command.trim(); // TODO remove me as cmd line editor fixed not to add spaces
		
		ProcessBuilder pb = new ProcessBuilder(command); 

		pb.directory(new File(directory));
		
	    try {
			p = pb.start();
		} catch (IOException e) {
			log.log(Level.SEVERE, "GPipe.run("+command+")", e);
			return false;
		}	    
	    
		input = new BufferedReader(new 
						InputStreamReader(p.getInputStream()));

		err = new BufferedReader(new 
				InputStreamReader(p.getErrorStream()));
		
		return true;
	}

	public TEvent checkPipe() {
		try {
			if( input.ready() || err.ready() || !p.isAlive() )
			{
				return TEvent.newNotifyPipeEvent(id,notify);
			}
		} catch (IOException e) {
			log.log(Level.SEVERE, "GPipe.checkPipe", e);
		}
		return null;
	}
	
	
	/*static int SetPipeView(int id, EModel  notify)
	{
		if (id < 0 || id > MAX_PIPES)
			return -1;
		
		if (Pipes[id] == null)
			return -1;

		Pipes[id].notify = notify;
		return 0;
	}*/

	
	public String ReadPipe()
	{
		String s = null;
		
		try {
			if(err.ready())
				s = err.readLine();
			else			
				s = input.readLine();
		} 
		catch (IOException e) 
		{
			log.log(Level.SEVERE, "ReadPipe", e);			
			stopped = true;
			return null;
		}
		
		if (s == null)
			stopped = true;

		return s;
	}
	

	public int ClosePipe()
	{
		int status = -1;
		try {
			input.close();
			err.close();
			p.destroy();
			status = p.waitFor();
		} catch (InterruptedException | IOException e) {
			log.log(Level.SEVERE, "ClosePipe", e);
		}		
			
		return status;
	}
	


	
	// -------------------------------------------------------------------
	// Static
	// -------------------------------------------------------------------

	public static String ReadPipe(int id)
	{
		if (id < 0 || id > MAX_PIPES)
			return null;
		
		if (Pipes[id] == null)
			return null;

		return Pipes[id].ReadPipe();
	}
	

	static int ClosePipe(int id)
	{
		if (id < 0 || id > MAX_PIPES)
			return -1;
		if (Pipes[id] == null)
			return -1;

		int status = Pipes[id].ClosePipe();
		/*
		int status = -1;
		try {
			Pipes[id].input.close();
			Pipes[id].p.destroy();
			status = Pipes[id].p.waitFor();
		} catch (InterruptedException | IOException e) {
			log.log(Level.SEVERE, "ClosePipe", e);
		}		
		*/
		Pipes[id] = null;
		return status;
	}

	static int OpenPipe(String Command, String directory, EModel  notify)
	{
		int i;

		for (i = 0; i < MAX_PIPES; i++) {
			if (Pipes[i] == null) {			
				Pipes[i] = new GPipe(i, notify);
				if( !Pipes[i].run(Command, directory) )
					return -1;
				return i;
			}
		}
		return -1;
	}




	/**
	 * Poor man's multithreading?
	 * 
	 * @return event to dispatch if some pipe has data to read or null
	 */
	public static TEvent checkPipeData()
	{
		TEvent ret = null;
		for(GPipe p : Pipes)
			if(p != null && (ret = p.checkPipe()) != null)
				return ret;
		
		return null;
	}
	
	
}