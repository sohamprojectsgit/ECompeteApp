package com.mycompany.ecompeteapp;

import javax.swing.*;
import javax.swing.table.*;
import java.awt.*;
import java.util.*;

class JoinPanel extends JPanel {
    private MainFrame parent;
    private JTable roomTable;
    private DefaultTableModel tableModel;
    private JTextField ipField;
    private JTextField portField;
    private JTextField nameField;
    private RoomDiscovery discovery;
    
    public JoinPanel(MainFrame parent) {
        this.parent = parent;
        setLayout(new BorderLayout(10, 10));
        setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
        
        // Header
        JPanel headerPanel = new JPanel();
        JLabel titleLabel = new JLabel("Join Contest Room");
        titleLabel.setFont(new Font("Arial", Font.BOLD, 24));
        headerPanel.add(titleLabel);
        
        // Available Rooms Table
        JPanel tablePanel = new JPanel(new BorderLayout(5, 5));
        tablePanel.setBorder(BorderFactory.createTitledBorder("Available Rooms on Network"));
        
        String[] columns = {"Room Name", "Host", "Game Type", "IP Address", "Port"};
        tableModel = new DefaultTableModel(columns, 0) {
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        roomTable = new JTable(tableModel);
        roomTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        JScrollPane scrollPane = new JScrollPane(roomTable);
        
        JButton refreshBtn = new JButton("Refresh Rooms");
        refreshBtn.addActionListener(e -> refreshRooms());
        
        tablePanel.add(scrollPane, BorderLayout.CENTER);
        tablePanel.add(refreshBtn, BorderLayout.SOUTH);
        
        // Manual Connect Panel
        JPanel manualPanel = new JPanel(new GridBagLayout());
        manualPanel.setBorder(BorderFactory.createTitledBorder("Manual Connect"));
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 5, 5, 5);
        gbc.fill = GridBagConstraints.HORIZONTAL;
        
        gbc.gridx = 0; gbc.gridy = 0;
        manualPanel.add(new JLabel("Your Name:"), gbc);
        gbc.gridx = 1;
        nameField = new JTextField(15);
        manualPanel.add(nameField, gbc);
        
        gbc.gridx = 0; gbc.gridy = 1;
        manualPanel.add(new JLabel("IP Address:"), gbc);
        gbc.gridx = 1;
        ipField = new JTextField(15);
        manualPanel.add(ipField, gbc);
        
        gbc.gridx = 0; gbc.gridy = 2;
        manualPanel.add(new JLabel("Port:"), gbc);
        gbc.gridx = 1;
        portField = new JTextField("8888", 15);
        manualPanel.add(portField, gbc);
        
        // Buttons
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 20, 10));
        
        JButton joinBtn = new JButton("Join Selected Room");
        JButton connectBtn = new JButton("Manual Connect");
        JButton backBtn = new JButton("Back");
        
        buttonPanel.add(joinBtn);
        buttonPanel.add(connectBtn);
        buttonPanel.add(backBtn);
        
        joinBtn.addActionListener(e -> joinSelectedRoom());
        connectBtn.addActionListener(e -> manualConnect());
        backBtn.addActionListener(e -> parent.showPanel("HOME"));
        
        JPanel centerPanel = new JPanel(new BorderLayout(5, 5));
        centerPanel.add(tablePanel, BorderLayout.CENTER);
        centerPanel.add(manualPanel, BorderLayout.SOUTH);
        
        add(headerPanel, BorderLayout.NORTH);
        add(centerPanel, BorderLayout.CENTER);
        add(buttonPanel, BorderLayout.SOUTH);
        
        discovery = new RoomDiscovery(tableModel);
        discovery.start();
    }
    
    private void refreshRooms() {
        discovery.discover();
    }
    
    private void joinSelectedRoom() {
        int selectedRow = roomTable.getSelectedRow();
        if (selectedRow == -1) {
            JOptionPane.showMessageDialog(this, "Please select a room!");
            return;
        }
        
        String name = nameField.getText().trim();
        if (name.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Please enter your name!");
            return;
        }
        
        String ip = (String) tableModel.getValueAt(selectedRow, 3);
        int port = Integer.parseInt((String) tableModel.getValueAt(selectedRow, 4));
        
        connectToRoom(ip, port, name);
    }
    
    private void manualConnect() {
        String name = nameField.getText().trim();
        String ip = ipField.getText().trim();
        String portStr = portField.getText().trim();
        
        if (name.isEmpty() || ip.isEmpty() || portStr.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Please fill all fields!");
            return;
        }
        
        try {
            int port = Integer.parseInt(portStr);
            connectToRoom(ip, port, name);
        } catch (NumberFormatException ex) {
            JOptionPane.showMessageDialog(this, "Invalid port number!");
        }
    }
    
    private void connectToRoom(String ip, int port, String name) {
        try {
            ContestClient client = new ContestClient(ip, port, name);
            if (client.connect()) {
                ClientGamePanel gamePanel = new ClientGamePanel(parent, client);
                parent.showGamePanel(gamePanel);
            }
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Connection failed: " + ex.getMessage());
        }
    }
}