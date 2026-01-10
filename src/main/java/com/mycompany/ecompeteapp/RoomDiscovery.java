import javax.swing.table.*;
import java.net.*;
import java.util.*;
import org.json.*;

class RoomDiscovery extends Thread {
    private DefaultTableModel tableModel;
    private DatagramSocket socket;
    private Map<String, Long> discoveredRooms;
    
    public RoomDiscovery(DefaultTableModel tableModel) {
        this.tableModel = tableModel;
        this.discoveredRooms = new HashMap<>();
        setDaemon(true);
    }
    
    @Override
    public void run() {
        try {
            socket = new DatagramSocket(9876);
            socket.setBroadcast(true);
            byte[] buffer = new byte[1024];
            
            while (true) {
                DatagramPacket packet = new DatagramPacket(buffer, buffer.length);
                socket.receive(packet);
                
                String data = new String(packet.getData(), 0, packet.getLength());
                processRoomInfo(data, packet.getAddress().getHostAddress());
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    
    private void processRoomInfo(String data, String ip) {
        try {
            JSONObject json = new JSONObject(data);
            String roomName = json.getString("roomName");
            String gameType = json.getString("gameType");
            String host = json.getString("host");
            int port = json.getInt("port");
            
            String roomKey = ip + ":" + port;
            discoveredRooms.put(roomKey, System.currentTimeMillis());
            
            // Update table
            updateTable(roomName, host, gameType, ip, String.valueOf(port));
            
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    
    private void updateTable(String roomName, String host, String gameType, String ip, String port) {
        boolean found = false;
        for (int i = 0; i < tableModel.getRowCount(); i++) {
            if (tableModel.getValueAt(i, 3).equals(ip) && 
                tableModel.getValueAt(i, 4).equals(port)) {
                found = true;
                break;
            }
        }
        
        if (!found) {
            tableModel.addRow(new Object[]{roomName, host, gameType, ip, port});
        }
    }
    
    public void discover() {
        // Clear old entries
        long now = System.currentTimeMillis();
        tableModel.setRowCount(0);
    }
}