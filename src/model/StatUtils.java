package model;

import java.io.File;
import java.io.IOException;
import java.time.LocalDate;
import java.util.ArrayList;

import javax.imageio.ImageIO;

import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.embed.swing.SwingFXUtils;
import javafx.scene.Scene;
import javafx.scene.chart.PieChart;
import javafx.scene.image.WritableImage;
import javafx.stage.Stage;
import model.scripting.BotFile;

public class StatUtils {

	public static ArrayList<Stat> dailyStats = new ArrayList<>();

	public static String dailyName() {
		LocalDate ld = LocalDate.now();
		String ldStr = ld.getDayOfMonth() + "_" + ld.getMonth().toString() + "_" + ld.getYear();
		return ldStr;
	}

	public static void test() {
		initDailyStats();
		addStat("Test1", 1);
		addStat("Test1", 1);
		addStat("Test1", 1);
		addStat("Test1", 1);
		addStat("Test1", 1);
		addStat("Testar2", 1);
		addStat("Testar2", 1);
		addStat("Testar2", 1);
		addStat("Testar2", 1);
		addStat("Testing3", 1);
		addStat("Testing3", 1);
		addStat("Testing3", 1);
		addStat("Testing3", 1);
		addStat("Testing3", 1);
		addStat("Testing3", 1);
		addStat("Testing3", 1);
		addStat("Testing3", 1);
		addStat("Testing3", 1);
		addStat("Testing3", 1);
		createStatGraphics();
	}
	
	public static void addStat(String name, int count) {
		if (contains(name)) {
			Stat stat = get(name);
			if (stat != null) {
				if (count > 1) {
					stat.count = count;
				} else {
					stat.plusplus();
				}
			}
		} else {
			if (count < 1) {
				count = 1;
			}
			dailyStats.add(new Stat(name, count));
		}
	}

	public static Stat get(String name) {
		for (Stat s : dailyStats) {
			if (s.name.equals(name)) {
				return s;
			}
		}
		return null;
	}

	public static boolean contains(String name) {
		for (Stat s : dailyStats) {
			if (s.name.equals(name)) {
				return true;
			}
		}
		return false;
	}

	public static void initDailyStats() {
		String content = BotFile.loadStatistics(dailyName() + ".txt");
		dailyStats = new ArrayList<>();
		if (content != null && !content.isEmpty()) {
			for (String n : content.split("\n")) {
				if (!n.isEmpty()) {
					addStat(n.split(" ")[0], Integer.parseInt(n.split(" ")[1]));
				}
			}
		}
	}

	public static void saveStats() {
		String content = "";
		if (!dailyStats.isEmpty()) {
			for (int i = 0; i < dailyStats.size(); i++) {
				content += dailyStats.get(i).name + " " + dailyStats.get(i).count + "\n";
			}
		}
		if (!content.isEmpty()) {
			BotFile.saveFile("logs/" + dailyName() + ".txt", content);
		}
	}

	public static void createStatGraphics() {
		if (dailyStats.isEmpty()) {
			return;
		}
		Platform.runLater(() -> {
			PieChart c = new PieChart();
			c.setPrefSize(400, 400);
			ObservableList<PieChart.Data> data = FXCollections.observableArrayList();
			for (Stat s : dailyStats) {
				PieChart.Data nData = new PieChart.Data(s.name + " [ " + s.count + " st ]", s.count);
				data.add(nData);
			}
			c.setData(data);
			Scene scene = new Scene(c);
			Stage stage = new Stage();
			stage.setWidth(400);
			stage.setHeight(400);
			stage.setScene(scene);
			stage.show();
			WritableImage img = scene.snapshot(null);
			try {
				ImageIO.write(SwingFXUtils.fromFXImage(img, null), "png", new File(dailyName() + ".png"));
			} catch (IOException e) {
				e.printStackTrace();
			}
			stage.close();
		});

		}

	}
