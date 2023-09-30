package ru.dz.jfte.c;

import java.util.Objects;

/**
 * 
 * TODO ICString
 * 
 * Work in progress
 * <p>
 * Replacement for C {@code char *}  
 * 
 * @author dz
 *
 * @see https://en.wikibooks.org/wiki/C_Programming/String_manipulation 
 *
 */

public interface ICString extends CharSequence, Comparable<CharSequence>  
{

	// -------------------------------------------------------------------
	// Size
	// -------------------------------------------------------------------
	
	/**
	 * Stops on {@code null} character.
	 * 
	 * @return Length of string up to first {@code \0} or to the end of backing array. 
	 */
	int strlen();

	/**
	 * @return Size of backing array.
	 */
	int size();
	

	// -------------------------------------------------------------------
	// Copy
	// -------------------------------------------------------------------

	/**
	 * Mimics libc strcpy() function - copies up to the end or to the 
	 * first '\0' character. <p> <b>NB!</b> The '\0' character itsels <b>is copied</b>.
	 * 
	 * <p>
	 * If you don't need that '\0' processing use new SCtring(src) instead. 
	 *   
	 * @param src Copy from
	 */
	
	void strcpy( CharSequence src );

	/**
	 * 
	 * Copies up to the end or to the 
	 * first '\0' character.
	 * 
	 * @see #strcpy
	 * 
	 * @param src Copy from
	 * @param size Copy this much
	 */
	void strncpy( CharSequence src, int size );

	void strcpy(byte[] src);
	void strcpy(byte[] src, int pos, int len);
	void strcpy(int pos, int len);

	void memcpy( CharSequence src, int size );
	void memmove( CharSequence src, int size ); 
	void memmove( int destPos, CharSequence src, int srcPos, int size ); 
	void memmove(int dest, int src, int len);

	void memmove(int destPos, char[] src, int srcPos, int size);
	void memmove(int destPos, byte[] src, int srcPos, int size);
	
	void memset( char data, int size ); 
	void memset( char data, int start, int size );
	
	void strcat( CharSequence src );
	void strncat( CharSequence src, int len );

	// -------------------------------------------------------------------
	// Compare
	// -------------------------------------------------------------------
	
	int strcmp( CharSequence src );
	int strncmp( CharSequence src, int len );

	int memcmp( CharSequence src, int size ); 
	
	int strcoll( CharSequence src ); 
	
	boolean equals( CharSequence src );


	// -------------------------------------------------------------------
	// Search and special
	// -------------------------------------------------------------------
	
	
	int strchr( char c );
	int strrchr( char c );
	
	int memchr( char c, int len ); 
	
	int strstr( CharSequence src ); 
	
	CStringTokenizer strtok( CharSequence src ); 
	
	int strspn( CharSequence src ); 
	int strspn(CharSequence delimiters, int start);
	
	int strcspn( CharSequence src ); 
	
	int strpbrk( CharSequence src ); 
	
	
	int strxfrm( CharSequence src, int len );

	// -------------------------------------------------------------------
	// Export
	// -------------------------------------------------------------------
	
	String toString(int startPos, int endPos);

	String toString(int startPos);
	
	String toString();

	CStringPtr toPointer();
	

	// -------------------------------------------------------------------
	// Impl
	// -------------------------------------------------------------------

    @SuppressWarnings("unchecked")
    public static int compare(CharSequence cs1, CharSequence cs2) {
        if (Objects.requireNonNull(cs1) == Objects.requireNonNull(cs2)) {
            return 0;
        }

        for (int i = 0, len = Math.min(cs1.length(), cs2.length()); i < len; i++) {
            char a = cs1.charAt(i);
            char b = cs2.charAt(i);
            if (a != b) {
                return a - b;
            }
        }

        return cs1.length() - cs2.length();
    }






}
