package model.scripting;

public class Config {
	
	private String name;
	private String type;
	private String value;
	
	private String description;
	
	public Config(String name, String type, String description) {
		this.setName(name);
		this.setType(type);
		this.setDescription(description);
	}
	
	public void setValue(String value) {
		this.value = value;
	}
	
	public String getValue() {
		return value;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getType() {
		return type;
	}

	public void setType(String type) {
		this.type = type;
	}

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}
}
