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
import edu.csulb.android.bluetoothmessenger.router.AllEncompasingP2PClient;
import edu.csulb.android.bluetoothmessenger.router.MeshNetworkManager;
import edu.csulb.android.bluetoothmessenger.router.Packet;
import edu.csulb.android.bluetoothmessenger.router.Sender;
import edu.csulb.android.bluetoothmessenger.wifi.WiFiDirectBroadcastReceiver;

public class ChatHelper implements HelperInterface {

    private static ChatHelper chatHelper;
    private static Callback callback;

    private BluetoothHelper bluetoothHelper;
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

        this.context = context;

        bluetoothHelper = new BluetoothHelper(context, callback);
        bluetoothHelper.init(context);
    }

    @Override
    public boolean isAvailable() {
        return bluetoothHelper.isAvailable();
    }

    @Override
    public boolean isEnabled() {
        return bluetoothHelper.isEnabled();
    }

    @Override
    public boolean isConnected() {
        return bluetoothHelper.isConnected();
    }

    @Override
    public void connect(int position) {
        if (bluetoothHelper.isEnabled()) {
            bluetoothHelper.connect(position);
        }
    }

    @Override
    public void toggle() {
        bluetoothHelper.toggle();
    }

    @Override
    public void close() {
        bluetoothHelper.close();
    }

    @Override
    public void sendTextMessage(byte[] b) {
        if (bluetoothHelper.isConnected()) {
            bluetoothHelper.sendTextMessage(b);
        }
        try {
            for (AllEncompasingP2PClient c : MeshNetworkManager.routingTable.values()) {
                if (c.getMac().equals(MeshNetworkManager.getSelf().getMac()))
                    continue;
                Sender.queuePacket(new Packet(Packet.TYPE.MESSAGE, b, c.getMac(), WiFiDirectBroadcastReceiver.MAC));
            }
        } catch (Exception e) {
            e.printStackTrace();
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

    public void sendAudio(byte[] bytes) throws JSONException {
        JSONObject json = new JSONObject();
        json.put(Constants.MESSAGE_TYPE, Constants.TYPE_AUDIO);
        json.put(Constants.MESSAGE_SIZE, bytes.length);

        // Send length and then the original message
        sendTextMessage(json.toString().getBytes());
        sendTextMessage(bytes);
    }

    @Override
    public void registerMessageCallback(MessageCallback messageCallback) {
        bluetoothHelper.registerMessageCallback(messageCallback);
    }

    public void startDiscovery() {
        bluetoothHelper.startDiscovery();
    }
}