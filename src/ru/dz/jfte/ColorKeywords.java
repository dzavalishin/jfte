package ru.dz.jfte;

import java.util.Arrays;

public class ColorKeywords implements ColorDefs
{
    int TotalCount = 0;
    int count[] = new int[CK_MAXLEN];
    //String []key;
    byte [][] key = new byte[CK_MAXLEN][];

    
    public ColorKeywords() {
		Arrays.fill( count, 0 );
		Arrays.fill( key, null );
	}
    
}
