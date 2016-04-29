import java.awt.BorderLayout;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.InetAddress;
import java.net.Socket;

import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.SwingUtilities;

public class ClientFrame extends JFrame {
	private JLabel nameLabel;
	private JLabel NOPOLabel;
	private JLabel inputNameLabel;
	private JLabel sameNameLabel;
	private JLabel inputMessageLabel;
	private JTextField myNameField;
	private JTextField NOPOField;
	private JTextField inputNameField;
	private JTextField inputMessageField;
	private JPanel topPanel;
	private JPanel botPanel;
	private JTextArea textArea;
	private JScrollPane scrollPane;
	private ObjectInputStream input;
	private ObjectOutputStream output;
	private Socket connection;
	private String name;

	public ClientFrame() {
		super("Chat Room Client");

		topPanel = new JPanel();
		topPanel.setLayout(new GridLayout(2, 4));
		nameLabel = new JLabel("我的暱稱:");
		nameLabel.setVisible(false);
		topPanel.add(nameLabel);
		myNameField = new JTextField();
		myNameField.setEditable(false);
		myNameField.setVisible(false);
		topPanel.add(myNameField);
		NOPOLabel = new JLabel("線上人數:");
		NOPOLabel.setVisible(false);
		topPanel.add(NOPOLabel);
		NOPOField = new JTextField();
		NOPOField.setEditable(false);
		NOPOField.setVisible(false);
		topPanel.add(NOPOField);
		inputNameLabel = new JLabel("請輸入您的暱稱:");
		topPanel.add(inputNameLabel);
		inputNameField = new JTextField();
		topPanel.add(inputNameField);
		sameNameLabel = new JLabel();
		topPanel.add(sameNameLabel);
		topPanel.add(new JPanel());
		add(topPanel, BorderLayout.NORTH);

		textArea = new JTextArea();
		textArea.setEditable(false);
		scrollPane = new JScrollPane(textArea);
		add(scrollPane, BorderLayout.CENTER);
		textArea.append("<System>連線至server:\"127.0.0.1:12345\"...\n");

		botPanel = new JPanel();
		botPanel.setLayout(new GridLayout(1, 2));
		inputMessageLabel = new JLabel("傳送訊息:");
		inputMessageLabel.setVisible(false);
		botPanel.add(inputMessageLabel);
		inputMessageField = new JTextField();
		inputMessageField.setVisible(false);
		botPanel.add(inputMessageField);
		add(botPanel, BorderLayout.SOUTH);

		inputNameField.addActionListener(new TextFieldHandler());
		inputMessageField.addActionListener(new TextFieldHandler());
	}

	public void runClient() {
		String message = "";
		try {
			connection = new Socket(InetAddress.getByName("127.0.0.1"), 12345);  //連線到ip"127.0.0.1"，就是本機
			textArea.append("<Server>您已成功連線至Server:\"127.0.0.1:12345\"\n");
			output = new ObjectOutputStream(connection.getOutputStream());
			output.flush();
			input = new ObjectInputStream(connection.getInputStream());
		} catch (IOException ioException) {
			JOptionPane.showMessageDialog(this,"Can not connect to Chat Server","Chat Room Closed",JOptionPane.WARNING_MESSAGE);
			System.exit(1);
		}
		while (true) {
			try {
				message = (String) input.readObject();
				if (message.contains("NAME EXISTING")) {  //接收字串包括NAME EXISTING就是暱稱已經有人用了
					sameNameLabel.setText("此暱稱已有人使用");
				} else if (message.contains("CREATE SUCCESSFULLY")) {  //成功創立暱稱，把原本隱藏的標籤輸入欄顯示
					nameLabel.setVisible(true);
					myNameField.setVisible(true);
					myNameField.setText(name);
					NOPOLabel.setVisible(true);
					NOPOField.setVisible(true);
					inputNameLabel.setVisible(false);
					inputNameField.setVisible(false);
					sameNameLabel.setVisible(false);
					inputMessageLabel.setVisible(true);
					inputMessageField.setVisible(true);
				}else if(message.contains("UPDATE NOPO")){  //接收字串包括UPDATE NOPO就代表要更新線上人數
					String [] str=message.split(":");
					NOPOField.setText(str[1]);
				}else {  //都不是就是接收訊息
					displayMessage(message);
				}
			} catch (ClassNotFoundException classNotFoundException) {

			} catch (IOException ioException) {  //Server端關閉斷線就會到這，離開程式
				JOptionPane.showMessageDialog(this,"Connection to server has ended","Chat Room Closed",JOptionPane.WARNING_MESSAGE);
				System.exit(1);
			}
		}

	}

	private class TextFieldHandler implements ActionListener {

		public void actionPerformed(ActionEvent event) {
			if (event.getSource().equals(inputNameField)) {  //輸入暱稱欄位
				sendData("NAME REQUEST:" + event.getActionCommand());
				name = event.getActionCommand();
			} else if (event.getSource().equals(inputMessageField)) {  //輸入訊息欄位
				sendData(name + " : " + event.getActionCommand() + "\n");
				inputMessageField.setText("");
			}
		}
	}

	public void sendData(String message) {
		try {
			output.writeObject(message);
			output.flush();
		} catch (IOException ioException) {
			ioException.printStackTrace();
		}
	}

	public void displayMessage(final String messageToDisplay) {
		SwingUtilities.invokeLater(new Runnable() {
			public void run() {
				textArea.append(messageToDisplay);
			}
		});
	}
}
