import javax.swing.*;
import javax.swing.table.*;
import java.awt.*;
import java.util.*;
import javax.swing.Timer;
import java.awt.event.*;
import java.io.File;

class HostControlPanel extends GamePanel {
    private ContestServer server;
    private JTable participantTable;
    private DefaultTableModel tableModel;
    private JLabel statusLabel;
    private JTextArea logArea;
    private Timer updateTimer;
    private JFrame hostFrame;
    
    public HostControlPanel(MainFrame parent, ContestServer server) {
        super(parent);
        this.server = server;
        
        // Create fullscreen window for host
        createFullscreenWindow();
    }
    
    private void createFullscreenWindow() {
        hostFrame = new JFrame("E-Compete - Host Dashboard");
        hostFrame.setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
        hostFrame.setUndecorated(false);
        hostFrame.setExtendedState(JFrame.MAXIMIZED_BOTH);
        hostFrame.setAlwaysOnTop(false);
        hostFrame.setFocusableWindowState(true);
        
        // Add window listener
        hostFrame.addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                stopContest();
            }
            
            @Override
            public void windowDeactivated(WindowEvent e) {
                // Immediately regain focus if user tries to switch windows
                SwingUtilities.invokeLater(() -> {
                    hostFrame.requestFocus();
                    hostFrame.toFront();
                });
            }
        });
        
        JPanel mainPanel = new JPanel(new BorderLayout());
        mainPanel.setBackground(Color.WHITE);
        
        // Header
        JPanel headerPanel = new JPanel(new BorderLayout());
        headerPanel.setBackground(new Color(34, 139, 34));
        headerPanel.setPreferredSize(new Dimension(900, 80));
        
        JLabel titleLabel = new JLabel("Contest Host Dashboard");
        titleLabel.setFont(new Font("Arial", Font.BOLD, 28));
        titleLabel.setForeground(Color.WHITE);
        titleLabel.setHorizontalAlignment(SwingConstants.CENTER);
        
        statusLabel = new JLabel("Status: Active | Participants: 0");
        statusLabel.setFont(new Font("Arial", Font.PLAIN, 14));
        statusLabel.setForeground(Color.WHITE);
        statusLabel.setHorizontalAlignment(SwingConstants.CENTER);
        
        headerPanel.add(titleLabel, BorderLayout.CENTER);
        headerPanel.add(statusLabel, BorderLayout.SOUTH);
        
        // Participant Table
        String[] columns = {"Rank", "Name", "Score", "Completion Time", "Status", "Answers"};
        tableModel = new DefaultTableModel(columns, 0) {
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        
        participantTable = new JTable(tableModel);
        participantTable.setRowHeight(30);
        participantTable.getColumnModel().getColumn(0).setPreferredWidth(50);
        participantTable.getColumnModel().getColumn(1).setPreferredWidth(150);
        
        JScrollPane tableScroll = new JScrollPane(participantTable);
        tableScroll.setBorder(BorderFactory.createTitledBorder("Live Leaderboard"));
        
        // Log Area
        logArea = new JTextArea(5, 50);
        logArea.setEditable(false);
        logArea.setFont(new Font("Monospaced", Font.PLAIN, 12));
        JScrollPane logScroll = new JScrollPane(logArea);
        logScroll.setBorder(BorderFactory.createTitledBorder("Activity Log"));
        
        // Split pane
        JSplitPane splitPane = new JSplitPane(JSplitPane.VERTICAL_SPLIT, tableScroll, logScroll);
        splitPane.setDividerLocation(450);
        
        // Control Panel
        JPanel controlPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 15, 10));
        controlPanel.setBackground(Color.WHITE);
        
        JButton refreshBtn = new JButton("Refresh Data");
        JButton exportBtn = new JButton("Export Results");
        JButton stopBtn = new JButton("Stop Contest");
        JButton backBtn = new JButton("Back to Home");
        
        styleButton(refreshBtn, new Color(30, 144, 255));
        styleButton(exportBtn, new Color(255, 140, 0));
        styleButton(stopBtn, new Color(220, 20, 60));
        styleButton(backBtn, new Color(128, 128, 128));
        
        refreshBtn.addActionListener(e -> updateParticipantTable());
        exportBtn.addActionListener(e -> exportResults());
        stopBtn.addActionListener(e -> stopContest());
        backBtn.addActionListener(e -> goBack());
        
        controlPanel.add(refreshBtn);
        controlPanel.add(exportBtn);
        controlPanel.add(stopBtn);
        controlPanel.add(backBtn);
        
        mainPanel.add(headerPanel, BorderLayout.NORTH);
        mainPanel.add(splitPane, BorderLayout.CENTER);
        mainPanel.add(controlPanel, BorderLayout.SOUTH);
        
        hostFrame.add(mainPanel);
        hostFrame.setVisible(true);
        hostFrame.requestFocus();
        hostFrame.toFront();
        
        // Start auto-update timer
        updateTimer = new Timer(2000, e -> updateParticipantTable());
        updateTimer.start();
        
        addLog("Contest started successfully!");
        updateParticipantTable();
    }
    
    private void styleButton(JButton btn, Color color) {
        btn.setPreferredSize(new Dimension(140, 35));
        btn.setBackground(color);
        btn.setForeground(Color.WHITE);
        btn.setFont(new Font("Arial", Font.BOLD, 12));
        btn.setFocusPainted(false);
    }
    
    private void updateParticipantTable() {
        tableModel.setRowCount(0);
        
        java.util.List<ParticipantData> leaderboard = server.getContestData().getLeaderboard();
        statusLabel.setText("Status: Active | Participants: " + leaderboard.size());
        
        int rank = 1;
        for (ParticipantData p : leaderboard) {
            String status = p.getCompletionTime() > 0 ? "Completed" : "In Progress";
            String timeStr = p.getCompletionTime() > 0 ? formatTime(p.getCompletionTime()) : "--:--";
            
            tableModel.addRow(new Object[]{
                rank++,
                p.getName(),
                p.getScore(),
                timeStr,
                status,
                p.toJSON().optInt("correctAnswers", 0) + "/" + p.toJSON().optInt("totalAnswers", 0)
            });
        }
    }
    
    private String formatTime(long millis) {
        long seconds = millis / 1000;
        long minutes = seconds / 60;
        seconds = seconds % 60;
        return String.format("%02d:%02d", minutes, seconds);
    }
    
    private void addLog(String message) {
        String timestamp = new java.text.SimpleDateFormat("HH:mm:ss").format(new Date());
        logArea.append("[" + timestamp + "] " + message + "\n");
        logArea.setCaretPosition(logArea.getDocument().getLength());
    }
    
    private void exportResults() {
        JFileChooser fileChooser = new JFileChooser();
        fileChooser.setSelectedFile(new File("contest_results.json"));
        
        if (fileChooser.showSaveDialog(hostFrame) == JFileChooser.APPROVE_OPTION) {
            server.getContestData().exportToFile(fileChooser.getSelectedFile().getAbsolutePath());
            addLog("Results exported to: " + fileChooser.getSelectedFile().getName());
            JOptionPane.showMessageDialog(hostFrame, "Results exported successfully!");
        }
    }
    
    private void stopContest() {
        int result = JOptionPane.showConfirmDialog(hostFrame,
            "Are you sure you want to stop the contest?",
            "Stop Contest",
            JOptionPane.YES_NO_OPTION);
            
        if (result == JOptionPane.YES_OPTION) {
            cleanup();
            addLog("Contest stopped by host");
            JOptionPane.showMessageDialog(hostFrame, "Contest stopped. Data has been saved.");
            
            if (hostFrame != null) {
                hostFrame.dispose();
            }
            
            parent.showPanel("HOME");
        }
    }
    
    private void goBack() {
        stopContest();
    }
    
    @Override
    public void cleanup() {
        if (updateTimer != null) {
            updateTimer.stop();
        }
        if (server != null) {
            server.stopServer();
        }
    }
}