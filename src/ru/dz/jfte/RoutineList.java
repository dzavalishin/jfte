package ru.dz.jfte;

import java.util.List;
import java.util.ArrayList;

public class RoutineList 
{
    //int Count = 0;
    //int []Lines = null;

    List<RoutineDef> lines = new ArrayList<>();

	public int Count() {
		return lines.size();
	}
}


class RoutineDef
{
	int line = 0;

	public RoutineDef(int l) {
		line = l;
	}
}