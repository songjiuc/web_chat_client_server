import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;

/*
This program includes a web chat server which allows multiple users chat with
each other simultaneously from different client browsers.
In the main function, after a new HTTP server is created, the server will start to
receive the HTTP request from clients and response to clients.

The constructor will create a new HTTP server. Once a new server socket is created, it
will listen to the client and create a client socket.
The client socket will be passed by as a parameter to HTTPRequest, HTTPResponse and Room classes.
 */

public class HTTPServer {
    ServerSocket client;

    ArrayList<Thread> connections_;


    public HTTPServer() throws IOException {

        connections_ = new ArrayList<>();

        try {
            client = new ServerSocket(8080);
        } catch (IOException e) {
            System.out.println("The server cannot be connected.");
            throw new IOException(e);
        }

        try {
            while(true) {
                Socket s1 = client.accept();
                MyRunnable runnable = new MyRunnable(s1);
                Thread myThread = new Thread(runnable);
                myThread.start();
                connections_.add(myThread);
            }

        } catch (IOException e) {
            System.out.println("An error occurred while listening to client");
            throw new IOException(e);
        }
    }


    public static void main(String[] args) throws IOException {
        HTTPServer hs1 = new HTTPServer();
    }

}
