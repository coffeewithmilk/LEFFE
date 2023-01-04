package view;

import javafx.scene.control.Button;

public class Styles {
	
	public static void setButtonStyles(Button... buttons) {
		for (Button b : buttons) {
			b.setOnMouseEntered((e) -> {
				b.setStyle("-fx-background-color: #446644; -fx-text-fill: #44FF44; -fx-font-weight: bold;");
			});
			b.setOnMouseExited((e) -> {
				b.setStyle("-fx-background-color: #444444; -fx-text-fill: #00FF00; -fx-font-weight: bold;");
			});
			b.setOnMouseClicked((e) -> {
				b.setStyle("-fx-background-color: #00FF00; -fx-text-fill: #004400; -fx-font-weight: bold;");
			});
		}
	}

}
