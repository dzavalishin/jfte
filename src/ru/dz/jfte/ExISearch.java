package ru.dz.jfte;

public class ExISearch extends ExView implements KeyDefs 
{
	static enum IState { IOk, INoMatch, INoPrev, INoNext }
	static final int  MAXISEARCH = 256; 

    String ISearchStr = "";
    EPoint Orig;
    EPoint [] stack = new EPoint[MAXISEARCH];
    int len = 0;
    int stacklen = 0;
    EBuffer Buffer;
    IState state = IState.INoMatch;
    int Direction = 0;
	

    
    
    

   static String PrevISearch = "";

   ExISearch(EBuffer B) {
       Orig = Buffer.CP; // ?
   }

   @Override
   public void close() {
       if (!ISearchStr.isBlank())
           PrevISearch = ISearchStr;
   }

   /*void Activate(boolean gotfocus) {
       ExView::Activate(gotfocus);
   }*/

   int BeginMacro() { return 1; }

   void HandleEvent(TEvent Event) {
       int Case = BFI(Buffer, BFI_MatchCase) ? 0 : SEARCH_NCASE;
       
       super.HandleEvent(Event);
       switch (Event.What) {
       case evKeyDown:
           SetState(IState.IOk);
           int kcode = ((TKeyEvent)Event).Code;
           switch (KeyDefs.kbCode(kcode)) {
           case kbEsc: 
               Buffer.SetPos(Orig.Col, Orig.Row);
               EndExec(0); 
               break;
           case kbEnter: EndExec(1); break;
           case kbBackSp:
               if (len > 0) {
                   if (stacklen > 0) {
                       stacklen--;
                       if (!Buffer.CenterPos(stack[stacklen].Col, stack[stacklen].Row)) return;
                   }
                   len--;
                   ISearchStr = ISearchStr.substring(0,len);
                   if (len > 0 && !Buffer.FindStr(ISearchStr, len, Case | Direction)) {
                       SetState(IState.INoMatch);
                   }
               } else {
                   if (Buffer.CenterPos(Orig.Col, Orig.Row) == 0) return;
               }
               break;
           case kbUp:
               Buffer.ScrollDown(1);
               break;
           case kbDown:
               Buffer.ScrollUp(1);
               break;
           case kbLeft:
               Buffer.ScrollRight(8);
               break;
           case kbRight:
               Buffer.ScrollLeft(8);
               break;
           case kbPgDn:
               Buffer.MovePageDown();
               break;
           case kbPgUp:
               Buffer.MovePageUp();
               break;
           case kbPgUp | kfCtrl:
               Buffer.MoveFileStart();
               break;
           case kbPgDn | kfCtrl:
               Buffer.MoveFileEnd();
               break;
           case kbHome:
               Buffer.MoveLineStart();
               break;
           case kbEnd:
               Buffer.MoveLineEnd();
               break;
           case kbTab | kfShift:
               Direction = SEARCH_BACK;
               if (len == 0) {
                   ISearchStr = PrevISearch;
                   len = ISearchStr.length();
                   if (len == 0)
                       break;
               }
               if (Buffer.FindStr(ISearchStr, len, Case | Direction | SEARCH_NEXT) == 0) {
                   Buffer.FindStr(ISearchStr, len, Case);
                   SetState(IState.INoPrev);
               }
               break;
           case kbTab:
               Direction = 0;
               if (len == 0) {
                   ISearchStr = PrevISearch;
                   len = ISearchStr.length();
                   if (len == 0)
                       break;
               }
               if (Buffer.FindStr(ISearchStr, len, Case | Direction | SEARCH_NEXT) == 0) {
                   Buffer.FindStr(ISearchStr, len, Case);
                   SetState(IState.INoNext);
               }
               break;
           case 'Q' | kfCtrl:
               //Event.What = evKeyDown;
               //Event.Key.Code = Win.GetChar(null);
               Event = new TKeyEvent(evKeyDown,(int)Win.GetChar(null));
           default:
               if (KeyDefs.isAscii(kcode) && (len < MAXISEARCH)) {
                   char Ch = (char) kcode;
                   
                   stack[stacklen++] = Buffer.CP;
                   
                   ISearchStr += ""+Ch;
                   len++;
                   
                   if (!Buffer.FindStr(ISearchStr, len, Case | Direction)) {
                       SetState(IState.INoMatch);
                       len--;
                       stacklen--;
                       ISearchStr = ISearchStr.substring(0,len);
                       Buffer.FindStr(ISearchStr, len, Case | Direction);
                   } else {
                   }
               }
               break;
           }
       }
   }

   void UpdateView() {
       if (Next!= null) {
           Next.UpdateView();
       }
   }

   void RepaintView() {
       if (Next!= null) {
           Next.RepaintView();
       }
   }

   void UpdateStatus() {
       RepaintStatus();
   }

   void RepaintStatus() {
       TDrawBuffer B = new TDrawBuffer();
       String p;
       int [] W = {0}, H = {0};
       
       ConQuerySize(W, H);
       
       switch (state) {
       case INoMatch: p = " No Match. "; break;
       case INoPrev: p = " No Prev Match. "; break;
       case INoNext: p = " No Next Match. "; break;
       case IOk: default: p = ""; break;
       }
       
       String s = String.format("ISearch [%s]%s", ISearchStr, p);
       B.MoveCh( ' ', 0x17, W);
       B.MoveStr( 0, W[0], s, 0x17, W[0]);
       ConPutBox(0, H[0] - 1, W[0], 1, B);
       ConSetCursorPos(s.length() - 1, H[0] - 1);
       ConShowCursor();
   }

   void SetState(IState s) {
       state = s;
       RepaintView();
   }
    
    
}
