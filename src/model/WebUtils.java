package model;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintWriter;
import java.net.URL;
import java.util.ArrayList;

import org.w3c.dom.Node;
import org.w3c.dom.html.HTMLDocument;
import org.w3c.dom.html.HTMLElement;
import org.w3c.dom.html.HTMLInputElement;

import javafx.application.Platform;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.paint.Color;
import javafx.scene.web.WebEngine;
import javafx.scene.web.WebView;
import model.scripting.GlobalVariables;
import model.scripting.Variable;

public class WebUtils {

	public static ArrayList<WebEngine> successEngines = new ArrayList<>();

	public static void loadURL(WebEngine we, String URL) {
		System.out.println(we.getUserAgent());
		try {
			we.load(URL);
		} catch (Exception e) {
			e.printStackTrace();
		}
		/*while (we.getLoadWorker().getProgress() < 1) {
			BotUtils.wait(100);
		}*/		
	}
	
	

	public static void setTextContentID(WebEngine we, String id, String content) {
		Platform.runLater(() -> {
			HTMLDocument dom = (HTMLDocument) we.getDocument();
			HTMLElement e = (HTMLElement) dom.getElementById(id);
			e.setAttribute("value", content);
			we.executeScript("document.getElementById('"+id+"').innerText = '" + content + "'");
			e.setNodeValue(content);
		});
	}

	private static Object awaitO;

	public static boolean isExpressionReturningTrue(WebEngine we, String exec) {
		awaitO = "Test";
		Platform.runLater(() -> {

		});
		while(awaitO.equals("Test")) {
			BotUtils.wait(100);
		}
		if (awaitO instanceof Boolean) {
			return (Boolean) awaitO;
		}
		return false;
	}

	public static String getIDContent(WebEngine we, String id) {
		HTMLDocument dom = (HTMLDocument) we.getDocument();
		HTMLElement he = (HTMLElement) dom.getElementById(id);
		String content = he.getTextContent();
		if (content != null && !content.isEmpty()) {
			return content;
		}

		String attr = he.getAttribute("value");
		if (attr != null) {
			return attr;
		}

		return "";
	}

	public static String getNameContent(WebEngine we, String name) {
		HTMLDocument dom = (HTMLDocument) we.getDocument();
		HTMLElement he = (HTMLElement) dom.getElementsByName(name).item(0);
		String content = he.getTextContent();
		if (content != null && !content.isEmpty()) {
			return content;
		}

		String attr = he.getAttribute("value");
		if (attr != null) {
			return attr;
		}

		return "";
	}

	public static void setTextContentName(WebEngine we, String name, String content) {
		HTMLDocument dom = (HTMLDocument) we.getDocument();
		Node e = dom.getElementsByName(name).item(0);
		if (e instanceof HTMLInputElement) { 
			setTextContent((HTMLInputElement) e, content);
		} else {
			HTMLElement he = (HTMLElement) e;
			he.setAttribute("value", content);
		}
	}

	private static void setTextContent(HTMLInputElement e, String content) {
		e.setValue(content);
	}

	public static void clickID(WebEngine we, String id) {
		we.executeScript("document.getElementById('"+ id +"').click();");
	}

	public static void clickName(WebEngine we, String name) {
		we.executeScript("document.getElementsByName('"+ name +"')[0].click();");
	}

	public static String executeScript(WebEngine we, String script) {
		Platform.runLater(() -> {
			we.executeScript(script);
		});
		return "";
	}

	public static String executeScriptReturn(WebEngine we, String script) {
		script = script.replace("\n", "");
		script = script.replace("\r", "");
		return we.executeScript(script).toString();
	}
	
	public static String retstr = "";
	
	public static String executeMultiLineScriptReturn(WebEngine we, String script, long waitValue) {
		waitValue = (int)((double)waitValue*GlobalVariables.WAIT_MULTIPLIER);
		if (waitValue < 1) {
			waitValue = 1;
		}
		retstr = "";
		if (script.contains("¤")) {
		String[] str = script.split("¤");
		for (int i = 0; i < str.length; i++) {
			System.out.println(str[i]);
			final String curStr = str[i];
			if (curStr.trim().isEmpty()) {continue;}
			Platform.runLater(() -> {
				retstr = we.executeScript(curStr).toString();
			});
			try {
				Thread.sleep(waitValue);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
		} else {
			retstr = we.executeScript(script).toString();
		}
		
		return retstr;
	}

	public static void executeScript(WebEngine we, ArrayList<?> value) {
		String total = "";
		for (Object s : value) {
			String ss = (String) ((Variable)s).getValue();
			total += ss + "\r\n";
		}
		executeScript(we, total);
	}

	public static Image scale(Image source, int targetWidth, int targetHeight, boolean preserveRatio) {
		ImageView imageView = new ImageView(source);
		imageView.setPreserveRatio(preserveRatio);
		imageView.setFitWidth(targetWidth);
		imageView.setFitHeight(targetHeight);
		return imageView.snapshot(null, null);
	}

	public static void main(String[] args) throws FileNotFoundException {

		print(loadIcon("file:C:\\Users\\jvs154\\Desktop\\david.jpg"));
	}

	public static void print(Image ii) throws FileNotFoundException {
		//JFXPanel jfxPanel = new JFXPanel();

		//String str = "";
		StringBuilder strb = new StringBuilder();
		//File f = new File("file:C:\\Users\\jvs154\\Desktop\\david.jpg");
		Image i = scale(ii, 400, 400, true);
		for(int y = 0; y < i.getHeight(); y++) {
			for(int x = 0; x < i.getWidth(); x++) {
				Color c = i.getPixelReader().getColor(x, y);
				double cc = (c.getRed()+c.getBlue()+c.getGreen())/3;
				if (cc < 0.1) {
					strb.append("@@@");
				} else if (cc < 0.2) {
					strb.append("###");
				} else if (cc < 0.3) {
					strb.append("%%%");
				} else if (cc < 0.4) {
					strb.append("¤¤¤");
				} else if (cc < 0.5) {
					strb.append("===");
				} else if (cc < 0.6) {
					strb.append("+++");
				} else if (cc < 0.7) {
					strb.append("---");
				} else if (cc < 0.8) {
					strb.append("...");
				} else if (cc < 0.9) {
					strb.append("'''");
				} else {
					strb.append("   ");
				}
			}
			strb.append("\n");
		}
		File ff = new File("output.txt");
		try {
			ff.createNewFile();
		} catch (IOException e) {
			e.printStackTrace();
		}
		PrintWriter pw = new PrintWriter(ff);
		pw.println(strb.toString());
		pw.flush();
		pw.close();
	}

	public static Image loadIcon(String fullPath) {
		URL url = null;
		InputStream is = null;
		try {
			url = new URL(fullPath);
			is = url.openStream();
		} catch (IOException e) {
			e.printStackTrace();
		}
		if (url != null && is != null) {
			return new Image(is);
		}
		return null;
	}

	public static Image loadIcon(WebView wv) {
		return wv.snapshot(null, null);
	}

}
