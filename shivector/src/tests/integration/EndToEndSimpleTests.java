package tests.integration;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.ServerSocket;
import java.net.Socket;

import org.junit.Test;

public class EndToEndSimpleTests {

    @Test
    public void testBasic() throws Exception {
        // TODO
        new Server().run();
        new Client().run();
    }

    class Server {
        void run() throws Exception {
            ServerSocket providerSocket = new ServerSocket(2004, 10);
            System.out.println("Waiting for connection");
            Socket connection = providerSocket.accept();
            System.out.println("Connection received from "
                    + connection.getInetAddress().getHostName());
            OutputStream out = connection.getOutputStream();
            out.flush();
            InputStream in = connection.getInputStream();
            sendMessage(out, "Connection successful");
            String message = readMessage(in);
            System.out.println("client>" + message);
            message = readMessage(in);
            System.out.println("client>" + message);
            if (message.equals("bye"))
                sendMessage(out, "bye");
            in.close();
            out.close();
            providerSocket.close();
        }
    }

    class Client {
        void run() throws Exception {
            Socket requestSocket = new Socket("localhost", 2004);
            System.out.println("Connected to localhost in port 2004");
            OutputStream out = requestSocket.getOutputStream();
            out.flush();
            InputStream in = requestSocket.getInputStream();
            String message = readMessage(in);
            System.out.println("server>" + message);
            sendMessage(out, "Hi my server");
            message = "bye";
            sendMessage(out, message);
            message = readMessage(in);
            in.close();
            out.close();
            requestSocket.close();
        }
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

    /** Reads a String from the input stream. */
    public static String readMessage(InputStream in) {
        int bufferLength = 9999;
        byte[] buffer = new byte[bufferLength];
        int bytesRead = 0;
        int length = 0;
        do {
            try {
                bufferLength -= bytesRead;
                length += bytesRead;
                bytesRead = in.read(buffer, length, bufferLength);
            } catch (Exception e) {
                return "";
            }
        } while (bytesRead > -1);
        return byteArrayToString(buffer, length);
    }

    /** Writes a byte array of the given msg to out. */
    public static void sendMessage(OutputStream out, String msg) {
        try {
            out.write(stringToByteArray(msg));
            out.flush();
        } catch (IOException ioException) {
            ioException.printStackTrace();
        }
    }

}
