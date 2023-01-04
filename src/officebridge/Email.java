package officebridge;

import java.io.File;

import javax.mail.Message;

public class Email {
	
	private String sender;
	private String title;
	private String description;
	
	private File[] files;
	
	private Message message;
	
	public Email(String sender, String title, String description, File[] file, Message message) {
		System.out.println("---" + sender + title + description + file + message + "---");
		this.setSender(sender);
		this.setTitle(title);
		this.setDescription(description);
		this.setFiles(file);
		this.setMessage(message);
	}

	public boolean hasAttachment() {
		return files != null && files.length > 0;
	}
	
	public String getSender() {
		return sender;
	}

	public void setSender(String sender) {
		this.sender = sender;
	}

	public String getTitle() {
		return title;
	}

	public void setTitle(String title) {
		this.title = title;
	}

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	public File[] getFiles() {
		return files;
	}

	public void setFiles(File[] files) {
		this.files = files;
	}

	public Message getMessage() {
		return message;
	}

	public void setMessage(Message message) {
		this.message = message;
	}
}
