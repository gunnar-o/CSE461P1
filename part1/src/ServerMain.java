import java.io.IOException;
import java.net.DatagramPacket;
import java.net.ServerSocket;
import java.net.SocketException;
import java.nio.ByteBuffer;
import java.util.Random;

public class ServerMain {


	public static void main(String[] args) throws Exception {

		Server server = new Server();
		int[] udpPacket = stageA(server);
		ByteBuffer lastUDPMessage = stageB(server, udpPacket);
		stageC(server, lastUDPMessage);
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
		System.out.println("tcpPort = " + tcpPort );
		tcpPortMessage.putInt(tcpPort);
		temp.close();
		tcpPortMessage.putInt(new Random().nextInt());	// Generate random secretB
		server.sendData(tcpPortMessage.array(), receivedPacket.getAddress(), receivedPacket.getPort());
		return tcpPortMessage;
	}
	
	public static void stageC(Server server, ByteBuffer lastUDPMessage) {
		System.out.println("\n---- Starting Stage C ----");
		int tcpPort = lastUDPMessage.getInt(Server.Header.HEADER_LENGTH);
		System.out.println("Waiting on TCP port " + tcpPort);
		
	}

}
