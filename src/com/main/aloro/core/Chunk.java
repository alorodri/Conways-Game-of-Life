package com.main.aloro.core;

public class Chunk {

    int id;
    int width;
    int height;
    boolean loaded = true;
    boolean guard = false;

    public Chunk(final int id, final int width, final int height) {
	this.id = id;
	this.width = width;
	this.height = height;
    }

}
