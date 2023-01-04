package model.scripting;

import java.io.File;
import java.io.FileNotFoundException;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Scanner;

import javafx.application.Platform;
import javafx.stage.Stage;
import model.BotUtils;
import model.Cache;
import security.Account;
import view.Window;

public abstract class Environment {
	private static ArrayList<Variable> variables = new ArrayList<>();
	private ArrayList<String> log = new ArrayList<>();
	private ArrayList<String> codeRows = new ArrayList<>();

	private ArrayList<BoolWait> ifBools = new ArrayList<>();
	private ArrayList<ForLoop> forLoops = new ArrayList<>();

	private ArrayList<Variable> arguments = new ArrayList<>();

	public Environment() {
		GlobalVariables.addVariables(this);
	}

	public void loadVariables(String str) {
		Scanner scan = new Scanner(str);
		while (scan.hasNextLine()) {
			parseStatement(scan.nextLine());
		}
		scan.close();
	}

	public boolean existsVariable(String keyword) {
		Object[] obs = variables.toArray();
		for (Object o : obs) {
			Variable v = (Variable) o;
			if (keyword.equals(v.getKey())) {
				return true;
			}
		}
		return false;
	}

	public ArrayVar getArguments() {
		return new ArrayVar("args", arguments);
	}
	
	public Variable getVariable(String name) {
		boolean isArray = name.contains("[") && name.contains("]");
		int num = 0;
		if (isArray) {
			num = (int) arithmetic(name.substring(name.indexOf("[")+1, name.indexOf("]")));
			name = name.substring(0, name.indexOf("["));
			if (name.equals("args")) {
				return (Variable)((ArrayList<?>) new ArrayVar(name, arguments).getValue()).get(num);
			}
		}
		Object[] obs = variables.toArray();
		for (Object o : obs) {
			Variable v = (Variable) o;
			if (v.getKey().equals(name)) {
				if (isArray) {
					Object var = v.getValue();
					if (var instanceof ArrayList<?>) {
						return (Variable)((ArrayList<?>) var).get(num);
					}
				}
				return v;
			}
		}
		return null;
	}

	public abstract void addLog(String log);

	public ArrayList<String> getLog() {
		return log;
	}

	public void addString(String name, String value) {
		Variable v = getVariable(name);
		if (v == null) {
			variables.add(new StringVar(name, value));
		} else {
			((StringVar) v).setValue(value);
		}
	}

	public void parseStatement(String statement) {
		statement = statement.trim();
		try {
			for (Statement s : Statement.STATEMENTS) {
				if (s.parseStatement(statement, this)) {
					System.out.println(statement + " || " + s.getKeyword());
					break;
				}
			}
		} catch (Exception e) {addLog("An error has occured on row: " + codeIndex + "\n\t" + e); System.err.print(e);}
	}

	public boolean bool(String statement) {
		int parCount = 0;
		int parStart = -1;
		String noParStatement = "";
		if (statement.startsWith("(") && statement.endsWith(")")) {
			statement = statement.substring(1, statement.length() - 1);
		}
		// This for loop will solve the parentheses in a recursive manner, by
		// solving the outermost parentheses.
		for (int i = 0; i < statement.length(); i++) {
			char c = statement.charAt(i);
			if (c == '(') {
				if (parCount == 0) {
					parStart = i;
				}
				parCount++;
			} else if (c == ')') {
				if (parCount == 1) {
					String parSubString = statement.substring(parStart + 1, i);
					noParStatement += bool(parSubString);
				}
				parCount--;
			} else {
				if (parCount < 1) {
					noParStatement += c;
				}
			}
		}
		boolean expression = false;
		if (noParStatement.contains("OR")) {
			String[] solve = noParStatement.split("OR");
			for (int i = 0; i < solve.length; i++) {
				if (bool(solve[i].trim())) {
					expression = true;
					break;
				}
			}
		} else if (noParStatement.contains("AND")) {
			String[] solve = noParStatement.split("AND");
			expression = true;
			for (int i = 0; i < solve.length; i++) {
				if (!bool(solve[i].trim())) {
					expression = false;
					break;
				}
			}
		} else if (noParStatement.contains(">=")) {
			String[] solve = noParStatement.split(">=");
			expression = arithmetic(solve[0].trim()) >= arithmetic(solve[1].trim());
		} else if (noParStatement.contains(">")) {
			String[] solve = noParStatement.split(">");
			expression = arithmetic(solve[0].trim()) > arithmetic(solve[1].trim());
		} else if (noParStatement.contains("<=")) {
			String[] solve = noParStatement.split("<=");
			expression = arithmetic(solve[0].trim()) <= arithmetic(solve[1].trim());
		} else if (noParStatement.contains("<")) {
			String[] solve = noParStatement.split("<");
			expression = arithmetic(solve[0].trim()) < arithmetic(solve[1].trim());
		} else if (noParStatement.contains("==")) {
			String[] solve = noParStatement.split("==");
			expression = arithmetic(solve[0].trim()) == arithmetic(solve[1].trim());
		} else if (noParStatement.contains("!=")) {
			String[] solve = noParStatement.split("!=");
			expression = arithmetic(solve[0].trim()) != arithmetic(solve[1].trim());
		} else if (noParStatement.contains("contains")) {
			String[] solve = noParStatement.split("contains");
			expression = stringExpression(solve[0].trim()).trim().contains(stringExpression(solve[1].trim()).trim());
		} else if (noParStatement.contains("equals")) {
			String[] solve = noParStatement.split("equals");
			expression = stringExpression(solve[0].trim()).trim().equals(stringExpression(solve[1].trim()).trim());
		} else if (noParStatement.contains("startswith")) {
			String[] solve = noParStatement.split("startswith");
			expression = stringExpression(solve[0].trim()).startsWith(stringExpression(solve[1].trim()));
		} else if (noParStatement.contains("endswith")) {
			String[] solve = noParStatement.split("endswith");
			expression = stringExpression(solve[0].trim()).endsWith(stringExpression(solve[1].trim()));
		} else if (noParStatement.contains("isLetter")) {
			String solve = noParStatement.replaceAll("isLetter", "").trim();
			if (stringExpression(solve).isEmpty()) {
				expression = false;
			} else {
				expression = Character.isAlphabetic(stringExpression(solve).charAt(0));
			}
		} else if (noParStatement.contains("isWeekend")) {
			String solve = noParStatement.replaceAll("isWeekend", "").trim();
			if (stringExpression(solve).isEmpty()) {
				expression = false;
			} else {
				LocalDate ldt = LocalDate.parse(solve);
				expression = ldt.getDayOfWeek().equals(java.time.DayOfWeek.SATURDAY) || ldt.getDayOfWeek().equals(java.time.DayOfWeek.SUNDAY);
			}
		} else if (noParStatement.contains("isNumber")) {
			String solve = noParStatement.replaceAll("isNumber", "").trim();
			String strExpr = stringExpression(solve);
			if (strExpr.isEmpty()) {
				expression = false;
			} else {
				char c = strExpr.charAt(0);
				expression = Character.isDigit(c);
			}
		} else if (noParStatement.contains("dateGreaterThan")) {
			String[] solve = noParStatement.split("dateGreaterThan");
			String date1 = solve[0].trim();
			String date2 = solve[1].trim();
			if (date1.isEmpty() || date2.isEmpty()) {
				expression = false;
			} else {
				LocalDate ld1 = LocalDate.parse(date1);
				LocalDate ld2 = LocalDate.parse(date2);
				expression = ld1.isAfter(ld2);
			}
		}

		return expression;

	}

	public double arithmetic(String statement) {
		int parCount = 0;
		int parStart = -1;
		String noParStatement = "";
		if (statement.replaceAll(" ", "").equals("")) {
			return 0;
		}
		// This for loop will solve the parentheses in a recursive manner, by
		// solving outermost parentheses.
		for (int i = 0; i < statement.length(); i++) {
			char c = statement.charAt(i);
			if (c == '(') {
				if (parCount == 0) {
					parStart = i;
				}
				parCount++;
			} else if (c == ')') {
				if (parCount == 1) {
					String parSubString = statement.substring(parStart + 1, i);
					noParStatement += arithmetic(parSubString);
				}
				parCount--;
			} else {
				if (parCount < 1) {
					noParStatement += c;
				}
			}
		}

		double arithmetic = 0;
		if (noParStatement.contains("+")) {
			String[] solve = noParStatement.split("\\+");
			arithmetic = arithmetic(solve[0].trim());
			for (int i = 1; i < solve.length; i++) {
				arithmetic += arithmetic(solve[i].trim());
			}
		} else if (noParStatement.contains("-") && !noParStatement.split("\\-")[0].trim().isEmpty()
				&& !noParStatement.split("\\-")[1].trim().isEmpty()) {
			String[] solve = noParStatement.split("\\-");
			arithmetic = arithmetic(solve[0].trim());
			for (int i = 1; i < solve.length; i++) {
				arithmetic -= arithmetic(solve[i].trim());
			}
		} else if (noParStatement.contains("*")) {
			String[] solve = noParStatement.split("\\*");
			arithmetic = arithmetic(solve[0].trim());
			for (int i = 1; i < solve.length; i++) {
				arithmetic *= arithmetic(solve[i].trim());
			}
		} else if (noParStatement.contains("/")) {
			String[] solve = noParStatement.split("\\/");
			arithmetic = arithmetic(solve[0].trim());
			for (int i = 1; i < solve.length; i++) {
				arithmetic /= arithmetic(solve[i].trim());
			}
		} else {
			try {
				if (isDigitsOnly(noParStatement)) {
					arithmetic = Double.parseDouble(noParStatement);
				} else {
					Variable dv = getVariable(noParStatement);
					if (dv instanceof NumVar) {
						arithmetic = (Double)((NumVar)dv).getValue();
					} else if (dv instanceof StringVar) {
						Double.parseDouble((String)dv.getValue());
					}
				}
			} catch (NumberFormatException e) {
				try {
					arithmetic = (Double) getVariable(noParStatement).getValue();
				} catch (Exception ev) {
					try {
						arithmetic = Double.parseDouble((String)getVariable(noParStatement).getValue());
					} catch (Exception exv) {
						System.err.println(exv);
					}
				}
			}
		}
		return arithmetic;
	}
	
	private boolean isDigitsOnly(String str) {
		str = str.trim();
		for(char c : str.toCharArray()) {
			if (!Character.isDigit(c))
				return false;
		}
		return true;
	}

	public String stringExpression(String statement) {
		String str = "";
		boolean upperCase = false;
		boolean lowerCase = false;
		if (statement.startsWith("@")) {
			upperCase = true;
			statement = statement.substring(1, statement.length());
		}
		if (statement.startsWith("_")) {
			lowerCase = true;
			statement = statement.substring(1, statement.length());
		}
		String[] split = statement.split("\\+");
		for (String s : split) {
			s = s.trim();
			try {
				str += s.startsWith("\"") && s.endsWith("\"") ? s.substring(1, s.length() - 1)
						: (String) getVariable(s).getValue();
			} catch (Exception e) {
				try {
				str += Double.toString((double) getVariable(s).getValue());
				} catch (Exception exv) {System.err.println(exv);}
			}
		}

		if (lowerCase) {
			str = str.toLowerCase();
		} else if (upperCase) {
			str = str.toUpperCase();
		}
		return str;
	}
	
	

	public double parseNum(String string) {
		int i;
		try {
			i = Integer.parseInt(string);
		} catch (NumberFormatException e) {
			try {
			Object temp = getVariable(string).getValue();
			if (temp instanceof String) {
				i = Integer.parseInt((String) temp);
			} else {
				i = (int) ((double) temp);
			}
			} catch (Exception exv) { System.err.println(exv); i=0;}
		}
		return i;
	}

	public void addNum(String name, double value) {
		Variable v = getVariable(name);
		if (v == null) {
			variables.add(new NumVar(name, value));
		} else {
			((NumVar) v).setValue(value);
		}
	}

	public void addArray(String name, ArrayList<Variable> value) {
		Variable v = getVariable(name);
		if (v == null) {
			if (name.equals("args")) {
				arguments = value;
			} else {
				variables.add(new ArrayVar(name, value));
			}
		} else {
			((ArrayVar) v).setValue(value);
		}
	}

	private int codeIndex = 0;

	public void setCodeIndex(int i) {
		codeIndex = i;
	}

	public int getCodeIndex() {
		return codeIndex;
	}

	public void addWebpage(String name, String url) {
		Variable v = getVariable(name);
		if (v == null) {
			WebVar wv = new WebVar(name);
			wv.setValue(url);
			variables.add(wv);
		} else {
			if (v instanceof WebVar) {
				final Stage stTemp = ((WebVar)v).stage;
				Platform.runLater( () -> {
					stTemp.close();
				});
				variables.remove(v);
				addWebpage(name, url);
			}
		}
	}

	public static long interruptor = 0;

	public void parseCode(String filename, boolean isNewThread, boolean interruptThreads) {
		final String filenamefinal = filename;
		if (isNewThread) {
			final Thread thread = new Thread(new Runnable() {
				@Override
				public void run() {
					parseCode(filenamefinal, false, false);
				}
			});
			thread.setDaemon(true);
			thread.start();
			return;
		}

		if (interruptThreads) {
			interruptor = Thread.currentThread().getId();
		}

		addLog("User: " + Account.getUser() + " has started the script: " + filename);
		boolean isconfig = false;
		for(int i = 0; i < codeRows.size(); i++) {
			if (codeRows.get(i).trim().startsWith("$")) {
				isconfig = true;
				break;
			}
		}
		if (isconfig) {
			String config = BotFile.loadConfigFile(filename);
			if (config == null || config.isEmpty()) {
				this.addLog("ERROR! Code stopped running. There's no configuration for this file!");
				if (interruptThreads) {
					interruptor = 0;
				}
				return;
			}

			for (String s : config.split("\n")) {
				parseStatement(s);
			}
		}
		if (Window.isCacheMode) {
			if (codeIndex > 0) {
				ArrayList<Variable> vars = Cache.findCodeIndex(codeIndex);
				if (vars == null) {
					this.addLog("ERROR! No cache found!");
					if (interruptThreads) {
						interruptor = 0;
					}
					return;
				}
				variables = vars;
			}
		}
		Cache.caches = new ArrayList<>();
		while (codeIndex < codeRows.size()) {
			GlobalVariables.addVariables(this);
			String str = codeRows.get(codeIndex);
			int wait = 1;
			while(str.contains("¤")) {
				str = str.replaceFirst("¤", "");
				wait += (int)(500.0*GlobalVariables.WAIT_MULTIPLIER);
			}
			
			if (str.contains("<<<DISPLAY>>>")) {
				String totalDisplay = "<<<DISPLAY>>>" + str.split("<<<DISPLAY>>>")[1];
				String display = str.split("<<<DISPLAY>>>")[1];
				Window.window.updateTitle(display);
				str = str.replace(totalDisplay, "").trim();
			}
			
			parseStatement(str);
			if (Window.isCacheMode) {
				ArrayList<Variable> newVar = new ArrayList<>();
				newVar.addAll(variables);
				Cache.caches.add(new Cache(codeIndex, newVar));
			}
			try {
				displayVars();
			} catch (Exception e) {System.err.println(e);}
			if (forLoops.size() == 0) {
				BotUtils.wait(10);
			}
			if (wait > 1) {
				parseStatement("wait " + wait);
			}
			while (interruptor != 0 && interruptor != Thread.currentThread().getId()) {
				if (!Window.IS_ALIVE) {
					codeIndex = 0;
					if (interruptThreads) {
						interruptor = 0;
					}
					return;
				}
				parseStatement("wait " + 250);
			}
			codeIndex++;
			if (!Window.IS_ALIVE) {
				codeIndex = 0;
				if (interruptThreads) {
					interruptor = 0;
				}
				return;
			}
		}
		codeIndex = 0;
		if (interruptThreads) {
			interruptor = 0;
		}
	}
	
	public String retrieveJavaScript() {
		String js = "";
		for (codeIndex++;codeIndex < codeRows.size(); codeIndex++) {
			if (codeRows.get(codeIndex).trim().startsWith("endjavascript")) {
				return js;
			}
			js += codeRows.get(codeIndex) + "\r\n";
		}
		return js;
	}

	public void displayVars() {
		displayVariables(variables);
	}

	public void loadFile(File file) {
		try {
			Scanner scanner = new Scanner(file);
			String s = "";
			while (scanner.hasNextLine()) {
				s += scanner.nextLine();
			}
			scanner.close();
			addStatements(s);
		} catch (FileNotFoundException e) {
			System.err.println(e);
		}
	}

	private void addFunction(FunctionVar fv) {
		String key = fv.getKey();
		Variable v = getVariable(key);
		if (v != null) {
			v.setValue(fv.getValue());
		} else {
			variables.add(fv);
		}
	}

	private String extractFunctions(String str) {
		if (!(str.contains("function") && str.contains("endfunction"))) {
			return str;
		}
		Scanner scanner = new Scanner(str);
		String newStr = "";
		FunctionVar fv = new FunctionVar("");
		while (scanner.hasNextLine()) {
			String row = scanner.nextLine().trim();
			if (row.startsWith(Statement.FUNCTION.keyword)) {
				fv = new FunctionVar(row.replaceFirst(Statement.FUNCTION.keyword, "").trim());

			} else if (row.startsWith(Statement.ENDFUNCTION.keyword)) {
				if (!fv.getKey().equals("")) {
					addFunction(fv);
					fv = new FunctionVar("");
				}
			} else if (!fv.getKey().equals("")) {
				fv.addCommand(row);
			} else {
				newStr += row + "\n";
			}
		}
		scanner.close();
		return newStr;
	}

	public void addStatements(String str) {
		str = extractFunctions(str);
		codeRows = new ArrayList<>();
		codeIndex = 0;
		Scanner scanner = new Scanner(str);
		while (scanner.hasNextLine()) {
			codeRows.add(scanner.nextLine());
		}
		scanner.close();

	}

	public void addIfBool(boolean ifBool) {
		ifBools.add(new BoolWait(ifBool));
	}

	public void modifyIfBool(boolean ifBool) {
		ifBools.set(ifBools.size() - 1, new BoolWait(ifBool));
	}

	public void decrementIfBool() {
		ifBools.remove(ifBools.size() - 1);
	}

	public boolean evaluateIfBoolsWaitFor() {
		for (int i = 0; i < ifBools.size(); i++) {
			if (!ifBools.get(i).bool || ifBools.get(i).wait) {
				return false;
			}
		}
		for (int i = 0; i < forLoops.size(); i++) {
			if (forLoops.get(i).waitForFor) {
				return false;
			}
		}
		return true;
	}

	public void waitForEndIf() {
		ifBools.get(ifBools.size() - 1).wait = true;
	}

	public boolean isWaitEndIf() {
		return ifBools.get(ifBools.size() - 1).wait;
	}

	public boolean getCurrentIf() {
		return ifBools.get(ifBools.size() - 1).bool;
	}

	public class BoolWait {
		private boolean bool;
		private boolean wait = false;

		public BoolWait(boolean bool) {
			this.setBool(bool);
		}

		public boolean isBool() {
			return bool;
		}

		public void setBool(boolean bool) {
			this.bool = bool;
		}

		public boolean isWait() {
			return wait;
		}

		public void setWait(boolean wait) {
			this.wait = wait;
		}
	}

	public void createForLoop(String varStatement, String condition, String updateVar) {
		parseStatement(varStatement);
		Variable v = variables.get(variables.size() - 1);
		forLoops.add(new ForLoop(v, codeIndex, condition, updateVar));
	}

	public void endFor() {
		ForLoop fl = forLoops.get(forLoops.size() - 1);
		fl.updateVariable();
		if (fl.checkCondition()) {
			codeIndex = fl.startIndex;
		} else {
			forLoops.remove(forLoops.size() - 1);
		}
	}

	public class ForLoop {
		Variable var;
		int startIndex;
		String condition;
		String updateVar;
		boolean waitForFor;

		public boolean checkCondition() {
			return bool(condition);
		}

		public void updateVariable() {
			parseStatement(updateVar);
		}

		public ForLoop(Variable var, int startIndex, String condition, String updateVar) {
			this.var = var;
			this.startIndex = startIndex;
			this.condition = condition;
			this.updateVar = updateVar;
			this.waitForFor = !checkCondition();
		}

	}

	public void createForEachWordLoop(String strVarKey, String descr) {
		String[] strings = descr.split("\\s");
		ForLoop fl = new ForLoop(null, codeIndex, null, null) {
			int i = 0;
			String[] words = strings;

			@Override
			public boolean checkCondition() {
				words = strings;
				return i < words.length;
			}

			@Override
			public void updateVariable() {
				i++;
				Variable v = getVariable(strVarKey);
				if (i < words.length) {
					v.setValue(words[i]);
					if (words[i].isEmpty()) {
						updateVariable();
					}
				}
			}

		};
		addString(strVarKey, strings[0]);
		forLoops.add(fl);
	}

	public void createForEachLetterLoop(String strVarKey, String descr) {
		ForLoop fl = new ForLoop(null, codeIndex, null, null) {
			int i = 0;
			String str = descr;

			@Override
			public boolean checkCondition() {
				str = descr;
				return i < str.length();
			}

			@Override
			public void updateVariable() {
				i++;
				Variable v = getVariable(strVarKey);
				if (i < str.length())
					v.setValue(str.charAt(i) + "");
			}

		};
		addString(strVarKey, "" + descr.charAt(0));
		forLoops.add(fl);
	}

	public abstract void displayVariables(ArrayList<Variable> vars);

	public void stop() {
		codeIndex = codeRows.size();
	}

	public void repeat() {
		codeIndex = -1;
	}

	public double[] coordExpression(String key) {
		return (double[])getVariable(key).getValue();
	}

	public void addCoord(String key, double[] value) {
		Variable v = getVariable(key);
		if (v != null) {
			variables.remove(v);
		}
		variables.add(new CoordVar(key, value));
	}

	public void addAccount(String name, String userName, String encryptedPassword) {
		AccountVar av;
		if (this.existsVariable(name)) {
			av = (AccountVar)getVariable(name);
		} else {
			av = new AccountVar(name);
			variables.add(av);
		}
		av.setValue(userName + " " + encryptedPassword);

	}

	public void removeAccount(String name) {
		Variable v = getVariable(name);
		if (v != null) {
			variables.remove(v);
		}
	}

	public void removeVariables() {
		for (Variable v : variables) {
			if (v instanceof WebVar) {
				WebVar wv = (WebVar) v;
				wv.clearThisVar();
			}
		}
		if (!variables.isEmpty()) {
			variables.removeAll(variables);
		}
		this.displayVars();
	}

	public void addExcel(String name, String path) {
		ExcelVar excelVar;
		if (existsVariable(name)) {
			excelVar = (ExcelVar)getVariable(name);
		} else {
			excelVar = new ExcelVar(name);
			variables.add(excelVar);
		}
		excelVar.setExcel(path);

	}

	public ExcelVar getExcel(String name) {
		return (ExcelVar)getVariable(name);
	}

}
