package model.scripting;

import java.awt.Color;
import java.awt.Point;
import java.io.File;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;


import javafx.application.Platform;
import javafx.concurrent.Worker;
import javafx.scene.control.Dialog;
import javafx.scene.web.WebEngine;
import javafx.stage.Modality;
import model.BotUtils;
import model.Canvas;
import model.Sound;
import model.StatUtils;
import model.WebUtils;
import model.scripting.Argument.Type;
import officebridge.Attachment;
import officebridge.Outlook;
import security.Account;
import view.KeyAdapter;
import view.Prompt;
import view.Window;

public abstract class Statement {
	String keyword = "";
	String description = "";

	Argument[] arguments;

	public Argument[] getArguments() {
		return arguments;
	}

	private Statement(String keyword, String description, Argument... args) {
		this.keyword = keyword;
		this.description = description; 
		this.arguments = args;
	} 

	public String getDescription() {
		return description;
	}

	public String getKeyword() {
		return keyword;
	}

	public boolean isStatement(String statement, Environment e) {
		return statement.toLowerCase().startsWith(this.keyword.toLowerCase())
				&& (e == null || e.evaluateIfBoolsWaitFor());
	}

	public String delKeyword(String statement) {
		return statement.replaceAll(keyword + " ", "");
	}

	public abstract boolean parseStatement(String statement, Environment environment);

	private static final Statement STRING = new Statement("string", "Creates a new string (text) variable. Syntax: string name = \"value\"") {
		@Override 
		public boolean parseStatement(String statement, Environment environment) {
			if (!this.isStatement(statement, environment)) {
				return false;
			}

			String[] variable = statement.replaceAll("", "").replaceFirst(keyword + " ", "").split("=");
			environment.addString(variable[0].trim(), environment.stringExpression(statement.replaceFirst(keyword, "").replaceFirst(variable[0].trim(), "").trim().replaceFirst("=", "").trim()));
			return true;
		}
	};

	private static final Statement NUM = new Statement("num", "Creates a new number variable. Syntax: num name = 10.0") {
		@Override
		public boolean parseStatement(String statement, Environment environment) {
			if (!this.isStatement(statement, environment)) {
				return false;
			}

			String[] variable = statement.replaceAll(" ", "").replaceFirst(keyword, "").split("=");
			System.out.println(variable[1].trim() + "<<<<" + environment.arithmetic(variable[1].trim()));
			environment.addNum(variable[0], environment.arithmetic(variable[1].trim()));
			return true;
		}
	};

	private static final Statement WEBPAGE = new Statement("webpage", "Creates a new webpage and load a url. Syntax: webpage varName = \"https://url.com\"") {
		@Override
		public boolean parseStatement(String statement, Environment environment) {
			if (!this.isStatement(statement, environment)) {
				return false;
			}

			String[] variable = statement.replaceAll("", "").replaceFirst(keyword + " ", "").split("=");
			environment.addWebpage(variable[0].trim(), environment.stringExpression(variable[1].trim()));
			try {
				Thread.sleep(1000);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}

			WebVar wv = (WebVar)environment.getVariable(variable[0].trim());
			try {
				Thread.sleep(500);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}

			return true;
		}
	};

	private static final Statement WEBSETCONTENT = new Statement("websetcontent", "Sets the content of a HTML element. "
			+ "Syntax: websetcontent webpageVar;TYPE;ID-or-NAME(string);CONTENT;", new Argument(Type.webpage, "wp")) {
		@Override
		public boolean parseStatement(String statement, Environment environment) {
			if (!this.isStatement(statement, environment)) {
				return false;
			}

			String[] args = statement.replaceFirst(keyword, "").trim().split(";");

			Variable v = environment.getVariable(args[0].trim());
			String type = args[1].trim().toUpperCase();
			String idname = environment.stringExpression(args[2].trim());
			String content = environment.stringExpression(args[3].trim());

			if (!(v instanceof WebVar)) {
				return true;
			}

			WebVar webvar = (WebVar) v;
			WebEngine we = webvar.value;
			if (type.equals("ID")) {
				WebUtils.setTextContentID(we, idname, content);
			} else if (type.equals("NAME")) {
				webvar.setNameContent(idname, content);
			}

			//webvar.waitForLoad();
			return true;
		}
	};

	private static final Statement WEBGETCONTENT = new Statement("webgetcontent", "Gets the content of a HTML element. "
			+ "Syntax: websetcontent webpageVar;TYPE;ID-or-NAME(string);strVar;") {
		@Override
		public boolean parseStatement(String statement, Environment environment) {
			if (!this.isStatement(statement, environment)) {
				return false;
			}

			String[] args = statement.replaceFirst(keyword, "").trim().split(";");

			Variable v = environment.getVariable(args[0].trim());
			String type = args[1].trim().toUpperCase();
			String idname = environment.stringExpression(args[2].trim());
			String strVar = args[3].trim();

			StringVar sv = (StringVar) environment.getVariable(strVar);
			if (sv == null) {
				environment.addString(strVar, "");
				sv = (StringVar) environment.getVariable(strVar);
			}

			System.out.println(v.toString() +"\n"+ type +"\n"+ idname +"\n"+ strVar);

			if (!(v instanceof WebVar)) {
				return true;
			}

			WebVar webvar = (WebVar) v;
			WebEngine we = webvar.value;
			if (type.equals("ID")) {
				sv.setValue(WebUtils.getIDContent(we, idname));
			} else if (type.equals("NAME")) {
				sv.setValue(WebUtils.getNameContent(we, idname));
			}

			//webvar.waitForLoad();
			return true;
		}
	};

	StringVar svTemp;
	private static final Statement WEBEXECUTE = new Statement("webexec", "Executes a script on the specified webpage. Syntax: webexec webpageVar;scriptString;optionalReturnStr;") {
		@Override
		public boolean parseStatement(String statement, Environment environment) {
			if (!this.isStatement(statement, environment)) {
				return false;
			}

			String[] args = statement.replaceFirst(keyword, "").trim().split(";");
			String strVar = null;
			svTemp = null;
			if (args.length > 2) {
				strVar = args[2];
				System.out.println(strVar);
				svTemp = (StringVar) environment.getVariable(strVar);
				if (svTemp == null) {
					environment.addString(strVar, "");
					svTemp = (StringVar) environment.getVariable(strVar);
				}
			}
			Variable v = environment.getVariable(args[0].trim());
			if (v instanceof WebVar) {
				Variable param = environment.getVariable(args[1].trim());
				while(!((WebVar)v).state.equals(Worker.State.SUCCEEDED)) {
					try {
						Thread.sleep(50);
					} catch (InterruptedException e) {
						e.printStackTrace();
					}
				}
				Platform.runLater(() -> {
					if (svTemp == null) {
						svTemp = new StringVar("str");
					}
					svTemp.setValue(WebUtils.executeScriptReturn((WebEngine)(((WebVar) v).getValue()), environment.stringExpression(args[1].trim())));
				});

				//}
			}
			//((WebVar) v).waitForLoad();
			return true;
		}
	};

	private static final Statement WEBMAXIMIZE = new Statement("webmaximize", "Maximizes the web view. Syntax: webmaximize webpageVar") {
		@Override
		public boolean parseStatement(String statement, Environment environment) {
			if (!this.isStatement(statement, environment)) {
				return false;
			}

			String args = statement.replaceFirst(keyword, "").trim();

			Variable v = environment.getVariable(args);
			if (v != null && v instanceof WebVar) {
				((WebVar)v).maximize();
			}
			return true;
		}
	};

	private static final Statement WEBRESTORE = new Statement("webrestore", "Restores the web view to a smaller window. Syntax: webrestore webpageVar") {
		@Override
		public boolean parseStatement(String statement, Environment environment) {
			if (!this.isStatement(statement, environment)) {
				return false;
			}

			String args = statement.replaceFirst(keyword, "").trim();

			Variable v = environment.getVariable(args);
			if (v != null && v instanceof WebVar) {
				((WebVar)v).restore();
			}

			return true;
		}
	};

	private static final Statement WEBMINIMIZE = new Statement("webminimize", "Minimizes the web view. Syntax: webminimize webpageVar") {
		@Override
		public boolean parseStatement(String statement, Environment environment) {
			if (!this.isStatement(statement, environment)) {
				return false;
			}

			String args = statement.replaceFirst(keyword, "").trim();

			Variable v = environment.getVariable(args);
			if (v != null && v instanceof WebVar) {
				((WebVar)v).restore();
			}
			return true;
		}
	};

	private static final Statement WEBCLICK = new Statement("webclick", "Click on an HTML element. Syntax: webclick webPageVar;TYPE;ID/NAME;") {
		@Override
		public boolean parseStatement(String statement, Environment environment) {
			if (!this.isStatement(statement, environment)) {
				return false;
			}

			String[] args = statement.replaceFirst(keyword, "").trim().split(";");

			Variable v = environment.getVariable(args[0].trim());
			String type = args[1].trim().toUpperCase();
			String idname = args[2].trim();

			if (!(v instanceof WebVar)) {
				return true;
			}

			WebVar webvar = (WebVar) v;
			WebEngine we = webvar.value;
			if (type.equals("ID")) {
				WebUtils.clickID(we, idname);
			} else if (type.equals("NAME")) {
				WebUtils.clickName(we, idname);
			}
			//webvar.waitForLoad();
			return true;
		}
	};

	private static final Statement WEBAWAIT = new Statement("webawait", "Awaits a webpage to return a true JS expression. Syntax: webawait webPageVar;JSExpression;") {
		@Override
		public boolean parseStatement(String statement, Environment environment) {
			if (!this.isStatement(statement, environment)) {
				return false;
			}
			String[] args = statement.replaceFirst(keyword, "").trim().split(";");
			Variable v = environment.getVariable(args[0].trim());
			if (!(v instanceof WebVar)) {
				return true;
			}

			String expr = args[1];

			WebVar webvar = (WebVar) v;
			WebEngine we = webvar.value;

			while (!WebUtils.isExpressionReturningTrue(we, expr)) {
				BotUtils.wait(100);
			}
			return true;
		}
	};

	private static final Statement ARRAY = new Statement("array", "Creates a new array variable. An array can only consist of one type of variable. Syntax: array name = []") {
		@Override
		public boolean parseStatement(String statement, Environment environment) {
			if (!this.isStatement(statement, environment)) {
				return false;
			}
			String[] variable = statement.replaceAll(" ", "").replaceFirst(keyword, "").split("=");
			environment.addArray(variable[0], new ArrayList<Variable>());
			return true;
		}
	};

	private static final Statement ARRAYADD = new Statement("arrayadd", "Adds a new variable to an array. Syntax: arrayadd arrayName;value") {
		@SuppressWarnings("unchecked")
		@Override
		public boolean parseStatement(String statement, Environment environment) {
			if (!this.isStatement(statement, environment)) {
				return false;
			}
			String[] variable = statement.replaceFirst(keyword, "").trim().split(";");

			ArrayList<Variable> arr = (ArrayList<Variable>)environment.getVariable(variable[0]).getValue();
			if (variable[1].trim().startsWith("\"")) {
				arr.add(new StringVar("", environment.stringExpression(variable[1].trim())));
				return true;
			}
			Variable v = environment.getVariable(variable[1]);
			if (v == null) {
				arr.add(new StringVar("", environment.stringExpression(statement.replaceFirst(keyword, "").replaceFirst(variable[0], "").trim())));
			}
			if (v instanceof StringVar) {
				arr.add(new StringVar("", environment.stringExpression(v.getKey())));
			} else if (v instanceof NumVar) {
				arr.add(new NumVar("", environment.arithmetic(v.getKey())));
			} else if (v instanceof CoordVar) {
				CoordVar cv = new CoordVar("", environment.coordExpression(v.getKey()));
				arr.add(cv);
			}
			return true;
		}
	};

	private static final Statement ARRAYREMOVE = new Statement("arrayremove", "Removes an index from the array. Syntax: arrayremove arrayName index") {
		@Override
		public boolean parseStatement(String statement, Environment environment) {
			if (!this.isStatement(statement, environment)) {
				return false;
			}
			String[] variable = statement.replaceFirst(keyword, "").trim().split(" ");
			((ArrayList<?>)environment.getVariable(variable[0]).getValue()).remove((int)environment.arithmetic(variable[1]));
			return true;
		}
	};

	private static final Statement SIZEOF = new Statement("sizeof", "Retrieves the current size of an array. Syntax: sizeof sizeNumVar arrayName") {
		@Override
		public boolean parseStatement(String statement, Environment environment) {
			if (!this.isStatement(statement, environment)) {
				return false;
			}
			String[] variable = statement.replaceFirst(keyword, "").trim().split(" ");
			Variable v;
			if (variable[1].trim().equals("args")) {
				v = environment.getArguments();
			} else {
				v = environment.getVariable(variable[1]);
			}
			if (v instanceof ArrayVar) {
				environment.addNum(variable[0].trim(), ((ArrayList<?>)v.getValue()).size());
			} else if (v instanceof StringVar) {
				environment.addNum(variable[0].trim(), environment.stringExpression(variable[1]).length());
			}
			return true;
		}
	};

	private static final Statement MOUSEMOVE = new Statement("mousemove", "Moves the mouse cursor to a location on the screen. Syntax: mousemove x y, eg. mousemove 130 150.") {
		@Override
		public boolean parseStatement(String statement, Environment environment) {
			if (!this.isStatement(statement, environment)) {
				return false;
			}
			String[] coords = delKeyword(statement).trim().split(" ");
			if (coords.length < 2) {
				double[] crds = environment.coordExpression(delKeyword(statement).trim());
				BotUtils.mouseMove(crds[0], crds[1], false);
			} else {
				BotUtils.mouseMove(environment.parseNum(coords[0]), environment.parseNum(coords[1]), false);
			}
			return true;
		}
	};

	private static final Statement SMOOTHMOVE = new Statement("smoothmove", "Same as mousemove but the cursor travels to the target location instead of \"teleporting\"") {
		@Override
		public boolean parseStatement(String statement, Environment environment) {
			if (!this.isStatement(statement, environment)) {
				return false;
			}
			String[] coords = delKeyword(statement).trim().split(" ");
			if (coords.length < 2) {
				double[] crds = environment.coordExpression(delKeyword(statement).trim());
				BotUtils.smoothMove(crds[0], crds[1], false);
			} else {
				BotUtils.smoothMove(environment.parseNum(coords[0]), environment.parseNum(coords[1]), false);
			}
			return true;
		}
	};

	private static final Statement SEQUENCE = new Statement("sequence", "Performs a sequence of mousemove, leftclick, and wait. Endless sequences can be made. Syntax: sequence <wait time>;<COORD>;<COORD>;<COORD>;...") {

		@Override
		public boolean parseStatement(String statement, Environment environment) {
			if (!this.isStatement(statement, environment)) {
				return false;
			}
			statement = statement.replaceFirst("sequence", "").trim();
			String[] args = statement.split(";");
			double time = environment.arithmetic(args[0]);
			for (int i = 1; i < args.length; i++) {
				double[] currentCoord = environment.coordExpression(args[i]);
				BotUtils.mouseMove(currentCoord[0], currentCoord[1], false);
				BotUtils.wait(100);
				BotUtils.leftClick();
				BotUtils.wait((int)time);
			}
			return true;
		}

	};

	private static final Statement LEFTCLICK = new Statement("leftclick", "Performs a full left button mouse click.") {
		@Override
		public boolean parseStatement(String statement, Environment environment) {
			if (!this.isStatement(statement, environment)) {
				return false;
			}
			BotUtils.leftClick();
			return true;
		}
	};

	private static final Statement RIGHTCLICK = new Statement("rightclick", "Performs a full right button mouse click.") {
		@Override
		public boolean parseStatement(String statement, Environment environment) {
			if (!this.isStatement(statement, environment)) {
				return false;
			}
			BotUtils.rightClick();
			return true;
		}
	};

	private static final Statement LEFTDOWN = new Statement("leftdown", "Holds the left mouse button down.") {
		@Override
		public boolean parseStatement(String statement, Environment environment) {
			if (!this.isStatement(statement, environment)) {
				return false;
			}
			BotUtils.leftDown();
			return true;
		}
	};

	private static final Statement LEFTUP = new Statement("leftup", "Releases the left mouse button.") {
		@Override
		public boolean parseStatement(String statement, Environment environment) {
			if (!this.isStatement(statement, environment)) {
				return false;
			}
			BotUtils.leftUp();
			return true;
		}
	};

	private static final Statement RIGHTDOWN = new Statement("rightdown", "Holds the right mouse button down.") {
		@Override
		public boolean parseStatement(String statement, Environment environment) {
			if (!this.isStatement(statement, environment)) {
				return false;
			}
			BotUtils.rightDown();
			return true;
		}
	};

	private static final Statement RIGHTUP = new Statement("rightup", "Releases the right mouse button.") {
		@Override
		public boolean parseStatement(String statement, Environment environment) {
			if (!this.isStatement(statement, environment)) {
				return false;
			}
			BotUtils.rightUp();
			return true;
		}
	};

	private static final Statement CHECKARGS = new Statement("checkargs", "Test arguments in a function. Execute code on false condition."
			+ "Multiple code lines possible with ';' delimiter. Syntax: checkio <condition>;<condition>;...;<condition> : executeOnFalse1;executeOnFalse2;...;executeOnFalseN") {
		@Override
		public boolean parseStatement(String statement, Environment environment) {
			if (!this.isStatement(statement, null)) {
				return false;
			}
			String[] onConditionFalse = statement.split(":")[1].trim().split(";");
			statement = statement.split(":")[0].trim();
			String[] conditions = statement.replaceFirst("checkargs", "").trim().split(";");

			ArrayVar av = (ArrayVar) environment.getVariable("args");
			ArrayList<Variable> ar = av.val;

			if (conditions.length != ar.size()) {
				for (int i = 0; i < onConditionFalse.length; i++) {
					environment.parseStatement(onConditionFalse[i]);
				}
				return true;
			}
			for (int i = 0; i < conditions.length; i++) {
				String str = "args["+i+"]";
				System.out.println(str + " " + conditions[i]);
				if (!environment.bool(str + " " + conditions[i])) {
					for (int j = 0; j < onConditionFalse.length; j++) {
						environment.parseStatement(onConditionFalse[j]);
					}
					return true;
				}
			}
			return true;
		}
	};

	private static String retStr = "";

	private static final Statement CHECKELEMENT = new Statement("checkelement", "Check if an element exists within a webpage. Execute multiline functions delimited by ';' on failure."
			+ " Syntax: checkelement wpVar;tagName;attribute;value : ") {
		@Override
		public boolean parseStatement(String statement, Environment environment) {
			if (!this.isStatement(statement, null)) {
				return false;
			}
			String[] onFailure = statement.split(":")[1].trim().split(";");
			String[] arguments = statement.split(":")[0].replaceFirst("checkelement", "").trim().split(";");
			String wpname = arguments[0];
			String tagName = environment.stringExpression(arguments[1]);
			String attribute = environment.stringExpression(arguments[2]);
			String value = environment.stringExpression(arguments[3]);

			if (!attribute.equals("innerText")) {
				attribute = "getAttribute('"+attribute+"')";
			}

			WebEngine we = (WebEngine)(((WebVar) environment.getVariable(wpname)).getValue());
			String execString = "var els = document.getElementsByTagName('"+tagName+"'); for (let i = 0; i < els.length; i++) { if(els[i]."+attribute+" == '"+value+"') { els[i]; } }";
			retStr = "NOT_YET_DETERMINED";
			System.out.println(execString);
			Platform.runLater(() -> {
				retStr = WebUtils.executeScriptReturn(we, execString);
			});

			while (retStr.equals("NOT_YET_DETERMINED")) {
				try {
					Thread.sleep(100);
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
			}
			System.out.println("Retstr = " + retStr);
			if (retStr.equals("undefined")) {
				for (int i = 0; i < onFailure.length; i++) {
					environment.parseStatement(onFailure[i]);
				}
				return true;
			}
			return true;
		}
	};

	private static final Statement IF = new Statement("if", "Test if an expression is true. "
			+ "If the expression is true then all actions will be performed until else, elseif or endif. Syntax: if (x < 5)") {

		@Override
		public boolean parseStatement(String statement, Environment environment) {
			if (!this.isStatement(statement, null)) {
				return false;
			}
			environment.addIfBool(environment.bool(statement.trim().replaceFirst(keyword, "").trim()));
			return true;
		}

	};

	private static final Statement ENDIF = new Statement("endif", "Ends an if statement.") {

		@Override
		public boolean parseStatement(String statement, Environment environment) {
			if (!this.isStatement(statement, null)) {
				return false;
			}
			environment.decrementIfBool();
			return true;
		}

	};

	private static final Statement ELSE = new Statement("else", "If all other expressions have been tested but none were true, perform actions until endif.") {

		@Override
		public boolean parseStatement(String statement, Environment environment) {
			if (!this.isStatement(statement, null)) {
				return false;
			}
			if (environment.getCurrentIf() || environment.isWaitEndIf()) {
				environment.waitForEndIf();
			} else {
				environment.modifyIfBool(true);
			}
			return true;
		}

	};

	private static final Statement ELSEIF = new Statement("elseif", "Tests if a new expression is true but only if no other expression has been true yet. Syntax: elseif (x > 9)") {

		@Override
		public boolean parseStatement(String statement, Environment environment) {
			if (!this.isStatement(statement, null)) {
				return false;
			}
			if (environment.getCurrentIf() || environment.isWaitEndIf()) {
				environment.waitForEndIf();
			} else {
				environment.modifyIfBool(environment.bool(statement.trim().replaceFirst(keyword, "").trim()));
			}
			return true;
		}

	};

	private static final Statement FOR = new Statement("for", "Perform actions in a loop. Syntax example: for(num i = 0; i < 5; i = i + 1)") {
		@Override
		public boolean parseStatement(String statement, Environment environment) {
			if (!this.isStatement(statement, environment)) {
				return false;
			}
			String stm = statement.trim().replaceFirst(keyword, "").trim();
			if (stm.startsWith("(") && stm.endsWith(")")) {
				stm = stm.substring(1, stm.length() - 1);
			}
			String[] forStatements = stm.split(";");
			environment.createForLoop(forStatements[0], forStatements[1], forStatements[2]);

			return true;
		}
	};

	private static final Statement ENDFOR = new Statement("endfor", "Return to the beginning of the for-loop if the condition is true, and update the variable.") {
		@Override
		public boolean parseStatement(String statement, Environment environment) {
			if (!this.isStatement(statement, environment)) {
				return false;
			}
			environment.endFor();
			return true;
		}
	};

	public static final Statement FUNCTION = new Statement("function", "Initiates a new function that can be called from anywhere. The function scope has to end with 'endfunction'. "
			+ "Syntax: function functionName") {

		@Override
		public boolean parseStatement(String statement, Environment environment) {
			if (!this.isStatement(statement, environment)) {
				return false;
			}
			return true;
		}

	};

	public static final Statement GETLASTCMSG = new Statement("getlastcmsg", "retrieves the last thing written in console.log(); syntax: getlastcmsg <newStrVar>") {

		@Override
		public boolean parseStatement(String statement, Environment environment) {
			if (!this.isStatement(statement, environment)) {
				return false;
			}
			StringVar sv;
			String varName = statement.trim().split(" ")[1];
			sv = (StringVar) environment.getVariable(varName);
			if (sv == null) {
				environment.addString(varName, "");
				sv = (StringVar)environment.getVariable(varName);
			}

			sv.setValue(WebVar.console);

			return true;
		}

	};

	private static final Statement RESETATTACHMENT = new Statement("resetattachment", "Removes all current attachments and creates a new, empty set of attachments. syntax: resetattachment") {

		@Override
		public boolean parseStatement(String statement, Environment environment) {
			if (!this.isStatement(statement, environment)) {
				return false;
			} 
			Attachment.resetAttachment();
			return true;
		}

	};

	private static final Statement ATTACHMENT = new Statement("attachment", "Adds an attachment to the current set of attachments use the sign * after the path to include all files in a folder. Syntax: attachment <filepath>") {

		@Override
		public boolean parseStatement(String statement, Environment environment) {
			if (!this.isStatement(statement, environment)) {
				return false;
			}
			String path = environment.stringExpression(statement.replaceFirst(keyword, "").trim());
			Attachment.addAttachment(path);
			return true;
		}



	};

	public static final Statement ENDFUNCTION = new Statement("endfunction", "Creates the last function initiated by command 'function'. Syntax: endfunction") {

		@Override
		public boolean parseStatement(String statement, Environment environment) {
			if (!this.isStatement(statement, environment)) {
				return false;
			}
			return false;
		}

	};

	String str = "";
	long waitTime = 250;
	public static final Statement JAVASCRIPT = new Statement("javascript", "Initiates a snippet of JavaScript code. Include LEFFEScript variables by placing them within §-signs. Include waits by separating executable javascript code by ¤. The time length of ¤ is defined as 3rd argument. Syntax: javascript <webvar>;<string returnedString>;<num waitvalue>") {

		@Override
		public boolean parseStatement(String statement, Environment environment) {
			if (!this.isStatement(statement, environment)) {
				if (statement.trim().split(" ")[0].equals("javascript") && !environment.evaluateIfBoolsWaitFor()) {
					environment.retrieveJavaScript();
				}
				return false;
			}



			String strTemp = environment.retrieveJavaScript();
			str = "";
			String[] strs = strTemp.split("§");
			for (int i = 0; i < strs.length; i++) {
				if (i % 2 == 0) {
					str += strs[i];
				} else {
					str += environment.stringExpression(strs[i]);
				}
			}
			waitTime = 250;
			String[] args = statement.replaceFirst(keyword, "").trim().split(";");
			String strVar = null;
			svTemp = null;
			if (args.length > 1) { 
				strVar = args[1];
				System.out.println(strVar);
				svTemp = (StringVar) environment.getVariable(strVar);
				if (svTemp == null) {
					environment.addString(strVar, "");
					svTemp = (StringVar) environment.getVariable(strVar);
				}
				svTemp.setValue("TEMPORARY");
			}
			if (args.length > 2) {
				waitTime = (long) environment.arithmetic(args[2]);
			}
			Variable v = environment.getVariable(args[0].trim());
			if (v instanceof WebVar) {
				Variable param = environment.getVariable(args[1].trim());
				while(!((WebVar)v).state.equals(Worker.State.SUCCEEDED)) {
					try {
						Thread.sleep(50);
					} catch (InterruptedException e) {
						e.printStackTrace();
					}
				}
				if (svTemp == null) {
					svTemp = new StringVar("str");
				}
				svTemp.setValue(WebUtils.executeMultiLineScriptReturn((WebEngine)(((WebVar) v).getValue()), str, waitTime));
			}

			while (svTemp.getValue().equals("TEMPORARY")) {
				try {
					Thread.sleep(50);
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
			}

			return true;
		}

	};
	
	private static final Statement SETAUTH = new Statement("setauth", "Sets the data for intranet authorization.") {

		@Override
		public boolean parseStatement(String statement, Environment environment) {
			if (!this.isStatement(statement, environment)) {
				return false;
			}
			String[] args = statement.replace(keyword, "").trim().split(";");
			String uname = environment.stringExpression(args[0]);
			String pword = environment.stringExpression(args[1]);
			Account.AUTH_UNAME = uname;
			Account.AUTH_PW = pword;
			return true;
		}
		
	};
	
	private static final Statement SETSMSCODE = new Statement("setsmscode", "Sets the SMS code for intranet authorization.") {

		@Override
		public boolean parseStatement(String statement, Environment environment) {
			if (!this.isStatement(statement, environment)) {
				return false;
			}
			String arg = statement.replace(keyword, "").trim();
			String code = environment.stringExpression(arg);
			Account.PASSCODE = code;
			return true;
		}
		
	};

	public static final Statement ENDJAVASCRIPT = new Statement("endjavascript", "Closes the javascript snippet.") {

		@Override
		public boolean parseStatement(String statement, Environment environment) {
			return false;
		}

	};

	private static final Statement PRINT = new Statement("print", "Prints a string to the log. Syntax: print string, eg. print \"Hello world\"") {

		@Override
		public boolean parseStatement(String statement, Environment environment) {
			if (!this.isStatement(statement, environment)) {
				return false;
			}
			LocalTime lt = LocalTime.now();
			environment.addLog(
					lt.toString().substring(0, 8) + " >> " + environment.stringExpression(delKeyword(statement)));
			return true;
		}

	};

	private static final Statement KEY = new Statement("key", "Press one or more keys in the given order. Then also release the keys in reverse order. Syntax: key CTRL ALT DELETE") {
		@Override
		public boolean parseStatement(String statement, Environment environment) {
			if (!this.isStatement(statement, environment)) {
				return false;
			}
			int[] kcs;
			String[] keys = statement.replaceFirst(keyword + " ", "").split(" ");
			kcs = new int[keys.length];
			for (int i = 0; i < keys.length; i++) {
				kcs[i] = KeyAdapter.kc(keys[i]);
			}
			if (kcs[0] == -1 && kcs.length == 1) {
				String keyString = environment.stringExpression(keys[0]);
				kcs = new int[keyString.length()];
				for (int i = 0; i < keyString.length(); i++) {
					kcs[i] = KeyAdapter.kc(keyString.charAt(i) + "");
				}
			}
			BotUtils.key(kcs);
			return true;
		}
	};

	private static final Statement KEYDOWN = new Statement("keydown", "Press one or more keys in the given order. Syntax: keydown CTRL ALT DELETE") {
		@Override
		public boolean parseStatement(String statement, Environment environment) {
			if (!this.isStatement(statement, environment)) {
				return false;
			}
			int[] kcs;
			String[] keys = statement.replaceFirst(keyword + " ", "").trim().split(" ");
			kcs = new int[keys.length];
			for (int i = 0; i < keys.length; i++) {
				kcs[i] = KeyAdapter.kc(keys[i]);
			}
			BotUtils.keyDown(kcs);
			return true;
		}
	};

	private static final Statement KEYUP = new Statement("keyup", "Release one or more keys in the given order. Syntax: keydown CTRL ALT DELETE") {
		@Override
		public boolean parseStatement(String statement, Environment environment) {
			if (!this.isStatement(statement, environment)) {
				return false;
			}
			int[] kcs;
			String[] keys = statement.replaceFirst(keyword + " ", "").split(" ");
			kcs = new int[keys.length];
			for (int i = 0; i < keys.length; i++) {
				kcs[i] = KeyAdapter.kc(keys[i]);
			}
			BotUtils.keyUp(kcs);
			return true;
		}
	};

	private static final Statement WAIT = new Statement("wait", "The program will wait for a given number of milliseconds (1000 ms = 1 s). Syntax: wait 1000") {
		@Override
		public boolean parseStatement(String statement, Environment environment) {
			if (!this.isStatement(statement, environment)) {
				return false;
			}
			BotUtils.wait((int) environment.arithmetic(statement.replaceFirst("wait ", "")));
			return true;
		}
	};

	private static final Statement CLIPBOARD = new Statement("setclipboard", "Sets the clipboard to a given string. Syntax: setclipboard \"Hello world\"") {

		@Override
		public boolean parseStatement(String statement, Environment environment) {
			if (!this.isStatement(statement, environment)) {
				return false;
			}
			BotUtils.setClipboard(environment.stringExpression(statement.replaceFirst(keyword + " ", "")));
			return true;
		}

	};

	private static final Statement UPDATE_VARIABLE = new Statement("", "") {

		@Override
		public boolean parseStatement(String statement, Environment environment) {
			if (environment.getVariable(statement.split(" ")[0]) != null && (environment == null || environment.evaluateIfBoolsWaitFor())) {
				String[] variables = statement.split("=");
				Variable v = environment.getVariable(variables[0].trim());
				if (v instanceof NumVar) {
					v.setValue(environment.arithmetic(variables[1].trim()));
				} else if (v instanceof StringVar || v instanceof WebVar) {
					v.setValue(environment.stringExpression(variables[1].trim()));
				}
				return true;
			}
			return false;
		}

	};

	private static final Statement FUNCTION_CALL = new Statement("", "") {

		@Override
		public boolean parseStatement(String statement, Environment environment) {
			if (environment.getVariable(statement.split(" ")[0]) != null && (environment == null || environment.evaluateIfBoolsWaitFor())) {
				boolean spawnThread = false, interruptThreads = false;
				if (statement.contains("&thread")) {
					spawnThread = true;
					statement = statement.replace("&thread", "").trim();
				}

				if (statement.contains("&interrupt")) {
					interruptThreads = true;
					statement = statement.replace("&interrupt", "").trim();
				}

				Variable v = environment.getVariable(statement.split(" ")[0].trim());
				if (!(v instanceof FunctionVar)) {
					return false;
				}
				if (v != null && v instanceof FunctionVar) {
					String[] args;
					if (!statement.contains(";") && statement.trim().split(" ").length == 1) {
						args = new String[] {};
					} else {
						args = statement.replaceFirst(statement.split(" ")[0], "").trim().split(";");
					}
					((FunctionVar)v).runCode(environment, spawnThread, interruptThreads, args);
				}
				return true;
			}
			return false;			
		}

	};

	private static final Statement LOAD = new Statement("load", "Loads and runs a .bot-file, Syntax: load \"variables.bot\"") {

		@Override
		public boolean parseStatement(String statement, Environment environment) {
			if (!this.isStatement(statement, environment)) {
				return false;
			}
			String[] args = statement.split(";");

			ArrayList<Variable> argAVList = new ArrayList<>();



			for (int i = 1; i < args.length; i++) {
				String arg = args[i].trim();
				Variable v = environment.getVariable(arg);
				if (arg.startsWith("\"")) {
					argAVList.add(new StringVar("", environment.stringExpression(arg)));
				} else if (Character.isDigit(arg.charAt(0))) {
					argAVList.add(new NumVar("", environment.arithmetic(arg)));
				} else if (v != null)
					if (v instanceof StringVar) {
						argAVList.add(new StringVar("", environment.stringExpression(v.getKey())));
					} else if (v instanceof NumVar) {
						argAVList.add(new NumVar("", environment.arithmetic(v.getKey())));
					} else if (v instanceof CoordVar) {
						CoordVar cv = new CoordVar("", environment.coordExpression(v.getKey()));
						argAVList.add(cv);
					}
			}

			environment.addArray("args", argAVList);

			String fileName = "files/" + environment.stringExpression(args[0].replaceFirst(keyword + " ", ""));
			String s = BotFile
					.loadFile(fileName);
			Environment env = new Environment() {

				@Override
				public void addLog(String log) {
					environment.addLog(log);
				}

				@Override
				public void displayVariables(ArrayList<Variable> vars) {
					environment.displayVariables(vars);
				}

			};
			env.addStatements(s);
			env.parseCode(fileName, false, false);
			return true;
		}

	};

	private static final Statement CANVAS = new Statement("canvas", "The keyword canvas is used to draw images. The images can then be saved."
			+ "The first argument should be a string representing which action to be made. Choose from the following:"
			+ "\nsave | arg: pathName |	- Saves the canvas to a file path."
			+ "\nshow	- shows the current image in a frame"
			+ "\nresize	| arg1: width | arg2: height | 		- resizes the canvas"
			+ "\nhide	- hides the frame"
			+ "\ncolor	| arg1: r | arg2: g | arg3: b |	arg4: alpha |	- sets the color to be used when drawing [values from 0-255]"
			+ "\nfont	| arg1: nameStr | arg2: size |		- sets the font to be used when writing a text"
			+ "\nimage	| arg1: x | arg2: y | arg3: width | arg4: height | arg5: path |		- draw an image to the canvas"
			+ "\ncircle	| arg1: x | arg2: y | arg3: width | arg4: height |		- draw a circle on the canvas"
			+ "\nrect	| arg1: x | arg2: y | arg3: width | arg4: height | 		- draw a rectangle on the canvas"
			+ "\ntext	| arg1: x | arg2: y | arg3: textStr | 	- write a text on the canvas"
			+ "\n USE SEMICOLON TO SPLIT THE ARGUMENTS\n"
			+ "Example: canvas \"image\";25;10;200;200;\"C:\\image.png\"") {

		@Override
		public boolean parseStatement(String statement, Environment environment) {
			if (!this.isStatement(statement, environment)) {
				return false;
			}

			String[] args = statement.replace(keyword, "").trim().split(";");
			String method = environment.stringExpression(args[0]);
			int r, g, b, a;
			int x,y,w,h;
			switch(method) {
			case "save":
				String pathName = environment.stringExpression(args[1]);
				Canvas.saveImage(pathName);
			case "show":
				Canvas.show();
				break;
			case "resize":
				x = (int)environment.arithmetic(args[1]);
				y = (int)environment.arithmetic(args[2]);
				Canvas.resize(x,y);
				break;
			case "hide":
				Canvas.hide();
				break;
			case "color":
				r = (int)environment.arithmetic(args[1]);
				g = (int)environment.arithmetic(args[2]);
				b = (int)environment.arithmetic(args[3]);
				a = (int)environment.arithmetic(args[4]);
				Canvas.setColor(r,g,b,a);
			case "font":
				String fontName = environment.stringExpression(args[1]);
				int fontSize = (int) environment.arithmetic(args[2]);
				Canvas.setFont(fontName, fontSize);
				break;
			case "image":
				x = (int)environment.arithmetic(args[1]);
				y = (int)environment.arithmetic(args[2]);
				w = (int)environment.arithmetic(args[3]);
				h = (int)environment.arithmetic(args[4]);
				String path = environment.stringExpression(args[5]);
				Canvas.drawImage(x, y, w, h, path);
				break;
			case "circle":
				x = (int)environment.arithmetic(args[1]);
				y = (int)environment.arithmetic(args[2]);
				w = (int)environment.arithmetic(args[3]);
				h = (int)environment.arithmetic(args[4]);
				Canvas.drawCircle(x,y,w,h);
				break;
			case "rect":
				x = (int)environment.arithmetic(args[1]);
				y = (int)environment.arithmetic(args[2]);
				w = (int)environment.arithmetic(args[3]);
				h = (int)environment.arithmetic(args[4]);
				Canvas.drawRect(x,y,w,h);
				break;
			case "text":
				x = (int)environment.arithmetic(args[1]);
				y = (int)environment.arithmetic(args[2]);
				String text = environment.stringExpression(args[3]);
				System.out.println(x + " " + y + " " + text);
				Canvas.drawText(x, y, text);
				break;
			default:
				break;
			}
			return true;
		}

	};

	private static final Statement FOCUS = new Statement("requestfocus", "Requests focus of a Browser window. syntax: requestfocus <webpage variable>") {

		@Override
		public boolean parseStatement(String statement, Environment environment) {
			if (!this.isStatement(statement, environment)) {
				return false; 
			}
			String arg = statement.replace(keyword, "").trim();
			WebVar wv = (WebVar)environment.getVariable(arg);
			Platform.runLater(() -> {
				wv.stage.requestFocus();
			});
			
			try {
				Thread.sleep(1500);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
			return true;
		}

	};

	private static final Statement PARSE = new Statement("parse", ""
			+ "Loops through all the rows in a string and sets a given variable to the text after a given keyword on the current row. "
			+ "All arguments must be variables. "
			+ "Syntax: parse varName multiLineTextVariable keywordVariable (only variables allowed, no free strings within quotation marks.) "
			+ "Lets say you want to find a telephone number in a long multi-lined text and put it in a variable. "
			+ "The multi-line text is:     \"Hi     My number is: XX-XXXXXX Best regards, David.\" "
			+ "   You create a new variable: string telephoneNumber = \"\" "
			+ "You create a keyword variable: string keyword = \"My number is:\" "
			+ "The multi-lined text is currently stored in the clipboard. "
			+ "Then you perform parse: "
			+ "parse telephoneNumber CLIPBOARD keyword.") {

		@Override
		public boolean parseStatement(String statement, Environment environment) {
			if (!this.isStatement(statement, environment)) {
				return false;
			}
			String[] ss = statement.replaceFirst(keyword, "").trim().split(" ");
			String s1 = ss[0].trim();
			String s2 = environment.stringExpression(ss[1].trim());
			String s3 = environment.stringExpression(ss[2].trim());
			String[] scan = s2.split("\n");
			for (int i = 0; i < scan.length; i++) {
				String str = scan[i];
				if (str.contains(s3)) {
					environment.addString(s1, str.split(s3)[1].trim());
					return true;
				}
			}
			environment.addString(s1, "");
			return true;
		}

	};

	private static final Statement COLORAT = new Statement("colorat", "Get the color of a given pixel. "
			+ "All arguments need to be variables. Syntax: colorat redVar greenVar blueVar x y") {
		@Override
		public boolean parseStatement(String statement, Environment environment) {
			if (!this.isStatement(statement, environment)) {
				return false;
			}
			String[] rgbxy = statement.replaceFirst(keyword, "").trim().split(" ");
			Color c = BotUtils.colorAt((int) environment.arithmetic(rgbxy[3].trim()),
					(int) environment.arithmetic(rgbxy[4].trim()));
			environment.addNum(rgbxy[0], c.getRed());
			environment.addNum(rgbxy[1], c.getGreen());
			environment.addNum(rgbxy[2], c.getBlue());
			return true;
		}
	};

	private static final Statement COLORCOORD = new Statement("colorcoord", "Get the x- & y-coordinates where a given color is found, within a given area on the screen. "
			+ "All arguments must be variables. Syntax:"
			+ "colorcoord xVar yVar red green blue xMin xMax yMin yMax") {
		@Override
		public boolean parseStatement(String statement, Environment environment) {
			if (!this.isStatement(statement, environment)) {
				return false;
			}
			String[] xyrgbxXyY = statement.replaceFirst(keyword, "").trim().split(" ");
			Point p = BotUtils.colorPosition((int) environment.arithmetic(xyrgbxXyY[2].trim()),
					(int) environment.arithmetic(xyrgbxXyY[3].trim()),
					(int) environment.arithmetic(xyrgbxXyY[4].trim()),
					(int) environment.arithmetic(xyrgbxXyY[5].trim()),
					(int) environment.arithmetic(xyrgbxXyY[6].trim()),
					(int) environment.arithmetic(xyrgbxXyY[7].trim()),
					(int) environment.arithmetic(xyrgbxXyY[8].trim()));
			if (p != null) {
				environment.addNum(xyrgbxXyY[0].trim(), p.x);
				environment.addNum(xyrgbxXyY[1].trim(), p.y);
			} else {
				environment.addNum(xyrgbxXyY[0].trim(), -1);
				environment.addNum(xyrgbxXyY[1].trim(), -1);
			}
			return true;
		}
	};

	private static final Statement REPEAT = new Statement("repeat", "Start from the beginning of the code within this file.") {
		@Override
		public boolean parseStatement(String statement, Environment environment) {
			if (!this.isStatement(statement, environment)) {
				return false;
			}
			environment.repeat();
			return true;
		}
	};

	private static final Statement RETURN = new Statement("return", "Stop this file from running (returns to file that called it if there is one)") {
		@Override
		public boolean parseStatement(String statement, Environment environment) {
			if (!this.isStatement(statement, environment)) {
				return false;
			}
			environment.stop();
			return true;
		}
	};

	private static final Statement SCROLL = new Statement("scroll", "Perform a mouse scroll for a given amount of \"ticks\".Syntax:"
			+ "scroll 10     <- Scroll 10 ticks downwards."
			+ "scroll -5     <- Scroll 5 ticks upwards.") {
		@Override
		public boolean parseStatement(String statement, Environment environment) {
			if (!this.isStatement(statement, environment)) {
				return false;
			}
			BotUtils.scroll(environment.arithmetic(statement.replaceFirst(keyword, "").trim()));
			return true;
		}
	};

	private static final Statement FOREACHWORD = new Statement("foreachword", "Loop through each word in a long sentence. "
			+ "Both arguments need to be a variable. Syntax: foreachword (wordVar : longTextVar)") {
		@Override
		public boolean parseStatement(String statement, Environment environment) {
			if (!this.isStatement(statement, environment)) {
				return false;
			}
			String stm = statement.trim().replaceFirst(keyword, "").trim();
			if (stm.startsWith("(") && stm.endsWith(")")) {
				stm = stm.substring(1, stm.length() - 1);
			}
			String[] statements = stm.split(":");
			environment.createForEachWordLoop(statements[0].trim(), environment.stringExpression(statements[1].trim()));
			return true;
		}
	};

	private static final Statement FOREACHLETTER = new Statement("foreachletter", "Loop through each letter in a string. "
			+ "Both arguments need to be a variable. Syntax: foreachletter (letterVar : longTextVar)") {
		@Override
		public boolean parseStatement(String statement, Environment environment) {
			if (!this.isStatement(statement, environment)) {
				return false;
			}
			String stm = statement.trim().replaceFirst(keyword, "").trim();
			if (stm.startsWith("(") && stm.endsWith(")")) {
				stm = stm.substring(1, stm.length() - 1);
			}
			String[] statements = stm.split(":");
			environment.createForEachLetterLoop(statements[0].trim(), environment.stringExpression(statements[1].trim()));
			return true;
		}
	};

	private static final Statement COMMENT = new Statement("//", "Comment") {
		@Override
		public boolean parseStatement(String statement, Environment environment) {
			if (!this.isStatement(statement, environment)) {
				return false;
			}			
			return true;
		}
	};

	private static final Statement LASTLETTERS = new Statement("lastletters", "Retrieves the last X letters from a string. Syntax: lastletters newString withinString lastAmountNum") {
		@Override
		public boolean parseStatement(String statement, Environment environment) {
			if (!this.isStatement(statement, environment)) {
				return false;
			}
			String[] variables = statement.replaceFirst(keyword, "").trim().split(" ");
			int numLast = (int) environment.arithmetic(variables[2].trim());
			String str = environment.stringExpression(variables[1]);
			String newString = str.substring(str.length() - numLast);
			environment.addString(variables[0].trim(), newString);
			return true;
		}
	};

	private static final Statement PLUSDAYS = new Statement("plusdays", "Adds an amount of days to a date string. "
			+ "The new date variable (first argument) will be set to the new date. "
			+ "The date string should be on the form YYYY-MM-DD and must be a variable. "
			+ "The day argument can be either a variable or a directly written number. "
			+ "Syntax: plusdays newDateVar dateString numDays") {
		@Override
		public boolean parseStatement(String statement, Environment environment) {
			if (!this.isStatement(statement, environment)) {
				return false;
			}
			String[] variables = statement.replaceFirst(keyword, "").trim().split(" ");
			LocalDate ld = LocalDate.parse(environment.stringExpression(variables[1])).plusDays((int) environment.arithmetic(variables[2]));
			environment.addString(variables[0], ld.toString());			
			return true;
		}
	};

	private static final Statement DATEDIFF = new Statement("datediff", "Calculates the number of days between two dates. Syntax: datediff dateNumVar date1 date2") {
		@Override
		public boolean parseStatement(String statement, Environment environment) {
			if (!this.isStatement(statement, environment)) {
				return false;
			}
			String[] variables = statement.replaceFirst(keyword, "").trim().split(" ");
			LocalDate ld1 = LocalDate.parse(environment.stringExpression(variables[1]));
			LocalDate ld2 = LocalDate.parse(environment.stringExpression(variables[2]));
			environment.addNum(variables[0], ld1.until(ld2, ChronoUnit.DAYS));			
			return true;
		}
	};

	private static final Statement DAYOFWEEK = new Statement("dayofweek", "Returns the day of week of a date value. Syntax: dayofweek weekdayVar dateString") {
		@Override
		public boolean parseStatement(String statement, Environment environment) {
			if (!this.isStatement(statement, environment)) {
				return false;
			}
			String[] variables = statement.replaceFirst(keyword, "").trim().split(" ");
			String varName = variables[0];
			LocalDate ld = LocalDate.parse(environment.stringExpression(variables[1]));
			environment.addString(varName, ld.getDayOfWeek().toString());	
			return true;
		}
	};

	private static final Statement CONFIG = new Statement("$", "Adds a variable to the configuration file. This is useful when switching the environment in which the application is run. Syntax: "
			+ "$[datatype] [name] = [value]") {
		@Override
		public boolean parseStatement(String statement, Environment environment) {
			if (this.isStatement(statement, environment)) {
				return true;
			} else {
				return false;
			}
		}
	};

	private static final Statement CMD = new Statement("cmd", "Sends a command to cmd.exe") {
		@Override
		public boolean parseStatement(String statement, Environment environment) {
			if (!this.isStatement(statement, environment)) {
				return false;
			}
			String command = statement.replaceFirst(keyword, "").trim();
			String txt = environment.stringExpression(command);
			BotUtils.cmdExec(txt);
			return true;
		}
	};

	private static final Statement ACCOUNT = new Statement("account", "Creates an account variable, containing username and password. "
			+ "Contains functions for encrypting/decrypting the password."
			+ "No syntax. Requires a PROMPT.") {
		@Override
		public boolean parseStatement(String statement, Environment environment) {
			if (!this.isStatement(statement, environment)) {
				return false;
			}
			String[] account = statement.split("=")[1].trim().split(" ");
			String key = statement.replaceFirst(keyword, "").split("=")[0].trim();
			String name = account[0];
			String encryptedPassword = account[1];
			environment.addAccount(key, name, encryptedPassword);
			return true;
		}
	};

	private static final Statement PASTEPASSWORD = new Statement("pastepassword", "Pastes the password of a specified account variable. The account variable is then removed.") {
		@Override
		public boolean parseStatement(String statement, Environment environment) {
			if (!this.isStatement(statement, environment)) {
				return false;
			}
			String password = ((AccountVar) environment.getVariable(statement.replaceFirst(keyword, "").trim())).revealPassword();
			BotUtils.setClipboard(password);
			BotUtils.key(KeyAdapter.kc("CTRL"), KeyAdapter.kc("V"));
			BotUtils.setClipboard("");
			//environment.removeAccount(statement.replaceFirst(keyword, "").trim());
			return true;
		}
	};

	private static final Statement PASTEUSERNAME = new Statement("pasteusername", "Pastes the username of a specified account variable.") {
		@Override
		public boolean parseStatement(String statement, Environment environment) {
			if (!this.isStatement(statement, environment)) {
				return false;
			}
			String password = ((AccountVar) environment.getVariable(statement.replaceFirst(keyword, "").trim())).revealUsername();
			BotUtils.setClipboard(password);
			BotUtils.key(KeyAdapter.kc("CTRL"), KeyAdapter.kc("V"));
			BotUtils.setClipboard("");
			return true;
		}
	};

	private static final Statement COORD = new Statement("coord", "Creates a new coordinate variable. Syntax: coord name = x y") {
		@Override
		public boolean parseStatement(String statement, Environment environment) {
			if (!this.isStatement(statement, environment)) {
				return false;
			}
			String[] variable = statement.replaceFirst(keyword, "").trim().split("=");
			String[] nums = variable[1].trim().split(" ");
			environment.addCoord(variable[0].trim(), new double[]{environment.arithmetic(nums[0].trim()), environment.arithmetic(nums[1].trim())});
			return true;
		}
	};
	Prompt prompt;
	String value = "";
	private static final Statement PROMPT = new Statement("prompt", "Prompts the user based on a set of parameters. Syntax: prompt TYPE varName description") {
		@Override
		public boolean parseStatement(String statement, Environment environment) {
			if (!this.isStatement(statement, environment)) {
				return false;
			}
			value = "";
			String[] args = statement.replaceFirst(keyword, "").trim().split(" ");
			String type = args[0];
			String varName = args[1];
			String descr = statement.replaceFirst(keyword, "").trim().replaceFirst(type, "").trim().replaceFirst(varName, "").trim();

			Prompt.Type t = Prompt.Type.valueOf(type);

			Platform.runLater(() -> {
				prompt = new Prompt(t, (int)(Window.window.getX() + Window.window.getWidth() / 2), (int) (Window.window.getY() + Window.window.getHeight()), varName, descr);
				value = prompt.getValue();
			});

			while (value.equals("")) {
				try {
					Thread.sleep(100);
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
			}
			if (value.equals(" ")) {
				return true;
			}
			switch (t) {
			case ACCOUNT:
				environment.addString(varName, value);
				break;
			case STRING:
				environment.addString(varName, value);
				break;
			case NUM:
				environment.addNum(varName, environment.arithmetic(value));
				break;
			case COORD:
				environment.addCoord(varName, new double[]{Double.parseDouble(value.split(" ")[0]), Double.parseDouble(value.split(" ")[1])});
				break;
			}
			return true;
		}
	};

	private static final Statement MESSAGE = new Statement("message", "Shows a message on the screen which disappears after a specified time(in ms). Syntax: message <num TimeToCloseMS>;<string message>") {

		@Override
		public boolean parseStatement(String statement, Environment environment) {
			if (!this.isStatement(statement, environment)) {
				return false;
			}

			String[] args = statement.replaceFirst("message", "").trim().split(";");

			long time = (long) environment.arithmetic(args[0]);
			String message = environment.stringExpression(args[1]);
			int x = (int) (Window.window.getX() + Window.window.getWidth()/2-100);
			int y = (int) (Window.window.getY() + Window.window.getHeight()/2-100);

			Platform.runLater(() -> {
				final Dialog<String> d = new Dialog<String>();
				d.setContentText(message);
				d.setX(x);
				d.setY(y);
				d.setTitle(message);
				d.initModality(Modality.NONE);
				d.setResult(message);
				d.show();



				new Thread(() -> {
					try {
						Thread.sleep(time);
						Platform.runLater(() -> {
							d.hide();
						});
					} catch (InterruptedException e) {
						e.printStackTrace();
					}
				}).start();
				try {
					Thread.sleep(time);
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
			});
			return true;
		}

	};

	private static final Statement SAVELOG = new Statement("savelog", "Saves the log. No arguments.") {
		@Override
		public boolean parseStatement(String statement, Environment environment) {
			if (!this.isStatement(statement, environment)) {
				return false;
			}
			Window.window.saveLog();
			return true;
		}
	};

	private static final Statement ADDSTAT = new Statement("addstat", "Add a statistic. syntax: addstat nameString") {
		@Override
		public boolean parseStatement(String statement, Environment environment) {
			if (!this.isStatement(statement, environment)) {
				return false;
			}

			String name = statement.replaceFirst(keyword, "").trim();
			name = environment.stringExpression(name);

			StatUtils.addStat(name, 1);		

			return true;
		}
	};

	private static final Statement WEBSTRINGFORMAT = new Statement("webstringformat", "Makes all the newlines compatible with JavaScript. Syntax: webstringformat stringVar") {
		@Override
		public boolean parseStatement(String statement, Environment environment) {
			if (!this.isStatement(statement, environment)) {
				return false;
			}
			String varName = statement.replaceFirst(keyword, "").trim();
			StringVar sv = (StringVar) environment.getVariable(varName);
			String expr = environment.stringExpression(varName);

			String newExpr = "";

			for(int i = 0; i < expr.length(); i++) {
				if (expr.charAt(i) == '\n' || expr.charAt(i) == '\r') {
					newExpr += "\\n";
				} else {
					newExpr += expr.charAt(i);
				}
			}

			sv.setValue(newExpr);

			return true;
		}
	};

	private static final Statement EXCEL = new Statement("excel", "Creates a new excel variable. Syntax: excel varName = \"pathOfFile.xlsx\"") {

		@Override
		public boolean parseStatement(String statement, Environment environment) {
			if (!this.isStatement(statement, environment)) {
				return false;
			}

			String name, path;

			name = statement.replaceFirst(keyword, "").trim().split("=")[0].trim();
			path = environment.stringExpression(statement.substring(statement.indexOf("=") + 1).trim());

			environment.addExcel(name, path);

			return true;
		}

	};

	private static final Statement CLOSEEXCELSTREAM = new Statement("closeexcelstream", "Closes an excel stream, allowing other processes to modify the previously opened Excel file. Syntax: closeexcelstream <excelVar>") {
		@Override
		public boolean parseStatement(String statement, Environment environment) {
			if (!this.isStatement(statement, environment)) {
				return false;
			}

			String varName = statement.replaceFirst(keyword, "").trim();
			ExcelVar ev = environment.getExcel(varName);
			if (ev != null) {
				ev.getExcel().closeStream();
			}

			return true;
		}
	};

	private static final Statement GETCELLVALUE = new Statement("getcellvalue", "Returns the value of specified cell (and sheet). Syntax: getcellvalue stringVar;ExcelVar;sheetNum;rowNum;colNum;") {

		@Override
		public boolean parseStatement(String statement, Environment environment) {
			if (!this.isStatement(statement, environment)) {
				return false;
			}

			String excelVarName;
			int sheetNum, rowNum, colNum;

			String[] args = statement.replaceFirst(keyword, "").trim().split(";");
			String strVarName = args[0];
			if (!environment.existsVariable(strVarName)) {
				environment.addString(strVarName, "");
			}
			StringVar sv = (StringVar)environment.getVariable(strVarName);
			excelVarName = args[1];
			sheetNum = (int) environment.arithmetic(args[2]);
			rowNum = (int) environment.arithmetic(args[3]);
			colNum = (int) environment.arithmetic(args[4]);

			ExcelVar ev = environment.getExcel(excelVarName);

			sv.setValue(ev.getExcel().getValueOfCell(sheetNum, rowNum, colNum));

			return true;
		}

	};

	private static final Statement SETCELLVALUE = new Statement("setcellvalue", "Sets the value of specified cell (and sheet). Syntax: setcellvalue stringValue;ExcelVar;sheetNum;rowNum;colNum;") {

		@Override
		public boolean parseStatement(String statement, Environment environment) {
			if (!this.isStatement(statement, environment)) {
				return false;
			}

			String excelVarName;
			int sheetNum, rowNum, colNum;

			String[] args = statement.replaceFirst(keyword, "").trim().split(";");
			String strValue = environment.stringExpression(args[0]);
			excelVarName = args[1];
			sheetNum = (int) environment.arithmetic(args[2]);
			rowNum = (int) environment.arithmetic(args[3]);
			colNum = (int) environment.arithmetic(args[4]);

			ExcelVar ev = environment.getExcel(excelVarName);

			ev.getExcel().setValueOfCell(sheetNum, rowNum, colNum, strValue);

			return true;
		}

	};

	private static final Statement SAVEEXCEL = new Statement("saveexcel", "Saves the excel file.") {

		@Override
		public boolean parseStatement(String statement, Environment environment) {
			if (!this.isStatement(statement, environment)) {
				return false;
			}

			String varName = statement.replaceFirst(keyword, "").trim();
			ExcelVar ev = environment.getExcel(varName);
			if (ev != null) {
				ev.getExcel().save();
			}

			return true;
		}

	};

	private static final Statement BROWSERFORM = new Statement("browserform", "creates a browser form. Syntax: browserform WebVAR;Title;FunctionName;arg0;arg1;arg2;arg3;...;arg_n;") {
		@Override
		public boolean parseStatement(String statement, Environment environment) {
			if (!this.isStatement(statement, environment)) {
				return false;
			}

			String[] args = statement.replaceFirst(keyword, "").trim().split(";");

			WebVar wv = (WebVar) environment.getVariable(args[0]);
			String[] args2 = new String[args.length-3 ];
			for(int i = 0; i < args.length-3; i++) {
				args2[i] = environment.stringExpression(args[i+3]);
			}
			Platform.runLater(() -> {
				wv.addForm(environment.stringExpression(args[1]), environment.stringExpression(args[2]), args2);
			});

			return true;
		}
	};

	private static final Statement SAVEEXCELTEXT = new Statement("saveexcelastext", "Saves the excel file in a text (tab delimited) format. Syntax: saveexcelastext excelVar;(string)fileName", new Argument(Type.excel, "excelvar"), new Argument(Type.string, "fileName")) {

		@Override
		public boolean parseStatement(String statement, Environment environment) {
			if (!this.isStatement(statement, environment)) {
				return false;
			}
			String[] args = statement.replaceFirst(keyword, "").trim().split(";");
			String varName = args[0];
			String fileName = args[1];
			ExcelVar ev = environment.getExcel(varName);
			if (ev != null) {
				//ev.getExcel().saveAs(fileName);
			}

			return true;
		}

	};

	private static final Statement GOTOROW = new Statement("gotorow", "Go to a row number in current function or file. If it's a function, the first row of the function is always 0 although it's placed within a file.") {
		public boolean parseStatement(String statement, Environment environment) {
			if (!this.isStatement(statement, environment)) {
				return false;
			}
			int rownum = (int) environment.arithmetic(statement.replace(keyword, "").trim());
			environment.setCodeIndex(rownum - 1);
			return true;
		}
	};

	private static final Statement STOPCODE = new Statement("stopcode", "Stops all code from running.") {
		public boolean parseStatement(String statement, Environment environment) {
			if (!this.isStatement(statement, environment)) {
				return false;
			}
			Window.forcestop();
			return true;
		}
	};

	private static final Statement PLAYFAILURE = new Statement("playfailure", "Plays a failure sound.") {
		public boolean parseStatement(String statement, Environment environment) {
			if (!this.isStatement(statement, environment)) {
				return false;
			}
			Sound.playFailure();
			return true;
		}
	};

	private static final Statement TONUMBER = new Statement("tonumber", "Converts a string to a number. Non-digit characters are ignored. Syntax: tonumber numVar;<string>") {
		public boolean parseStatement(String statement, Environment environment) {
			if (!this.isStatement(statement, environment)) {
				return false;
			}
			statement = statement.replace("tonumber", "").trim();
			String[] args = statement.split(";");
			String varName = args[0].trim();
			String expr = environment.stringExpression(args[1].trim());
			String numexpr = "";
			for (int i = 0; i < expr.length(); i++) {
				if (Character.isDigit(expr.charAt(i)) || ("" + expr.charAt(i)).equals(".")) {
					numexpr += expr.charAt(i);
				}
			}
			if (numexpr.isEmpty()) {
				numexpr = "0";
			}
			double number = Double.parseDouble(numexpr);
			System.out.println("num : " + number);
			Variable v = environment.getVariable(varName);
			if (v == null) {
				environment.addNum(varName, number);
			} else {
				((NumVar)v).setValue(number);
			}
			return true;
		}
	};

	private static final Statement INTSTRING = new Statement("intstring", "Converts a number to an integer and then convert it to a string. Syntax: intstring <string:newString>;<num>number") {
		public boolean parseStatement(String statement, Environment environment) {
			if (!this.isStatement(statement, environment)) {
				return false;
			}
			statement = statement.replace("intstring", "").trim();
			String[] args = statement.split(";");
			Variable v = environment.getVariable(args[0].trim());
			double d = environment.arithmetic(args[1].trim());
			int i = (int)d;
			String newVal = "" + i;
			if (v != null) {
				v.setValue(newVal);
			} else {
				environment.addString(args[0].trim(), newVal);
			}
			return true;
		}
	};

	private static final Statement SNAPSHOT = new Statement("snapshot", "Creates a snapshot of the screen. Syntax: snapshot <string::name>") {
		public boolean parseStatement(String statement, Environment environment) {
			if (!this.isStatement(statement, environment)) {
				return false;
			}
			String arg = environment.stringExpression(statement.replaceFirst("snapshot", "").trim());
			BotUtils.createScreenshot(arg);
			return true;
		}
	};


	private static final Statement SENDMAIL = new Statement("sendmail" , "Sends an email containing subject and description to a given recipient. Requires a password. Syntax: sendmail recipient;subject;description;robotpw") {
		public boolean parseStatement(String statement, Environment environment) {
			if (!this.isStatement(statement, environment)) {
				return false;
			}
			String[] args = statement.replaceFirst("sendmail", "").trim().split(";");
			String recipient = environment.stringExpression(args[0]);
			String subject = environment.stringExpression(args[1]);
			String description = environment.stringExpression(args[2]);
			String pw = environment.stringExpression(args[3]);
			File[] files = new File[0];
			if(args.length > 4) {
				files = Attachment.getFiles();
			}

			Outlook.sendMail(recipient, subject, description, Outlook.name, pw, files);

			return true;
		}
	};

	private static final Statement DATASET = new Statement("dataset", "Creates and loads a data set from the hard drive. The data is then stored as a string. Syntax: dataset fileName\nfileName must not be an existing variable.") {
		public boolean parseStatement(String statement, Environment environment) {
			if (!this.isStatement(statement, environment)) {
				return false;
			}
			String arg = statement.replaceFirst("dataset", "").trim();
			String value = BotFile.loadData(arg);
			if (value == null) {
				value = "";
			}
			environment.addString(arg, value);
			return true;
		}
	};

	private static final Statement ADDTODATASET = new Statement("addtodataset", "Adds a string to an existing dataset. Syntax: addtodataset <dataset>;string") {

		public boolean parseStatement(String statement, Environment environment) {
			if (!this.isStatement(statement, environment)) {
				return false;
			}
			String[] args = statement.replaceFirst("addtodataset", "").trim().split(";");

			String value = environment.stringExpression(args[0].trim());
			String newData = environment.stringExpression(args[1].trim());

			value += "\r\n" + newData;
			environment.addString(args[0].trim(), value);
			return true;
		}

	};

	private static final Statement SAVEDATASET = new Statement("savedataset", "Saves a data set. Syntax: savedataset <stringvariable>") {
		public boolean parseStatement(String statement, Environment environment) {
			if (!this.isStatement(statement, environment)) {
				return false;
			}
			String arg = statement.replaceFirst("savedataset", "").trim();
			String value = environment.stringExpression(arg);
			BotFile.saveDataset(arg, value);
			return true;
		}
	};

	private static final Statement SAVETEXTFILE = new Statement("savetextfile", "Saves a text file to Desktop.") {

		@Override
		public boolean parseStatement(String statement, Environment environment) {
			if (!this.isStatement(statement, environment)) {
				return false;
			}
			String arg = statement.replaceFirst("textfile", "").trim();
			//TODO implement
			return true;
		}

	};

	private static final Statement GETWORD = new Statement("getword", "Retrieves the word (separated by optional character[default: whitespace]) within a string at specified index. Syntax: getword <NewVarName>;<num index>;<string withinString>;[optional: character(s)]") {
		public boolean parseStatement(String statement, Environment environment) {
			if (!this.isStatement(statement, environment)) {
				return false;
			}
			String[] args = statement.replaceFirst("getword", "").trim().split(";");
			String newVarName = args[0].trim();
			double index = environment.arithmetic(args[1].trim());
			String withinString = environment.stringExpression(args[2].trim());
			String splitCharacters = " ";
			if (args.length > 3 && args[3] != null && !args[3].isEmpty()) {
				splitCharacters = environment.stringExpression(args[3]);
			}
			environment.addString(newVarName, withinString.split(splitCharacters)[(int)index]);
			return true;
		}
	};

	/*public static void main(String[] args) {
		String delimiter = "&#x000D;&#x000A;";
		for(Statement s : STATEMENTS) {
			System.out.print(s.keyword + delimiter);
		}
	}*/

	public static Config getConfig(String statement) {
		String str = statement.replaceAll("\\$", "");
		String descr = str.split("=")[1].replaceAll("\"", "");
		String type = str.split(" ")[0].trim();
		String name = str.split(" ")[1].trim(); 
		Config c = new Config(name, type, descr);
		return c;
	}

	public static final Statement[] STATEMENTS = new Statement[] {
			DAYOFWEEK, PROMPT, PASTEPASSWORD, PASTEUSERNAME, ACCOUNT, CMD, CONFIG, STRING, NUM, DATASET, ADDTODATASET, RESETATTACHMENT, ATTACHMENT, SAVEDATASET, 
			MESSAGE, WEBPAGE, WEBSTRINGFORMAT, COORD, SEQUENCE, SETSMSCODE, SETAUTH, ARRAYADD, ARRAYREMOVE, SIZEOF, ARRAY, MOUSEMOVE, SMOOTHMOVE, LEFTCLICK, 
			RIGHTCLICK, SCROLL, LEFTDOWN, LEFTUP, RIGHTDOWN, RIGHTUP, IF, ENDIF, ELSEIF, CHECKELEMENT, TONUMBER, INTSTRING, ELSE, GETLASTCMSG, PRINT, 
			KEYDOWN, KEYUP, KEY, WAIT, CLIPBOARD, PLAYFAILURE, GETWORD, FOREACHWORD, FOREACHLETTER, FOR, STOPCODE, SENDMAIL, ENDFOR,
			LOAD, PARSE, COLORAT, COLORCOORD, REPEAT, RETURN, COMMENT, CANVAS, LASTLETTERS, PLUSDAYS, DATEDIFF, FUNCTION_CALL, WEBSETCONTENT,
			WEBCLICK, WEBEXECUTE, WEBAWAIT, FOCUS, WEBMAXIMIZE, WEBRESTORE, WEBMINIMIZE, WEBGETCONTENT, SAVELOG, ADDSTAT, UPDATE_VARIABLE, 
			FUNCTION, ENDFUNCTION, JAVASCRIPT, ENDJAVASCRIPT, CHECKARGS, EXCEL, GETCELLVALUE, SETCELLVALUE, SAVEEXCEL, CLOSEEXCELSTREAM, GOTOROW, BROWSERFORM, 
			SNAPSHOT
	};
}
