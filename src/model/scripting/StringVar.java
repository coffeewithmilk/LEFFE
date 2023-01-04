package model.scripting;

public class StringVar extends Variable {

	String value;

	public StringVar(String key, String value) {
		super(key);
		setValue(value);
	}

	public StringVar(String key) {
		super(key);
	}
	
	public String toString() {
		return this.getValue().toString();
	}
	
}
