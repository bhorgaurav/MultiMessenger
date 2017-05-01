package edu.csulb.android.bluetoothmessenger.fragments;


import android.bluetooth.BluetoothAdapter;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ListView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.List;

import edu.csulb.android.bluetoothmessenger.Constants;
import edu.csulb.android.bluetoothmessenger.R;
import edu.csulb.android.bluetoothmessenger.activities.ChatActivity;
import edu.csulb.android.bluetoothmessenger.adapters.DeviceAdapter;
import edu.csulb.android.bluetoothmessenger.helpers.ChatHelper;
import edu.csulb.android.bluetoothmessenger.interfaces.Callback;
import edu.csulb.android.bluetoothmessenger.pojos.PeerDevice;

public class BluetoothFragment extends Fragment implements AdapterView.OnItemClickListener {

    private static final int REQUEST_PERMISSION_BT = 22;
    private static final int REQUEST_ENABLE_BT = 23;

    private ChatHelper chatHelper;
    private List<PeerDevice> peerDeviceList = new ArrayList<>();
    private DeviceAdapter adapter;
    private ListView listView;
    private View rootView;
    private Button buttonDiscover;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        rootView = inflater.inflate(R.layout.fragment_bluetooth, container, false);

        buttonDiscover = (Button) rootView.findViewById(R.id.buttonDiscover);
        buttonDiscover.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                chatHelper.startDiscovery();
            }
        });


        adapter = new DeviceAdapter(getContext(), peerDeviceList);
        listView = (ListView) rootView.findViewById(R.id.list_view_devices);
        listView.setAdapter(adapter);
        listView.setOnItemClickListener(this);

        chatHelper = ChatHelper.getInstance(new Callback() {

            @Override
            public void peersChanged(List<?> peerDevices) {
                adapter.refresh((List<PeerDevice>) peerDevices);
            }

            @Override
            public void notSupported() {
                Toast.makeText(getContext(), "Bluetooth is not available", Toast.LENGTH_LONG).show();
            }

            @Override
            public void notEnabled() {
                Intent discoverableIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_DISCOVERABLE);
                discoverableIntent.putExtra(BluetoothAdapter.EXTRA_DISCOVERABLE_DURATION, 300);
                startActivity(discoverableIntent);
            }

            @Override
            public void notDiscoverable() {
                Intent discoverableIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_DISCOVERABLE);
                discoverableIntent.putExtra(BluetoothAdapter.EXTRA_DISCOVERABLE_DURATION, 300);
                startActivity(discoverableIntent);
            }

            @Override
            public void onConnection(boolean connected, String deviceName) {
                if (connected) {
                    PeerDevice device = null;
                    for (PeerDevice dev : peerDeviceList) {
                        if (dev.name.equals(deviceName)) {
                            device = dev;
                            break;
                        }
                    }
                    Toast.makeText(getContext(), "Connected to " + device.name, Toast.LENGTH_SHORT).show();
                    Intent intent = new Intent(getContext(), ChatActivity.class);
                    intent.putExtra(Constants.DEVICE, device);
                    startActivity(intent);
                } else {
                    Toast.makeText(getContext(), "Could not connect.", Toast.LENGTH_SHORT).show();
                }
            }
        });
        chatHelper.init(getContext());

        if (isVisible()) {
            ActionBar ab = ((AppCompatActivity) getActivity()).getSupportActionBar();
            ab.setTitle("Bluetooth Device");
            ab.setSubtitle("ON");
        }

        return rootView;
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        switch (requestCode) {
            case REQUEST_PERMISSION_BT: {
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                } else {
                    Toast.makeText(getContext(), "This permission is required", Toast.LENGTH_SHORT).show();
                }
            }
        }
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        chatHelper.connect(position);
    }

    public void open() {
        rootView.setVisibility(View.VISIBLE);
        chatHelper.init(getContext());
    }

    public void close() {
        rootView.setVisibility(View.GONE);
        chatHelper.close();
    }

}