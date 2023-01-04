package model;

import javafx.application.Application;
import javafx.stage.Stage;

public class PrintImage extends Application {

	public static void main(String[] args) {
		launch(args);
	}
	
	@Override
	public void start(Stage arg0) throws Exception {
		WebUtils.print(WebUtils.loadIcon("file:C:\\\\Users\\\\jvs154\\\\Desktop\\\\david.jpg"));
	}

}
