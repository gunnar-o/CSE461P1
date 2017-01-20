import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.Random;

public class Server extends Thread {
	
	public static final int TIMEOUT = 3000;
	public static final int PORT_NUMBER = 12235;
	public static final String HOST_NAME = "localhost";
	
	
	public DatagramPacket receiveData(int port) {
		try {
			DatagramSocket serverSocket = new DatagramSocket(port);
			System.out.println("Listening on port: " + serverSocket.getLocalPort());
			byte[] receivedData = new byte[1024];
			DatagramPacket receivedPacket = new DatagramPacket(receivedData, receivedData.length);
			serverSocket.receive(receivedPacket);
			
			System.out.println("Server recieved packet. Message:");
			for (int i = 12; i < receivedData.length; i++) {
				System.out.print(Integer.toHexString(receivedData[i]) + "|");
			}
			System.out.println();
			serverSocket.close();
			return receivedPacket;
			
		} catch (Exception e) {
			e.printStackTrace();
			return null;
		}
	}
	
	public void sendData(byte[] data, InetAddress address, int port) {
		try {
			DatagramSocket serverSocket = new DatagramSocket();
			DatagramPacket packet = new DatagramPacket(data, data.length, address, port);
			serverSocket.send(packet);
			serverSocket.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	// Reads in the header from the ByteBuffer, changing the current position to just after the header
	public Header verifyHeader(ByteBuffer data) {
		Header h = new Header(data.getInt(), data.getInt(), data.getShort(), data.getShort());
		// TODO: verify header is constructed correctly. If not, return null.
		System.out.println(h);
		return h;
	}
	
	// Reads and returns the next length bytes (length / 2 characters) starting at the ByteBuffer's current pos.
	// Throws BufferUnderflowException if the header payload length > (packet size - header size)
	public String readMessage(ByteBuffer data, int length) {
		String message = "";
		for (int i = 0; i < length - 1; i++) {	// run length-1 times to exclude null terminator '\0'
			message += (char)data.get();
		}
		return message;
	}
	
	// Sends the UDP packet containing the 4 randomly-generated ints for part a
	// Returns a 4-integer array representing the udp packet sent
	public int[] sendUDPPacket(Header receivedHeader, InetAddress address, int port) throws SocketException {
		DatagramSocket socket = new DatagramSocket();
		Random r = new Random();
		Header header = new Header(16, receivedHeader.pSecret, (short) (receivedHeader.step + 1), Header.LAST_3_SSN);
		ByteBuffer packet = ByteBuffer.allocate(Header.HEADER_LENGTH + header.payloadLen);
		packet.order(ByteOrder.BIG_ENDIAN);
		header.addToBuffer(packet);
		int num = r.nextInt(5) + 1;
		int len = (r.nextInt(4) + 1) * 4;
		DatagramSocket temp = new DatagramSocket();
		int udpPort = temp.getLocalPort();
		temp.close();
		int secretA = r.nextInt();
		packet.putInt(num);
		packet.putInt(len);
		packet.putInt(udpPort);
		packet.putInt(secretA);
		System.out.println("Sending udp packet: " + num + " | " + len + " | " + udpPort + " | " + secretA);
		this.sendData(packet.array(), address, port);
		return new int[]{num, len, udpPort, secretA};
	}


	class Header {
		public static final int HEADER_LENGTH = 12; 	// Length of the packet header in bytes
		public static final short  LAST_3_SSN = 906;
		public int payloadLen;
		public int pSecret;
		public short step;
		public short ssn;
		
		public Header(int payloadLen, int pSecret, short step, short ssn) {
			this.payloadLen = payloadLen;
			this.pSecret = pSecret;
			this.step = step;
			this.ssn = ssn;
		}
		
		public void addToBuffer(ByteBuffer packet) {
			packet.putInt(payloadLen);
			packet.putInt(pSecret);
			packet.putShort(step);
			packet.putShort(ssn);
		}
		
		public String toString() { return "Header: " + payloadLen + " | " + pSecret + " | " + step + " | " + ssn; }
	}
}
