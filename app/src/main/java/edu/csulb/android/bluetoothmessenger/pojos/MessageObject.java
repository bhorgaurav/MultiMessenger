package edu.csulb.android.bluetoothmessenger.pojos;

public class MessageObject {

    public Object message;
    public int type;
    public boolean isSender;

    public MessageObject(Object message, int type, boolean isSender) {
        this.message = message;
        this.type = type;
        this.isSender = isSender;
    }
}
