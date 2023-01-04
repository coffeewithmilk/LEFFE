package view;

import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.control.Button;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.stage.Stage;
import model.scripting.GlobalVariables;

public class WaitMultiplierPrompt extends Stage {
	int width = 700, height = 700;

	public boolean isActive = true;

	public WaitMultiplierPrompt(double parentmidx, double parentmidy, Window window) {
		//super(StageStyle.TRANSPARENT);
		setTitle("Set wait multiplier");
		setX(parentmidx - width / 2);
		setY(parentmidy - height / 2);
		setWidth(width);
		setHeight(height);

		this.setOnCloseRequest(e -> {
			isRunning = false;
		});

		Button closeButton = new Button("Save & Close");
		closeButton.setOnAction((e) -> {
			isRunning = false;
			close();
		});


		LogSlider ls = new LogSlider();

		Styles.setButtonStyles(closeButton);


		HBox buttons = new HBox(12);
		buttons.getChildren().addAll(closeButton);
		VBox root = new VBox(200);
		root.setStyle(
				"-fx-border-width: 1pt; -fx-border-color: #FFFFFF; -fx-background-color: transparent; -fx-background-color: #222222;");

		root.setPadding(new Insets(100, 50, 50, 50));

		root.getChildren().addAll(ls, buttons);
		Scene scene = new Scene(root, width, height);
		scene.setFill(Color.TRANSPARENT);
		setScene(scene);
		showAndWait();

	}

	boolean isRunning = true;
	GraphicsContext gc;
	public class LogSlider extends Canvas {
		double value = GlobalVariables.WAIT_MULTIPLIER;
		LogButton lb = new LogButton();
		double x = 50;
		double y = 100;
		String currentValue = "";
		final double xMax = 500;
		public LogSlider() {
			setWidth(600);
			setHeight(300);
			gc = this.getGraphicsContext2D();
			this.setOnMousePressed(e -> {
				double x = e.getX();
				double y = e.getY();
				lb.onPress(x, y);
			});
			this.setOnMouseReleased(e -> {
				double x = e.getX();
				double y = e.getY();
				lb.onRelease(x, y);
			});
			this.setOnMouseDragged(e -> {
				double x = e.getX();
				double y = e.getY();
				lb.onDrag(x, y);
			});
			loop();
		}

		public void loop() {
			new Thread(() ->  {
				while(isRunning) {
					updateValue();
					Platform.runLater(() -> {
						draw();
					});
					try {
						Thread.sleep(30);
					} catch (InterruptedException e) {
						e.printStackTrace();
					}
				}
			}).start();
		}

		public void draw() {
			gc.setFill(Color.valueOf("#222222"));
			gc.fillRect(0, 0, width, height);
			gc.setStroke(Color.rgb(231, 230, 232));
			gc.setFill(Color.rgb(231, 230, 232));
			gc.strokeLine(x, y, x+xMax, y); 
			String valueStr = String.valueOf(value).substring(0, 3);
			if(valueStr.endsWith(".")) {valueStr = valueStr.substring(0, 2);}
			gc.fillText(valueStr + "x", lb.x, lb.y - 50);
			lb.draw(gc);
		}

		public void updateValue() {
			this.value = Math.pow(10, (double)(-1 + 2*(lb.x-49) / xMax));
			GlobalVariables.WAIT_MULTIPLIER = value;
		}

	}

	public static class LogButton {
		double x = 250+50, y = 100;
		boolean isDragging = false;
		public LogButton() {
			System.out.println(GlobalVariables.WAIT_MULTIPLIER);
			double startx = Math.log10(GlobalVariables.WAIT_MULTIPLIER);
			System.out.println(startx);
			startx = startx + 1.0;
			System.out.println(startx);
			startx = startx / 2.0;
			System.out.println(startx);
			startx = startx * 500;
			System.out.println(startx);
			x = startx+50;
		}
		public void draw(GraphicsContext gc) {
			gc.fillRect(x-10, y-30, 20, 60);
		}
		public void onPress(double mousex, double mousey) {
			if (mousex > x-10 && mousex < x+10 && mousey > y-30 && mousey < y+30) {
				isDragging = true;
			}
		}

		public void onDrag(double mousex, double mousey) {
			if(isDragging) {
				if (mousex>=50 && mousex<=550) {
					this.x = mousex;
				}
			}
		}

		public void onRelease(double mousex, double mousey) {
			isDragging = false;
		}
	}

}
