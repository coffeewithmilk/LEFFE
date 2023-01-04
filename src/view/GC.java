package view;

import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;

public class GC {

	private GraphicsContext gc;
	
	double deltay = 0;
	
	public GC() {
	}
	
	public void scroll(double d) {
		deltay += d;
	}
	
	public double getDeltaY() {
		return deltay;
	}
	
	public void clearRect(int x, int y, int w, int h) {
		gc.clearRect(x, y, w, h);
	}

	public void setFill(Color col) {
		gc.setFill(col);
	}

	public void setFont(Font font) {
		gc.setFont(font);
	}

	public void fillRect(int i, int j, int width, int height) {
		gc.fillRect(i, j - deltay, width, height);
	}
	
	public void fillRectStatic(int x, int y, int w, int h) {
		gc.fillRect(x, y, w, h);
	}

	public Font getFont() {
		return gc.getFont();
	}

	public void fillText(String wss, double x, double y) {
		gc.fillText(wss, x, y - deltay);
	}

	public void setGC(GraphicsContext graphicsContext2D) {
		this.gc = graphicsContext2D;
	}

	
	
}
