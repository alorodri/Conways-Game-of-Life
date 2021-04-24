package com.main.aloro.core;

public class Chunk {

	final int width;
	final int height;
	boolean loaded = true;
	boolean guard = false;

	public Chunk(final int width, final int height) {
		this.width = width;
		this.height = height;
	}

	public int getWidth() {
		return width;
	}

	public int getHeight() {
		return height;
	}

}
