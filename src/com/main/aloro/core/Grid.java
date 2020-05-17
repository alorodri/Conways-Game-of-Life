package com.main.aloro.core;

import com.main.aloro.log.Log;

public class Grid {

	private final boolean firstBuffer[][] = new boolean[WindowConstants.WIDTH][WindowConstants.HEIGHT];
	private final boolean secondBuffer[][] = new boolean[WindowConstants.WIDTH][WindowConstants.HEIGHT];

	/**
	 * That flag indicates whether the buffered matrix was sent to UI or not. If was
	 * sent, we'll paint grid (true). If wasn't sent, we'll paint the content of
	 * buffer. That's an implementation of a double buffer system.
	 */
	private boolean paintingFirst = true;

	private int generation = 0;

	private static Grid instance;

	private Runnable simulationCallback;

	public static Grid get() {
		if (instance == null) {
			instance = new Grid();
		}
		return instance;
	}

	private Grid() {
		for (int y = 0; y < WindowConstants.HEIGHT; y++) {
			for (int x = 0; x < WindowConstants.WIDTH; x++) {
				firstBuffer[x][y] = false;
				secondBuffer[x][y] = false;
			}
		}

		ChunkManager.initialize(AppConstants.CHUNK_SIZE);
		Log.write(Log.Constants.CORE, "Chunk width: " + ChunkManager.get().getChunkWidth() + "px");
		Log.write(Log.Constants.CORE, "Chunk height: " + ChunkManager.get().getChunkHeight() + "px");

		// *** POSITIONS FOR TESTING *** \\
		// ACORN
		firstBuffer[500][500] = true;
		firstBuffer[501][498] = true;
		firstBuffer[501][500] = true;
		firstBuffer[503][499] = true;
		firstBuffer[504][500] = true;
		firstBuffer[505][500] = true;
		firstBuffer[506][500] = true;

		// firstBuffer[2][5] = true;
		// firstBuffer[3][3] = true;
		// firstBuffer[3][5] = true;
		// firstBuffer[5][4] = true;
		// firstBuffer[6][5] = true;
		// firstBuffer[7][5] = true;
		// firstBuffer[8][5] = true;
		// *** POSITIONS FOR TESTING *** \\

	}

	public void run() {
		simulationLoop();
	}

	public void setSimulationCallback(final Runnable r) {
		simulationCallback = r;
	}

	private int deltaTime = 0;

	private void simulationLoop() {
		// do things
		long savedTime = System.nanoTime();
		while (true) {

			final long time = System.nanoTime();
			doSimulation();

			paintingFirst = !paintingFirst;
			generation++;
			simulationCallback.run();

			if (!(((time - savedTime) / 1e6) < 1000 / AppConstants.TICK_RATE)) {
				try {
					Thread.sleep(1000 / AppConstants.TICK_RATE);
				} catch (final InterruptedException e) {
					e.printStackTrace();
				}
			}

			deltaTime = (int) ((time - savedTime) / 1e6);
			savedTime = time;

		}
	}

	public int getDeltaTime() {
		return deltaTime;
	}

	// TODO refactor cyclomatic complexity (so much braces)
	private void doSimulation() {

		for (int i = 0; i < ChunkManager.get().getChunksLength(); i++) {
			final int initialXPos = ChunkManager.get().getXZeroPositionOfChunk(i);
			final int initialYPos = ChunkManager.get().getYZeroPositionOfChunk(i);

			// TODO refactor to not should calculate
			if (!ChunkManager.get().shouldCalculateChunk(i)) {
				continue;
			}

			boolean chunkHasCells = false;
			for (int y = initialYPos; y < initialYPos + ChunkManager.get().getChunkHeight(); y++) {
				for (int x = initialXPos; x < initialXPos + ChunkManager.get().getChunkWidth(); x++) {

					final int alive = countNeighbours(x, y);

					if (x >= WindowConstants.WIDTH || y >= WindowConstants.HEIGHT) {
						continue;
					}

					if (paintingFirst) {
						// painting first, so we edit the second buffer
						if (firstBuffer[x][y]) {
							final Limit shouldLoadX = shouldLoadNeighbourChunk(x % ChunkManager.get().getChunkWidth(),
									ChunkManager.get().getChunkWidth());
							final Limit shouldLoadY = shouldLoadNeighbourChunk(y % ChunkManager.get().getChunkHeight(),
									ChunkManager.get().getChunkHeight());

							loadChunks(Coordinate.X, shouldLoadX, initialXPos, i);
							loadChunks(Coordinate.Y, shouldLoadY, initialYPos, i);

							chunkHasCells = true;
							// alive
							if (alive == 2 || alive == 3) {
								// continues alive
								secondBuffer[x][y] = true;
							} else {
								// dead
								secondBuffer[x][y] = false;
							}
						} else {
							// dead
							if (alive == 3) {
								// new cell born
								secondBuffer[x][y] = true;
							} else {
								// continues dead
								secondBuffer[x][y] = false;
							}
						}
					} else {
						if (secondBuffer[x][y]) {
							final Limit shouldLoadX = shouldLoadNeighbourChunk(x % ChunkManager.get().getChunkWidth(),
									ChunkManager.get().getChunkWidth());
							final Limit shouldLoadY = shouldLoadNeighbourChunk(y % ChunkManager.get().getChunkHeight(),
									ChunkManager.get().getChunkHeight());

							loadChunks(Coordinate.X, shouldLoadX, initialXPos, i);
							loadChunks(Coordinate.Y, shouldLoadY, initialYPos, i);

							chunkHasCells = true;
							// alive
							if (alive == 2 || alive == 3) {
								// continues alive
								firstBuffer[x][y] = true;
							} else {
								// dead
								firstBuffer[x][y] = false;
							}
						} else {
							// dead
							if (alive == 3) {
								// new cell born
								firstBuffer[x][y] = true;
							} else {
								// continues dead
								firstBuffer[x][y] = false;
							}
						}
					}
				}
			}

			if (!chunkHasCells) {
				if (ChunkManager.get().isGuarded(i)) {
					ChunkManager.get().removeGuard(i);
				} else {
					ChunkManager.get().unloadChunk(i);
				}
			}
		}
	}

	private enum Limit {
		MIN, MAX
	}

	private enum Coordinate {
		X, Y
	}

	// TODO pass logic to ChunkManager
	private Limit shouldLoadNeighbourChunk(final int actualCell, final int maxValue) {
		if (maxValue - actualCell < 3) {
			return Limit.MAX;
		} else if (maxValue - actualCell > maxValue - 3) {
			return Limit.MIN;
		}
		return null;
	}

	// TODO pass logic to ChunkManager
	private void loadChunks(final Coordinate coord, final Limit limit, final int initialPixel, final int chunkId) {
		if (coord == Coordinate.X) {
			if (limit == Limit.MIN && initialPixel != 0) {
				ChunkManager.get().loadChunk(chunkId - 1);
			} else if (limit == Limit.MAX
					&& initialPixel != ChunkManager.get().getChunkWidth() * (ChunkManager.get().getNumberOfHorizontalChunks() - 1)) {
				ChunkManager.get().loadChunk(chunkId + 1);
			}
		} else if (coord == Coordinate.Y) {
			if (limit == Limit.MIN && initialPixel != 0) {
				ChunkManager.get().loadChunk(chunkId - ChunkManager.get().getNumberOfHorizontalChunks());
			} else if (limit == Limit.MAX
					&& initialPixel != ChunkManager.get().getChunkHeight() * (ChunkManager.get().getNumberOfVerticalChunks() - 1)) {
				ChunkManager.get().loadChunk(chunkId + ChunkManager.get().getNumberOfHorizontalChunks());
			}
		}
	}

	private int countNeighbours(final int xPos, final int yPos) {
		int aliveCount = 0;
		for (int y = yPos - 1; y < yPos + 2; y++) {
			for (int x = xPos - 1; x < xPos + 2; x++) {

				if (y == yPos && x == xPos) {
					continue;
				}

				if (y < 0 || y >= WindowConstants.HEIGHT) {
					continue;
				}

				if (x < 0 || x >= WindowConstants.WIDTH) {
					continue;
				}

				if (paintingFirst) {
					// painting grid, so we check first buffer
					if (firstBuffer[x][y]) {
						aliveCount++;
					}
				} else {
					if (secondBuffer[x][y]) {
						aliveCount++;
					}
				}
			}
		}
		return aliveCount;
	}

	public int getGeneration() {
		return generation;
	}

	public boolean isAlive(final int x, final int y) {
		if (paintingFirst) {
			return firstBuffer[x][y];
		} else {
			return secondBuffer[x][y];
		}
	}

}
