package ru.dz.jfte;

import java.io.IOException;
import java.util.Arrays;

public class UpMenu implements ColorDefs, EventDefs, KeyDefs 
{
	UpMenu up = null;
	int id;
	int vert;
	int x, y, w, h;

	static mMenu [] Menus = new mMenu [0];
	static int MenuCount = 0;


	static int GetHOfsItem(int id, int cur) {
		int pos = 2;
		int i, len;

		for (i = 0; i < Menus[id].Items.size(); i++) {
			if (i == cur) return pos;

			String s = Menus[id].Items.get(i).Name;

			if (s!=null) {
				len = PCell.CStrLen(s);
				pos += len + 2;
			} else
				pos++;
		}
		return -1;
	}

	static int GetHPosItem(int id, int X) {
		int pos = 1;
		int i, len;

		for (i = 0; i < Menus[id].Items.size(); i++) 
		{
			String s = Menus[id].Items.get(i).Name;
			if (s!=null) {
				len = PCell.CStrLen(s);
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
			for (i = 0; i < Menus[id].Items.size(); i++) {
				if (i == active) {
					color1 = hcMenu_ActiveItem;
					color2 = hcMenu_ActiveChar;
				} else {
					color1 = hcMenu_NormalItem;
					color2 = hcMenu_NormalChar;
				}

				String name = Menus[id].Items.get(i).Name;

				if (name!=null) {
					len = PCell.CStrLen(name);
					B.MoveChar( pos, Cols, ' ', color1, len + 2);
					B.MoveCStr( pos + 1, Cols, name, color1, color2, len);
					pos += len + 2;
				} else {
					B.MoveChar( pos, Cols, Console.ConGetDrawChar(DCH_V), hcMenu_Background, 1);
					pos++;
				}
			}
		}
		//System.out.println( B.toString() );
		Console.ConPutBox(x, y, Cols - x, 1, B);
		return 1;
	}

	static int GetVPosItem(int id, int w, int X, int Y) {
		if (Y <= 0 || Y > Menus[id].Items.size()) return -1;
		if (Menus[id].Items.get(Y - 1).Name == null) return -1;
		if (X <= 0 || X >= w - 1) return -1;
		return Y - 1;
	}

	static int GetVSize(int id, int []X, int []Y) {
		int xsize = 0;
		int len;

		Y[0] = Menus[id].Items.size();
		for (int i = 0; i < Y[0]; i++) {
			len = 0;
			if (Menus[id].Items.get(i).Name!=null)
				len = PCell.CStrLen(Menus[id].Items.get(i).Name);
			if (len > xsize)
				xsize = len;
		}
		X[0] = xsize;
		return 0;
	}


	static int DrawVMenu(int x, int y, int id, int active) 
	{
		TDrawBuffer B = new TDrawBuffer();
		//int i;
		int /*TAttr*/ color1, color2;
		int w, h;

		if (id == -1) return -1;

		{
			int [] wp = {0}, hp = {0};
			GetVSize(id, wp, hp);
			w = wp[0];
			h = hp[0];
		}

		w += 4;
		h += 2;

		B.MoveChar( 0, w, Console.ConGetDrawChar(DCH_H), hcMenu_Background, w);
		B.MoveCh( Console.ConGetDrawChar(DCH_C1), hcMenu_Background, 1);
		new PCell(B,w - 1).MoveCh(/*B + w - 1,*/ Console.ConGetDrawChar(DCH_C2), hcMenu_Background, 1);
		Console.ConPutBox(x, y, w, 1, B);

		for (int i = 0; i < Menus[id].Items.size(); i++) 
		{
			if (i == active) {
				color1 = hcMenu_ActiveItem;
				color2 = hcMenu_ActiveChar;
			} else {
				color1 = hcMenu_NormalItem;
				color2 = hcMenu_NormalChar;
			}
			if (Menus[id].Items.get(i).Name!=null) {
				//String name;
				//char *arg = 0;
				int len2 = 0;

				String mname = Menus[id].Items.get(i).Name;
				int tabPos = mname.indexOf('\t');
				/*
				arg = strchr(name, '\t');
				if (arg)					*arg++ = 0;

				len = PCell.CStrLen(name);
				if (arg)					len2 = PCell.CStrLen(arg);
				 */
				int len = mname.length();
				String arg = "";

				if(tabPos >= 0)
				{
					len = tabPos;
					arg = mname.substring(tabPos+1);
					len2 = arg.length();
				}

				B.MoveChar( 0, w, ' ', color1, w);
				B.MoveCh( Console.ConGetDrawChar(DCH_V), hcMenu_Background, 1);
				new PCell( B, w - 1).MoveCh( /*B + w - 1,*/ Console.ConGetDrawChar(DCH_V), hcMenu_Background, 1);

				B.MoveCStr( 2, len + 2, Menus[id].Items.get(i).Name, color1, color2, len);
				if (!arg.isEmpty())
					B.MoveCStr( w - len2 - 2, w + 4, arg, color1, color2, len2);

				if (Menus[id].Items.get(i).SubMenu != -1) {
					new PCell( B, w - 2).MoveCh(/*B + w - 2,*/ Console.ConGetDrawChar(DCH_RPTR), color1, 1);
				}
			} else {
				B.MoveChar( 0, w, Console.ConGetDrawChar(DCH_H), hcMenu_Background, w);
				B.MoveCh( Console.ConGetDrawChar(DCH_V), hcMenu_Background, 1);
				new PCell( B, w - 1).MoveCh(/*B + w - 1,*/ Console.ConGetDrawChar(DCH_V), hcMenu_Background, 1);
			}
			Console.ConPutBox(x, y + i + 1, w, 1, B);
		}
		B.MoveChar( 0, w, Console.ConGetDrawChar(DCH_H), hcMenu_Background, w);
		B.MoveCh( Console.ConGetDrawChar(DCH_C3), hcMenu_Background, 1);
		new PCell( B, w - 1).MoveCh(/*B + w - 1,*/ Console.ConGetDrawChar(DCH_C4), hcMenu_Background, 1);
		Console.ConPutBox(x, y + Menus[id].Items.size() + 1, w, 1, B);
		return 1;
	}

	static int ExecVertMenu(int x, int y, int id, TEvent E, UpMenu up) throws IOException {
		int cur = 0;
		int abort;
		int w, h;
		PCell c;
		PCell SaveC = null;
		int SaveX, SaveY, SaveW, SaveH;
		boolean wasmouse = false;
		UpMenu here = new UpMenu();
		boolean dovert = false;
		int rx;
		int Cols = Console.getWidth(), Rows = Console.getHeigh();

		//ConQuerySize(&Cols, &Rows);

		here.up = up;

		if (x < 0) x = 0;
		if (y < 0) y = 0;

		//GetVSize(id, w, h);
		{
			int [] wp = {0}, hp = {0};
			GetVSize(id, wp, hp);
			w = wp[0];
			h = hp[0];
		}
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
			TMouseEvent mE = (TMouseEvent) E;

			cur = GetVPosItem(id, w, mE.X - x, mE.Y - y);
			dovert = false;
			wasmouse = true;
			E.What = evNone;
		}
		abort = -2;
		while (abort == -2) {
			DrawVMenu(x, y, id, cur);
			if (dovert) {
				if (cur != -1) {
					if (Menus[id].Items.get(cur).SubMenu != -1) {
						rx = ExecVertMenu(x + w - 1, y + cur,
								Menus[id].Items.get(cur).SubMenu, E, here);
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
				E = Console.ConGetEvent(evCommand | evMouseDown | evMouseMove | evMouseUp | evKeyDown | evNotify, -1, true);
				if( E != null &&  0 != (E.What & evNotify))
					GUI.gui.DispatchEvent(GUI.frames, GUI.frames.Active, E);
			} while( 0!= (E.What & evNotify));
			if( 0!= (E.What & evMouse)) {
				//fprintf(stderr, "Mouse: %d %d %d\n", E.What, mE.X, mE.Y);
			}
			dovert = false;
			switch (E.What) {
			case evCommand:
				if (((TMsgEvent)E).Command == cmResize) abort = -3;
				break;
			case evKeyDown:
				switch (KeyDefs.kbCode(((TKeyEvent)E).Code)) {
				case kbPgDn:
				case kbEnd: cur = Menus[id].Items.size();
				case kbUp: 
				{
					int xx = cur;

					do {
						cur--;
						if (cur < 0) cur = Menus[id].Items.size() - 1;
					} while (cur != xx && Menus[id].Items.get(cur).Name == null);
				}
				break;
				case kbPgUp:
				case kbHome: cur = -1;
				case kbDown: 
				{
					int xx = cur;
					do {
						cur++;
						if (cur >= Menus[id].Items.size()) cur = 0;
					} while (cur != xx && Menus[id].Items.get(cur).Name == null);
				}
				break;
				case kbEsc: abort = -1; break;
				case kbEnter:
					if (cur != -1) {
						if (Menus[id].Items.get(cur).SubMenu < 0) {
							//TMsgEvent ne = new TMsgEvent(evCommand);
							//E.What = evCommand;
							//ne.View = GUI.frames.Active;
							//ne.Command = Menus[id].Items.get(cur).Cmd;
							//E = ne;
							E = new TMsgEvent(evCommand, GUI.frames.Active, Menus[id].Items.get(cur).Cmd);
							// dz - right?
							//GUI.gui.ConPutEvent(E);
							E.dispatch();
							abort = 1;
						} else {
							dovert = true;
						}
					}
					break;
				case kbLeft:
				case kbRight:
					GUI.gui.ConPutEvent(E);
					abort = -1;
					break;
				default:
					if (KeyDefs.isAscii(((TKeyEvent)E).Code)) {
						char cc;
						int i;

						cc = Character.toUpperCase((char)(((TKeyEvent)E).Code & 0xFF));

						for (i = 0; i < Menus[id].Items.size(); i++) 
						{
							mItem item = Menus[id].Items.get(i);
							if (item.Name!=null) 
							{
								int amppos = item.Name.indexOf('&');
								char ch = item.Name.charAt(amppos+1);
								if(Character.toUpperCase(ch) == cc )
								{
									cur = i;
									if (cur != -1) {
										if (Menus[id].Items.get(cur).SubMenu == -1) {
											E = new TMsgEvent(evCommand, GUI.frames.Active, Menus[id].Items.get(cur).Cmd);
											E.dispatch();
											abort = 1;
										} else {
											dovert = true;
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
			{
				TMouseEvent mE = (TMouseEvent) E;

				if (mE.X >= x && mE.Y >= y &&
						mE.X < x + w && mE.Y < y + h) 
				{
					cur = GetVPosItem(id, w, mE.X - x, mE.Y - y);
				} else {
					if (up!=null) 
						GUI.gui.ConPutEvent(E);
					abort = -1;
				}
				wasmouse = true;
				dovert = true;
			}
			break;

			case evMouseMove:
			{
				TMouseEvent mE = (TMouseEvent) E;
				if (mE.Buttons!=0)  {
					dovert = true;
					if (mE.X >= x && mE.Y >= y &&
							mE.X < x + w && mE.Y < y + h)
					{
						cur = GetVPosItem(id, w, mE.X - x, mE.Y - y);
					} else {
						UpMenu p = up;
						int first = 1;

						if (wasmouse) {
							while (p!=null) {
								if (mE.X >= p.x && mE.Y >= p.y &&
										mE.X < p.x + p.w && mE.Y < p.y + p.h)
								{
									if (first == 1) {
										if (p.vert!=0) {
											int i = GetVPosItem(p.id, p.w, mE.X - p.x, mE.Y - p.y);
											if (i != -1)
												if (Menus[p.id].Items.get(i).SubMenu == id) break;
										} else {
											int i = GetHPosItem(p.id, mE.X);
											if (i != -1)
												if (Menus[p.id].Items.get(i).SubMenu == id) break;
										}
										first = 0;
									}
									GUI.gui.ConPutEvent(E);
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
			}
			break;

			case evMouseUp:
			{
				TMouseEvent mE = (TMouseEvent) E;

				if (mE.X >= x && mE.Y >= y &&
						mE.X < x + w && mE.Y < y + h)
				{
					cur = GetVPosItem(id, w, mE.X - x, mE.Y - y);
				}
				if (cur == -1) {
					if (up != null) {
						UpMenu p = up;
						cur = 0;
						if (mE.X >= p.x && mE.Y >= p.y &&
								mE.X < p.x + p.w && mE.Y < p.y + p.h)
						{
							if (p.vert!=0) {
								int i = GetVPosItem(p.id, p.w, mE.X - p.x, mE.Y - p.y);
								if (i != -1)
									if (Menus[p.id].Items.get(i).SubMenu == id) break;
							} else {
								int i = GetHPosItem(p.id, mE.X);
								if (i != -1)
									if (Menus[p.id].Items.get(i).SubMenu == id) break;
							}
							abort = -1;
						}
					} else
						abort = -1;
					if (mE.X >= x && mE.Y >= y &&
							mE.X < x + w && mE.Y < y + h);
					else {
						GUI.gui.ConPutEvent(E);
						abort = -3;
					}
				} else {
					if (Menus[id].Items.get(cur).Name != null &&
							Menus[id].Items.get(cur).SubMenu == -1)
					{
						new TMsgEvent(evCommand, GUI.frames.Active, Menus[id].Items.get(cur).Cmd).dispatch();
						abort = 1;
					}
				}
			}
			break;
			}
		}
		if (SaveC!= null) {
			Console.ConPutBox(SaveX, SaveY, SaveW, SaveH, SaveC);
			SaveC = null;
		}
		Console.ConShowCursor();
		if (up!=null && abort == -3) return -3;
		return (abort == 1) ? 1 : -1;
	}

	static UpMenu top = new UpMenu(); // { 0, 0, 0, 0, 0, 0, 1 };

	static int ExecMainMenu(TEvent E, char sub) throws IOException 
	{
		int cur = 0;
		int id = GetMenuId(GUI.frames.Menu);
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

			for (i = 0; i < Menus[id].Items.size(); i++) {
				if (Menus[id].Items.get(i).Name!=null) 
				{
					int amppos = Menus[id].Items.get(i).Name.indexOf('&');
					if(amppos >= 0)
					{
						char c = Menus[id].Items.get(i).Name.charAt(amppos+1);
						if( Character.toUpperCase(c) == Character.toUpperCase(sub) )
						{
							cur = i;
							break;
						}
					}
					/*
					char []o = strchr(Menus[id].Items.get(i).Name, '&');
					if (o)
						if (Character.toUpperCase(o[1]) == Character.toUpperCase(sub)) {
							cur = i;
							break;
						}
					 */
				}
			}
		}

		if (E.What == evMouseDown) {
			TMouseEvent mE = (TMouseEvent) E;
			cur = GetHPosItem(id, mE.X);
			dovert = 1;
		}
		abort = -2;
		while (abort == -2) {
			DrawHMenu(0, 0, id, cur);
			if (dovert!=0) {
				if (cur != -1) {
					if (Menus[id].Items.get(cur).SubMenu != -1) {
						rx = ExecVertMenu(GetHOfsItem(id, cur) - 2, 1,
								Menus[id].Items.get(cur).SubMenu, E, top);
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
				E = Console.ConGetEvent(evCommand | evMouseDown | evMouseMove | evMouseUp | evKeyDown | evNotify, -1, true);
				if(E != null && 0 != (E.What & evNotify))
					GUI.gui.DispatchEvent(GUI.frames, GUI.frames.Active, E);
			} while(0 != (E.What & evNotify));
			dovert = 0;
			switch (E.What) {
			case evCommand:
				if (((TMsgEvent)E).Command == cmResize) abort = -1;
				break;
			case evKeyDown:
				switch (KeyDefs.kbCode(((TKeyEvent)E).Code)) {
				case kbEnd: cur = Menus[id].Items.size();
				case kbLeft:
					dovert = 1;
					{
						int x = cur;
						do {
							cur--;
							if (cur < 0) cur = Menus[id].Items.size() - 1;
						} while (cur != x && Menus[id].Items.get(cur).Name == null);
					}
					break;
				case kbHome: cur = -1;
				case kbRight:
					dovert = 1;
					{
						int x = cur;
						do {
							cur++;
							if (cur >= Menus[id].Items.size()) cur = 0;
						} while (cur != x && Menus[id].Items.get(cur).Name == null);
					}
					break;
				case kbEsc: abort = -1; dovert = 0; break;
				case kbEnter:
					if (cur != -1) {
						if (Menus[id].Items.get(cur).SubMenu == -1) {
							//E.What = evCommand;
							//E.Msg.View = GUI.frames.Active;
							//E.Msg.Command = Menus[id].Items.get(cur).Cmd;

							E = new TMsgEvent(evCommand, GUI.frames.Active, Menus[id].Items.get(cur).Cmd);
							E.dispatch();

							abort = 1;
						} else {
							dovert = 1;
						}
					}
					break;
				default:
					if (KeyDefs.isAscii(((TKeyEvent)E).Code)) {
						char cc;
						int i;

						cc = Character.toUpperCase((char)(((TKeyEvent)E).Code & 0xFF));

						for (i = 0; i < Menus[id].Items.size(); i++) {
							mItem item = Menus[id].Items.get(i);
							if (item.Name != null) 
							{
								int amppos = item.Name.indexOf('&');
								char c = item.Name.charAt(amppos+1);
								if(Character.toUpperCase(c) == cc )
								{
									cur = i;
									if (cur != -1) {
										if (Menus[id].Items.get(cur).SubMenu == -1) {
											E = new TMsgEvent(evCommand, GUI.frames.Active, Menus[id].Items.get(cur).Cmd);
											E.dispatch();
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
			{
				TMouseEvent mE = (TMouseEvent) E;

				if (mE.Y == 0) {
					int oldcur = cur;
					cur = GetHPosItem(id, mE.X);
					if (cur == oldcur) {
						abort = -1;
					}
				} else {
					cur = -1;
					abort = -1;
				}
				dovert = 1;
			}
			break;

			case evMouseMove:
			{
				TMouseEvent mE = (TMouseEvent) E;

				if (mE.Buttons!=0) {
					if (mE.Y == 0)
						cur = GetHPosItem(id, mE.X);
					else
						cur = -1;
					dovert = 1;
				}
			}
			break;
			case evMouseUp:
			{
				TMouseEvent mE = (TMouseEvent) E;

				if (mE.Y == 0)
					cur = GetHPosItem(id, mE.X);
				if (cur == -1)
					abort = -1;
				else {
					if (Menus[id].Items.get(cur).Name != null &&
							Menus[id].Items.get(cur).SubMenu == -1) 
					{
						/*
						TMsgEvent me = new TMsgEvent(evCommand);
						//E.What = evCommand;
						//E.Msg.View = GUI.frames.Active;
						//E.Msg.Command = Menus[id].Items.get(cur).Cmd;
						me.View = GUI.frames.Active;
						me.Command = Menus[id].Items.get(cur).Cmd;

						E = me;
						 */
						E = new TMsgEvent(evCommand, GUI.frames.Active, Menus[id].Items.get(cur).Cmd);
						E.dispatch();

						abort = 1;
					}
				}
			}
			break;
			}
		}
		DrawHMenu(0, 0, id, -1);
		Console.ConPutBox(0, 0, Cols, 1, (PCell) topline);
		Console.ConShowCursor();
		return (abort == 1) ? 1 : -1;
	}











	static int NewMenu(String Name) 
	{
		//Menus = (mMenu *) realloc((void *) Menus, sizeof(mMenu) * (MenuCount + 1));		
		Menus = Arrays.copyOf(Menus, MenuCount + 1);

		int n = MenuCount;

		Menus[n] = new mMenu();

		Menus[n].Name = Name;
		//Menus[n].Count = 0;
		//Menus[n].Items = null;

		MenuCount++;
		return n;
	}

	static int NewItem(int menu, String Name) 
	{
		assert (menu < MenuCount);

		//Menus[menu].Items = (mItem *) realloc(Menus[menu].Items,				sizeof(mItem) * (Menus[menu].Count + 1));
		Menus = Arrays.copyOf(Menus, MenuCount + 1);

		//int n = Menus[menu].Count;

		//if( Menus[menu].Items[n] == null ) 
		mItem i = new mItem();

		i.SubMenu = -1;
		i.Name = Name;
		i.Arg = null;
		i.Cmd = -1;

		/*
		Menus[menu].Items[n].SubMenu = -1;
		Menus[menu].Items[n].Name = Name;
		Menus[menu].Items[n].Arg = null;
		Menus[menu].Items[n].Cmd = -1;
		 */

		Menus[menu].Items.add(i);

		//Menus[menu].Count++;
		return Menus[menu].Items.size() - 1;//n;
	}

	static int NewSubMenu(int menu, String Name, int submenu, int Type) {
		assert (menu < MenuCount);

		//Menus[menu].Items = (mItem *) realloc(Menus[menu].Items,				sizeof(mItem) * (Menus[menu].Count + 1));
		Menus = Arrays.copyOf(Menus, MenuCount + 1);
		int n = Menus[menu].Items.size();

		mItem i = new mItem();

		i.SubMenu = submenu;
		i.Name = Name;
		i.Arg = null;
		i.Cmd = Type;

		Menus[menu].Items.add(i);
		return n;
	}

	static int GetMenuId(String Name) 
	{
		for (int i = 0; i < MenuCount; i++)
			if(Menus[i].Name.equals(Name))
				return i;
		return -1;
	}



}
