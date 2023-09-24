package ru.dz.jfte;

public class EColorize implements ModeDefs
{
    String Name;
    EColorize Next;
    EColorize Parent;
    int SyntaxParser;
    ColorKeywords Keywords; // keywords to highlight
    HMachine hm;
    int Colors[] = new int[COUNT_CLR];

    static EColorize Colorizers;

    
    int SetColor(int idx, String Value) {
        int Col;
        int ColBg, ColFg;
        /*ChColor*/ int C;

        //if (sscanf(Value, "%1X %1X", &ColFg, &ColBg) != 2)            return 0;

        String[] ss = Value.split(" ");
        if( ss.length != 2)
            return 0;
        
        ColFg = Integer.parseInt(ss[0], 16);
        ColBg = Integer.parseInt(ss[1], 16);
        
        Col = ColFg | (ColBg << 4);
        C = Col; //ChColor(Col);

        if (idx < 0 || idx >= COUNT_CLR)
            return 0;
        Colors[idx] = C;
        return 1;
    }
    
    
}
