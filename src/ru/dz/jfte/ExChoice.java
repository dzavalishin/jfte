package ru.dz.jfte;

import java.io.IOException;

public class ExChoice extends ExView implements ColorDefs 
{
    String Title;
    String Prompt;
    int NOpt;
    String [] SOpt;
    
    int Cur;
    int lTitle;
    int lChoice;
    boolean MouseCaptured = false;

	
    
    ExChoice(String ATitle, int NSel, Object... choice) 
    {
    	String msg;
        int i;
        String fmt;
        
        Cur = 0;
        
        Title = ATitle;
        lTitle = Title.length();
        NOpt = NSel;
        lChoice = 0;
        
        SOpt = new String[NSel];
        
        for (i = 0; i < NSel; i++) {
            SOpt[i] = choice[i].toString();
            lChoice += PCell.CStrLen(SOpt[i]) + 1;
        }
        
        fmt = choice[i++].toString();
        int nPrintParms = choice.length - i;
        String [] pp = new String[nPrintParms];

        int ppi = 0;
        for (; i < choice.length; i++, ppi++ )
            pp[ppi] = choice[i].toString();
        
        Prompt = String.format(fmt, pp);
    }


    @Override
    void Activate(boolean gotfocus) {
        super.Activate(gotfocus);
    }

    int BeginMacro() {
        return 1;
    }

    int FindChoiceByPoint(int x, int y) {
        int pos, i;
        int [] W = {0}, H = {0};
        
        Win.ConQuerySize(W, H);
        
        if (y != H[0] - 1)
            return -1;
        
        pos = W[0] - lChoice;
        if (x < pos)
            return -1;
        
        for (i = 0; i < NOpt; i++) {
            int clen = PCell.CStrLen(SOpt[i]);
            
            if (x > pos && x <= pos + clen)
                return i;
            pos += clen + 1;
        }
        return -1;
    }

    @Override
    void HandleEvent(TEvent pEvent) throws IOException 
    {
        switch (pEvent.What) {
        case evKeyDown:
        {
        	TKeyEvent Event = (TKeyEvent) pEvent;
            switch (KeyDefs.kbCode(Event.Code)) {
            case kbTab | kfShift:
            case kbLeft: 
            	if (Cur == -1) 
            		Cur = 0; 
            	Cur--; 
            	if (Cur < 0) 
            		Cur = NOpt - 1; 
            	Event.What = evNone; 
            break;
            
            case kbTab:
            case kbRight: if (Cur == -1) Cur = 0; Cur++; if (Cur >= NOpt) Cur = 0; Event.What = evNone; break;
            case kbHome: Cur = 0; Event.What = evNone; break;
            case kbEnd: Cur = NOpt - 1; Event.What = evNone; break;
            case kbEnter: if (Cur >= 0 && NOpt > 0) EndExec(Cur); Event.What = evNone; break;
            case kbEsc: EndExec(-1); Event.What = evNone; break;
            default:
                if (KeyDefs.isAscii(Event.Code)) {
                    char c = (char)(Event.Code & 0xFF);
                    String s = "&" + (char)(Character.toUpperCase((char)c) & 0xFF);  
                    
                    
                    for (int i = 0; i < NOpt; i++) {
                        //if (strstr(SOpt[i], s) != 0)
                    	if( s.indexOf(SOpt[i]) >= 0)
                        {
                            Win.EndExec(i);
                            break;
                        }
                    }
                    Event.What = evNone;
                }
                break;
            }
            break;
        }
        case evMouseDown:
        case evMouseMove:
        case evMouseUp:
        	handleMouse((TMouseEvent) pEvent);
            break;
        }
    }

    private void handleMouse(TMouseEvent Event) throws IOException
    {
        switch (Event.What) {
        case evMouseDown:
            if (Win.CaptureMouse(true)!=0)
                MouseCaptured = true;
            else
                break;
            Cur = FindChoiceByPoint(Event.X, Event.Y);
            Event.What = evNone;
            break;
        case evMouseMove:
            if (MouseCaptured)
                Cur = FindChoiceByPoint(Event.X, Event.Y);
            Event.What = evNone;
            break;
        case evMouseUp:
            if (MouseCaptured)
                Win.CaptureMouse(false);
            else
                break;
            MouseCaptured = false;
            Cur = FindChoiceByPoint(Event.X, Event.Y);
            Event.What = evNone;
            if (Cur >= 0 && Cur < NOpt && NOpt > 0)
                EndExec(Cur); 
            else
                Cur = 0;
            break;
        }    	
    }
    
    @Override
    void UpdateView() {
        if (Next!=null) {
            Next.UpdateView();
        }
    }

    @Override
    void RepaintView() {
        if (Next!=null) {
            Next.RepaintView();
        }
    }

    @Override
    void UpdateStatus() {
        RepaintStatus();
    }

    @Override
    void RepaintStatus() {
        TDrawBuffer B = new TDrawBuffer();
        int [] W = {0}, H = {0};
        int pos, i;
        int /*TAttr*/ color1, color2;
        
        ConQuerySize(W, H);
        
        
        if (Cur != -1) {
            if (Cur >= NOpt) Cur = NOpt - 1;
            if (Cur < 0) Cur = 0;
        }
        
        B.MoveCh( ' ', hcChoice_Background, W[0]);
        B.MoveStr( 0, W[0], Title, hcChoice_Title, W[0]);
        B.MoveChar( lTitle, W[0], ':', hcChoice_Background, 1);
        B.MoveStr( lTitle + 2, W[0], Prompt, hcChoice_Param, W[0]);
        
        pos = W[0] - lChoice;
        for (i = 0; i < NOpt; i++) {
            if (i == Cur) {
                color1 = hcChoice_ActiveItem;
                color2 = hcChoice_ActiveChar;
            } else {
                color1 = hcChoice_NormalItem;
                color2 = hcChoice_NormalChar;
            }
            if (i == Cur)
                ConSetCursorPos(pos + 1, H[0] - 1);
            B.MoveChar( pos, W[0], Console.ConGetDrawChar(DCH_V), hcChoice_Background, 1);
            B.MoveCStr( pos + 1, W[0], SOpt[i], color1, color2, W[0]);
            pos += PCell.CStrLen(SOpt[i]) + 1;
        }
        ConPutBox(0, H[0] - 1, W[0], 1, B);
    }
    
}
