package model;

import java.awt.AWTException;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.MouseInfo;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.Robot;
import java.awt.Toolkit;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.StringSelection;
import java.awt.event.MouseEvent;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;

import javax.imageio.ImageIO;

import com.sun.glass.events.KeyEvent;

import model.scripting.GlobalVariables;
import view.Window;

public class BotUtils {
	private static Robot bot;

	public static void checkBotInit() {
		if (bot == null) {
			try {
				bot = new Robot();
			} catch (AWTException e) {
				e.printStackTrace();
			}
		}
	}
	
	public static void createScreenshot(String name) {
		checkBotInit();
		if (!name.endsWith(".png")) {
			name = name + ".png";
		}
		Dimension d = Toolkit.getDefaultToolkit().getScreenSize();
		Rectangle r = new Rectangle(d.width * 3, d.height);
		BufferedImage img = bot.createScreenCapture(r);
		System.out.println(img.getWidth());
		File fi = new File("screenshots");
		System.out.println(fi.mkdir());
		File file = new File("screenshots/"+name);
		file.mkdirs();
		try {
			ImageIO.write(img, "png", file);
		} catch (IOException e) {
			System.out.println(e);
		}
	}

	public static Color getPixelColor(int x, int y) {
		checkBotInit();
		return bot.getPixelColor(x, y);
	}
	
	public static void mouseMove(double x, double y, boolean smooth) {
		checkBotInit();
		double tempY = Toolkit.getDefaultToolkit().getScreenSize().getHeight() / 2;
		bot.mouseMove((int) MouseInfo.getPointerInfo().getLocation().getX(), (int) tempY);
		try {
			Thread.sleep(10);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		bot.mouseMove((int) x, (int) tempY);
		try {
			Thread.sleep(10);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		bot.mouseMove((int) x, (int) y);
	}

	public static void leftClick() {
		checkBotInit();
		bot.mousePress(MouseEvent.BUTTON1_DOWN_MASK);
		wait(100);
		bot.mouseRelease(MouseEvent.BUTTON1_DOWN_MASK);
	}

	public static void key(int... kcs) {
		checkBotInit();
		for (int kc : kcs) {
			bot.keyPress(kc);
		}
		wait(100);
		for (int i = kcs.length - 1; i >= 0; i--) {
			bot.keyRelease(kcs[i]);
		}
	}

	public static void keyDown(int[] kcs) {
		checkBotInit();
		for (int kc : kcs) {
			bot.keyPress(kc);
		}
	}

	public static void keyUp(int[] kcs) {
		checkBotInit();
		for (int i = kcs.length - 1; i >= 0; i--) {
			bot.keyRelease(kcs[i]);
		}
	}

	public static void keyPress(int kc) {
		checkBotInit();
		bot.keyPress(kc);
	}

	public static void keyRelease(int kc) {
		checkBotInit();
		bot.keyRelease(kc);
	}

	public static void rightClick() {
		checkBotInit();
		bot.mousePress(MouseEvent.BUTTON3_DOWN_MASK);
		try {
			Thread.sleep(100);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		bot.mouseRelease(MouseEvent.BUTTON3_DOWN_MASK);
	}

	public static void wait(int millis) {
		checkBotInit();
		try {
			millis = (int)((double)millis*GlobalVariables.WAIT_MULTIPLIER);
			if (millis <= 0) {
				millis = 1;
			}
			Thread.sleep(millis);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
	}

	private static int attempts = 0;
	
	public static String getClipboard() {
		String clipboard = "";
		try {
			clipboard = (String) Toolkit.getDefaultToolkit().getSystemClipboard().getData(DataFlavor.stringFlavor);
		} catch (Exception e) {
			wait(300);
			attempts++;
			if (attempts > 5) {
				attempts = 0;
				return "";
			}
			return getClipboard();
		}
		return clipboard;
	}

	public static void setClipboard(String string) {
		StringSelection str = new StringSelection(string);
		try {
			Toolkit.getDefaultToolkit().getSystemClipboard().setContents(str, str);
		} catch (Exception e) {
			wait(300);
			setClipboard(string);
		}
	}

	public static void writeText(String text) {
		String currClip = getClipboard();
		setClipboard(text);
		key(KeyEvent.VK_CONTROL, KeyEvent.VK_V);
		setClipboard(currClip);
	}

	public static void leftDown() {
		checkBotInit();
		bot.mousePress(MouseEvent.BUTTON1_DOWN_MASK);
	}

	public static void leftUp() {
		checkBotInit();
		bot.mouseRelease(MouseEvent.BUTTON1_DOWN_MASK);
	}

	public static void rightDown() {
		checkBotInit();
		bot.mousePress(MouseEvent.BUTTON3_DOWN_MASK);
	}

	public static void rightUp() {
		checkBotInit();
		bot.mouseRelease(MouseEvent.BUTTON3_DOWN_MASK);
	}

	public static Color colorAt(int x, int y) {
		checkBotInit();
		return bot.getPixelColor(x, y);
	}

	public static Point colorPosition(int r, int g, int b, int minx, int miny, int maxx, int maxy) {
		checkBotInit();
		Color c1 = new Color(r, g, b);
		BufferedImage bimg = bot.createScreenCapture(new Rectangle(minx, miny, maxx - minx, maxy - miny));
		for (int x = 0; x < bimg.getWidth(); x++) {
			for (int y = 0; y < bimg.getHeight(); y++) {
				Color c2 = new Color(bimg.getRGB(x, y));
				if (c1.equals(c2)) {
					return new Point(minx+x, miny+y);
				}
			}
		}
		return null;
	}

	public static void smoothMove(double x, double y, boolean b) {
		double tempY = Toolkit.getDefaultToolkit().getScreenSize().getHeight() / 2;
		smoothMove(MouseInfo.getPointerInfo().getLocation().getX(), tempY);
		smoothMove((int) x, (int) tempY);
		smoothMove((int) x, (int) y);
	}
	
	public static void smoothMove(double x, double y) {
		checkBotInit();
		double mx, my;
		Point p = MouseInfo.getPointerInfo().getLocation();
		mx = p.getX();
		my = p.getY();
		double dx, dy, hy;
		dx = x - mx;
		dy = y - my;
		hy = p.distance(x, y);
		double xframe = dx / hy;
		double yframe = dy / hy;

		while (p.distance(x, y) > 4 && Window.IS_ALIVE) {
			p = MouseInfo.getPointerInfo().getLocation();
			mx = p.getX();
			my = p.getY();
			dx = x - mx;
			dy = y - my;
			hy = p.distance(x, y);
			xframe = dx / hy * 5;
			yframe = dy / hy * 5;
			bot.mouseMove((int) (p.x + xframe), (int) (p.y + yframe));
			try {
				Thread.sleep(3);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}

		bot.mouseMove((int) x, (int) y);
	}

	public static void scroll(double arithmetic) {
		arithmetic = -arithmetic;
		int scr = 1;
		if (arithmetic < 0) {
			scr = -1;
		}
		int scrollamount = (int) Math.abs(arithmetic);
		for (int i = 0; i < scrollamount; i++) {
			bot.mouseWheel(scr);
			try {
				Thread.sleep(5);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
	}

	public static void cmdExec(String txt) {
		try {
			Runtime.getRuntime().exec("cmd.exe /c " + txt);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

}
