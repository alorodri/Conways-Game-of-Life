package com.main.aloro.core;

import java.util.function.Supplier;

public abstract class Window {

	public String getCompleteTitle() {
		return WindowConstants.WINDOW_TITLE_COMPLETE;
	}

	public abstract void paintFPS(Supplier<Integer> r);

	public abstract void showWindow();

	protected abstract void paintGrid();

}