package com.main.aloro.swing;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;

import javax.swing.JComponent;
import javax.swing.JFrame;

import com.main.aloro.core.ChunkManager;
import com.main.aloro.core.Grid;
import com.main.aloro.core.Window;
import com.main.aloro.core.WindowConstants;
import com.main.aloro.log.Log;

public class WindowImpl extends Window {

	JFrame frame;
	Canvas canvas;

	public WindowImpl() {
		Log.write(Log.Constants.SWING, "Swing user interface loaded");
		frame = new JFrame(
				String.format(WindowConstants.WINDOW_TITLE_COMPLETE, deltaTime, Grid.get().getDeltaTime(), Grid.get().getGeneration()));
		frame.setLayout(new BorderLayout());
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

		canvas = new Canvas();
		frame.add(canvas, BorderLayout.CENTER);
		frame.pack();
		frame.setLocationRelativeTo(null);
	}

	@Override
	public void showWindow() {

		frame.setVisible(true);

	}

	public Runnable getPaintGridCallback() {
		return () -> {
			paintGrid();
		};
	}

	private int deltaTime = 0;
	long savedTime = System.nanoTime();

	@Override
	protected void paintGrid() {

		final long time = System.nanoTime();

		canvas.repaint();
		updateTitle();

		deltaTime = (int) ((time - savedTime) / 1e6);
		savedTime = time;
	}

	private void updateTitle() {
		frame.setTitle(
				String.format(WindowConstants.WINDOW_TITLE_COMPLETE, deltaTime, Grid.get().getDeltaTime(), Grid.get().getGeneration()));
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
			final int xPos = ChunkManager.get().getXZeroPositionOfChunk(i);
			final int yPos = ChunkManager.get().getYZeroPositionOfChunk(i);
			g.setColor(new Color(255, 0, 0, 120));
			g.drawRect(xPos, yPos, w, h);
			final String chunkData = (ChunkManager.get().shouldCalculateChunk(i) ? "LOADED " : "") + String.format("[%d]", i);
			g.drawString(chunkData, xPos + w - (9 + g.getFontMetrics().stringWidth(chunkData)), yPos + h - 10);
		}
	}

	@Override
	protected void paintComponent(final Graphics g) {
		g.setColor(Color.BLACK);
		g.fillRect(0, 0, WindowConstants.WIDTH, WindowConstants.HEIGHT);
		for (int i = 0; i < ChunkManager.get().getChunksLength(); i++) {
			final int initialXPos = ChunkManager.get().getXZeroPositionOfChunk(i);
			final int initialYPos = ChunkManager.get().getYZeroPositionOfChunk(i);

			if (!ChunkManager.get().shouldCalculateChunk(i)) {
				continue;
			}

			for (int y = initialYPos; y < initialYPos + ChunkManager.get().getChunkHeight(); y++) {
				for (int x = initialXPos; x < initialXPos + ChunkManager.get().getChunkWidth(); x++) {
					if (Grid.get().isAlive(x, y)) {
						g.setColor(Color.WHITE);
					} else {
						g.setColor(Color.BLACK);
					}
					g.drawRect(x, y, 0, 0);
				}
			}
		}
		paintChunks(g);
	}

}