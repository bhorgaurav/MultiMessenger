package edu.csulb.android.bluetoothmessenger.activities;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.view.View;
import android.widget.EditText;
import android.widget.ListView;

import java.io.IOException;
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

    private static final int PICK_IMAGE = 11;
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
                messageObjectList.add(m);
                adapter.notifyDataSetChanged();
            }
        });
    }

    public void buttonClick(View view) {
        switch (view.getId()) {
            case R.id.button_send_message:
                String text = editTextMessage.getText().toString();
                if (!TextUtils.isEmpty(text)) {
                    chatHelper.sendTextMessage(text.getBytes());
                    editTextMessage.setText("");
                    messageObjectList.add(new MessageObject(text, Constants.TYPE_TEXT, true));
                }
                break;
            case R.id.button_pick_photo:
                Intent intent = new Intent();
                intent.setType("image/*");
                intent.setAction(Intent.ACTION_GET_CONTENT);
                startActivityForResult(Intent.createChooser(intent, "Select Picture"), PICK_IMAGE);
                break;
            case R.id.button_record_audio:
                break;
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == PICK_IMAGE && resultCode == Activity.RESULT_OK) {
            if (data == null) {
                return;
            }
            try {
                Bitmap bitmap = MediaStore.Images.Media.getBitmap(getContentResolver(), data.getData());
                messageObjectList.add(new MessageObject(bitmap, Constants.TYPE_IMAGE, true));
                adapter.notifyDataSetChanged();
                chatHelper.sendImage(bitmap);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}