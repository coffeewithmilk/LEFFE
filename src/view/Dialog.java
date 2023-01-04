package view;

import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.TextField;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.stage.Stage;

public class Dialog extends Stage {

	int width = 400, height = 400;

	public Dialog(int parentmidx, int parentmidy, TextField inputField, EventHandler<ActionEvent> onOK,
			EventHandler<ActionEvent> onCancel) {
		//super(StageStyle.TRANSPARENT);
		setTitle("Dialog window");
		setX(parentmidx - width / 2);
		setY(parentmidy - height / 2);
		setWidth(width);
		setHeight(height);

		Button okButton = new Button("OK");
		okButton.setOnAction((e) -> {
			onOK.handle(e);
			close();
		});
		okButton.setStyle("-fx-font-weight: bold; -fx-text-fill: #00FF00; -fx-background-color: #444444;");

		Button cancelButton = new Button("Cancel");
		cancelButton.setOnAction((e) -> {
			onCancel.handle(e);
			close();
		});
		
		Styles.setButtonStyles(okButton, cancelButton);
		
		cancelButton.setStyle("-fx-font-weight: bold; -fx-text-fill: #00FF00; -fx-background-color: #444444;");
		HBox buttons = new HBox(12);
		buttons.getChildren().addAll(okButton, cancelButton);
		VBox root = new VBox(200);
		root.setStyle(
				"-fx-border-width: 1pt; -fx-border-color: #FFFFFF; -fx-background-color: transparent; -fx-background-color: #222222;");

		root.setPadding(new Insets(100, 50, 50, 50));
		if (inputField != null) {
			inputField.setStyle("-fx-font-weight: bold; -fx-text-fill: #FFFFFF; -fx-background-color: #444444;");
			inputField.setFocusTraversable(false);
			root.getChildren().add(inputField);
		}
		root.getChildren().addAll(buttons);
		Scene scene = new Scene(root, width, height);
		scene.setFill(Color.TRANSPARENT);
		setScene(scene);
		show();
	}

}
