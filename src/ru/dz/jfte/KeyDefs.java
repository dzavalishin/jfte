package ru.dz.jfte;

import ru.dz.jfte.c.ByteArrayPtr;
import ru.dz.jfte.struct.KeyDef;

public interface KeyDefs {


	static public final int kfAltXXX    =0x01000000;
	static public final int kfModifier  =0x02000000;
	static public final int kfSpecial   =0x00010000;
	static public final int kfAlt       =0x00100000;
	static public final int kfCtrl      =0x00200000;
	static public final int kfShift     =0x00400000;
	static public final int kfGray      =0x00800000;
	static public final int kfKeyUp     =0x10000000;
	static public final int kfAll       =0x00F00000;

	static public boolean isAltXXX(int x) { return (((x) & (kfAltXXX)) != 0); }
	static public boolean isAlt(int x)   { return (((x) & kfAlt) != 0); }
	static public boolean isCtrl(int x)   { return (((x) & kfCtrl) != 0); }
	static public boolean isShift(int x)  { return (((x) & kfShift) != 0); }
	static public boolean isGray(int x)  { return (((x) & kfGray) != 0); }
	static public int keyType(int x)  { return ((x) & kfAll); }
	static public int keyCode(int x)  { return  ((x) & 0x000FFFFF); }
	static public int kbCode(int x)  { return (((x) & 0x0FFFFFFF) & ~(kfGray | kfAltXXX)); }
	static public boolean isAscii(int x)  { return  ((((x) & (kfAlt | kfCtrl)) == 0) && (keyCode(x) < 256)); }

	public static boolean isalnum(char c) {
		return Character.isAlphabetic(c) || Character.isDigit(c);
	}


	static public final int kbF1         = (kfSpecial | 0x101);
	static public final int kbF2         = (kfSpecial | 0x102);
	static public final int kbF3         = (kfSpecial | 0x103);
	static public final int kbF4         = (kfSpecial | 0x104);
	static public final int kbF5         = (kfSpecial | 0x105);
	static public final int kbF6         = (kfSpecial | 0x106);
	static public final int kbF7         = (kfSpecial | 0x107);
	static public final int kbF8         = (kfSpecial | 0x108);
	static public final int kbF9         = (kfSpecial | 0x109);
	static public final int kbF10        = (kfSpecial | 0x110);
	static public final int kbF11        = (kfSpecial | 0x111);
	static public final int kbF12        = (kfSpecial | 0x112);

	static public final int kbUp         = (kfSpecial | 0x201);
	static public final int kbDown       = (kfSpecial | 0x202);
	static public final int kbLeft       = (kfSpecial | 0x203);
	static public final int kbCenter     = (kfSpecial | 0x204);
	static public final int kbRight      = (kfSpecial | 0x205);
	static public final int kbHome       = (kfSpecial | 0x206);
	static public final int kbEnd        = (kfSpecial | 0x207);
	static public final int kbPgUp       = (kfSpecial | 0x208);
	static public final int kbPgDn       = (kfSpecial | 0x209);
	static public final int kbIns        = (kfSpecial | 0x210);
	static public final int kbDel        = (kfSpecial | 0x211);

	static public final int kbSpace      = 32;

	static public final int kbBackSp     = (kfSpecial | 8);
	static public final int kbTab        = (kfSpecial | 9); 
	static public final int kbEnter      = (kfSpecial | 13);
	static public final int kbEsc        = (kfSpecial | 27);

	static public final int kbAlt        =(kfModifier | 0x301);
	static public final int kbCtrl       =(kfModifier | 0x302);
	static public final int kbShift      =(kfModifier | 0x303);
	static public final int kbCapsLock   =(kfModifier | 0x304);
	static public final int kbNumLock    =(kfModifier | 0x305);
	static public final int kbScrollLock =(kfModifier | 0x306);

	static public final int kbPause      = (kfSpecial | 0x401);
	static public final int kbPrtScr     = (kfSpecial | 0x402);
	static public final int kbSysReq     = (kfSpecial | 0x403);
	static public final int kbBreak      = (kfSpecial | 0x404);


	
	static KeyDef KeyList[] = {
		    new KeyDef( "Esc", kbEsc ),
		    new KeyDef( "Tab", kbTab ),
		    new KeyDef( "Space", kbSpace ),
		    new KeyDef( "Enter", kbEnter ),
		    new KeyDef( "BackSp", kbBackSp ),
		    new KeyDef( "F1", kbF1 ),
		    new KeyDef( "F2", kbF2 ),
		    new KeyDef( "F3", kbF3 ),
		    new KeyDef( "F4", kbF4 ),
		    new KeyDef( "F5", kbF5 ),
		    new KeyDef( "F6", kbF6 ),
		    new KeyDef( "F7", kbF7 ),
		    new KeyDef( "F8", kbF8 ),
		    new KeyDef( "F9", kbF9 ),
		    new KeyDef( "F10", kbF10 ),
		    new KeyDef( "F11", kbF11 ),
		    new KeyDef( "F12", kbF12 ),
		    new KeyDef( "Left", kbLeft ),
		    new KeyDef( "Right", kbRight ),
		    new KeyDef( "Up", kbUp ),
		    new KeyDef( "Down", kbDown ),
		    new KeyDef( "Home", kbHome ),
		    new KeyDef( "End", kbEnd ),
		    new KeyDef( "PgUp", kbPgUp ),
		    new KeyDef( "PgDn", kbPgDn ),
		    new KeyDef( "Ins", kbIns ),
		    new KeyDef( "Del", kbDel ),
		    new KeyDef( "Center", kbCenter ),
		    new KeyDef( "Break", kbBreak ),
		    new KeyDef( "Pause", kbPause ),
		    new KeyDef( "PrtScr", kbPrtScr ),
		    new KeyDef( "SysReq", kbSysReq ),
		};

	
	static int ParseKey(String Key, KeySel ks) {
	    //unsigned char *p = (unsigned char *)Key;
	    ByteArrayPtr p = new ByteArrayPtr(Key.getBytes());
	    long /*TKeyCode*/ KeyFlags = 0;
	    //int i;

	    ks.Mask = 0;
	    ks.Key = 0;
	    while (p.hasBytesLeft() >= 2 && p.r(0) != 0 && ((p.r(1) == '+') || (p.r(1) == '-'))) {
	        if (p.r(1) == '-') {
	            switch (p.r(0)) {
	            case 'A': ks.Mask |= kfAlt; break;
	            case 'C': ks.Mask |= kfCtrl; break;
	            case 'S': ks.Mask |= kfShift; break;
	            case 'G': ks.Mask |= kfGray; break;
	            case 'X': ks.Mask |= kfSpecial; break;
	            }
	        } else if (p.r(1) == '+') {
	            switch (p.r(0)) {
	            case 'A': KeyFlags |= kfAlt; break;
	            case 'C': KeyFlags |= kfCtrl; break;
	            case 'S': KeyFlags |= kfShift; break;
	            case 'G': KeyFlags |= kfGray; break;
	            case 'X': KeyFlags |= kfSpecial; break;
	            }
	        }
	        //p += 2;
	        p.shift(2);
	    }
	    
	    /*for (i = 0; i < int(sizeof(KeyList)/sizeof(KeyList[0])); i++)
	    {
	        if (strcmp((char *)p, KeyList[i].Name) == 0) {
	            ks.Key = KeyList[i].Key;
	            break;
	        }
	    }*/
	    
	    for( KeyDef km : KeyList )
	    {
	    	String ps = p.getRestAsString();
	    	if( km.getName().equals(ps) )
	    	{
	            ks.Key = km.getKey();
	            break;
	    	}
	    }
	    
	    
	    if (ks.Key == 0)
	        ks.Key = p.r(0);
	    if (0 !=(KeyFlags & kfCtrl) && 0==(KeyFlags & kfSpecial)) 
	    {
	        if (ks.Key < 256) {
	            if (ks.Key < 32)
	                ks.Key += 64;
	            else
	                ks.Key = Character.toUpperCase(ks.Key);
	        }
	    }
	    ks.Key |= KeyFlags;
	    return 0;
	}

	static int GetKeyName(String []Key, KeySel ks) {
	    Key[0] = "";

	    if(0 != (ks.Key  & kfAlt))   Key[0] += "A+";
	    if(0 != (ks.Mask & kfAlt))   Key[0] += "A-";
	    if(0 != (ks.Key  & kfCtrl))  Key[0] += "C+";
	    if(0 != (ks.Mask & kfCtrl))  Key[0] += "C-";
	    if(0 != (ks.Key  & kfGray))  Key[0] += "G+";
	    if(0 != (ks.Mask & kfGray))  Key[0] += "G-";
	    if(0 != (ks.Key  & kfShift)) Key[0] += "S+";
	    if(0 != (ks.Mask & kfShift)) Key[0] += "S-";

	    if (KeyDefs.keyCode(ks.Key) < 256) {

	        char c = (char)(ks.Key & 0xFF);

	        //if (ks.Key & kfCtrl)
	        //    if (c[0] < ' ')
	        //        c[0] += '@';
	        if (c == 32)
	            Key[0] += "Space";
	        else
	            Key[0] += ""+c;
	    } else {
	        for (KeyDef k : KeyList)
	            if (k.getKey() == KeyDefs.keyCode(ks.Key)) {
	            	Key[0] += k.getName();
	                break;
	            }
	    }
	    return 0;
	}
	
}
