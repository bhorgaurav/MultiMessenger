package edu.csulb.android.bluetoothmessenger;

import java.util.UUID;

public interface Constants {

    UUID UUID = new UUID(9944485533L, 33322233L);

    int MESSAGE_STATE_CHANGE = 1;
    int MESSAGE_READ = 2;
    int MESSAGE_WRITE = 3;
    int MESSAGE_DEVICE_NAME = 4;
    int MESSAGE_TOAST = 5;
    int ERROR = 6;
    int CONNECTED = 7;
    int TYPE_TEXT = 11;
    int TYPE_IMAGE = 12;
    int TYPE_AUDIO = 13;

    String TOAST = "toast";
    String DEVICE_NAME = "device_name";
    String DEVICE = "device";

    String MESSAGE_TYPE = "MESSAGE_TYPE";
    String MESSAGE_SIZE = "MESSAGE_SIZE";
}
