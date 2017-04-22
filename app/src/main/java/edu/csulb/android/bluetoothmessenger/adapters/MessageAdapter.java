package edu.csulb.android.bluetoothmessenger.adapters;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import java.util.List;

import edu.csulb.android.bluetoothmessenger.Constants;
import edu.csulb.android.bluetoothmessenger.R;
import edu.csulb.android.bluetoothmessenger.pojos.MessageObject;

public class MessageAdapter extends ArrayAdapter<MessageObject> {

    private List<MessageObject> objects;

    public MessageAdapter(@NonNull Context context, @NonNull List<MessageObject> objects) {
        super(context, 0, objects);
        this.objects = objects;
    }

    @NonNull
    @Override
    public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
        Holder h;
        if (null == convertView) {
            convertView = LayoutInflater.from(getContext()).inflate(R.layout.item_message, parent, false);
            h = new Holder();
            h.tvMessage = (TextView) convertView.findViewById(R.id.text_view_message);
            h.imageViewPhoto = (ImageView) convertView.findViewById(R.id.image_view_photo);
            h.audioView = convertView.findViewById(R.id.audio_view);
            h.linearLayout = (LinearLayout) convertView.findViewById(R.id.message_container);
            convertView.setTag(h);
        } else {
            h = (Holder) convertView.getTag();
        }

        MessageObject m = objects.get(position);
        ViewGroup.LayoutParams params = h.linearLayout.getLayoutParams();
        if (m.isSender) {
            m.message = "You: " + m.message;
        } else {
            m.message = "Other: " + m.message;
        }

        switch (m.type) {
            case Constants.TYPE_TEXT:
                h.tvMessage.setVisibility(View.VISIBLE);
                h.imageViewPhoto.setVisibility(View.GONE);
                h.audioView.setVisibility(View.GONE);

                h.tvMessage.setText(m.message);
                break;
            case Constants.TYPE_IMAGE:
                h.tvMessage.setVisibility(View.GONE);
                h.imageViewPhoto.setVisibility(View.VISIBLE);
                h.audioView.setVisibility(View.GONE);
                break;
            case Constants.TYPE_AUDIO:
                h.tvMessage.setVisibility(View.GONE);
                h.imageViewPhoto.setVisibility(View.GONE);
                h.audioView.setVisibility(View.VISIBLE);
                break;
        }
        return convertView;
    }

    private class Holder {
        public TextView tvMessage;
        public ImageView imageViewPhoto;
        public View audioView;
        public LinearLayout linearLayout;
    }
}
