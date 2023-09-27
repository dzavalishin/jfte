package ru.dz.jfte;

public class KeySel {
    int /*TKeyCode*/ Mask;
    int /*TKeyCode*/ Key;
    
    @Override
    public String toString() {
    	return String.format("%x %x", Mask, Key);
    }
}
