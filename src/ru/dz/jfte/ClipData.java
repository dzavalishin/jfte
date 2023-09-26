package ru.dz.jfte;

import java.awt.HeadlessException;
import java.awt.Toolkit;
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.StringSelection;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.io.IOException;

public class ClipData {
	private String data = "";

	
	
	int fLen() { return data.length(); }
	
	private int charAt(int i) {
		return data.charAt(i);
	}
	
	@Override
	public String toString() {
		return data;
	}
	
	boolean GetClipText()
	{
		try {
			data = (String) Toolkit.getDefaultToolkit()
			        .getSystemClipboard().getData(DataFlavor.stringFlavor);
		} catch (HeadlessException | UnsupportedFlavorException | IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return false;
		}
		return true;
	}

	boolean PutClipText()
	{
		StringSelection stringSelection = new StringSelection(data);
		Clipboard clipboard = Toolkit.getDefaultToolkit().getSystemClipboard();
		clipboard.setContents(stringSelection, null);

		return true;
	}



	static boolean GetPMClip() {
		ClipData cd = new ClipData();
		int i,j, l, dx;
		EPoint P = new EPoint();

		if (!cd.GetClipText()) 
			return false;

		EBuffer.SSBuffer.Clear();
		j = 0;
		l = 0;

		for (i = 0; i < cd.fLen(); i++) {
			//if (cd.fChar[i] == 0x0A)
			if( cd.charAt(i) == 0x0A)
			{
				EBuffer.SSBuffer.AssertLine(l);
				P.Col = 0; P.Row = l++;
				dx = 0;
				if ((i > 0) && (cd.charAt(i-1) == 0x0D)) dx++;
				//EBuffer.SSBuffer.InsertLine(P, i - j - dx, cd.fChar + j);
				EBuffer.SSBuffer.InsertLine(P, i - j - dx, cd.toString().substring(j) );
				j = i + 1;
			}
		}
		if (j < cd.fLen()) { // remainder
			i = cd.fLen();
			EBuffer.SSBuffer.AssertLine(l);
			P.Col = 0; P.Row = l++;
			dx = 0;
			if ((i > 0) && (cd.charAt(i-1) == 0x0D)) dx++;
			//EBuffer.SSBuffer.InsText(P.Row, P.Col, i - j - dx, cd.fChar + j);
			EBuffer.SSBuffer.InsText(P.Row, P.Col, i - j - dx, cd.toString().substring(j));
			j = i + 1;
		}

		return false;
	}




	static boolean PutPMClip() {
		//char *p = 0;
		//int l = 0;
		//ELine L;
		//int Len;
		ClipData cd = new ClipData();
		int rc;

		for (int i = 0; i < EBuffer.SSBuffer.RCount; i++) {
			ELine L = EBuffer.SSBuffer.RLine(i);
			/*
			p = (char )realloc(p, l + (Len = L.getCount()) + 2);
			memcpy(p + l, L.Chars, L.getCount());
			l += Len;
			if (i < EBuffer.SSBuffer.RCount - 1) {
				p[l++] = 13;
				p[l++] = 10;
			} */
			cd.data += L.toString();
			cd.data += "\n";
		}
		/*
		p = (char )realloc(p, l + 1);
		p[l++] = 0;
		cd.fChar = p;
		cd.fLen() = l; */

		return cd.PutClipText();
	}

}
