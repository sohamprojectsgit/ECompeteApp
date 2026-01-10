import java.net.*;
import java.io.*;
import java.util.*;
import java.util.concurrent.*;

class ContestServer extends Thread {
    private ServerSocket serverSocket;
    private int port;
    private String roomName;
    private String gameType;
    private int timeLimit;
    private List<ClientHandler> clients;
    private volatile boolean running;
    private BroadcastService broadcast;
    private ContestData contestData;
    
    public ContestServer(int port, String roomName, String gameType, int timeLimit) {
        this.port = port;
        this.roomName = roomName;
        this.gameType = gameType;
        this.timeLimit = timeLimit;
        this.clients = new CopyOnWriteArrayList<>();
        this.running = true;
        this.contestData = new ContestData(roomName, gameType);
    }
    
    @Override
    public void run() {
        try {
            serverSocket = new ServerSocket(port);
            broadcast = new BroadcastService(port, roomName, gameType);
            broadcast.start();
            
            System.out.println("Server started on port " + port);
            
            while (running) {
                Socket clientSocket = serverSocket.accept();
                ClientHandler handler = new ClientHandler(clientSocket, this);
                clients.add(handler);
                handler.start();
            }
        } catch (IOException e) {
            if (running) {
                e.printStackTrace();
            }
        }
    }
    
    public void stopServer() {
        running = false;
        if (broadcast != null) broadcast.stopBroadcast();
        try {
            if (serverSocket != null) serverSocket.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    
    public List<ClientHandler> getClients() {
        return clients;
    }
    
    public String getGameType() {
        return gameType;
    }
    
    public int getTimeLimit() {
        return timeLimit;
    }
    
    public ContestData getContestData() {
        return contestData;
    }
    
    public void broadcastMessage(String message) {
        for (ClientHandler client : clients) {
            client.sendMessage(message);
        }
    }
}
