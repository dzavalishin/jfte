package ru.dz.jfte;

public interface ColorDefs {

	int /*ChColor*/  MsgColor[] = { 0x07, 0x0B, 0x04 };

	/* Status line */

	int /*ChColor*/  hcStatus_Normal = 0x30;
	int /*ChColor*/  hcStatus_Active = 0x70;

	int /*ChColor*/  hcEntry_Field   = 0x07;
	int /*ChColor*/  hcEntry_Prompt  = 0x0F;
	int /*ChColor*/  hcEntry_Selection = 0x2F;

	/* Plain */

	int /*ChColor*/  hcPlain_Normal = 0x07;
	int /*ChColor*/  hcPlain_Background = 0x07;
	int /*ChColor*/  hcPlain_Selected = 0x80;
	int /*ChColor*/  hcPlain_Markers = 0x03;
	int /*ChColor*/  hcPlain_Found = 0x40;
	int /*ChColor*/  hcPlain_Keyword = 0x0F;
	int /*ChColor*/  hcPlain_Folds[] = { 0x0A, 0x0A, 0x0A, 0x0A, 0x0A };
	int /*ChColor*/  hcPlain_HilitWord = 0x0D;
	int /*ChColor*/  hcPlain_Bookmark = 0x20;

	/* LIST */
	//int /*ChColor*/  hcList_Border       = 0x03;
	int /*ChColor*/  hcList_Status       = 0x70;
	int /*ChColor*/  hcList_Normal       = 0x07;
	int /*ChColor*/  hcList_Selected     = 0x1F;
	int /*ChColor*/  hcList_Hilited      = 0x0F;
	int /*ChColor*/  hcList_HilitSelect  = 0x1F;
	int /*ChColor*/  hcList_Marked       = 0xB0;
	int /*ChColor*/  hcList_MarkSelect   = 0x1B;
	int /*ChColor*/  hcList_MarkHilit    = 0xB1;
	int /*ChColor*/  hcList_MarkHilitSel = 0x1B;

	int /*ChColor*/  hcScrollBar_Arrows = 0x70;
	int /*ChColor*/  hcScrollBar_Back   = 0x07;
	int /*ChColor*/  hcScrollBar_Fore   = 0x07;

	int /*ChColor*/  hcAsciiChars = 0x07;

	int /*ChColor*/  hcMenu_Background = 0x70;
	int /*ChColor*/  hcMenu_ActiveItem = 0x1F;
	int /*ChColor*/  hcMenu_ActiveChar = 0x1C;
	int /*ChColor*/  hcMenu_NormalItem = 0x70;
	int /*ChColor*/  hcMenu_NormalChar = 0x74;

	int /*ChColor*/  hcChoice_Title      = 0x1F;
	int /*ChColor*/  hcChoice_Param      = 0x1B;
	int /*ChColor*/  hcChoice_Background = 0x17;
	int /*ChColor*/  hcChoice_ActiveItem = 0x20;
	int /*ChColor*/  hcChoice_ActiveChar = 0x2F;
	int /*ChColor*/  hcChoice_NormalItem = 0x1F;
	int /*ChColor*/  hcChoice_NormalChar = 0x1E;
	
	static final int CK_MAXLEN = 32;
	
}
