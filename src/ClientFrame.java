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
		nameLabel = new JLabel("�ڪ��ʺ�:");
		nameLabel.setVisible(false);
		topPanel.add(nameLabel);
		myNameField = new JTextField();
		myNameField.setEditable(false);
		myNameField.setVisible(false);
		topPanel.add(myNameField);
		NOPOLabel = new JLabel("�u�W�H��:");
		NOPOLabel.setVisible(false);
		topPanel.add(NOPOLabel);
		NOPOField = new JTextField();
		NOPOField.setEditable(false);
		NOPOField.setVisible(false);
		topPanel.add(NOPOField);
		inputNameLabel = new JLabel("�п�J�z���ʺ�:");
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
		textArea.append("<System>�s�u��server:\"127.0.0.1:12345\"...\n");

		botPanel = new JPanel();
		botPanel.setLayout(new GridLayout(1, 2));
		inputMessageLabel = new JLabel("�ǰe�T��:");
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
			connection = new Socket(InetAddress.getByName("127.0.0.1"), 12345);  //�s�u��ip"127.0.0.1"�A�N�O����
			textArea.append("<Server>�z�w���\�s�u��Server:\"127.0.0.1:12345\"\n");
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
				if (message.contains("NAME EXISTING")) {  //�����r��]�ANAME EXISTING�N�O�ʺ٤w�g���H�ΤF
					sameNameLabel.setText("���ʺ٤w���H�ϥ�");
				} else if (message.contains("CREATE SUCCESSFULLY")) {  //���\�Х߼ʺ١A��쥻���ê����ҿ�J�����
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
				}else if(message.contains("UPDATE NOPO")){  //�����r��]�AUPDATE NOPO�N�N��n��s�u�W�H��
					String [] str=message.split(":");
					NOPOField.setText(str[1]);
				}else {  //�����O�N�O�����T��
					displayMessage(message);
				}
			} catch (ClassNotFoundException classNotFoundException) {

			} catch (IOException ioException) {  //Server�������_�u�N�|��o�A���}�{��
				JOptionPane.showMessageDialog(this,"Connection to server has ended","Chat Room Closed",JOptionPane.WARNING_MESSAGE);
				System.exit(1);
			}
		}

	}

	private class TextFieldHandler implements ActionListener {

		public void actionPerformed(ActionEvent event) {
			if (event.getSource().equals(inputNameField)) {  //��J�ʺ����
				sendData("NAME REQUEST:" + event.getActionCommand());
				name = event.getActionCommand();
			} else if (event.getSource().equals(inputMessageField)) {  //��J�T�����
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
