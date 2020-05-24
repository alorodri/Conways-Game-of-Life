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
	private boolean runningSim = true;

	private int generation = 0;

	private static Grid instance;

	// stats
	private int aliveCells = 0;
	private int deadCells = 0;
	private int loadedChunks = 0;
	private int unloadedChunks = 0;

	public static Grid get() {
		if (instance == null) {
			instance = new Grid();
		}
		return instance;
	}

	private Grid() {
		for (int y = 0; y < WindowConstants.HEIGHT; y++) {
			for (int x = 0; x < WindowConstants.WIDTH; x++) {
				boolean alive = false;
				if (AppConstants.RANDOM_GENERATION) {
					final double rand = Math.random();
					if (rand > 1 - AppConstants.PERCENT_OF_RANDOM_GENERATION) {
						alive = true;
					}
				}
				firstBuffer[x][y] = alive;
				secondBuffer[x][y] = alive;
			}
		}

		ChunkManager.initialize(AppConstants.CHUNK_SIZE);
		Log.write(Log.Constants.CORE, "Chunk width: " + ChunkManager.get().getChunkWidth() + "px");
		Log.write(Log.Constants.CORE, "Chunk height: " + ChunkManager.get().getChunkHeight() + "px");

		// ACORN
		if (!AppConstants.RANDOM_GENERATION) {
			firstBuffer[500][500] = true;
			firstBuffer[501][498] = true;
			firstBuffer[501][500] = true;
			firstBuffer[503][499] = true;
			firstBuffer[504][500] = true;
			firstBuffer[505][500] = true;
			firstBuffer[506][500] = true;

			firstBuffer[600][500] = true;
			firstBuffer[601][498] = true;
			firstBuffer[601][500] = true;
			firstBuffer[603][499] = true;
			firstBuffer[604][500] = true;
			firstBuffer[605][500] = true;
			firstBuffer[606][500] = true;

			firstBuffer[700][500] = true;
			firstBuffer[701][498] = true;
			firstBuffer[701][500] = true;
			firstBuffer[703][499] = true;
			firstBuffer[704][500] = true;
			firstBuffer[705][500] = true;
			firstBuffer[706][500] = true;
		} else {
			Log.write(Log.Constants.CORE,
					String.format("Generating cells with a %.2f%% of probability", AppConstants.PERCENT_OF_RANDOM_GENERATION));
		}

	}

	public void run() {
		simulationLoop();
	}

	public void swapSimulationRunningState() {
		runningSim = !runningSim;
	}

	public boolean isSimulationRunning() {
		return runningSim;
	}

	public int getAliveCells() {
		return aliveCells;
	}

	public int getLoadedChunks() {
		return loadedChunks;
	}

	public int getUnloadedChunks() {
		return unloadedChunks;
	}

	public int getDeadCells() {
		return deadCells;
	}

	private int deltaTime = 0;

	private void simulationLoop() {
		long savedTime = System.nanoTime();
		final long time = System.nanoTime();

		doSimulation();

		paintingFirst = !paintingFirst;
		generation++;

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

	public int getDeltaTime() {
		return deltaTime;
	}

	class ComputingData {
		boolean rArray[][];
		boolean wArray[][];
		int aliveNeighbours;
		boolean chunkHasCells;
		int initialX;
		int initialY;
	}

	int aliveCellsCount = 0;
	int deadCellsCount = 0;
	int loadedChunksCount = 0;
	int unloadedChunksCount = 0;
	private void doSimulation() {

		aliveCellsCount = 0;
		deadCellsCount = 0;
		loadedChunksCount = 0;
		unloadedChunksCount = 0;

		for (int i = 0; i < ChunkManager.get().getChunksLength(); i++) {

			if (ChunkManager.get().isNotLoaded(i)) {
				unloadedChunksCount++;
				continue;
			}

			loadedChunksCount++;

			final ComputingData cd = new ComputingData();
			cd.initialX = ChunkManager.get().getXZeroPositionOfChunk(i);
			cd.initialY = ChunkManager.get().getYZeroPositionOfChunk(i);
			for (int y = cd.initialY; y < cd.initialY + ChunkManager.get().getChunkHeight(); y++) {
				for (int x = cd.initialX; x < cd.initialX + ChunkManager.get().getChunkWidth(); x++) {

					final int alive = countNeighbours(x, y);

					if (x >= WindowConstants.WIDTH || y >= WindowConstants.HEIGHT) {
						continue;
					}

					cd.aliveNeighbours = alive;

					// swap buffers
					if (paintingFirst) {
						cd.rArray = firstBuffer;
						cd.wArray = secondBuffer;
						loadChunksAndComputeCells(cd, x, y, i);
					} else {
						cd.rArray = secondBuffer;
						cd.wArray = firstBuffer;
						loadChunksAndComputeCells(cd, x, y, i);
					}
				}
			}

			if (!cd.chunkHasCells) {
				if (ChunkManager.get().isGuarded(i)) {
					ChunkManager.get().removeGuard(i);
				} else {
					ChunkManager.get().unloadChunk(i);
				}
			}
		}

		aliveCells = aliveCellsCount;
		deadCells = deadCellsCount;
		loadedChunks = loadedChunksCount;
		unloadedChunks = unloadedChunksCount;

	}

	private void loadChunksAndComputeCells(final ComputingData computingData, final int x, final int y, final int i) {
		if (computingData.rArray[x][y]) {
			final Limit shouldLoadX = shouldLoadNeighbourChunk(x % ChunkManager.get().getChunkWidth(), ChunkManager.get().getChunkWidth());
			final Limit shouldLoadY = shouldLoadNeighbourChunk(y % ChunkManager.get().getChunkHeight(),
					ChunkManager.get().getChunkHeight());

			loadChunks(Coordinate.X, shouldLoadX, computingData.initialX, i);
			loadChunks(Coordinate.Y, shouldLoadY, computingData.initialY, i);

			computingData.chunkHasCells = true;
			// alive
			if (computingData.aliveNeighbours == 2 || computingData.aliveNeighbours == 3) {
				// continues alive
				aliveCellsCount++;
				computingData.wArray[x][y] = true;
			} else {
				// dead
				deadCellsCount++;
				computingData.wArray[x][y] = false;
			}
		} else {
			// dead
			if (computingData.aliveNeighbours == 3) {
				// new cell born
				aliveCellsCount++;
				computingData.wArray[x][y] = true;
			} else {
				// continues dead
				deadCellsCount++;
				computingData.wArray[x][y] = false;
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
