package ru.dz.jfte;

public class HMachine {
    int stateCount;
    int transCount;
    HState state;
    HTrans trans;
    

    HState LastState() { return state + stateCount - 1; }

}
