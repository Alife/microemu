package org.microemu.android.media;

import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;

import javax.imageio.ImageIO;
import javax.microedition.lcdui.Canvas;
import javax.microedition.lcdui.Graphics;
import javax.microedition.lcdui.Image;
import javax.microedition.media.Control;
import javax.microedition.media.MediaException;
import javax.microedition.media.Player;
import javax.microedition.media.PlayerListener;
import javax.microedition.media.control.VideoControl;

import org.microemu.android.MEmulator;
import org.microemu.app.ui.DisplayRepaintListener;

import android.media.AudioManager;
import android.net.Uri;

public class MediaPlayer implements Player,VideoControl{

	String file="";
	public MediaPlayer() {
	}
	
	@Override
	public Control getControl(String arg0) {
		// TODO Auto-generated method stub
		return null;
	}

	public void setInput(String s) {
		if (s.startsWith("file://"))s = s.replace("file://", "");
		file = s;
	}

	@Override
	public Control[] getControls() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void realize() throws MediaException {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void prefetch() throws MediaException {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void start() throws MediaException {
		MEmulator.bgm.play(MEmulator.context, Uri.fromFile(new File(file)), true, AudioManager.STREAM_MUSIC);
		
	}

	@Override
	public void stop() throws MediaException {
		MEmulator.bgm.stop();
		
	}

	@Override
	public void deallocate() {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void close() {
		// TODO Auto-generated method stub
		
	}

	@Override
	public long setMediaTime(long paramLong) throws MediaException {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public long getMediaTime() {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public int getState() {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public long getDuration() {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public String getContentType() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void setLoopCount(int paramInt) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void addPlayerListener(PlayerListener paramPlayerListener) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void removePlayerListener(PlayerListener paramPlayerListener) {
		// TODO Auto-generated method stub
		
	}

	
	


	private int m_displayWidth = 160;
	private int m_displayHeight = 100;
	private int m_displayX = 0;
	private int m_displayY = 0;
	private int m_sourceWidth = 160;
	private int m_sourceHeight = 100;
	private Canvas m_canvas;
	private Player m_player;
	private boolean m_visible = false;
	private boolean m_started = false;
	private int m_frameCounter = 0;
	private RepaintThread m_repaintThread;
	
	private DisplayRepaintListener m_displayListener = new DisplayRepaintListener() {

		public void repaintInvoked(Object repaintObject) {
//			Graphics g = new J2SEDisplayGraphics((J2SEGraphicsSurface) repaintObject);
//			
//			m_frameCounter+=4;
//			// stop early so that we can still see the white text
//			if (m_frameCounter > 0xD0) {
//				m_frameCounter = 0;
//			}
//			int x = m_displayX - g.getTranslateX();
//			int y = m_displayY - g.getTranslateY();
//			doPaint(g, x, y, m_displayWidth, m_displayHeight);
		}
		
	};
	
	/**
	 * creates a capture control.
	 * @param player the player object
	 * @param locator the locator that was used when creating the player.
	 * 
	 */
	public MediaPlayer(Player player, String locator) {
		m_player = player;
		// TODO we should parse the locator in order to setup the
		// original video size properly.
	}

	/* (non-Javadoc)
	 * @see javax.microedition.media.control.VideoControl#getDisplayHeight()
	 */
	public int getDisplayHeight() {
		return m_displayHeight;
	}

	/* (non-Javadoc)
	 * @see javax.microedition.media.control.VideoControl#getDisplayWidth()
	 */
	public int getDisplayWidth() {
		return m_displayHeight;
	}

	/* (non-Javadoc)
	 * @see javax.microedition.media.control.VideoControl#getDisplayX()
	 */
	public int getDisplayX() {
		return m_displayX;
	}

	/* (non-Javadoc)
	 * @see javax.microedition.media.control.VideoControl#getDisplayY()
	 */
	public int getDisplayY() {
		return m_displayY;
	}

	/* (non-Javadoc)
	 * @see javax.microedition.media.control.VideoControl#getSnapshot(java.lang.String)
	 */
	public byte[] getSnapshot(String s) throws MediaException {
		BufferedImage image = new BufferedImage(m_sourceWidth, m_sourceHeight, BufferedImage.TYPE_INT_ARGB);
		Image captureImage = Image.createImage(m_sourceWidth, m_sourceHeight);
		// paint the simulated video frame onto a bitmap
		doPaint(captureImage.getGraphics(), 0, 0, m_sourceWidth, 
				m_sourceHeight);
		int[] argb = new int[m_sourceWidth * m_sourceHeight];
		// now read the RGB array
		captureImage.getRGB(argb, 0, m_sourceWidth, 0, 0, m_sourceWidth, 
				m_sourceHeight);
		// paint it onto the BufferedImage which we need to convert to JPEG
		image.setRGB(0, 0, m_sourceWidth, m_sourceHeight, argb, 0, 
				m_sourceWidth);
		ByteArrayOutputStream bos = new ByteArrayOutputStream(argb.length * 4);
		try {
			// convert the image to JPEG
			ImageIO.write(image, "JPG", bos);
			return bos.toByteArray();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} finally {
			try {
			    if (bos != null) {
			    	bos.close();
			    }
			} catch (Throwable e) {
				// ignore
			}
		}
		return null;
	}

	/* (non-Javadoc)
	 * @see javax.microedition.media.control.VideoControl#getSourceHeight()
	 */
	public int getSourceHeight() {
		return m_sourceHeight;
	}

	/* (non-Javadoc)
	 * @see javax.microedition.media.control.VideoControl#getSourceWidth()
	 */
	public int getSourceWidth() {
		return m_sourceWidth;
	}

	/* (non-Javadoc)
	 * @see javax.microedition.media.control.VideoControl#initDisplayMode(int, java.lang.Object)
	 */
	public Object initDisplayMode(int mode, Object obj) {
		if (mode != USE_DIRECT_VIDEO) {
			throw new IllegalArgumentException(
					"only USE_DIRECT_VIDEO supported for now, requested: " + mode);
		}
		Canvas canvas = (Canvas) obj;
		m_canvas = canvas;
		if (m_player.getState() == Player.STARTED) {
			startVideo();
		}
		return null;
	}

	/* (non-Javadoc)
	 * @see javax.microedition.media.control.VideoControl#setDisplayFullScreen(boolean)
	 */
	public void setDisplayFullScreen(boolean flag) throws MediaException {
		// TODO Auto-generated method stub

	}

	/* (non-Javadoc)
	 * @see javax.microedition.media.control.VideoControl#setDisplayLocation(int, int)
	 */
	public void setDisplayLocation(int x, int y) {
		m_displayX = x;
		m_displayY = y;
		repaintIfVisible();
	}

	/* (non-Javadoc)
	 * @see javax.microedition.media.control.VideoControl#setDisplaySize(int, int)
	 */
	public void setDisplaySize(int width, int height) throws MediaException {
		m_displayWidth = width;
		m_displayHeight = height;
		repaintIfVisible();
	}

	private void repaintIfVisible() {
		if (m_visible && m_started && m_canvas != null) {
			m_canvas.repaint();
		}
	}

	/* (non-Javadoc)
	 * @see javax.microedition.media.control.VideoControl#setVisible(boolean)
	 */
	public void setVisible(boolean flag) {
		m_visible = flag;
		if (m_repaintThread == null) {
			doStartVideo();
		}
	}

	void startVideo() {
		m_started = true;
		doStartVideo();
	}

	private synchronized void doStartVideo() {
		if (m_started && m_visible && m_canvas != null) {
//			SwingDisplayComponent dc = (SwingDisplayComponent) DeviceImpl.getEmulatorContext().getDisplayComponent();
//			dc.addDisplayRepaintListener(m_displayListener);
//			m_repaintThread = new RepaintThread();
//			m_repaintThread.start();
		}
	}

	void stopVideo() {
		m_started = false;
		doStopVideo();
	}

	private synchronized void doStopVideo() {
		if (m_repaintThread != null) {
			m_repaintThread.stopRepaintThread();
			m_repaintThread = null;
		}
		if (m_started && m_visible) {
			return;
		}
		if (m_canvas != null) {
//			SwingDisplayComponent dc = (SwingDisplayComponent) DeviceImpl.getEmulatorContext().getDisplayComponent();
//			dc.removeDisplayRepaintListener(m_displayListener);
//			m_canvas.repaint();
		}
	}

	private void doPaint(Graphics g, int x, int y, int width, int height) {
		g.setColor(m_frameCounter, m_frameCounter, m_frameCounter);
		g.fillRect(x, y, width, height);
		g.setColor(0xFF4040);
		g.drawRect(x, y, width, height);
		g.drawLine(x, y, x + width, y + height);
		g.drawLine(x + width, y, x, y + height);
		g.setColor(0xFFFFFF);
		g.drawString("Video:" + m_displayX + "," + m_displayY + "," + m_displayWidth + "," + m_displayHeight, x, y, Graphics.LEFT | Graphics.TOP);
	}

	private class RepaintThread extends Thread {
		private volatile boolean m_stopped = false;
		
		public void stopRepaintThread() {
			m_stopped = true;
		}
		public void run() {
			while (!m_stopped) {
				m_canvas.repaint(m_displayX, m_displayY, m_displayWidth, m_displayHeight);
				try {
					if (!m_stopped) {
						sleep(100);
					}
				} catch (InterruptedException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		}
	}

	
}
