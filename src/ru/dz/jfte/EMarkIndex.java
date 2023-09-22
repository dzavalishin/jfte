package ru.dz.jfte;

import java.io.BufferedWriter;
import java.io.IOException;
import java.util.Arrays;

public class EMarkIndex {
	//private int marks.length = 0;
	private EMark [] marks = null;
	
	public static EMarkIndex markIndex;



	EMarkIndex() {
	}


	EMark insert(String aName, String aFileName, EPoint aPoint, EBuffer aBuffer) {
	    int L = 0, R = marks.length, M, cmp;

	    //assert(aName != 0 && aName[0] != 0);
	    //assert(aFileName != 0 && aFileName[0] != 0);

	    while (L < R) {
	        M = (L + R) / 2;
	        cmp = aName.compareTo(marks[M].getName());
	        if (cmp == 0)
	            return null;
	        else if (cmp > 0)
	            L = M + 1;
	        else
	            R = M;
	    }

	    /*
	    EMark newMarks = (EMark *)realloc(marks,
	                                         sizeof(marks[0]) * (marks.length + 1));
	    
	    if (newMarks == 0)
	        return 0;
	    marks = newMarks; */
	    if( null == marks )
	    	marks = new EMark[1];
	    else
	    	marks = Arrays.copyOf(marks, marks.length + 1);

	    EMark m = new EMark(aName, aFileName, aPoint, aBuffer);

	    //memmove(marks + L + 1, marks + L, sizeof(marks[0]) * (marks.length - L));
	    System.arraycopy(marks, L, marks, L + 1, marks.length - L);
	    //marks.length++;
	    marks[L] = m;
	    return m;
	}

	EMark insert(String aName, EBuffer aBuffer, EPoint aPoint) {
	    //assert(aName != 0 && aName[0] != 0);
	    //assert(aBuffer != 0);
	    //assert(aBuffer.FileName != 0);

	    return insert(aName, aBuffer.FileName, aPoint, aBuffer);
	}

	EMark locate(String aName) {
	    int L = 0, R = marks.length, M, cmp;

	    //assert(aName != 0 && aName[0] != 0);

	    while (L < R) {
	        M = (L + R) / 2;
	        cmp = aName.compareTo(marks[M].getName());
	        if (cmp == 0)
	            return marks[M];
	        else if (cmp > 0)
	            L = M + 1;
	        else
	            R = M;
	    }
	    return null;
	}

	int remove(String aName) {
	    int L = 0, R = marks.length, M, cmp;

	    //assert(aName != 0 && aName[0] != 0);

	    while (L < R) {
	        M = (L + R) / 2;
	        cmp = aName.compareTo(marks[M].getName());
	        if (cmp == 0) {
	            //EMark m = marks[M];
	            
	            /*
	            memmove(marks + M,
	                    marks + M + 1,
	                    sizeof(marks[0]) * (marks.length - M - 1));
				*/
	    	    System.arraycopy(marks, M+1, marks, M, marks.length - L);

	            /*
	            marks.length--;

	            EMark newMarks = (EMark *)realloc(marks,
	                                               sizeof(marks[0]) * (marks.length));
	            if (newMarks != 0 || marks.length == 0)
	                marks = newMarks;
				*/
	    	    marks = Arrays.copyOf(marks, marks.length - 1);


	            return 1;
	        } else if (cmp > 0)
	            L = M + 1;
	        else
	            R = M;
	    }
	    return 0;
	}

	boolean view(EView aView, String aName) {
	    EMark m = locate(aName);
	    if (m!=null) {
	        EBuffer b = m.getBuffer();
	        if (b == null) {
	            if (!Console.FileLoad(0, m.getFileName(), null, aView))
	                return false;
	            if (retrieveForBuffer((EBuffer)EModel.ActiveModel) == 0)
	                return false;
	            b = (EBuffer)EModel.ActiveModel;
	        }
	        aView.SwitchToModel(b);
	        return b.GotoBookmark(m.getName());
	    }
	    return false;
	}

	int retrieveForBuffer(EBuffer aBuffer) {
	    for (int n = 0; n < marks.length; n++)
	        if (marks[n].getBuffer() == null &&
	            Console.filecmp(aBuffer.FileName, marks[n].getFileName()) == 0)
	        {
	            if (marks[n].setBuffer(aBuffer) == 0)
	                return 0;
	        }
	    return 1;
	}

	int storeForBuffer(EBuffer aBuffer) {
	    for (int n = 0; n < marks.length; n++)
	        if (marks[n].getBuffer() == aBuffer)
	            if (marks[n].removeBuffer(aBuffer) == 0)
	                return 0;
	    return 1;
	}

	int saveToDesktop(BufferedWriter fp) throws IOException {
	    for (int n = 0; n < marks.length; n++) {
	        EPoint p = marks[n].getPoint();

	        // ??? file of buffer or of mark? (different if file renamed) ???
	        // perhaps marks should be duplicated?
	        String l = String.format("M|%d|%d|%s|%s\n",
	                p.Row, p.Col,
	                marks[n].getName(),
	                marks[n].getFileName());
	        
	        fp.write(l);
	    }
	    return 1;
	}

	// needs performance fixes (perhaps a redesign ?)

	EMark pushMark(EBuffer aBuffer, EPoint P) {
	    int stackTop = -1;
	    
	    for (int n = 0; n < marks.length; n++) {
	        String name = marks[n].getName();
	        if (name != null && name.charAt(0) == '#' 
	        		&& Character.isDigit(name.charAt(1))) {
	            int no = Integer.parseUnsignedInt(name.substring(1));
	            if (no > stackTop)
	                stackTop = no;
	        }
	    }
	    String name = String.format("#%d", stackTop + 1);
	    return insert(name, aBuffer, P);
	}

	int popMark(EView aView) {
	    int stackTop = -1;
	    
	    for (int n = 0; n < marks.length; n++) {
	        String name = marks[n].getName();
	        if (name != null && name.charAt(0) == '#' 
	        		&& Character.isDigit(name.charAt(1))) {
	            //int no = atoi(name + 1);
	            int no = Integer.parseUnsignedInt(name.substring(1));
	            if (no > stackTop)
	                stackTop = no;
	        }
	    }
	    if (stackTop == -1)
	        return 0;

	    String name = String.format("#%d", stackTop);
	    if (!view(aView, name))
	        return 0;
	    assert(remove(name) == 1);
	    return 1;
	}
	
	
	
}
