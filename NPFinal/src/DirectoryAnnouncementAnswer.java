/*License: GNU General Public License version 2*/


public class DirectoryAnnouncementAnswer extends Encoder{
	
	public String flag;

	public DirectoryAnnouncementAnswer(String flag) {
		this.flag = flag;
	}
	
	public Encoder encoder() {
		Encoder my_seq = (new Encoder()).initSequence()
			.addToSequence(new Encoder(flag));
		return my_seq;
	}
}
