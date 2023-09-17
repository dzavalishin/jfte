package ru.dz.jfte;

public class EColorize 
{
    String Name;
    EColorize Next;
    EColorize Parent;
    int SyntaxParser;
    ColorKeywords Keywords; // keywords to highlight
    HMachine hm;
    ChColor Colors[COUNT_CLR];

    static EColorize Colorizers;
    
}
