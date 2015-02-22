/*License: GNU General Public License version 2*/
import java.net.UnknownHostException;



public class MainClass {

	/**
	 * @param args
	 * @throws InterruptedException 
	 * @throws UnknownHostException 
	 */
	public static void main(String[] args) throws InterruptedException, UnknownHostException {
		// TODO Auto-generated method stub
//		args = new String [11];
//		args[0] = "-p";
//		args[1] = "5588";
//		args[2] = "-d";
//		args[3] = "deliberation.db";
//		args[4] = "-u";
//		args[5] = "-b";
//		args[6] = "-r";
//		args[7] = "127.0.0.1:5599";
//		args[8] = "-D";
//		args[9] = "-g";
//		args[10] = "55";
		TCPServerSelector ts = new TCPServerSelector(args);
		ts.start();
		Client cl = new Client(args);
		cl.start();
		User us = new User(args);
		us.start();
	}

}
