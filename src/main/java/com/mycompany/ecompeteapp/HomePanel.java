package com.mycompany.ecompeteapp;

import javax.swing.*;
import java.awt.*;
import java.net.*;

class HomePanel extends JPanel {
    private MainFrame parent;
    private JLabel ipLabel;
    
    public HomePanel(MainFrame parent) {
        this.parent = parent;
        setLayout(new BorderLayout(10, 10));
        setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
        
        // Header
        JPanel headerPanel = new JPanel();
        JLabel titleLabel = new JLabel("E-COMPETE");
        titleLabel.setFont(new Font("Arial", Font.BOLD, 32));
        headerPanel.add(titleLabel);
        
        // Network Info Panel
        JPanel infoPanel = new JPanel(new GridLayout(2, 1, 5, 5));
        infoPanel.setBorder(BorderFactory.createTitledBorder("Network Information"));
        
        ipLabel = new JLabel("IP Address: " + getLocalIP());
        JLabel portLabel = new JLabel("Default Port: 8888");
        
        infoPanel.add(ipLabel);
        infoPanel.add(portLabel);
        
        // Center Panel with buttons
        JPanel centerPanel = new JPanel(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(10, 10, 10, 10);
        gbc.fill = GridBagConstraints.HORIZONTAL;
        
        JButton hostBtn = new JButton("Host Contest Room");
        JButton joinBtn = new JButton("Join Contest Room");
        JButton statsBtn = new JButton("View Statistics");
        JButton exitBtn = new JButton("Exit");
        
        hostBtn.setPreferredSize(new Dimension(200, 40));
        joinBtn.setPreferredSize(new Dimension(200, 40));
        statsBtn.setPreferredSize(new Dimension(200, 40));
        exitBtn.setPreferredSize(new Dimension(200, 40));
        
        gbc.gridx = 0; gbc.gridy = 0;
        centerPanel.add(hostBtn, gbc);
        gbc.gridy = 1;
        centerPanel.add(joinBtn, gbc);
        gbc.gridy = 2;
        centerPanel.add(statsBtn, gbc);
        gbc.gridy = 3;
        centerPanel.add(exitBtn, gbc);
        
        hostBtn.addActionListener(e -> parent.showPanel("HOST"));
        joinBtn.addActionListener(e -> parent.showPanel("JOIN"));
        statsBtn.addActionListener(e -> new StatsViewer());
        exitBtn.addActionListener(e -> System.exit(0));
        
        add(headerPanel, BorderLayout.NORTH);
        add(infoPanel, BorderLayout.SOUTH);
        add(centerPanel, BorderLayout.CENTER);
    }
    
    private String getLocalIP() {
        try {
            return InetAddress.getLocalHost().getHostAddress();
        } catch (Exception e) {
            return "Unknown";
        }
    }
}