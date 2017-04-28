package edu.csulb.android.bluetoothmessenger.helpers;

import android.content.Context;
import android.graphics.Bitmap;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.ByteArrayOutputStream;

import edu.csulb.android.bluetoothmessenger.Constants;
import edu.csulb.android.bluetoothmessenger.interfaces.Callback;
import edu.csulb.android.bluetoothmessenger.interfaces.HelperInterface;
import edu.csulb.android.bluetoothmessenger.interfaces.MessageCallback;
import edu.csulb.android.bluetoothmessenger.interfaces.MessageInterface;

public class ChatHelper implements HelperInterface, MessageInterface {

    private static ChatHelper chatHelper;
    private static Callback callback;

    private BluetoothHelper bluetoothHelper;
    private WifiHelper wifiHelper;
    private Context context;
    private MessageCallback messageCallback;

    private ChatHelper(Callback callback) {
        ChatHelper.callback = callback;
    }

    public static ChatHelper getInstance(Callback callback) {
        if (chatHelper == null) {
            chatHelper = new ChatHelper(callback);
        }
        return chatHelper;
    }

    @Override
    public void init(Context context) {

        bluetoothHelper = new BluetoothHelper(context, callback);
        wifiHelper = new WifiHelper(context, callback);
        this.context = context;
        // Init wifi first
        wifiHelper.init(context);
    }

    @Override
    public boolean isAvailable() {
        return bluetoothHelper.isAvailable() || wifiHelper.isAvailable();
    }

    @Override
    public boolean isEnabled() {
        return bluetoothHelper.isEnabled() || wifiHelper.isEnabled();
    }

    @Override
    public boolean isConnected() {
        return bluetoothHelper.isConnected() || wifiHelper.isConnected();
    }

    @Override
    public void connect(int position) {
        if (bluetoothHelper.isEnabled()) {
            bluetoothHelper.connect(position);
        }
        if (wifiHelper.isEnabled()) {
            wifiHelper.connect(position);
        }
    }

    @Override
    public void toggle() {
        bluetoothHelper.toggle();
        wifiHelper.toggle();
    }

    @Override
    public void close() {
        bluetoothHelper.close();
        wifiHelper.close();
    }

    @Override
    public void sendTextMessage(byte[] b) {
        if (bluetoothHelper.isConnected()) {
            bluetoothHelper.sendTextMessage(b);
        } else if (wifiHelper.isConnected()) {
            wifiHelper.sendTextMessage(b);
        }
    }

    public void sendImage(Bitmap bitmap) {
        try {
            // Convert image to bytes
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            bitmap.compress(Bitmap.CompressFormat.JPEG, 50, baos);
            byte[] b = baos.toByteArray();

            JSONObject json = new JSONObject();
            json.put(Constants.MESSAGE_TYPE, Constants.TYPE_IMAGE);
            json.put(Constants.MESSAGE_SIZE, b.length);

            // Send length and then the original message
            sendTextMessage(json.toString().getBytes());
            sendTextMessage(b);
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    public void sendAudio() {

    }

    @Override
    public void registerMessageCallback(MessageCallback messageCallback) {
        bluetoothHelper.registerMessageCallback(messageCallback);
        wifiHelper.registerMessageCallback(messageCallback);
    }
}