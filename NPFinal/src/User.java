import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Scanner;

/*License: GNU General Public License version 2*/
public class User extends Thread {

	public static String dbname = "";
	public static int port = 0;
	Connection conn = null;
	public static boolean finddata = false;

	User(String[] args) {
		dbname = args[3];
		port = Integer.parseInt(args[1]);
	}

	public synchronized void run() {
		while (true) {
			System.out
					.println("User thread is starting. please input the query from keyboard.");
			Scanner scanner = new Scanner(System.in);
			String query = scanner.nextLine();
			if (query.equals("quit"))
				break;
			if(query.startsWith("select")) {
				try {
					ResultSet rs = askDB(query);
					if (query.contains("peer_address")) {
						System.out.println("peer_address:");
						while (rs.next()) {
							finddata = true;
							System.out.println("peer_address_id:"
									+ rs.getString("peer_address_id"));
							System.out
									.println("peer_id:" + rs.getString("peer_id"));
							System.out
									.println("address:" + rs.getString("address"));
							System.out.println("type:" + rs.getString("type"));
						}
					} else {
						while (rs.next()) {
							System.out
									.println("peer_id:" + rs.getString("peer_id"));
							System.out.println("global_peer_id:"
									+ rs.getString("global_peer_id"));
							System.out.println("name:" + rs.getString("name"));
							System.out.println("broadcastable:"
									+ rs.getString("broadcastable"));
							System.out.println("slogan:" + rs.getString("slogan"));
							System.out.println("last_sync_date:"
									+ rs.getString("last_sync_date"));
							System.out.println("arrival_date:"
									+ rs.getString("arrival_date"));
						}
					}
					if (!finddata) {
						System.out
								.println("database does not have any data for this query.");
					} else
						finddata = false;
					conn.close();
					rs.close();
				} catch (ClassNotFoundException e) {
					e.printStackTrace();
				} catch (SQLException e) {
					e.printStackTrace();
					continue;
				}
			} else {
				try {
					int result = updateDB(query);
					if(result > 0)
						System.out.println("update or insert success");
					else 
						System.out.println("update or insert failed");
				} catch (Exception e) {
					e.printStackTrace();
					continue;
				}
				
			}
			
		}

	}

	public synchronized ResultSet askDB(String query)
			throws ClassNotFoundException, SQLException {
		Class.forName("org.sqlite.JDBC");
		conn = DriverManager.getConnection("jdbc:sqlite:" + dbname, // jdbc:oracle:thin:@hex:1521:ORCL
				"", "");
		conn.setAutoCommit(true);
		Statement stmt = conn.createStatement();
		ResultSet rs = stmt.executeQuery(query);
		return rs;
	}
	
	public synchronized int updateDB(String query)
		throws ClassNotFoundException, SQLException {
		Class.forName("org.sqlite.JDBC");
		conn = DriverManager.getConnection("jdbc:sqlite:" + dbname, // jdbc:oracle:thin:@hex:1521:ORCL
				"", "");
		conn.setAutoCommit(false);
		Statement stmt = conn.createStatement();
		int result = stmt.executeUpdate(query);
		conn.commit();
		return result;
	}
}
