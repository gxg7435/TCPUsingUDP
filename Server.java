import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Queue;

/**
 * This class is designed to implement the server side of the project. In server
 * side we send the packet with sequence number based on the value of received
 * acknowledgement number.
 * 
 * @author gaurav gaur(gxg7435@g.rit.edu)
 *
 */
public class Server {

	FileHandle fh;
	String compFile;

	DatagramSocket serverSocket;
	private DatagramPacket recievePacket;
	DatagramPacket sendPacket;

	byte[] recieve;
	byte[] send;

	int totalPacketsCreated, port, pack_sent;
	HashMap<Integer, HeaderInfo> eachPacket;

	InetAddress ip;
	Queue<HeaderInfo> pack_queue = new LinkedList<HeaderInfo>();

	/**
	 * This is a constructor for server side that is used to initialize the
	 * variables.
	 * 
	 * @param fh
	 *            : object of filehandle class
	 * @param compFile:
	 *            compressed File
	 */
	public Server(FileHandle fh, String compFile) {
		this.fh = fh;
		this.compFile = compFile;
		eachPacket = new HashMap<Integer, HeaderInfo>();
		try {
			serverSocket = new DatagramSocket(7000);
			recieve = new byte[65507];
			send = new byte[65507];
		} catch (SocketException e) {
			e.printStackTrace();
		}
	}

	/**
	 * This is the main method for the server program
	 * 
	 * @param args[0]
	 *            : filename
	 */
	public static void main(String[] args) {

		FileHandle fh = new FileHandle();
		String compFile = fh.compress(args[0]);

		Server s = new Server(fh, compFile);
		s.setHeaderInfo();

		s.startReciever();

	}

	/**
	 * This method is used to call the reciever side logic in the server
	 */
	private void startReciever() {
		ServerRecieveSide srs = new ServerRecieveSide();
		srs.start();

	}

	/**
	 * This method is used to set header fields in the packet.
	 */
	private void setHeaderInfo() {

		HashMap<Integer, byte[]> result = fh.segment();
		totalPacketsCreated = result.size();
		System.out.println("Total packets :" + totalPacketsCreated);

		HeaderInfo info;

		for (int i = 1; i <= totalPacketsCreated; i++) {
			info = new HeaderInfo();
			info.setSeqNo(i);
			info.setData(result.get(i));

			if (i == totalPacketsCreated) {
				info.setFinishFlag(true);
			}
			eachPacket.put(i, info);
		}

	}

	/**
	 * This class is implemented to handle logic for recieving the packets from the
	 * client.
	 * 
	 * @author gaurav gaur
	 *
	 */
	public class ServerRecieveSide extends Thread {

		int next_pack, prev_ack = Integer.MAX_VALUE, no_of_pack;

		/**
		 * This method gets called when server recieve side thread is started
		 */
		public void run() {
			System.out.println("Waiting for client");
			HeaderInfo pack = null;
			while (true) {
				recievePacket = new DatagramPacket(recieve, recieve.length);
				try {
					serverSocket.receive(recievePacket);
					ip = recievePacket.getAddress();
					port = recievePacket.getPort();
					recieve = recievePacket.getData();
					pack = Deserialize(recieve);
					System.out.println("Received ack: " + pack.getAckNo());

					if (pack.isSynFlag()) {
						no_of_pack = 1;
						next_pack = 1;
						pack_sent = next_pack;
						new ServerSenderSide().start();
					} else {
						no_of_pack = 2;
						pack_sent = next_pack;
					}

					System.out.println(pack_sent + "/" + no_of_pack + "/" + totalPacketsCreated);
					if ((pack_sent + no_of_pack <= totalPacketsCreated + 1)) {
						System.out.println("coming here");
						synchronized (pack_queue) {
							for (int i = 0; i < no_of_pack; i++) {
								pack_queue.add(eachPacket.get(pack_sent));
								next_pack++;
								pack_sent++;
							}
							pack_queue.notifyAll();
						}
					}

					else if (pack_sent == totalPacketsCreated) {
						synchronized (pack_queue) {
							pack_queue.add(eachPacket.get(pack_sent));
							pack_queue.notifyAll();
						}
					}
					prev_ack = pack.getAckNo();

				} catch (IOException e) {
					e.printStackTrace();
				}

			}
		}

	}

	/**
	 * This method will take the packet in byte[] format and deserialize it get
	 * corresponding values of header object.
	 * 
	 * @param data:
	 *            packet in byte[] format
	 * @return: header class object
	 */
	public HeaderInfo Deserialize(byte[] data) {
		HeaderInfo result = null;
		ByteArrayInputStream b = new ByteArrayInputStream(data);
		try {
			ObjectInputStream o = new ObjectInputStream(b);
			result = (HeaderInfo) o.readObject();
		} catch (IOException | ClassNotFoundException e) {
			e.printStackTrace();
		}

		return result;
	}

	/**
	 * This class is implemented to handle server sender side logic
	 * 
	 * @author gaurav gaur
	 *
	 */
	class ServerSenderSide extends Thread {

		/**
		 * This method gets called when server send side thread is created.
		 */
		public void run() {
			while (true) {
				synchronized (pack_queue) {
					if (pack_queue.size() == 0) {
						try {
							pack_queue.wait();
						} catch (InterruptedException e) {
							e.printStackTrace();
						}
					}
					for (int i = 0; i < pack_queue.size(); i++) {
						System.out.println("Sending packet with sequence number :" + pack_queue.peek().getSeqNo());
						HeaderInfo hi = pack_queue.poll();
						send = serialize(hi);
						sendPacket = new DatagramPacket(send, send.length, ip, port);
						try {
							serverSocket.send(sendPacket);
							Thread.sleep(2000);
						} catch (IOException | InterruptedException e) {
							e.printStackTrace();
						}
					}
				}
			}
		}
	}

	/**
	 * This method will take the object of header class and serialize it to get
	 * corresponding packet in byte[] format
	 * 
	 * @param hi
	 *            : header class object.
	 * @return packet in byte[] format
	 */
	public byte[] serialize(HeaderInfo hi) {
		ByteArrayOutputStream b = new ByteArrayOutputStream();
		try {
			ObjectOutputStream o = new ObjectOutputStream(b);
			o.writeObject(hi);
		} catch (IOException e) {
			e.printStackTrace();
		}

		byte[] result = b.toByteArray();
		return result;
	}

}
