

import java.io.*;
import java.net.Socket;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Base64;
import java.util.Map;
import org.json.simple.JSONObject;


/*
The HTTPResponse class has a client socket to send response to clients.
 */

public class HTTPResponse {

    HTTPRequest request_;

    String filename_;

    String filename_extension_;

    String request_result_;

    Map headers_;

    boolean isWSRequest;

    OutputStream output_;

    Socket client;

    int opcode;

    String room;

    String user;

    PrintWriter pw;

/*
The constructor takes a HTTPRequest variable as its parameter, initializes its class member variable as the
HTTPRequest r, and create new output stream and print writer of the client socket.
 */

    HTTPResponse(HTTPRequest r) throws IOException {

        request_ = r;
        filename_ = request_.filename;
        filename_extension_ = request_.filename_extension;
        request_result_ = request_.request_result;
        headers_ = request_.headers_;
        isWSRequest = r.isWSRequest;
        client = request_.s1;
        output_ = client.getOutputStream();
        pw = new PrintWriter(output_);
    }

    // The sendResponse method will test if the request from clients is websocket request
    // and then send the corresponding response back.

    public void sendResponse() throws IOException {

        try {
            if(isWSRequest) {
                sendWSResponse();
            }
            else {
                sendNormalResponse();
            }
        } catch (IOException e){
            System.out.println("An error occurred while responding to client.");
            throw new IOException(e);
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException(e);
        }
    }

    /*
    The readPayload method takes an input stream as the parameter, reads the web socket request
    in binary language, and decodes the encoded payload. It returns a byte array which stores all
    decoded payload message.
     */

    private byte[] readPayload(InputStream input) throws IOException {
        DataInputStream readstream = new DataInputStream(input);

        byte b0 = readstream.readByte();
        boolean fin = (b0 & 0x80)!=0;
        opcode = b0 & 0x0F; // opcode 1 = text message 8 = end 2 = binary message

        byte b1= readstream.readByte();
        boolean isMasked = ((b1 & 0x80)!=0);

        int payload_len = b1 & 0x7F;

        int real_length;

        if(payload_len ==126){
            real_length = readstream.readShort();
        }
        else if(payload_len ==127){
            real_length = (int) readstream.readLong();
        }
        else{
            real_length = payload_len;
        }

        byte[] decoded;

        if(isMasked){
            byte[] mask_key = readstream.readNBytes(4);
            byte[] encoded = new byte[real_length];
            readstream.read(encoded);
            decoded = new byte[real_length];
            for(var i=0; i<encoded.length; i++){
                decoded[i] = (byte) (encoded[i] ^ mask_key[i%4]);
            }
        }
        else{
            decoded = new byte[real_length];
            readstream.read(decoded);
        }

        return decoded;
    }

    /*
The writeJSONMessage method takes a byte array with decoded payload message as the parameter, reorganize the
message in JSON format and returns a JSON object result.
 */
    private JSONObject writeJSONMessage(byte[] payload) throws IOException {

        JSONObject json = new JSONObject();

            String message = new String(payload, StandardCharsets.UTF_8);

            String[] pieces = message.split(" ");
            String type = pieces[0];
            user = pieces[1];
            room = null;


            json.put("type", type);
            json.put("user", user);


            if (type.equals("join")) {
                room = pieces[2];
                json.put("room", room);
            } else if (type.equals("message")) {
                String client_message = pieces[2];
                room = pieces[3];
                String time = pieces[4];
                json.put("message", client_message);
                json.put("room", room);
                json.put("time", time);
            }

        return json;
    }

    /*
The handleRoom method takes a string room name, a client socket, and a JSONObject as its parameters and returns nothing.
It aims to test the message type sent from clients and handle "join a room" and "leave a room" request.
If a room is existing, the chat history in the room will be sent to new added user in the room.
*/
    private void handleRoom(String roomname, Socket s, JSONObject json) throws IOException {

        String jsonString = json.toString();
        String type = (String) json.get("type");

        Room newRoom = Room.getRoom(roomname);

        if(type.equals("join")){
            newRoom.addClient(s);
            newRoom.sendHistory(client);
        }
        else if(type.equals("leave")){
            newRoom.removeClient(s);
        }
        newRoom.addMessage(jsonString);
        newRoom.sendMessage(jsonString);
    }

    /*
The sendWSResponse method returns nothing but sends websocket response to clients.
*/
    private void sendWSResponse() throws IOException, NoSuchAlgorithmException {

        wsHandShake();

        while(true){

            byte[] decoded = readPayload(client.getInputStream());

            String s = new String(decoded, StandardCharsets.UTF_8);
            System.out.println("Server received message: "+s);

            JSONObject json = new JSONObject();
            if(opcode ==0x8){
                //JSONObject json = new JSONObject();
                json.put("type","leave");
                json.put("user",user);
                json.put("room",room);
                handleRoom(room,client, json);
                break;
            }
            else {
                json = writeJSONMessage(decoded);
                handleRoom(room, client, json);
            }

            System.out.println("Server sent message here.");
        }

    }

    /*
The sendNormalResponse method returns nothing but sends normal HTTP response to clients.
*/
    private void sendNormalResponse() throws IOException {

        // create outputstream and printwriter

        File openfile = new File(filename_);
        String content_length;

        String request_result = "HTTP/1.1";

        if (openfile.exists()) {
            request_result = request_result + " 200 success";
            content_length = "Content-length: " + openfile.length();

        } else {
            request_result = request_result + " 404 not found";
            content_length = "Content-length: 0";
        }

        // send the response header
        pw.println(request_result);

        // determine the file extension (html/css) and use
        pw.println("Content-type: text/" + filename_extension_);
        pw.println(content_length);
        System.out.println("Just sent the normal response header.");

        pw.println("\n");
        pw.flush();
        System.out.println(openfile.length());

        //send the data from the file
        FileInputStream responsefstream = new FileInputStream(openfile);
        for(int i=0; i<openfile.length(); i++) {
            pw.write(responsefstream.read());
            pw.flush();
        }
    }

    /*
The wsHandShake method returns nothing but sends websocket response header to clients.
*/
    private void wsHandShake() throws NoSuchAlgorithmException, UnsupportedEncodingException {
        pw.println("HTTP/1.1 101 Switching Protocols");
        pw.println("Upgrade: websocket");
        pw.println("Connection: Upgrade");

        String theMagicString = "258EAFA5-E914-47DA-95CA-C5AB0DC85B11";
        String newKey = request_.headers_.get("Sec-WebSocket-Key") + theMagicString;
        String encodedKey = Base64.getEncoder().encodeToString(MessageDigest.getInstance("SHA-1").digest(newKey.getBytes("UTF-8")));

        pw.println("Sec-WebSocket-Accept: " + encodedKey);
        System.out.println("Just sent the ws response header to client.");
        pw.println();
        pw.flush();
    }

}


