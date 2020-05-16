package com.main.aloro.core;

public class Core {

    public static void main(final String[] args) {
	Class<?> windowImpl;
	try {
	    windowImpl = Class.forName(WindowConstants.SWING_IMPLEMENTATION);
	    if (Window.class.isAssignableFrom(windowImpl)) {
		final Window win = (Window) windowImpl.getConstructor().newInstance();
		win.showWindow();
		Grid.get().run();
	    } else {
		throw new RuntimeException(
			"Graphical implementation isn't a correct Window implementation. Check WindowImpl.");
	    }
	} catch (final Exception e) {
	    e.printStackTrace();
	}
    }

}
