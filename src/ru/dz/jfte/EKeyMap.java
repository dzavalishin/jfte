package ru.dz.jfte;

public class EKeyMap implements KeyDefs 
{
    EKeyMap fParent;
    EKey fKeys;

    EKeyMap() {
        fKeys = null;
        fParent = null;
    }

    void close() {
        // free keys
        {
            EKey e;

            while((e = fKeys) != null)
            {
                fKeys = fKeys.fNext;
                e.close();
            }
        }
    }

    void AddKey(EKey aKey) {
        aKey.fNext = fKeys;
        fKeys = aKey;
    }

    
    static int MatchKey( int /*TKeyCode*/ aKey, KeySel aSel) 
    {
        int flags = aKey & ~ 0xFFFF;
        int key = aKey & 0xFFFF;

        flags &= ~kfAltXXX;

        if(0!= (flags & kfShift)) {
            if (key < 256)
                if (flags == kfShift)
                    flags &= ~kfShift;
                else if (KeyDefs.isAscii(key))
                    key = Character.toUpperCase(key); 
        }
        if (0 != (flags & kfCtrl) && 0 == (flags & kfSpecial))
            if (key < 32)
                key += 64;

        flags &= ~aSel.Mask;

        if(0!= (aSel.Mask & kfShift)) {
            if (key < 256)
                if (KeyDefs.isAscii(key))
                    key = Character.toUpperCase(key);
        }
        aKey = key | flags;
        if (aKey == aSel.Key)
            return 1;
        return 0;
    }

    EKey FindKey(int /*TKeyCode*/ aKey) {
        EKey p = fKeys;

        while (p!=null) {
            if (MatchKey(aKey, p.fKey)!=0) return p;
            p = p.fNext;
        }
        return null;
    }

	public void dump() {
		for( EKey p = fKeys; p != null; p = p.fNext )
			p.dump();
		
	}
    
}
