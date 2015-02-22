/*License: GNU General Public License version 2*/
import java.io.InputStream;
import java.io.OutputStream;
import java.math.BigInteger;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.SocketAddress;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.nio.ByteBuffer;
import java.nio.channels.SocketChannel;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;

public class Client extends Thread {
	Connection conn = null;
	String server = "";
	static boolean updateDB = false;
	public static String dbname = "";
	public static int port = 0;
	private String directoryAddress;
	private String globalID;
	private static Boolean syncReqOpt = false;
	private static Boolean syncReqBro = true;
	private String directoryIP;
	private String directoryPort;
	private String localIP;

	Client(String[] args) throws UnknownHostException {
		dbname = args[3];
		port = Integer.parseInt(args[1]);
		directoryAddress = args[7];
		if(args[8].equals("-D")) {
			globalID = args[10];
			syncReqOpt = true;
		} else {
			globalID = args[9];
		}
		localIP = InetAddress.getLocalHost().getHostAddress().toString();
		if (!directoryAddress.equals(null)) {
			int j = directoryAddress.indexOf(":");
			this.directoryIP = directoryAddress.substring(0, j);
			this.directoryPort = directoryAddress.substring(j + 1, j + 5);
		}

	}

	public synchronized void run() {
		try {
			if (conn == null)
				connectionDB();
			if(syncReqOpt)
				updateNewAddressToDirectoryServer();
			ArrayList<String> list = getAddressListFromDB();
			while (true) {
				for (int i = 0; i < list.size(); i++) {

					SocketChannel cIntChan2 = SocketChannel.open();

					cIntChan2.configureBlocking(true);
					String[] idandport = list.get(i).split(" ")[0].split(":");
					int timeout = 2000;
					Socket st = cIntChan2.socket();
					try {
						System.out.println("== start to connect peer_ID : " + list.get(i).split(" ")[1] + " with IP: " + idandport[0] + ":" + idandport[1] + " ==");
						st.connect(new InetSocketAddress(idandport[0], Integer
								.parseInt(idandport[1])), timeout);
						
						if (cIntChan2.isConnected()) {
							System.out.println("connected success");
							Encoder my_str2 = new Encoder(
									"get data from server thread");
							Encoder my_seq2 = (new Encoder()).initSequence()
									.addToSequence(my_str2).addToSequence(new Encoder(localIP + " " + globalID));
							ByteBuffer writeBuf2 = ByteBuffer.wrap(my_seq2
									.getBytes());
							if (writeBuf2.hasRemaining()) {
								cIntChan2.write(writeBuf2);
							}
							int bytesRcvd = 0;
							ByteBuffer readBuf2 = ByteBuffer.allocate(1024);
							if ((bytesRcvd = cIntChan2.read(readBuf2)) == -1) {
								throw new SocketException(
										"Connection closed prematurely");
							}
							Decoder dec2 = new Decoder(readBuf2.array(), 0);
							// Decoder data = dec.getContent();
							if (!dec2.toString().startsWith(" 0")) {
								if (conn == null)
									connectionDB();
								System.out.println("update success");
								insertToDB(dec2);
							}
							writeBuf2.clear();
							readBuf2.clear();
							cIntChan2.close();
						} 
					} catch (Exception e) {
						System.out.println("connected failed, start to connect discovery server.");
						DirectoryRequest request = new DirectoryRequest(list.get(i).split(" ")[1]);
						Socket socket = new Socket();
						SocketAddress remoteAddr = new InetSocketAddress(directoryIP,Integer.parseInt(directoryPort)); 
						socket.connect(remoteAddr);
						OutputStream output = socket.getOutputStream();
						InputStream input = socket.getInputStream();
						output.write(request.encoder().getBytes());
						output.flush();
						byte response[] = new byte[1024];
						int readNr = input.read(response);
						byte receive[] = new byte[readNr];
						for (int p = 0; p < readNr; p++) {
							receive[p] = response[p];
						}
						
						Decoder dec = new Decoder(receive,0);
						String ip = dec.getFirstObject(true).getString();
						if(ip.contains("failed")) {
							System.out.println("discovery server does not contain this peer_id");
							continue;
						}
						String peer_ID = list.get(i).split(" ")[1];
						list.remove(i);
						list.add(i, ip.substring(ip.indexOf("1"))+ " " + peer_ID);
						socket.close();
						String str = "update peer_address set address = '" + ip.substring(ip.indexOf("1")) + "' where peer_ID = '" + peer_ID + "';";
						PreparedStatement prep = conn.prepareStatement(str);
						conn.setAutoCommit(false);
						prep.executeUpdate();
						conn.commit();
						prep.close();
						continue;
					}

				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}

	}

	public void updateNewAddressToDirectoryServer() throws Exception {
		System.out.println("== update discovery server start ==");
		DirectoryAnnouncement announcement = new DirectoryAnnouncement(localIP, port, globalID);
		Socket socket = new Socket();
		SocketAddress remoteAddr = new InetSocketAddress(directoryIP,Integer.parseInt(directoryPort)); 
		socket.connect(remoteAddr);
		OutputStream output = socket.getOutputStream();
		InputStream  input = socket.getInputStream();
        output.write(announcement.encoder().getBytes());
        output.flush();
        
        int readNr;
        byte response[] = new byte[1024];
        readNr = input.read(response);
		byte receive[] = new byte[readNr];
		for (int i = 0; i < readNr; i++) {
			receive[i] = response[i];
		}
		
		
		Decoder dec = new Decoder(receive,0);
		dec=dec.getContent();
		if(dec.getFirstObject(true).getString().equals("success")){
			Statement stat = conn.createStatement();
			ResultSet rs;
			String num = "0";
			String sql2 = "SELECT directory_ID FROM directory;";
			rs = stat.executeQuery(sql2);
			while(rs.next()){
				num = rs.getString("directory_ID");
			}
			String sql1 = "INSERT into directory(domain_IP,port,comments) VALUES ('"+directoryIP+"','"+directoryPort+"','"+globalID+"');";
			stat.executeUpdate(sql1);
			String sql = "INSERT into peer_address(address, type, peer_ID) VALUES ('"+directoryAddress+"'," + "'DIR','"+ (Integer.parseInt(num)+1) +"');";
			stat.executeUpdate(sql);
			
		}
		
		socket.close();
		System.out.println("== update discovery server end ==");
	}

	public void connectionDB() throws SQLException, ClassNotFoundException {
		Class.forName("org.sqlite.JDBC");
		conn = DriverManager.getConnection("jdbc:sqlite:" + dbname, // jdbc:oracle:thin:@hex:1521:ORCL
				"", "");
		// conn.setAutoCommit(true);
	}

	public synchronized void insertToDB(Decoder data) {
		Decoder ded = data.getContent();
		String tablename = ded.getFirstObject(true).getString();
		data = ded.getContent();
		while (data.length > 0 || ded.length > 0) {
			if (data.length == 0 && ded.length > 0) {
				ded.getFirstObject(true).getString();
				tablename = ded.getFirstObject(true).getString();
				data = ded.getContent();
				continue;
			}
			Decoder ded2 = data.getContent();
			if (tablename.equals("peer")) {

				String str = "insert into peer (global_peer_ID,name,slogan,broadcastable,last_sync_date) values(";
				String val = ded2.getFirstObject(true).getString();
				if (checkDB(val)) {
					if (val == null)
						str += "'',";
					else
						str += "'" + val + "',";

					val = ded2.getFirstObject(true).getString();
					if (val == null)
						str += "'',";
					else
						str += "'" + val + "',";
					val = ded2.getFirstObject(true).getString();
					if (val == null)
						str += "'',";
					else
						str += "'" + val + "',";
					val = ded2.getFirstObject(true).getString();
					if (val == null)
						str += "'',";
					else
						str += "'" + val + "',";
					val = ded2.getFirstObject(true).getString();
					if (val == null)
						str += "'')";
					else
						str += "'" + val + "')";
					PreparedStatement prep = null;
					try {
						prep = conn.prepareStatement(str);

						conn.setAutoCommit(false);
						prep.executeUpdate();
						conn.commit();
					} catch (Exception e) {
						e.printStackTrace();
					}

				}
				data.getFirstObject(true);
			} else {
				String str = "insert into peer_address (address, type, peer_ID) values(";
				String val = ded2.getFirstObject(true).getString();

				if (val == null)
					str += "'',";
				else
					str += "'" + val + "',";

				val = ded2.getFirstObject(true).getString();
				if (val == null)
					str += "'',";
				else
					str += "'" + val + "',";
				BigInteger peerid = ded2.getFirstObject(true).getInteger();
				if (checkDB2(peerid)) {
					str += peerid + ")";
					PreparedStatement prep = null;
					try {
						prep = conn.prepareStatement(str);
						prep.executeUpdate();
						conn.setAutoCommit(false);
						conn.commit();
					} catch (Exception e) {
						e.printStackTrace();
					}
				}
				data.getFirstObject(true);
				if (ded.getFirstObject(true) != null)
					;
			}
		}
	}

	boolean checkDB(String global_id) {
		Statement stmt = null;
		try {
			stmt = conn.createStatement();
			String id = "SELECT * FROM peer where global_peer_ID = '";
			id += global_id;
			id += "';";
			ResultSet rs1 = stmt.executeQuery(id);

			if (rs1.next()) {
				rs1.close();
				return false;
			} else {
				rs1.close();
				return true;
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return false;
	}

	boolean checkDB2(BigInteger global_id) {
		Statement stmt = null;
		try {
			stmt = conn.createStatement();
			String id = "SELECT * FROM peer_address WHERE peer_ID = ";
			id += global_id;
			id += ";";
			ResultSet rs1 = stmt.executeQuery(id);
			if (rs1.next()) {
				rs1.close();
				return false;
			}

			else {
				rs1.close();
				return true;
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return false;
	}

	public ArrayList<String> getAddressListFromDB() {
		ArrayList<String> al = new ArrayList<String>();
		Statement stmt = null;
		try {
			stmt = conn.createStatement();
			String id = "SELECT address,peer_ID FROM peer_address where type != 'DIR'";
			ResultSet rs1 = stmt.executeQuery(id);
			while (rs1.next())
				al.add(rs1.getString("address") + " " + rs1.getString("peer_ID"));
			rs1.close();
		} catch (Exception e) {
			e.printStackTrace();
		}
		return al;
	}
}
