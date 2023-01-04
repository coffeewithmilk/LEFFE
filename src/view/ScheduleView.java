package view;

import java.io.File;
import java.util.ArrayList;
import java.util.Optional;

import calendar.Schedule;
import javafx.collections.FXCollections;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ComboBox;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.TextField;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.stage.Stage;
import model.scripting.BotFile;
import security.Account;

public class ScheduleView extends Stage {
	int width = 800, height = 600;


	ArrayList<FileBox> boxes = new ArrayList<>();

	class FileBox {
		ComboBox<String> fileButtons;
		Button button;

		public FileBox(ComboBox<String> fileButtons, Button button) {
			this.fileButtons = fileButtons;
			this.button = button;
		}
	}

	Schedule sc = new Schedule();

	ArrayList<Schedule> schedules = BotFile.loadSchedules();

	private void reloadBoxes(VBox box) {
		if (!box.getChildren().isEmpty()) {
			box.getChildren().removeAll(box.getChildren());
		}

		for (FileBox b : boxes) {
			HBox hb = new HBox(4);
			hb.getChildren().addAll(b.fileButtons, b.button);
			box.getChildren().add(hb);
		}
	}

	private void reloadFiles(VBox box) {
		box.setPadding(new Insets(20, 20, 20, 20));
		schedules = BotFile.loadSchedules();
		for (Schedule s : schedules) {
			Button button = new Button(s.getName());
			button.setMinWidth(158);
			button.setStyle("-fx-font-weight: bold; -fx-text-fill: #00FF00; -fx-background-color: #444444;");
			Styles.setButtonStyles(button);
			button.setOnAction((ev) -> {
				sc = s;
				boxes.removeAll(boxes);
				File[] files = BotFile.getFiles();
				ArrayList<String> arr = new ArrayList<>();
				for (File file : files) {
					arr.add(file.getName());
				}
				repeatButton.setSelected(sc.isRepeat());
				fileNameField.setText(sc.getName());
				for (String string : sc.fileOrder()) {
					ComboBox<String> str = new ComboBox<String>(FXCollections.observableArrayList(arr));
					str.getSelectionModel().select(string);
					Button deleteButton = new Button("-");
					deleteButton.setStyle("-fx-font-weight: bold; -fx-text-fill: #00FF00; -fx-background-color: #444444;");
					Styles.setButtonStyles(deleteButton);
					FileBox fb = new FileBox(str, deleteButton);
					deleteButton.setOnAction((a) -> {
						boxes.remove(fb);
						reloadBoxes(dataBox);
					});
					boxes.add(fb);
				}
				reloadBoxes(dataBox);
			});
			box.getChildren().add(button);
		}
	}

	private VBox dataBox;
	CheckBox repeatButton;
	public ScheduleView(int parentmidx, int parentmidy, Window window) {
		//super(StageStyle.UNDECORATED);
		setTitle("Schedule");
		setX(parentmidx - width / 2);
		setY(parentmidy - height / 2);
		setWidth(width);
		setHeight(height);

		repeatButton = new CheckBox("Repeat");
		repeatButton.setOnAction((ActionEvent) -> {
			sc.setRepeat(repeatButton.isSelected());
		});

		repeatButton.setStyle("-fx-font-weight: bold; -fx-text-fill: #00FF00");


		dataBox = new VBox(8);
		dataBox.setPadding(new Insets(20, 20, 20, 20));
		ScrollPane sp = new ScrollPane(dataBox);
		sp.setMinHeight(200);

		sp.setStyle("-fx-font-weight: bold; -fx-text-fill: #00FF00; -fx-background-color: #444444;");

		//TextField userField = new TextField();
		//		userField.setPromptText("Enter your name");
		//userField.setFocusTraversable(false);

		Button OKButton = new Button("Run Code");
		OKButton.setOnAction((e) -> {
			Account.user = "";
			if (Account.loggedIn) {
				Account.user = "ADMIN";
			} else {
				new NamePrompt((int) (this.getX() + this.getWidth() / 2), (int) (this.getY() + this.getHeight() / 2), null);
			}

			if (Account.user.length() <= 3) {
				Alert alert = new Alert(AlertType.CONFIRMATION);
				alert.setTitle("No name entered.");
				alert.setHeaderText("You need to enter your name");
				alert.setContentText("Please enter at least 4 characters.");
				Optional<ButtonType> result = alert.showAndWait();
				if (!result.get().equals(ButtonType.OK)) {
					return;
				}
				return;
			}
			for(FileBox fb : boxes) {
				sc.addFile(fb.fileButtons.getSelectionModel().getSelectedItem());
			}
			//System.out.println(sc.generateCode());
			window.setCode(Account.getUser(), sc.generateCode());
			this.close(); 
		});

		OKButton.setStyle("-fx-font-weight: bold; -fx-text-fill: #00FF00; -fx-background-color: #444444;");

		Button cancelButton = new Button("Cancel");
		cancelButton.setOnAction((e) -> {
			this.close();
		});
		cancelButton.requestFocus();
		cancelButton.setStyle("-fx-font-weight: bold; -fx-text-fill: #00FF00; -fx-background-color: #444444;");
		HBox buttons = new HBox(12);
		buttons.getChildren().addAll(OKButton, cancelButton);
		VBox r2 = new VBox(10);

		VBox addBox = new VBox(8);
		Button addButton = new Button("+");
		addButton.setStyle("-fx-font-weight: bold; -fx-text-fill: #00FF00; -fx-background-color: #444444;");


		addButton.setOnAction((ActionEvent) -> {

			File[] files = BotFile.getFiles();
			ArrayList<String> arr = new ArrayList<>();
			for (File file : files) {
				arr.add(file.getName());
			}

			ComboBox<String> str = new ComboBox<String>(FXCollections.observableArrayList(arr));

			Button deleteButton = new Button("-");

			Styles.setButtonStyles(deleteButton);

			FileBox fb = new FileBox(str, deleteButton);
			deleteButton.setStyle("-fx-font-weight: bold; -fx-text-fill: #00FF00; -fx-background-color: #444444;");
			deleteButton.setOnAction((a) -> {
				boxes.remove(fb);
				reloadBoxes(dataBox);
			});
			boxes.add(fb);
			reloadBoxes(dataBox);
		});

		Button saveButton = new Button("Save file");
		saveButton.setStyle("-fx-font-weight: bold; -fx-text-fill: #00FF00; -fx-background-color: #444444;");


		Styles.setButtonStyles(OKButton, cancelButton, addButton, saveButton);


		addBox.getChildren().addAll(addButton, repeatButton, saveButton);



		//----FILES
		VBox r1 = new VBox(10);

		fileNameField = new TextField();
		fileNameField.setPromptText("File name");
		fileNameField.setFocusTraversable(false);

		VBox fileBox = new VBox(8);

		ScrollPane filePane = new ScrollPane(fileBox);
		filePane.setMinHeight(200);
		filePane.setMinWidth(200);

		filePane.setStyle("-fx-font-weight: bold; -fx-text-fill: #00FF00; -fx-background-color: #444444;");

		r1.getChildren().addAll(filePane);

		reloadFiles(fileBox);

		//---

		saveButton.setOnAction((ActionEvent) -> {
			if (fileNameField.getText().isEmpty()) {
				Alert alert = new Alert(AlertType.ERROR, "You need to enter a file name.");
				alert.showAndWait();
				return;
			}
			if (!sc.fileOrder().isEmpty()) {
				sc.fileOrder().removeAll(sc.fileOrder());
			}
			for(FileBox fb : boxes) {
				sc.addFile(fb.fileButtons.getSelectionModel().getSelectedItem());
			}
			BotFile.createScheduleFile(fileNameField.getText(), sc);
			Alert alert = new Alert(AlertType.INFORMATION, "File saved.");
			alert.showAndWait();
			reloadFiles(fileBox);
		});

		HBox root = new HBox(10);
		root.getChildren().addAll(r1, r2);
		root.setPadding(new Insets(100, 50, 50, 50));
		root.setStyle(
				"-fx-border-width: 1pt; -fx-border-color: #FFFFFF; -fx-background-color: transparent; -fx-background-color: #222222;");


		r2.getChildren().addAll(fileNameField, sp, addBox, buttons);
		Scene scene = new Scene(root, width, height);
		scene.setFill(Color.TRANSPARENT);
		setScene(scene);
		show();

	}
	TextField fileNameField;
}
