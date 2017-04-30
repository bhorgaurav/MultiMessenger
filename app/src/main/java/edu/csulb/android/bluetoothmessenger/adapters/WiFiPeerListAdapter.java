package edu.csulb.android.bluetoothmessenger.adapters;

import android.content.Context;
import android.net.wifi.p2p.WifiP2pDevice;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import java.util.List;

import edu.csulb.android.bluetoothmessenger.R;
import edu.csulb.android.bluetoothmessenger.Utils;

public class WiFiPeerListAdapter extends ArrayAdapter<WifiP2pDevice> {

    private List<WifiP2pDevice> items;

    /**
     * @param context
     * @param textViewResourceId
     * @param objects
     */
    public WiFiPeerListAdapter(Context context, int textViewResourceId, List<WifiP2pDevice> objects) {
        super(context, textViewResourceId, objects);
        items = objects;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        View v = convertView;
        if (v == null) {
            LayoutInflater vi = (LayoutInflater) getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            v = vi.inflate(R.layout.item_device, null);
        }
        WifiP2pDevice device = items.get(position);
        if (device != null) {
            TextView top = (TextView) v.findViewById(R.id.text_view_device_name);
            TextView bottom = (TextView) v.findViewById(R.id.text_view_device_description);
            if (top != null) {
                top.setText(device.deviceName);
            }
            if (bottom != null) {
                bottom.setText(Utils.getDeviceStatus(device.status));
            }
        }
        return v;
    }
}