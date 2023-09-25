package ru.dz.jfte;

import ru.dz.jfte.c.ByteArrayPtr;

public class HState implements ModeDefs, ColorDefs 
{
    int transCount = 0;
    int firstTrans = 0;
    int color = 0;
    
    ColorKeywords keywords = new ColorKeywords();
    //String wordChars;
    char [] wordChars;
    long options = 0;
    int nextKwdMatchedState = -1;
    int nextKwdNotMatchedState = -1;
    int nextKwdNoCharState = -1;

    
	/*
    void InitState() {
        memset((void *)&keywords, 0, sizeof(keywords));
        firstTrans = 0;
        transCount = 0;
        color = 0;
        wordChars = 0;
        options = 0;
        nextKwdMatchedState = -1;
        nextKwdNotMatchedState = -1;
        nextKwdNoCharState = -1;
    }
        */

    int GetHilitWord(int len, String str, /*ChColor*/ int [] clr) 
    {
        //byte [] p;

        if (len >= CK_MAXLEN || len < 1)
            return 0;

        if( keywords.key[len] == null )
            return 0;

        ByteArrayPtr p = new ByteArrayPtr(keywords.key[len]);
        
        if(0 != (options & STATE_NOCASE) ) 
        {
            while (p.hasCurrent()) 
            {
            	String pw = p.getLenAsString(len);
            	
                if (str.equalsIgnoreCase(pw)) {
                    clr[0] = p.ur(len);
                    return 1;
                }
                
                p.shift( len + 1 );
            }
        } 
        else 
        {
            while (p.hasCurrent()) 
            {
            	String pw = p.getLenAsString(len);

            	if (str.equals(pw)) {
                    clr[0] = p.ur(len);
                    return 1;
                }
            	
                p.shift( len + 1 );
            }
        }
        
        return 0;
    }
    
}
