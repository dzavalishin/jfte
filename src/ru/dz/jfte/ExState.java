package ru.dz.jfte;

public class ExState {
    int Macro;
    int Pos;
    

    
    int GetStrParam(EView view, String [] str)
    {
        if (Macro == -1
    	|| Pos == -1
    	//|| Pos >= ExMacro.Macros[Macro].Count
    	)
    	return 0;
        
        ExMacro m = ExMacro.Macros.get(Macro);
        if(m == null) return 0;
        
        if(m.cmds.size() <= Pos)
        {
        	System.err.printf("GetStrParam pos %d >= size %d\n", Pos, m.cmds.size() );
        	return 0;
        }
        
        if (m.cmds.get(Pos).type == CommandType.CT_STRING) {
            str[0] = m.cmds.get(Pos).string;
            Pos++;
        } else if (view != null && m.cmds.get(Pos).type == CommandType.CT_VARIABLE) {
            //puts("variable\x7");
            if (view.GetStrVar((int)m.cmds.get(Pos).num, str) == 0)
                return 0;
            Pos++;
        } else
            return 0;
        if (Pos < m.cmds.size()) {
            if (m.cmds.get(Pos).type == CommandType.CT_CONCAT) {
                Pos++;

                //puts("concat\x7");
                String add[] = {""};
                
                if (GetStrParam(view, add) == 0)
                    return 0;
                
                str[0] += add[0];
            }
        }
        return 1;
    }

    int GetIntParam(EView view, int [] value) {
        if (Macro == -1
    	|| Pos == -1
    	//|| Pos >= m.Count
    	)
    	return 0;

        ExMacro m = ExMacro.Macros.get(Macro);
        if(m == null) return 0;
        
        if( m.cmds.size() <= Pos )
        {
        	System.err.printf("GetIntParam for Macro %d Pos %d out of array size %d",
        			Macro, Pos, m.cmds.size() );
        	return 0;
        }
        
        CommandType cmd = m.cmds.get(Pos);
        
		if (cmd.type == CommandType.CT_NUMBER) {
            value[0] = (int) cmd.num;
            Pos++;
        } else if (view != null && cmd.type == CommandType.CT_VARIABLE) {
            if (view.GetIntVar((int)cmd.num, value) == 0)
                return 0;
            Pos++;
        } else
            return 0;
        return 1;
    }
    
}
