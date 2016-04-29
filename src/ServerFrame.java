import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Iterator;
import java.util.LinkedList;

import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;
import javax.swing.border.LineBorder;

public class ServerFrame extends JFrame {
	private JPanel topPanel;
	private JLabel NOPOLabel;
	private JLabel broadcastLabel;
	private JTextField NOPOField;
	private JTextField broadcastField;
	private JTextArea textArea;
	private JList currentList;
	private JScrollPane textScrollPane;
	private JScrollPane listScrollPane;
	private ServerSocket server;
	private Thread acceptThread;
	private int IDcounter = 0;
	private LinkedList<User> connectionList;
	private LinkedList<User> userList;
	private Iterator<User> userIte;
	private int numberOfPeopleOnline=0;
	private Socket connection;

	public ServerFrame() {
		super("Chat Room Server");

		topPanel = new JPanel();
		topPanel.setLayout(new GridLayout(1, 4));
		NOPOLabel = new JLabel("�u�W�H��:", SwingConstants.RIGHT);
		topPanel.add(NOPOLabel);
		NOPOField = new JTextField("0");
		NOPOField.setEditable(false);
		topPanel.add(NOPOField);
		broadcastLabel = new JLabel("�s��:", SwingConstants.RIGHT);
		topPanel.add(broadcastLabel);
		broadcastField = new JTextField();
		topPanel.add(broadcastField);
		add(topPanel, BorderLayout.NORTH);

		textArea = new JTextArea();
		textArea.setEditable(false);
		textScrollPane = new JScrollPane(textArea);
		add(textScrollPane, BorderLayout.CENTER);

		currentList = new JList();
		listScrollPane = new JScrollPane(currentList);
		add(listScrollPane, BorderLayout.EAST);

		connectionList = new LinkedList<User>();
		userList=new LinkedList<User>();
		
		broadcastField.addActionListener(new textFieldHandler());

		acceptThread = new Thread() { //�o��Thread�@�������s���s�u
			public void run() {
				while (true) {
					try {
						connection = server.accept();
						connectionList.add(new User(connection));  //connectionList�|��Ҧ��s��Server��User�[�i�h�A�s�ئ۳Ъ�User class
					} catch (IOException e) {
						e.printStackTrace();
					}
				}
			}
		};

	}

	public void runServer() {
		try {
			server = new ServerSocket(12345);  //�إ�ServerSocket(port)
			textArea.append("<System>��ѫǤw�g�W�u�A���ݨӦ�port:12345���s�u...\n");
			acceptThread.start();  //�Ұʱ����s���s�u��Thread

		} catch (IOException ioException) {
			ioException.printStackTrace();
		}
	}	
	public void displayMessage(final String messageToDisplay){  //��s��r�ϰT������k
		SwingUtilities.invokeLater(new Runnable() {
			public void run() {
				textArea.append(messageToDisplay);
			}
		});		
	}
	public void broadcast(String messageToBroadcast){  //�s����k�A��C�ӽu�W�ϥΪ̵o�e�T��
		userIte=userList.iterator();
		while(userIte.hasNext()){
			userIte.next().sendData(messageToBroadcast);
		}
	}
	public void updateNOPO(){
		String s=String.format("%d",numberOfPeopleOnline);  //��s�H�ơA��C�ӽu�W�ϥΪ̵o�e���
		NOPOField.setText(s);
		userIte=userList.iterator();
		while(userIte.hasNext()){
			userIte.next().sendData("UPDATE NOPO:"+s);
		}
	}

	private class User extends Thread {  //User���O�A�O�@��Thread
		private Socket connection;
		private String name;
		private int ID;
		private ObjectInputStream input;
		private ObjectOutputStream output;

		public User(Socket s) {
			connection = s;
			try {
				output = new ObjectOutputStream(connection.getOutputStream());
				output.flush();
				input = new ObjectInputStream(connection.getInputStream());
			} catch (IOException ioException) {
				ioException.printStackTrace();
				System.exit(1);
			}
			this.start();  //Thread�}�l����
		}

		public void run() {
			String message = "";
			while (true) {  
				try {
					message = (String) input.readObject();  //�����r��
					if (message.contains("NAME REQUEST:")) {  //�p�G�o�r��]�tNAME REQUEST: �N�O�n���ʺ٪��ɭ�
						String[] str = message.split(":");
						if (isNameExist(str[1])) {  //�P�_�ʺ٬O�_�w�s�b
							sendData("NAME EXISTING");  //�s�b���ܦ^��NAME EXISTING
						} else {
							sendData("CREATE SUCCESSFULLY");  //���s�b���ܴN�ХߨϥΪ̡A����ID�å[��ϥΪ̦W��UserList��
							name=str[1];
							ID = IDcounter;
							IDcounter++;
							userList.add(this);
							numberOfPeopleOnline++;
							
							userIte=userList.iterator();  //�ǵ��C�ӽu�W�ϥΪ̤W�u�T��
							while(userIte.hasNext()){
								userIte.next().sendData("<Server>Client Name : \" "+getUserName()+" \" �i�J��ѫ�\n");
							}
							displayMessage("<Server>client : "+connection.getInetAddress()+" �s�u�إߡAID : "+getID()+"\n");  //��s��r�ϰT��
							currentList.setListData(userList  //��sJList�W��
									.toArray(new User[0]));
							updateNOPO();  //�I�s��s�H�Ƥ�k
						}
					} else {  //�����쪺�r�ꤣ�O�n���W�r�h�O��ܰT��
						displayMessage(message);  //��s��r��
						
						userIte=userList.iterator();  //���C�ӨϥΪ̵o�e�T���ϫȤ����ܰT��
						while(userIte.hasNext()){
							userIte.next().sendData(message);
						}
					}
				} catch (ClassNotFoundException e) {
					e.printStackTrace();
				} catch (IOException e) {  //�ϥΪ����������_�u�N�|��o��
					displayMessage("<Server>client ID : "+getID()+" �w����s�u\n");
					userList.remove(this);  //�q�ϥΪ̦W��R��
					currentList.setListData(userList  //��sJList�W��
							.toArray(new User[0]));
					userIte=userList.iterator();  //���Ҧ��ϥΪ̶ǰe���}�T��
					while(userIte.hasNext()){
						userIte.next().sendData("<Server>Client Name : \" "+getUserName()+" \" �w�g���}��ѫ�\n");
					}
					numberOfPeopleOnline--;
					updateNOPO();
					break;  //���}�o��while�AThread����
				}
			}
		}

		public boolean isNameExist(String nameToTry) {  //�P�_�ʺ٭��Ƥ�k

				userIte = userList.iterator();
				while (userIte.hasNext()) {
					if (userIte.next().getUserName().contains(nameToTry)) {
						return true;
					}
				}
			
			return false;
		}

		public String getUserName() {
			return name;
		}

		public int getID() {
			return ID;
		}

		public void sendData(String message) {  //�ǰe��Ƥ�k
			try {
				output.writeObject(message);
				output.flush();
			} catch (IOException ioException) {
				ioException.printStackTrace();
			}
		}

		public String toString() {  //��JList�W�i�H��ܼʺ�+(ID)
			return getUserName() + "(ID:" + getID() + ")";
		}
	}
	
	private class textFieldHandler implements ActionListener{

		public void actionPerformed(ActionEvent event) {
			if(event.getSource().equals(broadcastField)){
				displayMessage("***Server brocast*** : "+event.getActionCommand()+"\n");
				broadcast("***Server brocast*** : "+event.getActionCommand()+"\n");
				broadcastField.setText("");
			}
		}
		
	}

}
