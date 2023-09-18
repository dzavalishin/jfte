package ru.dz.jfte;

public class RoutineView extends EList 
{
    EBuffer Buffer;

    RoutineView(int createFlags, EModel []ARoot, EBuffer AB) 
    {
    	super(createFlags, ARoot, "Routines");
    	
        Buffer = AB;
        if (Buffer.rlst.Count == 0)
            Buffer.ScanForRoutines();
        Row = 0;
        int Row = Buffer.VToR(Buffer.CP.Row);
        for (int i = Buffer.rlst.Count - 1; i >= 0; --i)
            if (Row >= Buffer.rlst.Lines[i]) {
                Row = i;
                break;
            }
        {
            SetTitle(String.format("Routines %s: %d",
                    Buffer.FileName,
                    Buffer.rlst.Count));
        }
    };


    EEventMap GetEventMap() {
        return EEventMap.FindEventMap("ROUTINES");
    }

    int ExecCommand(int Command, ExState State) {
        switch (Command) {
        case ExRescan:
            Buffer.ScanForRoutines();
            UpdateList();
            return ErOK;

        case ExActivateInOtherWindow:
            if (Row < Buffer.rlst.Count) {
                View.Next.SwitchToModel(Buffer);
                Buffer.CenterPosR(0, Buffer.rlst.Lines[Row]);
                return ErOK;
            }
            return ErFAIL;
            
        case ExCloseActivate:
            return ErFAIL;
        }
        return super.ExecCommand(Command, State);
    }
        
    void DrawLine(PCell B, int Line, int Col, ChColor color, int Width) {
        if (Buffer.RLine(Buffer.rlst.Lines[Line]).Count > Col) {
            char str[1024];
            int len;

            len = UnTabStr(str, sizeof(str),
                           Buffer.RLine(Buffer.rlst.Lines[Line]).Chars,
                           Buffer.RLine(Buffer.rlst.Lines[Line]).Count);
                        
            if (len > Col)
                MoveStr(B, 0, Width, str + Col, color, len - Col);
        }
    }

    String FormatLine(int Line) {
        char *p = 0;
        PELine L = Buffer.RLine(Buffer.rlst.Lines[Line]);
        
        p = (char *) malloc(L.Count + 1);
        if (p) {
            memcpy(p, L.Chars, L.Count);
            p[L.Count] = 0;
        }
        return p;
    }

    int Activate(int No) {
        if (No < Buffer.rlst.Count) {
            View.SwitchToModel(Buffer);
            Buffer.CenterPosR(0, Buffer.rlst.Lines[No]);
            return 1;
        }
        return 0;
    }

    void RescanList() {
        Buffer.ScanForRoutines();
        UpdateList();
        NeedsRedraw = 1;
    }

    void UpdateList() {
        Count = Buffer.rlst.Count;
    }

    int GetContext() {
        return CONTEXT_ROUTINES;
    }

    @Override
    String GetName() {
        return "Routines";
    }

    @Override
    String GetInfo() {
        return String.format( "%2d %04d/%03d Routines (%s)", ModelNo, Row + 1, Count, Buffer.FileName);
    }

    void GetTitle(String [] ATitle, String [] ASTitle) {
        ATitle[0] = String.format( "Routines: %s", Buffer.FileName );
        ASTitle[0] = "Routines";
        
    }
    
}
