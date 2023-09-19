package ru.dz.jfte;

import java.io.Closeable;
import java.io.IOException;
import java.util.Arrays;

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

    static EMessages CompilerMsgs = null;
    
    EMessages(int createFlags, EModel []ARoot, String ADir, String ACommand) 
    {
    	super(createFlags, ARoot, "Messages");
    	
        CompilerMsgs = this;
        RunPipe(ADir, ACommand);
    }

    @Override
    public void close() {
        GUI.gui.ClosePipe(PipeId);
        FreeErrors();
        CompilerMsgs = null;
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
            if (ErrList[i].Buf == null && ErrList[i].file != null)
                FindErrorFile(i);
    }

    void FindErrorFile(int err) {
        assert(err >= 0 && err < ErrCount);
        if (ErrList[err].file == null)
            return ;
        
        EBuffer B;

        ErrList[err].Buf = null;
        
        B = FindFile(ErrList[err].file);
        if (B == null)
            return ;

        if(!B.Loaded)
            return;

        AddFileError(B, err);
    }

    void AddFileError(EBuffer B, int err) {
        EPoint P = new EPoint();

        assert(err >= 0 && err < ErrCount);

        String bk = String.format("_MSG.%d", err);
        
        P.Col = 0;
        P.Row = ErrList[err].line - 1; // offset 0


        if (P.Row >= B.RCount)
            P.Row = B.RCount - 1;
        if (P.Row < 0)
            P.Row = 0;

        if (B.PlaceBookmark(bk, P) == 1)
            ErrList[err].Buf = B;
    }

    void FindFileErrors(EBuffer B) {
        for (int i = 0; i < ErrCount; i++)
            if (ErrList[i].Buf == null && ErrList[i].file != null) {
                if (filecmp(B.FileName, ErrList[i].file) == 0) {
                    AddFileError(B, i);
                }
            }
    }

    int RunPipe(String ADir, String ACommand) {
        if (Config.KeepMessages==0)
            FreeErrors();
        
        Command = ACommand;
        Directory = ADir;
        
        MatchCount = 0;
        ReturnCode = -1;
        Running = 1;
        BufLen = BufPos = 0;
        Row = ErrCount - 1;

        {
            String s = String.format("[running '%s' in '%s']", Command, Directory);
            AddError(null, -1, null, s);
        }

        {
        	String s = String.format("Messages [%s]: %s", Directory, Command);
            SetTitle(s);
        }
        
        Console.ChangeDir(Directory);
        PipeId = GUI.gui.OpenPipe(Command, this);
        return 0;
    }

    EEventMap GetEventMap() {
        return EEventMap.FindEventMap("MESSAGES");
    }

    ExResult ExecCommand(ExCommands Command, ExState State) {
        switch (Command) {
        case ExChildClose:
            if (Running == 0 || PipeId == -1)
                break;
            ReturnCode = GUI.gui.ClosePipe(PipeId);
            PipeId = -1;
            Running = 0;
            {
                String s = String.format("[aborted, status=%d]", ReturnCode);
                AddError(null, -1, null, s);
            }
            return ExResult.ErOK;
            
        case ExActivateInOtherWindow:
            ShowError(View.Next, Row);
            return ExResult.ErOK;
        }
        return super.ExecCommand(Command, State);
    }

    void AddError(Error p) 
    {
        ErrCount++;
        //ErrList = (Error **) realloc(ErrList, sizeof(void *) * ErrCount);
        ErrList = Arrays.copyOf(ErrList, ErrCount);
        ErrList[ErrCount - 1] = p;
        ErrList[ErrCount - 1].Buf = null;
        FindErrorFile(ErrCount - 1);

        if (ErrCount > Count)
            if (Row >= Count - 1) {
                //if (ErrCount > 1 && !ErrList[TopRow].file)
                    Row = ErrCount - 1;
            }

        UpdateList();
    }

    void AddError(String file, int line, String msg, String text, int hilit) {
        Error pe = new Error();

        pe.file = file;
        pe.line = line;
        pe.msg = msg;
        pe.text = text;
        pe.hilit = hilit;

        AddError(pe);
    }

    void FreeErrors() {
        if (ErrList!=null) {
            for (int i = 0; i < ErrCount; i++) {
                if (ErrList[i].Buf != null) {
                    String bk = String.format("_MSG.%d", i);
                    ((EBuffer)(ErrList[i].Buf)).RemoveBookmark(bk);
                }
            }
        }
        ErrCount = 0;
        ErrList = null;
        BufLen = BufPos = 0;
    }

    int GetLine(String [] Line, int maxim) {
        int rc;
        String p;
        int l;
        
        //fprintf(stderr, "GetLine: %d\n", Running);
        
        Line[0] = null;
        if (Running && PipeId != -1) {
            rc = GUI.gui.ReadPipe(PipeId, MsgBuf + BufLen, sizeof(MsgBuf) - BufLen);
            //fprintf(stderr, "GetLine: ReadPipe rc = %d\n", rc);
            if (rc == -1) {
                ReturnCode = GUI.gui.ClosePipe(PipeId);
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