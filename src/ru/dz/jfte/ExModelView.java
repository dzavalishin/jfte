package ru.dz.jfte;

public class ExModelView extends ExView 
{
    EView View;
    boolean MouseCaptured = false;
    boolean MouseMoved = false;


    
    ExModelView(EView AView) {
        View = AView;
        View.MView = this;
    }

    /* ~ExModelView() {
        if (View) { // close it
            delete View;
            View = 0;
        }
    } */

    int GetContext() {
        return View.GetContext();
    }

    void Activate(int gotfocus) {
        super.Activate(gotfocus);
        View.Activate(gotfocus);
    }

    EEventMap GetEventMap() {
        return View.GetEventMap();
    }

    ExResult ExecCommand(ExCommands Command, ExState State) {
        return View.ExecCommand(Command, State);
    }

    int BeginMacro() {
        return View.BeginMacro();
    }

    void HandleEvent(TEvent Event) {
        super.HandleEvent(Event);
        View.HandleEvent(Event);
    }

    void UpdateView() {
        View.UpdateView();
    }

    void RepaintView() {
        View.RepaintView();
    }

    void RepaintStatus() {
        View.RepaintStatus();
    }

    
    
}
