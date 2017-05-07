package edu.csulb.android.bluetoothmessenger.router;

import android.support.v4.app.FragmentActivity;
import android.util.Log;

import java.util.concurrent.ConcurrentLinkedQueue;

import edu.csulb.android.bluetoothmessenger.activities.ChatActivity;
import edu.csulb.android.bluetoothmessenger.config.Configuration;
import edu.csulb.android.bluetoothmessenger.fragments.WifiFragment;
import edu.csulb.android.bluetoothmessenger.router.tcp.TcpReciever;

/**
 * The main receiver class
 *
 * @author Matthew Vertescher
 * @author Peter Henderson
 */
public class Receiver implements Runnable {

    /**
     * Flag if the receiver has been running to prevent overzealous thread spawning
     */
    public static boolean running = false;

    /**
     * A ref to the fragment
     */
    static FragmentActivity fragment;

    /**
     * Constructor with fragment
     *
     * @param a
     */
    public Receiver(FragmentActivity a) {
        Receiver.fragment = a;
        running = true;
    }

    public static void somebodyJoined(String smac) {

        final String message;
        final String msg;
        message = msg = smac + " has joined.";
        final String name = smac;
        Log.e(WifiFragment.TAG, message);
    }

    public static void somebodyLeft(String smac) {

        final String message;
        final String msg;
        message = msg = smac + " has left.";
        final String name = smac;
        Log.e(WifiFragment.TAG, message);
    }

    /**
     * Main thread runner
     */
    public void run() {
        /*
         * A queue for received packets
		 */
        ConcurrentLinkedQueue<Packet> packetQueue = new ConcurrentLinkedQueue<Packet>();

		/*
         * Receiver thread
		 */
        new Thread(new TcpReciever(Configuration.RECEIVE_PORT, packetQueue)).start();

        Packet p;

		/*
         * Keep going through packets
		 */
        while (true) {
            if (packetQueue.isEmpty()) {
                continue;
            }
            /*
             * Pop a packet off the queue
			 */
            p = packetQueue.remove();


			/*
             * If it's a hello, this is special and need to go through the connection mechanism for any node receiving this
			 */
            if (p.getType().equals(Packet.TYPE.HELLO)) {
                // Put it in your routing table
                for (AllEncompasingP2PClient c : MeshNetworkManager.routingTable.values()) {
                    if (c.getMac().equals(MeshNetworkManager.getSelf().getMac()) || c.getMac().equals(p.getSenderMac()))
                        continue;
                    Packet update = new Packet(Packet.TYPE.UPDATE, Packet.getMacAsBytes(p.getSenderMac()), c.getMac(),
                            MeshNetworkManager.getSelf().getMac());
                    Sender.queuePacket(update);
                }

                MeshNetworkManager.routingTable.put(p.getSenderMac(),
                        new AllEncompasingP2PClient(p.getSenderMac(), p.getSenderIP(), p.getSenderMac(),
                                MeshNetworkManager.getSelf().getMac()));

                // Send routing table back as HELLO_ACK
                byte[] rtable = MeshNetworkManager.serializeRoutingTable();

                Packet ack = new Packet(Packet.TYPE.HELLO_ACK, rtable, p.getSenderMac(), MeshNetworkManager.getSelf().getMac());
                Sender.queuePacket(ack);
                somebodyJoined(p.getSenderMac());
            } else {
                System.out.println("HERE TOO");
                // If you're the intended target for a non hello message
                if (p.getMac().equals(MeshNetworkManager.getSelf().getMac())) {
                    //if we get a hello ack populate the table
                    if (p.getType().equals(Packet.TYPE.HELLO_ACK)) {
                        MeshNetworkManager.deserializeRoutingTableAndAdd(p.getData());
                        MeshNetworkManager.getSelf().setGroupOwnerMac(p.getSenderMac());
                        somebodyJoined(p.getSenderMac());
                    } else if (p.getType().equals(Packet.TYPE.UPDATE)) {
                        //if it's an update, add to the table
                        String emb_mac = Packet.getMacBytesAsString(p.getData(), 0);
                        MeshNetworkManager.routingTable.put(emb_mac,
                                new AllEncompasingP2PClient(emb_mac, p.getSenderIP(), p.getMac(), MeshNetworkManager
                                        .getSelf().getMac()));

                        final byte[] message = p.getData();
                        final String name = p.getSenderMac();
//                        fragment.getActivity().runOnUiThread(new Runnable() {
//
//                            @Override
//                            public void run() {
//                                ChatActivity.addMessage(message);
//                            }
//                        });
                    } else if (p.getType().equals(Packet.TYPE.MESSAGE)) {
                        //If it's a message display the message and update the table if they're not there
                        // for whatever reason
                        final byte[] message = p.getData();
                        final String name = p.getSenderMac();
                        System.out.println("HERE");
                        if (!MeshNetworkManager.routingTable.contains(p.getSenderMac())) {
                            /*
                             * Update your routing table if for some reason this
							 * guy isn't in it
							 */
                            MeshNetworkManager.routingTable.put(p.getSenderMac(),
                                    new AllEncompasingP2PClient(p.getSenderMac(), p.getSenderIP(), p.getSenderMac(),
                                            MeshNetworkManager.getSelf().getGroupOwnerMac()));
                        }
                        fragment.runOnUiThread(new Runnable() {

                            @Override
                            public void run() {
                                ChatActivity.addMessage(message);
                            }
                        });
                    }
                } else {
                    // otherwise forward it if you're not the recipient
                    int ttl = p.getTtl();
                    // Have a ttl so that they don't bounce around forever
                    ttl--;
                    if (ttl > 0) {
                        Sender.queuePacket(p);
                        p.setTtl(ttl);
                    }
                }
            }

        }
    }

}