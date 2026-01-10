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
    private volatile boolean isExiting = false;
    
    public HostControlPanel(MainFrame parent, ContestServer server) {
        super(parent);
        this.server = server;
        createFullscreenWindow();
    }
    
    private void createFullscreenWindow() {
        hostFrame = new JFrame("E-Compete - Host Dashboard");
        hostFrame.setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
        hostFrame.setUndecorated(true);
        hostFrame.setExtendedState(JFrame.MAXIMIZED_BOTH);
        
        hostFrame.addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                stopContest();
            }
        });
        
        // Alt+F4 support
        hostFrame.getRootPane().registerKeyboardAction(
            e -> stopContest(),
            KeyStroke.getKeyStroke(KeyEvent.VK_F4, KeyEvent.ALT_DOWN_MASK),
            JComponent.WHEN_IN_FOCUSED_WINDOW
        );
        
        JPanel mainPanel = new JPanel(new BorderLayout(10, 10));
        mainPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        
        // Header
        JPanel headerPanel = new JPanel(new BorderLayout());
        
        JLabel titleLabel = new JLabel("Contest Host Dashboard", SwingConstants.CENTER);
        titleLabel.setFont(new Font("Arial", Font.BOLD, 24));
        
        statusLabel = new JLabel("Status: Active | Participants: 0", SwingConstants.CENTER);
        statusLabel.setFont(new Font("Arial", Font.PLAIN, 14));
        
        headerPanel.add(titleLabel, BorderLayout.NORTH);
        headerPanel.add(statusLabel, BorderLayout.SOUTH);
        
        // Participant Table
        String[] columns = {"Rank", "Name", "Score", "Time", "Status", "Answers"};
        tableModel = new DefaultTableModel(columns, 0) {
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        
        participantTable = new JTable(tableModel);
        participantTable.setRowHeight(25);
        JScrollPane tableScroll = new JScrollPane(participantTable);
        tableScroll.setBorder(BorderFactory.createTitledBorder("Live Leaderboard"));
        
        // Log Area
        logArea = new JTextArea(5, 50);
        logArea.setEditable(false);
        logArea.setFont(new Font("Monospaced", Font.PLAIN, 11));
        JScrollPane logScroll = new JScrollPane(logArea);
        logScroll.setBorder(BorderFactory.createTitledBorder("Activity Log"));
        
        JSplitPane splitPane = new JSplitPane(JSplitPane.VERTICAL_SPLIT, tableScroll, logScroll);
        splitPane.setDividerLocation(400);
        
        // Control Panel
        JPanel controlPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 5));
        
        JButton refreshBtn = new JButton("Refresh Data");
        JButton exportBtn = new JButton("Export Results");
        JButton stopBtn = new JButton("Stop Contest");
        JButton backBtn = new JButton("Back to Home");
        
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
        
        updateTimer = new Timer(2000, e -> {
            if (!isExiting) {
                updateParticipantTable();
            }
        });
        updateTimer.start();
        
        addLog("Contest started successfully!");
        updateParticipantTable();
    }
    
    private void updateParticipantTable() {
        if (isExiting) return;
        
        try {
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
        } catch (Exception e) {
            System.err.println("Error updating participant table: " + e.getMessage());
        }
    }
    
    private String formatTime(long millis) {
        long seconds = millis / 1000;
        long minutes = seconds / 60;
        seconds = seconds % 60;
        return String.format("%02d:%02d", minutes, seconds);
    }
    
    private void addLog(String message) {
        if (isExiting) return;
        
        try {
            String timestamp = new java.text.SimpleDateFormat("HH:mm:ss").format(new Date());
            logArea.append("[" + timestamp + "] " + message + "\n");
            logArea.setCaretPosition(logArea.getDocument().getLength());
        } catch (Exception e) {
            System.err.println("Error adding log: " + e.getMessage());
        }
    }
    
    private void exportResults() {
        if (isExiting) return;
        
        hostFrame.setAlwaysOnTop(false);
        
        JFileChooser fileChooser = new JFileChooser();
        fileChooser.setSelectedFile(new File("contest_results.json"));
        
        if (fileChooser.showSaveDialog(hostFrame) == JFileChooser.APPROVE_OPTION) {
            server.getContestData().exportToFile(fileChooser.getSelectedFile().getAbsolutePath());
            addLog("Results exported to: " + fileChooser.getSelectedFile().getName());
            JOptionPane.showMessageDialog(hostFrame, "Results exported successfully!");
        }
        
        hostFrame.setAlwaysOnTop(true);
    }
    
    private void stopContest() {
        if (isExiting) return;
        
        hostFrame.setAlwaysOnTop(false);
        
        int result = JOptionPane.showConfirmDialog(hostFrame,
            "Are you sure you want to stop the contest?",
            "Stop Contest",
            JOptionPane.YES_NO_OPTION);
            
        if (result == JOptionPane.YES_OPTION) {
            isExiting = true;
            cleanup();
            addLog("Contest stopped by host");
            
            SwingUtilities.invokeLater(() -> {
                if (hostFrame != null) {
                    hostFrame.setVisible(false);
                    hostFrame.dispose();
                }
                parent.setVisible(true);
                parent.toFront();
                parent.requestFocus();
                parent.showPanel("HOME");
            });
        } else {
            hostFrame.setAlwaysOnTop(true);
        }
    }
    
    private void goBack() {
        stopContest();
    }
    
    @Override
    public void cleanup() {
        isExiting = true;
        
        if (updateTimer != null && updateTimer.isRunning()) {
            updateTimer.stop();
        }
        if (server != null) {
            try {
                server.stopServer();
            } catch (Exception e) {
                System.err.println("Error stopping server: " + e.getMessage());
            }
        }
    }
}
