package ru.dz.jfte.undo;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import ru.dz.jfte.EPoint;
import ru.dz.jfte.c.BinaryString;

/**
 * 
 * Item of Undo/Redo stack. Represents one (possible) undo/redo op - 
 * record of editor (reverse) action to play later.
 * 
 * @author dz
 *
 */

public class UndoStackItem implements UndoDefs
{
	private boolean disabled = false;
    private List<Object> objects = new ArrayList<>(32); 

	//--------------------------------------------------------------------
	// Read
	//--------------------------------------------------------------------
    
    public Object get(int index) { return objects.get(index); }
    
	Iterator<Object> iterator() 
	{
		return objects.iterator(); 
	}
    
	//--------------------------------------------------------------------
	// Write
	//--------------------------------------------------------------------
    
    private void add(Object o) {
    	if(disabled) return;
    	objects.add(0, o); 
    	}


    
    public UndoStackItem pushOp( UndoOperation op )
    {
	    add(op);
	    return this;
	}
    
    /*
    public UndoStackItem pushLong(long l) 
    {
	    add((Long)l);
	    return this;
	}*/

    public UndoStackItem pushChar(char ch) 
	{
	    add((Character)ch);
	    return this;
	}

    public UndoStackItem pushInt(int i) 
	{
	    add((Integer)i);
	    return this;
	}
	
    public UndoStackItem pushData(Object data) 
	{ 
		add(data); 
		return this; 
	}

    
    
    //public UndoStackItem pushString(String s) {
    public UndoStackItem pushString(CharSequence s) {
		pushData( s.toString() );
		return this;
	}

    public UndoStackItem pushString(CharSequence s, int count) {
		pushData( s.toString().substring(0, count) );
		return this;
	}

    public UndoStackItem pushString(CharSequence s, int start, int count) {
		pushData( s.toString().substring(start, start+count) );
		return this;
	}

    

	public UndoStackItem pushString(BinaryString s) {
		pushData( s.toString() );
		return this;
	}
    
	public UndoStackItem pushString(BinaryString s, int start, int count) {
		pushData( s.substring(start, start+count) );
		return this;
	}


	//--------------------------------------------------------------------
	// Helpers for some operations
	//--------------------------------------------------------------------
    
    
	public void pushModified() {
		pushOp(UndoOperation.ucModified);
	}
	
	public void pushPosition(EPoint CP) {
		pushInt(CP.getCol());
		pushInt(CP.getRow());
		pushOp(UndoOperation.ucPosition);
		
	}

	//--------------------------------------------------------------------
	// Turn on/off
	//--------------------------------------------------------------------
	
	/**
	 * @return true if disabled
	 */
	public boolean isDisabled() { return disabled; }

	/**
	 * Disable me
	 */
	public void setDisabled() { disabled = true; }
	
}

