package ru.dz.jfte;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

class GPipe {
	//boolean used = false;
	int id;
	//int fd;
	//int pid;
	boolean stopped = false;
	Process p;
	BufferedReader input;

	EModel notify;

	public boolean run(String command) {
		
		ProcessBuilder pb = new ProcessBuilder(command); 

	    try {
			p = pb.start();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return false;
		}

		input = new BufferedReader(new 
						InputStreamReader(p.getInputStream()));
		/*		
        String line; 
        while ((line = input.readLine()) != null) { 
            System.out.println(line); 
        } 
    } 
} catch (IOException e) { 
    e.printStackTrace(); 
} */

		return true;
	}

	public TEvent checkPipe() {
		try {
			if( input.ready() )
			{
				return TEvent.newNotifyPipeEvent(id,notify);
			}
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return null;
	}
}