package model.scripting;

public class CoordVar extends Variable {
	public CoordVar(String key) {
		super(key);
	}
	
	public CoordVar(String key, double[] xy) {
		super(key);
		this.setValue(xy);
	}
	
	public Object getValue() {
		return xy;
	}

	public void setValue(double[] value) {
		this.xy = value;
	}

	double[] xy;
	
	public String toString() {
		return "" + xy[0] + ", " + xy[1];
	}

}
