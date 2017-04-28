package edu.csulb.android.bluetoothmessenger.adapters;

import android.content.Context;
import android.graphics.Bitmap;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.github.piasy.rxandroidaudio.PlayConfig;
import com.github.piasy.rxandroidaudio.RxAudioPlayer;

import java.io.File;
import java.util.List;

import edu.csulb.android.bluetoothmessenger.Constants;
import edu.csulb.android.bluetoothmessenger.R;
import edu.csulb.android.bluetoothmessenger.pojos.MessageObject;
import io.reactivex.Observer;
import io.reactivex.disposables.Disposable;
import io.reactivex.schedulers.Schedulers;

public class MessageAdapter extends ArrayAdapter<MessageObject> {

    private List<MessageObject> objects;
    private RxAudioPlayer mRxAudioPlayer = RxAudioPlayer.getInstance();

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
        if (m.isSender) {
            h.linearLayout.setGravity(Gravity.END);
        } else {
            h.linearLayout.setGravity(Gravity.START);
        }

        switch (m.type) {
            case Constants.TYPE_TEXT:
                h.tvMessage.setVisibility(View.VISIBLE);
                h.imageViewPhoto.setVisibility(View.GONE);
                h.audioView.setVisibility(View.GONE);
                h.tvMessage.setText((String) m.message);
                break;
            case Constants.TYPE_IMAGE:
                h.tvMessage.setVisibility(View.GONE);
                h.imageViewPhoto.setVisibility(View.VISIBLE);
                h.audioView.setVisibility(View.GONE);

                h.imageViewPhoto.setImageBitmap((Bitmap) m.message);
                break;
            case Constants.TYPE_AUDIO:
                h.tvMessage.setVisibility(View.GONE);
                h.imageViewPhoto.setVisibility(View.GONE);

                h.audioView.setVisibility(View.VISIBLE);
                h.audioView.setTag(m.message);
                h.audioView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        mRxAudioPlayer.play(PlayConfig.file((File) v.getTag()).looping(false).build())
                                .subscribeOn(Schedulers.io())
                                .subscribe(new Observer<Boolean>() {
                                    @Override
                                    public void onSubscribe(final Disposable disposable) {
                                    }

                                    @Override
                                    public void onNext(final Boolean aBoolean) {
                                    }

                                    @Override
                                    public void onError(final Throwable throwable) {
                                    }

                                    @Override
                                    public void onComplete() {
                                    }
                                });
                        Toast.makeText(getContext(), "Audio started!", Toast.LENGTH_SHORT).show();
                    }
                });
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