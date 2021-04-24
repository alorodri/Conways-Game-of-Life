package com.main.aloro.core;

import com.main.aloro.log.Log;

import java.awt.*;
import java.util.concurrent.atomic.AtomicInteger;

public class ChunkManager {

	private static ChunkManager instance;
	private final Chunk[] chunks;

	int numberOfChunksInRow = -1;
	int widthDiff;
	int heightDiff;

	private ChunkManager(final Integer size) {

		Log.write(Log.Constants.CHUNK_MANAGER, "Trying to load " + size + " chunks");

		AtomicInteger mutsize = new AtomicInteger(size);

		// we check if canvas can be divided by size, if not, we initialize chunk array
		// with another appropriated size
		final Dimension d = findAppropriatedSize(mutsize);
		final int width = d.width;
		final int height = d.height;

		// should sqrt be a int number because we've checked it on findAppropriatedSize()
		widthDiff = WindowConstants.WIDTH % numberOfChunksInRow;
		heightDiff = WindowConstants.HEIGHT % numberOfChunksInRow;

		chunks = new Chunk[mutsize.get()];
		for (int i = 0; i < chunks.length; i++) {
			int finalWidth = width;
			int finalHeight = height;

			if (i % numberOfChunksInRow == numberOfChunksInRow - 1) {
				finalWidth = width + widthDiff;
			}

			if (i / numberOfChunksInRow == numberOfChunksInRow - 1) {
				// last row of chunks
				finalHeight = height + heightDiff;
			}
			chunks[i] = new Chunk(finalWidth, finalHeight);
		}
	}

	private Dimension findAppropriatedSize(AtomicInteger size) {

		numberOfChunksInRow = (int) Math.round(Math.sqrt(size.get()));

		if (size.get() != numberOfChunksInRow * numberOfChunksInRow) {
			size.set(numberOfChunksInRow * numberOfChunksInRow);
		}

		final int width = WindowConstants.WIDTH / numberOfChunksInRow;
		final int height = WindowConstants.HEIGHT / numberOfChunksInRow;

		Log.write(Log.Constants.CHUNK_MANAGER, "Calculated number of chunks to load: " + size);

		return new Dimension(width, height);
	}

	public Chunk getChunk(final int chunkId) {
		return chunks[chunkId];
	}

	public int getChunkWidth(final int chunkId) {
		return chunks[chunkId].width;
	}

	public int getChunkHeight(final int chunkId) {
		return chunks[chunkId].height;
	}

	public boolean isNotLoaded(final int id) {
		return !chunks[id].loaded;
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
		return WindowConstants.WIDTH / getChunkWidth(0);
	}

	public int getNumberOfVerticalChunks() {
		return WindowConstants.HEIGHT / getChunkHeight(0);
	}

	public int getXZeroPositionOfChunk(final int id) {
		final int yn = id / numberOfChunksInRow;
		if (id == numberOfChunksInRow) {
			return (id * getChunkWidth(0) + widthDiff) % (WindowConstants.WIDTH);
		} else if (yn >= 1) {
			return (id * getChunkWidth(0) + widthDiff * yn) % (WindowConstants.WIDTH);
		}
		return (id * getChunkWidth(0)) % (WindowConstants.WIDTH);
	}

	public int getYZeroPositionOfChunk(final int id) {
		return id / (WindowConstants.WIDTH / getChunkWidth(0)) * getChunkHeight(0);
	}

	public int getAbsoluteXPosition(final int id, final int x) {
		return getXZeroPositionOfChunk(id) + x;
	}

	public int getAbsoluteYPosition(final int id, final int y) {
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
			throw new RuntimeException("Trying to re-instantiate initialized ChunkManager");
		}
		return instance;
	}

}
