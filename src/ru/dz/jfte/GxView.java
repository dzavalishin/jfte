package ru.dz.jfte;

import java.io.Closeable;
import java.io.IOException;

public class GxView extends GView implements Closeable, EventDefs, KeyDefs, ModeDefs 
{
    ExView Top;
    ExView Bottom;
    boolean MouseCaptured = false;
    
    ExView GetStatusContext() { if (Top != null) return Top.GetStatusContext(); else return null; }
    ExView GetViewContext() { if (Top != null) return Top.GetViewContext(); else return null; }
    
    int IsModelView() { return (Top != null) ? Top.IsModelView() : 0; }

    
    GxView(GFrame Parent) {
    	super(Parent, -1, -1); 
    
        Top = Bottom = null;
    }

    public void close() throws IOException {
        if (Top != null) {
            ExView V;

            while (Top != null) {
                V = Top;
                Top = Top.Next;
                V.Win = null;
                V.close();
            }
        }
    }

    void PushView(ExView view) {
        int [] W = new int[1], H = new int[1];
        ConQuerySize(W, H);

        view.Win = this;
        if (Top == null) {
            Top = Bottom = view;
            view.Next = null;
        } else {
            Top.Activate(0);
            view.Next = Top;
            Top = view;
            Top.Activate(1);
        }
        Top.Resize(W[0], H[0]);
    }

    ExView PopView() {
        ExView V;

        if (Top == null)
            return null;

        Top.Activate(0);

        V = Top;
        Top = Top.Next;

        if (Top == null)
            Bottom = null;
        else {
            Top.Activate(1);
            Top.Repaint();
        }
        V.Win = null;

        return V;
    }

    void NewView(ExView  view) {
    }

    EEventMap GetEventMap() {
        if (Top != null)
            return Top.GetEventMap();
        return null;
    }

    ExResult ExecCommand(int Command, ExState State) {
        if (Top != null)
            return Top.ExecCommand(Command, State);
        return ExResult.ErFAIL;
    }

    int BeginMacro() {
        if (Top != null)
            return Top.BeginMacro();
        return 1;
    }

    int GetContext() {
        if (Top != null)
            return Top.GetContext();
        else
            return CONTEXT_NONE;
    }

    void HandleEvent(TEvent pEvent) {
        super.HandleEvent(pEvent);
        Top.HandleEvent(pEvent);

        if( 0 != (pEvent.What & evMouse)) 
        {
        	TMouseEvent Event = (TMouseEvent) pEvent;
            int [] W = {0}, H = {0};

            ConQuerySize(W, H);

            if (Event.What != evMouseDown || Event.Y == H[0] - 1) {
                switch (Event.What) {
                case evMouseDown:
                    if (CaptureMouse(1)!=0)
                        MouseCaptured = true;
                    else
                        break;
                    Event.What = evNone;
                    break;
                case evMouseMove:
                    if (MouseCaptured) {
                        if (Event.Y != H[0] - 1)
                            ExpandHeight(Event.Y - H[0] + 1);
                        Event.What = evNone;
                    }
                    break;
                case evMouseAuto:
                    if (MouseCaptured)
                        Event.What = evNone;
                    break;
                case evMouseUp:
                    if (MouseCaptured)
                        CaptureMouse(0);
                    else
                        break;
                    MouseCaptured = false;
                    Event.What = evNone;
                    break;
                }
                return ;
            }
        }
    }

    void Update() {
        if (Top != null) {
            Top.Update();
        }// else
           // Repaint();
    }

    void Repaint() {
        if (Top != null) {
            Top.Repaint();
        } else {
            TDrawBuffer B;
            int [] X, Y;

            ConQuerySize(X, Y);
            MoveCh(B, ' ', 0x07, X);
            ConPutLine(0, 0, X[0], Y[0], B);
        }
    }

    void Resize(int width, int height) {
        ExView V;
        super.Resize(width, height);
        V = Top;

        while (V != null) {
            V.Resize(width, height);
            V = V.Next;
        }
    }

    void Activate(int gotfocus) {
        if (Top != null)
            Top.Activate(gotfocus);
        super.Activate(gotfocus);
    }

    void UpdateTitle(String Title, String STitle) {
        if (Parent != null && Parent.Active == this) {
            Parent.ConSetTitle(Title, STitle);
        }
    }

    int GetStr(String Prompt, String []Str, int HistId) {
        if ((GFrame.HaveGUIDialogs & GUIDLG_PROMPT) && Config.GUIDialogs) {
            return DLGGetStr(this, Prompt, Str, HistId, 0);
        } else {
            return ReadStr(Prompt, Str, null, 1, HistId);
        }
    }

    int GetFile(String Prompt, String []Str, int HistId, int Flags) {
        if ((GFrame.HaveGUIDialogs & GUIDLG_FILE) && Config.GUIDialogs)
            return DLGGetFile(this, Prompt, BufLen, Str, Flags);
        else
            return ReadStr(Prompt, Str, CompletePath, SelectPathname, HistId);
    }

    int ReadStr(String Prompt, String []Str, Completer Comp, int Select, int HistId) throws IOException {
        int rc;
        ExInput input;

        input = new ExInput(Prompt, Str, Comp, Select, HistId);
        if (input == null)
            return 0;

        PushView((ExView )input);

        rc = Execute();

        PopView();

        Repaint();

        if (rc == 1) {
            Str[0] = input.Line;
        }
        input.close();

        return rc;
    }

    int Choice(long Flags, String Title, int NSel, Object ... choices /*, format, args */) {
        int rc;

        if ((GFrame.HaveGUIDialogs & GUIDLG_CHOICE) && Config.GUIDialogs) {
            rc = DLGPickChoice(this, Title, NSel, choices, Flags);
            return rc;
        } else {
            ExChoice choice = new ExChoice(Title, NSel, choices);

            PushView(choice);
            rc = Execute();
            PopView();
            Repaint();

            choice.close();

            return rc;
        }
    }

    long /*TKeyCode*/ GetChar(String Prompt) {
        int rc;
        long /*TKeyCode*/K = 0;

        ExKey key = new ExKey(Prompt);
        if (key == null)
            return 0;

        PushView(key);
        rc = Execute();
        PopView();
        Repaint();

        if (rc == 1)
            K = key.Key;
        //delete key;

        return K;
    }

    int IncrementalSearch(EView View) {
        int rc;
        ExISearch search;
        EBuffer B = null;

        if (View.GetContext() != CONTEXT_FILE)
            return 0;

        B = (EBuffer )View.Model;

        search = new ExISearch(B);

        if (search == 0)
            return 0;

        PushView(search);
        rc = Execute();
        PopView();
        Repaint();

        search.close();

        return rc;
    }

    int PickASCII() {
        int rc;
        ExASCII ascii;

        ascii = new ExASCII();

        PushView(ascii);
        rc = Execute();
        PopView();
        Repaint();

        ascii.close();
        return rc;
    }

    int ICompleteWord(EView View) {
        int rc = 0;

        if (View.GetContext() == CONTEXT_FILE) {
            ExComplete c = new ExComplete((EBuffer )View.Model);
            if (c != null) {
                PushView(c);
                rc = Execute();
                PopView();
                Repaint();
                c.close();
            }
        }
        return rc;
    }
    
}
