package ru.dz.jfte;

public class EAbbrev {
    EAbbrev next;
    int Cmd;
    String Match;
    String Replace;

    EAbbrev(String aMatch, String aReplace) {
        next = null;
        Match = aMatch;
        Replace = aReplace;
        Cmd = -1;
    }

    EAbbrev(String aMatch, int aCmd) {
        next = null;
        Replace = null;
        Match = aMatch;
        Cmd = aCmd;
    }
    
}
