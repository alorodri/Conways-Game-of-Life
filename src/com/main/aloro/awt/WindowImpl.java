package com.main.aloro.awt;

import java.util.function.Supplier;

import com.main.aloro.core.Window;

public class WindowImpl extends Window {

	public WindowImpl() {
		System.out.println("Loading AWT implementation of window.");
	}

	@Override
	public void showWindow() {
		// TODO AWT implementation
	}

	@Override
	protected void paintGrid() {
		// TODO AWT implementation
	}

	@Override
	public void paintFPS(Supplier<Integer> r) {
		// TODO Auto-generated method stub

	}

}
