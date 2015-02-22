/*License: GNU General Public License version 2*/
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.Statement;

public class EchoSelectorProtocol implements TCPProtocol {
	private int bufSize; // Size of I/O buffer
	public  static int MAX_CONNECTION = 10;
	public static int CURRENT_CONNECTION = 0;
	public static String dbname = "";
	public EchoSelectorProtocol(int bufSize,String db) {
		this.bufSize = bufSize;
		dbname = db;
	}

	public void handleAccept(SelectionKey key) throws IOException {
		if(CURRENT_CONNECTION <  10) {
			SocketChannel clntChan = ((ServerSocketChannel) key.channel()).accept();
			clntChan.configureBlocking(false); // Must be nonblocking to register
			clntChan.register(key.selector(), SelectionKey.OP_READ, ByteBuffer
					.allocate(bufSize));
			CURRENT_CONNECTION++;
		} else {
			System.out.println("over max connection!");
		}
		
	}

	public void handleRead(SelectionKey key) throws IOException {// Client
		// socket
		// channel
		// has
		// pending
		// data
		SocketChannel clntChan = (SocketChannel) key.channel();
		ByteBuffer buf = (ByteBuffer) key.attachment();
		buf.clear();
		long bytesRead = clntChan.read(buf);
		if (bytesRead == -1) {
			clntChan.close();
		} else if (bytesRead > 0) {
			buf.flip();
			Decoder dec = new Decoder(buf.array(), 0);
			dec = dec.getContent();
			if (dec.getFirstObject(true).getString().equals("get data from server thread")) {
				String newtargetIP = dec.getFirstObject(true).getString();
				renewLocalDBForNewTargetIP(newtargetIP);
				Encoder eq = getResult("peer");
				Encoder l1 = new Encoder("peer");
//				Encoder my_seq = (new Encoder()).initSequence().addToSequence(
//						l1).addToSequence(eq);

				Encoder eq2 = getResult("peer_address");

				Encoder l2 = new Encoder("peer_address");
				Encoder my_seq = (new Encoder()).initSequence().addToSequence(l1)
						.addToSequence(eq).addToSequence(l2).addToSequence(eq2);

//				ByteBuffer writeBuf2 = ByteBuffer.wrap(my_seq.getBytes());
//				clntChan.write(writeBuf2);
//				if (!writeBuf2.hasRemaining()) { // Buffer completely
//													// written?
//					key.interestOps(SelectionKey.OP_READ);
//				}
				
				ByteBuffer writeBuf = ByteBuffer.wrap(my_seq.getBytes());
				clntChan.write(writeBuf);
				if (!writeBuf.hasRemaining()) { // Buffer completely written?
					key.interestOps(SelectionKey.OP_READ);
				}
				writeBuf.clear();
				clntChan.close();
				CURRENT_CONNECTION--;
				// writeBuf2.clear();
			} 
			// key.interestOps(SelectionKey.OP_READ | SelectionKey.OP_WRITE);
		}
		// byte receive[] = new byte[1024];
		// buf.get(receive);
		// Decoder dec = new Decoder(receive,0);
		// dec=dec.getContent();
		// System.out.println(dec.getString());
		// long bytesRead = clntChan.read(buf);
		// if (bytesRead == -1) {
		// clntChan.close();
		// } // Did the other end close?
		// else if (bytesRead > 0) {
		// key.interestOps(SelectionKey.OP_READ | SelectionKey.OP_WRITE);
		// }
	}

	/*
	 * * Channel is available for writing, and key is valid (i.e., client
	 * channel * not closed).
	 */
	
	public void renewLocalDBForNewTargetIP(String newIP) {
		String ip = newIP.split(" ")[0];
		String global = newIP.split(" ")[1];
		StringBuffer sbuff = new StringBuffer("");
		Connection conn = null;
		Statement stmt = null;
		try {
			Class.forName("org.sqlite.JDBC");
			conn = DriverManager.getConnection("jdbc:sqlite:" + dbname, // jdbc:oracle:thin:@hex:1521:ORCL
					"", "");
			conn.setAutoCommit(false);
			stmt = conn.createStatement();
			String searchSql = "select * from peer_address where peer_ID = '"
				+ global + "';";
			ResultSet rs = stmt.executeQuery(searchSql);
				if (rs.next()) {
	
					String sql = "update peer_address set peer_address = '" + ip + "' where global_peer_ID = '" + global
							+ "';";
					stmt.executeUpdate(sql);
				} else {
					String sql = "INSERT INTO peer_address (peer_address, type, peer_ID) VALUES ('"
							+ ip + "','','" + global + "');";
					stmt.executeUpdate(sql);
				}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
		
		
	public void handleWrite(SelectionKey key) throws IOException {
		ByteBuffer buf = (ByteBuffer) key.attachment(); // Retrieve data read
		// earlier
		buf.flip(); // Prepare buffer for writing
		SocketChannel clntChan = (SocketChannel) key.channel();
		clntChan.write(buf);
		if (!buf.hasRemaining()) { // Buffer completely written?
			key.interestOps(SelectionKey.OP_READ);
		}
		buf.compact();
	}

	public  Encoder getResult(String tablename) {
		StringBuffer sbuff = new StringBuffer("");
		Connection conn = null;
		Statement stmt = null;
		try {
			Class.forName("org.sqlite.JDBC");
			conn = DriverManager.getConnection("jdbc:sqlite:" + dbname, // jdbc:oracle:thin:@hex:1521:ORCL
					"", "");
			conn.setAutoCommit(false);
			stmt = conn.createStatement();
			if (tablename.equals("peer")) {
				Encoder content = new Encoder().initSequence();
				ResultSet rs = stmt
						.executeQuery("SELECT global_peer_ID, name,slogan, broadcastable, last_sync_date,arrival_date FROM peer  WHERE broadcastable != 0;");
				sbuff.append("peers table result: \r\n");
				for (int i = 0; i < rs.getRow(); i++) {
					Encoder seq = null;
					if (rs.next()) {
						Encoder en_global_peer_ID = new Encoder(rs
								.getString("global_peer_ID"));
						Encoder en_name = new Encoder(rs.getString("name"));
						Encoder en_broadcastable = new Encoder(rs
								.getString("broadcastable"));
						Encoder en_slogan = new Encoder(rs.getString("slogan"));
						Encoder last_sync_date = new Encoder(rs
								.getString("last_sync_date"));
						Encoder en_date = new Encoder(rs
								.getString("arrival_date"));
						seq = (new Encoder()).initSequence().addToSequence(
								en_global_peer_ID).addToSequence(en_name)
								.addToSequence(en_broadcastable).addToSequence(
										last_sync_date).addToSequence(en_date)
								.addToSequence(en_slogan);
					}
					if (seq != null)
						content.addToSequence(seq);
				}
				conn.close();
				return content;
			} else {
				Encoder content = new Encoder().initSequence();
				ResultSet rs1 = stmt
						.executeQuery("SELECT address, type, global_peer_ID FROM peer JOIN peer_address ON peer.global_peer_ID=peer_address.peer_ID WHERE broadcastable != 0;");
				sbuff.append("peers_addresses table result: \r\n");
				for (int i = 0; i < rs1.getRow(); i++) {
					Encoder seq = null;
					if (rs1.next()) {
						Encoder address = new Encoder(rs1.getString("address"));
						Encoder type = new Encoder(rs1.getString("type"));
						Encoder global_peer_ID = new Encoder(rs1
								.getInt("global_peer_ID"));
						seq = (new Encoder()).initSequence().addToSequence(
								address).addToSequence(type).addToSequence(
								global_peer_ID);
					}
					if (seq != null)
						content.addToSequence(seq);
				}
				conn.close();
				return content;
			}

		} catch (Exception e) {
			e.printStackTrace();
		}
		return null;
	}
}
