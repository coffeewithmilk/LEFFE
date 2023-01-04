package model.scripting;

public class NumVar extends Variable {

	public NumVar(String key, double value) {
		super(key);
		setValue(value);
	}

	public NumVar(String key) {
		super(key);
	}
	
	public String toString() {
		return Double.toString((double)this.getValue());
	}

}
