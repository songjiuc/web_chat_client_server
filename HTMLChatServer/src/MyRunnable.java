import java.io.IOException;
import java.net.Socket;

/*
The runnable class is used to create the runnable code for multiple threads.
*/
public class MyRunnable implements Runnable{

    Socket s1;

    public MyRunnable(Socket s){
        this.s1 = s;
    }



    /*
After a new HTTP server is created, the runnable will create HTTP request, get request, create HTTP response
and send response.
*/

    @Override
    public void run() {

        try {
            HTTPRequest request = new HTTPRequest(s1);
            request.getRequest();
            HTTPResponse response = new HTTPResponse(request);
            response.sendResponse();
            s1.close();

        } catch (IOException e) {
            System.out.println("A runtime exception occurred in HTTP Request/Response.");
            throw new RuntimeException(e);
        }

    }
}
