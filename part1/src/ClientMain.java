public class ClientMain {
    static final private String HOST_NAME = "attu2.cs.washington.edu";
    static final private int PORT_NUMBER = 12235;

    public static void main(String[] args) {
        Client client = new Client(HOST_NAME, PORT_NUMBER);

        PartA(client);
    }

    private static byte[] PartA(Client client) {
        short SSN = 997;
        short step = 1;
        byte[] message = client.FormatMessage("hello world", 0, step, SSN);
        return client.SendUDP(message);
    }
}
