package com.main.aloro.core;

import com.main.aloro.log.Log;

import java.awt.*;
import java.util.concurrent.atomic.AtomicInteger;

public class ChunkManager {

	private static ChunkManager instance;
	private final Chunk[] chunks;

	private ChunkManager(final Integer size) {

		Log.write(Log.Constants.CHUNK_MANAGER, "Trying to load " + size + " chunks");

		AtomicInteger mutsize = new AtomicInteger(size);

		// we check if canvas can be divided by size, if not, we initialize chunk array
		// with another appropriated size
		final Dimension d = findAppropriatedSize(mutsize);
		final int width = d.width;
		final int height = d.height;

		// should sqrt be a int number because we've checked it on findAppropriatedSize()
		int numberOfChunksInRow = (int) Math.sqrt(mutsize.get());
		final int widthDiff = WindowConstants.WIDTH % numberOfChunksInRow;
		final int heightDiff = WindowConstants.HEIGHT % numberOfChunksInRow;

		chunks = new Chunk[mutsize.get()];
		for (int i = 0; i < chunks.length; i++) {
			if (i == numberOfChunksInRow -1) {
				chunks[i] = new Chunk(i, width + widthDiff, height);
			} else if (i == mutsize.get() - 1) {
				// last chunk
				chunks[i] = new Chunk(i, width + widthDiff, height + heightDiff);
			}
			else {
				chunks[i] = new Chunk(i, width, height);
			}
		}
	}

	private Dimension findAppropriatedSize(AtomicInteger size) {

		int numberOfChunksInRow = (int) Math.round(Math.sqrt(size.get()));

		if (size.get() != numberOfChunksInRow * numberOfChunksInRow) {
			size.set(numberOfChunksInRow * numberOfChunksInRow);
		}

		final int width = WindowConstants.WIDTH / numberOfChunksInRow;
		final int height = WindowConstants.HEIGHT / numberOfChunksInRow;

		Log.write(Log.Constants.CHUNK_MANAGER, "Calculated number of chunks to load: " + size);

		return new Dimension(width, height);
	}

	/**
	 * Get chunks width.
	 * @deprecated
	 * This method is deprecated. We should take width of each chunk, not getting chunk[0] to get
	 * the size of "all" chunks, because that's not valid when we're going to paint them.
	 * @return chunk width.
	 */
	@Deprecated
	public int getChunkWidth() {
		return chunks[0].width;
	}

	/**
	 * Get chunks height.
	 * @deprecated
	 * This method is deprecated. We should take height of each chunk, not getting chunk[0] to get
	 * the size of "all" chunks, because that's not valid when we're going to paint them.
	 * @return chunk width.
	 */
	@Deprecated
	public int getChunkHeight() {
		return chunks[0].height;
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
