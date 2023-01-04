package officebridge;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Date;
import java.util.Properties;
import java.util.Scanner;

import javax.activation.DataHandler;
import javax.activation.FileDataSource;
import javax.mail.BodyPart;
import javax.mail.Flags.Flag;
import javax.mail.Folder;
import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.Multipart;
import javax.mail.NoSuchProviderException;
import javax.mail.Part;
import javax.mail.PasswordAuthentication;
import javax.mail.Session;
import javax.mail.Store;
import javax.mail.Transport;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeBodyPart;
import javax.mail.internet.MimeMessage;
import javax.mail.internet.MimeMultipart;

import org.apache.commons.io.IOUtils;

public class Outlook {

	public static String name = "";
	static String pw = "";
	static String host = "";
	static int port = 25;

	private static String dateMonthNum(Date d) {
		String s = d.toString().split(" ")[1];
		String dmn = "00";
		switch (s) {
		case "Jan": dmn = "01"; break;
		case "Feb": dmn = "02"; break;
		case "Mar": dmn = "03"; break;
		case "Apr": dmn = "04"; break;
		case "May": dmn = "05"; break;
		case "Jun": dmn = "06"; break;
		case "Jul": dmn = "07"; break;
		case "Aug": dmn = "08"; break;
		case "Sep": dmn = "09"; break;
		case "Oct": dmn = "10"; break;
		case "Nov": dmn = "11"; break;
		case "Dec": dmn = "12"; break;
		}
		return dmn;
	}
	
	public static void loadMailServerInfo() {
		File mailServerInfoFolderLocation = new File("mail");
		if (!mailServerInfoFolderLocation.exists()) {
			mailServerInfoFolderLocation.mkdir();
		}
		
		Scanner scanner = new Scanner(mailServerInfoFolderLocation + "//mailserver.txt");
		while(scanner.hasNextLine()) {
			String line = scanner.nextLine();
			if (line.startsWith("servername")) {
				host = line.split("\t")[1];
			} else if (line.startsWith("port")) {
				port = Integer.parseInt(line.split("\t")[1]);
			} else if (line.startsWith("username")) {
				name = line.split("\t")[1];
			} else if (line.startsWith("password")) {
				pw = line.split("\t")[1];
			}
		}
		scanner.close();
	}

	private static String dateYearNum(Date d) {
		String s = d.toString().split(" ")[5];
		return s;
	}

	private static String dateDayNum(Date d) {
		return d.toString().split(" ")[2];
	}

	private static String dateToString(Date d) {
		return dateYearNum(d) + "-" + dateMonthNum(d) + "-" + dateDayNum(d);
	}

	private static boolean dateGreaterThan(String dateGreaterThan, String otherDate) {
		LocalDate ld1 = LocalDate.parse(dateGreaterThan);
		LocalDate ld2 = LocalDate.parse(otherDate);
		return ld1.isAfter(ld2);
	}

	public static void sendMail(String recipient, String title, String description, String name, String pw, File[] files) {
		Properties props = new Properties();
		props.put("mail.debug", "true");
		props.put("mail.debug.auth", "true");
		props.put("mail.smtp.auth", "true");
		props.put("mail.smtp.starttls.enable", "true");
		props.put("mail.smtp.host", host);
		props.put("mail.smtp.port", port);

		Session session = Session.getInstance(props,
				new javax.mail.Authenticator() { 
			protected PasswordAuthentication getPasswordAuthentication() {
				return new PasswordAuthentication(name, pw);
			}
		});

		try {

			MimeMessage message = new MimeMessage(session);
			message.setFrom(new InternetAddress(name));
			message.setRecipients(Message.RecipientType.TO,
					InternetAddress.parse(recipient));
			message.setSubject(title);

			MimeBodyPart messagePart = new MimeBodyPart();
			messagePart.setText(description);

			Multipart mp = new MimeMultipart();
			mp.addBodyPart(messagePart);

			for(File file : files) {
				FileDataSource fileDataSource = new FileDataSource(file);
				MimeBodyPart attachmentPart = new MimeBodyPart();
				attachmentPart.setDataHandler(new DataHandler(fileDataSource));
				attachmentPart.setFileName(file.getName());
				mp.addBodyPart(attachmentPart);
			}

			message.setContent(mp);

			Transport.send(message);

			System.out.println("Done");

		} catch (MessagingException e) {
			throw new RuntimeException(e);
		}

	}

	

	private static String getTextFromMessage(Message message) throws Exception {
		String result = "";
		if (message.isMimeType("text/plain")) {
			result = message.getContent().toString();
		} else if (message.isMimeType("multipart/*")) {
			MimeMultipart mimeMultipart = (MimeMultipart) message.getContent();
			result = getTextFromMimeMultipart(mimeMultipart);
		}
		return result;
	}

	private static String getTextFromMimeMultipart(
			MimeMultipart mimeMultipart)  throws Exception{
		String result = "";
		int count = mimeMultipart.getCount();
		for (int i = 0; i < count; i++) {
			BodyPart bodyPart = mimeMultipart.getBodyPart(i);
			if (bodyPart.isMimeType("text/plain")) {
				result = result + "\n" + bodyPart.getContent();
				break; // without break same text appears twice in my tests
			} else if (bodyPart.isMimeType("text/html")) {
				String html = (String) bodyPart.getContent();
				result = result + "\n" + html;
			} else if (bodyPart.getContent() instanceof MimeMultipart){
				result = result + getTextFromMimeMultipart((MimeMultipart)bodyPart.getContent());
			}
		}
		return result;
	}

	public static ArrayList<Email> getEmails(String user, String password, String type, String dateString, boolean onlyAttachments, Email matchAndDelete) 
	{
		ArrayList<Email> emails = new ArrayList<>();
		int port = 143;
		switch (type) {
		case "imap": port = 143; break;
		case "pop3": port = 110; break;
		case "imaps": port = 993; break;
		case "pop3s": port = 995; break;
		}

		try {

			Properties properties = new Properties();

			properties.put("mail."+type+".host", host);
			properties.put("mail."+type+".port", "" + port);
			properties.setProperty("mail."+type+".ssl.enable", type.contains("s") ? "true" : "false");
			properties.setProperty("mail."+type+".starttls.enable", type.contains("s") ? "false" : "true"); 
			properties.setProperty("mail."+type+".starttls.required", type.contains("s") ? "false" : "true");
			
			Session emailSession = Session.getInstance(properties,
					new javax.mail.Authenticator() {
				protected PasswordAuthentication getPasswordAuthentication() {
					return new PasswordAuthentication(
							user, password);
				}
			});

			Store store = emailSession.getStore(type);

			store.connect();

			Folder emailFolder = store.getFolder("INBOX");
			emailFolder.open(Folder.READ_WRITE);

			Message[] messages = emailFolder.getMessages();
			System.out.println("messages.length---" + messages.length);

			for (int i = messages.length - 1; i >= 0; i--) {
				Message message = messages[i];
				boolean flag = message.isSet(Flag.SEEN);
				boolean neverMind = false;
				Date d1 = message.getReceivedDate();
				String dStr = dateToString(d1);
				if (dateGreaterThan(dateString, dStr)) {
					break;
				}
				ArrayList<File> fileArr = new ArrayList<>();
				String contentType = message.getContentType();
				String inlineText = getTextFromMessage(message);
				if (contentType.contains("multipart")) {
					Multipart mp = (Multipart) message.getContent();
					for (int j = 0; j < mp.getCount(); j++) {
						MimeBodyPart mbp = (MimeBodyPart) mp.getBodyPart(j);
						if (Part.ATTACHMENT.equalsIgnoreCase(mbp.getDisposition())) {
							File file = new File(mbp.getFileName());
							InputStream is = mbp.getInputStream();
							OutputStream outStream = new FileOutputStream(file);
							byte[] buffer = new byte[8 * 1024];
							int bytesRead;

							while ((bytesRead = is.read(buffer)) != -1) {
								outStream.write(buffer, 0, bytesRead);
							}

							IOUtils.closeQuietly(is);
							IOUtils.closeQuietly(outStream);
							fileArr.add(file);
						}
					}
				}
				if (fileArr.size() > 0 || !onlyAttachments) {
					File[] files = new File[fileArr.size()];
					for (int x = 0; x < fileArr.size(); x++) {
						files[x] = fileArr.get(x);
					}
					String subject = message.getSubject();
					if (subject == null) { subject = ""; }			
					Email e = new Email(message.getFrom()[0].toString(), subject, inlineText, files, message);
					emails.add(e);
					if (matchAndDelete != null) {
						if (e.getTitle().trim().startsWith(matchAndDelete.getTitle().trim())) {
							message.setFlag(Flag.DELETED, true);
							neverMind = true;
						}
					}
				}
				if (!neverMind) {
					message.setFlag(Flag.SEEN, flag);
				}

			}

			//close the store and folder objects
			emailFolder.close(true);
			store.close();

		} catch (NoSuchProviderException e) {
			e.printStackTrace();
		} catch (MessagingException e) {
			e.printStackTrace();
		} catch (Exception e) {
			e.printStackTrace();
		}
		return emails;
	}
}
