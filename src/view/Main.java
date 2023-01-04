package view;

import java.io.File;
import java.io.IOException;
import java.io.PrintStream;
import java.time.LocalDate;
import java.util.Scanner;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.jnativehook.GlobalScreen;

import javafx.application.Application;
import javafx.stage.Stage;
import model.BotUtils;
import security.Account;

public class Main extends Application {

	@Override
	public void start(Stage stage) throws Exception {
		stage = new Window(); 
	}

	public static void main(String[] args) throws IOException {
		// Get the logger for "org.jnativehook" and set the level to warning.
		Logger logger = Logger.getLogger(GlobalScreen.class.getPackage().getName());
		logger.setLevel(Level.OFF); 
 
		// Don't forget to disable the parent handlers.
		logger.setUseParentHandlers(false);
		File fatalCrashFile = new File("fatal_crashes.txt");
		fatalCrashFile.createNewFile(); 
		Scanner scan = new Scanner(fatalCrashFile);
		String str = ""; 
		while(scan.hasNextLine()) { 
			str += scan.nextLine();
			//System.out.println(str);  
		}
		scan.close();
 
 
		PrintStream ps = new PrintStream(fatalCrashFile) {
			@Override
			public void println(String str) { 
				super.println(str);
				this.flush(); 
			}
		};

		// System.setErr(ps);
		//System.err.println(str);

		System.err.println("\r\n--------" + LocalDate.now().toString() + "----e----\r\n");

		
		
		BotUtils.setClipboard("");
		if (args.length > 0) {
			String startScript = args[0];
			if (args.length > 1) {
				boolean debug = args[1].toLowerCase().equals("debug");
				if (debug) {
					Account.loggedIn = true;
				}

			}
			if (startScript.endsWith(".bot")) {
				Window.PREPARE_BOTFILE(startScript);
			}
		}
		launch(args);
	}
}

