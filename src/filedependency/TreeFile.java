package filedependency;

import java.awt.Color;
import java.awt.Graphics;
import java.util.ArrayList;
import java.util.Random;

import model.scripting.BotFile;

public class TreeFile {
	private ArrayList<TreeFile> children = new ArrayList<>();
	private String path = "";
	public TreeFile(String path) {
		this.path = path;
	}

	private final int sizex = 200;
	private final int sizey = 50;

	private boolean contains(String path) {
		for (TreeFile tf : children) {
			if (tf.path.equals(path)) {
				return true;
			}
		}
		return false;
	}

	private static int currentx, currenty;
	public static void initExplore() {
		currentx = 0;
		currenty = 0;
	}

	Random random = new Random();

	public void explore(int curx, int cury, Graphics g, int width, int height) {
		g.setColor(new Color(221, 221, 221, 127));
		g.fillRect(currentx, currenty, sizex, sizey);
		g.setColor(new Color(20, 20, 20, 195));
		g.drawString(path, currentx + 20, currenty + sizey/2);
		//int midx = currentx + sizex / 2;
		//int midy = currenty + sizey / 2;

		int prevx = currentx;
		int prevy = currenty;


		//int starty = cury;

		try {
			String content = BotFile.loadFile("files\\"+path);
			for (String s : content.split("\n")) {
				if (s.trim().startsWith("load")) {
					String fileName = s.trim().replace("load ", "").replace("\"", "");
					TreeFile tf = new TreeFile(fileName);
					if (!contains(fileName)) {
						children.add(tf);
						currentx += sizex + 20 + new Random().nextInt(40);
						if (currentx + sizex > width) {
							currentx = new Random().nextInt(40);
							currenty += sizey + 20 + new Random().nextInt(40);
						}
						g.setColor(new Color(155, 155, 155, 125));

						int x1 = prevx + sizex / 2 + random.nextInt(sizex / 2), x2 = currentx + random.nextInt(sizex/2), y1 = prevy + sizey/2 + random.nextInt(sizey/2), y2 = currenty + random.nextInt(sizey/2);
						
						for (int i = -1; i < 2; i++) {
							g.drawLine(x1+i, y1, x2, y2);
						}


						tf.explore(0, 0, g, width, height);

					}
				}
			}
		} catch (Exception e) {
			return;
		}
	}



}
