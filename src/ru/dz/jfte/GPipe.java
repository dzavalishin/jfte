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

	EModel notify;
	
	public GPipe(int id, EModel m) {
		this.id = id;
		notify = m;
	}
	

	public boolean run(String command, String directory) {
		
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

		return true;
	}

	public TEvent checkPipe() {
		try {
			if( input.ready() || !p.isAlive() )
			{
				return TEvent.newNotifyPipeEvent(id,notify);
			}
		} catch (IOException e) {
			log.log(Level.SEVERE, "GPipe.checkPipe", e);
		}
		return null;
	}
	
	
	
	
	
	
	
	
	
	
	
	
	



	static final int MAX_PIPES = 4;
	//#define PIPE_BUFLEN 4096

	static GPipe [] Pipes = new GPipe[MAX_PIPES];


	/**
	 * Poor man's multithreading?
	 * 
	 * @return true if some pipe has data to read
	 */
	public static TEvent checkPipeData()
	{
		TEvent ret = null;
		for(GPipe p : Pipes)
			if(p != null && (ret = p.checkPipe()) != null)
				return ret;
		
		return null;
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

	static int SetPipeView(int id, EModel  notify)
	{
		if (id < 0 || id > MAX_PIPES)
			return -1;
		
		if (Pipes[id] == null)
			return -1;

		Pipes[id].notify = notify;
		return 0;
	}

	static String ReadPipe(int id)
	{
		int rc;

		if (id < 0 || id > MAX_PIPES)
			return null;
		
		if (Pipes[id] == null)
			return null;

		
		String s;
		try {
			s = Pipes[id].input.readLine();
		} 
		catch (IOException e) 
		{
			log.log(Level.SEVERE, "ReadPipe", e);			
			return null;
		}
		
		if (s == null) {
			Pipes[id].stopped = true;
		}

		return s;
	}

	static int ClosePipe(int id)
	{
		if (id < 0 || id > MAX_PIPES)
			return -1;
		if (Pipes[id] == null)
			return -1;
		
		int status = -1;
		try {
			Pipes[id].input.close();
			Pipes[id].p.destroy();
			status = Pipes[id].p.waitFor();
		} catch (InterruptedException | IOException e) {
			log.log(Level.SEVERE, "ClosePipe", e);
		}		
			
		Pipes[id] = null;
		return status;
	}

	
	
	
}