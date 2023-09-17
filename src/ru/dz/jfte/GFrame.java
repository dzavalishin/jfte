package ru.dz.jfte;

public class GFrame {
    GFrame Prev, Next;
    GView Top, Active;
    GFramePeer Peer;
    String Menu;

	
	
	GFrame(int XSize, int YSize) {
	    Menu = null;
	    if (frames == 0) {
	        frames = Prev = Next = this;
	    } else {
	        Next = frames.Next;
	        Prev = frames;
	        frames.Next.Prev = this;
	        frames.Next = this;
	        frames = this;
	    }
	    Top = Active = 0;
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
	    int dmy;
	    
	    newview.Parent = this;
	    newview.Peer.wX = 0;
	    ConQuerySize(&newview.Peer.wW, &dmy);
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
	    if (Active != 0) {
	        return ConSplitView(Active, view);
	    } else {
	        int W, H;
	        
	        view.Parent = this;
	        view.Prev = view.Next = 0;
	        
	        view.Peer.wX = 0;
	        if (ShowMenuBar)
	            view.Peer.wY = 1;
	        else
	            view.Peer.wY = 0;
	        ConQuerySize(&W, &H);
	        if (ShowMenuBar)
	            H--;
	        if (ShowVScroll)
	            W--;
	        if (ShowHScroll)
	            H--;
	        view.ConSetSize(W, H);
	        InsertView(Top, view);
	        return 0;
	    }
	}

	void Update() {
	    GView v = Active;
	    
	    UpdateMenu();
	    while (v != null) {
	        v.Update();
	        if ((ShowVScroll || ShowHScroll) && (v.Peer.sbVupdate || v.Peer.sbHupdate)) {
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
	    while (v) {
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
	    if (Active == 0) {
	        Active = view;
	        Active.Activate(1);
	    }
	}

	void RemoveView(GView view) {
	    if (!view) return ;
	    
	    if (Active == view)
	        Active.Activate(0);
	    if (view.Next == view) {
	        Top = Active = 0;
	        delete this;
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
	            Active.Activate(1);
	        }
	    }
	}

	void SelectNext(int back) {
	    GView c = Active;
	    
	    if (c == 0 && Top == 0)
	        return;
	    
	    if (FocusCapture != 0)
	        return ;
	    
	    else if (c == 0)
	        c = Active = Top;
	    else
	        if (back) {
	            Active = Active.Prev;
	        } else {
	            Active = Active.Next;
	        }
	    if (c != Active) {
	        if (c)
	            c.Activate(0);
	        if (Active) 
	            Active.Activate(1);
	    }
	}

	int SelectView(GView view) {
	    if (Top == 0)
	        return 0;
	    
	    if (FocusCapture != 0)
	        view = view;
	    
	    if (Active)
	        Active.Activate(0);
	    Active = view;
	    if (Active)
	        Active.Activate(1);
	    return 1;
	}

	void Resize(int width, int height) {
	    GView V;
	    int count = 0;
	    
	    
	    V = Top;
	    while (V) {
	        count++;
	        if (V == Top) break;
	    }
	    if (height < 2 * count + 2 || width < 16) {
	        ::ConSetSize(16, 2 * count + 1);
	        return;
	    }
	    
	    if (!Top)
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
	    NextEvent.What = evCommand;
	    NextEvent.Msg.Command = cmMainMenu;
	    NextEvent.Msg.Param1 = Sub;
	    return 0;
	}

	int SetMenu(String Name) {
	    Menu = Name;
	    return 0;
	}

	void Show() {
	}

	void Activate() {
	    frames = this;
	}
	
}
