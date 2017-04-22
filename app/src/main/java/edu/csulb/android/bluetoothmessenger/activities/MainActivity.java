package edu.csulb.android.bluetoothmessenger.activities;

import android.Manifest;
import android.bluetooth.BluetoothAdapter;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.support.v4.app.ActivityCompat;
import android.support.v7.widget.SwitchCompat;
import android.view.Menu;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.List;

import edu.csulb.android.bluetoothmessenger.Constants;
import edu.csulb.android.bluetoothmessenger.R;
import edu.csulb.android.bluetoothmessenger.adapters.DeviceAdapter;
import edu.csulb.android.bluetoothmessenger.helpers.ChatHelper;
import edu.csulb.android.bluetoothmessenger.interfaces.Callback;
import edu.csulb.android.bluetoothmessenger.pojos.PeerDevice;

public class MainActivity extends SuperActivity implements View.OnClickListener, AdapterView.OnItemClickListener {

    private static final int REQUEST_PERMISSION_BT = 22;
    private static final int REQUEST_ENABLE_BT = 23;

    private ChatHelper chatHelper;
    private List<PeerDevice> peerDeviceList;
    private DeviceAdapter adapter;
    private ListView listView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, REQUEST_PERMISSION_BT);
        WifiManager wifi = (WifiManager) getApplicationContext().getSystemService(Context.WIFI_SERVICE);
        wifi.setWifiEnabled(true);

        peerDeviceList = new ArrayList<>();
        chatHelper = ChatHelper.getInstance(new Callback() {

            @Override
            public void peersChanged(List<?> peerDevices) {
                adapter.refresh((List<PeerDevice>) peerDevices);
            }

            @Override
            public void notEnabled() {
                Intent enableIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
                startActivityForResult(enableIntent, REQUEST_ENABLE_BT);
            }

            @Override
            public void notDiscoverable() {
                Intent discoverableIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_DISCOVERABLE);
                discoverableIntent.putExtra(BluetoothAdapter.EXTRA_DISCOVERABLE_DURATION, 300);
                startActivity(discoverableIntent);
            }

            @Override
            public void onConnection(boolean connected, String deviceName) {
                PeerDevice device = null;
                for (PeerDevice dev : peerDeviceList) {
                    if (dev.name.equals(deviceName)) {
                        device = dev;
                        break;
                    }
                }
                if (connected) {
                    Toast.makeText(getApplicationContext(), "Connected to " + device.name, Toast.LENGTH_SHORT).show();
                    Intent intent = new Intent(getApplicationContext(), ChatActivity.class);
                    intent.putExtra(Constants.DEVICE, device);
                    startActivity(intent);
                } else {
                    Toast.makeText(getApplicationContext(), "Could not connect to " + device.name, Toast.LENGTH_SHORT).show();
                }
            }
        });
        chatHelper.init(getApplicationContext());

        adapter = new DeviceAdapter(getApplicationContext(), peerDeviceList);
        listView = (ListView) findViewById(R.id.list_view_devices);
        listView.setAdapter(adapter);
        listView.setOnItemClickListener(this);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main, menu);
        SwitchCompat toggle = (SwitchCompat) menu.findItem(R.id.myswitch).getActionView().findViewById(R.id.actionbar_toggle);
        toggle.setOnClickListener(this);
        return true;
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.actionbar_toggle:
                chatHelper.toggle();
//                adapter.refresh(null);
                break;
        }
    }

    public void onRequestPermissionsResult(int requestCode, String permissions[], int[] grantResults) {
        switch (requestCode) {
            case REQUEST_PERMISSION_BT: {
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                } else {
                    Toast.makeText(this, "This permission is required", Toast.LENGTH_SHORT).show();
                }
                return;
            }
        }
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        chatHelper.connect(position);
    }
}