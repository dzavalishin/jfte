package ru.dz.jfte;

public enum ExResult {
	ErFAIL, ErOK;

	static ExResult ofBool(boolean b) {
		return b ? ErOK : ErFAIL;
	}
	
	static ExResult ofBoolean(boolean b) {
		return b ? ErOK : ErFAIL;
	}

	/*
	ExResult ofInt(int rc) {
		return rc != 0 ? ErOK : ErFAIL;
	}*/
}
