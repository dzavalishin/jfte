package ru.dz.jfte;

public abstract class TEvent implements EventDefs, KeyDefs 
{
	int /*TEventMask*/  What = evNone;
	GView View = null;

	abstract public TEvent clone();

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

	public TKeyEvent clone()
	{
		TKeyEvent e = new TKeyEvent(What);

		e.View = View;
		e.Code = Code;

		return e;
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

	public TMsgEvent(int w, int c, int p1) {
		What = w;
		Command = c;
		Param1 = p1;
	}

	public TMsgEvent(int w, GView v, int cmd) {
		What = w;
		View = v;
		Command = cmd;
	}
	
	public TMsgEvent(int w) {
		What = w;
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
