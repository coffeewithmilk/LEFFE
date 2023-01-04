package model.scripting;

public class Argument {
	private Type type;
	private String name;
	
	enum Type {
		string,
		number,
		excel,
		account,
		array,
		coord,
		webpage
	}
	
	public Argument(Type type, String name) {
		this.setType(type);
		this.setName(name);
	}

	public Type getType() {
		return type;
	}

	public void setType(Type type) {
		this.type = type;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}
	
	
	
}
