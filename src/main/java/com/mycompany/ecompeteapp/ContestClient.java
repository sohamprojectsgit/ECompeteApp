package com.mycompany.ecompeteapp;

import java.net.*;
import java.io.*;
import org.json.*;

class ContestClient {
    private Socket socket;
    private BufferedReader in;
    private PrintWriter out;
    private String serverIP;
    private int serverPort;
    private String playerName;
    private String gameType;
    private int timeLimit;
    private ClientMessageListener listener;
    
    public ContestClient(String serverIP, int serverPort, String playerName) {
        this.serverIP = serverIP;
        this.serverPort = serverPort;
        this.playerName = playerName;
    }
    
    public boolean connect() {
        try {
            socket = new Socket(serverIP, serverPort);
            in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            out = new PrintWriter(socket.getOutputStream(), true);
            
            // Send participant info
            JSONObject json = new JSONObject();
            json.put("name", playerName);
            out.println(json.toString());
            
            // Receive game info
            String response = in.readLine();
            JSONObject gameInfo = new JSONObject(response);
            gameType = gameInfo.getString("gameType");
            timeLimit = gameInfo.getInt("timeLimit");
            
            // Start listening thread
            new Thread(() -> listenForMessages()).start();
            
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }
    
    private void listenForMessages() {
        try {
            String message;
            while ((message = in.readLine()) != null) {
                if (listener != null) {
                    listener.onMessageReceived(message);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    
    public void sendAnswer(int questionId, String answer, boolean correct) {
        try {
            JSONObject json = new JSONObject();
            json.put("type", "ANSWER");
            json.put("questionId", questionId);
            json.put("answer", answer);
            json.put("correct", correct);
            json.put("timestamp", System.currentTimeMillis());
            out.println(json.toString());
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    
    public void sendCompletion(int score, long completionTime) {
        try {
            JSONObject json = new JSONObject();
            json.put("type", "COMPLETE");
            json.put("score", score);
            json.put("time", completionTime);
            out.println(json.toString());
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    
    public void setMessageListener(ClientMessageListener listener) {
        this.listener = listener;
    }
    
    public String getGameType() {
        return gameType;
    }
    
    public int getTimeLimit() {
        return timeLimit;
    }
    
    public void disconnect() {
        try {
            if (in != null) in.close();
            if (out != null) out.close();
            if (socket != null) socket.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}

interface ClientMessageListener {
    void onMessageReceived(String message);
}