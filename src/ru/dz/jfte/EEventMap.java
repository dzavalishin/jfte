package ru.dz.jfte;

import java.util.Arrays;

import ru.dz.jfte.c.BitOps;

public class EEventMap extends KeyMapper implements ModeDefs  
{
	EEventMap Next;
	EEventMap Parent;
	String Name;

	//EKeyMap KeyMap;


	String [] Menu = new String[100]; // main + local    
	EAbbrev [] abbrev = new EAbbrev [100];

	static EEventMap EventMaps = null;


	EEventMap(String AName, EEventMap AParent) {
		Name = AName;
		Parent = AParent;
		KeyMap = null;
		Next = EventMaps;
		EventMaps = this;
		Arrays.fill(Menu, null);
		Arrays.fill(abbrev, null);
	}


	void SetMenu(int which, String What) {
		if (which < 0 || which >= EM_MENUS)
			return;
		Menu[which] = What;
	}

	String GetMenu(int which) {
		if (which < 0 || which >= EM_MENUS)
			return null;
		if ( (Menu[which] != null) || Parent == null)
			return Menu[which];
		else
			return Parent.GetMenu(which);
	}

	int AddAbbrev(EAbbrev ab) {
		int i = BitOps.HashStr(ab.Match, ABBREV_HASH);
		ab.next = abbrev[i];
		abbrev[i] = ab;
		return 1;
	}




	static EEventMap FindEventMap(String Name)
	{
		EEventMap m = EventMaps;
		while (m != null) {
			if(m.Name.equals(Name)) return m;

			m = m.Next;
		}
		return null;
	}

	
	static EEventMap FindActiveMap(EMode Mode) {
	    while (Mode != null) {
	        if (Mode.fEventMap != null)
	            return Mode.fEventMap;
	        Mode = Mode.fParent;
	    }
	    return null;
	}


	public void dump() {
		System.out.printf("EEventMap '%s'\n", Name);
		if(KeyMap != null) KeyMap.dump();
		System.out.printf("EEventMap '%s' ---- END\n", Name);
		if(null != Parent)
		{
			System.out.printf("EEventMap '%s' parent:\n", Name);
			Parent.dump();
		}
	}
	
}
