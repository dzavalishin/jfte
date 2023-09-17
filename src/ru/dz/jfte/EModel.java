package ru.dz.jfte;

public class EModel {
    EModel [] Root;   // root ptr of this list
    EModel Next;    // next model
    EModel Prev;    // prev model
    EView View;     // active view of model
    
    int ModelNo;



   static EModel ActiveModel = null;
   String msgbuftmp = "";

   EModel FindModelID(EModel Model, int ID) {
       EModel M = Model;
       int No = ID;

       while (M != null) {
           if (M.ModelNo == No)
               return M;
           M = M.Next;
           if (M == Model)
               break;
       }
       return null;
   }

   int GetNewModelID(EModel B) {
       static int lastid = -1;

       if (ReassignModelIds) lastid = 0;   // 0 is used by buffer list
       while (FindModelID(B, ++lastid) != 0) /* */;

       return lastid;
   }

   EModel(int createFlags, EModel [] ARoot) {
       Root = ARoot;

       if (Root) {
           if (*Root) {
               if (createFlags & cfAppend) {
                   Prev = *Root;
                   Next = (*Root).Next;
               } else {
                   Next = *Root;
                   Prev = (*Root).Prev;
               }
               Prev.Next = this;
               Next.Prev = this;
           } else
               Prev = Next = this;

           if (!(createFlags & cfNoActivate))
               *Root = this;
       } else
           Prev = Next = this;
       View = 0;
       ModelNo = -1;
       ModelNo = GetNewModelID(this);
   }

   /* TODO destr
   ~EModel() {
       EModel *D = this;
       
       while (D) {
           D.NotifyDelete(this);
           D = D.Next;
           if (D == this)
               break;
       }
       
       if (Next != this) {
           Prev.Next = Next;
           Next.Prev = Prev;
           if (*Root == this)
               *Root = Next;
       } else
           *Root = 0;
   } */

   void AddView(EView V) {
       RemoveView(V);
       if (V) 
           V.NextView = View;
       View = V;
   }

   void RemoveView(EView V) {
       EView **X = &View;
       
       if (!V) return;
       while (*X) {
           if ((*X) == V) {
               *X = V.NextView;
               return;
           }
           X = (&(*X).NextView);
       }
   }

   void SelectView(EView V) {
       RemoveView(V);
       AddView(V);
   }

   EViewPort CreateViewPort(EView V) {
       return null;
   }

   int ExecCommand(int Command, ExState State) {
       return ErFAIL;
   }

   void HandleEvent(TEvent &/*Event*/) {
   }

   void Msg(int level, const char *s, ...) {
       va_list ap;
       
       if (View == 0)
           return;
       
       va_start(ap, s);
       vsprintf(msgbuftmp, s, ap);
       va_end(ap);
       
       if (level != S_BUSY)
           View.SetMsg(msgbuftmp);
   }

   int CanQuit() {
       return 1;
   }

   int ConfQuit(GxView  V, int multiFile) {
       return 1;
   }

   int GetContext() { return CONTEXT_NONE; }
   EEventMap GetEventMap() { return null; }
   int BeginMacro() { return 1; }
   String GetName() { return null; }
   String GetPath() { return null; }
   String GetInfo() { return null; }
   void GetTitle(String [] ATitle, String [] ASTitle) { ATitle[0] = null; ASTitle[0] = null; }
   void NotifyPipe(int /*PipeId*/) { }

   void NotifyDelete(EModel Deleted) {
   }
   void DeleteRelated() {
   }


   void UpdateTitle() {
       char Title[256] = ""; //fte: ";
       char STitle[256] = ""; //"fte: ";
       EView V;
       
       GetTitle((char *)(Title + 0), sizeof(Title) - 0,
                (char *)(STitle + 0), sizeof(STitle) - 0);

       V = View;
       while (V) {
           V.MView.Win.UpdateTitle(Title, STitle);
           V = V.NextView;
       }
   }

   int GetStrVar(int var, String [] str, int buflen) {
       switch (var) {
       case mvCurDirectory:
           return GetDefaultDirectory(this, str, buflen);
       }
       return 0;
   }

   int GetIntVar(int var, int [] value) {
       return 0;
   }
    
}
