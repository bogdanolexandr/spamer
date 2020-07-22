package spamer;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import javax.activation.DataHandler;
import javax.activation.FileDataSource;
import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.Multipart;
import javax.mail.Session;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeBodyPart;
import javax.mail.internet.MimeMessage;
import javax.mail.internet.MimeMultipart;

import com.sun.mail.smtp.SMTPTransport;

public class Sender {

	private static String SMTP_SERVER;
	private static String USERNAME;
	private static String PASSWORD;
	private static String EMAIL_FROM;
	private static List<String> EMAIL_TO;
	private static String EMAIL_TO_CC;
	private static List<String> EMAIL_SUBJECT;
	private static List<String> EMAIL_TEXT;
	private static String ENCODING;
	private static int currentLetterIndex = 0;
	private static int currentSubjectIndex = 0;

	private static Writer writer = new Writer();
	
	private static Properties props;

	public static void main(String[] args) throws UnsupportedEncodingException {
		try {
		init();
		for (String to : EMAIL_TO) {
			sendMessage(to);
			writer.write(to);
		}
		}catch(Throwable e) {
			writer.close();
			while(true) {
				
			}
		}
		writer.close();
	}

	private static void sendMessage(String address) {
		Properties prop = getProperties();
		Session session = Session.getInstance(prop, null);
		Message msg = createMimeMessage(session, address);
		try {
			Multipart mp = createMimeBodyPart();
			msg.setContent(mp);
			SMTPTransport t = (SMTPTransport) session.getTransport("smtp");
			t.connect(SMTP_SERVER, 465, USERNAME, PASSWORD);
			t.sendMessage(msg, msg.getAllRecipients());
			System.out.println("Send to "+ address + " Response: " + t.getLastServerResponse());
			t.close();
		} catch (MessagingException e) {
			e.printStackTrace();
		}
	}

	private static void init() {
		InputStream is = null;
		props = new Properties();
		try {
			is = new FileInputStream("config.properties");
			props.load(new InputStreamReader(is, "UTF-8"));
			

		} catch (IOException e) {
			e.printStackTrace();
		}

		SMTP_SERVER = props.getProperty("SMTP_SERVER");
		USERNAME = props.getProperty("USERNAME");
		PASSWORD = props.getProperty("PASSWORD");
		EMAIL_FROM = props.getProperty("EMAIL_FROM");
		EMAIL_TO = getAddresses(initMailList());
		ENCODING = props.getProperty("ENCODING");
		EMAIL_TO_CC = "";
		EMAIL_SUBJECT = initSubjects();
		EMAIL_TEXT = initLetters();
		

	}

	private static String initMailList() {
		InputStream mail_list = null;
		try {
			mail_list = new FileInputStream("mail_list.txt");
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}
		return read(mail_list);
	}

	private static List<String> initSubjects() {
		return readFiles("subjects");
	}

	private static List<String> initLetters() {
		return readFiles("letters");
	}

	private static List<String> readFiles(String path) {
		File file = new File(path);
		String[] paths = file.list();
		InputStreamReader stream = null;
		List<String> files = new ArrayList<>();
		try {
//			System.out.println(ENCODING);
			for (String fileName : paths) {
				stream = new InputStreamReader(new FileInputStream(path + "/" + fileName),ENCODING);//"UTF-8");
				files.add(readLetter(stream));
				stream.close();
			}

		} catch (Exception e) {
			e.printStackTrace();
		}
		return files;
	}

	private static Multipart createMimeBodyPart() throws MessagingException {
		MimeBodyPart p1 = new MimeBodyPart();
		//p1.setText(getLetter());
		p1.setContent(getLetter(), "text/html; charset=utf-8");
		File file = new File("attachment");
		String[] fileNames = file.list();
		Multipart mp = new MimeMultipart();
		for (String fileName : fileNames) {
			mp.addBodyPart(addFile(fileName));
		}
		mp.addBodyPart(p1);
		return mp;
	}

	private static Message createMimeMessage(Session session, String to) {
		Message msg = new MimeMessage(session);
		try {
			msg.setFrom(new InternetAddress(EMAIL_FROM));
			msg.setRecipients(Message.RecipientType.TO, InternetAddress.parse(to, false));
			msg.setSubject(getSubject());
//			msg.setContent("<html><head></head><body><div>Доброго дня</div></body></html>", to);
		} catch (Exception e) {
			e.printStackTrace();
		}
		return msg;
	}

	private static Properties getProperties() {
		Properties prop = System.getProperties();
		prop.put("mail.smtp.auth", "true");
		prop.put("mail.smtp.socketFactory.class", "javax.net.ssl.SSLSocketFactory");
		prop.put("mail.smtp.auth", "true");
		return prop;
	}

	private static String readLetter(InputStreamReader letter) {
		int i;
		String text = "";
		try {
			while ((i = letter.read()) != -1) {
				text += (char) i;
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
		return text;
	}

	private static MimeBodyPart addFile(String fileName) throws MessagingException {
		MimeBodyPart p2 = new MimeBodyPart();
		FileDataSource fds = new FileDataSource("attachment/" + fileName);
		p2.setDataHandler(new DataHandler(fds));
		p2.setFileName(fds.getName());
		return p2;
	}

	private static String read(InputStream stream) {
		int i;
		String text = "";
		try {
			while ((i = stream.read()) != -1) {
				text += (char) i;
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
		return text;
	}

	private static String getLetter() {
		if (EMAIL_TEXT.size() > currentLetterIndex) {
			return EMAIL_TEXT.get(currentLetterIndex++);
		}
		currentLetterIndex = 0;
		return EMAIL_TEXT.get(currentLetterIndex++);
	}

	private static String getSubject() {
		if (EMAIL_SUBJECT.size() > currentSubjectIndex) {
			return EMAIL_SUBJECT.get(currentSubjectIndex++);
		}
		currentSubjectIndex = 0;
		return EMAIL_SUBJECT.get(currentSubjectIndex++);
	}

	private static List<String> getAddresses(String address) {
		
		
		address = address.replaceAll("\\s", ",");
		String[] k = address.split(",");
		return Stream.of(k).filter(m -> m.contains("@")).collect(Collectors.toList());
		
//		String[] addresses = address.split(",");
//		return Stream.of(addresses).collect(Collectors.toList());
	}

}
