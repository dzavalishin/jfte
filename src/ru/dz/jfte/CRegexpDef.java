package ru.dz.jfte;

import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;
import java.util.List;
import java.util.ArrayList;

public class CRegexpDef {
    int RefFile;
    int RefLine;
    int RefMsg;
    Pattern rx;

    
    static List<CRegexpDef> CRegexp = new ArrayList<>();
    
    static int AddCRegexp(int file, int line, int msg, String regexp) {
    	CRegexpDef r = new CRegexpDef();

        r.RefFile = file;
        r.RefLine = line;
        r.RefMsg = msg;
        
        try {
        
        r.rx = Pattern.compile(regexp);
        
        } catch(PatternSyntaxException e)
        {
            return 0;
        }
        
        return 1;
    }
    
}
