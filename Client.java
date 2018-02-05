import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.TreeMap;
import java.util.zip.GZIPInputStream;

/**
 * This program is designed to implement client side of the project. In client
 * side we recieve the packet and remove the sequence number from the list that
 * is used to identify what packet is lost. Secondly, we will send packet to
 * server having ack flag set to 1 and ack number equal to recieved sequence
 * number + 1
 * 
 * @author gaurav gaur(gxg7435@g.rit.edu)
 *
 */
class Client extends Thread {

	TreeMap<Integer, HeaderInfo> recievedPack;
	InetAddress ip;
	int port, counter;

	ArrayList<Integer> seqNo;
	byte[] recieve;
	byte[] send;

	DatagramSocket clientSocket;
	DatagramPacket recievePacket;
	DatagramPacket sendPacket;

	/**
	 * This is a constructor for client side to initialize the variables
	 * 
	 * @param ip
	 *            : ip address of server
	 * @param port
	 *            : port number of server(set to 7000)
	 */
	public Client(String ip, int port) {
		try {
			this.ip = InetAddress.getByName(ip);
			this.port = port;

			recievedPack = new TreeMap<Integer, HeaderInfo>();
			seqNo = new ArrayList<Integer>();

			recieve = new byte[65507];
			send = new byte[65507];

			clientSocket = new DatagramSocket(8000);
			recievePacket = null;
			sendPacket = null;
		} catch (UnknownHostException | SocketException e) {
			e.printStackTrace();
		}
	}

	/**
	 * This is the main method for client program.
	 * 
	 * @param args[0]
	 *            : ip address of server args[1] : port number of server
	 */
	public static void main(String args[]) {
		Client c = new Client(args[0], Integer.parseInt(args[1]));
		c.sendData(0);
	}

	/**
	 * this method will send the packet to server with ack flag set to 1 & ack no =
	 * seq_no + 1
	 * 
	 * @param ack
	 *            : recieved seq_No
	 */
	private void sendData(int ack) {

		HeaderInfo dataToSend = new HeaderInfo();
		if (counter == 0) {
			dataToSend.setSynFlag(true);
			System.out.println("Syn Flag set in packet");
			counter++;
		}

		else {
			dataToSend.setAckFlag(true);
			dataToSend.setAckNo(ack);
			System.out.println("Ack Sent:" + ack);
		}

		send = serialize(dataToSend);
		sendPacket = new DatagramPacket(send, send.length, ip, port);
		try {
			clientSocket.send(sendPacket);
			if (dataToSend.isSynFlag()) {
				this.start();
			}

		} catch (IOException e) {
			e.printStackTrace();
		}

	}

	/**
	 * This method will get executed when thread gets started
	 */
	public void run() {
		recieveCode();
	}

	/**
	 * This method is used to remove the sequence number from the list that is
	 * received by the client.
	 * 
	 * @param seqNoLatest:
	 *            sequence number recieved
	 * @return : sequence number + 1
	 */
	public int nextSeqNum(int seqNoLatest) {
		boolean flag = false;
		if (seqNo.isEmpty()) {
			seqNo.add(seqNoLatest + 1);
		} else {

			for (int i = 0; i < seqNo.size(); i++) {
				if (seqNo.get(i) == seqNoLatest) {
					System.out.println(seqNo.get(i) + " removed");
					if (seqNo.size() > 1) {
						flag = true;
					}
					seqNo.remove(i);
				}
			}
			if (!flag) {
				seqNo.add(seqNoLatest + 1);
				flag = false;
			}
			return seqNo.get(0);
		}
		return seqNoLatest + 1;
	}

	/**
	 * This method is used to receive the packet from the server.
	 */
	public void recieveCode() {

		HeaderInfo data;
		int seqNo = 0;
		while (true) {
			recievePacket = new DatagramPacket(recieve, recieve.length);
			try {
				clientSocket.receive(recievePacket);
				recieve = recievePacket.getData();
				data = Deserialize(recieve);
				seqNo = data.getSeqNo();
				System.out.println("packet recieved having sequence No: " + seqNo);

				sendData(nextSeqNum(seqNo));
				recievedPack.put(data.getSeqNo(), data);
				if (data.isFinishFlag() && data.getSeqNo() == recievedPack.size()) {
					System.out.println("all packets recieved");
					openPacket();
				}
			} catch (IOException e) {
				e.printStackTrace();
			}
		}

	}

	/**
	 * This method is used to write the data to a file and then decompress it to get
	 * final output image.
	 */
	public void openPacket() {
		File newFile = new File("output.gz");
		FileOutputStream fos = null;
		try {
			fos = new FileOutputStream(newFile, true);
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}
		for (int index = 1; index < recievedPack.size(); index++) {

			try {
				fos.write(recievedPack.get(index).getData());
				fos.flush();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		System.out.println("Completed writing to the file");

		int len;
		byte[] buffer = new byte[1024];
		try {
			GZIPInputStream giz = new GZIPInputStream(new FileInputStream("output.gz"));
			FileOutputStream fos1 = new FileOutputStream(("final.jpg"));
			while ((len = giz.read(buffer)) > 0) {
				fos1.write(buffer, 0, len);
			}
			giz.close();
			fos1.close();
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		try {
			fos.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	/**
	 * This method will take received packet & deserialize it to get corresponding
	 * header values.
	 * 
	 * @param data
	 *            : recieved packet
	 * @return : object of header class
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
	 * This method will take the object of header class & serialize it to get
	 * corresponding data in byte format.
	 * 
	 * @param hi
	 * @return
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