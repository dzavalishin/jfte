package ru.dz.jfte;

public interface UndoDefs {
	/*
	 * only core operations can be directly undone
	 * - Insert # of Lines
	 * - Delete # of Lines
	 * - Insert # Line
	 * - Delete Line Text
	 * - Insert Line Text
	 * - Positioning
	 * - Block marking
	 */
	 
	static final int ucInsLine          =1;
	static final int ucDelLine          =2;
	static final int ucInsChars         =3;
	static final int ucDelChars         =4;

	static final int ucJoinLine         =5;
	static final int ucSplitLine        =6;

	static final int ucPosition         =7;
	static final int ucBlock            =8;
	static final int ucModified         =9;

	static final int ucFoldCreate       =11;
	static final int ucFoldDestroy      =12;
	static final int ucFoldPromote      =13;
	static final int ucFoldDemote       =14;
	static final int ucFoldOpen         =15;
	static final int ucFoldClose        =16;

	static final int ucPlaceUserBookmark  =17;
	static final int ucRemoveUserBookmark =18;

}
