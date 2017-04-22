package edu.csulb.android.bluetoothmessenger.interfaces;

import android.content.Context;

public interface HelperInterface {

    void init(Context applicationContext);

    boolean isAvailable();

    boolean isEnabled();

    boolean isConnected();

    void connect(int position);

    void registerMessageCallback(MessageCallback messageCallback);

    void toggle();

    void close();

}
