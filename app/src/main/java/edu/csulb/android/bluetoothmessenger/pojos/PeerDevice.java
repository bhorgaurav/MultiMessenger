package edu.csulb.android.bluetoothmessenger.pojos;

import java.io.Serializable;

public class PeerDevice implements Serializable {

    public String name, deviceAddress;

    public PeerDevice(String name, String deviceAddress) {
        this.name = name;
        this.deviceAddress = deviceAddress;
    }
}