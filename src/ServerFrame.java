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
		NOPOLabel = new JLabel("線上人數:", SwingConstants.RIGHT);
		topPanel.add(NOPOLabel);
		NOPOField = new JTextField("0");
		NOPOField.setEditable(false);
		topPanel.add(NOPOField);
		broadcastLabel = new JLabel("廣播:", SwingConstants.RIGHT);
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

		acceptThread = new Thread() { //這個Thread一直接受新的連線
			public void run() {
				while (true) {
					try {
						connection = server.accept();
						connectionList.add(new User(connection));  //connectionList會把所有連到Server的User加進去，新建自創的User class
					} catch (IOException e) {
						e.printStackTrace();
					}
				}
			}
		};

	}

	public void runServer() {
		try {
			server = new ServerSocket(12345);  //建立ServerSocket(port)
			textArea.append("<System>聊天室已經上線，等待來自port:12345的連線...\n");
			acceptThread.start();  //啟動接受新的連線的Thread

		} catch (IOException ioException) {
			ioException.printStackTrace();
		}
	}	
	public void displayMessage(final String messageToDisplay){  //更新文字區訊息的方法
		SwingUtilities.invokeLater(new Runnable() {
			public void run() {
				textArea.append(messageToDisplay);
			}
		});		
	}
	public void broadcast(String messageToBroadcast){  //廣播方法，對每個線上使用者發送訊息
		userIte=userList.iterator();
		while(userIte.hasNext()){
			userIte.next().sendData(messageToBroadcast);
		}
	}
	public void updateNOPO(){
		String s=String.format("%d",numberOfPeopleOnline);  //更新人數，對每個線上使用者發送資料
		NOPOField.setText(s);
		userIte=userList.iterator();
		while(userIte.hasNext()){
			userIte.next().sendData("UPDATE NOPO:"+s);
		}
	}

	private class User extends Thread {  //User類別，是一個Thread
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
			this.start();  //Thread開始執行
		}

		public void run() {
			String message = "";
			while (true) {  
				try {
					message = (String) input.readObject();  //接收字串
					if (message.contains("NAME REQUEST:")) {  //如果這字串包含NAME REQUEST: 就是要取暱稱的時候
						String[] str = message.split(":");
						if (isNameExist(str[1])) {  //判斷暱稱是否已存在
							sendData("NAME EXISTING");  //存在的話回傳NAME EXISTING
						} else {
							sendData("CREATE SUCCESSFULLY");  //不存在的話就創立使用者，給予ID並加到使用者名單UserList裡
							name=str[1];
							ID = IDcounter;
							IDcounter++;
							userList.add(this);
							numberOfPeopleOnline++;
							
							userIte=userList.iterator();  //傳給每個線上使用者上線訊息
							while(userIte.hasNext()){
								userIte.next().sendData("<Server>Client Name : \" "+getUserName()+" \" 進入聊天室\n");
							}
							displayMessage("<Server>client : "+connection.getInetAddress()+" 連線建立，ID : "+getID()+"\n");  //更新文字區訊息
							currentList.setListData(userList  //更新JList名單
									.toArray(new User[0]));
							updateNOPO();  //呼叫更新人數方法
						}
					} else {  //接收到的字串不是要取名字則是對話訊息
						displayMessage(message);  //更新文字區
						
						userIte=userList.iterator();  //像每個使用者發送訊息使客戶端顯示訊息
						while(userIte.hasNext()){
							userIte.next().sendData(message);
						}
					}
				} catch (ClassNotFoundException e) {
					e.printStackTrace();
				} catch (IOException e) {  //使用者關閉視窗斷線就會到這邊
					displayMessage("<Server>client ID : "+getID()+" 已停止連線\n");
					userList.remove(this);  //從使用者名單刪除
					currentList.setListData(userList  //更新JList名單
							.toArray(new User[0]));
					userIte=userList.iterator();  //像所有使用者傳送離開訊息
					while(userIte.hasNext()){
						userIte.next().sendData("<Server>Client Name : \" "+getUserName()+" \" 已經離開聊天室\n");
					}
					numberOfPeopleOnline--;
					updateNOPO();
					break;  //離開這個while，Thread結束
				}
			}
		}

		public boolean isNameExist(String nameToTry) {  //判斷暱稱重複方法

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

		public void sendData(String message) {  //傳送資料方法
			try {
				output.writeObject(message);
				output.flush();
			} catch (IOException ioException) {
				ioException.printStackTrace();
			}
		}

		public String toString() {  //讓JList上可以顯示暱稱+(ID)
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
