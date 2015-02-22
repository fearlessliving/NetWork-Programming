/*License: GNU General Public License version 2*/
public class DirectoryRequest {
	
	public DirectoryRequest(String globalID) {
		this.globalID = globalID;
	}
	
	public Encoder encoder() {
		Encoder my_seq = (new Encoder()).initSequence()
			.addToSequence(new Encoder("search"))
			.addToSequence(new Encoder(globalID));
		return my_seq;
	}

	public String globalID;
	
}
