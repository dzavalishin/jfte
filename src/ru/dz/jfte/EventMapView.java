package ru.dz.jfte;

import java.util.List;
import java.util.ArrayList;

public class EventMapView extends EList 
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
	   String KeyName;
       String p;
       int id;
       
       if (aPrefix) {
           KeyName = aPrefix+"_";
       }
       
       GetKeyName(KeyName + strlen(KeyName), Key.fKey);
       sprintf(Entry, "%13s   ", KeyName);
       id = Key.Cmd;
       for (int i = 0; i < Macros[id].Count; i++) {
           p = Entry + strlen(Entry);
           if (Macros[id].cmds[i].type == CT_COMMAND) {
               if (Macros[id].cmds[i].repeat > 1)
                   sprintf(p, "%d:%s ", Macros[id].cmds[i].repeat, GetCommandName(Macros[id].cmds[i].u.num));
               else
                   sprintf(p, "%s ", GetCommandName(Macros[id].cmds[i].u.num));
           } else if (Macros[id].cmds[i].type == CT_NUMBER) {
               sprintf(p, "%ld ", Macros[id].cmds[i].u.num);
           } else if (Macros[id].cmds[i].type == CT_STRING) {
               sprintf(p, "'%s' ", Macros[id].cmds[i].u.string);
           }
           if (strlen(Entry) > 1950) {
               strcat(Entry, "...");
               break;
           }
       }
       AddLine(Entry);
   }
       
   void DumpMap(String aPrefix, EKeyMap aKeyMap) {
       EKey Key;
       
       Key = aKeyMap.fKeys;
       while (Key) {
           if (Key.fKeyMap) {
               //char Prefix[32] = "";
               
               if (aPrefix) {
                   strcpy(Prefix, aPrefix);
                   strcat(Prefix, "_");
               }
               GetKeyName(Prefix + strlen(Prefix), Key.fKey);
               DumpMap(Prefix, Key.fKeyMap);
           } else {
               DumpKey(aPrefix, Key);
           }
           Key = Key.fNext;
       }
   }
       
   void DumpEventMap(EEventMap aEventMap) {
       //char name[256];
       
       while (aEventMap) {
           strcpy(name, aEventMap.Name);
           if (aEventMap.Parent) {
               strcat(name, ": ");
               strcat(name, aEventMap.Parent.Name);
           }
           AddLine(name);
           if (aEventMap.KeyMap)
               DumpMap(0, aEventMap.KeyMap);
           aEventMap = aEventMap.Parent;
           if (aEventMap != 0)
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
       FreeView();
       DumpEventMap(EMap = Map);
   }

   @Override
   EEventMap GetEventMap() {
       return FindEventMap("EVENTMAPVIEW");
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
       if (Line < BCount) 
           if (Col < int(strlen(BList[Line])))
               MoveStr(B, 0, Width, BList[Line] + Col, color, Width);
   }

   @Override
   String FormatLine(int Line) {
       return strdup(BList[Line]);
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

    
}
