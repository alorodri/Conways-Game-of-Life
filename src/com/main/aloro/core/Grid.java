package com.main.aloro.core;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import com.main.aloro.log.Log;

public class Grid {

	private ExecutorService executorService;
	private final boolean[][] firstBuffer = new boolean[WindowConstants.WIDTH][WindowConstants.HEIGHT];
	private final boolean[][] secondBuffer = new boolean[WindowConstants.WIDTH][WindowConstants.HEIGHT];

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
	private AtomicInteger aliveCells = new AtomicInteger(0);
	private AtomicInteger deadCells = new AtomicInteger(0);
	private AtomicInteger loadedChunks = new AtomicInteger(0);
	private AtomicInteger unloadedChunks = new AtomicInteger(0);

	public static Grid get() {
		if (instance == null) {
			instance = new Grid();
		}
		return instance;
	}

	private Grid() {
		executorService = Executors.newFixedThreadPool(Runtime.getRuntime().availableProcessors());
		for (int y = 0; y < WindowConstants.HEIGHT; ++y) {
			for (int x = 0; x < WindowConstants.WIDTH; ++x) {
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
		Log.write(Log.Constants.CORE, "Chunk width: " + ChunkManager.get().getChunkWidth(0) + "px");
		Log.write(Log.Constants.CORE, "Chunk height: " + ChunkManager.get().getChunkHeight(0) + "px");

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
		return aliveCells.get();
	}

	public int getLoadedChunks() {
		return loadedChunks.get();
	}

	public int getUnloadedChunks() {
		return unloadedChunks.get();
	}

	public int getDeadCells() {
		return deadCells.get();
	}

	private void simulationLoop() {

		doSimulation();

		paintingFirst = !paintingFirst;
		generation++;

	}

	static class ComputingData {
		boolean[][] rArray;
		boolean[][] wArray;
		int aliveNeighbours;
		boolean chunkHasCells;
		int initialX;
		int initialY;
	}

	// Using AtomicInteger for thread-safe counters during parallel processing
	private AtomicInteger aliveCellsCountInSim = new AtomicInteger(0);
	private AtomicInteger deadCellsCountInSim = new AtomicInteger(0);
	private AtomicInteger loadedChunksCountInSim = new AtomicInteger(0);
	private AtomicInteger unloadedChunksCountInSim = new AtomicInteger(0);

	private void doSimulation() {

		aliveCellsCountInSim.set(0);
		deadCellsCountInSim.set(0);
		loadedChunksCountInSim.set(0);
		unloadedChunksCountInSim.set(0);

		final int numChunks = ChunkManager.get().getChunksLength();
		if (numChunks == 0) { // Handle case with no chunks
			aliveCells.set(0);
			deadCells.set(0);
			loadedChunks.set(0);
			unloadedChunks.set(0);
			return;
		}
		final CountDownLatch latch = new CountDownLatch(numChunks);

		for (int i = 0; i < numChunks; ++i) {
			final int chunkId = i;
			executorService.submit(() -> {
				try {
					processSingleChunk(chunkId);
				} finally {
					latch.countDown();
				}
			});
		}

		try {
			latch.await();
		} catch (InterruptedException e) {
			Log.write(Log.Constants.ERROR, "Simulation interrupted during latch await: " + e.getMessage());
			Thread.currentThread().interrupt(); // Restore interrupted status
		}

		aliveCells.set(aliveCellsCountInSim.get());
		deadCells.set(deadCellsCountInSim.get());
		loadedChunks.set(loadedChunksCountInSim.get());
		unloadedChunks.set(unloadedChunksCountInSim.get());

	}

	private void processSingleChunk(int chunkId) {
		if (ChunkManager.get().isNotLoaded(chunkId)) {
			unloadedChunksCountInSim.incrementAndGet();
			return;
		}

		loadedChunksCountInSim.incrementAndGet();

		final ComputingData cd = new ComputingData();
		cd.initialX = ChunkManager.get().getXZeroPositionOfChunk(chunkId);
		cd.initialY = ChunkManager.get().getYZeroPositionOfChunk(chunkId);
		for (int y = cd.initialY; y < cd.initialY + ChunkManager.get().getChunkHeight(chunkId); ++y) {
			for (int x = cd.initialX; x < cd.initialX + ChunkManager.get().getChunkWidth(chunkId); ++x) {

				final int alive = countNeighbours(x, y);

				if (x >= WindowConstants.WIDTH || y >= WindowConstants.HEIGHT) {
					continue;
				}

				cd.aliveNeighbours = alive;

				// swap buffers
				if (paintingFirst) {
					cd.rArray = firstBuffer;
					cd.wArray = secondBuffer;
				} else {
					cd.rArray = secondBuffer;
					cd.wArray = firstBuffer;
				}
				loadChunksAndComputeCells(cd, x, y, chunkId);
			}
		}

		if (!cd.chunkHasCells) {
			if (ChunkManager.get().isGuarded(chunkId)) {
				ChunkManager.get().removeGuard(chunkId);
			} else {
				ChunkManager.get().unloadChunk(chunkId);
			}
		}
	}

	private void loadChunksAndComputeCells(final ComputingData computingData, final int x, final int y, final int chunkId) {
		if (computingData.rArray[x][y]) {
			final Limit shouldLoadX = shouldLoadNeighbourChunk(x % ChunkManager.get().getChunkWidth(chunkId), ChunkManager.get().getChunkWidth(chunkId));
			final Limit shouldLoadY = shouldLoadNeighbourChunk(y % ChunkManager.get().getChunkHeight(chunkId),
					ChunkManager.get().getChunkHeight(chunkId));

			loadChunks(Coordinate.X, shouldLoadX, computingData.initialX, chunkId);
			loadChunks(Coordinate.Y, shouldLoadY, computingData.initialY, chunkId);

			computingData.chunkHasCells = true;
			// alive
			if (computingData.aliveNeighbours == 2 || computingData.aliveNeighbours == 3) {
				// continues alive
				aliveCellsCountInSim.incrementAndGet();
				computingData.wArray[x][y] = true;
			} else {
				// dead
				deadCellsCountInSim.incrementAndGet(); // this will change with chunks cache
				computingData.wArray[x][y] = false;
			}
		} else {
			// dead
			if (computingData.aliveNeighbours == 3) {
				// new cell born
				aliveCellsCountInSim.incrementAndGet();
				computingData.wArray[x][y] = true;
			} else {
				// continues dead
				deadCellsCountInSim.incrementAndGet(); // this will change with chunks cache
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
					&& initialPixel != ChunkManager.get().getChunkWidth(chunkId) * (ChunkManager.get().getNumberOfHorizontalChunks() - 1)) {
				ChunkManager.get().loadChunk(chunkId + 1);
			}
		} else if (coord == Coordinate.Y) {
			if (limit == Limit.MIN && initialPixel != 0) {
				ChunkManager.get().loadChunk(chunkId - ChunkManager.get().getNumberOfHorizontalChunks());
			} else if (limit == Limit.MAX
					&& initialPixel != ChunkManager.get().getChunkHeight(chunkId) * (ChunkManager.get().getNumberOfVerticalChunks() - 1)) {
				ChunkManager.get().loadChunk(chunkId + ChunkManager.get().getNumberOfHorizontalChunks());
			}
		}
	}

	private int countNeighbours(final int xPos, final int yPos) {
		int aliveCount = 0;
		for (int y = yPos - 1; y < yPos + 2; ++y) {
			for (int x = xPos - 1; x < xPos + 2; ++x) {

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

	public void shutdownExecutor() {
		if (executorService != null && !executorService.isShutdown()) {
			executorService.shutdown(); // Disable new tasks from being submitted
			try {
				// Wait a while for existing tasks to terminate
				if (!executorService.awaitTermination(5, TimeUnit.SECONDS)) {
					executorService.shutdownNow(); // Cancel currently executing tasks
					// Wait a while for tasks to respond to being cancelled
					if (!executorService.awaitTermination(5, TimeUnit.SECONDS)) {
						Log.write(Log.Constants.ERROR, "ExecutorService did not terminate.");
					}
				}
			} catch (InterruptedException ie) {
				// (Re-)Cancel if current thread also interrupted
				executorService.shutdownNow();
				// Preserve interrupt status
				Thread.currentThread().interrupt();
				Log.write(Log.Constants.CORE, "ExecutorService shutdown interrupted.");
			}
		}
	}

}
