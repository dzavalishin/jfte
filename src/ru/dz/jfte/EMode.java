package ru.dz.jfte;

public class EMode {
    EMode fNext = null;
    String fName;
    String MatchName = null;
    String MatchLine = null;
    RxNode MatchNameRx = null;
    RxNode MatchLineRx = null;
    EBufferFlags Flags = new EBufferFlags();
    EEventMap fEventMap;
    EMode fParent;

    // TODO EColorize fColorize = null;

    String filename;

    
    EMode(EMode aMode, EEventMap Map, String aName) {
        fName = aName;
        fEventMap = Map;
        fParent = aMode;
        //InitWordChars();
        if (aMode != null) {
            // TODO fColorize = aMode.fColorize;
            Flags = aMode.Flags;

            // duplicate strings in flags to allow them be freed
            /*
            for (int i=0; i<BFS_COUNT; i++)
            {
                if (aMode.Flags.str[i] != null)
                    Flags.str[i] = aMode.Flags.str[i];
            }*/

            if (aMode.MatchName!=null) {
                MatchName = aMode.MatchName;
                MatchNameRx = RxNode.RxCompile(MatchName);
            }
            if (aMode.MatchLine!=null) {
                MatchLine = aMode.MatchLine;
                MatchLineRx = RxNode.RxCompile(MatchLine);
            }
        }
    }

    
}
