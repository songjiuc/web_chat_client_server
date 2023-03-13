import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.HashMap;
import java.util.Map;
import java.util.Scanner;


/*
The HTTPRequest class has a client socket to receive and parse the HTTP request sent from clients.
 */

public class HTTPRequest {

    Socket s1;

    String filename;

    String filename_extension;

    String request_result;

    Map headers_;

    boolean isWSRequest;

/*
The constructor takes a client socket as its parameter, initialize its hashmap variable to
store http header information later.
 */
    HTTPRequest(Socket s) throws IOException {

        s1 = s;
        isWSRequest = false;

        headers_ = new HashMap<String, String>();
    }

    /*
    The getRequest method aims to read HTTP request header information sent from clients and store
    them to use later.
    The client socket has been initialized in the constructor so there is no parameter taken.
    It returns nothing because the return type is void and the method only works to read and save
    HTTP request headers.
     */
    public void getRequest() throws IOException {
        try {
            Scanner request_sc = new Scanner(s1.getInputStream());

            // store the request
            String requestline = request_sc.nextLine();
            System.out.println(requestline);
            String[] firstline = requestline.split(" ");

            // store the requested filename
            filename = firstline[1]; // "/"
            filename_extension = filename.substring(filename.lastIndexOf(".") + 1);
            filename = "resources/" + filename;

            // store the http/1.1 result
            request_result = firstline[2];


            // read request

            String line = request_sc.nextLine();

            while (!line.equals("")) {
                String[] substring = line.split(": ");
                // store in map
                headers_.put(substring[0], substring[1]);
                line = request_sc.nextLine();
            }

            if (headers_.containsKey("Sec-WebSocket-Key")) {
                isWSRequest = true;
            }
        } catch (IOException e){
            System.out.println("An error occurred while reading client's request.");
            throw new IOException(e);
        }
    }

}
