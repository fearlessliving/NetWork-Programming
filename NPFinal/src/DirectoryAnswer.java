/*License: GNU General Public License version 2*/

public class DirectoryAnswer {
	
	public DirectoryAnswer(String address) {
		this.address = address;
	}
	
	public Encoder encoder() {
		Encoder my_seq = (new Encoder()).initSequence()
			.addToSequence(new Encoder(address));
		return my_seq;
	}

	public String address;
	
	
}
