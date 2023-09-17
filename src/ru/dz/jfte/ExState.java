package ru.dz.jfte;

public class ExState {
    int Macro;
    int Pos;
    
    int GetStrParam(EView view, String [] str)
    {
        if (Macro == -1
    	|| Pos == -1
    	|| Pos >= Macros[Macro].Count)
    	return 0;
        if (Macros[Macro].cmds[Pos].type == CT_STRING) {
            str[0] = Macros[Macro].cmds[Pos].u.string;
            Pos++;
        } else if (view && Macros[Macro].cmds[Pos].type == CT_VARIABLE) {
            //puts("variable\x7");
            if (view.GetStrVar(Macros[Macro].cmds[Pos].u.num, str) == 0)
                return 0;
            Pos++;
        } else
            return 0;
        if (Pos < Macros[Macro].Count) {
            if (Macros[Macro].cmds[Pos].type == CT_CONCAT) {
                Pos++;
                int len = strlen(str);
                int left = maxlen - len;

                assert(left >= 0);

                //puts("concat\x7");
                if (GetStrParam(view, str + len, left) == 0)
                    return 0;
            }
        }
        return 1;
    }

    int GetIntParam(EView view, int [] value) {
        if (Macro == -1
    	|| Pos == -1
    	|| Pos >= Macros[Macro].Count)
    	return 0;
        if (Macros[Macro].cmds[Pos].type == CT_NUMBER) {
            value[0] = Macros[Macro].cmds[Pos].u.num;
            Pos++;
        } else if (view && Macros[Macro].cmds[Pos].type == CT_VARIABLE) {
            if (view->GetIntVar(Macros[Macro].cmds[Pos].u.num, value) == 0)
                return 0;
            Pos++;
        } else
            return 0;
        return 1;
    }
    
}
