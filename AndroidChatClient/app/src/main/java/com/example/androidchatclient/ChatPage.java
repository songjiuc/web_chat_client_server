package com.example.androidchatclient;

import static com.example.androidchatclient.MainActivity.ws_;

import androidx.appcompat.app.AppCompatActivity;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;

import java.security.Key;
import java.util.ArrayList;

public class ChatPage extends AppCompatActivity {

    static ArrayList<String> messages = new ArrayList<>();

    protected static ArrayAdapter adapter;

    protected static ListView lv_;;

    TextView msg;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat_page);

        lv_= findViewById(R.id.chatLV);
        adapter = new ArrayAdapter(this,android.R.layout.simple_list_item_1, messages);
        lv_.setAdapter(adapter);

        msg = findViewById(R.id.msg_box);
        msg.setSelectAllOnFocus(true);
        msg.setOnKeyListener(new View.OnKeyListener() {
            @Override
            public boolean onKey(View view, int i, KeyEvent keyEvent) {
                if((keyEvent.getAction() == keyEvent.ACTION_DOWN)&&
                        (i == keyEvent.KEYCODE_ENTER)){
                    handleSend(msg);
                    return true;
                }
                return false;
            }
        });


    }

    public void handleBack(View view){
        Log.d("DD: ","Pressed the back button");

        ws_.sendClose();

        Intent intent = new Intent(this,MainActivity.class);
        startActivity(intent);
    }

    public void handleSend(View view){

        msg = findViewById(R.id.msg_box);

        Intent sendIntent = getIntent();
        String user_name_s = sendIntent.getStringExtra("user");

        String message = user_name_s + " " + msg.getText().toString();
        Log.d("DD: ", "Send message: "+message);

        ws_.sendText(message);

        msg.setText("");

    }
}