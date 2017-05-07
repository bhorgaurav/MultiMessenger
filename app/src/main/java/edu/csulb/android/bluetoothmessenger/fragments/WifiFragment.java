package edu.csulb.android.bluetoothmessenger.fragments;

import android.app.ProgressDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.wifi.WifiConfiguration;
import android.net.wifi.WifiManager;
import android.net.wifi.WpsInfo;
import android.net.wifi.p2p.WifiP2pConfig;
import android.net.wifi.p2p.WifiP2pDevice;
import android.net.wifi.p2p.WifiP2pDeviceList;
import android.net.wifi.p2p.WifiP2pInfo;
import android.net.wifi.p2p.WifiP2pManager;
import android.net.wifi.p2p.WifiP2pManager.PeerListListener;
import android.os.Bundle;
import android.support.v4.app.ListFragment;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ListView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.List;

import edu.csulb.android.bluetoothmessenger.Constants;
import edu.csulb.android.bluetoothmessenger.R;
import edu.csulb.android.bluetoothmessenger.Utils;
import edu.csulb.android.bluetoothmessenger.activities.ChatActivity;
import edu.csulb.android.bluetoothmessenger.adapters.WiFiPeerListAdapter;
import edu.csulb.android.bluetoothmessenger.config.Configuration;
import edu.csulb.android.bluetoothmessenger.interfaces.DeviceActionListener;
import edu.csulb.android.bluetoothmessenger.router.Packet;
import edu.csulb.android.bluetoothmessenger.router.Sender;
import edu.csulb.android.bluetoothmessenger.wifi.WiFiBroadcastReceiver;
import edu.csulb.android.bluetoothmessenger.wifi.WiFiDirectBroadcastReceiver;

import static android.os.Looper.getMainLooper;

public class WifiFragment extends ListFragment implements PeerListListener, WifiP2pManager.ConnectionInfoListener, WifiP2pManager.ChannelListener, DeviceActionListener {

    public static final String TAG = "wifidirectdemo";
    private final IntentFilter intentFilter = new IntentFilter();
    private final IntentFilter wifiIntentFilter = new IntentFilter();
    private List<WifiP2pDevice> peers = new ArrayList<>();
    private ProgressDialog progressDialog = null;
    private View mContentView = null;
    private WifiP2pDevice device;
    private WifiP2pManager manager;
    private boolean isWifiP2pEnabled = false;
    private boolean retryChannel = false;
    private WifiP2pManager.Channel channel;
    private BroadcastReceiver receiver = null;
    private Context context;
    private WifiManager wifiManager;
    private WiFiBroadcastReceiver receiverWifi;
    private boolean isWifiConnected;

    private Button buttonDisconnect, buttonDiscover;

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        this.setListAdapter(new WiFiPeerListAdapter(getActivity(), R.layout.item_device, peers));
    }

    public void setIsWifiP2pEnabled(boolean isWifiP2pEnabled) {
        this.isWifiP2pEnabled = isWifiP2pEnabled;
    }

    /**
     * Inflate the devices list view
     */
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        mContentView = inflater.inflate(R.layout.device_list, null);

        buttonDisconnect = (Button) mContentView.findViewById(R.id.buttonDisconnect);
        buttonDisconnect.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                disconnect();
            }
        });

        buttonDiscover = (Button) mContentView.findViewById(R.id.buttonDiscover);
        buttonDiscover.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startDiscovery();
            }
        });

        context = getActivity().getApplicationContext();

        intentFilter.addAction(WifiP2pManager.WIFI_P2P_STATE_CHANGED_ACTION);
        intentFilter.addAction(WifiP2pManager.WIFI_P2P_PEERS_CHANGED_ACTION);
        intentFilter.addAction(WifiP2pManager.WIFI_P2P_CONNECTION_CHANGED_ACTION);
        intentFilter.addAction(WifiP2pManager.WIFI_P2P_THIS_DEVICE_CHANGED_ACTION);

        wifiManager = (WifiManager) context.getApplicationContext().getSystemService(Context.WIFI_SERVICE);
        wifiManager.setWifiEnabled(true);
        manager = (WifiP2pManager) context.getSystemService(Context.WIFI_P2P_SERVICE);
        channel = manager.initialize(context, getMainLooper(), null);

        if (Configuration.isDeviceBridgingEnabled) {
            // Check for wifi is disabled
            if (!wifiManager.isWifiEnabled()) {
                // If wifi disabled then enable it
                Toast.makeText(context, "wifi is disabled..making it enabled", Toast.LENGTH_LONG).show();
                wifiManager.setWifiEnabled(true);
            }

            // wifi scaned value broadcast receiver
            receiverWifi = new WiFiBroadcastReceiver(wifiManager, this, this.isWifiConnected);

            // Register broadcast receiver
            // Broacast receiver will automatically call when number of wifi
            // connections changed
            wifiIntentFilter.addAction(WifiManager.SCAN_RESULTS_AVAILABLE_ACTION);
            wifiIntentFilter.addAction(WifiManager.NETWORK_STATE_CHANGED_ACTION);
            wifiIntentFilter.addAction(WifiManager.WIFI_STATE_CHANGED_ACTION);

            context.registerReceiver(receiverWifi, wifiIntentFilter);

			/*
             * This shouldn't be hard coded, but for our purposes we wanted to
			 * demonstrate bridging.
			 */
//            this.connectToAccessPoint("DIRECT-Sq-Android_ca89", "c5umx0mw");
            // connectToAccessPoint(String ssid, String passphrase)
        }
        startDiscovery();
        return mContentView;
    }

    @Override
    public void onListItemClick(ListView l, View v, int position, long id) {

        WifiP2pDevice device = (WifiP2pDevice) getListAdapter().getItem(position);
        if (WifiP2pDevice.CONNECTED == device.status || WifiP2pDevice.INVITED == device.status) {
            Intent i = new Intent(context, ChatActivity.class);
            i.putExtra(Constants.DEVICE, device);
            startActivity(i);
        } else {
            WifiP2pConfig config = new WifiP2pConfig();
            config.deviceAddress = device.deviceAddress;
            config.wps.setup = WpsInfo.PBC;
            if (progressDialog != null && progressDialog.isShowing()) {
                progressDialog.dismiss();
            }
            progressDialog = ProgressDialog.show(getActivity(), "Press back to cancel", "Connecting to :"
                    + device.deviceAddress, true, true);
            connect(config);
        }
    }

    @Override
    public void onConnectionInfoAvailable(WifiP2pInfo wifiP2pInfo) {
        if (!wifiP2pInfo.isGroupOwner) {
            Sender.queuePacket(new Packet(Packet.TYPE.HELLO, new byte[0], null, WiFiDirectBroadcastReceiver.MAC));
        }
    }

    /**
     * Update UI for this device.
     *
     * @param device WifiP2pDevice object
     */
    public void updateThisDevice(WifiP2pDevice device) {
        this.device = device;
        if (isVisible()) {
            ActionBar ab = ((AppCompatActivity) getActivity()).getSupportActionBar();
            ab.setTitle(device.deviceName);
            ab.setSubtitle(Utils.getDeviceStatus(device.status));
        }
    }

    public void startDiscovery() {
        if (!isWifiP2pEnabled) {
            // If p2p not enabled try to connect as a legacy device
            wifiManager.startScan();
        }

        manager.discoverPeers(channel, new WifiP2pManager.ActionListener() {

            @Override
            public void onSuccess() {
            }

            @Override
            public void onFailure(int reasonCode) {
            }
        });
    }

    public void connectToAccessPoint(String ssid, String passphrase) {

        Log.d(TAG, "Trying to connect to AP : (" + ssid + "," + passphrase + ")");

        WifiConfiguration wc = new WifiConfiguration();
        wc.SSID = "\"" + ssid + "\"";
        wc.preSharedKey = "\"" + passphrase + "\""; // "\""+passphrase+"\"";
        wc.status = WifiConfiguration.Status.ENABLED;
        wc.allowedGroupCiphers.set(WifiConfiguration.GroupCipher.TKIP);
        wc.allowedGroupCiphers.set(WifiConfiguration.GroupCipher.CCMP);
        wc.allowedKeyManagement.set(WifiConfiguration.KeyMgmt.WPA_PSK);
        wc.allowedPairwiseCiphers.set(WifiConfiguration.PairwiseCipher.TKIP);
        wc.allowedPairwiseCiphers.set(WifiConfiguration.PairwiseCipher.CCMP);
        wc.allowedProtocols.set(WifiConfiguration.Protocol.RSN);
        // connect to and enable the connection
        int netId = wifiManager.addNetwork(wc);
        wifiManager.enableNetwork(netId, true);
        wifiManager.setWifiEnabled(true);

        Log.d(TAG, "Connected? ip = " + wifiManager.getConnectionInfo().getIpAddress());
        Log.d(TAG, "Connected? bssid = " + wifiManager.getConnectionInfo().getBSSID());
        Log.d(TAG, "Connected? ssid = " + wifiManager.getConnectionInfo().getSSID());

        if (wifiManager.getConnectionInfo().getIpAddress() != 0) {
            this.isWifiConnected = true;
        }
    }

    /**
     * Callback for async peer searching
     */
    @Override
    public void onPeersAvailable(WifiP2pDeviceList peerList) {
        if (progressDialog != null && progressDialog.isShowing()) {
            progressDialog.dismiss();
        }
        peers.clear();
        peers.addAll(peerList.getDeviceList());
        ((WiFiPeerListAdapter) getListAdapter()).notifyDataSetChanged();
        if (peers.size() == 0) {
            Log.d(TAG, "No devices found");
            return;
        }
    }

    @Override
    public void onChannelDisconnected() {
        if (manager != null && !retryChannel) {
            Toast.makeText(context, "Channel lost. Trying again", Toast.LENGTH_LONG).show();
            retryChannel = true;
            manager.initialize(context, getMainLooper(), this);
        } else {
            Toast.makeText(context, "Severe! Channel is probably lost premanently. Try Disable/Re-Enable P2P.", Toast.LENGTH_LONG).show();
        }
    }

    @Override
    public void showDetails(WifiP2pDevice device) {
    }

    @Override
    public void cancelDisconnect() {
        if (manager != null) {
            if (device == null || device.status == WifiP2pDevice.CONNECTED) {
                disconnect();
            } else if (device.status == WifiP2pDevice.AVAILABLE || device.status == WifiP2pDevice.INVITED) {

                manager.cancelConnect(channel, new WifiP2pManager.ActionListener() {

                    @Override
                    public void onSuccess() {
                        Toast.makeText(context, "Aborting connection", Toast.LENGTH_SHORT).show();
                    }

                    @Override
                    public void onFailure(int reasonCode) {
                        Toast.makeText(context, "Connect abort request failed. Reason Code: " + reasonCode, Toast.LENGTH_SHORT).show();
                    }
                });
            }
        }
    }

    @Override
    public void connect(WifiP2pConfig config) {
        manager.connect(channel, config, new WifiP2pManager.ActionListener() {

            @Override
            public void onSuccess() {
                Intent i = new Intent(context, ChatActivity.class);
                startActivity(i);
            }

            @Override
            public void onFailure(int reason) {
                Toast.makeText(context, "Connect failed. Retry.", Toast.LENGTH_SHORT).show();
            }
        });
    }

    @Override
    public void disconnect() {
        // TODO: again here it should also include the other wifi hotspot thing
        manager.removeGroup(channel, new WifiP2pManager.ActionListener() {

            @Override
            public void onFailure(int reasonCode) {
            }

            @Override
            public void onSuccess() {
            }
        });
    }

    @Override
    public void onResume() {
        super.onResume();
        receiver = new WiFiDirectBroadcastReceiver(manager, channel, this);
        context.registerReceiver(receiver, intentFilter);
    }

    @Override
    public void onPause() {
        super.onPause();
        context.unregisterReceiver(receiver);
    }

    public void open() {
        mContentView.setVisibility(View.VISIBLE);
        startDiscovery();
    }

    public void close() {
        mContentView.setVisibility(View.GONE);
        disconnect();
        manager.cancelConnect(channel, null);
        manager.stopPeerDiscovery(channel, null);
    }
}