package com.mycompany.ecompeteapp;

import java.net.*;
import java.io.*;
import org.json.*;

class ClientHandler extends Thread {
    private Socket socket;
    private ContestServer server;
    private BufferedReader in;
    private PrintWriter out;
    private String participantName;
    private ParticipantData participantData;
    
    public ClientHandler(Socket socket, ContestServer server) {
        this.socket = socket;
        this.server = server;
        this.participantData = new ParticipantData();
    }
    
    @Override
    public void run() {
        try {
            in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            out = new PrintWriter(socket.getOutputStream(), true);
            
            // Receive participant info
            String initMsg = in.readLine();
            JSONObject json = new JSONObject(initMsg);
            participantName = json.getString("name");
            participantData.setName(participantName);
            
            // Send game info
            JSONObject gameInfo = new JSONObject();
            gameInfo.put("type", "GAME_INFO");
            gameInfo.put("gameType", server.getGameType());
            gameInfo.put("timeLimit", server.getTimeLimit());
            out.println(gameInfo.toString());
            
            // Handle messages
            String message;
            while ((message = in.readLine()) != null) {
                handleMessage(message);
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            cleanup();
        }
    }
    
    private void handleMessage(String message) {
        try {
            JSONObject json = new JSONObject(message);
            String type = json.getString("type");
            
            switch (type) {
                case "ANSWER":
                    handleAnswer(json);
                    break;
                case "COMPLETE":
                    handleCompletion(json);
                    break;
                case "HEARTBEAT":
                    // Keep alive
                    break;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    
    private void handleAnswer(JSONObject json) {
        participantData.addAnswer(json);
        server.getContestData().updateParticipant(participantName, participantData);
    }
    
    private void handleCompletion(JSONObject json) {
        participantData.setCompletionTime(json.getLong("time"));
        participantData.setScore(json.getInt("score"));
        server.getContestData().updateParticipant(participantName, participantData);
    }
    
    public void sendMessage(String message) {
        if (out != null) {
            out.println(message);
        }
    }
    
    public String getParticipantName() {
        return participantName;
    }
    
    public ParticipantData getParticipantData() {
        return participantData;
    }
    
    private void cleanup() {
        try {
            if (in != null) in.close();
            if (out != null) out.close();
            if (socket != null) socket.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}