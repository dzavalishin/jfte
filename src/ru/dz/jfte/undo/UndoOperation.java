package ru.dz.jfte.undo;


/** 
 * Undo/Redo operation id.
 * 
 * <p>
 * 
 * @author dz
 * 
 * <p>
 * 
 * only core operations can be directly undone
 * - Insert # of Lines
 * - Delete # of Lines
 * - Insert # Line
 * - Delete Line Text
 * - Insert Line Text
 * - Positioning
 * - Block marking
 */

public enum UndoOperation 
{
	 
	ucInsLine          ,
	ucDelLine          ,
	ucInsChars         ,
	ucDelChars         ,

	ucJoinLine         ,
	ucSplitLine        ,

	ucPosition         ,
	ucBlock            ,
	ucModified         ,

	ucFoldCreate       ,
	ucFoldDestroy      ,
	ucFoldPromote      ,
	ucFoldDemote       ,
	ucFoldOpen         ,
	ucFoldClose        ,

	ucPlaceUserBookmark  ,
	ucRemoveUserBookmark ,
	
}
