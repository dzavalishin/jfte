package ru.dz.jfte;

import java.io.IOException;

public class ExModelView extends ExView 
{
    protected EView View;
    protected boolean MouseCaptured = false;
    protected boolean MouseMoved = false;

    @Override
    boolean IsModelView() { return true; }

    
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

    @Override
    int GetContext() {
        return View.GetContext();
    }

    @Override
    void Activate(boolean gotfocus) {
        super.Activate(gotfocus);
        View.Activate(gotfocus);
    }

    @Override
    EEventMap GetEventMap() {
        return View.GetEventMap();
    }

    @Override
    ExResult ExecCommand(ExCommands Command, ExState State) {
        return View.ExecCommand(Command, State);
    }

    @Override
    int BeginMacro() {
        return View.BeginMacro();
    }

    @Override
    void HandleEvent(TEvent Event) throws IOException {
        super.HandleEvent(Event);
        View.HandleEvent(Event);
    }

    @Override
    void UpdateView() {
        View.UpdateView();
    }

    @Override
    void RepaintView() {
        View.RepaintView();
    }

    @Override
    void RepaintStatus() {
        View.RepaintStatus();
    }

    @Override
    void UpdateStatus() {
        View.UpdateStatus();
    }

    @Override
    void Resize(int width, int height) {
        View.Resize(width, height);
    }

    @Override
    void WnSwitchBuffer(EModel B) {
       if (View!=null)
           View.SwitchToModel(B);
    }
    
    
}
