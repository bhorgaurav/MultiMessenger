package edu.csulb.android.bluetoothmessenger.helpers;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.wifi.WpsInfo;
import android.net.wifi.p2p.WifiP2pConfig;
import android.net.wifi.p2p.WifiP2pDevice;
import android.net.wifi.p2p.WifiP2pDeviceList;
import android.net.wifi.p2p.WifiP2pManager;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import edu.csulb.android.bluetoothmessenger.interfaces.Callback;
import edu.csulb.android.bluetoothmessenger.interfaces.HelperInterface;
import edu.csulb.android.bluetoothmessenger.interfaces.MessageCallback;
import edu.csulb.android.bluetoothmessenger.interfaces.MessageInterface;
import edu.csulb.android.bluetoothmessenger.pojos.PeerDevice;

import static android.os.Looper.getMainLooper;

public class WifiHelper implements HelperInterface, MessageInterface {

    private WifiP2pManager mManager;
    private WifiP2pManager.Channel mChannel;
    private BroadcastReceiver receiver;
    private boolean enabled, connected;
    private IntentFilter intentFilter;
    private Context context;
    private List<WifiP2pDevice> peers = new ArrayList<>();
    private WifiP2pManager.PeerListListener peerListListener;
    private List<PeerDevice> peerDevices;
    private Callback callback;
    private WifiP2pManager.ActionListener actionListener;
    private MessageCallback messageCallback;

    public WifiHelper(Context context, Callback callback) {
        this.context = context;
        this.callback = callback;
    }

    @Override
    public void init(Context context) {

        peerDevices = new ArrayList<>();
        enabled = true;

        mManager = (WifiP2pManager) context.getSystemService(Context.WIFI_P2P_SERVICE);
        mChannel = mManager.initialize(context, getMainLooper(), null);

        intentFilter = new IntentFilter();
        intentFilter.addAction(WifiP2pManager.WIFI_P2P_STATE_CHANGED_ACTION);
        intentFilter.addAction(WifiP2pManager.WIFI_P2P_PEERS_CHANGED_ACTION);
        intentFilter.addAction(WifiP2pManager.WIFI_P2P_CONNECTION_CHANGED_ACTION);
        intentFilter.addAction(WifiP2pManager.WIFI_P2P_THIS_DEVICE_CHANGED_ACTION);

        peerListListener = new WifiP2pManager.PeerListListener() {
            @Override
            public void onPeersAvailable(WifiP2pDeviceList peerList) {
                Collection<WifiP2pDevice> refreshedPeers = peerList.getDeviceList();
                peers.clear();
                peerDevices.clear();
                for (WifiP2pDevice device : refreshedPeers) {
                    peers.add(device);
                    peerDevices.add(new PeerDevice(device.deviceName, device.deviceAddress));
                }
                callback.peersChanged(peerDevices);
            }
        };

        receiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                String action = intent.getAction();
                if (WifiP2pManager.WIFI_P2P_STATE_CHANGED_ACTION.equals(action)) {
                    int state = intent.getIntExtra(WifiP2pManager.EXTRA_WIFI_STATE, -1);
                    enabled = state == WifiP2pManager.WIFI_P2P_STATE_ENABLED;
                } else if (WifiP2pManager.WIFI_P2P_PEERS_CHANGED_ACTION.equals(action)) {
                    mManager.requestPeers(mChannel, peerListListener);
                } else if (WifiP2pManager.WIFI_P2P_CONNECTION_CHANGED_ACTION.equals(action)) {

                    // Connection state changed!  We should probably do something about
                    // that.

                } else if (WifiP2pManager.WIFI_P2P_THIS_DEVICE_CHANGED_ACTION.equals(action)) {
                    intent.getParcelableExtra(WifiP2pManager.EXTRA_WIFI_P2P_DEVICE);
                }
            }
        };

        actionListener = new WifiP2pManager.ActionListener() {
            @Override
            public void onSuccess() {
            }

            @Override
            public void onFailure(int reasonCode) {
                Toast.makeText(WifiHelper.this.context, "Connect failed. Retry.", Toast.LENGTH_SHORT).show();
            }
        };

        context.registerReceiver(receiver, intentFilter);
        mManager.discoverPeers(mChannel, actionListener);
    }

    @Override
    public boolean isAvailable() {
        return true;
    }

    @Override
    public boolean isEnabled() {
        return enabled;
    }

    @Override
    public boolean isConnected() {
        return false;
    }

    @Override
    public void connect(int position) {
        // Picking the first device found on the network.
        WifiP2pDevice device = peers.get(position);

        WifiP2pConfig config = new WifiP2pConfig();
        config.deviceAddress = device.deviceAddress;
        config.wps.setup = WpsInfo.PBC;

        mManager.connect(mChannel, config, new WifiP2pManager.ActionListener() {

            @Override
            public void onSuccess() {
                connected = true;
                callback.onConnection(connected, "");
            }

            @Override
            public void onFailure(int reason) {
                connected = false;
                callback.onConnection(connected, "");
            }
        });
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
        try {
            enabled = false;
            connected = false;
            mManager.clearLocalServices(mChannel, actionListener);
            mManager.clearServiceRequests(mChannel, actionListener);
            context.unregisterReceiver(receiver);
        } catch (IllegalArgumentException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void sendTextMessage(String message) {

    }

    @Override
    public void sendImage() {

    }

    @Override
    public void sendAudio() {

    }
}
