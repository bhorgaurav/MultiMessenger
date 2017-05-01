package edu.csulb.android.bluetoothmessenger.helpers;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.widget.Toast;

import java.io.File;
import java.io.FileOutputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import edu.csulb.android.bluetoothmessenger.BluetoothChatService;
import edu.csulb.android.bluetoothmessenger.Constants;
import edu.csulb.android.bluetoothmessenger.interfaces.Callback;
import edu.csulb.android.bluetoothmessenger.interfaces.HelperInterface;
import edu.csulb.android.bluetoothmessenger.interfaces.MessageCallback;
import edu.csulb.android.bluetoothmessenger.pojos.MessageObject;
import edu.csulb.android.bluetoothmessenger.pojos.PeerDevice;

public class BluetoothHelper implements HelperInterface {

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
                        switch (msg.arg2) {
                            case Constants.TYPE_TEXT:
                                String writeMessage = new String(writeBuf);
                                messageCallback.gotMessage(new MessageObject(writeMessage, Constants.TYPE_TEXT, true));
                                break;
                            case Constants.TYPE_IMAGE:
                                Bitmap bmp = BitmapFactory.decodeByteArray(writeBuf, 0, writeBuf.length);
                                messageCallback.gotMessage(new MessageObject(bmp, Constants.TYPE_IMAGE, true));
                                break;
                            case Constants.TYPE_AUDIO:
//                                messageCallback.gotMessage(new MessageObject(bmp, Constants.TYPE_AUDIO, true));
                                break;
                        }
                        break;
                    case Constants.MESSAGE_READ:
                        byte[] readBuf = (byte[]) msg.obj;
                        switch (msg.arg2) {
                            case Constants.TYPE_TEXT:
                                String writeMessage = new String(readBuf, 0, msg.arg1);
                                messageCallback.gotMessage(new MessageObject(writeMessage, Constants.TYPE_TEXT, false));
                                break;
                            case Constants.TYPE_IMAGE:
                                Bitmap bmp = BitmapFactory.decodeByteArray(readBuf, 0, readBuf.length);
                                messageCallback.gotMessage(new MessageObject(bmp, Constants.TYPE_IMAGE, false));
                                break;
                            case Constants.TYPE_AUDIO:
                                try {
                                    File f = new File(Environment.getExternalStorageDirectory().getAbsolutePath() + File.separator + "Recording_" + System.nanoTime() + ".aac");
                                    if (!f.exists()) {
                                        f.createNewFile();
                                    }
                                    FileOutputStream fos = new FileOutputStream(f);
                                    fos.write(readBuf);
                                    fos.flush();
                                    fos.close();
                                    messageCallback.gotMessage(new MessageObject(f, Constants.TYPE_AUDIO, false));
                                } catch (Exception e) {
                                    e.printStackTrace();
                                }
                                break;
                        }
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
            callback.notSupported();
            return;
        }
        if (!mBluetoothAdapter.isEnabled()) {
            callback.notEnabled();
            return;
        }

        if (mBluetoothAdapter.getScanMode() != BluetoothAdapter.SCAN_MODE_CONNECTABLE_DISCOVERABLE) {
            callback.notDiscoverable();
        }

        Set<BluetoothDevice> newPairedDevices = mBluetoothAdapter.getBondedDevices();
        pairedDevices.clear();
        if (newPairedDevices.size() > 0) {
            for (BluetoothDevice d : newPairedDevices) {
                addDevice(d);
            }
        }

        receiver = new BroadcastReceiver() {
            public void onReceive(Context context, Intent intent) {
                String action = intent.getAction();
                System.out.println("action: " + action);
                if (BluetoothDevice.ACTION_FOUND.equals(action)) {
                    BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
                    addDevice(device);
                }
            }
        };
        context.registerReceiver(receiver, filter);

        chatService = new BluetoothChatService(context, handler);
    }

    public void startDiscovery() {

        mBluetoothAdapter.startDiscovery();

        Handler mHandler = new Handler();
        mHandler.postDelayed(new Runnable() {
            @Override
            public void run() {
                mBluetoothAdapter.stopLeScan(null);
            }
        }, 60000);

        mBluetoothAdapter.startLeScan(new BluetoothAdapter.LeScanCallback() {
            @Override
            public void onLeScan(BluetoothDevice device, int rssi, byte[] scanRecord) {
                addDevice(device);
            }
        });
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
        try {
            mBluetoothAdapter.cancelDiscovery();
            BluetoothDevice device = pairedDevices.get(position);
            device.setPairingConfirmation(true);

            if (chatService.getState() == BluetoothChatService.STATE_NONE) {
                chatService.startAndConnect(device);
            } else {
                chatService.connect(device, false);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
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
        try {
            context.unregisterReceiver(receiver);
        } catch (IllegalArgumentException e) {
        }
        if (chatService != null) {
            chatService.stop();
        }
    }

    @Override
    public void sendTextMessage(byte[] message) {
        if (chatService.getState() != BluetoothChatService.STATE_CONNECTED) {
            Toast.makeText(context, "Not connected to any device.", Toast.LENGTH_SHORT).show();
            return;
        }
        chatService.write(message);
    }

    private void addDevice(BluetoothDevice device) {
        boolean isPresent = false;
        for (PeerDevice d : peerDevices) {
            if (d.deviceAddress.equals(device.getAddress())) {
                isPresent = true;
                break;
            }
        }
        if (!isPresent) {
            String deviceName = device.getName();
            String deviceHardwareAddress = device.getAddress();
            peerDevices.add(new PeerDevice(deviceName, deviceHardwareAddress));
            pairedDevices.add(device);
            callback.peersChanged(peerDevices);
        }
    }
}
