package edu.csulb.android.bluetoothmessenger.activities;

import android.Manifest;
import android.content.Context;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.support.v4.app.ActivityCompat;
import android.support.v7.widget.SwitchCompat;
import android.view.Menu;
import android.view.View;

import edu.csulb.android.bluetoothmessenger.R;
import edu.csulb.android.bluetoothmessenger.fragments.BluetoothFragment;
import edu.csulb.android.bluetoothmessenger.fragments.WifiFragment;

public class MainActivity extends SuperActivity implements View.OnClickListener {

    WifiFragment wifiFragment;
    BluetoothFragment bluetoothFragment;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, 25);
        ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.BLUETOOTH_PRIVILEGED}, 26);

        wifiFragment = (WifiFragment) getSupportFragmentManager().findFragmentById(R.id.fragment_wifi);
        wifiFragment.close();
        bluetoothFragment = (BluetoothFragment) getSupportFragmentManager().findFragmentById(R.id.fragment_bluetooth);

        WifiManager wifi = (WifiManager) getApplicationContext().getSystemService(Context.WIFI_SERVICE);
        wifi.setWifiEnabled(true);
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
                if (wifiFragment.isVisible()) {
                    wifiFragment.close();
                } else {
                    wifiFragment.open();
                }

                if (bluetoothFragment.isVisible()) {
                    bluetoothFragment.close();
                } else {
                    bluetoothFragment.open();
                }
                break;
        }
    }
}