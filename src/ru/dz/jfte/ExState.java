package ru.dz.jfte;

public class ExState {
    int Macro;
    int Pos;
    

    
    int GetStrParam(EView view, String [] str)
    {
        if (Macro == -1
    	|| Pos == -1
    	|| Pos >= ExMacro.Macros[Macro].Count)
    	return 0;
        if (ExMacro.Macros[Macro].cmds[Pos].type == CommandType.CT_STRING) {
            str[0] = ExMacro.Macros[Macro].cmds[Pos].string;
            Pos++;
        } else if (view != null && ExMacro.Macros[Macro].cmds[Pos].type == CommandType.CT_VARIABLE) {
            //puts("variable\x7");
            if (view.GetStrVar((int)ExMacro.Macros[Macro].cmds[Pos].num, str) == 0)
                return 0;
            Pos++;
        } else
            return 0;
        if (Pos < ExMacro.Macros[Macro].Count) {
            if (ExMacro.Macros[Macro].cmds[Pos].type == CommandType.CT_CONCAT) {
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
    	|| Pos >= ExMacro.Macros[Macro].Count)
    	return 0;
        if (ExMacro.Macros[Macro].cmds[Pos].type == CommandType.CT_NUMBER) {
            value[0] = (int) ExMacro.Macros[Macro].cmds[Pos].num;
            Pos++;
        } else if (view != null && ExMacro.Macros[Macro].cmds[Pos].type == CommandType.CT_VARIABLE) {
            if (view.GetIntVar((int)ExMacro.Macros[Macro].cmds[Pos].num, value) == 0)
                return 0;
            Pos++;
        } else
            return 0;
        return 1;
    }
    
}
