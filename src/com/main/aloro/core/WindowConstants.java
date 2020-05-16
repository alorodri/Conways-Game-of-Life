package com.main.aloro.core;

public class WindowConstants {

	public final static int WIDTH = 1920;
	public final static int HEIGHT = 1080;

	public final static char SPACE = ' ';

	public final static String WINDOW_TITLE = "Conway's Game of Life";
	public final static String APPLICATION_VERSION = AppConstants.APPLICATION_VERSION;
	/**
	 * FPS text showing as [%dfps]. Needs to be formatted to show the number
	 * with String.format
	 */
	public final static String INFO_FPS = "[%dms repaint][%dms sim]";
	public final static String GENERATION = "Gen: %d";
	/**
	 * Application title + FPS showing
	 */
	public final static String WINDOW_TITLE_FPS = WINDOW_TITLE + SPACE + INFO_FPS;
	public final static String WINDOW_TITLE_COMPLETE = WINDOW_TITLE + SPACE + APPLICATION_VERSION + SPACE + INFO_FPS + SPACE + GENERATION;

	public final static String AWT_IMPLEMENTATION = "com.main.aloro.awt.WindowImpl";
	public final static String SWING_IMPLEMENTATION = "com.main.aloro.swing.WindowImpl";
	public final static String CONSOLE_IMPLEMENTATION = "com.main.aloro.console.WindowImpl";

}
