package com.main.aloro.core;

import com.main.aloro.log.Log;

public class ChunkManager {

	private static ChunkManager instance;
	private final Chunk[] chunks;

	private ChunkManager(final int size) {

		// we check if canvas can be divided by size, if not, we initialize chunk array
		// with another apropiated size
		// TODO impl algorithm to find apropiated size

		Log.write(Log.Constants.CHUNK_MANAGER, size + " chunks loaded.");

		chunks = new Chunk[size];
		for (int i = 0; i < chunks.length; i++) {
			int width = (int) Math.sqrt(size);
			int height = width;
			width = WindowConstants.WIDTH / width;
			height = WindowConstants.HEIGHT / height;
			chunks[i] = new Chunk(i, width, height);
		}
	}

	public int getChunkWidth() {
		return chunks[0].width;
	}

	public int getChunkHeight() {
		return chunks[0].height;
	}

	public boolean shouldCalculateChunk(final int id) {
		return chunks[id].loaded;
	}

	public int getChunksLength() {
		return chunks.length;
	}

	public void unloadChunk(final int id) {
		chunks[id].loaded = false;
	}

	public void loadChunk(final int id) {
		chunks[id].loaded = true;
		chunks[id].guard = true;
	}

	public boolean isGuarded(final int id) {
		return chunks[id].guard;
	}

	public void removeGuard(final int id) {
		chunks[id].guard = false;
	}

	public int getNumberOfHorizontalChunks() {
		return WindowConstants.WIDTH / getChunkWidth();
	}

	public int getNumberOfVerticalChunks() {
		return WindowConstants.HEIGHT / getChunkHeight();
	}

	public int getXZeroPositionOfChunk(final int id) {
		return (id * getChunkWidth()) % (WindowConstants.WIDTH);
	}

	public int getYZeroPositionOfChunk(final int id) {
		return id / (WindowConstants.WIDTH / getChunkWidth()) * getChunkHeight();
	}

	public int getAbsoluteXPosition(final int id, final int x) {
		return getXZeroPositionOfChunk(id) + x;
	}

	public int getAbsokuteYPosition(final int id, final int y) {
		return getYZeroPositionOfChunk(id) + y;
	}

	public static ChunkManager get() {
		if (instance == null) {
			throw new RuntimeException("ChunkManager is not initialized. Please use ChunkManager.initialize(int)");
		}
		return instance;
	}

	public static ChunkManager initialize(final int size) {
		if (instance == null) {
			instance = new ChunkManager(size);
		} else {
			throw new RuntimeException("Trying to re-instanciate initialized ChunkManager");
		}
		return instance;
	}

}
