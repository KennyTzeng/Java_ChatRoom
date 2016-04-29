import javax.swing.JFrame;


public class Server {
	public static void main(String [] args){
		ServerFrame application=new ServerFrame();
		application.setVisible(true);
		application.setSize(700,500);
		application.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		application.runServer();
	}
}
