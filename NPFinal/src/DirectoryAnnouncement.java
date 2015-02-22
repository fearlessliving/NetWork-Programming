/*License: GNU General Public License version 2*/


public class DirectoryAnnouncement extends Encoder{
	
	public DirectoryAnnouncement(String ip, int port, String global_id) {
		this.ip = ip;
		this.port = port;
		this.global_id = global_id;
	}
	
	public Encoder encoder() {
		Encoder my_seq = (new Encoder()).initSequence()
			.addToSequence(new Encoder("update"))
			.addToSequence(new Encoder(ip))
			.addToSequence(new Encoder(port))
			.addToSequence(new Encoder(global_id))
			; 
		return my_seq;
	}
	
	public String ip;
	public int port;
	public String global_id;
}
