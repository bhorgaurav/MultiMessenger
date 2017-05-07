package edu.csulb.android.bluetoothmessenger.interfaces;

import java.util.List;

import edu.csulb.android.bluetoothmessenger.pojos.PeerDevice;

public interface Callback {

    void peersChanged(List<PeerDevice> peerDeviceList);

    void notSupported();

    void notEnabled();

    void notDiscoverable();

    void onConnection(boolean connected, String deviceName);
}
