package edu.csulb.android.bluetoothmessenger.interfaces;

import android.net.wifi.p2p.WifiP2pConfig;
import android.net.wifi.p2p.WifiP2pDevice;

public interface DeviceActionListener {

    void showDetails(WifiP2pDevice device);

    void cancelDisconnect();

    void connect(WifiP2pConfig config);

    void disconnect();
}