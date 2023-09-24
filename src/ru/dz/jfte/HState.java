package ru.dz.jfte;

public class HState {
    int transCount = 0;
    int firstTrans = 0;
    int color = 0;
    
    ColorKeywords keywords = new ColorKeywords();
    String wordChars;
    long options = 0;
    int nextKwdMatchedState = -1;
    int nextKwdNotMatchedState = -1;
    int nextKwdNoCharState = -1;

    
    void InitState() {
    	/*
        memset((void *)&keywords, 0, sizeof(keywords));
        firstTrans = 0;
        transCount = 0;
        color = 0;
        wordChars = 0;
        options = 0;
        nextKwdMatchedState = -1;
        nextKwdNotMatchedState = -1;
        nextKwdNoCharState = -1;
        */
    }

    int GetHilitWord(int len, char *str, ChColor &clr) 
    {
        char *p;

        if (len >= CK_MAXLEN || len < 1)
            return 0;

        p = keywords.key[len];
        if (options & STATE_NOCASE) {
            while (p && *p) {
                if (strnicmp(p, str, len) == 0) {
                    clr = p[len];
                    return 1;
                }
                p += len + 1;
            }
        } else {
            while (p && *p) {
                if (memcmp(p, str, len) == 0) {
                    clr = p[len];
                    return 1;
                }
                p += len + 1;
            }
        }
        return 0;
    }
    
}
