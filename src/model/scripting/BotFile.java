package model.scripting;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Scanner;

import calendar.Schedule;

public class BotFile {

	public static String loadFile(String path) {
		String str = "";
		try {
			File file = new File(path);
			if (!file.exists()) {
				file.createNewFile();
			}
			File folder = new File("files");
			File folder2 = new File("logs");
			File folder3 = new File("config");
			File folder4 = new File("schedule");
			folder4.mkdir();
			folder3.mkdir();
			folder2.mkdir();
			folder.mkdir();
			Scanner scanner = new Scanner(file);
			while (scanner.hasNextLine()) {
				String s = scanner.nextLine();
				str += s + "\n";
			}
			scanner.close();
			return str;
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		return null;
	}
	
	public static String loadStatistics(String path) {
		File f = new File("logs/" + path);
		if (!f.exists()) {
			try {
				f.createNewFile();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		return loadFile("logs/" + path);
	}

	public static void saveFile(String path, String content) {
		try {
			File file = new File(path);
			File folder = new File("files");
			File folder2 = new File("logs");
			File folder3 = new File("config");
			File folder4 = new File("schedule");
			folder4.mkdir();
			folder3.mkdir();
			folder2.mkdir();
			folder.mkdir();
			
			file.createNewFile();
			PrintWriter pw = new PrintWriter(file);
			pw.println(content);
			pw.flush();
			pw.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public static File[] getFiles() {
		File folder = new File("files");
		return folder.listFiles();
	}

	public static ArrayList<Config> preConfiguration(String code) {
		ArrayList<Config> arr = new ArrayList<>();
		String[] strArr = code.split("\n");
		for(String str : strArr) {
			if (str.trim().startsWith("$")) {
				arr.add(Statement.getConfig(str));
			}
		}
		return arr;
	}
	
	public static String loadConfigFile(String filename) {
		if (filename.startsWith("files/")) {
			filename = filename.replace("files/", "");
		}
		return loadFile("config/" + filename.replace(".bot", "") + ".conf");
	}

	public static void createConfigFile(String currentFile, ArrayList<Config> configs) {
		String txt = "";
		for (Config c : configs) {
			txt += c.getType() + " " + c.getName() + " = " + c.getValue() + "\n";
		}
		saveFile("config/" + currentFile.replace(".bot", "") + ".conf",
				txt);
	}
	
	public static void createScheduleFile(String fileName, Schedule sch) {
		System.out.println(sch.generateCode());
		saveFile("schedule/" + fileName, sch.generateCode());
	}
	
	public static ArrayList<Schedule> loadSchedules() {
		File file = new File("schedule");
		File folder4 = new File("schedule");
		folder4.mkdir();
		ArrayList<Schedule> s = new ArrayList<>();
		for (File f : file.listFiles()) {
			s.add(Schedule.load(f.getName(), loadFile("schedule/" + f.getName())));
		}
		return s;
	}

	public static String loadData(String arg) {
		if (!arg.endsWith(".data")) {
			arg += ".data";
		}
		File file = new File("datasets");
		file.mkdir();
		return loadFile("datasets/"+arg);
	}

	public static void saveDataset(String arg, String value) {
		if (!arg.endsWith(".data")) {
			arg += ".data";
		}
		File file = new File("datasets");
		file.mkdir();
		saveFile("datasets/"+arg, value);
	}

}
