package ru.dz.jfte;

public interface ConfigDefs {

	public static final int CF_STRING   =1;
	public static final int CF_INT      =2;
	public static final int CF_REGEXP   =3;

	public static final int CF_END     =  100;
	public static final int CF_SUB     =  101;
	public static final int CF_MENU    =  102;
	public static final int CF_OBJECT  =  103;
	public static final int CF_COMMAND =  104;
	public static final int CF_ITEM    =  105;
	public static final int CF_SUBMENU =  106;
	public static final int CF_MENUSUB =  107;
	public static final int CF_MODE    =  108;
	public static final int CF_PARENT  =  109;
	public static final int CF_KEYSUB  =  110;
	public static final int CF_KEY     =  111;
	public static final int CF_COLOR   =  112;
	public static final int CF_KEYWORD =  113;
	public static final int CF_SETVAR  =  114;
	public static final int CF_COMPRX  =  115;
	public static final int CF_EVENTMAP=  116;
	public static final int CF_COLORIZE=  117;
	public static final int CF_ABBREV  =  118;
	public static final int CF_HSTATE  =  119;
	public static final int CF_HTRANS  =  120;
	public static final int CF_HWORDS  =  121;
	public static final int CF_SUBMENUCOND=  122;
	public static final int CF_HWTYPE  =  123;
	public static final int CF_VARIABLE=  124;
	public static final int CF_CONCAT  =  125;
	public static final int CF_CVSIGNRX=  126;

	public static final int CF_EOF      =254;

	public static final int CONFIG_ID   =0x1A1D70E1;
	
	
}
