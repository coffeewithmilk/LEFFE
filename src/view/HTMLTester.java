package view;

import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.layout.VBox;
import javafx.scene.web.HTMLEditor;
import javafx.scene.web.WebEngine;
import javafx.scene.web.WebView;
import javafx.stage.Stage;

public class HTMLTester extends Application{

	public static void main(String[] args) {
		launch(args);
	}

	class HTMLTest extends Stage{

		public HTMLTest() {
			he.setPrefSize(200, 200);
			
			he.setOnKeyPressed((ev) -> {
				getPlainText();
			});
			
			setScene(new Scene(new VBox(he, wv)));
			
			show();
		}
		
		private WebView wv = new WebView();
		
		private HTMLEditor he = new HTMLEditor();
		
		public String getPlainText() {
			WebEngine we = wv.getEngine();
			we.loadContent(he.getHtmlText());
			System.out.println(he.getHtmlText());
			
			
			System.out.println();
			//e.getAttribute("innerText");
			System.out.println();
			return "";
		}
		
	}

	@Override
	public void start(Stage primaryStage) throws Exception {
		primaryStage = new HTMLTest();
	}
}
