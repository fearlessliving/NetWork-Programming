/*License: GNU General Public License version 2*/
import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.util.Iterator;

public class TCPServerSelector extends Thread{
	private static final int BUFSIZE = 256; // Buffer size (bytes)
	private static final int TIMEOUT = 3000; // Wait timeout (milliseconds)
	public static String dbname = "";
	public static int port = 0;
	TCPServerSelector (String []args) {
		dbname = args[3];
		port = Integer.parseInt(args[1]);
	}
	public synchronized void run(){
		try {
//			args[0] = "5588";
//			if (args.length < 1)
//				throw new IllegalArgumentException("Parameter(s): <Port> ...");
			Selector selector = Selector.open();
//			for (String arg : args) {
				ServerSocketChannel listnChannel = ServerSocketChannel.open();
				listnChannel.socket().bind(
						new InetSocketAddress(port));
				listnChannel.configureBlocking(false); // must be nonblocking to
														// register
				listnChannel.register(selector, SelectionKey.OP_ACCEPT);
//			}
			TCPProtocol protocol = new EchoSelectorProtocol(BUFSIZE,dbname);
			while (true) {
				if (selector.select(TIMEOUT) == 0) {
					continue;
				}
				Iterator<SelectionKey> keyIter = selector.selectedKeys().iterator();

				while (keyIter.hasNext()) {
					SelectionKey key = keyIter.next();
					if (key.isAcceptable()) {
						protocol.handleAccept(key);
					}
					if (key.isReadable()) {
						protocol.handleRead(key);
					}
					if (key.isValid() && key.isWritable()) {
						protocol.handleWrite(key);
					}
					keyIter.remove();
				}
			}
		} catch (Exception  e) {
			e.printStackTrace();
		}

	}
}
