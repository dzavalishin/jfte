package ru.dz.jfte;

import java.util.ArrayList;
import java.util.List;

public class InputHistory 
{
	//static int 		Count;
	String	Line;
	int		Id;

	static List<InputHistory> hlist = new ArrayList<>();

	static void AddInputHistory(int Id, String s) {

		for( InputHistory h : hlist )
		{
			if( h.Id == Id && h.Line.equals(s))
			{
				hlist.remove(h);
				hlist.add(0,h);
				return;
			}
		}

		InputHistory h = new InputHistory();
		h.Id = Id;
		h.Line = s;
		hlist.add(0,h);    			
	}



	static int CountInputHistory(int Id) { 
		int c = 0;

		for( InputHistory h : hlist )
			if (h.Id == Id) c++;
		return c;
	}

	static boolean GetInputHistory(int Id, String []s, int Nth) { 
		for( InputHistory h : hlist )
		{
			if (h.Id == Id) {
				Nth--;
				if (Nth == 0) {
					s[0] = h.Line;
					return true;
				}
			}
		}
		return false;
	}





}
