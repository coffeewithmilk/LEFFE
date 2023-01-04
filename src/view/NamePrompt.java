package view;

import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.input.KeyCode;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.stage.Stage;
import security.Account;

public class NamePrompt extends Stage {
	int width = 600, height = 500;

	public boolean isActive = true;
	
	public NamePrompt(int parentmidx, int parentmidy, Window window) {
		//super(StageStyle.TRANSPARENT);
		setTitle("Enter your name");
		setX(parentmidx - width / 2);
		setY(parentmidy - height / 2);
		setWidth(width);
		setHeight(height);
		

		VBox dataBox = new VBox(8);

		Label nameLabel = new Label("Enter your name:");
		nameLabel.setFont(Font.font("Consolas", 14.0));
		nameLabel.setStyle("-fx-text-fill: #00FF00;");

		TextField nameField = new TextField();
		
		dataBox.getChildren().addAll(nameLabel, nameField);

		Button OKButton = new Button("OK");
		OKButton.setOnAction((e) -> {
			if (nameField.getText().length() > 3) {
				Account.user = nameField.getText();
			}
			this.close();
			isActive = false;
		});
		
		OKButton.setStyle("-fx-font-weight: bold; -fx-text-fill: #00FF00; -fx-background-color: #444444;");
		
		nameField.setOnKeyPressed(KeyEvent -> {
			if (KeyEvent.getCode().equals(KeyCode.ENTER)) {
				OKButton.fire();
			}
		});
		
		Button cancelButton = new Button("Cancel");
		cancelButton.setOnAction((e) -> {
			this.close();
			isActive = false;
		});
		
		Styles.setButtonStyles(OKButton, cancelButton);
		
		cancelButton.requestFocus();
		cancelButton.setStyle("-fx-font-weight: bold; -fx-text-fill: #00FF00; -fx-background-color: #444444;");
		HBox buttons = new HBox(12);
		buttons.getChildren().addAll(OKButton, cancelButton);
		VBox root = new VBox(200);
		root.setStyle(
				"-fx-border-width: 1pt; -fx-border-color: #FFFFFF; -fx-background-color: transparent; -fx-background-color: #222222;");

		root.setPadding(new Insets(100, 50, 50, 50));

		root.getChildren().addAll(dataBox, buttons);
		Scene scene = new Scene(root, width, height);
		scene.setFill(Color.TRANSPARENT);
		setScene(scene);
		showAndWait();
		
	}
}
