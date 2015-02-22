/*License: GNU General Public License version 2*/
import java.util.TimerTask;


public class TimerOut extends TimerTask {
	Thread threadToTimeOut;

	public TimerOut(Thread threadToTimeOut) {
		this.threadToTimeOut = threadToTimeOut;
	}

	public void run() {
		System.out.println("TimerOut running...");
		threadToTimeOut.interrupt();
	}
}
