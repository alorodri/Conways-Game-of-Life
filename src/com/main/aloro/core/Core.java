package com.main.aloro.core;

public class Core {

	public static void main(final String[] args) {
		Class<?> windowImpl;
		// Grid is a singleton, get the instance that will be used by the simulation.
		// Grid initialization (including ExecutorService) happens within Grid.get() if not already initialized.
		Grid grid = Grid.get(); // Ensures grid instance is available for finally block.
		try {
			windowImpl = Class.forName(WindowConstants.SWING_IMPLEMENTATION);
			if (Window.class.isAssignableFrom(windowImpl)) {
				final Window win = (Window) windowImpl.getConstructor().newInstance();
				win.showWindow();
				final Simulation sim = new Simulation();
				// The sim.run() method uses the same Grid instance.
				sim.run(grid, win);
			} else {
				throw new RuntimeException("Graphical implementation isn't a correct Window implementation. Check WindowImpl.");
			}
		} catch (final Exception e) {
			e.printStackTrace();
		} finally {
			if (grid != null) {
				grid.shutdownExecutor();
			}
		}
	}

}
