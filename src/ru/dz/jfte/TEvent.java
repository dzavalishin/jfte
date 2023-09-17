package ru.dz.jfte;

public abstract class TEvent {

}


class TKeyEvent extends TEvent {
    long /*TEventMask*/  What;
    GView View;
    long /*TKeyCode*/  Code;
} ;

class TMouseEvent extends TEvent{
    long /*TEventMask*/  What;
    GView View;
    long X;
    long Y;
    int Buttons;
    int Count;
    long /*TKeyCode*/  KeyMask;
} ;

class TMsgEvent extends TEvent
{
    long /*TEventMask*/  What;
    GView View;
    EModel Model;
    long /*TCommand*/  Command;
    long Param1;
    Object Param2;
} ;
