package ru.dz.jfte;

import ru.dz.jfte.c.BitOps;

public class TransTable implements BufferDefs, ModeDefs 
{

	final char [] t = new char[256];
	
	private TransTable() {
	}
	
	static TransTable GetTrans(ExState State, EView View) throws ExecException {
	    String [] TrS = {""};
	    String [] TrD = {""};

	    if (State.GetStrParam(View, TrS) == 0)
	        if (View.MView.Win.GetStr("Trans From", TrS, HIST_TRANS) == 0)
	            return null;
	    
	    if (State.GetStrParam(View, TrD) == 0)
	        if (View.MView.Win.GetStr("Trans To", TrD, HIST_TRANS) == 0)
	            return null;
	    
	    TransTable tab = ParseTrans(TrS[0], TrD[0]);
	    
	    if (tab == null) {
	        //Msg(S_ERROR, "Bad Trans Arguments %s %s.", TrS, TrD);
	        //return null;
	    	throw new ExecException("Bad Trans Arguments %s %s.", TrS, TrD);
	    }
	    
	    return tab;
	}

	
	// FLAW: NULL characters can not be translated, need escaping
	static TransTable ParseTrans(String Sp, String Dp) {
	    //char Dest[512];
	    char A, B;
	    int i;

	    TransTable tab = new TransTable();
	    
		if (Sp == null || Dp == null)
	        return null;

	    //strncpy((char *)Dest, (char *)D, sizeof(Dest) - 1); Dest[sizeof(Dest) - 1] = 0;
	    //D = Dest;
		char[] Sa = Sp.toCharArray();
		char[] Da = Dp.toCharArray();

		int S = 0, D = 0;
		
	    // no translation
	    for (i = 0; i < 256; i++)
	        tab.t[i] = (char)i;

	    while (S < Sa.length && D < Da.length) 
	    {
	        //if (Sa[S+0] != 0 && Sa[S+1] == '-' && Sa[S+2] != 0) 
	        if(S+2 < Sa.length && Sa[S+1] == '-') 
	        {
	            if (Sa[S+0] <= Sa[S+2]) {
	                A = Sa[S++];
	                if (Sa[S+0] >= Sa[S+2])
	                    S += 2;
	            } else {
	                A = Sa[S--];
	                if (Sa[S+0] <= Sa[S+2])
	                    S += 2;
	            }
	        } else {
	            A = Sa[S++];
	        }
	        
	        //if (Da[D+0] != 0 && Da[D+1] == '-' && Da[D+2] != 0) 
	        if (D+2 < Da.length && Da[D+1] == '-') 
	        {
	            if (Da[D+0] <= Da[D+2]) {
	                B = Da[D++];
	                if (Da[D+0] >= Da[D+2])
	                    D += 2;
	            } else {
	                B = Da[D--];
	                if (Da[D+0] <= Da[D+2])
	                    D += 2;
	            }
	        } else {
	            B = Da[D++];
	        }
	        tab.t[A] = B;
	    }
	    
	    if (S < Sa.length || D < Da.length) // one was too short
	        return null;

	    return tab;
	}

	
	
	static TransTable MakeTrans(int What) {
	    int i;
	    TransTable tab = new TransTable();

	    // no translation
	    for (i = 0; i <= 255; i++)
	        tab.t[i] = (char)i;

	    switch (What) {
	    case ccToggle:
	    case ccUp:
	        for (i = 33; i <= 255; i++)
	            if (BitOps.isalpha((char) i) && (BitOps.toupper(i) != i))
	                tab.t[i] = (char) BitOps.toupper(i);
	        if (What != ccToggle)
	            break;
	    
	    case ccDown:
	        for (i = 33; i <= 255; i++)
	            if (BitOps.isalpha((char) i) && (i == tab.t[i]) && (BitOps.tolower(i) != i))
	                tab.t[i] = (char) BitOps.tolower(i);
	        break;
	    
	    default:
	        return null;
	    }
	    
	    return tab;
	}
	
}
