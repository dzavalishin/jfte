package ru.dz.jfte;

public class ExComplete extends ExView 
{

	EPoint Orig;
	EBuffer Buffer;
	int WordsLast;
	String []Words;
	String WordBegin;
	String WordContinue;
	int WordPos;
	int WordFixed;
	int WordFixedCount;

	static int CheckASCII(int c) {
		return ((c < 256)
				&& (isalnum(c) || (c == '_') || (c == '.'))) ? 1 : 0;
	}



	static final String STRCOMPLETE = "Complete Word: [";
	static final String STRNOCOMPLETE = "No word for completition...";

	/*
	static int CmpStr(const void *p1, const void *p2) {
		//printf("%s  %s  %d\n", *(char **)p1, *(char **)p2,
		//	   strcoll(*(char **)p1, *(char **)p2));
		return strcoll(*(char **)p1, *(char **)p2);
	} */

	/**
	 * Create Sorted list of possible word extensions
	 */
	ExComplete(EBuffer B)
	{
		Buffer = B;
		Orig = Buffer.CP;
		WordBegin = null;
		WordFixed = WordPos = WordsLast = 0;
		Words = new String[MAXCOMPLETEWORDS + 2];
		if (Words != null)
			RefreshComplete();
	}



	/*void Activate(boolean gotfocus)
    {
        ExView::Activate(gotfocus);
    }*/

	int BeginMacro()
	{
		return 1;
	}

	void HandleEvent(TEvent Event)
	{
		long kb = kbCode(Event.Key.Code);
		boolean DoQuit = false;
		int i = 0;

		if (WordsLast < 2) {
			if ((WordsLast == 1) && (kb != kbEsc)) {
				DoQuit = 1;
			} else {
				EndExec(0);
				Event.What = evNone;
			}
		} else if (Event.What == evKeyDown) {
			switch(kb) {
			case kbPgUp:
			case kbLeft:
				// if there would not be locale sort, we could check only
				// the next string, but with `locale sort` this is impossible!!
				// this loop is little inefficient but it's quite short & nice
				for (i = WordPos; i-- > 0;)
					if (strncmp(Words[WordPos], Words[i], WordFixed) == 0) {
						WordPos = i;
						break;
					}
				Event.What = evNone;
				break;
			case kbPgDn:
			case kbRight:
				for(i = WordPos; i++ < WordsLast - 1;)
					if (strncmp(Words[WordPos], Words[i], WordFixed) == 0) {
						WordPos = i;
						break;
					}
				Event.What = evNone;
				break;
			case kbHome:
				for (i = 0; i < WordPos; i++)
					if (strncmp(Words[WordPos], Words[i], WordFixed) == 0)
						WordPos = i;
				Event.What = evNone;
				break;
			case kbEnd:
				for (i = WordsLast - 1; i > WordPos; i--)
					if (strncmp(Words[WordPos], Words[i], WordFixed) == 0)
						WordPos = i;
				Event.What = evNone;
				break;
			case kbTab:
				while (WordPos < WordsLast - 1) {
					WordPos++;
					if (strncmp(Words[WordPos], Words[WordPos - 1],
							WordFixed + 1))
						break;
				}
				Event.What = evNone;
				break;
			case kbTab | kfShift:
				while (WordPos > 0) {
					WordPos--;
					if (strncmp(Words[WordPos], Words[WordPos + 1],
							WordFixed + 1))
						break;
				}
				Event.What = evNone;
				break;
			case kbIns:
			case kbUp:
				FixedUpdate(1);
				Event.What = evNone;
				break;
			case kbBackSp:
			case kbDel:
			case kbDown:
				FixedUpdate(-1);
				Event.What = evNone;
				break;
			case kbEsc:
				EndExec(0);
				Event.What = evNone;
				break;
			case kbEnter:
			case kbSpace:
			case kbTab | kfCtrl:
				DoQuit = 1;
				break;
			default:
				if (CheckASCII(Event.Key.Code&~kfShift)) {
					//char *s = new char[WordFixed + 2];
					if (s != null) {
						if (WordFixed > 0)
							strncpy(s, Words[WordPos], WordFixed);
						s[WordFixed] = (char)(Event.Key.Code & 0xFF);
						s[WordFixed + 1] = 0;
						for (int i = 0; i < WordsLast; i++)
							if (strncmp(s, Words[i], WordFixed + 1) == 0) {
								WordPos = i;
								if (WordFixedCount == 1)
									DoQuit = 1;
								else
									FixedUpdate(1);
								break;
							}
						//delete s;
					}
					Event.What = evNone;
				}
				break;
			}
		}

		if (DoQuit) {
			int rc = 0;
			int l = Words[WordPos].length();

			if (Buffer.InsText(Buffer.VToR(Orig.Row), Orig.Col, l, Words[WordPos], 1)
					&& Buffer.SetPos(Orig.Col + l, Orig.Row)) {
				Buffer.Draw(Buffer.VToR(Orig.Row), Buffer.VToR(Orig.Row));
				rc = 1;
			}
			EndExec(rc);
			Event.What = evNone;
		}

	}

	void UpdateView()
	{
		if (Next != null) {
			Next.UpdateView();
		}
	}

	void RepaintView()
	{
		if (Next != null) {
			Next.RepaintView();
		}
	}

	void UpdateStatus()
	{
		RepaintStatus();
	}

	// Currently use this fixed colors - maybe there are some better defines??
	static final int COM_NORM =0x17;
	static final int COM_ORIG =0x1C;
	static final int COM_HIGH =0x1E;
	static final int COM_MARK =0x2E;
	static final int COM_ERR  =0x1C;

	void RepaintStatus()
	{
		TDrawBuffer B = new TDrawBuffer();
		int W, H;


		{
			int [] w = {0}, h = {0}; 
			ConQuerySize(w, h);
			W = w[0];
			H = h[0];
		}

		B.MoveCh( ' ', COM_NORM, W);

		if ((WordsLast > 0) && (WordBegin != null) && (Words != null)
				&& (Words[WordPos]) != null) 
		{
			//const char *sc = STRCOMPLETE;
			int p = STRCOMPLETE.length();
			if (W < 35) {
				// if the width is quite small
				sc += p - 1; // jump to last character
				p = 1;
			}
			B.MoveStr( 0, W, sc, COM_NORM, W);
			// int cur = p;
			B.MoveStr( p, W, WordBegin, COM_ORIG, W);
			p += WordBegin.length();
			int l = Words[WordPos].length();
			if (WordFixed > 0) {
				B.MoveStr( p, W, Words[WordPos], COM_MARK, W);
				p += WordFixed;
				l -= WordFixed;
			}
			B.MoveStr( p, W, Words[WordPos] + WordFixed,
					(WordFixedCount == 1) ? COM_ORIG : COM_HIGH, W);
			p += l;
			String s = String.format("] (T:%d/%d  S:%d)", WordPos + 1, WordsLast, WordFixedCount);
			B.MoveStr( p, W, s, COM_NORM, W);
			// ConSetCursorPos(cur + WordFixed, H - 1);
		} else
			B.MoveStr( 0, W, STRNOCOMPLETE, COM_ERR, W);
		ConPutBox(0, H - 1, W, 1, B);
		ConShowCursor();
	}

	int RefreshComplete()
	{
		if ((Buffer.CP.Col == 0)
				|| (Buffer.SetPos(Buffer.CP.Col, Buffer.CP.Row) == 0))
			return 0;

		PELine L = Buffer.VLine(Buffer.CP.Row);
		int C = Buffer.CP.Col;
		int P = Buffer.CharOffset(L, C);

		if (!P || P > L.Count)
			return 0;

		int P1 = P;
		while ((P > 0) && CheckASCII(L.Chars[P - 1]))
			P--;

		int wlen = P1 - P;
		if (!wlen)
			return 0;

		WordBegin = new char[wlen + 1];
		if (WordBegin == null)
			return 0;

		strncpy(WordBegin, L.Chars + P, wlen);
		WordBegin[wlen] = 0;

		// fprintf(stderr, "Calling %d  %s\n", wlen, WordBegin);
		// Search words in TAGS
		TagComplete(Words, &WordsLast, MAXCOMPLETEWORDS, WordBegin);
		// fprintf(stderr, "Located %d words\n", WordsLast);
		// these words are already sorted

		// Search words in current TEXT
		Buffer.Match.Col = Buffer.Match.Row = 0;
		// this might look strange but it is necessary to catch
		// the first word at position 0,0 for match :-)
		long mask = SEARCH_NOPOS | SEARCH_WORDBEG;

		while (Buffer.FindStr(L.Chars + P, wlen, mask) == 1) {
			mask |= SEARCH_NEXT;
			PELine M = Buffer.RLine(Buffer.Match.Row);
			int X = Buffer.CharOffset(M, Buffer.Match.Col);

			if ((L.Chars == M.Chars) && (P == X))
				continue;

			int XL = X;

			while ((XL < M.Count) && CheckASCII(M.Chars[XL]))
				XL++;

			int len = XL - X - wlen;

			if (len == 0)
				continue;

			//char *s = new char[len + 1];

			if (s != null) {
				strncpy(s, M.Chars + X + wlen, len);
				s[len] = 0;

				int c = 1, H = 0, L = 0, R = WordsLast;

				// using sort to insert only unique words
				while (L < R) {
					H = (L + R) / 2;
					c = strcmp(s, Words[H]);
					if (c < 0)
						R = H;
					else if (c > 0)
						L = H + 1;
					else
						break;
				}

				if (c != 0) {
					// Loop exited without finding the word. Instead,
					// it found the spot where the new should be inserted.
					WordsLast++;

					int i = WordsLast;

					while (i > L) {
						Words[i] = Words[i-1];
						i--;
					}

					Words[i] = s;

					if (WordsLast >= MAXCOMPLETEWORDS)
						break;
				} else
				{
					// word was already listed, free duplicate.
					//delete s;
				}
			}
		}
		Buffer.Match.Row = Buffer.Match.Col = -1;
		Buffer.MatchLen = Buffer.MatchCount = 0;

		/* TODO		#ifdef LOCALE_SORT
		// sort by current locales
		qsort(Words, WordsLast, sizeof(Words[0]), CmpStr);
		#endif */

		FixedUpdate(0);

		//for(int i = 0; i < WordsLast; i++)
		//if (Words[i])
		//fprintf(stderr, "%3d:\t%10p\t%s\n", i, Words[i], Words[i]);
		//fprintf(stderr, "Words %3d\n", WordsLast);

		return WordsLast;
	}

	void FixedUpdate(int add)
	{
		if (add < 0) {
			if (WordFixed > 0)
				WordFixed += add;
		} else if (add > 0) {
			if (Words[WordPos].length() > WordFixed)
				WordFixed += add;
		}

		if (WordFixed > 0) {
			WordFixedCount = 0;
			for(int i = 0; i < WordsLast; i++)
				if (strncmp(Words[WordPos], Words[i], WordFixed) == 0)
					WordFixedCount++;
		} else
			WordFixedCount = WordsLast;

	}
	/*
        // Well this was my first idea - but these menus are unusable
        int menu = NewMenu("CW");
        int n;
        n = NewItem(menu, "Word1");
        Menus[menu].Items[n].Cmd = ExFind;
        n = NewItem(menu, "Word2");
        Menus[menu].Items[n].Cmd = ExMoveLineStart;
        n = NewItem(menu, "Word3");
        Menus[menu].Items[n].Cmd = ExMovePageEnd;
        printf(">%d ****\n", View.MView.Win.Parent.PopupMenu("CW"));
	 */


}
