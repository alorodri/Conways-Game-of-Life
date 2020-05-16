package com.main.aloro.swing;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;

import javax.swing.JComponent;
import javax.swing.JFrame;

import com.main.aloro.core.AppConstants;
import com.main.aloro.core.ChunkManager;
import com.main.aloro.core.Grid;
import com.main.aloro.core.Window;
import com.main.aloro.core.WindowConstants;
import com.main.aloro.log.Log;

public class WindowImpl extends Window {

    JFrame frame;
    Canvas canvas;

    // TODO painting threads need to be ahead the window implementation (should be
    // invoked in Core.java)
    Thread uiPainter;

    public WindowImpl() {
	Log.write(Log.Constants.SWING, "Swing user interface loaded");
	frame = new JFrame(String.format(WindowConstants.WINDOW_TITLE_COMPLETE, deltaTime, Grid.get().getDeltaTime(),
		Grid.get().getGeneration()));
	frame.setLayout(new BorderLayout());
	frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

	canvas = new Canvas();
	frame.add(canvas, BorderLayout.CENTER);
	frame.pack();
	frame.setLocationRelativeTo(null);

	uiPainter = new Thread(() -> {
	    paintGrid();
	});
    }

    @Override
    public void showWindow() {

	frame.setVisible(true);
	uiPainter.start();

    }

    private int deltaTime = 0;

    @Override
    protected void paintGrid() {

	long savedTime = System.nanoTime();
	while (true) {

	    final long time = System.nanoTime();
	    canvas.repaint();
	    updateTitle();

	    try {
		Thread.sleep(1000 / AppConstants.TICK_RATE);
	    } catch (final InterruptedException e) {
		e.printStackTrace();
	    }

	    deltaTime = (int) ((time - savedTime) / 1e6);
	    savedTime = time;
	}
    }

    private void updateTitle() {
	frame.setTitle(String.format(WindowConstants.WINDOW_TITLE_COMPLETE, deltaTime, Grid.get().getDeltaTime(),
		Grid.get().getGeneration()));
    }

}

class Canvas extends JComponent {

    private static final long serialVersionUID = 1L;

    public Canvas() {
	setPreferredSize(new Dimension(WindowConstants.WIDTH, WindowConstants.HEIGHT));
    }

    protected void paintChunks(final Graphics g) {
	final int l = ChunkManager.get().getChunksLength();
	final int w = ChunkManager.get().getChunkWidth();
	final int h = ChunkManager.get().getChunkHeight();
	for (int i = 0; i < l; i++) {
	    g.setColor(new Color(255, 0, 0, 120));
	    final int xPos = ChunkManager.get().getXZeroPositionOfChunk(i);
	    final int yPos = ChunkManager.get().getYZeroPositionOfChunk(i);
	    g.drawRect(xPos, yPos, w, h);
	    final String chunkData = (ChunkManager.get().shouldCalculateChunk(i) ? "LOADED " : "")
		    + String.format("[%d]", i);
	    g.drawString(chunkData, xPos + w - (9 + g.getFontMetrics().stringWidth(chunkData)), yPos + h - 10);
	}
    }

    @Override
    protected void paintComponent(final Graphics g) {
	for (int y = 0; y < WindowConstants.HEIGHT; y++) {
	    for (int x = 0; x < WindowConstants.WIDTH; x++) {
		if (Grid.get().isAlive(x, y)) {
		    g.setColor(Color.WHITE);
		} else {
		    g.setColor(Color.BLACK);
		}
		g.drawRect(x, y, 0, 0);
	    }
	}
	paintChunks(g);
    }

}