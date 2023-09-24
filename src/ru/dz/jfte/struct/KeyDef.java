package ru.dz.jfte.struct;

public class KeyDef {
    private final String Name;
    private final int /*TKeyCode*/ Key;
    
    public KeyDef( String name, int key )
    {
    	this.Name = name;
    	this.Key = key;
    }

	/**
	 * @return the name
	 */
	public String getName() {
		return Name;
	}

	/**
	 * @return the key
	 */
	public int /*TKeyCode*/ getKey() {
		return Key;
	}
}