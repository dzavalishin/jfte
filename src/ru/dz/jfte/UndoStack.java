package ru.dz.jfte;

import java.util.ArrayList;
import java.util.List;

import ru.dz.jfte.c.BinaryString;

/**
 * 
 * Redesigned completely, differs from C code a lot.
 * <P>
 * Basically UNDO stack is a list of UndoStackItem
 * which is a list of (int/long/char) objects used by undo/redo code for
 * each operation.
 * 
 * @author dz
 *
 */

public class UndoStack implements UndoDefs 
{
    //int NextCmd = 1;
    boolean Record = true; // is 'recording' of actions is enabled. Turned off while redo active to not to save it's operations again
    boolean Undo = false; // We're in progress of undo operation
    int UndoPtr = 0;
    //int getNum() = 0; Number of UndoStackItem in undo list. == 
    
    //Object [] Data = null;  
    //int *Top;
    List<UndoStackItem> data = new ArrayList<>(32); 

    int getNum() { return data.size(); }
    
    
	boolean CanUndo() {
	    if (getNum() == 0 || UndoPtr == 0) return false;
	    return true;
	}

	boolean CanRedo() {
	    if (getNum() == 0 || UndoPtr == getNum()) return false;
	    return true;
	}


	/**
	 * 
	 * @param no UndoStackItem position in undo stack
	 * @return number of objects in given UndoStackItem - used as start position to pop them 
	 */
	public int getStartPos(int no) 
	{
	    UndoStackItem op = data.get(no);
		return op.objects.size();
	}
    
}



class UndoStackItem implements UndoDefs
{
	//int cmd; // action type
	private boolean disabled = false;

    List<Object> objects = new ArrayList<>(32); 

    void add(Object o) {
    	if(disabled) return;
    	objects.add(o); 
    	}

    Object get(int index) { return objects.get(index); }

    UndoStackItem PushULong(long l) 
    {
	    add((Long)l);
	    return this;
	}

	UndoStackItem PushUChar(char ch) 
	{
	    add((Character)ch);
	    return this;
	}

	UndoStackItem PushUChar(int ch) 
	{
	    add((Character)(char)ch);
	    return this;
	}
	
	UndoStackItem PushUData(Object data) { add(data); return this; }
	
	public void pushModified() {
		PushUChar(ucModified);
	}
	public void pushPosition(EPoint CP) {
		PushULong(CP.Col);
		PushULong(CP.Row);
		PushUChar(ucPosition);
		
	}
	public UndoStackItem pushString(String s, int aCount) {
		PushUData( s.substring(0, aCount) );
		return this;
	}
	public UndoStackItem pushString(BinaryString s, int ofs, int aCount) {

		PushUData( s.substring(ofs, aCount) );

		
		return this;
	}

	/**
	 * @return the disabled
	 */
	public boolean isDisabled() {		return disabled;	}

	/**
	 * @param disabled the disabled to set
	 */
	public void setDisabled(boolean disabled) {		this.disabled = disabled;	}
	
	
}




