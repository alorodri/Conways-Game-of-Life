package com.main.aloro.swing;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.beans.PropertyChangeListener;
import java.util.function.Supplier;

import javax.swing.Action;
import javax.swing.JComponent;
import javax.swing.JFrame;
import javax.swing.KeyStroke;

import com.main.aloro.core.ChunkManager;
import com.main.aloro.core.Grid;
import com.main.aloro.core.Window;
import com.main.aloro.core.WindowConstants;
import com.main.aloro.log.Log;

public class WindowImpl extends Window {

	JFrame frame;
	Canvas canvas;
	private boolean fullscreenFlag = false;

	public WindowImpl() {
		Log.write(Log.Constants.SWING, "Swing user interface loaded");
		frame = new JFrame(String.format(WindowConstants.WINDOW_TITLE_COMPLETE, Grid.get().getGeneration()));
		frame.setLayout(new BorderLayout());
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

		canvas = new Canvas();
		frame.add(canvas, BorderLayout.CENTER);

		frame.pack();

		setUpBindings();

		frame.setLocationRelativeTo(null);
		frame.setExtendedState(JFrame.MAXIMIZED_BOTH);
	}

	public void setUpBindings() {

		bindKeyToFcn(KeyStroke.getKeyStroke('m'), a -> {
			swapFullScreen();
		});

		bindKeyToFcn(KeyStroke.getKeyStroke('p'), a -> {
			Grid.get().swapSimulationRunningState();
		});

		bindKeyToFcn(KeyStroke.getKeyStroke('z'), a -> {
			canvas.swapShowingData();
		});

		bindKeyToFcn(KeyStroke.getKeyStroke('c'), a -> {
			canvas.swapShowingChunks();
		});

	}

	@Override
	public void showWindow() {
		frame.setVisible(true);
	}

	private void swapFullScreen() {
		fullscreenFlag = !fullscreenFlag;
		frame.dispose();
		frame.setUndecorated(fullscreenFlag);
		frame.setVisible(true);
	}

	private void bindKeyToFcn(final KeyStroke key, final ActionListener fcn) {
		frame.getRootPane().getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).put(key, fcn.hashCode());
		frame.getRootPane().getActionMap().put(fcn.hashCode(), new WindowActionImpl(fcn));
	}

	@Override
	protected void paintGrid() {

		canvas.repaint();
		updateTitle();

	}

	private void updateTitle() {
		frame.setTitle(String.format(WindowConstants.WINDOW_TITLE_COMPLETE, Grid.get().getGeneration()));
	}

	@Override
	public void paintFPS(Supplier<Integer> supplier) {
		canvas.setFPSPainter(supplier);
	}

}

class Canvas extends JComponent {

	private static final long serialVersionUID = 1L;
	private boolean showData = false;
	private boolean showChunks = false;

	private Supplier<Integer> FPSPainter;

	public Canvas() {
		setPreferredSize(new Dimension(WindowConstants.WIDTH, WindowConstants.HEIGHT));
	}

	protected void setFPSPainter(Supplier<Integer> r) {
		FPSPainter = r;
	}

	private void paintChunks(final Graphics g) {
		final int l = ChunkManager.get().getChunksLength();
		final int w = ChunkManager.get().getChunkWidth();
		final int h = ChunkManager.get().getChunkHeight();
		g.setFont(new Font("Crisp", Font.PLAIN, 12));
		for (int i = 0; i < l; i++) {
			final int xPos = ChunkManager.get().getXZeroPositionOfChunk(i);
			final int yPos = ChunkManager.get().getYZeroPositionOfChunk(i);
			g.setColor(new Color(255, 0, 0, 120));
			g.drawRect(xPos, yPos, w, h);
			final String chunkData = (ChunkManager.get().isNotLoaded(i) ? "" : "LOADED") + String.format("[%d]", i);
			g.drawString(chunkData, xPos + w - (9 + g.getFontMetrics().stringWidth(chunkData)), yPos + h - 5);
		}
	}

	int textYPosition = 0;

	private void paintData(final Graphics g) {
		textYPosition = 15;
		g.setColor(new Color(255, 255, 255, 200));
		g.setFont(new Font("Crisp", Font.PLAIN, 12));
		if (showData) {
			drawStringData(g, "Hide data (press Z)");
			drawStringData(g, String.format("Chunks loaded: %d", Grid.get().getLoadedChunks()));
			drawStringData(g, String.format("Chunks not loaded: %d", Grid.get().getUnloadedChunks()));
			drawStringData(g, String.format("Cells alive: %,d", Grid.get().getAliveCells()));
			drawStringData(g, String.format("Cells dead: %,d", Grid.get()
					.getDeadCells()
					+ Grid.get().getUnloadedChunks() * ChunkManager.get().getChunkWidth() * ChunkManager.get().getChunkHeight()));
			drawStringData(g, String.format("Generation: %,d", Grid.get().getGeneration()));
			if (FPSPainter != null) {
				drawStringData(g, String.format("Current FPS: %d", FPSPainter.get()));
			}
		} else {
			drawStringData(g, "Show data (press Z)");
		}
		drawStringData(g, "Show/hide chunks (press C)");
		drawStringData(g, "Enter/Exit fullscreen (press M)");
	}

	int textXPosition = 10;

	private void drawStringData(final Graphics g, final String text) {
		g.drawString(text, textXPosition, textYPosition);
		textYPosition += 15;
	}

	protected void swapShowingData() {
		showData = !showData;
	}

	protected void swapShowingChunks() {
		showChunks = !showChunks;
	}

	@Override
	protected void paintComponent(final Graphics g) {
		g.setColor(Color.BLACK);
		g.fillRect(0, 0, WindowConstants.WIDTH, WindowConstants.HEIGHT);
		for (int i = 0; i < ChunkManager.get().getChunksLength(); i++) {
			final int initialXPos = ChunkManager.get().getXZeroPositionOfChunk(i);
			final int initialYPos = ChunkManager.get().getYZeroPositionOfChunk(i);

			if (ChunkManager.get().isNotLoaded(i)) {
				continue;
			}

			for (int y = initialYPos; y < initialYPos + ChunkManager.get().getChunkHeight(); y++) {
				for (int x = initialXPos; x < initialXPos + ChunkManager.get().getChunkWidth(); x++) {

					// FIXME Index 1080 out of bounds for length 1080
					if (y == 1080) {
						Log.write(Log.Constants.SWING,
								String.format("initialYPos: %d, chunk height: %d", initialYPos, ChunkManager.get().getChunkHeight()));
					}

					if (Grid.get().isAlive(x, y)) {
						g.setColor(Color.WHITE);
						g.drawRect(x, y, 0, 0);
					}
				}
			}
		}
		if (showChunks) {
			paintChunks(g);
		}
		paintData(g);
	}

}

class WindowActionImpl implements Action {

	private final ActionListener fcn;

	public WindowActionImpl(final ActionListener e) {
		fcn = e;
	}

	@Override
	public void actionPerformed(final ActionEvent e) {
		fcn.actionPerformed(e);
	}

	@Override
	public Object getValue(final String key) {
		return null;
	}

	@Override
	public void putValue(final String key, final Object value) {
	}

	@Override
	public void setEnabled(final boolean b) {
	}

	@Override
	public boolean isEnabled() {
		return true;
	}

	@Override
	public void addPropertyChangeListener(final PropertyChangeListener listener) {
	}

	@Override
	public void removePropertyChangeListener(final PropertyChangeListener listener) {
	}

}