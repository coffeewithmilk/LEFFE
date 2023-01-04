package view;

import java.awt.MouseInfo;
import java.awt.Point;
import java.io.File;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.Optional;

import org.jnativehook.GlobalScreen;
import org.jnativehook.keyboard.NativeKeyEvent;
import org.jnativehook.keyboard.NativeKeyListener;

import javafx.application.Platform;
import javafx.beans.InvalidationListener;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.canvas.Canvas;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.CheckBox;
import javafx.scene.control.CheckMenuItem;
import javafx.scene.control.Label;
import javafx.scene.control.Menu;
import javafx.scene.control.MenuBar;
import javafx.scene.control.MenuItem;
import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableColumn.CellDataFeatures;
import javafx.scene.control.TableView;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.image.Image; 
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.Background;
import javafx.scene.layout.BackgroundFill;
import javafx.scene.layout.CornerRadii;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.Text;
import javafx.stage.FileChooser;
import javafx.stage.FileChooser.ExtensionFilter;
import javafx.stage.Stage;
import javafx.stage.WindowEvent;
import javafx.util.Callback;
import model.StatUtils;
import model.scripting.BotFile;
import model.scripting.Config;
import model.scripting.Environment;
import model.scripting.Statement;
import model.scripting.Variable;
import security.Account;

public class Window extends Stage {

	private static final String LEFFE_TITLE = "L.E.F.F.E v1.5";

	private TextArea logArea = new TextArea();
	private TextArea codeArea = new TextArea();

	private Label mouseInfoLabel = new Label();

	//private WebView webview = new WebView();

	private TableView<Variable> varTableView;

	private MenuBar toolbar = new MenuBar();
	private Menu file = new Menu("File");
	private MenuItem fileNew = new MenuItem("New [CTRL+N]");
	private MenuItem fileOpen = new MenuItem("Open [CTRL+O]");
	private MenuItem fileSave = new MenuItem("Save [CTRL+S]");
	private MenuItem fileSaveLog = new MenuItem("Save log");
	private MenuItem fileExit = new MenuItem("Exit");

	private Menu tools = new Menu("Tools");
	private MenuItem toolsSchedule = new MenuItem("Schedule");
	private MenuItem toolsConfig = new MenuItem("Config $-coordinates");
	private MenuItem toolsClearVars = new MenuItem("Clear variables.");
	private MenuItem toolsCache = new MenuItem("Cache mode");
	private MenuItem toolsStat = new MenuItem("Create statistics graphics");
	private MenuItem toolsWaitMultiplier = new MenuItem("Set wait multiplier");

	private Menu application = new Menu("Application");
	private MenuItem appRun = new MenuItem("Run [CTRL+ALT+K]");
	private MenuItem appCacheRun = new MenuItem("Run from row...");
	private MenuItem appStop = new MenuItem("Stop [CTRL+ALT+ESCAPE]");

	private Menu views = new Menu("View");
	private CheckMenuItem viewsEditMode = new CheckMenuItem("Switch code view");
	private MenuItem viewsConfig = new MenuItem("View current $-configuration");
	private MenuItem viewsLoginButton = new MenuItem("Login");

	private Menu insert = new Menu("Insert");
	private Menu insertCommand = new Menu("Command");

	private Menu mouseInfoMenu = new Menu("");

	//private HTMLEditor syntaxPane = new HTMLEditor();

	private TabPane filePane = new TabPane();

	private Environment e = new Environment() {
		@Override
		public void addLog(String log) {
			Platform.runLater(() -> {
				logArea.setText(log + "\n" + logArea.getText());
			});
		}

		@Override
		public void displayVariables(ArrayList<Variable> vars) {
			Platform.runLater(() -> {
				if (!varTableView.getItems().isEmpty()) {
					varTableView.getItems().removeAll(varTableView.getItems());
				}
				varTableView.getItems().addAll(vars);
			});
		}
	};

	public Environment getEnvironment() {
		return e;
	}

	public static Window window;

	private boolean isEditMode = false;
	public static boolean isCacheMode = false;

	class CodeArea extends TextArea {

		Canvas c;
		//private int width, height;

		private int ss = 0;

		private String description = "";
		private Point descrPoint = new Point(0, 0);

		private long lastUpdated = 0;

		void reformatAndTrim() {
			String newCode = "";
			String oldCode = this.getText();

			String[] enters = oldCode.split("\n");
			for (int i = 0; i < enters.length; i++) {
				String row = enters[i];
				while(row.endsWith(" ") || row.endsWith("\t")) {
					row = row.substring(0, row.length() - 1);
				}
				newCode += row + "\n";
			}
			this.setText(newCode);
		}

		private boolean isResizing = false;
		
		public CodeArea(int width, int height) {
			//setPrefSize(width, height);
			//resize(width, height);
			//this.width = width;
			//this.height = height;
			//this.prefHeightProperty().bind(window.heightProperty().multiply(0.55));
			c = new Canvas(width, height);

			//c.setWidth(widthProperty().doubleValue());
			getChildren().add(c);

			setOnMousePressed((MouseEvent) -> {
				reformatAndTrim();
				
				int mx, my;
				mx = (int) MouseEvent.getX();
				my = (int) MouseEvent.getY();
				
				if (mx > this.getWidth() - 50 && my > this.getHeight() - 50) {
					isResizing = true;
					return;
				}
				
				int distance = Integer.MAX_VALUE;
				int distanceI = -1;
				for (int i = 0; i < indexes.size(); i++) {
					IndexXY ixy = indexes.get(i);
					int dTemp = (int) new Point(ixy.x * 10, ixy.y * 100).distance(mx * 10, (my + 6 + gc.getDeltaY()) * 100);
					if (dTemp < distance) {
						distance = dTemp;
						distanceI = i;
					}
				}
				this.selectRange(distanceI, distanceI);
				ss = distanceI;
			});

			setOnMouseDragged((MouseEvent) -> {
				int mx, my;
				mx = (int) MouseEvent.getX();
				my = (int) MouseEvent.getY();
				
				if (isResizing) {
					this.setPrefWidth(mx + 25);
					this.setPrefHeight(my + 25);
					c.setWidth(mx + 25);
					c.setHeight(my + 25);
					((VBox)this.getParent()).setPrefWidth(mx + 25);
					((VBox)this.getParent()).setPrefHeight(my + 25);
				}
				
				int distance = Integer.MAX_VALUE;
				int distanceI = -1;
				for (int i = 0; i < indexes.size(); i++) {
					IndexXY ixy = indexes.get(i);
					int dTemp = (int) new Point(ixy.x, ixy.y).distance(mx, my + 6 + gc.getDeltaY());
					if (dTemp < distance) {
						distance = dTemp;
						distanceI = i;
					}
				}
				this.selectRange(ss, distanceI);
			});
			
			setOnMouseReleased((MouseEvent) -> {
				isResizing = false;
			});
			
			setOnMouseExited((ev) -> {
				isResizing = false;
			});

			setOnMouseMoved((MouseEvent) -> {
				double distance = Double.MAX_VALUE;
				descrPoint = new Point((int)MouseEvent.getX(), (int)(MouseEvent.getY() + gc.getDeltaY() + 6));

				String word = "";
				for (IndexXY ixy : indexes) {
					if (ixy.str != null) {
						double d = descrPoint.distance(ixy.x, ixy.y);
						if (d < distance) {
							distance = d;
							word = ixy.str;
						}
					}
				}
				description = "";

				if (distance < 50) {
					for (Statement stat : Statement.STATEMENTS) {
						if (stat.getKeyword().equals(word)) {
							description = stat.getDescription();
						}
					}
				}
				descrPoint.setLocation(new Point((int) descrPoint.getX() + 40, (int) descrPoint.getY() + 40));
				if (lastUpdated < System.currentTimeMillis()) {

					redraw();
					lastUpdated = System.currentTimeMillis() + 20;
				}
			});

			setOnKeyPressed((KeyEvent) -> {
				if (KeyEvent.getCode().equals(KeyCode.DOWN) || KeyEvent.getCode().equals(KeyCode.UP)) {
					reformatAndTrim();
				}

				if (KeyEvent.isAltDown()) {
					if (KeyEvent.getCode().equals(KeyCode.DIGIT4)) {
						this.insertText(this.getAnchor(), "$");
					}
					if (KeyEvent.getCode().equals(KeyCode.DIGIT7)) {
						this.insertText(this.getAnchor(), "{");
					}
					if (KeyEvent.getCode().equals(KeyCode.DIGIT0)) {
						this.insertText(this.getAnchor(), "}");
					}
					if (KeyEvent.getCode().equals(KeyCode.DIGIT8)) {
						this.insertText(this.getAnchor(), "[");
					}
					if (KeyEvent.getCode().equals(KeyCode.DIGIT9)) {
						this.insertText(this.getAnchor(), "]");
					}
					if (KeyEvent.getCode().equals(KeyCode.PLUS)) {
						this.insertText(this.getAnchor(), "\\");
					}
				}
				
				if (KeyEvent.isControlDown()) {
					if (KeyEvent.getCode().equals(KeyCode.S)) {
						if (currentFile != null && !currentFile.isEmpty()) {
							BotFile.saveFile("files/" + currentFile + (currentFile.endsWith(".bot") ? "" : ".bot"),
									codeArea.getText());
							e.addLog("File saved.");
							lastSavedText = codeArea.getText();
						}
					}
					if (KeyEvent.getCode().equals(KeyCode.F)) {
						if (!isEditMode) {
							isEditMode = !isEditMode;
							((CodeArea)codeArea).redraw();
						}
						new TextFinder();
					}
				}
			});

			this.setOnScroll((e) -> {
				//setScrollTop(this.getScrollTop() + e.getDeltaY() * 4);
				if (!isEditMode) {
					double dy = e.getDeltaY();
					gc.scroll(-dy);
					layoutChildren();
				}
			});

		}

		public void redraw() {
			Platform.runLater(() -> {
				layoutChildren();
			});
		}

		GC gc = new GC();

		@Override
		protected void layoutChildren() {
			super.layoutChildren();
			this.setFont(Font.font("Consolas", 18.0));
			if(isEditMode) {
				if (getChildren().contains(c)) {
					getChildren().removeAll(c);
				}
				//this.getChildren().get(0).resize(width, height);
				//this.resize(window.widthProperty().doubleValue() * 0.6, window.heightProperty().multiply(0.6).doubleValue());
				return;
			}
			if (getChildren().contains(c)) {
				getChildren().removeAll(c);
			}
			getChildren().add(c);


			gc.setGC(c.getGraphicsContext2D());

			gc.clearRect(0, 0, (int)(widthProperty().doubleValue()), (int) ((int) (heightProperty().doubleValue())));

			gc.setFill(Color.valueOf("#444444"));
			gc.setFont(Font.font("Consolas", 12.0));
			gc.fillRectStatic(0, 0, (int)(widthProperty().doubleValue()), (int) ((int) (heightProperty().doubleValue())));
			gc.setFill(Color.valueOf("#FFFFFF"));
			String[] enters = getText().split("\n");
			int x = 40, y = 15;
			int selectStart = this.getSelection().getStart();
			int selectEnd = this.getSelection().getEnd();
			indexes = new ArrayList<>();
			indexes.add(new IndexXY(x, y, null));

			for (int rowIndex = 0; rowIndex < enters.length; rowIndex++) {
				gc.setFill(Color.LIGHTCYAN);
				gc.fillText("" + rowIndex, 6, y);
				String currentRow = enters[rowIndex];
				String[] tabs = currentRow.split("\t");
				for (int tabIndex = 0; tabIndex < tabs.length; tabIndex++) {
					String currentTab = tabs[tabIndex];
					String[] wordsWithinTab = currentTab.split(" ");
					for (int wordWithinTabIndex = 0; wordWithinTabIndex < wordsWithinTab.length; wordWithinTabIndex++) {
						String curWordWithinTab = wordsWithinTab[wordWithinTabIndex];
						boolean isKeyword = false;
						for (Statement statement : Statement.STATEMENTS) {
							if (curWordWithinTab.equals(statement.getKeyword())) {
								isKeyword = true;
							}
						}
						Text text = new Text(curWordWithinTab);
						text.setFont(gc.getFont());
						for (int iCharInWordInTab = 0; iCharInWordInTab < curWordWithinTab.length(); iCharInWordInTab++) {
							int tempx = x + (int) (((double) (iCharInWordInTab + 1) / (double) curWordWithinTab.length())
									* text.getLayoutBounds().getWidth());
							indexes.add(new IndexXY(tempx, y, curWordWithinTab));
							if (indexes.size() - 1 > selectStart && indexes.size() - 1 <= selectEnd
									&& selectEnd > selectStart) {
								gc.setFill(Color.color(0.5, 0.6, 1, 0.3));
								gc.fillRect(tempx - 8, y - 12, 10, 12);
							}
						}
						if (isKeyword && !curWordWithinTab.equals("//")) {
							gc.setFill(Color.valueOf("#AAFF00"));
						} else {
							if (currentRow.contains("//")) {
								gc.setFill(Color.GOLDENROD);
							} else if (currentRow.trim().startsWith("$")) {
								gc.setFill(Color.LIGHTPINK);
							} else {
								gc.setFill(Color.valueOf("#FFFFFF"));
							}
						}
						gc.fillText(curWordWithinTab, x, y);
						x = x + (int) text.getLayoutBounds().getWidth();
						if (wordWithinTabIndex != wordsWithinTab.length - 1 || currentTab.endsWith(" ")) {
							gc.fillText(" ", x, y);
							Text text2 = new Text(" ");
							text2.setFont(gc.getFont());
							x = x + (int) text2.getLayoutBounds().getWidth();
							indexes.add(new IndexXY(x, y, null));
						}
					}
					x += 22;
					if (tabIndex != tabs.length - 1 || currentRow.endsWith("\t")) {
						indexes.add(new IndexXY(x, y, null));
					}
				}
				if (rowIndex == currentStatementIndex && e != null && IS_ALIVE) {
					gc.setFill(Color.valueOf("#CC3333"));
					gc.fillText(">", 27, y);
					if (y - gc.getDeltaY() > c.getHeight() / 2) {
						gc.scroll(gc.getFont().getSize() + 2);
					}
				}
				y += (int) gc.getFont().getSize() + 2;
				x = 40;
				if (rowIndex != enters.length - 1 || currentRow.endsWith("\r")) {
					indexes.add(new IndexXY(x, y, null));
				}
			}
			gc.setFill(Color.color(0.5, 0.6, 1, 0.3));
			if (selectStart >= 0 && selectStart < indexes.size() && selectEnd >= 0 && selectEnd < indexes.size())
				if (selectStart == selectEnd && selectStart >= 0 && selectStart < indexes.size() && selectEnd >= 0
				&& selectEnd < indexes.size()) {
					gc.setFill(Color.WHITE);
					gc.fillText("|", indexes.get(selectStart).x - gc.getFont().getSize() / 4,
							indexes.get(selectStart).y);
				}
			if (!description.isEmpty()) {
				String[] descr = description.split("\\s");
				double yDescr = descrPoint.getY()+10;
				double xDescr = descrPoint.getX();
				gc.setFill(Color.color(1, 1, 0.87, 0.9));
				gc.fillRect(descrPoint.x, descrPoint.y, 550, (int) ((descr.length / 5 + 3) * gc.getFont().getSize()));
				gc.setFill(Color.valueOf("#444444"));

				for (int i = 0; i < descr.length; i++) {
					if(i%5 == 0) {
						yDescr += gc.getFont().getSize();
						xDescr = descrPoint.getX();
					}
					Text text2 = new Text(descr[i] + " ");
					text2.setFont(gc.getFont());
					gc.fillText(descr[i], xDescr + 10, yDescr + 5);
					xDescr = xDescr + (int) text2.getLayoutBounds().getWidth();
				}
			}
		}
	}

	class TextFinder extends Stage {

		public TextFinder() {
			setTitle("Find/Replace");
			int t_width = 400;
			int t_height = 200;
			this.setWidth(t_width);
			this.setHeight(t_height);
			this.setX(window.getX() + window.getWidth() / 2 - t_width / 2);
			this.setY(window.getY() + window.getHeight() / 2 - t_height / 2);



			VBox box = new VBox(8);
			box.setPadding(new Insets(40,40,40,40));
			TextField inputField = new TextField();
			inputField.setPromptText("Find text...");
			Button searchBtn = new Button("Find");
			searchBtn.setPrefWidth(100);
			TextField replaceField = new TextField("Replace with...");
			Button replaceBtn = new Button("Replace all");
			replaceBtn.setPrefWidth(100);

			searchBtn.setOnAction(aev -> {
				if (inputField.getText().isEmpty()) {
					return;
				}

				String withinstr = codeArea.getText().toLowerCase();
				if (!withinstr.contains(inputField.getText().toLowerCase())) {
					return;
				}
				int anchorindex = withinstr.indexOf(inputField.getText().toLowerCase(), codeArea.getCaretPosition());
				if (anchorindex == -1) {
					anchorindex = withinstr.indexOf(inputField.getText().toLowerCase(), 0);
				}
				codeArea.selectRange(anchorindex, anchorindex + inputField.getText().length());
			});

			replaceBtn.setOnAction(aev -> {
				if (inputField.getText().isEmpty()) {
					return;
				}

				String withinstr = codeArea.getText().toLowerCase();
				if (!withinstr.contains(inputField.getText().toLowerCase())) {
					return;
				}
				codeArea.setText(codeArea.getText().toLowerCase().replaceAll(inputField.getText().toLowerCase(), replaceField.getText()));
			});

			HBox findbox = new HBox(8);

			HBox replaceBox = new HBox(8);

			findbox.getChildren().addAll(inputField, searchBtn);

			replaceBox.getChildren().addAll(replaceField, replaceBtn);

			box.getChildren().addAll(findbox, replaceBox);

			inputField.setOnKeyPressed(kev -> {
				if (kev.getCode().equals(KeyCode.ENTER)) {
					searchBtn.fire();
				}
				if (kev.getCode().equals(KeyCode.ESCAPE)) {
					this.close();
				}
			});

			replaceField.setOnKeyPressed(kev -> {
				if (kev.getCode().equals(KeyCode.ENTER)) {
					replaceBtn.fire();
				}
				if (kev.getCode().equals(KeyCode.ESCAPE)) {
					this.close();
				}
			});


			Scene scene = new Scene(box);
			setScene(scene);
			this.show();

		}

	}

	ArrayList<IndexXY> indexes = new ArrayList<>();

	class IndexXY {
		int x, y;
		String str;

		public IndexXY(int x, int y, String str) {
			this.x = x;
			this.y = y;
			this.str = str;
		}
	}

	private String currentFile = "";

	Thread thread = null;

	public static boolean IS_ALIVE = false;
	public static boolean IS_COORDS = false;

	public Window() {
		window = this;
		//System.err.close();
		setTitle(LEFFE_TITLE);
		VBox main = new VBox();
		HBox root = new HBox(16);
		if (!Account.loggedIn) {
			root.setSpacing(16);
		}
		
		//System.err.close();
		this.setHeight(900);

		main.setOnMouseMoved(MouseEvent -> {
			if (mousethread == null || !mousethread.isAlive()) {
				updateLoop();
			}
		});

		Scene scene = new Scene(main);
		scene.getStylesheets().add(getClass().getResource("style.css").toExternalForm());
		setScene(scene);
		
		
		scene.addEventHandler(KeyEvent.KEY_PRESSED, e -> {
			if(e.isAltDown()) e.consume();
		});
		
		 
		//setResizable(false);

		root.setPrefSize(1480, 900);

		if (!Account.loggedIn) {
			root.setPrefSize(1480, 900);
		}

		HBox toolbar = new HBox();
		Button file = new Button("File");
		Button edit = new Button("Edit");
		Button code = new Button("Code");
		Button help = new Button("Help");
		file.setStyle("-fx-background-color: #FFFFFF;");
		edit.setStyle("-fx-background-color: #FFFFFF;");
		code.setStyle("-fx-background-color: #FFFFFF;");
		help.setStyle("-fx-background-color: #FFFFFF;");
		toolbar.setStyle("-fx-background-color: #FFFFFF;");

		toolbar.getChildren().addAll(file, edit, code, help);

		VBox logBox = new VBox(0); 
		logBox.prefWidth(600);
		logBox.prefHeightProperty().bind(this.heightProperty().multiply(0.8));

		HBox mouseInfo = new HBox(16);

		mouseInfoLabel.setStyle("-fx-text-fill: #FFFFFF;-fx-stroke: #FFFFFF;");

		//Button logDocButton = new Button("LOG/DOC");



		mouseInfo.getChildren().add(mouseInfoLabel);
		if (Account.loggedIn) {
			//mouseInfo.getChildren().add(logDocButton);
		}

		VBox codeBox = new VBox(0);
		//codeBox.prefWidth(600);
		//codeBox.prefHeightProperty().bind(this.heightProperty().multiply(0.8));


		TextField startAtRow = new TextField("");


		//HBox codeBoxButtons = new HBox(16);
		codeArea = new CodeArea(800,732);
		appRun.setOnAction((ActionEvent) -> {
			Account.user = "";
			if (Account.loggedIn) {
				Account.user = "ADMIN";
			} else {
				new NamePrompt((int) (this.getX() + this.getWidth() / 2), (int) (this.getY() + this.getHeight() / 2), this);
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
			e = new Environment() {

				@Override
				public void addLog(String log) {
					Platform.runLater(() -> {
						logArea.setText(log + "\n" + logArea.getText());
					});
				}

				@Override
				public void displayVariables(ArrayList<Variable> vars) {
					Object[] obs = vars.toArray();
					Platform.runLater(() -> {
						if (!varTableView.getItems().isEmpty()) {
							varTableView.getItems().removeAll(varTableView.getItems());
						}
						for(Object o : obs) {
							varTableView.getItems().add((Variable)o);
						}
					});
				}

			};
			e.addStatements(codeArea.getText());
			IS_ALIVE = true;
			thread = new Thread(() -> {
				currentStatementIndex = 0;
				if (isCacheMode && !startAtRow.getText().isEmpty()) {
					try {
						int startIndex = Integer.parseInt(startAtRow.getText());
						e.setCodeIndex(startIndex);
					} catch (Exception e) {}
				}
				Platform.runLater(() -> {
					//window.toBack();
				});
				e.parseCode(currentFile, false, false);

			});
			thread.start();
		});


		appStop.setOnAction((ActionEvent) -> {
			IS_ALIVE = false;
			e.stop();
			saveLog();
		});

		viewsEditMode.setOnAction(ActionEvent -> {
			isEditMode = viewsEditMode.isSelected();
			((CodeArea)codeArea).redraw();
		});


		startAtRow.setPromptText("Start at");
		startAtRow.setStyle("-fx-control-inner-background: #222222; -fx-text-fill: #FFFFFF; -fx-font-size: 10pt;");
		startAtRow.setEditable(false);

		CheckBox debugButton = new CheckBox();
		debugButton.setText("Cache mode");
		debugButton.setStyle("-fx-text-fill: #FFFFFF; height: 20pt;");
		debugButton.setOnAction(ActionEvent -> {
			isCacheMode = debugButton.isSelected();
			startAtRow.setEditable(isCacheMode);

			if (isCacheMode) {
				startAtRow.setStyle("-fx-control-inner-background: #FFFFFF; -fx-text-fill: #222222; -fx-font-size: 10pt;");
			} else {
				startAtRow.setStyle("-fx-control-inner-background: #222222; -fx-text-fill: #222222; -fx-font-size: 10pt;");
			}
		});


		toolsSchedule.setOnAction((a) -> {
			new ScheduleView((int) (this.getX() + this.getWidth() / 2), (int) (this.getY() + this.getHeight() / 2), this);
		});

		if (Account.loggedIn) {
			//codeBoxButtons.getChildren().addAll(debugButton, startAtRow);
		}

		//codeArea.prefHeightProperty().bind(codeBox.prefHeightProperty());


		logArea.setPrefSize(600, 300);
		logArea.prefHeightProperty().bind(logBox.heightProperty().multiply(0.5));
		logArea.setStyle("-fx-control-inner-background: #444444; -fx-text-fill: #FFFFFF; -fx-font-size: 10pt;");
		logArea.setBackground(new Background(new BackgroundFill(Color.rgb(0, 0, 0), CornerRadii.EMPTY, Insets.EMPTY)));
		HBox varArea = new HBox(0);

		varTableView = new TableView<Variable>();
		varTableView.setPrefSize(600, 364);
		varTableView.prefHeightProperty().bind(logBox.heightProperty().multiply(0.5));
		varArea.getChildren().add(varTableView);
		TableColumn<Variable, String> name = new TableColumn<Variable, String>("Name");
		name.setCellValueFactory(new Callback<CellDataFeatures<Variable, String>, ObservableValue<String>>() {
			public ObservableValue<String> call(CellDataFeatures<Variable, String> p) {
				return new ObservableValue<String>() {
					@Override
					public void addListener(InvalidationListener listener) {}
					@Override
					public void removeListener(InvalidationListener listener) {}
					@Override
					public void addListener(ChangeListener<? super String> listener) {}
					@Override
					public void removeListener(ChangeListener<? super String> listener) {}
					@Override
					public String getValue() {
						if(p != null && p.getValue() != null) {
							return p.getValue().getKey();
						}
						return "";
					}
				};
			}
		}); 

		TableColumn<Variable, String> type = new TableColumn<Variable, String>("Type");
		type.setCellValueFactory(new Callback<CellDataFeatures<Variable, String>, ObservableValue<String>>() {
			public ObservableValue<String> call(CellDataFeatures<Variable, String> p) {
				return new ObservableValue<String>() {
					@Override
					public void addListener(InvalidationListener listener) {}
					@Override
					public void removeListener(InvalidationListener listener) {}
					@Override
					public void addListener(ChangeListener<? super String> listener) {}
					@Override
					public void removeListener(ChangeListener<? super String> listener) {}
					@Override
					public String getValue() {
						if (p != null && p.getValue() != null) {
							return p.getValue().getClass().getName().replace("model.scripting.", "").replace("Var", "").toLowerCase();
						}
						return "";
					}
				};
			}
		});

		TableColumn<Variable, String> value = new TableColumn<Variable, String>("Value");
		value.setCellValueFactory(new Callback<CellDataFeatures<Variable, String>, ObservableValue<String>>() {
			public ObservableValue<String> call(CellDataFeatures<Variable, String> p) {
				return new ObservableValue<String>() {

					@Override
					public void addListener(InvalidationListener listener) {}
					@Override
					public void removeListener(InvalidationListener listener) {}
					@Override
					public void addListener(ChangeListener<? super String> listener) {}
					@Override
					public void removeListener(ChangeListener<? super String> listener) {}
					@Override
					public String getValue() {
						if (p != null && p.getValue() != null) {
							return p.getValue().toString();
						}
						return "";
					}
				};
			}
		});

		name.prefWidthProperty().bind(varTableView.widthProperty().multiply(0.33));
		type.prefWidthProperty().bind(varTableView.widthProperty().multiply(0.33));
		value.prefWidthProperty().bind(varTableView.widthProperty().multiply(0.33));

		varTableView.getColumns().add(name);
		varTableView.getColumns().add(type);
		varTableView.getColumns().add(value);

		logArea.setEditable(false);

		toolsWaitMultiplier.setOnAction((ae) -> {
			new WaitMultiplierPrompt(this.getX() + this.getWidth() / 2, this.getY() + this.getHeight() / 2, this);
		});
		
		toolsStat.setOnAction((ae) -> {
			StatUtils.saveStats();
			StatUtils.createStatGraphics();
		});

		Label logLabel = new Label("Log");
		Label codeLabel = new Label("Code: ");
		logLabel.setStyle("-fx-text-fill: #FFFFFF;");
		codeLabel.setStyle("-fx-text-fill: #FFFFFF;");

		//HBox codeMenu = new HBox(16);

		//fileNew.setStyle("-fx-font-weight: bold; -fx-text-fill: #00FF00; -fx-background-color: #444444;");

		fileNew.setOnAction((e) -> {
			FileChooser fileChooser = new FileChooser();
			fileChooser.getExtensionFilters().add(new ExtensionFilter("BOT-files", "*.bot"));
			fileChooser.setInitialDirectory(new File("files/"));
			File tempFile = fileChooser.showSaveDialog(new Stage());
			BotFile.saveFile("files/" + tempFile.getName() + (tempFile.getName().endsWith(".bot") ? "" : ".bot"), "");
			openFile(tempFile, true);
		});

		fileExit.setOnAction((event) -> {
			this.getOnCloseRequest().handle(new WindowEvent(this, WindowEvent.WINDOW_CLOSE_REQUEST));
		});

		this.fileSave.setOnAction((event) -> {
			if (currentFile != null && !currentFile.isEmpty()) {
				BotFile.saveFile("files/" + currentFile + (currentFile.endsWith(".bot") ? "" : ".bot"),
						codeArea.getText());
				e.addLog("File saved.");
				lastSavedText = codeArea.getText();
			}
		});

		if (Account.loggedIn) {
			this.file.getItems().addAll(fileNew, fileOpen, fileSave, fileSaveLog, fileExit);
			this.insert.getItems().addAll(this.insertCommand);
			this.application.getItems().addAll(this.appRun, this.appCacheRun, this.appStop);
			this.tools.getItems().addAll(this.toolsSchedule, this.toolsConfig, this.toolsCache, this.toolsStat, this.toolsWaitMultiplier, this.toolsClearVars);
			this.views.getItems().addAll(this.viewsEditMode, this.viewsConfig, this.viewsLoginButton);
			this.toolbar.getMenus().addAll(this.file, this.insert, this.application, this.tools, this.views, mouseInfoMenu);
		} else {
			this.file.getItems().addAll(fileOpen, fileSaveLog, fileExit);
			this.application.getItems().addAll(this.appRun, this.appStop);
			this.views.getItems().addAll(this.viewsLoginButton);
			this.toolbar.getMenus().addAll(this.file, this.application, this.views);
		}

		fileOpen.setOnAction((event) -> {
			FileChooser fc = new FileChooser();
			fc.setInitialDirectory(new File("files/"));
			fc.getExtensionFilters().add(new ExtensionFilter("BOT-Files", "*.bot"));
			File selectedOpenFile = fc.showOpenDialog(new Stage());
			openFile(selectedOpenFile, true);
		});

		//loadButtons(codeMenu, fileNew);

		HBox logMenu = new HBox(4);

		fileSaveLog.setOnAction((ev) -> {
			LocalDateTime ldt = LocalDateTime.now();
			if (currentFile != null) {
				String fileName = "logs/" + currentFile.replace(".bot", "") + "_" + LocalDate.now() + "_" + (ldt.getHour() < 10 ? "0" + ldt.getHour() : ldt.getHour()) + "_"
						+ (ldt.getMinute() < 10 ? "0" + ldt.getMinute() : ldt.getMinute()) + ".txt";
				BotFile.saveFile(fileName, logArea.getText());
				e.addLog("Log-file saved: " + fileName);
			}

		});


		toolsClearVars.setOnAction((ActionEvent) -> {
			if (e != null) {
				e.removeVariables();
			}
		});


		logBox.getChildren().addAll(logMenu);
		if (Account.loggedIn) {
			logBox.getChildren().addAll(varArea);
		}
		logBox.getChildren().addAll(logArea, mouseInfo);

		/*logDocButton.setOnAction((event) -> {
			if (logBox.getChildren().contains(varArea)) {
				logBox.getChildren().removeAll(logBox.getChildren());
				logBox.getChildren().addAll(webview, mouseInfo);
			} else {
				logBox.getChildren().removeAll(logBox.getChildren());
				logBox.getChildren().addAll(logMenu);
				if (Account.loggedIn) {
					logBox.getChildren().addAll(varArea);
				}
				logBox.getChildren().addAll(logArea, mouseInfo);
			}
		});*/


		if (!Account.loggedIn) {
			codeArea.setEditable(false);
		}
		codeBox.getChildren().addAll(filePane, codeArea);

		this.setOnCloseRequest((WindowEvent) -> {
			if (!this.lastSavedText.equals(codeArea.getText())) {
				Alert alert = new Alert(AlertType.CONFIRMATION);
				alert.setTitle("File not saved");
				alert.setHeaderText("The file has not been saved.");
				alert.setContentText("Do you want to close the application anyway?");
				Optional<ButtonType> result = alert.showAndWait();
				if (!result.get().equals(ButtonType.OK)) {
					WindowEvent.consume();
					return;
				}
			}
			System.exit(1);
			isRunning = false;
		});

		if (Account.loggedIn) {
			viewsLoginButton.setText("Logout");
			viewsLoginButton.setOnAction((ActionEvent) -> {
				Account.loggedIn = false;
				this.close();
				new Window();
			});
		} else {
			viewsLoginButton.setOnAction((ActionEvent) -> {
				new LoginView((int) (this.getX() + this.getWidth() / 2), (int) (this.getY() + this.getHeight() / 2), this);
			});
		}


		root.getChildren().addAll(logBox, codeBox);

		root.setStyle(
				"-fx-font-family: \"Consolas\"; -fx-font-size: 10pt; -fx-background-color: #222222; -fx-stroke: #FFFFFF;");

		root.setPadding(new Insets(32, 32, 32, 32));

		root.prefHeightProperty().bind(this.heightProperty());

		main.getChildren().addAll(this.toolbar, root);

		NativeKeyListener nkl = new NativeKeyListener() {

			ArrayList<Integer> keys = new ArrayList<>();

			@Override
			public void nativeKeyPressed(NativeKeyEvent arg0) {
				if (!keys.contains(arg0.getKeyCode())) {
					keys.add(arg0.getKeyCode());
				}
				if (keys.contains((Object) org.jnativehook.keyboard.NativeKeyEvent.VC_CONTROL) && keys.contains((Object) org.jnativehook.keyboard.NativeKeyEvent.VC_ALT) && keys.contains((Object) org.jnativehook.keyboard.NativeKeyEvent.VC_ESCAPE)) {
					appStop.fire();
				}
				if (keys.contains((Object) org.jnativehook.keyboard.NativeKeyEvent.VC_CONTROL) && keys.contains((Object) org.jnativehook.keyboard.NativeKeyEvent.VC_ALT) && keys.contains((Object) org.jnativehook.keyboard.NativeKeyEvent.VC_K)) {
					if (!IS_ALIVE) {
						appRun.fire();
					}
				}
				if (keys.contains((Object) org.jnativehook.keyboard.NativeKeyEvent.VC_PAUSE)) {
					IS_COORDS = true;
				}
			}

			@Override
			public void nativeKeyReleased(NativeKeyEvent arg0) {
				if (keys.contains(arg0.getKeyCode())) {
					keys.remove((Object)arg0.getKeyCode());
				}
			}

			@Override
			public void nativeKeyTyped(NativeKeyEvent arg0) {

			}

		};


		viewsConfig.setOnAction((ActionEvent) -> {
			ArrayList<Config> configs = BotFile.preConfiguration(codeArea.getText());
			if (configs.size() > 0) {
				CoordinateView cc = new CoordinateView(this.currentFile);
				cc.show();
			}
		});

		toolsConfig.setOnAction((ActionEvent) -> {
			ArrayList<Config> configs = BotFile.preConfiguration(codeArea.getText());
			if (configs.size() > 0) {
				CoordConfig cc = new CoordConfig((int) (this.getX() + this.getWidth() / 2), (int) (this.getY() + this.getHeight() / 2), configs, this.currentFile);
				cc.show();
			}

		});

		for (Statement statement : Statement.STATEMENTS) {
			String statm = statement.getKeyword();
			if (statm.isEmpty()) {
				continue;
			}
			MenuItem mi = new MenuItem(statm);
			mi.setOnAction(ev -> {
				codeArea.insertText(codeArea.getSelection().getStart(), mi.getText());
			});
			insertCommand.getItems().add(mi);
		}

		insertCommand.getItems().sort(Comparator.comparing(MenuItem::getText));

		//System.err.close();

		GlobalScreen.addNativeKeyListener(nkl);



		try {
			GlobalScreen.registerNativeHook();
		} catch (Exception e) {
		}
		getIcons().add(new Image(Window.class.getResourceAsStream("leffe.png")));
		show();
		updateLoop();
		if (STARTSCRIPT != null && !STARTSCRIPT.isEmpty()) {
			codeArea.setText(BotFile.loadFile("files/" + STARTSCRIPT));
			lastSavedText = codeArea.getText();
			currentFile = STARTSCRIPT;
			STARTSCRIPT = "";
			appRun.fire();
		}
	}

	class QuickTab extends Tab {

		private File boundFile;

		public void onSelection() {
			window.openFile(boundFile, false);
		}

		public QuickTab(File f) {
			this.boundFile = f;
			this.setText(f.getName());
		}

		public void initSelectAction() {
			this.setOnSelectionChanged((ev) -> {
				if(this.isSelected()) {
					this.onSelection();
				} else {
					if (filePane.getTabs().size() == 0) {
						if (currentFile != null && !currentFile.isEmpty())
							BotFile.saveFile("files/" + currentFile + (currentFile.endsWith(".bot") ? "" : ".bot"),
									codeArea.getText());
						codeArea.setText("");
						lastSavedText = codeArea.getText();
						setTitle(LEFFE_TITLE);
						currentFile = "";
					}
				}
			});
		}

	}

	public void openFile(File f, boolean createTab) {
		if (currentFile != null && !currentFile.isEmpty())
			BotFile.saveFile("files/" + currentFile + (currentFile.endsWith(".bot") ? "" : ".bot"),
					codeArea.getText());
		codeArea.setText(BotFile.loadFile(f.getAbsolutePath()));
		lastSavedText = codeArea.getText();
		setTitle(f.getName() + " - " + LEFFE_TITLE);
		currentFile = f.getName();
		if (createTab) {
			QuickTab qt = new QuickTab(f);
			filePane.getTabs().add(qt);
			filePane.getSelectionModel().select(qt);
			qt.initSelectAction();
		}
	}

	public void saveLog() {
		Platform.runLater(() -> {
			fileSaveLog.fire();
		});
	}

	public static void forcestop() {
		window.appStop.fire();
	}

	private String lastSavedText = "";
	//private ComboBox<String> fileButtons;
	/*private void loadButtons(HBox codeMenu, Button newCodeBtn) {
		codeMenu.getChildren().removeAll(codeMenu.getChildren());
		File[] files = BotFile.getFiles();
		if (Account.loggedIn) {
			codeMenu.getChildren().add(newCodeBtn);
		}
		if (files == null) {
			return;
		}

		ArrayList<String> arr = new ArrayList<>();
		for (File f : files) {
			arr.add(f.getName());
		}
		fileButtons = new ComboBox<String>(FXCollections.observableArrayList(arr));

		fileButtons.setOnAction((ev) -> {
			String s = fileButtons.getSelectionModel().getSelectedItem();
			for (int i = 0; i < files.length; i++) {
				final int index = i;
				if (s.equals(files[i].getName())) {
					if (currentFile != null && currentFile.equals(files[index].getName())) {
						return;
					}
					if (currentFile != null && !currentFile.isEmpty())
						BotFile.saveFile("files/" + currentFile + (currentFile.endsWith(".bot") ? "" : ".bot"),
								codeArea.getText());
					codeArea.setText(BotFile.loadFile("files/" + files[index].getName()));
					lastSavedText = codeArea.getText();
					currentFile = files[index].getName();
				}
			}
		});


		fileButtons.setPromptText("--Files--");
		codeMenu.getChildren().add(fileButtons);





		toolsConfig.setOnAction((ActionEvent) -> {
			ArrayList<Config> configs = BotFile.preConfiguration(codeArea.getText());
			if (configs.size() > 0) {
				CoordConfig cc = new CoordConfig((int) (this.getX() + this.getWidth() / 2), (int) (this.getY() + this.getHeight() / 2), configs, this.currentFile);
				cc.show();
			}

		});



		if (Account.loggedIn) {
			viewsLoginButton.setText("Logout");
			viewsLoginButton.setOnAction((ActionEvent) -> {
				Account.loggedIn = false;
				this.close();
				new Window();
			});
		} else {
			viewsLoginButton.setOnAction((ActionEvent) -> {
				new LoginView((int) (this.getX() + this.getWidth() / 2), (int) (this.getY() + this.getHeight() / 2), this);
			});
		}
	}*/


	private boolean isRunning = true;
	private int currentStatementIndex = 0;

	private Thread mousethread;

	private void updateLoop() {
		mousethread = new Thread(() -> {
			while (isRunning) {
				if(MouseInfo.getPointerInfo() == null) {
					continue;
				}
				Point p = MouseInfo.getPointerInfo().getLocation();
				Platform.runLater(() -> {
					if (p != null) 
						mouseInfoMenu.setText("Mouse Location: X = " + p.x + ", Y = " + p.y);
				});

				if (e != null) {
					int i = e.getCodeIndex();
					if (i > currentStatementIndex) {
						currentStatementIndex = i;
						((CodeArea) codeArea).redraw();
					}
					if (i < currentStatementIndex) {
						currentStatementIndex = i;
					}
				}
				try {
					Thread.sleep(250);
				} catch (InterruptedException e) {
					e.printStackTrace();
				}

			}
		});
		mousethread.start();
	}

	public void setCode(String user, String generateCode) {
		Account.user = user;		
		e.addStatements(generateCode);
		IS_ALIVE = true;
		thread = new Thread(() -> {
			currentStatementIndex = 0;
			Platform.runLater(() -> {
				window.toBack();
			});
			e.parseCode("schedule.bot", false, false);
		});
		thread.start();
	}

	private static String STARTSCRIPT;

	public static void PREPARE_BOTFILE(String startScript) {
		STARTSCRIPT = startScript;
	}

	public void updateTitle(String display) {
		Platform.runLater(() -> {
			this.setTitle(display.trim() + " - " + LEFFE_TITLE);
		});
	}

}