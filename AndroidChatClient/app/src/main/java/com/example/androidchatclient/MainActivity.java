package com.example.androidchatclient;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.TextView;

import com.neovisionaries.ws.client.WebSocket;
import com.neovisionaries.ws.client.WebSocketFactory;

import java.io.IOException;
import java.util.ArrayList;

public class MainActivity extends AppCompatActivity {

    private boolean switchActivities_ = false;
    protected static WebSocket ws_ = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        try {
            ws_ = new WebSocketFactory().createSocket("ws://10.0.2.2:8080/endpoint", 1000 );
        }
        catch( IOException e ) {
            Log.e( "Dd:","WS error" );
        }
        ws_.addListener( new MyWebSocket() );
        ws_.connectAsynchronously();
        Log.d("DD: ","ws async connect called");

    }

    public void handleClick(View view){
        Log.i("DD: ","click login button");

        TextView user_name = findViewById(R.id.user_name_area);
        TextView room_name = findViewById(R.id.room_name_area);

        String user_name_s = user_name.getText().toString();
        String room_name_s = room_name.getText().toString();

        if(!user_name_s.equals("") && !room_name_s.equals("")){
            switchActivities_ = true;
            Log.d("DD:","1"+room_name_s+"1");
        }

        for(int i=0;i<room_name_s.length();i++){
            if(room_name_s.charAt(i) < 'a' || room_name_s.charAt(i)>'z'){
                switchActivities_ = false;
            }
        }


        if(switchActivities_){

            String message = "join " + user_name_s + " " + room_name_s;
            ws_.sendText(message);
            Log.d("DD: ","Sending the Join message-" + message);

            Intent intent = new Intent(this,ChatPage.class);

            intent.putExtra("room", room_name_s);
            intent.putExtra("user",user_name_s);

            Intent intent2 = new Intent(this,MyWebSocket.class);

            intent2.putExtra("room", room_name_s);
            intent2.putExtra("user",user_name_s);

            startActivity(intent);
        }
        else{
            AlertDialog.Builder builder1 = new AlertDialog.Builder(this);
            builder1.setMessage("Please enter valid user name and room name.");
            builder1.setCancelable(false);
            builder1.setPositiveButton("OK", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialogInterface, int i) {
                    dialogInterface.cancel();
                }
            });
            AlertDialog alert1 = builder1.create();
            alert1.show();

            user_name.setText("");
            room_name.setText("");
            user_name.setSelectAllOnFocus(true);
            room_name.setSelectAllOnFocus(true);
        }
    }
}