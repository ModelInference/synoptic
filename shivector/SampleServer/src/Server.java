import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.ServerSocket;
import java.net.Socket;

public class Server {

    public static void main(String[] args) throws Exception {
        Server server = new Server();
        while (true) {
            server.run();
        }
    }

    void run() throws Exception {
        ServerSocket providerSocket = new ServerSocket(Util.SERVER_PORT,
                Util.BACKLOG);
        System.out.println("Waiting for connection");

        Socket connection = providerSocket.accept();
        System.out.println("Connection received from "
                + connection.getInetAddress().getHostName());

        ObjectOutputStream out = new ObjectOutputStream(
                connection.getOutputStream());
        out.flush();

        sendMessage(out, "Connection successful");

        ObjectInputStream in = new ObjectInputStream(
                connection.getInputStream());

        String message = "";
        while (!(message = (String) in.readObject()).equals("bye")) {
            System.out.println("Message received: " + message);
            sendMessage(out, "Thanks!");
        }
        System.out.println("Message received: " + message);
        sendMessage(out, "bye");

        System.out.println("Closing connection");
        in.close();
        out.close();
        providerSocket.close();
    }

    /** Convert a string to a byte[] */
    public static byte[] stringToByteArray(String msg) {
        return msg.getBytes();
    }

    /** Convert a byte[] to a string */
    public static String byteArrayToString(byte[] msg, int length) {
        if (msg == null)
            return null;
        return new String(msg, 0, length);
    }

    public static void sendMessage(ObjectOutputStream out, String msg) {
        try {
            System.out.println("sending>" + msg);
            out.writeObject(msg);
            out.flush();
        } catch (IOException ioException) {
            ioException.printStackTrace();
        }
    }
}
