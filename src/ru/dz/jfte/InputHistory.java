package ru.dz.jfte;

import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

public class InputHistory implements MainConst 
{
	private static final Logger log = Logger.getLogger(InputHistory.class.getName());

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



	
	
	
	
	
	
	
	
	
	
	
	
	
	
	


	static boolean SaveHistory(String FileName) 
	{
		try(BufferedWriter w = Files.newBufferedWriter(Path.of(FileName), Main.charset)) 
		{
			w.write(HISTORY_VER);
			w.newLine();

			
			for( FPosHistory fp : FPosHistory.FPHistory.values() )
			{
				w.write( String.format( "F|%d|%d|%s\n",
	                    fp.Row,
	                    fp.Col,
	                    fp.FileName ));

				for( HBookmark b : fp.Books )
					w.write( String.format( "B|%d|%d|%s\n",
							b.Row, b.Col, b.Name ));				
			}
			
			// input history, TODO store in reverse order to preserve order when loading
			for( InputHistory h : hlist )
				w.write( String.format( "I|%d|%s\n", 
						h.Id, h.Line ));

			w.close();
		}
		catch(IOException e)
		{
			log.log(Level.SEVERE, "Can't save history to "+FileName, e);
			return false;
		}
		 
	    return true;
	}

	static boolean LoadHistory(String FileName) { 
		/* TODO LoadHistory
	    FILE *fp;
	    char line[2048];
	    String p, *e;
	    FPosHistory *last=NULL;
	    HBookmark **nb;
	    
	    fp = fopen(FileName, "r");
	    if (fp == 0)
	        return 0;


	    if (fgets(line, sizeof(line), fp) == 0 ||
	        strcmp(line, HISTORY_VER) != 0)
	    {
	        fclose(fp);
	        return 0;
	    }
	    while (fgets(line, sizeof(line), fp) != 0) {
	        if (line[0] == 'F' && line[1] == '|') { // file position history
	            int r, c, L, R, M, cmp;
	            p = line + 2;
	            r = strtol(p, &e, 10);
	            if (e == p)
	                break;
	            if (*e == '|')
	                e++;
	            else
	                break;
	            c = strtol(p = e, &e, 10);
	            if (e == p)
	                break;
	            if (*e == '|')
	                e++;
	            else
	                break;
	            e = strchr(p = e, '\n');
	            if (e == 0)
	                break;
	            *e = 0;
	            last=NULL;
	            if (UpdateFPos(p, r, c) == 0)
	                break;
	            // Get current file's record for storing bookmarks
	            L=0;R=FPHistoryCount;
	            while (L < R) {
	                M = (L + R) / 2;
	                cmp = filecmp(p, FPHistory[M].FileName);
	                if (cmp == 0) {
	                    last=FPHistory[M];
	                    break;
	                } else if (cmp < 0) {
	                    R = M;
	                } else {
	                    L = M + 1;
	                }
	            }
	        } else if (line[0] == 'B' && line[1] == '|') { // bookmark history for last file
	            if (last) {
	                int r, c;
	                p = line + 2;
	                r = strtol(p, &e, 10);
	                if (e == p)
	                    break;
	                if (*e == '|')
	                    e++;
	                else
	                    break;
	                c = strtol(p = e, &e, 10);
	                if (e == p)
	                    break;
	                if (*e == '|')
	                    e++;
	                else
	                    break;
	                e = strchr(p = e, '\n');
	                if (e == 0)
	                    break;
	                *e = 0;
	                nb=(HBookmark **)realloc (last.Books,sizeof (HBookmark *)*(last.BookCount+1));
	                if (nb) {
	                    last.Books=nb;
	                    nb[last.BookCount]=(HBookmark *)malloc (sizeof (HBookmark));
	                    if (nb[last.BookCount]) {
	                        nb[last.BookCount].Row=r;
	                        nb[last.BookCount].Col=c;
	                        nb[last.BookCount].Name=strdup (p);
	                        last.BookCount++;
	                    }
	                }
	            }
	        } else if (line[0] == 'I' && line[1] == '|') { // input history
	            int i;
	            
	            p = line + 2;
	            i = strtol(p, &e, 10);
	            if (e == p)
	                break;
	            if (*e == '|')
	                e++;
	            else
	                break;
	            e = strchr(p = e, '\n');
	            if (e == 0)
	                break;
	            *e = 0;
	            AddInputHistory(i, p);
	        }
	    }
	    fclose(fp);
	    return 1;
	*/
		return false;
	}


}
