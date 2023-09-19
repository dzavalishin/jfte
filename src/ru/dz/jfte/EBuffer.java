package ru.dz.jfte;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.Arrays;

import ru.dz.jfte.c.ArrayPtr;
import ru.dz.jfte.c.BinaryString;
import ru.dz.jfte.c.BitOps;
import ru.dz.jfte.c.ByteArrayPtr;

public class EBuffer extends EModel implements BufferDefs, ModeDefs, GuiDefs, ColorDefs, EventDefs 
{
	String FileName = null;
	int Modified = 0;
	EPoint TP = new EPoint();
	EPoint CP = new EPoint();

	EPoint BB = new EPoint(-1, -1);
	EPoint BE = new EPoint(-1, -1);
	EPoint PrevPos = new EPoint(-1, -1);
	EPoint SavedPos = new EPoint(-1, -1);

	EBufferFlags Flags;
	EMode Mode;
	int BlockMode = bmStream;
	int ExtendGrab = 0;
	boolean AutoExtend = false;
	boolean Loaded = false;

	// TODO UndoStack US;

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


	// TODO folds
	//int FCount = 0;
	//EFold FF = null;

	EPoint Match = new EPoint(-1, -1);
	int MatchLen = 0;
	int MatchCount = 0;
	RxMatchRes MatchRes;

	/* TODO /* TODO #ifdef CONFIG_BOOKMARKS
    int BMCount;
    EBookmark *BMarks;
#endif */

	///* TODO #ifdef CONFIG_OBJ_ROUTINE
	RoutineList rlst;
	RoutineView Routines;
	//#endif */ 

	int MinRedraw, MaxRedraw;
	int RedrawToEos;

	/* TODO /* TODO #ifdef CONFIG_WORD_HILIT
    String *WordList;
    int WordCount;
#endif */
	/* TODO #ifdef CONFIG_SYNTAX_HILIT
#endif */ 
	SyntaxProc HilitProc;
	int StartHilit, EndHilit;





	final static int   ccUp       = 0;
	final static int   ccDown     =1;
	final static int   ccToggle   =2;

	static SearchReplaceOptions LSearch;
	static int suspendLoads;
	static EBuffer SSBuffer = null; // scrap buffer (clipboard)


	int iBFI(EBuffer y, int x)  { return (y.Flags.num[x & 0xFF]); }
	boolean BFI(EBuffer y, int x)  { return (y.Flags.num[x & 0xFF]) != 0; }
	void BFI_SET(EBuffer y, int x, int v) { y.Flags.num[x & 0xFF]=v; }
	String BFS(EBuffer y,int x) { return y.Flags.str[x & 0xFF]; }

	///////////////////////////////////////////////////////////////////////////////

	EBuffer(int createFlags, EModel []ARoot, String AName)
	//:EModel(createFlags, ARoot), TP(0,0), CP(0,0), BB(-1,-1), BE(-1,-1),
	//PrevPos(-1, -1), SavedPos(-1, -1), Match(-1, -1)

	{
		super(createFlags, ARoot);


		/*/* TODO #ifdef CONFIG_UNDOREDO
		US.Num = 0;
		US.Data = 0;
		US.Top = 0;
		US.UndoPtr = 0;
		US.NextCmd = 1;
		US.Record = 1;
		US.Undo = 0;
		#endif */
		/* TODO #ifdef CONFIG_BOOKMARKS
		BMCount = 0;
		BMarks = 0;
		#endif */
		/* TODO #ifdef CONFIG_OBJ_ROUTINE
		rlst.getCount() = 0;
		rlst.Lines = 0;
		Routines = 0;
		#endif */
		/* TODO #ifdef CONFIG_WORD_HILIT
		WordList = 0;
		WordCount = 0;
		#endif */ 
		//Name = strdup(AName);
		Allocate(0);
		AllocVis(0);
		Mode = GetModeForName("");
		Flags = (Mode.Flags);
		// was BFI(this, BFI_Undo) = 0;
		BFI_SET(this, BFI_Undo,  0);
		// was BFI(
		BFI_SET(this, BFI_ReadOnly, 0);

		Modified = 0;
		MinRedraw = -1;
		MaxRedraw = -1;
		RedrawToEos = 0;
		/* TODO /* TODO #ifdef CONFIG_SYNTAX_HILIT
		StartHilit = 0;
		EndHilit = -1;
		HilitProc = 0;
		if (Mode && Mode.fColorize)
			HilitProc = GetHilitProc(Mode.fColorize.SyntaxParser);
		#endif */ 
		InsertLine(CP,0,null); /* there should always be at least one line in the edit buffer */
		Flags = (Mode.Flags);
		Modified = 0;
	}

	/* TODO
	~EBuffer() {
		/* TODO #ifdef CONFIG_HISTORY
		if (FileName != 0 && Loaded) {
			UpdateFPos(FileName, VToR(CP.Row), CP.Col);
			/* TODO #ifdef CONFIG_BOOKMARKS
			if (BFI (this,BFI_SaveBookmarks)==3) StoreBookmarks(this);
			#endif 
		}
		#endif 
		if (FileName && Loaded)
			markIndex.storeForBuffer(this);

		Clear();

		/* TODO #ifdef CONFIG_BOOKMARKS
		if (BMCount != 0) {
			BMarks = 0;
			BMCount = 0;
		}
		#endif */
	/* TODO #ifdef CONFIG_OBJ_ROUTINE
		rlst.Lines = 0;
		DeleteRelated();
		#endif 
	} */

	void DeleteRelated() {
		/* /* TODO #ifdef CONFIG_OBJ_ROUTINE
		if (Routines) {
			::ActiveView.DeleteModel(Routines);
			Routines = 0;
		}
		#endif */ 
	}

	boolean Clear() {
		Modified = 1;
		/* TODO #ifdef CONFIG_SYNTAX_HILIT
		EndHilit = -1;
		StartHilit = 0;

		while (WordCount--)
		{
			free(WordList[WordCount]);
		}
		free(WordList);

		WordCount = 0;
		WordList = 0;
		#endif */
		/* TODO #ifdef CONFIG_OBJ_ROUTINE
		rlst.getCount() = 0;
		if (rlst.Lines) {
			free(rlst.Lines);
			rlst.Lines = 0;
		}
		#endif */
		LL = null;
		RCount = RAllocated = RGap = 0;
		VCount = VAllocated = VGap = 0;
		VV = null;

		/* TODO #ifdef CONFIG_UNDOREDO
		FreeUndo();
		#endif */

		return false;
	}

	/* TODO #ifdef CONFIG_UNDOREDO
	int FreeUndo() {
		for (int j = 0; j < US.Num; j++)
			free(US.Data[j]);
		free(US.Top);
		free(US.Data);
		US.Num = 0;
		US.Data = 0;
		US.Top = 0;
		US.Undo = 0;
		US.Record = 1;
		US.UndoPtr = 0;
		return true;
	}
	#endif */

	boolean Modify() {
		if (BFI(this, BFI_ReadOnly)!=0) 
		{
			Msg(S_ERROR, "File is read-only.");
			return false;
		}
		if (Modified == 0) {


			if ((FileName != null) && FileOk) 
			{
				Path fp = Paths.get(FileName);
				BasicFileAttributes StatBuf = null;
				try {
					StatBuf = Files.readAttributes( fp, BasicFileAttributes.class);
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}

				if( StatBuf != null &&
						(FileStatus == null || FileStatus.size() != StatBuf.size() ||
						FileStatus.lastModifiedTime() != StatBuf.lastModifiedTime())
						)
				{
					View.MView.Win.Choice(GPC_ERROR, "Warning! Press Esc!",
							0,
							"File %-.55s changed on disk!", 
							FileName);
					switch (View.MView.Win.Choice(0, "File Changed on Disk",
							2,
							"&Modify",
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
			/* TODO #ifdef CONFIG_UNDOREDO
			if (BFI(this, BFI_Undo))
				if (PushUChar(ucModified) == 0) return false;
			#endif */
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
		EView V;

		UpdateMark(SavedPos, Type, Row, Col, Rows, Cols);
		UpdateMark(PrevPos, Type, Row, Col, Rows, Cols);

		UpdateMark(BB, Type, Row, Col, Rows, Cols);
		UpdateMark(BE, Type, Row, Col, Rows, Cols);

		V = View;
		while (V != null) {
			if (V.Model != this)
				assert(1 == 0);
			if (V != View) {
				EPoint M;

				M = GetViewVPort(V).TP;
				UpdateMark(GetViewVPort(V).TP, Type, Row, Col, Rows, Cols);
				GetViewVPort(V).TP.Col = M.Col;
				UpdateMark(GetViewVPort(V).CP, Type, Row, Col, Rows, Cols);
			}
			V = V.NextView;
		}

		/* TODO #ifdef CONFIG_OBJ_ROUTINE
		for (int i = 0; i < rlst.getCount() && rlst.Lines; i++) {
			EPoint M;

			M.Col = 0;
			M.Row = rlst.Lines[i];
			UpdateMark(M, Type, Row, Col, Rows, Cols);
			rlst.Lines[i] = M.Row;
		}
		#endif */

		/* TODO FF
		for (int f = 0; f < FCount; f++) {
			EPoint M = new EPoint();

			M.Col = 0;
			M.Row = FF[f].line;
			UpdateMark(M, Type, Row, Col, Rows, Cols);
			FF[f].line = M.Row;
		} */

		/* TODO #ifdef CONFIG_BOOKMARKS
		for (int b = 0; b < BMCount; b++)
			UpdateMark(BMarks[b].BM, Type, Row, Col, Rows, Cols);
		#endif */

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
			Mode = FindMode(AMode);
		if (Mode == null)
			Mode = GetModeForName(AFileName);
		assert(Mode != null);
		Flags = (Mode.Flags);
		/* TODO #ifdef CONFIG_SYNTAX_HILIT
		HilitProc = 0;
		if (Mode && Mode.fColorize)
			HilitProc = GetHilitProc(Mode.fColorize.SyntaxParser);
		#endif */
		UpdateTitle();
		return FileName != null;
	}

	boolean SetPos(int Col, int Row) {
		return SetPos(Col, Row, 0);
	}	

	boolean SetPos(int Col, int Row, int tabMode) {
		assert (Col >= 0 && Row >= 0 && Row < VCount);

		/* TODO #ifdef CONFIG_UNDOREDO
		if (BFI(this, BFI_Undo) == 1 && BFI(this, BFI_UndoMoves) == 1) {
			if (PushULong(CP.Col) == false) return false;
			if (PushULong(CP.Row) == false) return false;
			if (PushUChar(ucPosition) == false) return false;
		}
		#endif */
		if (AutoExtend) {
			BlockExtendBegin();
			AutoExtend = true;
		}
		PrevPos = CP;
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
		if (BFI(this, BFI_CursorThroughTabs) == 0) {
			if (tabMode == tmLeft) {
				if (MoveTabStart() == false) return false;
			} else if (tabMode == tmRight) {
				if (MoveTabEnd() == false) return false;
			}
		}
		if (ExtendGrab == 0 && !AutoExtend  && BFI(this, BFI_PersistentBlocks) == 0) {
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

	boolean SetNearPos(int Col, int Row, int tabMode) {
		if (Row >= VCount) Row = VCount - 1;
		if (Row < 0) Row = 0;
		if (Col < 0) Col = 0;
		return SetPos(Col, Row, tabMode);
	}

	boolean SetNearPosR(int Col, int Row, int tabMode) {
		if (Row >= RCount) Row = RCount - 1;
		if (Row < 0) Row = 0;
		if (Col < 0) Col = 0;
		return SetPosR(Col, Row, tabMode);
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


	boolean DelLine(int Row) { return DelLine(Row, false); }	
	
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

		/* TODO
		if (FindFold(Row) != -1) {
			if (FoldDestroy(Row) == false) return false;
		} */

		VLine = RToV(Row);
		assert(VLine != -1);

		/* TODO #ifdef CONFIG_UNDOREDO
		if (BFI(this, BFI_Undo) == 1) {
			if (PushUData(RLine(Row).Chars, RLine(Row).getCount()) == false) return false;
			if (PushULong(RLine(Row).getCount()) == false) return false;
			if (PushULong(Row) == false) return false;
			if (PushUChar(ucDelLine) == false) return false;
		}
		#endif */
		if (DoMark)
			UpdateMarker(umDelete, Row, 0, 1, 0);
		//puts("Here");

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

	boolean InsLine(int Row, int DoAppend) {
		return InsLine(Row, DoAppend, false);
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
		ELine L = new ELine("");
		//if (L == 0) return false;
		/* TODO #ifdef CONFIG_UNDOREDO
		if (BFI(this, BFI_Undo) == 1) {
			if (PushULong(Row) == false) return false;
			if (PushUChar(ucInsLine) == false) return false;
		}
		#endif */
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
		LL[RGap] = L;
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

	boolean DelChars(int Row, int Ofs, int ACount) {
		ELine L;

		//   printf("DelChars: %d:%d %d\n", Row, Ofs, ACount);
		if (Row < 0) return false;
		if (Row >= RCount) return false;
		L = RLine(Row);

		if (Ofs < 0) return false;
		if (Ofs >= L.getCount()) return false;
		if (Ofs + ACount >= L.getCount())
			ACount = L.getCount() - Ofs;
		if (ACount == 0) return true;

		if (!Modify()) return false;

		/* TODO #ifdef CONFIG_UNDOREDO
		if (BFI(this, BFI_Undo) == 1) {
			if (PushUData(L.Chars + Ofs, ACount) == false) return false;
			if (PushULong(ACount) == false) return false;
			if (PushULong(Ofs) == false) return false;
			if (PushULong(Row) == false) return false;
			if (PushUChar(ucDelChars) == false) return false;
		}
		#endif */

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

	boolean InsChars(int Row, int Ofs, int ACount, String Buffer) {
		//   printf("InsChars: %d:%d %d\n", Row, Ofs, ACount);

		assert(Row >= 0 && Row < RCount && Ofs >= 0);
		ELine L = RLine(Row);

		if (Ofs < 0) return false;
		if (Ofs > L.getCount()) return false;
		if (ACount == 0) return true;

		if (!Modify()) return false;

		/* TODO #ifdef CONFIG_UNDOREDO
		if (BFI(this, BFI_Undo) == 1) {
			if (PushULong(Row) == false) return false;
			if (PushULong(Ofs) == false) return false;
			if (PushULong(ACount) == false) return false;
			if (PushUChar(ucInsChars) == false) return false;
		}
		#endif */
		L.Allocate(L.getCount() + ACount);
		if (L.getCount() > Ofs)
			//memmove(L.Chars + Ofs + ACount, L.Chars + Ofs, L.getCount() - Ofs);
			L.memmove(Ofs + ACount, Ofs, L.getCount() - Ofs);
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
			TPos = NextTab(Pos, BFI(this, BFI_TabSize));
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

		/* TODO #ifdef CONFIG_UNDOREDO
		if (BFI(this, BFI_Undo) == 1) {
			if (PushUData(L.Chars + Ofs, ACount) == false) return false;
			if (PushULong(ACount) == false) return false;
			if (PushULong(Ofs) == false) return false;
			if (PushULong(Row) == false) return false;
			if (PushUChar(ucDelChars) == false) return false;
			if (PushULong(Row) == false) return false;
			if (PushULong(Ofs) == false) return false;
			if (PushULong(ACount) == false) return false;
			if (PushUChar(ucInsChars) == false) return false;
		}
		#endif */
		Hilit(Row);
		Draw(Row, Row);
		return true;
	}

	boolean DelText(int Row, int Col, int ACount) {
		return DelText(Row, Col, ACount, false);
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

	
	boolean InsText(int Row, int Col, int ACount, String ABuffer) {
		return InsText(Row, Col, ACount, ABuffer, false );
		}	
	boolean InsText(int Row, int Col, int ACount, String ABuffer, boolean DoMark) {
		int B, L;

		//   printf("InsText: %d:%d %d\n", Row, Col, ACount);
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
		//   printf("OK\n");
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
		if (BFI(this, BFI_ReadOnly) == 1)
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
				TPos = NextTab(Pos, BFI(this, BFI_TabSize));
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
		if (InsText(Row, Col, C, Line.Chars + B) == false) return false;
		//   printf("OK\n");
		return true;
	}

	boolean SplitLine(int Row, int Col) {
		int VL;

		assert(Row >= 0 && Row < RCount && Col >= 0);

		if (BFI(this, BFI_ReadOnly) == 1) return false;

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
			if (Col < LineLen(Row)) {
				int P, L;
				//if (RLine(Row).ExpandTabs(Col, -2, &Flags) == false) return false;
				if (!UnTabPoint(Row, Col))
					return false;

				P = CharOffset(RLine(Row), Col);
				L = LineLen(Row);

				if (InsText(Row + 1, 0, RLine(Row).getCount() - P, RLine(Row).Chars + P, 0) == false) return false;
				if (!DelText(Row, Col, L - Col, false)) return false;
			}
		}
		Draw(Row, -1);
		Hilit(Row);
		return true;
	}

	boolean JoinLine(int Row, int Col) {
		int Len, VLine;

		if (BFI(this, BFI_ReadOnly) == 1) return false;
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
		boolean ExpandTabs = BFI(this, BFI_ExpandTabs) != 0;
		int TabSize = BFI(this, BFI_TabSize);

		if (!ExpandTabs) {
			return Offset;
		} else {
			//String p = L.Chars;
			ArrayPtr<Character> p = L.getPointer();
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
		boolean ExpandTabs = BFI(this, BFI_ExpandTabs) != 0;
		int TabSize = BFI(this, BFI_TabSize);

		if (!ExpandTabs) {
			return ScreenPos;
		} else {
			int Pos = 0;
			int Ofs = 0;
			//String p = L.Chars;
			ArrayPtr<Character> p = L.getPointer();
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

		LL = new ELine [ACount];
		Arrays.fill(LL, null);

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
		//int []V;

		//V = (int []) realloc(VV, sizeof(int) * (ACount + 1));
		//if (V == 0 && ACount != 0) return 0;
		VAllocated = ACount;
		//VV = V;
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
		// TODO /* TODO #ifdef DEBUG_EDITOR
		int N = GapLine(No, RGap, RCount, RAllocated);
		if (!((No < RCount) && (No >= 0) && (LL[N]!=null))) {
			printf("Get No = %d/%d Gap=%d RAlloc = %d, VCount = %d\n", No, RCount, RGap, RAllocated, VCount);
			assert((No < RCount) && (No >= 0) && (LL[N]!=null));
		}
		//#endif */
		return LL[GapLine(No, RGap, RCount, RAllocated)];
	}

	void RLine(int No, ELine L) {
		// TODO /* TODO #ifdef DEBUG_EDITOR
		if (!((No >= 0))) printf("Set No = %d\n", No);
		assert((No >= 0));
		// TODO #endif */
		LL[GapLine(No, RGap, RCount, RAllocated)] = L;
	}

	int Vis(int No) {
		// TODO /* TODO #ifdef DEBUG_EDITOR
		if (No < 0 || No >= VCount) {
			printf("Vis get no %d of %d\n", No, VCount);
			assert (No >= 0 && No < VCount);
		}
		// TODO #endif */
		return VV[GapLine(No, VGap, VCount, VAllocated)];
	}

	void Vis(int No, int V) {
		// TODO /* TODO #ifdef DEBUG_EDITOR
		if (No < 0 || No >= VCount) {
			printf("Vis set no %d of %d to %d\n", No, VCount, V);
			assert (No >= 0 && No < VCount);
		}
		//#endif */
		VV[GapLine(No, VGap, VCount, VAllocated)] = V;
	}

	ELine VLine(int No) {
		// TODO /* TODO #ifdef DEBUG_EDITOR
		if (!((No < VCount) && (No >= 0))) {
			printf("VGet No = %d\n", No);
			assert((No < VCount) && (No >= 0));
		}
		if (Vis(No) < 0)
			assert(1 == 0);
		//#endif */
		return RLine(No + Vis(No));
	}

	void VLine(int No, ELine L) {
		// TODO /* TODO #ifdef DEBUG_EDITOR
		if (!((No >= 0))) {
			printf("VSet No = %d\n", No);
			assert((No >= 0));
		}
		if (VV[No] < 0)
			assert(1 == 0);
		//#endif */
		RLine(No + Vis(No), L);
	}

	int VToR(int No) {
		// TODO /* TODO #ifdef DEBUG_EDITOR
		if (!(No < VCount)) {
			printf("Get No = %d\n", No);
			assert((No < VCount));
		}
		//#endif */
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






























	boolean ExposeRow(int Row) { /*FOLD00*/
		int level, oldlevel = 100;

		//DumpFold();

		assert(Row >= 0 && Row < RCount); // range

		int V = RToV(Row);
		if (V != -1) return true; // already exposed

		/* TODO fold
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
		*/
		
		V = RToV(Row);
		//	    if (V == -1) {
		//	        printf("Expose Row = %d\n", Row);
		//	        DumpFold();
		//	    }
		assert (V != -1);
		return true;
	}





	boolean GetMap(int Row, int []StateLen, byte /*hsState*/ [][]StateMap) {
		int/*hlState*/ State = 0;

		Rehilit(Row);

		StateLen[0] = LineChars(Row);
		if (Row > 0) State = RLine(Row - 1).StateE;
		if (StateLen[0] > 0) {
			ELine L = RLine(Row);
			int [] ECol = {0};

			StateMap[0] = new byte[StateLen[0]];
			if (StateMap[0] == null) return false;

			/* TODO #ifdef CONFIG_SYNTAX_HILIT
	        if (BFI(this, BFI_HilitOn) == 1 && HilitProc != 0)
	            HilitProc(this, Row, 0, 0, *StateLen, L, State, *StateMap, &ECol);
	        else
	#endif */
			// TODO Hilit_Plain(this, Row, 0, 0, StateLen[0], L, State, StateMap[0], ECol);
			//        if (L.StateE != State) {
			//            L.StateE = State;
			//        }
		} else {
			StateLen[0] = 1;
			StateMap[0] = new byte[1];
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

		if (BFI(this, BFI_MultiLineHilit) == 0) // everything handled in redisplay
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

			if (BFI(this, BFI_HilitOn) == 1 && HilitProc != null) {
				HilitProc.proc(this, StartHilit, null, 0, 0, L, State, 0, ECol);
			} else {
				Hilit_Plain(this, StartHilit, 0, 0, 0, L, State, 0, ECol);
			}
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

			if (Row > 0) State = RLine(Row - 1).StateE;
			else State = 0;
			/* TODO #ifdef CONFIG_SYNTAX_HILIT
	        if (BFI(this, BFI_HilitOn) == 1 && HilitProc != 0)
	            HilitProc(this, Row, B, C, W, L, State, 0, &ECol);
	        else
	#endif */
			int [] ecp = {0};
			Hilit_Plain(this, Row, B, C, W, L, State, 0, ecp);
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
				int l;

				/* TODO fold
				int f = FindFold(Row);
				if (f != -1) {
					int foldColor;
					if (FF[f].level<5) foldColor=hcPlain_Folds[FF[f].level];
					else foldColor=hcPlain_Folds[4];
					if (FF[f].open == 1) {
						l = sprintf(fold, "[%d]", FF[f].level);
						MoveStr(B, ECol - C + 1, W, fold, foldColor, 10);
						ECol += l;
					} else {
						if (VRow < VCount - 1) {
							Folded = Vis(VRow + 1) - Vis(VRow) + 1;
						} else if (VRow < VCount) {
							Folded = RCount - (VRow + Vis(VRow));
						}
						l = sprintf(fold, "(%d:%d)", FF[f].level, Folded);
						MoveStr(B, ECol - C + 1, W, fold, foldColor, 10);
						ECol += l;
						MoveAttr(B, 0, W, foldColor, W);
					}
				} */
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
				if( 0 != (BFI(this, BFI_SeeThruSel)))
					B.MoveBgAttr( StartPos, W, hcPlain_Selected, EndPos - StartPos);
				else
					B.MoveAttr( StartPos, W, hcPlain_Selected, EndPos - StartPos);
			}
			/* TODO #ifdef CONFIG_BOOKMARKS
	        if (BFI(this, BFI_ShowBookmarks)) {
	            int i = 0;
	            String Name;
	            EPoint P;
	            while ((i = GetBookmarkForLine(i, Row, Name, P)) != -1) {
	                if (strncmp(Name, "_BMK", 4) == 0) {
	                    // User bookmark, hilite line
	                    if (BFI(this, BFI_SeeThruSel))
	                        MoveBgAttr(B, 0, W, hcPlain_Bookmark, W);
	                    else
	                        MoveAttr(B, 0, W, hcPlain_Bookmark, W);
	                    break;
	                }
	            }
	        }
	#endif */
			if (Match.Row != -1 && Match.Col != -1) {
				if (Row == Match.Row) {
					if(0 != (BFI(this, BFI_SeeThruSel)))
						B.MoveBgAttr( Match.Col - C, W, hcPlain_Found, MatchLen);
					else
						B.MoveAttr( Match.Col - C, W, hcPlain_Found, MatchLen);
				}
			}
		} else if (VRow == VCount) {
			if(0 != (BFI(this, BFI_ShowMarkers)))
				B.MoveChar( 0, W, Console.ConGetDrawChar(DCH_END), hcPlain_Markers, W);
		}
	}

	void Redraw() {
		int HilitX;
		EView V;
		EEditPort W;
		int Row;
		TDrawBuffer B = new TDrawBuffer();
		//char s[256];
		int /*ChColor*/ SColor;
		int RowA, RowZ;

		{
			int [] W1, H1;
			if (!(View != null && View.MView != null))
				return;
			View.MView.ConQuerySize(W1, H1);

			if (H1[0] < 1 || W1[0] < 1) return;
		}
		//    printf("Redraw\n");
		if (CP.Row >= VCount) CP.Row = VCount - 1;
		if (CP.Row < 0) CP.Row = 0;

		CheckBlock();
		V = View; /* check some window data */
		if (V==null) {
			MinRedraw = MaxRedraw = -1;
			RedrawToEos = 0;
			return;
		}
		if (View == null|| View.MView == null || View.MView.Win == null)
			return ;

		for ( ; V != null; V = V.NextView) {
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
			B.MoveChar( 0, W.Cols, ' ', SColor, W.Cols);

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
						CCharStr = String.format("%3u,%02X", CurCh, CurCh);
					} else {
						if (CurPos > NumChars) CCharStr = "      ";
						else if (CurLine < NumLines - 1) CCharStr = "   EOL";
						else CCharStr = "   EOF";
					}

					String s = String.format( "%04d:%02d %c%c%c%c%c %.6s %c"
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
											//                    (BFI(this, BFI_ExpandTabs))?'T':' ',
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
																		/* TODO #ifdef CONFIG_WORDWRAP
	                        (BFI(this, BFI_WordWrap) == 3) ? 't' :
	                        (BFI(this, BFI_WordWrap) == 2) ? 'W' :
	                        (BFI(this, BFI_WordWrap) == 1) ? 'w' :
	                        ' ',
	#endif */
																		//                    (BFI(this, BFI_Undo))?'U':' ',
																		//                    (BFI(this, BFI_Trim))?'E':' ',
																		//                    (Flags.KeepBackups)?'B':' ',
																		Mode.fName,
																		(Modified != 0)?'*':(BFI(this, BFI_ReadOnly))?'%':' '
							);

					int l = s.length();
					int fw = W.Cols - l;
					int fl = FileName.length();
					//char num[10];

					B.MoveStr( 0, W.Cols, s, SColor, W.Cols);
					String num = String.format(" %s %d", CCharStr, ModelNo);
					B.MoveStr( W.Cols - num.length(), W.Cols, num, SColor, W.Cols);

					fw -= num.length();

					if (fl > fw) {
						B.MoveStr( l, W.Cols, FileName + fl - fw, SColor, W.Cols);
					} else {
						B.MoveStr( l, W.Cols, FileName, SColor, W.Cols);
					}
				}
			} else {
				B.MoveStr( 0, W.Cols, V.CurMsg, SColor, W.Cols);
			}
			if (V.MView.Win.GetStatusContext() == V.MView) {
				V.MView.ConPutBox(0, W.Rows, W.Cols, 1, B);
				if (V.MView.IsActive()) {
					V.MView.ConShowCursor();
					V.MView.ConSetCursorPos(W.CP.Col - W.TP.Col, W.CP.Row - W.TP.Row);
					if (BFI(this, BFI_Insert)) {
						V.MView.ConSetCursorSize(CursorInsSize[0], CursorInsSize[1]);
					} else {
						V.MView.ConSetCursorSize(CursorOverSize[0], CursorOverSize[1]);
					}
				}
			}
		}

		Rehilit(VToR(CP.Row));

		if (BFI(this, BFI_AutoHilitParen) == 1) {
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

		V = View;
		while (V!=null) {
			if (V.Model != this)
				assert(1 == 0);

			W = GetViewVPort(V);

			for (int R = W.TP.Row; R < W.TP.Row + W.Rows; R++) {
				Row = R;
				if ((Row >= RowA) &&
						(RedrawToEos || Row <= RowZ))
				{
					DrawLine(B, Row, W.TP.Col, W.Cols, HilitX);
					W.DrawLine(Row, B);
					if (HilitX && Row == RowZ)
						RowZ++;
				}
			}
			V = V.NextView;
		}
		MinRedraw = MaxRedraw = -1;
		RedrawToEos = 0;
	}

	boolean GetHilitWord(int len, String str, int /*ChColor*/ []clr, int IgnCase) {
		//String p;

		if (Mode == null || Mode.fColorize == 0)
			return false;

		if (len >= CK_MAXLEN)
			return false;

		/* TODO #ifdef CONFIG_WORD_HILIT
	    {
	        char s[CK_MAXLEN + 1];
	        s[CK_MAXLEN] = 0;
	        memcpy(s, str, len);
	        s[len] = 0;
	        if (HilitFindWord(s)) {
	            clr = hcPlain_HilitWord;
	            return true;
	        }
	    }
	#endif */
		if (len < 1) return false;
		p = Mode.fColorize.Keywords.key[len];
		if (IgnCase) {
			while (p && p[0]) {
				if (strnicmp(p, str, len) == 0) {
					clr = p[len];
					return true;
				}
				p += len + 1;
			}
		} else {
			while (p && p[0]) {
				if (memcmp(p, str, len) == 0) {
					clr = p[len];
					return true;
				}
				p += len + 1;
			}
		}
		if (len < 128) {
			//char s[128];

			memcpy(s, str, len);
			s[len] = 0;
			if (BFI(this, BFI_HilitTags)&&TagDefined(s)) {
				//clr = 0x0A;
				clr = Mode.fColorize.Colors[CLR_HexNumber];
				return true;
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
			//P = X.Chars;
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
								|| (BitOps.BitOps.WGETBIT(Flags.WordChars, X.Chars.charAt(C - 1) /*P[C - 1]*/) == 0))
						&&
						(!osWordEnd
								|| (C + Len >= End)
								|| (BitOps.BitOps.WGETBIT(Flags.WordChars, X.Chars.charAt(C + Len)/*P[C + Len]*/) == 0))
						&&
						((!osNCase
								&& ( X.Chars.charAt(C)/*P[C]*/ == Data[0])
								&& (memcmp(P + C, Data, Len) == 0))
								||
								(osNCase
										&& (toupper(X.Chars.charAt(C)/*P[C]*/) == toupper(Data[0]))
										&& (strnicmp(P + C, Data, Len) == 0))
								) /* && BOL | EOL */
						)
				{
					Match.Col = ScreenPos(X, C);
					Match.Row = L;
					MatchCount = Len;
					MatchLen = ScreenPos(X, C + Len) - Match.Col;
					if (!(Options & SEARCH_NOPOS)) {
						if (Options & SEARCH_CENTER)
							CenterPosR(Match.Col, Match.Row);
						else
							SetPosR(Match.Col, Match.Row);
					}
					Draw(L, L);
					return true;
				}
				if (Options & SEARCH_BACK) C--; else C++;
			}
			if (Options & SEARCH_BACK) {
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
		return false; // TODO 

		/*
	    RxNode regx;
	    int line;
	    RxMatchRes res;

	    if (BFS(this, BFS_RoutineRegexp) == null) {
	        View.MView.Win.Choice(GPC_ERROR, "Error", 1, "O&K", "No routine regexp.");
	        return false;
	    }
	    regx = RxCompile(BFS(this, BFS_RoutineRegexp));
	    if (regx == 0) {
	        View.MView.Win.Choice(GPC_ERROR, "Error", 1, "O&K", "Failed to compile regexp '%s'", BFS(this, BFS_RoutineRegexp));
	        return false;
	    }

	    if (rlst.Lines) {
	        free(rlst.Lines);
	        rlst.Lines = 0;
	    }
	    rlst.Lines = 0;
	    rlst.getCount() = 0;

	    Msg(S_BUSY, "Matching %s", BFS(this, BFS_RoutineRegexp));
	    for (line = 0; line < RCount; line++) {
	    	ELine L = RLine(line);
	        if (RxExec(regx, L.Chars, L.getCount(), L.Chars, res) == 1) {
	            rlst.getCount()++;
	            //rlst.Lines =  realloc((void *) rlst.Lines, sizeof(int) * (rlst.getCount() | 0x1F));
	            rlst.Lines[rlst.getCount() - 1] = line;
	            Msg(S_BUSY, "Routines: %d", rlst.getCount());
	        }
	    }
	    //RxFree(regx);
	    return true;
		 */
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

		while ((P < L.getCount()) && (BitOps.WGETBIT(Flags.WordChars, L.Chars.charAt(P]) == wS)) P++;
		while ((P < L.getCount()) && (BitOps.WGETBIT(Flags.WordChars, L.Chars.charAt(P]) == wE)) P++;
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

		while ((P < L.getCount()) && (BitOps.WGETBIT(Flags.CapitalChars, L.Chars.charAt(P]) == 1)) P++;
		while ((P < L.getCount()) && (BitOps.WGETBIT(Flags.WordChars, L.Chars.charAt(P]) == 1) && (BitOps.WGETBIT(Flags.CapitalChars, L.Chars.charAt(P]) == 0)) P++;
		while ((P < L.getCount()) && (BitOps.WGETBIT(Flags.WordChars, L.Chars.charAt(P]) == 0)) P++;
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
			else if (L.Chars.charAt(C) == 9) P = NextTab(P, BFI(this, BFI_TabSize));
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

		P = NextTab(P, BFI(this, BFI_TabSize));
		return SetPos(P, CP.Row);
	}

	boolean MovePrevTab() {
		int P = CP.Col;

		if (P > 0) {
			P = ((P - 1) / BFI(this, BFI_TabSize)) * BFI(this, BFI_TabSize);
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

	int MoveLineBottom() {
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
		} else if (BFI(this, BFI_WordWrap) == 2 && CP.Row > 0 && !IsLineBlank(Y - 1) &&
					CP.Col <= BFI(this, BFI_LeftMargin) && CP.Col <= LineIndented(Y))
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
		/* TODO #ifdef CONFIG_WORDWRAP
	    if (BFI(this, BFI_WordWrap) == 2) {
	        if (DoWrap(0) == false) return false;
	    }
	#endif */
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
		/* TODO #ifdef CONFIG_WORDWRAP
	    if (BFI(this, BFI_WordWrap) == 2) {
	        if (DoWrap(0) == false) return false;
	        if (CP.Col >= LineLen(Y))
	            if (CP.Row < VCount - 1) {
	                if (SetPos(BFI(this, BFI_LeftMargin), CP.Row + 1) == false) return false;
	            }
	    }
	#endif */
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
			/* TODO #ifdef CONFIG_INDENT_C
	        case INDENT_C: rc = Indent_C(this, L, 1); break;
	#endif
	#ifdef CONFIG_INDENT_REXX
	        case INDENT_REXX: rc = Indent_REXX(this, L, 1); break;
	#endif
	#ifdef CONFIG_INDENT_SIMPLE
	        case INDENT_SIMPLE: rc = Indent_SIMPLE(this, L, 1); break;
	#endif */
			default: rc = Indent_Plain(this, L, 1); break;
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

	boolean CLine() {
		assert(1 == 0);
		return VToR(CP.Row);
	}

	int CColumn() {
		return CP.Col;
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

	boolean InsertString(String aStr, int aCount) {
		int P;
		int C, L;
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
		C = CP.Col;
		L = VToR(CP.Row);
		P = CharOffset(RLine(L), C);
		P += aCount;
		C = ScreenPos(RLine(L), P);
		if (SetPos(C, CP.Row) == false)
			return false;
		if (BFI(this, BFI_Trim))
			if (TrimLine(L) == false)
				return false;
		/* TODO #ifdef CONFIG_WORDWRAP
	    if (BFI(this, BFI_WordWrap) == 2) {
	        if (DoWrap(0) == false) return false;
	    } else if (BFI(this, BFI_WordWrap) == 1) {
	        int P, C = CP.Col;
	        ELine LP;
	        int L;

	        if (C > BFI(this, BFI_RightMargin)) {
	            L = CP.Row;

	            C = BFI(this, BFI_RightMargin);
	            P = CharOffset(LP = RLine(L), C);
	            while ((C > BFI(this, BFI_LeftMargin)) &&
	                   ((LP.Chars.charAt(P) != ' ') &&
	                    (LP.Chars.charAt(P) != 9)))
	                C = ScreenPos(LP, --P);

	            if (P <= BFI(this, BFI_LeftMargin)) {
	                C = BFI(this, BFI_RightMargin);
	            } else
	                C = ScreenPos(LP, P);
	            if (SplitLine(L, C) == false) return false;
	            IndentLine(L + 1, BFI(this, BFI_LeftMargin));
	            if (SetPos(CP.Col - C - 1 + BFI(this, BFI_LeftMargin), CP.Row + 1) == false) return false;
	        }
	    }
	#endif */
		return true;
	}

	boolean InsertSpacesToTab(int TSize) {
		int P = CP.Col, P1;

		if (iBFI(this, BFI_InsertKillBlock) == 1)
			if (CheckBlock())
				if (BlockKill() == false)
					return false;

		if (TSize <= 0)
			TSize = BFI(this, BFI_TabSize);

		P1 = NextTab(P, TSize);
		if (BFI(this, BFI_Insert) == 0) {
			if (CP.Col < LineLen())
				if (DelText(VToR(CP.Row), CP.Col, P1 - P) == false) return false;
		}
		if (InsText(VToR(CP.Row), CP.Col, P1 - P, 0) == false) return false;
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
		BinaryString PC = RLine(Row).Chars;

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

	/*#ifdef CONFIG_UNDOREDO
	boolean CanUndo() {
	    if (BFI(this, BFI_Undo) == 0) return false;
	    if (US.Num == 0 || US.UndoPtr == 0) return false;
	    return true;
	}

	boolean CanRedo() {
	    if (BFI(this, BFI_Undo) == 0) return false;
	    if (US.Num == 0 || US.UndoPtr == US.Num) return false;
	    return true;
	}
	#endif */

	boolean IsLineBlank(int Row) {
		ELine X = RLine(Row);
		int P;

		for (P = 0; P < X.getCount(); P++) 
			if (X.Chars.charAt(P) != ' ' && X.Chars.charAt(P) != 9)
				return false;
		return true;
	}

	/* TODO #ifdef CONFIG_WORDWRAP
	#define WFAIL(x) return 0	//do { puts(#x "\x7"); return -1; } while (0) 

	boolean DoWrap(int WrapAll) {
	    int L, Len, C, P, Ind;
	    ELine LP;
	    int Left = BFI(this, BFI_LeftMargin), Right = BFI(this, BFI_RightMargin);
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
	                    if (SetPos(Left, CP.Row) == false) WFAIL(1);
	            Ind = IndentLine(L, Left);
	            if (VToR(CP.Row) == L)
	                if (SetPos((CP.Col + Ind > 0) ? CP.Col + Ind : 0, CP.Row) == false) WFAIL(2);
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
	                if (JoinLine(L, Len + 1) == false) WFAIL(3);
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
	            if (SplitLine(L, C) == false) WFAIL(4);
	            IndentLine(L + 1, Left);
	            if (L < RCount - 2 && LineLen(L + 1) == Left) {
	                if (!IsLineBlank(L + 2)) {
	                    if (JoinLine(L + 1, Left) == false) WFAIL(5);
	                }
	            }
	            if (L == VToR(CP.Row) && CP.Col > C) {
	                if (SetPos(Left + CP.Col - C - 1, CP.Row + 1) == false) WFAIL(6);
	            }
	            NoChange = 0;
	            L++;
	            continue;
	        }
	        if (WrapAll == 0)
	            if (NoChangeX) {
	                //printf("\n\nBreak OUT = %d\n\x7", L);
	                break;
	            }
	        L++;
	        NoChangeX = NoChange;
	    }
	    if (WrapAll == 1)
	        if (SetPosR(Left,
	                    (L < RCount - 2) ? (L + 2) :
	                    (L < RCount - 1) ? (L + 1) :
	                    (RCount - 1)) == 0) WFAIL(7);
	    return true;
	}

	boolean WrapPara() {
	    while (VToR(CP.Row) < RCount - 1 && IsLineBlank(VToR(CP.Row)))
	        if (SetPos(CP.Col, CP.Row + 1) == false) return false;
	    return DoWrap(1);
	}
	#endif */

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
				return InsertString(RLine(L).Chars + P, Len);
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

	boolean ToggleAutoIndent() { TOGGLE(AutoIndent); }
	boolean ToggleInsert() { TOGGLE(Insert); }
	boolean ToggleExpandTabs() { TOGGLE_R(ExpandTabs); }
	boolean ToggleShowTabs() { TOGGLE_R(ShowTabs); }
	boolean ToggleUndo() { FreeUndo(); TOGGLE(Undo); }
	boolean ToggleReadOnly() { TOGGLE(ReadOnly); }
	boolean ToggleKeepBackups() { TOGGLE(KeepBackups); }
	boolean ToggleMatchCase() { TOGGLE(MatchCase); }
	boolean ToggleBackSpKillTab() { TOGGLE(BackSpKillTab); }
	boolean ToggleDeleteKillTab() { TOGGLE(DeleteKillTab); }
	boolean ToggleSpaceTabs() { TOGGLE(SpaceTabs); }
	boolean ToggleIndentWithTabs() { TOGGLE(IndentWithTabs); }
	boolean ToggleBackSpUnindents() { TOGGLE(BackSpUnindents); }
	boolean ToggleTrim() { TOGGLE(Trim); }
	boolean ToggleShowMarkers() { TOGGLE_R(ShowMarkers); }
	boolean ToggleHilitTags() { TOGGLE_R(HilitTags); }
	boolean ToggleShowBookmarks() { TOGGLE_R(ShowBookmarks); }

	boolean ToggleWordWrap() { 
		BFI(this, BFI_WordWrap) = (BFI(this, BFI_WordWrap) + 1) % 3;
		/*Msg(INFO,
	        "WordWrap is now %s.",
	        (BFI(this, BFI_WordWrap) == 2) ? "AUTO" :
	       (BFI(this, BFI_WordWrap) == 1) ? "ON" : "OFF"); */
		return true;
	}

	boolean SetLeftMargin() {
		BFI(this, BFI_LeftMargin) = CP.Col;
		Msg(S_INFO, "LeftMargin set to %d.", BFI(this, BFI_LeftMargin) + 1);
		return true;
	}

	boolean SetRightMargin() {
		BFI(this, BFI_RightMargin) = CP.Col;
		Msg(S_INFO, "RightMargin set to %d.", BFI(this, BFI_RightMargin) + 1);
		return true;
	}

	boolean ChangeMode(String AMode) {
		if (FindMode(AMode) != 0) {
			Mode = FindMode(AMode);
			Flags = Mode.Flags;
			HilitProc = 0;
			if (Mode && Mode.fColorize)
				HilitProc = GetHilitProc(Mode.fColorize.SyntaxParser);
			FullRedraw();
			return true;
		}
		Msg(S_ERROR, "Mode '%s' not found.", AMode);
		return false;
	}

	boolean ChangeKeys(String AMode) {
		if (FindMode(AMode) != 0) {
			Mode = FindMode(AMode);
			HilitProc = 0;
			if (Mode && Mode.fColorize)
				HilitProc = GetHilitProc(Mode.fColorize.SyntaxParser);
			FullRedraw();
			return true;
		}
		Msg(S_ERROR, "Mode '%s' not found.", AMode);
		return false;
	}

	boolean ChangeFlags(String AMode) {
		if (FindMode(AMode) != 0) {
			EMode XMode;
			XMode = FindMode(AMode);
			Flags = XMode.Flags;
			HilitProc = 0;
			if (Mode && Mode.fColorize)
				HilitProc = GetHilitProc(Mode.fColorize.SyntaxParser);
			FullRedraw();
			return true;
		}
		Msg(S_ERROR, "Mode '%s' not found.", AMode);
		return false;
	}






	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	///////////////////////////////////////////////////////////////////////////////
	//Block Commands                                                            //
	///////////////////////////////////////////////////////////////////////////////

	boolean SetBB(EPoint M) {
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

	boolean SetBE(EPoint M) {
		EPoint OldBE = BE;
		int MinL, MaxL;

		if (BE.Row == M.Row && BE.Col == M.Col) return true;
		/* TODO #ifdef CONFIG_UNDOREDO
		if (PushBlockData() == 0) return false;
		#endif */
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

	boolean BlockRedraw() {
		if (BB.Row == -1 || BE.Row == -1) return false;
		Draw(BB.Row, BE.Row);
		return true;
	}


	boolean BlockBegin() {
		EPoint X;

		X.Row = VToR(CP.Row);
		X.Col = CP.Col;
		CheckBlock();
		SetBB(X);
		return true;
	}

	boolean BlockEnd() {
		EPoint X;

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
			if (Config.SystemClipboard!=0)
				GetPMClip();
		} else
			SSBuffer.Clear();
			SSBuffer.BlockMode = BlockMode;
			BFI(SSBuffer, BFI_TabSize) = BFI(this, BFI_TabSize);
			BFI(SSBuffer, BFI_ExpandTabs) = BFI(this, BFI_ExpandTabs);
			BFI(SSBuffer, BFI_Undo) = 0;
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
					if (SSBuffer.InsLine(SL, 0) == 0) return false;
					if (SSBuffer.InsLineText(SL, 0, E.Col - B.Col, B.Col, RLine(L)) == 0) return false;
					if (SSBuffer.PadLine(SL, E.Col - B.Col) == 0) return false;
					SL++;
				}
				break;

			case bmStream:
				if (B.Row == E.Row) {
					if (SSBuffer.InsLine(SL, 0) == 0) return false;
					if (SSBuffer.InsLineText(SL, 0, E.Col - B.Col, B.Col, RLine(B.Row)) == 0) return false;
				} else {
					if (SSBuffer.InsLine(SL, 0) == 0) return false;
					if (SSBuffer.InsLineText(SL, 0, -1, B.Col, RLine(B.Row)) == 0) return false;
					SL++;
					for (L = B.Row + 1; L < E.Row; L++) {
						if (SSBuffer.InsLine(SL, 0) == 0) return false;
						if (SSBuffer.InsLineText(SL, 0, -1, 0, RLine(L)) == 0) return false;
						SL++;
					}
					if (SSBuffer.InsLine(SL, 0) == 0) return false;
					if (SSBuffer.InsLineText(SL, 0, E.Col, 0, RLine(E.Row)) == 0) return false;
				}
				if (Append && OldCount > 0)
					if (SSBuffer.JoinLine(OldCount - 1, 0) == 0)
						return false;
				break;
			}
			if (SystemClipboard)
				PutPMClip();
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
		EPoint B, E;
		int L, BL;

		if (Config.SystemClipboard!=0)
			GetPMClip();

		if (SSBuffer == 0) return false;
		if (SSBuffer.RCount == 0) return false;
		AutoExtend = 0;
		BFI(SSBuffer, BFI_TabSize) = BFI(this, BFI_TabSize);
		BFI(SSBuffer, BFI_ExpandTabs) = BFI(this, BFI_ExpandTabs);
		BFI(SSBuffer, BFI_Undo) = 0;
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

		/* TODO #ifdef CONFIG_UNDOREDO
		if (BFI(this, BFI_Undo) == 1) {
			if (PushULong(CP.Col) == false) return false;
			if (PushULong(CP.Row) == false) return false;
			if (PushUChar(ucPosition) == false) return false;
		}
		#endif */

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
		if (Config.SystemClipboard!=0)
			PutPMClip();
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

	boolean BlockWriteTo(String AFileName, int Append) {
		//int error = 0;
		EPoint B, E;
		int L;
		ELine LL;
		int A, Z;
		FILE fp;
		int bc = 0, lc = 0, oldc = 0;

		AutoExtend = 0;
		if (CheckBlock() == false) return false;
		if (RCount == 0) return false;
		B = BB;
		E = BE;
		Msg(S_INFO, "Writing %s...", AFileName);
		fp = fopen(AFileName, Append ? "ab" : "wb");
		if (fp == NULL) goto error;
		setvbuf(fp, FileBuffer, _IOFBF, sizeof(FileBuffer));
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
							if ((int)fwrite(LL.Chars + A, 1, Z - A, fp) != Z - A) {
								goto error;
							} else
								bc += Z - A;
						}
				}
				if (BFI(this, BFI_AddCR) == 1)
					if (fputc(13, fp) < 0) goto error;
					else
						bc++;
				if (BFI(this, BFI_AddLF) == 1)
					if (fputc(10, fp) < 0)
						goto error;
					else {
						bc++;
						lc++;
					}
				if (bc > 65536 + oldc) {
					Msg(S_INFO, "Writing %s, %d lines, %d bytes.", AFileName, lc, bc);
					oldc = bc;
				}
			}
		}
		fclose(fp);
		Msg(S_INFO, "Wrote %s, %d lines, %d bytes.", AFileName, lc, bc);
		return true;
		error:
			if(fp != NULL)
			{
				fclose(fp);
				unlink(AFileName);
			}
		View.MView.Win.Choice(GPC_ERROR, "Error", 1, "O&K", "Failed to write block to %s", AFileName);
		return false;
	}

	boolean BlockReadFrom(String AFileName, int blockMode) {
		EBuffer B;
		int savesys;
		int rc;

		if (FileExists(AFileName) == false) {
			View.MView.Win.Choice(GPC_ERROR, "Error", 1, "O&K", "File not found: %s", AFileName);
			return false;
		}

		B = new EBuffer(0, (EModel **)&SSBuffer, AFileName);
		if (B == 0) return false;
		B.SetFileName(AFileName, 0);
		if (B.Load() == false) {
			delete B;
			return false;
		}

		savesys = SystemClipboard;
		SystemClipboard = 0;

		switch (blockMode) {
		case bmColumn: rc = BlockPasteColumn(); break;
		case bmLine:   rc = BlockPasteLine(); break;
		default:
		case bmStream: rc = BlockPasteStream(); break;
		}

		SystemClipboard = savesys;

		if (rc == 0)
			return false;
		delete B;
		return true;
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

		AutoExtend = 0;
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
					int C2 = NextTab(C, BFI(this, BFI_TabSize));
					int N = BFI(this, BFI_TabSize) - (C2 - C);
					if (O - O1 < N)
						N = O - O1;
					if (N > 0) {
						if (DelChars(i, O - N, N) != 1)
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
				} else if (((C % BFI(this, BFI_TabSize)) == (BFI(this, BFI_TabSize) - 1)) &&
						(C - C1 > 0))
				{ // reached a tab and can put one
					int N = BFI(this, BFI_TabSize);
					if (O - O1 + 1 < N) {
						N = O - O1 + 1;
					} else if (O - O1 + 1 > N) {
						O1 = O - N + 1;
					}
					if (DelChars(i, O1, N) != 1)
						return false;
					if (InsChars(i, O1, 1, ""+tab) != 1)
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



}


