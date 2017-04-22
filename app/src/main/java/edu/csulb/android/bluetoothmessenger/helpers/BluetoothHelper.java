package edu.csulb.android.bluetoothmessenger.helpers;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Handler;
import android.os.Message;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import edu.csulb.android.bluetoothmessenger.BluetoothChatService;
import edu.csulb.android.bluetoothmessenger.Constants;
import edu.csulb.android.bluetoothmessenger.interfaces.Callback;
import edu.csulb.android.bluetoothmessenger.interfaces.HelperInterface;
import edu.csulb.android.bluetoothmessenger.interfaces.MessageCallback;
import edu.csulb.android.bluetoothmessenger.interfaces.MessageInterface;
import edu.csulb.android.bluetoothmessenger.pojos.MessageObject;
import edu.csulb.android.bluetoothmessenger.pojos.PeerDevice;

public class BluetoothHelper implements HelperInterface, MessageInterface {

    private Context context;
    private Callback callback;
    private BluetoothAdapter mBluetoothAdapter;
    private boolean enabled, connected;
    private BroadcastReceiver receiver;
    private IntentFilter filter;
    private List<PeerDevice> peerDevices;
    private List<BluetoothDevice> pairedDevices;
    private Handler handler;
    private BluetoothChatService chatService;
    private MessageCallback messageCallback;

    public BluetoothHelper(Context context, Callback callback) {
        this.context = context;
        this.callback = callback;
    }

    @Override
    public void init(final Context context) {

        enabled = true;
        peerDevices = new ArrayList<>();
        pairedDevices = new ArrayList<>();
        handler = new Handler() {

            @Override
            public void handleMessage(Message msg) {
                switch (msg.what) {
                    case Constants.MESSAGE_STATE_CHANGE:
                        switch (msg.arg1) {
                            case BluetoothChatService.STATE_CONNECTED:
                                connected = true;
                                String deviceName = msg.getData().getString(Constants.DEVICE_NAME);
                                callback.onConnection(connected, deviceName);
                                break;
                            case BluetoothChatService.STATE_CONNECTING:
                                break;
                            case BluetoothChatService.STATE_LISTEN:
                            case BluetoothChatService.STATE_NONE:
                                break;
                        }
                        break;
                    case Constants.MESSAGE_WRITE:
                        byte[] writeBuf = (byte[]) msg.obj;
                        String writeMessage = new String(writeBuf);
                        messageCallback.gotMessage(new MessageObject(writeMessage, Constants.TYPE_TEXT, true));
                        System.out.println("writeMessage: " + writeMessage);
                        break;
                    case Constants.MESSAGE_READ:
                        byte[] readBuf = (byte[]) msg.obj;
                        String readMessage = new String(readBuf, 0, msg.arg1);
                        messageCallback.gotMessage(new MessageObject(readMessage, Constants.TYPE_TEXT, false));
                        System.out.println("readMessage: " + readMessage);
                        break;
                    case Constants.MESSAGE_DEVICE_NAME:
                        connected = true;
                        String deviceName = msg.getData().getString(Constants.DEVICE_NAME);
                        callback.onConnection(connected, deviceName);
                        break;
                    case Constants.MESSAGE_TOAST:
//                        callback.gotMessage(msg.getData());
                        break;
                }
            }
        };

        filter = new IntentFilter(BluetoothDevice.ACTION_FOUND);
        filter.addAction(BluetoothAdapter.ACTION_DISCOVERY_STARTED);
        filter.addAction(BluetoothAdapter.ACTION_DISCOVERY_FINISHED);
        mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();

        if (mBluetoothAdapter == null) {
            Toast.makeText(context, "Bluetooth is not available", Toast.LENGTH_LONG).show();
            return;
        }
        if (!mBluetoothAdapter.isEnabled()) {
            callback.notEnabled();
        }
        if (mBluetoothAdapter.getScanMode() != BluetoothAdapter.SCAN_MODE_CONNECTABLE_DISCOVERABLE) {
            callback.notDiscoverable();
        }

        receiver = new BroadcastReceiver() {
            public void onReceive(Context context, Intent intent) {
                String action = intent.getAction();
                if (BluetoothDevice.ACTION_FOUND.equals(action)) {
                    BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
                    addDevice(device);
                }
            }
        };

        Set<BluetoothDevice> newPairedDevices = mBluetoothAdapter.getBondedDevices();
        pairedDevices.clear();
        if (newPairedDevices.size() > 0) {
            for (BluetoothDevice d : newPairedDevices) {
                addDevice(d);
            }
        }
        context.registerReceiver(receiver, filter);

        chatService = new BluetoothChatService(context, handler);
    }

    @Override
    public boolean isAvailable() {
        return false;
    }

    @Override
    public boolean isEnabled() {
        return enabled;
    }

    @Override
    public boolean isConnected() {
        return connected;
    }

    @Override
    public void connect(int position) {
        if (chatService.getState() == BluetoothChatService.STATE_NONE) {
            chatService.start();
        }
        BluetoothDevice device = pairedDevices.get(position);
        chatService.connect(device, true);
    }

    @Override
    public void registerMessageCallback(MessageCallback messageCallback) {
        this.messageCallback = messageCallback;
    }

    @Override
    public void toggle() {
        if (isEnabled()) {
            close();
        } else {
            init(context);
        }
    }

    @Override
    public void close() {
        enabled = false;
        connected = false;
        context.unregisterReceiver(receiver);
        chatService.stop();
    }

    @Override
    public void sendTextMessage(String message) {
        if (chatService.getState() != BluetoothChatService.STATE_CONNECTED) {
            Toast.makeText(context, "Not connected to any device.", Toast.LENGTH_SHORT).show();
            return;
        }
        System.out.println("message.length(): " + message.length());
        if (message.length() > 0) {
            byte[] send = message.getBytes();
            chatService.write(send);
        }
    }

    @Override
    public void sendImage() {
    }

    @Override
    public void sendAudio() {
    }

    private void addDevice(BluetoothDevice device) {
        String deviceName = device.getName();
        String deviceHardwareAddress = device.getAddress();
        peerDevices.add(new PeerDevice(deviceName, deviceHardwareAddress));
        pairedDevices.add(device);
        callback.peersChanged(peerDevices);
    }
}
