package ru.dz.jfte;

public class ExecException extends Exception {

	public ExecException() {
	}

	public ExecException(String message, Object...objects ) {
		super(String.format(message, objects));
	}
	
	public ExecException(String message) {
		super(message);
	}

	public ExecException(Throwable cause) {
		super(cause);
	}

	public ExecException(String message, Throwable cause) {
		super(message, cause);
	}

	public ExecException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
		super(message, cause, enableSuppression, writableStackTrace);
	}

}
