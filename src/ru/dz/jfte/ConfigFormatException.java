package ru.dz.jfte;

public class ConfigFormatException extends Exception {

	public ConfigFormatException() {
	}

	public ConfigFormatException(String message) {
		super(message);
	}

	public ConfigFormatException(Throwable cause) {
		super(cause);
	}

	public ConfigFormatException(String message, Throwable cause) {
		super(message, cause);
	}

	public ConfigFormatException(String message, Throwable cause, boolean enableSuppression,
			boolean writableStackTrace) {
		super(message, cause, enableSuppression, writableStackTrace);
	}

}
