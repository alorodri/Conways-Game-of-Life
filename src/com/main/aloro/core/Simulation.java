package com.main.aloro.core;

public class Simulation {
	
	public Simulation() {
	}

	private int deltaTime = 0;
	private long savedTime = System.nanoTime();
	private int fpsValue = 0;

	public void run(final Grid grid, final Window render) {

		render.paintFPS(() -> fpsValue );

		while (true) {

			final long time = System.nanoTime();

			deltaTime = (int) ((time - savedTime) / 1e6);

			if(grid.isSimulationRunning()) {
				grid.run();
			}
			
			render.paintGrid();

			if (deltaTime < 1000 / AppConstants.TICK_RATE) {
				final long diff = (1000 / AppConstants.TICK_RATE) - deltaTime;
				deltaTime += diff;
				try {
					Thread.sleep(diff);
				} catch (final InterruptedException e) {
					e.printStackTrace();
				}
			}

			fpsValue = 1000 / deltaTime;

			savedTime = time;

		}
	}

}
