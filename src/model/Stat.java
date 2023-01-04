package model;

public class Stat {
	
	String name;
	int count;
	double elapsed;
	
	public Stat(String name, int count) {
		this.name = name;
		this.count = count;
	}
	
	public Stat(String name) {
		this.name = name;
	}
	
	public void plusplus() {
		count++;
	}
	
}
