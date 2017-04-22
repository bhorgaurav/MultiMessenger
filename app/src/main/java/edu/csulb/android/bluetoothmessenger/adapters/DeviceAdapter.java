package edu.csulb.android.bluetoothmessenger.adapters;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import java.util.List;

import edu.csulb.android.bluetoothmessenger.R;
import edu.csulb.android.bluetoothmessenger.pojos.PeerDevice;

public class DeviceAdapter extends ArrayAdapter<PeerDevice> {

    private List<PeerDevice> objects;

    public DeviceAdapter(@NonNull Context context, @NonNull List<PeerDevice> objects) {
        super(context, 0, objects);
        this.objects = objects;
    }

    @NonNull
    @Override
    public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {

        Holder holder;
        if (convertView == null) {
            convertView = LayoutInflater.from(getContext()).inflate(R.layout.item_device, parent, false);

            holder = new Holder();
            holder.name = (TextView) convertView.findViewById(R.id.text_view_device_name);
            holder.description = (TextView) convertView.findViewById(R.id.text_view_device_description);

            convertView.setTag(holder);
        } else {
            holder = (Holder) convertView.getTag();
        }

        PeerDevice peerDevice = objects.get(position);
        holder.name.setText(peerDevice.name);
        holder.description.setText(peerDevice.description);
        return convertView;
    }

    public void refresh(List<PeerDevice> peerDevices) {
        objects.clear();
        if (peerDevices != null) {
            objects.addAll(peerDevices);
        }
        notifyDataSetChanged();
    }

    private class Holder {
        TextView name, description;
    }
}
