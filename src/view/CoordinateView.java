package view;

import java.util.ArrayList;

import javafx.application.Platform;
import javafx.scene.Scene;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.control.Button;
import javafx.scene.layout.HBox;
import javafx.scene.paint.Color;
import javafx.scene.paint.Paint;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import model.scripting.BotFile;

public class CoordinateView extends Stage {

	class C {
		int x = 0;
		int y = 0;
		String name = "";
	}
	ArrayList<C> cs;

	GraphicsContext g2d;

	public CoordinateView(String file) {
		super(StageStyle.TRANSPARENT);
		this.setOpacity(0.4);

		this.setOnCloseRequest((e) -> {
			isRunning = false;
		});

		String content = BotFile.loadConfigFile(file);

		String[] sm = content.split("\n");

		cs = new ArrayList<>();

		for (int i = 0; i < sm.length; i++) {
			if (sm[i].contains("coord")) {
				String statement = sm[i].replaceFirst("coord", "").trim();
				String name = statement.split("=")[0].trim();
				String val = statement.split("=")[1].trim();
				C c = new C();
				c.name = name;
				c.x = Integer.parseInt(val.split(" ")[0].trim());
				c.y = Integer.parseInt(val.split(" ")[1].trim());
				cs.add(c);
			}
		}
		
		System.out.println(cs.size());

		this.setX(0);
		this.setY(0);

		this.setWidth(8000);
		this.setHeight(2000);

		HBox root = new HBox();

		Canvas c = new Canvas();

		Button close = new Button("X");
		close.setStyle("-fx-background-color: #FF0000; -fx-text-fill: #FFFFFF;");
		close.setOnAction((e) -> {
			isRunning = false;
			close();
		});

		c.setWidth(6000);
		c.setHeight(2000);
		g2d = c.getGraphicsContext2D();
		root.setStyle("-fx-border-color: #FFFFFF; "
				+ "-fx-background-color: transparent; -fx-background-color: #FFFFFF;");
		
		c.setOnMouseClicked((MouseEvent) -> {
			if (MouseEvent.getX() < 40 && MouseEvent.getY() < 40) {
				isRunning = false;
				close();
			}
		});
		
		root.getChildren().add(c);
		Scene scene = new Scene(root);
		setScene(scene);
		scene.setFill(Color.TRANSPARENT);
		show();
		update();
	}

	private boolean isRunning = true;

	private void update() {
		new Thread(() -> {		
			while(isRunning || isShowing()) {

				Platform.runLater(() -> {
					g2d.clearRect(0, 0, 6000, 1100); 
				});
				
				//if (System.currentTimeMillis() % 1000 > 300) {
					Platform.runLater(() -> {
						g2d.setFont(Font.font("Segoe UI", FontWeight.BOLD, 40));
						g2d.setFill(Paint.valueOf("#000000"));
						g2d.fillRect(0, 0, 40, 40);
						g2d.setFill(Paint.valueOf("#FF0000"));
						g2d.fillText("X", 6, 34);
						for(int i = 0; i < cs.size(); i++) {
							g2d.setFill(Paint.valueOf("#FF0000"));
							g2d.fillOval(cs.get(i).x - 3, cs.get(i).y - 3, 6, 6);
							g2d.setFill(Paint.valueOf("#000000"));
							g2d.setFont(Font.font("Segoe UI", FontWeight.BOLD, 16));
							g2d.fillText(cs.get(i).name, cs.get(i).x + 10, cs.get(i).y + 5);
							System.out.println(cs.get(i).x);
							g2d.fillText("X: " + cs.get(i).x + ", Y: " + cs.get(i).y, cs.get(i).x - 20, cs.get(i).y + 20);
						}
					});

				//}
				try {
					Thread.sleep(2000);
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
			}
		}).start();
	}

}
