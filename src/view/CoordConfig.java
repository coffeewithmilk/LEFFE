package view;

import java.awt.MouseInfo;
import java.awt.Point;
import java.util.ArrayList;

import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.geometry.Insets;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.scene.input.KeyCode;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.stage.Stage;
import model.scripting.BotFile;
import model.scripting.Config;
import security.BusinessAccount;

public class CoordConfig extends Stage {

	int width = 600, height = 600;

	int i = 0;

	boolean isAlive = true;

	ArrayList<Config> configs;

	TextField xfield, yfield;

	TextField userField;
	PasswordField passField;

	public void listenForCoords() {
		new Thread( () -> {
			while (isAlive) {
				if (Window.IS_COORDS) {
					Window.IS_COORDS = false;
					if (configs.get(i).getType().equals("coord")) {
						Point mp = MouseInfo.getPointerInfo().getLocation();						
						Platform.runLater(() -> {
							xfield.setText("" + mp.x);
							yfield.setText("" + mp.y);
							ActionEvent e = null;
							nextButton.getOnAction().handle(e);
						});
					}
				}
				try {
					Thread.sleep(10);
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
			}
		}).start();
	}

	Button nextButton;

	Config c;
	
	public CoordConfig(int parentmidx, int parentmidy, ArrayList<Config> configs, String filename) { 
		//super(StageStyle.TRANSPARENT);
		setTitle("Set coordinates");
		setX(parentmidx - width / 2);
		setY(parentmidy - height / 2);
		setWidth(width);
		setMinHeight(height);
		this.configs = configs;
		xfield = new TextField();
		yfield = new TextField();

		xfield.setPromptText("X-coord");
		yfield.setPromptText("Y-coord");

		VBox dataBox = new VBox(8);
		Label descrlbl = new Label();
		descrlbl.setFont(Font.font("Consolas", 14.0));
		descrlbl.setStyle("-fx-text-fill: #00FF00;");

		Label namelbl = new Label();
		namelbl.setFont(Font.font("Consolas", 14.0));
		namelbl.setStyle("-fx-text-fill: #00FF00;");

		userField = new TextField();
		passField = new PasswordField();
		
		userField.setVisible(false);
		passField.setVisible(false);
		
		dataBox.getChildren().addAll(namelbl, descrlbl, xfield, yfield, userField, passField);

		namelbl.setWrapText(true);
		descrlbl.setWrapText(true);
		namelbl.setMinHeight(20);
		descrlbl.setMinHeight(60);

		namelbl.setText("Var name: " + configs.get(0).getName());
		descrlbl.setText("Var description: " + configs.get(i).getDescription());

		nextButton = new Button("Next");

		c = configs.get(i);
		
		userField.setPromptText("Username");
		passField.setPromptText("Password");
		
		if (c.getType().equals("account")) {
			xfield.setVisible(false);
			yfield.setVisible(false);
			userField.setVisible(true);
			passField.setVisible(true);
		} else if (c.getType().equals("coord")) {
			xfield.setVisible(true);
			yfield.setVisible(true);
			userField.setVisible(false);
			passField.setVisible(false);
		}

		nextButton.setOnAction((e) -> {	
			
			if (i < configs.size()) {
				c = configs.get(i);
				switch (c.getType()) {
				case "coord": configs.get(i).setValue(xfield.getText() + " " + yfield.getText()); break;
				case "account": configs.get(i).setValue(BusinessAccount.encrypt(userField.getText(), BusinessAccount.ENCRYPTION_DEPTH) + " " + BusinessAccount.encrypt(passField.getText(), BusinessAccount.ENCRYPTION_DEPTH)); break;
				}

				i++;
				if (i == configs.size()) {
					nextButton.setText("Finish");
					namelbl.setText("All variables are now set!");
					descrlbl.setText("");
					xfield.setVisible(false);
					yfield.setVisible(false);
					userField.setVisible(false);
					passField.setVisible(false);
				} else {
					descrlbl.setText("Var description: " + configs.get(i).getDescription());
					namelbl.setText("Var name: " + configs.get(i).getName());
					c = configs.get(i);
					if (c.getType().equals("account")) {
						xfield.setVisible(false);
						yfield.setVisible(false);
						userField.setVisible(true);
						passField.setVisible(true);
					} else if (c.getType().equals("coord")) {
						xfield.setVisible(true);
						yfield.setVisible(true);
						userField.setVisible(false);
						passField.setVisible(false);
					}
				}
			} else {
				BotFile.createConfigFile(filename, configs);
				isAlive = false;
				this.close();
			}
		});


		nextButton.setStyle("-fx-font-weight: bold; -fx-text-fill: #00FF00; -fx-background-color: #444444;");

		Button cancelButton = new Button("Cancel");
		cancelButton.setOnAction((e) -> {
			isAlive = false;
			configs.removeAll(configs);
			this.close();
		});
		
		Styles.setButtonStyles(nextButton, cancelButton);
		
		cancelButton.requestFocus();
		cancelButton.setStyle("-fx-font-weight: bold; -fx-text-fill: #00FF00; -fx-background-color: #444444;");
		HBox buttons = new HBox(12);
		buttons.getChildren().addAll(nextButton, cancelButton);
		VBox root = new VBox(200);
		root.setStyle(
				"-fx-border-width: 1pt; -fx-border-color: #FFFFFF; -fx-background-color: transparent; -fx-background-color: #222222;");

		root.setPadding(new Insets(50, 50, 50, 50));

		listenForCoords();

		for (Node node : dataBox.getChildren()) {
			node.setOnKeyPressed((event) -> {
				if (event.getCode().equals(KeyCode.ENTER)) {
					nextButton.fire();
				}
			});
		}
		
		root.getChildren().addAll(dataBox, buttons);
		Scene scene = new Scene(root, width, height);
		scene.setFill(Color.TRANSPARENT);
		setScene(scene);
		show();
	}

}
