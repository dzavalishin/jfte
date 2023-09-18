package ru.dz.jfte;

public class GFrame implements EventDefs 
{
    GFrame Prev, Next;
    GView Top, Active;
    GFramePeer Peer;
    String Menu;


    static boolean ShowVScroll = true;
    static boolean ShowHScroll = true;
    static boolean ShowMenuBar = true;
    static boolean ShowToolBar = false;
    static boolean HaveGUIDialogs = false; // no gui dialogs in text gui
    
	
	GFrame(int XSize, int YSize) {
	    Menu = null;
	    if (GUI.frames == null) {
	    	GUI.frames = Prev = Next = this;
	    } else {
	        Next = GUI.frames.Next;
	        Prev = GUI.frames;
	        GUI.frames.Next.Prev = this;
	        GUI.frames.Next = this;
	        GUI.frames = this;
	    }
	    Top = Active = null;
	    Peer = new GFramePeer(this, XSize, YSize);
	}

	/* TODO ~GFrame() {
	    if (Peer) {
	        delete Peer;
	        Peer = 0;
	    }
	    if (Next == this) {
	        frames = 0;
//	        printf("No more frames\x7\x7\n");
	    } else {
	        Next.Prev = Prev;
	        Prev.Next = Next;
	        frames = Next;
	    }
	    Next = Prev = 0;
	    if (Menu)
		free(Menu);
	} */

	int ConSetTitle(String Title, String STitle) {
	    return Peer.ConSetTitle(Title, STitle);
	}

	int ConGetTitle(String []Title, String []STitle) {
	    return Peer.ConGetTitle(Title, STitle);
	}

	int ConSetSize(int X, int Y) {
	    return Peer.ConSetSize(X, Y);
	}

	int ConQuerySize(int []X, int []Y) {
	    return Peer.ConQuerySize(X, Y);
	}

	int ConSplitView(GView view, GView newview) {
	    //int dmy;
	    
	    newview.Parent = this;
	    newview.Peer.wX = 0;
	    //Console.ConQuerySize(&newview.Peer.wW, dmy);
	    newview.Peer.wW = Console.getWidth();
	    //dmy = Console.getHeigh();
	    
	    if (ShowVScroll) 
	        newview.Peer.wW--;
	    newview.Peer.wY = view.Peer.wY + view.Peer.wH / 2;
	    newview.Peer.wH = view.Peer.wH - view.Peer.wH / 2;
	    if (ShowHScroll) {
	        newview.Peer.wY++;
	        newview.Peer.wH--;
	    }
	    view.Peer.wH /= 2;
	    view.ConSetSize(view.Peer.wW, view.Peer.wH);
	    newview.ConSetSize(newview.Peer.wW, newview.Peer.wH);
	    InsertView(view, newview);
	    return 0;
	}

	int ConCloseView(GView view) {
	    return 0;
	}

	int ConResizeView(GView view, int DeltaY) {
	    return 0;
	}

	int AddView(GView view) {
	    if (Active != null) {
	        return ConSplitView(Active, view);
	    } else {
	        int [] W = {0}, H = {0};
	        
	        view.Parent = this;
	        view.Prev = view.Next = null;
	        
	        view.Peer.wX = 0;
	        if (ShowMenuBar)
	            view.Peer.wY = 1;
	        else
	            view.Peer.wY = 0;
	        ConQuerySize(W, H);
	        if (ShowMenuBar)
	            H[0]--;
	        if (ShowVScroll)
	            W[0]--;
	        if (ShowHScroll)
	            H[0]--;
	        view.ConSetSize(W[0], H[0]);
	        InsertView(Top, view);
	        return 0;
	    }
	}

	void Update() {
	    GView v = Active;
	    
	    UpdateMenu();
	    while (v != null) {
	        v.Update();
	        if ((ShowVScroll || ShowHScroll) && (v.Peer.sbVupdate != 0 || v.Peer.sbHupdate != 0)) {
	            v.Peer.DrawScrollBar();
	            v.Peer.sbVupdate = 0;
	            v.Peer.sbHupdate = 0;
	        }
	        v = v.Next;
	        if (v == Active) 
	            break;
	    }
	}

	void UpdateMenu() {
	    if (ShowMenuBar)
	        DrawMenuBar();
	}

	void Repaint() {
	    GView v = Active;
	    
	    if (ShowMenuBar)
	        DrawMenuBar();
	    while (v != null) {
	        v.Repaint();
	        if (ShowVScroll || ShowHScroll) {
	            v.Peer.DrawScrollBar();
	            v.Peer.sbVupdate = 0;
	            v.Peer.sbHupdate = 0;
	        }
	        v = v.Next;
	        if (v == Active) 
	            break;
	    }
	}

	void InsertView(GView Prev, GView view) {
	    if (view == null) return ;
	    if (Prev != null) {
	        view.Prev = Prev;
	        view.Next = Prev.Next;
	        Prev.Next = view;
	        view.Next.Prev = view;
	    } else {
	        view.Prev = view.Next = view;
	        Top = view;
	    }
	    if (Active == null) {
	        Active = view;
	        Active.Activate(true);
	    }
	}

	void RemoveView(GView view) {
	    if (view == null) return ;
	    
	    if (Active == view)
	        Active.Activate(false);
	    if (view.Next == view) {
	        Top = Active = null;
	        close();
	    } else {
	        view.Next.Prev = view.Prev;
	        view.Prev.Next = view.Next;
	        
	        if (Top == view) {
	            Top = view.Next;
	            Top.Peer.wY -= view.Peer.wH;
	            Top.ConSetSize(Top.Peer.wW, Top.Peer.wH + view.Peer.wH + (ShowHScroll ? 1 : 0));
	        } else {
	            view.Prev.ConSetSize(view.Prev.Peer.wW,
	                                   view.Prev.Peer.wH + view.Peer.wH + (ShowHScroll ? 1 : 0));
	        }
	        
	        if (Active == view) {
	            Active = view.Prev;
	            Active.Activate(true);
	        }
	    }
	}

	void SelectNext(int back) {
	    GView c = Active;
	    
	    if (c == null && Top == null)
	        return;
	    
	    if (GView.FocusCapture != null)
	        return ;
	    
	    else if (c == null)
	        c = Active = Top;
	    else
	        if (back!=0) {
	            Active = Active.Prev;
	        } else {
	            Active = Active.Next;
	        }
	    if (c != Active) {
	        if (c!=null)
	            c.Activate(false);
	        if (Active!=null) 
	            Active.Activate(true);
	    }
	}

	int SelectView(GView view) {
	    if (Top == null)
	        return 0;
	    
	    if (GView.FocusCapture != null)
	        this.view = view;
	    
	    if (Active!=null)
	        Active.Activate(false);
	    
	    Active = view;
	    
	    if (Active!=null)
	        Active.Activate(true);
	    
	    return 1;
	}

	void Resize(int width, int height) {
	    GView V;
	    int count = 0;
	    
	    
	    V = Top;
	    while (V!=null) {
	        count++;
	        if (V == Top) break;
	    }
	    if (height < 2 * count + 2 || width < 16) {
	        Console.ConSetSize(16, 2 * count + 1);
	        return;
	    }
	    
	    if (Top==null)
	        return;
	    
	    if (ShowVScroll)
	        width--;
	    if (ShowHScroll)
	        height--;
	    
//	    fprintf(stderr, "Resize: %d %d\n", width, height);
	    
	    V = Top.Prev;
	    
	    while (V != Top) {
	        int h, y;
	        
	        h = V.Peer.wH;
	        y = V.Peer.wY;
	        
	        if (y >= height - 2) {
	            y = height - 2;
	        }
	        if (y + h != height) {
	            h = height - y;
	        }
	        V.Peer.wY = y;
	        V.ConSetSize(width, h);
	        height = y;
	        V = V.Prev;
	    }
	    if (ShowMenuBar)
	        height--;
	    Top.ConSetSize(width, height);
	    Repaint();
	    //  fprintf(stderr, "Resize: %d %d Done\n", width, height);
	}

	int ExecMainMenu(char Sub) {
		GUI.NextEvent = new TMsgEvent(evCommand, cmMainMenu, Sub);
	    //GUI.NextEvent.What = evCommand;
	    //GUI.NextEvent.Msg.Command = cmMainMenu;
	    //GUI.NextEvent.Msg.Param1 = Sub;
	    return 0;
	}

	int SetMenu(String Name) {
	    Menu = Name;
	    return 0;
	}

	void Show() {
	}

	void Activate() {
		GUI.frames = this;
	}

	boolean isLastFrame() {
	    return (this == Next && GUI.frames == this);
	}

	void DrawMenuBar() {
	    int id = UpMenu.GetMenuId(Menu);

	    UpMenu.DrawHMenu(0, 0, id, -1);
	}
	
}
