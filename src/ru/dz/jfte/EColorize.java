package ru.dz.jfte;

import java.util.Arrays;

public class EColorize implements ModeDefs
{
    String Name;
    EColorize Next;
    EColorize Parent;
    int SyntaxParser;
    ColorKeywords Keywords = new ColorKeywords(); // keywords to highlight
    HMachine hm;
    int Colors[] = new int[COUNT_CLR];

    static EColorize Colorizers;

    

    
    public EColorize(String AName, String AParent) {
        Name = AName;
        SyntaxParser = HILIT_PLAIN;
        Next = Colorizers;
        hm = null;
        Colorizers = this;
        Parent = FindColorizer(AParent);
        //memset((void *)&Keywords, 0, sizeof(Keywords));
        //memset((void *)Colors, 0, sizeof(Colors));
        Arrays.fill(Colors, 0);
        
        if (Parent != null) {
            SyntaxParser = Parent.SyntaxParser;
            //memcpy((void *)Colors, (void *)Parent.Colors, sizeof(Colors));
            Colors = Arrays.copyOf(Parent.Colors, Parent.Colors.length);
        } else {
            SyntaxParser = HILIT_PLAIN;
        }
    }


    public static EColorize FindColorizer(String AName) {
        EColorize p = Colorizers;

        if( AName == null ) return null;
        
        while (p != null) {
            if (AName.equals(p.Name))
                return p;
            p = p.Next;
        }
        
        return null;
    }
    
    
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
