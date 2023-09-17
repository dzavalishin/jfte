package ru.dz.jfte;

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
	
}
