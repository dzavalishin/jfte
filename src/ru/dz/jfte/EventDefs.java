package ru.dz.jfte;

public interface EventDefs {

	/* don't change these, used as index */
	static final int  DCH_C1 =0;
	static final int  DCH_C2 =1;
	static final int  DCH_C3 =2;
	static final int  DCH_C4 =3;
	static final int  DCH_H  =4;
	static final int  DCH_V  =5;
	static final int  DCH_M1 =6;
	static final int  DCH_M2 =7;
	static final int  DCH_M3 =8;
	static final int  DCH_M4 =9;
	static final int  DCH_X  =10;
	static final int  DCH_RPTR= 11;
	static final int  DCH_EOL =12;
	static final int  DCH_EOF =13;
	static final int  DCH_END =14;
	static final int  DCH_AUP =15;
	static final int  DCH_ADOWN =16;
	static final int  DCH_HFORE =17;
	static final int  DCH_HBACK =18;
	static final int  DCH_ALEFT =19;
	static final int  DCH_ARIGHT =20;

	static final int  ConMaxCols =256;
	static final int  ConMaxRows =128;

	static final int  csUp       =0;
	static final int  csDown     =1;
	static final int  csLeft     =2;
	static final int  csRight    =3;

	static final int  evNone      =       0;
	static final int  evKeyDown   =  0x0001;
	static final int  evKeyUp     =  0x0002;
	static final int  evMouseDown =  0x0010;
	static final int  evMouseUp   =  0x0020;
	static final int  evMouseMove =  0x0040;
	static final int  evMouseAuto =  0x0080;
	static final int  evCommand   =  0x0100;
	static final int  evBroadcast =  0x0200;
	static final int  evNotify    =  0x0400;

	static final int  evKeyboard  =  (evKeyDown | evKeyUp);
	static final int  evMouse     =  (evMouseDown | evMouseUp | evMouseMove | evMouseAuto);
	static final int  evMessage   =  (evCommand | evBroadcast);

	//#include "conkbd.h"

	static final int  cmRefresh   =1;
	static final int  cmResize    =2;
	static final int  cmClose     =3;
	static final int  cmPipeRead  =4;
	static final int  cmMainMenu  =5;
	static final int  cmPopupMenu =6;

	/* vertical scroll */
	    
	static final int  cmVScrollUp   =  10;
	static final int  cmVScrollDown =  11;
	static final int  cmVScrollPgUp =  12;
	static final int  cmVScrollPgDn =  13;
	static final int  cmVScrollMove =  14;
	    
	/* horizontal scroll */
	    
	static final int  cmHScrollLeft  = 15;
	static final int  cmHScrollRight = 16;
	static final int  cmHScrollPgLt  = 17;
	static final int  cmHScrollPgRt  = 18;
	static final int  cmHScrollMove  = 19;

	static final int  cmDroppedFile  = 30;
	static final int  cmRenameFile   = 31;   /* TODO: in-place editing of titlebar */
	
}
