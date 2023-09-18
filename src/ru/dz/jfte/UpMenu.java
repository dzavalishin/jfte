package ru.dz.jfte;

public class UpMenu implements ColorDefs, EventDefs, KeyDefs 
{
    UpMenu up = null;
    int id;
    int vert;
    int x, y, w, h;

    static mMenu [] Menus = null;
    
    
    static int GetHOfsItem(int id, int cur) {
        int pos = 2;
        int i, len;
        
        for (i = 0; i < Menus[id].Count; i++) {
            if (i == cur) return pos;
            if (Menus[id].Items[i].Name!=null) {
                len = PCell.CStrLen(Menus[id].Items[i].Name);
                pos += len + 2;
            } else
                pos++;
        }
        return -1;
    }

    static int GetHPosItem(int id, int X) {
        int pos = 1;
        int i, len;
        
        for (i = 0; i < Menus[id].Count; i++) {
            if (Menus[id].Items[i].Name!=null) {
                len = PCell.CStrLen(Menus[id].Items[i].Name);
                if (X >= pos && X <= pos + len + 1) return i;
                pos += len + 2;
            } else 
                pos++;
        }
        return -1;
    }

    static int DrawHMenu(int x, int y, int id, int active) {
        int pos = 1;
        TDrawBuffer B = new TDrawBuffer();
        int i, len;
        int /*TAttr*/ color1, color2;
        int Cols = Console.getWidth(), Rows = Console.getHeigh();
        
        B.MoveChar(0, Cols, ' ', hcMenu_Background, Cols);
        if (id != -1) {
            for (i = 0; i < Menus[id].Count; i++) {
                if (i == active) {
                    color1 = hcMenu_ActiveItem;
                    color2 = hcMenu_ActiveChar;
                } else {
                    color1 = hcMenu_NormalItem;
                    color2 = hcMenu_NormalChar;
                }
                
                if (Menus[id].Items[i].Name!=null) {
                    len = PCell.CStrLen(Menus[id].Items[i].Name);
                    B.MoveChar( pos, Cols, ' ', color1, len + 2);
                    B.MoveCStr( pos + 1, Cols, Menus[id].Items[i].Name, color1, color2, len);
                    pos += len + 2;
                } else {
                    B.MoveChar( pos, Cols, Console.ConGetDrawChar(DCH_V), hcMenu_Background, 1);
                    pos++;
                }
            }
        }
        Console.ConPutBox(x, y, Cols - x, 1, B);
        return 1;
    }

    static int GetVPosItem(int id, int w, int X, int Y) {
        if (Y <= 0 || Y > Menus[id].Count) return -1;
        if (Menus[id].Items[Y - 1].Name == null) return -1;
        if (X <= 0 || X >= w - 1) return -1;
        return Y - 1;
    }

    static int GetVSize(int id, int []X, int []Y) {
        int xsize = 0;
        int len;
        
        Y[0] = Menus[id].Count;
        for (int i = 0; i < Y[0]; i++) {
            len = 0;
            if (Menus[id].Items[i].Name!=null)
                len = PCell.CStrLen(Menus[id].Items[i].Name);
            if (len > xsize)
                xsize = len;
        }
        X[0] = xsize;
        return 0;
    }


    static int DrawVMenu(int x, int y, int id, int active) {
        TDrawBuffer B;
        int i, len;
        int /*TAttr*/ color1, color2;
        int w, h;
        
        if (id == -1) return -1;
        
        GetVSize(id, w, h);
        w += 4;
        h += 2;
        
        B.MoveChar( 0, w, Console.ConGetDrawChar(DCH_H), hcMenu_Background, w);
        B.MoveCh( Console.ConGetDrawChar(DCH_C1), hcMenu_Background, 1);
        MoveCh(B + w - 1, Console.ConGetDrawChar(DCH_C2), hcMenu_Background, 1);
        Console.ConPutBox(x, y, w, 1, B);
        for (i = 0; i < Menus[id].Count; i++) {
            if (i == active) {
                color1 = hcMenu_ActiveItem;
                color2 = hcMenu_ActiveChar;
            } else {
                color1 = hcMenu_NormalItem;
                color2 = hcMenu_NormalChar;
            }
            if (Menus[id].Items[i].Name!=null) {
                String name;
                //char *arg = 0;
                int len2 = 0;
                
                name = Menus[id].Items[i].Name;
                arg = strchr(name, '\t');
                if (arg)
                    *arg++ = 0;

                len = PCell.CStrLen(name);
                if (arg) 
                    len2 = PCell.CStrLen(arg);
                
                B.MoveChar( 0, w, ' ', color1, w);
                B.MoveCh( Console.ConGetDrawChar(DCH_V), hcMenu_Background, 1);
                new PCell( B, w - 1).MoveCh( /*B + w - 1,*/ Console.ConGetDrawChar(DCH_V), hcMenu_Background, 1);

                B.MoveCStr( 2, len + 2, Menus[id].Items[i].Name, color1, color2, len);
                if (arg)
                    B.MoveCStr( w - len2 - 2, w + 4, arg, color1, color2, len2);
                
                if (Menus[id].Items[i].SubMenu != -1) {
                	new PCell( B, w - 2).MoveCh(/*B + w - 2,*/ Console.ConGetDrawChar(DCH_RPTR), color1, 1);
                }
            } else {
                B.MoveChar( 0, w, Console.ConGetDrawChar(DCH_H), hcMenu_Background, w);
                B.MoveCh( Console.ConGetDrawChar(DCH_V), hcMenu_Background, 1);
                MoveCh(B + w - 1, Console.ConGetDrawChar(DCH_V), hcMenu_Background, 1);
            }
            Console.ConPutBox(x, y + i + 1, w, 1, B);
        }
        B.MoveChar( 0, w, Console.ConGetDrawChar(DCH_H), hcMenu_Background, w);
        B.MoveCh( Console.ConGetDrawChar(DCH_C3), hcMenu_Background, 1);
        MoveCh(B + w - 1, Console.ConGetDrawChar(DCH_C4), hcMenu_Background, 1);
        Console.ConPutBox(x, y + Menus[id].Count + 1, w, 1, B);
        return 1;
    }

    static int ExecVertMenu(int x, int y, int id, TEvent E, UpMenu up) {
        int cur = 0;
        int abort;
        int w, h;
        PCell c;
        PCell SaveC = null;
        int SaveX, SaveY, SaveW, SaveH;
        int wasmouse = 0;
        UpMenu here;
        int dovert = 0;
        int rx;
        int Cols = Console.getWidth(), Rows = Console.getHeigh();
        
        //ConQuerySize(&Cols, &Rows);
        
        here.up = up;
        
        if (x < 0) x = 0;
        if (y < 0) y = 0;

        GetVSize(id, w, h);
        w += 4;
        h += 2;
        
        if (w > Cols) w = Cols;
        if (h > Rows) h = Rows;
        
        if (x + w > Cols)
            if (up != null && up.x == 0 && up.y == 0 && up.h == 1) {
                x = Cols - w;
            } else {
                if (up!=null)
                    x = up.x - w + 1;
                else
                    x = x - w + 1;
            }
        if (y + h > Rows)
            if (up!=null)
                y = y - h + 3;
            else {
                y = y - h + 1;
            }
        if (x < 0) x = 0;
        if (y < 0) y = 0;
        
        here.x = x;
        here.y = y;
        here.w = w;
        here.h = h;
        here.id = id;
        here.vert = 1;
        
        c = new PCell(w * h);
        Console.ConGetBox(x, y, w, h, c);
        
        SaveC = c;
        SaveX = x;
        SaveY = y;
        SaveW = w;
        SaveH = h;
        
        if (E.What == evMouseMove || E.What == evMouseDown) {
        }
        if(0!= (E.What & evMouse)) {
            cur = GetVPosItem(id, w, E.X - x, E.Y - y);
            dovert = 0;
            wasmouse = 1;
            E.What = evNone;
        }
        abort = -2;
        while (abort == -2) {
            DrawVMenu(x, y, id, cur);
            if (dovert) {
                if (cur != -1) {
                    if (Menus[id].Items[cur].SubMenu != -1) {
                        rx = ExecVertMenu(x + w - 1, y + cur,
                                          Menus[id].Items[cur].SubMenu, E, &here);
                        if (rx == 1) {
                            abort = 1;
                            continue;
                        } else if (rx == -3) {
                            abort = -3;
                            break;
                        } else
                            abort = -2;

                    }
                }
            }
            Console.ConHideCursor();
            do {
            	E = Console.ConGetEvent(evCommand | evMouseDown | evMouseMove | evMouseUp | evKeyDown | evNotify, -1, 1);
                if( 0!= (E.What & evNotify))
                    GUI.gui.DispatchEvent(frames, frames.Active, E);
            } while( 0!= (E.What & evNotify));
            if( 0!= (E.What & evMouse)) {
                //fprintf(stderr, "Mouse: %d %d %d\n", E.What, E.X, E.Y);
            }
            dovert = 0;
            switch (E.What) {
            case evCommand:
                if (E.Msg.Command == cmResize) abort = -3;
                break;
            case evKeyDown:
                switch (kbCode(E.Key.Code)) {
                case kbPgDn:
                case kbEnd: cur = Menus[id].Count;
                case kbUp: 
                    {
                        int xx = cur;
                        
                        do {
                            cur--;
                            if (cur < 0) cur = Menus[id].Count - 1;
                        } while (cur != xx && Menus[id].Items[cur].Name == null);
                    }
                    break;
                case kbPgUp:
                case kbHome: cur = -1;
                case kbDown: 
                    {
                        int xx = cur;
                        do {
                            cur++;
                            if (cur >= Menus[id].Count) cur = 0;
                        } while (cur != xx && Menus[id].Items[cur].Name == null);
                    }
                    break;
                case kbEsc: abort = -1; break;
                case kbEnter:
                    if (cur != -1) {
                        if (Menus[id].Items[cur].SubMenu < 0) {
                            E.What = evCommand;
                            E.Msg.View = frames.Active;
                            E.Msg.Command = Menus[id].Items[cur].Cmd;
                            abort = 1;
                        } else {
                            dovert = 1;
                        }
                    }
                    break;
                case kbLeft:
                case kbRight:
                    GUI.gui.ConPutEvent(E);
                    abort = -1;
                    break;
                default:
                    if (isAscii(E.Key.Code)) {
                        char cc;
                        int i;
                        
                        cc = (char)(toupper(char(E.Key.Code & 0xFF)));
                        
                        for (i = 0; i < Menus[id].Count; i++) {
                            if (Menus[id].Items[i].Name!=null) {
                                char []o = strchr(Menus[id].Items[i].Name, '&');
                                if (o)
                                    if (toupper(o[1]) == cc) {
                                        cur = i;
                                        if (cur != -1) {
                                            if (Menus[id].Items[cur].SubMenu == -1) {
                                                E.What = evCommand;
                                                E.Msg.View = frames.Active;
                                                E.Msg.Command = Menus[id].Items[cur].Cmd;
                                                abort = 1;
                                            } else {
                                                dovert = 1;
                                            }
                                        }
                                        break;
                                    }
                            }
                        }
                    }
                }
                break;
            case evMouseDown:
                if (E.X >= x && E.Y >= y &&
                    E.X < x + w && E.Y < y + h) 
                {
                    cur = GetVPosItem(id, w, E.X - x, E.Y - y);
                } else {
                    if (up) 
                        gui.ConPutEvent(E);
                    abort = -1;
                }
                wasmouse = 1;
                dovert = 1;
                break;
            case evMouseMove:
                if (E.Buttons)  {
                    dovert = 1;
                    if (E.X >= x && E.Y >= y &&
                        E.X < x + w && E.Y < y + h)
                    {
                        cur = GetVPosItem(id, w, E.X - x, E.Y - y);
                    } else {
                        UpMenu p = up;
                        int first = 1;
                        
                        if (wasmouse) {
                            while (p) {
                                if (E.X >= p.x && E.Y >= p.y &&
                                    E.X < p.x + p.w && E.Y < p.y + p.h)
                                {
                                    if (first == 1) {
                                        if (p.vert) {
                                            int i = GetVPosItem(p.id, p.w, E.X - p.x, E.Y - p.y);
                                            if (i != -1)
                                                if (Menus[p.id].Items[i].SubMenu == id) break;
                                        } else {
                                            int i = GetHPosItem(p.id, E.X);
                                            if (i != -1)
                                                if (Menus[p.id].Items[i].SubMenu == id) break;
                                        }
                                        first = 0;
                                    }
                                    gui.ConPutEvent(E);
                                    abort = -1;
                                    break;
                                }
                                first = 0;
                                p = p.up;
                            }
                            cur = -1;
                        } else
                            cur = -1;
                    }
                }
                break;
            case evMouseUp:
                if (E.X >= x && E.Y >= y &&
                    E.X < x + w && E.Y < y + h)
                {
                    cur = GetVPosItem(id, w, E.X - x, E.Y - y);
                }
                if (cur == -1) {
                    if (up) {
                        UpMenu p = up;
                        cur = 0;
                        if (E.X >= p.x && E.Y >= p.y &&
                            E.X < p.x + p.w && E.Y < p.y + p.h)
                        {
                            if (p.vert) {
                                int i = GetVPosItem(p.id, p.w, E.X - p.x, E.Y - p.y);
                                if (i != -1)
                                    if (Menus[p.id].Items[i].SubMenu == id) break;
                            } else {
                                int i = GetHPosItem(p.id, E.X);
                                if (i != -1)
                                    if (Menus[p.id].Items[i].SubMenu == id) break;
                            }
                            abort = -1;
                        }
                    } else
                        abort = -1;
                    if (E.X >= x && E.Y >= y &&
                        E.X < x + w && E.Y < y + h);
                    else {
                        gui.ConPutEvent(E);
                        abort = -3;
                    }
                } else {
                    if (Menus[id].Items[cur].Name != null &&
                        Menus[id].Items[cur].SubMenu == -1)
                    {
                        E.What = evCommand;
                        E.Msg.View = frames.Active;
                        E.Msg.Command = Menus[id].Items[cur].Cmd;
                        //fprintf(stderr, "Command set = %d %d %d\n", id, cur, Menus[id].Items[cur].Cmd);
                        abort = 1;
                    }
                }
                break;
            }
        }
        if (SaveC) {
            Console.ConPutBox(SaveX, SaveY, SaveW, SaveH, SaveC);
            //free(SaveC);
            SaveC = 0;
        }
        Console.ConShowCursor();
        if (up && abort == -3) return -3;
        return (abort == 1) ? 1 : -1;
    }

    static UpMenu top = new UpMenu(); // { 0, 0, 0, 0, 0, 0, 1 };
    
    static int ExecMainMenu(TEvent E, char sub) 
    {
        int cur = 0;
        int id = GetMenuId(frames.Menu);
        int abort;
        int dovert = 1;
        int rx;
        PCell topline = new PCell(ConMaxCols);
        //int Cols, Rows;
        int Cols = Console.getWidth();
        int Rows = Console.getHeigh();
        
        //ConQuerySize(&Cols, &Rows);
        
        top.x = 0;
        top.y = 0;
        top.h = 1;
        top.w = Cols;
        top.id = id;
        top.vert = 0;
        
        Console.ConGetBox(0, 0, Cols, 1, (PCell) topline);
        
        if (sub != 0) {
            int i;
            
            for (i = 0; i < Menus[id].Count; i++) {
                if (Menus[id].Items[i].Name!=null) {
                    char []o = strchr(Menus[id].Items[i].Name, '&');
                    if (o)
                        if (toupper(o[1]) == toupper(sub)) {
                            cur = i;
                            break;
                        }
                }
            }
        }

        if (E.What == evMouseDown) {
            cur = GetHPosItem(id, E.X);
            dovert = 1;
        }
        abort = -2;
        while (abort == -2) {
            DrawHMenu(0, 0, id, cur);
            if (dovert) {
                if (cur != -1) {
                    if (Menus[id].Items[cur].SubMenu != -1) {
                        rx = ExecVertMenu(GetHOfsItem(id, cur) - 2, 1,
                                          Menus[id].Items[cur].SubMenu, E, &top);
                        if (rx == 1) {
                            abort = 1;
                            continue;
                        } else if (rx == -3) {
                            abort = -1;
                            break;
                        } else
                            abort = -2;
                    }
                }
            }
            Console.ConHideCursor();
            do {
                ConGetEvent(evCommand | evMouseDown | evMouseMove | evMouseUp | evKeyDown | evNotify, &E, -1, 1);
                if (E.What & evNotify)
                    gui.DispatchEvent(frames, frames.Active, E);
            } while (E.What & evNotify);
            dovert = 0;
            switch (E.What) {
            case evCommand:
                if (E.Msg.Command == cmResize) abort = -1;
                break;
            case evKeyDown:
                switch (KeyDefs.kbCode(E.Key.Code)) {
                case kbEnd: cur = Menus[id].Count;
                case kbLeft:
                    dovert = 1;
                    {
                        int x = cur;
                        do {
                            cur--;
                            if (cur < 0) cur = Menus[id].Count - 1;
                        } while (cur != x && Menus[id].Items[cur].Name == 0);
                    }
                    break;
                case kbHome: cur = -1;
                case kbRight:
                    dovert = 1;
                    {
                        int x = cur;
                        do {
                            cur++;
                            if (cur >= Menus[id].Count) cur = 0;
                        } while (cur != x && Menus[id].Items[cur].Name == 0);
                    }
                    break;
                case kbEsc: abort = -1; dovert = 0; break;
                case kbEnter:
                    if (cur != -1) {
                        if (Menus[id].Items[cur].SubMenu == -1) {
                            E.What = evCommand;
                            E.Msg.View = frames.Active;
                            E.Msg.Command = Menus[id].Items[cur].Cmd;
                            abort = 1;
                        } else {
                            dovert = 1;
                        }
                    }
                    break;
                default:
                    if (isAscii(E.Key.Code)) {
                        char cc;
                        int i;
                        
                        cc = char(toupper(char(E.Key.Code & 0xFF)));
                        
                        for (i = 0; i < Menus[id].Count; i++) {
                            if (Menus[id].Items[i].Name) {
                                char *o = strchr(Menus[id].Items[i].Name, '&');
                                if (o)
                                    if (toupper(o[1]) == cc) {
                                        cur = i;
                                        if (cur != -1) {
                                            if (Menus[id].Items[cur].SubMenu == -1) {
                                                E.What = evCommand;
                                                E.Msg.View = frames.Active;
                                                E.Msg.Command = Menus[id].Items[cur].Cmd;
                                                abort = 1;
                                            } else {
                                                dovert = 1;
                                            }
                                        }
                                        break;
                                    }
                            }
                        }
                    }
                    break;
                }
                break;
            case evMouseDown:
                if (E.Y == 0) {
                    int oldcur = cur;
                    cur = GetHPosItem(id, E.X);
                    if (cur == oldcur) {
                        abort = -1;
                    }
                } else {
                    cur = -1;
                    abort = -1;
                }
                dovert = 1;
                break;
            case evMouseMove:
                if (E.Buttons) {
                    if (E.Y == 0)
                        cur = GetHPosItem(id, E.X);
                    else
                        cur = -1;
                    dovert = 1;
                }
                break;
            case evMouseUp:
                if (E.Y == 0)
                    cur = GetHPosItem(id, E.X);
                if (cur == -1)
                    abort = -1;
                else {
                    if (Menus[id].Items[cur].Name != 0 &&
                        Menus[id].Items[cur].SubMenu == -1) 
                    {
                        E.What = evCommand;
                        E.Msg.View = frames.Active;
                        E.Msg.Command = Menus[id].Items[cur].Cmd;
                        abort = 1;
                    }
                }
                break;
            }
        }
        DrawHMenu(0, 0, id, -1);
        ConPutBox(0, 0, Cols, 1, (PCell) topline);
        ConShowCursor();
        return (abort == 1) ? 1 : -1;
    }
    
}