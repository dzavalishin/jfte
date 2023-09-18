package ru.dz.jfte;

import java.io.IOException;

public class ExKey extends ExView 
{
    String Prompt;
    int /*TKeyCode*/ Key;
    char ch;

    
    ExKey(String APrompt) {
            Prompt = APrompt;
    }


    //void Activate(boolean gotfocus) {        super.Activate(gotfocus);    }

    int BeginMacro() {
        return 1;
    }

    @Override
    void HandleEvent(TEvent Event) throws IOException {
        switch (Event.What) {
        case evKeyDown:
            Key = ((TKeyEvent)Event).Code;
            if( 0 == (Key & kfModifier)) // not ctrl,alt,shift, ....
                EndExec(1);
            Event.What = evNone;
            break;
        }
    }

    void UpdateView() {
        if (Next!=null) {
            Next.UpdateView();
        }
    }

    void RepaintView() {
        if (Next!=null) {
            Next.RepaintView();
        }
    }

    void UpdateStatus() {
        RepaintStatus();
    }

    void RepaintStatus() {
        TDrawBuffer B = new TDrawBuffer();
        int [] W = {0}, H = {0};
        
        ConQuerySize(W, H);
        
        B.MoveCh(' ', 0x17, W[0]);
        Console.ConPutBox(0, H[0] - 1, W[0], 1, B);
    }
    
}
