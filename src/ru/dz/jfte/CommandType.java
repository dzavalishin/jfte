package ru.dz.jfte;

public class CommandType {
    int type = -1;
    //short repeat;
    int repeat = 0;
    //short ign;
    int ign = 0;

    //long num = 0;
    int num = 0;
    String string = null;


    static final int CT_COMMAND  = 0;
    static final int CT_NUMBER   =1;
    static final int CT_STRING   =2;
    static final int CT_VARIABLE =3;
    static final int CT_CONCAT   =4; /* concatenate strings */
    

}
