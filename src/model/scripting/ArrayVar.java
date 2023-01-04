package model.scripting;

import java.util.ArrayList;

import model.Cache;

public class ArrayVar extends Variable {

	ArrayList<Variable> val;
	
	public ArrayVar(String key, ArrayList<Variable> value) {
		super(key);
		setValue(value);
		val = value;
	}

	public String toString() {
		String str = "";
		if (getValue() == null) {
			return "";
		}
		for (Object v : (ArrayList<?>)getValue()) {
			str += v.toString() + " ; ";
		}
		return str;
	}
	
	public void setValue(ArrayList<Variable> value) {
		super.setValue(value);
		this.val = value;
	}
	
	public ArrayList<Variable> getArrayList() {
		return val;
	}

	public static ArrayVar copy(ArrayVar v) {
		Object[] varObjArr = ((ArrayList<?>)v.getValue()).toArray();
		ArrayList<Variable> vars = new ArrayList<Variable>();
		for (Object o : varObjArr) {
			vars.add(Cache.copyVariable((Variable) o));
		}
		return new ArrayVar(v.getKey(), vars);
	}
	
}
