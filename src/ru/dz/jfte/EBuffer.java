package ru.dz.jfte;

public class EBuffer extends EModel 
{

	EBufferFlags Flags;
	EMode Mode;
	int BlockMode;
	int ExtendGrab;
	int AutoExtend;
	boolean Loaded = false;

	// TODO UndoStack US;

	//struct stat FileStatus;
	int FileOk;
	boolean Loading = false;

	int RAllocated;   // text line allocation
	int RGap;
	int RCount;
	ELine [] LL;

	int VAllocated;   // visible lines
	int VGap;
	int VCount;
	int []VV;

	// TODO folds
	//int FCount;
	//EFold FF;

	EPoint Match;
	int MatchLen;
	int MatchCount;
	RxMatchRes MatchRes;

	/* TODO /* TODO #ifdef CONFIG_BOOKMARKS
    int BMCount;
    EBookmark *BMarks;
#endif */

/* TODO #ifdef CONFIG_OBJ_ROUTINE
    RoutineList rlst;
    RoutineView *Routines;
#endif */ 

	int MinRedraw, MaxRedraw;
	int RedrawToEos;

	/* TODO /* TODO #ifdef CONFIG_WORD_HILIT
    char **WordList;
    int WordCount;
#endif */
/* TODO #ifdef CONFIG_SYNTAX_HILIT
    SyntaxProc HilitProc;
    int StartHilit, EndHilit;
#endif */ 





	final static int   ccUp       = 0;
	final static int   ccDown     =1;
	final static int   ccToggle   =2;

	static SearchReplaceOptions LSearch;
	static int suspendLoads;
	static EBuffer SSBuffer = null; // scrap buffer (clipboard)

	///////////////////////////////////////////////////////////////////////////////

	EBuffer(int createFlags, EModel []ARoot, String AName)
	//:EModel(createFlags, ARoot), TP(0,0), CP(0,0), BB(-1,-1), BE(-1,-1),
	//PrevPos(-1, -1), SavedPos(-1, -1), Match(-1, -1)
	
	{
		super(createFlags, ARoot);

		TP(0,0), CP(0,0), BB(-1,-1), BE(-1,-1),
		PrevPos(-1, -1), SavedPos(-1, -1), Match(-1, -1)
		
		FileName = 0;
		LL = 0;
		VV = 0;
		FF = 0;
		RGap = RCount = RAllocated = 0;
		VGap = VCount = VAllocated = 0;
		FCount = 0;
		Modified = 0;
		BlockMode = bmStream;
		ExtendGrab = 0;
		AutoExtend = 0;
		MatchLen = MatchCount = 0;
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
		rlst.Count = 0;
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
		BFI(this, BFI_Undo) = 0;
		BFI(this, BFI_ReadOnly) = 0;
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
		#endif */ */
		InsertLine(CP,0,0); /* there should always be at least one line in the edit buffer */
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
		rlst.Count = 0;
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
		FCount = 0;
		FF = null;

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
		if (BFI(this, BFI_ReadOnly)) {
			Msg(S_ERROR, "File is read-only.");
			return 0;
		}
		if (Modified == 0) {
			struct stat StatBuf;

			if ((FileName != 0) && FileOk && (stat(FileName, &StatBuf) == 0)) {
				if (FileStatus.st_size != StatBuf.st_size ||
						FileStatus.st_mtime != StatBuf.st_mtime)
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

	int InsertLine(EPoint Pos, int ACount, char *AChars) {
		if (InsLine(Pos.Row, 0) == 0) return 0;
		if (InsText(Pos.Row, Pos.Col, ACount, AChars) == 0) return 0;
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
				if (Cols) {
					if (M.Row == Row)
						if (M.Col >= Col)
							M.Col += Cols;
				}
				if (Rows) {
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
				if (Cols) {
					if (M.Row == Row)
						if (M.Col >= Col)
							if (M.Col < Col + Cols)
								M.Col = Col;
							else
								M.Col -= Cols;
				}
				if (Rows) {
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
		for (int i = 0; i < rlst.Count && rlst.Lines; i++) {
			EPoint M;

			M.Col = 0;
			M.Row = rlst.Lines[i];
			UpdateMark(M, Type, Row, Col, Rows, Cols);
			rlst.Lines[i] = M.Row;
		}
		#endif */

		for (int f = 0; f < FCount; f++) {
			EPoint M;

			M.Col = 0;
			M.Row = FF[f].line;
			UpdateMark(M, Type, Row, Col, Rows, Cols);
			FF[f].line = M.Row;
		}

		/* TODO #ifdef CONFIG_BOOKMARKS
		for (int b = 0; b < BMCount; b++)
			UpdateMark(BMarks[b].BM, Type, Row, Col, Rows, Cols);
		#endif */

		if (OldBB.Row != BB.Row) {
			int MinL = Min(OldBB.Row, BB.Row);
			int MaxL = Max(OldBB.Row, BB.Row);
			if (MinL != -1 && MaxL != -1)  
				Draw(MinL, MaxL);
		}
		if (OldBE.Row != BE.Row) {
			int MinL = Min(OldBE.Row, BE.Row);
			int MaxL = Max(OldBE.Row, BE.Row);
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
			if (InsLine(RCount, 0) == 0) return 0;
		return 1;
	}

	int SetFileName(String AFileName, String AMode) {
		FileOk = 0;

		FileName = AFileName;
		Mode = 0;
		if (AMode)
			Mode = FindMode(AMode);
		if (Mode == 0)
			Mode = GetModeForName(AFileName);
		assert(Mode != 0);
		Flags = (Mode.Flags);
		/* TODO #ifdef CONFIG_SYNTAX_HILIT
		HilitProc = 0;
		if (Mode && Mode.fColorize)
			HilitProc = GetHilitProc(Mode.fColorize.SyntaxParser);
		#endif */
		UpdateTitle();
		return FileName?1:0;
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
			AutoExtend = 1;
		}
		PrevPos = CP;
		PrevPos.Row = (CP.Row < VCount) ? VToR(CP.Row) : (CP.Row - VCount + RCount);
		CP.Row = Row;
		CP.Col = Col;
		if (AutoExtend) {
			BlockExtendEnd();
			AutoExtend = 1;
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
		if (ExtendGrab == 0 && AutoExtend == 0 && BFI(this, BFI_PersistentBlocks) == 0) {
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
		if (View && View.Model == this) {
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
		return ScreenPos(L, L.Count);
	}

	int LineChars(int Row) {
		assert(Row >= 0 && Row < RCount);
		return RLine(Row).Count;
	}

	int DelLine(int Row, int DoMark) {
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
			if (PushUData(RLine(Row).Chars, RLine(Row).Count) == 0) return 0;
			if (PushULong(RLine(Row).Count) == 0) return 0;
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

		delete LL[RGap + GapSize];
		LL[RGap + GapSize] = 0;
		RCount--;
		GapSize++;
		if (RAllocated - RAllocated / 2 > RCount) {
			memmove(LL + RGap + GapSize - RAllocated / 3,
					LL + RGap + GapSize,
					sizeof(ELine) * (RCount - RGap));
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
			memmove(VV + VGap + GapSize - VAllocated / 3,
					VV + VGap + GapSize,
					sizeof(VV[0]) * (VCount - VGap));
			if (AllocVis(VAllocated - VAllocated / 3) == 0) return 0;
		}
		return 1;
	}

	int InsLine(int Row, int DoAppend, int DoMark) {
		ELine L;
		int VLine = -1;

		//    printf("InsLine: %d\n", Row);

		if (Row < 0) return 0;
		if (Row > RCount) return 0;
		if (Modify() == 0) return 0;
		if (DoAppend) {
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
		L = new ELine(0, (char *)0);
		if (L == 0) return 0;
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
		if (Ofs >= L.Count) return 0;
		if (Ofs + ACount >= L.Count)
			ACount = L.Count - Ofs;
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

		if (L.Count > Ofs + ACount)
			memmove(L.Chars + Ofs, L.Chars + Ofs + ACount, L.Count - Ofs - ACount);
		L.Count -= ACount;
		if (L.Allocate(L.Count) == 0) return 0;
		Draw(Row, Row);
		Hilit(Row);
		//   printf("OK\n");
		return 1;
	}

	int InsChars(int Row, int Ofs, int ACount, char *Buffer) {
		ELine L;

		//   printf("InsChars: %d:%d %d\n", Row, Ofs, ACount);

		assert(Row >= 0 && Row < RCount && Ofs >= 0);
		L = RLine(Row);

		if (Ofs < 0) return 0;
		if (Ofs > L.Count) return 0;
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
		if (L.Allocate(L.Count + ACount) == 0) return 0;
		if (L.Count > Ofs)
			memmove(L.Chars + Ofs + ACount, L.Chars + Ofs, L.Count - Ofs);
		if (Buffer == 0) 
			memset(L.Chars + Ofs, ' ', ACount);
		else
			memmove(L.Chars + Ofs, Buffer, ACount);
		L.Count += ACount;
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
		if (Ofs >= L.Count)
			return 1;
		if (L.Chars[Ofs] != '\t')
			return 1;
		Pos = ScreenPos(L, Ofs);
		if (Pos < Col) {
			TPos = NextTab(Pos, BFI(this, BFI_TabSize));
			if (DelChars(Row, Ofs, 1) != 1)
				return 0;
			if (InsChars(Row, Ofs, TPos - Pos, 0) != 1)
				return 0;
		}
		return 1;
	}

	int ChgChars(int Row, int Ofs, int ACount, char * /*Buffer*/) {
		ELine L;

		assert(Row >= 0 && Row < RCount && Ofs >= 0);
		L = RLine(Row);

		if (Ofs < 0) return 0;
		if (Ofs > L.Count) return 0;
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

	int DelText(int Row, int Col, int ACount, int DoMark) {
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

	int InsText(int Row, int Col, int ACount, char *ABuffer, int DoMark) {
		int B, L;

		//   printf("InsText: %d:%d %d\n", Row, Col, ACount);
		assert(Row >= 0 && Row < RCount && Col >= 0 && ACount >= 0);
		if (ACount == 0) return 1;
		if (Modify() == 0) return 0;

		if (DoMark) UpdateMarker(umInsert, Row, Col, 0, ACount);
		L = LineLen(Row);
		if (L < Col) {
			if (InsChars(Row, RLine(Row).Count, Col - L, 0) == 0)
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
			if (InsChars(Row, RLine(Row).Count, Length - L, 0) == 0)
				return 0;
		return 1;
	}

	int InsLineText(int Row, int Col, int ACount, int LCol, ELine Line) {
		int Ofs, Pos, TPos, C, B, L;

		//fprintf(stderr, "\n\nInsLineText: %d:%d %d %d", Row, Col, ACount, LCol);
		assert(Row >= 0 && Row < RCount && Col >= 0 && LCol >= 0);
		if (BFI(this, BFI_ReadOnly) == 1)
			return 0;

		L = ScreenPos(Line, Line.Count);
		if (LCol >= L) return 1;
		if (ACount == -1) ACount = L - LCol;
		if (ACount + LCol > L) ACount = L - LCol;
		if (ACount == 0) return 1;
		assert(ACount > 0);

		B = Ofs = CharOffset(Line, LCol);
		if (Ofs < Line.Count && Line.Chars[Ofs] == '\t') {
			Pos = ScreenPos(Line, Ofs);
			if (Pos < LCol) {
				TPos = NextTab(Pos, BFI(this, BFI_TabSize));
				if (InsText(Row, Col, TPos - LCol, 0) == 0)
					return 0;
				Col += TPos - LCol;
				ACount -= TPos - LCol;
				LCol = TPos;
				B++;
			}
		}
		C = Ofs = CharOffset(Line, LCol + ACount);
		if (Ofs < Line.Count && Line.Chars[Ofs] == '\t') {
			Pos = ScreenPos(Line, Ofs);
			if (Pos < LCol + ACount) {
				if (InsText(Row, Col, LCol + ACount - Pos, 0) == 0)
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
			if (InsLine(Row, 0, 1) == 0) return 0;
		} else {
			UpdateMarker(umSplitLine, Row, Col, 0, 0);
			if (InsLine(Row, 1, 0) == 0) return 0;
			RLine(Row).StateE = short((Row > 0) ? RLine(Row - 1).StateE : 0);
			if (Col < LineLen(Row)) {
				int P, L;
				//if (RLine(Row).ExpandTabs(Col, -2, &Flags) == 0) return 0;
				if (UnTabPoint(Row, Col) != 1)
					return 0;

				P = CharOffset(RLine(Row), Col);
				L = LineLen(Row);

				if (InsText(Row + 1, 0, RLine(Row).Count - P, RLine(Row).Chars + P, 0) == 0) return 0;
				if (DelText(Row, Col, L - Col, 0) == 0) return 0;
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
		if (Col == 0 && RLine(Row).Count == 0) {
			if (DelLine(Row, 1) == 0) return 0;
		} else {
			if (InsText(Row, Col, RLine(Row + 1).Count, RLine(Row + 1).Chars, 0) == 0) return 0;
			if (DelLine(Row + 1, 0) == 0) return 0;
			UpdateMarker(umJoinLine, Row, Col, 0, 0);
		}
		Draw(Row, -1);
		Hilit(Row);
		return 1;
	}


































	int ScreenPos(ELine L, int Offset) {
		int ExpandTabs = BFI(this, BFI_ExpandTabs);
		int TabSize = BFI(this, BFI_TabSize);

		if (!ExpandTabs) {
			return Offset;
		} else {
			char *p = L->Chars;
			int Len = L->Count;
			int Pos = 0;
			int Ofs = Offset;

			if (Ofs > Len) {
				while (Len > 0) {
					if (*p++ != '\t')
						Pos++;
					else
						Pos = NextTab(Pos, TabSize);
					Len--;
				}
				Pos += Ofs - L->Count;
			} else {
				while (Ofs > 0) {
					if (*p++ != '\t')
						Pos++;
					else
						Pos = NextTab(Pos, TabSize);
					Ofs--;
				}
			}
			return Pos;
		}
	}

	int CharOffset(ELine L, int ScreenPos) {
		int ExpandTabs = BFI(this, BFI_ExpandTabs);
		int TabSize = BFI(this, BFI_TabSize);

		if (!ExpandTabs) {
			return ScreenPos;
		} else {
			int Pos = 0;
			int Ofs = 0;
			char *p = L->Chars;
			int Len = L->Count;

			while (Len > 0) {
				if (*p++ != '\t')
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
		ELine L = (ELine *) realloc(LL, sizeof(ELine) * (ACount + 1));
		if (L == 0 && ACount != 0)
			return 0;
		RAllocated = ACount;
		LL = L;
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
				memmove(LL + RGap,
						LL + RGap + GapSize,
						sizeof(ELine) * (RPos - RGap));
			}
		} else {
			if (RGap - RPos == 1) {
				LL[RPos + GapSize] = LL[RPos];
			} else {
				memmove(LL + RPos + GapSize,
						LL + RPos,
						sizeof(ELine) * (RGap - RPos));
			}
		}
		RGap = RPos;
		return 1;
	}

	int AllocVis(int ACount) {
		int *V;

		V = (int *) realloc(VV, sizeof(int) * (ACount + 1));
		if (V == 0 && ACount != 0) return 0;
		VAllocated = ACount;
		VV = V;
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
				memmove(VV + VGap,
						VV + VGap + GapSize,
						sizeof(VV[0]) * (VPos - VGap));
			}
		} else {
			if (VGap - VPos == 1) {
				VV[VPos + GapSize] = VV[VPos];
			} else {
				memmove(VV + VPos + GapSize,
						VV + VPos,
						sizeof(VV[0]) * (VGap - VPos));
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
		if (!((No < RCount) && (No >= 0) && (LL[N]))) {
			printf("Get No = %d/%d Gap=%d RAlloc = %d, VCount = %d\n", No, RCount, RGap, RAllocated, VCount);
			assert((No < RCount) && (No >= 0) && (LL[N]));
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
	
}
