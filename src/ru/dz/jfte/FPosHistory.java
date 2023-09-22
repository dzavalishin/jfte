package ru.dz.jfte;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class FPosHistory
{
	String FileName;
	int Row, Col;
	List<HBookmark> Books = new ArrayList<>();
	//int BookCount;

	static Map<String,FPosHistory> FPHistory = new HashMap<>();



	static void UpdateFPos(String FileName, int Row, int Col) 
	{ 

		FPosHistory fp = FPHistory.get(FileName);
		if( fp != null )
		{
			fp.Row = Row;
			fp.Col = Col;
		}
		else
		{
			fp = new FPosHistory();
			fp.FileName = FileName;
			fp.Row = Row;
			fp.Col = Col;
			FPHistory.put(FileName, fp);
		}

		/*
        int L = 0, R = FPHistory.size(), M, N;
        FPosHistory fp;//, **NH;
        int cmp;

        if (FPHistory != 0) {
            while (L < R) {
                M = (L + R) / 2;
                cmp = filecmp(FileName, FPHistory[M].FileName);
                if (cmp == 0) {
                    FPHistory[M].Row = Row;
                    FPHistory[M].Col = Col;
                    return 1;
                } else if (cmp < 0) {
                    R = M;
                } else {
                    L = M + 1;
                }
            }
        } else {
            FPHistory.size() = 0;
            L = 0;
        }
        assert(L >= 0 && L <= FPHistory.size());
        fp = (FPosHistory *)malloc(sizeof(FPosHistory));
        if (fp == 0)
            return 0;
        fp.Row = Row;
        fp.Col = Col;
        fp.FileName = strdup(FileName);
        fp.Books = null;
        fp.BookCount = 0;
        if (fp.FileName == 0) {
            free(fp);
            return 0;
        }

        N = 64;
        while (N <= FPHistory.size()) N *= 2;
        NH = (FPosHistory **)realloc((void *)FPHistory, N * sizeof(FPosHistory *));
        if (NH == 0)
        {
            free(fp.FileName);
            free(fp);
            return 0;
        }

        FPHistory = NH;

        if (L < FPHistory.size())
            memmove(FPHistory + L + 1,
                    FPHistory + L,
                    (FPHistory.size() - L) * sizeof(FPosHistory *));
        FPHistory[L] = fp;
        FPHistory.size()++;
        return 1;
		 */
	}

	static boolean RetrieveFPos(String FileName, int [] Row, int [] Col) 
	{
		FPosHistory fp = FPHistory.get(FileName);
		if( fp != null )
		{
			Row[0] = fp.Row;
			Col[0] = fp.Col;

			return true;
		}
		return false;

		/*

        int L = 0, R = FPHistory.size(), M;
        int cmp;

        if (FPHistory == 0)
            return 0;

        while (L < R) {
            M = (L + R) / 2;
            cmp = filecmp(FileName, FPHistory[M].FileName);
            if (cmp == 0) {
                Row = FPHistory[M].Row;
                Col = FPHistory[M].Col;
                return 1;
            } else if (cmp < 0) {
                R = M;
            } else {
                L = M + 1;
            }
        }
        return 0;
		 */
	}


	/*
	 * Get bookmarks for given Buffer (file) from history.
	 */
	static boolean RetrieveBookmarks(EBuffer buffer) { /*fold00*/

		//int L = 0, R = FPHistory.size(), M,i;
		//int cmp;
		//HBookmark bmk;
		//String name = "_BMK";
		//EPoint P;

		assert(buffer!=null);
		if (FPHistory.size()==0) return true;

		FPosHistory fp = FPHistory.get(buffer.FileName);
		if( fp == null )
			return true;


		// Now "copy" bookmarks to Buffer
		for (HBookmark b : fp.Books) 
		{
			String name = "_BMK" + b.Name;
			EPoint P = new EPoint();

			P.Row=b.Row;P.Col=b.Col;

			if (P.Row<0) P.Row=0;
			else if (P.Row>=buffer.RCount) P.Row=buffer.RCount-1;

			if (P.Col<0) P.Col=0;

			buffer.PlaceBookmark(name,P);
		}

		return true;


		/*
        while (L < R) {
            M = (L + R) / 2;
            cmp = filecmp(buffer.FileName, FPHistory[M].FileName);
            if (cmp == 0) {
                // Now "copy" bookmarks to Buffer
                for (i=0;i<FPHistory[M].BookCount;i++) {
                    bmk=FPHistory[M].Books[i];
                    strcpy (name+4,bmk.Name);
                    P.Row=bmk.Row;P.Col=bmk.Col;
                    if (P.Row<0) P.Row=0;
                    else if (P.Row>=buffer.RCount) P.Row=buffer.RCount-1;
                    if (P.Col<0) P.Col=0;
                    buffer.PlaceBookmark(name,P);
                }
                return 1;
            } else if (cmp < 0) {
                R = M;
            } else {
                L = M + 1;
            }
        }
        return 1;
		 */
	}

	/*
	 * Store given Buffer's bookmarks to history.
	 */
	static boolean StoreBookmarks(EBuffer buffer) { /*fold00*/

		//int L = 0, R = FPHistory.size(), M,i,j;
		//int cmp;
		HBookmark bmk;

		int [] _i = {0}, _j = {0};

		assert (buffer!=null);
		if (!RetrieveFPos (buffer.FileName,_i,_j)) {
			// File not found in FPHistory . add it
			UpdateFPos (buffer.FileName,0,0);
		}
		// Now file is surely in FPHistory

		FPosHistory fp = FPHistory.get(buffer.FileName);
		assert( fp != null );



		// First delete previous bookmarks
		fp.Books.clear();

		/*
		int i, j;
		// Now add new bookmarks - first get # of books to store
		for (i=j=0;(i=buffer.GetUserBookmarkForLine(i,-1,name,P))>=0;j++)
			;
		*/
		int BookCount=buffer.BMarks.size();    

		//for (i=j=0;(i=buffer.GetUserBookmarkForLine(i,-1,name,P))>=0;j++) 
		for( EBookmark bm : buffer.BMarks.values() )	
		{
			bmk = new HBookmark();

			bmk.Row = bm.BM.Row;
			bmk.Col=bm.BM.Col;
			bmk.Name=bm.Name;

			fp.Books.add(bmk);
		}

		return true;




		/*
        while (L < R) {
            M = (L + R) / 2;
            cmp = filecmp(buffer.FileName, FPHistory[M].FileName);
            if (cmp == 0) {
                // First delete previous bookmarks
                for (i=0;i<FPHistory[M].BookCount;i++) {
                    bmk=FPHistory[M].Books[i];
                    if (bmk.Name) free (bmk.Name);
                    free (bmk);
                }
                free (FPHistory[M].Books);
                FPHistory[M].Books=null;
                // Now add new bookmarks - first get # of books to store
                for (i=j=0;(i=buffer.GetUserBookmarkForLine(i,-1,name,P))>=0;j++);
                FPHistory[M].BookCount=j;
                if (j) {
                    // Something to store
                    FPHistory[M].Books=(HBookmark **)malloc (sizeof (HBookmark *)*j);
                    if (FPHistory[M].Books) {
                        for (i=j=0;(i=buffer.GetUserBookmarkForLine(i,-1,name,P))>=0;j++) {
                            bmk=FPHistory[M].Books[j]=(HBookmark *)malloc (sizeof (HBookmark));
                            if (bmk) {
                                bmk.Row=P.Row;bmk.Col=P.Col;
                                bmk.Name=strdup (name);
                            } else {
                                // Only part set
                                FPHistory[M].BookCount=j;
                                return 0;
                            }
                        }
                        return 1;
                    } else {
                        // Alloc error
                        FPHistory[M].BookCount=0;
                        return 0;
                    }
                }
                return 1;
            } else if (cmp < 0) {
                R = M;
            } else {
                L = M + 1;
            }
        }
        // Should not get here
        return 0;
		 */
	}




}
