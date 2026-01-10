import javax.swing.*;
import java.awt.*;
import java.awt.event.*;

class ClientGamePanel extends GamePanel implements ClientMessageListener {
    private ContestClient client;
    private JPanel gameContentPanel;
    private JLabel timerLabel;
    private JLabel scoreLabel;
    private Timer countdownTimer;
    private long startTime;
    private int timeLimit;
    private int currentScore;
    private AbstractGame currentGame;
    private JFrame gameFrame;
    
    public ClientGamePanel(MainFrame parent, ContestClient client) {
        super(parent);
        this.client = client;
        this.client.setMessageListener(this);
        this.timeLimit = client.getTimeLimit() * 60; // Convert to seconds
        this.startTime = System.currentTimeMillis();
        this.currentScore = 0;
        
        // Create fullscreen window
        createFullscreenWindow();
    }
    
    private void createFullscreenWindow() {
        gameFrame = new JFrame("E-Compete - Contest");
        gameFrame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        gameFrame.setUndecorated(true);
        gameFrame.setExtendedState(JFrame.MAXIMIZED_BOTH);
        gameFrame.setAlwaysOnTop(true);
        
        // Prevent Alt+Tab and other window switching (keeps focus)
        gameFrame.setFocusableWindowState(true);
        
        // Add window listener to prevent closing except Alt+F4
        gameFrame.addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                gameFrame.setAlwaysOnTop(false);
                confirmExit();
            }
            
            @Override
            public void windowDeactivated(WindowEvent e) {
                // Immediately regain focus if user tries to switch windows
                SwingUtilities.invokeLater(() -> {
                    gameFrame.requestFocus();
                    gameFrame.toFront();
                });
            }
        });
        
        // Global key listener for Alt+F4
        KeyboardFocusManager.getCurrentKeyboardFocusManager().addKeyEventDispatcher(new KeyEventDispatcher() {
            @Override
            public boolean dispatchKeyEvent(KeyEvent e) {
                if (e.getID() == KeyEvent.KEY_PRESSED) {
                    if (e.getKeyCode() == KeyEvent.VK_F4 && e.isAltDown()) {
                        confirmExit();
                        return true;
                    }
                }
                return false;
            }
        });
        
        JPanel mainPanel = new JPanel(new BorderLayout());
        mainPanel.setBackground(Color.WHITE);
        
        // Header with timer, score, and close button
        JPanel headerPanel = new JPanel(new BorderLayout());
        headerPanel.setBackground(new Color(30, 144, 255));
        headerPanel.setPreferredSize(new Dimension(900, 60));
        
        timerLabel = new JLabel("Time: " + formatTime(timeLimit));
        timerLabel.setFont(new Font("Arial", Font.BOLD, 24));
        timerLabel.setForeground(Color.BLACK);
        timerLabel.setHorizontalAlignment(SwingConstants.CENTER);
        
        scoreLabel = new JLabel("Score: 0");
        scoreLabel.setFont(new Font("Arial", Font.BOLD, 20));
        scoreLabel.setForeground(Color.BLACK);
        scoreLabel.setBorder(BorderFactory.createEmptyBorder(0, 20, 0, 0));
        
        JButton closeBtn = new JButton("Exit Contest");
        closeBtn.setBackground(new Color(220, 20, 60));
        closeBtn.setForeground(Color.BLACK);
        closeBtn.setFont(new Font("Arial", Font.BOLD, 14));
        closeBtn.setFocusPainted(false);
        closeBtn.setPreferredSize(new Dimension(130, 40));
        closeBtn.addActionListener(e -> confirmExit());
        
        JPanel leftPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        leftPanel.setBackground(new Color(30, 144, 255));
        leftPanel.add(scoreLabel);
        
        JPanel rightPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        rightPanel.setBackground(new Color(30, 144, 255));
        rightPanel.add(closeBtn);
        
        headerPanel.add(leftPanel, BorderLayout.WEST);
        headerPanel.add(timerLabel, BorderLayout.CENTER);
        headerPanel.add(rightPanel, BorderLayout.EAST);
        
        // Game content area
        gameContentPanel = new JPanel(new BorderLayout());
        gameContentPanel.setBackground(Color.WHITE);
        
        // Load appropriate game
        loadGame(client.getGameType());
        
        mainPanel.add(headerPanel, BorderLayout.NORTH);
        mainPanel.add(gameContentPanel, BorderLayout.CENTER);
        
        gameFrame.add(mainPanel);
        gameFrame.setVisible(true);
        gameFrame.requestFocus();
        gameFrame.toFront();
        
        // Start countdown
        startCountdown();
    }
    
    private void confirmExit() {
        int result = JOptionPane.showConfirmDialog(
            gameFrame,
            "Are you sure you want to exit the contest?\nYour progress will be saved.",
            "Exit Contest",
            JOptionPane.YES_NO_OPTION,
            JOptionPane.WARNING_MESSAGE
        );
        
        if (result == JOptionPane.YES_OPTION) {
            exitContest();
        }
    }
    
    private void exitContest() {
        if (countdownTimer != null) {
            countdownTimer.stop();
        }
        
        long completionTime = System.currentTimeMillis() - startTime;
        client.sendCompletion(currentScore, completionTime);
        
        cleanup();
        
        if (gameFrame != null) {
            gameFrame.dispose();
        }
        gameFrame.dispose();
        parent.showPanel("HOME");
    }
    
    private void loadGame(String gameType) {
        switch (gameType) {
            case "Quiz Challenge":
                currentGame = new QuizGame(this);
                break;
            case "Debug Hunt":
                currentGame = new DebugGame(this);
                break;
            case "Problem Solver":
                currentGame = new ProblemSolverGame(this);
                break;
            case "Code World":
                currentGame = new CodeWorldGame(this);
                break;
        }
        
        if (currentGame != null) {
            gameContentPanel.add(currentGame, BorderLayout.CENTER);
        }
    }
    
    private void startCountdown() {
        countdownTimer = new Timer(1000, e -> {
            long elapsed = (System.currentTimeMillis() - startTime) / 1000;
            long remaining = timeLimit - elapsed;
            
            if (remaining <= 0) {
                endGame();
            } else {
                timerLabel.setText("Time: " + formatTime((int)remaining));
                
                if (remaining <= 60) {
                    timerLabel.setForeground(Color.RED);
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
        currentScore += points;
        scoreLabel.setText("Score: " + currentScore);
    }
    
    public void submitAnswer(int questionId, String answer, boolean correct) {
        client.sendAnswer(questionId, answer, correct);
        if (correct) {
            updateScore(10);
        }
    }
    
    private void endGame() {
        if (countdownTimer != null) {
            countdownTimer.stop();
        }
        
        long completionTime = System.currentTimeMillis() - startTime;
        client.sendCompletion(currentScore, completionTime);
        
        JOptionPane.showMessageDialog(gameFrame,
            "Contest Completed!\nYour Score: " + currentScore +
            "\nTime: " + formatTime((int)(completionTime / 1000)),
            "Results",
            JOptionPane.INFORMATION_MESSAGE);
        
        cleanup();
        
        if (gameFrame != null) {
            gameFrame.dispose();
        }
        
        parent.showPanel("HOME");
    }
    
    public void completeGame() {
        endGame();
    }
    
    @Override
    public void onMessageReceived(String message) {
        // Handle server messages if needed
        System.out.println("Server message: " + message);
    }
    
    @Override
    public void cleanup() {
        if (countdownTimer != null) {
            countdownTimer.stop();
        }
        if (client != null) {
            client.disconnect();
        }
        if (currentGame != null) {
            currentGame.cleanup();
        }
    }
}