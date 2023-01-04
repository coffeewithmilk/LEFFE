package model;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.RenderingHints;
import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;

import javax.imageio.ImageIO;
import javax.swing.JFrame;
import javax.swing.JPanel;

public class Canvas {
	
	private static Canvas instance;
	
	BufferedImage img = null;
	private Graphics2D graphics = null;
	
	private JFrame frame = new JFrame();
	
	private boolean isRendering = false;
	
	private Canvas() {
		img = new BufferedImage(400,400,BufferedImage.TYPE_INT_ARGB);
		graphics = (Graphics2D) img.getGraphics();
		graphics.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
		graphics.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
		frame.setPreferredSize(new Dimension(400,400));
		//frame.setResizable(false);
		JPanel panel = new JPanel() {
			private static final long serialVersionUID = 6529279584648330572L;

			public void paintComponent(Graphics g) {
				Graphics2D g2d = (Graphics2D)g;
				g2d.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
				g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
				g2d.drawImage(img, 0, 0, null);
			}
		};
		frame.addWindowListener(new WindowListener() {

			@Override
			public void windowActivated(WindowEvent arg0) {}
			@Override
			public void windowClosed(WindowEvent arg0) {}
			@Override
			public void windowClosing(WindowEvent arg0) {
				hide();
			}
			@Override
			public void windowDeactivated(WindowEvent arg0) {}
			@Override
			public void windowDeiconified(WindowEvent arg0) {}
			@Override
			public void windowIconified(WindowEvent arg0) {}
			@Override
			public void windowOpened(WindowEvent arg0) {}
		});
		frame.add(panel);
		frame.pack();
	}
	
	public static Canvas getInstance() {
		if (instance == null) {
			instance = new Canvas();
		}
		return instance;
	}
	
	private void render() {
		isRendering = true;
		new Thread(() -> {
			while (isRendering) {
				frame.repaint();
				frame.revalidate();
				try {
					Thread.sleep(50);
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
			}
		}).start();
	}
	
	public static void show() {
		if(getInstance().isRendering) {
			return;
		}
		getInstance().frame.setVisible(true);
		getInstance().render();
	}
	
	public static void resize(int w, int h) {
		hide();
		Image i = getInstance().img;
		getInstance().img = new BufferedImage(w,h,BufferedImage.TYPE_INT_ARGB);
		getInstance().graphics = (Graphics2D) getInstance().img.getGraphics();
		getInstance().graphics.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
		getInstance().graphics.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
		getInstance().graphics.drawImage(i, 0, 0, null);
		getInstance().frame.getComponent(0).setPreferredSize(new Dimension(w,h));
		getInstance().frame.pack();
		show();
	}
	
	public static void hide() {
		if(!getInstance().isRendering) {
			return;
		}
		getInstance().isRendering = false;
		getInstance().frame.setVisible(false);
	}
	
	public static void saveImage(String path) {
		try {
			ImageIO.write(getInstance().img, "png", new File(path));
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	public static void setFont(String name, int size) {
		Font font = new Font(name, Font.PLAIN, size);
		getInstance().graphics.setFont(font);
	}
	
	public static void drawText(int x, int y, String text) {
		getInstance().graphics.drawString(text, x, y);
	}
	
	public static void drawImage(int x, int y, int w, int h, String path) {
		try {
			BufferedImage tempImage = ImageIO.read(new File(path));
			Image i = tempImage.getScaledInstance(w, h, Image.SCALE_SMOOTH);
			getInstance().graphics.drawImage(i, x, y, w, h, null);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public static void setColor(int r, int g, int b, int a) {
		Color c = new Color(r,g,b,a);
		getInstance().graphics.setColor(c);
	}

	public static void drawCircle(int x, int y, int w, int h) {
		getInstance().graphics.fillOval(x, y, w, h);
	}

	public static void drawRect(int x, int y, int w, int h) {
		getInstance().graphics.fillRect(x, y, w, h);
	}

}
