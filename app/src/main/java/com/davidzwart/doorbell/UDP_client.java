package com.davidzwart.doorbell;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.InterfaceAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.util.Enumeration;

import android.content.Context;
import android.os.StrictMode;
import android.util.Log;

public class UDP_client {

    private static final String TAG = "MyActivity";
    int UDP_port_send = 500;

    public void sendBroadcast(String messageStr) {
        // Hack Prevent crash (sending should be done using an async task)
        StrictMode.ThreadPolicy policy = new   StrictMode.ThreadPolicy.Builder().permitAll().build();
        StrictMode.setThreadPolicy(policy);

        try {
            //Open a random port to send the package
            DatagramSocket socket = new DatagramSocket();
            socket.setBroadcast(true);
            byte[] sendData = messageStr.getBytes();
            DatagramPacket sendPacket = new DatagramPacket(sendData, sendData.length, getBroadcastAddress(), UDP_port_send);
            socket.send(sendPacket);
            System.out.println(getClass().getName() + "Broadcast packet sent to: " + getBroadcastAddress().getHostAddress());
        } catch (IOException e) {
            Log.e(TAG, "IOException: " + e.getMessage());
        }
    }

    /**
     * get broadcast address
     * when hotspot is opening, the default broadcast address is 182.168.43.255
     */
    public InetAddress getBroadcastAddress() {
        InetAddress broadcastAddress = null;
        try {
            Enumeration<NetworkInterface> networkInterface = NetworkInterface.getNetworkInterfaces();

            while (broadcastAddress == null && networkInterface.hasMoreElements()) {
                NetworkInterface singleInterface = networkInterface.nextElement();
                String interfaceName = singleInterface.getName();
                if (interfaceName.contains("wlan0") || interfaceName.contains("eth0")) {
                    for (InterfaceAddress infaceAddress : singleInterface.getInterfaceAddresses()) {
                        broadcastAddress = infaceAddress.getBroadcast();
                        if (broadcastAddress != null) {
                            break;
                        }
                    }
                }
            }

        } catch (SocketException e) {
            e.printStackTrace();
        }

        return broadcastAddress;
    }
}