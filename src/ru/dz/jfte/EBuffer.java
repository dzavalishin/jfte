package ru.dz.jfte;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.Arrays;

import ru.dz.jfte.c.ArrayPtr;
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
	boolean ExtendGrab = false;
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
    char **WordList;
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


	int BFI(EBuffer y, int x)  { return (y.Flags.num[x & 0xFF]); }
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

	int Clear() {
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

		return 0;
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
		return 1;
	}
	#endif */

	int Modify() {
		if (BFI(this, BFI_ReadOnly)!=0) 
		{
			Msg(S_ERROR, "File is read-only.");
			return 0;
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
						return 0;
					}
				}
			}
			/* TODO #ifdef CONFIG_UNDOREDO
			if (BFI(this, BFI_Undo))
				if (PushUChar(ucModified) == 0) return 0;
			#endif */
		}
		Modified++;
		if (Modified == 0) Modified++;
		return 1;
	}

	int LoadRegion(EPoint A, int FH, int StripChar, int LineChar) {
		return 0;
	}

	int InsertLine(EPoint Pos, int ACount, String AChars) {
		if (InsLine(Pos.Row, 0, false) == 0) return 0;
		if (InsText(Pos.Row, Pos.Col, ACount, AChars, false) == 0) return 0;
		return 1;
	}


	int UpdateMark(EPoint M, int Type, int Row, int Col, int Rows, int Cols) {
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
		return 1;
	}

	int UpdateMarker(int Type, int Row, int Col, int Rows, int Cols) {
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
		return 1;
	}

	int ValidPos(EPoint Pos) {
		if ((Pos.Col >= 0) &&
				(Pos.Row >= 0) &&
				(Pos.Row < VCount))
			return 1;
		return 0;
	}

	int RValidPos(EPoint Pos) {
		if ((Pos.Col >= 0) &&
				(Pos.Row >= 0) &&
				(Pos.Row < RCount))
			return 1;
		return 0;
	}

	int AssertLine(int Row) {
		if (Row == RCount)
			if (InsLine(RCount, 0, false) == 0) return 0;
		return 1;
	}

	int SetFileName(String AFileName, String AMode) {
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
		return FileName != null?1:0;
	}

	int SetPos(int Col, int Row, int tabMode) {
		assert (Col >= 0 && Row >= 0 && Row < VCount);

		/* TODO #ifdef CONFIG_UNDOREDO
		if (BFI(this, BFI_Undo) == 1 && BFI(this, BFI_UndoMoves) == 1) {
			if (PushULong(CP.Col) == 0) return 0;
			if (PushULong(CP.Row) == 0) return 0;
			if (PushUChar(ucPosition) == 0) return 0;
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
				if (MoveTabStart() == 0) return 0;
			} else if (tabMode == tmRight) {
				if (MoveTabEnd() == 0) return 0;
			}
		}
		if (!ExtendGrab && !AutoExtend  && BFI(this, BFI_PersistentBlocks) == 0) {
			if (CheckBlock() == 1)
				if (BlockUnmark() == 0)
					return 0;
		}
		return 1;
	}

	int SetPosR(int Col, int Row, int tabMode) {
		assert (Row >= 0 && Row < RCount && Col >= 0);

		int L = RToV(Row);

		if (L == -1)
			if (ExposeRow(Row) == 0) return 0;

		L = RToV(Row);

		return SetPos(Col, L, tabMode);
	}

	int SetNearPos(int Col, int Row, int tabMode) {
		if (Row >= VCount) Row = VCount - 1;
		if (Row < 0) Row = 0;
		if (Col < 0) Col = 0;
		return SetPos(Col, Row, tabMode);
	}

	int SetNearPosR(int Col, int Row, int tabMode) {
		if (Row >= RCount) Row = RCount - 1;
		if (Row < 0) Row = 0;
		if (Col < 0) Col = 0;
		return SetPosR(Col, Row, tabMode);
	}

	int CenterPos(int Col, int Row, int tabMode) {
		assert(Row >= 0 && Row < VCount && Col >= 0);

		if (SetPos(Col, Row, tabMode) == 0) return 0;
		if (View != null && View.Model == this) {
			Row -= GetVPort().Rows / 2;
			if (Row < 0) Row = 0;
			Col -= GetVPort().Cols - 8;
			if (Col < 0) Col = 0;
			if (GetVPort().SetTop(Col, Row) == 0) return 0;
			GetVPort().ReCenter = 1;
		}
		return 1;
	}

	int CenterPosR(int Col, int Row, int tabMode) {
		int L;

		assert(Row >= 0 && Row < RCount && Col >= 0);

		L = RToV(Row);
		if (L == -1)
			if (ExposeRow(Row) == 0) return 0;
		L = RToV(Row);
		return CenterPos(Col, L, tabMode);
	}

	int CenterNearPos(int Col, int Row, int tabMode) {
		if (Row >= VCount) Row = VCount - 1;
		if (Row < 0) Row = 0;
		if (Col < 0) Col = 0;
		return CenterPos(Col, Row, tabMode);
	}

	int CenterNearPosR(int Col, int Row, int tabMode) {
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

	int DelLine(int Row, boolean DoMark) {
		int VLine;
		int GapSize;
		//   printf("DelLine: %d\n", Row);
		if (Row < 0) return 0;
		if (Row >= RCount) return 0;
		if (Modify() == 0) return 0;

		VLine = RToV(Row);
		if (VLine == -1)
			if (ExposeRow(Row) == 0) return 0;
		VLine = RToV(Row);
		assert(VLine != -1);

		if (FindFold(Row) != -1) {
			if (FoldDestroy(Row) == 0) return 0;
		}

		VLine = RToV(Row);
		assert(VLine != -1);

		/* TODO #ifdef CONFIG_UNDOREDO
		if (BFI(this, BFI_Undo) == 1) {
			if (PushUData(RLine(Row).Chars, RLine(Row).getCount()) == 0) return 0;
			if (PushULong(RLine(Row).getCount()) == 0) return 0;
			if (PushULong(Row) == 0) return 0;
			if (PushUChar(ucDelLine) == 0) return 0;
		}
		#endif */
		if (DoMark)
			UpdateMarker(umDelete, Row, 0, 1, 0);
		//puts("Here");

		Draw(Row, -1);
		Hilit(Row);
		assert(RAllocated >= RCount);
		if (RGap != Row) 
			if (MoveRGap(Row) == 0) return 0;

		GapSize = RAllocated - RCount;

		//delete LL[RGap + GapSize];
		LL[RGap + GapSize] = null;
		RCount--;
		GapSize++;
		if (RAllocated - RAllocated / 2 > RCount) {
			//memmove(LL + RGap + GapSize - RAllocated / 3, LL + RGap + GapSize, sizeof(ELine) * (RCount - RGap));
			System.arraycopy(LL, RGap + GapSize, LL, RGap + GapSize - RAllocated / 3, RCount - RGap);
			if (Allocate(RAllocated - RAllocated / 3) == 0) return 0;
		}

		assert(VAllocated >= VCount);
		if (VGap != VLine)
			if (MoveVGap(VLine) == 0) return 0;
		GapSize = VAllocated - VCount;
		VV[VGap + GapSize] = 0;
		VCount--;
		GapSize++;
		if (VAllocated - VAllocated / 2 > VCount) {
			//memmove(VV + VGap + GapSize - VAllocated / 3,VV + VGap + GapSize,sizeof(VV[0]) * (VCount - VGap));
			System.arraycopy(VV, VGap + GapSize, VV, VGap + GapSize - VAllocated / 3, VCount - VGap);
			if (AllocVis(VAllocated - VAllocated / 3) == 0) return 0;
		}
		return 1;
	}

	int InsLine(int Row, int DoAppend, boolean DoMark) {
		//ELine L;
		int VLine = -1;

		//    printf("InsLine: %d\n", Row);

		if (Row < 0) return 0;
		if (Row > RCount) return 0;
		if (Modify() == 0) return 0;
		if (DoAppend!=0) {
			Row++;
		}
		if (Row < RCount) {
			VLine = RToV(Row);
			if (VLine == -1)
				if (ExposeRow(Row) == 0) return 0;
			VLine = RToV(Row);
			assert(VLine != -1);
		} else {
			VLine = VCount;
		}
		ELine L = new ELine("");
		//if (L == 0) return 0;
		/* TODO #ifdef CONFIG_UNDOREDO
		if (BFI(this, BFI_Undo) == 1) {
			if (PushULong(Row) == 0) return 0;
			if (PushUChar(ucInsLine) == 0) return 0;
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
			if (Allocate(RCount ? (RCount * 2) : 1) == 0) return 0;
			memmove(LL + RAllocated - (RCount - RGap),
					LL + RGap,
					sizeof(ELine) * (RCount - RGap));
		}
		if (RGap != Row)
			if (MoveRGap(Row) == 0) return 0;
		LL[RGap] = L;
		RGap++;
		RCount++;
		//    printf("-- %d G:C:A :: %d - %d - %d\n", Row, RGap, RCount, RAllocated);

		assert(VCount <= VAllocated);
		if (VCount == VAllocated) {
			if (AllocVis(VCount ? (VCount * 2) : 1) == 0) return 0;
			memmove(VV + VAllocated - (VCount - VGap),
					VV + VGap,
					sizeof(VV[0]) * (VCount - VGap));
		}
		if (VGap != VLine)
			if (MoveVGap(VLine) == 0) return 0;
		VV[VGap] = Row - VGap;
		VGap++;
		VCount++;

		/*    if (AllocVis(VCount + 1) == 0) return 0;
   memmove(VV + VLine + 1, VV + VLine, sizeof(VV[0]) * (VCount - VLine));
   VCount++;
   Vis(VLine, Row - VLine);*/
		return 1;
	}

	int DelChars(int Row, int Ofs, int ACount) {
		ELine L;

		//   printf("DelChars: %d:%d %d\n", Row, Ofs, ACount);
		if (Row < 0) return 0;
		if (Row >= RCount) return 0;
		L = RLine(Row);

		if (Ofs < 0) return 0;
		if (Ofs >= L.getCount()) return 0;
		if (Ofs + ACount >= L.getCount())
			ACount = L.getCount() - Ofs;
		if (ACount == 0) return 1;

		if (Modify() == 0) return 0;

		/* TODO #ifdef CONFIG_UNDOREDO
		if (BFI(this, BFI_Undo) == 1) {
			if (PushUData(L.Chars + Ofs, ACount) == 0) return 0;
			if (PushULong(ACount) == 0) return 0;
			if (PushULong(Ofs) == 0) return 0;
			if (PushULong(Row) == 0) return 0;
			if (PushUChar(ucDelChars) == 0) return 0;
		}
		#endif */

		if (L.getCount() > Ofs + ACount)
			memmove(L.Chars + Ofs, L.Chars + Ofs + ACount, L.getCount() - Ofs - ACount);
		L.getCount() -= ACount;
		if (L.Allocate(L.getCount()) == 0) return 0;
		Draw(Row, Row);
		Hilit(Row);
		//   printf("OK\n");
		return 1;
	}

	int InsChars(int Row, int Ofs, int ACount, String Buffer) {
		//   printf("InsChars: %d:%d %d\n", Row, Ofs, ACount);

		assert(Row >= 0 && Row < RCount && Ofs >= 0);
		ELine L = RLine(Row);

		if (Ofs < 0) return 0;
		if (Ofs > L.getCount()) return 0;
		if (ACount == 0) return 1;

		if (Modify() == 0) return 0;

		/* TODO #ifdef CONFIG_UNDOREDO
		if (BFI(this, BFI_Undo) == 1) {
			if (PushULong(Row) == 0) return 0;
			if (PushULong(Ofs) == 0) return 0;
			if (PushULong(ACount) == 0) return 0;
			if (PushUChar(ucInsChars) == 0) return 0;
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
		return 1;
	}

	int UnTabPoint(int Row, int Col) {
		ELine L;
		int Ofs, Pos, TPos;

		assert(Row >= 0 && Row < RCount && Col >= 0);
		L = RLine(Row);
		Ofs = CharOffset(L, Col);
		if (Ofs >= L.getCount())
			return 1;
		if (L.charAt(Ofs) != '\t')
			return 1;
		Pos = ScreenPos(L, Ofs);
		if (Pos < Col) {
			TPos = NextTab(Pos, BFI(this, BFI_TabSize));
			if (DelChars(Row, Ofs, 1) != 1)
				return 0;
			if (InsChars(Row, Ofs, TPos - Pos, null) != 1)
				return 0;
		}
		return 1;
	}

	int ChgChars(int Row, int Ofs, int ACount, String Buffer) {
		ELine L;

		assert(Row >= 0 && Row < RCount && Ofs >= 0);
		L = RLine(Row);

		if (Ofs < 0) return 0;
		if (Ofs > L.getCount()) return 0;
		if (ACount == 0) return 1;

		if (Modify() == 0) return 0;

		/* TODO #ifdef CONFIG_UNDOREDO
		if (BFI(this, BFI_Undo) == 1) {
			if (PushUData(L.Chars + Ofs, ACount) == 0) return 0;
			if (PushULong(ACount) == 0) return 0;
			if (PushULong(Ofs) == 0) return 0;
			if (PushULong(Row) == 0) return 0;
			if (PushUChar(ucDelChars) == 0) return 0;
			if (PushULong(Row) == 0) return 0;
			if (PushULong(Ofs) == 0) return 0;
			if (PushULong(ACount) == 0) return 0;
			if (PushUChar(ucInsChars) == 0) return 0;
		}
		#endif */
		Hilit(Row);
		Draw(Row, Row);
		return 1;
	}

	int DelText(int Row, int Col, int ACount, boolean DoMark) {
		int L, B, C;

		//   printf("DelTExt: %d:%d %d\n", Row, Col, ACount);

		assert(Row >= 0 && Row < RCount && Col >= 0);
		if (Modify() == 0) return 0;

		if (ACount == 0) return 1;
		L = LineLen(Row);
		if (Col >= L)
			return 1;
		if (ACount == -1 || ACount + Col > L)
			ACount = L - Col;
		if (UnTabPoint(Row, Col) == 0)
			return 0;
		if (UnTabPoint(Row, Col + ACount) == 0)
			return 0;
		B = CharOffset(RLine(Row), Col);
		C = CharOffset(RLine(Row), Col + ACount);
		if ((ACount > 0) && (B != -1) && (C != -1)) {
			if (DelChars(Row, B, C - B) == 0) return 0;
			if (DoMark) UpdateMarker(umDelete, Row, Col, 0, ACount);
		}
		//   printf("OK\n");
		return 1;
	}

	int InsText(int Row, int Col, int ACount, String ABuffer, boolean DoMark) {
		int B, L;

		//   printf("InsText: %d:%d %d\n", Row, Col, ACount);
		assert(Row >= 0 && Row < RCount && Col >= 0 && ACount >= 0);
		if (ACount == 0) return 1;
		if (Modify() == 0) return 0;

		if (DoMark) UpdateMarker(umInsert, Row, Col, 0, ACount);
		L = LineLen(Row);
		if (L < Col) {
			if (InsChars(Row, RLine(Row).getCount(), Col - L, null) == 0)
				return 0;
		} else
			if (UnTabPoint(Row, Col) == 0) return 0;
		B = CharOffset(RLine(Row), Col);
		if (InsChars(Row, B, ACount, ABuffer) == 0) return 0;
		//   printf("OK\n");
		return 1;
	}

	int PadLine(int Row, int Length) {
		int L;

		L = LineLen(Row);
		if (L < Length)
			if (InsChars(Row, RLine(Row).getCount(), Length - L, null) == 0)
				return 0;
		return 1;
	}

	int InsLineText(int Row, int Col, int ACount, int LCol, ELine Line) {
		int Ofs, Pos, TPos, C, B, L;

		//fprintf(stderr, "\n\nInsLineText: %d:%d %d %d", Row, Col, ACount, LCol);
		assert(Row >= 0 && Row < RCount && Col >= 0 && LCol >= 0);
		if (BFI(this, BFI_ReadOnly) == 1)
			return 0;

		L = ScreenPos(Line, Line.getCount());
		if (LCol >= L) return 1;
		if (ACount == -1) ACount = L - LCol;
		if (ACount + LCol > L) ACount = L - LCol;
		if (ACount == 0) return 1;
		assert(ACount > 0);

		B = Ofs = CharOffset(Line, LCol);
		if (Ofs < Line.getCount() && Line.charAt(Ofs) == '\t') {
			Pos = ScreenPos(Line, Ofs);
			if (Pos < LCol) {
				TPos = NextTab(Pos, BFI(this, BFI_TabSize));
				if (InsText(Row, Col, TPos - LCol, null, false) == 0)
					return 0;
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
				if (InsText(Row, Col, LCol + ACount - Pos, null, false) == 0)
					return 0;
			}
		}
		//fprintf(stderr, "B = %d, C = %d\n", B, C);
		C -= B;
		if (C <= 0) return 1;
		if (InsText(Row, Col, C, Line.Chars + B) == 0) return 0;
		//   printf("OK\n");
		return 1;
	}

	int SplitLine(int Row, int Col) {
		int VL;

		assert(Row >= 0 && Row < RCount && Col >= 0);

		if (BFI(this, BFI_ReadOnly) == 1) return 0;

		VL = RToV(Row);
		if (VL == -1) 
			if (ExposeRow(Row) == 0) return 0;
		if (Row > 0) {
			VL = RToV(Row - 1);
			if (VL == -1)
				if (ExposeRow(Row - 1) == 0) return 0;
		}
		VL = RToV(Row);
		assert(VL != -1);
		if (Col == 0) {
			if (InsLine(Row, 0, true) == 0) return 0;
		} else {
			UpdateMarker(umSplitLine, Row, Col, 0, 0);
			if (InsLine(Row, 1, false) == 0) return 0;
			RLine(Row).StateE = (short)((Row > 0) ? RLine(Row - 1).StateE : 0);
			if (Col < LineLen(Row)) {
				int P, L;
				//if (RLine(Row).ExpandTabs(Col, -2, &Flags) == 0) return 0;
				if (UnTabPoint(Row, Col) != 1)
					return 0;

				P = CharOffset(RLine(Row), Col);
				L = LineLen(Row);

				if (InsText(Row + 1, 0, RLine(Row).getCount() - P, RLine(Row).Chars + P, 0) == 0) return 0;
				if (DelText(Row, Col, L - Col, false) == 0) return 0;
			}
		}
		Draw(Row, -1);
		Hilit(Row);
		return 1;
	}

	int JoinLine(int Row, int Col) {
		int Len, VLine;

		if (BFI(this, BFI_ReadOnly) == 1) return 0;
		if (Row < 0 || Row >= RCount - 1) return 0;
		if (Col < 0) return 0;
		Len = LineLen(Row);
		if (Col < Len) Col = Len;
		VLine = RToV(Row);
		if (VLine == -1) {
			if (ExposeRow(Row) == 0) return 0;
			if (ExposeRow(Row + 1) == 0) return 0;
		}
		VLine = RToV(Row);
		if (Col == 0 && RLine(Row).getCount() == 0) {
			if (DelLine(Row, true) == 0) return 0;
		} else {
			if (InsText(Row, Col, RLine(Row + 1).getCount(), RLine(Row + 1).Chars, false) == 0) return 0;
			if (DelLine(Row + 1, false) == 0) return 0;
			UpdateMarker(umJoinLine, Row, Col, 0, 0);
		}
		Draw(Row, -1);
		Hilit(Row);
		return 1;
	}


































	int ScreenPos(ELine L, int Offset) {
		boolean ExpandTabs = BFI(this, BFI_ExpandTabs) != 0;
		int TabSize = BFI(this, BFI_TabSize);

		if (!ExpandTabs) {
			return Offset;
		} else {
			//char *p = L.Chars;
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
			//char *p = L.Chars;
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


	int Allocate(int ACount) {
		//ELine [] L = (ELine ) realloc(LL, sizeof(ELine) * (ACount + 1));

		LL = new ELine [ACount];
		Arrays.fill(LL, null);

		RAllocated = ACount;

		return 1;
	}

	int MoveRGap(int RPos) {
		int GapSize = RAllocated - RCount;

		if (RGap == RPos) return 1;
		if (RPos < 0 || RPos > RCount) return 0;

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
		return 1;
	}

	int AllocVis(int ACount) {
		//int []V;

		//V = (int []) realloc(VV, sizeof(int) * (ACount + 1));
		//if (V == 0 && ACount != 0) return 0;
		VAllocated = ACount;
		//VV = V;
		VV = Arrays.copyOf(VV, ACount);
		return 1;
	}

	int MoveVGap(int VPos) {
		int GapSize = VAllocated - VCount;

		if (VGap == VPos) return 1;
		if (VPos < 0 || VPos > VCount) return 0;

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
		return 1;
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






























	int ExposeRow(int Row) { /*FOLD00*/
		int V;
		int f, level, oldlevel = 100;

		//DumpFold();

		assert(Row >= 0 && Row < RCount); // range

		V = RToV(Row);
		if (V != -1) return 1; // already exposed

		f = FindNearFold(Row);
		assert(f != -1); // if not visible, must be folded

		while (f >= 0) {
			level = FF[f].level;
			if (level < oldlevel) {
				if (FF[f].open == 0) {
					//	                printf("opening fold %d\n", f);
					if (FoldOpen(FF[f].line) == 0) return 0;
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
		return 1;
	}





	int GetMap(int Row, int []StateLen, byte /*hsState*/ [][]StateMap) {
		byte/*hlState*/ State = 0;

		Rehilit(Row);

		StateLen[0] = LineChars(Row);
		if (Row > 0) State = RLine(Row - 1).StateE;
		if (StateLen[0] > 0) {
			ELine L = RLine(Row);
			int [] ECol = {0};

			StateMap[0] = new byte[StateLen[0]];
			if (StateMap[0] == null) return 0;

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
			//if (*StateMap == 0) return 0;
			StateMap[0][0] = (byte)(State & 0xFF);
		}
		return 1;
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
		byte /*hlState*/ State;
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
		byte /*hlState*/ State;
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
				HilitX = 1;
				L.StateE = State;
			}
			if (BFI(this, BFI_ShowMarkers)) {
				MoveChar(B, ECol - C, W, ConGetDrawChar((Row == RCount - 1) ? DCH_EOF : DCH_EOL), hcPlain_Markers, 1);
				ECol += 1;
			}
			if (Row < RCount) {
				int f;
				int Folded = 0;
				//static char fold[20];
				int l;

				f = FindFold(Row);
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
				}
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
	            char *Name;
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
				B.MoveChar( 0, W, ConGetDrawChar(DCH_END), hcPlain_Markers, W);
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
				int scrollJumpX = Math.min(ScrollJumpX, W.Cols / 2);
				int scrollJumpY = Math.min(ScrollJumpY, W.Rows / 2);
				int scrollBorderX = Math.min(ScrollBorderX, W.Cols / 2);
				int scrollBorderY = Math.min(ScrollBorderY, W.Rows / 2);

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
					int NumChars = RLine(ActLine).Count;
					//            int NumColumns = ScreenPos(Line(CurLine), NumChars);
					String fName = FileName;
					char CurCh = 0xFF;
					int lf = fName.length();
					String CCharStr; //[20] = "";

					if (lf > 34) fName += lf - 34;

					if (CurPos < NumChars) {
						CurCh = VLine(CurLine).Chars[CurPos];
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
																		((BlockMode == bmStream) ? 'S' :
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

	int GetHilitWord(int len, String str, int /*ChColor*/ []clr, int IgnCase) {
		//char *p;

		if (Mode == null || Mode.fColorize == 0)
			return 0;

		if (len >= CK_MAXLEN)
			return 0;

		/* TODO #ifdef CONFIG_WORD_HILIT
	    {
	        char s[CK_MAXLEN + 1];
	        s[CK_MAXLEN] = 0;
	        memcpy(s, str, len);
	        s[len] = 0;
	        if (HilitFindWord(s)) {
	            clr = hcPlain_HilitWord;
	            return 1;
	        }
	    }
	#endif */
		if (len < 1) return 0;
		p = Mode.fColorize.Keywords.key[len];
		if (IgnCase) {
			while (p && p[0]) {
				if (strnicmp(p, str, len) == 0) {
					clr = p[len];
					return 1;
				}
				p += len + 1;
			}
		} else {
			while (p && p[0]) {
				if (memcmp(p, str, len) == 0) {
					clr = p[len];
					return 1;
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
				return 1;
			}
		}

		return 0;
	}















	int FindStr(String Data, int Len, int Options) {
		int LLen, Start, End;
		int C, L;
		ELine X;
		//char *P;

		boolean osBack = 0 != (Options & SEARCH_BACK);
		boolean osWordBeg = 0 != (Options & SEARCH_WORDBEG);
		boolean osWordEnd = 0 != (Options & SEARCH_WORDEND);
		boolean osNCase = 0 != (Options & SEARCH_NCASE);

		if(0 != (Options & SEARCH_RE))
			return 0;
		if (Len <= 0)
			return 0;

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
					if (L == 0) return 0;
					L--;
					X = RLine(L);
					C = X.getCount();
				}
			} else {
				C += CC;
				if (C >= X.getCount()) {
					C = 0;
					L++;
					if (L == RCount) return 0;
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
								|| (BitOps.WGETBIT(Flags.WordChars, X.Chars.charAt(C - 1) /*P[C - 1]*/) == 0))
						&&
						(!osWordEnd
								|| (C + Len >= End)
								|| (BitOps.WGETBIT(Flags.WordChars, X.Chars.charAt(C + Len)/*P[C + Len]*/) == 0))
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
					return 1;
				}
				if (Options & SEARCH_BACK) C--; else C++;
			}
			if (Options & SEARCH_BACK) {
				L--;
				if (L >= 0)
					C = RLine(L).Count;
			} else {
				C = 0;
				L++;
			}
		}
		//SetPos(OC, OL);
		return 0;
	}


	
	
	
	
	
	
	
	//#ifdef CONFIG_OBJ_ROUTINE
	int ScanForRoutines() {
		return 0; // TODO 
		
		/*
	    RxNode regx;
	    int line;
	    RxMatchRes res;

	    if (BFS(this, BFS_RoutineRegexp) == null) {
	        View.MView.Win.Choice(GPC_ERROR, "Error", 1, "O&K", "No routine regexp.");
	        return 0;
	    }
	    regx = RxCompile(BFS(this, BFS_RoutineRegexp));
	    if (regx == 0) {
	        View.MView.Win.Choice(GPC_ERROR, "Error", 1, "O&K", "Failed to compile regexp '%s'", BFS(this, BFS_RoutineRegexp));
	        return 0;
	    }

	    if (rlst.Lines) {
	        free(rlst.Lines);
	        rlst.Lines = 0;
	    }
	    rlst.Lines = 0;
	    rlst.Count = 0;

	    Msg(S_BUSY, "Matching %s", BFS(this, BFS_RoutineRegexp));
	    for (line = 0; line < RCount; line++) {
	    	ELine L = RLine(line);
	        if (RxExec(regx, L.Chars, L.Count, L.Chars, res) == 1) {
	            rlst.Count++;
	            //rlst.Lines =  realloc((void *) rlst.Lines, sizeof(int) * (rlst.Count | 0x1F));
	            rlst.Lines[rlst.Count - 1] = line;
	            Msg(S_BUSY, "Routines: %d", rlst.Count);
	        }
	    }
	    //RxFree(regx);
	    return 1;
	    */
	}
	//#endif

	
	
	
}


