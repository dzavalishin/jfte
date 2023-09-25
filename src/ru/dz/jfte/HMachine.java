package ru.dz.jfte;

import java.util.Arrays;

public class HMachine {
    HState [] state = null;
    HTrans [] trans = null;
    

    HState LastState() { return state[state.length-1]; }


    void AddState(HState aState) {
    	if(state == null)
    		state = new HState[1];
    	else
    		state = Arrays.copyOf(state, state.length + 1);

        state[state.length-1] = aState;
        state[state.length-1].firstTrans = (trans == null) ? 0 : trans.length;
    }

    void AddTrans(HTrans aTrans) 
    {
        assert(state.length > 0);
        
        if(trans == null )
        	trans = new HTrans[1];
        else
        	trans = Arrays.copyOf(trans, trans.length + 1);
        
        state[state.length-1].transCount++;
        trans[trans.length-1] = aTrans;
    }
    
}
