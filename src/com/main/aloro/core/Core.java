package com.main.aloro.core;

import com.main.aloro.log.Log;

public class Core {

	public static void main(final String[] args) {
		Class<?> windowImpl;
		try {
			windowImpl = Class.forName(WindowConstants.SWING_IMPLEMENTATION);
			if (Window.class.isAssignableFrom(windowImpl)) {
				final Window win = (Window) windowImpl.getConstructor().newInstance();
				win.showWindow();
				if (win instanceof com.main.aloro.swing.WindowImpl swingWindow) {
					Grid.get().setSimulationCallback(swingWindow.getPaintGridCallback());
					Log.write(Log.Constants.CORE, "Added paintGridCallback() to simulation");
				}
				Grid.get().run();
			} else {
				throw new RuntimeException("Graphical implementation isn't a correct Window implementation. Check WindowImpl.");
			}
		} catch (final Exception e) {
			e.printStackTrace();
		}
	}

}
