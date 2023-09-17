package ru.dz.jfte;

public class KeyTable {
	class KeyMap {
	    final String Name;
	    final long /*TKeyCode*/ Key;
	    
	    KeyMap( String name, long key )
	    {
	    	this.Name = name;
	    	this.Key = key;
	    }
	}
	
	static KeyMap KeyList[] = {
	    new KeyMap( "Esc", kbEsc ),
	    new KeyMap( "Tab", kbTab ),
	    new KeyMap( "Space", kbSpace ),
	    new KeyMap( "Enter", kbEnter ),
	    new KeyMap( "BackSp", kbBackSp ),
	    new KeyMap( "F1", kbF1 ),
	    new KeyMap( "F2", kbF2 ),
	    new KeyMap( "F3", kbF3 ),
	    new KeyMap( "F4", kbF4 ),
	    new KeyMap( "F5", kbF5 ),
	    new KeyMap( "F6", kbF6 ),
	    new KeyMap( "F7", kbF7 ),
	    new KeyMap( "F8", kbF8 ),
	    new KeyMap( "F9", kbF9 ),
	    new KeyMap( "F10", kbF10 ),
	    new KeyMap( "F11", kbF11 ),
	    new KeyMap( "F12", kbF12 ),
	    new KeyMap( "Left", kbLeft ),
	    new KeyMap( "Right", kbRight ),
	    new KeyMap( "Up", kbUp ),
	    new KeyMap( "Down", kbDown ),
	    new KeyMap( "Home", kbHome ),
	    new KeyMap( "End", kbEnd ),
	    new KeyMap( "PgUp", kbPgUp ),
	    new KeyMap( "PgDn", kbPgDn ),
	    new KeyMap( "Ins", kbIns ),
	    new KeyMap( "Del", kbDel ),
	    new KeyMap( "Center", kbCenter ),
	    new KeyMap( "Break", kbBreak ),
	    new KeyMap( "Pause", kbPause ),
	    new KeyMap( "PrtScr", kbPrtScr ),
	    new KeyMap( "SysReq", kbSysReq ),
	};

	static int ParseKey(String Key, KeySel ks) {
	    unsigned char *p = (unsigned char *)Key;
	    long /*TKeyCode*/ KeyFlags = 0;
	    int i;

	    ks.Mask = 0;
	    ks.Key = 0;
	    while ((*p) && ((p[1] == '+') || (p[1] == '-'))) {
	        if (p[1] == '-') {
	            switch (p[0]) {
	            case 'A': ks.Mask |= kfAlt; break;
	            case 'C': ks.Mask |= kfCtrl; break;
	            case 'S': ks.Mask |= kfShift; break;
	            case 'G': ks.Mask |= kfGray; break;
	            case 'X': ks.Mask |= kfSpecial; break;
	            }
	        } else if (p[1] == '+') {
	            switch (p[0]) {
	            case 'A': KeyFlags |= kfAlt; break;
	            case 'C': KeyFlags |= kfCtrl; break;
	            case 'S': KeyFlags |= kfShift; break;
	            case 'G': KeyFlags |= kfGray; break;
	            case 'X': KeyFlags |= kfSpecial; break;
	            }
	        }
	        p += 2;
	    }
	    for (i = 0; i < int(sizeof(KeyList)/sizeof(KeyList[0])); i++)
	        if (strcmp((char *)p, KeyList[i].Name) == 0) {
	            ks.Key = KeyList[i].Key;
	            break;
	        }
	    if (ks.Key == 0)
	        ks.Key = *p;
	    if ((KeyFlags & kfCtrl) && !(KeyFlags & kfSpecial)) {
	        if (ks.Key < 256) {
	            if (ks.Key < 32)
	                ks.Key += 64;
	            else
	                ks.Key = toupper(ks.Key);
	        }
	    }
	    ks.Key |= KeyFlags;
	    return 0;
	}

	static int GetKeyName(String []Key, KeySel ks) {
	    Key[0] = "";

	    if (ks.Key  & kfAlt)   Key[0] += "A+";
	    if (ks.Mask & kfAlt)   Key[0] += "A-";
	    if (ks.Key  & kfCtrl)  Key[0] += "C+";
	    if (ks.Mask & kfCtrl)  Key[0] += "C-";
	    if (ks.Key  & kfGray)  Key[0] += "G+";
	    if (ks.Mask & kfGray)  Key[0] += "G-";
	    if (ks.Key  & kfShift) Key[0] += "S+";
	    if (ks.Mask & kfShift) Key[0] += "S-";

	    if (keyCode(ks.Key) < 256) {
	        char c[2];

	        c[0] = (char)(ks.Key & 0xFF);
	        c[1] = 0;

	        //if (ks.Key & kfCtrl)
	        //    if (c[0] < ' ')
	        //        c[0] += '@';
	        if (c[0] == 32)
	            Key[0] += "Space";
	        else
	            Key[0] += c;
	    } else {
	        for (KeyMap k : KeyList)
	            if (k.Key == keyCode(ks.Key)) {
	            	Key[0] += k.Name;
	                break;
	            }
	    }
	    return 0;
	}

}
