package com.main.aloro.core;

public abstract class Window {

	public String getCompleteTitle() {
		return WindowConstants.WINDOW_TITLE_COMPLETE;
	}

	public abstract void showWindow();

	protected abstract void paintGrid();

}
