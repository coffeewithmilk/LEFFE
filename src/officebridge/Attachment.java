package officebridge;

import java.io.File;
import java.util.ArrayList;

public class Attachment {
	
	private static ArrayList<String> attachment_urls = new ArrayList<>();
	
	public static void resetAttachment() {
		attachment_urls = new ArrayList<>();
	}
	
	public static void addAttachment(String url) {
		if (url.endsWith("*")) {
			url = url.substring(0, url.length() - 1);
			File file = new File(url);
			for (File f : file.listFiles()) {
				attachment_urls.add(f.getAbsolutePath());
			}
		} else {
			attachment_urls.add(url);
		}
	}

	public static File[] getFiles() {
		File[] files = new File[attachment_urls.size()];
		for(int i = 0; i < attachment_urls.size(); i++) {
			files[i] = new File(attachment_urls.get(i));
		}
		return files;
	}
	
}
