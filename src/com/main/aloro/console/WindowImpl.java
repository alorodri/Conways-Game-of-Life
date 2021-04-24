package com.main.aloro.console;

import java.util.function.Supplier;

import com.main.aloro.core.AppConstants;
import com.main.aloro.core.Grid;
import com.main.aloro.core.Window;
import com.main.aloro.core.WindowConstants;

public class WindowImpl extends Window {

	private final Thread uiPainter;

	public WindowImpl() {
		System.out.println("Loading console implementation of window.");
		uiPainter = new Thread(this::paintGrid);
	}

	@Override
	public void showWindow() {
		uiPainter.start();
		System.out.println("Console implementation ready.");
	}

	@Override
	protected void paintGrid() {
		// TODO annotations??
		if (WindowConstants.WIDTH > 10 || WindowConstants.HEIGHT > 10) {
			throw new RuntimeException("Console implementation is restricted to 10x10 max. grids. Please, specify a lower resolution");
		}

		while (true) {
			// TODO pass generation from core class, all window
			// implementations must know generation number from the same source
			System.out.println("Generation: " + Grid.get().getGeneration());
			for (int y = 0; y < WindowConstants.HEIGHT; y++) {
				for (int x = 0; x < WindowConstants.WIDTH; x++) {
					System.out.format("[%s]", Grid.get().isAlive(x, y) ? "x" : " ");
				}
				System.out.println();
			}
			System.out.println("------------------------------");

			try {
				Thread.sleep(1000 / AppConstants.TICK_RATE);
			} catch (final InterruptedException e) {
				e.printStackTrace();
			}
		}

	}

	@Override
	public void paintFPS(Supplier<Integer> r) {
		// TODO Auto-generated method stub

	}

}
