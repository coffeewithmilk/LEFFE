package calendar;

import java.util.ArrayList;

public class Schedule {
	
	private boolean repeat;
	private ArrayList<String> fileOrder = new ArrayList<>();
	
	private String name;
	
	public Schedule() {
		
	}
	
	public void addFile(String fileName) {
		fileOrder.add(fileName);
	}
	
	public void removeFile(int i) {
		fileOrder.remove(i);
	}
	
	public ArrayList<String> fileOrder() {
		return fileOrder;
	}
	
	public void setRepeat(boolean repeat) {
		this.repeat = repeat;
	}
	
	public boolean isRepeat() {
		return repeat;
	}
	
	public static Schedule load(String fileName, String content) {
		Schedule sch = new Schedule();
		sch.name = fileName;
		for (String s : content.split("\n")) {
			if (s.startsWith("load")) {
				sch.fileOrder.add(s.replaceAll("load", "").replaceAll("\"", "").trim());
			} else if (s.contains("repeat")) {
				sch.repeat = true;
			}
		}
		return sch;
	}
	
	public String generateCode() {
		String str = "";
		for (String f : fileOrder) {
			str += "load \"" + f + "\"\n";
			str += "wait 1000\n";
		}
		if (isRepeat()) {
			str += "repeat\n";
		}
		return str;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}
}
