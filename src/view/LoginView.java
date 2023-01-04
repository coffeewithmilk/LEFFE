package view;

import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.input.KeyCode;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.stage.Stage;
import security.Account;

public class LoginView extends Stage {

		int width = 600, height = 500;

		
		
		public LoginView(int parentmidx, int parentmidy, Window window) {
			//super(StageStyle.TRANSPARENT);
			setTitle("Login");
			setX(parentmidx - width / 2);
			setY(parentmidy - height / 2);
			setWidth(width);
			setHeight(height);
			

			VBox dataBox = new VBox(8);

			Label passLabel = new Label("Password:");
			passLabel.setFont(Font.font("Consolas", 14.0));
			passLabel.setStyle("-fx-text-fill: #00FF00;");

			PasswordField pf = new PasswordField();
			
			dataBox.getChildren().addAll(passLabel, pf);

			Button OKButton = new Button("OK");
			OKButton.setOnAction((e) -> {
				if (Account.tryPassword(pf.getText())) {
					Account.loggedIn = true;
					window.close();
					new Window();
				}
				this.close(); 
			});
			pf.setOnKeyPressed(KeyEvent -> {
				if (KeyEvent.getCode().equals(KeyCode.ENTER)) {
					OKButton.fire();
				}
			});
			OKButton.setStyle("-fx-font-weight: bold; -fx-text-fill: #00FF00; -fx-background-color: #444444;");
			
			Button cancelButton = new Button("Cancel");
			cancelButton.setOnAction((e) -> {
				this.close();
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
			show();
			
		}

	}
