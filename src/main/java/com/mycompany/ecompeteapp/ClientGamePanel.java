import javax.swing.*;
import java.awt.*;
import java.awt.event.*;

class ClientGamePanel extends GamePanel implements ClientMessageListener {
    private final ContestClient client;
    private JPanel gameContentPanel;
    private JLabel timerLabel;
    private JLabel scoreLabel;
    private JLabel warningLabel;
    private Timer countdownTimer;
    private Timer focusMonitorTimer;
    private final long startTime;
    private final int timeLimit;
    private int currentScore;
    private AbstractGame currentGame;
    private JFrame gameFrame;
    private volatile boolean isExiting = false;
    private int warningCount = 0;
    private boolean wasMinimized = false;
    private static final int MAX_WARNINGS = 3;
    
    public ClientGamePanel(MainFrame parent, ContestClient client) {
        super(parent);
        this.client = client;
        this.client.setMessageListener(this);
        this.timeLimit = client.getTimeLimit() * 60;
        this.startTime = System.currentTimeMillis();
        this.currentScore = 0;
        
        // Hide parent immediately
        parent.setVisible(false);
        
        // Create game window
        SwingUtilities.invokeLater(this::createGameWindow);
    }
    
    private void createGameWindow() {
        gameFrame = new JFrame("E-Compete - Contest (Full Screen Mode - Do Not Minimize)");
        gameFrame.setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
        
        // ANTI-CHEAT: Keep window undecorated and fullscreen
        gameFrame.setUndecorated(true);
        gameFrame.setExtendedState(JFrame.MAXIMIZED_BOTH);
        gameFrame.setResizable(false);
        gameFrame.setAlwaysOnTop(true); // Prevent switching to other apps
        
        // Window close handler
        gameFrame.addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                handleExit();
            }
            
            @Override
            public void windowDeactivated(WindowEvent e) {
                // ANTI-CHEAT: User tried to switch windows
                handleWindowFocusLost();
            }
            
            @Override
            public void windowIconified(WindowEvent e) {
                // ANTI-CHEAT: User tried to minimize
                handleMinimizeAttempt();
            }
        });
        
        // ANTI-CHEAT: Monitor window state changes
        gameFrame.addWindowStateListener(e -> {
            if ((e.getNewState() & Frame.ICONIFIED) != 0) {
                handleMinimizeAttempt();
            }
        });
        
        // ANTI-CHEAT: Keyboard shortcuts disabled
        gameFrame.getRootPane().registerKeyboardAction(
            e -> handleExit(),
            KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0),
            JComponent.WHEN_IN_FOCUSED_WINDOW
        );
        
        // ANTI-CHEAT: Block common shortcuts
        blockCheatingShortcuts();
        
        // Build UI
        JPanel mainPanel = new JPanel(new BorderLayout());
        
        // Header with score, timer, and warning
        JPanel headerPanel = new JPanel(new BorderLayout(10, 5));
        headerPanel.setBorder(BorderFactory.createEmptyBorder(5, 10, 5, 10));
        headerPanel.setBackground(new Color(240, 240, 240));
        
        // Left side - Score
        scoreLabel = new JLabel("Score: 0");
        scoreLabel.setFont(new Font("Arial", Font.BOLD, 16));
        
        // Center - Timer and Warnings
        JPanel centerPanel = new JPanel(new BorderLayout());
        
        timerLabel = new JLabel("Time: " + formatTime(timeLimit), SwingConstants.CENTER);
        timerLabel.setFont(new Font("Arial", Font.BOLD, 20));
        timerLabel.setForeground(new Color(0, 100, 0));
        
        warningLabel = new JLabel("Warnings: 0/" + MAX_WARNINGS + " (Switching tabs/minimizing will count as cheating)", SwingConstants.CENTER);
        warningLabel.setFont(new Font("Arial", Font.BOLD, 12));
        warningLabel.setForeground(new Color(150, 0, 0));
        
        centerPanel.add(timerLabel, BorderLayout.NORTH);
        centerPanel.add(warningLabel, BorderLayout.SOUTH);
        
        // Right side - Exit button
        JButton exitBtn = new JButton("Exit Contest");
        exitBtn.setFont(new Font("Arial", Font.BOLD, 12));
        exitBtn.addActionListener(e -> handleExit());
        
        headerPanel.add(scoreLabel, BorderLayout.WEST);
        headerPanel.add(centerPanel, BorderLayout.CENTER);
        headerPanel.add(exitBtn, BorderLayout.EAST);
        
        // Game content area
        gameContentPanel = new JPanel(new BorderLayout());
        loadGame(client.getGameType());
        
        mainPanel.add(headerPanel, BorderLayout.NORTH);
        mainPanel.add(gameContentPanel, BorderLayout.CENTER);
        
        gameFrame.add(mainPanel);
        gameFrame.setVisible(true);
        
        // Force focus
        gameFrame.toFront();
        gameFrame.requestFocus();
        
        // Start timers
        startCountdown();
        startFocusMonitoring();
    }
    
    private void blockCheatingShortcuts() {
        // Block Alt+Tab, Windows Key, Alt+F4 (except we handle exit ourselves)
        KeyEventDispatcher keyEventDispatcher = e -> {
            if (isExiting) return false;
            
            // Block Alt+Tab
            if (e.getID() == KeyEvent.KEY_PRESSED && 
                e.getKeyCode() == KeyEvent.VK_TAB && 
                e.isAltDown()) {
                showCheatingWarning("Tab switching is not allowed!");
                return true; // Consume event
            }
            
            // Block Windows key
            if (e.getID() == KeyEvent.KEY_PRESSED && 
                e.getKeyCode() == KeyEvent.VK_WINDOWS) {
                showCheatingWarning("Windows key is disabled during contest!");
                return true;
            }
            
            // Block Alt+F4 (we handle exit with confirmation)
            if (e.getID() == KeyEvent.KEY_PRESSED && 
                e.getKeyCode() == KeyEvent.VK_F4 && 
                e.isAltDown()) {
                handleExit();
                return true;
            }
            
            // Block Ctrl+Alt+Del
            if (e.getKeyCode() == KeyEvent.VK_DELETE && 
                e.isControlDown() && e.isAltDown()) {
                showCheatingWarning("Task Manager is not allowed!");
                return true;
            }
            
            return false;
        };
        
        KeyboardFocusManager.getCurrentKeyboardFocusManager()
            .addKeyEventDispatcher(keyEventDispatcher);
    }
    
    private void startFocusMonitoring() {
        // Monitor focus every 500ms
        focusMonitorTimer = new Timer(500, e -> {
            if (isExiting) {
                ((Timer)e.getSource()).stop();
                return;
            }
            
            // Check if window is focused
            if (!gameFrame.isFocused() && gameFrame.isVisible()) {
                // Window lost focus - bring it back
                gameFrame.toFront();
                gameFrame.requestFocus();
            }
            
            // Check if window is minimized
            if ((gameFrame.getExtendedState() & JFrame.ICONIFIED) != 0) {
                handleMinimizeAttempt();
            }
        });
        focusMonitorTimer.start();
    }
    
    private void handleWindowFocusLost() {
        if (isExiting) return;
        
        SwingUtilities.invokeLater(() -> {
            gameFrame.toFront();
            gameFrame.requestFocus();
            gameFrame.setAlwaysOnTop(true);
            
            addWarning("Switching windows detected!");
        });
    }
    
    private void handleMinimizeAttempt() {
        if (isExiting || wasMinimized) return;
        wasMinimized = true;
        
        SwingUtilities.invokeLater(() -> {
            // Restore window immediately
            gameFrame.setExtendedState(JFrame.MAXIMIZED_BOTH);
            gameFrame.toFront();
            gameFrame.requestFocus();
            
            addWarning("Minimizing is not allowed!");
            
            wasMinimized = false;
        });
    }
    
    private void showCheatingWarning(String message) {
        SwingUtilities.invokeLater(() -> {
            JOptionPane.showMessageDialog(
                gameFrame,
                message + "\n\nThis action has been logged.",
                "Cheating Attempt Detected",
                JOptionPane.WARNING_MESSAGE);
            
            addWarning(message);
        });
    }
    
    private void addWarning(String reason) {
        if (isExiting) return;
        
        warningCount++;
        
        // Update warning display
        warningLabel.setText("Warnings: " + warningCount + "/" + MAX_WARNINGS + 
            " - Last: " + reason);
        
        if (warningCount >= MAX_WARNINGS / 2) {
            warningLabel.setForeground(Color.RED);
        }
        
        // Log to server
        if (client != null) {
            new Thread(() -> {
                try {
                    client.sendAnswer(-1, "CHEAT_WARNING: " + reason, false);
                } catch (Exception e) {
                    System.err.println("Error sending warning: " + e.getMessage());
                }
            }).start();
        }
        
        // Auto-exit after max warnings
        if (warningCount >= MAX_WARNINGS) {
            SwingUtilities.invokeLater(() -> {
                JOptionPane.showMessageDialog(
                    gameFrame,
                    "You have exceeded the maximum number of warnings.\n" +
                    "The contest will now be terminated.\n\n" +
                    "Your current score will be submitted.",
                    "Contest Terminated - Cheating",
                    JOptionPane.ERROR_MESSAGE);
                
                exitGame(true); // Force exit with score submission
            });
        }
    }
    
    private void loadGame(String gameType) {
        switch (gameType) {
            case "Quiz Challenge" -> currentGame = new QuizGame(this);
            case "Debug Hunt" -> currentGame = new DebugGame(this);
            case "Problem Solver" -> currentGame = new ProblemSolverGame(this);
            case "Code World" -> currentGame = new CodeWorldGame(this);
            default -> currentGame = new QuizGame(this);
        }
        
        if (currentGame != null) {
            gameContentPanel.add(currentGame, BorderLayout.CENTER);
        }
    }
    
    private void startCountdown() {
        countdownTimer = new Timer(1000, e -> {
            if (isExiting) {
                ((Timer)e.getSource()).stop();
                return;
            }
            
            long elapsed = (System.currentTimeMillis() - startTime) / 1000;
            long remaining = timeLimit - elapsed;
            
            if (remaining <= 0) {
                completeGame();
            } else {
                timerLabel.setText("Time: " + formatTime((int)remaining));
                
                if (remaining <= 60) {
                    timerLabel.setForeground(Color.RED);
                } else if (remaining <= 300) {
                    timerLabel.setForeground(new Color(200, 100, 0));
                }
            }
        });
        countdownTimer.start();
    }
    
    private String formatTime(int seconds) {
        int mins = seconds / 60;
        int secs = seconds % 60;
        return String.format("%02d:%02d", mins, secs);
    }
    
    public void updateScore(int points) {
        if (!isExiting) {
            SwingUtilities.invokeLater(() -> {
                currentScore += points;
                scoreLabel.setText("Score: " + currentScore);
            });
        }
    }
    
    public void submitAnswer(int questionId, String answer, boolean correct) {
        if (isExiting || client == null) return;
        
        new Thread(() -> {
            try {
                client.sendAnswer(questionId, answer, correct);
            } catch (Exception e) {
                System.err.println("Error sending answer: " + e.getMessage());
            }
        }, "Answer-Submit-Thread").start();
        
        if (correct) {
            updateScore(10);
        }
    }
    
    private void handleExit() {
        if (isExiting) return;
        
        SwingUtilities.invokeLater(() -> {
            if (isExiting) return;
            
            // Temporarily disable always-on-top for dialog
            gameFrame.setAlwaysOnTop(false);
            
            int result = JOptionPane.showConfirmDialog(
                gameFrame,
                "Are you sure you want to exit the contest?\n" +
                "Your progress will be saved.\n\n" +
                "Warnings: " + warningCount + "/" + MAX_WARNINGS,
                "Exit Contest",
                JOptionPane.YES_NO_OPTION,
                JOptionPane.WARNING_MESSAGE);
            
            if (result == JOptionPane.YES_OPTION) {
                exitGame(false);
            } else {
                gameFrame.setAlwaysOnTop(true);
            }
        });
    }
    
    public void completeGame() {
        if (isExiting) return;
        
        SwingUtilities.invokeLater(() -> {
            if (isExiting) return;
            
            gameFrame.setAlwaysOnTop(false);
            
            long completionTime = System.currentTimeMillis() - startTime;
            JOptionPane.showMessageDialog(
                gameFrame,
                "Contest Completed!\n\n" +
                "Your Score: " + currentScore + " points\n" +
                "Time Taken: " + formatTime((int)(completionTime / 1000)) + "\n" +
                "Warnings: " + warningCount + "/" + MAX_WARNINGS,
                "Contest Results",
                JOptionPane.INFORMATION_MESSAGE);
            
            exitGame(true);
        });
    }
    
    private void exitGame(boolean sendCompletion) {
        if (isExiting) return;
        isExiting = true;
        
        System.out.println("=== EXIT GAME STARTED ===");
        
        // Stop timers
        if (countdownTimer != null && countdownTimer.isRunning()) {
            countdownTimer.stop();
            countdownTimer = null;
        }
        
        if (focusMonitorTimer != null && focusMonitorTimer.isRunning()) {
            focusMonitorTimer.stop();
            focusMonitorTimer = null;
        }
        
        // Disable always-on-top
        gameFrame.setAlwaysOnTop(false);
        
        // Cleanup game
        if (currentGame != null) {
            try {
                currentGame.cleanup();
                gameContentPanel.removeAll();
            } catch (Exception e) {
                System.err.println("Error cleaning up game: " + e.getMessage());
            }
        }
        
        // Show home page FIRST
        showHomePage();
        
        // Background cleanup
        new Thread(() -> {
            try {
                if (sendCompletion && client != null) {
                    long completionTime = System.currentTimeMillis() - startTime;
                    try {
                        client.sendCompletion(currentScore, completionTime);
                        System.out.println("Completion sent (Warnings: " + warningCount + ")");
                    } catch (Exception e) {
                        System.err.println("Error sending completion: " + e.getMessage());
                    }
                }
                
                Thread.sleep(300);
                
                SwingUtilities.invokeLater(() -> {
                    if (gameFrame != null) {
                        gameFrame.setVisible(false);
                        gameFrame.dispose();
                        System.out.println("Game window disposed");
                    }
                });
                
                if (client != null) {
                    try {
                        client.disconnect();
                        System.out.println("Disconnected from server");
                    } catch (Exception e) {
                        System.err.println("Error disconnecting: " + e.getMessage());
                    }
                }
                
                System.out.println("=== EXIT COMPLETE ===");
                
            } catch (Exception e) {
                System.err.println("Error in background cleanup: " + e.getMessage());
            }
        }, "Exit-Cleanup-Thread").start();
    }
    
    private void showHomePage() {
        SwingUtilities.invokeLater(() -> {
            if (parent != null) {
                parent.setVisible(true);
                parent.setExtendedState(JFrame.NORMAL);
                parent.setState(JFrame.NORMAL);
                parent.toFront();
                parent.repaint();
                parent.requestFocus();
                parent.showPanel("HOME");
            }
        });
    }
    
    @Override
    public void onMessageReceived(String message) {
        if (!isExiting) {
            System.out.println("Server: " + message);
        }
    }
    
    @Override
    public void cleanup() {
        exitGame(false);
    }
}