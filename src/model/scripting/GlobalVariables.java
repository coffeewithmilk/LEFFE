package model.scripting;

import java.awt.MouseInfo;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;

import model.BotUtils;
import security.Account;

public class GlobalVariables {
	private static final StringVar CLIPBOARD = new StringVar("CLIPBOARD") {
		@Override
		public void setValue(Object value) {
			BotUtils.setClipboard((String) value);
		}

		@Override
		public String getValue() {
			return BotUtils.getClipboard();
		}
	};
	
	private static final StringVar MONTHSTRING = new StringVar("MONTHSTRING") {
		@Override
		public void setValue(Object value) {
			
		}

		@Override
		public String getValue() {
			return LocalDate.now().getMonth().name();
		}
	};
	
	private static final StringVar YEARSTRING = new StringVar("YEARSTRING") {
		@Override
		public void setValue(Object value) {
			
		}

		@Override
		public String getValue() {
			return LocalDate.now().getYear() + "";
		}
	};

	private static final NumVar MOUSE_X = new NumVar("MOUSE_X") {
		@Override
		public void setValue(Object value) {
		}

		@Override
		public Object getValue() {
			return MouseInfo.getPointerInfo().getLocation().getX();
		}
	};

	private static final NumVar MOUSE_Y = new NumVar("MOUSE_Y") {
		@Override
		public void setValue(Object value) {
		}

		@Override
		public Object getValue() {
			return MouseInfo.getPointerInfo().getLocation().getY();
		}
	};
	
	private static final CoordVar MOUSE = new CoordVar("MOUSE") {
		public void setValue(Object value) {
			
		} 
		public Object getValue() {
			double[] i = new double[]{(double)MOUSE_X.getValue(), (double)MOUSE_Y.getValue()};
			return i;
		}
	};
	
	private static final StringVar DATE = new StringVar("DATE") {
		@Override
		public void setValue(Object value) {
		}

		@Override
		public Object getValue() {
			return LocalDate.now().toString();
		}
	};
	
	private static final StringVar NEWLINE = new StringVar("NEWLINE") {
		@Override
		public void setValue(Object value) {
		}

		@Override
		public Object getValue() {
			return System.getProperty("line.separator");
		}
	};
	
	private static final StringVar USER = new StringVar("USER") {
		@Override
		public void setValue(Object value) {
		}

		@Override
		public Object getValue() {
			return Account.getUser();
		}
	};

	private static final NumVar TIME = new NumVar("TIME") {
		@Override
		public void setValue(Object value) {
		}

		@Override
		public Object getValue() {
			int time = 0;
			LocalTime lt = LocalTime.now();
			time = lt.getHour() * 100 + lt.getMinute();
			return (double)time;
		}
	};
	
	private static final ArrayVar SPECIAL_CHARACTERS = new ArrayVar("SPEC_CHARS", null) {
		
		@Override
		public void setValue(Object value) {
			
		}
		
		@Override
		public Object getValue() {
			ArrayList<Variable> arrayList = new ArrayList<Variable>();
			arrayList.add(new StringVar("", "+"));
			arrayList.add(new StringVar("", "\""));
			arrayList.add(new StringVar("", "="));
			this.val = arrayList;
			return arrayList;
		}
	};
	
	private static final StringVar AUTH_SMSCODE = new StringVar("AUTH_CODE", "") {
		
		@Override
		public void setValue(Object value) {
			
		}
		
		@Override
		public Object getValue() {
			return Account.PASSCODE;
		}
	};
	
	public static final Variable[] GLOBAL_VARIABLES = { CLIPBOARD, MOUSE_X, MOUSE_Y, DATE, MOUSE, USER, NEWLINE, TIME, SPECIAL_CHARACTERS, MONTHSTRING, YEARSTRING, AUTH_SMSCODE};
 
	public static void addVariables(Environment env) { //TODO fix if adding ArrayVar to global variables
		SPECIAL_CHARACTERS.getValue();
		for (Variable v : GLOBAL_VARIABLES) {
			if (v instanceof StringVar) {
				env.addString(v.getKey(), v.getValue().toString());
			} else if (v instanceof NumVar) {
				env.addNum(v.getKey(), (double) v.getValue());
			} else if (v instanceof CoordVar) {
				env.addCoord(v.getKey(), (double[])v.getValue());
			} else if (v instanceof ArrayVar) {
				ArrayList<Variable> val = ((ArrayVar)v).getArrayList();
				env.addArray(v.getKey(), val);
			}
		}
		env.addNum("CURRENTROW", env.getCodeIndex());
	}
	
	public static double WAIT_MULTIPLIER = 1.0;
	
}