package ru.dz.jfte.undo;

import java.util.Iterator;

import ru.dz.jfte.EBuffer;
import ru.dz.jfte.EPoint;

/**
 * 
 * Undo/Redo engine facade.
 * 
 * @author dz
 *
 */

public class UndoRedoController 
{
	/**
	 * We are in undo processing, record operations to Redo stack, play from Undo.
	 */
	private boolean inUndo = false;

	/**
	 * We are in redo processing, record operations to Undo stack, play from Redo.
	 */
	private boolean inRedo = false;

	/**
	 * Contains stored in reverse order undo operations
	 */
	private UndoRedoStack undoStack = new UndoRedoStack();

	/**
	 * Contains stored in reverse order redo operations
	 */
	private UndoRedoStack redoStack = new UndoRedoStack();

	private EBuffer b;


	//--------------------------------------------------------------------
	// Constructor
	//--------------------------------------------------------------------

	public UndoRedoController(EBuffer b) {
		this.b = b;
	}

	//--------------------------------------------------------------------
	// Interface
	//--------------------------------------------------------------------

	/**
	 * Main entry point for redo action recording operation.
	 * <p>
	 * Get root undo/redo record used to store objects related to one undo/redo operation.
	 * <p>
	 * Usage: 
	 * {@code
	 * getRecordSlot().
	 *   pushInt(x).
	 *   pushInt(y).
	 *   pushOp(ucPosition);
	 * }
	 * <p>
	 * As a result new undo (or redo if we are undoing now) record is stored to be
	 * played back later.
	 * 
	 * @return object to record undo/redo operation for editor action
	 */
	public UndoStackItem getRecordSlot()
	{
		UndoStackItem ret = new UndoStackItem();

		// If we are undoing - record actions to redo stack.
		// Else - on undo.

		if(inUndo)
			redoStack.push(ret);
		else
			undoStack.push(ret);

		// Allways save current position - will be played after user op
		ret.pushPosition(b.CP);

		return ret;
	}

	public boolean undo()
	{
		assert(!inRedo);

		if(!CanUndo()) return false;

		inUndo = true;
		doUndo();
		inUndo = false;

		return true;
	}

	public boolean redo()
	{
		assert(!inUndo);

		if(!CanRedo()) return false;

		inRedo = true;
		doRedo();
		inRedo = false;

		return true;
	}

	public boolean CanUndo() {
		return !undoStack.isEmpty();
	}

	public boolean CanRedo() {
		return !redoStack.isEmpty();
	}

	//--------------------------------------------------------------------
	// Implementation
	//--------------------------------------------------------------------

	private void doUndo() {
		UndoStackItem item = undoStack.pop();
		playBack( item.iterator() );
	}


	private void doRedo() {
		UndoStackItem item = redoStack.pop();
		playBack( item.iterator() );
	}


	/**
	 * Play back editor actions stored in item
	 * 
	 * @param b 
	 * @param iterator
	 */
	private boolean playBack(Iterator<Object> i) 
	{
		assert(i.hasNext());

		while(i.hasNext())
		{
			UndoOperation op = (UndoOperation) i.next();

			switch(op)
			{
			case ucInsLine:
			{
				int Line = (Integer) i.next(); 
				//	            printf("\tDelLine %d\n", Line);
				if (!b.DelLine((int) Line)) return false;
			}
			break;

			case ucDelLine:
			{
				int line = (Integer) i.next(); 
				String data = (String)  i.next();

				// printf("\tInsLine %d\n", Line);
				if (!b.InsLine(line, 0)) return false;
				// printf("\tInsText %d - %d\n", Line, Len);
				if (!b.InsText(line, 0, data.length(), data)) return false;
			}
			break;

			case ucInsChars:
			{
				int count = (Integer) i.next(); 
				int col   = (Integer) i.next(); 
				int line  = (Integer) i.next(); 
				//	            printf("\tDelChars %d %d %d\n", Line, Col, ACount);
				if (!b.DelChars(line, col, count)) return false;
			}
			break;

			case ucDelChars:
			{
				int line = (Integer) i.next(); 
				int col = (Integer) i.next(); 
				String data = (String)  i.next();
				//	            printf("\tInsChars %d %d %d\n", Line, Col, ACount);
				if (!b.InsChars(line, col, data.length(),  data)) return false;
				//Pos -= ACount;
			}
			break;

			case ucPosition:
			{
				int line = (Integer) i.next(); 
				int col = (Integer) i.next(); 
				//	            printf("\tSetPos %d %d\n", Line, Col);
				if(!b.SetPos(col, line)) return false;
			}
			break;

			case ucBlock: 
			{
				int l, r, c;

				//	                printf("\tBlock\n");
				l = (Integer) i.next(); 

				if (b.BlockMode != l) b.BlockRedraw();
				b.BlockMode = l;

				r = (Integer) i.next();
				c = (Integer) i.next();
				if (!b.SetBE(new EPoint(r,c))) return false;

				r = (Integer) i.next();
				c = (Integer) i.next();
				if (!b.SetBB(new EPoint(r,c))) return false;
			}
			break;


			case ucFoldCreate:
				// puts("ucFoldCreate");
			{
				int line = (Integer) i.next(); 
				if (!b.FoldDestroy(line)) return false;
			}
			break;

			case ucFoldDestroy:
				// puts("ucFoldDestroy");
			{
				int Line = (Integer) i.next(); 
				int level = (Integer) i.next(); 
				if (!b.FoldCreate(Line)) return false;

				int ff = b.FindFold(Line);
				assert(ff != -1);
				b.FF[ff].level = level;
			}
			break;
			case ucFoldPromote:
				// puts("ucFoldPromote");
			{
				int Line = (Integer) i.next(); 
				if (!b.FoldDemote(Line)) return false;
			}
			break;

			case ucFoldDemote:
				// puts("ucFoldDemote");
			{
				int Line = (Integer) i.next(); 
				if (!b.FoldPromote(Line)) return false;
			}
			break;

			case ucFoldOpen:
				// puts("ucFoldOpen");
			{
				int Line = (Integer) i.next(); 
				if (!b.FoldClose(Line)) return false;
			}
			break;

			case ucFoldClose:
				// puts("ucFoldClose");
			{
				int Line = (Integer) i.next(); 
				if (!b.FoldOpen(Line)) return false;
			}
			break;

			case ucModified:
				//	            printf("\tModified\n");
				b.Modified = 0;
				break;

			case ucPlaceUserBookmark:
			{
				//puts ("ucPlaceUserBookmark");

				String data = (String)  i.next();
				//Pos -= ACount;
				int col = (Integer) i.next(); 
				int line = (Integer) i.next(); 
				if(col == -1 || line == -1) {
					if (!b.RemoveUserBookmark (data)) return false;
				} else {
					if (!b.PlaceUserBookmark (data, new EPoint(line,col))) return false;
				}
			}
			break;

			case ucRemoveUserBookmark:
			{
				//puts("ucRemoveUserBookmark");

				String data = (String)  i.next();
				//Pos -= ACount;
				int col = (Integer) i.next(); 
				int line = (Integer) i.next(); 
				if(!b.PlaceUserBookmark (data, new EPoint(line,col))) return false;
			}
			break;

			default:
				assert(null == "Oops: invalid undo command.\n");
			}
		}

		return true;
	}


}
