package ru.dz.jfte;

public class RoutineView extends EList implements GuiDefs, ModeDefs, EventDefs, ColorDefs
{
    EBuffer Buffer;

    RoutineView(int createFlags, EModel []ARoot, EBuffer AB) 
    {
    	super(createFlags, ARoot, "Routines");
    	
        Buffer = AB;
        if (Buffer.rlst.lines.isEmpty())
            Buffer.ScanForRoutines();
        Row = 0;
        int Row = Buffer.VToR(Buffer.CP.Row);
        for (int i = Buffer.rlst.lines.size() - 1; i >= 0; --i)
            if (Row >= Buffer.rlst.lines.get(i).line) {
                Row = i;
                break;
            }
        {
            SetTitle(String.format("Routines %s: %d",
                    Buffer.FileName,
                    Buffer.rlst.lines.size()));
        }
    };


    @Override
    EEventMap GetEventMap() {
        return EEventMap.FindEventMap("ROUTINES");
    }

    @Override
    ExResult ExecCommand(ExCommands Command, ExState State) {
        switch (Command) {
        case ExRescan:
            Buffer.ScanForRoutines();
            UpdateList();
            return ExResult.ErOK;

        case ExActivateInOtherWindow:
            if (Row < Buffer.rlst.Count()) {
                View.Next.SwitchToModel(Buffer);
                Buffer.CenterPosR(0, Buffer.rlst.lines.get(Row).line, 0);
                return ExResult.ErOK;
            }
            return ExResult.ErFAIL;
            
        case ExCloseActivate:
            return ExResult.ErFAIL;
        }
        return super.ExecCommand(Command, State);
    }
        
    @Override
    void DrawLine(PCell B, int Line, int Col, int /*ChColor*/ color, int Width) 
    {
        if (Buffer.RLine(Buffer.rlst.lines.get(Line).line).getCount() > Col) {
            String str = PCell.UnTabStr( Buffer.RLine(Buffer.rlst.lines.get(Line).line).Chars.toString() );
            int len = str.length();
                        
            if (len > Col)
                B.MoveStr( 0, Width, str + Col, color, len - Col);
        }
    }

    @Override
    String FormatLine(int Line) {
        //char *p = 0;
        ELine L = Buffer.RLine(Buffer.rlst.lines.get(Line).line);
        /*
        p = (char *) malloc(L.Count + 1);
        if (p) {
            memcpy(p, L.Chars, L.Count);
            p[L.Count] = 0;
        } */
        return L.toString();
    }

    @Override
    int Activate(int No) {
        if (No < Buffer.rlst.Count()) {
            View.SwitchToModel(Buffer);
            Buffer.CenterPosR(0, Buffer.rlst.lines.get(No).line, 0);
            return 1;
        }
        return 0;
    }

    @Override
    void RescanList() {
        Buffer.ScanForRoutines();
        UpdateList();
        NeedsRedraw = 1;
    }

    @Override
    void UpdateList() {
        Count = Buffer.rlst.Count();
    }

    @Override
    int GetContext() {
        return CONTEXT_ROUTINES;
    }

    @Override
    String GetName() {
        return "Routines";
    }

    @Override
    String GetInfo() {
        return String.format( "%2d %04d/%03d Routines (%s)", getModelNo(), Row + 1, Count, Buffer.FileName);
    }

    @Override
    void GetTitle(String [] ATitle, String [] ASTitle) {
        ATitle[0] = String.format( "Routines: %s", Buffer.FileName );
        ASTitle[0] = "Routines";
        
    }
    
}
