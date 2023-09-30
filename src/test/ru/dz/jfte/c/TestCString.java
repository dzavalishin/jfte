/**
 * 
 */
package test.ru.dz.jfte.c;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test;

import ru.dz.jfte.c.CString;
import ru.dz.jfte.c.CStringTokenizer;

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

		cs.memmove( 2, "aaaqqq", 3, 2); 
		assertEquals("##qqefxyz", cs.toString());

		cs.memmove( 7, "z-!xel".toCharArray(), 1, 2); 
		assertEquals("##qqefx-!", cs.toString());

		cs.memmove( 1, "zyz".getBytes(), 1, 2); 
		assertEquals("#yzqefx-!", cs.toString());
		
		cs.memmove( 0, 7, 2); 
		assertEquals("-!zqefx-!", cs.toString());
	}	

	
	@Test
	void testMemSet() {
		CString cs;
		cs = new CString("abcdefxyz");

		cs.memset( '%', 3 ); 
		assertEquals("%%%defxyz", cs.toString());

		cs.memset( '*', 6, 3 ); 
		assertEquals("%%%def***", cs.toString());
	}	


	@Test
	void testStrCat() {
		CString cs;
		cs = new CString("abcdefxyz");

		cs.strcat( "123" ); 
		assertEquals("abcdefxyz123", cs.toString());

		cs.strncat( "789", 2 ); 
		assertEquals("abcdefxyz12378", cs.toString());
	}	
	

	
	@Test
	void testCmp() {
		CString cs;
		cs = new CString("abcdefxyz");
		
		assertTrue( cs.strcmp(new CString("bbcdefxyz")) < 0 );
		assertTrue( cs.strcmp(new CString("aacdefxyz")) > 0 );
		assertTrue( cs.strcmp(new CString("abcdefxyz")) == 0 );

		assertTrue( cs.strncmp(new CString("bbcdefxyz"), 3) < 0 );
		assertTrue( cs.strncmp(new CString("aacdefxyz"), 3) > 0 );
		assertTrue( cs.strncmp(new CString("abcdefxyz"), 3) == 0 );
		
		assertTrue( cs.strncmp(new CString("abc---"), 3) == 0 );
		assertTrue( cs.strncmp(new CString("ab\0"), 3) != 0 );

		cs = new CString("ab\0aa");
		assertTrue( cs.strncmp(new CString("ab\0bb"), 3) == 0 );
		
		assertTrue( cs.memcmp(new CString("ab\0bb"), 4) != 0 );
	}	

	
	@Test
	void testSearch() {
		CString cs;
		cs = new CString("abcdefxyzabc");

		assertEquals( 3, cs.strchr('d') );

		assertEquals( 1, cs.strchr('b') );
		assertEquals( 10, cs.strrchr('b') );

		cs = new CString("abcdef\0yzabc");
		assertEquals( 1, cs.strrchr('b') );
		assertEquals( -1, cs.strchr('z') );
		assertEquals( 8, cs.memchr('z', cs.length()) );

		assertEquals( 9, cs.strchr('a', 7) );
		assertEquals( -1, cs.strchr('a', 1) );
		
		assertEquals( 6, cs.strlen() );
		assertEquals( 12, cs.length() );

		assertEquals( 1, cs.strstr("bc") );
		assertEquals( -1, cs.strstr("bcz") );
		
		
		
		cs = new CString("abc:def/xyz,abc");		
		CStringTokenizer t = cs.strtok(":/,");
		
		assertTrue(t.hasNext());
		assertEquals( "abc", t.next().toString() );
		assertEquals( "def", t.next().toString() );
		assertEquals( "xyz", t.next().toString() );
		assertEquals( "abc", t.next().toString() );
		
		
	}	
	
}















