import java.net.DatagramPacket;
import java.net.SocketException;
import java.nio.ByteBuffer;

public class ServerMain {


	public static void main(String[] args) throws SocketException {

		Server server = new Server();
		int[] udpPacket = stageA(server);
		stageB(server, udpPacket);
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
	
	public static void stageB(Server server, int[] udpPacket) {
		System.out.println("---- Starting Stage B ----");
		for (int i = 0; i < udpPacket[0]; i++) {
			DatagramPacket receivedPacket = server.receiveData(udpPacket[2]);
			ByteBuffer receivedData = ByteBuffer.wrap(receivedPacket.getData());
			if ((int)(Math.random() * 10) % 2 == 0) {
				// Randomly choose to ack a packet
				int packetId = ByteBuffer.wrap(receivedPacket.getData()).getInt(Server.Header.HEADER_LENGTH);
				Server.Header header = server.new Header(4, receivedData.getInt(4), receivedData.getShort(8), Server.Header.LAST_3_SSN);
				ByteBuffer ackMessage = ByteBuffer.allocate(Server.Header.HEADER_LENGTH + header.payloadLen);
				header.addToBuffer(ackMessage);
				ackMessage.putInt(packetId);
				System.out.println("Sending ack packet id: " + packetId);
				server.sendData(ackMessage.array(), receivedPacket.getAddress(), receivedPacket.getPort());
			} else {
				System.out.println("Ignoring packet -- waiting for another");
				i--;
			}
		}
		System.out.println("Acknowledged all " + udpPacket[0] + " packets.");
	}

}
