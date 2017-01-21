import java.nio.ByteBuffer;
import java.util.Arrays;

public class ClientMain {
    static final private String HOST_NAME = "attu2.cs.washington.edu";
    static final private short step = 1;
    static final private short SSN = 997;
    static private byte c;

    public static void main(String[] args) {
        Client client = new Client(HOST_NAME);
        int[] responseA = PartA(client);
        int[] responseB = PartB(client, responseA);
        int[] responseC = PartC(client, responseB);
        int responseD = PartD(client, responseC);
    }

    private static int[] PartA(Client client) {
        System.out.println("-- START: 1A --");
        String data = "hello world";
        ByteBuffer message = client.FormatHeader(data.length() + 1, 0, step, SSN);

        for(int i = 0; i < data.length(); i++) {
            message.put((byte) (data.charAt(i) & 0xFF));
        }
        message.put((byte) ('\0' & 0xFF));

        byte[] response = client.SendUDP(message.array(), 12235, 1);
        ByteBuffer b = ByteBuffer.wrap(response, 12, response.length - 12);
        int[] responseA = new int[4];
        for(int i = 0; i < 4; i++) {
            responseA[i] = b.getInt();
        }
        System.out.println("\n-- RESPONSE 1A --");
        System.out.println("num: " + responseA[0]);
        System.out.println("len: " + responseA[1]);
        System.out.println("udp_port: " + responseA[2]);
        System.out.println("secretA: " + responseA[3]);
        return responseA;
    }


    private static int[] PartB(Client client, int[] responseA) {
        System.out.println("\n-- START: 1B --");
        byte[] response =  null;
        ByteBuffer responseBuf = null;

        for(int i = 0; i < responseA[0]; i++) {
            ByteBuffer message = client.FormatHeader(responseA[1] + 4, responseA[3], step, SSN);
            message.putInt(i);
            int count = i == responseA[0] - 1 ? 2 : 1;
            response = client.SendUDP(message.array(), responseA[2], count);
            responseBuf = ByteBuffer.wrap(response, 12, response.length - 12);
            if(count == 2 || responseBuf.getInt() == i)
                System.out.println("Acknowledge: Success");
            else {
                throw new IllegalStateException("Ack Packet is formatted incorrectly");
            }
        }

        int[] responseB = new int[2];
        for(int i = 0; i < 2; i++) {
            responseB[i] = responseBuf.getInt();
        }

        System.out.println("\n-- RESPONSE 1B --");
        System.out.println("tcp_port: " + responseB[0]);
        System.out.println("secretB: " + responseB[1]);
        return responseB;
    }

    private static int[] PartC(Client client, int responseB[]) {
        System.out.println("\n-- START: 1C --");
        client.OpenTCP(responseB[0]);
        byte[] response = client.ReadTCP(28);
        System.out.println(Arrays.toString(response));
        ByteBuffer responseBuf = ByteBuffer.wrap(response, 12, response.length - 12);

        int[] responseC = new int[3];
        for(int i = 0; i < 3; i++) {
            responseC[i] = responseBuf.getInt();
        }
        c = responseBuf.get();

        System.out.println("\n-- RESPONSE 1C --");
        System.out.println("num2: " + responseC[0]);
        System.out.println("len2: " + responseC[1]);
        System.out.println("secretC: " + responseC[2]);
        System.out.println("c: \'" + (char)c + "\'");
        System.out.println(Arrays.toString(response));
        return responseC;
    }

    private static int PartD(Client client, int responseC[]) {
        System.out.println("\n-- START: 1D --");
        ByteBuffer message = client.FormatHeader(responseC[1], responseC[2], step, SSN);
        for(int i = 0; i < responseC[1]; i++) {
            message.put(c);
        }
        client.SendTCP(message.array(), responseC[0]);
        byte[] response = client.ReadTCP(16);
        ByteBuffer responseBuf = ByteBuffer.wrap(response, 12, response.length - 12);
        int secretD = responseBuf.getInt();
        System.out.println("\n-- RESPONSE D --");
        System.out.println(Arrays.toString(response));
        System.out.println("secretD: " + secretD);

        client.CloseTCP();
        return secretD;
    }
}