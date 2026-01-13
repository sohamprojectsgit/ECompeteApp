package com.mycompany.ecompeteapp;

import javax.swing.*;
import java.awt.*;

class CodeWorldGame extends AbstractGame {
    private int robotX, robotY;
    private int targetX, targetY;
    private int gridSize = 8;
    private JPanel gridPanel;
    private JTextArea codeArea;
    private JButton runBtn;
    private JLabel statusLabel;
    private boolean[][] obstacles;
    private volatile boolean isCleanedUp = false;
    private Thread executionThread;
    
    public CodeWorldGame(ClientGamePanel gamePanel) {
        super(gamePanel);
        setLayout(new BorderLayout(10, 10));
        setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        
        robotX = 0;
        robotY = 0;
        targetX = gridSize - 1;
        targetY = gridSize - 1;
        
        obstacles = new boolean[gridSize][gridSize];
        setupObstacles();
        
        createUI();
    }
    
    private void createUI() {
        // Header
        JPanel headerPanel = new JPanel(new BorderLayout(5, 5));
        
        JLabel titleLabel = new JLabel("Guide the robot to the target!", SwingConstants.CENTER);
        titleLabel.setFont(new Font("Arial", Font.BOLD, 16));
        titleLabel.setForeground(new Color(0, 100, 200));
        
        statusLabel = new JLabel("Commands: moveRight(), moveDown(), moveLeft(), moveUp()", SwingConstants.CENTER);
        statusLabel.setFont(new Font("Arial", Font.PLAIN, 13));
        
        headerPanel.add(titleLabel, BorderLayout.NORTH);
        headerPanel.add(statusLabel, BorderLayout.SOUTH);
        
        // Grid panel
        gridPanel = new JPanel(new GridLayout(gridSize, gridSize, 2, 2));
        gridPanel.setPreferredSize(new Dimension(400, 400));
        gridPanel.setBorder(BorderFactory.createTitledBorder("World Grid (R=Robot, T=Target, X=Obstacle)"));
        updateGrid();
        
        // Code editor
        JPanel codePanel = new JPanel(new BorderLayout(5, 5));
        codePanel.setBorder(BorderFactory.createTitledBorder("Write Your Commands"));
        
        codeArea = new JTextArea(15, 30);
        codeArea.setFont(new Font("Monospaced", Font.PLAIN, 13));
        codeArea.setTabSize(4);
        codeArea.setText("// Write your commands here\n// Example:\n// moveRight();\n// moveDown();\n\n");
        JScrollPane codeScroll = new JScrollPane(codeArea);
        
        runBtn = new JButton("Run Code");
        runBtn.setFont(new Font("Arial", Font.BOLD, 14));
        runBtn.addActionListener(e -> executeCode());
        
        codePanel.add(codeScroll, BorderLayout.CENTER);
        codePanel.add(runBtn, BorderLayout.SOUTH);
        
        // Layout
        add(headerPanel, BorderLayout.NORTH);
        add(gridPanel, BorderLayout.WEST);
        add(codePanel, BorderLayout.CENTER);
    }
    
    private void setupObstacles() {
        obstacles[2][2] = true;
        obstacles[3][3] = true;
        obstacles[4][1] = true;
        obstacles[5][5] = true;
        obstacles[1][4] = true;
        obstacles[6][2] = true;
    }
    
    private void updateGrid() {
        if (isCleanedUp) return;
        
        gridPanel.removeAll();
        
        for (int y = 0; y < gridSize; y++) {
            for (int x = 0; x < gridSize; x++) {
                JPanel cell = new JPanel();
                cell.setBorder(BorderFactory.createLineBorder(Color.GRAY));
                cell.setLayout(new GridBagLayout());
                
                JLabel label = new JLabel();
                label.setFont(new Font("Arial", Font.BOLD, 20));
                
                if (x == robotX && y == robotY) {
                    cell.setBackground(new Color(144, 238, 144));
                    label.setText("R");
                    label.setForeground(Color.BLUE);
                } else if (x == targetX && y == targetY) {
                    cell.setBackground(new Color(255, 215, 0));
                    label.setText("T");
                    label.setForeground(Color.RED);
                } else if (obstacles[y][x]) {
                    cell.setBackground(new Color(128, 128, 128));
                    label.setText("X");
                    label.setForeground(Color.WHITE);
                } else {
                    cell.setBackground(Color.WHITE);
                }
                
                cell.add(label);
                gridPanel.add(cell);
            }
        }
        
        gridPanel.revalidate();
        gridPanel.repaint();
    }
    
    private void executeCode() {
        if (isCleanedUp) return;
        
        runBtn.setEnabled(false);
        statusLabel.setText("Executing...");
        
        executionThread = new Thread(() -> {
            String code = codeArea.getText();
            robotX = 0;
            robotY = 0;
            
            SwingUtilities.invokeLater(() -> updateGrid());
            
            String[] lines = code.split("\n");
            for (String line : lines) {
                if (isCleanedUp || Thread.currentThread().isInterrupted()) break;
                
                line = line.trim();
                if (line.startsWith("//") || line.isEmpty()) continue;
                
                if (line.contains("moveRight")) {
                    moveRobot(1, 0);
                } else if (line.contains("moveDown")) {
                    moveRobot(0, 1);
                } else if (line.contains("moveLeft")) {
                    moveRobot(-1, 0);
                } else if (line.contains("moveUp")) {
                    moveRobot(0, -1);
                }
                
                SwingUtilities.invokeLater(() -> updateGrid());
                
                try {
                    Thread.sleep(400);
                } catch (InterruptedException e) {
                    break;
                }
            }
            
            if (!isCleanedUp) {
                SwingUtilities.invokeLater(() -> {
                    checkWin();
                    runBtn.setEnabled(true);
                    statusLabel.setText("Commands: moveRight(), moveDown(), moveLeft(), moveUp()");
                });
            }
        }, "Code-Execution-Thread");
        
        executionThread.start();
    }
    
    private void moveRobot(int dx, int dy) {
        int newX = robotX + dx;
        int newY = robotY + dy;
        
        if (newX >= 0 && newX < gridSize && newY >= 0 && newY < gridSize) {
            if (!obstacles[newY][newX]) {
                robotX = newX;
                robotY = newY;
            }
        }
    }
    
    private void checkWin() {
        if (isCleanedUp) return;
        
        if (robotX == targetX && robotY == targetY) {
            JOptionPane.showMessageDialog(this, 
                "Success! Robot reached the target!\n\n+10 points", 
                "Victory", 
                JOptionPane.INFORMATION_MESSAGE);
            gamePanel.submitAnswer(0, "completed", true);
            gamePanel.completeGame();
        } else {
            JOptionPane.showMessageDialog(this, 
                "Try again! Robot didn't reach the target.\n\n" +
                "Current position: (" + robotX + ", " + robotY + ")\n" +
                "Target position: (" + targetX + ", " + targetY + ")", 
                "Not There Yet", 
                JOptionPane.WARNING_MESSAGE);
        }
    }
    
    @Override
    public void cleanup() {
        if (isCleanedUp) return;
        isCleanedUp = true;
        
        // Stop execution thread
        if (executionThread != null && executionThread.isAlive()) {
            executionThread.interrupt();
            try {
                executionThread.join(500);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }
        
        if (runBtn != null) {
            runBtn.setEnabled(false);
            for (var listener : runBtn.getActionListeners()) {
                runBtn.removeActionListener(listener);
            }
        }
        
        if (codeArea != null) {
            codeArea.setEnabled(false);
        }
        
        obstacles = null;
        System.out.println("CodeWorldGame cleaned up");
    }
}