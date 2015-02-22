/*License: GNU General Public License version 2*/

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.math.BigInteger;
import java.net.ServerSocket;
import java.net.Socket;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;

public class DirectoryServer {

	/**
	 * @param args
	 */
	
	
	static byte[] send = new byte[1024];
	private static int port;
	private static String database;

	private static ServerSocket serverSocket;
	private static Socket socket;

	// private static BufferedWriter writer;

	public static void main(String[] args) throws IOException,
			ClassNotFoundException, SQLException {
		// TODO Auto-generated method stub

		if (args.length > 0) {
			port = Integer.parseInt(args[1]);
			database = args[3];
		} else {
			port = 5599;
			database = "directory.db";
		}

		serverSocket = new ServerSocket(port, 10);

		for (;;) {

			socket = serverSocket.accept();
			DirectoryServerThread dst = new DirectoryServerThread(socket, database);
			dst.start();
			
		}

	}

	
	

}

class DirectoryServerThread extends Thread{
	
	private static String database;
	private static OutputStream output;
	private static InputStream input;
	static byte[] service = new byte[1024];
	
	private Socket socket;
	
	DirectoryServerThread(Socket s, String database) {
		this.socket = s;
		this.database = database;
	}
	
	public void run() {
		try {
			output = socket.getOutputStream();
			input = socket.getInputStream();
			int readNr;
			readNr = input.read(service);
			byte receive[] = new byte[readNr];
			for (int i = 0; i < readNr; i++) {
				receive[i] = service[i];
			}
			Decoder dec = new Decoder(receive, 0);
			// if (receive.length < JUDGEMENT) {
			// send = QueryService(receive);
			// } else {
			// send = RegistryService(receive);
			// }
			dec = dec.getContent();
			String flag = dec.getFirstObject(true).getString();
			if (flag.equals("update")) {
				output.write(RegistryService(dec));
			} else if (flag.equals("search")) {
				output.write(QueryService(dec));
			}

			output.flush();
		} catch (Exception e) {
			e.printStackTrace();
		}
		
	}
	
	// The peer connect to this Directory Server and announce itself.
	private static synchronized byte[] RegistryService(Decoder dec)
			throws ClassNotFoundException, SQLException, IOException {

		Class.forName("org.sqlite.JDBC");
		// ResultSet rs;
		Connection conn = DriverManager
				.getConnection("jdbc:sqlite:" + database);
		Statement stat = conn.createStatement();

		String ip = dec.getFirstObject(true).getString();
		BigInteger port = dec.getFirstObject(true).getInteger();
		String global_id = dec.getFirstObject(true).getString();
		String searchSql = "select * from registered where global_peer_ID = '"
				+ global_id + "';";
		ResultSet rs = stat.executeQuery(searchSql);
		if (rs.next()) {

			String sql = "update registered set addresses = '" + ip + ":"
					+ port + "' where global_peer_ID = '" + global_id + "';";
			stat.executeUpdate(sql);

			DirectoryAnnouncementAnswer answer = new DirectoryAnnouncementAnswer(
					"success");
			byte send[] = answer.encoder().getBytes();

			stat.close();
			conn.close();
			return send;
		} else {
			String sql = "INSERT INTO registered (global_peer_ID,addresses) VALUES ('"
					+ global_id + "','" + ip + ":" + port + "');";
			stat.executeUpdate(sql);

			DirectoryAnnouncementAnswer answer = new DirectoryAnnouncementAnswer(
					"success");
			byte send[] = answer.encoder().getBytes();

			stat.close();
			conn.close();
			return send;
		}

	}

	// The peer can't connect to the other peer with old address,then ask
	// Directory Server to get new addresses.
	private static synchronized byte[] QueryService(Decoder dec)
			throws ClassNotFoundException, SQLException, IOException {
		Class.forName("org.sqlite.JDBC");
		ResultSet rs;
		Connection conn = DriverManager
				.getConnection("jdbc:sqlite:" + database);
		Statement stat = conn.createStatement();

		String global_id = dec.getFirstObject(true).getString();
		String sql = "SELECT addresses FROM registered WHERE global_peer_ID =="
				+ "'" + global_id + "';";
		rs = stat.executeQuery(sql);
		if (rs.next()) {
			String temp_address = rs.getString("addresses");
			DirectoryAnswer answer = new DirectoryAnswer(temp_address);
			stat.close();
			conn.close();
			return answer.encoder().getBytes();
		}
		stat.close();
		conn.close();
		DirectoryAnswer answer = new DirectoryAnswer("failed");
		return answer.encoder().getBytes();
	}

	// Generate the Generalized Time
	private static String getDate() {

		SimpleDateFormat lFormat;

		Date date = null;

		Calendar MyDate = Calendar.getInstance();

		MyDate.setTime(new java.util.Date());

		date = MyDate.getTime(); //

		lFormat = new SimpleDateFormat("yyyyMMddHHmmss.SSS");

		String gRtnStr = lFormat.format(date) + "Z";

		return gRtnStr;
	}
}
