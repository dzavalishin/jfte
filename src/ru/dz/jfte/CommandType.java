package ru.dz.jfte;

public class CommandType {
    int type;
    short repeat;
    short ign;

    long num;
    String string;


    static final int CT_COMMAND  = 0;
    static final int CT_NUMBER   =1;
    static final int CT_STRING   =2;
    static final int CT_VARIABLE =3;
    static final int CT_CONCAT   =4; /* concatenate strings */
    

}
