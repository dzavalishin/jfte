package ru.dz.jfte;

public class EKeyMap {
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
    
}
