package com.main.aloro.core;

public class AppConstants {

	// TODO read from properties?
	public final static String APPLICATION_VERSION = "v0.1";

	/**
	 * Tick rate in FPS for the simulation. Specify -1 for "unlimited tick rate" and
	 * 0 to pause the simulation. User can increase the tick rate manually inside
	 * the simulation.
	 */
	public final static int TICK_RATE = 500;

	public final static int CHUNK_SIZE = 100;

	public final static boolean RANDOM_GENERATION = false;
	public final static double PERCENT_OF_RANDOM_GENERATION = 0.1;

}