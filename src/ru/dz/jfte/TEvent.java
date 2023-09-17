package ru.dz.jfte;

public abstract class TEvent implements EventDefs 
{
    int /*TEventMask*/  What;
    GView View;

}


class TKeyEvent extends TEvent {
    int /*TKeyCode*/  Code;
} ;

class TMouseEvent extends TEvent{
    int X;
    int Y;
    int Buttons;
    int Count;
    long /*TKeyCode*/  KeyMask;
} ;

class TMsgEvent extends TEvent
{
    EModel Model;
    int /*TCommand*/  Command;
    long Param1;
    Object Param2;
} ;
