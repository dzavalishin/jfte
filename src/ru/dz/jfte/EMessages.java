package ru.dz.jfte;

import java.io.Closeable;

public class EMessages extends EList implements Closeable 
{
    String Command = null;
    String Directory = null;
    
    int ErrCount = 0;
    Error [] ErrList = null;
    int Running = 1;

    int BufLen = 0;
    int BufPos = 0;
    int PipeId;
    int ReturnCode = -1;
    int MatchCount = 0;
    String MsgBuf;
    aDir   curr_dir = null;                       // top of dir stack.


    
    EMessages(int createFlags, EModel []ARoot, String ADir, String ACommand) 
    {
    	super(createFlags, ARoot, "Messages");
    	
        CompilerMsgs = this;
        RunPipe(ADir, ACommand);
    }

    close() {
        gui.ClosePipe(PipeId);
        FreeErrors();
        CompilerMsgs = 0;
    }


    void NotifyDelete(EModel Deleting) {
        for (int i = 0; i < ErrCount; i++) {
            if (ErrList[i].Buf == Deleting) {
                /* NOT NEEDED!
                 char bk[16];
                 sprintf(bk, "_MSG.%d", i);
                 ((EBuffer *)Deleting).RemoveBookmark(bk);
                 */
                ErrList[i].Buf = null;
            }
        }
    }

    void FindErrorFiles() {
        for (int i = 0; i < ErrCount; i++)
            if (ErrList[i].Buf == 0 && ErrList[i].file != 0)
                FindErrorFile(i);
    }

    void FindErrorFile(int err) {
        assert(err >= 0 && err < ErrCount);
        if (ErrList[err].file == 0)
            return ;
        
        EBuffer B;

        ErrList[err].Buf = 0;
        
        B = FindFile(ErrList[err].file);
        if (B == 0)
            return ;

        if (B.Loaded == 0)
            return;

        AddFileError(B, err);
    }

    void AddFileError(EBuffer B, int err) {
        char bk[16];
        EPoint P;

        assert(err >= 0 && err < ErrCount);

        sprintf(bk, "_MSG.%d", err);
        P.Col = 0;
        P.Row = ErrList[err].line - 1; // offset 0


        if (P.Row >= B.RCount)
            P.Row = B.RCount - 1;
        if (P.Row < 0)
            P.Row = 0;

        if (B.PlaceBookmark(bk, P) == 1)
            ErrList[err].Buf = B;
    }

    void FindFileErrors(EBuffer *B) {
        for (int i = 0; i < ErrCount; i++)
            if (ErrList[i].Buf == 0 && ErrList[i].file != 0) {
                if (filecmp(B.FileName, ErrList[i].file) == 0) {
                    AddFileError(B, i);
                }
            }
    }

    int RunPipe(StringADir, StringACommand) {
        if (!KeepMessages)
            FreeErrors();
        
        free(Command);
        free(Directory);
            
        Command = strdup(ACommand);
        Directory = strdup(ADir);
        
        MatchCount = 0;
        ReturnCode = -1;
        Running = 1;
        BufLen = BufPos = 0;
        Row = ErrCount - 1;

        {
            char s[2 * MAXPATH * 4];

            sprintf(s, "[running '%s' in '%s']", Command, Directory);
            AddError(0, -1, 0, s);
        }

        {
            char s[MAXPATH * 2];
            sprintf(s, "Messages [%s]: %s", Directory, Command);
            SetTitle(s);
        }
        
        ChangeDir(Directory);
        PipeId = gui.OpenPipe(Command, this);
        return 0;
    }

    EEventMap GetEventMap() {
        return FindEventMap("MESSAGES");
    }

    int ExecCommand(int Command, ExState State) {
        switch (Command) {
        case ExChildClose:
            if (Running == 0 || PipeId == -1)
                break;
            ReturnCode = gui.ClosePipe(PipeId);
            PipeId = -1;
            Running = 0;
            {
                char s[30];
                
                sprintf(s, "[aborted, status=%d]", ReturnCode);
                AddError(0, -1, 0, s);
            }
            return ErOK;
            
        case ExActivateInOtherWindow:
            ShowError(View.Next, Row);
            return ErOK;
        }
        return EList::ExecCommand(Command, State);
    }

    void AddError(Error p) {
        ErrCount++;
        ErrList = (Error **) realloc(ErrList, sizeof(void *) * ErrCount);
        ErrList[ErrCount - 1] = p;
        ErrList[ErrCount - 1].Buf = 0;
        FindErrorFile(ErrCount - 1);

        if (ErrCount > Count)
            if (Row >= Count - 1) {
                //if (ErrCount > 1 && !ErrList[TopRow].file)
                    Row = ErrCount - 1;
            }

        UpdateList();
    }

    void AddError(Stringfile, int line, Stringmsg, const String text, int hilit) {
        Error pe;

        pe = (Error ) malloc(sizeof(Error));
        if (pe == 0)
            return ;

        pe.file = file ? strdup(file) : 0;
        pe.line = line;
        pe.msg = msg ? strdup(msg) : 0;
        pe.text = text ? strdup(text) : 0;
        pe.hilit = hilit;

        AddError(pe);
    }

    void FreeErrors() {
        if (ErrList) {
            for (int i = 0; i < ErrCount; i++) {
                if (ErrList[i].Buf != 0) {
                    char bk[16];
                    sprintf(bk, "_MSG.%d", i);
                    ((EBuffer *)(ErrList[i].Buf)).RemoveBookmark(bk);
                }
            }
        }
        ErrCount = 0;
        ErrList = 0;
        BufLen = BufPos = 0;
    }

    int GetLine(String Line, int maxim) {
        int rc;
        String p;
        int l;
        
        //fprintf(stderr, "GetLine: %d\n", Running);
        
        *Line = 0;
        if (Running && PipeId != -1) {
            rc = gui.ReadPipe(PipeId, MsgBuf + BufLen, sizeof(MsgBuf) - BufLen);
            //fprintf(stderr, "GetLine: ReadPipe rc = %d\n", rc);
            if (rc == -1) {
                ReturnCode = gui.ClosePipe(PipeId);
                PipeId = -1;
                Running = 0;
            }
            if (rc > 0)
                BufLen += rc;
        }
        l = maxim - 1;
        if (BufLen - BufPos < l)
            l = BufLen - BufPos;
        //fprintf(stderr, "GetLine: Data %d\n", l);
        p = (String )memchr(MsgBuf + BufPos, '\n', l);
        if (p) {
            *p = 0;
            strcpy(Line, MsgBuf + BufPos);
            l = strlen(Line);
            if (l > 0 && Line[l - 1] == '\r')
                Line[l - 1] = 0;
            BufPos = p + 1 - MsgBuf;
            //fprintf(stderr, "GetLine: Line %d\n", strlen(Line));
        } else if (Running && sizeof(MsgBuf) != BufLen) {
            memmove(MsgBuf, MsgBuf + BufPos, BufLen - BufPos);
            BufLen -= BufPos;
            BufPos = 0;
            //fprintf(stderr, "GetLine: Line Incomplete\n");
            return 0;
        } else {
            if (l == 0) 
                return 0;
            memcpy(Line, MsgBuf + BufPos, l);
            Line[l] = 0;
            if (l > 0 && Line[l - 1] == '\r')
                Line[l - 1] = 0;
            BufPos += l;
            //fprintf(stderr, "GetLine: Line Last %d\n", l);
        }
        memmove(MsgBuf, MsgBuf + BufPos, BufLen - BufPos);
        BufLen -= BufPos;
        BufPos = 0;
        //fprintf(stderr, "GetLine: Got Line\n");
        return 1;
    }
    
}


class aDir
{
    aDir       next;
    String     name;
}