package filedependency;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.image.BufferedImage;

public class FileTree {
	
	public static BufferedImage expandFileTree(String filePath) {
		TreeFile tf = new TreeFile(filePath);
		BufferedImage img = new BufferedImage(1000, 1000, BufferedImage.TYPE_INT_ARGB);
		Graphics g = img.getGraphics();
		g.setColor(Color.white);
		g.fillRect(0, 0, 1000, 1000);
		TreeFile.initExplore();
		tf.explore(0, 0, g, 1000, 1000);
		return img;
	}
	
	/*public static void main(String[] args) throws IOException {
		BufferedImage img = expandFileTree("SRNya.bot");
		ImageIO.write(img, "PNG", new File("test.png"));
	}*/

}
