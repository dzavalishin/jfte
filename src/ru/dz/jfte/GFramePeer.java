package ru.dz.jfte;

public class GFramePeer {
    int fW, fH;
    GFrame Frame;

    GFramePeer(GFrame aFrame, int Width, int Height) {
        Frame = aFrame;
        if (Width != -1 && Height != -1)
            ConSetSize(Width, Height);
        ConQuerySize(&fW, &fH);
    }


    int ConSetSize(int X, int Y) {
        return ::ConSetSize(X, Y);
    }

    int ConQuerySize(int *X, int *Y) {
        ::ConQuerySize(&fW, &fH);
        if (X) *X = fW;
        if (Y) *Y = fH;
        return 1;
    }   

    //int GFrame::ConQuerySize(int *X, int *Y) {
//        ::ConQuerySize(X, Y);
//        if (ShowVScroll)
//            --*X;
    //}

    int ConSetTitle(String Title, String STitle) {
        ::ConSetTitle(Title, STitle);
        return 0;
    }

    int ConGetTitle(String []Title, String []STitle) {
        return ::ConGetTitle(Title, STitle);
    }
    
    
}
