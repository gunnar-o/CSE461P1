import java.io.IOException;
import java.net.DatagramPacket;
import java.net.ServerSocket;
import java.net.SocketException;
import java.nio.ByteBuffer;
import java.util.Arrays;
import java.util.Random;

public class ServerMain {


	public static void main(String[] args) throws Exception {

		Server server = new Server();
		int[] udpPacket = stageA(server);
		ByteBuffer lastUDPMessage = stageB(server, udpPacket);
		ByteBuffer firstTCPMessage = stageC(server, lastUDPMessage);
		stageD(server, firstTCPMessage);
	}

	// Returns a 4-integer array representing the udp packet sent
	public static int[] stageA(Server server) throws SocketException {
		System.out.println("---- Starting Stage A ----");
		DatagramPacket receivedPacket = server.receiveData(Server.PORT_NUMBER);
		ByteBuffer data = ByteBuffer.wrap(receivedPacket.getData());
		Server.Header dataHeader = server.verifyHeader(data);
		String message = server.readMessage(data, dataHeader.payloadLen);
		System.out.println("Read message: " + message);
		if (message.equals("hello world")) {
			return server.sendUDPPacket(dataHeader, receivedPacket.getAddress(), receivedPacket.getPort()); // Return the sent packet as ByteBuffer
		} else {
			// TODO: Handle incorrect handshake
			return null;
		}
	}
	
	public static ByteBuffer stageB(Server server, int[] udpPacket) throws IOException {
		System.out.println("---- Starting Stage B ----");
		System.out.println("UDP Packet ints: " + udpPacket[0] + " " + udpPacket[1] + " " + udpPacket[2] + " " + udpPacket[3]);
		boolean failedToAck = false;	// ensure that we don't acknowledge at least once
		Server.Header finalUDPHeader = null;
		DatagramPacket receivedPacket = null;
		for (int i = 0; i < udpPacket[0]; i++) {
			receivedPacket = server.receiveData(udpPacket[2]);
			ByteBuffer receivedData = ByteBuffer.wrap(receivedPacket.getData());
			int packetId = ByteBuffer.wrap(receivedPacket.getData()).getInt(Server.Header.HEADER_LENGTH);
			if ((int)(Math.random() * 10) % 2 == 0 || !failedToAck) {
				// Randomly choose to ack a packet
				Server.Header header = server.new Header(4, receivedData.getInt(4), receivedData.getShort(8), Server.Header.LAST_3_SSN);
				ByteBuffer ackMessage = ByteBuffer.allocate(Server.Header.HEADER_LENGTH + header.payloadLen);
				header.addToBuffer(ackMessage);
				ackMessage.putInt(packetId);
				System.out.println("Sending ack packet id: " + packetId);
				server.sendData(ackMessage.array(), receivedPacket.getAddress(), receivedPacket.getPort());
			} else {
				System.out.println("Ignoring packet " + packetId + " -- waiting for another");
				i--;
				failedToAck = true;
			}
			if (finalUDPHeader == null) {
				finalUDPHeader = server.new Header(8, receivedData.getInt(4), (short) 2, Server.Header.LAST_3_SSN);
			}
		}
		if (receivedPacket == null || finalUDPHeader == null) throw new IllegalStateException("Didn't process any udp packets");
		System.out.println("Acknowledged all " + udpPacket[0] + " packets.");
		
		// Step b2 -- Send TCP port number & secret B
		ByteBuffer tcpPortMessage = ByteBuffer.allocate(Server.Header.HEADER_LENGTH + finalUDPHeader.payloadLen);
		finalUDPHeader.addToBuffer(tcpPortMessage);
		ServerSocket temp = new ServerSocket(0);
		int tcpPort = temp.getLocalPort();
		System.out.println("tcpPort = " + tcpPort);
		tcpPortMessage.putInt(tcpPort);
		temp.close();
		int secretB = 22222;//new Random().nextInt();
		tcpPortMessage.putInt(secretB);	// Generate random secretB
		server.sendData(tcpPortMessage.array(), receivedPacket.getAddress(), receivedPacket.getPort());
		return tcpPortMessage;
	}
	
	public static ByteBuffer stageC(Server server, ByteBuffer lastUDPMessage) throws IOException {
		System.out.println("\n---- Starting Stage C ----");
		int tcpPort = lastUDPMessage.getInt(Server.Header.HEADER_LENGTH);
		ServerSocket serverSocket = new ServerSocket(tcpPort);
		server.openTCP(serverSocket);
		Server.Header c2Header = server.new Header(13, lastUDPMessage.getInt(4), (short)2, Server.Header.LAST_3_SSN);
		int packetLenNoPadding = Server.Header.HEADER_LENGTH + c2Header.payloadLen;
		ByteBuffer c2Message = ByteBuffer.allocate(packetLenNoPadding + 3);
		c2Header.addToBuffer(c2Message);
		Random r = new Random();
		int num2 = (r.nextInt(5)) + 1;
		int len2 = (r.nextInt(4) + 1) * 4; // Randomly choose 4, 8, 12, or 16
		int secretC = 333333;//r.nextInt();
		char c = (char) (r.nextInt('~' - 'A') + 'A');
		c2Message.putInt(num2);
		c2Message.putInt(len2);
		c2Message.putInt(secretC);
		c2Message.put((byte) (c & 0xFF));
		System.out.println("Sending c2 packet: " + num2 + " " + len2 + " " + secretC + " " + c);
		System.out.println(Arrays.toString(c2Message.array()));
		server.sendTCP(c2Message.array());
		return c2Message;
	}
	
	public static void stageD(Server server, ByteBuffer firstTCPMessage) {
		System.out.println("\n---- Starting Stage D ----");
		int num2 = firstTCPMessage.getInt(Server.Header.HEADER_LENGTH);
		int len2 = firstTCPMessage.getInt(Server.Header.HEADER_LENGTH + Integer.SIZE / 8);
		byte c = firstTCPMessage.get(Server.Header.HEADER_LENGTH + 3 * (Integer.SIZE / 8));
		
		// Receive the character packets and verify
		System.out.println("Verifying characters...");
		for (int i = 0; i < num2; i++) {
			byte[] message = server.readTCP(Server.Header.HEADER_LENGTH + len2);
			System.out.println("Read char message: " + Arrays.toString(message));
			for (int j = Server.Header.HEADER_LENGTH; j < message.length; j++) {
				// Check all of the character bytes in the packet are the correct character c
				if (message[Server.Header.HEADER_LENGTH + i] != c) throw new IllegalStateException("Character incorrect in packet @" + j);
			}
		}
		System.out.println("Character verification complete.");
		
		// Send the packet with secretD
		Server.Header d2Header = server.new Header(4, firstTCPMessage.getInt(4), (short)2, Server.Header.LAST_3_SSN);
		ByteBuffer d2Message = ByteBuffer.allocate(Server.Header.HEADER_LENGTH + d2Header.payloadLen);
		d2Header.addToBuffer(d2Message);
		int secretD = 44444;//new Random().nextInt();
		d2Message.putInt(secretD);
		System.out.println("Sending d2 packet: " + secretD);
		System.out.println(Arrays.toString(d2Message.array()));
		server.sendTCP(d2Message.array());
		server.closeTCP();
	}

}
