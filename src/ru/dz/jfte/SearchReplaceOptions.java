package ru.dz.jfte;

public class SearchReplaceOptions implements GuiDefs, ModeDefs 
{
    int ok;
    String strSearch;
    String strReplace;
    int Options;
    //
    int resCount;
    
    // ------------------ search process state below

    private EView View;
	private EBuffer b;
	private EPoint Match;
	private EPoint BE;
	private EPoint BB;
    
    public void clear()
    {
    	strSearch = strReplace = "";
    	Options = 0;
    	resCount = 0;
    	ok = 0;
    }
    

    
    boolean Find(EBuffer eBuffer) 
    {
		this.b = eBuffer;
		this.View = eBuffer.View;
		this.Match = b.Match;
		this.BB = b.BB;
		this.BE = b.BE;
		
		return doFind();
    }    
    
    boolean doFind() 
    {
    	//SearchReplaceOptions opt = this;
    	
    	
        int slen = strSearch.length();
        //int Options = opt.Options;
        int rlen = strReplace.length();
        RxNode Rx = null;

        resCount = -1;

        if (slen == 0) return false;
        
        if(0 != (Options & SEARCH_BLOCK)) {
            if (!b.CheckBlock()) return false;
        }
        
        if(0 != (Options & SEARCH_RE)) {
            // TODO Rx = RxCompile(strSearch);
            if (Rx == null) {
                View.MView.Win.Choice(GPC_ERROR, "Find", 1, "O&K", "Invalid regular expression.");
                return false;
            }
        }
        
        if(0 != (Options & SEARCH_GLOBAL)) {
            if(0 != (Options & SEARCH_BLOCK)) {
                if(0 != (Options & SEARCH_BACK)) {
                    if (!b.SetPosR(BE.Col, BE.Row)) return error();
                } else {
                    if (!b.SetPosR(BB.Col, BB.Row)) return error();
                }
            } else {
                if(0 != (Options & SEARCH_BACK)) {
                    if (b.RCount < 1) return error();
                    if (!b.SetPosR(b.LineLen(b.RCount - 1), b.RCount - 1)) return error();
                } else {
                    if (!b.SetPosR(0, 0)) return error();
                }
            }
        }
        resCount = 0;
        while (true) {
            if(0 != (Options & SEARCH_RE)) {
                //if (FindRx(Rx, Options) == 0) return done();
            	return error();
            } else {
                if (!b.FindStr(strSearch, slen, Options)) return done();
            }
            resCount++;

            do {
            if(0 != (Options & SEARCH_REPLACE)) 
            {
                char ask = 'A';

                if (0==(Options & SEARCH_NASK)) {
                    char ch;

                    ask = askReplace(this, ask);

                    if (ask == 'N') break; // goto try_join;
                    if (ask == 'Q') return done();
                    if (ask == 'A') Options |= SEARCH_NASK;
                }

                if(0 != (Options & SEARCH_RE)) {
                    ELine L = b.RLine(Match.Row);
                    int P, R;
                    String PR = null;
                    int LR = 0;

                    R = Match.Row;
                    P = Match.Col;
                    P = b.CharOffset(L, P);

                    /* TODO RxReplace
                    if (0 == RxReplace(strReplace, L.Chars, L.getCount(), b.MatchRes, PR, LR)) 
                    {
                        if (!b.DelText(R, Match.Col, b.MatchLen)) return error();
                        if (PR!=null && LR > 0)
                            if (!b.InsText(R, Match.Col, LR, PR)) return error();
                        rlen = LR;
                    } */
                } else {
                    if (!b.DelText(Match.Row, Match.Col, b.MatchLen)) return error();
                    if (!b.InsText(Match.Row, Match.Col, rlen, strReplace)) return error();
                }
                if (0==(Options & SEARCH_BACK)) {
                	b.MatchLen = rlen;
                    b.MatchCount = rlen;
                }
                if (ask == 'O')
                    return done();
            }
        	} while(false);
        //try_join:
            
            do {
            if(0 != (Options & SEARCH_JOIN)) {
                char ask = 'A';

                if (0==(Options & SEARCH_NASK)) {
                    char ch;

                    ask = askJoin(ask);

                    if (ask == 'N') break; // goto try_delete;
                    if (ask == 'Q') return done();
                    if (ask == 'A') Options |= SEARCH_NASK;
                }

                if (!b.JoinLine(Match.Row, Match.Col)) return error();

                if (ask == 'O')
                    return done();
            } } while(false);
        //try_delete:
            
            boolean doNext = false;
            boolean doBreak= false;
            do {
            if(0 != (Options & SEARCH_DELETE)) {
                char ask = 'A';

                if (0==(Options & SEARCH_NASK)) {
                    char ch;

                    ask = askDelete(ask);

                    if (ask == 'N') { doNext = true; break; } //goto next;
                    if (ask == 'Q') return done();
                    if (ask == 'A') Options |= SEARCH_NASK;
                }

                if (Match.Row == b.RCount - 1) {
                    if (!b.DelText(Match.Row, 0, b.LineLen())) return error();
                } else
                    if (!b.DelLine(Match.Row)) return error();

                if (ask == 'O')
                    return done();
                if (0==(Options & SEARCH_ALL))
                { doBreak = true; break; } //break;
                { doNext = false; break; } //goto last;
            } } while(true);
        //next:
            if(doBreak)
            	break;
            
        	if(doNext)
        	{
            if (0==(Options & SEARCH_ALL))
                break;
            Options |= SEARCH_NEXT;
        	}
        //last:
            ;
        }
        
        // end of search

        return done();
    }



	private char askDelete(char ask) {
		char ch;
		while (true) {
			b.Draw(b.VToR(b.CP.Row), 1);
		    b.Redraw();
		    switch (View.MView.Win.Choice(0, "Delete Line",
		                                     5,
		                                     "&Yes",
		                                     "&All",
		                                     "&Once",
		                                     "&Skip",
		                                     "&Cancel",
		                                     "Delete line %d?", b.VToR(b.CP.Row)))
		    {
		    case 0: ch = 'Y'; break;
		    case 1: ch = 'A'; break;
		    case 2: ch = 'O'; break;
		    case 3: ch = 'N'; break;
		    case 4:
		    case -1:
		    default:
		        ch = 'Q'; break;
		    }
		    if (ch == 'Y') { ask = 'Y'; break; }
		    if (ch == 'N') { ask = 'N'; break; }
		    if (ch == 'Q') { ask = 'Q'; break; }
		    if (ch == 'A') { ask = 'A'; break; }
		    if (ch == 'O') { ask = 'O'; break; }
		}
		return ask;
	}



	private char askJoin(char ask) {
		char ch;
		while (true) {
			b.Draw(b.VToR(b.CP.Row), 1);
			b.Redraw();
		    switch (View.MView.Win.Choice(0, "Join Line",
		                                     5,
		                                     "&Yes",
		                                     "&All",
		                                     "&Once",
		                                     "&Skip",
		                                     "&Cancel",
		                                     "Join lines %d and %d?", 1 + b.VToR(b.CP.Row), 1 + b.VToR(b.CP.Row) + 1))
		    {
		    case 0: ch = 'Y'; break;
		    case 1: ch = 'A'; break;
		    case 2: ch = 'O'; break;
		    case 3: ch = 'N'; break;
		    case 4:
		    case -1:
		    default:
		        ch = 'Q'; break;
		    }
		    if (ch == 'Y') { ask = 'Y'; break; }
		    if (ch == 'N') { ask = 'N'; break; }
		    if (ch == 'Q') { ask = 'Q'; break; }
		    if (ch == 'A') { ask = 'A'; break; }
		    if (ch == 'O') { ask = 'O'; break; }
		}
		return ask;
	}



	private char askReplace(SearchReplaceOptions opt, char ask) {
		char ch;
		while (true) {
			b.Draw(b.VToR(b.CP.Row), 1);
			b.Redraw();
		    switch (View.MView.Win.Choice(0, "Replace",
		                                     5,
		                                     "&Yes",
		                                     "&All",
		                                     "&Once",
		                                     "&Skip",
		                                     "&Cancel",
		                                     "Replace with %s?", opt.strReplace))
		    {
		    case 0: ch = 'Y'; break;
		    case 1: ch = 'A'; break;
		    case 2: ch = 'O'; break;
		    case 3: ch = 'N'; break;
		    case 4:
		    case -1:
		    default:
		        ch = 'Q'; break;
		    }
		    if (ch == 'Y') { ask = 'Y'; break; }
		    if (ch == 'N') { ask = 'N'; break; }
		    if (ch == 'Q') { ask = 'Q'; break; }
		    if (ch == 'A') { ask = 'A'; break; }
		    if (ch == 'O') { ask = 'O'; break; }
		}
		return ask;
	}

    private boolean done()
    {
        if(0 != (Options & SEARCH_ALL))
        	b.Msg(S_INFO, "%d match(es) found.", resCount);
        else {
            if (resCount == 0) {
                b.Msg(S_INFO, "[%s] not found", strSearch);
                return false;
            }
        }
    	
        return true;
    }
    
    private boolean error()
    {
        View.MView.Win.Choice(GPC_ERROR, "Find", 1, "O&K", "Error in search/replace.");
        return false;
    }
    
    
}
