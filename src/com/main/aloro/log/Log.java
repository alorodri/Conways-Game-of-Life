package com.main.aloro.log;

import java.util.Date;

public class Log {

    public static void write(final String module, final String msg) {
	System.out.format("%s: [%s] %s\n", writeDate(), module, msg);
    }

    private static String writeDate() {
	return new Date().toString();
    }

    static public class Constants {
	public static final String SWING = "SWING";
	public static final String AWT = "AWT";
	public static final String CORE = "CORE";
	public static final String CONSOLE = "CONSOLE";
	public static final String WINDOW = "WINDOW";
	public static final String CHUNK_MANAGER = "CHUNK_MANAGER";
	public static final String CHUNK_REGION = "CHUNK_REGION_%d";
    }
}
