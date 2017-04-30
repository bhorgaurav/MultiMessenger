package edu.csulb.android.bluetoothmessenger.activities;

import android.Manifest;
import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.media.MediaRecorder;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.Toast;

import com.github.piasy.rxandroidaudio.AudioRecorder;

import org.json.JSONException;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
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

public class ChatActivity extends AppCompatActivity implements View.OnTouchListener {

    private static final int PICK_IMAGE = 11;
    private static final int MY_PERMISSIONS_REQUEST = 22;
    private PeerDevice device;
    private ChatHelper chatHelper;
    private EditText editTextMessage;
    private List<MessageObject> messageObjectList;
    private ListView listView;
    private MessageAdapter adapter;
    private AudioRecorder mAudioRecorder;
    private File mAudioFile;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat);

        ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.RECORD_AUDIO, Manifest.permission.WRITE_EXTERNAL_STORAGE,
                Manifest.permission.READ_EXTERNAL_STORAGE}, MY_PERMISSIONS_REQUEST);

        Intent intent = getIntent();
        device = (PeerDevice) intent.getSerializableExtra(Constants.DEVICE);

        getSupportActionBar().setTitle(device.name);
        editTextMessage = (EditText) findViewById(R.id.edit_text_message);
        listView = (ListView) findViewById(R.id.list_view_messages);
        ImageButton buttonRecordAudio = (ImageButton) findViewById(R.id.button_record_audio);
        buttonRecordAudio.setOnTouchListener(this);

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
        mAudioRecorder = AudioRecorder.getInstance();
    }

    public void buttonClick(View view) {
        switch (view.getId()) {
            case R.id.button_send_message:
                String text = editTextMessage.getText().toString();
                if (!TextUtils.isEmpty(text)) {
                    chatHelper.sendTextMessage(text.getBytes());
                    editTextMessage.setText("");
                    messageObjectList.add(new MessageObject(text, Constants.TYPE_TEXT, true));
                    adapter.notifyDataSetChanged();
                }
                break;
            case R.id.button_pick_photo:
                Intent intent = new Intent();
                intent.setType("image/*");
                intent.setAction(Intent.ACTION_GET_CONTENT);
                startActivityForResult(Intent.createChooser(intent, "Select Picture"), PICK_IMAGE);
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

    @Override
    public boolean onTouch(View v, MotionEvent event) {
        if (event.getAction() == MotionEvent.ACTION_DOWN) {
            mAudioFile = new File(Environment.getExternalStorageDirectory().getAbsolutePath() + File.separator + "Recording_" + System.nanoTime() + ".aac");
            mAudioRecorder.prepareRecord(MediaRecorder.AudioSource.MIC, MediaRecorder.OutputFormat.AAC_ADTS, MediaRecorder.AudioEncoder.AAC, mAudioFile);
            mAudioRecorder.startRecord();
            Toast t = Toast.makeText(this, "Recording started!", Toast.LENGTH_SHORT);
            t.setGravity(Gravity.TOP, 0, 0);
            t.show();
        } else if (event.getAction() == MotionEvent.ACTION_UP) {
            mAudioRecorder.stopRecord();
            Toast t = Toast.makeText(this, "Recording stopped!", Toast.LENGTH_SHORT);
            t.setGravity(Gravity.TOP, 0, 0);
            t.show();
            try {
                int size = (int) mAudioFile.length();
                byte[] bytes = new byte[size];
                BufferedInputStream buf = new BufferedInputStream(new FileInputStream(mAudioFile));
                buf.read(bytes, 0, bytes.length);
                buf.close();

                chatHelper.sendAudio(bytes);
            } catch (IOException | JSONException e) {
                e.printStackTrace();
            }

            File temp = mAudioFile;
            messageObjectList.add(new MessageObject(temp, Constants.TYPE_AUDIO, true));
            adapter.notifyDataSetChanged();
        }
        return true;
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String permissions[], int[] grantResults) {
        switch (requestCode) {
            case MY_PERMISSIONS_REQUEST:
                // If request is cancelled, the result arrays are empty.
                if (grantResults.length <= 0 || grantResults[0] != PackageManager.PERMISSION_GRANTED) {
                    Toast.makeText(this, "This permission is required", Toast.LENGTH_SHORT).show();
                }
                break;
        }
    }
}