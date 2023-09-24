package ru.dz.jfte;

public class HTrans {
    String match;
    //int matchLen;
    long matchFlags;
    int nextState;
    int color;

    
    void InitTrans() {
        match = null;
        //matchLen = 0;
        matchFlags = 0;
        nextState = 0;
        color = 0;
    }
    
}
