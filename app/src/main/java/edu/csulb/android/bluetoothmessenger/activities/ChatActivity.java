package edu.csulb.android.bluetoothmessenger.activities;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.EditText;
import android.widget.ListView;

import java.util.ArrayList;
import java.util.List;

import edu.csulb.android.bluetoothmessenger.Constants;
import edu.csulb.android.bluetoothmessenger.R;
import edu.csulb.android.bluetoothmessenger.adapters.MessageAdapter;
import edu.csulb.android.bluetoothmessenger.helpers.ChatHelper;
import edu.csulb.android.bluetoothmessenger.interfaces.MessageCallback;
import edu.csulb.android.bluetoothmessenger.pojos.MessageObject;
import edu.csulb.android.bluetoothmessenger.pojos.PeerDevice;

public class ChatActivity extends AppCompatActivity {

    private PeerDevice device;
    private ChatHelper chatHelper;
    private EditText editTextMessage;
    private List<MessageObject> messageObjectList;
    private ListView listView;
    private MessageAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat);

        Intent intent = getIntent();
        device = (PeerDevice) intent.getSerializableExtra(Constants.DEVICE);

        getSupportActionBar().setTitle(device.name);
        editTextMessage = (EditText) findViewById(R.id.edit_text_message);
        listView = (ListView) findViewById(R.id.list_view_messages);

        messageObjectList = new ArrayList<>();
        adapter = new MessageAdapter(getApplicationContext(), messageObjectList);
        listView.setAdapter(adapter);

        chatHelper = ChatHelper.getInstance(null);
        chatHelper.registerMessageCallback(new MessageCallback() {

            @Override
            public void gotMessage(Object object) {
                MessageObject m = (MessageObject) object;
                System.out.println("m.message: " + m.message);
                messageObjectList.add(m);
                adapter.notifyDataSetChanged();
            }
        });
    }

    public void buttonClick(View view) {
        switch (view.getId()) {
            case R.id.button_send_message:
                String text = editTextMessage.getText().toString();
                System.out.println(text);
                chatHelper.sendTextMessage(text);
                editTextMessage.setText("");
                break;
            case R.id.button_take_photo:
                break;
            case R.id.button_record_audio:
                break;
        }
    }
}