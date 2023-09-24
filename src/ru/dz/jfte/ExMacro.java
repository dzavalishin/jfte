package ru.dz.jfte;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ExMacro {
    String Name;
    //int Count;
    //CommandType []cmds;
    List<CommandType> cmds = new ArrayList<> ();

    //static int CMacros = 0;
    //static ExMacro [] Macros = null;
    
    //private static List<ExMacro> Macros = new ArrayList<>();
    
    static Map<Integer,ExMacro> Macros = new HashMap<>();

    
    
    public ExMacro(String name) {
		Name = name;
	}

    
    
    
    
	static int AddCommand(int no, int Command, int count, int ign) {
        if (count == 0) return 0;
        if (Command == 0) return 0;
        /*
        Macros[no].cmds = (CommandType *)realloc(Macros[no].cmds, sizeof(CommandType) * (Macros[no].Count + 1));
        Macros[no].cmds[Macros[no].Count].type = CT_COMMAND;
        Macros[no].cmds[Macros[no].Count].u.num = Command;
        Macros[no].cmds[Macros[no].Count].repeat = short(count);
        Macros[no].cmds[Macros[no].Count].ign = short(ign);
        Macros[no].Count++;
        */
        ExMacro m = Macros.get(no);
        
        CommandType c = new CommandType();
        c.type = CommandType.CT_COMMAND;
        c.num = Command;
        c.repeat = count;
        
        m.cmds.add(c);
        
        return 1;
    }

	static int AddString(int no, String s) {
    	/*
        Macros[no].cmds = (CommandType *)realloc(Macros[no].cmds, sizeof(CommandType) * (Macros[no].Count + 1));
        Macros[no].cmds[Macros[no].Count].type = CT_STRING;
        Macros[no].cmds[Macros[no].Count].u.string = strdup(String);
        Macros[no].cmds[Macros[no].Count].repeat = 0;
        Macros[no].cmds[Macros[no].Count].ign = 0;
        Macros[no].Count++;
        */
        ExMacro m = Macros.get(no);
        
        CommandType c = new CommandType();
        c.type = CommandType.CT_STRING;
        c.string = s;
        c.repeat = 0;
        c.ign = 0;
        
        m.cmds.add(c);
    	
    	
        return 1;
    }

	static int AddNumber(int no, long number) {
    	/*
        Macros[no].cmds = (CommandType *)realloc(Macros[no].cmds, sizeof(CommandType) * (Macros[no].Count + 1));
        Macros[no].cmds[Macros[no].Count].type = CT_NUMBER;
        Macros[no].cmds[Macros[no].Count].u.num = number;
        Macros[no].cmds[Macros[no].Count].repeat = 0;
        Macros[no].cmds[Macros[no].Count].ign = 0;
        Macros[no].Count++;
		*/
        ExMacro m = Macros.get(no);
        
        CommandType c = new CommandType();
        c.type = CommandType.CT_NUMBER;
        c.num = number;
        c.repeat = 0;
        c.ign = 0;
        
        m.cmds.add(c);
        
        return 1;
    }

	static int AddConcat(int no) {
    	/*
        Macros[no].cmds = (CommandType *)realloc(Macros[no].cmds, sizeof(CommandType) * (Macros[no].Count + 1));
        Macros[no].cmds[Macros[no].Count].type = CT_CONCAT;
        Macros[no].cmds[Macros[no].Count].u.num = 0;
        Macros[no].cmds[Macros[no].Count].repeat = 0;
        Macros[no].cmds[Macros[no].Count].ign = 0;
        Macros[no].Count++;
        */
    	
        ExMacro m = Macros.get(no);
        
        CommandType c = new CommandType();
        c.type = CommandType.CT_CONCAT;
        c.num = 0;
        c.repeat = 0;
        c.ign = 0;
        
        m.cmds.add(c);
    	
        return 1;
    }

	static int AddVariable(int no, int number) {
    	/*
        Macros[no].cmds = (CommandType *)realloc(Macros[no].cmds, sizeof(CommandType) * (Macros[no].Count + 1));
        Macros[no].cmds[Macros[no].Count].type = CT_VARIABLE;
        Macros[no].cmds[Macros[no].Count].u.num = number;
        Macros[no].cmds[Macros[no].Count].repeat = 0;
        Macros[no].cmds[Macros[no].Count].ign = 0;
        Macros[no].Count++;
        */
    	
        ExMacro m = Macros.get(no);
        
        CommandType c = new CommandType();
        c.type = CommandType.CT_VARIABLE;
        c.num = number;
        c.repeat = 0;
        c.ign = 0;
        
        m.cmds.add(c);
    	
        return 1;
    }

	/*static int NewCommand(String name) {
    	/*
        Macros = (ExMacro *) realloc(Macros, sizeof(ExMacro) * (1 + CMacros));
        Macros[CMacros].Count = 0;
        Macros[CMacros].cmds = 0;
        Macros[CMacros].Name = (Name != NULL) ? strdup(Name) : 0;
        CMacros++;
        * /
    	
    	Macros.add(new ExMacro(name));
    	
        return Macros.size() - 1;
    }*/

	
	static void NewCommand(String name, int index) {
    	
    	Macros.put(index,new ExMacro(name));
    	
        //return Macros.size() - 1;
    }
	
}
