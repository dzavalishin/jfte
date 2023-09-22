package ru.dz.jfte;

import java.io.Closeable;

public class EMark implements Closeable
{

	    /* bookmark */
	private String Name;
	private EPoint Point;
	private String FileName;

	    /* bookmark in file */
	private EBuffer Buffer = null;

	
	
	
	EMark(String aName, String aFileName, EPoint aPoint, EBuffer aBuffer) {
	    Point = aPoint;
	    Name =  aName;
	    FileName = aFileName;
	    
	    if (aBuffer == null)
	        aBuffer = Console.FindFile(aFileName);
	    
	    if (aBuffer!=null && aBuffer.Loaded)
	        setBuffer(aBuffer);
	    else
	        aBuffer = null;
	}

	public void close() {
	    if (Buffer!=null)
	        removeBuffer(Buffer);
	}

	
	String getName() { return Name; }
    String getFileName() { return FileName; }
    EBuffer getBuffer() { return Buffer; }
	
	
	
	
	int setBuffer(EBuffer aBuffer) {

	    assert(Console.filecmp(aBuffer.FileName, FileName) == 0);

	    if (Point.Row >= aBuffer.RCount)
	        Point.Row = aBuffer.RCount - 1;
	    if (Point.Row < 0)
	        Point.Row = 0;

	    if (aBuffer.PlaceBookmark(Name, Point)) {
	        Buffer = aBuffer;
	        return 1;
	    }
	    return 0;
	}

	int removeBuffer(EBuffer aBuffer) {

	    if (Buffer == null || Buffer != aBuffer)
	        return 0;
	    assert(Console.filecmp(aBuffer.FileName, FileName) == 0);

	    if ((Point=Buffer.GetBookmark(Name)) == null)
	        return 0;

	    if (!Buffer.RemoveBookmark(Name))
	        return 0;
	    
	    Buffer = null;
	    return 1;
	}

	EPoint getPoint() 
	{
	    if (Buffer!=null) {
	    	Point = Buffer.GetBookmark(Name);
	        assert(Point != null);
	    }
	    return Point;
	}
	
}
