/*
 * Copyright (C) 2011 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package edu.csulb.android.bluetoothmessenger.wifi;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.net.NetworkInfo;
import android.net.wifi.p2p.WifiP2pDevice;
import android.net.wifi.p2p.WifiP2pGroup;
import android.net.wifi.p2p.WifiP2pManager;
import android.net.wifi.p2p.WifiP2pManager.ActionListener;
import android.net.wifi.p2p.WifiP2pManager.Channel;
import android.util.Log;

import edu.csulb.android.bluetoothmessenger.config.Configuration;
import edu.csulb.android.bluetoothmessenger.fragments.WifiFragment;
import edu.csulb.android.bluetoothmessenger.router.AllEncompasingP2PClient;
import edu.csulb.android.bluetoothmessenger.router.MeshNetworkManager;
import edu.csulb.android.bluetoothmessenger.router.Receiver;
import edu.csulb.android.bluetoothmessenger.router.Sender;

/**
 * A BroadcastReceiver that notifies of important wifi p2p events.
 */
public class WiFiDirectBroadcastReceiver extends BroadcastReceiver {

    public static String MAC;
    private WifiP2pManager manager;
    private Channel channel;
    private WifiFragment wifiFragment;

    /**
     * @param manager      WifiP2pManager system service
     * @param channel      Wifi p2p channel
     * @param wifiFragment activity associated with the receiver
     */
    public WiFiDirectBroadcastReceiver(WifiP2pManager manager, Channel channel, WifiFragment wifiFragment) {
        super();
        this.manager = manager;
        this.channel = channel;
        this.wifiFragment = wifiFragment;
    }

    /**
     * State transitions based on connection and state information, callback based on P2P library
     */
    @Override
    public void onReceive(Context context, Intent intent) {
        String action = intent.getAction();

        if (WifiP2pManager.WIFI_P2P_STATE_CHANGED_ACTION.equals(action)) {

            // UI update to indicate wifi p2p status.
            int state = intent.getIntExtra(WifiP2pManager.EXTRA_WIFI_STATE, -1);

            if (state == WifiP2pManager.WIFI_P2P_STATE_ENABLED) {
                // Wifi Direct mode is enabled
                wifiFragment.setIsWifiP2pEnabled(true);

                manager.createGroup(channel, new ActionListener() {

                    @Override
                    public void onSuccess() {
                        Log.d(WifiFragment.TAG, "P2P Group created");
                    }

                    @Override
                    public void onFailure(int reason) {
                        Log.d(WifiFragment.TAG, "P2P Group failed");
                    }
                });
            } else {
                wifiFragment.setIsWifiP2pEnabled(false);
            }

            Log.d(WifiFragment.TAG, "P2PACTION : WIFI_P2P_STATE_CHANGED_ACTION state = " + state);
        } else if (WifiP2pManager.WIFI_P2P_PEERS_CHANGED_ACTION.equals(action)) {

            // request available peers from the wifi p2p manager. This is an
            // asynchronous call and the calling activity is notified with a
            // callback on PeerListListener.onPeersAvailable()
            if (manager != null) {
                manager.requestPeers(channel, wifiFragment);
            }
            Log.d(WifiFragment.TAG, "P2PACTION : WIFI_P2P_PEERS_CHANGED_ACTION");
        } else if (WifiP2pManager.WIFI_P2P_CONNECTION_CHANGED_ACTION.equals(action)) {

            if (manager == null) {
                return;
            }

            NetworkInfo networkInfo = (NetworkInfo) intent.getParcelableExtra(WifiP2pManager.EXTRA_NETWORK_INFO);

            if (networkInfo.isConnected()) {
                // we are connected with the other device, request connection
                // info to find group owner IP
                manager.requestConnectionInfo(channel, wifiFragment);
            } else {
                // It's a disconnect
                Log.d(WifiFragment.TAG, "P2PACTION : WIFI_P2P_CONNECTION_CHANGED_ACTION -- DISCONNECT");
            }
        } else if (WifiP2pManager.WIFI_P2P_THIS_DEVICE_CHANGED_ACTION.equals(action)) {
            wifiFragment.updateThisDevice((WifiP2pDevice) intent.getParcelableExtra(WifiP2pManager.EXTRA_WIFI_P2P_DEVICE));

            MAC = ((WifiP2pDevice) intent.getParcelableExtra(WifiP2pManager.EXTRA_WIFI_P2P_DEVICE)).deviceAddress;

            //Set yourself on connection
            MeshNetworkManager.setSelf(new AllEncompasingP2PClient(((WifiP2pDevice) intent
                    .getParcelableExtra(WifiP2pManager.EXTRA_WIFI_P2P_DEVICE)).deviceAddress, Configuration.GO_IP,
                    ((WifiP2pDevice) intent.getParcelableExtra(WifiP2pManager.EXTRA_WIFI_P2P_DEVICE)).deviceName,
                    ((WifiP2pDevice) intent.getParcelableExtra(WifiP2pManager.EXTRA_WIFI_P2P_DEVICE)).deviceAddress));

            //Launch receiver and sender once connected to someone
            if (!Receiver.running) {
                Receiver r = new Receiver(wifiFragment);
                new Thread(r).start();
                Sender s = new Sender();
                new Thread(s).start();
            }

            manager.requestGroupInfo(channel, new WifiP2pManager.GroupInfoListener() {
                @Override
                public void onGroupInfoAvailable(WifiP2pGroup group) {
                    if (group != null) {
                        // clients require these
                        String ssid = group.getNetworkName();
                        String passphrase = group.getPassphrase();

                        Log.d(WifiFragment.TAG, "GROUP INFO AVALABLE");
                        Log.d(WifiFragment.TAG, " SSID : " + ssid + "\n Passphrase : " + passphrase);

                    }
                }
            });
        }
    }
}
