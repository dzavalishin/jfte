package ru.dz.jfte;

public class EKey extends KeyTable 
{
    KeySel fKey;
    int Cmd;
    EKeyMap fKeyMap;
    EKey fNext;

    EKey(String aKey) {
        fNext = null;
        ParseKey(aKey, fKey);
        fKeyMap = null;
        Cmd = -1;
    }

    EKey(String aKey, EKeyMap aKeyMap) {
        fNext = null;
        Cmd = -1;
        ParseKey(aKey, fKey);
        fKeyMap = aKeyMap;
    }

    void close()
    {
        // if there is child keymaps delete them
        fKeyMap.close();
    }
    
}
