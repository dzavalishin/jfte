package ru.dz.jfte;

public class EColorize implements ModeDefs
{
    String Name;
    EColorize Next;
    EColorize Parent;
    int SyntaxParser;
    ColorKeywords Keywords; // keywords to highlight
    HMachine hm;
    int Colors[] = new int[COUNT_CLR];

    static EColorize Colorizers;
    
}
