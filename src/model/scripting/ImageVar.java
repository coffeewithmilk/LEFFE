package model.scripting;

import java.awt.Point;
import java.awt.image.BufferedImage;

public class ImageVar extends Variable {

	public BufferedImage img;
	
	public ImageVar(String key) {
		super(key);
	}
	
	public void setValue(BufferedImage img) {
		this.img = img;
	}
	
	public Point xyOfImage() {
		return null;
	}
	
}
