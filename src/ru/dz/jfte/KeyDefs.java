package ru.dz.jfte;

public interface KeyDefs {


static public final long kfAltXXX    =0x01000000L;
static public final long kfModifier  =0x02000000L;
static public final long kfSpecial   =0x00010000L;
static public final long kfAlt       =0x00100000L;
static public final long kfCtrl      =0x00200000L;
static public final long kfShift     =0x00400000L;
static public final long kfGray      =0x00800000L;
static public final long kfKeyUp     =0x10000000L;
static public final long kfAll       =0x00F00000L;

static public boolean isAltXXX(int x) { return (((x) & (kfAltXXX)) != 0); }
static public boolean isAlt(int x)   { return (((x) & kfAlt) != 0); }
static public boolean isCtrl(int x)   { return (((x) & kfCtrl) != 0); }
static public boolean isShift(int x)  { return (((x) & kfShift) != 0); }
static public boolean isGray(int x)  { return (((x) & kfGray) != 0); }
static public long keyType(int x)  { return ((x) & kfAll); }
static public long keyCode(int x)  { return  ((x) & 0x000FFFFF); }
static public long kbCode(int x)  { return (((x) & 0x0FFFFFFF) & ~(kfGray | kfAltXXX)); }
static public boolean isAscii(int x)  { return  ((((x) & (kfAlt | kfCtrl)) == 0) && (keyCode(x) < 256)); }
                                  
static public final long kbF1         = (kfSpecial | 0x101);
static public final long kbF2         = (kfSpecial | 0x102);
static public final long kbF3         = (kfSpecial | 0x103);
static public final long kbF4         = (kfSpecial | 0x104);
static public final long kbF5         = (kfSpecial | 0x105);
static public final long kbF6         = (kfSpecial | 0x106);
static public final long kbF7         = (kfSpecial | 0x107);
static public final long kbF8         = (kfSpecial | 0x108);
static public final long kbF9         = (kfSpecial | 0x109);
static public final long kbF10        = (kfSpecial | 0x110);
static public final long kbF11        = (kfSpecial | 0x111);
static public final long kbF12        = (kfSpecial | 0x112);

static public final long kbUp         = (kfSpecial | 0x201);
static public final long kbDown       = (kfSpecial | 0x202);
static public final long kbLeft       = (kfSpecial | 0x203);
static public final long kbCenter     = (kfSpecial | 0x204);
static public final long kbRight      = (kfSpecial | 0x205);
static public final long kbHome       = (kfSpecial | 0x206);
static public final long kbEnd        = (kfSpecial | 0x207);
static public final long kbPgUp       = (kfSpecial | 0x208);
static public final long kbPgDn       = (kfSpecial | 0x209);
static public final long kbIns        = (kfSpecial | 0x210);
static public final long kbDel        = (kfSpecial | 0x211);

static public final long kbSpace      = 32;

static public final long kbBackSp     = (kfSpecial | 8);
static public final long kbTab        = (kfSpecial | 9); 
static public final long kbEnter      = (kfSpecial | 13);
static public final long kbEsc        = (kfSpecial | 27);

static public final long kbAlt        =(kfModifier | 0x301);
static public final long kbCtrl       =(kfModifier | 0x302);
static public final long kbShift      =(kfModifier | 0x303);
static public final long kbCapsLock   =(kfModifier | 0x304);
static public final long kbNumLock    =(kfModifier | 0x305);
static public final long kbScrollLock =(kfModifier | 0x306);

static public final long kbPause      = (kfSpecial | 0x401);
static public final long kbPrtScr     = (kfSpecial | 0x402);
static public final long kbSysReq     = (kfSpecial | 0x403);
static public final long kbBreak      = (kfSpecial | 0x404);
	
}
