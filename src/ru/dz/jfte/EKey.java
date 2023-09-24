package ru.dz.jfte;

public class EKey extends KeyMapper implements KeyDefs 
{
    KeySel fKey;
    int Cmd;
    //EKeyMap fKeyMap;
    EKey fNext;

    EKey(String aKey) {
        fNext = null;
        KeyDefs.ParseKey(aKey, fKey);
        //fKeyMap = null;
        Cmd = -1;
    }

    EKey(String aKey, EKeyMap aKeyMap) {
        fNext = null;
        Cmd = -1;
        KeyDefs.ParseKey(aKey, fKey);
        KeyMap = aKeyMap;
    }

    void close()
    {
        // if there is child keymaps delete them
        KeyMap.close();
    }
    
}
