package ru.dz.jfte;

import ru.dz.jfte.Config.CurPos;
import ru.dz.jfte.Config.Obj;

public class ConfigFormatException extends Exception {
	int offset;

	public ConfigFormatException(int offset) {
	}

	public ConfigFormatException(int offset, String message) {
		super(message);
	}

	
	public ConfigFormatException(CurPos cp, String message) {
		super(message);
		offset = cp.c.getPos();
	}
	
	public ConfigFormatException(int offset, Throwable cause) {
		super(cause);
	}

	public ConfigFormatException(int offset, String message, Throwable cause) {
		super(message, cause);
	}

	public ConfigFormatException(int offset, String message, Throwable cause, boolean enableSuppression,
			boolean writableStackTrace) {
		super(message, cause, enableSuppression, writableStackTrace);
	}

	public ConfigFormatException(CurPos cp, Obj obj, String message) {
		super(message+" unknown obj type "+obj.type);
		offset = cp.c.getPos();
	}

	@Override
	public String toString() {
		return "Offset "+offset+" "+super.toString();
	}
	
}
