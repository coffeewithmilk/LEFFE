package view;

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
import security.BusinessAccount;

public class Prompt extends Stage {

	private Type type;
	private double width = 600;
	private double height = 600;

	//ACCOUNT
	TextField userField = new TextField();
	PasswordField passField = new PasswordField();

	//STRING
	TextField strField = new TextField();

	//NUM
	TextField numField = new TextField();

	//COORD
	TextField xField = new TextField();
	TextField yField = new TextField();

	public Prompt(Type t, int parentmidx, int parentmidy, String name, String description) {
		//super(StageStyle.TRANSPARENT);
		setTitle("Enter input for: " + name);
		this.type = t; 

		setTitle("Enter input for variable: " + name);
		
		Button nextButton = new Button("OK");
		nextButton.setStyle("-fx-font-weight: bold; -fx-text-fill: #00FF00; -fx-background-color: #444444;");
		Button cancelButton = new Button("Cancel");
		cancelButton.setOnAction((e) -> {
			value = " ";
			Window.IS_ALIVE = false;
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

		VBox dataBox = new VBox(8);

		Label descrlbl = new Label(description);
		descrlbl.setFont(Font.font("Consolas", 14.0));
		descrlbl.setStyle("-fx-text-fill: #00FF00;");

		Label namelbl = new Label(name);
		namelbl.setFont(Font.font("Consolas", 14.0));
		namelbl.setStyle("-fx-text-fill: #00FF00;");
		
		dataBox.getChildren().addAll(namelbl, descrlbl);
		
		switch(type) {
		
		case ACCOUNT:
			dataBox.getChildren().addAll(userField, passField);
			userField.setPromptText("Username");
			passField.setPromptText("Password");
			nextButton.setOnAction((ActionEvent) -> {
				value = userField.getText() + " " + passField.getText();
				close();
			});
			break;
		case STRING:
			dataBox.getChildren().addAll(strField);
			strField.setPromptText("String value");
			nextButton.setOnAction((ActionEvent) -> {
				value = strField.getText(); 
				close();
			});
			break;
		case NUM:
			dataBox.getChildren().addAll(numField);
			strField.setPromptText("Num value");
			nextButton.setOnAction((ActionEvent) -> {
				value = numField.getText();
				close();
			});
			break;
		case COORD:
			dataBox.getChildren().addAll(xField, yField);
			xField.setPromptText("X coordinate");
			yField.setPromptText("Y Coordinate");
			nextButton.setOnAction((ActionEvent) -> {
				value = xField.getText() + " " + yField.getText();
				close();
			});
			break;
		}

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
		showAndWait();
	}

	private String value;
	
	public String getValue() {
		return value;
	}

	public static enum Type {
		ACCOUNT,
		STRING,
		NUM,
		COORD
	}

}
