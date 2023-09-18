package ru.dz.jfte;

public class EModel implements ModeDefs 
{
	EModel [] Root;   // root ptr of this list
	EModel Next;    // next model
	EModel Prev;    // prev model
	EView View;     // active view of model

	int ModelNo;


	static final int cfAppend = 1;
	static final int cfNoActivate = 2;



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

	private static int lastid = -1;
	int GetNewModelID(EModel B) {

		if (0 !=Config.ReassignModelIds) lastid = 0;   // 0 is used by buffer list
		while (FindModelID(B, ++lastid) != null) /* */;

		return lastid;
	}

	EModel(int createFlags, EModel [] ARoot) {
		Root = ARoot;

		if (Root!=null) {
			if (Root[0]!=null) {
				if(0 != (createFlags & cfAppend)) {
					Prev = Root[0];
					Next = (Root[0]).Next;
				} else {
					Next = Root[0];
					Prev = (Root[0]).Prev;
				}
				Prev.Next = this;
				Next.Prev = this;
			} else
				Prev = Next = this;

			if (0 ==(createFlags & cfNoActivate))
				Root[0] = this;
		} else
			Prev = Next = this;
		View = null;
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
           if (Root[0] == this)
               Root[0] = Next;
       } else
           Root[0] = 0;
   } */

	void AddView(EView V) {
		RemoveView(V);
		if (V!=null) 
			V.NextView = View;
		View = V;
	}

	void RemoveView(EView V) {
		/* TODO
       EView **X = &View;

       if (V==null) return;
       while (*X) {
           if ((*X) == V) {
		 *X = V.NextView;
               return;
           }
           X = (&(*X).NextView);
       }
		 */
	}

	void SelectView(EView V) {
		RemoveView(V);
		AddView(V);
	}

	EViewPort CreateViewPort(EView V) {
		return null;
	}

	ExResult ExecCommand(ExCommands Command, ExState State) {
		return ExResult.ErFAIL;
	}

	void HandleEvent(TEvent Event) {
	}

	public void Msg(int level, String s, Object... o) {       
		if (View == null)
			return;

		msgbuftmp = String.format(s, o);

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
	void NotifyPipe(int PipeId) { }

	void NotifyDelete(EModel Deleted) {
	}
	void DeleteRelated() {
	}


	void UpdateTitle() {
		String Title[] = {null}; //fte: ";
		String STitle[] = {null}; //"fte: ";
		EView V;

		GetTitle(Title,STitle);

		V = View;
		while (V != null) {
			V.MView.Win.UpdateTitle(Title[0], STitle[0]);
			V = V.NextView;
		}
	}

	int GetStrVar(int var, String [] str) {
		switch (var) {
		case mvCurDirectory:
			return Console.GetDefaultDirectory(this, str);
		}
		return 0;
	}

	int GetIntVar(int var, int [] value) {
		return 0;
	}

}
