import javax.swing.JFrame;


public class Client {
	public static void main(String [] args){
		ClientFrame application=new ClientFrame();
		application.setVisible(true);
		application.setSize(600,337);
		application.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		application.runClient();
	}
}
