package ru.dz.jfte;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.IOException;
import java.io.StringWriter;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.print.Doc;
import javax.print.DocFlavor;
import javax.print.DocPrintJob;
import javax.print.PrintException;
import javax.print.PrintService;
import javax.print.PrintServiceLookup;
import javax.print.SimpleDoc;

import ru.dz.jfte.c.BinaryString;
import ru.dz.jfte.c.BitOps;
import ru.dz.jfte.c.CString;
import ru.dz.jfte.c.CStringPtr;
import ru.dz.jfte.undo.UndoDefs;
import ru.dz.jfte.undo.UndoOperation;
import ru.dz.jfte.undo.UndoRedoController;
import ru.dz.jfte.undo.UndoStackItem;

public class EBuffer extends EModel implements BufferDefs, ModeDefs, GuiDefs, ColorDefs, EventDefs, UndoDefs, RxDefs 
{
	private static final Logger log = Logger.getLogger(EBuffer.class.getName());


	String FileName = null;
	public int Modified = 0;
	EPoint TP = new EPoint();
	public EPoint CP = new EPoint();

	EPoint BB = new EPoint(-1, -1);
	EPoint BE = new EPoint(-1, -1);
	EPoint PrevPos = new EPoint(-1, -1);
	EPoint SavedPos = new EPoint(-1, -1);

	EBufferFlags Flags;
	EMode Mode;
	public int BlockMode = bmStream;
	int ExtendGrab = 0;
	boolean AutoExtend = false;
	boolean Loaded = false;

	UndoRedoController us = new UndoRedoController(this); 

	BasicFileAttributes FileStatus = null;
	boolean FileOk = false;
	boolean Loading = false;

	int RAllocated = 0;   // text line allocation
	int RGap = 0;
	int RCount = 0;
	ELine [] LL = null;

	int VAllocated = 0;   // visible lines
	int VGap = 0;
	int VCount = 0;
	int [] VV  = null;


	int FCount = 0;
	public EFold [] FF = new EFold[0];

	EPoint Match = new EPoint(-1, -1);
	int MatchLen = 0;
	int MatchCount = 0;
	//RxMatchRes MatchRes;

	//int BMCount;
	Map<String,EBookmark> BMarks = new HashMap<>();

	RoutineList rlst = new RoutineList();
	RoutineView Routines = null;

	int MinRedraw, MaxRedraw;
	int RedrawToEos;

	String [] WordList = null;
	int WordCount = 0;

	SyntaxProc HilitProc = null;
	int StartHilit, EndHilit;






	static SearchReplaceOptions LSearch = new SearchReplaceOptions();
	static int suspendLoads = 0;
	static EBuffer SSBuffer = null; // scrap buffer (clipboard)


	static int iBFI(EBuffer y, int x)  { return (y.Flags.num[x & 0xFF]); }
	static boolean BFI(EBuffer y, int x)  { return (y.Flags.num[x & 0xFF]) != 0; }
	static void BFI_SET(EBuffer y, int x, int v) { y.Flags.num[x & 0xFF]=v; }
	static String BFS(EBuffer y,int x) { return y.Flags.str[x & 0xFF]; }

	///////////////////////////////////////////////////////////////////////////////

	/*static EBuffer newEBuffer(int createFlags, EModel ARoot, String AName)
	{
		EModel []Root = {ARoot};
		return new EBuffer(createFlags, Root, AName);

	}*/


	EBuffer(int createFlags, EModel []ARoot, String AName)
	//:EModel(createFlags, ARoot), TP(0,0), CP(0,0), BB(-1,-1), BE(-1,-1),
	//PrevPos(-1, -1), SavedPos(-1, -1), Match(-1, -1)

	{
		super(createFlags, ARoot);

		//Name = strdup(AName);
		Allocate(0);
		AllocVis(0);
		Mode = EMode.GetModeForName("");
		Flags = Mode.Flags;
		// was BFI(this, BFI_Undo) = 0;
		BFI_SET(this, BFI_Undo,  0);
		// was BFI(
		BFI_SET(this, BFI_ReadOnly, 0);

		Modified = 0;
		MinRedraw = -1;
		MaxRedraw = -1;
		RedrawToEos = 0;

		StartHilit = 0;
		EndHilit = -1;
		resetHiliter();

		InsertLine(CP,0,null); /* there should always be at least one line in the edit buffer */
		Flags = (Mode.Flags);
		Modified = 0;
	}


	@Override
	public void close() 
	{
		if (FileName != null && Loaded) {
			FPosHistory.UpdateFPos(FileName, VToR(CP.Row), CP.Col);

			if (iBFI (this,BFI_SaveBookmarks)==3) 
				FPosHistory.StoreBookmarks(this);
		}

		if (FileName != null && Loaded)
			EMarkIndex.markIndex.storeForBuffer(this);

		Clear();

		DeleteRelated();
	} 


	boolean Clear() {
		Modified = 1;

		EndHilit = -1;
		StartHilit = 0;

		WordCount = 0;
		WordList = null;

		rlst.lines.clear();

		LL = null;
		RCount = RAllocated = RGap = 0;
		VCount = VAllocated = VGap = 0;
		VV = null;

		return false;
	}



	boolean Modify()  {
		if (BFI(this, BFI_ReadOnly)) 
		{
			Msg(S_ERROR, "File is read-only.");
			return false;
		}

		if (Modified == 0) 
		{
			if ((FileName != null) && FileOk) 
			{
				Path fp = Paths.get(FileName);
				BasicFileAttributes StatBuf = null;
				try {
					StatBuf = Files.readAttributes( fp, BasicFileAttributes.class);
				} catch (IOException e) {
					log.log(Level.SEVERE, "readAttributes "+FileName, e);
				}

				if( StatBuf != null &&
						( FileStatus == null || FileStatus.size() != StatBuf.size() ||
						!FileStatus.lastModifiedTime().equals(StatBuf.lastModifiedTime()) )
						)
				{
					View.MView.Win.Choice(
							GPC_ERROR, "Warning! Press Esc!",
							0,
							"File %-55.55s changed on disk!", 
							FileName);

					int cr = View.MView.Win.Choice(0, "File Changed on Disk",
							2,
							"&Modify",
							"&Cancel",
							"%s", FileName);

					switch (cr)
					{
					case 0:
						break;
					case 1:
					case -1:
					default:
						return false;
					}
				}
			}

			getSlot().pushModified();
		}
		Modified++;
		if (Modified == 0) Modified++;
		return true;
	}

	boolean LoadRegion(EPoint A, int FH, int StripChar, int LineChar) {
		return false;
	}

	boolean InsertLine(EPoint Pos, int ACount, String AChars) {
		if (!InsLine(Pos.Row, 0, false)) return false;
		if (!InsText(Pos.Row, Pos.Col, ACount, AChars, false)) return false;
		return true;
	}


	boolean UpdateMark(EPoint M, int Type, int Row, int Col, int Rows, int Cols) {
		switch (Type) {
		case umInsert: /* text inserted */
			switch (BlockMode) {
			case bmLine:
			case bmColumn:
				if (M.Row >= Row)
					M.Row += Rows;
				break;
			case bmStream:
				if (Cols!=0) {
					if (M.Row == Row)
						if (M.Col >= Col)
							M.Col += Cols;
				}
				if (Rows!=0) {
					if (M.Row >= Row)
						M.Row += Rows;
				}
				break;
			}
			break;
		case umDelete:
			switch (BlockMode) {
			case bmLine:
			case bmColumn:
				if (M.Row >= Row)
					if (InRange(Row, M.Row, Row + Rows))
						M.Row = Row;
					else
						M.Row -= Rows;
				break;
			case bmStream:
				if (Cols!=0) {
					if (M.Row == Row)
						if (M.Col >= Col)
							if (M.Col < Col + Cols)
								M.Col = Col;
							else
								M.Col -= Cols;
				}
				if (Rows!=0) {
					if (M.Row >= Row)
						if (M.Row < Row + Rows) {
							M.Row = Row;
							M.Col = 0;
						} else M.Row -= Rows;
				}
			}
			break;
		case umSplitLine:
			switch (BlockMode) {
			case bmLine:
			case bmColumn:
				if (M.Row == Row) {
					if (Col <= M.Col) {
						M.Row++;
						M.Col -= Col;
					}
				} else if (M.Row > Row) M.Row++;
				break;
			case bmStream:
				if (M.Row == Row) {
					if (Col <= M.Col) {
						M.Row++;
						M.Col -= Col;
					}
				} else if (M.Row > Row) M.Row++;
				break;
			}
			break;
		case umJoinLine:
			switch (BlockMode) {
			case bmLine:
			case bmColumn:
				if (M.Row == Row + 1)
					M.Row--;
				else if (M.Row > Row) M.Row--;
				break;
			case bmStream:
				if (M.Row == Row + 1) {
					M.Row--;
					M.Col += Col;
				} else if (M.Row > Row) M.Row--;
				break;
			}
			break;
		}
		return true;
	}

	boolean UpdateMarker(int Type, int Row, int Col, int Rows, int Cols) {
		EPoint OldBB = BB, OldBE = BE;
		//EView V;

		UpdateMark(SavedPos, Type, Row, Col, Rows, Cols);
		UpdateMark(PrevPos, Type, Row, Col, Rows, Cols);

		UpdateMark(BB, Type, Row, Col, Rows, Cols);
		UpdateMark(BE, Type, Row, Col, Rows, Cols);

		//V = View;
		//while (V != null) 
		for(EView V : views)
		{
			if (V.Model != this)
				assert(1 == 0);
			if (V != View) {
				EPoint M;

				M = GetViewVPort(V).TP;
				UpdateMark(GetViewVPort(V).TP, Type, Row, Col, Rows, Cols);
				GetViewVPort(V).TP.Col = M.Col;
				UpdateMark(GetViewVPort(V).CP, Type, Row, Col, Rows, Cols);
			}
			//V = V.NextView;
		}

		for ( RoutineDef l : rlst.lines) {
			EPoint M = new EPoint();

			M.Col = 0;
			M.Row = l.line;
			UpdateMark(M, Type, Row, Col, Rows, Cols);
			l.line = M.Row;
		}

		for (int f = 0; f < FCount; f++) {
			EPoint M = new EPoint();

			M.Col = 0;
			M.Row = FF[f].line;
			UpdateMark(M, Type, Row, Col, Rows, Cols);
			FF[f].line = M.Row;
		} 
		
		for( EBookmark bm : BMarks.values() )
			UpdateMark(bm.BM, Type, Row, Col, Rows, Cols);


		if (OldBB.Row != BB.Row) {
			int MinL = Math.min(OldBB.Row, BB.Row);
			int MaxL = Math.max(OldBB.Row, BB.Row);
			if (MinL != -1 && MaxL != -1)  
				Draw(MinL, MaxL);
		}
		if (OldBE.Row != BE.Row) {
			int MinL = Math.min(OldBE.Row, BE.Row);
			int MaxL = Math.max(OldBE.Row, BE.Row);
			if (MinL != -1 && MaxL != -1)  
				Draw(MinL, MaxL);
		}
		return true;
	}

	boolean ValidPos(EPoint Pos) {
		if ((Pos.Col >= 0) &&
				(Pos.Row >= 0) &&
				(Pos.Row < VCount))
			return true;
		return false;
	}

	boolean RValidPos(EPoint Pos) {
		if ((Pos.Col >= 0) &&
				(Pos.Row >= 0) &&
				(Pos.Row < RCount))
			return true;
		return false;
	}

	boolean AssertLine(int Row) {
		if (Row == RCount)
			if (!InsLine(RCount, 0, false)) return false;
		return true;
	}

	boolean SetFileName(String AFileName, String AMode) {
		FileOk = false;

		FileName = AFileName;
		Mode = null;
		if (AMode != null)
			Mode = EMode.FindMode(AMode);
		if (Mode == null)
			Mode = EMode.GetModeForName(AFileName);
		assert(Mode != null);
		Flags = (Mode.Flags);

		HilitProc = null;
		if (Mode != null && Mode.fColorize != null)
			HilitProc = Hiliter.GetHilitProc(Mode.fColorize.SyntaxParser);

		UpdateTitle();
		return FileName != null;
	}

	public boolean SetPos(int Col, int Row) {
		return SetPos(Col, Row, 0);
	}	

	boolean SetPos(int Col, int Row, int tabMode) {
		assert (Col >= 0 && Row >= 0 && Row < VCount);

		if (BFI(this, BFI_Undo) && BFI(this, BFI_UndoMoves))
			getSlot().pushPosition(CP);


		if (AutoExtend) {
			BlockExtendBegin();
			AutoExtend = true;
		}
		PrevPos = new EPoint( CP );
		PrevPos.Row = (CP.Row < VCount) ? VToR(CP.Row) : (CP.Row - VCount + RCount);
		CP.Row = Row;
		CP.Col = Col;
		if (AutoExtend) {
			BlockExtendEnd();
			AutoExtend = true;
		}
		//        if (View && View.Model == this ) {
		//            View.GetVPort();
		//        }
		if (!BFI(this, BFI_CursorThroughTabs)) {
			if (tabMode == tmLeft) {
				if (MoveTabStart() == false) return false;
			} else if (tabMode == tmRight) {
				if (MoveTabEnd() == false) return false;
			}
		}
		if (ExtendGrab == 0 && !AutoExtend  && !BFI(this, BFI_PersistentBlocks)) {
			if (CheckBlock())
				if (BlockUnmark() == false)
					return false;
		}
		return true;
	}

	boolean SetPosR(int Col, int Row) {
		return SetPosR(Col, Row, 0);
	}

	boolean SetPosR(int Col, int Row, int tabMode) {
		assert (Row >= 0 && Row < RCount && Col >= 0);

		int L = RToV(Row);

		if (L == -1)
			if (!ExposeRow(Row)) return false;

		L = RToV(Row);

		return SetPos(Col, L, tabMode);
	}

	boolean SetNearPos(int Col, int Row) {
		return SetNearPos(Col, Row, 0);
	}	

	boolean SetNearPos(int Col, int Row, int tabMode) {
		if (Row >= VCount) Row = VCount - 1;
		if (Row < 0) Row = 0;
		if (Col < 0) Col = 0;
		return SetPos(Col, Row, tabMode);
	}

	boolean SetNearPosR(int Col, int Row) {
		return SetNearPosR( Col,  Row, 0);
	}	
	boolean SetNearPosR(int Col, int Row, int tabMode) {
		if (Row >= RCount) Row = RCount - 1;
		if (Row < 0) Row = 0;
		if (Col < 0) Col = 0;
		return SetPosR(Col, Row, tabMode);
	}

	boolean CenterPos(int Col, int Row) {
		return CenterPos( Col,  Row, 0);
	}	

	boolean CenterPos(int Col, int Row, int tabMode) {
		assert(Row >= 0 && Row < VCount && Col >= 0);

		if (!SetPos(Col, Row, tabMode)) return false;
		if (View != null && View.Model == this) {
			Row -= GetVPort().Rows / 2;
			if (Row < 0) Row = 0;
			Col -= GetVPort().Cols - 8;
			if (Col < 0) Col = 0;
			if (GetVPort().SetTop(Col, Row) == 0) return false;
			GetVPort().ReCenter = 1;
		}
		return true;
	}

	boolean CenterPosR(int Col, int Row) {
		return CenterPosR(Col, Row, 0);
	}

	boolean CenterPosR(int Col, int Row, int tabMode) {
		int L;

		assert(Row >= 0 && Row < RCount && Col >= 0);

		L = RToV(Row);
		if (L == -1)
			if (!ExposeRow(Row)) return false;
		L = RToV(Row);
		return CenterPos(Col, L, tabMode);
	}

	boolean CenterNearPos(int Col, int Row, int tabMode) {
		if (Row >= VCount) Row = VCount - 1;
		if (Row < 0) Row = 0;
		if (Col < 0) Col = 0;
		return CenterPos(Col, Row, tabMode);
	}

	boolean CenterNearPosR(int Col, int Row) {
		return CenterNearPosR( Col, Row, 0);
	}	

	boolean CenterNearPosR(int Col, int Row, int tabMode) {
		if (Row >= RCount) Row = RCount - 1;
		if (Row < 0) Row = 0;
		if (Col < 0) Col = 0;
		return CenterPosR(Col, Row, tabMode);
	}

	int LineLen(int Row) {
		assert(Row >= 0 && Row < RCount);
		ELine L = RLine(Row);
		return ScreenPos(L, L.getCount());
	}

	int LineChars(int Row) {
		assert(Row >= 0 && Row < RCount);
		return RLine(Row).getCount();
	}


	public boolean DelLine(int Row) { return DelLine(Row, true); }	

	boolean DelLine(int Row, boolean DoMark) {
		int VLine;
		int GapSize;
		//   printf("DelLine: %d\n", Row);
		if (Row < 0) return false;
		if (Row >= RCount) return false;
		if (!Modify()) return false;

		VLine = RToV(Row);
		if (VLine == -1)
			if (ExposeRow(Row) == false) return false;
		VLine = RToV(Row);
		assert(VLine != -1);

		if (FindFold(Row) != -1) {
			if (FoldDestroy(Row) == false) return false;
		} 

		VLine = RToV(Row);
		assert(VLine != -1);

		getSlot().
		pushString(RLine(Row).Chars).
		//pushInt(RLine(Row).getCount().
		pushInt(Row).
		pushOp( UndoOperation.ucDelLine );

		if (DoMark)
			UpdateMarker(umDelete, Row, 0, 1, 0);

		Draw(Row, -1);
		Hilit(Row);
		assert(RAllocated >= RCount);
		if (RGap != Row) 
			if (MoveRGap(Row) == false) return false;

		GapSize = RAllocated - RCount;

		//delete LL[RGap + GapSize];
		LL[RGap + GapSize] = null;
		RCount--;
		GapSize++;
		if (RAllocated - RAllocated / 2 > RCount) {
			//memmove(LL + RGap + GapSize - RAllocated / 3, LL + RGap + GapSize, sizeof(ELine) * (RCount - RGap));
			System.arraycopy(LL, RGap + GapSize, LL, RGap + GapSize - RAllocated / 3, RCount - RGap);
			if (Allocate(RAllocated - RAllocated / 3) == false) return false;
		}

		assert(VAllocated >= VCount);
		if (VGap != VLine)
			if (MoveVGap(VLine) == false) return false;
		GapSize = VAllocated - VCount;
		VV[VGap + GapSize] = 0;
		VCount--;
		GapSize++;
		if (VAllocated - VAllocated / 2 > VCount) {
			//memmove(VV + VGap + GapSize - VAllocated / 3,VV + VGap + GapSize,sizeof(VV[0]) * (VCount - VGap));
			System.arraycopy(VV, VGap + GapSize, VV, VGap + GapSize - VAllocated / 3, VCount - VGap);
			if (AllocVis(VAllocated - VAllocated / 3) == false) return false;
		}
		return true;
	}

	public boolean InsLine(int Row, int DoAppend) {
		return InsLine(Row, DoAppend, true);
	}	

	boolean InsLine(int Row, int DoAppend, boolean DoMark) {
		//ELine L;
		int VLine = -1;

		//    printf("InsLine: %d\n", Row);

		if (Row < 0) return false;
		if (Row > RCount) return false;
		if (!Modify()) return false;
		if (DoAppend!=0) {
			Row++;
		}
		if (Row < RCount) {
			VLine = RToV(Row);
			if (VLine == -1)
				if (ExposeRow(Row) == false) return false;
			VLine = RToV(Row);
			assert(VLine != -1);
		} else {
			VLine = VCount;
		}
		//ELine L = new ELine("");
		//if (L == 0) return false;

		getSlot().pushInt(Row).pushOp( UndoOperation.ucInsLine );

		if (DoMark)
			UpdateMarker(umInsert, Row, 0, 1, 0);
		Draw(Row, -1);
		Hilit(Row);
		VLine = RToVN(Row);
		assert(RCount <= RAllocated);
		//   printf("++ %d G:C:A :: %d - %d - %d\n", Row, RGap, RCount, RAllocated);
		if (RCount == RAllocated) {
			if (Allocate(RCount != 0 ? (RCount * 2) : 1) == false) return false;
			//memmove(LL + RAllocated - (RCount - RGap), LL + RGap, sizeof(ELine) * (RCount - RGap));
			System.arraycopy(LL, RGap, LL, RAllocated - (RCount - RGap), RCount - RGap);
		}
		if (RGap != Row)
			if (MoveRGap(Row) == false) return false;
		LL[RGap] = new ELine("");
		RGap++;
		RCount++;
		//    printf("-- %d G:C:A :: %d - %d - %d\n", Row, RGap, RCount, RAllocated);

		assert(VCount <= VAllocated);
		if (VCount == VAllocated) {
			if (AllocVis(VCount != 0 ? (VCount * 2) : 1) == false) return false;
			//memmove(VV + VAllocated - (VCount - VGap), VV + VGap, sizeof(VV[0]) * (VCount - VGap));
			System.arraycopy( VV, VGap, VV,  VAllocated - (VCount - VGap), VCount - VGap);
		}
		if (VGap != VLine)
			if (MoveVGap(VLine) == false) return false;
		VV[VGap] = Row - VGap;
		VGap++;
		VCount++;

		/*    if (AllocVis(VCount + 1) == false) return false;
   memmove(VV + VLine + 1, VV + VLine, sizeof(VV[0]) * (VCount - VLine));
   VCount++;
   Vis(VLine, Row - VLine);*/
		return true;
	}

	public boolean DelChars(int Row, int Ofs, int ACount) 
	{
		if (Row < 0) return false;
		if (Row >= RCount) return false;
		ELine L = RLine(Row);

		if (Ofs < 0) return false;
		if (Ofs >= L.getCount()) return false;
		if (Ofs + ACount >= L.getCount())
			ACount = L.getCount() - Ofs;
		if (ACount == 0) return true;

		if (!Modify()) return false;

		/* -----------------------------------------------
		if (BFI(this, BFI_Undo) == 1) {
			if (PushUData(L.Chars + Ofs, ACount) == false) return false;
			if (PushULong(ACount) == false) return false;
			if (PushULong(Ofs) == false) return false;
			if (PushULong(Row) == false) return false;
			if (PushUChar(ucDelChars) == false) return false;
		}
		#endif */

		getSlot().
		pushString( L.Chars, Ofs, ACount ).
		pushInt(Ofs).
		pushInt(Row).
		pushOp( UndoOperation.ucDelChars);

		/*
		if (L.getCount() > Ofs + ACount)
			memmove(L.Chars + Ofs, L.Chars + Ofs + ACount, L.getCount() - Ofs - ACount);
		L.getCount() -= ACount;
		if (L.Allocate(L.getCount()) == false) return false;
		 */

		if (L.getCount() > Ofs + ACount)
			L.memmove(Ofs, Ofs + ACount, L.getCount() - Ofs - ACount);

		L.Allocate(L.getCount() - ACount);

		Draw(Row, Row);
		Hilit(Row);
		//   printf("OK\n");
		return true;
	}

	public boolean InsChars(int Row, int Ofs, int ACount, String Buffer) 
	{
		assert(Row >= 0 && Row < RCount && Ofs >= 0);
		ELine L = RLine(Row);

		if (Ofs < 0) return false;
		if (Ofs > L.getCount()) return false;
		if (ACount == 0) return true;

		if (!Modify()) return false;

		getSlot().
		pushInt(Row).
		pushInt(Ofs).
		pushInt(ACount).
		pushOp( UndoOperation.ucInsChars);

		L.Allocate(L.getCount() + ACount);
		if (L.getSize() > Ofs)
			//memmove(L.Chars + Ofs + ACount, L.Chars + Ofs, L.getCount() - Ofs);
			//L.memmove(Ofs + ACount, Ofs, L.getCount() - Ofs - 1); // [dz] added -1 - was error in orig code?
			L.memmove(Ofs + ACount, Ofs, L.getCount() - Ofs - ACount); // [dz] added -ACount - was error in orig code?
		if (Buffer == null) 
			//memset(L.Chars + Ofs, ' ', ACount);
			L.memset(Ofs, ' ', ACount);
		else
			//memmove(L.Chars + Ofs, Buffer, ACount);
			L.copyIn(Ofs, Buffer, ACount);

		//L.getCount() += ACount;
		Draw(Row, Row);
		Hilit(Row);
		// printf("OK\n");
		return true;
	}

	boolean UnTabPoint(int Row, int Col) {
		ELine L;
		int Ofs, Pos, TPos;

		assert(Row >= 0 && Row < RCount && Col >= 0);
		L = RLine(Row);
		Ofs = CharOffset(L, Col);
		if (Ofs >= L.getCount())
			return true;
		if (L.charAt(Ofs) != '\t')
			return true;
		Pos = ScreenPos(L, Ofs);
		if (Pos < Col) {
			TPos = NextTab(Pos, iBFI(this, BFI_TabSize));
			if (!DelChars(Row, Ofs, 1))
				return false;
			if (!InsChars(Row, Ofs, TPos - Pos, null))
				return false;
		}
		return true;
	}

	boolean ChgChars(int Row, int Ofs, int ACount, String Buffer) {
		ELine L;

		assert(Row >= 0 && Row < RCount && Ofs >= 0);
		L = RLine(Row);

		if (Ofs < 0) return false;
		if (Ofs > L.getCount()) return false;
		if (ACount == 0) return true;

		if (!Modify()) return false;

		getSlot().
		pushString(L.Chars, Ofs, ACount).
		pushInt(Ofs).
		pushInt(Row).
		pushOp( UndoOperation.ucDelChars).
		pushInt(Row).
		pushInt(Ofs).
		pushInt(ACount).
		pushOp( UndoOperation.ucInsChars);

		Hilit(Row);
		Draw(Row, Row);
		return true;
	}

	boolean DelText(int Row, int Col, int ACount) {
		return DelText(Row, Col, ACount, true);
	}

	boolean DelText(int Row, int Col, int ACount, boolean DoMark) {
		int L, B, C;

		//   printf("DelTExt: %d:%d %d\n", Row, Col, ACount);

		assert(Row >= 0 && Row < RCount && Col >= 0);
		if (!Modify()) return false;

		if (ACount == 0) return true;
		L = LineLen(Row);
		if (Col >= L)
			return true;
		if (ACount == -1 || ACount + Col > L)
			ACount = L - Col;
		if (UnTabPoint(Row, Col) == false)
			return false;
		if (UnTabPoint(Row, Col + ACount) == false)
			return false;
		B = CharOffset(RLine(Row), Col);
		C = CharOffset(RLine(Row), Col + ACount);
		if ((ACount > 0) && (B != -1) && (C != -1)) {
			if (DelChars(Row, B, C - B) == false) return false;
			if (DoMark) UpdateMarker(umDelete, Row, Col, 0, ACount);
		}
		//   printf("OK\n");
		return true;
	}


	public boolean InsText(int Row, int Col, int ACount, String ABuffer) {
		return InsText(Row, Col, ACount, ABuffer, true );
	}	

	boolean InsText(int Row, int Col, int ACount, String ABuffer, boolean DoMark) {
		int B, L;

		assert(Row >= 0 && Row < RCount && Col >= 0 && ACount >= 0);
		if (ACount == 0) return true;
		if (!Modify()) return false;

		if (DoMark) UpdateMarker(umInsert, Row, Col, 0, ACount);
		L = LineLen(Row);
		if (L < Col) {
			if (!InsChars(Row, RLine(Row).getCount(), Col - L, null))
				return false;
		} else
			if (UnTabPoint(Row, Col) == false) return false;
		B = CharOffset(RLine(Row), Col);
		if (InsChars(Row, B, ACount, ABuffer) == false) return false;

		return true;
	}

	boolean PadLine(int Row, int Length) {
		int L;

		L = LineLen(Row);
		if (L < Length)
			if (!InsChars(Row, RLine(Row).getCount(), Length - L, null))
				return false;
		return true;
	}

	boolean InsLineText(int Row, int Col, int ACount, int LCol, ELine Line) {
		int Ofs, Pos, TPos, C, B, L;

		//fprintf(stderr, "\n\nInsLineText: %d:%d %d %d", Row, Col, ACount, LCol);
		assert(Row >= 0 && Row < RCount && Col >= 0 && LCol >= 0);
		if (iBFI(this, BFI_ReadOnly) == 1)
			return false;

		L = ScreenPos(Line, Line.getCount());
		if (LCol >= L) return true;
		if (ACount == -1) ACount = L - LCol;
		if (ACount + LCol > L) ACount = L - LCol;
		if (ACount == 0) return true;
		assert(ACount > 0);

		B = Ofs = CharOffset(Line, LCol);
		if (Ofs < Line.getCount() && Line.charAt(Ofs) == '\t') {
			Pos = ScreenPos(Line, Ofs);
			if (Pos < LCol) {
				TPos = NextTab(Pos, iBFI(this, BFI_TabSize));
				if (!InsText(Row, Col, TPos - LCol, null, false))
					return false;
				Col += TPos - LCol;
				ACount -= TPos - LCol;
				LCol = TPos;
				B++;
			}
		}
		C = Ofs = CharOffset(Line, LCol + ACount);
		if (Ofs < Line.getCount() && Line.charAt(Ofs) == '\t') {
			Pos = ScreenPos(Line, Ofs);
			if (Pos < LCol + ACount) {
				if (!InsText(Row, Col, LCol + ACount - Pos, null, false))
					return false;
			}
		}
		//fprintf(stderr, "B = %d, C = %d\n", B, C);
		C -= B;
		if (C <= 0) return true;
		String ss = Line.substring(B); // Line.Chars + B
		if (InsText(Row, Col, C, ss) == false) return false;
		//   printf("OK\n");
		return true;
	}

	boolean SplitLine(int Row, int Col) {
		int VL;

		assert(Row >= 0 && Row < RCount && Col >= 0);

		if (iBFI(this, BFI_ReadOnly) == 1) return false;

		VL = RToV(Row);
		if (VL == -1) 
			if (!ExposeRow(Row)) return false;
		if (Row > 0) {
			VL = RToV(Row - 1);
			if (VL == -1)
				if (!ExposeRow(Row - 1)) return false;
		}
		VL = RToV(Row);
		assert(VL != -1);
		if (Col == 0) {
			if (!InsLine(Row, 0, true)) return false;
		} else {
			UpdateMarker(umSplitLine, Row, Col, 0, 0);
			if (!InsLine(Row, 1, false)) return false;
			RLine(Row).StateE = (short)((Row > 0) ? RLine(Row - 1).StateE : 0);
			if (Col < LineLen(Row)) 
			{
				//if (RLine(Row).ExpandTabs(Col, -2, &Flags) == false) return false;
				if (!UnTabPoint(Row, Col))
					return false;

				int P = CharOffset(RLine(Row), Col);
				int L = LineLen(Row);

				String ss = RLine(Row).substring(P); // RLine(Row).Chars + P
				if (InsText(Row + 1, 0, RLine(Row).getCount() - P, ss, false) == false) return false;
				if (!DelText(Row, Col, L - Col, false)) return false;
			}
		}
		Draw(Row, -1);
		Hilit(Row);
		return true;
	}

	boolean JoinLine(int Row, int Col) {
		int Len, VLine;

		if (BFI(this, BFI_ReadOnly)) return false;
		if (Row < 0 || Row >= RCount - 1) return false;
		if (Col < 0) return false;
		Len = LineLen(Row);
		if (Col < Len) Col = Len;
		VLine = RToV(Row);
		if (VLine == -1) {
			if (!ExposeRow(Row)) return false;
			if (!ExposeRow(Row + 1)) return false;
		}
		VLine = RToV(Row);
		if (Col == 0 && RLine(Row).getCount() == 0) {
			if (!DelLine(Row, true)) return false;
		} else {
			if (!InsText(Row, Col, RLine(Row + 1).getCount(), RLine(Row + 1).Chars.toString(), false)) return false;
			if (!DelLine(Row + 1, false)) return false;
			UpdateMarker(umJoinLine, Row, Col, 0, 0);
		}
		Draw(Row, -1);
		Hilit(Row);
		return true;
	}


































	int ScreenPos(ELine L, int Offset) {
		boolean ExpandTabs = BFI(this, BFI_ExpandTabs);
		int TabSize = iBFI(this, BFI_TabSize);

		if (!ExpandTabs) {
			return Offset;
		} else {
			//String p = L.Chars;
			//ArrayPtr<Character> p = L.getPointer();
			CStringPtr p = L.getPointer();
			int Len = L.getCount();
			int Pos = 0;
			int Ofs = Offset;

			if (Ofs > Len) {
				while (Len > 0) {
					//if (*p++ != '\t')
					if (p.r() != '\t')
						Pos++;
					else
						Pos = NextTab(Pos, TabSize);
					Len--;
					p.inc();
				}
				Pos += Ofs - L.getCount();
			} else {
				while (Ofs > 0) {
					//if (*p++ != '\t')
					if (p.r() != '\t')
						Pos++;
					else
						Pos = NextTab(Pos, TabSize);
					Ofs--;
					p.inc();
				}
			}
			return Pos;
		}
	}



	int CharOffset(ELine L, int ScreenPos) {
		boolean ExpandTabs = BFI(this, BFI_ExpandTabs);
		int TabSize = iBFI(this, BFI_TabSize);

		if (!ExpandTabs) {
			return ScreenPos;
		} else {
			int Pos = 0;
			int Ofs = 0;
			//String p = L.Chars;
			//ArrayPtr<Character> p = L.getPointer();
			CStringPtr p = L.getPointer();
			int Len = L.getCount();

			while (Len > 0) {
				//if (*p++ != '\t')
				if (p.rpp() != '\t')
					Pos++;
				else
					Pos = NextTab(Pos, TabSize);
				if (Pos > ScreenPos)
					return Ofs;
				Ofs++;
				Len--;
			}
			return Ofs + ScreenPos - Pos;
		}
	}


	boolean Allocate(int ACount) {
		//ELine [] L = (ELine ) realloc(LL, sizeof(ELine) * (ACount + 1));

		if(LL == null)
		{
			LL = new ELine [ACount];
			Arrays.fill(LL, null);
		}
		else
			LL = Arrays.copyOf(LL, ACount);			

		RAllocated = ACount;

		return true;
	}

	boolean MoveRGap(int RPos) {
		int GapSize = RAllocated - RCount;

		if (RGap == RPos) return true;
		if (RPos < 0 || RPos > RCount) return false;

		if (RGap < RPos) {
			if (RPos - RGap == 1) {
				LL[RGap] = LL[RGap + GapSize];
			} else {
				//memmove(LL + RGap, LL + RGap + GapSize,	sizeof(ELine) * (RPos - RGap));
				System.arraycopy(LL, RGap + GapSize, LL, RGap, RPos - RGap);
			}
		} else {
			if (RGap - RPos == 1) {
				LL[RPos + GapSize] = LL[RPos];
			} else {
				//memmove(LL + RPos + GapSize, LL + RPos,	sizeof(ELine) * (RGap - RPos));
				System.arraycopy(LL, RPos, LL, RPos + GapSize, RGap - RPos);
			}
		}
		RGap = RPos;
		return true;
	}

	boolean AllocVis(int ACount) {
		VAllocated = ACount;
		if(VV==null)
			VV = new int[ACount];
		else
			VV = Arrays.copyOf(VV, ACount);
		return true;
	}

	boolean MoveVGap(int VPos) {
		int GapSize = VAllocated - VCount;

		if (VGap == VPos) return true;
		if (VPos < 0 || VPos > VCount) return false;

		if (VGap < VPos) {
			if (VPos - VGap == 1) {
				VV[VGap] = VV[VGap + GapSize];
			} else {
				//memmove(VV + VGap, VV + VGap + GapSize, sizeof(VV[0]) * (VPos - VGap));
				System.arraycopy( VV, VGap + GapSize, VV, VGap, VPos - VGap );
			}
		} else {
			if (VGap - VPos == 1) {
				VV[VPos + GapSize] = VV[VPos];
			} else {
				//memmove(VV + VPos + GapSize, VV + VPos, sizeof(VV[0]) * (VGap - VPos));
				System.arraycopy( VV, VPos, VV, VPos + GapSize, VGap - VPos );
			}
		}
		VGap = VPos;
		return true;
	}

	int RToV(int No) {
		int L = 0, R = VCount, M, V;

		if (No > Vis(VCount - 1) + VCount - 1)   // beyond end
			return -1;
		if (No < VCount) // no folds before (direct match)
			if (Vis(No) == 0) return No;

		while (L < R) {
			M = (L + R) >> 1;
		V = Vis(M) + M;
		if (V == No)
			return M;
		else if (V > No)
			R = M;
		else
			L = M + 1;
		}
		return -1;
	}

	int RToVN(int No) {
		int L = 0, R = VCount, M, V;

		if (No == RCount)
			return VCount;
		if (No > Vis(VCount - 1) + VCount - 1) 
			return VCount - 1;
		if (No < VCount)
			if (Vis(No) == 0) return No;

		while (L < R) {
			M = (L + R) >> 1;
		V = Vis(M) + M;
		if (V == No)
			return M;
		else if (V > No)
			R = M;
		else {
			if (M == VCount - 1)
				return M;
			else if (Vis(M + 1) + M + 1 > No)
				return M;
			L = M + 1;
		}
		}
		return R;
	}

























	ELine RLine(int No) {
		if(Main.DEBUG_EDITOR)
		{
			int N = GapLine(No, RGap, RCount, RAllocated);
			if (!((No < RCount) && (No >= 0) && (LL[N]!=null))) {
				printf("Get No = %d/%d Gap=%d RAlloc = %d, VCount = %d\n", No, RCount, RGap, RAllocated, VCount);
				assert((No < RCount) && (No >= 0) && (LL[N]!=null));
			}
		}
		return LL[GapLine(No, RGap, RCount, RAllocated)];
	}

	void RLine(int No, ELine L) {
		if(Main.DEBUG_EDITOR)
		{
			if (!((No >= 0))) printf("Set No = %d\n", No);
			assert((No >= 0));
		}
		LL[GapLine(No, RGap, RCount, RAllocated)] = L;
	}

	int Vis(int No) {
		if(Main.DEBUG_EDITOR)
		{
			if (No < 0 || No >= VCount) {
				printf("Vis get no %d of %d\n", No, VCount);
				assert (No >= 0 && No < VCount);
			}
		}
		return VV[GapLine(No, VGap, VCount, VAllocated)];
	}

	void Vis(int No, int V) {
		if(Main.DEBUG_EDITOR)
		{
			if (No < 0 || No >= VCount) {
				printf("Vis set no %d of %d to %d\n", No, VCount, V);
				assert (No >= 0 && No < VCount);
			}
		}
		VV[GapLine(No, VGap, VCount, VAllocated)] = V;
	}

	ELine VLine(int No) {
		if(Main.DEBUG_EDITOR)
		{
			if (!((No < VCount) && (No >= 0))) {
				printf("VGet No = %d\n", No);
				assert((No < VCount) && (No >= 0));
			}
			if (Vis(No) < 0)
				assert(1 == 0);
		}
		return RLine(No + Vis(No));
	}

	void VLine(int No, ELine L) {
		if(Main.DEBUG_EDITOR)
		{
			if (!((No >= 0))) {
				printf("VSet No = %d\n", No);
				assert((No >= 0));
			}
			if (VV[No] < 0)
				assert(1 == 0);
		}
		RLine(No + Vis(No), L);
	}

	int VToR(int No) {
		if(Main.DEBUG_EDITOR)
		{
			if (!(No < VCount)) {
				printf("Get No = %d\n", No);
				assert((No < VCount));
			}
		}
		return No + Vis(No);
	}









	EEditPort GetViewVPort(EView V) {
		return (EEditPort )V.Port;
	}

	EEditPort GetVPort() {
		return (EEditPort)View.Port;
	}


	void printf(String f, Object... o)
	{
		System.out.printf(f, o);
	}





























	/*
	boolean ExposeRow(int Row) { 
		int level, oldlevel = 100;

		//DumpFold();

		assert(Row >= 0 && Row < RCount); // range

		int V = RToV(Row);
		if (V != -1) return true; // already exposed

		/* 
		int f = FindNearFold(Row);
		assert(f != -1); // if not visible, must be folded

		while (f >= 0) {
			level = FF[f].level;
			if (level < oldlevel) {
				if (FF[f].open == 0) {
					//	                printf("opening fold %d\n", f);
					if (FoldOpen(FF[f].line) == 0) return false;
				}
				oldlevel = level;
			}
			f--;
			if (level == 0) break;
		}
		 * /

		V = RToV(Row);
		//	    if (V == -1) {
		//	        printf("Expose Row = %d\n", Row);
		//	        DumpFold();
		//	    }
		assert (V != -1);
		return true;
	} */





	boolean GetMap(int Row, int []StateLen, int /*hsState*/ [][]StateMap) {
		int/*hlState*/ State = 0;

		Rehilit(Row);

		StateLen[0] = LineChars(Row);
		if (Row > 0) State = RLine(Row - 1).StateE;
		if (StateLen[0] > 0) {
			ELine L = RLine(Row);
			int [] ECol = {0};

			StateMap[0] = new int[StateLen[0]];
			if (StateMap[0] == null) return false;

			if (BFI(this, BFI_HilitOn) && HilitProc != null)
				//HilitProc(this, Row, 0, 0, *StateLen, L, State, *StateMap, &ECol);
				HilitProc.proc(this, Row, null, 0, StateLen[0], L, State, StateMap[0], ECol);
			else
				Hilit_Plain(this, Row, null, 0, StateLen[0], L, State, StateMap[0], ECol);
			if (L.StateE != State) {
				L.StateE = State;
			}
		} else {
			StateLen[0] = 1;
			StateMap[0] = new int[1];
			//if (*StateMap == 0) return false;
			StateMap[0][0] = (byte)(State & 0xFF);
		}
		return true;
	}

	void FullRedraw() { // redraw all views
		EView V = View;
		EEditPort W;
		int Min, Max;

		while (V != null) {
			W = GetViewVPort(V);
			// Need to use real lines, not virtual
			// (similar to HilitMatchBracket)
			Min = VToR(W.TP.Row);
			Max = W.TP.Row + W.Rows;
			if (Max >= VCount)
				Max = RCount;
			else
				Max = VToR(Max);
			Draw(Min, Max);
			V = V.Next;
			if (V == View)
				break;
		}
	}

	void Hilit(int FromRow) {
		if (FromRow != -1) {
			if (StartHilit == -1)
				StartHilit = FromRow;
			else if (FromRow < StartHilit)
				StartHilit = FromRow;
		}
	}

	void Rehilit(int ToRow) {
		int /*hlState*/ State;
		int HilitX;
		//ELine L;
		int [] ECol = {0};

		if (StartHilit == -1)   // all ok
			return ;

		if (!BFI(this, BFI_MultiLineHilit)) // everything handled in redisplay
			return;

		if (ToRow <= StartHilit) // will be handled in redisplay
			return;

		if (ToRow >= RCount)
			ToRow = RCount;

		HilitX = 1;
		while ((StartHilit < RCount) && ((StartHilit < ToRow) || HilitX != 0 )) {
			ELine L = RLine(StartHilit);

			HilitX = 0;
			if (StartHilit > 0)
				State = RLine(StartHilit - 1).StateE;
			else
				State = 0;

			if (BFI(this, BFI_HilitOn) && HilitProc != null)
				HilitProc.proc(this, StartHilit, null, 0, 0, L, State, null, ECol);
			else 
				Hilit_Plain(this, StartHilit, null, 0, 0, L, State, null, ECol);

			if (L.StateE != State) {
				HilitX = 1;
				L.StateE = State;
			}
			Draw(StartHilit, StartHilit);  // ?
			if (StartHilit > EndHilit)
				EndHilit = StartHilit;
			if (HilitX == 0) // jump over (can we always do this ?)
				if (StartHilit < EndHilit) {
					StartHilit = EndHilit;
				}
			StartHilit++;
		}
	}

	void Draw(int Row0, int RowE) {
		//    printf("r0 = %d, re = %d\n", Row0, RowE);
		//    printf("m = %d, max = %d, rts = %d\n", MinRedraw, MaxRedraw, RedrawToEos);
		if (Row0 == -1) Row0 = 0;
		if ((Row0 < MinRedraw) || (MinRedraw == -1)) {
			MinRedraw = Row0;
			if (MaxRedraw == -1) MaxRedraw = MinRedraw;
		}
		if (RowE == -1) {
			RedrawToEos = 1;
			MaxRedraw = MinRedraw;
		} else if (((RowE > MaxRedraw) || (MaxRedraw == -1)) && (RowE != -1))
			MaxRedraw = RowE;
		//    printf("m = %d, max = %d, rts = %d\n", MinRedraw, MaxRedraw, RedrawToEos);
	}

	void DrawLine(TDrawBuffer B, int VRow, int C, int W, int [] HilitX) {
		int /*hlState*/ State;
		int StartPos, EndPos;

		HilitX = null;
		B.MoveChar( 0, W, ' ', hcPlain_Background, W);
		//    if ((VRow == VCount - 1) && !BFI(this, BFI_ForceNewLine)) {
		// if (BFI(this, BFI_ShowMarkers))
		//     MoveChar(B, 0, W, EOF_MARKER, hcPlain_Markers, W);
		//    }
		if (VRow < VCount) {
			int Row = VToR(VRow);
			ELine L = RLine(Row);
			int ECol = 0;

			int [] ecp = {0};
			if (Row > 0) State = RLine(Row - 1).StateE;
			else State = 0;

			if (BFI(this, BFI_HilitOn) && HilitProc != null)
				HilitProc.proc(this, Row, B, C, W, L, State, null, ecp);
			else
				Hilit_Plain(this, Row, B, C, W, L, State, null, ecp);
			ECol = ecp[0];

			if (L.StateE != State) {
				HilitX[0] = 1;
				L.StateE = State;
			}
			if (BFI(this, BFI_ShowMarkers)) {
				B.MoveChar(ECol - C, W, Console.ConGetDrawChar((Row == RCount - 1) ? DCH_EOF : DCH_EOL), hcPlain_Markers, 1);
				ECol += 1;
			}
			if (Row < RCount) {
				int Folded = 0;
				//static char fold[20];
				//int l;

				int f = FindFold(Row);
				if (f != -1) {
					int foldColor;
					if (FF[f].level<5) foldColor=hcPlain_Folds[FF[f].level];
					else foldColor=hcPlain_Folds[4];
					if (FF[f].open) {
						String fold = String.format("[%d]", FF[f].level);
						B.MoveStr(ECol - C + 1, W, fold, foldColor, 10);
						ECol += fold.length();
					} else {
						if (VRow < VCount - 1) {
							Folded = Vis(VRow + 1) - Vis(VRow) + 1;
						} else if (VRow < VCount) {
							Folded = RCount - (VRow + Vis(VRow));
						}
						String fold = String.format("(%d:%d)", FF[f].level, Folded);
						B.MoveStr( ECol - C + 1, W, fold, foldColor, 10);
						ECol += fold.length();
						B.MoveAttr(0, W, foldColor, W);
					}
				} //*/
			}
			if (BB.Row != -1 && BE.Row != -1 && Row >= BB.Row && Row <= BE.Row) {
				switch(BlockMode) {
				case bmLine:
					StartPos = 0;
					if (Row == BE.Row) EndPos = 0;
					else EndPos = W;
					break;
				case bmColumn:
					StartPos = BB.Col - C;
					if (Row == BE.Row) EndPos = BB.Col - C;
					else EndPos = BE.Col - C;
					break;
				case bmStream:
					if (Row == BB.Row && Row == BE.Row) {
						StartPos = BB.Col - C;
						EndPos = BE.Col - C;
					} else if (Row == BB.Row) {
						StartPos = BB.Col - C;
						EndPos = W;
					} else if (Row == BE.Row) {
						StartPos = 0;
						EndPos = BE.Col - C;
					} else {
						StartPos = 0;
						EndPos = W;
					}
					break;
				default:
					StartPos = EndPos = 0;
					break;
				}
				if(BFI(this, BFI_SeeThruSel))
					B.MoveBgAttr( StartPos, W, hcPlain_Selected, EndPos - StartPos);
				else
					B.MoveAttr( StartPos, W, hcPlain_Selected, EndPos - StartPos);
			}

			if (BFI(this, BFI_ShowBookmarks)) 
			{
				//int i = 0;
				//String Name;
				//EPoint P;

				List<EBookmark> ret = GetBookmarksForLine(Row);

				//while ((i = GetBookmarkForLine(i, Row, Name, P)) != -1) 
				for( EBookmark b : ret )
				{
					if( b.Name.substring(0,4).equals("_BMK"))
					{
						// User bookmark, hilite line
						if (BFI(this, BFI_SeeThruSel))
							B.MoveBgAttr( 0, W, hcPlain_Bookmark, W);
						else
							B.MoveAttr( 0, W, hcPlain_Bookmark, W);
						break;
					}
				}
			}

			if (Match.Row != -1 && Match.Col != -1) {
				if (Row == Match.Row) {
					if(BFI(this, BFI_SeeThruSel))
						B.MoveBgAttr( Match.Col - C, W, hcPlain_Found, MatchLen);
					else
						B.MoveAttr( Match.Col - C, W, hcPlain_Found, MatchLen);
				}
			}
		} else if (VRow == VCount) {
			if(BFI(this, BFI_ShowMarkers))
				B.MoveChar( 0, W, Console.ConGetDrawChar(DCH_END), hcPlain_Markers, W);
		}
	}

	void Redraw() {
		//int HilitX;
		//EView V;
		EEditPort W;
		int Row;
		TDrawBuffer dB = new TDrawBuffer();
		//char s[256];
		int /*ChColor*/ SColor;
		int RowA, RowZ;

		{
			int [] W1 = {0}, H1 = {0};
			if (!(View != null && View.MView != null))
				return;
			View.MView.ConQuerySize(W1, H1);

			if (H1[0] < 1 || W1[0] < 1) return;
		}
		//    printf("Redraw\n");
		if (CP.Row >= VCount) CP.Row = VCount - 1;
		if (CP.Row < 0) CP.Row = 0;

		CheckBlock();
		//V = View; /* check some window data */
		//if (V==null) 
		if (View==null) 
		{
			MinRedraw = MaxRedraw = -1;
			RedrawToEos = 0;
			return;
		}
		if (View == null|| View.MView == null || View.MView.Win == null)
			return ;

		//for ( ; V != null; V = V.NextView) 
		for ( EView V : views ) 
		{
			//        printf("Checking\x7\n");
			if (V.Model != this)
				assert(1 == 0);

			W = GetViewVPort(V);

			if (W.Rows < 1 || W.Cols < 1)
				continue;

			if (V == View) {
				int scrollJumpX = Math.min(Config.ScrollJumpX, W.Cols / 2);
				int scrollJumpY = Math.min(Config.ScrollJumpY, W.Rows / 2);
				int scrollBorderX = Math.min(Config.ScrollBorderX, W.Cols / 2);
				int scrollBorderY = Math.min(Config.ScrollBorderY, W.Rows / 2);

				W.CP = CP;
				TP = W.TP;

				if (W.ReCenter!=0) {
					W.TP.Row = CP.Row - W.Rows / 2;
					W.TP.Col = CP.Col - W.Cols + 8;
					W.ReCenter = 0;
				}

				if (W.TP.Row + scrollBorderY > CP.Row) W.TP.Row = CP.Row - scrollJumpY + 1 - scrollBorderY;
				if (W.TP.Row + W.Rows - scrollBorderY <= CP.Row) W.TP.Row = CP.Row - W.Rows + 1 + scrollJumpY - 1 + scrollBorderY;
				if (!Config.WeirdScroll)
					if (W.TP.Row + W.Rows >= VCount) W.TP.Row = VCount - W.Rows;
				if (W.TP.Row < 0) W.TP.Row = 0;

				if (W.TP.Col + scrollBorderX > CP.Col) W.TP.Col = CP.Col - scrollJumpX - scrollBorderX;
				if (W.TP.Col + W.Cols - scrollBorderX <= CP.Col) W.TP.Col = CP.Col - W.Cols + scrollJumpX + scrollBorderX;
				if (W.TP.Col < 0) W.TP.Col = 0;

				if (W.OldTP.Row != -1 && W.OldTP.Col != -1 && RedrawToEos == 0) {

					if ((W.OldTP.Row != W.TP.Row) || (W.OldTP.Col != W.TP.Col)) {
						int A, B;
						int DeltaX, DeltaY;
						int Rows = W.Rows;
						int Delta1 = 0, Delta2 = 0;

						DeltaY = W.TP.Row - W.OldTP.Row ;
						DeltaX = W.TP.Col - W.OldTP.Col;

						if ((DeltaX == 0) && (-Rows < DeltaY) && (DeltaY < Rows)) {
							if (DeltaY < 0) {
								W.ScrollY(DeltaY);
								A = W.TP.Row;
								B = W.TP.Row - DeltaY;
							} else {
								W.ScrollY(DeltaY);
								A = W.TP.Row + Rows - DeltaY;
								B = W.TP.Row + Rows;
							}
						} else {
							A = W.TP.Row;
							B = W.TP.Row + W.Rows;
						}
						if (A >= VCount) {
							Delta1 = A - VCount + 1;
							A = VCount - 1;
						}
						if (B >= VCount) {
							Delta2 = B - VCount + 1;
							B = VCount - 1;
						}
						if (A < 0) A = 0;
						if (B < 0) B = 0;
						Draw(VToR(A) + Delta1, VToR(B) + Delta2);
					}
				} else {
					int A = W.TP.Row;
					int B = A + W.Rows;
					int Delta = 0;

					if (B > VCount) {
						Delta += B - VCount;
						B = VCount;
					}
					int LastV = VToR(VCount - 1);
					int B1 = (B == VCount) ? RCount : VToR(B);

					if (B1 >= LastV) {
						Delta += B1 - LastV;
						B1 = LastV;
					}
					if (B1 < 0) B1 = 0;
					Draw(VToR(A), B1 + Delta);
				}

				W.OldTP = W.TP;
				TP = W.TP;
			}
			if (W.CP.Row >= VCount) W.CP.Row = VCount - 1;
			if (W.CP.Row < 0) W.CP.Row = 0;
			if (W.TP.Row > W.CP.Row) W.TP.Row = W.CP.Row;
			if (W.TP.Row < 0) W.TP.Row = 0;

			if (V.MView.IsActive()) // hack
				SColor = hcStatus_Active;
			else
				SColor = hcStatus_Normal;
			dB.MoveChar( 0, W.Cols, ' ', SColor, W.Cols);

			if (V.MView.Win.GetViewContext() == V.MView) {
				V.MView.Win.SetSbVPos(W.TP.Row, W.Rows, VCount + (Config.WeirdScroll ? W.Rows - 1 : 0));
				V.MView.Win.SetSbHPos(W.TP.Col, W.Cols, 1024 + (Config.WeirdScroll ? W.Cols - 1 : 0));
			}

			if (V.CurMsg == null) {
				{
					int CurLine = W.CP.Row;
					int ActLine = VToR(W.CP.Row);
					int CurColumn = W.CP.Col;
					int CurPos = CharOffset(RLine(ActLine), CurColumn);
					int NumLines = RCount;
					int NumChars = RLine(ActLine).getCount();
					//            int NumColumns = ScreenPos(Line(CurLine), NumChars);
					String fName = FileName;
					char CurCh = 0xFF;
					int lf = fName.length();
					String CCharStr; //[20] = "";

					if (lf > 34) fName += lf - 34;

					if (CurPos < NumChars) {
						CurCh = VLine(CurLine).Chars.charAt(CurPos);
						CCharStr = String.format("%3d,%02X", (int)CurCh, (int)CurCh);
					} else {
						if (CurPos > NumChars) CCharStr = "      ";
						else if (CurLine < NumLines - 1) CCharStr = "   EOL";
						else CCharStr = "   EOF";
					}

					/*
					String s = String.format( "%04d:%02d %c%c%c%c %.6s %c"
							//#ifdef DOS
							//                        " %lu "
							//#endif
							,
							//                    CurLine + 1,
							ActLine + 1,
							CurColumn + 1,
							//                    CurPos + 1,
							(BFI(this, BFI_Insert)) ? 'I' : ' ',
									(BFI(this, BFI_AutoIndent)) ? 'A' : ' ',
											//  (BFI(this, BFI_ExpandTabs))?'T':' ',
											(BFI(this, BFI_MatchCase)) ? 'C' : ' ',
													AutoExtend ?
															(
																	(BlockMode == bmStream) ? 's' :
																		(BlockMode == bmLine) ? 'l' : 'c'
																	) :
																		(
																				(BlockMode == bmStream) ? 'S' :
																					(BlockMode == bmLine) ? 'L': 'C'
																				),
																		/*  #ifdef CONFIG_WORDWRAP
	                        (BFI(this, BFI_WordWrap) == 3) ? 't' :
	                        (BFI(this, BFI_WordWrap) == 2) ? 'W' :
	                        (BFI(this, BFI_WordWrap) == 1) ? 'w' :
	                        ' ',
							#endif * /
																		//                    (BFI(this, BFI_Undo))?'U':' ',
																		//                    (BFI(this, BFI_Trim))?'E':' ',
																		//                    (Flags.KeepBackups)?'B':' ',
																		Mode.fName,
																		(Modified != 0)?'*':(BFI(this, BFI_ReadOnly))?'%':' '
							);

					 */

					String s = String.format( "%04d:%02d ",// %c%c%c%c %.6s %c"
							ActLine + 1,
							CurColumn + 1 );


					s += (BFI(this, BFI_Insert)) ? 'I' : ' ';
					s += (BFI(this, BFI_AutoIndent)) ? 'A' : ' ';
					s += (BFI(this, BFI_ExpandTabs))?'T':' ';
					s += (BFI(this, BFI_MatchCase)) ? 'C' : ' ';
					s += AutoExtend ?
							(
									(BlockMode == bmStream) ? 's' :
										(BlockMode == bmLine) ? 'l' : 'c'
									) :
										(
												(BlockMode == bmStream) ? 'S' :
													(BlockMode == bmLine) ? 'L': 'C'
												);

					s += (iBFI(this, BFI_WordWrap) == 3) ? 't' :
						(iBFI(this, BFI_WordWrap) == 2) ? 'W' :
							(iBFI(this, BFI_WordWrap) == 1) ? 'w' :
								' ';

					s += (BFI(this, BFI_Undo))?'U':' '; // Turned off in orig code
					s += (BFI(this, BFI_Trim))?'E':' ';// Turned off in orig code
					//s += (Flags.KeepBackups)?'B':' ';// Turned off in orig code


					s += " "+Mode.fName+" ";
					s += (Modified != 0) ? '*' : 
						(BFI(this, BFI_ReadOnly)) ? '%' : ' ';




					int l = s.length();
					int fw = W.Cols - l;
					int fl = FileName.length();
					//char num[10];

					dB.MoveStr( 0, W.Cols, s, SColor, W.Cols);
					String num = String.format(" %s %d", CCharStr, getModelNo());
					dB.MoveStr( W.Cols - num.length(), W.Cols, num, SColor, W.Cols);

					fw -= num.length();

					if (fl > fw) {
						String ss = FileName.substring(fl - fw); // FileName + fl - fw
						dB.MoveStr( l, W.Cols, ss, SColor, W.Cols);
					} else {
						dB.MoveStr( l, W.Cols, FileName, SColor, W.Cols);
					}
				}
			} else {
				dB.MoveStr( 0, W.Cols, V.CurMsg, SColor, W.Cols);
			}
			if (V.MView.Win.GetStatusContext() == V.MView) {
				V.MView.ConPutBox(0, W.Rows, W.Cols, 1, dB);
				if (V.MView.IsActive()) {
					V.MView.ConShowCursor();
					V.MView.ConSetCursorPos(W.CP.Col - W.TP.Col, W.CP.Row - W.TP.Row);
					if (BFI(this, BFI_Insert)) {
						V.MView.ConSetCursorSize(Config.CursorInsSize[0], Config.CursorInsSize[1]);
					} else {
						V.MView.ConSetCursorSize(Config.CursorOverSize[0], Config.CursorOverSize[1]);
					}
				}
			}
		}

		Rehilit(VToR(CP.Row));

		if (BFI(this, BFI_AutoHilitParen)) {
			if (Match.Row == -1 && Match.Col == -1)
				HilitMatchBracket();
		}

		//    if ((Window == WW) && (MinRedraw == -1))
		//        MaxRedraw = MinRedraw = VToR(CP.Row);

		//printf("\n\nMinRedraw = %d, MaxRedraw = %d", MinRedraw, MaxRedraw);
		if (MinRedraw == -1)
			return;

		//    printf("Will redraw: %d to %d, to eos = %d\n", MinRedraw, MaxRedraw, RedrawToEos);
		if (MinRedraw >= VCount) MinRedraw = VCount - 1;
		if (MinRedraw < 0) MinRedraw = 0;
		//    puts("xxx\x7");
		//    printf("%d\n", MinRedraw);
		Row = RowA = RToVN(MinRedraw);
		//    puts("xxx\x7");
		RowZ = MaxRedraw;
		if (MaxRedraw != -1) {
			int Delta = 0;

			if (MaxRedraw >= RCount) {
				Delta = MaxRedraw - RCount + 1;
				MaxRedraw = RCount - 1;
			}
			if (MaxRedraw < 0) MaxRedraw = 0;
			//        printf("%d\n", MaxRedraw);
			RowZ = RToVN(MaxRedraw) + Delta;
		}
		//    puts("xxx\x7");
		//printf("\nRowA = %d, RowZ = %d", RowA, RowZ);

		//V = View;
		//while (V!=null) 
		for( EView V : views )
		{
			if (V.Model != this)
				assert(1 == 0);

			W = GetViewVPort(V);

			for (int R = W.TP.Row; R < W.TP.Row + W.Rows; R++) {
				Row = R;
				if ((Row >= RowA) &&
						(RedrawToEos!=0 || Row <= RowZ))
				{
					int [] HilitX = {0};
					DrawLine(dB, Row, W.TP.Col, W.Cols, HilitX);
					W.DrawLine(Row, dB);
					if (HilitX[0] != 0 && Row == RowZ)
						RowZ++;
				}
			}
			//V = V.NextView;
		}
		MinRedraw = MaxRedraw = -1;
		RedrawToEos = 0;
	}

	boolean GetHilitWord(int len, String str, int /*ChColor*/ []clr, int IgnCase) {
		//String p;

		//* TODO GetHilitWord
		if (Mode == null || Mode.fColorize == null)
			return false;

		if (len >= CK_MAXLEN)
			return false;

		{
			//char s[CK_MAXLEN + 1];
			//s[CK_MAXLEN] = 0;
			//memcpy(s, str, len);
			String s = str.substring(0, len);
			//s[len] = 0;
			if (HilitFindWord(s)) {
				clr[0] = hcPlain_HilitWord;
				return true;
			}
		}

		if (len < 1) return false;
		byte[] b = Mode.fColorize.Keywords.key[len];
		int p = 0;
		if (IgnCase!=0) {
			while(p < b.length) 
			{
				String kw = new String( b, p, len );
				//if (strnicmp(p, str, len) == 0) 
				if(kw.equalsIgnoreCase(str)) 
				{
					clr[0] = b[p+len];
					return true;
				}
				p += len + 1;
			}
		} else {
			while(p < b.length) 
			{
				String kw = new String( b, p, len );
				//if (memcmp(p, str, len) == 0) 
				if(kw.equals(str)) 
				{
					clr[0] = b[p+len];
					return true;
				}
				p += len + 1;
			}
		}

		if (len < 128) {
			//char s[128];

			//memcpy(s, str, len);
			//s[len] = 0;
			String s = str.substring(0, len);

			if( BFI(this, BFI_HilitTags) && Tags.TagDefined(s) ) 
			{
				//clr = 0x0A;
				clr[0] = Mode.fColorize.Colors[CLR_HexNumber];
				return true;
			} 
		}

		return false;
	}



	boolean HilitFindWord(String Word) 
	{
		for (int i = 0; i < WordCount; i++) 
		{
			if (BFI(this, BFI_MatchCase)) {
				//if (strcmp(Word, WordList[i]) == 0) return true;
				if(WordList[i].equals(Word)) return true;
			} else {
				//if (stricmp(Word, WordList[i]) == 0) return true;
				if(WordList[i].equalsIgnoreCase(Word)) return true;
			}
		}

		return false;
	}


























	boolean FindStr(String Data, int Len, int Options) {
		int LLen, Start, End;
		int C, L;
		ELine X;
		//String P;

		boolean osBack = 0 != (Options & SEARCH_BACK);
		boolean osWordBeg = 0 != (Options & SEARCH_WORDBEG);
		boolean osWordEnd = 0 != (Options & SEARCH_WORDEND);
		boolean osNCase = 0 != (Options & SEARCH_NCASE);

		if(0 != (Options & SEARCH_RE))
			return false;
		if (Len <= 0)
			return false;

		if(0 != (Options & SEARCH_NOPOS)) {
			C = Match.Col;
			L = Match.Row;
		} else {
			C = CP.Col;
			L = VToR(CP.Row);
		}
		if (Match.Row != -1)
			Draw(Match.Row, Match.Row);
		Match.Row = -1;
		Match.Col = -1;
		X = RLine(L);
		C = CharOffset(X, C);

		if(0 != (Options & SEARCH_NEXT)) {
			int CC = MatchCount != 0 ? MatchCount : 1;

			if(osBack) {
				C -= CC;
				if (C < 0) {
					if (L == 0) return false;
					L--;
					X = RLine(L);
					C = X.getCount();
				}
			} else {
				C += CC;
				if (C >= X.getCount()) {
					C = 0;
					L++;
					if (L == RCount) return false;
				}
			}
		}
		MatchLen = 0;
		MatchCount = 0;

		if(0 != (Options & SEARCH_BLOCK)) {
			if(osBack) {
				if (BlockMode == bmStream) {
					if (L > BE.Row) {
						L = BE.Row;
						C = BE.Col;
					}
					if (L == BE.Row && C > BE.Col)
						C = BE.Col;
				} else {
					if (L >= BE.Row && BE.Row > 0) {
						L = BE.Row - 1;
						C = RLine(L).getCount();
					}
					if (BlockMode == bmColumn)
						if (L == BE.Row - 1 && C >= BE.Col)
							C = BE.Col;
				}
			} else {
				if (L < BB.Row) {
					L = BB.Row;
					C = 0;
				}
				if (L == BB.Row && C < BB.Col)
					C = BB.Col;
			}
		}
		while (true) {
			if(0 != (Options & SEARCH_BLOCK)) {
				if (BlockMode == bmStream) {
					if (L > BE.Row || L < BB.Row) break;
				} else
					if (L >= BE.Row || L < BB.Row) break;
			} else
				if (L >= RCount || L < 0) break;

			X = RLine(L);

			LLen = X.getCount();
			//BinaryString P = X.Chars;
			CString P = X.Chars;
			//int Ppos = 0;
			Start = 0;
			End = LLen;

			if(0 != (Options & SEARCH_BLOCK)) {
				if (BlockMode == bmColumn) {
					Start = CharOffset(X, BB.Col);
					End = CharOffset(X, BE.Col);
				} else if (BlockMode == bmStream) {
					if (L == BB.Row)
						Start = CharOffset(X, BB.Col);
					if (L == BE.Row)
						End = CharOffset(X, BE.Col);
				}
			}
			if(osBack) {
				if (C >= End - Len)
					C = End - Len;
			} else {
				if (C < Start)
					C = Start;
			}



			while (((!osBack) && (C <= End - Len)) || (osBack && (C >= Start))) 
			{
				if (
						(!osWordBeg
								|| (C == 0)
								|| (BitOps.WGETBIT(Flags.WordChars, X.Chars.charAt(C - 1) /*P[C - 1]*/) == 0))
						&&
						(!osWordEnd
								|| (C + Len >= End)
								|| (BitOps.WGETBIT(Flags.WordChars, X.Chars.charAt(C + Len)/*P[C + Len]*/) == 0))
						&&
						((!osNCase
								&& ( X.Chars.charAt(C)/*P[C]*/ == Data.charAt(0))
								//&& (memcmp(P + C, Data, Len) == 0))
								&& (P.memcmp( C, Data, Len) == 0))
								||
								(osNCase
										&& (Character.toUpperCase(X.Chars.charAt(C)/*P[C]*/) == Character.toUpperCase(Data.charAt(0)))
										//&& (BitOps.strnicmp(P + C, Data, Len) == 0))
										&& (P.memicmp( C, Data, Len) == 0))
								) /* && BOL | EOL */
						)
				{
					Match.Col = ScreenPos(X, C);
					Match.Row = L;
					MatchCount = Len;
					MatchLen = ScreenPos(X, C + Len) - Match.Col;
					if (0 ==(Options & SEARCH_NOPOS)) {
						if(0 != (Options & SEARCH_CENTER))
							CenterPosR(Match.Col, Match.Row);
						else
							SetPosR(Match.Col, Match.Row);
					}
					Draw(L, L);
					return true;
				}
				if(0 != (Options & SEARCH_BACK)) C--; else C++;
			}
			if(0 != (Options & SEARCH_BACK)) {
				L--;
				if (L >= 0)
					C = RLine(L).getCount();
			} else {
				C = 0;
				L++;
			}
		}
		//SetPos(OC, OL);
		return false;
	}









	//#ifdef CONFIG_OBJ_ROUTINE
	boolean ScanForRoutines() {
		int line;
		//RxMatchRes res;

		if (BFS(this, BFS_RoutineRegexp) == null) {
			View.MView.Win.Choice(GPC_ERROR, "Error", 1, "O&K", "No routine regexp.");
			return false;
		}
		Pattern regx = Pattern.compile(BFS(this, BFS_RoutineRegexp));
		if (regx == null) {
			View.MView.Win.Choice(GPC_ERROR, "Error", 1, "O&K", "Failed to compile regexp '%s'", BFS(this, BFS_RoutineRegexp));
			return false;
		}

		rlst.lines.clear();

		Msg(S_BUSY, "Matching %s", BFS(this, BFS_RoutineRegexp));
		for (line = 0; line < RCount; line++) {
			ELine L = RLine(line);

			Matcher m = regx.matcher(L.Chars);

			//if (RxExec(regx, L.Chars, L.getCount(), L.Chars, res) == 1)
			if(m.find())
			{
				rlst.lines.add( new RoutineDef(line) );
				Msg(S_BUSY, "Routines: %d", rlst.lines.size());
			}
		}
		//RxFree(regx);
		return true;
		// */
	}
	//#endif


























	boolean MoveLeft() {
		if (CP.Col == 0) return false;
		SetPos(CP.Col - 1, CP.Row, tmLeft);
		return true;
	}

	boolean MoveRight() {
		SetPos(CP.Col + 1, CP.Row, tmRight);
		return true;
	}

	boolean MoveUp() {
		if (CP.Row == 0) return false;
		SetPos(CP.Col, CP.Row - 1, tmLeft);
		return true;
	}

	boolean MoveDown() {
		if (CP.Row == VCount - 1) return false;
		SetPos(CP.Col, CP.Row + 1, tmLeft);
		return true;
	}

	boolean MovePrev() {
		if (MoveLeft()) return true;
		if (MoveUp() && MoveLineEnd()) return true;
		return false;
	}

	boolean MoveNext() {
		if (CP.Col < LineLen())
			if (MoveRight()) return true;
		if (MoveDown() && MoveLineStart()) return true;
		return false;
	}


	boolean MoveWordLeftX(int start) {
		if (CP.Col > 0) {
			int wS = start, wE = 1 - start;
			ELine L = VLine(CP.Row);
			int C, P;

			C = CP.Col;
			P = CharOffset(L, C);

			if (P > L.getCount()) P = L.getCount();
			if (P > 0) {
				while ((P > 0) && (BitOps.WGETBIT(Flags.WordChars, L.Chars.charAt(P - 1)) == wE)) P--;
				while ((P > 0) && (BitOps.WGETBIT(Flags.WordChars, L.Chars.charAt(P - 1)) == wS)) P--;
				C = ScreenPos(L, P);
				return SetPos(C, CP.Row);
			} else return false;
		} else return false;
	}


	boolean MoveWordRightX(int start) {
		ELine L = VLine(CP.Row);
		int C, P;
		int wS = start, wE = 1 - start;

		C = CP.Col;
		P = CharOffset(L, C);

		if (P >= L.getCount()) return false;

		while ((P < L.getCount()) && (BitOps.WGETBIT(Flags.WordChars, L.Chars.charAt(P)) == wS)) P++;
		while ((P < L.getCount()) && (BitOps.WGETBIT(Flags.WordChars, L.Chars.charAt(P)) == wE)) P++;
		C = ScreenPos(L, P);
		return SetPos(C, CP.Row);
	}

	boolean MoveWordLeft() {
		return MoveWordLeftX(1);
	}

	boolean MoveWordRight() {
		return MoveWordRightX(1);
	}

	boolean MoveWordPrev() {
		if (MoveWordLeft()) return true;
		if (MoveUp() && MoveLineEnd()) return true;
		return false;
	}

	boolean MoveWordNext() {
		if (MoveWordRight()) return true;
		if (MoveDown() && MoveLineStart()) return true;
		return false;
	}

	boolean MoveWordEndLeft() {
		return MoveWordLeftX(0);
	}

	boolean MoveWordEndRight() {
		return MoveWordRightX(0);
	}

	boolean MoveWordEndPrev() {
		if (MoveWordEndLeft()) return true;
		if (MoveUp() && MoveLineEnd()) return true;
		return false;
	}

	boolean MoveWordEndNext() {
		if (MoveWordEndRight()) return true;
		if (MoveDown() && MoveLineStart()) return true;
		return false;
	}

	boolean MoveWordOrCapLeft() {
		if (CP.Col > 0) {
			ELine L = VLine(CP.Row);
			int C, P;

			C = CP.Col;
			P = CharOffset(L, C);

			if (P > L.getCount()) P = L.getCount();
			if (P > 0) {
				while ((P > 0) && (BitOps.WGETBIT(Flags.WordChars, L.Chars.charAt(P - 1)) == 0)) P--;
				while ((P > 0) && (BitOps.WGETBIT(Flags.WordChars, L.Chars.charAt(P - 1)) == 1) && (BitOps.WGETBIT(Flags.CapitalChars, L.Chars.charAt(P - 1)) == 0)) P--;
				while ((P > 0) && (BitOps.WGETBIT(Flags.CapitalChars, L.Chars.charAt(P - 1)) == 1)) P--;
				C = ScreenPos(L, P);
				return SetPos(C, CP.Row);
			} else return false;
		} else return false;
	}

	boolean MoveWordOrCapRight() {
		ELine L = VLine(CP.Row);
		int C, P;

		C = CP.Col;
		P = CharOffset(L, C);

		if (P >= L.getCount()) return false;

		while ((P < L.getCount()) && (BitOps.WGETBIT(Flags.CapitalChars, L.Chars.charAt(P)) == 1)) P++;
		while ((P < L.getCount()) && (BitOps.WGETBIT(Flags.WordChars, L.Chars.charAt(P)) == 1) && (BitOps.WGETBIT(Flags.CapitalChars, L.Chars.charAt(P)) == 0)) P++;
		while ((P < L.getCount()) && (BitOps.WGETBIT(Flags.WordChars, L.Chars.charAt(P)) == 0)) P++;
		C = ScreenPos(L, P);
		return SetPos(C, CP.Row);
	}

	boolean MoveWordOrCapPrev() {
		if (MoveWordOrCapLeft()) return true;
		if (MoveUp() && MoveLineEnd()) return true;
		return false;
	}

	boolean MoveWordOrCapNext() {
		if (MoveWordOrCapRight()) return true;
		if (MoveDown() && MoveLineStart()) return true;
		return false;
	}

	boolean MoveWordOrCapEndLeft() {
		if (CP.Col > 0) {
			ELine L = VLine(CP.Row);
			int C, P;

			C = CP.Col;
			P = CharOffset(L, C);

			if (P > L.getCount()) P = L.getCount();
			if (P > 0) {
				while ((P > 0) && (BitOps.WGETBIT(Flags.WordChars, L.Chars.charAt(P - 1)) == 1) && (BitOps.WGETBIT(Flags.CapitalChars, L.Chars.charAt(P - 1)) == 0)) P--;
				while ((P > 0) && (BitOps.WGETBIT(Flags.CapitalChars, L.Chars.charAt(P - 1)) == 1)) P--;
				while ((P > 0) && (BitOps.WGETBIT(Flags.WordChars, L.Chars.charAt(P - 1)) == 0)) P--;
				C = ScreenPos(L, P);
				return SetPos(C, CP.Row);
			} else return false;
		} else return false;
	}

	boolean MoveWordOrCapEndRight() {
		ELine L = VLine(CP.Row);
		int C, P;

		C = CP.Col;
		P = CharOffset(L, C);

		if (P >= L.getCount()) return false;

		while ((P < L.getCount()) && (BitOps.WGETBIT(Flags.WordChars, L.Chars.charAt(P)) == 0)) P++;
		while ((P < L.getCount()) && (BitOps.WGETBIT(Flags.CapitalChars, L.Chars.charAt(P)) == 1)) P++;
		while ((P < L.getCount()) && (BitOps.WGETBIT(Flags.WordChars, L.Chars.charAt(P)) == 1) && (BitOps.WGETBIT(Flags.CapitalChars, L.Chars.charAt(P)) == 0)) P++;
		C = ScreenPos(L, P);
		return SetPos(C, CP.Row);
	}

	boolean MoveWordOrCapEndPrev() {
		if (MoveWordOrCapEndLeft()) return true;
		if (MoveUp() && MoveLineEnd()) return true;
		return false;
	}

	boolean MoveWordOrCapEndNext() {
		if (MoveWordOrCapEndRight()) return true;
		if (MoveDown() && MoveLineStart()) return true;
		return false;
	}

	boolean MoveLineStart() {
		SetPos(0, CP.Row);
		return true;
	}

	boolean MoveLineEnd() {
		SetPos(LineLen(VToR(CP.Row)), CP.Row);
		return true;
	}

	boolean MovePageUp() {
		return ScrollDown(GetVPort().Rows);
	}

	boolean MovePageDown() {
		return ScrollUp(GetVPort().Rows);
	}

	boolean MovePageLeft() {
		return ScrollRight(GetVPort().Cols);
	}

	boolean MovePageRight() {
		return ScrollRight(GetVPort().Cols);
	}

	boolean MovePageStart() {
		SetPos(CP.Col, GetVPort().TP.Row, tmLeft);
		return true;
	}

	boolean MovePageEnd() {
		SetNearPos(CP.Col, 
				GetVPort().TP.Row +
				GetVPort().Rows - 1, tmLeft);
		return true;
	}

	boolean MoveFileStart() {
		SetPos(0, 0);
		return true;
	}

	boolean MoveFileEnd() {
		SetPos(LineLen(VToR(VCount - 1)), VCount - 1);
		return true;
	}

	boolean MoveBlockStart() {
		if (BB.Col == -1 && BB.Row == -1)
			return false;
		assert(BB.Col >= 0 && BB.Row >= 0 && BB.Row < RCount);
		if (SetPosR(BB.Col, BB.Row))
			return true;
		return false;
	}

	boolean MoveBlockEnd() {
		if (BE.Col == -1 && BE.Row == -1)
			return false;
		assert(BE.Col >= 0 && BE.Row >= 0 && BE.Row < RCount);
		if (SetPosR(BE.Col, BE.Row))
			return true;
		return false;
	}

	boolean MoveFirstNonWhite() {
		int C = 0, P = 0;
		ELine L = VLine(CP.Row);

		while (C < L.getCount()) {
			if (L.Chars.charAt(C) == ' ') P++;
			else if (L.Chars.charAt(C) == 9) P = NextTab(P, iBFI(this, BFI_TabSize));
			else break;
			C++;
		}
		if (SetPos(P, CP.Row) == false) return false;
		return true;
	}

	boolean MoveLastNonWhite() {
		int C = LineLen(), P;
		ELine L = VLine(CP.Row);

		while (C > 0) {
			if (L.Chars.charAt(C - 1) == ' ' || L.Chars.charAt(C - 1) == 9) C--;
			else break;
		}
		P = ScreenPos(VLine(CP.Row), C);
		if (SetPos(P, CP.Row) == false) return false;
		return true;
	}

	boolean MovePrevEqualIndent() {
		int L = VToR(CP.Row);
		int I = LineIndented(L);

		while (--L >= 0)
			if ((RLine(L).getCount() > 0) && (LineIndented(L) == I))
				return SetPosR(I, L);
		return false;
	}

	boolean MoveNextEqualIndent() {
		int L = VToR(CP.Row);
		int I = LineIndented(L);

		while (L++ < RCount - 1)
			if ((RLine(L).getCount() > 0) && (LineIndented(L) == I))
				return SetPosR(I, L);
		return false;
	}

	boolean MoveNextTab() {
		int P = CP.Col;

		P = NextTab(P, iBFI(this, BFI_TabSize));
		return SetPos(P, CP.Row);
	}

	boolean MovePrevTab() {
		int P = CP.Col;

		if (P > 0) {
			P = ((P - 1) / iBFI(this, BFI_TabSize)) * iBFI(this, BFI_TabSize);
			return SetPos(P, CP.Row);
		} else return false;
	}

	boolean MoveLineTop() {
		if (View!=null)
			if (GetVPort().SetTop(GetVPort().TP.Col, CP.Row) == 0) return false;
		return true;
	}

	boolean MoveLineCenter() {
		if (View!=null) {
			int Row = CP.Row - GetVPort().Rows / 2;

			if (Row < 0) Row = 0;
			if (GetVPort().SetTop(GetVPort().TP.Col, Row) == 0) return false;
		}
		return true;
	}

	boolean MoveLineBottom() {
		if (View!=null) {
			int Row = CP.Row - GetVPort().Rows + 1;

			if (Row < 0) Row = 0;
			if (GetVPort().SetTop(GetVPort().TP.Col, Row) == 0) return false;
		}
		return true;
	}

	boolean MovePrevPos() {
		if (PrevPos.Col == -1 || PrevPos.Row == -1) return false;
		if (SetPosR(PrevPos.Col, PrevPos.Row) == false) return false;
		return true;
	}

	boolean MoveSavedPosCol() {
		if (SavedPos.Col == -1) return false;
		if (SetPos(SavedPos.Col, CP.Row) == false) return false;
		return true;
	}

	boolean MoveSavedPosRow() {
		if (SavedPos.Row == -1) return false;
		if (SetPosR(CP.Col, SavedPos.Row) == false) return false;
		return true;
	}

	boolean MoveSavedPos() {
		if (SavedPos.Col == -1 || SavedPos.Row == -1) return false;
		if (SetPosR(SavedPos.Col, SavedPos.Row) == false) return false;
		return true;
	}

	boolean SavePos() {
		SavedPos = CP;
		SavedPos.Row = VToR(CP.Row);
		return true;
	}

	boolean MoveTabStart() {
		ELine X = VLine(CP.Row);
		int C = CharOffset(X, CP.Col);

		if (C < X.getCount())
			if (X.Chars.charAt(C) == 9)
				return SetPos(ScreenPos(X, C), CP.Row);
		return true;
	}

	boolean MoveTabEnd() {
		ELine X = VLine(CP.Row);
		int C = CharOffset(X, CP.Col);

		if (C < X.getCount())
			if (X.Chars.charAt(C) == 9)
				if (ScreenPos(X, C) < CP.Col)
					return SetPos(ScreenPos(X, C + 1), CP.Row);
		return true;
	}

	boolean ScrollLeft(int Cols) {
		int C = GetVPort().TP.Col;
		if (SetNearPos(CP.Col + Cols, CP.Row, tmLeft) == false) return false;
		if (GetVPort().SetTop(C + Cols, GetVPort().TP.Row) == 0) return false;
		return true;
	}

	boolean ScrollRight(int Cols) {  
		int C = GetVPort().TP.Col;
		if (SetNearPos(CP.Col - Cols, CP.Row, tmLeft) == false) return false;
		if (GetVPort().SetTop(C - Cols, GetVPort().TP.Row) == 0) return false;
		return true;
	}

	boolean ScrollDown(int Lines) {
		int L = GetVPort().TP.Row;
		if (SetNearPos(CP.Col, CP.Row - Lines, tmLeft) == false) return false;
		if (GetVPort().SetTop(GetVPort().TP.Col, L - Lines) == 0) return false;
		return true;
	}

	boolean ScrollUp(int Lines) {
		int L = GetVPort().TP.Row;
		if (SetNearPos(CP.Col, CP.Row + Lines, tmLeft) == false) return false;
		if (GetVPort().SetTop(GetVPort().TP.Col, L + Lines) == 0) return false;
		return true;
	}

	boolean MoveBeginOrNonWhite() {
		if (CP.Col == 0)
			return MoveFirstNonWhite();
		else
			return MoveLineStart();
	}

	boolean MoveBeginLinePageFile() {
		int L = GetVPort().TP.Row;

		if (CP.Col == 0 && CP.Row == L)
			return MoveFileStart();
		else if (CP.Col == 0)
			return MovePageStart();
		else
			return MoveLineStart();
	}

	boolean MoveEndLinePageFile() {
		int L = GetVPort().TP.Row + GetVPort().Rows - 1;
		int Len = LineLen();

		if (CP.Col == Len && CP.Row == L)
			return MoveFileEnd();
		else if (CP.Col == Len)
			if (MovePageEnd() == false)
				return false;
		return MoveLineEnd();
	}

	boolean KillLine() {
		int Y = VToR(CP.Row);

		if (Y == RCount - 1) {
			if (DelText(Y, 0, LineLen())) return true;
		} else 
			if (DelLine(Y)) return true;
		return false;
	}

	boolean KillChar() {
		int Y = VToR(CP.Row);
		if (CP.Col < LineLen()) {
			if (DelText(Y, CP.Col, 1)) return true;
		} else if (LineJoin()) return true;
		return false;
	}

	boolean KillCharPrev() {
		if (CP.Col == 0) {
			if (CP.Row > 0)
				if (ExposeRow(VToR(CP.Row) - 1) == false) return false;
			if (!MoveUp()) return false;
			if (!MoveLineEnd()) return false;
			if (LineJoin()) return true;
		} else {
			if (!MovePrev()) return false;
			if (DelText(CP.Row, CP.Col, 1)) return true;
		} 
		return false;
	}

	int ChClass(char x) { return (BitOps.WGETBIT(Flags.WordChars, (x)) != 0? 1 : 0); }
	int ChClassK(char x) { return ((x == ' ') || (x == (char)9)) ? 2 : ChClass(x); }


	boolean KillWord() {
		int Y = VToR(CP.Row);
		if (CP.Col >= LineLen()) {
			if (KillChar() == false) return false;
		} else {
			ELine L = RLine(Y);
			int P = CharOffset(L, CP.Col);
			int C;
			int Class = ChClassK(L.Chars.charAt(P));

			while ((P < L.getCount()) && (ChClassK(L.Chars.charAt(P)) == Class)) P++;
			C = ScreenPos(L, P);
			if (DelText(Y, CP.Col, C - CP.Col) == false) return false;
		}
		return true;
	}

	boolean KillWordPrev() {
		int Y = VToR(CP.Row);

		if (CP.Col == 0) {
			if (KillCharPrev() == false) return false;
		} else if (CP.Col > LineLen()) {
			if (SetPos(LineLen(), CP.Row) == false) return false;
		} else {
			ELine L = RLine(Y);
			int P = CharOffset(L, CP.Col);
			int C;
			int Class = ChClassK(L.Chars.charAt(P - 1));

			while ((P > 0) && (ChClassK(L.Chars.charAt(P - 1)) == Class)) P--;
			C = ScreenPos(L, P);
			if (DelText(Y, C, CP.Col - C) == false) return false;
			if (SetPos(C, CP.Row) == false) return false;
		}
		return true;
	}

	boolean KillWordOrCap() {
		int Y = VToR(CP.Row);
		if (CP.Col >= LineLen()) {
			if (KillChar() == false) return false;
		} else {
			ELine L = VLine(CP.Row);
			int P = CharOffset(L, CP.Col);
			int C;
			int Class = ChClassK(L.Chars.charAt(P));

			if (Class == 1) {
				if (BitOps.WGETBIT(Flags.CapitalChars, L.Chars.charAt(P)) == 1)
					while ((P < L.getCount()) && (BitOps.WGETBIT(Flags.CapitalChars, L.Chars.charAt(P)) == 1)) P++;
				while ((P < L.getCount()) && (BitOps.WGETBIT(Flags.WordChars, L.Chars.charAt(P)) == 1) && (BitOps.WGETBIT(Flags.CapitalChars, L.Chars.charAt(P)) == 0)) P++;
			} else while ((P < L.getCount()) && (ChClassK(L.Chars.charAt(P)) == Class)) P++;
			C = ScreenPos(L, P);
			if (!DelText(Y, CP.Col, C - CP.Col)) return false;
		}
		return true;
	}

	boolean KillWordOrCapPrev() {
		int Y = VToR(CP.Row);

		if (CP.Col == 0) {
			if (KillCharPrev() == false) return false;
		} else if (CP.Col > LineLen()) {
			if (SetPos(LineLen(), CP.Row) == false) return false;
		} else {
			ELine L = RLine(Y);
			int P = CharOffset(L, CP.Col);
			int C;
			int Class = ChClassK(L.Chars.charAt(P - 1));

			if (Class == 1) {
				if (BitOps.WGETBIT(Flags.CapitalChars, L.Chars.charAt(P - 1)) == 0)
					while ((P > 0) && (BitOps.WGETBIT(Flags.WordChars, L.Chars.charAt(P - 1)) == 1) && (BitOps.WGETBIT(Flags.CapitalChars, L.Chars.charAt(P - 1)) == 0)) P--;
				while ((P > 0) && (BitOps.WGETBIT(Flags.CapitalChars, L.Chars.charAt(P - 1)) == 1)) P--;
			} else while ((P > 0) && (ChClassK(L.Chars.charAt(P - 1)) == Class)) P--;
			C = ScreenPos(L, P);
			if (!DelText(Y, C, CP.Col - C)) return false;
			if (SetPos(C, CP.Row) == false) return false;
		}
		return true;
	}

	boolean KillToLineStart() {
		int Y = VToR(CP.Row);
		if (DelText(Y, 0, CP.Col) == false) return false;
		if (MoveLineStart() == false) return false;
		return true;
	}

	boolean KillToLineEnd() {
		int Y = VToR(CP.Row);
		if (DelText(Y, CP.Col, LineLen() - CP.Col)) return true;
		return false;
	}

	boolean KillBlock() {
		return BlockKill();
	}

	boolean KillBlockOrChar() {
		if (CheckBlock() == false)
			return KillChar();
		else
			return BlockKill();
	}

	boolean KillBlockOrCharPrev() {
		if (CheckBlock() == false)
			return KillCharPrev();
		else
			return BlockKill();
	}

	boolean BackSpace() {
		int Y = VToR(CP.Row);

		if (CheckBlock() && BFI(this, BFI_BackSpKillBlock)) {
			if (BlockKill() == false)
				return false;
		} else if (iBFI(this, BFI_WordWrap) == 2 && CP.Row > 0 && !IsLineBlank(Y - 1) &&
					CP.Col <= iBFI(this, BFI_LeftMargin) && CP.Col <= LineIndented(Y))
		{
			if (SetPos(LineLen(Y - 1), CP.Row - 1) == false) return false;
		} else if (CP.Col == 0) {
			if (CP.Row > 0)
				if (ExposeRow(VToR(CP.Row) - 1) == false) return false;
			if (MoveUp() == false) return false;
			if (MoveLineEnd() == false) return false;
			if (LineJoin() == false) return false;
		} else {
			if (BFI(this, BFI_BackSpUnindents) && (LineIndented(Y) >= CP.Col)) {
				int C = CP.Col, C1 = 0;
				int L = VToR(CP.Row);

				C1 = C;
				while (L > 0 && (IsLineBlank(L - 1) || (C1 = LineIndented(L - 1)) >= C)) L--;
				if (L == 0) C1 = 0;
				if (C1 == C) C1--;
				if (C1 < 0) C1 = 0;
				if (C1 > C) C1 = C;
				if (SetPos(C1, CP.Row) == false) return false;
				if (C > LineIndented(Y)) return false;
				if (DelText(Y, C1, C - C1) == false) return false;
			} else if (BFI(this, BFI_BackSpKillTab)) {
				int P;
				int C = CP.Col, C1;

				P = CharOffset(RLine(Y), C - 1);
				C1 = ScreenPos(RLine(Y), P);
				if (SetPos(C1, CP.Row) == false) return false;
				if (DelText(Y, C1, C - C1) == false) return false;
			} else {
				if (MovePrev() == false) return false;
				if (DelText(Y, CP.Col, 1) == false) return false;
			} 
		}

		if (iBFI(this, BFI_WordWrap) == 2) {
			if (DoWrap(0) == false) return false;
		}

		if (BFI(this, BFI_Trim)) {
			Y = VToR(CP.Row);
			if (TrimLine(Y) == false) return false;
		}
		return true;
	}

	boolean Delete() {
		int Y = VToR(CP.Row);
		if (CheckBlock() && BFI(this, BFI_DeleteKillBlock)) {
			if (BlockKill() == false)
				return false;
		} else if (CP.Col < LineLen()) {
			if (BFI(this, BFI_DeleteKillTab)) {
				int P;
				int C = CP.Col, C1;

				P = CharOffset(RLine(Y), C);
				C1 = ScreenPos(RLine(Y), P + 1);
				if (DelText(Y, C, C1 - C) == false) return false;
			} else 
				if (DelText(Y, CP.Col, 1) == false) return false;
		} else 
			if (LineJoin() == false) return false;

		if (iBFI(this, BFI_WordWrap) == 2) {
			if (DoWrap(0) == false) return false;
			if (CP.Col >= LineLen(Y))
				if (CP.Row < VCount - 1) {
					if (SetPos(iBFI(this, BFI_LeftMargin), CP.Row + 1) == false) return false;
				}
		}

		if (BFI(this, BFI_Trim))
			if (TrimLine(VToR(CP.Row)) == false)
				return false;
		return true;
	}

	boolean LineInsert() {
		if (InsLine(VToR(CP.Row), 0)) return true;
		return false;
	}

	boolean LineAdd() {
		if (InsLine(VToR(CP.Row), 1) && MoveDown()) return true;
		return false;
	}

	boolean LineSplit() {
		if (SplitLine(VToR(CP.Row), CP.Col) == false) return false;
		if (BFI(this, BFI_Trim))
			if (TrimLine(VToR(CP.Row)) == false) return false;
		return true;
	}

	boolean LineJoin() {
		if (JoinLine(VToR(CP.Row), CP.Col)) return true;
		return false;
	}

	boolean LineNew() {
		if (SplitLine(VToR(CP.Row), CP.Col) == false)
			return false;

		if (!MoveDown())
			return false;

		if (CP.Col > 0) {

			if (!MoveLineStart())
				return false;

			//int Indent = LineIndented(VToR(CP.Row));

			if (!LineIndent())
				return false;

			//if (Indent > 0)
			//  if (InsText(Row, C, Indent, 0) == false)
			//    return false;

			if (BFI(this, BFI_Trim))
				if (TrimLine(VToR(CP.Row - 1)) == false)
					return false;
		}
		return true;
	}

	boolean LineIndent() {
		int rc = 1;

		if (BFI(this, BFI_AutoIndent)) {
			int L = VToR(CP.Row);

			switch (iBFI(this, BFI_IndentMode)) {
			case INDENT_C: rc = CLanguage.Indent_C(this, L, 1); break;
			//case INDENT_REXX: rc = Indent_REXX(this, L, 1); break;
			case INDENT_SIMPLE: rc = CLanguage.Indent_SIMPLE(this, L, 1); break;
			default: rc = Hiliter.Indent_Plain(this, L, 1); break;
			}
		}
		if (rc == 0) return false;
		if (BFI(this, BFI_Trim))
			if (TrimLine(VToR(CP.Row)) == false) return false;
		return true;
	}

	int LineLen() {
		return LineLen(VToR(CP.Row));
	}

	int LineCount() {
		assert(1 == 0);
		return RCount;
	}

	int CLine() {
		assert(1 == 0);
		return VToR(CP.Row);
	}

	int CColumn() {
		return CP.Col;
	}

	boolean InsertChar(ExState State) {
		char Ch;
		int [] No = {0};

		if (State.GetIntParam(View, No) == 0) {
			/*
	        TEvent E;
	        E.What = evKeyDown;
	        E.Key.Code = View.MView.Win.GetChar("Quote Char:");
	        if (!GetCharFromEvent(E, Ch)) return false;
	        No = Ch;
			 */
			TKeyEvent E = new TKeyEvent(evKeyDown, (char) View.MView.Win.GetChar("Quote Char:") );	    	
			No[0] = E.GetChar();
		}

		Ch = (char)No[0];
		if (Ch < 0 || Ch > 255) return false;
		return InsertChar(Ch);
	}

	boolean TypeChar(ExState State) {
		char Ch;
		int [] No = {0};

		if (State.GetIntParam(View, No) == 0) 
		{
			/*
	        TEvent E;
	        E.What = evKeyDown;
	        E.Key.Code = View.MView.Win.GetChar(0);
	        if (!GetCharFromEvent(E, Ch)) return false;
			 */
			TKeyEvent E = new TKeyEvent(evKeyDown, (char) View.MView.Win.GetChar(null) );	    	
			No[0] = E.GetChar();
		}

		Ch = (char)No[0];
		if (Ch < 0 || Ch > 255) return false;
		return TypeChar(Ch);
	}


	boolean InsertChar(char aCh) {
		return InsertString(""+aCh, 1);
	}

	boolean TypeChar(char aCh) { // does abbrev expansion if appropriate
		if (iBFI(this, BFI_InsertKillBlock) == 1)
			if (CheckBlock())
				if (BlockKill() == false)
					return false;
		/*#ifdef CONFIG_ABBREV    //fprintf(stderr, "TypeChar\n");
	    if (ChClass(aCh) == 0 && BFI(this, BFI_Abbreviations) == 1) {
	        ELine L = VLine(CP.Row);
	        int C, P, P1, C1, Len, R;
	        char Str[256];
	        EAbbrev *ab;

	        R = VToR(CP.Row);
	        C = CP.Col;
	        P = CharOffset(L, C);
	        if (P >= 0 && P <= L.getCount()) {
	            //fprintf(stderr, "TypeChar 1\n");
	            P1 = P;
	            C1 = ScreenPos(L, P);
	            while ((P > 0) && ((ChClass(L.Chars.charAt(P - 1)) == 1) || (L.Chars.charAt(P - 1) == '_'))) P--;
	            Len = P1 - P;
	            C = ScreenPos(L, P);
	            assert(C1 - C == Len);
		    if (Len > 0 && Len < int (sizeof(Str))) {
	                //fprintf(stderr, "TypeChar 2\n");
	                memcpy(Str, L.Chars + P, Len);
	                Str[Len] = 0;
	                ab = Mode.FindAbbrev(Str);
	                if (ab) {
	                    //fprintf(stderr, "TypeChar 3\n");
	                    if (ab.Replace != 0) {
	                        //fprintf(stderr, "TypeChar 4\n");
	                        if (DelText(R, C, C1 - C) == false)
	                            return false;
	                        if (ab.Replace) {
	                            //fprintf(stderr, "TypeChar 5 %s <- %s\n", ab.Replace, ab.Match);
	                            Len = strlen(ab.Replace);
	                            if (InsText(R, C, Len, ab.Replace) == false)
	                                return false;
	                            if (SetPos(C + Len, CP.Row) == false)
	                                return false;
	                        } else {
	                            if (SetPos(C, CP.Row) == false)
	                                return false;
	                        }
	                    } else {
	                        if (((EGUI *)gui).ExecMacro(View.MView.Win, ab.Cmd) == 0)
	                            return false;
	                    }
	                }
	            }
	        }
	    }
	#endif */
		return InsertString(""+aCh, 1);
	}

	boolean InsertString(String aStr) 
	{
		return InsertString(aStr, aStr.length()); 
	}

	boolean InsertString(String aStr, int aCount) 
	{
		int Y = VToR(CP.Row);

		if (iBFI(this, BFI_InsertKillBlock) == 1)
			if (CheckBlock())
				if (BlockKill() == false)
					return false;

		if (iBFI(this, BFI_Insert) == 0)
			if (CP.Col < LineLen())
				if (KillChar() == false)
					return false;

		if (InsText(Y, CP.Col, aCount, aStr) == false)
			return false;

		int Cc = CP.Col;
		int Lr = VToR(CP.Row);
		int Po = CharOffset(RLine(Lr), Cc);
		Po += aCount;
		Cc= ScreenPos(RLine(Lr), Po);

		if (SetPos(Cc, CP.Row) == false)
			return false;

		if (BFI(this, BFI_Trim))
			if (TrimLine(Lr) == false)
				return false;

		if (iBFI(this, BFI_WordWrap) == 2) {
			if (DoWrap(0) == false) return false;
		} else if (iBFI(this, BFI_WordWrap) == 1) {
			int P, C = CP.Col;
			ELine LP;
			int L;

			if (C > iBFI(this, BFI_RightMargin)) {
				L = CP.Row;

				C = iBFI(this, BFI_RightMargin);
				P = CharOffset(LP = RLine(L), C);
				while ((C > iBFI(this, BFI_LeftMargin)) &&
						((LP.Chars.charAt(P) != ' ') &&
								(LP.Chars.charAt(P) != 9)))
					C = ScreenPos(LP, --P);

				if (P <= iBFI(this, BFI_LeftMargin)) {
					C = iBFI(this, BFI_RightMargin);
				} else
					C = ScreenPos(LP, P);
				if (SplitLine(L, C) == false) return false;
				IndentLine(L + 1, iBFI(this, BFI_LeftMargin));
				if (SetPos(CP.Col - C - 1 + iBFI(this, BFI_LeftMargin), CP.Row + 1) == false) return false;
			}
		}

		return true;
	}

	boolean InsertSpacesToTab(int TSize) {
		int P = CP.Col, P1;

		if (iBFI(this, BFI_InsertKillBlock) == 1)
			if (CheckBlock())
				if (BlockKill() == false)
					return false;

		if (TSize <= 0)
			TSize = iBFI(this, BFI_TabSize);

		P1 = NextTab(P, TSize);
		if (!BFI(this, BFI_Insert)) {
			if (CP.Col < LineLen())
				if (DelText(VToR(CP.Row), CP.Col, P1 - P) == false) return false;
		}
		if (InsText(VToR(CP.Row), CP.Col, P1 - P, null) == false) return false;
		if (SetPos(P1, CP.Row) == false) return false;
		return true;
	}

	boolean InsertTab() {
		return (BFI(this, BFI_SpaceTabs)) ?
				InsertSpacesToTab(iBFI(this, BFI_TabSize)) : InsertChar((char) 9);
	}

	boolean InsertSpace() {
		return TypeChar((char) 32);
	}

	int LineIndented(int Row) {
		int P;
		//String PC;
		int I;
		int Ind = 0;

		if (Row < 0) return 0;
		if (Row >= RCount) return 0;
		P = RLine(Row).getCount();
		//BinaryString PC = RLine(Row).Chars;
		CString PC = RLine(Row).Chars;

		for (I = 0; I < P; I++) {
			if (PC.charAt(I) == ' ') Ind++;
			else if ((PC.charAt(I) == 9) && (iBFI(this, BFI_ExpandTabs) == 1)) Ind = NextTab(Ind, iBFI(this, BFI_TabSize));
			else break;
		}
		return Ind;
	}

	int IndentLine(int Row, int Indent) {
		int I, C;
		int Ind = Indent;

		if (Row < 0) return 0;
		if (Row >= RCount) return 0;
		if (Indent < 0) Indent = 0;
		I = LineIndented(Row);
		if (Indent != I) {
			if (I > 0) 
				if (DelText(Row, 0, I) == false) return 0;
			if (Indent > 0) {
				C = 0;
				if (BFI(this, BFI_IndentWithTabs)) {
					char ch = 9;

					while (iBFI(this, BFI_TabSize) <= Indent) {
						if (InsText(Row, C, 1, ""+ch) == false) return 0;
						Indent -= iBFI(this, BFI_TabSize);
						C += iBFI(this, BFI_TabSize);
					}
				}
				if (Indent > 0)
					if (InsText(Row, C, Indent, null) == false) return 0;
			}
		}
		return Ind - I;
	}


	boolean IsLineBlank(int Row) {
		ELine X = RLine(Row);
		int P;

		for (P = 0; P < X.getCount(); P++) 
			if (X.Chars.charAt(P) != ' ' && X.Chars.charAt(P) != 9)
				return false;
		return true;
	}


	//#define WFAIL(x) return 0	//do { puts(#x "\x7"); return -1; } while (0) 

	boolean DoWrap(int WrapAll) {
		int L, Len, C, P, Ind;
		ELine LP;
		int Left = iBFI(this, BFI_LeftMargin), Right = iBFI(this, BFI_RightMargin);
		int FirstParaLine;
		int NoChange = 0, NoChangeX = 0;

		if (Left >= Right) return false;

		L = VToR(CP.Row);

		FirstParaLine = 0;
		if (L > 0)
			if (IsLineBlank(L - 1)) FirstParaLine = L;

		while (L < RCount) {
			NoChange = 1;

			if (VToR(CP.Row) != L || L != FirstParaLine) {
				if (VToR(CP.Row) == L)
					if (CP.Col <= LineIndented(L))
						if (SetPos(Left, CP.Row) == false) return false; //WFAIL(1);
				Ind = IndentLine(L, Left);
				if (VToR(CP.Row) == L)
					if (SetPos((CP.Col + Ind > 0) ? CP.Col + Ind : 0, CP.Row) == false) return false; //WFAIL(2);
				NoChange = 0;
			}
			Len = LineLen(L);

			if (IsLineBlank(L)) break;

			if (Len < Right) {
				int firstwordbeg = -1;
				int firstwordend = -1;
				int X;
				ELine lp;

				if (L < RCount - 1) {
					IndentLine(L + 1, 0);
					if ((ScreenPos(RLine(L + 1), RLine(L + 1).getCount()) == 0) ||
							(RLine(L + 1).Chars.charAt(0) == '>') || (RLine(L + 1).Chars.charAt(0) == '<')) break;
				} else
					break;
				if (L + 1 >= RCount) break;

				lp = RLine(L + 1);
				for (X = 0; X < lp.getCount(); X++) {
					if (firstwordbeg == -1 && 
							((lp.Chars.charAt(X) != ' ') && (lp.Chars.charAt(X) != '\t')))
					{
						firstwordbeg = X;
					} else if (firstwordend == -1 &&
							((lp.Chars.charAt(X) == ' ' || lp.Chars.charAt(X) == '\t')))
					{
						firstwordend = X - 1;
					}
				}
				if (firstwordbeg != -1)
					if (firstwordend == -1)
						firstwordend = lp.getCount();

				if (firstwordend == -1) break;
				if (Right - Len > firstwordend - firstwordbeg) {
					if (JoinLine(L, Len + 1) == false) return false; //WFAIL(3);
					NoChange = 0;
					continue;
				} else
					IndentLine(L + 1, Left);
			} else if (Len > Right) {
				C = Right;
				P = CharOffset(LP = RLine(L), C);
				while ((C > Left) && 
						((LP.Chars.charAt(P) != ' ') &&
								(LP.Chars.charAt(P) != 9)))
					C = ScreenPos(LP, --P);

				if (P <= Left) {
					L++;
					continue;
				}
				C = ScreenPos(LP, P);
				if (SplitLine(L, C) == false) return false; //WFAIL(4);
				IndentLine(L + 1, Left);
				if (L < RCount - 2 && LineLen(L + 1) == Left) {
					if (!IsLineBlank(L + 2)) {
						if (JoinLine(L + 1, Left) == false) return false; //WFAIL(5);
					}
				}
				if (L == VToR(CP.Row) && CP.Col > C) {
					if (SetPos(Left + CP.Col - C - 1, CP.Row + 1) == false) return false; //WFAIL(6);
				}
				NoChange = 0;
				L++;
				continue;
			}
			if (WrapAll == 0)
				if (NoChangeX!=0) {
					//printf("\n\nBreak OUT = %d\n\x7", L);
					break;
				}
			L++;
			NoChangeX = NoChange;
		}
		if (WrapAll == 1)
			if (!SetPosR(Left,
					(L < RCount - 2) ? (L + 2) :
						(L < RCount - 1) ? (L + 1) :
							(RCount - 1))) return false; //WFAIL(7);
		return true;
	}

	boolean WrapPara() {
		while (VToR(CP.Row) < RCount - 1 && IsLineBlank(VToR(CP.Row)))
			if (SetPos(CP.Col, CP.Row + 1) == false) return false;
		return DoWrap(1);
	}
	//#endif */

	int LineCenter() {
		if (LineTrim() == false)
			return 0;
		int ind = LineIndented(VToR(CP.Row));
		int left = iBFI(this, BFI_LeftMargin);
		int right = iBFI(this, BFI_RightMargin);
		int len = LineLen();

		//int chs = len - ind;
		int newind = left + ((right - left) - (len - ind)) / 2;
		if (newind < left)
			newind = left;
		return IndentLine(VToR(CP.Row), newind);
	}

	boolean InsPrevLineChar() {
		int L = VToR(CP.Row);
		int C = CP.Col, P;

		if (L > 0) {
			L--;
			if (C < LineLen(L)) {
				P = CharOffset(RLine(L), C);
				return InsertChar(RLine(L).Chars.charAt(P));
			}
		}
		return false;
	}

	boolean InsPrevLineToEol() {
		int L = VToR(CP.Row);
		int C = CP.Col;
		int Len;

		if (L > 0) {
			L--;
			int P = CharOffset(RLine(L), C);
			Len = RLine(L).getCount() - P;
			if (Len > 0)
			{
				String ss = RLine(L).substring(P); //RLine(L).Chars + P
				return InsertString(ss, Len);
			}
		}
		return false;
	}

	boolean LineDuplicate() {
		int Y = VToR(CP.Row);
		if (InsLine(Y, 1) == false) return false;
		if (InsChars(Y + 1, 0, RLine(Y).getCount(), RLine(Y).Chars.toString()) == false) return false;
		return true;
	}

	boolean TrimLine(int Row) {
		ELine L = RLine(Row);
		int P, X, E;

		if (L.getCount() == 0) return true;
		P = L.getCount();
		while ((P > 0) && ((L.Chars.charAt(P - 1) == ' ') || (L.Chars.charAt(P - 1) == 9)))
			P--;
		X = ScreenPos(L, P);
		E = ScreenPos(L, L.getCount());
		if (E - X > 0) 
			if (DelText(Row, X, E - X, true) == false) return false;
		return true;
	}

	boolean LineTrim() {
		return TrimLine(VToR(CP.Row));
	}

	boolean FileTrim() {
		for (int L = 0; L < RCount; L++)
			if (TrimLine(L) == false)
				return false;
		return true;
	}

	boolean BlockTrim() {
		EPoint B, E;
		int L;

		AutoExtend = false;
		if (CheckBlock() == false) return false;
		if (RCount <= 0) return false;
		B = BB;
		E = BE;
		Draw(B.Row, E.Row);
		for (L = B.Row; L <= E.Row; L++) {
			switch (BlockMode) {
			case bmStream:
				if (L < E.Row || E.Col != 0)
					if (TrimLine(L) == false)
						return false;
				break;
			case bmLine:
			case bmColumn:
				if (L < E.Row)
					if (TrimLine(L) == false)
						return false;
				break;
			}
		}
		return true;
	}

	/*
	#define TOGGLE(x) \
	    Flags.num[BFI_##x] = (Flags.num[BFI_##x]) ? 0 : 1; \
	    ///*Msg(INFO, #x " is now %s.", Flags.num[BFI_##x] ? "ON" : "OFF"); \
	    return true;

	#define TOGGLE_R(x) \
	    Flags.num[BFI_##x] = (Flags.num[BFI_##x]) ? 0 : 1; \
	    //Msg(INFO, #x " is now %s.", Flags.num[BFI_##x] ? "ON" : "OFF"); \
	    FullRedraw(); \
	    return true;
	 */
	private void TOGGLE_R(int x) { TOGGLE(x); FullRedraw(); } 

	private void TOGGLE(int x)
	{
		Flags.num[x] = (Flags.num[x] != 0) ? 0 : 1; 
	}


	boolean ToggleAutoIndent() { TOGGLE(BFI_AutoIndent);  return true; }
	boolean ToggleInsert() { TOGGLE(BFI_Insert);  return true; }
	boolean ToggleExpandTabs() { TOGGLE_R(BFI_ExpandTabs);  return true; }
	boolean ToggleShowTabs() { TOGGLE_R(BFI_ShowTabs);  return true; }
	boolean ToggleUndo() {  TOGGLE(BFI_Undo);  return true; }
	boolean ToggleReadOnly() { TOGGLE(BFI_ReadOnly);  return true; }
	boolean ToggleKeepBackups() { TOGGLE(BFI_KeepBackups);  return true; }
	boolean ToggleMatchCase() { TOGGLE(BFI_MatchCase);  return true; }
	boolean ToggleBackSpKillTab() { TOGGLE(BFI_BackSpKillTab);  return true; }
	boolean ToggleDeleteKillTab() { TOGGLE(BFI_DeleteKillTab);  return true; }
	boolean ToggleSpaceTabs() { TOGGLE(BFI_SpaceTabs);  return true; }
	boolean ToggleIndentWithTabs() { TOGGLE(BFI_IndentWithTabs);  return true; }
	boolean ToggleBackSpUnindents() { TOGGLE(BFI_BackSpUnindents);  return true; }
	boolean ToggleTrim() { TOGGLE(BFI_Trim);  return true; }
	boolean ToggleShowMarkers() { TOGGLE_R(BFI_ShowMarkers);  return true; }
	boolean ToggleHilitTags() { TOGGLE_R(BFI_HilitTags);  return true; }
	boolean ToggleShowBookmarks() { TOGGLE_R(BFI_ShowBookmarks);  return true; }

	boolean ToggleWordWrap() { 
		//BFI(this, BFI_WordWrap) = (BFI(this, BFI_WordWrap) + 1) % 3;

		Flags.num[BFI_WordWrap]++; 
		Flags.num[BFI_WordWrap] %= 3; 
		/*Msg(INFO,
	        "WordWrap is now %s.",
	        (BFI(this, BFI_WordWrap) == 2) ? "AUTO" :
	       (BFI(this, BFI_WordWrap) == 1) ? "ON" : "OFF"); */
		return true;
	}

	boolean SetLeftMargin() {
		//BFI(this, BFI_LeftMargin) = CP.Col;
		Flags.num[BFI_LeftMargin] = CP.Col;
		Msg(S_INFO, "LeftMargin set to %d.", iBFI(this, BFI_LeftMargin) + 1);
		return true;
	}

	boolean SetRightMargin() {
		//BFI(this, BFI_RightMargin) = CP.Col;
		Flags.num[BFI_RightMargin] = CP.Col;
		Msg(S_INFO, "RightMargin set to %d.", iBFI(this, BFI_RightMargin) + 1);
		return true;
	}

	boolean ChangeMode(String AMode) {
		if (EMode.FindMode(AMode) != null) {
			Mode = EMode.FindMode(AMode);
			Flags = Mode.Flags;
			resetHiliter();
			FullRedraw();
			return true;
		}
		Msg(S_ERROR, "Mode '%s' not found.", AMode);
		return false;
	}

	boolean ChangeKeys(String AMode) {
		if (EMode.FindMode(AMode) != null) {
			Mode = EMode.FindMode(AMode);
			resetHiliter();
			FullRedraw();
			return true;
		}
		Msg(S_ERROR, "Mode '%s' not found.", AMode);
		return false;
	}

	boolean ChangeFlags(String AMode) {
		if (EMode.FindMode(AMode) != null) {
			EMode XMode;
			XMode = EMode.FindMode(AMode);
			Flags = XMode.Flags;
			resetHiliter();
			FullRedraw();
			return true;
		}
		Msg(S_ERROR, "Mode '%s' not found.", AMode);
		return false;
	}

	private void resetHiliter() {
		HilitProc = null;
		if(Mode != null && Mode.fColorize != null)
			HilitProc = Hiliter.GetHilitProc(Mode.fColorize.SyntaxParser);
	}































	///////////////////////////////////////////////////////////////////////////////
	//Block Commands                                                            //
	///////////////////////////////////////////////////////////////////////////////

	public boolean SetBB(EPoint M) {
		EPoint OldBB = BB;
		int MinL, MaxL;

		if (BB.Row == M.Row && BB.Col == M.Col) return true;
		/* #ifdef CONFIG_UNDOREDO
		if (PushBlockData() == 0) return false;
		#endif */
		BB = M;
		if (OldBB.Row == -1) OldBB = BE;
		if ((OldBB.Col != BB.Col) && (BlockMode == bmColumn)) BlockRedraw();
		MinL = Math.min(OldBB.Row, BB.Row);
		MaxL = Math.max(OldBB.Row, BB.Row);
		if (MinL != -1)
			if (MinL <= MaxL) Draw(MinL, MaxL);
		return true;
	}

	public boolean SetBE(EPoint M) {
		EPoint OldBE = BE;
		int MinL, MaxL;

		if (BE.Row == M.Row && BE.Col == M.Col) return true;

		PushBlockData(); // to undo stack

		BE = M;
		if (OldBE.Row == -1) OldBE = BB;
		if ((OldBE.Col != BE.Col) && (BlockMode == bmColumn)) BlockRedraw();
		MinL = Math.min(OldBE.Row, BE.Row);
		MaxL = Math.max(OldBE.Row, BE.Row);
		if (MinL != -1)
			if (MinL <= MaxL) Draw(MinL, MaxL);
		return true;
	}

	boolean CheckBlock() {
		if (BB.Row == -1 && BE.Row == 1) {
			BB.Col = -1;
			BE.Col = -1;
			return false;
		}
		if (BB.Row == -1 || BE.Row == -1) return false;
		if (BB.Row >= RCount) BB.Row = RCount - 1;
		if (BE.Row >= RCount) BE.Row = RCount - 1;
		switch(BlockMode) {
		case bmLine:
			BB.Col = 0;
			BE.Col = 0;
			if (BB.Row >= BE.Row) return false;
			break;
		case bmColumn:
			if (BB.Col >= BE.Col) return false;
			if (BB.Row >= BE.Row) return false;
			break;
		case bmStream:
			if (BB.Row > BE.Row) return false;
			if (BB.Row == BE.Row && BB.Col >= BE.Col) return false;
			break;
		}
		return true;
	}

	public boolean BlockRedraw() {
		if (BB.Row == -1 || BE.Row == -1) return false;
		Draw(BB.Row, BE.Row);
		return true;
	}


	boolean BlockBegin() {
		EPoint X = new EPoint();

		X.Row = VToR(CP.Row);
		X.Col = CP.Col;
		CheckBlock();
		SetBB(X);
		return true;
	}

	boolean BlockEnd() {
		EPoint X = new EPoint();

		X.Row = VToR(CP.Row);
		X.Col = CP.Col;
		CheckBlock();
		SetBE(X);
		return true;
	}

	boolean BlockUnmark() {
		EPoint Null = new EPoint(-1,-1);

		SetBB(BE);
		SetBE(Null);
		SetBB(Null);
		AutoExtend = false;
		return true;
	}

	boolean BlockCut(boolean Append) {
		if (BlockCopy(Append) && BlockKill()) return true;
		return false;
	}

	boolean BlockCopy(boolean Append) {
		EPoint B, E;
		int L;
		int SL, OldCount;

		AutoExtend = false;
		if (CheckBlock() == false) return false;
		if (RCount == 0) return false;
		if (SSBuffer == null) return false;
		if (Append) {
			if (Config.SystemClipboard)
				ClipData.GetPMClip();
		} else
			SSBuffer.Clear();
		SSBuffer.BlockMode = BlockMode;

		//BFI(SSBuffer, BFI_TabSize) = BFI(this, BFI_TabSize);
		//BFI(SSBuffer, BFI_ExpandTabs) = BFI(this, BFI_ExpandTabs);
		//BFI(SSBuffer, BFI_Undo) = 0;

		BFI_SET(SSBuffer, BFI_TabSize, iBFI(this, BFI_TabSize));
		BFI_SET(SSBuffer, BFI_ExpandTabs, iBFI(this, BFI_ExpandTabs));
		BFI_SET(SSBuffer, BFI_Undo, 0);

		B = BB;
		E = BE;
		OldCount = SL = SSBuffer.RCount;
		switch (BlockMode) {
		case bmLine:
			for (L = B.Row; L < E.Row; L++) {
				if (SSBuffer.InsLine(SL, 0) == false) return false;
				if (SSBuffer.InsLineText(SL, 0, -1, 0, RLine(L)) == false) return false;
				SL++;
			}
			break;

		case bmColumn:
			for (L = B.Row; L < E.Row; L++) {
				if (!SSBuffer.InsLine(SL, 0)) return false;
				if (!SSBuffer.InsLineText(SL, 0, E.Col - B.Col, B.Col, RLine(L))) return false;
				if (!SSBuffer.PadLine(SL, E.Col - B.Col)) return false;
				SL++;
			}
			break;

		case bmStream:
			if (B.Row == E.Row) {
				if (!SSBuffer.InsLine(SL, 0)) return false;
				if (!SSBuffer.InsLineText(SL, 0, E.Col - B.Col, B.Col, RLine(B.Row))) return false;
			} else {
				if (!SSBuffer.InsLine(SL, 0)) return false;
				if (!SSBuffer.InsLineText(SL, 0, -1, B.Col, RLine(B.Row))) return false;
				SL++;
				for (L = B.Row + 1; L < E.Row; L++) {
					if (!SSBuffer.InsLine(SL, 0)) return false;
					if (!SSBuffer.InsLineText(SL, 0, -1, 0, RLine(L))) return false;
					SL++;
				}
				if (!SSBuffer.InsLine(SL, 0)) return false;
				if (!SSBuffer.InsLineText(SL, 0, E.Col, 0, RLine(E.Row))) return false;
			}
			if (Append && OldCount > 0)
				if (!SSBuffer.JoinLine(OldCount - 1, 0))
					return false;
			break;
		}
		if (Config.SystemClipboard)
			ClipData.PutPMClip();
		return true;
	}

	boolean BlockPasteStream() {
		BlockMode = bmStream;
		return BlockPaste();
	}

	boolean BlockPasteLine() {
		BlockMode = bmLine;
		return BlockPaste();
	}

	boolean BlockPasteColumn() {
		BlockMode = bmColumn;
		return BlockPaste();
	}

	boolean BlockPaste() {
		EPoint B = new EPoint(), E = new EPoint();
		int L, BL;

		if (Config.SystemClipboard)
			ClipData.GetPMClip();

		if (SSBuffer == null) return false;
		if (SSBuffer.RCount == 0) return false;
		AutoExtend = false;

		//BFI(SSBuffer, BFI_TabSize) = BFI(this, BFI_TabSize);
		//BFI(SSBuffer, BFI_ExpandTabs) = BFI(this, BFI_ExpandTabs);
		//BFI(SSBuffer, BFI_Undo) = 0;

		BFI_SET(SSBuffer, BFI_TabSize, iBFI(this, BFI_TabSize));
		BFI_SET(SSBuffer, BFI_ExpandTabs, iBFI(this, BFI_ExpandTabs));
		BFI_SET(SSBuffer, BFI_Undo, 0);

		BlockUnmark();
		B.Row = VToR(CP.Row);
		B.Col = CP.Col;
		BL = B.Row;
		switch(BlockMode) {
		case bmLine:
			B.Col = 0;
			for (L = 0; L < SSBuffer.RCount; L++) {
				if (InsLine(BL, 0) == false) return false;
				if (InsLineText(BL, 0, SSBuffer.LineLen(L), 0, SSBuffer.RLine(L)) == false) return false;
				BL++;
			}
			E.Row = BL;
			E.Col = 0;
			SetBB(B);
			SetBE(E);
			break;

		case bmColumn:
			for (L = 0; L < SSBuffer.RCount; L++) {
				if (AssertLine(BL) == false) return false;
				if (InsLineText(BL, B.Col, SSBuffer.LineLen(L), 0, SSBuffer.RLine(L)) == false) return false;
				if (TrimLine(BL) == false) return false;
				BL++;
			}
			if (AssertLine(BL) == false) return false;
			E.Row = BL;
			E.Col = B.Col + SSBuffer.LineLen(0);
			SetBB(B);
			SetBE(E);
			break;

		case bmStream:
			if (SSBuffer.RCount > 1)
				if (SplitLine(B.Row, B.Col) == false) return false;
			if (InsLineText(B.Row, B.Col, SSBuffer.LineLen(0), 0, SSBuffer.RLine(0)) == false) return false;
			E = B;
			E.Col += SSBuffer.LineLen(0);
			BL++;
			if (SSBuffer.RCount > 1) {
				for (L = 1; L < SSBuffer.RCount - 1; L++) {
					if (InsLine(BL, 0) == false) return false;
					if (InsLineText(BL, 0, SSBuffer.LineLen(L), 0, SSBuffer.RLine(L)) == false) return false;
					BL++;
				}
				L = SSBuffer.RCount - 1;
				if (InsLineText(BL, 0, SSBuffer.LineLen(L), 0, SSBuffer.RLine(L)) == false) return false;
				E.Col = SSBuffer.LineLen(L);
				E.Row = BL;
			}
			SetBB(B);
			SetBE(E);
			break;
		}
		return true;
	}

	boolean BlockKill() {
		EPoint B, E;
		int L;
		int Y = -1;

		AutoExtend = false;
		if (CheckBlock() == false) return false;
		if (RCount <= 0) return false;
		B = BB;
		E = BE;
		Draw(B.Row, -1);
		//    if (MoveToPos(B.Col, B.Row) == false) return false;

		getSlot().pushPosition(CP);


		switch (BlockMode) {
		case bmLine:
			Y = VToR(CP.Row);
			if (Y >= B.Row) {
				if (Y >= E.Row) {
					if (SetPosR(CP.Col, Y - (E.Row - B.Row)) == false) return false;
				} else {
					if (SetPosR(CP.Col, B.Row) == false) return false;
				}
			}
			for (L = B.Row; L < E.Row; L++)
				if (DelLine(B.Row) == false) return false;
			break;

		case bmColumn:
			Y = VToR(CP.Row);
			if (Y >= B.Row && Y < E.Row) {
				if (CP.Col >= B.Col) {
					if (CP.Col >= E.Col) {
						if (SetPos(CP.Col - (E.Col - B.Col), CP.Row) == false) return false;
					} else {
						if (SetPos(B.Col, CP.Row) == false) return false;
					}
				}
			}
			for (L = B.Row; L < E.Row; L++)
				if (DelText(L, B.Col, E.Col - B.Col) == false) return false;
			break;

		case bmStream:
			Y = VToR(CP.Row);

			if (B.Row == E.Row) {
				if (Y == B.Row) {
					if (CP.Col >= B.Col) {
						if (CP.Col >= E.Col) {
							if (SetPos(CP.Col - (E.Col - B.Col), CP.Row) == false) return false;
						} else {
							if (SetPos(B.Col, CP.Row) == false) return false;
						}
					}
				}
				if (DelText(B.Row, B.Col, E.Col - B.Col) == false) return false;
			} else {
				if (Y >= B.Row) {
					if (Y > E.Row || (Y == E.Row && E.Col == 0)) {
						if (SetPosR(CP.Col, Y - (E.Row - B.Row)) == false) return false;
					} else if (Y == E.Row) {
						if (CP.Col >= E.Col) {
							if (SetPosR(CP.Col - E.Col + B.Col, B.Row) == false) return false;
						} else {
							if (SetPosR(B.Col, B.Row) == false) return false;
						}
					} else {
						if (SetPosR(B.Col, B.Row) == false) return false;
					}
				}
				if (DelText(E.Row, 0, E.Col) == false) return false;
				for (L = B.Row + 1; L < E.Row; L++)
					if (DelLine(B.Row + 1) == false) return false;
				if (DelText(B.Row, B.Col, -1) == false) return false;
				if (JoinLine(B.Row, B.Col) == false) return false;
			}
			break;
		}
		return BlockUnmark();
	}

	boolean ClipClear() {
		if (SSBuffer == null)
			return false;
		SSBuffer.Clear();
		if (Config.SystemClipboard)
			ClipData.PutPMClip();
		return true;
	}

	boolean BlockIndent() {
		EPoint B, E;
		int L;

		AutoExtend = false;
		if (CheckBlock() == false) return false;
		if (RCount <= 0) return false;
		B = BB;
		E = BE;
		Draw(B.Row, E.Row);
		if (SetPosR(B.Col, B.Row) == false) return false;
		for (L = B.Row; L <= E.Row; L++) {
			switch (BlockMode) {
			case bmStream:
			case bmLine:
				if (L < E.Row || E.Col != 0) {
					int I = LineIndented(L) + 1;
					IndentLine(L, I);
				}
				break;
			case bmColumn:
				if (L < E.Row) {
					if (InsText(L, B.Col, 1, null) == false) return false;
					if (DelText(L, E.Col, 1) == false) return false;
				}
				break;
			}
		}
		if (SetPosR(B.Col, B.Row) == false) return false;
		return true;
	}

	boolean BlockUnindent() {
		EPoint B, E;
		int L;

		AutoExtend = false;
		if (CheckBlock() == false) return false;
		if (RCount <= 0) return false;
		B = BB;
		E = BE;
		Draw(B.Row, E.Row);
		if (SetPosR(B.Col, B.Row) == false) return false;
		for (L = B.Row; L <= E.Row; L++) {
			switch (BlockMode) {
			case bmStream:
			case bmLine:
				if (L < E.Row || E.Col != 0) {
					int I = LineIndented(L) - 1;
					if (I >= 0)
						IndentLine(L, I);
				}
				break;
			case bmColumn:
				if (L < E.Row) {
					if (InsText(L, E.Col, 1, null) == false) return false;
					if (DelText(L, B.Col, 1) == false) return false;
				}
				break;
			}
		}
		if (SetPosR(B.Col, B.Row) == false) return false;
		return true;
	}

	boolean BlockClear() {
		return false;
	}

	boolean BlockMarkStream() {
		if (BlockMode != bmStream) BlockUnmark();
		BlockMode= bmStream;
		if (AutoExtend) AutoExtend = false;
		else {
			BlockUnmark();
			AutoExtend = true;
		}
		return true;
	}

	boolean BlockMarkLine() {
		if (BlockMode != bmLine) BlockUnmark();
		BlockMode= bmLine;
		if (AutoExtend) AutoExtend = false;
		else {
			BlockUnmark();
			AutoExtend = true;
		}
		return true;
	}

	boolean BlockMarkColumn() {
		if (BlockMode != bmColumn) BlockUnmark();
		BlockMode= bmColumn;
		if (AutoExtend) AutoExtend = false;
		else {
			BlockUnmark();
			AutoExtend = true;
		}
		return true;
	}

	boolean BlockExtendBegin() {
		CheckBlock();
		ExtendGrab = 0;
		AutoExtend = false;
		int Y = VToR(CP.Row);

		switch (BlockMode) {
		case bmStream:
			if ((Y == BB.Row) && (CP.Col == BB.Col)) ExtendGrab |= 1;
			if ((Y == BE.Row) && (CP.Col == BE.Col)) ExtendGrab |= 2;
			break;
		case bmLine:
			if (Y == BB.Row) ExtendGrab |= 1;
			if (Y == BE.Row) ExtendGrab |= 2;
			break;
		case bmColumn:
			if (Y == BB.Row) ExtendGrab |= 1;
			if (Y == BE.Row) ExtendGrab |= 2;
			if (CP.Col == BB.Col) ExtendGrab |= 4;
			if (CP.Col == BE.Col) ExtendGrab |= 8;
			break;
		}

		if (ExtendGrab == 0) {
			BlockBegin();
			BlockEnd();
			if (BlockMode == bmColumn)
				ExtendGrab = 1 | 2 | 4 | 8;
			else
				ExtendGrab = 1 | 2;
		}
		return true;
	}

	boolean BlockExtendEnd() {
		EPoint T, B, E;

		CheckBlock();
		B = BB;
		E = BE;
		switch (BlockMode) {
		case bmLine:
			if(0 != (ExtendGrab & 1)) { B.Row = VToR(CP.Row); B.Col = 0; }
			else if(0 != (ExtendGrab & 2)) { E.Row = VToR(CP.Row); E.Col = 0; }
			if (B.Row > E.Row) {
				T = B;
				B = E;
				E = T;
			}
			break;
		case bmStream:
			if(0 != (ExtendGrab & 1)) { B.Col = CP.Col; B.Row = VToR(CP.Row); }
			else if(0 != (ExtendGrab & 2)) { E.Col = CP.Col; E.Row = VToR(CP.Row); }
			if ((B.Row > E.Row) ||
					((B.Row == E.Row) && (B.Col > E.Col))) {
				T = B;
				B = E;
				E = T;
			}
			break;
		case bmColumn:
			if(0 != (ExtendGrab & 1)) B.Row = VToR(CP.Row);
			else if(0 != (ExtendGrab & 2)) E.Row = VToR(CP.Row);
			if(0 != (ExtendGrab & 4)) B.Col = CP.Col;
			else if(0 != (ExtendGrab & 8)) E.Col = CP.Col;
			if (B.Row > E.Row) {
				int tT;

				tT = B.Row;
				B.Row = E.Row;
				E.Row = tT;
			}
			if (B.Col > E.Col) {
				int tT;

				tT = B.Col;
				B.Col = E.Col;
				E.Col = tT;
			}
			break;
		}
		SetBB(B);
		SetBE(E);
		ExtendGrab = 0;
		AutoExtend = false;
		return true;
	}

	boolean BlockIsMarked() {
		if ((BB.Row != -1) && (BE.Row != -1) && (BB.Col != -1) && (BE.Col != -1)) return true;
		return false;
	}

	boolean BlockReIndent() {
		EPoint P = CP;
		EPoint B, E;

		AutoExtend = false;
		if (CheckBlock() == false) return false;
		if (RCount <= 0) return false;
		B = BB;
		E = BE;
		Draw(B.Row, E.Row);
		for (int i = B.Row; i < E.Row; i++) {
			if (SetPosR(0, i) == false) return false;
			if (LineIndent() == false) return false;
		}
		return SetPos(P.Col, P.Row);
	}

	boolean BlockSelectWord() {
		int Y = VToR(CP.Row);
		ELine L = RLine(Y);
		int P;
		int C;

		if (BlockUnmark() == false) return false;
		BlockMode = bmStream;

		P = CharOffset(L, CP.Col);

		if (P >= L.getCount()) return false;
		C = ChClassK(L.Chars.charAt(P));

		while ((P > 0) && (C == ChClassK(L.Chars.charAt(P - 1)))) P--;
		if (SetBB(new EPoint(Y, ScreenPos(L, P))) == false) return false;
		while ((P < L.getCount()) && (C == ChClassK(L.Chars.charAt(P)))) P++;
		if (SetBE(new EPoint(Y, ScreenPos(L, P))) == false) return false;
		return true;
	}

	boolean BlockSelectLine() {
		int Y = VToR(CP.Row);
		if (BlockUnmark() == false) return false;
		BlockMode = bmStream;

		if (SetBB(new EPoint(Y, 0)) == false) return false;
		if (Y == RCount - 1) {
			if (SetBE(new EPoint(Y, LineLen(Y))) == false) return false;
		} else {
			if (SetBE(new EPoint(Y + 1, 0)) == false) return false;
		}
		return true;
	}

	boolean BlockSelectPara() {
		return true;
	}

	boolean BlockWriteTo(String AFileName, boolean Append) 
	{
		if(Append)
			throw new RuntimeException("no append yet");

		Msg(S_INFO, "Writing %s...", AFileName);


		try (BufferedWriter writer = Files.newBufferedWriter(Path.of(AFileName), Main.charset)) {
			//String s;
			//writer.write(s, 0, s.length());

			doBlockWriteTo(writer,AFileName);

			//Msg(S_INFO, "Wrote %s, %d lines, %d bytes.", AFileName, lc, bc);
			Msg(S_INFO, "Wrote %s", AFileName);

			return true;

		} catch (IOException x) {
			System.err.format("Writing to %s IOException: %s%n", AFileName, x);
			View.MView.Win.Choice(GPC_ERROR, "Error", 1, "O&K", "Failed to write block to %s", AFileName);
			new File(AFileName).delete();
		}

		return false;
	}	

	private void doBlockWriteTo(BufferedWriter writer, Object AFileName) throws IOException {
		//int error = 0;
		EPoint B, E;
		int L;
		ELine LL;
		int A, Z;
		//FILE fp;
		int bc = 0, lc = 0, oldc = 0;

		AutoExtend = false;
		if (CheckBlock() == false) throw new IOException("No block");//return false;
		if (RCount == 0) throw new IOException("No lines"); //return false;
		B = BB;
		E = BE;
		//Msg(S_INFO, "Writing %s...", AFileName);
		//fp = fopen(AFileName, Append ? "ab" : "wb");
		//if (fp == null) return wrError( fp,  AFileName);


		//setvbuf(fp, FileBuffer, _IOFBF, sizeof(FileBuffer));

		for (L = B.Row; L <= E.Row; L++) {
			A = -1;
			Z = -1;
			LL = RLine(L);
			switch (BlockMode) {
			case bmLine:
				if (L < E.Row) {
					A = 0;
					Z = LL.getCount();
				}
				break;
			case bmColumn:
				if (L < E.Row) {
					A = CharOffset(LL, B.Col);
					Z = CharOffset(LL, E.Col);
				}
				break;
			case bmStream:
				if (B.Row == E.Row) {
					A = CharOffset(LL, B.Col);
					Z = CharOffset(LL, E.Col);
				} else if (L == B.Row) {
					A = CharOffset(LL, B.Col);
					Z = LL.getCount();
				} else if (L < E.Row) {
					A = 0;
					Z  = LL.getCount();
				} else if (L == E.Row) {
					A = 0;
					Z = CharOffset(LL, E.Col);
				}
				break;
			}
			if (A != -1 && Z != -1) {
				if (A < LL.getCount()) {
					if (Z > LL.getCount())
						Z = LL.getCount();
					if (Z > A) {
						int len = Z - A;
						//if ((int)fwrite(LL.Chars + A, 1, Z - A, fp) != Z - A) {
						//	return wrError( fp,  AFileName);

						writer.write(LL.toString(), A, len);

						//} else
						bc += Z - A;
					}
				}
				if (BFI(this, BFI_AddCR))
					//if (fputc(13, fp) < 0) return wrError( fp,  AFileName);
					//else 						bc++;
					writer.write("\r", 0, 1);
				if (BFI(this, BFI_AddLF))
				{
					/*if (fputc(10, fp) < 0)
						return wrError( fp,  AFileName);

					else {
						bc++;
						lc++;
					}*/

					writer.write("\n", 0, 1);
					bc++;
					lc++;
				}
				if (bc > 65536 + oldc) {
					Msg(S_INFO, "Writing %s, %d lines, %d bytes.", AFileName, lc, bc);
					oldc = bc;
				}
			}
		}
		//fclose(fp);
		//Msg(S_INFO, "Wrote %s, %d lines, %d bytes.", AFileName, lc, bc);
		//return true;
		//error:
		//return wrError( fp,  AFileName);
	}

	/*
	private boolean wrError(Object fp, String AFileName)
	{
		if(fp != null)
		{
			fclose(fp);
			unlink(AFileName);
		}
	View.MView.Win.Choice(GPC_ERROR, "Error", 1, "O&K", "Failed to write block to %s", AFileName);
	return false;
	}*/

	boolean BlockReadFrom(String AFileName, int blockMode) 
	{
		if (Console.FileExists(AFileName) == false) {
			View.MView.Win.Choice(GPC_ERROR, "Error", 1, "O&K", "File not found: %s", AFileName);
			return false;
		}

		EModel [] em = {(EModel)SSBuffer};

		try( EBuffer B = new EBuffer(0, em, AFileName) )
		{
			B.SetFileName(AFileName, null);
			if (B.Load() == false) return false;
		}

		boolean savesys = Config.SystemClipboard;
		Config.SystemClipboard = false;

		boolean rc = false;

		switch (blockMode) {
		case bmColumn: rc = BlockPasteColumn(); break;
		case bmLine:   rc = BlockPasteLine(); break;
		default:
		case bmStream: rc = BlockPasteStream(); break;
		}

		Config.SystemClipboard = savesys;

		return rc;
	}

	static EBuffer SortBuffer;
	static int SortReverse;
	static int [] SortRows = null;
	static int SortMinRow;
	static int SortMaxRow;
	static int SortMinCol;
	static int SortMaxCol;

	/*
	static int _LNK_CONV SortProc(const void *A, const void *B) {
		int *AA = (int *)A;
		int *BB = (int *)B;
		ELine *LA = SortBuffer.RLine(*AA);
		ELine *LB = SortBuffer.RLine(*BB);
		int rc;

		if (SortMinCol == -1) {
			int lA = LA.getCount();
			int lB = LB.getCount();

			if (BFI(SortBuffer, BFI_MatchCase) == 1)
				rc = memcmp(LA.Chars, LB.Chars, (lA < lB) ? lA : lB);
			else
				rc = memicmp(LA.Chars, LB.Chars, (lA < lB) ? lA : lB);
			if (rc == 0) {
				if (lA > lB)
					rc = 1;
				else
					rc = -1;
			}
		} else {
			int lA = LA.getCount();
			int lB = LB.getCount();
			int PA = SortBuffer.CharOffset(LA, SortMinCol);
			int PB = SortBuffer.CharOffset(LB, SortMinCol);

			lA -= PA;
			lB -= PB;
			if (lA < 0 && lB < 0)
				rc = 0;
			else if (lA < 0 && lB > 0)
				rc = -1;
			else if (lA > 0 && lB < 0)
				rc = 1;
			else {
				if (SortMaxCol != -1) {
					if (lA > SortMaxCol - SortMinCol)
						lA = SortMaxCol - SortMinCol;
					if (lB > SortMaxCol - SortMinCol)
						lB = SortMaxCol - SortMinCol;
				}
				if (BFI(SortBuffer, BFI_MatchCase) == 1)
					rc = memcmp(LA.Chars+ PA, LB.Chars + PB, (lA < lB) ? lA : lB);
				else
					rc = memicmp(LA.Chars + PA, LB.Chars + PB, (lA < lB) ? lA : lB);
				if (rc == 0) {
					if (lA > lB)
						rc = 1;
					else
						rc = -1;
				}
			}
		}

		if (SortReverse)
			return -rc;
		return rc;
	}
	 */
	boolean BlockSort(int Reverse) {
		/* 
		int rq;
		ELine oldL;

		if (CheckBlock() == false) return false;
		if (RCount == 0) return false;

		SortMinRow = BB.Row;
		SortMaxRow = BE.Row;
		if (BlockMode != bmStream || BE.Col == 0)
			SortMaxRow--;

		if (SortMinRow >= SortMaxRow)
			return true;

		SortBuffer = this;
		SortReverse = Reverse;
		switch (BlockMode) {
		case bmLine:
		case bmStream:
			SortMinCol = -1;
			SortMaxCol = -1;
			break;

		case bmColumn:
			SortMinCol = BB.Col;
			SortMaxCol = BE.Col;
			break;
		}

		SortRows = (int *)malloc((SortMaxRow - SortMinRow + 1) * sizeof(int));
		if (SortRows == 0) {
			free(SortRows);
			return false;
		}
		for (rq = 0; rq <= SortMaxRow - SortMinRow; rq++)
			SortRows[rq] = rq + SortMinRow;

		qsort(SortRows, SortMaxRow - SortMinRow + 1, sizeof(int), SortProc);

		// now change the order of lines according to new order in Rows array.

		for (rq = 0; rq <= SortMaxRow - SortMinRow; rq++) {
			oldL = RLine(SortRows[rq]);
			if (InsLine(1 + rq + SortMaxRow, 0) == false)
				return false;
			if (InsChars(1 + rq + SortMaxRow, 0, oldL.getCount(), oldL.Chars) == false)
				return false;
		}

		for (rq = 0; rq <= SortMaxRow - SortMinRow; rq++)
			if (DelLine(SortMinRow) == false)
				return false;

		free(SortRows);
		return true;
		 */
		return false;
	}

	boolean BlockUnTab() {
		EPoint B, E;
		ELine L;
		int O, C;

		AutoExtend = false;
		if (CheckBlock() == false) return false;
		if (RCount <= 0) return false;
		B = BB;
		E = BE;
		Draw(B.Row, E.Row);
		for (int i = B.Row; i < E.Row; i++) {
			L = RLine(i);
			O = 0;
			C = 0;
			while (O < L.getCount()) {
				if (L.Chars.charAt(O) == '\t') {
					C = NextTab(C, iBFI(this, BFI_TabSize));

					if (DelChars(i, O, 1) != true)
						return false;
					if (InsChars(i, O, C - O, null) != true)
						return false;
					O = C;
				} else {
					O++;
					C++;
				}
			}
		}
		return true;
	}

	boolean BlockEnTab() {
		EPoint B, E;
		ELine L;
		int O, C, O1, C1;
		char tab = '\t';

		AutoExtend = false;
		if (CheckBlock() == false) return false;
		if (RCount <= 0) return false;
		B = BB;
		E = BE;
		Draw(B.Row, E.Row);
		for (int i = B.Row; i < E.Row; i++) {
			L = RLine(i);
			O = C = 0;
			O1 = C1 = 0;
			while (O < L.getCount()) {
				if (L.Chars.charAt(O) == '\t') { // see if there are spaces to remove
					int C2 = NextTab(C, iBFI(this, BFI_TabSize));
					int N = iBFI(this, BFI_TabSize) - (C2 - C);
					if (O - O1 < N)
						N = O - O1;
					if (N > 0) {
						if (!DelChars(i, O - N, N))
							return false;
						O -= N;
						C = C2;
						O++;
						C1 = C;
						O1 = O;
					} else {
						O++;
						C = C2;
						O1 = O;
						C1 = C;
					}
				} else if (L.Chars.charAt(O) != ' ') { // nope, cannot put tab here
					O++;
					C++;
					C1 = C;
					O1 = O;
				} else if (((C % iBFI(this, BFI_TabSize)) == (iBFI(this, BFI_TabSize) - 1)) &&
						(C - C1 > 0))
				{ // reached a tab and can put one
					int N = iBFI(this, BFI_TabSize);
					if (O - O1 + 1 < N) {
						N = O - O1 + 1;
					} else if (O - O1 + 1 > N) {
						O1 = O - N + 1;
					}
					if (!DelChars(i, O1, N))
						return false;
					if (!InsChars(i, O1, 1, ""+tab))
						return false;
					O1++;
					O = O1;
					C++;
					C1 = C;
				} else {
					O++;
					C++;
				}
			}
		}
		return true;
	}

	//FindFunction -- search for line matching 'RoutineRegexp'
	//starting from current line + 'delta'. 'way' should be +1 or -1.
	int FindFunction(int delta, int way) 
	{
		/*
		RxNode     regx;
		int         line;
		ELine      L;
		RxMatchRes  res;

		if (BFS(this, BFS_RoutineRegexp) == 0) {
			View.MView.Win.Choice(GPC_ERROR, "Error", 1,
					"O&K", "No routine regexp.");
			return -1;
		}
		regx = RxCompile(BFS(this, BFS_RoutineRegexp));
		if (regx == 0) {
			View.MView.Win.Choice(GPC_ERROR, "Error", 1,
					"O&K", "Failed to compile regexp '%s'",
					BFS(this, BFS_RoutineRegexp));
			return -1;
		}

		//** Scan backwards from the current cursor position,
		Msg(S_BUSY, "Matching %s", BFS(this, BFS_RoutineRegexp));
		line = VToR(CP.Row) + delta;
		while (line >= 0 && line < RCount) {
			L = RLine(line);
			if (RxExec(regx, L.Chars, L.getCount(), L.Chars, &res) == 1)
				break;
			line += way;
		}
		if (line < 0)
			line = 0;
		if (line >= RCount)
			line = RCount - 1;
		RxFree(regx);		
		return line;
		 */
		return -1;
	}

	//Selects the current function.
	boolean BlockMarkFunction() {
		int by, ey;

		if (BlockUnmark() == false)
			return false;

		if ((by = FindFunction( 0, -1)) == -1)
			return false;
		if ((ey = FindFunction(+1, +1)) == -1)
			return false;

		//** Start and end are known. Set the block;
		BlockMode = bmStream;
		if (SetBB(new EPoint(by, 0)) == false)
			return false;
		if (SetBE(new EPoint(ey, 0)) == false)
			return false;

		return true;
	}

	boolean IndentFunction() {
		EPoint P = CP;
		int by, ey;

		if ((by = FindFunction( 0, -1)) == -1)
			return false;
		if ((ey = FindFunction(+1, +1)) == -1)
			return false;

		//Draw(by, ey); ?
		for (int i = by; i < ey; i++) {
			if (SetPosR(0, i) == false)
				return false;
			if (LineIndent() == false)
				return false;
		}
		return SetPos(P.Col, P.Row);
	}

	boolean MoveFunctionPrev() {
		int line = FindFunction(-1, -1);

		if (line == -1)
			return false;

		return CenterPosR(0, line);
	}

	boolean MoveFunctionNext() {
		int line = FindFunction(+1, +1);

		if (line == -1)
			return false;

		return CenterPosR(0, line);
	}




	boolean NextCommand() {
		if (Match.Row != -1) {
			Draw(Match.Row, Match.Row);
			Match.Col = Match.Row = -1;
		}

		if (View != null)
			View.SetMsg(null);

		return true;
	}
























	boolean Load() {
		return LoadFrom(FileName);
	}

	boolean Reload() {
		int R = VToR(CP.Row), C = CP.Col;

		if (!LoadFrom(FileName))
			return false;
		SetNearPosR(C, R);
		return true;
	}

	boolean Save() {
		if (BFI(this, BFI_ReadOnly)) {
			Msg(S_ERROR, "File is read-only.");
			return false;
		}
		if (BFI(this, BFI_TrimOnSave))
			FileTrim();
		return SaveTo(FileName);
	}



	boolean LoadFrom(String AFileName) {

		int SaveUndo = iBFI(this, BFI_Undo);
		int SaveReadOnly = iBFI(this, BFI_ReadOnly);

		try(BufferedReader reader = Files.newBufferedReader(Path.of(AFileName), Main.charset)) {

			boolean rc = doLoadFrom(reader, AFileName);
			reader.close();
			BFI_SET(this, BFI_Undo, SaveUndo);
			BFI_SET(this, BFI_ReadOnly, SaveReadOnly);

			FileStatus = Files.readAttributes( Path.of(AFileName), BasicFileAttributes.class);

			if (null == FileStatus) 
			{
				FileStatus = null;
				FileOk = false;
				throw new RuntimeException("no file?");
			} else {
				if(Console.isReadonly(FileName))
					BFI_SET(this, BFI_ReadOnly, 1);
				else
					BFI_SET(this, BFI_ReadOnly, 0);
			}

			FileOk = true;
			Modified = 0;
			Loading = false;
			Loaded = true;
			Draw(0, -1);
			Msg(S_INFO, "Loaded %s.", AFileName);

			return rc;
		}
		catch(IOException e)
		{
			BFI_SET(this, BFI_Undo, SaveUndo);
			BFI_SET(this, BFI_ReadOnly, SaveReadOnly);

			Loading = false;
			FileOk = false;
			Draw(0, -1);
			View.MView.Win.Choice(GPC_ERROR, "Error", 1, "O&K", "Error loading %s.", AFileName);
			return false;
		}


	}	

	boolean doLoadFrom(BufferedReader reader, String AFileName) throws IOException 
	{
		int len = 0, partLen;
		long numChars = 0, Lines = 0;
		//String p, *e, *m = null;
		int epos;
		int lm = 0;
		int lf;

		BinaryString m = new BinaryString();


		int strip = iBFI(this, BFI_StripChar);
		int lchar = iBFI(this, BFI_LineChar);
		int margin = iBFI(this, BFI_LoadMargin);

		FileOk = false;
		/* 
	    fd = open(AFileName, O_RDONLY | O_BINARY, 0);
	    if (fd == -1) {
	        if (errno != ENOENT) {
	            Msg(S_INFO, "Could not open file %s (errno=%d, %s) .",
	                AFileName, errno, strerror(errno));
	        } else {
	            Msg(S_INFO, "New file %s.", AFileName);
	        }
	        Loaded = true;
	        return false;
	    }
		 */

		if(!Console.FileExists(AFileName))
		{
			Msg(S_INFO, "New file %s.", AFileName);
			Loaded = true;
			return false;
			//return true;
		}

		Loading = true;
		Clear();
		BlockUnmark();
		BFI_SET(this, BFI_Undo, 0);
		BFI_SET(this, BFI_ReadOnly, 0);

		boolean first = true;

		while (true) 
		{
			char cFileBuffer[] = new char[RWBUFSIZE];

			len = reader.read(cFileBuffer);
			if( len <= 0 )
				break;

			String FileBuffer = new String(cFileBuffer).substring(0,len);

			if (first) {
				first = false;
				detectCrLf(FileBuffer, len);

				// again after detectCrLf
				strip = iBFI(this, BFI_StripChar);
				lchar = iBFI(this, BFI_LineChar);
				margin = iBFI(this, BFI_LoadMargin);
			}


			//p = FileBuffer;
			int ppos = 0;

			do {
				if (lchar != -1) {     // do we have a LINE delimiter
					//e = (String )memchr(p, lchar, FileBuffer - p + len);
					epos = FileBuffer.indexOf(lchar, ppos);
					if (epos == -1) {
						epos = len;
						lf = 0;
					} else
						lf = 1;
				} else if (margin != -1) { // do we have a right margin for wrap
					if (len >= ppos + margin) {
						epos = ppos + margin;
						lf = 1;
					} else {
						epos = len;
						lf = 0;
					}
				} else {
					epos = len;
					lf = 0;
				}
				partLen = epos - ppos; // # of chars in buffer for current line
				//m = (String )realloc(m, (lm + partLen) + CHAR_TRESHOLD);
				m.setSize((lm + partLen) + CHAR_TRESHOLD);

				//memcpy((m + lm), p, partLen);
				m.copyIn(lm, FileBuffer.substring(ppos), partLen);
				lm += partLen;
				numChars += partLen;

				if (lf!=0) {
					// there is a new line, add it to buffer

					//if (lm == 0 && m == null && (m = (String )malloc(CHAR_TRESHOLD)) == 0)
					//	goto fail;

					if (lm == 0)// && m == null)
						m.tryExtend(CHAR_TRESHOLD);

					/*#if 0
	                { // support for VIM tabsize commands
	                    String t = strstr(m,"vi:ts=");
	                    int ts = 0;
	                    if (t && isdigit(t[6]))
	                        ts = atoi(&t[6]);
	                    if (ts > 0 && ts <= 16)
	                        BFI(this, BFI_TabSize) = ts;
	                }
					#endif */

					// Grow the line table if required,
					if (RCount == RAllocated)
						Allocate(RCount !=0 ? (RCount * 2) : 1);

					LL[RCount++] = new ELine(m.substring(0, lm));
					RGap = RCount;

					lm = 0;
					//m = null;
					m.setSize(0);
					Lines++;
				}

				ppos = epos;
				if (lchar != -1) // skip LINE terminator/separator
					ppos++;
			} while (lf!=0);
			Msg(S_INFO, "Loading: %d lines, %d bytes.", Lines, numChars);
		}

		// No full line was created, create one, or store final line with no NL char at end
		if ((RCount == 0) || (lm > 0) || !BFI(this, BFI_ForceNewLine)) {
			//if (lm == 0 && m == null && (m = (String )malloc(CHAR_TRESHOLD)) == 0)
			//	throw new IOException();
			if (lm == 0)
				m.setSize(CHAR_TRESHOLD);

			// Grow the line table if required,
			if (RCount == RAllocated)
				Allocate(RCount!=0 ? (RCount * 2) : 1);
			LL[RCount++] = new ELine(m.toString());

			//m = null;
			m.setSize(0);
			RGap = RCount;
		}

		//m = null;
		m.setSize(0);

		// initialize folding array.
		VCount = RCount;
		VGap = VCount;
		AllocVis(VCount !=0 ? VCount : 1);
		//memset(VV, 0, VCount );
		Arrays.fill(VV, 0);

		if (strip != -1) { // strip CR character from EOL if specified

			// this should be done during load above to improve performance
			for (int l = 0; l < RCount; l++) {
				if (LL[l].getCount() > 0)
				{
					int llen = LL[l].getCount();
					//if (LL[l].Chars[LL[l].getCount() - 1] == strip)
					//    LL[l].Count--;
					if(LL[l].charAt(llen-1) == strip)
						//LL[l].Chars = LL[l].Chars.substring(0, llen-1);
						//LL[l].Chars.TryContract(llen-1);
						LL[l].Chars.setSize(llen-1);
				}
			}
		}

		/* TODO folds/bookmarks
	    if (BFI(this, BFI_SaveFolds) || iBFI(this, BFI_SaveBookmarks) == 1 || iBFI(this, BFI_SaveBookmarks) == 2) 
	    {
	        int len_start = 0, len_end = 0;
	        int level = 0, open = 0;
	        int l;
	        int pos = -1, startpos;
	        char [] foldnum = "00".toCharArray();

	        if (BFS(this, BFS_CommentStart) == null) len_start = 0;
	        else len_start = BFS(this, BFS_CommentStart).length();
	        if (BFS(this, BFS_CommentEnd) == null) len_end = 0;
	        else len_end = BFS(this, BFS_CommentEnd).length();

		for (l = RCount - 1; l >= 0; l--) {
		    if (LL[l].getCount() >= len_start + len_end + 6) {
	                for (int where = 1; where < 3; where++) {
	                    // where == 1 - start-of-line
	                    // where == 2 - end-of-line
	                    open = -1;
	                    level = -1;
	                    if (iBFI(this, BFI_SaveFolds) != where && iBFI(this, BFI_SaveBookmarks) != where) continue;
	                    if (where == 1) {
	                        pos = 0;
	                    } else {
	                        pos = LL[l].getCount() - len_end;
	                        // Check if line ends with end comment (if defined)
	                        if (len_end != 0 && CString.memcmp(LL[l].Chars, pos, BFS(this, BFS_CommentEnd), len_end) != 0) continue;
	                        if (iBFI(this, BFI_SaveBookmarks) == 2 && pos - 10 >= 0 && LL[l].Chars.charAt(pos-1) == 'b') { // Bookmarks can be at end
	                            //char numbuf[5];
	                        	CString numbuf = new CString(5);
	                            int i;

	                            numbuf.memmove(0, LL[l].Chars, pos - 5, 4); numbuf.w(4, 0);
	                            if (1 != sscanf(numbuf, "%x", i)) continue;
	                            pos -= i + 6;
	                            if (pos < 0) continue;
	                        }
	                        if (iBFI(this, BFI_SaveFolds) == 2 && pos - 6 >= 0 &&
	                            (CString.memcmp(LL[l].Chars, pos - 6, "FOLD", 4) == 0 ||
	                            		CString.memcmp(LL[l].Chars, pos - 6, "fold", 4) == 0)) pos -= 6;
	                        pos -= len_start;
	                    }
	                    // Check comment start
	                    if (pos < 0 || (len_start != 0 && CString.memcmp(LL[l].Chars, pos, BFS(this, BFS_CommentStart), len_start) != 0)) continue;
	                    startpos = pos;
	                    pos += len_start;
	                    // We have starting position after comment start
	                    // Now we will read fold and/or bookmark info and check
	                    // for end of comment (if where == 2, it must end at EOLN)

	                    // This code is not very good, since on error we stop
	                    // parsing comments (and leave it in file), but everything
	                    // already done is not undone (e.g. bookmarks, folds)

	                    // Folds come always first
	                    if (iBFI(this, BFI_SaveFolds) == where && (pos + len_end + 6 <= LL[l].getCount())) {
	                        if (CString.memcmp(LL[l].Chars, pos, "FOLD", 4) == 0) {
	                            open = 1;
	                        } else if (CString.memcmp(LL[l].Chars, pos, "fold", 4) == 0) {
	                            open = 0;
	                        } else
	                            open = -1;
	                    }
	                    if (open != -1) {
	                        foldnum[0] = LL[l].Chars.charAt(pos + 4);
	                        foldnum[1] = LL[l].Chars.charAt(pos + 4 + 1);
	                        if (1 != sscanf(foldnum, "%2d", level))
	                            level = -1;

	                        if (!CString.isdigit(LL[l].Chars.charAt(pos + 4)) ||
	                            !CString.isdigit(LL[l].Chars.charAt(pos + 5)))
	                            level = -1;

	                        if (level == -1 || open >= 100) continue;
	                        pos += 6;
	                    }

	                    // Now get bookmarks
	                    if (iBFI(this, BFI_SaveBookmarks) == where && (pos + len_end + 10 <= LL[l].getCount()) && CString.memcmp(LL[l].Chars, pos, "BOOK", 4) == 0) {
	                        int error = 0;
	                        int i, col, startBook;
	                        //char numbuf[5], buf[256];

	                        startBook = pos; pos += 4;
	                        while (pos + len_end + 6 + 6 <= LL[l].getCount()) {
	                            // Read column
	                        	CString.memcpy(numbuf, LL[l].Chars + pos, 4); numbuf[4] = 0;
	                            pos += 4;
	                            if (1 != sscanf(numbuf, "%x", col)) {
	                                error = 1; break;
	                            }
	                            // Read length
	                            CString.memcpy(numbuf, LL[l].Chars + pos, 2); numbuf[2] = 0;
	                            pos += 2;
	                            if (1 != sscanf(numbuf, "%x", i)) {
	                                error = 1; break;
	                            }
	                            if (pos + i + 6 + len_end > LL[l].getCount() || i == 0) {
	                                error = 1; break;
	                            }
	                            if (i) {
	                            	CString.memcpy(buf, LL[l].Chars + pos, i);
	                                pos += i;
	                                if (!PlaceUserBookmark(buf, EPoint(l, col))) throw new IOException("PlaceUserBookmark failed");//goto fail;
	                            }
	                            if (LL[l].Chars.charAt(pos) == 'x') {
	                                // Read total length (just test for correctness)
	                            	CString.memcpy(numbuf, LL[l].Chars + pos + 1, 4);
	                                numbuf[4] = 0;
	                                if (1 != sscanf(numbuf, "%x", i)) {
	                                    error = 1; break;
	                                }
	                                if (i != pos - startBook || LL[l].Chars.charAt(pos + 5) != 'b') error = 1;
	                                else pos += 6;
	                                break;
	                            }
	                        }
	                        // Stop parsing this comment if error occured
	                        if (error) continue;
	                    }

	                    // And last: check, if comment is properly terminated
	                    if (pos + len_end > LL[l].getCount()) continue;
	                    if (len_end != 0 && LL[l].Chars.memcmp(pos, BFS(this, BFS_CommentEnd), len_end) != 0) continue;
	                    // Not at EOLN, but should be (comment at EOLN)
	                    if (where == 2 && LL[l].getCount() != pos + len_end) continue;
	                    pos += len_end;

	                    // Create fold if whole comment was successfully parsed
	                    if (open != -1) {
	                        int f;

	                        if (!FoldCreate(l)) throw new IOException("FoldCreate failed");//goto fail;
	                        f = FindFold(l);
	                        assert(f != -1);
	                        FF[f].level = (char)(level & 0xFF);
	                        if (open == 0)
	                            if (!FoldClose(l)) throw new IOException("FoldClose failed");//goto fail;
	                    }
	                    // Now remove parsed comment from line
	                    //memmove(LL[l].Chars + startpos,	LL[l].Chars + pos, LL[l].getCount() - pos);
	                    //LL[l].Count -= pos - startpos;
	                    
	                    LL[l].Chars.memmove(startpos, pos, LL[l].getCount() - pos);
	                    LL[l].Chars.setSize( LL[l].getCount() - (pos - startpos) );
	                }
	            }
	        }
	    }
	    //folds/bookmarks */

		if (!SetPosR(0, 0)) return false; 

		return true;

	}
	private void detectCrLf(String FileBuffer, int len) {
		if (BFI(this, BFI_DetectLineSep)) {
			int was_lf = 0, was_cr = 0;
			for (int c = 0; c < len; c++) {
				if (FileBuffer.charAt(c) == 10) {
					was_lf++;
					break;
				} else if (FileBuffer.charAt(c) == 13) {
					was_cr++;
					if (was_cr == 2)
						break;
				}
			}
			/* !!! fails if first line > 32k
			 * ??? set first to 1 in that case ? */
			if (was_cr!=0 || was_lf!=0) {
				BFI_SET(this, BFI_StripChar, -1);
				BFI_SET(this, BFI_LoadMargin, -1);
				BFI_SET(this, BFI_AddLF, 0);
				BFI_SET(this, BFI_AddCR, 0);
				if (was_lf!=0) {
					BFI_SET(this, BFI_LineChar, 10);
					BFI_SET(this, BFI_AddLF, 1);
					if (was_cr!=0) {
						BFI_SET(this, BFI_StripChar, 13);
						BFI_SET(this, BFI_AddCR, 1);
					}
				} else if (was_cr!=0) {
					BFI_SET(this, BFI_LineChar, 13);
					BFI_SET(this, BFI_AddCR, 1);
				} else {
					BFI_SET(this, BFI_LineChar, -1);
					BFI_SET(this, BFI_LoadMargin, 64);
				}
			}
		}
	}

	private static String MakeBackup(String FileName, String [] NewName) {
		//	    static char NewName[260];

		if(FileName.isBlank())
			return null;

		NewName[0] = FileName+"~";

		if (!Console.IsSameFile(FileName,NewName[0])) {
			if (Console.FileExists(NewName[0]))                 // Backup already exists?
				Console.unlink(NewName[0]);                         // Then delete the file..
			if (!Console.FileExists(FileName))                // Original found?
				return NewName[0];
			if (Console.copyfile(FileName, NewName[0]))
				return NewName[0];
		}

		return null;
	}


	boolean SaveTo(String AFileName) {
		String [] ABackupName = {""};

		Msg(S_INFO, "Backing up %s...", AFileName);
		if (MakeBackup(AFileName, ABackupName) == null) {
			View.MView.Win.Choice(GPC_ERROR, "Error", 1, "O&K", "Could not create backup file.");
			return false;
		}
		Msg(S_INFO, "Writing %s...", AFileName); 

		try(BufferedWriter writer = Files.newBufferedWriter(Path.of(AFileName), Main.charset)) {
			boolean rc = doSaveTo(writer, AFileName);

			Msg(S_INFO, "Wrote %s.", AFileName);
			if (!BFI(this, BFI_KeepBackups)
					// No backups for CVS logs
					|| this == ECvsLog.CvsLogView
					) {
				Console.unlink(ABackupName[0]);
			}


			return rc;

		}
		catch(IOException e)
		{
			Console.unlink(AFileName);
			if (!Console.rename(ABackupName[0], AFileName)) {
				View.MView.Win.Choice(GPC_ERROR, "Error", 1, "O&K", "Error renaming backup file to original!");
			} else {
				View.MView.Win.Choice(GPC_ERROR, "Error", 1, "O&K", "Error writing file, backup restored.");
			}

			return false;
		}

	}


	private boolean doSaveTo(BufferedWriter writer, String AFileName) throws IOException {
		//struct stat StatBuf;

		//FILE *fp;
		int l;
		//ELine L;
		long ByteCount = 0, OldCount = 0;

		//int f;
		//char fold[64];
		//unsigned int foldlen = 0;

		//int bindex;
		//unsigned int blen = 0;
		//String bname, book = "BOOK";
		//EPoint bpos;

		//unsigned int len_start = 0, len_end = 0;

		BasicFileAttributes curr = Console.stat(FileName);

		if (FileOk && (curr != null)) 
		{
			if( (!FileStatus.lastModifiedTime().equals(curr.lastModifiedTime()) ) ||
					FileStatus.size() != curr.size())
			{
				switch (View.MView.Win.Choice(GPC_ERROR, "File Changed on Disk",
						2,
						"&Save",
						"&Cancel",
						"%s", FileName))
				{
				case 0:
					break;
				case 1:
				case -1:
				default:
					return false;
				}
			}
		}

		if (RCount <= 0) return false;
		//Msg(S_INFO, "Backing up %s...", AFileName);
		/*if (MakeBackup(AFileName, ABackupName) == 0) {
	        View.MView.Win.Choice(GPC_ERROR, "Error", 1, "O&K", "Could not create backup file.");
	        return false;
	    }
	    Msg(S_INFO, "Writing %s...", AFileName); */

		/*
	    fp = 0;
	    if (fp == 0)
	        fp = fopen(AFileName, "wb");
	    if (fp == 0) goto erroropen;

	    setvbuf(fp, FileBuffer, _IOFBF, sizeof(FileBuffer));
		 */

		// Some initializations (to speed-up saving)
		int len_start = 0, len_end = 0;

		if (BFS(this, BFS_CommentStart)!=null) len_start = BFS(this, BFS_CommentStart).length();
		if (BFS(this, BFS_CommentEnd)!=null) len_end = BFS(this, BFS_CommentEnd).length();

		for (l = 0; l < RCount; l++) {
			ELine L = RLine(l);
			int blen = 0;

	        int f = FindFold(l);
	        String fold = null;
	        // format fold
	        if ((f != -1) && (iBFI(this, BFI_SaveFolds) != 0)) {
	            fold = String.format(
	                              FF[f].open ? "FOLD%02d" : "fold%02d",
	                              FF[f].level);
	        }
			int foldlen = fold.length();
	        
	        //bindex = 0; 
	        blen = 0;
			// */

			String book = "BOOK";
			if (iBFI(this, BFI_SaveBookmarks) == 1 || iBFI(this, BFI_SaveBookmarks) == 2) {
				//int bindex;
				//String [] bname = {""};
				//EPoint [] bpos = {new EPoint()};

				//blen = 4;     // Just after "BOOK"

				List<EBookmark> bl = GetUserBookmarkForLine(l);

				for(EBookmark b: bl) {
					// Skip too long bookmarks
					//if (strlen(bname) > 256 || blen + strlen(bname) + 6 + 6 > sizeof(book)) continue;
					book += String.format("%04x%02x%s", b.BM.Col, b.Name.length(), b.Name );
				}
				blen = book.length();
				if (!bl.isEmpty()) {
					book += String.format( "x%04xb", blen);
				} else blen = 0;      // Signal, that no bookmarks were saved
			}


			// what - write at 1 = beginning / 2 = end of line
			for (int what = 1; what < 3; what++) {
				if ((iBFI(this, BFI_SaveFolds) == what && foldlen!=0) ||
						(iBFI(this, BFI_SaveBookmarks) == what && blen!=0)
						) {

					if (len_start!=0) {
						//if (fwrite(BFS(this, BFS_CommentStart), 1, len_start, fp) != len_start) throw new IOException(); //goto fail;
						writer.write(BFS(this, BFS_CommentStart), 0, len_start);
						ByteCount += len_start;
					}
 
					if (iBFI(this, BFI_SaveFolds) == what && foldlen > 0) {
					
	                    //if (fwrite(fold, 1, foldlen, fp) != foldlen) goto fail;
	                    writer.write(fold);
	                    ByteCount += foldlen;
	                } //*/

					/*
	                if (iBFI(this, BFI_SaveBookmarks) == what && blen > 0) {
	                    if (fwrite(book, 1, blen, fp) != blen) goto fail;
	                    ByteCount += blen;
	                }
					// */
					if (iBFI(this, BFI_SaveBookmarks) == what && blen != 0) {
						//if (fwrite(book, 1, blen, fp) != blen) goto fail;
						writer.write(book);
						ByteCount += blen;
					}

					if (len_end!=0) {
						//if (fwrite(BFS(this, BFS_CommentEnd), 1, len_end, fp) != len_end) goto fail;
						writer.write(BFS(this, BFS_CommentEnd), 0, len_end);
						ByteCount += len_end;
					}
				}
				if (what == 1) {
					// write data
					//if ((int)(fwrite(L.Chars, 1, L.getCount(), fp)) != L.getCount())
					//    goto fail;
					writer.write(L.toString());
					ByteCount += L.getCount();
				}
			}
			// write eol
			if ((l < RCount - 1) || BFI(this, BFI_ForceNewLine)) {
				if (iBFI(this, BFI_AddCR) == 1) {
					//if (fputc(13, fp) < 0) goto fail;
					writer.write("\r");
					ByteCount++;
				}
				if (iBFI(this, BFI_AddLF) == 1) {
					//if (fputc(10, fp) < 0) goto fail;
					writer.write("\n");
					ByteCount++;
				}
			}
			if (ByteCount > OldCount + 65536) {
				Msg(S_INFO, "Saving: %d lines, %d bytes.", l, ByteCount);
				OldCount = ByteCount;
			}
		}
		//if (fclose(fp) != 0) goto fail;
		writer.close();
		Modified = 0;
		FileOk = true;
		if ((FileStatus=Console.stat(FileName)) == null) {
			//memset(FileStatus, 0, sizeof(FileStatus));
			FileOk = false;
			//goto fail;
			throw new IOException();
		}
		return true;
		/*
	fail:
	    fclose(fp);
	    unlink(AFileName);
	    if (rename(ABackupName, AFileName) == -1) {
	        View.MView.Win.Choice(GPC_ERROR, "Error", 1, "O&K", "Error renaming backup file to original!");
	    } else {
	        View.MView.Win.Choice(GPC_ERROR, "Error", 1, "O&K", "Error writing file, backup restored.");
	    }
	    return 0;
	erroropen:
	    View.MView.Win.Choice(GPC_ERROR, "Error", 1, "O&K", "Error writing %s (errno=%d).", AFileName, errno);
	    return 0;
		 */
	}























	boolean PlaceBookmark(String Name, EPoint P) {
		BMarks.put(Name, new EBookmark(Name,P));
		/*
	    int i;
	    EBookmark *p;

	    assert(P.Row >= 0 && P.Row < RCount && P.Col >= 0);

	    for (i = 0; i < BMCount; i++) {
	        if (strcmp(Name, BMarks[i].Name) == 0) {
	            BMarks[i].BM = P;
	            return 1;
	        }
	    }
	    p = (EBookmark *) realloc(BMarks, sizeof (EBookmark) * (1 + BMCount));
	    if (p == 0) return 0;
	    BMarks = p;
	    BMarks[BMCount].Name = strdup(Name);
	    BMarks[BMCount].BM = P;
	    BMCount++;
		 */
		return true;
	}

	boolean RemoveBookmark(String Name) {
		/*
	    int i;

	    for (i = 0; i < BMCount; i++) {
	        if (strcmp(Name, BMarks[i].Name) == 0) {
	            free(BMarks[i].Name);
	            memmove(BMarks + i, BMarks + i + 1, sizeof(EBookmark) * (BMCount - i - 1));
	            BMCount--;
	            BMarks = (EBookmark *) realloc(BMarks, sizeof (EBookmark) * BMCount);
	            return 1;
	        }
	    }
		 */
		if( null != BMarks.remove(Name))
			return true;

		View.MView.Win.Choice(GPC_ERROR, "RemoveBookmark", 1, "O&K", "Bookmark %s not found.", Name);
		return false;
	}

	EPoint GetBookmark(String Name) {
		EBookmark b = BMarks.get(Name);
		if(b != null) return b.BM;
		return null;
		/*
	    for (int i = 0; i < BMCount; i++)
	        if (strcmp(Name, BMarks[i].Name) == 0) {
	            P = BMarks[i].BM;
	            return 1;
	        }
	    return 0;
		 */
	}


	List<EBookmark> GetBookmarksForLine(int searchForLine) 
	{
		List<EBookmark> ret = new ArrayList<>();

		for( EBookmark bm : BMarks.values() )	
		{
			if (searchForLine==-1||bm.BM.Row==searchForLine) 
				ret.add(bm);
			/*{
	            Name[0] = bm.Name;
	            P[0] = bm.BM;
	            return true;
	        }*/
		}
		return ret;
	}


	boolean GotoBookmark(String Name) {
		EBookmark b = BMarks.get(Name);

		if(b!=null)
			return CenterNearPosR(b.BM.Col, b.BM.Row, 0);

		View.MView.Win.Choice(GPC_ERROR, "GotoBookmark", 1, "O&K", "Bookmark %s not found.", Name);
		return false;
	}


















	static char cr = 13;
	static char lf = 10;

	boolean BlockPrint() {

		PrintService printService =
				PrintServiceLookup.lookupDefaultPrintService();

		if(printService==null)
		{
			Msg(S_ERROR, "No print service found");
			return false;
		}

		Msg(S_INFO, "Printing block to %s...", printService.getName());

		DocPrintJob job = printService.createPrintJob();
		//DocFlavor docFlavor = DocFlavor.INPUT_STREAM.AUTOSENSE;
		//DocFlavor docFlavor = DocFlavor.INPUT_STREAM.TEXT_PLAIN_HOST;
		DocFlavor docFlavor = DocFlavor.STRING.TEXT_PLAIN;

		
		StringWriter sw = new StringWriter();
		boolean rc = doBlockPrint(sw);

		Doc doc = new SimpleDoc( sw.toString(), docFlavor, null);

		try {
			job.print(doc, null);
		} catch (PrintException e) {
			Msg(S_ERROR, "Error printing block to %s: %s", printService.getName(), e.getMessage());
			return false;
		}		

		if(rc)
			Msg(S_INFO, "Printed block.");
		else
			Msg(S_ERROR, "Error printing block to %s.", printService.getName());

		return rc;
	}	
	
	//*
	boolean doBlockPrint(StringWriter sw) {
		EPoint B, E;
		int L;
		int A, Z;
		ELine LL;
		int bc = 0, lc = 0;
		//int error = 0;

		AutoExtend = false;
		if (!CheckBlock()) return false;
		if (RCount == 0) return false;
		B = BB;
		E = BE;
		//Msg(S_INFO, "Printing to %s...", PrintDevice);


		for (L = B.Row; L <= E.Row; L++) {
			A = -1;
			Z = -1;
			LL = RLine(L);
			switch (BlockMode) {
			case bmLine:
				if (L < E.Row) {
					A = 0;
					Z = LL.getCount();
				}
				break;
			case bmColumn:
				if (L < E.Row) {
					A = CharOffset(LL, B.Col);
					Z = CharOffset(LL, E.Col);
				}
				break;
			case bmStream:
				if (B.Row == E.Row) {
					A = CharOffset(LL, B.Col);
					Z = CharOffset(LL, E.Col);
				} else if (L == B.Row) {
					A = CharOffset(LL, B.Col);
					Z = LL.getCount();
				} else if (L < E.Row) {
					A = 0;
					Z = LL.getCount();
				} else if (L == E.Row) {
					A = 0;
					Z = CharOffset(LL, E.Col);
				}
				break;
			}
			if (A != -1 && Z != -1) {
				if (A < LL.getCount()) {
					if (Z > LL.getCount())
						Z = LL.getCount();
					if (Z > A) {
						sw.write(LL.Chars.substring(A, Z).toString());
						/*if ((int)(fwrite(LL.Chars + A, 1, Z - A, fp)) != Z - A) {
							error++;
							break;
						} else */
							bc += Z - A;
					}
				}
				if (BFI(this, BFI_AddCR))
				{
					sw.write(cr);
					/*if (fwrite(&cr, 1, 1, fp) != 1) {
						error++;
						break;
					} else */
						bc++;
				}
				if (BFI(this, BFI_AddLF))
				{
					sw.write(lf);
					/*if (fwrite(&lf, 1, 1, fp) != 1) {
						error++;
						break;
					} else */ {
						bc++;
						lc++;
					}
				}
				if ((lc % 200) == 0)
					Msg(S_INFO, "Printing, %d lines, %d bytes.", lc, bc);

			}
		}
		/*if (!error) {
			fwrite("\f\n", 2, 1, fp);
			fclose(fp);
			Msg(S_INFO, "Printing %d lines, %d bytes.", lc, bc);
			return 1;
		}
		fclose(fp);
		Msg(S_INFO, "Failed to write to %s", PrintDevice);
		return 0; */
		return true;
	}
	 //*/



	boolean FilePrint() {

		PrintService printService =
				PrintServiceLookup.lookupDefaultPrintService();

		if(printService==null)
		{
			Msg(S_ERROR, "No print service found");
			return false;
		}

		//Arrays.stream(
		DocFlavor[] fs = printService.getSupportedDocFlavors();
		
		
		for( DocFlavor f : fs )
		{
			System.out.println(f.getMediaType()+":"+f.getMimeType()+":"+f.getRepresentationClassName());
		}
		
		Msg(S_INFO, "Printing %s to %s...", FileName, printService.getName());

		DocPrintJob job = printService.createPrintJob();
		//DocFlavor docFlavor = DocFlavor.INPUT_STREAM.AUTOSENSE;
		DocFlavor docFlavor = DocFlavor.STRING.TEXT_PLAIN;

		StringWriter sw = new StringWriter();
		boolean rc = doFilePrint(sw);

		Doc doc = new SimpleDoc( sw.toString(), docFlavor, null);

		try {
			job.print(doc, null);
		} catch (PrintException e) {
			Msg(S_ERROR, "Error printing %s to %s: %s", FileName, printService.getName(), e.getMessage());
			return false;
		}		

		if(rc)
			Msg(S_INFO, "Printed %s.", FileName);
		else
			Msg(S_ERROR, "Error printing %s to %s.", FileName, printService.getName());

		return rc;
	}

	boolean doFilePrint(StringWriter sw) {
		//int l;
		//FILE *fp;
		//long ByteCount = 0;
		//int BChars;

		/*
	        fp = fopen(PrintDevice, "w");
	    if (fp == null) {
	        Msg(S_ERROR, "Error printing %s to %s.", FileName, PrintDevice);
	        return false;
	    }*/

		//BChars = 0;
		for (int l = 0; l < RCount; l++) {


			sw.write(RLine(l).Chars.toString());

			if (BFI(this, BFI_AddCR)) sw.write( cr );
			/*if (BFI(this, BFI_AddLF))*/ sw.write( lf );

		}

		/*
	    if (BChars) {
	        ByteCount += BChars;
	        Msg(S_INFO, "Printing: %d lines, %d bytes.", l, ByteCount);
	        //if ((int)(fwrite(FileBuffer, 1, BChars, fp)) != BChars) goto fail;
	    }
	    BChars = 0;
	    //fclose(fp);
	    return 1;
	fail:
	    if (fp != null) {
	            fclose(fp);
	    }
	    return 0;
		 */
		return true;
	}

















	public boolean PlaceUserBookmark( String n,EPoint P) {
		String name = "_BMK"+n;
		EPoint prev;

		if ((prev=GetBookmark(name))!=null) 
		{
			prev.Row=-1;
			prev.Col=-1;
		}

		boolean result = PlaceBookmark(name, P);

		if (result) {
			if (BFI (this,BFI_ShowBookmarks)) {
				FullRedraw ();
			}
			if(iBFI(this,BFI_SaveBookmarks)==1||iBFI(this,BFI_SaveBookmarks)==2) {
				if (!Modify ()) return result;   // Never try to save to read-only

				getSlot().
				pushInt(prev.Row).
				pushInt(prev.Col).
				pushString(n).
				pushOp( UndoOperation.ucPlaceUserBookmark);
			}
		}
		return result;
	}

	public boolean RemoveUserBookmark(String n) {
		String name  = "_BMK"+n;
		boolean result;
		EPoint p;

		p = GetBookmark(name);       // p is valid only if remove is successful
		result=RemoveBookmark(name);
		if (result) {
			if (BFI (this,BFI_ShowBookmarks)) {
				FullRedraw ();
			}
			if (iBFI (this,BFI_SaveBookmarks)==1||iBFI (this,BFI_SaveBookmarks)==2) {
				if (!Modify ()) return result;   // Never try to save to read-only
				/* #ifdef CONFIG_UNDOREDO
	            if (PushULong(p.Row) == 0) return 0;
	            if (PushULong(p.Col) == 0) return 0;
	            if (PushUData((void *)n,strlen (n)+1) == 0) return 0;
	            if (PushULong(strlen (n)+1) == 0) return 0;
	            if (PushUChar(ucRemoveUserBookmark) == 0) return 0;
	#endif */
				getSlot().
				pushInt(p.Row).
				pushInt(p.Col).
				pushString(n).
				pushOp( UndoOperation.ucRemoveUserBookmark);

			}
		}
		return result;
	}

	boolean GotoUserBookmark( String n) {
		return GotoBookmark("_BMK"+n);
	}

	List<EBookmark> GetUserBookmarkForLine(int searchForLine) 
	{
		List<EBookmark> ret = new ArrayList<>();

		for(EBookmark b : GetBookmarksForLine(searchForLine)) 
		{

			if(b.Name.substring(0, 4).equals("_BMK"))
				ret.add(b);
		}

		return ret;
	}

	boolean PlaceBookmark(ExState State) {
		String [] name = {""};
		EPoint P = CP;

		P.Row = VToR(P.Row);

		if (State.GetStrParam(View, name ) == 0)
			if (View.MView.Win.GetStr("Place Bookmark", name, HIST_BOOKMARK) == 0) return false;
		return PlaceUserBookmark(name[0], P);
	}

	boolean RemoveBookmark(ExState State) {
		String [] name = {""};

		if (State.GetStrParam(View, name) == 0)
			if (View.MView.Win.GetStr("Remove Bookmark", name, HIST_BOOKMARK) == 0) return false;
		return RemoveUserBookmark(name[0]);
	}

	boolean GotoBookmark(ExState State) {
		String [] name = {""};

		if (State.GetStrParam(View, name) == 0)
			if (View.MView.Win.GetStr("Goto Bookmark", name, HIST_BOOKMARK) == 0) return false;
		return GotoUserBookmark(name[0]);
	}


	boolean PlaceGlobalBookmark(ExState State) {
		String [] name = {""};
		EPoint P = CP;

		P.Row = VToR(P.Row);

		if (State.GetStrParam(View, name) == 0)
			if (View.MView.Win.GetStr("Place Global Bookmark", name, HIST_BOOKMARK) == 0) return false;
		if (EMarkIndex.markIndex.insert(name[0], this, P) == null) {
			Msg(S_ERROR, "Error placing global bookmark %s.", name[0]);
		}
		return true;
	}

	boolean PushGlobalBookmark() {
		EPoint P = CP;

		P.Row = VToR(P.Row);
		EMark m = EMarkIndex.markIndex.pushMark(this, P);
		if (m != null)
			Msg(S_INFO, "Placed bookmark %s", m.getName());
		return m != null;
	}








	@Override
	EViewPort CreateViewPort(EView V) {
		V.Port = new EEditPort(this, V);
		AddView(V);

		if (!Loaded && suspendLoads == 0) 
		{
			Load();

			if (EMessages.CompilerMsgs != null)
				EMessages.CompilerMsgs.FindFileErrors(this);

			if (ECvsDiff.CvsDiffView != null) ECvsDiff.CvsDiffView.FindFileLines(this);

			EMarkIndex.markIndex.retrieveForBuffer(this);

			int [] r = {0}, c = {0};

			if (FPosHistory.RetrieveFPos(FileName, r, c))
				SetNearPosR(c[0], r[0]);
			//printf("setting to c:%d r:%d f:%s", c, r, FileName);
			V.Port.GetPos();
			V.Port.ReCenter = 1;

			if (iBFI (this,BFI_SaveBookmarks)==3) FPosHistory.RetrieveBookmarks(this);
		}
		return V.Port;
	}























	static int Hilit_Plain(EBuffer BF, int LN, PCell B, int Pos, int Width, ELine Line, int /*hlState*/ State, int /*hsState*/ [] StateMap, int []ECol) {
		//ChColor *Colors = BF.Mode.fColorize.Colors;
		//  
		//int[] Colors = BF.Mode.fColorize.Colors;
		//HILIT_VARS(Colors[CLR_Normal], Line);

		//PCLI BPtr; 
		int BPos; 
		// TOD int /*ChColor*/ Color = Colors[CLR_Normal];
		int Color = hcPlain_Normal;
		//int i; 
		//int len = Line.getCount(); 
		//String p = Line.Chars;
		//int pp = 0;
		int NC = 0, C = 0; 
		int TabSize = EBuffer.iBFI(BF, BFI_TabSize); 
		boolean ExpandTabs = EBuffer.BFI(BF, BFI_ExpandTabs);


		/* TODO #ifdef CONFIG_WORD_HILIT
	    int j = 0;

	    if (BF.Mode.fColorize.Keywords.TotalCount > 0 ||
	        BF.WordCount > 0)
	    { //* words have to be hilited, go slow 
	        for(int i = 0; i < Line.getCount(); ) 
	        {
	            IF_TAB() else {
	                if (isalpha(*p) || (*p == '_')) {
	                    j = 0;
	                    while (((i + j) < Line.getCount()) &&
	                           (isalnum(Line.Chars[i+j]) ||
	                            (Line.Chars[i + j] == '_'))
	                          ) j++;
	                    if (BF.GetHilitWord(j, Line.Chars + i, Color, 1)) ;
	                    else {
	                        Color = Colors[CLR_Normal];
	                        State = hsPLAIN_Normal;
	                    }
	                    if (StateMap)
	                        memset(StateMap + i, State, j);
	                    if (B != null)
	                        B.MoveMem( C - Pos, Width, Line.Chars + i, Color, j);
	                    i += j;
	                    len -= j;
	                    p += j;
	                    C += j;
	                    State = hsPLAIN_Normal;
	                    Color = Colors[CLR_Normal];
	                    continue;
	                }
	                ColorNext();
	                continue;
	            }
	        }
	    } else
		 */ // #endif
		/* TOD 
		  if (ExpandTabs) { // use slow mode 
	        for (i = 0; i < Line.getCount();) {
	            //IF_TAB() else {	                ColorNext();	            }

	            if (*p == '\t' && ExpandTabs) { 
	                NC = NextTab(C, TabSize); 
	                if (StateMap) StateMap[i] = hsState(State);
	                if (B) MoveChar(B, C - Pos, Width, ' ', Color, NC - C);
	                if (BFI(BF, BFI_ShowTabs)) ColorChar();
	                i++,len--,p++;
	                C = NC;
	                continue;
	                else {	                ColorNext();	            }

	        }
	    } else */ { /* fast mode */
	    	if (Pos < Line.getCount()) {
	    		if (Pos + Width < Line.getCount()) {
	    			if (B != null) 
	    				//B.MoveMem(0, Width, Line.Chars + Pos, Color, Width);
	    				B.MoveMem(0, Width, Line.Chars, Pos, Color, Width);
	    			if (StateMap != null)
	    				//memset(StateMap, State, Line.getCount());
	    				Arrays.fill(StateMap, 0, Line.getCount(), State);

	    		} else {
	    			if (B != null) 
	    				//B.MoveMem(0, Width, Line.Chars, + Pos, Color, Line.getCount() - Pos);
	    				B.MoveMem(0, Width, Line.Chars, Pos, Color, Line.getCount() - Pos);
	    			if (StateMap != null)
	    				Arrays.fill(StateMap, 0, Line.getCount(), State);
	    			//memset(StateMap, State, Line.getCount());
	    		}
	    	}
	    	C = Line.getCount();
	    }
	    ECol[0] = C;
	    State = 0;
	    return 0;
	}








	boolean ScrollLeft(ExState State) {
		int [] Cols = {0};

		if (State.GetIntParam(View, Cols) == 0)
			Cols[0] = 8;
		return ScrollLeft(Cols[0]);
	}

	boolean ScrollRight(ExState State) {
		int [] Cols = {0};

		if (State.GetIntParam(View, Cols) == 0)
			Cols[0] = 8;
		return ScrollRight(Cols[0]);
	}

	boolean ScrollDown(ExState State) {
		int [] Rows = {0};

		if (State.GetIntParam(View, Rows) == 0)
			Rows[0] = 1;
		return ScrollDown(Rows[0]);
	}

	boolean ScrollUp(ExState State) {
		int [] Rows = {0};

		if (State.GetIntParam(View, Rows) == 0)
			Rows[0] = 1;
		return ScrollUp(Rows[0]);
	}



	boolean InsertString(ExState State) {
		//char strbuf[1024] = "";
		String [] strbuf = {null};

		if (State.GetStrParam(View, strbuf) == 0) {
			if (View.MView.Win.GetStr("Insert String", strbuf, HIST_DEFAULT) == 0)
				return false;
		}
		return InsertString(strbuf[0], strbuf[0].length());
	}

	boolean ChangeMode(ExState State) {
		//char Mode[32] = "";
		String [] Mode = {null};

		if (State.GetStrParam(View, Mode ) == 0)
			if (View.MView.Win.GetStr("Mode", Mode, HIST_SETUP) == 0) 
				return false;

		boolean rc = ChangeMode(Mode[0]);
		FullRedraw();
		return rc;
	}


	// XXX [dz] "Mode" must be "Flags"?
	boolean ChangeFlags(ExState State) {
		//char Mode[32] = "";
		String [] Mode = {null};

		if (State.GetStrParam(View, Mode) == 0)
			if (View.MView.Win.GetStr("Mode", Mode, HIST_SETUP) == 0) 
				return false;

		boolean rc = ChangeFlags(Mode[0]);
		FullRedraw();
		return rc;
	}



	boolean FileSaveAs(ExState State) {
		String [] FName = {FileName};

		if (State.GetStrParam(View, FName) == 0)
			if (View.MView.Win.GetFile("Save As", FName, HIST_PATH, GF_SAVEAS) == 0)
				return false;

		return FileSaveAs(FName[0]);
	}


	boolean FileSaveAs(String FName) {
		String [] Name = {null};

		if (Console.ExpandPath(FName, Name) == -1) {
			View.MView.Win.Choice(GPC_ERROR, "Error", 1, "O&K", "Invalid path: %s.", FName);
			return false;
		}
		if (Console.FindFile(Name[0]) == null) {
			if (Console.FileExists(Name[0])) {
				switch (View.MView.Win.Choice(GPC_ERROR, "File Exists",
						2,
						"&Overwrite",
						"&Cancel",
						"%s", Name[0]))
				{
				case 0:
					break;
				case 1:
				case -1:
				default:
					return false;

				}
			}

			FileName = Name[0].trim();
			UpdateTitle();
			return Save();
		} else {
			View.MView.Win.Choice(GPC_ERROR, "Error", 1, "O&K", "Already editing '%s.'", Name[0]);
			return false;
		}
	}

	boolean ASCIITable(ExState State) {
		int rc = View.MView.Win.PickASCII();
		if (rc != -1)
			return InsertChar((char)rc);

		return true;
	}















	@Override
	EEventMap GetEventMap() {
		return EEventMap.FindActiveMap(Mode);
	}


	@Override
	ExResult ExecCommand(ExCommands Command, ExState State) {
		try {
			return doExecCommand(Command, State) ? ExResult.ErOK : ExResult.ErFAIL;
		} catch (ExecException e) {
			Msg(S_ERROR, "Fail: ", e.getMessage());
			return ExResult.ErFAIL;
		}
	}


	private boolean doExecCommand(ExCommands Command, ExState State) throws ExecException 
	{
		switch (Command) {
		case ExMoveUp:                return MoveUp();
		case ExMoveDown:              return MoveDown();
		case ExMoveLeft:              return MoveLeft();
		case ExMoveRight:             return MoveRight();
		case ExMovePrev:              return MovePrev();
		case ExMoveNext:              return MoveNext();
		case ExMoveWordLeft:          return MoveWordLeft();
		case ExMoveWordRight:         return MoveWordRight();
		case ExMoveWordPrev:          return MoveWordPrev();
		case ExMoveWordNext:          return MoveWordNext();
		case ExMoveWordEndLeft:       return MoveWordEndLeft();
		case ExMoveWordEndRight:      return MoveWordEndRight();
		case ExMoveWordEndPrev:       return MoveWordEndPrev();
		case ExMoveWordEndNext:       return MoveWordEndNext();
		case ExMoveWordOrCapLeft:     return MoveWordOrCapLeft();
		case ExMoveWordOrCapRight:    return MoveWordOrCapRight();
		case ExMoveWordOrCapPrev:     return MoveWordOrCapPrev();
		case ExMoveWordOrCapNext:     return MoveWordOrCapNext();
		case ExMoveWordOrCapEndLeft:  return MoveWordOrCapEndLeft();
		case ExMoveWordOrCapEndRight: return MoveWordOrCapEndRight();
		case ExMoveWordOrCapEndPrev:  return MoveWordOrCapEndPrev();
		case ExMoveWordOrCapEndNext:  return MoveWordOrCapEndNext();
		case ExMoveLineStart:         return MoveLineStart();
		case ExMoveLineEnd:           return MoveLineEnd();
		case ExMovePageStart:         return MovePageStart();
		case ExMovePageEnd:           return MovePageEnd();
		case ExMovePageUp:            return MovePageUp();
		case ExMovePageDown:          return MovePageDown();
		case ExMovePageLeft:          return MovePageLeft();
		case ExMovePageRight:         return MovePageEnd();
		case ExMoveFileStart:         return MoveFileStart();
		case ExMoveFileEnd:           return MoveFileEnd();
		case ExMoveBlockStart:        return MoveBlockStart();
		case ExMoveBlockEnd:          return MoveBlockEnd();
		case ExMoveFirstNonWhite:     return MoveFirstNonWhite();
		case ExMoveLastNonWhite:      return MoveLastNonWhite();
		case ExMovePrevEqualIndent:   return MovePrevEqualIndent();
		case ExMoveNextEqualIndent:   return MoveNextEqualIndent();
		case ExMovePrevTab:           return MovePrevTab();
		case ExMoveNextTab:           return MoveNextTab();
		case ExMoveTabStart:          return MoveTabStart();
		case ExMoveTabEnd:            return MoveTabEnd();
		case ExMoveLineTop:           return MoveLineTop();
		case ExMoveLineCenter:        return MoveLineCenter();
		case ExMoveLineBottom:        return MoveLineBottom();
		case ExMoveBeginOrNonWhite:   return MoveBeginOrNonWhite();
		case ExMoveBeginLinePageFile: return MoveBeginLinePageFile();
		case ExMoveEndLinePageFile:   return MoveEndLinePageFile();
		case ExScrollLeft:            return ScrollLeft(State);
		case ExScrollRight:           return ScrollRight(State);
		case ExScrollDown:            return ScrollDown(State);
		case ExScrollUp:              return ScrollUp(State);
		case ExKillLine:              return KillLine();
		case ExKillChar:              return KillChar();
		case ExKillCharPrev:          return KillCharPrev();
		case ExKillWord:              return KillWord();
		case ExKillWordPrev:          return KillWordPrev();
		case ExKillWordOrCap:         return KillWordOrCap();
		case ExKillWordOrCapPrev:     return KillWordOrCapPrev();
		case ExKillToLineStart:       return KillToLineStart();
		case ExKillToLineEnd:         return KillToLineEnd();
		case ExKillBlock:             return KillBlock();
		case ExBackSpace:             return BackSpace();
		case ExDelete:                return Delete();

		case ExCharCaseUp:            return CharCaseUp();
		case ExCharCaseDown:          return CharCaseDown();
		case ExCharCaseToggle:        return CharCaseToggle();
		case ExLineCaseUp:            return LineCaseUp();
		case ExLineCaseDown:          return LineCaseDown();
		case ExLineCaseToggle:        return LineCaseToggle();

		case ExLineInsert:            return LineInsert();
		case ExLineAdd:               return LineAdd();
		case ExLineSplit:             return LineSplit();
		case ExLineJoin:              return LineJoin();
		case ExLineNew:               return LineNew();
		case ExLineIndent:            return LineIndent();
		case ExLineTrim:              return LineTrim();
		case ExLineCenter:            return LineCenter() != 0;
		case ExInsertSpacesToTab:
		{
			int [] no = {0};

			if(State.GetIntParam(View, no) == 0)
				no[0] = 0;
			return InsertSpacesToTab(no[0]);
		}
		case ExInsertTab:             return InsertTab();
		case ExInsertSpace:           return InsertSpace();
		case ExWrapPara:			  return WrapPara();

		case ExInsPrevLineChar:       return InsPrevLineChar();
		case ExInsPrevLineToEol:      return InsPrevLineToEol();
		case ExLineDuplicate:         return LineDuplicate();
		case ExBlockBegin:            return BlockBegin();
		case ExBlockEnd:              return BlockEnd();
		case ExBlockUnmark:           return BlockUnmark();
		case ExBlockCut:              return BlockCut(false);
		case ExBlockCopy:             return BlockCopy(false);
		case ExBlockCutAppend:        return BlockCut(true);
		case ExBlockCopyAppend:       return BlockCopy(true);
		case ExClipClear:             return ClipClear();
		case ExBlockPaste:            return BlockPaste();
		case ExBlockKill:             return BlockKill();
		case ExBlockIndent:
		{
			int saved_persistence = iBFI(this, BFI_PersistentBlocks);
			BFI_SET(this, BFI_PersistentBlocks, 1);
			boolean ret_code = BlockIndent();
			BFI_SET(this, BFI_PersistentBlocks, saved_persistence);
			return ret_code;
		}
		case ExBlockUnindent:
		{
			int saved_persistence = iBFI(this, BFI_PersistentBlocks);
			BFI_SET(this, BFI_PersistentBlocks, 1);
			boolean ret_code = BlockUnindent();
			BFI_SET(this, BFI_PersistentBlocks, saved_persistence);
			return ret_code ;
		}
		case ExBlockClear:            return BlockClear();
		case ExBlockMarkStream:       return BlockMarkStream();
		case ExBlockMarkLine:         return BlockMarkLine();
		case ExBlockMarkColumn:       return BlockMarkColumn();
		case ExBlockCaseUp:           return BlockCaseUp();
		case ExBlockCaseDown:         return BlockCaseDown();
		case ExBlockCaseToggle:       return BlockCaseToggle();
		case ExBlockExtendBegin:      return BlockExtendBegin();
		case ExBlockExtendEnd:        return BlockExtendEnd();
		case ExBlockReIndent:         return BlockReIndent();
		case ExBlockSelectWord:       return BlockSelectWord();
		case ExBlockSelectLine:       return BlockSelectLine();
		case ExBlockSelectPara:       return BlockSelectPara();
		case ExBlockUnTab:            return BlockUnTab();
		case ExBlockEnTab:            return BlockEnTab();

		case ExUndo:                  return Undo();
		case ExRedo:                  return Redo();

		case ExMatchBracket:          return MatchBracket();
		case ExMovePrevPos:           return MovePrevPos();
		case ExMoveSavedPosCol:       return MoveSavedPosCol();
		case ExMoveSavedPosRow:       return MoveSavedPosRow();
		case ExMoveSavedPos:          return MoveSavedPos();
		case ExSavePos:               return SavePos();

		case ExCompleteWord:          return CompleteWord();
		case ExBlockPasteStream:      return BlockPasteStream();
		case ExBlockPasteLine:        return BlockPasteLine();
		case ExBlockPasteColumn:      return BlockPasteColumn();
		case ExShowPosition:   		  return ShowPosition();

		case ExFoldCreate:            return FoldCreate(VToR(CP.Row));
		case ExFoldDestroy:           return FoldDestroy(VToR(CP.Row));
		case ExFoldDestroyAll:        return FoldDestroyAll();
		case ExFoldPromote:           return FoldPromote(VToR(CP.Row));
		case ExFoldDemote:            return FoldDemote(VToR(CP.Row));
		case ExFoldOpen:              return FoldOpen(VToR(CP.Row));
		case ExFoldOpenNested:        return FoldOpenNested();
		case ExFoldClose:             return FoldClose(VToR(CP.Row));
		case ExFoldOpenAll:           return FoldOpenAll();
		case ExFoldCloseAll:          return FoldCloseAll();
		case ExFoldToggleOpenClose:   return FoldToggleOpenClose();
		case ExFoldCreateAtRoutines:  return FoldCreateAtRoutines();
		case ExMoveFoldTop:           return MoveFoldTop();
		case ExMoveFoldPrev:          return MoveFoldPrev();
		case ExMoveFoldNext:          return MoveFoldNext();
		// */
		case ExFileSave:              return Save();
		case ExFilePrint:             return FilePrint();
		case ExBlockPrint:            return BlockPrint();
		case ExBlockTrim:             return BlockTrim();
		case ExFileTrim:              return FileTrim();
		case ExHilitWord:
			// TODO #ifdef CONFIG_WORD_HILIT
			//        return HilitWord();
			//#else
			return false; //ErFAIL;
			//#endif

		case ExSearchWordPrev:        return SearchWord(SEARCH_BACK | SEARCH_NEXT);
		case ExSearchWordNext:        return SearchWord(SEARCH_NEXT);
		case ExHilitMatchBracket:     return HilitMatchBracket();
		case ExToggleAutoIndent:      return ToggleAutoIndent();
		case ExToggleInsert:          return ToggleInsert();
		case ExToggleExpandTabs:      return ToggleExpandTabs();
		case ExToggleShowTabs:        return ToggleShowTabs();
		case ExToggleUndo:            return ToggleUndo();
		case ExToggleReadOnly:        return ToggleReadOnly();
		case ExToggleKeepBackups:     return ToggleKeepBackups();
		case ExToggleMatchCase:       return ToggleMatchCase();
		case ExToggleBackSpKillTab:   return ToggleBackSpKillTab();
		case ExToggleDeleteKillTab:   return ToggleDeleteKillTab();
		case ExToggleSpaceTabs:       return ToggleSpaceTabs();
		case ExToggleIndentWithTabs:  return ToggleIndentWithTabs();
		case ExToggleBackSpUnindents: return ToggleBackSpUnindents();
		case ExToggleWordWrap:        return ToggleWordWrap();
		case ExToggleTrim:            return ToggleTrim();
		case ExToggleShowMarkers:     return ToggleShowMarkers();
		case ExToggleHilitTags:       return ToggleHilitTags();
		case ExToggleShowBookmarks:   return ToggleShowBookmarks();
		case ExSetLeftMargin:         return SetLeftMargin();
		case ExSetRightMargin:        return SetRightMargin();

		case ExSetIndentWithTabs:     return SetIndentWithTabs(State);

		// stuff with UI
		case ExMoveToLine:          return MoveToLine(State);
		case ExMoveToColumn:        return MoveToColumn(State);
		case ExFoldCreateByRegexp:  return FoldCreateByRegexp(State);


		case ExPlaceBookmark:       return PlaceBookmark(State);
		case ExRemoveBookmark:      return RemoveBookmark(State);
		case ExGotoBookmark:        return GotoBookmark(State);
		case ExPlaceGlobalBookmark: return PlaceGlobalBookmark(State);
		case ExPushGlobalBookmark:  return PushGlobalBookmark();
		case ExInsertString:        return InsertString(State);

		case ExSelfInsert:          return SelfInsert(State);
		case ExFileReload:          return FileReload(State);
		case ExFileSaveAs:          return FileSaveAs(State);
		case ExFileWriteTo:         return FileWriteTo(State);
		case ExBlockRead:           return BlockRead(State);
		case ExBlockReadStream:     return BlockReadStream(State);
		case ExBlockReadLine:       return BlockReadLine(State);
		case ExBlockReadColumn:     return BlockReadColumn(State);
		case ExBlockWrite:          return BlockWrite(State);
		case ExBlockSort:           return BlockSort(0);
		case ExBlockSortReverse:    return BlockSort(1);
		case ExFind:                return Find(State);
		case ExFindReplace:         return FindReplace(State);
		case ExFindRepeat:          return FindRepeat(State);
		case ExFindRepeatOnce:      return FindRepeatOnce(State);
		case ExFindRepeatReverse:   return FindRepeatReverse(State);

		case ExSearch:              return Search(State);
		case ExSearchB:             return SearchB(State);
		case ExSearchRx:            return SearchRx(State);
		case ExSearchAgain:         return SearchAgain(State);
		case ExSearchAgainB:        return SearchAgainB(State);
		case ExSearchReplace:       return SearchReplace(State);
		case ExSearchReplaceB:      return SearchReplaceB(State);
		case ExSearchReplaceRx:     return SearchReplaceRx(State); 

		case ExInsertChar:          return InsertChar(State);
		case ExTypeChar:            return TypeChar(State);
		case ExChangeMode:          return ChangeMode(State);
		case ExChangeKeys:          return ChangeKeys(State);
		case ExChangeFlags:         return ChangeFlags(State);
		case ExChangeTabSize:       return ChangeTabSize(State);
		case ExChangeLeftMargin:    return ChangeLeftMargin(State);
		case ExChangeRightMargin:   return ChangeRightMargin(State);
		case ExASCIITable:			return ASCIITable(State);
		case ExCharTrans:           return CharTrans(State);
		case ExLineTrans:           return LineTrans(State);
		case ExBlockTrans:          return BlockTrans(State);

	    case ExTagFind:             return FindTag(State);
	    case ExTagFindWord:         return FindTagWord(State);

		// TODO case ExSetCIndentStyle:     return SetCIndentStyle(State);

		case ExBlockMarkFunction:   return BlockMarkFunction();
		case ExIndentFunction:      return IndentFunction();
		case ExMoveFunctionPrev:    return MoveFunctionPrev();
		case ExMoveFunctionNext:    return MoveFunctionNext();

		case ExInsertDate:          return InsertDate(State);
		case ExInsertUid:           return InsertUid();
		case ExShowHelpWord:        return ShowHelpWord(State);

		}

		return super.ExecCommand(Command, State) == ExResult.ErOK;
	}


	@Override
	boolean CanQuit() {
		return Modified == 0;
	}

	@Override
	int ConfQuit(GxView V, int multiFile) {
		if (Modified!=0) {
			if (multiFile!=0) {
				switch (V.Choice(GPC_ERROR,
						"File Modified",
						5,
						"&Save",
						"&As",
						"A&ll",
						"&Discard",
						"&Cancel",
						"%s", FileName))
				{
				case 0: /* Save */
					if (!Save()) return 0;
					break;
				case 1: /* As */
				{
					String [] FName = {FileName};
					//strcpy(FName, FileName);
					if (V.GetFile("Save As", FName, HIST_PATH, GF_SAVEAS) == 0) return 0;
					if (FileSaveAs(FName[0])) return 0;
				}
				break;
				case 2: /* Save all */
					return -2;
				case 3: /* Discard */
					break;
				case 4: /* Cancel */
				case -1:
				default:
					return 0;
				}
			}else {
				switch (V.Choice(GPC_ERROR,
						"File Modified",
						4,
						"&Save",
						"&As",
						"&Discard",
						"&Cancel",
						"%s", FileName))
				{
				case 0: /* Save */
					if (!Save()) return 0;
					break;
				case 1: /* As */
				{
					String [] FName = {FileName};
					//char FName[MAXPATH];
					//strcpy(FName, FileName);
					if (V.GetFile("Save As", FName, HIST_PATH, GF_SAVEAS) == 0) return 0;
					if (!FileSaveAs(FName[0])) return 0;
				}
				break;
				case 2: /* Discard */
					break;
				case 3: /* Cancel */
				case -1:
				default:
					return 0;
				}
			}
		}
		return 1;
	}


	@Override
	String GetName() { return FileName; }

	@Override
	String GetPath() { 
		//String [] APath = {null};
		//Console.JustDirectory(FileName, APath);
		//return APath[0]; 

		return Console.directory(FileName);
	}

	@Override
	String GetInfo() { 
		String []  buf = {null};
		//char winTitle[256] = {0};
		String winTitle = "";

		Console.JustFileName(FileName, buf);
		if (buf[0] == null || buf[0].isBlank()) // if there is no filename, try the directory name.
			Console.JustLastDirectory(FileName, buf);

		if (buf[0] == null || buf[0].isBlank()) // if there is a file/dir name, stick it in here.
			winTitle += buf + " - ";

		winTitle += FileName;

		return String.format(
				"%2d %04d:%03d%c%-150s ",
				getModelNo(),
				1 + CP.Row, 1 + CP.Col,
				Modified!=0 ? '*': ' ',
						winTitle);

	}


	@Override
	void GetTitle(String [] ATitle, String [] ASTitle) 
	{ 
		String []  buf = {null};

		ATitle[0] = FileName;

		Console.JustFileName(FileName, buf);
		if (buf[0] == null || buf[0].isBlank())    	
			ASTitle[0] = FileName;
		else 
			ASTitle[0] = buf[0];
	}


	@Override
	void DeleteRelated() {
		if (Routines!=null) {
			EView.ActiveView.DeleteModel(Routines);
			Routines = null;
		}
	}


	@Override
	int GetStrVar(int var, String [] str) 
	{
		//puts("variable EBuffer\x7");
		switch (var) {
		case mvFilePath:
			//puts("variable FilePath\x7");
			str[0] = FileName;
			return 1;

		case mvFileName:
			Console.JustFileName(FileName, str);
			return 1;

		case mvFileDirectory:
			//Console.JustDirectory(FileName, str);
			str[0] = Console.directory(FileName);
			return 1;

		case mvFileBaseName:
		{
			String [] buf = {null};
			//String dot, *dot2;

			Console.JustFileName(FileName, buf);

			//File f = new File(buf[0]);
			//Path.of(buf[0]).

			int pointPos = buf[0].lastIndexOf('.');
			if(pointPos < 0)
				str[0] = buf[0];
			else
				str[0] = buf[0].substring(0, pointPos);
			/*
	            dot = strchr(buf, '.');
	            while ((dot2 = strchr(dot + 1, '.')) != null)
	                dot = dot2;
	            if (dot)
			 *dot = 0;
	            strcpy(str, buf);
			 */
		}
		return 1;

		case mvFileExtension:
		{
			String [] buf = {null};

			Console.JustFileName(FileName, buf);

			int pointPos = buf[0].lastIndexOf('.');
			if(pointPos < 0)
				str[0] = buf[0];
			else
				str[0] = buf[0].substring(pointPos+1);

			/*
	            dot = strchr(buf, '.');
	            while ((dot2 = strchr(dot + 1, '.')) != null)
	                dot = dot2;
	            if (dot)
	                strcpy(str, dot);
	            else
	                str[0] = 0;
			 */
		}
		return 1;

		case mvChar:
		case mvWord:
		case mvLine:
			return 0;
		}

		return super.GetStrVar(var, str);
	}

	@Override
	int GetIntVar(int var, int []value) {
		switch (var) {
		case mvCurRow: value[0] = VToR(CP.Row) + 1; return 1;
		case mvCurCol: value[0] = CP.Col; return 1;
		}
		return super.GetIntVar(var, value);
	}






























	boolean CanUndo() {
		if (!BFI(this, BFI_Undo)) return false;
		return us.CanUndo();
	}

	boolean CanRedo() {
		if (!BFI(this, BFI_Undo)) return false;
		return us.CanRedo();
	}





	void PushBlockData() {
		getSlot().
		pushInt(BB.Col).
		pushInt(BB.Row).
		pushInt(BE.Col).
		pushInt(BE.Row).
		pushInt(BlockMode).
		pushOp( UndoOperation.ucBlock);
	}




	UndoStackItem getSlot() 
	{

		UndoStackItem ret = us.getRecordSlot();

		// turned off? caller will write to not saved item
		if( (!BFI(this, BFI_Undo)))
			ret.setDisabled(); 

		return ret;
	}





	boolean Redo() 
	{
		if (!BFI(this, BFI_Undo)) return false;

		if (!us.CanRedo()) {
			Msg(S_INFO, "Nothing to redo.");
			return false;
		}

		return us.redo();
	}

	boolean Undo() {	    
		if (!BFI(this, BFI_Undo)) return false;

		if(!us.CanUndo()) {
			Msg(S_INFO, "Nothing to undo.");
			return false;
		}

		return us.undo();
	}

















	boolean ShowPosition() {
		int CLine, NLines;
		int CAct, NAct;
		int CColumn, NColumns;
		int CCharPos, NChars;

		if (View == null)
			return false;

		CLine = CP.Row + 1;
		NLines = VCount;
		CAct = VToR(CP.Row) + 1;
		NAct = RCount;
		CColumn = CP.Col + 1;
		NColumns = LineLen(CP.Row);
		CCharPos = CharOffset(VLine(CP.Row), CP.Col) + 1;
		NChars = VLine(CP.Row).getCount();


		/*#ifdef CONFIG_UNDOREDO
	    int NN = -1;
	    if (US.UndoPtr > 0)
	        NN = US.Top[US.UndoPtr - 1];
	#endif */
		Msg(S_INFO,
				"L%d/%d G%d/%d/%d A%d/%d C%d/%d P%d/%d "+
						/*#ifdef CONFIG_UNDOREDO
	        "U%d/%d/%d "
	#endif */
	"H%d/%d/%d",
	CLine, NLines,
	RGap, RCount, RAllocated,
	CAct, NAct,
	CColumn, NColumns,
	CCharPos, NChars,
	/*#ifdef CONFIG_UNDOREDO
	        US.UndoPtr, US.Num, NN,
	#endif */
	StartHilit, MinRedraw, MaxRedraw);
		return true;
	}





	boolean SearchWord(int SearchFlags) 
	{
		//char word[MAXSEARCH + 1];
		StringBuilder word = new StringBuilder();

		ELine L = VLine(CP.Row);
		int P, len = 0;
		int Case = BFI(this, BFI_MatchCase) ? 0 : SEARCH_NCASE;

		P = CharOffset(L, CP.Col);

		while ((P > 0) && ((ChClass(L.Chars.charAt(P - 1)) == 1) || (L.Chars.charAt(P - 1) == '_')))
			P--;

		while ( /*len < int(sizeof(word)) &&*/ P < L.getCount() && (ChClass(L.Chars.charAt(P)) == 1 || L.Chars.charAt(P) == '_'))
			word.append( L.Chars.charAt(P++) );

		if (len == 0)
			return false;

		return FindStr(word.toString(), len, Case | SearchFlags | SEARCH_WORD);
	}




	boolean HilitMatchBracket() {
		EPoint M = CP;

		if (View == null)
			return false;

		int Min = VToR(GetVPort().TP.Row);
		int Max = GetVPort().TP.Row + GetVPort().Rows;
		if (Max >= VCount)
			Max = RCount;
		else
			Max = VToR(Max);
		if (Min < 0)
			Min = 0;
		if (Max < Min)
			return false;

		if (GetMatchBrace(M, Min, Max, false)) {
			Match = M;
			MatchLen = 1;
			MatchCount = 1;
			Draw(Match.Row, Match.Row);
			return true;
		}
		return false;
	}




	boolean GetMatchBrace(EPoint M, int MinLine, int MaxLine, boolean show) {
		int [] StateLen = {0};
		//hsState *StateMap = 0;
		int [][] StateMap = new int[1][];
		int Pos;
		ELine L = VLine(M.Row);
		int dir = 0;
		//hsState State;
		char Ch1, Ch2;
		int CountX = 0;
		int StateRow = -1;

		M.Row = VToR(CP.Row);

		Pos = CharOffset(L, M.Col);
		if (Pos >= L.getCount()) return false;
		switch(L.Chars.charAt(Pos)) {
		case '{': dir = +1; Ch1 = '{'; Ch2 = '}'; break;
		case '[': dir = +1; Ch1 = '['; Ch2 = ']'; break;
		case '<': dir = +1; Ch1 = '<'; Ch2 = '>'; break;
		case '(': dir = +1; Ch1 = '('; Ch2 = ')'; break;
		case '}': dir = -1; Ch1 = '}'; Ch2 = '{'; break;
		case ']': dir = -1; Ch1 = ']'; Ch2 = '['; break;
		case '>': dir = -1; Ch1 = '>'; Ch2 = '<'; break;
		case ')': dir = -1; Ch1 = ')'; Ch2 = '('; break;
		default:
			return false;
		}
		//StateMap = 0;
		if (!GetMap(M.Row, StateLen, StateMap)) return false;
		int State = StateMap[0][Pos];
		StateRow = M.Row;

		while (M.Row >= MinLine && M.Row < MaxLine) {
			while (Pos >= 0 && Pos < L.getCount()) {
				if (L.Chars.charAt(Pos) == Ch1 || L.Chars.charAt(Pos) == Ch2) {
					// update syntax state if needed
					if (StateRow != M.Row) {
						StateMap[0] = null;
						GetMap(M.Row, StateLen, StateMap);
						if (StateMap[0] == null) return false;
						StateRow = M.Row;
					}
					if (StateMap[0][Pos] == State) {
						if (L.Chars.charAt(Pos) == Ch1) CountX++;
						if (L.Chars.charAt(Pos) == Ch2) CountX--;
						if (CountX == 0) {
							M.Col = ScreenPos(L, Pos);
							return true;
						}
					}
				}
				Pos += dir;
			}
			M.Row += dir;
			if (M.Row >= 0 && M.Row < RCount) {
				L = RLine(M.Row);
				Pos = (dir == 1) ? 0 : (L.getCount() - 1);
			}
		}

		if (show)
			Msg(S_INFO, "No match (%d missing).", CountX);

		return false;
	}

	boolean MatchBracket() {
		EPoint M = CP;

		if (GetMatchBrace(M, 0, RCount, true))
			return SetPosR(M.Col, M.Row);
		return false;
	}



























	boolean BlockTrans(TransTable tab) 
	{
		int L, I, B, E;
		ELine LL;

		if(tab == null) return false;

		if (!CheckBlock()) return false;
		if (RCount == 0) return false;

		for (L = BB.Row; L <= BE.Row; L++) {
			LL = RLine(L);
			B = 0;
			E = 0;
			switch (BlockMode) {
			case bmLine:
				if (L == BE.Row)
					E = 0;
				else
					E = LL.getCount();
				break;
			case bmColumn:
				if (L == BE.Row)
					E = 0;
				else {
					B = CharOffset(LL, BB.Col);
					E = CharOffset(LL, BE.Col);
				}
				break;
			case bmStream:
				if (L == BB.Row && L == BE.Row) {
					B = CharOffset(LL, BB.Col);
					E = CharOffset(LL, BE.Col);
				} else if (L == BB.Row) {
					B = CharOffset(LL, BB.Col);
					E = LL.getCount();
				} else if (L == BE.Row) {
					B = 0;
					E = CharOffset(LL, BE.Col);
				} else {
					B = 0;
					E = LL.getCount();
				}
				break;
			}
			if (B > LL.getCount())
				B = LL.getCount();
			if (E > LL.getCount())
				E = LL.getCount();
			if (E > B) {
				if (!ChgChars(L, B, E - B, null)) return false;
				for (I = B; I < E; I++)
					LL.Chars.w(I, tab.t[0xFF & LL.Chars.r(I)] );
			}
		}
		Draw(BB.Row, BE.Row);
		return true;
	}

	boolean CharTrans(TransTable tab) 
	{
		if(tab == null) return false;

		ELine L = VLine(CP.Row);
		int P = CharOffset(L, CP.Col);

		if (P >= L.getCount()) return false;
		if (!ChgChars(CP.Row, P, 1, null)) return false;
		//L.Chars[P] = tab[(char)L.Chars[P]];
		L.Chars.w(P, tab.t[0xFF & L.Chars.r(P)] );

		return true;
	}

	boolean LineTrans(TransTable tab) 
	{
		if(tab == null) return false;

		ELine L = VLine(CP.Row);
		int I;

		if (L.getCount() > 0) {
			if (!ChgChars(CP.Row, 0, L.getCount(), null)) return false;
			for (I = 0; I < L.getCount(); I++)
			{
				//L.Chars[I] = tab[(unsigned char)L.Chars[I]];
				L.Chars.w(I, tab.t[0xFF & L.Chars.r(I)] );
			}
		}
		return true;
	}

	boolean CharCaseUp() {
		return CharTrans(TransTable.MakeTrans(ccUp));
	}

	boolean CharCaseDown() {
		return CharTrans(TransTable.MakeTrans(ccDown));
	}

	boolean CharCaseToggle() {
		return CharTrans(TransTable.MakeTrans(ccToggle));
	}

	boolean LineCaseUp() {
		return LineTrans(TransTable.MakeTrans(ccUp));
	}

	boolean LineCaseDown() {
		return LineTrans(TransTable.MakeTrans(ccDown));
	}

	boolean LineCaseToggle()  {
		return LineTrans(TransTable.MakeTrans(ccToggle));
	}

	boolean BlockCaseUp() {
		return BlockTrans(TransTable.MakeTrans(ccUp));
	}

	boolean BlockCaseDown() {
		return BlockTrans(TransTable.MakeTrans(ccDown));
	}

	boolean BlockCaseToggle() {
		return BlockTrans(TransTable.MakeTrans(ccToggle));
	}


	boolean CharTrans(ExState State) throws ExecException {
		return CharTrans(TransTable.GetTrans(State,View));
	}

	boolean LineTrans(ExState State) throws ExecException {
		return LineTrans(TransTable.GetTrans(State,View));
	}

	boolean BlockTrans(ExState State) throws ExecException {
		return BlockTrans(TransTable.GetTrans(State,View));
	}
























	boolean Find(ExState State) {
		//char find[MAXSEARCH+1] = "";
		//char options[32] = "";
		String [] find = {""};
		String [] options = {""};
		int [] popt = {0};


		if (State.GetStrParam(View, find) != 0) {
			if (State.GetStrParam(View, options) == 0)
				options[0] = BFS(this, BFS_DefFindOpt);

			LSearch.ok = 0;
			LSearch.strSearch = find[0];
			LSearch.strReplace = null;
			LSearch.Options = 0;
			popt[0] = LSearch.Options;
			if (!ParseSearchOptions(false, options[0], popt)) return false;
			LSearch.Options = popt[0];
			LSearch.ok = 1;
		} /*else if ((Config.HaveGUIDialogs & GUIDLG_FIND) && GUIDialogs) {
	        LSearch.ok = 0;
	        LSearch.strSearch = "";
	        LSearch.strReplace = "";
	        LSearch.Options = 0;
	        if (BFS(this, BFS_DefFindOpt)!=null)
	            options[0] = BFS(this, BFS_DefFindOpt);
	        if (ParseSearchOptions(0, options[0], LSearch.Options) == 0)
	            LSearch.Options = 0;

	        if (DLGGetFind(View.MView.Win, LSearch) == 0)
	            return false;
	    } */ else {
	    	if (BFS(this, BFS_DefFindOpt)!=null)
	    		options[0] = BFS(this, BFS_DefFindOpt);
	    	if (View.MView.Win.GetStr("Find", find, HIST_SEARCH) == 0) return false;
	    	if (View.MView.Win.GetStr("Options (All/Block/Cur/Delln/Glob/Igncase/Joinln/Rev/Word/regX)", options, HIST_SEARCHOPT) == 0) return false;

	    	LSearch.ok = 0;
	    	LSearch.strSearch = find[0];
	    	LSearch.strReplace = "";
	    	LSearch.Options = 0;
	    	popt[0] = LSearch.Options;
	    	if (!ParseSearchOptions(false, options[0], popt)) return false;
	    	LSearch.Options = popt[0];
	    	LSearch.ok = 1;
	    }
		if (LSearch.ok == 0) return false;
		LSearch.Options |= SEARCH_CENTER;

		return LSearch.Find(this);
	}

	boolean FindReplace(ExState State) {
		//char find[MAXSEARCH+1] = "";
		//char replace[MAXSEARCH+1] = "";
		//char options[32] = "";
		String [] find = {""};
		String [] replace = {""};
		String [] options = {""};
		int [] popt = {0};

		if (State.GetStrParam(View, find) != 0) {
			if (State.GetStrParam(View, replace) == 0)
				return false;
			if (State.GetStrParam(View, options) == 0)
				return false;

			LSearch.ok = 0;
			LSearch.strSearch =  find[0];
			LSearch.strReplace =  replace[0];
			LSearch.Options = 0;
			popt[0] = LSearch.Options;
			if(!ParseSearchOptions(true, options[0], popt)) return false;
			LSearch.Options = popt[0];
			LSearch.Options |= SEARCH_REPLACE;
			LSearch.ok = 1;
		} /*else if ((HaveGUIDialogs & GUIDLG_FINDREPLACE) && GUIDialogs) {
	        LSearch.ok = 0;
	        LSearch.strSearch = "";
	        LSearch.strReplace = "";
	        LSearch.Options = 0;
	        if (BFS(this, BFS_DefFindReplaceOpt)!=null)
	            options[0] = BFS(this, BFS_DefFindReplaceOpt);
	        if (ParseSearchOptions(1, options[0], LSearch.Options) == 0)
	            LSearch.Options = 0;
	        if (DLGGetFindReplace(View.MView.Win, LSearch) == 0)
	            return false;
	    } */ else {
	    	if (BFS(this, BFS_DefFindReplaceOpt)!=null)
	    		options[0] = BFS(this, BFS_DefFindReplaceOpt);
	    	if (State.GetStrParam(View, find) == 0)
	    		if (View.MView.Win.GetStr("Find", find, HIST_SEARCH) == 0) return false;
	    	if (State.GetStrParam(View, replace) == 0)
	    		if (View.MView.Win.GetStr("Replace", replace, HIST_SEARCH) == 0) return false;
	    	if (State.GetStrParam(View, options) == 0)
	    		if (View.MView.Win.GetStr("Options (All/Block/Cur/Delln/Glob/Igncase/Joinln/Rev/Noask/Word/regX)", options, HIST_SEARCHOPT) == 0) return false;

	    	LSearch.ok = 0;
	    	LSearch.strSearch = find[0];
	    	LSearch.strReplace =replace[0];
	    	LSearch.Options = 0;
	    	popt[0] = LSearch.Options;
	    	if (!ParseSearchOptions(true, options[0], popt)) return false;
	    	LSearch.Options = popt[0];
	    	LSearch.Options |= SEARCH_REPLACE;
	    	LSearch.ok = 1;
	    }
		if (LSearch.ok == 0) return false;
		LSearch.Options |= SEARCH_CENTER;

		//return Find(LSearch);	    
		return LSearch.Find(this);
	}

	boolean FindRepeat(ExState State) {
		if (LSearch.ok == 0) return Find(State);
		LSearch.Options |= SEARCH_NEXT;
		LSearch.Options &= ~SEARCH_GLOBAL;
		//return Find(LSearch);
		return LSearch.Find(this);
	}

	boolean FindRepeatReverse(ExState State) {
		if (LSearch.ok == 0) return Find(State);
		LSearch.Options |= SEARCH_NEXT;
		LSearch.Options &= ~SEARCH_GLOBAL;
		LSearch.Options ^= SEARCH_BACK;
		boolean rc = LSearch.Find(this);
		LSearch.Options ^= SEARCH_BACK;
		return rc;
	}

	boolean FindRepeatOnce(ExState State) {
		if (LSearch.ok == 0) return Find(State);
		LSearch.Options |= SEARCH_NEXT;
		LSearch.Options &= ~SEARCH_GLOBAL;
		LSearch.Options &= ~SEARCH_ALL;
		return LSearch.Find(this);
	}



	boolean ChangeKeys(ExState State) {
		//char Mode[32] = "";
		String [] Mode = {""};

		if(0==State.GetStrParam(View, Mode))
			if (0==View.MView.Win.GetStr("Mode", Mode, HIST_SETUP)) return false;

		boolean rc = ChangeKeys(Mode[0]);
		FullRedraw();
		return rc;
	}



	boolean ChangeTabSize(ExState State) {
		int [] No = {0};

		if (State.GetIntParam(View, No) == 0) {
			//char Num[10];

			String [] Num = { String.format("%d", BFI(this, BFI_TabSize)) };
			if (View.MView.Win.GetStr("TabSize", Num, HIST_SETUP) == 0) return false;
			No[0] = BitOps.atoi(Num[0]);
		}
		if (No[0] < 1) return false;
		if (No[0] > 32) return false;
		BFI_SET(this, BFI_TabSize, No[0]);
		FullRedraw();
		return true;
	}

	boolean SetIndentWithTabs(ExState State) {
		int [] No = {0};

		if (State.GetIntParam(View, No) == 0) return false;
		Flags.num[BFI_IndentWithTabs] = No[0] != 0 ? 1 : 0;
		return true;
	}

	boolean ChangeRightMargin(ExState State) {
		//char Num[10];
		int [] No = {0};

		if (State.GetIntParam(View, No) == 0) {
			String [] Num = { String.format("%d", iBFI(this, BFI_RightMargin) + 1) };
			if (View.MView.Win.GetStr("RightMargin", Num, HIST_SETUP) == 0) return false;
			No[0] = BitOps.atoi(Num[0]) - 1;
		}
		if (No[0] <= 1) return false;
		BFI_SET(this, BFI_RightMargin, No[0]);
		Msg(S_INFO, "RightMargin set to %d.", No[0] + 1);
		return true;
	}

	boolean ChangeLeftMargin(ExState State) {
		//char Num[10];
		int [] No = {0};

		if (State.GetIntParam(View, No) == 0) {
			String [] Num = { String.format("%d", iBFI(this, BFI_LeftMargin) + 1) };
			if (View.MView.Win.GetStr("LeftMargin", Num, HIST_SETUP) == 0) return false;
			No[0] = BitOps.atoi(Num[0]) - 1;
		}
		if (No[0] < 0) return false;
		BFI_SET(this, BFI_LeftMargin, No[0]);
		Msg(S_INFO, "LeftMargin set to %d.", No[0] + 1);
		return true;
	}






















	boolean ParseSearchOption(boolean replace, char c, int [] opt) {
		switch (c) {
		case 'a': opt[0] |= SEARCH_ALL; break;      // search all occurances
		case 'b': opt[0] |= SEARCH_BLOCK; break;    // search in block only
		case 'c': opt[0] &= ~SEARCH_NEXT; break;    // search from current position
		case 'd': opt[0] |= SEARCH_DELETE; break;   // delete found line
		case 'g': opt[0] |= SEARCH_GLOBAL; break;   // search globally
		case 'i': opt[0] |= SEARCH_NCASE; break;    // don't match case
		case 'j': opt[0] |= SEARCH_JOIN; break;     // join found line
		case 'r': opt[0] |= SEARCH_BACK; break;     // search reverse
		case 'w': opt[0] |= SEARCH_WORDBEG | SEARCH_WORDEND; break;
		case '<': opt[0] |= SEARCH_WORDBEG; break;
		case '>': opt[0] |= SEARCH_WORDEND; break;
		case 'x': opt[0] |= SEARCH_RE; break;       // search using regexps
		default:
			if (!replace) return false;
			switch (c) {
			case 'n': opt[0] |= SEARCH_NASK; break; // don't ask before replacing
			default:
				return false;
			}
		}
		return true;
	}


	static String unquoteString(String str) 
	{
		StringBuilder sb = new StringBuilder();	
		int s = 0;

		while (s < str.length()) {
			if (str.charAt(s) == '\\') {
				s++;
				if(s >= str.length()) return null;
			}
			sb.append(str.charAt(s++));
		}

		return sb.toString();
	}

	boolean ParseSearchOptions(boolean replace, String str, int [] Options) {
		//String p = str;
		str.chars().forEach(c -> ParseSearchOption(replace, (char)c, Options));
		/*
	    Options = SEARCH_NEXT;
	    while (*p) {
	        if (ParseSearchOption(replace, *p, Options) == 0) return 0;
	        p++;
	    } */
		return true;
	}


	boolean ParseSearchReplace(EBuffer B, String str, boolean replace, SearchReplaceOptions opt) {
		int where = 0;
		int len = 0;
		//String p = str;
		int [] popt = {0};

		//memset(opt, 0, sizeof(opt));
		opt.clear();

		if (str == null) return false;

		if (replace) {
			popt[0] = opt.Options;
			if (!ParseSearchOptions(replace, BFS(B, BFS_DefFindReplaceOpt), popt )) return false;
			opt.Options = popt[0];
			opt.Options |= SEARCH_REPLACE;
		} else {
			popt[0] = opt.Options;
			if (!ParseSearchOptions(replace, BFS(B, BFS_DefFindOpt), popt)) return false;
			opt.Options = popt[0];
		}

		int p = 0;
		while (p < str.length()) {
			switch(str.charAt(p)) {
			case '\\':
				if (where == 0) {
					//opt.strSearch[len++] = str.charAt(p++);
					opt.strSearch += str.charAt(p++);
					//if (*p == 0) return 0;
					if( p >= str.length() ) return false;
					//opt.strSearch[len++] = str.charAt(p++);
					opt.strSearch += str.charAt(p++);
				} else if (where == 1) {
					//opt.strReplace[len++] = str.charAt(p++);
					opt.strReplace += str.charAt(p++);
					//if (*p == 0) return 0;
					if( p >= str.length() ) return false;
					//opt.strReplace[len++] = str.charAt(p++);
					opt.strReplace += str.charAt(p++);
				} else
					return false;
				break;
			case '/':
				where++;
				if (!replace && where == 1) where++;
				if (where == 2)
					opt.Options = SEARCH_NEXT;
				if (where > 2) return false;
				len = 0;
				p++;
				break;
			default:
				if (where == 0)
					//opt.strSearch[len++] = str.charAt(p++);
					opt.strSearch += str.charAt(p++);
				else if (where == 1)
					//opt.strReplace[len++] = str.charAt(p++);
					opt.strReplace += str.charAt(p++);
				else {
					char c = str.charAt(p);

					p++;
					popt[0] = opt.Options;
					if (!ParseSearchOption(replace, c, popt)) return false;
					opt.Options = popt[0];
				}
			}
		}
		if(0 != (opt.Options & SEARCH_RE))
			;
		else {
			opt.strSearch = unquoteString(opt.strSearch); 
			if (opt.strSearch == null) return false;
			if(0 != (opt.Options & SEARCH_REPLACE))
			{
				opt.strReplace = unquoteString(opt.strReplace);
				if (opt.strReplace == null) return false;
			}
		}

		opt.ok = 1;
		return true;
	}


	boolean Search(ExState State)          { return Search(State, null, 0, 1); }
	boolean SearchB(ExState State)         { return Search(State, null, SEARCH_BACK, 1); }
	boolean SearchRx(ExState State)        { return Search(State, null, SEARCH_RE, 1); }
	boolean SearchAgain(ExState State)     { return SearchAgain(State, 0); }
	boolean SearchAgainB(ExState State)    { return SearchAgain(State, SEARCH_BACK); }
	boolean SearchReplace(ExState State)   { return SearchReplace(State, null, null, 0); }
	boolean SearchReplaceB(ExState State)  { return SearchReplace(State, null, null, SEARCH_BACK); }
	boolean SearchReplaceRx(ExState State) { return SearchReplace(State, null, null, SEARCH_RE); }



	boolean Search(ExState State, String aString, int Options, int CanResume) {
		//char find[MAXSEARCH+1] = "";
		int Case = BFI(this, BFI_MatchCase) ? 0 : SEARCH_NCASE;
		int Next = 0;
		int erc = 0;
		//int Changed;
		String [] find = {""};

		if (aString != null)
			find[0] = aString;
		else
			if (State.GetStrParam(View, find) == 0)
				if ((erc = View.MView.Win.GetStr("Find", find, HIST_SEARCH)) == 0) return false;

		if (find[0].isBlank()) return false;

		if (erc == 2)
			Case ^= SEARCH_NCASE;

		//if (Changed == 0 && CanResume)
		//    Next |= SEARCH_NEXT;

		LSearch.ok = 0;
		LSearch.strSearch = find[0];
		LSearch.Options = Case | Next | (Options & ~SEARCH_NCASE);
		LSearch.ok = 1;

		return LSearch.Find(this);
	}


	boolean SearchAgain(ExState State, int Options) {
		if (LSearch.ok == 0) return false;
		LSearch.Options |= SEARCH_NEXT;
		if ((Options & SEARCH_BACK) != (LSearch.Options & SEARCH_BACK))
			LSearch.Options ^= SEARCH_BACK;

		return LSearch.Find(this);
	}



	boolean SearchReplace(ExState State, String aString, String aReplaceString, int Options) {
		String [] find = {""};
		String [] replace = {""};
		int Case = BFI(this, BFI_MatchCase) ? 0 : SEARCH_NCASE;

		if (aString != null)
			find[0] = aString;
		else
			if (State.GetStrParam(View, find) == 0)
				if (View.MView.Win.GetStr("Find", find, HIST_SEARCH) == 0) return false;

		if(find[0].isBlank()) return false;

		if (aReplaceString!=null)
			replace[0] = aReplaceString;
		else
			if (State.GetStrParam(View, replace) == 0)
				if (View.MView.Win.GetStr("Replace", replace, HIST_SEARCH) == 0) return false;

		LSearch.ok = 0;
		LSearch.strSearch = find[0];
		LSearch.strReplace= replace[0];
		LSearch.Options = Case | (Options & ~SEARCH_NCASE) | SEARCH_ALL | SEARCH_REPLACE;
		LSearch.ok = 1;

		return LSearch.Find(this);
	}

















	boolean SelfInsert(ExState State) {
		if (EGUI.LastEventChar != -1)
			return TypeChar((char)EGUI.LastEventChar);
		return false;
	}



	boolean FileReload(ExState State) {
		if (Modified!=0) {
			switch (View.MView.Win.Choice(GPC_ERROR, "File Modified",
					2,
					"&Reload",
					"&Cancel",
					"%s", FileName))
			{
			case 0:
				break;
			case 1:
			case -1:
			default:
				return false;
			}
		}
		//	    GetNewNumber();
		return Reload();
	}




	boolean FileWriteTo(String FName) {
		String [] Name = {""};

		if (Console.ExpandPath(FName, Name) == -1) {
			View.MView.Win.Choice(GPC_ERROR, "Error", 1, "O&K", "Invalid path: %s.", FName);
			return false;
		}
		if (null == Console.FindFile(Name[0])) {
			if (Console.FileExists(Name[0])) {
				switch (View.MView.Win.Choice(GPC_ERROR, "File Exists",
						2,
						"&Overwrite",
						"&Cancel",
						"%s", Name))
				{
				case 0:
					break;
				case 1:
				case -1:
				default:
					return false;
				}
			}
			return SaveTo(Name[0]);
		} else {
			View.MView.Win.Choice(GPC_ERROR, "Error", 1, "O&K", "Already editing '%s.'", Name);
			return false;
		}
	}

	boolean FileWriteTo(ExState State) {
		String [] FName = {FileName};

		if (State.GetStrParam(View, FName) == 0)
			if (View.MView.Win.GetFile("Write To", FName, HIST_PATH, GF_SAVEAS) == 0) return false;
		return FileWriteTo(FName[0]);
	}

	boolean BlockReadX(ExState State, int blockMode) {
		//char Name[MAXPATH];
		//char FName[MAXPATH];
		String [] FName = {""};
		String [] Name = {""};

		//if (Console.JustDirectory(FileName, FName) == -1) return false;	    
		FName[0] = Console.parent(FileName);
		if ( FName[0] == null) return false;

		FName[0] = Console.SlashDir(FName[0]);
		if (State.GetStrParam(View, FName) == 0)
			if (View.MView.Win.GetFile("Read block", FName, HIST_PATH, GF_OPEN) == 0) return false;

		if (Console.ExpandPath(FName[0], Name) == -1) {
			View.MView.Win.Choice(GPC_ERROR, "Error", 1, "O&K", "Invalid path: %s.", FName[0]);
			return false;
		}
		return BlockReadFrom(FName[0], blockMode);
	}

	boolean BlockRead(ExState State) {
		return BlockReadX(State, BlockMode);
	}

	boolean BlockReadStream(ExState State) {
		return BlockReadX(State, bmStream);
	}

	boolean BlockReadLine(ExState State) {
		return BlockReadX(State, bmLine);
	}

	boolean BlockReadColumn(ExState State) {
		return BlockReadX(State, bmColumn);
	}

	boolean BlockWrite(ExState State) {
		//char Name[MAXPATH];
		//char FName[MAXPATH];
		boolean Append = false;
		String [] FName = {""};
		String [] Name = {""};

		FName[0] = Console.parent(FileName);
		if ( FName[0] == null) return false;

		FName[0] = Console.SlashDir(FName[0]);
		if (State.GetStrParam(View, FName) == 0)
			if (View.MView.Win.GetFile("Write block", FName, HIST_PATH, GF_SAVEAS) == 0)
				return false;

		if (Console.ExpandPath(FName[0], Name) == -1) {
			View.MView.Win.Choice(GPC_ERROR, "Error", 1, "O&K", "Invalid path: %s.", FName);
			return false;
		}
		if (null == Console.FindFile(Name[0])) {
			if (Console.FileExists(Name[0])) {
				switch (View.MView.Win.Choice(GPC_ERROR, "File Exists",
						3,
						"&Overwrite",
						"&Append",
						"&Cancel",
						"%s", Name))
				{
				case 0:
					break;
				case 1:
					Append = true;
					break;
				case 2:
				case -1:
				default:
					return false;

				}
			}
		} else {
			View.MView.Win.Choice(GPC_ERROR, "Error", 1, "O&K", "Already editing '%s.'", Name);
			return false;
		}
		return BlockWriteTo(Name[0], Append);
	}














	boolean MoveToLine(ExState State) {
		int [] No = {0};

		if (State.GetIntParam(View, No) == 0) {
			String []  Num = {""};

			Num[0] = String.format( "%d", VToR(CP.Row) + 1);
			if (View.MView.Win.GetStr("Goto Line", Num, HIST_POSITION) == 0)
				return false;
			No[0] = BitOps.atoi(Num[0]);
		}
		return SetNearPosR(CP.Col, No[0] - 1);
	}

	boolean MoveToColumn(ExState State) {
		int [] No = {0};

		if (State.GetIntParam(View, No) == 0) {
			String []  Num = {""};

			Num[0] = String.format( "%d", CP.Col + 1);
			if (View.MView.Win.GetStr("Goto Column", Num, HIST_POSITION) == 0) return false;
			No[0] = BitOps.atoi(Num[0]);
		}
		return SetNearPos(No[0] - 1, CP.Row);
	}


	boolean CompleteWord() {
		return View.MView.Win.ICompleteWord(View) != 0; // TODO ret code?
	}










	// these two will probably be replaced in the future
	boolean InsertDate(ExState State) {
		/*
	    //char strArg[128] = "";
	    //char buf[128], *p;

	    time_t t;

	    //time(&t);

	    if (State.GetStrParam(View, strArg, sizeof(strArg))) {
	        //struct tm *tt = localtime(&t);
	        //strftime(buf, sizeof(buf), strArg, tt);
	        //buf[sizeof(buf) - 1] = 0;
	    } else {
	        //** 012345678901234567890123
	        //** Wed Jan 02 02:23:54 1991
	        //p = ctime(&t);
	        sprintf(buf, "%.10s %.4s", p, p + 20);
	    }
	    //puts(buf);

	    return InsertString(buf, strlen(buf));
		 */
		return InsertString( new Date().toString() );
	}


	boolean InsertUid() 
	{
		String p = System.getenv("USER");
		if (p == null) p = System.getenv("NAME");
		if (p == null) p = System.getenv("ID");
		// mostly for Windows.  Why they can't just be standard, I don't know...
		if (p == null) p = System.getenv("USERNAME");
		if (p == null) {
			Msg(S_INFO, "User ID not set ($USER).");
			//return 0;
			p = (String )"UNKNOWN USER";
		}
		return InsertString(p);
	}

	boolean ShowHelpWord(ExState State) 
	{
		//** Code for BlockSelectWord to find the word under the cursor,
		String achr = "+-_."; // these are accepted characters
		//char    buf[128];
		int     Y = VToR(CP.Row);
		ELine  L = RLine(Y);
		int     P;

		StringBuilder buf = new StringBuilder();

		P = CharOffset(L, CP.Col);

		// fix \b for the case of CATBS
		for(int i = 0; i < P; i++) {
			//printf("%d - %d  %d   %c %c\n", i, P, L.Chars.charAt(i),
			//L.Chars.charAt(i), L.Chars.charAt(P));
			if ((L.Chars.charAt(i) == '\b') && (P < (L.getCount() - 2)))
				P += 2;
		}
		//int len = 0;
		if (P < L.getCount()) 
		{
			// To start of word,

			while ((P > 0)
					&& ((L.Chars.charAt(P - 1) == '\b') || CString.isalnum(L.Chars.charAt(P - 1))
							|| (achr.indexOf( L.Chars.charAt(P - 1)) >= 0)))
				P--; // '_' for underline is hidden in achr

			if ((P < (L.getCount() - 1)) && (L.Chars.charAt(P) == '\b'))
				P++;

			// To end of word,

			while( P < L.getCount()) {
				if (((P + 1) < L.getCount()) && (L.Chars.charAt(P + 1) == '\b'))
					P += 2;
				else if (CString.isalnum(L.Chars.charAt(P))
						|| (achr.indexOf( L.Chars.charAt(P)) >= 0 ) )
					buf.append(  L.Chars.charAt(P++) );
				else
					break;
			}
		}
		//printf("Word: %s\n", buf);

		if (buf.isEmpty()) {
			Msg(S_INFO, "No valid word under the cursor.");
			return false;
		}

		return Console.SysShowHelp(State, buf.isEmpty() ? null : buf.toString() ) == ExResult.ErOK;
		// */
	}















	int FindRx(Pattern rx, int Options) {
		int LLen, Start, End;
		int C, L;
		String P;
		ELine X;
		//RxMatchRes b;

		if(0==(Options & SEARCH_RE))
			return 0;

		if(0 != (Options & SEARCH_BACK)) { // not supported
			View.MView.Win.Choice(GPC_ERROR, "FindRx", 1, "O&K", "Reverse regexp search not supported.");
			return 0;
		}
		if (rx == null)
			return 0;

		if (Match.Row != -1)
			Draw(Match.Row, Match.Row);
		Match.Row = -1;
		Match.Col = -1;

		C = CP.Col;
		L = VToR(CP.Row);
		X = RLine(L);
		C = CharOffset(X, C);

		if(0 != (Options & SEARCH_NEXT)) {
			int CC = MatchCount != 0 ? MatchCount : 1;

			if(0 != (Options & SEARCH_BACK)) {
				C -= CC;
				if(0 != (Options & SEARCH_BLOCK)) {
					if (C < BB.Col && L == BB.Row)
						return 0;
					L--;
					X = RLine(L);
					C = X.getCount();
					if (BlockMode == bmColumn)
						if (BE.Col < C)
							C = BE.Col;
				} else {
					if (C < 0 && L == 0)
						return 0;
					L--;
					X = RLine(L);
					C = X.getCount();
				}
			} else {
				C += CC;
				if(0 != (Options & SEARCH_BLOCK)) {
					if (BlockMode == bmStream || BlockMode == bmLine) {
						if (C >= X.getCount()) {
							C = 0;
							L++;
							if (BlockMode == bmLine) {
								if (L == BE.Row) return 0;
							} else
								if (L == BE.Row && (C >= BE.Col || C >= X.getCount()))
									return 0;
						}
					} else if (BlockMode == bmColumn) {
						if (C >= X.getCount() || C >= BE.Col) {
							C = BB.Col;
							L++;
							if (L == BE.Row) return 0;
						}
					}
				} else {
					if (C >= X.getCount()) {
						C = 0;
						L++;
						if (L == RCount) return 0;
					}
				}
			}
		}
		MatchLen = 0;
		MatchCount = 0;

		if(0 != (Options & SEARCH_BLOCK)) {
			if(0 != (Options & SEARCH_BACK)) {
				if (BlockMode == bmStream) {
					if (L > BE.Row) {
						L = BE.Row;
						C = BE.Col;
					}
					if (L == BE.Row && C > BE.Col)
						C = BE.Col;
				} else {
					if (L >= BE.Row && BE.Row > 0) {
						L = BE.Row - 1;
						C = RLine(L).getCount();
					}
					if (BlockMode == bmColumn)
						if (L == BE.Row - 1 && C >= BE.Col)
							C = BE.Col;
				}
			} else {
				if (L < BB.Row) {
					L = BB.Row;
					C = 0;
				}
				if (L == BB.Row && C < BB.Col)
					C = BB.Col;
			}
		}

		while (true) {
			if(0 != (Options & SEARCH_BLOCK)) {
				if (BlockMode == bmStream) {
					if (L > BE.Row || L < BB.Row) break;
				} else
					if (L >= BE.Row || L < BB.Row) break;
			} else
				if (L >= RCount || L < 0) break;

			X = RLine(L);
			LLen = X.getCount();
			P = X.Chars.toString();
			Start = 0;
			End = LLen;

			if(0 != (Options & SEARCH_BLOCK)) {
				if (BlockMode == bmColumn) {
					Start = CharOffset(X, BB.Col);
					End = CharOffset(X, BE.Col);
				} else if (BlockMode == bmStream) {
					if (L == BB.Row)
						Start = CharOffset(X, BB.Col);
					if (L == BE.Row)
						End = CharOffset(X, BE.Col);
				}
				if (End > LLen)
					End = LLen;
			}
			if(0 != (Options & SEARCH_BACK)) {
				if (C >= End)
					C = End;
			} else {
				if (C < Start)
					C = Start;
			}

			if (Start <= End) 
			{
				/*
	            if (RxExec(rx, P + Start, End - Start, P + C, b, (Options & SEARCH_NCASE) != 0 ? 0 : RX_CASE) == 1) 
	            {
	                C = ScreenPos(X, b.Open[0] + Start);
	                Match.Col = C;
	                Match.Row = L;
	                MatchCount = b.Close[0] - b.Open[0];
	                MatchLen = ScreenPos(X, b.Close[0] + Start) - C;
	                for (int mm = 0; mm < NSEXPS; mm++) {
	                    b.Open[mm] += Start;
	                    b.Close[mm] += Start;
	                }
	                MatchRes = b;
	                if (0==(Options & SEARCH_NOPOS)) {
	                    if(0 != (Options & SEARCH_CENTER))
	                        CenterPosR(C, L);
	                    else
	                        SetPosR(C, L);
	                }
	                Draw(L, L);
	                return 1;
	            }
				 */

				// [dz] simplified TODO RX SEARCH_NCASE

				Matcher m = rx.matcher(P.subSequence(Start, End));
				if( m.find() )
				{
					Match.Col = C;
					Match.Row = L;



					//MatchCount = b.Close[0] - b.Open[0];
					MatchCount = m.end(0) - m.start(0);
					//MatchLen = ScreenPos(X, b.Close[0] + Start) - C;
					MatchLen = ScreenPos(X, m.end(0) + Start) - C;

					/* TODO MatchRes 
	                for (int mm = 0; mm < NSEXPS; mm++) {
	                    b.Open[mm] += Start;
	                    b.Close[mm] += Start;
	                }
	                MatchRes = b; */

					if (0==(Options & SEARCH_NOPOS)) {
						if(0 != (Options & SEARCH_CENTER))
							CenterPosR(C, L);
						else
							SetPosR(C, L);
					}
					Draw(L, L);
					return 1;
				}

			}
			C = 0;
			L++;
		}
		//SetPos(OC, OL);
		return 0;

	}













	// -----------------------------------------------------------------------
	// Folds	
	// -----------------------------------------------------------------------





	public int FindFold(int Line) { // optimize /*FOLD00*/
		int f = FindNearFold(Line);
		if (f != -1)
			if (FF[f].line == Line)
				return f;
		return -1;
	}

	int FindNearFold(int Line) { /*FOLD00*/
		int b = 0, B = FCount - 1, c;

		while (b <= B) {
			c = (b + B) / 2;
			//	        printf("%d %d %d %d %d\n", b, B, c, Line, FF[c].line);
			if (FF[c].line == Line)
				return c;
			if (c < FCount - 1) {
				if (FF[c].line <= Line && FF[c + 1].line > Line)
					return c;
			} else {
				if (FF[c].line <= Line)
					return c;
			}
			if (FF[c].line < Line)
				b = c + 1;
			else
				B = c - 1;
			if (b > B)
				break;
		}
		return -1;
	}

	boolean ShowRow(int Row) { /*FOLD00*/
		int V = RToVN(Row), GapSize;

		//	    printf("Showing row %d\n", Row);

		assert(Row >= 0 && Row < RCount); // 0 cannot be hidden

		if (V + Vis(V) == Row) return true; // already visible

		assert(VCount <= VAllocated);
		if (VCount == VAllocated) 
		{
			if (!AllocVis(VCount != 0? (VCount * 2) : 1)) return false;
			
			//memmove(VV + VAllocated - (VCount - VGap),					VV + VGap,					sizeof(int) * (VCount - VGap));

			System.arraycopy(VV, VGap, VV, VAllocated - (VCount - VGap), VCount - VGap);
		}
		if (VGap != V + 1)
			if (!MoveVGap(V + 1)) return false;
		VV[VGap] = Row - (VGap);
		VGap++;
		VCount++;

		GapSize = VAllocated - VCount;
		if (VGap != V + 2)
			if (!MoveVGap(V + 2)) return false;
		for (int i = V + 2; i < VCount; i++)
			VV[i + GapSize]--;
		//	        Vis(i, Vis(i) - 1);
		UpdateVisible(Row, 1);
		//	    if (CP.Row > Row)
		//	        if (SetPos(CP.Col, CP.Row + 1) == 0) return false;
		Draw(Row, -1);
		return true;
	}

	boolean HideRow(int Row) { /*FOLD00*/
		int V = RToV(Row), GapSize;

		assert(Row > 0 && Row < RCount); // 0 cannot be hidden

		if (V == -1) return true; // already hidden
		UpdateVisible(Row, -1);

		if (VGap != V)
			if (!MoveVGap(V)) return false;
		GapSize = VAllocated - VCount;
		VV[VGap + GapSize] = 0;
		VCount--;
		GapSize++;
		if (VAllocated - VAllocated / 2 > VCount) {
			//memmove(VV + VGap + GapSize - VAllocated / 3, VV + VGap + GapSize, sizeof(int) * (VCount - VGap));
			System.arraycopy(VV, VGap + GapSize, VV, VGap + GapSize - VAllocated / 3, VCount - VGap);
			if (!AllocVis(VAllocated - VAllocated / 3)) return false;
		}
		GapSize = VAllocated - VCount;
		if (VGap != V)
			if (!MoveVGap(V)) return false;
		for (int i = V; i < VCount; i++)
			VV[i + GapSize]++;
		//	        Vis(i, Vis(i) + 1);
		//	    if (CP.Row > Row)
		//	        if (SetPos(CP.Col, CP.Row - 1) == 0) return false;
		Draw(Row, -1);
		return true;
	}

	boolean ExposeRow(int Row) { /*FOLD00*/
		int V;
		int f, level, oldlevel = 100;

		//DumpFold();

		assert(Row >= 0 && Row < RCount); // range

		V = RToV(Row);
		if (V != -1) return true; // already exposed

		f = FindNearFold(Row);
		assert(f != -1); // if not visible, must be folded

		while (f >= 0) {
			level = FF[f].level;
			if (level < oldlevel) {
				if (!FF[f].open) {
					//	                printf("opening fold %d\n", f);
					if (!FoldOpen(FF[f].line)) return false;
				}
				oldlevel = level;
			}
			f--;
			if (level == 0) break;
		}

		V = RToV(Row);
		//	    if (V == -1) {
		//	        printf("Expose Row = %d\n", Row);
		//	        DumpFold();
		//	    }
		assert (V != -1);
		return true;
	}

	void UpdateVis(EPoint M, int Row, int Delta) { /*FOLD00*/
		if (Delta < 0) {
			if (M.Row > Row)
				if (M.Row < Row - Delta)
					M.Row = Row;
				else
					M.Row += Delta;
		} else {
			if (M.Row >= Row)
				M.Row += Delta;
		}
	}

	void UpdateVisible(int Row, int Delta) { /*FOLD00*/
		EView w;

		Row = RToV(Row);
		UpdateVis(CP, Row, Delta);
		w = View;
		if (w != null) do {
			UpdateVis(GetViewVPort(w).TP, Row, Delta);
			UpdateVis(GetViewVPort(w).CP, Row, Delta);
			w = w.Next;
		} while (w != View);
	}

	public boolean FoldCreate(int Line) { /*FOLD00*/
		int n;

		if (!Modify()) return false;

		if (FindFold(Line) != -1) return true; // already exists

		if (BFI(this, BFI_Undo)) {
			//if (PushULong(Line) == 0) return false;
			//if (PushUChar(ucFoldCreate) == 0) return false;
			getSlot().
				pushInt(Line).
				pushOp(UndoOperation.ucFoldCreate);
		}

		n = FindNearFold(Line);
		n++;
		
		//FF = (EFold ) realloc(FF, sizeof(EFold) * ((1 + FCount) | 7));
		FF = Arrays.copyOf(FF, (1 + FCount) | 7);
		//memmove(FF + n + 1, FF + n, sizeof(EFold) * (FCount - n));
		System.arraycopy(FF, n, FF, n + 1, FCount - n);
		
		FCount++;
		FF[n].line = Line;
		FF[n].level = 0;
		FF[n].open = true;
		FF[n].flags = 0;
		Draw(Line, Line);
		return true;
	}

	boolean FoldCreateByRegexp(String Regexp) { /*FOLD00*/
		boolean err = true;

		if (!Modify()) return false;

		Pattern R = Pattern.compile(Regexp);
		if (R == null) return false;

		//return false;
		//*
		int first = -1;
		int L;

		for (L = 0; L < RCount; L++) {
			//RxMatchRes RM;

			ELine X = RLine(L);
			//if (RxExec(R, X.Chars, X.getCount(), X.Chars, RM) == 1)			
			Matcher m = R.matcher(X.Chars);
			
			if(m.matches())
			{
				if (first >= 0) {
					int i;

					for(i = L; i > 0; i--) {
						ELine Y;

						Y = RLine(i);
						if ((Y.getCount() == 0) || CString.strrchr(Y.Chars.toString(), '}') >= 0) 
						{
							if ((L - i) > 2) {
								while ((i > 0) && (RLine(i - 1).getCount() == 0))
									i--;
								if ((first >= 0) && i != 0
										&& (!FoldCreate(i)))
									err = false;
							}
							break;
						}
					}
				} else
					first = L;
				if (!FoldCreate(L)) {
					err = false;
					break;
				}
			}
		}

		return err; // */
	}

	boolean FoldCreateAtRoutines() { /*FOLD00*/
		if (BFS(this, BFS_RoutineRegexp) == null)
			return false;
		return FoldCreateByRegexp(BFS(this, BFS_RoutineRegexp));
	}

	public boolean FoldDestroy(int Line) { /*FOLD00*/
		int f = FindFold(Line);

		if (!Modify()) return false;

		if (f == -1) return false;
		if (!FF[f].open)
			if (!FoldOpen(Line)) return false;

		if (BFI(this, BFI_Undo)) {
			//if (PushULong(FF[f].level) == 0) return false;
			//if (PushULong(Line) == 0) return false;
			//if (PushUChar(ucFoldDestroy) == 0) return false;
			getSlot().
				pushInt(FF[f].level).
				pushInt(Line).
				pushOp(UndoOperation.ucFoldDestroy);
		}

		//memmove(FF + f, FF + f + 1, sizeof(EFold) * (FCount - f - 1));
		System.arraycopy(FF, f + 1, FF, f, FCount - f - 1);
		FCount--;
		//FF = (EFold ) realloc(FF, sizeof(EFold) * (FCount | 7));
		FF = Arrays.copyOf(FF, FCount | 7);
		Draw(Line, Line);
		return true;
	}

	boolean FoldDestroyAll() { /*FOLD00*/
		int l;

		if (!Modify()) return false;

		for (l = 0; l < RCount; l++)
			if (FindFold(l) != -1)
				if (!FoldDestroy(l)) return false;
		return true;
	}

	public boolean FoldPromote(int Line) { /*FOLD00*/
		int f = FindFold(Line);

		if (!Modify()) return false;

		if (f == -1) return false;
		if (!FF[f].open) return false;
		if (FF[f].level == 0) return false;

		if (BFI(this, BFI_Undo)) {
			//if (PushULong(Line) == 0) return false;
			//if (PushUChar(ucFoldPromote) == 0) return false;
			getSlot().
				pushInt(Line).
				pushOp(UndoOperation.ucFoldPromote);
		}

		if ((FF[f].line > 0) && (!ExposeRow(FF[f].line - 1)))
			return false;

		FF[f].level--;
		Draw(Line, Line);
		return true;
	}

	public boolean FoldDemote(int Line) { /*FOLD00*/
		int f = FindFold(Line);

		if (!Modify()) return false;

		if (f == -1) return false;
		if (!FF[f].open) return false;
		if (FF[f].level == 99) return false;

		if (BFI(this, BFI_Undo)) {
			//if (PushULong(Line) == 0) return false;
			//if (PushUChar(ucFoldDemote) == 0) return false;
			getSlot().
				pushInt(Line).
				pushOp(UndoOperation.ucFoldDemote);
		}

		if ((FF[f].line > 0) && (!ExposeRow(FF[f].line - 1)))
			return false;

		FF[f].level++;
		Draw(Line, Line);
		return true;
	}

	public boolean FoldOpen(int Line) { /*FOLD00*/
		int f = FindFold(Line);
		int l;
		int level, toplevel;
		int top;

		if (f == -1) return false;
		if (FF[f].open) return true; // already open

		if (!Modify()) return false;

		if (BFI(this, BFI_Undo)) {
			//if (PushULong(Line) == 0) return false;
			//if (PushUChar(ucFoldOpen) == 0) return false;
			getSlot().
			pushInt(Line).
			pushOp(UndoOperation.ucFoldOpen);
		}

		FF[f].open = true;
		top = FF[f].line;
		toplevel = FF[f].level;
		//    printf("Fold starts with %d\n", FF[f].line);
		if (!ShowRow(FF[f].line)) return false;
		while (f < FCount) {
			level = FF[f].level;
			if (FF[f].open) {
				// fold is open
				if (f == FCount - 1) {
					for (l = FF[f].line; l < RCount; l++)
						if (l != top)
							if (!ShowRow(l)) return false;
				} else {
					for (l = FF[f].line; l < FF[f + 1].line; l++)
						if (l != top)
							if (!ShowRow(l)) return false;
				}
				f++;
			} else { // fold is closed
				// show head line
				if (!ShowRow(FF[f].line)) return false;
				// skip closed folds
				while ((f < FCount) && (level < FF[f + 1].level))
					f++;
				f++;
			}
			if (f < FCount && FF[f].level <= toplevel)
				break;
		}
		return true;
	}

	boolean FoldOpenAll() { /*FOLD00*/
		int l;

		for (l = 0; l < RCount; l++)
			if (FindFold(l) != -1)
				if (!FoldOpen(l)) return false;
		return true;
	}

	boolean FoldOpenNested() { /*FOLD00*/
		int Line = VToR(CP.Row);
		int f = FindFold(Line);
		int l;
		int level;

		if (f == -1) return false;
		level = FF[f].level;

		while (f + 1 < FCount && FF[f + 1].level > level) f++;

		if (f + 1 == FCount) {
			if (!FoldOpen(Line)) return false;
		} else {
			for (l = Line; l < RCount && l < FF[f + 1].line; l++) {
				if (FindFold(l) != -1)
					if (!FoldOpen(l)) return false;
			}
		}
		return false;
	}

	public boolean FoldClose(int Line) { /*FOLD00*/
		int f = FindNearFold(Line);
		int l, top;
		int level;

		if (f == -1) return false;
		if (!FF[f].open) return true; // already closed

		if (!Modify()) return false;

		if (!SetPosR(CP.Col, FF[f].line, tmLeft)) return false;

		if (BFI(this, BFI_Undo)) {
			//if (PushULong(Line) == 0) return false;
			//if (PushUChar(ucFoldClose) == 0) return false;
			getSlot().
				pushInt(Line).
				pushOp(UndoOperation.ucFoldClose);
		}

		FF[f].open = false;
		top = FF[f].line;
		level = FF[f].level;
		while ((f < FCount - 1) && (FF[f + 1].level > level)) f++;

		/* performance tweak: do it in reverse (we'll see if it helps) */

		if (f == FCount - 1) {
			for (l = RCount - 1; l > top; l--)
				if (!HideRow(l)) return false;
		} else {
			for (l = FF[f + 1].line - 1; l > top; l--)
				if (!HideRow(l)) return false;
		}

		/* yup, it does. try below for a (MUCH!) slower version */

		/*if (f == FCount - 1) {
	     for (l = top + 1; l < RCount; l++)
	     if (HideRow(l) == 0) return false;
	     } else {
	     for (l = top + 1; l < FF[f + 1].line; l++)
	     if (HideRow(l) == 0) return false;
	     }*/
		return true;
	}

	boolean FoldCloseAll() { /*FOLD00*/
		int l;

		for (l = RCount - 1; l >= 0; l--)
			if (FindFold(l) != -1)
				if (!FoldClose(l)) return false;
		return true;
	}

	boolean FoldToggleOpenClose() { /*FOLD00*/
		int Line = VToR(CP.Row);
		int f;

		f = FindNearFold(Line);
		if (f == -1)
			return false;
		if (FF[f].open) {
			if (!FoldClose(Line)) return false;
		} else {
			if (!FoldOpen(Line)) return false;
		}
		return true;
	}

	boolean MoveFoldTop() { /*FOLD00*/
		int f = FindNearFold(VToR(CP.Row));

		if (f == 0 || f == -1) return false;

		if (FF[f].line == VToR(CP.Row))
			return true;
		if (!SetPosR(CP.Col, FF[f].line, tmLeft)) return false;
		return true;
	}

	boolean MoveFoldPrev() { /*FOLD00*/
		int f = FindNearFold(VToR(CP.Row));

		if (f == 0 || f == -1) return false;

		if (FF[f].line == VToR(CP.Row)) {
			do {
				f--;
				if (f < 0) return false;
				if (RToV(FF[f].line) != -1)
					break;
			} while (true);
		}
		if (!SetPosR(CP.Col, FF[f].line, tmLeft)) return false;
		return true;
	}

	boolean MoveFoldNext() { /*FOLD00*/
		int f = FindNearFold(VToR(CP.Row));

		if (f == FCount - 1 || f == -1) return false;

		do {
			f++;
			if (f == FCount) return false;
			if (RToV(FF[f].line) != -1)
				break;
		} while (true);
		if (!SetPosR(CP.Col, FF[f].line, tmLeft)) return false;
		return true;
	}








	boolean FoldCreateByRegexp(ExState State) {
		String [] strbuf = {""};

		if (State.GetStrParam(View, strbuf) == 0) {
			if (View.MView.Win.GetStr("Create Fold Regexp", strbuf, HIST_REGEXP) == 0) return false;
		}
		return FoldCreateByRegexp(strbuf[0]);
	}







	
	
	
	
	
	
	
	
	
	
	boolean FindTagWord(ExState State) {
	    //char word[MAXSEARCH + 1];
	    ELine L = VLine(CP.Row);
	    int P = 0;

	    StringBuilder word = new StringBuilder();
	    
	    P = CharOffset(L, CP.Col);
	    while ((P > 0) && ((ChClass(L.Chars.charAt(P - 1)) == 1) || (L.Chars.charAt(P - 1) == '_')))
	        P--;
	    while (P < L.getCount() && (ChClass(L.Chars.charAt(P)) == 1 || L.Chars.charAt(P) == '_'))
	        word.append( L.Chars.charAt(P++) );

	    if (word.length() == 0) {
	        Msg(S_INFO, "No word at cursor.");
	        return false;
	    }

	    int j = 2;
	    while (j-- > 0) {
	        int i;

	        i = Tags.TagFind(this, View, word.toString());
	        if (i > 0)
	            return true;
	        else if (j != 0 && (i < 0)) {
	            /* Try autoload tags */
	            if (View.ExecCommand(ExCommands.ExTagLoad, State) == ExResult.ErFAIL)
	                break;
	        } else {
	            Msg(S_INFO, "Tag '%s' not found.", word.toString());
	            break;
	        }
	    }
	    return false;
	}
	

	boolean FindTag(ExState State) {
	    String [] Tag = {""};

	    if (State.GetStrParam(View, Tag) == 0)
	        if (View.MView.Win.GetStr("Find tag", Tag, HIST_SEARCH) == 0) return false;

	    int j = 2;
	    while (j-- > 0) {
	        int i;

	        i = Tags.TagFind(this, View, Tag[0]);
	        if (i > 0)
	            return true;
	        else if (j != 0 && (i < 0)) {
	            /* Try autoload tags */
	            if (View.ExecCommand(ExCommands.ExTagLoad, State) == ExResult.ErFAIL)
	                break;
	        } else {
	            Msg(S_INFO, "Tag '%s' not found.", Tag[0]);
	            break;
	        }
	    }
	    
	    return false;
	}
	
	


}


