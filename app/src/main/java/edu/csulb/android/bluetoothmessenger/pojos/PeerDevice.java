package edu.csulb.android.bluetoothmessenger.pojos;

import java.io.Serializable;

public class PeerDevice implements Serializable {

    public String name, description;

    public PeerDevice(String name, String description) {
        this.name = name;
        this.description = description;
    }
}