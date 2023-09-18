package ru.dz.jfte;

public class GFramePeer {
    int fW, fH;
    GFrame Frame;

    GFramePeer(GFrame aFrame, int Width, int Height) {
        Frame = aFrame;
        if (Width != -1 && Height != -1)
            ConSetSize(Width, Height);
        
        int [] w = {0}, h = {0};
        ConQuerySize(w, h);
        
        fW = w[0];
        fH = h[0];
    }


    int ConSetSize(int X, int Y) {
        return Console.ConSetSize(X, Y);
    }

    int ConQuerySize(int []X, int []Y) {
        int [] tW = {0}, tH = {0};
        Console.ConQuerySize(tW, tH);
        fW = tW[0];
        fH = tH[0];
        if (X!=null) X[0] = tW[0];
        if (Y!=null) Y[0] = tH[0];
        return 1;
    }   

    //int GFrame::ConQuerySize(int *X, int *Y) {
//        Console.ConQuerySize(X, Y);
//        if (ShowVScroll)
//            --*X;
    //}

    int ConSetTitle(String Title, String STitle) {
        Console.ConSetTitle(Title, STitle);
        return 0;
    }

    int ConGetTitle(String []Title, String []STitle) {
        return Console.ConGetTitle(Title, STitle);
    }
    
    
}
