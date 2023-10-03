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

	
	public static boolean SetColor(String ColorV, String Value) {
	    //int Col;
	    //int ColBg, ColFg;
	    //int /*ChColor*/ C;

	    //if (sscanf(Value, "%1X %1X", &ColFg, &ColBg) != 2)	        return 0;

	    String[] sep = Value.split(" ");
	    
	    int ColFg = Integer.parseInt(sep[0],16);
	    int ColBg = Integer.parseInt(sep[0],16);
	    
	    int Col = ColFg | (ColBg << 4);
	    
	    /*
	    C = ChColor(Col);
	    for (unsigned int i = 0; i < NCOLORS; i++) {
	        if (strcmp(ColorV, Colors[i].Name) == 0) {
	            *Colors[i].C = C;
	            return 1;
	        }
	    }
	    return 0;
	    */
	    
	    /* TODO switch(ColorV)
	    
	    switch(ColorV)
	    {
	    case  "Status.Normal":     hcStatus_Normal = Col; break;
	    case  "Status.Active":     hcStatus_Active = Col; break;

	    case  "Message.Normal":    MsgColor[0] = Col; break;
	    case  "Message.Bold":      MsgColor[1] = Col; break;
	    case  "Message.Error":     MsgColor[2] = Col; break;

	    case  "Entry.Field":       hcEntry_Field = Col; break;
	    case  "Entry.Prompt":      hcEntry_Prompt = Col; break;
	    case  "Entry.Selection":   hcEntry_Selection = Col; break;

	    case  "LIST.Status":       hcList_Status = Col; break;
	    case  "LIST.Normal":       hcList_Normal = Col; break;
	    case  "LIST.Selected":     hcList_Selected = Col; break;
	    case  "LIST.Hilited":      hcList_Hilited = Col; break;
	    case  "LIST.HilitSelect":  hcList_HilitSelect = Col; break;
	    case  "LIST.Marked":       hcList_Marked = Col; break;
	    case  "LIST.MarkSelect":   hcList_MarkSelect = Col; break;
	    case  "LIST.MarkHilit":    hcList_MarkHilit = Col; break;
	    case  "LIST.MarkHilitSel": hcList_MarkHilitSel = Col; break;

	    case  "PLAIN.Normal":      hcPlain_Normal = Col; break;
	    case  "PLAIN.Background":  hcPlain_Background = Col; break;
	    case  "PLAIN.Selected":    hcPlain_Selected = Col; break;
	    case  "PLAIN.Markers":     hcPlain_Markers = Col; break;
	    case  "PLAIN.Found":       hcPlain_Found = Col; break;
	    case  "PLAIN.Keyword":     hcPlain_Keyword = Col; break;
	    case  "PLAIN.Folds0":      hcPlain_Folds[0] = Col; break;
	    case  "PLAIN.Folds1":      hcPlain_Folds[1] = Col; break;
	    case  "PLAIN.Folds2":      hcPlain_Folds[2] = Col; break;
	    case  "PLAIN.Folds3":      hcPlain_Folds[3] = Col; break;
	    case  "PLAIN.Folds4":      hcPlain_Folds[4] = Col; break;
	    case  "PLAIN.HilitWord":   hcPlain_HilitWord = Col; break;
	    case  "PLAIN.Bookmark":    hcPlain_Bookmark = Col; break;

	    case  "ScrollBar.Arrows":  hcScrollBar_Arrows = Col; break;
	    case  "ScrollBar.Back":    hcScrollBar_Back = Col; break;
	    case  "ScrollBar.Fore":    hcScrollBar_Fore = Col; break;

	    case  "ASCII.Chars":       hcAsciiChars = Col; break;

	    case  "Menu.Background":   hcMenu_Background = Col; break;
	    case  "Menu.ActiveItem":   hcMenu_ActiveItem = Col; break;
	    case  "Menu.ActiveChar":   hcMenu_ActiveChar = Col; break;
	    case  "Menu.NormalItem":   hcMenu_NormalItem = Col; break;
	    case  "Menu.NormalChar":   hcMenu_NormalChar = Col; break;

	    case  "Choice.Title":      hcChoice_Title = Col; break;
	    case  "Choice.Param":      hcChoice_Param = Col; break;
	    case  "Choice.Background": hcChoice_Background = Col; break;
	    case  "Choice.ActiveItem": hcChoice_ActiveItem = Col; break;
	    case  "Choice.ActiveChar": hcChoice_ActiveChar = Col; break;
	    case  "Choice.NormalItem": hcChoice_NormalItem = Col; break;
	    case  "Choice.NormalChar": hcChoice_NormalChar = Col; break;
	    
	    default:
	    	return false;
	    }
	    */
	    return true;
	}
	
}
