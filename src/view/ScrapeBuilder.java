package view;

import javafx.scene.Scene;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

public class ScrapeBuilder extends Stage {

	public static final String[] phrases = new String[] {
			"document",
			"querySelector('')",
			"querySelectorAll('')",
			"click()",
			"value = ''",
			"value",
			""
	};
	
	public ScrapeBuilder() {
		setTitle("Scrape Builder BETA");
		
		VBox root = new VBox();
		
		Scene scene = new Scene(root);
		setScene(scene);
		show();
	}
	
}
