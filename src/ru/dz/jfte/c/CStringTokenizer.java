package ru.dz.jfte.c;

import java.util.Iterator;

public class CStringTokenizer implements Iterator<CString> 
{

	private AbstractCString s;
	private int startPos = 0;
	private CharSequence delimiters;

	public CStringTokenizer(AbstractCString s, CharSequence delimiters) {
		this.s = s;
		this.delimiters = delimiters;		
	}

	@Override
	public CString next()
	{
		if(startPos >= s.length())
			return null;

		CString ret;

		int end = s.strcspn(delimiters, startPos);

		if( end < 0 )
		{
			ret = s.substring(startPos, s.length());
			startPos = s.length();
		}
		else
		{
			ret = s.substring(startPos, end);
			startPos = end+1;
		}

		return ret;
	}

	@Override
	public boolean hasNext() {
		return startPos < s.length();
	}

}
