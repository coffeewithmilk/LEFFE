package model.scripting;

import java.util.ArrayList;

public class FunctionVar extends Variable {

	private String code = "";
	
	//private boolean spawnThread = false;
	
	//private boolean interruptThreads = false;

	public FunctionVar(String key) {
		super(key);
	}

	public void addCommand(String cmd) {
		code += cmd + "\n";
	}

	@Override
	public Object getValue() {
		return code;
	}

	public String toString() {
		return "[CODE]";
	}

	public void runCode(Environment environment, boolean spawnThread, boolean interruptThreads, String... args) {
		ArrayList<Variable> argAVList = new ArrayList<>();
		for (int i = 0; i < args.length; i++) {
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

		Environment e = new Environment() {

			@Override
			public void addLog(String log) {
				//environment.addLog(log);
			}

			@Override
			public void displayVariables(ArrayList<Variable> vars) {
				//environment.displayVariables(vars);
			}

		};

		e.addStatements(code);
		if (args.length > 0) {
			e.addArray("args", argAVList);
		}
		e.parseCode("", spawnThread, interruptThreads);
	}

}
