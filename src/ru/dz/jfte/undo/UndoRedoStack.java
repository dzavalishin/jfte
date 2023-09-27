package ru.dz.jfte.undo;


import java.util.ArrayList;
import java.util.List;

/**
 * 
 * <P>
 * Basically UNDO (or REDO) stack is a list of {@code  UndoStackItem } 
 * which is a list of (int/long/char/other) objects used by undo/redo code for
 * each operation replay.
 * 
 * @author dz
 *
 */

public class UndoRedoStack implements UndoDefs 
{
    private List<UndoStackItem> data = new ArrayList<>(32); 

    private static final int MAX_SIZE = 200;
    
    int getNum() { return data.size(); }
    
    boolean isEmpty() { return data.isEmpty(); }


	public UndoStackItem pop() 
	{
		return data.remove(0);
	}
    
	public void push( UndoStackItem item )
	{
		// Keep size limited
		if(data.size() >= MAX_SIZE)
			data.remove(data.size()-1);
		
		data.add(0, item);
	}
	
}






