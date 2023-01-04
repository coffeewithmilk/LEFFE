package model.scripting;

import java.net.Authenticator;
import java.net.InetAddress;
import java.net.PasswordAuthentication;
import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;
import java.security.cert.X509Certificate;

import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;

import com.sun.webkit.network.CookieManager;

import javafx.application.Platform;
import javafx.concurrent.Worker;
import javafx.concurrent.Worker.State;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Dialog;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.image.Image;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.web.PromptData;
import javafx.scene.web.WebEngine;
import javafx.scene.web.WebView;
import javafx.stage.Stage;
import javafx.util.Callback;
import model.BotUtils;
import model.WebUtils;
import netscape.javascript.JSObject;
import security.Account;
import view.Window;

public class WebVar extends Variable {

	protected WebEngine value;
	protected Stage stage;
	protected TextField nameField;
	protected TextField contentField;

	public boolean isReady = false;

	private static boolean debug = true;
	public static String console;
	
	State state = Worker.State.READY;
	

	

	public static void setDebug(boolean dbg) {
		debug = dbg;
	}

	public WebVar(String key) {
		super(key);
	}

	public void addForm(String title, String function, String... args) {
		HBox form = new HBox(4);
		form.getChildren().add(new Label(title));
		TextField[] tfs = new TextField[args.length];
		int i = 0;
		for (String str : args) {
			TextField tf = new TextField();
			tf.setPromptText(str);
			tfs[i] = tf;
			form.getChildren().add(tfs[i]);
			i++;
		}
		Button btn = new Button("Submit");
		btn.setOnAction((event) -> {
			String str = function + " ";
			for (TextField tf : tfs) {
				str += "\""+tf.getText()+"\";";
			}
			final String pling = str;
			new Thread(() ->  {
				Window.window.getEnvironment().parseStatement(pling);
			}).start();	
		});
		form.getChildren().add(btn);
		root.getChildren().add(form);
	}

	VBox root;
	
	public static class WebConsoleListener {
		public void log(String text) {
			console = text;
		}
	}

	public static WebConsoleListener bridge = new WebConsoleListener();
	
	@Override
	public void setValue(Object val) {
		System.out.println("Val: " + val);
		if (val instanceof String) {
			if (this.value == null) {
				Platform.runLater(() -> {
					/*WebConsoleListener.setDefaultListener((webView, message, lineNumber, sourceId) -> {
					    System.out.println(message + " [at " + lineNumber + "]");
					    console = message;
					});*/
					stage = new Stage();
					stage.setTitle("LEFFE Browser");
					WebView wv = new WebView();
					this.value = wv.getEngine();
					

					stage.setTitle(this.getKey());
					stage.getIcons().add(new Image(Window.class.getResourceAsStream("browser.png")));
					value.onStatusChangedProperty().addListener((obs, oldstate, newvalue) -> {
						System.out.println(newvalue);
					});
					
					value.getLoadWorker().messageProperty().addListener((obs, oldstate, newstate) -> {
						System.out.println(newstate);
					});
					value.getLoadWorker().exceptionProperty().addListener((obs, oldstate, newstate) -> {
						System.out.println(newstate);
					});
					value.getLoadWorker().stateProperty().addListener((obs, oldstate, newstate) -> {
						state = newstate;
						if (newstate.equals(Worker.State.SUCCEEDED)) {
							isReady = true;
						}
						JSObject window = (JSObject) value.executeScript("window");
						window.setMember("java", bridge);
						value.executeScript("console.log = function(message)\n" +
							"{\n" +
							"    java.log(message);\n" +
							"};");
						    
					});
					value.setPromptHandler(new Callback<PromptData, String>() {

						@Override
						public String call(PromptData arg0) {
							System.out.println(arg0.getMessage());
							System.out.println(arg0.getDefaultValue());
							Dialog dialog = new Dialog();
							
							return "";
						}

					});
					root = new VBox();
					//value.setUserAgent("Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/81.0.4044.138 Safari/537.36");
					TextField tf = new TextField();
					tf.setPromptText("Enter JavaScript command...");
					tf.setOnKeyPressed((ev) -> {
						if (ev.getCode().equals(KeyCode.ENTER)) {
							System.out.println(WebUtils.executeScriptReturn(wv.getEngine(), tf.getText()));
						}
					});
					root.getChildren().addAll(wv);

					wv.prefHeightProperty().bind(root.widthProperty());
					wv.prefHeightProperty().bind(root.heightProperty());

					if (debug) {
						root.getChildren().add(tf);
					}
					Scene scene = new Scene(root);

					HBox nameContent = new HBox();

					nameField = new TextField();
					nameField.setPromptText("Name");

					contentField = new TextField();
					contentField.setPromptText("Set content");

					TextField commandField = new TextField();
					commandField.setPromptText("Enter LEFFEScript command...");
					commandField.setOnKeyPressed((ev) -> {
						if (ev.getCode().equals(KeyCode.ENTER)) {
							new Thread(() -> {
								Window.window.getEnvironment().parseStatement(commandField.getText());
							}).start();
						}
					});


					contentField.setOnKeyPressed(ev -> {
						if (ev.getCode().equals(KeyCode.ENTER)) {
							WebUtils.setTextContentName(wv.getEngine(), nameField.getText(), contentField.getText());
						}
					});
					if (debug) {
						root.getChildren().addAll(commandField);
						nameContent.getChildren().addAll(nameField, contentField);
						root.getChildren().add(nameContent);
					}
					stage.setScene(scene);
					stage.show();
					
					TrustManager trm = new X509TrustManager() {
				        public X509Certificate[] getAcceptedIssuers() {return null;}
				        public void checkClientTrusted(X509Certificate[] certs, String authType) {}
				        public void checkServerTrusted(X509Certificate[] certs, String authType) {}
				    };

				    SSLContext sc;
					try {
						sc = SSLContext.getInstance("SSL");
						sc.init(null, new TrustManager[] { trm }, null);
					    HttpsURLConnection.setDefaultSSLSocketFactory(sc.getSocketFactory());
					} catch (NoSuchAlgorithmException | KeyManagementException e) {
					}
				    
					System.setProperty("sun.net.http.allowRestrictedHeaders", "true");
					Authenticator.setDefault(new CustomAuthenticator());
					value.setUserAgent("Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/81.0.4044.138 Safari/537.36");
					 
					WebUtils.loadURL(value, (String) val);

				});
			}
		}
		waitForLoad();
	}

	public static class CustomAuthenticator extends Authenticator {

        protected PasswordAuthentication getPasswordAuthentication() {

            /*String prompt = getRequestingPrompt();
            String hostname = getRequestingHost();
            InetAddress ipaddr = getRequestingSite();
            int port = getRequestingPort();*/

            String username = Account.AUTH_UNAME;
            String password = Account.AUTH_PW;
            
            return new PasswordAuthentication(username, password.toCharArray());            
        }       
    }
	
	public void waitForLoad() {
		while (!isReady) {
			try {
				Thread.sleep(100);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
		isReady = false;
	}

	@Override
	public Object getValue() {
		return this.value;
	}

	public void maximize() {
		Platform.runLater(() -> {
			if (stage.isIconified()) {
				stage.setIconified(false);
			}
			stage.setMaximized(true);
		});
	}

	public void restore() {
		Platform.runLater(() -> {
			if (!stage.isIconified()) {
				stage.setIconified(true);
			}
			stage.setMaximized(false);
		});
	}

	public void minimize() {
		Platform.runLater(() -> {
			stage.setIconified(true);
		});
	}

	public void setNameContent(String idname, String content) {
		Platform.runLater(() -> {
			if (nameField != null && contentField != null) {
				BotUtils.wait(100);
				nameField.setText(idname);
				BotUtils.wait(100);
				contentField.setText(content);
				BotUtils.wait(100);
				KeyEvent ev = new KeyEvent(KeyEvent.KEY_PRESSED, "ENTER", "ENTER", KeyCode.ENTER, false, false, false, false);
				contentField.fireEvent(ev);
				BotUtils.wait(100);
			}
		});
	}

	public void clearThisVar() {
		CookieManager.setDefault(new CookieManager());
		stage.close();
	}

}
