package com.example.androidchatclient;

import static com.example.androidchatclient.ChatPage.adapter;
import static com.example.androidchatclient.ChatPage.lv_;
import static com.example.androidchatclient.ChatPage.messages;


import android.util.Log;

import com.neovisionaries.ws.client.WebSocket;
import com.neovisionaries.ws.client.WebSocketAdapter;
import com.neovisionaries.ws.client.WebSocketException;
import com.neovisionaries.ws.client.WebSocketFrame;

import org.json.JSONObject;

import java.util.List;
import java.util.Map;

public class MyWebSocket extends WebSocketAdapter {

    @Override
    public void onConnected(WebSocket websocket, Map<String, List<String>> headers) throws Exception {
        Log.d("DD: ", "Connect the websocket.");

    }

    @Override
    public void onTextMessage(WebSocket websocket, String text) throws Exception {
        Log.d("DD: ","Receiving-" + text);

        String msg = text;
        JSONObject msgObj = new JSONObject(msg);
        Log.d("DD: ", "Receiving JSON message-"+msgObj.toString());

        String msg_type = (String) msgObj.get("type");
        String msg_user = (String) msgObj.get("user");
        String msg_room = (String) msgObj.get("room");

        String message;

        if(msg_type.equals("join")){
            message = msg_user + " has joined the room " + msg_room + ".";
        }
        else if(msg_type.equals("leave")){
            message = msg_user + " left the room " + msg_room;
        }
        else{
            String msg_message = (String) msgObj.get("message");
            message = msg_user + ": " + msg_message;
        }

//        Log.d("DD: ", "Parsing JSON message-"+msg_type+" " +msg_room+" "+msg_user);
        Log.d("DD: ", "Server sent: "+message);

        messages.add(message);

        lv_.post(new Runnable() {
            @Override
            public void run() {
                adapter.notifyDataSetChanged();
                lv_.smoothScrollToPosition(adapter.getCount());
            }
        });
    }

    @Override
    public void onConnectError(WebSocket websocket, WebSocketException exception){
        Log.d("DD: ","on connect error.");
    }

    @Override
    public void onDisconnected(WebSocket websocket,
                               WebSocketFrame serverCloseFrame, WebSocketFrame clientCloseFrame,
                               boolean closedByServer) throws Exception {
        Log.d("DD: ", "on disconnect.");
    }

}
