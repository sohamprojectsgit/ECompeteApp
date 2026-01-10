import javax.swing.*;
import java.awt.*;
import java.awt.event.*;

class ClientGamePanel extends GamePanel implements ClientMessageListener {
    private final ContestClient client;
    private JPanel gameContentPanel;
    private JLabel timerLabel;
    private JLabel scoreLabel;
    private Timer countdownTimer;
    private final long startTime;
    private final int timeLimit;
    private int currentScore;
    private AbstractGame currentGame;
    private JFrame gameFrame;
    private volatile boolean isExiting = false;
    
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
        gameFrame = new JFrame("E-Compete - Contest");
        gameFrame.setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
        gameFrame.setUndecorated(false); // Keep window controls visible
        gameFrame.setExtendedState(JFrame.MAXIMIZED_BOTH);
        
        // Window close handler
        gameFrame.addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                handleExit();
            }
        });
        
        // Keyboard shortcuts for exit
        gameFrame.getRootPane().registerKeyboardAction(
            e -> handleExit(),
            KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0),
            JComponent.WHEN_IN_FOCUSED_WINDOW
        );
        
        // Build UI
        JPanel mainPanel = new JPanel(new BorderLayout());
        
        // Header with score and timer
        JPanel headerPanel = new JPanel(new BorderLayout(10, 5));
        headerPanel.setBorder(BorderFactory.createEmptyBorder(5, 10, 5, 10));
        headerPanel.setBackground(new Color(240, 240, 240));
        
        scoreLabel = new JLabel("Score: 0");
        scoreLabel.setFont(new Font("Arial", Font.BOLD, 16));
        
        timerLabel = new JLabel("Time: " + formatTime(timeLimit), SwingConstants.CENTER);
        timerLabel.setFont(new Font("Arial", Font.BOLD, 20));
        timerLabel.setForeground(new Color(0, 100, 0));
        
        JButton exitBtn = new JButton("Exit Contest");
        exitBtn.setFont(new Font("Arial", Font.BOLD, 12));
        exitBtn.addActionListener(e -> handleExit());
        
        headerPanel.add(scoreLabel, BorderLayout.WEST);
        headerPanel.add(timerLabel, BorderLayout.CENTER);
        headerPanel.add(exitBtn, BorderLayout.EAST);
        
        // Game content area
        gameContentPanel = new JPanel(new BorderLayout());
        loadGame(client.getGameType());
        
        mainPanel.add(headerPanel, BorderLayout.NORTH);
        mainPanel.add(gameContentPanel, BorderLayout.CENTER);
        
        gameFrame.add(mainPanel);
        gameFrame.setVisible(true);
        
        // Start countdown
        startCountdown();
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
                // Time up - game complete
                completeGame();
            } else {
                timerLabel.setText("Time: " + formatTime((int)remaining));
                
                // Warning colors
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
        
        // Send answer in background
        new Thread(() -> {
            try {
                client.sendAnswer(questionId, answer, correct);
            } catch (Exception e) {
                System.err.println("Error sending answer: " + e.getMessage());
            }
        }, "Answer-Submit-Thread").start();
        
        // Update score
        if (correct) {
            updateScore(10);
        }
    }
    
    // Called when user clicks exit or presses ESC
    private void handleExit() {
        if (isExiting) return;
        
        SwingUtilities.invokeLater(() -> {
            if (isExiting) return;
            
            int result = JOptionPane.showConfirmDialog(
                gameFrame,
                "Are you sure you want to exit the contest?\nYour progress will be saved.",
                "Exit Contest",
                JOptionPane.YES_NO_OPTION,
                JOptionPane.WARNING_MESSAGE);
            
            if (result == JOptionPane.YES_OPTION) {
                exitGame(false); // Early exit - no completion message
            }
        });
    }
    
    // Called when all questions completed or time runs out
    public void completeGame() {
        if (isExiting) return;
        
        SwingUtilities.invokeLater(() -> {
            if (isExiting) return;
            
            // Show completion dialog
            long completionTime = System.currentTimeMillis() - startTime;
            JOptionPane.showMessageDialog(
                gameFrame,
                "Contest Completed!\n\nYour Score: " + currentScore + " points" +
                "\nTime Taken: " + formatTime((int)(completionTime / 1000)),
                "Contest Results",
                JOptionPane.INFORMATION_MESSAGE);
            
            exitGame(true); // Normal completion
        });
    }
    
    // FLOWCHART: StopTimer → SendCompletion → ShowResults → ExitGame
    // ExitGame → ShowHomePage → DisposeGameWin → DisconnectClient → Home
    private void exitGame(boolean sendCompletion) {
        if (isExiting) return;
        isExiting = true;
        
        System.out.println("=== EXIT GAME STARTED ===");
        
        // Step 1: Stop Timer
        if (countdownTimer != null && countdownTimer.isRunning()) {
            countdownTimer.stop();
            countdownTimer = null;
            System.out.println("Timer stopped");
        }
        
        // Step 2: Cleanup game UI (disable interactions)
        if (currentGame != null) {
            try {
                currentGame.cleanup();
                gameContentPanel.removeAll();
                System.out.println("Game cleaned up");
            } catch (Exception e) {
                System.err.println("Error cleaning up game: " + e.getMessage());
            }
        }
        
        // Step 3: Show Home Page FIRST (as per flowchart)
        showHomePage();
        
        // Step 4: Background cleanup (dispose window + disconnect)
        new Thread(() -> {
            try {
                // Send completion if needed
                if (sendCompletion && client != null) {
                    long completionTime = System.currentTimeMillis() - startTime;
                    try {
                        client.sendCompletion(currentScore, completionTime);
                        System.out.println("Completion sent to server");
                    } catch (Exception e) {
                        System.err.println("Error sending completion: " + e.getMessage());
                    }
                }
                
                // Small delay to ensure home page is visible
                Thread.sleep(300);
                
                // Dispose game window in background
                SwingUtilities.invokeLater(() -> {
                    if (gameFrame != null) {
                        gameFrame.setVisible(false);
                        gameFrame.dispose();
                        System.out.println("Game window disposed");
                    }
                });
                
                // Disconnect from server
                if (client != null) {
                    try {
                        client.disconnect();
                        System.out.println("Disconnected from server");
                    } catch (Exception e) {
                        System.err.println("Error disconnecting: " + e.getMessage());
                    }
                }
                
                System.out.println("=== EXIT COMPLETE - HOME PAGE VISIBLE ===");
                
            } catch (Exception e) {
                System.err.println("Error in background cleanup: " + e.getMessage());
                e.printStackTrace();
            }
        }, "Exit-Cleanup-Thread").start();
    }
    
    // CRITICAL: Show home page IMMEDIATELY
    private void showHomePage() {
        SwingUtilities.invokeLater(() -> {
            if (parent != null) {
                System.out.println("Showing home page...");
                parent.setVisible(true);
                parent.setExtendedState(JFrame.NORMAL);
                parent.setState(JFrame.NORMAL);
                parent.toFront();
                parent.repaint();
                parent.requestFocus();
                parent.showPanel("HOME");
                System.out.println("Home page is now visible");
            } else {
                System.err.println("ERROR: Parent is null!");
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