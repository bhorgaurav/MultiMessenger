package edu.csulb.android.bluetoothmessenger.pojos;

public class MessageObject {

    public String message;
    public int type;
    public boolean isSender;

    public MessageObject(String message, int type, boolean isSender) {
        this.message = message;
        this.type = type;
        this.isSender = isSender;
    }
}
