package com.mycompany.ecompeteapp;

import java.net.*;
import org.json.*;

class BroadcastService extends Thread {
    private DatagramSocket socket;
    private int port;
    private String roomName;
    private String gameType;
    private volatile boolean running;
    
    public BroadcastService(int port, String roomName, String gameType) {
        this.port = port;
        this.roomName = roomName;
        this.gameType = gameType;
        this.running = true;
    }
    
    @Override
    public void run() {
        try {
            socket = new DatagramSocket();
            socket.setBroadcast(true);
            
            while (running) {
                try {
                    JSONObject json = new JSONObject();
                    json.put("roomName", roomName);
                    json.put("gameType", gameType);
                    json.put("port", port);
                    json.put("host", InetAddress.getLocalHost().getHostName());
                    
                    byte[] buffer = json.toString().getBytes();
                    DatagramPacket packet = new DatagramPacket(
                        buffer, buffer.length,
                        InetAddress.getByName("255.255.255.255"),
                        9876
                    );
                    socket.send(packet);
                    Thread.sleep(3000);
                } catch (Exception e) {
                    // Continue broadcasting
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    
    public void stopBroadcast() {
        running = false;
        if (socket != null) socket.close();
    }
}
