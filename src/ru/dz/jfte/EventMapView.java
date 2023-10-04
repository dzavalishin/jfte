package ru.dz.jfte;

import java.util.List;

import ru.dz.jfte.config.ConfigCompilerDefs;

import java.util.ArrayList;

public class EventMapView extends EList implements ConfigCompilerDefs 
{
    //String *BList;
    //int BCount = 0;
	List<String> BList = new ArrayList<>();
    EEventMap EMap;

    
    static EventMapView TheEventMapView;
	

   void AddLine(String Line) 
   {
       BList.add(Line);
   }

   void DumpKey(String aPrefix, EKey Key) {
       //char KeyName[128] = "";
       //char Entry[2048] = "";
	   String [] KeyName = {""};
       int id;
       
       
       KeyDefs.GetKeyName(KeyName, Key.fKey);

       if (aPrefix != null) 
           KeyName[0] = aPrefix+"_"+KeyName[0];
       
       String Entry = String.format("%13s   ", KeyName[0]);
       
       id = Key.Cmd;
       
       ExMacro m = ExMacro.Macros.get(id);
       
       for ( CommandType c : m.cmds ) 
       {
           String p = "";

           if (c.type == CommandType.CT_COMMAND) 
           {
               if (c.repeat > 1)
                   p = String.format( "%d:%s ", c.repeat, GetCommandName(c.num));
               else
            	   p = String.format( "%s ", GetCommandName(c.num));
           } else if (c.type == CommandType.CT_NUMBER) {
        	   p = String.format( "%d ", c.num);
           } else if (c.type == CommandType.CT_STRING) {
        	   p = String.format( "'%s' ", c.string);
           }
           
           Entry += p;
       }
       
       AddLine(Entry);
   }
       
   void DumpMap(String aPrefix, EKeyMap aKeyMap) {
       EKey Key;
       
       Key = aKeyMap.fKeys;
       while (Key!=null) 
       {
    	   
           /* TODO if (Key.fKeyMap) {
               //char Prefix[32] = "";
        	   String Prefix;
               
               if (aPrefix != null)
                   Prefix = aPrefix+"_";

               GetKeyName(Prefix + strlen(Prefix), Key.fKey);
               DumpMap(Prefix, Key.fKeyMap);
           } else */ {
               DumpKey(aPrefix, Key);
           }
           
           Key = Key.fNext;
       }
   }
       
   void DumpEventMap(EEventMap aEventMap) {
       //char name[256];
       
       while (aEventMap != null) 
       {
           String name = aEventMap.Name;
           
           if (aEventMap.Parent != null) {
               name += ": " + aEventMap.Parent.Name;
           }
           
           AddLine(name);
           
           if (aEventMap.KeyMap != null)
               DumpMap( null, aEventMap.KeyMap);
           
           aEventMap = aEventMap.Parent;
           if (aEventMap != null)
               AddLine("");
       }
   }

   EventMapView(int createFlags, EModel [] ARoot, EEventMap Map) 
   {
	   super(createFlags, ARoot, "Event Map");
	   
       DumpEventMap(EMap = Map);
       TheEventMapView = this;
   }

   @Override
   public void close() {
       TheEventMapView = null;
   }


   void ViewMap(EEventMap Map) {
       //FreeView();
       DumpEventMap(EMap = Map);
   }

   @Override
   EEventMap GetEventMap() {
       return EEventMap.FindEventMap("EVENTMAPVIEW");
   }

   /*
   @Override
   int ExecCommand(int Command, ExState State) {
       return EList::ExecCommand(Command, State);
   } */

   @Override
   int GetContext() {
       return CONTEXT_MAPVIEW;
   }

   @Override
   void DrawLine(PCell B, int Line, int Col, int color, int Width) 
   {
       if (Line < BList.size())
       {
    	   String l = BList.get(Line);
    	   
           if (Col < l.length())
               B.MoveStr(0, Width, l + Col, color, Width);
       }
   }

   @Override
   String FormatLine(int Line) {
       return BList.get(Line);
   }

   @Override
   void UpdateList() {
       Count = BList.size();
       super.UpdateList();
   }

   @Override
   boolean CanActivate(int Line) {
       return false;
   }

   @Override
   String GetName() {
       return "EventMapView";
   }

   @Override
   String GetInfo() {
	   return String.format( "%2d %04d/%03d EventMapView (%s)",
               ModelNo,
               Row + 1, Count,
               EMap.Name);
   	}

   @Override
   void GetTitle(String [] ATitle, String [] ASTitle) {
       ATitle[0] = String.format("EventMapView: %s", EMap.Name);
       ASTitle[0] = "EventMapView";
   }


   
   
   
   static String GetCommandName(int Command) 
   {
	    if(0 != (Command & CMD_EXT)) 
	    {
	        Command &= ~CMD_EXT;
	        if ((Command < 0) ||
	            (Command >= ExMacro.Macros.size()))
	            return "?INVALID?";
	        
	        ExMacro m = ExMacro.Macros.get(Command);
	        
	        if (m.Name != null)
	            return m.Name;
	        else
	            return "?NONE?";
	    }
	    
	    for (int i = 0; i < Command_Table.length; i++)
	    {
	        if (Command_Table[i].getCmdId() == Command)
	            return Command_Table[i].getName();
	    }
	    return "?invalid?";
	}
   
   
   
   
   
   
   
   
   
}
