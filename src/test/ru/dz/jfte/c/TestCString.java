/**
 * 
 */
package test.ru.dz.jfte.c;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test;

import ru.dz.jfte.c.CString;

/**
 * @author dz
 *
 */
class TestCString {

	@Test
	void testCreate() {
		//fail("Not yet implemented");
		
		CString cs;
		
		cs = new CString(0);
		assertEquals(0, cs.length());
		assertEquals("", cs.toString());
		//assertEquals(0, cs.getSize());

		
		cs = new CString("abc");
		assertEquals("abc", cs.toString());
		
		cs = new CString("abcdefxyz", 3, 3);
		assertEquals("def", cs.toString());

		
		cs = new CString( "zyz".getBytes());
		assertEquals("zyz", cs.toString());
		assertEquals(3, cs.length());

		cs = new CString( "zyzdefzyz".getBytes(), 3, 2);
		assertEquals("de", cs.toString());
		assertEquals(2, cs.length());

		cs = new CString( "zyxel".toCharArray() );
		assertEquals("zyxel", cs.toString());
		assertEquals(5, cs.length());
		
		cs = new CString( "zyxel".toCharArray(), 3 );
		assertEquals("el", cs.toString());
		assertEquals(2, cs.length());
	}

	
	@Test
	void testCharSequence()
	{
		CString cs;
		cs = new CString("abcdefxyz");
		
		assertEquals('z', cs.charAt(8));
		
		assertTrue( cs.compareTo(new CString("bbcdefxyz")) < 0 );
		assertTrue( new CString("accdefxyz").compareTo(cs) > 0 );

		assertEquals(9, cs.length());
		
	}
	
	@Test
	void testStrCpy() {
		CString cs;
		cs = new CString("abcdefxyz");

		
		
		cs.strcpy("aaa\0");  
		
		assertEquals(9, cs.length());
		assertEquals(3, cs.strlen());

		assertEquals("aaa\0efxyz", cs.toString());

		
		
		cs.strncpy("bba\0", 2);  

		assertEquals(9, cs.length());
		assertEquals(3, cs.strlen());

		assertEquals("bba\0efxyz", cs.toString());

		
		
		cs.strcpy("ee\0".getBytes());  
	
		assertEquals(9, cs.length());
		assertEquals(2, cs.strlen());

		assertEquals("ee\0\0efxyz", cs.toString());

		
		
		cs.strcpy("eeaacc__uu".getBytes(), 2, 4);  
		
		assertEquals(9, cs.length());
		assertEquals(9, cs.strlen());

		assertEquals("aaccefxyz", cs.toString());
		

		
		cs.strcpy(6, 3);  
		
		assertEquals(9, cs.length());
		assertEquals(9, cs.strlen());

		assertEquals("xyzcefxyz", cs.toString());
		
	}	
	

	@Test
	void testMemCpy() {
		CString cs;
		cs = new CString("abcdefxyz");
		
		
		cs.memcpy("---", 1);
		assertEquals("-bcdefxyz", cs.toString());
		
		cs.memmove("###", 2);
		assertEquals("##cdefxyz", cs.toString());
		
	}	
	
}
