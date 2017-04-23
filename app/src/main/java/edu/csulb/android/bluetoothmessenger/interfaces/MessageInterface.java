package edu.csulb.android.bluetoothmessenger.interfaces;

import android.net.Uri;

public interface MessageInterface {

    void sendTextMessage(String message);

    void sendImage(Uri data);

    void sendAudio();

}
