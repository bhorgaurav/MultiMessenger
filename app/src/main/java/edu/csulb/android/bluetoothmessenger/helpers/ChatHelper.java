package edu.csulb.android.bluetoothmessenger.helpers;

import android.content.Context;
import android.net.Uri;

import edu.csulb.android.bluetoothmessenger.interfaces.Callback;
import edu.csulb.android.bluetoothmessenger.interfaces.HelperInterface;
import edu.csulb.android.bluetoothmessenger.interfaces.MessageCallback;
import edu.csulb.android.bluetoothmessenger.interfaces.MessageInterface;

public class ChatHelper implements HelperInterface, MessageInterface {

    private static ChatHelper chatHelper;
    private static Callback callback;

    private BluetoothHelper bluetoothHelper;
    private WifiHelper wifiHelper;
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
    public void sendTextMessage(String message) {
        System.out.println("bluetoothHelper.isConnected(): " + bluetoothHelper.isConnected());
        if (bluetoothHelper.isConnected()) {
            bluetoothHelper.sendTextMessage(message);
        } else if (wifiHelper.isConnected()) {
            wifiHelper.sendTextMessage(message);
        }
    }

    @Override
    public void sendImage(Uri data) {
        if (bluetoothHelper.isEnabled()) {
            bluetoothHelper.sendImage(data);
        } else if (wifiHelper.isEnabled()) {
            wifiHelper.sendImage(data);
        }
    }

    @Override
    public void sendAudio() {
        if (bluetoothHelper.isEnabled()) {
            bluetoothHelper.sendAudio();
        } else if (wifiHelper.isEnabled()) {
            wifiHelper.sendAudio();
        }
    }

    @Override
    public void registerMessageCallback(MessageCallback messageCallback) {
        bluetoothHelper.registerMessageCallback(messageCallback);
        wifiHelper.registerMessageCallback(messageCallback);
    }
}