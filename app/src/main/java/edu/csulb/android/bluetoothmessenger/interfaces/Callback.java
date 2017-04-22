package edu.csulb.android.bluetoothmessenger.interfaces;

import java.util.List;

public interface Callback {

    void peersChanged(List<?> peerDeviceList);

    void notEnabled();

    void notDiscoverable();

    void onConnection(boolean connected, String deviceName);

}
