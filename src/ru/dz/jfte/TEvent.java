package ru.dz.jfte;

public abstract class TEvent implements EventDefs, KeyDefs 
{
	int /*TEventMask*/  What = evNone;
	GView View = null;

	abstract public TEvent clone();
	
	public static TEvent newKeyDownEvent(int code)
	{
		return new TKeyEvent(evKeyDown, code);
	}

	public static TEvent newKeyUpEvent(int code)
	{
		return new TKeyEvent(evKeyUp, code);
	}

	public static TEvent newMouseEvent(int what, int x, int y, int buttons, int count) {
		TMouseEvent me = new TMouseEvent(what);
		me.X = x;
		me.Y = y;
		me.Buttons = buttons;
		me.Count = count;
		return me;
	}

}

class TEmptyEvent extends TEvent {

	TEmptyEvent()
	{
	}

	public TEmptyEvent clone()
	{
		TEmptyEvent e = new TEmptyEvent();
		e.What = What;
		e.View = View;
		return e;
	}
}


class TKeyEvent extends TEvent 
{
	int /*TKeyCode*/  Code;

	public TKeyEvent(int what) {
		What = what;
	}

	public TKeyEvent(int what, int ch) {
		What = what;
		Code = ch;
	}

	public TKeyEvent clone()
	{
		TKeyEvent e = new TKeyEvent(What);

		e.View = View;
		e.Code = Code;

		return e;
	}

	public char GetChar() {
		char[] Ch = {0};
		GetChar(Ch);
		return Ch[0];
	}
	
	public boolean GetChar(char[] Ch) {
	    Ch[0] = 0;
	    
	    if(0!= (Code & kfModifier))
	        return false;
	    
	    if (KeyDefs.kbCode(Code) == kbEsc) { Ch[0] = 27; return true; }
	    if (KeyDefs.kbCode(Code) == kbEnter) { Ch[0] = 13; return true; }
	    if (KeyDefs.kbCode(Code) == (kbEnter | kfCtrl)) { Ch[0] = 10; return true; }
	    if (KeyDefs.kbCode(Code) == kbBackSp) { Ch[0] = 8; return true; }
	    if (KeyDefs.kbCode(Code) == (kbBackSp | kfCtrl)) { Ch[0] = 127; return true; }
	    if (KeyDefs.kbCode(Code) == kbTab) { Ch[0] = 9; return true; }
	    if (KeyDefs.kbCode(Code) == kbDel) { Ch[0] = 127; return true; }
	    
	    if (KeyDefs.keyType(Code) == kfCtrl) {
	        Ch[0] = (char) (Code & 0x1F);
	        return true;
	    }
	    if (KeyDefs.isAscii(Code)) {
	        Ch[0] = (char)Code;
	        return true;
	    }
	    
	    return false;
	}
	
}

class TMouseEvent extends TEvent{
	int X;
	int Y;
	int Buttons;
	int Count;
	long /*TKeyCode*/  KeyMask;
	
	public TMouseEvent(int what) {
		What = what;
	}

	public TMouseEvent clone()
	{
		TMouseEvent e = new TMouseEvent(What);

		e.View = View;
		e.X = X;
		e.Y = Y;
		e.Buttons = Buttons;
		e.Count = Count;

		return e;
	}
	
}

class TMsgEvent extends TEvent
{
	EModel Model = null;
	int /*TCommand*/  Command = 0;
	long Param1 = 0;
	Object Param2 = null;


	public TMsgEvent(int w) {
		What = w;
	}

	
	public TMsgEvent(int w, int c, int p1) {
		What = w;
		Command = c;
		Param1 = p1;
	}

	/**
	 * 
	 * @param w What
	 * @param v View
	 * @param cmd Command
	 */
	public TMsgEvent(int w, GView v, int cmd) {
		What = w;
		View = v;
		Command = cmd;
	}
	


	/**
	 * 
	 * @param w What
	 * @param view View
	 * @param cmd Command
	 * @param param1 Param1
	 */
	public TMsgEvent(int w, GView view, int cmd, int param1) {
		What = w;
		View = view;
		Command = cmd;
		Param1 = param1;
	}

	public TMsgEvent clone()
	{
		TMsgEvent e = new TMsgEvent(What);

		e.View = View;
		e.Model = Model;
		e.Command = Command;
		e.Param1 = Param1;
		e.Param2 = Param2;

		return e;
	}
}
