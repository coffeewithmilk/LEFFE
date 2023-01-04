package documentation;

import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.PrintWriter;
import java.util.Scanner;

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextArea;
import javax.swing.JTextField;

public class HTMLGenerator extends JFrame {
	
	/**
	 * 
	 */
	private static final long serialVersionUID = -631283326760659819L;

	public static void main(String[] args) {
		new HTMLGenerator();
	}
	
	public static void createHTMLFile(String keyword, String[] args, String descr, String example) {
		String html = "<html>\r\n<head>\r\n<title>\r\n";
		html += keyword;
		html += "\r\n</title>\r\n<link rel=\"stylesheet\" href=\"slidestyle.css\">\r\n</head>\r\n<body>\r\n<div class=\"slide\">\r\n<h2>Kommando</h2>\r\n<div class=\"slidetitle\">\r\n";
		html += keyword;
		html += "\r\n</div>\r\n<h2> Argument </h2>\r\n<br/>\r\n<div class=\"slidearguments\">\r\n";
		for (String s : args) {
			html += "<div class=\"slideargument\">\r\n";
			html += s;
			html += "\r\n</div>\r\n";
		}
		html += "\r\n</div>\r\n<h2>Beskrivning</h2>\r\n<div class=\"slidedescription\">\r\n";
		html += descr;
		html += "\r\n</div>\r\n<h2>Exempel</h2>\r\n<div class=\"slideexample\">\r\n";
		for (String s : example.split("\r\n")) {
			html += s + "<br />";
		}
		html += "\r\n</div>\r\n<h2>Övningsuppgift</h3>\r\n<div class=\"slideexercise\">\r\n\r\n</div>\r\n</div>\r\n</body>\r\n</html>";
		try {
			
			
			
			PrintWriter pw = new PrintWriter(new File(keyword + ".html"));
			pw.println(html);
			pw.flush();
			pw.close();
			
			File f = new File("links.html");
			
			Scanner scanner = new Scanner(f);
			String str = "";
			while (scanner.hasNextLine()) {
				str += scanner.nextLine();
			}
			
			scanner.close();
			
			pw = new PrintWriter(f);
			pw.append(str + "\r\n<a href=\"" + keyword + ".html\">" + keyword + "</a>");
			pw.flush();
			pw.close();
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}
	}
	
	public HTMLGenerator() {
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		JPanel pan = new JPanel();
		JTextField kw = new JTextField();
		JTextField args = new JTextField();
		JTextField descr = new JTextField();
		JTextArea example = new JTextArea();
		pan.setPreferredSize(new Dimension(400, 400));
		kw.setPreferredSize(new Dimension(380, 25));
		args.setPreferredSize(new Dimension(380, 25));
		descr.setPreferredSize(new Dimension(380, 25));
		example.setPreferredSize(new Dimension(380, 100));
		
		JButton create = new JButton("Create file");
		create.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent e) {
				createHTMLFile(kw.getText(), args.getText().split(";"), descr.getText(), example.getText());
				kw.setText("");
				args.setText("");
				descr.setText("");
				example.setText("");
			}
			
		});
		
		pan.add(new JLabel("Keyword:"));
		pan.add(kw);
		pan.add(new JLabel("Args:"));
		pan.add(args);
		pan.add(new JLabel("Description:"));
		pan.add(descr);
		pan.add(new JLabel("Example:"));
		pan.add(example);
		pan.add(create);
		add(pan);
		pack();
		setVisible(true);
	}

}
