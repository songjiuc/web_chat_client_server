import java.io.IOException;
import java.io.OutputStream;
import java.net.Socket;
import java.util.ArrayList;
import java.util.HashMap;

/*
The room class is created to manage all user-created rooms, users in the rooms and chat history in the rooms.
*/
public class Room {
    static String name_;
    ArrayList<Socket> clients_;
    ArrayList<String> chat_history_;
    static HashMap<String,Room> room_list_ = new HashMap<>();

    Room(String name){
        name_ = name;
        clients_ = new ArrayList<>();
        chat_history_ = new ArrayList<>();
    }

    /*
The getRoom method takes a string room name as the parameter and returns the room.
If the room doesn't exist, create a new one and add it to the room list.
If the room exists, just return the existing one.
*/
    public synchronized static Room getRoom(String name) throws IOException {
        if(!room_list_.containsKey(name)) {
            Room room = new Room(name);
            Room.room_list_.put(name,room);
            return room;
        }
        else{
            return room_list_.get(name);
        }
    }

    public synchronized void addClient(Socket s){
        clients_.add(s);
    }

    public synchronized void removeClient(Socket s){
        clients_.remove(s);
    }


    public void addMessage(String jsonString){
        chat_history_.add(jsonString);
    }


    public synchronized void sendMessage(String jsonString) throws IOException {
        for(Socket client: clients_) {
            writeMsg(client,jsonString);
        }
    }

    public synchronized void sendHistory(Socket client) throws IOException {
        for(String jsonString: chat_history_) {
            writeMsg(client,jsonString);
        }
    }

        /*
The writeMsg method takes a client socket and json message string as the parameters but returns nothing.
It aims to write the JSON message in byte format to the client and let the browser render the message.
*/

    public synchronized void writeMsg(Socket client, String jsonString) throws IOException {

            OutputStream os1 = client.getOutputStream();

            os1.write(0x81);

            byte[] length_bytes = null;

            if (jsonString.length() < 126) {
                os1.write(jsonString.length());
            } else {
                if (jsonString.length() < Math.pow(2, 16)) {
                    os1.write(0x7E);
                    length_bytes = new byte[2];
                    length_bytes[0] = (byte) ((jsonString.length() >> 8) & 0xFF);
                    length_bytes[1] = (byte) (jsonString.length() & 0xFF);
                    os1.write(length_bytes);
                } else {
                    os1.write(0x7F);

                    length_bytes = new byte[4];

                    length_bytes[0] = (byte) ((jsonString.length() >> 24) & 0xFF);
                    length_bytes[1] = (byte) ((jsonString.length() >> 16) & 0xFF);
                    length_bytes[2] = (byte) ((jsonString.length() >> 8) & 0xFF);
                    length_bytes[3] = (byte) (jsonString.length() & 0xFF);
                    os1.write(length_bytes);
                }
            }

            os1.write(jsonString.getBytes());
            System.out.println("From end of send message function: " + jsonString);
            os1.flush();
        }


}
