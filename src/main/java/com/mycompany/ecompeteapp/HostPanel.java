import javax.swing.*;
import java.awt.*;
import java.util.*;

class HostPanel extends JPanel {
    private MainFrame parent;
    private JTextField roomNameField;
    private JTextField portField;
    private JComboBox<String> gameTypeBox;
    private JSpinner timeLimitSpinner;
    private JButton startHostBtn;
    private ContestServer server;
    
    public HostPanel(MainFrame parent) {
        this.parent = parent;
        setLayout(new BorderLayout(10, 10));
        setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
        
        // Header
        JPanel headerPanel = new JPanel();
        JLabel titleLabel = new JLabel("Host Contest Room");
        titleLabel.setFont(new Font("Arial", Font.BOLD, 24));
        headerPanel.add(titleLabel);
        
        // Form Panel
        JPanel formPanel = new JPanel(new GridBagLayout());
        formPanel.setBorder(BorderFactory.createEmptyBorder(30, 50, 30, 50));
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(10, 10, 10, 10);
        gbc.fill = GridBagConstraints.HORIZONTAL;
        
        // Room Name
        gbc.gridx = 0; gbc.gridy = 0;
        formPanel.add(new JLabel("Room Name:"), gbc);
        gbc.gridx = 1;
        roomNameField = new JTextField(20);
        formPanel.add(roomNameField, gbc);
        
        // Port
        gbc.gridx = 0; gbc.gridy = 1;
        formPanel.add(new JLabel("Port:"), gbc);
        gbc.gridx = 1;
        portField = new JTextField("8888", 20);
        formPanel.add(portField, gbc);
        
        // Game Type
        gbc.gridx = 0; gbc.gridy = 2;
        formPanel.add(new JLabel("Game Type:"), gbc);
        gbc.gridx = 1;
        String[] games = {"Quiz Challenge", "Debug Hunt", "Problem Solver", "Code World"};
        gameTypeBox = new JComboBox<>(games);
        formPanel.add(gameTypeBox, gbc);
        
        // Time Limit
        gbc.gridx = 0; gbc.gridy = 3;
        formPanel.add(new JLabel("Time Limit (minutes):"), gbc);
        gbc.gridx = 1;
        timeLimitSpinner = new JSpinner(new SpinnerNumberModel(10, 1, 60, 1));
        formPanel.add(timeLimitSpinner, gbc);
        
        // Buttons
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 20, 20));
        
        startHostBtn = new JButton("Start Hosting");
        JButton backBtn = new JButton("Back");
        
        buttonPanel.add(startHostBtn);
        buttonPanel.add(backBtn);
        
        startHostBtn.addActionListener(e -> startHosting());
        backBtn.addActionListener(e -> parent.showPanel("HOME"));
        
        add(headerPanel, BorderLayout.NORTH);
        add(formPanel, BorderLayout.CENTER);
        add(buttonPanel, BorderLayout.SOUTH);
    }
    
    private void startHosting() {
        String roomName = roomNameField.getText().trim();
        if (roomName.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Please enter a room name!");
            return;
        }
        
        try {
            int port = Integer.parseInt(portField.getText());
            int timeLimit = (Integer) timeLimitSpinner.getValue();
            String gameType = (String) gameTypeBox.getSelectedItem();
            
            server = new ContestServer(port, roomName, gameType, timeLimit);
            server.start();
            
            HostControlPanel controlPanel = new HostControlPanel(parent, server);
            parent.showGamePanel(controlPanel);
            
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Error starting server: " + ex.getMessage());
        }
    }
}