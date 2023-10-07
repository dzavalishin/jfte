/**
 * 
 */
package test.ru.dz.jfte.c;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test;

import ru.dz.jfte.c.CString;
import ru.dz.jfte.c.CStringPtr;
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

		cs.memset( 6, '*', 3 ); 
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

	
	
	
	
	
	
	
	
	
	
	
	@Test
	void testPtrNew() {
		CString cs;
		CStringPtr p;

		cs = new CString("abcdefxyzabc");
		assertEquals( 12, cs.length() );
		
		p = new CStringPtr(cs);		
		assertEquals( "abcdefxyzabc", p.toString() );
		assertEquals( 12, p.length() );

		p = new CStringPtr(cs,2);
		assertEquals( "cdefxyzabc", p.toString() );
		assertEquals( 10, p.length() );

		{
		char [] ca = "defxyzabc".toCharArray();

		p = new CStringPtr(ca);
		assertEquals( "defxyzabc", p.toString() );
		assertEquals( 9, p.length() );

		p = new CStringPtr(ca,2);
		assertEquals( "fxyzabc", p.toString() );
		assertEquals( 7, p.length() );
		}

		//assertThrows(null, null)		
		
		cs = new CString(0);
		p = new CStringPtr(cs,2);
		assertEquals(0, cs.length());
		assertEquals("", cs.toString());
		//assertEquals(0, cs.getSize());

		
		cs = new CString("abc");
		p = new CStringPtr(cs,2);
		assertEquals("c", p.toString());
		
		cs = new CString("abcdefxyz", 3, 3);
		p = new CStringPtr(cs,2);
		assertEquals("f", p.toString());

		
		cs = new CString( "zyz".getBytes());
		p = new CStringPtr(cs,2);
		assertEquals("z", p.toString());
		assertEquals(1, p.length());

		cs = new CString( "zyzdefzyz".getBytes(), 3, 2);
		p = new CStringPtr(cs,2);
		assertEquals("", p.toString());
		assertEquals(0, p.length());

		cs = new CString( "zyxel".toCharArray() );
		p = new CStringPtr(cs,2);
		assertEquals("xel", p.toString());
		assertEquals(5, cs.length());
		
		cs = new CString( "zyxel".toCharArray(), 3 );
		p = new CStringPtr(cs,2);
		assertEquals("", p.toString());
		assertEquals(0, p.length());
		
	}

	

	@Test
	void testCharSequencePtr()
	{
		CString cs;
		CStringPtr p;
		
		cs = new CString("abcdefxyz");
		p = new CStringPtr(cs,2);
		
		assertEquals('z', p.charAt(6));
		
		assertTrue( p.compareTo(new CString("ddefxyz")) < 0 );
		assertTrue( new CString("ddefxyz").compareTo(p) > 0 );

		assertEquals(7, p.length());
		
	}
	
	@Test
	void testStrCpyPtr() {
		CString cs;
		CStringPtr p;

		cs = new CString("abcdefxyz");
		p = new CStringPtr(cs,2);		
		
		p.strcpy("aaa\0");  
		
		assertEquals(9, cs.length());
		assertEquals(5, cs.strlen());

		assertEquals(7, p.length());
		assertEquals(3, p.strlen());
		
		assertEquals("abaaa\0xyz", cs.toString());
		assertEquals("aaa\0xyz", p.toString());

		
		
		p.strncpy("bba\0", 2);  

		assertEquals(7, p.length());
		assertEquals(3, p.strlen());

		assertEquals("abbba\0xyz", cs.toString());

		
		
		p.strcpy("ee\0".getBytes());  
	
		assertEquals(7, p.length());
		assertEquals(2, p.strlen());

		assertEquals("abee\0\0xyz", cs.toString());
		assertEquals("ee\0\0xyz", p.toString());

		
		
		p.strcpy("eeaacc__uu".getBytes(), 2, 4);  
		
		assertEquals(7, p.strlen());
		assertEquals(9, cs.length());

		assertEquals("aaccxyz", p.toString());
		assertEquals("abaaccxyz", cs.toString());
		

		
		p.strcpy(4, 3);  
		
		assertEquals(9, cs.length());
		assertEquals(9, cs.strlen());

		assertEquals("abxyzcxyz", cs.toString());

		
	}	
	

	@Test
	void testMemCpyPtr() {
		CString cs;
		CStringPtr p;

		cs = new CString("abcdefxyz");
		p = new CStringPtr(cs,2);		
		
		
		p.memcpy("---", 1);
		assertEquals("ab-defxyz", cs.toString());
		assertEquals("-defxyz", p.toString());
		
		p.memmove("###", 2);
		assertEquals("ab##efxyz", cs.toString());
		assertEquals("##efxyz", p.toString());

		p.memmove( 2, "aaaqqq", 3, 2); 
		assertEquals("ab##qqxyz", cs.toString());

		p.memmove( 5, "z-!xel".toCharArray(), 1, 2); 
		assertEquals("ab##qqx-!", cs.toString());

		p.memmove( 1, "zyz".getBytes(), 1, 2); 
		assertEquals("ab#yzqx-!", cs.toString());
		
		p.memmove( 0, 5, 2); 
		assertEquals("ab-!zqx-!", cs.toString());
	}	

	
	@Test
	void testMemSetPtr() {
		CString cs;
		CStringPtr p;

		cs = new CString("abcdefxyz");
		p = new CStringPtr(cs,2);		

		p.memset( '%', 3 ); 
		assertEquals("ab%%%fxyz", cs.toString());

		p.memset( 4, '*', 3 ); 
		assertEquals("ab%%%f***", cs.toString());
	}	


	@Test
	void testStrCatPtr() {
		CString cs;
		CStringPtr p;

		cs = new CString("abcdefxyz");
		p = new CStringPtr(cs,2);		

		p.strcat( "123" ); 
		assertEquals("abcdefxyz123", cs.toString());

		p.strncat( "789", 2 ); 
		assertEquals("abcdefxyz12378", cs.toString());
	}	
	

	
	@Test
	void testCmpPtr() {
		CString cs;
		CStringPtr p;

		cs = new CString("abcdefxyz");
		p = new CStringPtr(cs,2);		
		
		assertTrue( p.strcmp(new CString("ddefxyz")) < 0 );
		assertTrue( p.strcmp(new CString("adefxyz")) > 0 );
		assertTrue( p.strcmp(new CString("cdefxyz")) == 0 );

		assertTrue( p.strncmp(new CString("ddefxyz"), 3) < 0 );
		assertTrue( p.strncmp(new CString("aacefxyz"), 3) > 0 );
		assertTrue( p.strncmp(new CString("cde==="), 3) == 0 );
		
		assertTrue( p.strncmp(new CString("cde---"), 3) == 0 );
		assertTrue( p.strncmp(new CString("cd\0"), 3) != 0 );

		cs = new CString("xxab\0aa");
		p = new CStringPtr(cs,2);		
		assertTrue( p.strncmp(new CString("ab\0bb"), 3) == 0 );		
		assertTrue( p.memcmp(new CString("ab\0bb"), 4) != 0 );
	}	

	
	@Test
	void testSearchPtr() {
		CString cs;
		CStringPtr p;

		cs = new CString("abcdefxyzabc");
		p = new CStringPtr(cs,2);		

		assertEquals( 1, p.strchr('d') );

		assertEquals( 0, p.strchr('c') );
		assertEquals( 9, p.strrchr('c') );

		cs = new CString("abcdef\0yzabc");
		p = new CStringPtr(cs,2);		

		assertEquals( 1, p.strrchr('d') );
		assertEquals( -1, p.strchr('z') );
		assertEquals( 6, p.memchr('z', p.length()) );

		assertEquals( 7, p.strchr('a', 7) );
		assertEquals( -1, p.strchr('d', 2) );
		
		assertEquals( 4, p.strlen() );
		assertEquals( 10, p.length() );

		assertEquals( 2, p.strstr("ef") );
		assertEquals( -1, p.strstr("bcz") );
		
		
		
		cs = new CString("abc:def/xyz,abc");		
		p = new CStringPtr(cs,2);		
		CStringTokenizer t = p.strtok(":/,");
		
		assertTrue(t.hasNext());
		assertEquals( "c", t.next().toString() );
		assertEquals( "def", t.next().toString() );
		assertEquals( "xyz", t.next().toString() );
		assertEquals( "abc", t.next().toString() );
		
	}	
	

	
	
	@Test
	void testSubstring() {
		CString cs;
		CStringPtr p;

		// TODO testSubstring testSubstringPtr 
		
	}

	@Test
	void testStaticCmp() {
		assertTrue( CString.memcmp("aaa", "aba", 3) < 0 );
		assertTrue( CString.memcmp("aba", "aaa", 3) > 0 );
		assertTrue( CString.memcmp("aba", "aba", 3) == 0 );

		assertTrue( CString.memcmp("abaA", "abaC", 3) == 0 );

		
		assertTrue( CString.memicmp("aaa", "aba", 3) < 0 );
		assertTrue( CString.memicmp("aAa", "aba", 3) < 0 );
		assertTrue( CString.memicmp("aaa", "aBa", 3) < 0 );
		assertTrue( CString.memicmp("aAa", "aBa", 3) < 0 );
		
		assertTrue( CString.memicmp("aba", "aaa", 3) > 0 );
		assertTrue( CString.memicmp("aBa", "aaa", 3) > 0 );
		assertTrue( CString.memicmp("aba", "aAa", 3) > 0 );
		assertTrue( CString.memicmp("aBa", "aAa", 3) > 0 );
		
		assertTrue( CString.memicmp("aba", "aba", 3) == 0 );
		assertTrue( CString.memicmp("aBa", "aba", 3) == 0 );
		assertTrue( CString.memicmp("aba", "aBa", 3) == 0 );
		assertTrue( CString.memicmp("aBa", "aBa", 3) == 0 );

		assertTrue( CString.memicmp("aBaA", "abaC", 3) == 0 );

		
		assertTrue( CString.memcmp("__aaa", 2, "aba", 3) < 0 );
		assertTrue( CString.memcmp("!aba", 1, "aaa", 3) > 0 );
		assertTrue( CString.memcmp("---aba", 3, "aba", 3) == 0 );
		
		assertTrue( CString.memicmp("##aBaA", 2, "abaC", 3) == 0 );

		assertTrue( CString.strcmp("aaa", "aba") < 0 );
		assertTrue( CString.strcmp("aba", "aaa") > 0 );
		assertTrue( CString.strcmp("aba", "aba") == 0 );
	
		assertTrue( CString.strcmp("aba!", "aba") > 0 );
		assertTrue( CString.strcmp("aba", "aba!") < 0 );

		assertTrue( CString.strncmp("aaa#", "aba!", 3) < 0 );
		assertTrue( CString.strncmp("aba$", "aaa@", 3) > 0 );
		assertTrue( CString.strncmp("aba%", "aba*", 3) == 0 );
	
		assertTrue( CString.strncmp("aba!", "aba#", 3) == 0 );
	
		assertTrue( CString.strnicmp("aAa#", "aba!", 3) < 0 );
		assertTrue( CString.strnicmp("aaa#", "aBa!", 3) < 0 );
		assertTrue( CString.strnicmp("aBa$", "aaa@", 3) > 0 );
		assertTrue( CString.strnicmp("aba$", "aAa@", 3) > 0 );
		assertTrue( CString.strnicmp("aBa%", "aba*", 3) == 0 );
		assertTrue( CString.strnicmp("aba%", "aBa*", 3) == 0 );
	}
	

	@Test
	void testStaticCharTypes() {
		
		assertTrue( CString.isalnum('f') );
		assertTrue( CString.isalnum('щ') );
		assertTrue( CString.isalnum('4') );
		
		assertFalse( CString.isalnum('&') );
		assertFalse( CString.isalnum('[') );
		assertFalse( CString.isalnum(';') );
	}	
	
	
}















