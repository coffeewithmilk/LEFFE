package model;

import java.awt.AWTException;
import java.awt.Color;
import java.awt.Rectangle;
import java.awt.Robot;
import java.awt.image.BufferedImage;
import java.io.IOException;

import net.sourceforge.tess4j.Tesseract;
import net.sourceforge.tess4j.TesseractException;

public class OCRParser { 
	
	public static void main(String[] args) throws IOException, AWTException {
		System.out.println(parseImage(new Robot().createScreenCapture(new Rectangle(0, 0, 1920, 1080))));
	}
	
	public static String parseImage(BufferedImage img) {
		img = enhanceText(img);
		Tesseract t = new Tesseract();
		t.setDatapath("C:\\Users\\jvs154\\Desktop\\filer\\Tess4J-3.4.8-src\\Tess4J\\tessdata");
		//t.setLanguage("eng");
		
		String result = "";
		try {
			result = t.doOCR(img);
		} catch (TesseractException e) {
			e.printStackTrace();
		}
		return result;		
	}
	
	public static BufferedImage enhanceText(BufferedImage img) {
		BufferedImage img2 = new BufferedImage(img.getWidth(), img.getHeight(), img.getType());
		for (int x = 0; x < img.getWidth(); x++) {
			for (int y = 0; y < img.getHeight(); y++) {
				Color c = new Color(img.getRGB(x, y));
				if ((c.getRed() + c.getGreen() + c.getBlue()) / 3 < 110) {
					img2.setRGB(x, y, new Color(0,0,0).getRGB());
				} else {
					img2.setRGB(x, y, new Color(255,255,255).getRGB());
				}
			}
		}
		return img2;
	}

}
